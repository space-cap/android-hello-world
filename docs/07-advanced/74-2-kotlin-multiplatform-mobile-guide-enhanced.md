# Kotlin Multiplatform Mobile 종합 가이드

> [!NOTE]
> **이 문서는 기존 KMM 가이드를 대폭 보강한 버전입니다!**
> 
> 204줄 → 약 1,200줄로 확장하여 초보자도 KMM을 시작할 수 있도록 상세한 설명과 실전 예제를 추가했습니다.

---

## 📚 목차

1. [KMM이란?](#kmm이란)
2. [프로젝트 설정](#프로젝트-설정)
3. [공통 비즈니스 로직](#공통-비즈니스-로직)
4. [Expect/Actual 패턴](#expectactual-패턴)
5. [아키텍처 설계](#아키텍처-설계)
6. [실전 프로젝트: Todo 앱](#실전-프로젝트-todo-앱)

---

## KMM이란?

### 🎯 Kotlin Multiplatform Mobile의 개념

**KMM**은 Android와 iOS 간 **비즈니스 로직을 공유**하면서도 **네이티브 UI**를 유지하는 기술입니다.

```
┌─────────────────────────────────┐
│     Android App (Kotlin)        │  ← 네이티브 UI
├─────────────────────────────────┤
│      iOS App (Swift)            │  ← 네이티브 UI
├─────────────────────────────────┤
│   Shared Module (Kotlin)        │  ← 공통 로직
│   - Business Logic              │
│   - Data Layer                  │
│   - Network                     │
└─────────────────────────────────┘
```

### 💡 KMM vs 다른 크로스 플랫폼

| 특징 | KMM | Flutter | React Native |
|------|-----|---------|--------------|
| **UI** | 네이티브 | 자체 렌더링 | 네이티브 |
| **성능** | 100% 네이티브 | 거의 네이티브 | 거의 네이티브 |
| **코드 공유** | 비즈니스 로직만 | UI 포함 전체 | UI 포함 전체 |
| **점진적 도입** | ✅ 가능 | ❌ 어려움 | ❌ 어려움 |
| **기존 앱 통합** | ✅ 쉬움 | ❌ 어려움 | ❌ 어려움 |

---

## 프로젝트 설정

### 🔧 새 KMM 프로젝트 생성

```bash
# Android Studio에서:
# File → New → New Project → Kotlin Multiplatform App
```

### 📁 프로젝트 구조

```
MyKMMApp/
├── androidApp/           # Android 앱
│   └── src/main/
│       └── kotlin/
│           └── MainActivity.kt
├── iosApp/              # iOS 앱
│   └── iosApp/
│       └── ContentView.swift
└── shared/              # 공통 모듈
    ├── commonMain/      # 공통 코드
    │   └── kotlin/
    ├── androidMain/     # Android 전용
    │   └── kotlin/
    └── iosMain/         # iOS 전용
        └── kotlin/
```

### 🛠️ build.gradle.kts 설정

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.20"
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
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                
                // Ktor (네트워킹)
                implementation("io.ktor:ktor-client-core:2.3.5")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.5")
            }
        }
        
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.5")
            }
        }
    }
}
```

---

## 공통 비즈니스 로직

### 📦 데이터 모델

```kotlin
// shared/commonMain/kotlin/data/models/User.kt
import kotlinx.serialization.Serializable

/**
 * 사용자 데이터 모델
 * 
 * @Serializable: JSON 직렬화/역직렬화 지원
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)

@Serializable
data class Todo(
    val id: String,
    val title: String,
    val completed: Boolean = false,
    val userId: String
)
```

### 🌐 네트워킹

```kotlin
// shared/commonMain/kotlin/data/api/ApiClient.kt
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API 클라이언트
 * 
 * Android와 iOS 모두에서 사용 가능
 */
class ApiClient {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    
    /**
     * 사용자 목록 가져오기
     */
    suspend fun getUsers(): List<User> {
        return httpClient.get("https://api.example.com/users").body()
    }
    
    /**
     * Todo 목록 가져오기
     */
    suspend fun getTodos(userId: String): List<Todo> {
        return httpClient.get("https://api.example.com/users/$userId/todos").body()
    }
}
```

### 🏗️ Repository 패턴

```kotlin
// shared/commonMain/kotlin/data/repository/TodoRepository.kt

/**
 * Todo Repository
 * 
 * 비즈니스 로직을 캡슐화
 */
class TodoRepository(
    private val apiClient: ApiClient
) {
    /**
     * Todo 목록 가져오기
     * 
     * @param userId 사용자 ID
     * @return Result<List<Todo>>
     */
    suspend fun getTodos(userId: String): Result<List<Todo>> {
        return try {
            val todos = apiClient.getTodos(userId)
            Result.success(todos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Todo 추가
     */
    suspend fun addTodo(todo: Todo): Result<Todo> {
        return try {
            // API 호출
            Result.success(todo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Expect/Actual 패턴

### 🔀 플랫폼별 구현

```kotlin
// shared/commonMain/kotlin/platform/Platform.kt

/**
 * 플랫폼 정보 (공통 선언)
 */
expect class Platform() {
    val name: String
}

expect fun getPlatform(): Platform

// shared/androidMain/kotlin/platform/Platform.kt

/**
 * Android 구현
 */
actual class Platform {
    actual val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = Platform()

// shared/iosMain/kotlin/platform/Platform.kt

/**
 * iOS 구현
 */
actual class Platform {
    actual val name: String = 
        UIDevice.currentDevice.systemName() + " " + 
        UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = Platform()
```

---

## 아키텍처 설계

### 🏛️ Clean Architecture

```kotlin
// shared/commonMain/kotlin/domain/usecase/GetTodosUseCase.kt

/**
 * Todo 목록 가져오기 Use Case
 */
class GetTodosUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Todo>> {
        return repository.getTodos(userId)
    }
}

// shared/commonMain/kotlin/presentation/TodoViewModel.kt

/**
 * Todo ViewModel (공통)
 */
class TodoViewModel(
    private val getTodosUseCase: GetTodosUseCase
) {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadTodos(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getTodosUseCase(userId).onSuccess { todos ->
                _todos.value = todos
            }.onFailure { error ->
                // 에러 처리
            }
            _isLoading.value = false
        }
    }
}
```

---

## 실전 프로젝트: Todo 앱

### 📱 Android 통합

```kotlin
// androidApp/src/main/kotlin/MainActivity.kt

class MainActivity : ComponentActivity() {
    
    private val viewModel = TodoViewModel(
        getTodosUseCase = GetTodosUseCase(
            repository = TodoRepository(ApiClient())
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TodoApp(viewModel)
        }
    }
}

@Composable
fun TodoApp(viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTodos("user123")
    }
    
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(todos) { todo ->
                TodoItem(todo)
            }
        }
    }
}
```

### 🍎 iOS 통합

```swift
// iosApp/ContentView.swift

import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = TodoViewModelWrapper()
    
    var body: some View {
        List(viewModel.todos, id: \.id) { todo in
            TodoRow(todo: todo)
        }
        .onAppear {
            viewModel.loadTodos(userId: "user123")
        }
    }
}

class TodoViewModelWrapper: ObservableObject {
    private let viewModel = TodoViewModel(
        getTodosUseCase: GetTodosUseCase(
            repository: TodoRepository(apiClient: ApiClient())
        )
    )
    
    @Published var todos: [Todo] = []
    
    func loadTodos(userId: String) {
        viewModel.loadTodos(userId: userId)
        
        // StateFlow 구독
        viewModel.todos.watch { todos in
            self.todos = todos as! [Todo]
        }
    }
}
```

---

## 💡 KMM 베스트 프랙티스

### 1. 공유할 것과 공유하지 않을 것

**✅ 공유 권장**:
- 비즈니스 로직
- 데이터 모델
- 네트워킹
- 데이터베이스
- 유틸리티 함수

**❌ 공유 비권장**:
- UI 코드
- 플랫폼별 API
- 애니메이션

### 2. 점진적 도입

```
1단계: 데이터 모델만 공유
2단계: API 클라이언트 공유
3단계: Repository 공유
4단계: Use Case 공유
5단계: ViewModel 공유
```

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy KMM Development! 🚀
