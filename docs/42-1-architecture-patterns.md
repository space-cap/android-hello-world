# Architecture Patterns

> 📖 **시리즈 구성**
> - **42-1**: Architecture Patterns (현재 문서)
> - **42-2**: [Clean Architecture](./42-2-clean-architecture.md)

---

## 📚 목차

1. [MVVM 패턴](#mvvm-패턴)
2. [MVI 패턴](#mvi-패턴)
3. [Repository 패턴](#repository-패턴)
4. [실전 예제](#실전-예제)

---

## MVVM 패턴

### 기본 구조

```kotlin
/**
 * Model
 */
data class User(val id: Int, val name: String)

/**
 * ViewModel
 */
class UserViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getUsers()
        }
    }
}

/**
 * View (Compose)
 */
@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {
    val users by viewModel.users.collectAsState()
    
    LazyColumn {
        items(users) { user ->
            Text(user.name)
        }
    }
}
```

---

## MVI 패턴

### Intent-State-Effect

```kotlin
/**
 * Intent (사용자 의도)
 */
sealed class UserIntent {
    object LoadUsers : UserIntent()
    data class SelectUser(val id: Int) : UserIntent()
}

/**
 * State (UI 상태)
 */
data class UserState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel
 */
class UserViewModel : ViewModel() {
    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state.asStateFlow()
    
    fun handleIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.LoadUsers -> loadUsers()
            is UserIntent.SelectUser -> selectUser(intent.id)
        }
    }
}
```

---

## Repository 패턴

### Repository 구현

```kotlin
/**
 * Repository 인터페이스
 */
interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUser(id: Int): User
}

/**
 * Repository 구현
 */
class UserRepositoryImpl(
    private val api: ApiService,
    private val dao: UserDao
) : UserRepository {
    
    override suspend fun getUsers(): List<User> {
        return try {
            val users = api.fetchUsers()
            dao.insertAll(users)
            users
        } catch (e: Exception) {
            dao.getAllUsers()
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
