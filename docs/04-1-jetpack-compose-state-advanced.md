# Jetpack Compose State 고급 패턴

> 📖 **State 가이드 시리즈**
> - **04**: [State 완벽 가이드](./04-jetpack-compose-state-guide.md) - 기초부터 ViewModel까지
> - **04-1**: State 고급 패턴 (현재 문서) - Side Effect, 성능 최적화
> - **04-2**: [State 실전 프로젝트](./04-2-jetpack-compose-state-projects.md) - 메모, 타이머, 채팅 앱
> - **04-3**: [State 치트시트](./04-3-jetpack-compose-state-cheatsheet.md) - 핵심 요약, 템플릿

---

## 📚 목차

1. [Side Effect 완벽 가이드](#side-effect-완벽-가이드)
2. [고급 State 패턴](#고급-state-패턴)
3. [성능 최적화](#성능-최적화)
4. [State 테스팅](#state-테스팅)

---

## Side Effect 완벽 가이드

### 🎯 Side Effect란?

**Side Effect**는 Composable 함수의 범위 밖에서 발생하는 상태 변경입니다.

```
Composable 함수 내부
    ↓
API 호출, DB 저장, 센서 접근 등
    ↓
외부 상태 변경 (Side Effect)
```

### 1. LaunchedEffect (코루틴 실행)

```kotlin
@Composable
fun UserProfile(userId: String) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // userId가 변경될 때마다 실행
    LaunchedEffect(userId) {
        isLoading = true
        user = loadUser(userId)  // suspend 함수
        isLoading = false
    }
    
    when {
        isLoading -> CircularProgressIndicator()
        user != null -> UserCard(user!!)
        else -> Text("사용자를 찾을 수 없습니다")
    }
}
```

**언제 사용?**
- API 호출
- 데이터베이스 쿼리
- 타이머/애니메이션
- 일회성 이벤트

**주의사항**:
```kotlin
// ❌ 나쁜 예: key 없음 → 한 번만 실행
LaunchedEffect(Unit) {
    loadData()  // Recomposition 시 실행 안 됨
}

// ✅ 좋은 예: key 지정 → key 변경 시 재실행
LaunchedEffect(userId) {
    loadData(userId)  // userId 변경 시 재실행
}
```

### 2. DisposableEffect (정리 작업)

```kotlin
@Composable
fun LocationScreen() {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        // 시작 시
        val locationManager = context.getSystemService<LocationManager>()
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 위치 업데이트
            }
        }
        
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            10f,
            listener
        )
        
        // 종료 시 (중요!)
        onDispose {
            locationManager?.removeUpdates(listener)
            Log.d("Location", "리스너 제거")
        }
    }
}
```

**언제 사용?**
- 센서 리스너 등록/해제
- 네트워크 연결 관리
- 타이머 시작/정지
- 리소스 정리

### 3. SideEffect (Compose 외부 동기화)

```kotlin
@Composable
fun AnalyticsScreen(screenName: String) {
    // Recomposition 성공 시마다 실행
    SideEffect {
        // Analytics에 화면 이름 전송
        Analytics.logScreenView(screenName)
    }
}
```

**언제 사용?**
- Analytics 이벤트
- Compose 외부 상태 동기화
- 성공적인 Recomposition 추적

### 4. rememberCoroutineScope (이벤트 핸들러)

```kotlin
@Composable
fun SnackbarScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Button(
            onClick = {
                // 이벤트 핸들러에서 코루틴 실행
                scope.launch {
                    snackbarHostState.showSnackbar("저장되었습니다")
                }
            }
        ) {
            Text("저장")
        }
    }
}
```

**언제 사용?**
- 버튼 클릭 핸들러
- 스크롤 이벤트
- 사용자 입력 처리

### 📊 Side Effect 비교

| Effect | 실행 시점 | 정리 작업 | 사용 사례 |
|--------|----------|----------|----------|
| **LaunchedEffect** | Composition 시작 | ✅ 자동 취소 | API 호출, 타이머 |
| **DisposableEffect** | Composition 시작 | ✅ onDispose | 리스너 등록/해제 |
| **SideEffect** | Recomposition 성공 | ❌ 없음 | Analytics |
| **rememberCoroutineScope** | 이벤트 발생 시 | ✅ 자동 취소 | 버튼 클릭 |

---

## 고급 State 패턴

### 1. produceState (비동기 → State)

```kotlin
@Composable
fun ImageLoader(imageUrl: String): State<Bitmap?> {
    return produceState<Bitmap?>(initialValue = null, imageUrl) {
        // 백그라운드에서 이미지 로드
        value = withContext(Dispatchers.IO) {
            loadImage(imageUrl)
        }
    }
}

@Composable
fun ImageScreen(imageUrl: String) {
    val bitmap by ImageLoader(imageUrl)
    
    when (bitmap) {
        null -> CircularProgressIndicator()
        else -> Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = null)
    }
}
```

**언제 사용?**
- Flow → State 변환
- 비동기 데이터 로딩
- 외부 데이터 소스 구독

### 2. snapshotFlow (State → Flow)

```kotlin
@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(300)  // 300ms 대기
            .filter { it.length >= 2 }
            .collect { searchQuery ->
                val results = performSearch(searchQuery)
                searchResults.clear()
                searchResults.addAll(results)
            }
    }
    
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("검색") }
        )
        
        LazyColumn {
            items(searchResults) { result ->
                Text(result)
            }
        }
    }
}
```

**언제 사용?**
- Debounce/Throttle
- State 변경 감지
- 복잡한 Flow 연산

### 3. rememberUpdatedState (최신 값 유지)

```kotlin
@Composable
fun TimerWithCallback(
    onTimeout: () -> Unit
) {
    // onTimeout이 변경되어도 LaunchedEffect 재시작 안 함
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    
    LaunchedEffect(Unit) {
        delay(5000)
        currentOnTimeout()  // 항상 최신 콜백 사용
    }
}
```

**언제 사용?**
- LaunchedEffect에서 최신 콜백 사용
- 재시작 없이 값 업데이트

---

## 성능 최적화

### 1. Recomposition 최적화

```kotlin
// ❌ 나쁜 예: 매번 새 객체 생성
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            UserCard(
                user = user,
                onClick = { println(user.name) }  // 매번 새 람다
            )
        }
    }
}

// ✅ 좋은 예: 안정적인 참조
@Composable
fun UserList(
    users: List<User>,
    onUserClick: (User) -> Unit  // 외부에서 전달
) {
    LazyColumn {
        items(
            items = users,
            key = { it.id }  // 안정적인 key
        ) { user ->
            UserCard(
                user = user,
                onClick = { onUserClick(user) }
            )
        }
    }
}
```

### 2. Immutable/Stable 어노테이션

```kotlin
// ✅ Immutable: 절대 변하지 않음
@Immutable
data class User(
    val id: String,
    val name: String,
    val email: String
)

// ✅ Stable: 변할 수 있지만 Compose가 감지 가능
@Stable
class UserRepository {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
}
```

### 3. derivedStateOf 활용

```kotlin
@Composable
fun FilteredList(items: List<Item>, filter: String) {
    // ❌ 나쁜 예: 매 Recomposition마다 계산
    val filteredItems = items.filter { it.name.contains(filter) }
    
    // ✅ 좋은 예: items나 filter 변경 시에만 계산
    val filteredItems by remember(items, filter) {
        derivedStateOf {
            items.filter { it.name.contains(filter) }
        }
    }
}
```

### 4. Key 사용

```kotlin
@Composable
fun TodoList(todos: List<Todo>) {
    LazyColumn {
        items(
            items = todos,
            key = { it.id }  // ← 중요!
        ) { todo ->
            TodoItem(todo)
        }
    }
}
```

**왜 중요?**
- 리스트 재정렬 시 성능 향상
- 애니메이션 정확도
- 불필요한 Recomposition 방지

---

## State 테스팅

### 1. ViewModel 테스트

```kotlin
class TodoViewModelTest {
    
    @Test
    fun `Todo 추가 테스트`() = runTest {
        // Given
        val viewModel = TodoViewModel()
        
        // When
        viewModel.addTodo("새 할 일")
        
        // Then
        val todos = viewModel.todos.value
        assertEquals(1, todos.size)
        assertEquals("새 할 일", todos[0].title)
    }
    
    @Test
    fun `Todo 완료 토글 테스트`() = runTest {
        // Given
        val viewModel = TodoViewModel()
        viewModel.addTodo("할 일")
        val todoId = viewModel.todos.value[0].id
        
        // When
        viewModel.toggleTodo(todoId)
        
        // Then
        assertTrue(viewModel.todos.value[0].completed)
    }
}
```

### 2. Composable 테스트

```kotlin
@Test
fun testCounter() = runComposeUiTest {
    setContent {
        var count by remember { mutableStateOf(0) }
        
        Column {
            Text("카운트: $count")
            Button(onClick = { count++ }) {
                Text("증가")
            }
        }
    }
    
    // 초기 상태 확인
    onNodeWithText("카운트: 0").assertExists()
    
    // 버튼 클릭
    onNodeWithText("증가").performClick()
    
    // 변경된 상태 확인
    onNodeWithText("카운트: 1").assertExists()
}
```

---

## 🎯 다음 단계

고급 패턴을 익혔다면, 이제 실전 프로젝트로 넘어가세요:

**[04-2. State 실전 프로젝트](./04-2-jetpack-compose-state-projects.md)**
- 메모 앱 (CRUD, 검색, 정렬)
- 타이머 앱 (백그라운드, 알림)
- 채팅 앱 UI (Flow, 무한 스크롤)

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy Advanced Coding! 🚀
