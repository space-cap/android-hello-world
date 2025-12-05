# 실전 회원가입 화면 만들기 (SignUpScreen)

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐ 중급  
> **예상 학습 시간**: 2-3시간  
> **선행 학습**: [102-로그인 가이드](./102-signin-screen-complete-guide.md)

## 목차
1. [개요](#개요)
2. [프로젝트 구조](#프로젝트-구조)
3. [상태 관리](#상태-관리)
4. [UI 구현](#ui-구현)
5. [유효성 검증](#유효성-검증)
6. [약관 동의](#약관-동의)
7. [최종 완성 코드](#최종-완성-코드)

---

## 개요

Material3 컴포넌트를 사용하여 **실무급 회원가입 화면**을 구현합니다. 로그인 화면보다 복잡한 입력 검증과 약관 동의 처리를 다룹니다.

### 구현할 기능

✅ 이름, 이메일, 비밀번호 입력  
✅ 비밀번호 확인 검증  
✅ 실시간 유효성 검증  
✅ 이용약관 및 개인정보 처리방침 동의  
✅ 전체 동의 체크박스  
✅ 회원가입 버튼 (로딩 상태)  
✅ 중복 이메일 확인  

---

## 프로젝트 구조

```
app/src/main/java/com/example/yourapp/
├── ui/
│   ├── signup/
│   │   ├── SignUpScreen.kt              # 메인 화면
│   │   ├── SignUpViewModel.kt           # ViewModel
│   │   ├── SignUpUiState.kt             # UI 상태
│   │   ├── SignUpEvent.kt               # 이벤트
│   │   └── components/
│   │       ├── NameTextField.kt         # 이름 입력
│   │       ├── EmailTextField.kt        # 이메일 입력
│   │       ├── PasswordTextField.kt     # 비밀번호 입력
│   │       ├── PasswordConfirmField.kt  # 비밀번호 확인
│   │       └── TermsAgreement.kt        # 약관 동의
│   └── components/
│       └── CheckboxWithText.kt          # 체크박스 컴포넌트
└── util/
    └── validation/
        ├── NameValidator.kt
        ├── EmailValidator.kt
        ├── PasswordValidator.kt
        └── ValidationResult.kt
```

---

## 상태 관리

### UI 상태 정의

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signup/SignUpUiState.kt`

```kotlin
package com.example.yourapp.ui.signup

/**
 * 회원가입 화면 UI 상태
 * - 모든 입력 필드와 검증 상태 관리
 */
data class SignUpUiState(
    // 입력 값
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    
    // 에러 메시지
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmError: String? = null,
    
    // 비밀번호 표시 상태
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    
    // 약관 동의 상태
    val isTermsAgreed: Boolean = false,
    val isPrivacyAgreed: Boolean = false,
    val isMarketingAgreed: Boolean = false,
    val isAllAgreed: Boolean = false,
    
    // UI 상태
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpSuccessful: Boolean = false
)

/**
 * 회원가입 이벤트
 * - 사용자 액션 정의
 */
sealed class SignUpEvent {
    data class NameChanged(val name: String) : SignUpEvent()
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class PasswordConfirmChanged(val passwordConfirm: String) : SignUpEvent()
    
    object TogglePasswordVisibility : SignUpEvent()
    object TogglePasswordConfirmVisibility : SignUpEvent()
    
    data class TermsAgreedChanged(val agreed: Boolean) : SignUpEvent()
    data class PrivacyAgreedChanged(val agreed: Boolean) : SignUpEvent()
    data class MarketingAgreedChanged(val agreed: Boolean) : SignUpEvent()
    object AllAgreedChanged : SignUpEvent()
    
    object SignUpClicked : SignUpEvent()
    data class ShowTermsDetail(val type: TermsType) : SignUpEvent()
}

/**
 * 약관 종류
 */
enum class TermsType {
    TERMS_OF_SERVICE,    // 이용약관
    PRIVACY_POLICY,      // 개인정보 처리방침
    MARKETING            // 마케팅 정보 수신
}
```

### ViewModel 구현

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signup/SignUpViewModel.kt`

```kotlin
package com.example.yourapp.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 회원가입 ViewModel
 * - 복잡한 유효성 검증 로직 포함
 */
class SignUpViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    /**
     * 이벤트 처리
     */
    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = event.name,
                        nameError = validateName(event.name)
                    )
                }
            }
            
            is SignUpEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.email,
                        emailError = validateEmail(event.email)
                    )
                }
            }
            
            is SignUpEvent.PasswordChanged -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        password = event.password,
                        passwordError = validatePassword(event.password),
                        // 비밀번호 확인 필드도 재검증
                        passwordConfirmError = if (currentState.passwordConfirm.isNotEmpty()) {
                            validatePasswordConfirm(event.password, currentState.passwordConfirm)
                        } else null
                    )
                }
            }
            
            is SignUpEvent.PasswordConfirmChanged -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        passwordConfirm = event.passwordConfirm,
                        passwordConfirmError = validatePasswordConfirm(
                            currentState.password,
                            event.passwordConfirm
                        )
                    )
                }
            }
            
            is SignUpEvent.TogglePasswordVisibility -> {
                _uiState.update {
                    it.copy(isPasswordVisible = !it.isPasswordVisible)
                }
            }
            
            is SignUpEvent.TogglePasswordConfirmVisibility -> {
                _uiState.update {
                    it.copy(isPasswordConfirmVisible = !it.isPasswordConfirmVisible)
                }
            }
            
            is SignUpEvent.TermsAgreedChanged -> {
                _uiState.update { currentState ->
                    val newState = currentState.copy(isTermsAgreed = event.agreed)
                    newState.copy(isAllAgreed = checkAllAgreed(newState))
                }
            }
            
            is SignUpEvent.PrivacyAgreedChanged -> {
                _uiState.update { currentState ->
                    val newState = currentState.copy(isPrivacyAgreed = event.agreed)
                    newState.copy(isAllAgreed = checkAllAgreed(newState))
                }
            }
            
            is SignUpEvent.MarketingAgreedChanged -> {
                _uiState.update { currentState ->
                    val newState = currentState.copy(isMarketingAgreed = event.agreed)
                    newState.copy(isAllAgreed = checkAllAgreed(newState))
                }
            }
            
            is SignUpEvent.AllAgreedChanged -> {
                _uiState.update { currentState ->
                    val newValue = !currentState.isAllAgreed
                    currentState.copy(
                        isTermsAgreed = newValue,
                        isPrivacyAgreed = newValue,
                        isMarketingAgreed = newValue,
                        isAllAgreed = newValue
                    )
                }
            }
            
            is SignUpEvent.SignUpClicked -> {
                signUp()
            }
            
            is SignUpEvent.ShowTermsDetail -> {
                // 약관 상세 화면으로 이동
            }
        }
    }
    
    /**
     * 이름 유효성 검증
     */
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "이름을 입력해주세요"
            name.length < 2 -> "이름은 2자 이상이어야 합니다"
            !name.all { it.isLetter() || it == ' ' } -> "이름은 문자만 입력 가능합니다"
            else -> null
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
            password.length < 8 -> "비밀번호는 8자 이상이어야 합니다"
            !password.any { it.isUpperCase() } -> "대문자를 포함해야 합니다"
            !password.any { it.isLowerCase() } -> "소문자를 포함해야 합니다"
            !password.any { it.isDigit() } -> "숫자를 포함해야 합니다"
            !password.any { !it.isLetterOrDigit() } -> "특수문자를 포함해야 합니다"
            else -> null
        }
    }
    
    /**
     * 비밀번호 확인 검증
     */
    private fun validatePasswordConfirm(password: String, passwordConfirm: String): String? {
        return when {
            passwordConfirm.isBlank() -> "비밀번호를 다시 입력해주세요"
            password != passwordConfirm -> "비밀번호가 일치하지 않습니다"
            else -> null
        }
    }
    
    /**
     * 전체 동의 체크
     */
    private fun checkAllAgreed(state: SignUpUiState): Boolean {
        return state.isTermsAgreed && state.isPrivacyAgreed && state.isMarketingAgreed
    }
    
    /**
     * 회원가입 실행
     */
    private fun signUp() {
        val state = _uiState.value
        
        // 최종 유효성 검증
        val nameError = validateName(state.name)
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val passwordConfirmError = validatePasswordConfirm(state.password, state.passwordConfirm)
        
        if (nameError != null || emailError != null || 
            passwordError != null || passwordConfirmError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    passwordConfirmError = passwordConfirmError
                )
            }
            return
        }
        
        // 필수 약관 동의 확인
        if (!state.isTermsAgreed || !state.isPrivacyAgreed) {
            _uiState.update {
                it.copy(errorMessage = "필수 약관에 동의해주세요")
            }
            return
        }
        
        // 회원가입 API 호출
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // API 호출 시뮬레이션
                kotlinx.coroutines.delay(2000)
                
                // 성공 처리
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignUpSuccessful = true
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "회원가입에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
```

---

## UI 구현

### 메인 화면

**📁 파일 경로**: `app/src/main/java/com/example/yourapp/ui/signup/SignUpScreen.kt`

```kotlin
package com.example.yourapp.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * 회원가입 화면
 * - Material3 컴포넌트 사용
 */
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {}
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
    
    Scaffold(
        topBar = {
            /**
             * 상단 앱바
             */
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSignIn) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /**
             * 안내 텍스트
             */
            Text(
                text = "환영합니다!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "새 계정을 만들어보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            /**
             * 이름 입력
             */
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(SignUpEvent.NameChanged(it)) },
                label = { Text("이름") },
                leadingIcon = {
                    Icon(Icons.Default.Person, null)
                },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            /**
             * 이메일 입력
             */
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
                label = { Text("이메일") },
                leadingIcon = {
                    Icon(Icons.Default.Email, null)
                },
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            /**
             * 비밀번호 입력
             */
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                label = { Text("비밀번호") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.onEvent(SignUpEvent.TogglePasswordVisibility) }
                    ) {
                        Icon(
                            if (uiState.isPasswordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = "비밀번호 표시 전환"
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            /**
             * 비밀번호 확인 입력
             */
            OutlinedTextField(
                value = uiState.passwordConfirm,
                onValueChange = { 
                    viewModel.onEvent(SignUpEvent.PasswordConfirmChanged(it)) 
                },
                label = { Text("비밀번호 확인") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = { 
                            viewModel.onEvent(SignUpEvent.TogglePasswordConfirmVisibility) 
                        }
                    ) {
                        Icon(
                            if (uiState.isPasswordConfirmVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = "비밀번호 확인 표시 전환"
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordConfirmVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = uiState.passwordConfirmError != null,
                supportingText = uiState.passwordConfirmError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            /**
             * 약관 동의
             */
            TermsAgreementSection(
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            /**
             * 에러 메시지
             */
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            /**
             * 회원가입 버튼
             */
            Button(
                onClick = { viewModel.onEvent(SignUpEvent.SignUpClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("회원가입", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            /**
             * 로그인 링크
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("이미 계정이 있으신가요?")
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToSignIn) {
                    Text("로그인")
                }
            }
        }
    }
}
```

### 약관 동의 섹션

```kotlin
/**
 * 약관 동의 섹션
 */
@Composable
fun TermsAgreementSection(
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Checkbox(
                    checked = isAllAgreed,
                    onCheckedChange = { onAllAgreedChanged() },
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "전체 동의",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalDivider()
            
            /**
             * 이용약관 (필수)
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { 
                        onTermsAgreedChanged(!isTermsAgreed) 
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTermsAgreed,
                    onCheckedChange = onTermsAgreedChanged,
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("(필수) 이용약관 동의")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* 상세보기 */ }) {
                    Icon(Icons.Default.ChevronRight, "상세보기")
                }
            }
            
            /**
             * 개인정보 처리방침 (필수)
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { 
                        onPrivacyAgreedChanged(!isPrivacyAgreed) 
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isPrivacyAgreed,
                    onCheckedChange = onPrivacyAgreedChanged,
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("(필수) 개인정보 처리방침 동의")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* 상세보기 */ }) {
                    Icon(Icons.Default.ChevronRight, "상세보기")
                }
            }
            
            /**
             * 마케팅 정보 수신 (선택)
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { 
                        onMarketingAgreedChanged(!isMarketingAgreed) 
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isMarketingAgreed,
                    onCheckedChange = onMarketingAgreedChanged,
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("(선택) 마케팅 정보 수신 동의")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* 상세보기 */ }) {
                    Icon(Icons.Default.ChevronRight, "상세보기")
                }
            }
        }
    }
}
```

---

## 유효성 검증

### 비밀번호 강도 표시

```kotlin
/**
 * 비밀번호 강도 표시기
 */
@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = calculatePasswordStrength(password)
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "비밀번호 강도",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        /**
         * 강도 바
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < strength) {
                                when (strength) {
                                    1 -> Color.Red
                                    2 -> Color(0xFFFF9800)
                                    3 -> Color(0xFFFFEB3B)
                                    4 -> Color(0xFF4CAF50)
                                    else -> Color.Gray
                                }
                            } else {
                                Color.Gray.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            when (strength) {
                0 -> "비밀번호를 입력하세요"
                1 -> "약함"
                2 -> "보통"
                3 -> "강함"
                4 -> "매우 강함"
                else -> ""
            },
            style = MaterialTheme.typography.bodySmall,
            color = when (strength) {
                1 -> Color.Red
                2 -> Color(0xFFFF9800)
                3 -> Color(0xFFFFEB3B)
                4 -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 비밀번호 강도 계산
 */
private fun calculatePasswordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    
    var strength = 0
    
    // 길이 체크
    if (password.length >= 8) strength++
    
    // 대소문자 포함
    if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) strength++
    
    // 숫자 포함
    if (password.any { it.isDigit() }) strength++
    
    // 특수문자 포함
    if (password.any { !it.isLetterOrDigit() }) strength++
    
    return strength
}
```

---

## 최종 완성 코드

### 통합 화면

```kotlin
/**
 * 완전한 회원가입 화면
 * - 모든 기능 통합
 */
@Composable
fun CompleteSignUpScreen(
    viewModel: SignUpViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    /**
     * 성공 다이얼로그
     */
    if (uiState.isSignUpSuccessful) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("회원가입 완료") },
            text = { Text("회원가입이 완료되었습니다!\n로그인 페이지로 이동합니다.") },
            confirmButton = {
                TextButton(onClick = onSignUpSuccess) {
                    Text("확인")
                }
            }
        )
    }
    
    /**
     * 에러 다이얼로그
     */
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("오류") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("확인")
                }
            }
        )
    }
    
    SignUpScreen(
        viewModel = viewModel,
        onSignUpSuccess = onSignUpSuccess,
        onNavigateToSignIn = onNavigateToSignIn
    )
}
```

---

## 요약

### 회원가입 vs 로그인 차이점

| 항목 | 로그인 | 회원가입 |
|------|--------|----------|
| **입력 필드** | 2개 | 4개+ |
| **검증 복잡도** | 낮음 | 높음 |
| **약관 동의** | 없음 | 필수 |
| **비밀번호 강도** | 체크 안함 | 표시 |
| **중복 확인** | 없음 | 이메일 중복 확인 |

### 핵심 포인트

✅ **복잡한 유효성 검증**: 비밀번호 확인, 강도 체크  
✅ **약관 동의 처리**: 필수/선택 구분  
✅ **전체 동의 체크박스**: 편의성 제공  
✅ **실시간 피드백**: 입력과 동시에 검증  

이제 완전한 회원가입 시스템을 구현할 수 있습니다! 🎉
