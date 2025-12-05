# Box로 커스텀 TextField 만들기

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐⭐ 고급  
> **예상 학습 시간**: 4-5시간

## 목차
1. [개요](#개요)
2. [왜 커스텀 TextField를 만드는가?](#왜-커스텀-textfield를-만드는가)
3. [BasicTextField 이해하기](#basictextfield-이해하기)
4. [Box 기반 커스텀 TextField 구현](#box-기반-커스텀-textfield-구현)
5. [텍스트 입력 상태 관리](#텍스트-입력-상태-관리)
6. [포커스와 키보드 처리](#포커스와-키보드-처리)
7. [고급 커스터마이징](#고급-커스터마이징)
8. [실전 예제](#실전-예제)
9. [성능 최적화](#성능-최적화)

---

## 개요

Material Design의 TextField로는 구현하기 어려운 독특한 디자인이나 특별한 인터랙션이 필요할 때, `BasicTextField`와 `Box`/`Row`/`Column`을 조합하여 완전히 커스텀된 TextField를 만들 수 있습니다.

### 커스텀 TextField가 필요한 경우
- ✅ Material Design을 따르지 않는 독특한 디자인
- ✅ 복잡한 레이아웃 (여러 레이어, 애니메이션)
- ✅ 특수한 입력 형식 (PIN 코드, OTP, 태그 입력 등)
- ✅ 고급 시각 효과와 인터랙션
- ✅ 완전한 컨트롤이 필요한 경우

---

## 왜 커스텀 TextField를 만드는가?

### Material TextField의 한계

```kotlin
// Material TextField는 내부 구조가 정해져 있음
OutlinedTextField(
    value = text,
    onValueChange = { text = it }
    // 커스터마이징에 한계가 있음
)
```

### BasicTextField의 유연성

```kotlin
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*

/**
 * BasicTextField는 최소한의 기능만 제공
 * 나머지는 개발자가 직접 구현
 */
@Composable
fun SimpleBasicTextField() {
    var text by remember { mutableStateOf("") }
    
    BasicTextField(
        value = text,
        onValueChange = { text = it }
        // 아무 스타일링도 없음
        // 라벨, 테두리, 배경 등 모두 직접 구현해야 함
    )
}
```

---

## BasicTextField 이해하기

### BasicTextField의 기본 구조

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BasicTextField의 모든 파라미터 이해
 */
@Composable
fun BasicTextFieldParameters() {
    var text by remember { mutableStateOf("") }
    
    BasicTextField(
        value = text,                         // 필수: 현재 텍스트
        onValueChange = { text = it },        // 필수: 값 변경 콜백
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        enabled = true,                       // 선택: 활성화 여부
        readOnly = false,                     // 선택: 읽기 전용
        textStyle = TextStyle(              // 선택: 텍스트 스타일
            fontSize = 16.sp,
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions.Default,  // 선택: 키보드 옵션
        keyboardActions = KeyboardActions.Default,  // 선택: 키보드 액션
        singleLine = false,                   // 선택: 단일 라인
        maxLines = Int.MAX_VALUE,            // 선택: 최대 라인
        minLines = 1,                        // 선택: 최소 라인
        visualTransformation = VisualTransformation.None, // 선택: 시각 변환
        onTextLayout = {},                   // 선택: 텍스트 레이아웃 콜백
        interactionSource = remember { MutableInteractionSource() },
        cursorBrush = SolidColor(Color.Black), // 선택: 커서 색상
        decorationBox = { innerTextField ->   // 선택: 장식 박스 (중요!)
            /**
             * decorationBox: TextField 주변을 꾸미는 곳
             * innerTextField()를 호출하면 실제 입력 필드가 렌더링됨
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                innerTextField() // 실제 TextField 위치
            }
        }
    )
}
```

### decorationBox의 중요성

```kotlin
/**
 * decorationBox는 커스텀 TextField의 핵심
 * - innerTextField() 호출로 실제 입력 필드 배치
 * - 주변에 원하는 UI 추가 가능
 */
@Composable
fun DecorationBoxExample() {
    var text by remember { mutableStateOf("") }
    
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        decorationBox = { innerTextField ->
            Column {
                // 상단 라벨
                Text("사용자 이름", fontSize = 12.sp)
                
                // 입력 필드와 아이콘
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    // 앞쪽 아이콘
                    Icon(Icons.Default.Person, contentDescription = null)
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 실제 입력 필드
                    Box(modifier = Modifier.weight(1f)) {
                        // 플레이스홀더
                        if (text.isEmpty()) {
                            Text(
                                "여기에 입력하세요",
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                    
                    // 뒤쪽 클리어 버튼
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { text = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "지우기")
                        }
                    }
                }
                
                // 하단 도움말
                Text(
                    "2-20자 사이로 입력하세요",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    )
}
```

---

## Box 기반 커스텀 TextField 구현

### 1. 기본 커스텀 TextField

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Box 기반 커스텀 TextField
 * 
 * @param value 현재 텍스트 값
 * @param onValueChange 값 변경 콜백
 * @param modifier Modifier
 * @param label 라벨 텍스트
 * @param placeholder 플레이스홀더 텍스트
 * @param enabled 활성화 여부
 * @param readOnly 읽기 전용 여부
 * @param isError 에러 상태
 * @param leadingIcon 앞쪽 아이콘
 * @param trailingIcon 뒤쪽 아이콘
 * @param singleLine 단일 라인 여부
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    /**
     * 인터랙션 추적
     */
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    /**
     * 색상 정의
     */
    val backgroundColor = when {
        !enabled -> Color(0xFFF5F5F5)
        isError -> Color(0xFFFFF3F3)
        isFocused -> Color.White
        else -> Color(0xFFFAFAFA)
    }
    
    val borderColor = when {
        !enabled -> Color(0xFFE0E0E0)
        isError -> Color(0xFFD32F2F)
        isFocused -> Color(0xFF6200EE)
        else -> Color(0xFFBDBDBD)
    }
    
    val borderWidth = if (isFocused) 2.dp else 1.dp
    
    /**
     * 텍스트 색상
     */
    val textColor = when {
        !enabled -> Color(0xFF9E9E9E)
        isError -> Color(0xFFD32F2F)
        else -> Color(0xFF212121)
    }
    
    Column(modifier = modifier) {
        /**
         * 라벨 (선택적)
         */
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = when {
                    isError -> Color(0xFFD32F2F)
                    isFocused -> Color(0xFF6200EE)
                    else -> Color(0xFF757575)
                },
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        /**
         * 메인 입력 박스
         */
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = textColor
            ),
            cursorBrush = SolidColor(Color(0xFF6200EE)),
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                /**
                 * 커스텀 장식 박스
                 */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(8.dp))
                        .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /**
                     * 앞쪽 아이콘
                     */
                    leadingIcon?.let {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            it()
                        }
                    }
                    
                    /**
                     * 입력 필드 영역
                     */
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        /**
                         * 플레이스홀더 (값이 비어있을 때만 표시)
                         */
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                fontSize = 16.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        
                        /**
                         * 실제 입력 필드
                         */
                        innerTextField()
                    }
                    
                    /**
                     * 뒤쪽 아이콘
                     */
                    trailingIcon?.let {
                        Box(modifier = Modifier.padding(start = 8.dp)) {
                            it()
                        }
                    }
                }
            }
        )
    }
}
```

### 2. 사용 예제

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun CustomTextFieldUsageExample() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 이메일 입력
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            placeholder = "example@email.com",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFF6200EE)
                )
            },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { email = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "지우기"
                        )
                    }
                }
            }
        )
        
        // 비밀번호 입력
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            placeholder = "비밀번호를 입력하세요",
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF6200EE)
                )
            }
        )
        
        // 검색 입력
        CustomTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = "검색...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF757575)
                )
            }
        )
    }
}
```

---

## 텍스트 입력 상태 관리

### 1. 포커스 상태에 따른 스타일 변경

```kotlin
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween

/**
 * 포커스 상태에 따라 애니메이션되는 TextField
 */
@Composable
fun AnimatedFocusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    /**
     * 애니메이션 값들
     */
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF6200EE) else Color(0xFFBDBDBD),
        animationSpec = tween(300),
        label = "border_color"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 1.dp,
        animationSpec = tween(300),
        label = "border_width"
    )
    
    val labelColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF6200EE) else Color(0xFF757575),
        animationSpec = tween(300),
        label = "label_color"
    )
    
    Column(modifier = modifier) {
        /**
         * 애니메이션되는 라벨
         */
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = labelColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            cursorBrush = SolidColor(Color(0xFF6200EE)),
            singleLine = true,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )
    }
}
```

### 2. 에러 상태 처리

```kotlin
/**
 * 에러 상태를 시각적으로 표현하는 TextField
 */
@Composable
fun ErrorStateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    /**
     * 에러 상태에 따른 색상
     */
    val borderColor = when {
        isError -> Color(0xFFD32F2F)  // 빨간색
        isFocused -> Color(0xFF6200EE) // 보라색
        else -> Color(0xFFBDBDBD)      // 회색
    }
    
    val backgroundColor = when {
        isError -> Color(0xFFFFF3F3)  // 연한 빨간색
        else -> Color.White
    }
    
    Column(modifier = modifier) {
        /**
         * 라벨
         */
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isError) Color(0xFFD32F2F) else Color(0xFF757575),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        /**
         * 입력 필드
         */
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            cursorBrush = SolidColor(if (isError) Color(0xFFD32F2F) else Color(0xFF6200EE)),
            singleLine = true,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(8.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    
                    /**
                     * 에러 아이콘
                     */
                    if (isError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "에러",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )
        
        /**
         * 에러 메시지
         */
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
```

---

## 포커스와 키보드 처리

### 1. 프로그래밍 방식으로 포커스 제어

```kotlin
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * 포커스를 프로그래밍적으로 제어하는 예제
 */
@Composable
fun FocusControlExample() {
    /**
     * FocusRequester 생성
     */
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 필드 1
        CustomTextField(
            value = field1,
            onValueChange = { 
                field1 = it
                // 입력이 완료되면 다음 필드로 자동 이동
                if (it.length == 4) {
                    focusRequester2.requestFocus()
                }
            },
            label = "필드 1 (4자리)",
            modifier = Modifier.focusRequester(focusRequester1)
        )
        
        // 필드 2
        CustomTextField(
            value = field2,
            onValueChange = { 
                field2 = it
                if (it.length == 4) {
                    focusRequester3.requestFocus()
                }
            },
            label = "필드 2 (4자리)",
            modifier = Modifier.focusRequester(focusRequester2)
        )
        
        // 필드 3
        CustomTextField(
            value = field3,
            onValueChange = { field3 = it },
            label = "필드 3",
            modifier = Modifier.focusRequester(focusRequester3)
        )
        
        /**
         * 첫 번째 필드에 포커스 주기 버튼
         */
        Button(onClick = { focusRequester1.requestFocus() }) {
            Text("첫 번째 필드 포커스")
        }
    }
}
```

### 2. 키보드 IME 액션 처리

```kotlin
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

/**
 * 키보드 IME 액션을 처리하는 TextField
 */
@Composable
fun IMEActionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    
    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
                onImeAction()
            },
            onDone = {
                focusManager.clearFocus()
                onImeAction()
            },
            onSearch = {
                focusManager.clearFocus()
                onImeAction()
            },
            onSend = {
                focusManager.clearFocus()
                onImeAction()
            }
        )
    )
}
```

---

## 고급 커스터마이징

### 1. PIN 코드 입력 필드

```kotlin
/**
 * PIN 코드 입력 (4-6자리)
 * - 각 자리를 별도 박스로 표시
 * - 자동으로 다음 칸으로 이동
 */
@Composable
fun PinCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    pinLength: Int = 4
) {
    /**
     * 숫자만 허용하고 길이 제한
     */
    val handleValueChange: (String) -> Unit = { newValue ->
        val filtered = newValue.filter { it.isDigit() }.take(pinLength)
        onValueChange(filtered)
    }
    
    BasicTextField(
        value = value,
        onValueChange = handleValueChange,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword
        ),
        decorationBox = { innerTextField ->
            /**
             * PIN 입력 박스들
             */
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pinLength) { index ->
                    /**
                     * 각 PIN 자리
                     */
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)  // 정사각형
                            .border(
                                width = 2.dp,
                                color = if (index == value.length) 
                                    Color(0xFF6200EE)  // 현재 입력 위치
                                else if (index < value.length)
                                    Color(0xFF4CAF50)  // 입력됨
                                else 
                                    Color(0xFFBDBDBD), // 미입력
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (index < value.length) 
                                    Color(0xFFF3E5F5) 
                                else 
                                    Color.White,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        /**
                         * 입력된 숫자 표시 (마스킹)
                         */
                        Text(
                            text = if (index < value.length) "●" else "",
                            fontSize = 24.sp,
                            color = Color(0xFF6200EE)
                        )
                    }
                }
            }
            
            /**
             * 실제 TextField는 숨김
             * (키보드 입력만 받음)
             */
            Box(modifier = Modifier.alpha(0f)) {
                innerTextField()
            }
        }
    )
}

/**
 * 사용 예제
 */
@Composable
fun PinCodeExample() {
    var pin by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PIN 코드 입력",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PinCodeTextField(
            value = pin,
            onValueChange = { pin = it },
            pinLength = 6
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "입력: $pin",
            color = Color.Gray
        )
    }
}
```

### 2. OTP 입력 필드

```kotlin
/**
 * OTP (One-Time Password) 입력 필드
 * - 자동으로 다음 칸으로 이동
 * - Backspace로 이전 칸으로 이동
 */
@Composable
fun OTPTextField(
    otpLength: Int = 6,
    onOtpComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * 각 자리의 값을 저장
     */
    var otpValues by remember { 
        mutableStateOf(List(otpLength) { "" }) 
    }
    
    /**
     * FocusRequesters
     */
    val focusRequesters = remember {
        List(otpLength) { FocusRequester() }
    }
    
    /**
     * 완성된 OTP 체크
     */
    LaunchedEffect(otpValues) {
        if (otpValues.all { it.isNotEmpty() }) {
            onOtpComplete(otpValues.joinToString(""))
        }
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        otpValues.forEachIndexed { index, value ->
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    /**
                     * 한 자리 숫자만 허용
                     */
                    val filtered = newValue.filter { it.isDigit() }.take(1)
                    
                    if (filtered.isNotEmpty()) {
                        // 값 업데이트
                        otpValues = otpValues.toMutableList().apply {
                            this[index] = filtered
                        }
                        
                        // 다음 칸으로 이동
                        if (index < otpLength - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    } else if (newValue.isEmpty() && value.isNotEmpty()) {
                        // Backspace 처리
                        otpValues = otpValues.toMutableList().apply {
                            this[index] = ""
                        }
                        
                        // 이전 칸으로 이동
                        if (index > 0) {
                            focusRequesters[index - 1].requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .focusRequester(focusRequesters[index]),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = if (value.isNotEmpty()) 
                                    Color(0xFF6200EE) 
                                else 
                                    Color(0xFFBDBDBD),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                }
            )
        }
    }
    
    /**
     * 첫 번째 필드에 초기 포커스
     */
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}
```

### 3. 태그 입력 필드

```kotlin
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

/**
 * 태그 입력 필드
 * - Enter 또는 콤마로 태그 추가
 * - 태그 클릭으로 삭제
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInputField(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "태그 입력 (Enter로 추가)"
) {
    var currentInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier) {
        /**
         * 태그 목록 표시
         */
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    /**
                     * 개별 태그 칩
                     */
                    AssistChip(
                        onClick = {
                            // 태그 삭제
                            onTagsChange(tags - tag)
                        },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "삭제",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
        
        /**
         * 태그 입력 필드
         */
        BasicTextField(
            value = currentInput,
            onValueChange = { newValue ->
                when {
                    // 콤마 입력 시 태그 추가
                    newValue.contains(",") -> {
                        val tag = newValue.replace(",", "").trim()
                        if (tag.isNotEmpty() && !tags.contains(tag)) {
                            onTagsChange(tags + tag)
                        }
                        currentInput = ""
                    }
                    else -> {
                        currentInput = newValue
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Enter 시 태그 추가
                    val tag = currentInput.trim()
                    if (tag.isNotEmpty() && !tags.contains(tag)) {
                        onTagsChange(tags + tag)
                    }
                    currentInput = ""
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    if (currentInput.isEmpty()) {
                        Text(placeholder, color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )
    }
}

/**
 * 사용 예제
 */
@Composable
fun TagInputExample() {
    var tags by remember { mutableStateOf(listOf("Kotlin", "Compose")) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("관심 태그", style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TagInputField(
            tags = tags,
            onTagsChange = { tags = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("현재 태그: ${tags.joinToString(", ")}")
    }
}
```

---

## 실전 예제

### 검색 바 with 애니메이션

```kotlin
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

/**
 * 확장 가능한 검색 바
 * - 아이콘 클릭 시 확장
 * - ESC 또는 외부 클릭 시 축소
 */
@Composable
fun ExpandableSearchBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) with slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        },
        label = "search_bar"
    ) { expanded ->
        if (expanded) {
            /**
             * 확장된 검색 바
             */
            BasicTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    onSearch(it)
                },
                modifier = modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(2.dp, Color(0xFF6200EE), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            tint = Color(0xFF6200EE)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("검색...", color = Color.Gray)
                            }
                            innerTextField()
                        }
                        
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "지우기"
                                )
                            }
                        }
                        
                        IconButton(onClick = { 
                            isExpanded = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "닫기"
                            )
                        }
                    }
                }
            )
            
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            /**
             * 축소된 검색 아이콘
             */
            IconButton(
                onClick = { isExpanded = true },
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = Color(0xFF6200EE)
                )
            }
        }
    }
}
```

---

## 성능 최적화

### 1. remember와 derivedStateOf 활용

```kotlin
/**
 * 성능 최적화된 TextField
 * - derivedStateOf로 불필요한 리컴포지션 방지
 */
@Composable
fun OptimizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    validator: (String) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    /**
     * 검증 결과를 캐싱
     * - validator가 변경되거나 value가 변경될 때만 재계산
     */
    val isValid by remember {
        derivedStateOf { validator(value) }
    }
    
    /**
     * 색상 계산을 캐싱
     */
    val borderColor = remember(isValid) {
        if (isValid) Color(0xFF4CAF50) else Color(0xFFD32F2F)
    }
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = TextStyle(fontSize = 16.sp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                innerTextField()
            }
        }
    )
}
```

### 2. LazyColumn에서의 TextField

```kotlin
/**
 * 리스트에서 TextField 사용 시 주의사항
 * - 각 항목의 값과 상태를 별도로 관리
 */
@Composable
fun TextFieldList() {
    /**
     * 아이템별 상태를 Map으로 관리
     */
    val itemStates = remember {
        mutableStateMapOf<Int, String>()
    }
    
    LazyColumn {
        items(100) { index ->
            CustomTextField(
                value = itemStates[index] ?: "",
                onValueChange = { newValue ->
                    itemStates[index] = newValue
                },
                label = "항목 $index",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
```

---

## 요약

Box와 BasicTextField로 커스텀 TextField를 만들기 위한 핵심 포인트:

1. **BasicTextField 이해**: decorationBox가 핵심
2. **상태 관리**: InteractionSource로 포커스, 호버 등 추적
3. **포커스 제어**: FocusRequester로 프로그래밍 방식 제어
4. **커스텀 디자인**: Box/Row/Column으로 원하는 레이아웃 구현
5. **특수 입력**: PIN, OTP, 태그 등 특별한 입력 형식 구현
6. **성능 최적화**: remember, derivedStateOf 활용
7. **접근성**: 키보드 네비게이션, IME 액션 처리

Material TextField로 부족할 때, 이제 완전히 커스텀된 TextField를 만들 수 있습니다!
