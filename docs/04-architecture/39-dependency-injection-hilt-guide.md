# 39. Dependency Injection with Hilt 완벽 가이드

## 📚 목차
1. [Dependency Injection이란?](#dependency-injection이란)
2. [왜 DI가 필요한가?](#왜-di가-필요한가)
3. [Hilt 소개](#hilt-소개)
4. [Hilt 설정하기](#hilt-설정하기)
5. [기본 사용법](#기본-사용법)
6. [ViewModel에 DI 적용](#viewmodel에-di-적용)
7. [Repository 패턴과 DI](#repository-패턴과-di)
8. [고급 기능](#고급-기능)
9. [테스팅](#테스팅)
10. [완전한 예제 프로젝트](#완전한-예제-프로젝트)

---

## Dependency Injection이란?

### 🤔 쉬운 설명

**Dependency Injection (의존성 주입)** 은 객체가 필요한 것들을 직접 만들지 않고, 외부에서 받아오는 디자인 패턴입니다.

#### 일상 생활의 비유

```
❌ DI 없이 (직접 만들기):
당신이 커피를 마시고 싶을 때
→ 커피 원두를 직접 재배하고
→ 로스팅 기계를 만들고
→ 커피 머신을 제작하고
→ 그제서야 커피를 만든다

✅ DI 사용 (외부에서 받기):
당신이 커피를 마시고 싶을 때
→ 카페에 가서 "커피 주세요"라고 말한다
→ 카페가 커피를 만들어서 준다
```

### 코드로 비교하기

#### ❌ DI 없이 (나쁜 예)

```kotlin
// UserRepository를 직접 생성
class UserViewModel {
    // ViewModel이 Repository를 직접 만든다
    // 문제: 테스트하기 어렵고, 변경하기 어렵다
    private val repository = UserRepository()
    
    fun getUser() {
        repository.fetchUser()
    }
}
```

#### ✅ DI 사용 (좋은 예)

```kotlin
// UserRepository를 외부에서 받아온다
class UserViewModel(
    // 생성자를 통해 Repository를 받는다 (주입받는다)
    // 장점: 테스트할 때 가짜(Mock) Repository를 넣을 수 있다
    private val repository: UserRepository
) {
    fun getUser() {
        repository.fetchUser()
    }
}
```

---

## 왜 DI가 필요한가?

### 1️⃣ 테스트하기 쉽다

```kotlin
// DI 없이: 테스트 불가능
class ViewModel {
    private val api = RealApiService() // 항상 진짜 서버 호출
}

// DI 사용: 테스트 가능
class ViewModel(private val api: ApiService) {
    // 테스트할 때는 FakeApiService를 주입할 수 있다
}
```

### 2️⃣ 코드 재사용성이 높다

```kotlin
// 같은 Repository를 여러 곳에서 사용
class UserViewModel(private val repo: UserRepository)
class ProfileViewModel(private val repo: UserRepository)
class SettingsViewModel(private val repo: UserRepository)
```

### 3️⃣ 코드 변경이 쉽다

```kotlin
// API 구현을 바꿔도 ViewModel 코드는 변경 불필요
interface ApiService {
    suspend fun getUser(): User
}

// Retrofit 사용
class RetrofitApiService : ApiService { ... }

// Ktor 사용
class KtorApiService : ApiService { ... }
```

---

## Hilt 소개

### Hilt란?

**Hilt**는 Android에서 DI를 쉽게 사용할 수 있게 해주는 라이브러리입니다.

- Google이 공식적으로 권장
- Dagger 기반으로 만들어짐 (더 쉽게 사용 가능)
- Android 생명주기와 잘 통합됨

### Hilt의 장점

✅ **자동으로 객체 생성**: 필요한 객체를 자동으로 만들어줌  
✅ **생명주기 관리**: Activity, Fragment 등의 생명주기에 맞춰 관리  
✅ **싱글톤 지원**: 앱 전체에서 하나의 인스턴스만 사용  
✅ **테스트 지원**: 테스트용 객체를 쉽게 주입 가능

---

## Hilt 설정하기

### 1단계: Gradle 설정

#### `build.gradle.kts` (Project level)

```kotlin
plugins {
    // Hilt Gradle 플러그인 추가
    id("com.google.dagger.hilt.android") version "2.48" apply false
}
```

#### `build.gradle.kts` (App level)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Annotation Processing을 위해 필요
    id("com.google.dagger.hilt.android") // Hilt 플러그인
}

android {
    // ... 기존 설정
}

dependencies {
    // Hilt 라이브러리
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // ViewModel에서 Hilt 사용
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // 테스트용 Hilt
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptTest("com.google.dagger:hilt-compiler:2.48")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.48")
}
```

### 2단계: Application 클래스 설정

```kotlin
package com.example.myapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * @HiltAndroidApp
 * - Hilt를 사용하기 위해 Application 클래스에 반드시 추가해야 하는 어노테이션
 * - 이 어노테이션이 있어야 Hilt가 앱 전체에서 DI를 관리할 수 있다
 * - 앱이 시작될 때 Hilt 컴포넌트가 생성된다
 */
@HiltAndroidApp
class MyApplication : Application() {
    // 특별한 코드 없이 어노테이션만 추가하면 됨
}
```

### 3단계: AndroidManifest.xml 수정

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <application
        android:name=".MyApplication"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApp">
        <!-- ... -->
    </application>
    
</manifest>
```

---

## 기본 사용법

### 1️⃣ Activity에 Hilt 적용

```kotlin
package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

/**
 * @AndroidEntryPoint
 * - Activity, Fragment, View, Service 등에서 DI를 사용하려면 반드시 추가
 * - 이 어노테이션이 있어야 해당 클래스에 의존성을 주입할 수 있다
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Compose UI
        }
    }
}
```

### 2️⃣ 간단한 클래스 주입하기

#### 주입할 클래스 만들기

```kotlin
package com.example.myapp.data

import javax.inject.Inject

/**
 * UserRepository 클래스
 * 
 * @Inject constructor()
 * - 생성자에 @Inject를 붙이면 Hilt가 자동으로 이 클래스를 생성할 수 있다
 * - "이 클래스는 DI로 관리해주세요"라는 의미
 */
class UserRepository @Inject constructor() {
    
    /**
     * 사용자 정보를 가져오는 함수
     */
    fun getUser(): String {
        return "John Doe"
    }
}
```

#### ViewModel에서 사용하기

```kotlin
package com.example.myapp.ui

import androidx.lifecycle.ViewModel
import com.example.myapp.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @HiltViewModel
 * - ViewModel에서 DI를 사용하려면 반드시 추가
 * - @Inject constructor()와 함께 사용
 * 
 * @Inject constructor(...)
 * - 생성자 파라미터로 필요한 의존성을 선언
 * - Hilt가 자동으로 UserRepository 인스턴스를 생성해서 주입해준다
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    // UserRepository를 주입받는다
    // Hilt가 자동으로 UserRepository() 인스턴스를 만들어서 넣어준다
    private val repository: UserRepository
) : ViewModel() {
    
    /**
     * Repository를 사용하여 사용자 정보 가져오기
     */
    fun getUserName(): String {
        // repository는 이미 주입되어 있으므로 바로 사용 가능
        return repository.getUser()
    }
}
```

#### Compose에서 ViewModel 사용하기

```kotlin
package com.example.myapp.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Compose 화면
 * 
 * hiltViewModel()
 * - Hilt가 관리하는 ViewModel을 가져온다
 * - @HiltViewModel이 붙은 ViewModel을 자동으로 생성해준다
 * - 필요한 의존성도 자동으로 주입된다
 */
@Composable
fun UserScreen(
    // hiltViewModel()을 사용하여 ViewModel 가져오기
    viewModel: UserViewModel = hiltViewModel()
) {
    // ViewModel 사용
    val userName = viewModel.getUserName()
    
    Text(text = "사용자: $userName")
}
```

---

## ViewModel에 DI 적용

### 복잡한 의존성 주입 예제

```kotlin
package com.example.myapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.UserRepository
import com.example.myapp.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 여러 Repository를 주입받는 ViewModel
 * 
 * Hilt는 생성자에 선언된 모든 의존성을 자동으로 주입해준다
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    // 여러 개의 의존성을 주입받을 수 있다
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    // UI 상태를 관리하는 StateFlow
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState
    
    init {
        // ViewModel이 생성될 때 자동으로 데이터 로드
        loadProfile()
    }
    
    /**
     * 프로필 정보 로드
     */
    private fun loadProfile() {
        viewModelScope.launch {
            try {
                // 여러 Repository를 사용하여 데이터 가져오기
                val user = userRepository.getUser()
                val settings = settingsRepository.getSettings()
                
                // 성공 상태로 업데이트
                _uiState.value = ProfileUiState.Success(
                    userName = user,
                    isDarkMode = settings.isDarkMode
                )
            } catch (e: Exception) {
                // 에러 상태로 업데이트
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * UI 상태를 나타내는 sealed class
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userName: String, val isDarkMode: Boolean) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
```

---

## Repository 패턴과 DI

### Interface와 Implementation 분리

#### 1단계: Interface 정의

```kotlin
package com.example.myapp.data

/**
 * UserRepository 인터페이스
 * 
 * 왜 인터페이스를 사용하나?
 * - 테스트할 때 가짜(Fake) 구현을 쉽게 만들 수 있다
 * - 실제 구현을 바꿔도 ViewModel 코드는 변경 불필요
 * - 여러 구현체를 만들 수 있다 (로컬, 원격 등)
 */
interface UserRepository {
    suspend fun getUser(userId: String): User
    suspend fun updateUser(user: User)
}

/**
 * User 데이터 클래스
 */
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

#### 2단계: 실제 구현 만들기

```kotlin
package com.example.myapp.data

import javax.inject.Inject

/**
 * UserRepository의 실제 구현
 * 
 * @Inject constructor()
 * - 이 클래스도 DI로 관리된다
 * - ApiService를 주입받아 사용한다
 */
class UserRepositoryImpl @Inject constructor(
    // API 서비스를 주입받는다
    private val apiService: ApiService
) : UserRepository {
    
    /**
     * 서버에서 사용자 정보 가져오기
     */
    override suspend fun getUser(userId: String): User {
        // API 호출
        val response = apiService.getUser(userId)
        
        // 응답을 User 객체로 변환
        return User(
            id = response.id,
            name = response.name,
            email = response.email
        )
    }
    
    /**
     * 사용자 정보 업데이트
     */
    override suspend fun updateUser(user: User) {
        apiService.updateUser(user)
    }
}
```

#### 3단계: Module로 바인딩하기

```kotlin
package com.example.myapp.di

import com.example.myapp.data.UserRepository
import com.example.myapp.data.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Module
 * - Hilt에게 "이 클래스는 의존성을 제공하는 모듈이야"라고 알려준다
 * 
 * @InstallIn(SingletonComponent::class)
 * - 이 모듈을 어디에 설치할지 지정
 * - SingletonComponent: 앱 전체에서 하나의 인스턴스만 사용
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * @Binds
     * - 인터페이스와 구현체를 연결해준다
     * - "UserRepository가 필요하면 UserRepositoryImpl을 사용해"라는 의미
     * 
     * @Singleton
     * - 앱 전체에서 하나의 인스턴스만 생성
     * - 메모리 효율적이고, 데이터 일관성 유지
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        // 실제 구현체
        impl: UserRepositoryImpl
    ): UserRepository // 인터페이스
}
```

#### 4단계: ViewModel에서 사용하기

```kotlin
package com.example.myapp.ui

import androidx.lifecycle.ViewModel
import com.example.myapp.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel은 인터페이스만 알면 된다
 * 실제 구현체가 무엇인지 몰라도 된다
 */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    // 인터페이스를 주입받는다
    // Hilt가 자동으로 UserRepositoryImpl을 주입해준다
    private val userRepository: UserRepository
) : ViewModel() {
    
    suspend fun loadUser(userId: String) {
        // Repository 사용
        val user = userRepository.getUser(userId)
        // ... UI 상태 업데이트
    }
}
```

---

## 고급 기능

### 1️⃣ @Provides로 외부 라이브러리 주입

```kotlin
package com.example.myapp.di

import com.example.myapp.data.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성을 제공하는 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * @Provides
     * - 객체를 직접 생성해서 제공할 때 사용
     * - @Inject constructor()를 사용할 수 없는 경우 (외부 라이브러리 등)
     * 
     * Retrofit 인스턴스 제공
     */
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * ApiService 제공
     * 
     * Retrofit을 파라미터로 받는다
     * Hilt가 자동으로 위에서 제공한 Retrofit을 주입해준다
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

### 2️⃣ Qualifier로 같은 타입 구분하기

```kotlin
package com.example.myapp.di

import javax.inject.Qualifier

/**
 * Qualifier 어노테이션
 * 같은 타입의 의존성을 구분할 때 사용
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteDataSource
```

```kotlin
package com.example.myapp.di

import com.example.myapp.data.UserDataSource
import com.example.myapp.data.LocalUserDataSource
import com.example.myapp.data.RemoteUserDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 같은 타입의 다른 구현체를 제공
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    
    /**
     * 로컬 데이터 소스
     */
    @Binds
    @LocalDataSource
    abstract fun bindLocalDataSource(
        impl: LocalUserDataSource
    ): UserDataSource
    
    /**
     * 원격 데이터 소스
     */
    @Binds
    @RemoteDataSource
    abstract fun bindRemoteDataSource(
        impl: RemoteUserDataSource
    ): UserDataSource
}
```

```kotlin
package com.example.myapp.data

import com.example.myapp.di.LocalDataSource
import com.example.myapp.di.RemoteDataSource
import javax.inject.Inject

/**
 * Repository에서 Qualifier 사용
 */
class UserRepositoryImpl @Inject constructor(
    // @LocalDataSource로 구분
    @LocalDataSource private val localDataSource: UserDataSource,
    // @RemoteDataSource로 구분
    @RemoteDataSource private val remoteDataSource: UserDataSource
) : UserRepository {
    
    override suspend fun getUser(userId: String): User {
        // 먼저 로컬에서 찾기
        return try {
            localDataSource.getUser(userId)
        } catch (e: Exception) {
            // 로컬에 없으면 원격에서 가져오기
            val user = remoteDataSource.getUser(userId)
            // 로컬에 저장
            localDataSource.saveUser(user)
            user
        }
    }
}
```

### 3️⃣ Scopes (생명주기 관리)

```kotlin
/**
 * Hilt가 제공하는 주요 Scopes
 * 
 * @Singleton - 앱 전체에서 하나의 인스턴스
 * @ActivityScoped - Activity 생명주기 동안 유지
 * @ActivityRetainedScoped - Configuration 변경에도 유지
 * @ViewModelScoped - ViewModel 생명주기 동안 유지
 * @FragmentScoped - Fragment 생명주기 동안 유지
 */

// 예제: Activity 생명주기에 맞춘 의존성
@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    
    @Provides
    @ActivityScoped // Activity가 살아있는 동안만 유지
    fun provideAnalytics(): Analytics {
        return Analytics()
    }
}
```

---

## 테스팅

### 테스트용 Module 만들기

```kotlin
package com.example.myapp

import com.example.myapp.data.UserRepository
import com.example.myapp.data.FakeUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * 테스트용 모듈
 * 
 * @TestInstallIn
 * - 테스트할 때 원래 모듈을 이 모듈로 교체한다
 * - replaces: 어떤 모듈을 교체할지 지정
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class] // 원래 모듈 교체
)
abstract class TestRepositoryModule {
    
    /**
     * 테스트용 가짜 Repository 제공
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: FakeUserRepository // 가짜 구현체
    ): UserRepository
}
```

### 가짜 Repository 구현

```kotlin
package com.example.myapp.data

import javax.inject.Inject

/**
 * 테스트용 가짜 UserRepository
 * 
 * 실제 서버를 호출하지 않고 미리 정의된 데이터를 반환
 */
class FakeUserRepository @Inject constructor() : UserRepository {
    
    // 테스트용 데이터
    private val fakeUsers = mutableMapOf(
        "1" to User("1", "Test User", "test@example.com")
    )
    
    override suspend fun getUser(userId: String): User {
        // 서버 호출 없이 즉시 반환
        return fakeUsers[userId] ?: throw Exception("User not found")
    }
    
    override suspend fun updateUser(user: User) {
        // 메모리에만 저장
        fakeUsers[user.id] = user
    }
}
```

### ViewModel 테스트

```kotlin
package com.example.myapp.ui

import com.example.myapp.data.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * @HiltAndroidTest
 * - Hilt를 사용하는 테스트 클래스에 추가
 */
@HiltAndroidTest
class UserDetailViewModelTest {
    
    /**
     * HiltAndroidRule
     * - Hilt 컴포넌트를 초기화하는 Rule
     */
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    /**
     * @Inject
     * - 테스트 클래스에서도 의존성 주입 가능
     * - FakeUserRepository가 주입된다
     */
    @Inject
    lateinit var userRepository: UserRepository
    
    @Before
    fun setup() {
        // Hilt 주입 실행
        hiltRule.inject()
    }
    
    @Test
    fun `사용자 정보를 성공적으로 로드한다`() = runTest {
        // Given: 테스트 데이터 준비
        val userId = "1"
        
        // When: ViewModel 생성 및 사용자 로드
        val viewModel = UserDetailViewModel(userRepository)
        viewModel.loadUser(userId)
        
        // Then: 결과 검증
        // ... assertions
    }
}
```

---

## 완전한 예제 프로젝트

### 프로젝트 구조

```
app/
├── data/
│   ├── model/
│   │   └── User.kt
│   ├── remote/
│   │   └── ApiService.kt
│   ├── repository/
│   │   ├── UserRepository.kt (interface)
│   │   └── UserRepositoryImpl.kt
│   └── local/
│       └── UserDao.kt
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── ui/
│   ├── UserViewModel.kt
│   └── UserScreen.kt
└── MyApplication.kt
```

### 완전한 코드 예제

#### 1. Application 클래스

```kotlin
package com.example.myapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application()
```

#### 2. Data Model

```kotlin
package com.example.myapp.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String
)
```

#### 3. API Service

```kotlin
package com.example.myapp.data.remote

import com.example.myapp.data.model.User
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API 인터페이스
 */
interface ApiService {
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): User
}
```

#### 4. Repository Interface

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * UserRepository 인터페이스
 */
interface UserRepository {
    /**
     * 사용자 정보를 Flow로 가져오기
     * Flow를 사용하면 데이터 변경을 실시간으로 관찰할 수 있다
     */
    fun getUser(userId: String): Flow<User>
    
    /**
     * 사용자 정보 새로고침
     */
    suspend fun refreshUser(userId: String)
}
```

#### 5. Repository Implementation

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.model.User
import com.example.myapp.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UserRepository 구현체
 */
class UserRepositoryImpl @Inject constructor(
    // API 서비스 주입
    private val apiService: ApiService
) : UserRepository {
    
    // 캐시된 사용자 정보
    private var cachedUser: User? = null
    
    /**
     * 사용자 정보를 Flow로 반환
     */
    override fun getUser(userId: String): Flow<User> = flow {
        // 1. 캐시된 데이터가 있으면 먼저 emit
        cachedUser?.let { emit(it) }
        
        // 2. 서버에서 최신 데이터 가져오기
        try {
            val user = apiService.getUser(userId)
            cachedUser = user
            emit(user)
        } catch (e: Exception) {
            // 캐시가 없고 네트워크 에러면 예외 발생
            if (cachedUser == null) {
                throw e
            }
        }
    }
    
    /**
     * 강제로 서버에서 데이터 새로고침
     */
    override suspend fun refreshUser(userId: String) {
        val user = apiService.getUser(userId)
        cachedUser = user
    }
}
```

#### 6. Network Module

```kotlin
package com.example.myapp.di

import com.example.myapp.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성 제공 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * OkHttpClient 제공
     * - 로깅 인터셉터 추가
     * - 타임아웃 설정
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 로깅 인터셉터 (개발 중 네트워크 요청/응답 확인용)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit 인스턴스 제공
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * ApiService 제공
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

#### 7. Repository Module

```kotlin
package com.example.myapp.di

import com.example.myapp.data.repository.UserRepository
import com.example.myapp.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 바인딩 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * UserRepository 인터페이스와 구현체 연결
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
```

#### 8. ViewModel

```kotlin
package com.example.myapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.model.User
import com.example.myapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 사용자 화면 ViewModel
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    // UserRepository 주입
    private val userRepository: UserRepository
) : ViewModel() {
    
    // UI 상태
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState
    
    /**
     * 사용자 정보 로드
     */
    fun loadUser(userId: String) {
        viewModelScope.launch {
            // 로딩 상태로 변경
            _uiState.value = UserUiState.Loading
            
            // Repository에서 데이터 가져오기
            userRepository.getUser(userId)
                .catch { exception ->
                    // 에러 처리
                    _uiState.value = UserUiState.Error(
                        exception.message ?: "알 수 없는 에러가 발생했습니다"
                    )
                }
                .collect { user ->
                    // 성공 상태로 업데이트
                    _uiState.value = UserUiState.Success(user)
                }
        }
    }
    
    /**
     * 새로고침
     */
    fun refresh(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.refreshUser(userId)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(
                    e.message ?: "새로고침 실패"
                )
            }
        }
    }
}

/**
 * UI 상태 정의
 */
sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: User) : UserUiState()
    data class Error(val message: String) : UserUiState()
}
```

#### 9. Compose UI

```kotlin
package com.example.myapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 사용자 정보 화면
 */
@Composable
fun UserScreen(
    userId: String,
    // Hilt가 관리하는 ViewModel 가져오기
    viewModel: UserViewModel = hiltViewModel()
) {
    // UI 상태 관찰
    val uiState by viewModel.uiState.collectAsState()
    
    // 화면이 처음 표시될 때 데이터 로드
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    
    // UI 상태에 따라 다른 화면 표시
    when (val state = uiState) {
        is UserUiState.Loading -> {
            // 로딩 화면
            LoadingScreen()
        }
        is UserUiState.Success -> {
            // 성공 화면
            UserContent(
                user = state.user,
                onRefresh = { viewModel.refresh(userId) }
            )
        }
        is UserUiState.Error -> {
            // 에러 화면
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadUser(userId) }
            )
        }
    }
}

/**
 * 로딩 화면
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 사용자 정보 표시
 */
@Composable
fun UserContent(
    user: User,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRefresh) {
            Text("새로고침")
        }
    }
}

/**
 * 에러 화면
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "에러 발생",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = message)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
```

#### 10. MainActivity

```kotlin
package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.myapp.ui.UserScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * 메인 Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                // 사용자 ID "1"의 정보 표시
                UserScreen(userId = "1")
            }
        }
    }
}
```

---

## 📝 요약

### Hilt 사용 체크리스트

✅ **설정**
- [ ] Gradle에 Hilt 의존성 추가
- [ ] Application 클래스에 `@HiltAndroidApp` 추가
- [ ] AndroidManifest.xml에 Application 클래스 등록

✅ **기본 사용**
- [ ] Activity/Fragment에 `@AndroidEntryPoint` 추가
- [ ] ViewModel에 `@HiltViewModel` 추가
- [ ] 생성자에 `@Inject` 추가

✅ **고급 사용**
- [ ] Module 만들기 (`@Module`, `@InstallIn`)
- [ ] Interface 바인딩 (`@Binds`)
- [ ] 객체 제공 (`@Provides`)
- [ ] Qualifier로 구분 (`@Qualifier`)

✅ **테스팅**
- [ ] 테스트용 Module 만들기 (`@TestInstallIn`)
- [ ] Fake 구현체 만들기
- [ ] 테스트 클래스에 `@HiltAndroidTest` 추가

### 핵심 개념 정리

| 개념 | 설명 | 예제 |
|------|------|------|
| **DI** | 객체를 외부에서 주입받는 패턴 | `class A(val b: B)` |
| **@Inject** | "이 클래스를 DI로 관리해주세요" | `@Inject constructor()` |
| **@HiltViewModel** | ViewModel에서 DI 사용 | `@HiltViewModel class VM` |
| **@Module** | 의존성을 제공하는 모듈 | `@Module object NetworkModule` |
| **@Binds** | 인터페이스와 구현체 연결 | `@Binds fun bind(): Interface` |
| **@Provides** | 객체를 직접 생성해서 제공 | `@Provides fun provide(): Retrofit` |
| **@Singleton** | 앱 전체에서 하나의 인스턴스 | `@Singleton class Repository` |

---

## 🎯 다음 단계

1. **간단한 프로젝트로 연습하기**
   - Repository 하나만 만들어서 ViewModel에 주입
   - 점진적으로 복잡도 높이기

2. **관련 문서 학습하기**
   - [40-kotlin-coroutines-flow-guide.md](./40-kotlin-coroutines-flow-guide.md)
   - [41-mvvm-mvi-architecture-guide.md](./41-mvvm-mvi-architecture-guide.md)

3. **실전 프로젝트에 적용하기**
   - 기존 프로젝트를 Hilt로 마이그레이션
   - 새 프로젝트는 처음부터 Hilt 사용

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
