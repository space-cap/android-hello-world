# Clean Architecture

> 📖 **시리즈 구성**
> - **42-1**: [Architecture Patterns](./42-1-architecture-patterns.md)
> - **42-2**: Clean Architecture (현재 문서)

---

## 📚 목차

1. [Clean Architecture 개요](#clean-architecture-개요)
2. [레이어 구조](#레이어-구조)
3. [의존성 규칙](#의존성-규칙)
4. [실전 예제](#실전-예제)

---

## Clean Architecture 개요

### 레이어 구조

```
┌─────────────────────────────────┐
│  Presentation (UI)              │
│  - Compose, ViewModel           │
├─────────────────────────────────┤
│  Domain (Business Logic)        │
│  - Use Cases, Entities          │
├─────────────────────────────────┤
│  Data (Data Sources)            │
│  - Repository, API, DB          │
└─────────────────────────────────┘
```

---

## 레이어 구조

### Domain Layer

```kotlin
/**
 * Entity (도메인 모델)
 */
data class User(
    val id: Int,
    val name: String,
    val email: String
)

/**
 * Use Case
 */
class GetUserUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: Int): Result<User> {
        return repository.getUser(id)
    }
}
```

### Data Layer

```kotlin
/**
 * Repository 구현
 */
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {
    
    override suspend fun getUser(id: Int): Result<User> {
        return try {
            val user = remoteDataSource.getUser(id)
            localDataSource.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Presentation Layer

```kotlin
/**
 * ViewModel
 */
class UserViewModel(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    
    fun loadUser(id: Int) {
        viewModelScope.launch {
            getUserUseCase(id)
                .onSuccess { user ->
                    _state.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _state.value = UiState.Error(error.message)
                }
        }
    }
}
```

---

## 의존성 규칙

### Dependency Injection

```kotlin
/**
 * Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(
        remoteDataSource: UserRemoteDataSource,
        localDataSource: UserLocalDataSource
    ): UserRepository {
        return UserRepositoryImpl(remoteDataSource, localDataSource)
    }
}
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
