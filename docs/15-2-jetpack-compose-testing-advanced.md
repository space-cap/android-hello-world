# Jetpack Compose 테스팅 고급

> 📖 **시리즈 구성**
> - **15-1**: [Jetpack Compose 테스팅 기초](./15-1-jetpack-compose-testing-basics.md)
> - **15-2**: Jetpack Compose 테스팅 고급 (현재 문서)
> - **15-3**: [Jetpack Compose 테스팅 실전 시나리오](./15-3-jetpack-compose-testing-scenarios.md)

---

## 📚 목차

1. [ViewModel 테스팅](#viewmodel-테스팅)
2. [Repository 테스팅](#repository-테스팅)
3. [Navigation 테스팅](#navigation-테스팅)
4. [고급 Compose 테스팅](#고급-compose-테스팅)

---

## ViewModel 테스팅

### MainDispatcherRule 설정

```kotlin
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

### 기본 ViewModel 테스트

```kotlin
class CounterViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun `increment increases count`() = runTest {
        val viewModel = CounterViewModel()
        
        assertEquals(0, viewModel.count.value)
        
        viewModel.increment()
        
        assertEquals(1, viewModel.count.value)
    }
}
```

### Flow 테스팅 (Turbine)

```kotlin
@Test
fun `counter flow emits correct values`() = runTest {
    val viewModel = CounterViewModel()
    
    viewModel.count.test {
        assertEquals(0, awaitItem())
        
        viewModel.increment()
        assertEquals(1, awaitItem())
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Repository 테스팅

### MockK 사용법

```kotlin
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
        val expectedUsers = listOf(User(1, "John", "john@example.com"))
        
        coEvery { apiService.getUsers() } returns expectedUsers
        coEvery { userDao.insertAll(any()) } just Runs
        
        val result = repository.getUsers()
        
        assertEquals(expectedUsers, result)
        coVerify { userDao.insertAll(expectedUsers) }
    }
}
```

---

## Navigation 테스팅

```kotlin
@Test
fun navigationTest() {
    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    )
    
    composeTestRule.setContent {
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        
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
    
    composeTestRule
        .onNodeWithText("Go to Details")
        .performClick()
    
    composeTestRule
        .onNodeWithText("Details Screen")
        .assertExists()
}
```

---

## 고급 Compose 테스팅

### Semantics 커스터마이징

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text(
            text = "Count: $count",
            modifier = Modifier.semantics { testTag = "counter_text" }
        )
        Button(
            onClick = { count++ },
            modifier = Modifier.semantics { testTag = "increment_button" }
        ) {
            Text("Increment")
        }
    }
}

@Test
fun counterTest() {
    composeTestRule.setContent { Counter() }
    
    composeTestRule.onNodeWithTag("counter_text").assertTextEquals("Count: 0")
    composeTestRule.onNodeWithTag("increment_button").performClick()
    composeTestRule.onNodeWithTag("counter_text").assertTextEquals("Count: 1")
}
```

### 비동기 테스트

```kotlin
@Test
fun waitUntilTest() {
    composeTestRule.setContent {
        var isLoading by remember { mutableStateOf(true) }
        
        LaunchedEffect(Unit) {
            delay(1000)
            isLoading = false
        }
        
        if (isLoading) {
            Text("Loading...")
        } else {
            Text("Loaded!")
        }
    }
    
    composeTestRule.waitUntil(timeoutMillis = 2000) {
        composeTestRule
            .onAllNodesWithText("Loaded!")
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
}
```

---

## 🎯 다음 단계

고급 테스팅을 마스터했습니다! 다음으로:

1. **[15-3. Jetpack Compose 테스팅 실전 시나리오](./15-3-jetpack-compose-testing-scenarios.md)** - TDD, 실전 예제

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
