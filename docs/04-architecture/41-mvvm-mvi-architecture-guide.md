# 41. MVVM/MVI 아키텍처 패턴 완벽 가이드

## 📚 목차
1. [아키텍처 패턴이란?](#아키텍처-패턴이란)
2. [왜 아키텍처가 필요한가?](#왜-아키텍처가-필요한가)
3. [MVVM 패턴](#mvvm-패턴)
4. [MVI 패턴](#mvi-패턴)
5. [MVVM vs MVI 비교](#mvvm-vs-mvi-비교)
6. [UiState 설계](#uistate-설계)
7. [단방향 데이터 플로우](#단방향-데이터-플로우)
8. [완전한 앱 예제](#완전한-앱-예제)
9. [Best Practices](#best-practices)

---

## 아키텍처 패턴이란?

### 🤔 쉬운 설명

**아키텍처 패턴**은 앱의 코드를 어떻게 구조화할지 정하는 설계 방법입니다.

#### 일상 생활의 비유

```
❌ 아키텍처 없이 (스파게티 코드):
집을 지을 때 설계도 없이 막 짓는다
→ 방, 화장실, 부엌이 뒤섞여 있다
→ 나중에 고치기 어렵다
→ 다른 사람이 이해하기 어렵다

✅ 아키텍처 사용 (체계적인 구조):
집을 지을 때 설계도를 먼저 그린다
→ 1층: 거실, 부엌
→ 2층: 침실, 화장실
→ 각 공간의 역할이 명확하다
→ 나중에 고치기 쉽다
```

### 코드로 비교하기

#### ❌ 아키텍처 없이 (모든 코드가 Activity에)

```kotlin
class MainActivity : ComponentActivity() {
    // UI, 비즈니스 로직, 데이터 처리가 모두 섞여있음
    private var users = mutableListOf<User>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 네트워크 요청 (Activity에서 직접!)
        CoroutineScope(Dispatchers.IO).launch {
            val response = URL("https://api.example.com/users").readText()
            val userList = Gson().fromJson(response, Array<User>::class.java)
            users = userList.toMutableList()
            
            // UI 업데이트 (메인 스레드로 전환)
            withContext(Dispatchers.Main) {
                // UI 코드...
            }
        }
    }
    // 문제: 테스트 불가능, 재사용 불가능, 유지보수 어려움
}
```

#### ✅ MVVM 아키텍처 사용

```kotlin
// ViewModel: 비즈니스 로직과 상태 관리
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getUsers()
        }
    }
}

// Activity: UI만 담당
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val users by viewModel.users.collectAsState()
            UserList(users)
        }
    }
}
// 장점: 테스트 가능, 재사용 가능, 유지보수 쉬움
```

---

## 왜 아키텍처가 필요한가?

### 아키텍처의 장점

✅ **관심사의 분리 (Separation of Concerns)**
- UI, 비즈니스 로직, 데이터가 분리됨
- 각 부분을 독립적으로 수정 가능

✅ **테스트 가능성 (Testability)**
- ViewModel, Repository를 쉽게 테스트
- UI 없이도 로직 테스트 가능

✅ **재사용성 (Reusability)**
- 같은 ViewModel을 여러 화면에서 사용
- Repository를 여러 ViewModel에서 공유

✅ **유지보수성 (Maintainability)**
- 코드 구조가 명확하여 수정이 쉬움
- 새로운 기능 추가가 용이

✅ **협업 (Collaboration)**
- 팀원들이 코드를 쉽게 이해
- 역할 분담이 명확

---

## MVVM 패턴

### MVVM이란?

**MVVM**은 Model-View-ViewModel의 약자입니다.

```
┌─────────────┐
│    View     │ ← UI (Compose, XML)
│  (Activity) │
└──────┬──────┘
       │ 관찰 (observe)
       ↓
┌─────────────┐
│  ViewModel  │ ← 비즈니스 로직, 상태 관리
└──────┬──────┘
       │ 데이터 요청
       ↓
┌─────────────┐
│    Model    │ ← 데이터 (Repository, Database, API)
│ (Repository)│
└─────────────┘
```

### MVVM 구성 요소

#### 1. Model (데이터 계층)

```kotlin
/**
 * Data Model
 * 앱에서 사용하는 데이터 구조
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String
)

/**
 * Repository Interface
 * 데이터 소스를 추상화
 */
interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUser(id: String): User
    suspend fun updateUser(user: User)
}

/**
 * Repository Implementation
 * 실제 데이터 처리 로직
 */
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) : UserRepository {
    
    /**
     * 사용자 목록 가져오기
     * 1. 로컬 DB에서 먼저 가져오기
     * 2. 서버에서 최신 데이터 가져오기
     * 3. 로컬 DB 업데이트
     */
    override suspend fun getUsers(): List<User> {
        // 로컬 데이터 먼저 반환 (빠른 응답)
        val localUsers = userDao.getAllUsers()
        
        // 서버에서 최신 데이터 가져오기
        try {
            val remoteUsers = apiService.getUsers()
            // 로컬 DB 업데이트
            userDao.insertUsers(remoteUsers)
            return remoteUsers
        } catch (e: Exception) {
            // 네트워크 에러 시 로컬 데이터 반환
            return localUsers
        }
    }
    
    override suspend fun getUser(id: String): User {
        return apiService.getUser(id)
    }
    
    override suspend fun updateUser(user: User) {
        apiService.updateUser(user)
        userDao.updateUser(user)
    }
}
```

#### 2. ViewModel (프레젠테이션 로직)

```kotlin
/**
 * UI 상태 정의
 * sealed class로 가능한 모든 상태를 표현
 */
sealed class UserListUiState {
    // 로딩 중
    object Loading : UserListUiState()
    
    // 성공 (데이터 있음)
    data class Success(
        val users: List<User>,
        val isRefreshing: Boolean = false
    ) : UserListUiState()
    
    // 에러
    data class Error(
        val message: String
    ) : UserListUiState()
}

/**
 * ViewModel
 * UI 상태를 관리하고 비즈니스 로직을 처리
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    // Repository 주입
    private val repository: UserRepository
) : ViewModel() {
    
    // UI 상태 (private - 외부에서 수정 불가)
    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    
    // UI 상태 (public - 읽기 전용)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    
    init {
        // ViewModel 생성 시 자동으로 데이터 로드
        loadUsers()
    }
    
    /**
     * 사용자 목록 로드
     */
    fun loadUsers() {
        viewModelScope.launch {
            // 로딩 상태로 변경
            _uiState.value = UserListUiState.Loading
            
            try {
                // Repository에서 데이터 가져오기
                val users = repository.getUsers()
                
                // 성공 상태로 변경
                _uiState.value = UserListUiState.Success(users)
            } catch (e: Exception) {
                // 에러 상태로 변경
                _uiState.value = UserListUiState.Error(
                    message = e.message ?: "알 수 없는 에러"
                )
            }
        }
    }
    
    /**
     * 새로고침
     */
    fun refresh() {
        viewModelScope.launch {
            // 현재 상태가 Success면 isRefreshing = true
            val currentState = _uiState.value
            if (currentState is UserListUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            }
            
            try {
                val users = repository.getUsers()
                _uiState.value = UserListUiState.Success(
                    users = users,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = UserListUiState.Error(e.message ?: "새로고침 실패")
            }
        }
    }
    
    /**
     * 사용자 삭제
     */
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                // Repository에서 삭제
                repository.deleteUser(userId)
                
                // UI 상태 업데이트 (해당 사용자 제거)
                val currentState = _uiState.value
                if (currentState is UserListUiState.Success) {
                    val updatedUsers = currentState.users.filter { it.id != userId }
                    _uiState.value = currentState.copy(users = updatedUsers)
                }
            } catch (e: Exception) {
                // 에러 처리 (토스트 메시지 등)
            }
        }
    }
}
```

#### 3. View (UI 계층)

```kotlin
/**
 * Compose UI
 * ViewModel의 상태를 관찰하고 UI를 그린다
 */
@Composable
fun UserListScreen(
    // Hilt가 ViewModel 주입
    viewModel: UserListViewModel = hiltViewModel(),
    onUserClick: (String) -> Unit
) {
    // UI 상태 관찰
    val uiState by viewModel.uiState.collectAsState()
    
    // UI 상태에 따라 다른 화면 표시
    when (val state = uiState) {
        // 로딩 중
        is UserListUiState.Loading -> {
            LoadingScreen()
        }
        
        // 성공 (데이터 있음)
        is UserListUiState.Success -> {
            UserListContent(
                users = state.users,
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onUserClick = onUserClick,
                onDeleteUser = { userId -> viewModel.deleteUser(userId) }
            )
        }
        
        // 에러
        is UserListUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadUsers() }
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
 * 사용자 목록 컨텐츠
 */
@Composable
fun UserListContent(
    users: List<User>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onUserClick: (String) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = users,
                key = { user -> user.id }
            ) { user ->
                UserListItem(
                    user = user,
                    onClick = { onUserClick(user.id) },
                    onDelete = { onDeleteUser(user.id) }
                )
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * 사용자 아이템
 */
@Composable
fun UserListItem(
    user: User,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아바타
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 사용자 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 삭제 버튼
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제"
                )
            }
        }
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("사용자 삭제") },
            text = { Text("${user.name}을(를) 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
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
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
```

---

## MVI 패턴

### MVI란?

**MVI**는 Model-View-Intent의 약자입니다.

```
┌─────────────┐
│    View     │ ← UI (Compose)
└──────┬──────┘
       │ Intent (사용자 액션)
       ↓
┌─────────────┐
│    Model    │ ← 상태 관리 (ViewModel)
│ (ViewModel) │
└──────┬──────┘
       │ State (UI 상태)
       ↓
┌─────────────┐
│    View     │ ← UI 업데이트
└─────────────┘

단방향 데이터 플로우!
```

### MVI 핵심 개념

#### 1. Intent (사용자 의도)

```kotlin
/**
 * Intent: 사용자가 할 수 있는 모든 액션
 * sealed class로 정의하여 가능한 액션을 명확히 함
 */
sealed class UserListIntent {
    // 화면 진입 (초기 로드)
    object LoadUsers : UserListIntent()
    
    // 새로고침
    object Refresh : UserListIntent()
    
    // 사용자 클릭
    data class UserClicked(val userId: String) : UserListIntent()
    
    // 사용자 삭제
    data class DeleteUser(val userId: String) : UserListIntent()
    
    // 검색어 입력
    data class SearchQueryChanged(val query: String) : UserListIntent()
}
```

#### 2. State (UI 상태)

```kotlin
/**
 * State: UI의 모든 상태를 하나의 객체로 표현
 * data class로 정의하여 불변성 보장
 */
data class UserListState(
    // 로딩 상태
    val isLoading: Boolean = false,
    
    // 새로고침 상태
    val isRefreshing: Boolean = false,
    
    // 사용자 목록
    val users: List<User> = emptyList(),
    
    // 에러 메시지
    val error: String? = null,
    
    // 검색어
    val searchQuery: String = "",
    
    // 필터링된 사용자 목록
    val filteredUsers: List<User> = emptyList()
) {
    /**
     * 검색어에 따라 필터링된 사용자 목록 계산
     */
    init {
        filteredUsers = if (searchQuery.isEmpty()) {
            users
        } else {
            users.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}
```

#### 3. ViewModel (상태 관리)

```kotlin
/**
 * MVI ViewModel
 * Intent를 받아서 State를 업데이트
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    // UI 상태 (private)
    private val _state = MutableStateFlow(UserListState())
    
    // UI 상태 (public - 읽기 전용)
    val state: StateFlow<UserListState> = _state.asStateFlow()
    
    /**
     * Intent 처리
     * 모든 사용자 액션은 이 함수를 통해 처리됨
     */
    fun handleIntent(intent: UserListIntent) {
        when (intent) {
            is UserListIntent.LoadUsers -> loadUsers()
            is UserListIntent.Refresh -> refresh()
            is UserListIntent.UserClicked -> handleUserClick(intent.userId)
            is UserListIntent.DeleteUser -> deleteUser(intent.userId)
            is UserListIntent.SearchQueryChanged -> updateSearchQuery(intent.query)
        }
    }
    
    /**
     * 사용자 목록 로드
     */
    private fun loadUsers() {
        viewModelScope.launch {
            // 상태 업데이트: 로딩 시작
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 데이터 로드
                val users = repository.getUsers()
                
                // 상태 업데이트: 성공
                _state.update { 
                    it.copy(
                        isLoading = false,
                        users = users,
                        error = null
                    )
                }
            } catch (e: Exception) {
                // 상태 업데이트: 에러
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "알 수 없는 에러"
                    )
                }
            }
        }
    }
    
    /**
     * 새로고침
     */
    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            
            try {
                val users = repository.getUsers()
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        users = users,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        error = e.message ?: "새로고침 실패"
                    )
                }
            }
        }
    }
    
    /**
     * 사용자 클릭 처리
     */
    private fun handleUserClick(userId: String) {
        // 네비게이션 이벤트 발생 등
        // 여기서는 예시로 로그만 출력
        Log.d("UserListViewModel", "User clicked: $userId")
    }
    
    /**
     * 사용자 삭제
     */
    private fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteUser(userId)
                
                // 상태 업데이트: 해당 사용자 제거
                _state.update { currentState ->
                    currentState.copy(
                        users = currentState.users.filter { it.id != userId }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "삭제 실패: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 검색어 업데이트
     */
    private fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
}
```

#### 4. View (UI)

```kotlin
/**
 * MVI Compose UI
 * State를 관찰하고 Intent를 발생시킴
 */
@Composable
fun UserListScreen(
    viewModel: UserListViewModel = hiltViewModel(),
    onUserClick: (String) -> Unit
) {
    // State 관찰
    val state by viewModel.state.collectAsState()
    
    // 화면 진입 시 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.handleIntent(UserListIntent.LoadUsers)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 검색 바
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { query ->
                // Intent 발생
                viewModel.handleIntent(
                    UserListIntent.SearchQueryChanged(query)
                )
            }
        )
        
        // 상태에 따른 UI 표시
        when {
            // 로딩 중
            state.isLoading -> {
                LoadingScreen()
            }
            
            // 에러
            state.error != null -> {
                ErrorScreen(
                    message = state.error!!,
                    onRetry = {
                        // Intent 발생
                        viewModel.handleIntent(UserListIntent.LoadUsers)
                    }
                )
            }
            
            // 성공
            else -> {
                UserListContent(
                    users = state.filteredUsers,
                    isRefreshing = state.isRefreshing,
                    onRefresh = {
                        // Intent 발생
                        viewModel.handleIntent(UserListIntent.Refresh)
                    },
                    onUserClick = { userId ->
                        // Intent 발생
                        viewModel.handleIntent(UserListIntent.UserClicked(userId))
                        onUserClick(userId)
                    },
                    onDeleteUser = { userId ->
                        // Intent 발생
                        viewModel.handleIntent(UserListIntent.DeleteUser(userId))
                    }
                )
            }
        }
    }
}

/**
 * 검색 바
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("검색...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "지우기"
                    )
                }
            }
        },
        singleLine = true
    )
}
```

---

## MVVM vs MVI 비교

### 차이점 요약

| 특징 | MVVM | MVI |
|------|------|-----|
| **상태 관리** | 여러 StateFlow | 하나의 State 객체 |
| **액션 처리** | 여러 함수 | Intent로 통합 |
| **데이터 플로우** | 양방향 가능 | 단방향 강제 |
| **복잡도** | 상대적으로 간단 | 상대적으로 복잡 |
| **디버깅** | 어려움 | 쉬움 (모든 상태 변화 추적) |
| **테스트** | 보통 | 쉬움 (Intent → State) |
| **학습 곡선** | 낮음 | 높음 |

### MVVM 예제

```kotlin
// MVVM: 여러 StateFlow
class UserViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // 여러 함수
    fun loadUsers() { }
    fun refresh() { }
    fun deleteUser(id: String) { }
}
```

### MVI 예제

```kotlin
// MVI: 하나의 State
class UserViewModel : ViewModel() {
    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state
    
    // 하나의 Intent 처리 함수
    fun handleIntent(intent: UserListIntent) {
        when (intent) {
            is UserListIntent.LoadUsers -> loadUsers()
            is UserListIntent.Refresh -> refresh()
            is UserListIntent.DeleteUser -> deleteUser(intent.userId)
        }
    }
}
```

### 언제 무엇을 사용할까?

#### MVVM을 사용하는 경우

✅ 간단한 앱  
✅ 팀이 MVVM에 익숙함  
✅ 빠른 개발이 필요함  
✅ 상태 관리가 복잡하지 않음

#### MVI를 사용하는 경우

✅ 복잡한 상태 관리가 필요함  
✅ 디버깅과 테스트가 중요함  
✅ 단방향 데이터 플로우를 선호함  
✅ 상태 변화를 명확히 추적하고 싶음

---

## UiState 설계

### UiState 설계 원칙

```kotlin
/**
 * 좋은 UiState 설계
 * 
 * 1. 불변성 (Immutability): data class 사용
 * 2. 단일 진실 공급원 (Single Source of Truth): 하나의 State
 * 3. 명확성 (Clarity): 모든 가능한 상태를 명시
 * 4. 계산된 속성 (Computed Properties): 파생 데이터는 계산
 */
data class ProductDetailState(
    // 원본 데이터
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // 사용자 입력
    val quantity: Int = 1,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    
    // 장바구니 상태
    val isAddingToCart: Boolean = false,
    val addToCartSuccess: Boolean = false
) {
    // 계산된 속성: 총 가격
    val totalPrice: Double
        get() = (product?.price ?: 0.0) * quantity
    
    // 계산된 속성: 구매 가능 여부
    val canPurchase: Boolean
        get() = product != null && 
                selectedColor != null && 
                selectedSize != null && 
                quantity > 0
    
    // 계산된 속성: 재고 있음
    val isInStock: Boolean
        get() = (product?.stock ?: 0) > 0
}
```

### 복잡한 UiState 예제

```kotlin
/**
 * 쇼핑 카트 화면의 UiState
 */
data class ShoppingCartState(
    // 장바구니 아이템
    val items: List<CartItem> = emptyList(),
    
    // 로딩 상태
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    
    // 에러
    val error: String? = null,
    
    // 쿠폰
    val appliedCoupon: Coupon? = null,
    val couponError: String? = null,
    
    // 배송 정보
    val selectedShippingMethod: ShippingMethod? = null,
    val shippingAddress: Address? = null,
    
    // 결제 정보
    val selectedPaymentMethod: PaymentMethod? = null
) {
    // 계산된 속성: 상품 총액
    val subtotal: Double
        get() = items.sumOf { it.product.price * it.quantity }
    
    // 계산된 속성: 할인 금액
    val discount: Double
        get() = appliedCoupon?.calculateDiscount(subtotal) ?: 0.0
    
    // 계산된 속성: 배송비
    val shippingCost: Double
        get() = selectedShippingMethod?.cost ?: 0.0
    
    // 계산된 속성: 최종 금액
    val total: Double
        get() = subtotal - discount + shippingCost
    
    // 계산된 속성: 결제 가능 여부
    val canCheckout: Boolean
        get() = items.isNotEmpty() &&
                shippingAddress != null &&
                selectedShippingMethod != null &&
                selectedPaymentMethod != null &&
                !isUpdating
}

data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedOptions: Map<String, String>
)

data class Coupon(
    val code: String,
    val discountType: DiscountType,
    val discountValue: Double
) {
    fun calculateDiscount(subtotal: Double): Double {
        return when (discountType) {
            DiscountType.PERCENTAGE -> subtotal * (discountValue / 100)
            DiscountType.FIXED_AMOUNT -> discountValue.coerceAtMost(subtotal)
        }
    }
}

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}
```

---

## 단방향 데이터 플로우

### 단방향 데이터 플로우란?

```
사용자 액션 → Intent → ViewModel → State 업데이트 → UI 업데이트
     ↑                                                    │
     └────────────────────────────────────────────────────┘
                    (다시 사용자 액션)
```

### 단방향 플로우의 장점

✅ **예측 가능성**: 데이터 흐름이 명확  
✅ **디버깅 용이**: 상태 변화를 추적하기 쉬움  
✅ **테스트 용이**: Intent → State 변화를 테스트  
✅ **시간 여행 디버깅**: 상태 히스토리 추적 가능

### 구현 예제

```kotlin
/**
 * 단방향 데이터 플로우 구현
 */
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {
    
    // State
    private val _state = MutableStateFlow(TodoState())
    val state: StateFlow<TodoState> = _state.asStateFlow()
    
    // Intent 처리
    fun handleIntent(intent: TodoIntent) {
        when (intent) {
            is TodoIntent.LoadTodos -> loadTodos()
            is TodoIntent.AddTodo -> addTodo(intent.title)
            is TodoIntent.ToggleTodo -> toggleTodo(intent.id)
            is TodoIntent.DeleteTodo -> deleteTodo(intent.id)
            is TodoIntent.FilterChanged -> updateFilter(intent.filter)
        }
    }
    
    private fun loadTodos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val todos = repository.getTodos()
                _state.update {
                    it.copy(
                        isLoading = false,
                        todos = todos
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private fun addTodo(title: String) {
        viewModelScope.launch {
            val newTodo = Todo(
                id = UUID.randomUUID().toString(),
                title = title,
                isCompleted = false
            )
            
            repository.addTodo(newTodo)
            
            _state.update { currentState ->
                currentState.copy(
                    todos = currentState.todos + newTodo
                )
            }
        }
    }
    
    private fun toggleTodo(id: String) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    todos = currentState.todos.map { todo ->
                        if (todo.id == id) {
                            todo.copy(isCompleted = !todo.isCompleted)
                        } else {
                            todo
                        }
                    }
                )
            }
            
            val updatedTodo = _state.value.todos.find { it.id == id }
            updatedTodo?.let { repository.updateTodo(it) }
        }
    }
    
    private fun deleteTodo(id: String) {
        viewModelScope.launch {
            repository.deleteTodo(id)
            
            _state.update { currentState ->
                currentState.copy(
                    todos = currentState.todos.filter { it.id != id }
                )
            }
        }
    }
    
    private fun updateFilter(filter: TodoFilter) {
        _state.update { it.copy(filter = filter) }
    }
}

/**
 * State
 */
data class TodoState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: TodoFilter = TodoFilter.ALL
) {
    // 필터링된 할 일 목록
    val filteredTodos: List<Todo>
        get() = when (filter) {
            TodoFilter.ALL -> todos
            TodoFilter.ACTIVE -> todos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> todos.filter { it.isCompleted }
        }
}

/**
 * Intent
 */
sealed class TodoIntent {
    object LoadTodos : TodoIntent()
    data class AddTodo(val title: String) : TodoIntent()
    data class ToggleTodo(val id: String) : TodoIntent()
    data class DeleteTodo(val id: String) : TodoIntent()
    data class FilterChanged(val filter: TodoFilter) : TodoIntent()
}

/**
 * Model
 */
data class Todo(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

enum class TodoFilter {
    ALL, ACTIVE, COMPLETED
}
```

---

## 완전한 앱 예제

### 프로젝트 구조

```
app/
├── data/
│   ├── model/
│   │   ├── Todo.kt
│   │   └── TodoFilter.kt
│   ├── local/
│   │   ├── TodoDao.kt
│   │   └── TodoDatabase.kt
│   └── repository/
│       ├── TodoRepository.kt
│       └── TodoRepositoryImpl.kt
├── di/
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── ui/
│   ├── TodoIntent.kt
│   ├── TodoState.kt
│   ├── TodoViewModel.kt
│   └── TodoScreen.kt
└── MainActivity.kt
```

### 전체 코드는 이전 섹션의 예제를 참고하세요.

---

## Best Practices

### 1. ViewModel에서 하지 말아야 할 것

```kotlin
// ❌ 나쁜 예
class BadViewModel : ViewModel() {
    // Android Context 참조 (메모리 누수!)
    private val context: Context
    
    // View 참조 (메모리 누수!)
    private val textView: TextView
    
    // 긴 작업을 메인 스레드에서 실행
    fun loadData() {
        val data = database.query() // UI 멈춤!
    }
}

// ✅ 좋은 예
class GoodViewModel @Inject constructor(
    // Application Context는 OK (앱 생명주기와 같음)
    @ApplicationContext private val context: Context,
    private val repository: Repository
) : ViewModel() {
    
    fun loadData() {
        viewModelScope.launch {
            // 백그라운드 스레드에서 실행
            val data = withContext(Dispatchers.IO) {
                repository.getData()
            }
        }
    }
}
```

### 2. State 업데이트는 불변성 유지

```kotlin
// ❌ 나쁜 예
_state.value.users.add(newUser) // 직접 수정 (불변성 위반!)

// ✅ 좋은 예
_state.update { currentState ->
    currentState.copy(
        users = currentState.users + newUser
    )
}
```

### 3. 에러 처리는 명확하게

```kotlin
// ✅ 좋은 예
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Exception) : UiState<Nothing>()
}

// 사용
when (val state = uiState) {
    is UiState.Loading -> ShowLoading()
    is UiState.Success -> ShowData(state.data)
    is UiState.Error -> ShowError(state.exception.message)
}
```

### 4. 일회성 이벤트는 SharedFlow 사용

```kotlin
class MyViewModel : ViewModel() {
    // 상태: StateFlow
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    
    // 이벤트: SharedFlow
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events
    
    fun saveData() {
        viewModelScope.launch {
            try {
                repository.save()
                _events.emit(UiEvent.ShowToast("저장 완료"))
                _events.emit(UiEvent.NavigateBack)
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowToast("저장 실패"))
            }
        }
    }
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}
```

### 5. Repository는 단일 책임 원칙

```kotlin
// ❌ 나쁜 예: 너무 많은 책임
class BadRepository {
    fun getUsers() { }
    fun getPosts() { }
    fun getComments() { }
    fun getPhotos() { }
}

// ✅ 좋은 예: 책임 분리
class UserRepository {
    fun getUsers() { }
    fun getUser(id: String) { }
    fun updateUser(user: User) { }
}

class PostRepository {
    fun getPosts() { }
    fun getPost(id: String) { }
}
```

---

## 📝 요약

### MVVM vs MVI 선택 가이드

```
간단한 앱 → MVVM
복잡한 상태 관리 → MVI
팀이 익숙한 패턴 → 그것 사용
새 프로젝트 → MVI 추천 (장기적으로 유리)
```

### 핵심 원칙

✅ **관심사의 분리**: UI, 로직, 데이터 분리  
✅ **단방향 데이터 플로우**: 예측 가능한 상태 관리  
✅ **불변성**: State는 항상 새로운 객체로 교체  
✅ **단일 진실 공급원**: 하나의 State가 모든 UI를 결정  
✅ **테스트 가능성**: ViewModel과 Repository를 쉽게 테스트

---

## 🎯 다음 단계

1. **간단한 Todo 앱 만들기**
   - MVVM으로 먼저 구현
   - MVI로 리팩토링

2. **관련 문서 학습**
   - [39-dependency-injection-hilt-guide.md](./39-dependency-injection-hilt-guide.md)
   - [40-kotlin-coroutines-flow-guide.md](./40-kotlin-coroutines-flow-guide.md)

3. **실전 프로젝트 적용**
   - 기존 프로젝트에 아키텍처 적용
   - 점진적으로 마이그레이션

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
