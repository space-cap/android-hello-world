# 커스텀 회원가입 화면 만들기 (BasicTextField & Box)

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐⭐ 고급  
> **예상 학습 시간**: 3-4시간  
> **선행 학습**: [103-커스텀 로그인](./103-custom-signin-screen-guide.md), [104-회원가입](./104-signup-screen-complete-guide.md)

## 목차
1. [개요](#개요)
2. [프로젝트 구조](#프로젝트-구조)
3. [커스텀 컴포넌트](#커스텀-컴포넌트)
4. [회원가입 화면 구현](#회원가입-화면-구현)
5. [약관 동의 UI](#약관-동의-ui)
6. [애니메이션 효과](#애니메이션-효과)
7. [완성된 화면](#완성된-화면)

---

## 개요

BasicTextField와 Box만으로 **완전 커스텀 회원가입 화면**을 구현합니다. Material3 없이 독특한 디자인을 자유롭게 만들 수 있습니다.

### 구현할 기능

✅ 커스텀 입력 필드 (4개)  
✅ 커스텀 체크박스  
✅ 스텝 인디케이터  
✅ 비밀번호 강도 바  
✅ 부드러운 애니메이션  
✅ 그라데이션 디자인  

---

## 프로젝트 구조

```
app/src/main/java/com/example/yourapp/
├── ui/
│   ├── signup/
│   │   ├── CustomSignUpScreen.kt       # 메인 화면
│   │   ├── SignUpViewModel.kt          # ViewModel (동일)
│   │   └── components/
│   │       ├── CustomTextField.kt      # 커스텀 입력 필드 (재사용)
│   │       ├── CustomCheckbox.kt       # 커스텀 체크박스
│   │       ├── StepIndicator.kt        # 단계 표시기
│   │       ├── PasswordStrengthBar.kt  # 비밀번호 강도
│   │       └── TermsCard.kt            # 약관 카드
│   └── theme/
│       └── CustomColors.kt             # 색상 정의
└── util/
    └── AnimationUtils.kt               # 애니메이션 유틸
```

---

## 커스텀 컴포넌트

### 커스텀 체크박스

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signup/components/CustomCheckbox.kt`

```kotlin
package com.example.yourapp.ui.signup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 완전 커스텀 체크박스
 * - Box로 구현
 * - 체크 애니메이션
 */
@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedColor: Color = Color(0xFF6C63FF),
    uncheckedColor: Color = Color(0xFF3A3A4A),
    checkmarkColor: Color = Color.White
) {
    /**
     * 체크 애니메이션
     */
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "checkbox scale"
    )
    
    /**
     * 배경색 애니메이션
     */
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) checkedColor else Color.Transparent,
        animationSpec = tween(200),
        label = "checkbox bg"
    )
    
    /**
     * 테두리 색상 애니메이션
     */
    val borderColor by animateColorAsState(
        targetValue = if (checked) checkedColor else uncheckedColor,
        animationSpec = tween(200),
        label = "checkbox border"
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        /**
         * 체크 아이콘
         */
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = checkmarkColor,
                modifier = Modifier
                    .size(16.dp)
                    .scale(scale)
            )
        }
    }
}
```

### 비밀번호 강도 바

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signup/components/PasswordStrengthBar.kt`

```kotlin
package com.example.yourapp.ui.signup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 커스텀 비밀번호 강도 표시기
 * - 애니메이션되는 진행 바
 * - 색상 변화
 */
@Composable
fun CustomPasswordStrengthBar(
    password: String,
    modifier: Modifier = Modifier
) {
    /**
     * 강도 계산 (0~4)
     */
    val strength = remember(password) {
        calculatePasswordStrength(password)
    }
    
    /**
     * 진행률 애니메이션 (0~1)
     */
    val progress by animateFloatAsState(
        targetValue = strength / 4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "strength progress"
    )
    
    /**
     * 색상 정의
     */
    val strengthColor = when (strength) {
        0 -> Color.Transparent
        1 -> Color(0xFFFF6B6B)      // 빨강 - 약함
        2 -> Color(0xFFFF9800)      // 주황 - 보통
        3 -> Color(0xFFFFEB3B)      // 노랑 - 강함
        4 -> Color(0xFF4CAF50)      // 녹색 - 매우 강함
        else -> Color.Gray
    }
    
    val strengthText = when (strength) {
        0 -> ""
        1 -> "약함"
        2 -> "보통"
        3 -> "강함"
        4 -> "매우 강함"
        else -> ""
    }
    
    /**
     * 색상 애니메이션
     */
    val animatedColor by animateColorAsState(
        targetValue = strengthColor,
        animationSpec = tween(300),
        label = "strength color"
    )
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        /**
         * 강도 바
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2A2A3A))
        ) {
            /**
             * 진행 바
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(animatedColor)
            )
        }
        
        /**
         * 강도 텍스트
         */
        if (strengthText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "비밀번호 강도: $strengthText",
                color = animatedColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 비밀번호 강도 계산
 */
private fun calculatePasswordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    
    var strength = 0
    
    // 1. 길이 체크 (8자 이상)
    if (password.length >= 8) strength++
    
    // 2. 대소문자 포함
    if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) {
        strength++
    }
    
    // 3. 숫자 포함
    if (password.any { it.isDigit() }) strength++
    
    // 4. 특수문자 포함
    if (password.any { !it.isLetterOrDigit() }) strength++
    
    return strength
}
```

### 스텝 인디케이터

```kotlin
/**
 * 단계 표시 인디케이터
 * - 회원가입 진행 상황 표시
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF6C63FF),
    inactiveColor: Color = Color(0xFF3A3A4A)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { step ->
            /**
             * 각 스텝 바
             */
            val isActive = step < currentStep
            
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                animationSpec = tween(300),
                label = "step color $step"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
```

---

## 회원가입 화면 구현

### 메인 화면

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signup/CustomSignUpScreen.kt`

```kotlin
package com.example.yourapp.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 완전 커스텀 회원가입 화면
 * - Material3 컴포넌트 미사용
 * - BasicTextField와 Box만으로 구현
 */
@Composable
fun CustomSignUpScreen(
    viewModel: SignUpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    /**
     * 회원가입 성공 시 처리
     */
    LaunchedEffect(uiState.isSignUpSuccessful) {
        if (uiState.isSignUpSuccessful) {
            onSignUpSuccess()
        }
    }
    
    /**
     * 그라데이션 배경
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F1E),
                        Color(0xFF1E1E2E),
                        Color(0xFF2E2E3E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            /**
             * 뒤로가기 버튼
             */
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            /**
             * 헤더
             */
            SignUpHeader()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            /**
             * 스텝 인디케이터 (예: 1/2 단계)
             */
            StepIndicator(
                currentStep = 1,
                totalSteps = 2
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            /**
             * 입력 폼
             */
            SignUpForm(viewModel, uiState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            /**
             * 비밀번호 강도 표시
             */
            if (uiState.password.isNotEmpty()) {
                CustomPasswordStrengthBar(password = uiState.password)
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            /**
             * 약관 동의
             */
            CustomTermsAgreement(
                isAllAgreed = uiState.isAllAgreed,
                isTermsAgreed = uiState.isTermsAgreed,
                isPrivacyAgreed = uiState.isPrivacyAgreed,
                isMarketingAgreed = uiState.isMarketingAgreed,
                onAllAgreedChanged = {
                    viewModel.onEvent(SignUpEvent.AllAgreedChanged)
                },
                onTermsAgreedChanged = {
                    viewModel.onEvent(SignUpEvent.TermsAgreedChanged(it))
                },
                onPrivacyAgreedChanged = {
                    viewModel.onEvent(SignUpEvent.PrivacyAgreedChanged(it))
                },
                onMarketingAgreedChanged = {
                    viewModel.onEvent(SignUpEvent.MarketingAgreedChanged(it))
                },
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            /**
             * 에러 메시지
             */
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            /**
             * 회원가입 버튼
             */
            CustomGradientButton(
                text = "회원가입 완료",
                onClick = { viewModel.onEvent(SignUpEvent.SignUpClicked) },
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )
        }
    }
}

/**
 * 헤더 섹션
 */
@Composable
private fun SignUpHeader() {
    Column {
        Text(
            text = "환영합니다!",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "새 계정을 만들어보세요",
            color = Color(0xFF9A9AA5),
            fontSize = 16.sp
        )
    }
}

/**
 * 입력 폼
 */
@Composable
private fun SignUpForm(
    viewModel: SignUpViewModel,
    uiState: SignUpUiState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 이름 입력
         */
        CustomTextField(
            value = uiState.name,
            onValueChange = { viewModel.onEvent(SignUpEvent.NameChanged(it)) },
            placeholder = "이름",
            leadingIcon = Icons.Default.Person,
            isError = uiState.nameError != null,
            errorMessage = uiState.nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            enabled = !uiState.isLoading
        )
        
        /**
         * 이메일 입력
         */
        CustomTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
            placeholder = "이메일",
            leadingIcon = Icons.Default.Email,
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            enabled = !uiState.isLoading
        )
        
        /**
         * 비밀번호 입력
         */
        CustomPasswordTextField(
            password = uiState.password,
            onPasswordChange = { 
                viewModel.onEvent(SignUpEvent.PasswordChanged(it)) 
            },
            placeholder = "비밀번호 (8자 이상, 대소문자/숫자/특수문자)",
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            enabled = !uiState.isLoading
        )
        
        /**
         * 비밀번호 확인 입력
         */
        CustomPasswordTextField(
            password = uiState.passwordConfirm,
            onPasswordChange = { 
                viewModel.onEvent(SignUpEvent.PasswordConfirmChanged(it)) 
            },
            placeholder = "비밀번호 확인",
            isError = uiState.passwordConfirmError != null,
            errorMessage = uiState.passwordConfirmError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            enabled = !uiState.isLoading
        )
    }
}
```

---

## 약관 동의 UI

### 커스텀 약관 카드

```kotlin
/**
 * 커스텀 약관 동의 섹션
 * - 글래스모피즘 스타일
 */
@Composable
fun CustomTermsAgreement(
    isAllAgreed: Boolean,
    isTermsAgreed: Boolean,
    isPrivacyAgreed: Boolean,
    isMarketingAgreed: Boolean,
    onAllAgreedChanged: () -> Unit,
    onTermsAgreedChanged: (Boolean) -> Unit,
    onPrivacyAgreedChanged: (Boolean) -> Unit,
    onMarketingAgreedChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    /**
     * 글래스모피즘 카드
     */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /**
             * 전체 동의
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onAllAgreedChanged() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomCheckbox(
                    checked = isAllAgreed,
                    onCheckedChange = { onAllAgreedChanged() },
                    enabled = enabled
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    "전체 동의",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            /**
             * 구분선
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            
            /**
             * 이용약관 (필수)
             */
            TermsCheckboxRow(
                checked = isTermsAgreed,
                onCheckedChange = onTermsAgreedChanged,
                text = "(필수) 이용약관 동의",
                enabled = enabled
            )
            
            /**
             * 개인정보 처리방침 (필수)
             */
            TermsCheckboxRow(
                checked = isPrivacyAgreed,
                onCheckedChange = onPrivacyAgreedChanged,
                text = "(필수) 개인정보 처리방침 동의",
                enabled = enabled
            )
            
            /**
             * 마케팅 (선택)
             */
            TermsCheckboxRow(
                checked = isMarketingAgreed,
                onCheckedChange = onMarketingAgreedChanged,
                text = "(선택) 마케팅 정보 수신 동의",
                enabled = enabled,
                isOptional = true
            )
        }
    }
}

/**
 * 약관 체크박스 행
 */
@Composable
private fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    enabled: Boolean,
    isOptional: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text,
            color = if (isOptional) Color(0xFF9A9AA5) else Color.White,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        /**
         * 상세보기 아이콘
         */
        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable { /* 상세보기 */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "상세보기",
                tint = Color(0xFF7A7A8A),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
```

---

## 애니메이션 효과

### 진입 애니메이션

```kotlin
/**
 * 화면 진입 애니메이션
 * - 페이드인 + 슬라이드업
 */
@Composable
fun AnimatedSignUpScreen(
    viewModel: SignUpViewModel,
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    /**
     * 애니메이션 상태
     */
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }
    
    /**
     * 알파 애니메이션
     */
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    /**
     * Y 오프셋 애니메이션
     */
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .offset(y = offsetY)
    ) {
        CustomSignUpScreen(
            viewModel = viewModel,
            onSignUpSuccess = onSignUpSuccess,
            onNavigateBack = onNavigateBack
        )
    }
}
```

### 입력 필드 순차 애니메이션

```kotlin
/**
 * 입력 필드 순차 등장 애니메이션
 */
@Composable
fun StaggeredSignUpForm(
    viewModel: SignUpViewModel,
    uiState: SignUpUiState
) {
    val fields = listOf("이름", "이메일", "비밀번호", "비밀번호 확인")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        fields.forEachIndexed { index, field ->
            /**
             * 각 필드의 지연 시간
             */
            var isVisible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay((index * 100).toLong())
                isVisible = true
            }
            
            /**
             * 필드 애니메이션
             */
            val alpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(400),
                label = "field $index alpha"
            )
            
            val offsetX by animateDpAsState(
                targetValue = if (isVisible) 0.dp else (-20).dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "field $index offset"
            )
            
            Box(
                modifier = Modifier
                    .alpha(alpha)
                    .offset(x = offsetX)
            ) {
                when (index) {
                    0 -> CustomTextField(/* 이름 */)
                    1 -> CustomTextField(/* 이메일 */)
                    2 -> CustomPasswordTextField(/* 비밀번호 */)
                    3 -> CustomPasswordTextField(/* 비밀번호 확인 */)
                }
            }
        }
    }
}
```

---

## 완성된 화면

### 성공 애니메이션

```kotlin
/**
 * 회원가입 성공 애니메이션
 * - 체크마크 애니메이션
 */
@Composable
fun SignUpSuccessAnimation(
    onDismiss: () -> Unit
) {
    /**
     * 애니메이션 상태
     */
    var showCheckmark by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        showCheckmark = true
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }
    
    /**
     * 체크마크 스케일
     */
    val scale by animateFloatAsState(
        targetValue = if (showCheckmark) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark scale"
    )
    
    /**
     * 오버레이
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /**
             * 원형 배경 + 체크마크
             */
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "회원가입 완료!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "로그인 페이지로 이동합니다",
                color = Color(0xFF9A9AA5),
                fontSize = 14.sp
            )
        }
    }
}
```

---

## 요약

### Material3 vs 커스텀 비교

| 기능 | Material3 | 커스텀 |
|------|-----------|--------|
| **개발 시간** | 2-3시간 | 4-5시간 |
| **디자인 자유도** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **애니메이션** | 기본 제공 | 완전 커스텀 |
| **브랜딩** | 제한적 | 무제한 |
| **유지보수** | 쉬움 | 신중 필요 |

### 핵심 구현 사항

✅ **커스텀 체크박스**: 애니메이션 포함  
✅ **비밀번호 강도 바**: 실시간 색상 변화  
✅ **순차 애니메이션**: 필드별 등장 효과  
✅ **글래스모피즘**: 약관 카드 디자인  
✅ **성공 애니메이션**: 체크마크 효과  

### 실무 적용 팁

💡 **디자인 시스템 먼저**: 색상, 간격, 폰트 정의  
💡 **컴포넌트 재사용**: 로그인과 공유  
💡 **애니메이션 절제**: 과하면 오히려 방해  
💡 **접근성 고려**: 색상만으로 구분 X  

완전히 독창적인 회원가입 경험을 만들어보세요! 🎨✨
