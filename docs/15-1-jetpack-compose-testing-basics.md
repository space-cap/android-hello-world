# Jetpack Compose 테스팅 기초

> 📖 **시리즈 구성**
> - **15-1**: Jetpack Compose 테스팅 기초 (현재 문서)
> - **15-2**: [Jetpack Compose 테스팅 고급](./15-2-jetpack-compose-testing-advanced.md)
> - **15-3**: [Jetpack Compose 테스팅 실전 시나리오](./15-3-jetpack-compose-testing-scenarios.md)

---

## 📚 목차

1. [테스팅 개요](#테스팅-개요)
2. [프로젝트 설정](#프로젝트-설정)
3. [기본 UI 테스트](#기본-ui-테스트)
4. [간단한 예제](#간단한-예제)

---

## 테스팅 개요

### 왜 테스팅이 필요한가?

```kotlin
// ❌ 테스트 없이 개발
fun addNumbers(a: Int, b: Int) = a + b  // 버그가 있어도 모름

// ✅ 테스트와 함께 개발
@Test
fun `addNumbers returns correct sum`() {
    assertEquals(5, addNumbers(2, 3))  // 버그 즉시 발견
}
```

**테스팅의 이점**:
- ✅ 버그 조기 발견
- ✅ 리팩토링 안전성
- ✅ 문서화 역할
- ✅ 코드 품질 향상

### 테스트 피라미드

```
      /\
     /E2E\     ← 적음 (느림, 비용 높음)
    /______\
   /        \
  /   UI    \   ← 중간
 /___________\
/             \
/  Unit Tests  \ ← 많음 (빠름, 비용 낮음)
/_______________\
```

---

## 프로젝트 설정

### 의존성 추가

```kotlin
dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    
    // Mock
    testImplementation("io.mockk:mockk:1.13.8")
}
```

---

## 기본 UI 테스트

### ComposeTestRule 사용법

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

### Finder, Assertion, Action

```kotlin
// Finder (요소 찾기)
onNodeWithText("버튼")
onNodeWithTag("my_button")
onNodeWithContentDescription("닫기")

// Assertion (검증)
.assertExists()
.assertIsDisplayed()
.assertTextEquals("Hello")

// Action (동작)
.performClick()
.performTextInput("입력")
.performScrollTo()
```

---

## 간단한 예제

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
    
    composeTestRule
        .onNodeWithText("Click Me")
        .performClick()
    
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
    
    composeTestRule
        .onNodeWithText("Name")
        .performTextInput("John")
    
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
    
    items.forEach { item ->
        composeTestRule
            .onNodeWithText(item)
            .assertExists()
    }
}
```

---

## 🎯 다음 단계

기초를 마스터했습니다! 다음으로:

1. **[15-2. Jetpack Compose 테스팅 고급](./15-2-jetpack-compose-testing-advanced.md)** - ViewModel, Repository 테스팅

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Testing! 🧪
