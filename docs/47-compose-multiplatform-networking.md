# Compose Multiplatform 네트워킹 및 데이터베이스

## 목차
1. [Ktor Client 소개](#ktor-client-소개)
2. [HTTP 요청](#http-요청)
3. [JSON 파싱](#json-파싱)
4. [에러 처리](#에러-처리)
5. [SQLDelight 데이터베이스](#sqldelight-데이터베이스)
6. [실전 예제: REST API 앱](#실전-예제-rest-api-앱)

---

## Ktor Client 소개

**Ktor Client**는 Kotlin Multiplatform을 위한 HTTP 클라이언트 라이브러리입니다.

### 의존성 추가

**build.gradle.kts**:
```kotlin
val ktorVersion = "2.3.7"

commonMain.dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    
    // JSON 직렬화
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    
    // 로깅
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

androidMain.dependencies {
    // Android 엔진
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
}

iosMain.dependencies {
    // iOS 엔진
    implementation("io.ktor:ktor-client-darwin:$ktorVersion")
}

desktopMain.dependencies {
    // Desktop 엔진
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
}
```

### Ktor Client 설정

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Ktor HTTP Client 생성
 */
fun createHttpClient(): HttpClient {
    return HttpClient {
        // JSON 직렬화 설정
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        // 로깅 설정
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}
```

---

## HTTP 요청

### GET 요청

```kotlin
import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * GET 요청 예제
 */
suspend fun fetchData(): String {
    val client = createHttpClient()
    
    return try {
        val response: HttpResponse = client.get("https://api.example.com/data")
        response.bodyAsText()
    } finally {
        client.close()
    }
}
```

### POST 요청

```kotlin
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * POST 요청 예제
 */
suspend fun createUser(user: User): User {
    val client = createHttpClient()
    
    return try {
        client.post("https://api.example.com/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
    } finally {
        client.close()
    }
}
```

---

## JSON 파싱

### 데이터 모델

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
```

### API 호출

```kotlin
import io.ktor.client.call.*

suspend fun getUsers(): List<User> {
    val client = createHttpClient()
    
    return try {
        client.get("https://jsonplaceholder.typicode.com/users").body()
    } finally {
        client.close()
    }
}
```

---

## 에러 처리

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

suspend fun fetchUsersWithErrorHandling(): Result<List<User>> {
    return try {
        Result.Loading
        val users = getUsers()
        Result.Success(users)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

---

## SQLDelight 데이터베이스

### 의존성 추가

```kotlin
plugins {
    id("app.cash.sqldelight") version "2.0.1"
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.app.db")
        }
    }
}

commonMain.dependencies {
    implementation("app.cash.sqldelight:runtime:2.0.1")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
}

androidMain.dependencies {
    implementation("app.cash.sqldelight:android-driver:2.0.1")
}

iosMain.dependencies {
    implementation("app.cash.sqldelight:native-driver:2.0.1")
}

desktopMain.dependencies {
    implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
}
```

### SQL 스키마 정의

**commonMain/sqldelight/com/example/app/db/User.sq**:
```sql
CREATE TABLE User (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    createdAt INTEGER NOT NULL
);

-- 모든 사용자 조회
selectAll:
SELECT * FROM User;

-- ID로 사용자 조회
selectById:
SELECT * FROM User WHERE id = ?;

-- 사용자 삽입
insert:
INSERT INTO User(name, email, createdAt)
VALUES (?, ?, ?);

-- 사용자 업데이트
update:
UPDATE User
SET name = ?, email = ?
WHERE id = ?;

-- 사용자 삭제
delete:
DELETE FROM User WHERE id = ?;
```

### 데이터베이스 드라이버 생성

```kotlin
import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// Android
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            AppDatabase.Schema,
            context,
            "app.db"
        )
    }
}

// iOS
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            AppDatabase.Schema,
            "app.db"
        )
    }
}

// Desktop
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:app.db")
            .also { AppDatabase.Schema.create(it) }
    }
}
```

---

## 실전 예제: REST API 앱

### Repository 패턴

```kotlin
class UserRepository(
    private val httpClient: HttpClient,
    private val database: AppDatabase
) {
    suspend fun fetchAndCacheUsers(): Result<List<User>> {
        return try {
            // API에서 데이터 가져오기
            val users = httpClient.get("https://api.example.com/users").body<List<User>>()
            
            // 데이터베이스에 저장
            users.forEach { user ->
                database.userQueries.insert(user.name, user.email, System.currentTimeMillis())
            }
            
            Result.Success(users)
        } catch (e: Exception) {
            // 에러 시 캐시된 데이터 반환
            val cachedUsers = database.userQueries.selectAll().executeAsList()
            if (cachedUsers.isNotEmpty()) {
                Result.Success(cachedUsers.map { /* 변환 */ })
            } else {
                Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

---

## 다음 단계

다음 문서에서는:
- **고급 기법**
- **성능 최적화**
- **테스팅**

를 다룹니다.
