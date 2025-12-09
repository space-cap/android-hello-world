# Jetpack Compose 테스팅 실전 시나리오

> 📖 **시리즈 구성**
> - **15-1**: [Jetpack Compose 테스팅 기초](./15-1-jetpack-compose-testing-basics.md)
> - **15-2**: [Jetpack Compose 테스팅 고급](./15-2-jetpack-compose-testing-advanced.md)
> - **15-3**: Jetpack Compose 테스팅 실전 시나리오 (현재 문서)

---

## 📚 목차

1. [TDD (Test-Driven Development)](#tdd-test-driven-development)
2. [실전 시나리오](#실전-시나리오)
3. [테스트 커버리지](#테스트-커버리지)
4. [CI/CD 통합](#cicd-통합)
5. [베스트 프랙티스](#베스트-프랙티스)

---

## TDD (Test-Driven Development)

### Red-Green-Refactor 사이클

**TDD의 핵심 원칙: 테스트를 먼저 작성하고, 코드는 나중에!**

```kotlin
/**
 * TDD 사이클 예제: 할 일 목록 앱
 */

// ========== 1단계: RED (실패하는 테스트 작성) ==========

/**
 * 테스트 먼저 작성 (아직 TodoViewModel이 없음)
 */
class TodoViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun `할 일 추가 시 목록에 추가된다`() {
        // Given: ViewModel 생성
        val viewModel = TodoViewModel()  // ❌ 컴파일 에러! 아직 없음
        
        // When: 할 일 추가
        viewModel.addTodo("우유 사기")
        
        // Then: 목록에 추가되었는지 확인
        val todos = viewModel.todos.value
        assertEquals(1, todos.size)
        assertEquals("우유 사기", todos[0].title)
    }
}

// ========== 2단계: GREEN (테스트 통과하는 최소 코드) ==========

/**
 * 테스트를 통과하는 최소한의 코드 작성
 */
data class Todo(
    val id: Int,
    val title: String,
    val completed: Boolean = false
)

class TodoViewModel : ViewModel() {
    
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    private var nextId = 1
    
    /**
     * 할 일 추가 (최소 구현)
     */
    fun addTodo(title: String) {
        val newTodo = Todo(
            id = nextId++,
            title = title
        )
        _todos.value = _todos.value + newTodo
    }
}

// ✅ 테스트 통과!

// ========== 3단계: REFACTOR (코드 개선) ==========

/**
 * 코드 개선 (테스트는 그대로 유지)
 */
class TodoViewModel : ViewModel() {
    
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    private val _nextId = AtomicInteger(1)
    
    /**
     * 할 일 추가 (개선된 버전)
     * 
     * - 빈 제목 검증 추가
     * - Thread-safe ID 생성
     */
    fun addTodo(title: String) {
        if (title.isBlank()) return  // 빈 제목 무시
        
        val newTodo = Todo(
            id = _nextId.getAndIncrement(),
            title = title.trim()  // 공백 제거
        )
        
        _todos.update { currentList ->
            currentList + newTodo
        }
    }
}

// ✅ 테스트 여전히 통과!
```

### TDD 실전 예제: 검색 기능

```kotlin
/**
 * TDD로 검색 기능 구현하기
 */

// ========== RED: 테스트 작성 ==========

class SearchViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: SearchViewModel
    
    @Before
    fun setup() {
        viewModel = SearchViewModel()
    }
    
    /**
     * 테스트 1: 검색어 입력 시 결과 반환
     */
    @Test
    fun `검색어 입력 시 관련 결과를 반환한다`() = runTest {
        // Given: 초기 데이터
        val items = listOf("Apple", "Banana", "Cherry")
        viewModel.setItems(items)
        
        // When: 검색
        viewModel.search("app")
        
        // Then: 결과 확인
        val results = viewModel.searchResults.value
        assertEquals(1, results.size)
        assertEquals("Apple", results[0])
    }
    
    /**
     * 테스트 2: 대소문자 무시
     */
    @Test
    fun `검색은 대소문자를 무시한다`() = runTest {
        val items = listOf("Apple", "Banana")
        viewModel.setItems(items)
        
        viewModel.search("APPLE")
        
        assertEquals(1, viewModel.searchResults.value.size)
    }
    
    /**
     * 테스트 3: 빈 검색어는 전체 결과
     */
    @Test
    fun `빈 검색어는 전체 결과를 반환한다`() = runTest {
        val items = listOf("Apple", "Banana", "Cherry")
        viewModel.setItems(items)
        
        viewModel.search("")
        
        assertEquals(3, viewModel.searchResults.value.size)
    }
}

// ========== GREEN: 구현 ==========

class SearchViewModel : ViewModel() {
    
    private val _items = MutableStateFlow<List<String>>(emptyList())
    
    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()
    
    /**
     * 아이템 설정
     */
    fun setItems(items: List<String>) {
        _items.value = items
        _searchResults.value = items  // 초기에는 전체 표시
    }
    
    /**
     * 검색 실행
     */
    fun search(query: String) {
        _searchResults.value = if (query.isBlank()) {
            // 빈 검색어: 전체 반환
            _items.value
        } else {
            // 검색어 포함 항목 필터링 (대소문자 무시)
            _items.value.filter { item ->
                item.contains(query, ignoreCase = true)
            }
        }
    }
}

// ========== REFACTOR: 개선 ==========

class SearchViewModel : ViewModel() {
    
    private val _items = MutableStateFlow<List<String>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    
    /**
     * 검색 결과를 Flow로 자동 계산
     */
    val searchResults: StateFlow<List<String>> = combine(
        _items,
        _searchQuery
    ) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            items.filter { it.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun setItems(items: List<String>) {
        _items.value = items
    }
    
    fun search(query: String) {
        _searchQuery.value = query
    }
}
```

---

## 실전 시나리오

### 시나리오 1: 로그인 화면 전체 테스트

```kotlin
/**
 * 로그인 화면 완전한 테스트 스위트
 */
class LoginScreenCompleteTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 테스트 1: 초기 상태
     */
    @Test
    fun loginScreen_initialState_displaysCorrectly() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        // 제목 표시
        composeTestRule.onNodeWithText("로그인").assertIsDisplayed()
        
        // 입력 필드 표시
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
        
        // 로그인 버튼 활성화
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
        
        // 에러 메시지 없음
        composeTestRule.onNodeWithTag("error_message").assertDoesNotExist()
    }
    
    /**
     * 테스트 2: 빈 이메일 검증
     */
    @Test
    fun loginScreen_emptyEmail_showsError() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        // 비밀번호만 입력
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        // 로그인 시도
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        // 에러 메시지 확인
        composeTestRule.onNodeWithText("이메일을 입력하세요")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 3: 잘못된 이메일 형식
     */
    @Test
    fun loginScreen_invalidEmailFormat_showsError() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("invalid-email")
        
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        composeTestRule.onNodeWithText("올바른 이메일 형식이 아닙니다")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 4: 짧은 비밀번호
     */
    @Test
    fun loginScreen_shortPassword_showsError() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("12345")  // 6자 미만
        
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        composeTestRule.onNodeWithText("비밀번호는 6자 이상이어야 합니다")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 5: 로딩 상태
     */
    @Test
    fun loginScreen_duringLogin_showsLoadingIndicator() {
        var isLoading = false
        
        composeTestRule.setContent {
            LoginScreen(
                isLoading = isLoading,
                onLogin = { _, _ ->
                    isLoading = true
                }
            )
        }
        
        // 정상 입력
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        // 로그인 버튼 클릭
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        // 로딩 인디케이터 표시
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
        
        // 로그인 버튼 비활성화
        composeTestRule.onNodeWithTag("login_button")
            .assertIsNotEnabled()
    }
    
    /**
     * 테스트 6: 성공적인 로그인
     */
    @Test
    fun loginScreen_validCredentials_callsOnLoginCallback() {
        var loginCalled = false
        var receivedEmail = ""
        var receivedPassword = ""
        
        composeTestRule.setContent {
            LoginScreen(
                onLogin = { email, password ->
                    loginCalled = true
                    receivedEmail = email
                    receivedPassword = password
                }
            )
        }
        
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        // 콜백 호출 확인
        assertTrue(loginCalled)
        assertEquals("test@example.com", receivedEmail)
        assertEquals("password123", receivedPassword)
    }
}
```

### 시나리오 2: 장바구니 기능 테스트

```kotlin
/**
 * 장바구니 화면 테스트
 */
class ShoppingCartTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 테스트 1: 빈 장바구니
     */
    @Test
    fun shoppingCart_empty_showsEmptyMessage() {
        composeTestRule.setContent {
            ShoppingCartScreen(items = emptyList())
        }
        
        composeTestRule.onNodeWithText("장바구니가 비어있습니다")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 2: 상품 추가
     */
    @Test
    fun shoppingCart_addItem_increasesCount() {
        val items = mutableListOf<CartItem>()
        
        composeTestRule.setContent {
            ShoppingCartScreen(
                items = items,
                onAddItem = { item ->
                    items.add(item)
                }
            )
        }
        
        // 상품 추가 버튼 클릭
        composeTestRule.onNodeWithTag("add_item_button")
            .performClick()
        
        // 상품 개수 확인
        assertEquals(1, items.size)
        
        composeTestRule.onNodeWithText("상품 1개")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 3: 총 금액 계산
     */
    @Test
    fun shoppingCart_multipleItems_calculatesTotalCorrectly() {
        val items = listOf(
            CartItem("상품 A", 10000, 2),  // 20,000원
            CartItem("상품 B", 5000, 3)    // 15,000원
        )
        
        composeTestRule.setContent {
            ShoppingCartScreen(items = items)
        }
        
        // 총 금액 확인
        composeTestRule.onNodeWithText("총 금액: 35,000원")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 4: 상품 삭제
     */
    @Test
    fun shoppingCart_removeItem_decreasesCount() {
        val items = mutableListOf(
            CartItem("상품 A", 10000, 1)
        )
        
        composeTestRule.setContent {
            ShoppingCartScreen(
                items = items,
                onRemoveItem = { item ->
                    items.remove(item)
                }
            )
        }
        
        // 삭제 버튼 클릭
        composeTestRule.onNodeWithTag("remove_button_0")
            .performClick()
        
        // 빈 장바구니 메시지 표시
        composeTestRule.onNodeWithText("장바구니가 비어있습니다")
            .assertIsDisplayed()
    }
}
```

### 시나리오 3: 무한 스크롤 테스트

```kotlin
/**
 * 무한 스크롤 리스트 테스트
 */
class InfiniteScrollTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 테스트: 스크롤 시 추가 로드
     */
    @Test
    fun infiniteScroll_scrollToEnd_loadsMoreItems() {
        var loadMoreCalled = false
        val items = (1..20).map { "아이템 $it" }.toMutableList()
        
        composeTestRule.setContent {
            InfiniteScrollList(
                items = items,
                onLoadMore = {
                    loadMoreCalled = true
                    // 추가 아이템 로드
                    val newItems = (21..40).map { "아이템 $it" }
                    items.addAll(newItems)
                }
            )
        }
        
        // 초기 아이템 확인
        composeTestRule.onNodeWithText("아이템 1").assertExists()
        
        // 마지막 아이템으로 스크롤
        composeTestRule.onNodeWithText("아이템 20")
            .performScrollTo()
        
        // 추가 로드 호출 확인
        assertTrue(loadMoreCalled)
        
        // 대기 후 새 아이템 확인
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("아이템 21")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("아이템 21").assertExists()
    }
}
```

---

## 테스트 커버리지

### 커버리지 측정 설정

```kotlin
// build.gradle.kts (Module: app)
android {
    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }
}

// 커버리지 플러그인 추가
plugins {
    id("jacoco")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files("build/intermediates/javac/debug"))
    executionData.setFrom(files("build/jacoco/testDebugUnitTest.exec"))
}
```

### 커버리지 실행

```bash
# 테스트 실행 및 커버리지 생성
./gradlew testDebugUnitTest jacocoTestReport

# 리포트 확인
open build/reports/jacoco/jacocoTestReport/html/index.html
```

### 커버리지 목표

```
┌─────────────────┬──────────┐
│ 테스트 유형      │ 목표     │
├─────────────────┼──────────┤
│ Unit Tests      │ 80% 이상 │
│ UI Tests        │ 60% 이상 │
│ Integration     │ 70% 이상 │
│ 전체            │ 75% 이상 │
└─────────────────┴──────────┘
```

---

## CI/CD 통합

### GitHub Actions 설정

```yaml
# .github/workflows/test.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout
        uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest
      
      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport
      
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
      
      - name: Run UI Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 33
          script: ./gradlew connectedDebugAndroidTest
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: '**/build/test-results/**/*.xml'
```

### GitLab CI 설정

```yaml
# .gitlab-ci.yml
image: openjdk:17-jdk

stages:
  - test
  - report

unit_tests:
  stage: test
  script:
    - ./gradlew testDebugUnitTest
  artifacts:
    reports:
      junit: '**/build/test-results/testDebugUnitTest/TEST-*.xml'

coverage:
  stage: report
  script:
    - ./gradlew jacocoTestReport
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    paths:
      - build/reports/jacoco/
```

---

## 베스트 프랙티스

### 1. Given-When-Then 패턴

```kotlin
/**
 * Given-When-Then 패턴으로 테스트 구조화
 */
@Test
fun `사용자가 상품을 장바구니에 추가하면 개수가 증가한다`() {
    // Given (준비): 초기 상태 설정
    val viewModel = ShoppingCartViewModel()
    val product = Product(1, "노트북", 1000000)
    
    // When (실행): 테스트할 동작 수행
    viewModel.addToCart(product)
    
    // Then (검증): 결과 확인
    val cartItems = viewModel.cartItems.value
    assertEquals(1, cartItems.size)
    assertEquals(product, cartItems[0].product)
}
```

### 2. 명확한 테스트 이름

```kotlin
// ✅ 좋은 예: 무엇을 테스트하는지 명확
@Test
fun `빈 이메일로 로그인 시도하면 에러 메시지가 표시된다`()

@Test
fun `increment 버튼 클릭 시 카운터가 1 증가한다`()

@Test
fun `네트워크 오류 발생 시 재시도 버튼이 표시된다`()

// ❌ 나쁜 예: 무엇을 테스트하는지 불명확
@Test
fun test1()

@Test
fun loginTest()

@Test
fun checkButton()
```

### 3. 테스트 독립성

```kotlin
/**
 * 각 테스트는 독립적이어야 함
 */
class IndependentTestExample {
    
    // ❌ 나쁜 예: 테스트 간 상태 공유
    private var sharedCounter = 0  // 위험!
    
    @Test
    fun test1() {
        sharedCounter++  // 다른 테스트에 영향
        assertEquals(1, sharedCounter)
    }
    
    @Test
    fun test2() {
        sharedCounter++  // test1 실행 여부에 따라 결과 달라짐
        assertEquals(1, sharedCounter)  // 실패 가능!
    }
    
    // ✅ 좋은 예: 각 테스트마다 새로운 인스턴스
    @Test
    fun independentTest1() {
        val counter = 0
        val newCounter = counter + 1
        assertEquals(1, newCounter)
    }
    
    @Test
    fun independentTest2() {
        val counter = 0
        val newCounter = counter + 1
        assertEquals(1, newCounter)  // 항상 성공
    }
}
```

### 4. 테스트 데이터 팩토리

```kotlin
/**
 * 테스트 데이터 생성 유틸리티
 */
object TestDataFactory {
    
    /**
     * 기본 사용자 생성
     */
    fun createUser(
        id: Int = 1,
        name: String = "홍길동",
        email: String = "hong@example.com"
    ) = User(id, name, email)
    
    /**
     * 여러 사용자 생성
     */
    fun createUsers(count: Int): List<User> {
        return (1..count).map { i ->
            createUser(
                id = i,
                name = "사용자 $i",
                email = "user$i@example.com"
            )
        }
    }
    
    /**
     * 상품 생성
     */
    fun createProduct(
        id: Int = 1,
        name: String = "테스트 상품",
        price: Int = 10000
    ) = Product(id, name, price)
}

/**
 * 사용 예제
 */
@Test
fun testWithFactory() {
    // 간단하게 테스트 데이터 생성
    val user = TestDataFactory.createUser()
    val users = TestDataFactory.createUsers(10)
    val product = TestDataFactory.createProduct(price = 50000)
    
    // 테스트 로직...
}
```

### 5. 에러 메시지 명확하게

```kotlin
/**
 * 명확한 에러 메시지로 디버깅 쉽게
 */
@Test
fun testWithClearMessages() {
    val viewModel = LoginViewModel()
    
    // ❌ 나쁜 예: 에러 메시지 없음
    assertTrue(viewModel.isValid())
    
    // ✅ 좋은 예: 명확한 에러 메시지
    assertTrue(
        viewModel.isValid(),
        "ViewModel이 유효하지 않습니다. 현재 상태: ${viewModel.state.value}"
    )
    
    // ✅ 더 좋은 예: 구체적인 에러 메시지
    val expectedEmail = "test@example.com"
    val actualEmail = viewModel.email.value
    assertEquals(
        expectedEmail,
        actualEmail,
        "이메일이 일치하지 않습니다. 예상: $expectedEmail, 실제: $actualEmail"
    )
}
```

---

## 💡 요약

### 테스트 작성 체크리스트

- ✅ Given-When-Then 패턴 사용
- ✅ 명확한 테스트 이름
- ✅ 테스트 독립성 유지
- ✅ 테스트 데이터 팩토리 활용
- ✅ 명확한 에러 메시지
- ✅ 적절한 커버리지 유지
- ✅ CI/CD 통합
- ✅ TDD 실천

### 테스트 우선순위

1. **핵심 비즈니스 로직** (최우선)
2. **사용자 인터랙션** (중요)
3. **에러 처리** (중요)
4. **엣지 케이스** (필요시)

---

## 🎯 완료!

Jetpack Compose 테스팅을 모두 마스터했습니다! 🎉

**학습한 내용:**
1. **15-1. 테스팅 기초** - ComposeTestRule, Finder, Assertion, Action
2. **15-2. 테스팅 고급** - ViewModel, Repository, Navigation, 비동기
3. **15-3. 실전 시나리오** - TDD, 실전 예제, 커버리지, CI/CD

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
