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

---

## TDD (Test-Driven Development)

### Red-Green-Refactor 사이클

```kotlin
// 1. Red: 실패하는 테스트 작성
@Test
fun `login with valid credentials shows home screen`() {
    // 아직 구현 안됨 - 실패!
    composeTestRule.setContent { LoginScreen() }
    
    composeTestRule.onNodeWithTag("email").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("password").performTextInput("password123")
    composeTestRule.onNodeWithTag("login_button").performClick()
    
    composeTestRule.onNodeWithText("Home").assertExists()
}

// 2. Green: 테스트를 통과하는 최소한의 코드 작성
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column {
        TextField(value = email, onValueChange = { email = it }, modifier = Modifier.testTag("email"))
        TextField(value = password, onValueChange = { password = it }, modifier = Modifier.testTag("password"))
        Button(onClick = { onLoginSuccess() }, modifier = Modifier.testTag("login_button")) {
            Text("Login")
        }
    }
}

// 3. Refactor: 코드 개선
```

---

## 실전 시나리오

### 1. 로그인 화면 테스트

```kotlin
@Test
fun `empty email shows error`() {
    composeTestRule.setContent { LoginScreen() }
    
    composeTestRule.onNodeWithTag("login_button").performClick()
    
    composeTestRule.onNodeWithText("이메일을 입력하세요").assertExists()
}
```

### 2. 폼 검증 테스트

```kotlin
@Test
fun `invalid email format shows error`() {
    composeTestRule.setContent { LoginScreen() }
    
    composeTestRule.onNodeWithTag("email").performTextInput("invalid-email")
    composeTestRule.onNodeWithTag("login_button").performClick()
    
    composeTestRule.onNodeWithText("올바른 이메일 형식이 아닙니다").assertExists()
}
```

### 3. 로딩 상태 테스트

```kotlin
@Test
fun `loading state shows progress indicator`() {
    composeTestRule.setContent {
        var isLoading by remember { mutableStateOf(true) }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loading"))
        } else {
            Text("Content")
        }
    }
    
    composeTestRule.onNodeWithTag("loading").assertExists()
}
```

### 4. 에러 상태 테스트

```kotlin
@Test
fun `error state shows error message`() {
    composeTestRule.setContent {
        ErrorScreen(message = "네트워크 오류")
    }
    
    composeTestRule.onNodeWithText("네트워크 오류").assertExists()
    composeTestRule.onNodeWithText("다시 시도").assertExists()
}
```

### 5. 무한 스크롤 테스트

```kotlin
@Test
fun `scrolling to end loads more items`() {
    val items = (1..100).map { "Item $it" }
    
    composeTestRule.setContent {
        LazyColumn {
            items(items) { item ->
                Text(item, modifier = Modifier.testTag(item))
            }
        }
    }
    
    composeTestRule.onNodeWithTag("Item 1").assertExists()
    composeTestRule.onNodeWithTag("Item 100").performScrollTo()
    composeTestRule.onNodeWithTag("Item 100").assertExists()
}
```

---

## 테스트 커버리지

### 커버리지 측정

```bash
# Gradle 명령어
./gradlew testDebugUnitTestCoverage

# 리포트 위치
build/reports/coverage/test/debug/index.html
```

### 목표 커버리지

```
Unit Tests: 80% 이상
UI Tests: 60% 이상
전체: 70% 이상
```

---

## CI/CD 통합

### GitHub Actions 설정

```yaml
# .github/workflows/test.yml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run Unit Tests
        run: ./gradlew test
      
      - name: Run UI Tests
        run: ./gradlew connectedAndroidTest
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

---

## 🎯 완료!

Jetpack Compose 테스팅을 모두 마스터했습니다! 🎉

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
