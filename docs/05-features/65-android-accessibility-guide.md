# Android Accessibility (접근성) 가이드

## 목차
1. [접근성이란?](#접근성이란)
2. [접근성 중요성](#접근성-중요성)
3. [Content Description](#content-description)
4. [TalkBack 지원](#talkback-지원)
5. [시맨틱 속성](#시맨틱-속성)
6. [키보드 네비게이션](#키보드-네비게이션)
7. [색상 대비](#색상-대비)
8. [폰트 크기 조정](#폰트-크기-조정)
9. [접근성 서비스](#접근성-서비스)
10. [테스트](#테스트)
11. [실전 예제](#실전-예제)

---

## 접근성이란?

**접근성(Accessibility)**은 장애가 있는 사용자를 포함한 모든 사람이 앱을 쉽게 사용할 수 있도록 만드는 것입니다.

### 대상 사용자
- 👁️ **시각 장애**: 전맹, 저시력, 색맹
- 👂 **청각 장애**: 농, 난청
- 🖐️ **운동 장애**: 손 떨림, 마비
- 🧠 **인지 장애**: 학습 장애, 주의력 결핍

---

## 접근성 중요성

### 1. 법적 요구사항
- 많은 국가에서 접근성 법규 존재
- Google Play 정책 요구사항

### 2. 사용자 확대
- 전 세계 인구의 약 15%가 장애를 가짐
- 일시적 장애 (부상, 환경 등)도 포함

### 3. UX 개선
- 모든 사용자에게 더 나은 경험
- 명확한 UI/UX

---

## Content Description

### 기본 사용법

```kotlin
/**
 * 이미지에 설명 추가
 */
@Composable
fun AccessibleImage() {
    Image(
        painter = painterResource(R.drawable.profile),
        contentDescription = "사용자 프로필 사진",  // ✅ 필수!
        modifier = Modifier.size(100.dp)
    )
}

/**
 * 장식용 이미지 (설명 불필요)
 */
@Composable
fun DecorativeImage() {
    Image(
        painter = painterResource(R.drawable.decoration),
        contentDescription = null,  // ✅ 장식용은 null
        modifier = Modifier.size(50.dp)
    )
}
```

### 좋은 설명 vs 나쁜 설명

```kotlin
/**
 * ❌ 나쁜 예
 */
@Composable
fun BadExample() {
    // 너무 짧음
    Icon(Icons.Default.Delete, contentDescription = "아이콘")
    
    // 너무 장황함
    Icon(
        Icons.Default.Delete,
        contentDescription = "이 버튼을 누르면 선택한 항목이 삭제됩니다. 삭제된 항목은 복구할 수 없습니다."
    )
}

/**
 * ✅ 좋은 예
 */
@Composable
fun GoodExample() {
    // 명확하고 간결함
    Icon(Icons.Default.Delete, contentDescription = "삭제")
    
    // 동작 설명
    IconButton(onClick = { /* 삭제 */ }) {
        Icon(Icons.Default.Delete, contentDescription = "항목 삭제")
    }
}
```

### 상태에 따른 설명

```kotlin
/**
 * 상태에 따라 설명 변경
 */
@Composable
fun PlayPauseButton() {
    var isPlaying by remember { mutableStateOf(false) }
    
    IconButton(onClick = { isPlaying = !isPlaying }) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "일시정지" else "재생"  // ✅ 상태 반영
        )
    }
}
```

---

## TalkBack 지원

### TalkBack이란?
Android의 화면 읽기 프로그램으로, 화면 내용을 음성으로 읽어줍니다.

### 커스텀 동작 추가

```kotlin
/**
 * 커스텀 접근성 동작
 */
@Composable
fun CustomAccessibilityActions() {
    var count by remember { mutableStateOf(0) }
    
    Text(
        text = "카운트: $count",
        modifier = Modifier.semantics {
            // 커스텀 동작 추가
            customActions = listOf(
                CustomAccessibilityAction("증가") {
                    count++
                    true
                },
                CustomAccessibilityAction("감소") {
                    count--
                    true
                },
                CustomAccessibilityAction("초기화") {
                    count = 0
                    true
                }
            )
        }
    )
}
```

### 라이브 영역

```kotlin
/**
 * 실시간 업데이트 알림
 */
@Composable
fun LiveRegion() {
    var message by remember { mutableStateOf("대기 중...") }
    
    Text(
        text = message,
        modifier = Modifier.semantics {
            // 내용 변경 시 자동으로 읽어줌
            liveRegion = LiveRegionMode.Polite
        }
    )
    
    // message가 변경되면 TalkBack이 자동으로 읽음
}
```

### 그룹화

```kotlin
/**
 * 관련 요소 그룹화
 */
@Composable
fun GroupedContent() {
    // ❌ 나쁜 예: 각각 읽음
    Column {
        Text("홍길동")
        Text("소프트웨어 엔지니어")
        Text("서울")
    }
    
    // ✅ 좋은 예: 하나로 그룹화
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Text("홍길동")
        Text("소프트웨어 엔지니어")
        Text("서울")
    }
    // TalkBack: "홍길동, 소프트웨어 엔지니어, 서울"
}
```

---

## 시맨틱 속성

### 역할 (Role)

```kotlin
/**
 * 요소의 역할 명시
 */
@Composable
fun SemanticRoles() {
    // 버튼
    Box(
        modifier = Modifier
            .clickable { /* 동작 */ }
            .semantics { role = Role.Button }
    ) {
        Text("클릭하세요")
    }
    
    // 체크박스
    Box(
        modifier = Modifier.semantics {
            role = Role.Checkbox
            toggleableState = ToggleableState.On
        }
    ) {
        Icon(Icons.Default.CheckBox, contentDescription = null)
    }
    
    // 이미지
    Box(
        modifier = Modifier.semantics { role = Role.Image }
    ) {
        Image(painterResource(R.drawable.photo), contentDescription = "풍경 사진")
    }
}
```

### 상태 정보

```kotlin
/**
 * 요소의 상태 전달
 */
@Composable
fun StateInformation() {
    var isChecked by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    
    // 체크박스 상태
    Checkbox(
        checked = isChecked,
        onCheckedChange = { isChecked = it },
        modifier = Modifier.semantics {
            stateDescription = if (isChecked) "선택됨" else "선택 안 됨"
        }
    )
    
    // 확장 가능한 항목
    Column(
        modifier = Modifier.semantics {
            stateDescription = if (isExpanded) "펼쳐짐" else "접혀짐"
        }
    ) {
        Text("제목", modifier = Modifier.clickable { isExpanded = !isExpanded })
        if (isExpanded) {
            Text("내용...")
        }
    }
}
```

### 진행 상태

```kotlin
/**
 * 진행률 표시
 */
@Composable
fun ProgressIndicator() {
    var progress by remember { mutableStateOf(0.5f) }
    
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = progress,
                range = 0f..1f
            )
            stateDescription = "${(progress * 100).toInt()}% 완료"
        }
    )
}
```

---

## 키보드 네비게이션

### 포커스 순서

```kotlin
/**
 * 포커스 순서 제어
 */
@Composable
fun FocusOrderExample() {
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    
    Column {
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .focusRequester(focusRequester1)
                .focusProperties {
                    next = focusRequester2  // 다음 포커스
                }
        )
        
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .focusRequester(focusRequester2)
                .focusProperties {
                    previous = focusRequester1  // 이전 포커스
                    next = focusRequester3
                }
        )
        
        Button(
            onClick = {},
            modifier = Modifier
                .focusRequester(focusRequester3)
                .focusProperties {
                    previous = focusRequester2
                }
        ) {
            Text("제출")
        }
    }
}
```

### 포커스 표시

```kotlin
/**
 * 포커스 시각적 표시
 */
@Composable
fun FocusIndicator() {
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color.Blue else Color.Transparent
            )
            .clickable { /* 동작 */ }
            .padding(16.dp)
    ) {
        Text("포커스 가능한 요소")
    }
}
```

---

## 색상 대비

### WCAG 기준
- **AA 등급**: 4.5:1 (일반 텍스트), 3:1 (큰 텍스트)
- **AAA 등급**: 7:1 (일반 텍스트), 4.5:1 (큰 텍스트)

### 대비 확인

```kotlin
/**
 * 색상 대비 계산
 */
fun calculateContrastRatio(color1: Color, color2: Color): Float {
    val luminance1 = calculateLuminance(color1)
    val luminance2 = calculateLuminance(color2)
    
    val lighter = maxOf(luminance1, luminance2)
    val darker = minOf(luminance1, luminance2)
    
    return (lighter + 0.05f) / (darker + 0.05f)
}

fun calculateLuminance(color: Color): Float {
    val r = if (color.red <= 0.03928f) color.red / 12.92f else ((color.red + 0.055f) / 1.055f).pow(2.4f)
    val g = if (color.green <= 0.03928f) color.green / 12.92f else ((color.green + 0.055f) / 1.055f).pow(2.4f)
    val b = if (color.blue <= 0.03928f) color.blue / 12.92f else ((color.blue + 0.055f) / 1.055f).pow(2.4f)
    
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

/**
 * 접근성 좋은 색상 조합
 */
@Composable
fun AccessibleColors() {
    // ✅ 좋은 대비 (검정 배경 + 흰색 텍스트: 21:1)
    Text(
        text = "읽기 쉬운 텍스트",
        color = Color.White,
        modifier = Modifier.background(Color.Black)
    )
    
    // ❌ 나쁜 대비 (회색 배경 + 밝은 회색 텍스트: 1.5:1)
    Text(
        text = "읽기 어려운 텍스트",
        color = Color.LightGray,
        modifier = Modifier.background(Color.Gray)
    )
}
```

### 색맹 고려

```kotlin
/**
 * 색상만으로 정보 전달하지 않기
 */
@Composable
fun ColorBlindFriendly() {
    // ❌ 나쁜 예: 색상만으로 구분
    Row {
        Box(Modifier.size(50.dp).background(Color.Red))
        Box(Modifier.size(50.dp).background(Color.Green))
    }
    
    // ✅ 좋은 예: 색상 + 아이콘/텍스트
    Row {
        Row {
            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
            Text("오류", color = Color.Red)
        }
        Row {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
            Text("성공", color = Color.Green)
        }
    }
}
```

---

## 폰트 크기 조정

### 동적 폰트 크기

```kotlin
/**
 * 시스템 폰트 크기 설정 반영
 */
@Composable
fun DynamicFontSize() {
    // ✅ 좋은 예: sp 단위 사용 (시스템 설정 반영)
    Text(
        text = "조절 가능한 텍스트",
        fontSize = 16.sp  // ✅ sp 사용
    )
    
    // ❌ 나쁜 예: dp 단위 사용 (고정 크기)
    Text(
        text = "고정 크기 텍스트",
        fontSize = 16.dp.value.sp  // ❌ 피해야 함
    )
}
```

### 최소 터치 영역

```kotlin
/**
 * 최소 터치 영역: 48dp x 48dp
 */
@Composable
fun MinimumTouchTarget() {
    // ❌ 나쁜 예: 너무 작음
    IconButton(
        onClick = {},
        modifier = Modifier.size(24.dp)  // ❌ 너무 작음
    ) {
        Icon(Icons.Default.Close, contentDescription = "닫기")
    }
    
    // ✅ 좋은 예: 최소 크기 보장
    IconButton(
        onClick = {},
        modifier = Modifier.size(48.dp)  // ✅ 적절한 크기
    ) {
        Icon(Icons.Default.Close, contentDescription = "닫기")
    }
}
```

---

## 접근성 서비스

### 접근성 서비스 확인

```kotlin
/**
 * 접근성 서비스 활성화 여부 확인
 */
class AccessibilityHelper(private val context: Context) {
    
    private val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    
    /**
     * TalkBack 활성화 여부
     */
    fun isTalkBackEnabled(): Boolean {
        return accessibilityManager.isEnabled &&
                accessibilityManager.isTouchExplorationEnabled
    }
    
    /**
     * 접근성 서비스 목록
     */
    fun getEnabledAccessibilityServices(): List<String> {
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        
        return enabledServices.map { it.id }
    }
}
```

### 접근성 이벤트 전송

```kotlin
/**
 * 커스텀 접근성 이벤트
 */
@Composable
fun CustomAccessibilityEvent() {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    
    Button(onClick = {
        message = "버튼이 클릭되었습니다"
        
        // 접근성 이벤트 전송
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(message)
        
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.sendAccessibilityEvent(event)
    }) {
        Text("클릭")
    }
}
```

---

## 테스트

### 수동 테스트

```kotlin
/**
 * 접근성 수동 테스트 체크리스트
 */
class AccessibilityTestChecklist {
    
    fun performManualTests() {
        // 1. TalkBack 활성화 후 앱 사용
        // 2. 화면 확대 (Settings > Accessibility > Magnification)
        // 3. 큰 폰트 크기 (Settings > Display > Font size)
        // 4. 색상 반전 (Settings > Accessibility > Color inversion)
        // 5. 키보드만으로 네비게이션
    }
}
```

### 자동 테스트

```kotlin
/**
 * 접근성 자동 테스트
 */
@Test
fun testContentDescriptions() {
    composeTestRule.setContent {
        MyScreen()
    }
    
    // 모든 이미지에 contentDescription 있는지 확인
    composeTestRule
        .onAllNodes(hasContentDescription())
        .assertAll(hasContentDescription())
}

@Test
fun testMinimumTouchTargetSize() {
    composeTestRule.setContent {
        MyButton()
    }
    
    // 최소 터치 영역 확인 (48dp)
    composeTestRule
        .onNode(hasClickAction())
        .assertHeightIsAtLeast(48.dp)
        .assertWidthIsAtLeast(48.dp)
}
```

### Accessibility Scanner

```
Google Play에서 "Accessibility Scanner" 앱 설치
1. 앱 실행
2. 스캔할 화면으로 이동
3. 플로팅 버튼 클릭
4. 문제점 확인 및 수정
```

---

## 실전 예제

### 접근성 좋은 로그인 화면

```kotlin
/**
 * 접근성을 고려한 로그인 화면
 */
@Composable
fun AccessibleLoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // 제목
        Text(
            text = "로그인",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics {
                heading()  // 제목임을 명시
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 이메일 입력
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "이메일 입력 필드"
                }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "비밀번호 입력 필드"
                    password()  // 비밀번호 필드임을 명시
                }
        )
        
        // 에러 메시지
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite  // 자동으로 읽어줌
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 로그인 버튼
        Button(
            onClick = {
                isLoading = true
                // 로그인 로직
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)  // 최소 터치 영역
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("로그인 중...")
            } else {
                Text("로그인")
            }
        }
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. 모든 이미지에 contentDescription 제공
Image(
    painter = painterResource(R.drawable.logo),
    contentDescription = "회사 로고"
)

// 2. 의미 있는 그룹화
Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
    Text("제목")
    Text("부제목")
}

// 3. 상태 정보 제공
Checkbox(
    checked = isChecked,
    onCheckedChange = { isChecked = it },
    modifier = Modifier.semantics {
        stateDescription = if (isChecked) "선택됨" else "선택 안 됨"
    }
)

// 4. 최소 터치 영역 보장 (48dp)
IconButton(
    onClick = {},
    modifier = Modifier.size(48.dp)
) {
    Icon(Icons.Default.Menu, contentDescription = "메뉴")
}

// 5. 충분한 색상 대비
Text(
    text = "읽기 쉬운 텍스트",
    color = Color.White,
    modifier = Modifier.background(Color.Black)
)
```

### DON'Ts ❌

```kotlin
// 1. contentDescription 누락
Image(
    painter = painterResource(R.drawable.icon),
    contentDescription = null  // ❌ 장식용이 아니면 필수
)

// 2. 색상만으로 정보 전달
Box(Modifier.background(if (isError) Color.Red else Color.Green))  // ❌

// 3. 너무 작은 터치 영역
IconButton(
    onClick = {},
    modifier = Modifier.size(24.dp)  // ❌ 너무 작음
) {
    Icon(Icons.Default.Close, contentDescription = "닫기")
}

// 4. 낮은 색상 대비
Text(
    text = "읽기 어려운 텍스트",
    color = Color.LightGray,
    modifier = Modifier.background(Color.Gray)  // ❌
)

// 5. 고정 폰트 크기
Text(
    text = "텍스트",
    fontSize = 16.dp.value.sp  // ❌ dp 사용하지 말 것
)
```

---

## 참고 자료

- [Accessibility 공식 문서](https://developer.android.com/guide/topics/ui/accessibility)
- [Material Design Accessibility](https://m3.material.io/foundations/accessible-design/overview)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
