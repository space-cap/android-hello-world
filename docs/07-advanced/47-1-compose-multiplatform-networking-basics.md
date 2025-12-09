# Compose Multiplatform 네트워킹 기초

> 📖 **시리즈 구성**
> - **47-1**: Compose Multiplatform 네트워킹 기초 (현재 문서) - Ktor Client, HTTP 요청
> - **47-2**: [Compose Multiplatform 네트워킹 고급](./47-2-compose-multiplatform-networking-advanced.md) - 인증, 캐싱, WebSocket
> - **47-3**: [Compose Multiplatform 네트워킹 플랫폼](./47-3-compose-multiplatform-networking-platform.md) - 플랫폼별 구현

---

## 📚 목차

1. [Ktor Client 소개](#ktor-client-소개)
2. [프로젝트 설정](#프로젝트-설정)
3. [HTTP 요청](#http-요청)
4. [JSON 직렬화](#json-직렬화)
5. [에러 처리](#에러-처리)

---

## Ktor Client 소개

### 🎯 Ktor Client란?

**Ktor Client**는 Kotlin Multiplatform을 위한 비동기 HTTP 클라이언트 라이브러리입니다.

```
Android ─┐
iOS      ├─→ Ktor Client (공통 코드) ─→ HTTP API
Desktop ─┘
```

### ✨ 주요 특징

```kotlin
/**
 * Ktor Client의 장점
 */

// ✅ 멀티플랫폼 지원
// - Android, iOS, Desktop, Web에서 동일한 코드 사용

// ✅ 코루틴 기반
// - suspend 함수로 비동기 처리

// ✅ 플러그인 시스템
// - 로깅, 인증, 재시도 등 기능 추가 가능

// ✅ 타입 안전
// - Kotlin Serialization과 통합
```

---

## 프로젝트 설정

### 📦 의존성 추가

#### 1단계: Kotlin Serialization 플러그인

**build.gradle.kts (프로젝트 루트)**:
```kotlin
plugins {
    // Kotlin Serialization 플러그인
    kotlin("plugin.serialization") version "1.9.21" apply false
}
```

**build.gradle.kts (모듈)**:
```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")  // Serialization 플러그인 적용
}
```

#### 2단계: Ktor 의존성

```kotlin
val ktorVersion = "2.3.7"

kotlin {
    sourceSets {
        // 공통 코드
        val commonMain by getting {
            dependencies {
                // Ktor Core - HTTP 클라이언트 핵심
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                
                // Content Negotiation - JSON 자동 변환
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                
                // Kotlinx JSON - JSON 직렬화
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                
                // Logging - 요청/응답 로깅
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                
                // Kotlinx Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }
        
        // Android
        val androidMain by getting {
            dependencies {
                // OkHttp 엔진 (Android 권장)
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        
        // iOS
        val iosMain by getting {
            dependencies {
                // Darwin 엔진 (iOS 전용)
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        
        // Desktop
        val desktopMain by getting {
            dependencies {
                // OkHttp 엔진 (Desktop)
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
    }
}
```

### 🔧 Ktor Client 생성

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP 클라이언트 생성 함수
 * 
 * 앱 전체에서 재사용할 수 있는 Ktor Client를 생성합니다.
 * 싱글톤 패턴으로 사용하는 것이 좋습니다.
 */
fun createHttpClient(): HttpClient {
    return HttpClient {
        
        // JSON 직렬화 설정
        install(ContentNegotiation) {
            json(Json {
                // JSON을 보기 좋게 출력 (디버깅용)
                prettyPrint = true
                
                // 엄격하지 않은 JSON 파싱 (따옴표 없는 키 허용 등)
                isLenient = true
                
                // 모델에 없는 JSON 필드 무시
                ignoreUnknownKeys = true
                
                // null 값을 가진 필드도 직렬화
                encodeDefaults = true
            })
        }
        
        // 로깅 설정
        install(Logging) {
            // 로거 설정
            logger = Logger.DEFAULT
            
            // 로그 레벨 설정
            // - NONE: 로그 없음
            // - INFO: 요청/응답 정보만
            // - HEADERS: 헤더 포함
            // - BODY: 바디 포함 (모든 정보)
            level = LogLevel.INFO
        }
    }
}
```

---

## HTTP 요청

### 📥 GET 요청

#### 기본 GET 요청

```kotlin
import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * 기본 GET 요청
 * 
 * 서버에서 데이터를 가져옵니다.
 */
suspend fun fetchData(): String {
    val client = createHttpClient()
    
    return try {
        // GET 요청 보내기
        val response: HttpResponse = client.get("https://api.example.com/data")
        
        // 응답을 문자열로 변환
        response.bodyAsText()
        
    } finally {
        // 클라이언트 종료 (리소스 해제)
        client.close()
    }
}
```

#### 쿼리 파라미터 추가

```kotlin
import io.ktor.client.request.*

/**
 * 쿼리 파라미터가 있는 GET 요청
 * 
 * URL: https://api.example.com/users?page=1&limit=20
 */
suspend fun fetchUsers(page: Int, limit: Int): String {
    val client = createHttpClient()
    
    return try {
        client.get("https://api.example.com/users") {
            // 쿼리 파라미터 추가
            parameter("page", page)
            parameter("limit", limit)
        }.bodyAsText()
        
    } finally {
        client.close()
    }
}
```

#### 헤더 추가

```kotlin
/**
 * 헤더가 있는 GET 요청
 * 
 * Authorization 헤더를 추가하여 인증된 요청을 보냅니다.
 */
suspend fun fetchProtectedData(token: String): String {
    val client = createHttpClient()
    
    return try {
        client.get("https://api.example.com/protected") {
            // 헤더 추가
            header("Authorization", "Bearer $token")
            header("Accept", "application/json")
        }.bodyAsText()
        
    } finally {
        client.close()
    }
}
```

### 📤 POST 요청

#### JSON 바디와 함께 POST

```kotlin
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * POST 요청으로 데이터 전송
 * 
 * JSON 바디를 서버로 전송합니다.
 */
suspend fun createUser(name: String, email: String): String {
    val client = createHttpClient()
    
    return try {
        client.post("https://api.example.com/users") {
            // Content-Type 설정
            contentType(ContentType.Application.Json)
            
            // JSON 바디 설정 (Map 사용)
            setBody(mapOf(
                "name" to name,
                "email" to email
            ))
        }.bodyAsText()
        
    } finally {
        client.close()
    }
}
```

### 🔄 PUT 요청

```kotlin
/**
 * PUT 요청으로 데이터 업데이트
 */
suspend fun updateUser(userId: Int, name: String, email: String): String {
    val client = createHttpClient()
    
    return try {
        client.put("https://api.example.com/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "name" to name,
                "email" to email
            ))
        }.bodyAsText()
        
    } finally {
        client.close()
    }
}
```

### 🗑️ DELETE 요청

```kotlin
/**
 * DELETE 요청으로 데이터 삭제
 */
suspend fun deleteUser(userId: Int): String {
    val client = createHttpClient()
    
    return try {
        client.delete("https://api.example.com/users/$userId").bodyAsText()
        
    } finally {
        client.close()
    }
}
```

---

## JSON 직렬화

### 📋 데이터 모델 정의

```kotlin
import kotlinx.serialization.Serializable

/**
 * User 데이터 모델
 * 
 * @Serializable: Kotlinx Serialization이 자동으로 직렬화/역직렬화
 */
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null  // null 가능한 필드
)

/**
 * Post 데이터 모델
 */
@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

/**
 * API 응답 래퍼
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
```

### 🔄 자동 직렬화/역직렬화

```kotlin
import io.ktor.client.call.*

/**
 * JSON을 User 객체로 자동 변환
 * 
 * ContentNegotiation 플러그인이 자동으로 변환합니다.
 */
suspend fun getUser(userId: Int): User {
    val client = createHttpClient()
    
    return try {
        // .body<User>()가 JSON을 User 객체로 자동 변환
        client.get("https://jsonplaceholder.typicode.com/users/$userId")
            .body<User>()
        
    } finally {
        client.close()
    }
}

/**
 * User 리스트 가져오기
 */
suspend fun getUsers(): List<User> {
    val client = createHttpClient()
    
    return try {
        client.get("https://jsonplaceholder.typicode.com/users")
            .body<List<User>>()
        
    } finally {
        client.close()
    }
}

/**
 * User 객체를 JSON으로 자동 변환하여 전송
 */
suspend fun createUser(user: User): User {
    val client = createHttpClient()
    
    return try {
        client.post("https://jsonplaceholder.typicode.com/users") {
            contentType(ContentType.Application.Json)
            // User 객체가 자동으로 JSON으로 변환됨
            setBody(user)
        }.body<User>()
        
    } finally {
        client.close()
    }
}
```

### 🏷️ 커스텀 필드 이름

```kotlin
import kotlinx.serialization.SerialName

/**
 * JSON 필드 이름과 Kotlin 프로퍼티 이름이 다를 때
 * 
 * @SerialName으로 JSON 필드 이름 지정
 */
@Serializable
data class Product(
    val id: Int,
    
    // JSON: "product_name" → Kotlin: productName
    @SerialName("product_name")
    val productName: String,
    
    // JSON: "unit_price" → Kotlin: unitPrice
    @SerialName("unit_price")
    val unitPrice: Double,
    
    // JSON: "created_at" → Kotlin: createdAt
    @SerialName("created_at")
    val createdAt: String
)
```

---

## 에러 처리

### 🛡️ Result 패턴

```kotlin
/**
 * Result 타입으로 성공/실패 표현
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * 에러 처리가 포함된 API 호출
 */
suspend fun fetchUsersWithErrorHandling(): ApiResult<List<User>> {
    val client = createHttpClient()
    
    return try {
        val users = client.get("https://jsonplaceholder.typicode.com/users")
            .body<List<User>>()
        
        ApiResult.Success(users)
        
    } catch (e: Exception) {
        // 에러 메시지 추출
        val message = e.message ?: "알 수 없는 에러"
        ApiResult.Error(message)
        
    } finally {
        client.close()
    }
}
```

### 🔍 HTTP 상태 코드 확인

```kotlin
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * HTTP 상태 코드에 따른 에러 처리
 */
suspend fun fetchUserWithStatusCheck(userId: Int): ApiResult<User> {
    val client = createHttpClient()
    
    return try {
        val response: HttpResponse = client.get("https://api.example.com/users/$userId")
        
        when (response.status) {
            HttpStatusCode.OK -> {
                // 200: 성공
                val user = response.body<User>()
                ApiResult.Success(user)
            }
            HttpStatusCode.NotFound -> {
                // 404: 찾을 수 없음
                ApiResult.Error("사용자를 찾을 수 없습니다", 404)
            }
            HttpStatusCode.Unauthorized -> {
                // 401: 인증 필요
                ApiResult.Error("로그인이 필요합니다", 401)
            }
            HttpStatusCode.InternalServerError -> {
                // 500: 서버 에러
                ApiResult.Error("서버 에러가 발생했습니다", 500)
            }
            else -> {
                ApiResult.Error("에러: ${response.status.value}", response.status.value)
            }
        }
        
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "네트워크 에러")
        
    } finally {
        client.close()
    }
}
```

### 🔄 재시도 로직

```kotlin
import kotlinx.coroutines.delay

/**
 * 실패 시 재시도하는 API 호출
 * 
 * @param maxRetries 최대 재시도 횟수
 * @param delayMillis 재시도 간격 (밀리초)
 */
suspend fun <T> retryRequest(
    maxRetries: Int = 3,
    delayMillis: Long = 1000,
    block: suspend () -> T
): T {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            
            if (attempt < maxRetries - 1) {
                // 마지막 시도가 아니면 대기 후 재시도
                delay(delayMillis * (attempt + 1))  // 지수 백오프
            }
        }
    }
    
    // 모든 재시도 실패
    throw lastException ?: Exception("재시도 실패")
}

/**
 * 재시도 로직 사용 예시
 */
suspend fun fetchUsersWithRetry(): ApiResult<List<User>> {
    return try {
        val users = retryRequest(maxRetries = 3, delayMillis = 1000) {
            val client = createHttpClient()
            try {
                client.get("https://jsonplaceholder.typicode.com/users")
                    .body<List<User>>()
            } finally {
                client.close()
            }
        }
        
        ApiResult.Success(users)
        
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "재시도 후에도 실패")
    }
}
```

---

## 💡 베스트 프랙티스

### 1. HttpClient 재사용

```kotlin
// ❌ 나쁜 예: 매번 새 클라이언트 생성
suspend fun badExample() {
    val client1 = createHttpClient()
    client1.get("...")
    client1.close()
    
    val client2 = createHttpClient()
    client2.get("...")
    client2.close()
}

// ✅ 좋은 예: 싱글톤 패턴
object HttpClientProvider {
    val client: HttpClient by lazy { createHttpClient() }
}

suspend fun goodExample() {
    HttpClientProvider.client.get("...")
    HttpClientProvider.client.get("...")
}
```

### 2. 타임아웃 설정

```kotlin
import io.ktor.client.plugins.*

fun createHttpClient(): HttpClient {
    return HttpClient {
        install(HttpTimeout) {
            // 요청 타임아웃: 30초
            requestTimeoutMillis = 30_000
            
            // 연결 타임아웃: 10초
            connectTimeoutMillis = 10_000
            
            // 소켓 타임아웃: 30초
            socketTimeoutMillis = 30_000
        }
        
        // ... 기타 설정
    }
}
```

### 3. Repository 패턴

```kotlin
/**
 * Repository 패턴으로 API 호출 캡슐화
 */
class UserRepository(private val client: HttpClient) {
    
    suspend fun getUsers(): ApiResult<List<User>> {
        return try {
            val users = client.get("https://api.example.com/users")
                .body<List<User>>()
            ApiResult.Success(users)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "에러 발생")
        }
    }
    
    suspend fun getUser(id: Int): ApiResult<User> {
        return try {
            val user = client.get("https://api.example.com/users/$id")
                .body<User>()
            ApiResult.Success(user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "에러 발생")
        }
    }
}
```

---

## 🎯 다음 단계

네트워킹 기초를 마스터했습니다! 다음 단계로:

1. **[47-2. Compose Multiplatform 네트워킹 고급](./47-2-compose-multiplatform-networking-advanced.md)** - 인증, 캐싱, WebSocket
2. **[47-3. Compose Multiplatform 네트워킹 플랫폼](./47-3-compose-multiplatform-networking-platform.md)** - 플랫폼별 구현

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
