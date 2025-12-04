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
4. [비동기 테스팅](#비동기-테스팅)
5. [고급 기법](#고급-기법)

---

## ViewModel 테스팅

### MainDispatcherRule 설정

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * MainDispatcherRule: 코루틴 테스트를 위한 Rule
 * 
 * 역할:
 * - Main Dispatcher를 Test Dispatcher로 교체
 * - 테스트 후 원래대로 복원
 */
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    /**
     * 테스트 시작 전 실행
     */
    override fun starting(description: Description) {
        // Main Dispatcher를 Test Dispatcher로 교체
        Dispatchers.setMain(testDispatcher)
    }
    
    /**
     * 테스트 종료 후 실행
     */
    override fun finished(description: Description) {
        // Main Dispatcher 복원
        Dispatchers.resetMain()
    }
}

/**
 * TestDispatcher 종류
 */
// 1. UnconfinedTestDispatcher: 즉시 실행 (기본)
val unconfinedDispatcher = UnconfinedTestDispatcher()

// 2. StandardTestDispatcher: 수동 제어
val standardDispatcher = StandardTestDispatcher()
```

### 기본 ViewModel 테스트

```kotlin
/**
 * 카운터 ViewModel
 */
class CounterViewModel : ViewModel() {
    
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    /**
     * 카운트 증가
     */
    fun increment() {
        _count.value++
    }
    
    /**
     * 카운트 감소
     */
    fun decrement() {
        _count.value--
    }
    
    /**
     * 카운트 리셋
     */
    fun reset() {
        _count.value = 0
    }
}

/**
 * CounterViewModel 테스트
 */
class CounterViewModelTest {
    
    /**
     * MainDispatcherRule 적용
     */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: CounterViewModel
    
    @Before
    fun setup() {
        viewModel = CounterViewModel()
    }
    
    /**
     * 테스트 1: 초기 상태 확인
     */
    @Test
    fun `초기 카운트는 0이다`() = runTest {
        assertEquals(0, viewModel.count.value)
    }
    
    /**
     * 테스트 2: 증가 기능
     */
    @Test
    fun `increment 호출 시 카운트가 증가한다`() = runTest {
        // Given: 초기 상태
        assertEquals(0, viewModel.count.value)
        
        // When: increment 호출
        viewModel.increment()
        
        // Then: 카운트 증가 확인
        assertEquals(1, viewModel.count.value)
    }
    
    /**
     * 테스트 3: 여러 번 증가
     */
    @Test
    fun `increment를 5번 호출하면 카운트가 5가 된다`() = runTest {
        repeat(5) {
            viewModel.increment()
        }
        
        assertEquals(5, viewModel.count.value)
    }
    
    /**
     * 테스트 4: 감소 기능
     */
    @Test
    fun `decrement 호출 시 카운트가 감소한다`() = runTest {
        viewModel.increment()  // 1
        viewModel.decrement()  // 0
        
        assertEquals(0, viewModel.count.value)
    }
    
    /**
     * 테스트 5: 리셋 기능
     */
    @Test
    fun `reset 호출 시 카운트가 0이 된다`() = runTest {
        repeat(10) { viewModel.increment() }
        
        viewModel.reset()
        
        assertEquals(0, viewModel.count.value)
    }
}
```

### Flow 테스팅 (Turbine)

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("app.cash.turbine:turbine:1.0.0")
}

/**
 * Turbine을 사용한 Flow 테스트
 */
class FlowViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    /**
     * 테스트 1: Flow 값 변화 확인
     */
    @Test
    fun `카운터 Flow가 올바른 값을 emit한다`() = runTest {
        val viewModel = CounterViewModel()
        
        // Turbine의 test 확장 함수 사용
        viewModel.count.test {
            // 초기값 확인
            assertEquals(0, awaitItem())
            
            // increment 후 값 확인
            viewModel.increment()
            assertEquals(1, awaitItem())
            
            // increment 후 값 확인
            viewModel.increment()
            assertEquals(2, awaitItem())
            
            // 테스트 종료
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    /**
     * 테스트 2: 여러 이벤트 확인
     */
    @Test
    fun `여러 작업 후 Flow 값 확인`() = runTest {
        val viewModel = CounterViewModel()
        
        viewModel.count.test {
            assertEquals(0, awaitItem())
            
            viewModel.increment()
            assertEquals(1, awaitItem())
            
            viewModel.increment()
            assertEquals(2, awaitItem())
            
            viewModel.decrement()
            assertEquals(1, awaitItem())
            
            viewModel.reset()
            assertEquals(0, awaitItem())
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### 복잡한 ViewModel 테스트

```kotlin
/**
 * 로그인 ViewModel
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * 로그인 실행
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // 로딩 상태
            _uiState.value = LoginUiState.Loading
            
            try {
                // 입력 검증
                if (email.isBlank()) {
                    _uiState.value = LoginUiState.Error("이메일을 입력하세요")
                    return@launch
                }
                
                if (password.length < 6) {
                    _uiState.value = LoginUiState.Error("비밀번호는 6자 이상이어야 합니다")
                    return@launch
                }
                
                // API 호출
                val user = authRepository.login(email, password)
                
                // 성공 상태
                _uiState.value = LoginUiState.Success(user)
                
            } catch (e: Exception) {
                // 에러 상태
                _uiState.value = LoginUiState.Error(e.message ?: "로그인 실패")
            }
        }
    }
}

/**
 * 로그인 UI 상태
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * LoginViewModel 테스트
 */
class LoginViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val authRepository = mockk<AuthRepository>()
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup() {
        viewModel = LoginViewModel(authRepository)
    }
    
    /**
     * 테스트 1: 초기 상태
     */
    @Test
    fun `초기 상태는 Idle이다`() = runTest {
        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }
    
    /**
     * 테스트 2: 빈 이메일 검증
     */
    @Test
    fun `빈 이메일로 로그인 시 에러 상태가 된다`() = runTest {
        viewModel.uiState.test {
            assertEquals(LoginUiState.Idle, awaitItem())
            
            viewModel.login("", "password123")
            
            assertEquals(LoginUiState.Loading, awaitItem())
            
            val errorState = awaitItem()
            assertTrue(errorState is LoginUiState.Error)
            assertEquals("이메일을 입력하세요", (errorState as LoginUiState.Error).message)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    /**
     * 테스트 3: 짧은 비밀번호 검증
     */
    @Test
    fun `짧은 비밀번호로 로그인 시 에러 상태가 된다`() = runTest {
        viewModel.uiState.test {
            awaitItem()  // Idle
            
            viewModel.login("test@example.com", "12345")
            
            awaitItem()  // Loading
            
            val errorState = awaitItem()
            assertTrue(errorState is LoginUiState.Error)
            assertEquals("비밀번호는 6자 이상이어야 합니다", (errorState as LoginUiState.Error).message)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    /**
     * 테스트 4: 성공적인 로그인
     */
    @Test
    fun `올바른 정보로 로그인 시 성공 상태가 된다`() = runTest {
        // Given: Mock 설정
        val expectedUser = User(1, "홍길동", "test@example.com")
        coEvery { authRepository.login(any(), any()) } returns expectedUser
        
        // When & Then
        viewModel.uiState.test {
            awaitItem()  // Idle
            
            viewModel.login("test@example.com", "password123")
            
            awaitItem()  // Loading
            
            val successState = awaitItem()
            assertTrue(successState is LoginUiState.Success)
            assertEquals(expectedUser, (successState as LoginUiState.Success).user)
            
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify: Repository 호출 확인
        coVerify { authRepository.login("test@example.com", "password123") }
    }
    
    /**
     * 테스트 5: 로그인 실패
     */
    @Test
    fun `로그인 실패 시 에러 상태가 된다`() = runTest {
        // Given: Mock이 예외 던지도록 설정
        coEvery { authRepository.login(any(), any()) } throws Exception("네트워크 오류")
        
        // When & Then
        viewModel.uiState.test {
            awaitItem()  // Idle
            
            viewModel.login("test@example.com", "password123")
            
            awaitItem()  // Loading
            
            val errorState = awaitItem()
            assertTrue(errorState is LoginUiState.Error)
            assertEquals("네트워크 오류", (errorState as LoginUiState.Error).message)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## Repository 테스팅

### MockK 기본 사용법

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("io.mockk:mockk:1.13.8")
}

/**
 * MockK 기본 문법
 */
class MockKBasicsTest {
    
    /**
     * Mock 객체 생성
     */
    @Test
    fun mockCreation() {
        // 인터페이스 Mock
        val apiService = mockk<ApiService>()
        
        // 클래스 Mock
        val repository = mockk<UserRepository>()
        
        // Relaxed Mock (기본 반환값 자동 생성)
        val relaxedMock = mockk<ApiService>(relaxed = true)
    }
    
    /**
     * 동작 정의 (every)
     */
    @Test
    fun mockBehavior() {
        val apiService = mockk<ApiService>()
        
        // 반환값 설정
        every { apiService.getUsers() } returns listOf(User(1, "홍길동"))
        
        // 여러 호출에 대한 반환값
        every { apiService.getUser(1) } returns User(1, "홍길동")
        every { apiService.getUser(2) } returns User(2, "김철수")
        
        // 예외 던지기
        every { apiService.getUser(999) } throws Exception("사용자 없음")
    }
    
    /**
     * 코루틴 함수 Mock (coEvery)
     */
    @Test
    fun mockSuspendFunction() = runTest {
        val apiService = mockk<ApiService>()
        
        // suspend 함수는 coEvery 사용
        coEvery { apiService.fetchUsers() } returns listOf(User(1, "홍길동"))
        
        val result = apiService.fetchUsers()
        assertEquals(1, result.size)
    }
    
    /**
     * 호출 검증 (verify)
     */
    @Test
    fun verifyCall() {
        val apiService = mockk<ApiService>(relaxed = true)
        
        apiService.getUsers()
        apiService.getUser(1)
        
        // 호출 확인
        verify { apiService.getUsers() }
        verify { apiService.getUser(1) }
        
        // 호출 횟수 확인
        verify(exactly = 1) { apiService.getUsers() }
        
        // 호출 안 됨 확인
        verify(exactly = 0) { apiService.getUser(2) }
    }
}
```

### Repository 테스트 예제

```kotlin
/**
 * UserRepository
 */
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    /**
     * 사용자 목록 가져오기
     * 
     * 1. API에서 데이터 가져오기
     * 2. DB에 저장
     * 3. 반환
     */
    suspend fun getUsers(): List<User> {
        val users = apiService.fetchUsers()
        userDao.insertAll(users)
        return users
    }
    
    /**
     * 특정 사용자 가져오기
     * 
     * 1. DB에서 먼저 확인
     * 2. 없으면 API 호출
     * 3. DB에 저장 후 반환
     */
    suspend fun getUser(id: Int): User {
        // DB 확인
        val cachedUser = userDao.getUser(id)
        if (cachedUser != null) {
            return cachedUser
        }
        
        // API 호출
        val user = apiService.fetchUser(id)
        userDao.insert(user)
        return user
    }
}

/**
 * UserRepository 테스트
 */
class UserRepositoryTest {
    
    private val apiService = mockk<ApiService>()
    private val userDao = mockk<UserDao>()
    private lateinit var repository: UserRepository
    
    @Before
    fun setup() {
        repository = UserRepository(apiService, userDao)
    }
    
    /**
     * 테스트 1: getUsers 성공
     */
    @Test
    fun `getUsers는 API 데이터를 반환하고 DB에 저장한다`() = runTest {
        // Given
        val expectedUsers = listOf(
            User(1, "홍길동", "hong@example.com"),
            User(2, "김철수", "kim@example.com")
        )
        
        coEvery { apiService.fetchUsers() } returns expectedUsers
        coEvery { userDao.insertAll(any()) } just Runs  // Unit 반환 함수
        
        // When
        val result = repository.getUsers()
        
        // Then
        assertEquals(expectedUsers, result)
        
        // Verify: DB 저장 확인
        coVerify { userDao.insertAll(expectedUsers) }
    }
    
    /**
     * 테스트 2: getUser - 캐시 히트
     */
    @Test
    fun `getUser는 DB에 데이터가 있으면 API를 호출하지 않는다`() = runTest {
        // Given: DB에 데이터 있음
        val cachedUser = User(1, "홍길동", "hong@example.com")
        coEvery { userDao.getUser(1) } returns cachedUser
        
        // When
        val result = repository.getUser(1)
        
        // Then
        assertEquals(cachedUser, result)
        
        // Verify: API 호출 안 됨
        coVerify(exactly = 0) { apiService.fetchUser(any()) }
    }
    
    /**
     * 테스트 3: getUser - 캐시 미스
     */
    @Test
    fun `getUser는 DB에 데이터가 없으면 API를 호출한다`() = runTest {
        // Given: DB에 데이터 없음
        val apiUser = User(1, "홍길동", "hong@example.com")
        coEvery { userDao.getUser(1) } returns null
        coEvery { apiService.fetchUser(1) } returns apiUser
        coEvery { userDao.insert(any()) } just Runs
        
        // When
        val result = repository.getUser(1)
        
        // Then
        assertEquals(apiUser, result)
        
        // Verify: API 호출 및 DB 저장
        coVerify { apiService.fetchUser(1) }
        coVerify { userDao.insert(apiUser) }
    }
    
    /**
     * 테스트 4: API 에러 처리
     */
    @Test
    fun `getUsers는 API 에러 시 예외를 던진다`() = runTest {
        // Given: API가 예외 던짐
        coEvery { apiService.fetchUsers() } throws Exception("네트워크 오류")
        
        // When & Then: 예외 발생 확인
        assertThrows<Exception> {
            repository.getUsers()
        }
    }
}
```

---

## Navigation 테스팅

### 기본 Navigation 테스트

```kotlin
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider

/**
 * Navigation 테스트
 */
class NavigationTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var navController: TestNavHostController
    
    @Before
    fun setup() {
        composeTestRule.setContent {
            // TestNavHostController 생성
            navController = TestNavHostController(
                ApplicationProvider.getApplicationContext()
            )
            
            // ComposeNavigator 추가
            navController.navigatorProvider.addNavigator(
                ComposeNavigator()
            )
            
            // NavHost 설정
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigateToDetails = {
                            navController.navigate("details")
                        }
                    )
                }
                composable("details") {
                    DetailsScreen()
                }
            }
        }
    }
    
    /**
     * 테스트 1: 초기 화면 확인
     */
    @Test
    fun navHost_startsAtHomeScreen() {
        // 현재 경로 확인
        assertEquals("home", navController.currentBackStackEntry?.destination?.route)
        
        // 홈 화면 표시 확인
        composeTestRule.onNodeWithText("홈 화면").assertIsDisplayed()
    }
    
    /**
     * 테스트 2: 화면 전환
     */
    @Test
    fun navHost_navigatesToDetailsScreen() {
        // 상세 화면으로 이동 버튼 클릭
        composeTestRule.onNodeWithText("상세 보기").performClick()
        
        // 현재 경로 확인
        assertEquals("details", navController.currentBackStackEntry?.destination?.route)
        
        // 상세 화면 표시 확인
        composeTestRule.onNodeWithText("상세 화면").assertIsDisplayed()
    }
    
    /**
     * 테스트 3: 뒤로 가기
     */
    @Test
    fun navHost_navigatesBack() {
        // 상세 화면으로 이동
        composeTestRule.onNodeWithText("상세 보기").performClick()
        
        // 뒤로 가기
        navController.popBackStack()
        
        // 홈 화면으로 돌아왔는지 확인
        assertEquals("home", navController.currentBackStackEntry?.destination?.route)
        composeTestRule.onNodeWithText("홈 화면").assertIsDisplayed()
    }
}

/**
 * 화면 Composable
 */
@Composable
fun HomeScreen(onNavigateToDetails: () -> Unit) {
    Column {
        Text("홈 화면")
        Button(onClick = onNavigateToDetails) {
            Text("상세 보기")
        }
    }
}

@Composable
fun DetailsScreen() {
    Text("상세 화면")
}
```

### 파라미터가 있는 Navigation 테스트

```kotlin
/**
 * 파라미터 전달 테스트
 */
@Test
fun navHost_passesArgumentsCorrectly() {
    val userId = 123
    
    composeTestRule.setContent {
        navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        
        NavHost(navController, startDestination = "home") {
            composable("home") {
                Button(onClick = {
                    navController.navigate("user/$userId")
                }) {
                    Text("사용자 보기")
                }
            }
            composable(
                route = "user/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("userId")
                Text("사용자 ID: $id")
            }
        }
    }
    
    // 사용자 화면으로 이동
    composeTestRule.onNodeWithText("사용자 보기").performClick()
    
    // 파라미터 확인
    val currentUserId = navController.currentBackStackEntry
        ?.arguments?.getInt("userId")
    assertEquals(userId, currentUserId)
    
    // UI 확인
    composeTestRule.onNodeWithText("사용자 ID: 123").assertIsDisplayed()
}
```

---

## 비동기 테스팅

### waitUntil 사용

```kotlin
/**
 * 비동기 작업 대기
 */
@Test
fun waitUntil_waitsForAsyncOperation() {
    composeTestRule.setContent {
        var isLoading by remember { mutableStateOf(true) }
        var data by remember { mutableStateOf("") }
        
        LaunchedEffect(Unit) {
            delay(1000)  // 1초 대기
            data = "로드 완료"
            isLoading = false
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.testTag("loading")
            )
        } else {
            Text(data, modifier = Modifier.testTag("data"))
        }
    }
    
    // 로딩 표시 확인
    composeTestRule.onNodeWithTag("loading").assertExists()
    
    // 데이터 로드 대기 (최대 2초)
    composeTestRule.waitUntil(timeoutMillis = 2000) {
        composeTestRule
            .onAllNodesWithTag("data")
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
    
    // 데이터 표시 확인
    composeTestRule.onNodeWithTag("data").assertTextEquals("로드 완료")
}
```

### mainClock 제어

```kotlin
/**
 * 시간 제어 테스트
 */
@Test
fun mainClock_controlsTime() {
    composeTestRule.mainClock.autoAdvance = false  // 자동 진행 비활성화
    
    composeTestRule.setContent {
        var count by remember { mutableStateOf(0) }
        
        LaunchedEffect(Unit) {
            repeat(5) {
                delay(1000)
                count++
            }
        }
        
        Text("Count: $count", modifier = Modifier.testTag("count"))
    }
    
    // 초기 상태
    composeTestRule.onNodeWithTag("count").assertTextEquals("Count: 0")
    
    // 1초 진행
    composeTestRule.mainClock.advanceTimeBy(1000)
    composeTestRule.onNodeWithTag("count").assertTextEquals("Count: 1")
    
    // 2초 진행
    composeTestRule.mainClock.advanceTimeBy(2000)
    composeTestRule.onNodeWithTag("count").assertTextEquals("Count: 3")
    
    // 완료까지 진행
    composeTestRule.mainClock.advanceTimeBy(2000)
    composeTestRule.onNodeWithTag("count").assertTextEquals("Count: 5")
}
```

---

## 고급 기법

### Custom Semantics

```kotlin
/**
 * Custom Semantics 정의
 */
val ProgressSemantics = SemanticsPropertyKey<Float>("Progress")
var SemanticsPropertyReceiver.progress by ProgressSemantics

@Composable
fun ProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier.semantics {
            this.progress = progress  // Custom Semantics 설정
        }
    )
}

/**
 * Custom Semantics 테스트
 */
@Test
fun customSemantics_test() {
    composeTestRule.setContent {
        ProgressBar(progress = 0.5f)
    }
    
    // Custom Semantics로 검증
    composeTestRule.onNode(
        SemanticsMatcher.expectValue(ProgressSemantics, 0.5f)
    ).assertExists()
}
```

### Screenshot 테스트

```kotlin
/**
 * Screenshot 비교 테스트
 */
@Test
fun screenshot_matchesGolden() {
    composeTestRule.setContent {
        MyApp()
    }
    
    // Screenshot 캡처 및 비교
    composeTestRule.onRoot()
        .captureToImage()
        .assertAgainstGolden("my_app_screenshot")
}
```

---

## 💡 베스트 프랙티스 요약

### ViewModel 테스팅
- ✅ MainDispatcherRule 사용
- ✅ Turbine으로 Flow 테스트
- ✅ Given-When-Then 패턴
- ✅ 모든 상태 전환 테스트

### Repository 테스팅
- ✅ MockK로 의존성 Mock
- ✅ coEvery/coVerify 사용
- ✅ 성공/실패 케이스 모두 테스트
- ✅ 캐시 로직 검증

### Navigation 테스팅
- ✅ TestNavHostController 사용
- ✅ 경로 및 파라미터 검증
- ✅ 뒤로 가기 동작 테스트

### 비동기 테스팅
- ✅ waitUntil로 대기
- ✅ mainClock으로 시간 제어
- ✅ 타임아웃 설정
- ✅ runTest 사용

---

## 🎯 다음 단계

고급 테스팅을 마스터했습니다! 다음으로:

1. **[15-3. Jetpack Compose 테스팅 실전 시나리오](./15-3-jetpack-compose-testing-scenarios.md)** - TDD, 실전 예제

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
