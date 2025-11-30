# Android Architecture 가이드 (MVVM & Clean Architecture)

## 📚 목차

1. [Architecture 개요](#architecture-개요)
2. [MVVM 패턴](#mvvm-패턴)
3. [Clean Architecture](#clean-architecture)
4. [의존성 주입 (Hilt)](#의존성-주입-hilt)
5. [프로젝트 구조](#프로젝트-구조)
6. [실전 예제](#실전-예제)

---

## Architecture 개요

### 왜 Architecture가 필요한가?

- ✅ **유지보수성**: 코드 변경이 쉬움
- ✅ **테스트 용이성**: 각 레이어를 독립적으로 테스트
- ✅ **확장성**: 새 기능 추가가 쉬움
- ✅ **팀 협업**: 명확한 책임 분리

### Android 권장 Architecture

```
UI Layer (Compose)
      ↓
Domain Layer (Use Cases)
      ↓
Data Layer (Repository)
      ↓
Data Sources (API, DB)
```

---

## MVVM 패턴

### MVVM 구조

```
View (Compose)
    ↕
ViewModel
    ↕
Model (Repository)
```

### View (Compose)

```kotlin
@Composable
fun UserListScreen(
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
    
    when (uiState) {
        is UiState.Loading -> LoadingView()
        is UiState.Success -> UserList(uiState.data)
        is UiState.Error -> ErrorView(uiState.message)
    }
}
```

### ViewModel

```kotlin
class UserViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<User>>> = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            getUsersUseCase()
                .onSuccess { users ->
                    _uiState.value = UiState.Success(users)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### Model (Repository)

```kotlin
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User>
}

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val userDao: UserDao
) : UserRepository {
    
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val users = apiService.getUsers()
            userDao.insertAll(users)
            Result.success(users)
        } catch (e: Exception) {
            // 네트워크 실패 시 캐시된 데이터 반환
            val cachedUsers = userDao.getAllUsers()
            if (cachedUsers.isNotEmpty()) {
                Result.success(cachedUsers)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getUserById(id: Int): Result<User> {
        return try {
            val user = apiService.getUserById(id)
            userDao.insert(user)
            Result.success(user)
        } catch (e: Exception) {
            val cachedUser = userDao.getUserById(id)
            if (cachedUser != null) {
                Result.success(cachedUser)
            } else {
                Result.failure(e)
            }
        }
    }
}
```

---

## Clean Architecture

### 레이어 구조

```
Presentation Layer (UI + ViewModel)
         ↓
Domain Layer (Use Cases + Entities)
         ↓
Data Layer (Repository + Data Sources)
```

### Domain Layer

#### Entity

```kotlin
// 도메인 모델 (비즈니스 로직의 핵심)
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val isVerified: Boolean
) {
    fun getDisplayName(): String {
        return if (isVerified) "✓ $name" else name
    }
}
```

#### Use Case

```kotlin
class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        return userRepository.getUsers()
    }
}

class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int): Result<User> {
        return userRepository.getUserById(userId)
    }
}

class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return userRepository.updateUser(user)
    }
}
```

### Data Layer

#### Repository Interface (Domain Layer)

```kotlin
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
}
```

#### Repository Implementation (Data Layer)

```kotlin
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {
    
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            // 원격 데이터 가져오기
            val remoteUsers = remoteDataSource.getUsers()
            
            // 로컬에 캐시
            localDataSource.saveUsers(remoteUsers)
            
            Result.success(remoteUsers)
        } catch (e: Exception) {
            // 실패 시 로컬 데이터 반환
            val localUsers = localDataSource.getUsers()
            if (localUsers.isNotEmpty()) {
                Result.success(localUsers)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getUserById(id: Int): Result<User> {
        return try {
            val user = remoteDataSource.getUserById(id)
            localDataSource.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            val cachedUser = localDataSource.getUserById(id)
            if (cachedUser != null) {
                Result.success(cachedUser)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            remoteDataSource.updateUser(user)
            localDataSource.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### Data Sources

```kotlin
// Remote Data Source
class UserRemoteDataSource(
    private val apiService: ApiService
) {
    suspend fun getUsers(): List<User> {
        return apiService.getUsers().map { it.toDomain() }
    }
    
    suspend fun getUserById(id: Int): User {
        return apiService.getUserById(id).toDomain()
    }
    
    suspend fun updateUser(user: User) {
        apiService.updateUser(user.toDto())
    }
}

// Local Data Source
class UserLocalDataSource(
    private val userDao: UserDao
) {
    suspend fun getUsers(): List<User> {
        return userDao.getAllUsers().map { it.toDomain() }
    }
    
    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)?.toDomain()
    }
    
    suspend fun saveUsers(users: List<User>) {
        userDao.insertAll(users.map { it.toEntity() })
    }
    
    suspend fun saveUser(user: User) {
        userDao.insert(user.toEntity())
    }
    
    suspend fun updateUser(user: User) {
        userDao.update(user.toEntity())
    }
}
```

#### Mappers

```kotlin
// DTO → Domain
fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        isVerified = this.verified
    )
}

// Domain → DTO
fun User.toDto(): UserDto {
    return UserDto(
        id = this.id,
        name = this.name,
        email = this.email,
        verified = this.isVerified
    )
}

// Entity → Domain
fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        isVerified = this.isVerified
    )
}

// Domain → Entity
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        name = this.name,
        email = this.email,
        isVerified = this.isVerified
    )
}
```

---

## 의존성 주입 (Hilt)

### 설정

```kotlin
// build.gradle.kts (Project)
plugins {
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

// build.gradle.kts (Module: app)
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
```

### Application 클래스

```kotlin
@HiltAndroidApp
class MyApp : Application()
```

### Module 정의

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    
    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        apiService: ApiService
    ): UserRemoteDataSource {
        return UserRemoteDataSource(apiService)
    }
    
    @Provides
    @Singleton
    fun provideUserLocalDataSource(
        userDao: UserDao
    ): UserLocalDataSource {
        return UserLocalDataSource(userDao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
```

### ViewModel 주입

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<User>>> = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            getUsersUseCase()
                .onSuccess { users ->
                    _uiState.value = UiState.Success(users)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
```

### Compose에서 사용

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun UserListScreen(
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // UI 구현
}
```

---

## 프로젝트 구조

### 패키지 구조 (Feature 기반)

```
com.example.myapp/
├── di/                          # 의존성 주입
│   ├── AppModule.kt
│   ├── DataSourceModule.kt
│   └── RepositoryModule.kt
│
├── data/                        # Data Layer
│   ├── local/
│   │   ├── dao/
│   │   │   └── UserDao.kt
│   │   ├── entity/
│   │   │   └── UserEntity.kt
│   │   └── database/
│   │       └── AppDatabase.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── ApiService.kt
│   │   └── dto/
│   │       └── UserDto.kt
│   ├── repository/
│   │   └── UserRepositoryImpl.kt
│   └── datasource/
│       ├── UserRemoteDataSource.kt
│       └── UserLocalDataSource.kt
│
├── domain/                      # Domain Layer
│   ├── model/
│   │   └── User.kt
│   ├── repository/
│   │   └── UserRepository.kt
│   └── usecase/
│       ├── GetUsersUseCase.kt
│       ├── GetUserByIdUseCase.kt
│       └── UpdateUserUseCase.kt
│
├── presentation/                # Presentation Layer
│   ├── user/
│   │   ├── list/
│   │   │   ├── UserListScreen.kt
│   │   │   ├── UserListViewModel.kt
│   │   │   └── components/
│   │   │       └── UserItem.kt
│   │   └── detail/
│   │       ├── UserDetailScreen.kt
│   │       └── UserDetailViewModel.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   └── common/
│       ├── components/
│       └── theme/
│
├── navigation/
│   └── NavGraph.kt
│
└── MyApp.kt
```

---

## 실전 예제

### 완전한 Feature 구현

```kotlin
// 1. Domain Model
data class Todo(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long
)

// 2. Repository Interface (Domain)
interface TodoRepository {
    fun getTodos(): Flow<List<Todo>>
    suspend fun getTodoById(id: Int): Todo?
    suspend fun insertTodo(todo: Todo)
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)
}

// 3. Use Cases
class GetTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> {
        return repository.getTodos()
    }
}

class AddTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(title: String, description: String) {
        val todo = Todo(
            id = 0,
            title = title,
            description = description,
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )
        repository.insertTodo(todo)
    }
}

class ToggleTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo) {
        repository.updateTodo(
            todo.copy(isCompleted = !todo.isCompleted)
        )
    }
}

// 4. Repository Implementation
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {
    
    override fun getTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTodoById(id: Int): Todo? {
        return todoDao.getTodoById(id)?.toDomain()
    }
    
    override suspend fun insertTodo(todo: Todo) {
        todoDao.insert(todo.toEntity())
    }
    
    override suspend fun updateTodo(todo: Todo) {
        todoDao.update(todo.toEntity())
    }
    
    override suspend fun deleteTodo(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }
}

// 5. ViewModel
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val getTodosUseCase: GetTodosUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase
) : ViewModel() {
    
    val todos: StateFlow<List<Todo>> = getTodosUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addTodo(title: String, description: String) {
        viewModelScope.launch {
            addTodoUseCase(title, description)
        }
    }
    
    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            toggleTodoUseCase(todo)
        }
    }
}

// 6. UI
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val todos by viewModel.todos.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 추가 다이얼로그 표시 */ }
            ) {
                Icon(Icons.Filled.Add, "추가")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    onToggle = { viewModel.toggleTodo(todo) }
                )
            }
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 단일 책임 원칙

```kotlin
// ✅ 각 클래스는 하나의 책임만
class GetUsersUseCase(private val repository: UserRepository)
class UpdateUserUseCase(private val repository: UserRepository)

// ❌ 여러 책임을 가진 클래스
class UserManager // 너무 광범위
```

### 2. 의존성 역전 원칙

```kotlin
// ✅ 인터페이스에 의존
class UserViewModel(private val repository: UserRepository)

// ❌ 구현체에 의존
class UserViewModel(private val repository: UserRepositoryImpl)
```

### 3. 레이어 간 데이터 변환

```kotlin
// ✅ 각 레이어마다 적절한 모델 사용
// DTO (Data Layer) → Domain Model → UI Model
```

### 4. Use Case는 단순하게

```kotlin
// ✅ 하나의 비즈니스 로직만
class GetUsersUseCase {
    suspend operator fun invoke(): Result<List<User>>
}
```

---

## 🎯 다음 단계

Architecture를 마스터했습니다! 다음으로:

1. **Advanced Compose** - Custom Layout, Canvas 등
2. **Complete App Example** - 실전 프로젝트

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Architecting! 🏗️
