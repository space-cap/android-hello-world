# Jetpack Compose 테스팅 기초

> 📖 **시리즈 구성**
> - **15-1**: Jetpack Compose 테스팅 기초 (현재 문서)
> - **15-2**: [Jetpack Compose 테스팅 고급](./15-2-jetpack-compose-testing-advanced.md)
> - **15-3**: [Jetpack Compose 테스팅 실전 시나리오](./15-3-jetpack-compose-testing-scenarios.md)

---

## 📚 목차

1. [테스팅 개요](#테스팅-개요)
2. [프로젝트 설정](#프로젝트-설정)
3. [ComposeTestRule](#composetestrule)
4. [Finder API](#finder-api)
5. [Assertion API](#assertion-api)
6. [Action API](#action-api)
7. [실전 예제](#실전-예제)

---

## 테스팅 개요

### 왜 테스팅이 필요한가?

```kotlin
/**
 * 테스트 없이 개발하는 경우
 */
// ❌ 나쁜 예: 수동 테스트만 의존
@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column {
        TextField(value = email, onValueChange = { email = it })
        TextField(value = password, onValueChange = { password = it })
        Button(onClick = { onLogin(email, password) }) {
            Text("로그인")
        }
    }
}
// 문제: 빈 이메일로 로그인 가능? 비밀번호 길이 제한은? → 모름

/**
 * 테스트와 함께 개발하는 경우
 */
// ✅ 좋은 예: 자동화된 테스트
@Test
fun `빈 이메일로 로그인 시도하면 에러 메시지 표시`() {
    composeTestRule.setContent {
        LoginScreen(onLogin = { _, _ -> })
    }
    
    // 빈 이메일로 로그인 시도
    composeTestRule.onNodeWithText("로그인").performClick()
    
    // 에러 메시지 확인
    composeTestRule.onNodeWithText("이메일을 입력하세요").assertExists()
}
// 장점: 버그를 코드 작성 즉시 발견!
```

**테스팅의 이점:**

| 이점 | 설명 |
|------|------|
| 🐛 **버그 조기 발견** | 개발 단계에서 버그 발견 → 수정 비용 최소화 |
| 🔄 **리팩토링 안전성** | 코드 변경 시 기존 기능 보장 |
| 📖 **문서화 역할** | 테스트 코드가 사용법 설명 |
| 💎 **코드 품질 향상** | 테스트 가능한 코드 = 좋은 설계 |
| ⚡ **빠른 피드백** | 수동 테스트보다 훨씬 빠름 |

### 테스트 피라미드

```
       /\
      /E2E\     ← UI 테스트 (적음)
     /______\      - 느림 (초 단위)
    /        \     - 비용 높음
   / Integration\  ← 통합 테스트 (중간)
  /______________\   - 중간 속도
 /                \  - 중간 비용
/   Unit Tests     \ ← 단위 테스트 (많음)
/____________________\ - 빠름 (밀리초)
                       - 비용 낮음
```

**각 레벨의 역할:**

```kotlin
/**
 * Unit Test: 개별 함수/클래스 테스트
 */
@Test
fun `이메일 유효성 검증`() {
    assertTrue(isValidEmail("test@example.com"))
    assertFalse(isValidEmail("invalid"))
}

/**
 * Integration Test: 여러 컴포넌트 통합 테스트
 */
@Test
fun `ViewModel과 Repository 통합 테스트`() {
    val viewModel = LoginViewModel(fakeRepository)
    viewModel.login("test@example.com", "password")
    assertEquals(LoginState.Success, viewModel.state.value)
}

/**
 * UI Test: 전체 화면 테스트
 */
@Test
fun `로그인 화면 전체 플로우 테스트`() {
    composeTestRule.setContent { LoginScreen() }
    // 이메일 입력 → 비밀번호 입력 → 로그인 → 성공 화면
}
```

### Compose 테스팅의 특징

**기존 View 테스팅 vs Compose 테스팅:**

```kotlin
// ❌ View 테스팅 (복잡함)
@Test
fun viewTest() {
    val activity = ActivityScenario.launch(MainActivity::class.java)
    val emailField = activity.findViewById<EditText>(R.id.email)
    emailField.setText("test@example.com")
    // findViewById, Espresso 등 복잡한 API
}

// ✅ Compose 테스팅 (간단함)
@Test
fun composeTest() {
    composeTestRule.setContent {
        LoginScreen()
    }
    composeTestRule.onNodeWithText("이메일").performTextInput("test@example.com")
    // 직관적이고 간단한 API
}
```

---

## 프로젝트 설정

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // ===== 기본 테스팅 =====
    
    // JUnit 4 (단위 테스트 프레임워크)
    testImplementation("junit:junit:4.13.2")
    
    // Kotlin Coroutines Test (코루틴 테스트)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // ===== Compose 테스팅 =====
    
    // Compose UI Test (필수)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    
    // Compose Test Manifest (디버그 빌드용)
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    
    // ===== Mock 라이브러리 =====
    
    // MockK (Kotlin 전용 Mock 라이브러리)
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    
    // ===== 추가 유틸리티 =====
    
    // Truth (가독성 좋은 Assertion)
    testImplementation("com.google.truth:truth:1.1.5")
    
    // Turbine (Flow 테스트)
    testImplementation("app.cash.turbine:turbine:1.0.0")
}
```

### 테스트 디렉토리 구조

```
app/src/
├── main/
│   └── kotlin/
│       └── com/example/app/
│           ├── ui/
│           │   └── LoginScreen.kt
│           └── viewmodel/
│               └── LoginViewModel.kt
├── test/                          ← 단위 테스트 (JVM)
│   └── kotlin/
│       └── com/example/app/
│           ├── viewmodel/
│           │   └── LoginViewModelTest.kt
│           └── util/
│               └── ValidationTest.kt
└── androidTest/                   ← UI 테스트 (Android)
    └── kotlin/
        └── com/example/app/
            └── ui/
                └── LoginScreenTest.kt
```

**차이점:**

| 디렉토리 | 실행 환경 | 용도 | 속도 |
|---------|----------|------|------|
| `test/` | JVM | 단위 테스트, ViewModel 테스트 | 빠름 ⚡ |
| `androidTest/` | Android 기기/에뮬레이터 | UI 테스트, 통합 테스트 | 느림 🐢 |

---

## ComposeTestRule

### ComposeTestRule 생성

```kotlin
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * 기본 ComposeTestRule
 */
class BasicComposeTest {
    
    /**
     * @get:Rule: JUnit Rule 어노테이션
     * - 테스트 전후로 자동 실행되는 규칙
     */
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun simpleTest() {
        // setContent: Compose UI 설정
        composeTestRule.setContent {
            Text("Hello World")
        }
        
        // 테스트 로직
        composeTestRule.onNodeWithText("Hello World").assertExists()
    }
}
```

### ComposeTestRule 주요 메서드

```kotlin
/**
 * ComposeTestRule 메서드 가이드
 */
class ComposeTestRuleGuide {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testRuleMethods() {
        // 1. setContent: UI 설정
        composeTestRule.setContent {
            MyApp()
        }
        
        // 2. onNode: 단일 노드 찾기
        composeTestRule.onNode(hasText("버튼"))
        
        // 3. onAllNodes: 여러 노드 찾기
        composeTestRule.onAllNodes(hasClickAction())
        
        // 4. waitUntil: 조건 대기
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("로딩 완료").fetchSemanticsNodes().isNotEmpty()
        }
        
        // 5. mainClock: 시간 제어 (애니메이션 테스트)
        composeTestRule.mainClock.advanceTimeBy(1000)
    }
}
```

### Activity 기반 테스트

```kotlin
import androidx.compose.ui.test.junit4.createAndroidComposeRule

/**
 * Activity를 사용하는 테스트
 * 
 * 사용 시기:
 * - Navigation 테스트
 * - ViewModel 통합 테스트
 * - 실제 앱 환경 테스트
 */
class ActivityBasedTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun testWithActivity() {
        // Activity가 자동으로 시작됨
        composeTestRule.onNodeWithText("홈 화면").assertExists()
        
        // Activity 접근 가능
        val activity = composeTestRule.activity
        assertNotNull(activity)
    }
}
```

---

## Finder API

### 텍스트로 찾기

```kotlin
/**
 * onNodeWithText: 텍스트로 노드 찾기
 */
@Test
fun findByText() {
    composeTestRule.setContent {
        Column {
            Text("제목")
            Text("부제목")
            Button(onClick = {}) {
                Text("클릭")
            }
        }
    }
    
    // 정확한 텍스트 매칭
    composeTestRule.onNodeWithText("제목").assertExists()
    
    // 부분 매칭 (substring = true)
    composeTestRule.onNodeWithText("제", substring = true).assertExists()
    
    // 대소문자 무시 (ignoreCase = true)
    composeTestRule.onNodeWithText("제목", ignoreCase = true).assertExists()
}

/**
 * onAllNodesWithText: 같은 텍스트를 가진 모든 노드
 */
@Test
fun findAllByText() {
    composeTestRule.setContent {
        Column {
            Text("아이템")
            Text("아이템")
            Text("아이템")
        }
    }
    
    // 모든 "아이템" 노드 찾기
    composeTestRule.onAllNodesWithText("아이템")
        .assertCountEquals(3)
    
    // 인덱스로 접근
    composeTestRule.onAllNodesWithText("아이템")[0].assertExists()
    composeTestRule.onAllNodesWithText("아이템")[1].assertExists()
}
```

### 태그로 찾기

```kotlin
/**
 * testTag: 테스트용 태그 설정
 */
@Composable
fun TaggedButton() {
    Button(
        onClick = {},
        modifier = Modifier.testTag("login_button")  // ✅ 테스트 태그
    ) {
        Text("로그인")
    }
}

@Test
fun findByTag() {
    composeTestRule.setContent {
        TaggedButton()
    }
    
    // 태그로 찾기 (권장 방법)
    composeTestRule.onNodeWithTag("login_button")
        .assertExists()
        .performClick()
}

/**
 * 왜 태그를 사용하나?
 * 
 * ❌ 텍스트로 찾기: 다국어 지원 시 문제
 * composeTestRule.onNodeWithText("Login")  // 영어에서만 작동
 * 
 * ✅ 태그로 찾기: 언어 독립적
 * composeTestRule.onNodeWithTag("login_button")  // 모든 언어에서 작동
 */
```

### ContentDescription으로 찾기

```kotlin
/**
 * contentDescription: 접근성 설명
 */
@Composable
fun IconWithDescription() {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "닫기",  // ✅ 접근성 설명
        modifier = Modifier.clickable { }
    )
}

@Test
fun findByContentDescription() {
    composeTestRule.setContent {
        IconWithDescription()
    }
    
    // ContentDescription으로 찾기
    composeTestRule.onNodeWithContentDescription("닫기")
        .assertExists()
        .performClick()
}
```

### Semantics Matcher로 찾기

```kotlin
import androidx.compose.ui.test.*

/**
 * Semantics Matcher: 고급 검색
 */
@Test
fun findWithSemantics() {
    composeTestRule.setContent {
        Column {
            Button(onClick = {}) { Text("버튼 1") }
            Button(onClick = {}, enabled = false) { Text("버튼 2") }
            Text("일반 텍스트")
        }
    }
    
    // 클릭 가능한 요소 찾기
    composeTestRule.onNode(hasClickAction()).assertExists()
    
    // 활성화된 요소 찾기
    composeTestRule.onNode(isEnabled()).assertExists()
    
    // 비활성화된 요소 찾기
    composeTestRule.onNode(isNotEnabled()).assertExists()
    
    // 여러 조건 조합
    composeTestRule.onNode(
        hasText("버튼 1") and hasClickAction()
    ).assertExists()
}

/**
 * 자주 사용하는 Matcher
 */
@Test
fun commonMatchers() {
    // hasText: 텍스트 포함
    onNode(hasText("Hello"))
    
    // hasTestTag: 태그 매칭
    onNode(hasTestTag("my_tag"))
    
    // hasContentDescription: ContentDescription 매칭
    onNode(hasContentDescription("Close"))
    
    // hasClickAction: 클릭 가능
    onNode(hasClickAction())
    
    // isEnabled: 활성화됨
    onNode(isEnabled())
    
    // isDisplayed: 화면에 표시됨
    onNode(isDisplayed())
    
    // hasScrollAction: 스크롤 가능
    onNode(hasScrollAction())
}
```

---

## Assertion API

### 존재 여부 확인

```kotlin
/**
 * 노드 존재 여부 테스트
 */
@Test
fun assertExistence() {
    composeTestRule.setContent {
        Column {
            Text("표시됨")
            // Text("숨겨짐") ← 주석 처리
        }
    }
    
    // 존재 확인
    composeTestRule.onNodeWithText("표시됨").assertExists()
    
    // 존재하지 않음 확인
    composeTestRule.onNodeWithText("숨겨짐").assertDoesNotExist()
}
```

### 표시 여부 확인

```kotlin
/**
 * 화면 표시 여부 테스트
 */
@Test
fun assertVisibility() {
    composeTestRule.setContent {
        Column {
            Text("보이는 텍스트")
            Text(
                "안 보이는 텍스트",
                modifier = Modifier.alpha(0f)  // 투명
            )
        }
    }
    
    // 화면에 표시됨
    composeTestRule.onNodeWithText("보이는 텍스트").assertIsDisplayed()
    
    // 화면에 표시 안 됨
    composeTestRule.onNodeWithText("안 보이는 텍스트").assertIsNotDisplayed()
}
```

### 텍스트 내용 확인

```kotlin
/**
 * 텍스트 내용 검증
 */
@Test
fun assertTextContent() {
    composeTestRule.setContent {
        Column {
            Text("정확한 텍스트")
            Text("부분 텍스트 포함")
        }
    }
    
    // 정확한 텍스트 매칭
    composeTestRule.onNodeWithText("정확한 텍스트")
        .assertTextEquals("정확한 텍스트")
    
    // 텍스트 포함 확인
    composeTestRule.onNodeWithText("부분", substring = true)
        .assertTextContains("부분")
}
```

### 상태 확인

```kotlin
/**
 * UI 상태 검증
 */
@Test
fun assertStates() {
    composeTestRule.setContent {
        Column {
            Button(onClick = {}) { Text("활성화 버튼") }
            Button(onClick = {}, enabled = false) { Text("비활성화 버튼") }
            Checkbox(checked = true, onCheckedChange = {})
            Checkbox(checked = false, onCheckedChange = {})
        }
    }
    
    // 활성화 상태
    composeTestRule.onNodeWithText("활성화 버튼").assertIsEnabled()
    
    // 비활성화 상태
    composeTestRule.onNodeWithText("비활성화 버튼").assertIsNotEnabled()
    
    // 선택 상태
    composeTestRule.onAllNodes(hasClickAction())[2].assertIsOn()
    
    // 선택 해제 상태
    composeTestRule.onAllNodes(hasClickAction())[3].assertIsOff()
}
```

---

## Action API

### 클릭 동작

```kotlin
/**
 * 클릭 테스트
 */
@Test
fun performClick() {
    var clickCount = 0
    
    composeTestRule.setContent {
        Button(onClick = { clickCount++ }) {
            Text("클릭 $clickCount")
        }
    }
    
    // 클릭 수행
    composeTestRule.onNodeWithText("클릭 0").performClick()
    
    // 결과 확인
    assertEquals(1, clickCount)
    composeTestRule.onNodeWithText("클릭 1").assertExists()
    
    // 여러 번 클릭
    repeat(5) {
        composeTestRule.onNodeWithText("클릭 ${it + 1}").performClick()
    }
    assertEquals(6, clickCount)
}
```

### 텍스트 입력

```kotlin
/**
 * 텍스트 입력 테스트
 */
@Test
fun performTextInput() {
    var text = ""
    
    composeTestRule.setContent {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("이름") },
            modifier = Modifier.testTag("name_field")
        )
    }
    
    // 텍스트 입력
    composeTestRule.onNodeWithTag("name_field")
        .performTextInput("홍길동")
    
    // 결과 확인
    assertEquals("홍길동", text)
    
    // 텍스트 추가 입력
    composeTestRule.onNodeWithTag("name_field")
        .performTextInput(" 님")
    
    assertEquals("홍길동 님", text)
}

/**
 * 텍스트 교체
 */
@Test
fun performTextReplacement() {
    var text = "기존 텍스트"
    
    composeTestRule.setContent {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.testTag("field")
        )
    }
    
    // 텍스트 교체
    composeTestRule.onNodeWithTag("field")
        .performTextReplacement("새 텍스트")
    
    assertEquals("새 텍스트", text)
}

/**
 * 텍스트 지우기
 */
@Test
fun performTextClearance() {
    var text = "지울 텍스트"
    
    composeTestRule.setContent {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.testTag("field")
        )
    }
    
    // 텍스트 지우기
    composeTestRule.onNodeWithTag("field")
        .performTextClearance()
    
    assertEquals("", text)
}
```

### 스크롤 동작

```kotlin
/**
 * 스크롤 테스트
 */
@Test
fun performScroll() {
    composeTestRule.setContent {
        LazyColumn(
            modifier = Modifier
                .height(200.dp)
                .testTag("list")
        ) {
            items(100) { index ->
                Text(
                    text = "아이템 $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
    
    // 특정 아이템으로 스크롤
    composeTestRule.onNodeWithText("아이템 50")
        .performScrollTo()
        .assertIsDisplayed()
    
    // 리스트 스크롤
    composeTestRule.onNodeWithTag("list")
        .performScrollToIndex(99)
    
    composeTestRule.onNodeWithText("아이템 99").assertIsDisplayed()
}
```

---

## 실전 예제

### 예제 1: 로그인 화면 테스트

```kotlin
/**
 * 로그인 화면
 */
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("로그인", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(Modifier.height(16.dp))
        
        // 이메일 입력
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_field")
        )
        
        Spacer(Modifier.height(8.dp))
        
        // 비밀번호 입력
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_field")
        )
        
        Spacer(Modifier.height(16.dp))
        
        // 로그인 버튼
        Button(
            onClick = {
                when {
                    email.isBlank() -> errorMessage = "이메일을 입력하세요"
                    password.isBlank() -> errorMessage = "비밀번호를 입력하세요"
                    password.length < 6 -> errorMessage = "비밀번호는 6자 이상이어야 합니다"
                    else -> {
                        errorMessage = ""
                        onLogin(email, password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_button")
        ) {
            Text("로그인")
        }
        
        // 에러 메시지
        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("error_message")
            )
        }
    }
}

/**
 * 로그인 화면 테스트
 */
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * 테스트 1: 화면 초기 상태 확인
     */
    @Test
    fun loginScreen_initialState() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        // 제목 표시 확인
        composeTestRule.onNodeWithText("로그인").assertIsDisplayed()
        
        // 입력 필드 표시 확인
        composeTestRule.onNodeWithTag("email_field").assertExists()
        composeTestRule.onNodeWithTag("password_field").assertExists()
        
        // 로그인 버튼 표시 확인
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
        
        // 에러 메시지 없음
        composeTestRule.onNodeWithTag("error_message").assertDoesNotExist()
    }
    
    /**
     * 테스트 2: 빈 이메일로 로그인 시도
     */
    @Test
    fun loginScreen_emptyEmail_showsError() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        // 비밀번호만 입력
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        // 로그인 버튼 클릭
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        // 에러 메시지 확인
        composeTestRule.onNodeWithText("이메일을 입력하세요")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 3: 짧은 비밀번호로 로그인 시도
     */
    @Test
    fun loginScreen_shortPassword_showsError() {
        composeTestRule.setContent {
            LoginScreen()
        }
        
        // 이메일 입력
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        
        // 짧은 비밀번호 입력
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("12345")
        
        // 로그인 버튼 클릭
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        // 에러 메시지 확인
        composeTestRule.onNodeWithText("비밀번호는 6자 이상이어야 합니다")
            .assertIsDisplayed()
    }
    
    /**
     * 테스트 4: 정상 로그인
     */
    @Test
    fun loginScreen_validCredentials_callsOnLogin() {
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
        
        // 이메일 입력
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        
        // 비밀번호 입력
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        // 로그인 버튼 클릭
        composeTestRule.onNodeWithTag("login_button")
            .performClick()
        
        // 콜백 호출 확인
        assertTrue(loginCalled)
        assertEquals("test@example.com", receivedEmail)
        assertEquals("password123", receivedPassword)
        
        // 에러 메시지 없음
        composeTestRule.onNodeWithTag("error_message")
            .assertDoesNotExist()
    }
}
```

### 예제 2: 카운터 앱 테스트

```kotlin
/**
 * 카운터 앱
 */
@Composable
fun CounterApp() {
    var count by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "카운트: $count",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.testTag("count_text")
        )
        
        Spacer(Modifier.height(16.dp))
        
        Row {
            Button(
                onClick = { count-- },
                modifier = Modifier.testTag("decrease_button")
            ) {
                Text("-")
            }
            
            Spacer(Modifier.width(16.dp))
            
            Button(
                onClick = { count++ },
                modifier = Modifier.testTag("increase_button")
            ) {
                Text("+")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { count = 0 },
            modifier = Modifier.testTag("reset_button")
        ) {
            Text("리셋")
        }
    }
}

/**
 * 카운터 앱 테스트
 */
class CounterAppTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun counterApp_initialState_isZero() {
        composeTestRule.setContent {
            CounterApp()
        }
        
        composeTestRule.onNodeWithTag("count_text")
            .assertTextEquals("카운트: 0")
    }
    
    @Test
    fun counterApp_increaseButton_incrementsCount() {
        composeTestRule.setContent {
            CounterApp()
        }
        
        // 증가 버튼 5번 클릭
        repeat(5) {
            composeTestRule.onNodeWithTag("increase_button")
                .performClick()
        }
        
        composeTestRule.onNodeWithTag("count_text")
            .assertTextEquals("카운트: 5")
    }
    
    @Test
    fun counterApp_decreaseButton_decrementsCount() {
        composeTestRule.setContent {
            CounterApp()
        }
        
        // 감소 버튼 3번 클릭
        repeat(3) {
            composeTestRule.onNodeWithTag("decrease_button")
                .performClick()
        }
        
        composeTestRule.onNodeWithTag("count_text")
            .assertTextEquals("카운트: -3")
    }
    
    @Test
    fun counterApp_resetButton_resetsToZero() {
        composeTestRule.setContent {
            CounterApp()
        }
        
        // 카운트 증가
        repeat(10) {
            composeTestRule.onNodeWithTag("increase_button")
                .performClick()
        }
        
        // 리셋 버튼 클릭
        composeTestRule.onNodeWithTag("reset_button")
            .performClick()
        
        composeTestRule.onNodeWithTag("count_text")
            .assertTextEquals("카운트: 0")
    }
}
```

---

## 💡 베스트 프랙티스 요약

### 테스트 작성
- ✅ testTag 사용 (텍스트 대신)
- ✅ 명확한 테스트 이름
- ✅ Given-When-Then 패턴
- ✅ 하나의 테스트는 하나의 기능만

### Finder 사용
- ✅ testTag 우선 사용
- ✅ 고유한 태그 사용
- ✅ Semantics Matcher 활용
- ✅ 여러 노드는 인덱스로 구분

### Assertion
- ✅ 명확한 Assertion 메시지
- ✅ 필요한 것만 검증
- ✅ 상태 변화 확인
- ✅ 에러 케이스도 테스트

### 성능
- ✅ waitUntil 타임아웃 설정
- ✅ 불필요한 대기 제거
- ✅ 테스트 독립성 유지
- ✅ 빠른 피드백

---

## 🎯 다음 단계

기초를 마스터했습니다! 다음으로:

1. **[15-2. Jetpack Compose 테스팅 고급](./15-2-jetpack-compose-testing-advanced.md)** - ViewModel, Repository, Navigation 테스팅

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
