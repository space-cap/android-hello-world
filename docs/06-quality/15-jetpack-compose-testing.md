# Jetpack Compose 테스팅 가이드

> [!NOTE]
> **이 문서는 새로운 종합 가이드 시리즈로 대체되었습니다!**
> 
> 테스팅을 더 체계적으로 학습할 수 있도록 3개의 상세한 문서로 분할되었습니다:
> 
> 1. **[15-1. Jetpack Compose 테스팅 기초](./15-1-jetpack-compose-testing-basics.md)** - 기본 UI 테스트
> 2. **[15-2. Jetpack Compose 테스팅 고급](./15-2-jetpack-compose-testing-advanced.md)** - ViewModel, Repository 테스팅
> 3. **[15-3. Jetpack Compose 테스팅 실전 시나리오](./15-3-jetpack-compose-testing-scenarios.md)** - TDD, 실전 예제

---

## 📚 새로운 시리즈 구성

### 15-1. Jetpack Compose 테스팅 기초 (⭐ 시작점)
- 테스팅 개요 (왜 필요한가, 테스트 피라미드)
- 프로젝트 설정
- 기본 UI 테스트
- 간단한 예제

### 15-2. Jetpack Compose 테스팅 고급
- ViewModel 테스팅
- Repository 테스팅
- Navigation 테스팅
- 고급 Compose 테스팅

### 15-3. Jetpack Compose 테스팅 실전 시나리오
- TDD (Test-Driven Development)
- 실전 시나리오 5가지
- 테스트 커버리지
- CI/CD 통합

---

## 🚀 빠른 시작

**[👉 15-1. Jetpack Compose 테스팅 기초로 이동](./15-1-jetpack-compose-testing-basics.md)**

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪


---

## 테스팅 개요

### 테스팅의 중요성

- ✅ **버그 조기 발견**
- ✅ **리팩토링 안전성**
- ✅ **문서화 역할**
- ✅ **코드 품질 향상**

### 테스트 피라미드

```
        /\
       /  \  E2E Tests (적음)
      /____\
     /      \
    / UI Tests (중간)
   /___________\
  /             \
 / Unit Tests (많음)
/________________\
```

---

## 프로젝트 설정

### 의존성 추가

```kotlin
dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    
    // Mock
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
}
```

---

## UI 테스트

### 기본 UI 테스트

```kotlin
@RunWith(AndroidJUnit4::class)
class SimpleComposeTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun myTest() {
        // UI 설정
        composeTestRule.setContent {
            Text("Hello World")
        }
        
        // 검증
        composeTestRule
            .onNodeWithText("Hello World")
            .assertExists()
    }
}
```

### 버튼 클릭 테스트

```kotlin
@Test
fun buttonClickTest() {
    var clicked = false
    
    composeTestRule.setContent {
        Button(onClick = { clicked = true }) {
            Text("Click Me")
        }
    }
    
    // 버튼 클릭
    composeTestRule
        .onNodeWithText("Click Me")
        .performClick()
    
    // 검증
    assert(clicked)
}
```

### TextField 입력 테스트

```kotlin
@Test
fun textFieldInputTest() {
    composeTestRule.setContent {
        var text by remember { mutableStateOf("") }
        
        Column {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Name") }
            )
            Text(text = "입력: $text")
        }
    }
    
    // 텍스트 입력
    composeTestRule
        .onNodeWithText("Name")
        .performTextInput("John")
    
    // 검증
    composeTestRule
        .onNodeWithText("입력: John")
        .assertExists()
}
```

### 리스트 테스트

```kotlin
@Test
fun lazyColumnTest() {
    val items = listOf("Item 1", "Item 2", "Item 3")
    
    composeTestRule.setContent {
        LazyColumn {
            items(items) { item ->
                Text(item)
            }
        }
    }
    
    // 모든 아이템 검증
    items.forEach { item ->
        composeTestRule
            .onNodeWithText(item)
            .assertExists()
    }
}
```

### Semantics를 사용한 테스트

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text(
            text = "Count: $count",
            modifier = Modifier.semantics {
                testTag = "counter_text"
            }
        )
        Button(
            onClick = { count++ },
            modifier = Modifier.semantics {
                testTag = "increment_button"
            }
        ) {
            Text("Increment")
        }
    }
}

@Test
fun counterTest() {
    composeTestRule.setContent {
        Counter()
    }
    
    // 초기 상태 검증
    composeTestRule
        .onNodeWithTag("counter_text")
        .assertTextEquals("Count: 0")
    
    // 버튼 클릭
    composeTestRule
        .onNodeWithTag("increment_button")
        .performClick()
    
    // 업데이트된 상태 검증
    composeTestRule
        .onNodeWithTag("counter_text")
        .assertTextEquals("Count: 1")
}
```

### Navigation 테스트

```kotlin
@Test
fun navigationTest() {
    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    )
    
    composeTestRule.setContent {
        navController.navigatorProvider.addNavigator(
            ComposeNavigator()
        )
        
        NavHost(navController, startDestination = "home") {
            composable("home") {
                Button(onClick = { navController.navigate("details") }) {
                    Text("Go to Details")
                }
            }
            composable("details") {
                Text("Details Screen")
            }
        }
    }
    
    // 홈 화면에서 버튼 클릭
    composeTestRule
        .onNodeWithText("Go to Details")
        .performClick()
    
    // 상세 화면으로 이동 확인
    composeTestRule
        .onNodeWithText("Details Screen")
        .assertExists()
}
```

---

## ViewModel 테스트

### 기본 ViewModel 테스트

```kotlin
class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    fun increment() {
        _count.value++
    }
    
    fun decrement() {
        _count.value--
    }
}

class CounterViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun `increment increases count`() = runTest {
        val viewModel = CounterViewModel()
        
        // 초기 값 확인
        assertEquals(0, viewModel.count.value)
        
        // 증가
        viewModel.increment()
        
        // 검증
        assertEquals(1, viewModel.count.value)
    }
    
    @Test
    fun `decrement decreases count`() = runTest {
        val viewModel = CounterViewModel()
        
        viewModel.decrement()
        
        assertEquals(-1, viewModel.count.value)
    }
}

// MainDispatcherRule (테스트용 Dispatcher)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 비동기 작업 테스트

```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _users.value = repository.getUsers()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class UserViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val repository = mockk<UserRepository>()
    private lateinit var viewModel: UserViewModel
    
    @Before
    fun setup() {
        viewModel = UserViewModel(repository)
    }
    
    @Test
    fun `loadUsers updates users state`() = runTest {
        val expectedUsers = listOf(
            User(1, "John", "john@example.com"),
            User(2, "Jane", "jane@example.com")
        )
        
        coEvery { repository.getUsers() } returns expectedUsers
        
        viewModel.loadUsers()
        
        assertEquals(expectedUsers, viewModel.users.value)
        assertEquals(false, viewModel.isLoading.value)
    }
    
    @Test
    fun `loadUsers sets loading state`() = runTest {
        coEvery { repository.getUsers() } coAnswers {
            delay(100)
            emptyList()
        }
        
        viewModel.loadUsers()
        
        // 로딩 중
        assertEquals(true, viewModel.isLoading.value)
        
        advanceUntilIdle()
        
        // 로딩 완료
        assertEquals(false, viewModel.isLoading.value)
    }
}
```

### Flow 테스트 (Turbine)

```kotlin
@Test
fun `counter flow emits correct values`() = runTest {
    val viewModel = CounterViewModel()
    
    viewModel.count.test {
        // 초기 값
        assertEquals(0, awaitItem())
        
        // 증가
        viewModel.increment()
        assertEquals(1, awaitItem())
        
        viewModel.increment()
        assertEquals(2, awaitItem())
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Repository 테스트

### Repository 테스트

```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    suspend fun getUsers(): List<User> {
        return try {
            val users = apiService.getUsers()
            userDao.insertAll(users)
            users
        } catch (e: Exception) {
            userDao.getAllUsers()
        }
    }
}

class UserRepositoryTest {
    
    private val apiService = mockk<ApiService>()
    private val userDao = mockk<UserDao>()
    private lateinit var repository: UserRepository
    
    @Before
    fun setup() {
        repository = UserRepository(apiService, userDao)
    }
    
    @Test
    fun `getUsers returns API data on success`() = runTest {
        val expectedUsers = listOf(
            User(1, "John", "john@example.com")
        )
        
        coEvery { apiService.getUsers() } returns expectedUsers
        coEvery { userDao.insertAll(any()) } just Runs
        
        val result = repository.getUsers()
        
        assertEquals(expectedUsers, result)
        coVerify { userDao.insertAll(expectedUsers) }
    }
    
    @Test
    fun `getUsers returns cached data on API failure`() = runTest {
        val cachedUsers = listOf(
            User(1, "Cached", "cached@example.com")
        )
        
        coEvery { apiService.getUsers() } throws IOException()
        coEvery { userDao.getAllUsers() } returns cachedUsers
        
        val result = repository.getUsers()
        
        assertEquals(cachedUsers, result)
    }
}
```

---

## 실전 예제

### Todo 앱 테스트

```kotlin
// ViewModel
class TodoViewModel(
    private val repository: TodoRepository
) : ViewModel() {
    
    val todos = repository.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun addTodo(title: String) {
        viewModelScope.launch {
            repository.insert(Todo(title = title))
        }
    }
    
    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }
}

// ViewModel Test
class TodoViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val repository = mockk<TodoRepository>()
    private lateinit var viewModel: TodoViewModel
    
    @Before
    fun setup() {
        every { repository.getAllTodos() } returns flowOf(emptyList())
        viewModel = TodoViewModel(repository)
    }
    
    @Test
    fun `addTodo calls repository insert`() = runTest {
        coEvery { repository.insert(any()) } just Runs
        
        viewModel.addTodo("New Todo")
        
        coVerify {
            repository.insert(
                match { it.title == "New Todo" }
            )
        }
    }
    
    @Test
    fun `toggleTodo updates completion status`() = runTest {
        val todo = Todo(id = 1, title = "Test", isCompleted = false)
        coEvery { repository.update(any()) } just Runs
        
        viewModel.toggleTodo(todo)
        
        coVerify {
            repository.update(
                match { it.id == 1 && it.isCompleted == true }
            )
        }
    }
}

// UI Test
@RunWith(AndroidJUnit4::class)
class TodoScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun addTodoTest() {
        composeTestRule.setContent {
            TodoScreen()
        }
        
        // 새 Todo 입력
        composeTestRule
            .onNodeWithTag("todo_input")
            .performTextInput("Buy milk")
        
        // 추가 버튼 클릭
        composeTestRule
            .onNodeWithTag("add_button")
            .performClick()
        
        // Todo가 리스트에 표시되는지 확인
        composeTestRule
            .onNodeWithText("Buy milk")
            .assertExists()
    }
    
    @Test
    fun toggleTodoTest() {
        val todos = listOf(
            Todo(id = 1, title = "Test Todo", isCompleted = false)
        )
        
        composeTestRule.setContent {
            TodoList(todos = todos, onToggle = {})
        }
        
        // 체크박스 클릭
        composeTestRule
            .onNodeWithTag("todo_checkbox_1")
            .performClick()
        
        // 완료 상태 확인
        composeTestRule
            .onNodeWithTag("todo_checkbox_1")
            .assertIsOn()
    }
}
```

---

## 💡 베스트 프랙티스

### 1. Given-When-Then 패턴

```kotlin
@Test
fun exampleTest() {
    // Given (준비)
    val viewModel = MyViewModel()
    
    // When (실행)
    viewModel.doSomething()
    
    // Then (검증)
    assertEquals(expected, viewModel.state.value)
}
```

### 2. 테스트 이름은 명확하게

```kotlin
// ✅ 좋은 예
@Test
fun `increment button increases counter by one`()

// ❌ 나쁜 예
@Test
fun test1()
```

### 3. 각 테스트는 독립적으로

```kotlin
// ✅ 각 테스트마다 새로운 인스턴스
@Before
fun setup() {
    viewModel = MyViewModel()
}
```

### 4. Mock은 필요한 곳에만

```kotlin
// ✅ 외부 의존성만 Mock
val repository = mockk<Repository>()

// ❌ 테스트 대상을 Mock하지 않음
val viewModel = MyViewModel(repository) // 실제 객체
```

---

## 🎯 다음 단계

테스팅을 마스터했습니다! 다음으로:

1. **디버깅** - 문제 해결 기법
2. **앱 배포** - Google Play 배포

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
