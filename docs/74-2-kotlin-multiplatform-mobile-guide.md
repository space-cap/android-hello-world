# Kotlin Multiplatform Mobile 가이드

> [!NOTE]
> **이 문서는 보강된 종합 가이드로 대체되었습니다!**
> 
> **[👉 74-2. KMM 종합 가이드로 이동](./74-2-kotlin-multiplatform-mobile-guide-enhanced.md)**
> 
> 267줄 → 약 1,200줄로 확장하여 다음 내용을 추가했습니다:
> - 상세한 프로젝트 설정 가이드
> - Clean Architecture 적용
> - 실전 Todo 앱 예제
> - Android/iOS 통합 가이드
> - 베스트 프랙티스

---



**Kotlin Multiplatform Mobile (KMM)**은 Android와 iOS 간 코드를 공유할 수 있는 기술입니다.

### 장점
- 🔄 **코드 재사용**: 비즈니스 로직 공유
- 📱 **네이티브 UI**: 각 플랫폼 네이티브 UI 사용
- 🚀 **점진적 도입**: 기존 앱에 점진적 적용

### 아키텍처
```
┌─────────────────────────────────┐
│     Android App (Kotlin)        │
├─────────────────────────────────┤
│      iOS App (Swift)            │
├─────────────────────────────────┤
│   Shared Module (Kotlin)        │
│   - Business Logic              │
│   - Data Layer                  │
│   - Network                     │
└─────────────────────────────────┘
```

---

## 프로젝트 설정

### build.gradle.kts (shared 모듈)

```kotlin
plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // 공통 의존성
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Android 전용 의존성
            }
        }
        
        val iosMain by getting {
            dependencies {
                // iOS 전용 의존성
            }
        }
    }
}
```

---

## 공통 비즈니스 로직

```kotlin
/**
 * 공통 모듈 (commonMain)
 */
class UserRepository {
    
    /**
     * 사용자 정보 가져오기
     */
    suspend fun getUser(id: String): User {
        // 공통 로직
        return User(id, "홍길동", "hong@example.com")
    }
    
    /**
     * 사용자 목록
     */
    suspend fun getUsers(): List<User> {
        // Android와 iOS 모두에서 사용
        return listOf(
            User("1", "홍길동", "hong@example.com"),
            User("2", "김철수", "kim@example.com")
        )
    }
}

data class User(
    val id: String,
    val name: String,
    val email: String
)
```

---

## Expect/Actual

```kotlin
/**
 * commonMain - expect 선언
 */
expect class Platform() {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * androidMain - actual 구현
 */
actual class Platform {
    actual val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = Platform()

/**
 * iosMain - actual 구현
 */
actual class Platform {
    actual val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = Platform()
```

---

## 네이티브 상호운용

### Android에서 사용

```kotlin
/**
 * Android Activity
 */
class MainActivity : AppCompatActivity() {
    
    private val userRepository = UserRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val users = userRepository.getUsers()
            // UI 업데이트
        }
    }
}
```

### iOS에서 사용

```swift
/**
 * iOS ViewController
 */
class ViewController: UIViewController {
    
    let userRepository = UserRepository()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        userRepository.getUsers { users, error in
            // UI 업데이트
        }
    }
}
```

---

## SQLDelight

```kotlin
/**
 * SQLDelight 설정
 */
// build.gradle.kts
plugins {
    id("com.squareup.sqldelight")
}

sqldelight {
    database("AppDatabase") {
        packageName = "com.example.app.db"
    }
}

/**
 * SQL 스키마
 */
// User.sq
CREATE TABLE User (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL
);

selectAll:
SELECT * FROM User;

insert:
INSERT INTO User(id, name, email)
VALUES (?, ?, ?);
```

---

## Ktor

```kotlin
/**
 * Ktor HTTP 클라이언트
 */
class ApiClient {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    
    suspend fun getUsers(): List<User> {
        return client.get("https://api.example.com/users").body()
    }
}
```

---

## 참고 자료

- [KMM 공식 문서](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [KMM Samples](https://github.com/Kotlin/kmm-samples)
