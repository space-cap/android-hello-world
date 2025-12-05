# 실전 로그인 화면 만들기 (SignInScreen)

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐ 중급  
> **예상 학습 시간**: 2-3시간  
> **선행 학습**: [92-버튼 가이드](./92-composable-button-production-guide.md), [94-TextField 가이드](./94-material3-textfield-production-guide.md), [99-Modifier 기초](./99-compose-modifier-fundamentals.md)

## 목차
1. [개요](#개요)
2. [화면 구조 설계](#화면-구조-설계)
3. [상태 관리](#상태-관리)
4. [UI 구현](#ui-구현)
5. [유효성 검증](#유효성-검증)
6. [로딩 및 에러 처리](#로딩-및-에러-처리)
7. [최종 완성 코드](#최종-완성-코드)
8. [추가 기능](#추가-기능)

---

## 프로젝트 구조

### 파일 배치

```
app/src/main/java/com/example/yourapp/
├── ui/
│   ├── signin/
│   │   ├── SignInScreen.kt          # 메인 화면 Composable
│   │   ├── SignInViewModel.kt       # ViewModel
│   │   ├── SignInUiState.kt         # UI 상태 클래스
│   │   ├── SignInEvent.kt           # 이벤트 클래스
│   │   └── components/              # 화면 전용 컴포넌트
│   │       ├── EmailTextField.kt
│   │       ├── PasswordTextField.kt
│   │       └── SocialSignInButtons.kt
│   └── components/                   # 공통 컴포넌트
│       ├── LoadingOverlay.kt
│       └── ErrorDialog.kt
└── util/
    └── validation/
        ├── EmailValidator.kt
        ├── PasswordValidator.kt
        └── ValidationResult.kt
```

> 💡 **참고**: `com.example.yourapp` 부분은 실제 앱의 패키지명으로 변경하세요.

---

## 개요

이 가이드에서는 **실제 프로덕션에서 사용 가능한 로그인 화면**을 처음부터 끝까지 만들어봅니다. 지금까지 배운 모든 개념을 종합적으로 활용합니다.

### 구현할 기능

✅ 이메일/비밀번호 입력 필드  
✅ 실시간 유효성 검증  
✅ 로그인 버튼 (로딩 상태 포함)  
✅ 비밀번호 보기/숨기기  
✅ "비밀번호 찾기" 링크  
✅ 소셜 로그인 버튼  
✅ 키보드 동작 처리  
✅ 접근성 고려  

---

## 화면 구조 설계

### 와이어프레임

```
┌─────────────────────────────┐
│                             │
│      [로고/타이틀]          │
│                             │
│   ┌─────────────────────┐   │
│   │ 이메일              │   │
│   └─────────────────────┘   │
│                             │
│   ┌─────────────────────┐   │
│   │ 비밀번호        👁   │   │
│   └─────────────────────┘   │
│                             │
│      [비밀번호 찾기]        │
│                             │
│   ┌─────────────────────┐   │
│   │    로그인 버튼      │   │
│   └─────────────────────┘   │
│                             │
│        or                   │
│                             │
│   [Google] [Apple]          │
│                             │
└─────────────────────────────┘
```

---

## 상태 관리

### UI 상태 정의

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/SignInUiState.kt`

```kotlin
package com.example.yourapp.ui.signin
import androidx.compose.runtime.*

/**
 * 로그인 화면 UI 상태
 * - 모든 입력 값과 에러 상태를 관리
 */
data class SignInUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 로그인 화면 이벤트
 * - 사용자 액션 정의
 * - sealed class로 타입 안전성 보장
 */
sealed class SignInEvent {
    data class EmailChanged(val email: String) : SignInEvent()
    data class PasswordChanged(val password: String) : SignInEvent()
    object TogglePasswordVisibility : SignInEvent()
    object SignInClicked : SignInEvent()
    object ForgotPasswordClicked : SignInEvent()
    data class SocialSignIn(val provider: String) : SignInEvent()
}
```

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/SignInEvent.kt`

---

### ViewModel 구현

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/SignInViewModel.kt`

```kotlin
package com.example.yourapp.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 로그인 화면 ViewModel
 * - 상태 관리 및 비즈니스 로직
 */
class SignInViewModel : ViewModel() {
    
    /**
     * UI 상태
     * - StateFlow로 상태 관리
     */
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()
    
    /**
     * 이벤트 처리
     */
    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> {
                _uiState.update { 
                    it.copy(
                        email = event.email,
                        emailError = validateEmail(event.email)
                    )
                }
            }
            
            is SignInEvent.PasswordChanged -> {
                _uiState.update { 
                    it.copy(
                        password = event.password,
                        passwordError = validatePassword(event.password)
                    )
                }
            }
            
            is SignInEvent.TogglePasswordVisibility -> {
                _uiState.update { 
                    it.copy(isPasswordVisible = !it.isPasswordVisible)
                }
            }
            
            is SignInEvent.SignInClicked -> {
                signIn()
            }
            
            is SignInEvent.ForgotPasswordClicked -> {
                // 비밀번호 찾기 화면으로 이동
            }
            
            is SignInEvent.SocialSignIn -> {
                socialSignIn(event.provider)
            }
        }
    }
    
    /**
     * 이메일 유효성 검증
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "이메일을 입력해주세요"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                "올바른 이메일 형식이 아닙니다"
            else -> null
        }
    }
    
    /**
     * 비밀번호 유효성 검증
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "비밀번호를 입력해주세요"
            password.length < 6 -> "비밀번호는 6자 이상이어야 합니다"
            else -> null
        }
    }
    
    /**
     * 로그인 실행
     */
    private fun signIn() {
        val state = _uiState.value
        
        // 최종 유효성 검증
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }
        
        // 로그인 API 호출
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // API 호출 시뮬레이션
                kotlinx.coroutines.delay(2000)
                
                // 성공 시 메인 화면으로 이동
                // navController.navigate("home")
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "로그인에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 소셜 로그인
     */
    private fun socialSignIn(provider: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 소셜 로그인 API 호출
                kotlinx.coroutines.delay(1500)
                
                // 성공 처리
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "$provider 로그인 실패: ${e.message}"
                    )
                }
            }
        }
    }
}
```

---

## UI 구현

### 메인 화면 Composable

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/SignInScreen.kt`

```kotlin
package com.example.yourapp.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

/**
 * 로그인 화면
 * - 모든 UI 요소를 조합
 */
@Composable
fun SignInScreen(
    viewModel: SignInViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    /**
     * 스크롤 가능한 컨테이너
     * - 키보드가 올라와도 스크롤 가능
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /**
         * 로고/타이틀
         */
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "로그인하여 계속하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        /**
         * 이메일 입력 필드
         */
        EmailTextField(
            email = uiState.email,
            emailError = uiState.emailError,
            onEmailChange = { viewModel.onEvent(SignInEvent.EmailChanged(it)) },
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * 비밀번호 입력 필드
         */
        PasswordTextField(
            password = uiState.password,
            passwordError = uiState.passwordError,
            isPasswordVisible = uiState.isPasswordVisible,
            onPasswordChange = { viewModel.onEvent(SignInEvent.PasswordChanged(it)) },
            onTogglePasswordVisibility = { 
                viewModel.onEvent(SignInEvent.TogglePasswordVisibility) 
            },
            onDone = {
                focusManager.clearFocus()
                viewModel.onEvent(SignInEvent.SignInClicked)
            },
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        /**
         * 비밀번호 찾기
         */
        TextButton(
            onClick = { viewModel.onEvent(SignInEvent.ForgotPasswordClicked) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("비밀번호를 잊으셨나요?")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        /**
         * 에러 메시지
         */
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        /**
         * 로그인 버튼
         */
        Button(
            onClick = { viewModel.onEvent(SignInEvent.SignInClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("로그인", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        /**
         * 구분선
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "또는",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        /**
         * 소셜 로그인 버튼
         */
        SocialSignInButtons(
            onGoogleSignIn = { viewModel.onEvent(SignInEvent.SocialSignIn("Google")) },
            onAppleSignIn = { viewModel.onEvent(SignInEvent.SocialSignIn("Apple")) },
            enabled = !uiState.isLoading
        )
    }
}
```

### 이메일 입력 필드

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/components/EmailTextField.kt`

```kotlin
package com.example.yourapp.ui.signin.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/**
 * 이메일 입력 TextField
 * - 유효성 검증 결과 표시
 * - 자동완성 힌트
 */
@Composable
fun EmailTextField(
    email: String,
    emailError: String?,
    onEmailChange: (String) -> Unit,
    onNext: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("이메일") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null
            )
        },
        isError = emailError != null,
        supportingText = emailError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 비밀번호 입력 필드

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/components/PasswordTextField.kt`

```kotlin
package com.example.yourapp.ui.signin.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*

/**
 * 비밀번호 입력 TextField
 * - 보기/숨기기 토글
 * - 유효성 검증 결과 표시
 */
@Composable
fun PasswordTextField(
    password: String,
    passwordError: String?,
    isPasswordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onDone: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("비밀번호") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    imageVector = if (isPasswordVisible) 
                        Icons.Default.Visibility 
                    else 
                        Icons.Default.VisibilityOff,
                    contentDescription = if (isPasswordVisible) 
                        "비밀번호 숨기기" 
                    else 
                        "비밀번호 보기"
                )
            }
        },
        visualTransformation = if (isPasswordVisible) 
            VisualTransformation.None 
        else 
            PasswordVisualTransformation(),
        isError = passwordError != null,
        supportingText = passwordError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 소셜 로그인 버튼

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signin/components/SocialSignInButtons.kt`

```kotlin
package com.example.yourapp.ui.signin.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 소셜 로그인 버튼들
 * - Google, Apple 로그인
 */
@Composable
fun SocialSignInButtons(
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /**
         * Google 로그인
         */
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            // Google 아이콘 (실제로는 이미지 사용)
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Google")
        }
        
        /**
         * Apple 로그인
         */
        OutlinedButton(
            onClick = onAppleSignIn,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            // Apple 아이콘 (실제로는 이미지 사용)
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apple")
        }
    }
}
```

---

## 유효성 검증

### 실시간 검증

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/util/validation/EmailValidator.kt`

```kotlin
package com.example.yourapp.util.validation

import android.util.Patterns

/**
 * 이메일 유효성 검증 규칙
 * - Singleton object로 구현
 */
object EmailValidator {
    fun validate(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("이메일을 입력해주세요")
            !isValidEmailFormat(email) -> ValidationResult.Error("올바른 이메일 형식이 아닙니다")
            else -> ValidationResult.Success
        }
    }
    
    private fun isValidEmailFormat(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * 비밀번호 유효성 검증 규칙
 * - Singleton object로 구현
 * - 다양한 검증 규칙 적용 가능
 */
// 📁 파일: app/src/main/java/com/example/yourapp/util/validation/PasswordValidator.kt
object PasswordValidator {
    fun validate(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("비밀번호를 입력해주세요")
            password.length < 6 -> ValidationResult.Error("비밀번호는 6자 이상이어야 합니다")
            !hasLetterAndNumber(password) -> 
                ValidationResult.Error("영문자와 숫자를 포함해야 합니다")
            else -> ValidationResult.Success
        }
    }
    
    private fun hasLetterAndNumber(password: String): Boolean {
        return password.any { it.isLetter() } && password.any { it.isDigit() }
    }
}

/**
 * 검증 결과
 * - sealed class로 성공/실패 타입 안전하게 표현
 */
// 📁 파일: app/src/main/java/com/example/yourapp/util/validation/ValidationResult.kt
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

---

## 로딩 및 에러 처리

### 로딩 오버레이

```kotlin
/**
 * 전체 화면 로딩 오버레이
 * - 필요시 사용
 */
@Composable
fun LoadingOverlay(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
```

### 에러 다이얼로그

```kotlin
/**
 * 에러 다이얼로그
 */
@Composable
fun ErrorDialog(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("오류") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("확인")
                }
            }
        )
    }
}
```

---

## 최종 완성 코드

### 전체 통합

```kotlin
/**
 * 완전한 로그인 화면
 * - 모든 기능 통합
 */
@Composable
fun CompleteSignInScreen(
    viewModel: SignInViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    /**
     * 성공 시 네비게이션
     */
    LaunchedEffect(uiState.isSignInSuccessful) {
        if (uiState.isSignInSuccessful) {
            onSignInSuccess()
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            /**
             * 메인 콘텐츠
             */
            SignInScreen(viewModel = viewModel)
            
            /**
             * 회원가입 링크 (하단)
             */
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "계정이 없으신가요?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToSignUp) {
                    Text("회원가입")
                }
            }
        }
    }
    
    /**
     * 에러 다이얼로그
     */
    ErrorDialog(
        errorMessage = uiState.errorMessage,
        onDismiss = { viewModel.clearError() }
    )
}
```

---

## 추가 기능

### Remember Me (로그인 상태 유지)

```kotlin
/**
 * Remember Me 체크박스
 */
@Composable
fun RememberMeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("로그인 상태 유지")
    }
}
```

### 생체 인증

```kotlin
/**
 * 생체 인증 버튼
 */
@Composable
fun BiometricSignInButton(
    onBiometricSignIn: () -> Unit
) {
    IconButton(
        onClick = onBiometricSignIn,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "지문 인증",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
```

---

## 요약

### 학습 포인트

이 로그인 화면 예제에서 배운 내용:

✅ **TextField**: 이메일, 비밀번호 입력 처리  
✅ **Button**: 로그인, 소셜 로그인 버튼 구현  
✅ **Modifier**: 레이아웃, 스타일링, 간격 처리  
✅ **상태 관리**: StateFlow를 사용한 UI 상태 관리  
✅ **유효성 검증**: 실시간 입력 검증  
✅ **키보드 제어**: IME Action 처리  
✅ **로딩 처리**: 비동기 작업 중 로딩 표시  
✅ **에러 처리**: 사용자 친화적 에러 메시지  

### Modifier 활용 예시

```kotlin
/**
 * 이 예제에서 사용한 주요 Modifier 패턴
 */

// 1. 레이아웃
.fillMaxSize()
.fillMaxWidth()
.padding(24.dp)

// 2. 크기
.height(56.dp)
.size(24.dp)

// 3. 배경과 스타일
.background(MaterialTheme.colorScheme.background)

// 4. 정렬
horizontalAlignment = Alignment.CenterHorizontally
.align(Alignment.End)

// 5. 간격
verticalArrangement = Arrangement.spacedBy(16.dp)
Spacer(modifier = Modifier.height(24.dp))

// 6. 스크롤
.verticalScroll(rememberScrollState())
```

이제 이 패턴을 활용해서 다양한 화면을 만들어보세요! 🚀
