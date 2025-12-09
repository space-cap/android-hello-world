# 커스텀 로그인 화면 만들기 (BasicTextField & Box)

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐⭐ 고급  
> **예상 학습 시간**: 3-4시간  
> **선행 학습**: [95-커스텀 TextField](./95-custom-textfield-with-box.md), [93-커스텀 Button](./93-custom-button-with-box.md), [99-Modifier 기초](./99-compose-modifier-fundamentals.md)

## 목차
1. [개요](#개요)
2. [왜 커스텀 컴포넌트인가](#왜-커스텀-컴포넌트인가)
3. [프로젝트 구조](#프로젝트-구조)
4. [커스텀 TextField 구현](#커스텀-textfield-구현)
5. [커스텀 Button 구현](#커스텀-button-구현)
6. [완성된 로그인 화면](#완성된-로그인-화면)
7. [애니메이션 추가](#애니메이션-추가)
8. [다크 모드 지원](#다크-모드-지원)

---

## 개요

이 가이드에서는 **Material3 컴포넌트 없이** BasicTextField와 Box만으로 완전히 커스텀한 로그인 화면을 만듭니다. 디자인 시스템에 완벽하게 맞춘 독특한 UI를 구현할 수 있습니다.

### 구현할 기능

✅ BasicTextField 기반 완전 커스텀 입력 필드  
✅ Box 기반 완전 커스텀 버튼  
✅ 그라데이션 배경  
✅ 부드러운 애니메이션  
✅ 글래스모피즘 효과  
✅ 다크 모드 지원  
✅ 리플 효과 구현  

---

## 왜 커스텀 컴포넌트인가

### Material3의 한계

```kotlin
/**
 * ❌ Material3 TextField의 제약
 * - 정해진 디자인 스타일
 * - 제한적인 커스터마이징
 * - 독특한 브랜드 디자인 구현 어려움
 */
OutlinedTextField(
    value = "",
    onValueChange = { },
    // 여기서 할 수 있는 것이 제한적
)

/**
 * ✅ BasicTextField의 자유도
 * - 완전한 커스터마이징
 * - 독특한 디자인 구현
 * - 브랜드 아이덴티티 반영
 */
BasicTextField(
    value = "",
    onValueChange = { },
    decorationBox = { innerTextField ->
        // 원하는 모든 것을 구현 가능!
    }
)
```

---

## 프로젝트 구조

```
app/src/main/java/com/example/yourapp/
├── ui/
│   ├── signin/
│   │   ├── CustomSignInScreen.kt        # 메인 화면
│   │   ├── SignInViewModel.kt           # ViewModel (동일)
│   │   └── components/
│   │       ├── CustomTextField.kt       # 커스텀 입력 필드
│   │       ├── CustomButton.kt          # 커스텀 버튼
│   │       └── GlassmorphicCard.kt      # 글래스모피즘 카드
│   └── theme/
│       ├── Color.kt                     # 커스텀 색상
│       └── Shape.kt                     # 커스텀 Shape
└── util/
    └── ModifierExt.kt                   # Modifier 확장 함수
```

---

## 커스텀 TextField 구현

### 기본 커스텀 TextField

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/components/CustomTextField.kt`

```kotlin
package com.example.yourapp.ui.signin.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 완전 커스텀 TextField
 * - BasicTextField 기반
 * - 모든 디자인 요소를 직접 구현
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFF1E1E2E),
    focusedBorderColor: Color = Color(0xFF6C63FF),
    unfocusedBorderColor: Color = Color(0xFF3A3A4A),
    errorBorderColor: Color = Color(0xFFFF6B6B),
    textColor: Color = Color.White,
    placeholderColor: Color = Color(0xFF7A7A8A)
) {
    /**
     * 포커스 상태 추적
     */
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    /**
     * 애니메이션되는 테두리 색상
     */
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> errorBorderColor
            isFocused -> focusedBorderColor
            else -> unfocusedBorderColor
        },
        animationSpec = tween(300),
        label = "border color"
    )
    
    /**
     * 컨테이너
     */
    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(focusedBorderColor),
            decorationBox = { innerTextField ->
                /**
                 * 커스텀 decoration
                 * - 배경, 테두리, 아이콘, placeholder 모두 여기서 구현
                 */
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /**
                     * Leading Icon
                     */
                    leadingIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isFocused) focusedBorderColor else placeholderColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    /**
                     * TextField + Placeholder
                     */
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        /**
                         * Placeholder
                         */
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = placeholderColor,
                                fontSize = 16.sp
                            )
                        }
                        
                        /**
                         * 실제 입력 필드
                         */
                        innerTextField()
                    }
                    
                    /**
                     * Trailing Icon
                     */
                    trailingIcon?.let {
                        Spacer(modifier = Modifier.width(12.dp))
                        it()
                    }
                }
            }
        )
        
        /**
         * 에러 메시지
         */
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = errorBorderColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
```

### 비밀번호 전용 커스텀 TextField

```kotlin
/**
 * 비밀번호 입력 전용 커스텀 TextField
 * - 보기/숨기기 기능 내장
 */
@Composable
fun CustomPasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "비밀번호",
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    /**
     * 비밀번호 표시 상태
     */
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    CustomTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Lock,
        trailingIcon = {
            /**
             * 커스텀 아이콘 버튼
             */
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { isPasswordVisible = !isPasswordVisible },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPasswordVisible) 
                        Icons.Default.Visibility 
                    else 
                        Icons.Default.VisibilityOff,
                    contentDescription = if (isPasswordVisible) 
                        "비밀번호 숨기기" 
                    else 
                        "비밀번호 보기",
                    tint = Color(0xFF7A7A8A),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        isError = isError,
        errorMessage = errorMessage,
        visualTransformation = if (isPasswordVisible) 
            VisualTransformation.None 
        else 
            PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        enabled = enabled
    )
}
```

---

## 커스텀 Button 구현

### 그라데이션 버튼

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/components/CustomButton.kt`

```kotlin
package com.example.yourapp.ui.signin.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 그라데이션 커스텀 버튼
 * - 완전 커스텀 구현
 * - 프레스 애니메이션
 */
@Composable
fun CustomGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradientColors: List<Color> = listOf(
        Color(0xFF6C63FF),
        Color(0xFF5A52E0)
    )
) {
    /**
     * 인터랙션 추적
     */
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    /**
     * 프레스 애니메이션
     */
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button scale"
    )
    
    /**
     * 버튼 컨테이너
     */
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(gradientColors)
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF3A3A4A), Color(0xFF2A2A3A))
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 커스텀 리플을 원하면 직접 구현
                enabled = enabled && !isLoading
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            /**
             * 로딩 인디케이터
             */
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            /**
             * 버튼 텍스트
             */
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFF7A7A8A),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
```

### 아웃라인 버튼

```kotlin
/**
 * 아웃라인 스타일의 커스텀 버튼
 * - 소셜 로그인 등에 사용
 */
@Composable
fun CustomOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    borderColor: Color = Color(0xFF6C63FF),
    textColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "outline button scale"
    )
    
    Row(
        modifier = modifier
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(
                width = 1.dp,
                color = if (enabled) borderColor else Color(0xFF3A3A4A),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        /**
         * 아이콘
         */
        icon?.let {
            it()
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        /**
         * 텍스트
         */
        Text(
            text = text,
            color = if (enabled) textColor else Color(0xFF7A7A8A),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
```

---

## 완성된 로그인 화면

### 메인 화면 구현

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/CustomSignInScreen.kt`

```kotlin
package com.example.yourapp.ui.signin

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
 * 완전 커스텀 로그인 화면
 * - Material3 컴포넌트 미사용
 * - BasicTextField와 Box만으로 구현
 */
@Composable
fun CustomSignInScreen(
    viewModel: SignInViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
        /**
         * 스크롤 가능한 콘텐츠
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /**
             * 로고 & 타이틀
             */
            LogoSection()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            /**
             * 글래스모피즘 카드
             */
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    /**
                     * 이메일 입력
                     */
                    CustomTextField(
                        value = uiState.email,
                        onValueChange = { 
                            viewModel.onEvent(SignInEvent.EmailChanged(it)) 
                        },
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    /**
                     * 비밀번호 입력
                     */
                    CustomPasswordTextField(
                        password = uiState.password,
                        onPasswordChange = { 
                            viewModel.onEvent(SignInEvent.PasswordChanged(it)) 
                        },
                        isError = uiState.passwordError != null,
                        errorMessage = uiState.passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        enabled = !uiState.isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    /**
                     * 비밀번호 찾기
                     */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                viewModel.onEvent(SignInEvent.ForgotPasswordClicked) 
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            "비밀번호를 잊으셨나요?",
                            color = Color(0xFF6C63FF),
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    /**
                     * 로그인 버튼
                     */
                    CustomGradientButton(
                        text = "로그인",
                        onClick = { viewModel.onEvent(SignInEvent.SignInClicked) },
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    /**
                     * 구분선
                     */
                    DividerWithText(text = "또는")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    /**
                     * 소셜 로그인 버튼들
                     */
                    SocialLoginButtons(
                        onGoogleClick = { 
                            viewModel.onEvent(SignInEvent.SocialSignIn("Google")) 
                        },
                        onAppleClick = { 
                            viewModel.onEvent(SignInEvent.SocialSignIn("Apple")) 
                        },
                        enabled = !uiState.isLoading
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            /**
             * 회원가입 링크
             */
            SignUpLink()
        }
    }
}

/**
 * 로고 섹션
 */
@Composable
private fun LogoSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /**
         * 로고 (아이콘으로 대체)
         */
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6C63FF),
                            Color(0xFF5A52E0)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Welcome Back",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "로그인하여 계속하세요",
            color = Color(0xFF9A9AA5),
            fontSize = 16.sp
        )
    }
}

/**
 * 소셜 로그인 버튼들
 */
@Composable
private fun SocialLoginButtons(
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /**
         * Google 로그인
         */
        CustomOutlineButton(
            text = "Google",
            onClick = onGoogleClick,
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(20.dp)
                )
            },
            enabled = enabled
        )
        
        /**
         * Apple 로그인
         */
        CustomOutlineButton(
            text = "Apple",
            onClick = onAppleClick,
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(20.dp)
                )
            },
            enabled = enabled
        )
    }
}

/**
 * 텍스트 포함 구분선
 */
@Composable
private fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFF3A3A4A))
        )
        
        Text(
            text = text,
            color = Color(0xFF7A7A8A),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFF3A3A4A))
        )
    }
}

/**
 * 회원가입 링크
 */
@Composable
private fun SignUpLink() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "계정이 없으신가요?",
            color = Color(0xFF9A9AA5),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .clickable { /* 회원가입으로 이동 */ }
                .padding(4.dp)
        ) {
            Text(
                "회원가입",
                color = Color(0xFF6C63FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
```

---

## 애니메이션 추가

### 글래스모피즘 카드

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/components/GlassmorphicCard.kt`

```kotlin
package com.example.yourapp.ui.signin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 글래스모피즘 효과 카드
 * - 반투명 배경
 * - 블러 효과
 * - 테두리 하이라이트
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}
```

### 페이드인 애니메이션

```kotlin
/**
 * 화면 진입 시 페이드인 애니메이션
 */
@Composable
fun AnimatedSignInScreen() {
    /**
     * 애니메이션 상태
     */
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    /**
     * 알파 애니메이션
     */
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "screen fade in"
    )
    
    /**
     * Y 오프셋 애니메이션
     */
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "screen slide up"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .offset(y = offsetY)
    ) {
        CustomSignInScreen()
    }
}
```

---

## 다크 모드 지원

### 테마 색상 정의

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/theme/CustomColors.kt`

```kotlin
package com.example.yourapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 커스텀 색상 팔레트
 */
data class CustomColorScheme(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val error: Color,
    val onError: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val borderFocused: Color
)

/**
 * 라이트 모드 색상
 */
val LightCustomColors = CustomColorScheme(
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    textPrimary = Color(0xFF1E1E1E),
    textSecondary = Color(0xFF7A7A8A),
    border = Color(0xFFE0E0E0),
    borderFocused = Color(0xFF6C63FF)
)

/**
 * 다크 모드 색상
 */
val DarkCustomColors = CustomColorScheme(
    background = Color(0xFF0F0F1E),
    surface = Color(0xFF1E1E2E),
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.White,
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    textPrimary = Color.White,
    textSecondary = Color(0xFF9A9AA5),
    border = Color(0xFF3A3A4A),
    borderFocused = Color(0xFF6C63FF)
)

/**
 * CompositionLocal로 제공
 */
val LocalCustomColors = compositionLocalOf { DarkCustomColors }

/**
 * 현재 색상 스킴 가져오기
 */
object CustomTheme {
    val colors: CustomColorScheme
        @Composable
        get() = LocalCustomColors.current
}
```

---

## 요약

### 커스텀 vs Material3 비교

| 기능 | Material3 | 커스텀 (BasicTextField + Box) |
|------|-----------|-------------------------------|
| **개발 속도** | ⚡ 빠름 | 🐢 느림 |
| **커스터마이징** | ⚠️ 제한적 | ✅ 무제한 |
| **디자인 자유도** | ⚠️ Material 스타일 | ✅ 완전 자유 |
| **유지보수** | ✅ 쉬움 | ⚠️ 직접 관리 필요 |
| **번들 크기** | ⚠️ 큼 | ✅ 작음 |
| **성능** | ✅ 최적화됨 | ⚠️ 직접 최적화 필요 |

### 언제 커스텀을 사용해야 하나?

✅ **커스텀 추천:**
- 독특한 브랜드 디자인이 필요할 때
- Material Design을 따르지 않을 때
- 완전한 디자인 제어가 필요할 때
- 특별한 애니메이션/효과가 필요할 때

❌ **Material3 추천:**
- 빠른 MVP 개발이 필요할 때
- Material Design을 따를 때
- 개발 리소스가 제한적일 때
- 접근성이 최우선일 때

이제 여러분만의 독특한 로그인 화면을 만들어보세요! 🎨
