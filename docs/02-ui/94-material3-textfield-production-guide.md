# Material 3 TextField 실무 가이드

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐ 중급  
> **예상 학습 시간**: 3-4시간

## 목차
1. [개요](#개요)
2. [기본 TextField 이해하기](#기본-textfield-이해하기)
3. [실무 TextField 설계 원칙](#실무-textfield-설계-원칙)
4. [재사용 가능한 TextField 구현](#재사용-가능한-textfield-구현)
5. [입력 검증과 에러 처리](#입력-검증과-에러-처리)
6. [고급 패턴](#고급-패턴)
7. [접근성과 모범 사례](#접근성과-모범-사례)
8. [실전 예제](#실전-예제)

---

## 개요

TextField는 사용자로부터 텍스트 입력을 받는 가장 기본적이면서도 중요한 UI 컴포넌트입니다. Material Design 3는 다양한 상황에 맞는 TextField를 제공하며, 이를 올바르게 사용하는 것이 좋은 사용자 경험의 핵심입니다.

### 이 가이드에서 배울 내용
- ✅ Material 3 TextField 컴포넌트 완벽 이해
- ✅ 실무에서 바로 사용 가능한 재사용 가능한 TextField 구현
- ✅ 입력 검증, 에러 처리, 포맷팅
- ✅ 다양한 입력 타입 (이메일, 비밀번호, 전화번호 등)
- ✅ 접근성을 고려한 TextField 개발

---

## 기본 TextField 이해하기

### Material 3 TextField 종류

```kotlin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*

@Composable
fun TextFieldTypesExample() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 1. TextField (기본)
         * - Material Design 3의 기본 Filled TextField
         * - 배경색이 있는 스타일
         */
        TextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("기본 TextField") },
            placeholder = { Text("여기에 입력하세요") }
        )
        
        /**
         * 2. OutlinedTextField
         * - 테두리만 있는 스타일
         * - 더 깔끔하고 현대적인 느낌
         * - 실무에서 더 많이 사용됨
         */
        OutlinedTextField(
            value = text2,
            onValueChange = { text2 = it },
            label = { Text("Outlined TextField") },
            placeholder = { Text("여기에 입력하세요") }
        )
    }
}
```

### TextField의 기본 구조

```kotlin
@Composable
fun TextFieldStructure() {
    var text by remember { mutableStateOf("") }
    
    TextField(
        value = text,                           // 필수: 현재 텍스트 값
        onValueChange = { text = it },          // 필수: 값 변경 콜백
        modifier = Modifier.fillMaxWidth(),     // 선택: 크기, 스타일링
        enabled = true,                         // 선택: 활성화 여부
        readOnly = false,                       // 선택: 읽기 전용
        textStyle = LocalTextStyle.current,     // 선택: 텍스트 스타일
        label = { Text("라벨") },               // 선택: 라벨
        placeholder = { Text("플레이스홀더") }, // 선택: 플레이스홀더
        leadingIcon = {                         // 선택: 앞쪽 아이콘
            Icon(Icons.Default.Email, contentDescription = null)
        },
        trailingIcon = {                        // 선택: 뒤쪽 아이콘
            Icon(Icons.Default.Clear, contentDescription = null)
        },
        supportingText = {                      // 선택: 하단 도움말 텍스트
            Text("도움말 텍스트")
        },
        isError = false,                        // 선택: 에러 상태
        visualTransformation = VisualTransformation.None, // 선택: 비밀번호 등
        keyboardOptions = KeyboardOptions.Default,        // 선택: 키보드 옵션
        keyboardActions = KeyboardActions.Default,        // 선택: 키보드 액션
        singleLine = true,                      // 선택: 단일 라인
        maxLines = 1,                           // 선택: 최대 라인 수
        minLines = 1,                           // 선택: 최소 라인 수
        shape = TextFieldDefaults.shape,        // 선택: 모양
        colors = TextFieldDefaults.colors()     // 선택: 색상
    )
}
```

---

## 실무 TextField 설계 원칙

### 1. 입력 유형별 설정

```kotlin
/**
 * 입력 유형별 키보드 타입과 옵션을 정의
 */
enum class InputType {
    TEXT,           // 일반 텍스트
    EMAIL,          // 이메일
    PASSWORD,       // 비밀번호
    NUMBER,         // 숫자
    PHONE,          // 전화번호
    MULTILINE       // 여러 줄
}

/**
 * 입력 유형에 맞는 KeyboardOptions 반환
 */
fun getKeyboardOptions(inputType: InputType): KeyboardOptions {
    return when (inputType) {
        InputType.EMAIL -> KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next // 다음 필드로 이동
        )
        InputType.PASSWORD -> KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
        InputType.NUMBER -> KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
        InputType.PHONE -> KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )
        InputType.MULTILINE -> KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default
        )
        InputType.TEXT -> KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )
    }
}
```

### 2. 검증 규칙 정의

```kotlin
/**
 * 입력 검증 결과
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * 일반적인 검증 함수들
 */
object InputValidators {
    
    /**
     * 이메일 형식 검증
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isEmpty() -> ValidationResult.Invalid("이메일을 입력해주세요")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult.Invalid("올바른 이메일 형식이 아닙니다")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * 비밀번호 검증 (최소 8자, 영문+숫자 포함)
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Invalid("비밀번호를 입력해주세요")
            password.length < 8 -> ValidationResult.Invalid("비밀번호는 최소 8자 이상이어야 합니다")
            !password.any { it.isDigit() } -> ValidationResult.Invalid("숫자를 포함해야 합니다")
            !password.any { it.isLetter() } -> ValidationResult.Invalid("영문을 포함해야 합니다")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * 전화번호 검증 (한국 형식)
     */
    fun validatePhone(phone: String): ValidationResult {
        val phonePattern = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$".toRegex()
        return when {
            phone.isEmpty() -> ValidationResult.Invalid("전화번호를 입력해주세요")
            !phonePattern.matches(phone.replace("-", "")) -> 
                ValidationResult.Invalid("올바른 전화번호 형식이 아닙니다")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * 필수 입력 검증
     */
    fun validateRequired(value: String, fieldName: String = "필드"): ValidationResult {
        return when {
            value.trim().isEmpty() -> ValidationResult.Invalid("${fieldName}을(를) 입력해주세요")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * 길이 제한 검증
     */
    fun validateLength(
        value: String, 
        minLength: Int? = null, 
        maxLength: Int? = null
    ): ValidationResult {
        return when {
            minLength != null && value.length < minLength -> 
                ValidationResult.Invalid("최소 ${minLength}자 이상 입력해주세요")
            maxLength != null && value.length > maxLength -> 
                ValidationResult.Invalid("최대 ${maxLength}자까지 입력 가능합니다")
            else -> ValidationResult.Valid
        }
    }
}
```

---

## 재사용 가능한 TextField 구현

### 기본 AppTextField 컴포넌트

```kotlin
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * 실무용 재사용 가능한 TextField 컴포넌트
 * 
 * @param value 현재 입력 값
 * @param onValueChange 값 변경 콜백
 * @param label 라벨 텍스트
 * @param modifier Modifier
 * @param inputType 입력 타입 (TEXT, EMAIL, PASSWORD 등)
 * @param placeholder 플레이스홀더 텍스트
 * @param enabled 활성화 여부
 * @param readOnly 읽기 전용 여부
 * @param isError 에러 상태
 * @param errorMessage 에러 메시지
 * @param supportingText 도움말 텍스트
 * @param leadingIcon 앞쪽 아이콘
 * @param maxLength 최대 입력 길이
 * @param singleLine 단일 라인 여부
 * @param maxLines 최대 라인 수
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    inputType: InputType = InputType.TEXT,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    val focusManager = LocalFocusManager.current
    
    /**
     * 비밀번호 표시/숨김 상태
     */
    var passwordVisible by remember { mutableStateOf(false) }
    
    /**
     * 입력 타입에 따른 VisualTransformation 결정
     */
    val visualTransformation = when (inputType) {
        InputType.PASSWORD -> {
            if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation()
        }
        else -> VisualTransformation.None
    }
    
    /**
     * 키보드 액션 설정
     */
    val keyboardActions = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) },
        onDone = { focusManager.clearFocus() }
    )
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // 최대 길이 제한이 있으면 적용
            if (maxLength == null || newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = {
            when {
                // 비밀번호 타입: 표시/숨김 아이콘
                inputType == InputType.PASSWORD -> {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) 
                                "비밀번호 숨기기" 
                            else 
                                "비밀번호 표시"
                        )
                    }
                }
                // 에러 상태: 에러 아이콘
                isError -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "에러",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                // 입력값이 있고 활성화 상태: 클리어 버튼
                value.isNotEmpty() && enabled && !readOnly -> {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "지우기"
                        )
                    }
                }
            }
        },
        supportingText = {
            when {
                // 에러 메시지 우선 표시
                isError && errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // 도움말 텍스트 표시
                supportingText != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(supportingText)
                        // 최대 길이 표시
                        if (maxLength != null) {
                            Text(
                                text = "${value.length}/$maxLength",
                                color = if (value.length == maxLength) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // 최대 길이만 표시
                maxLength != null -> {
                    Text(
                        text = "${value.length}/$maxLength",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        },
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = getKeyboardOptions(inputType),
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors()
    )
}
```

### 사용 예제

```kotlin
@Composable
fun AppTextFieldUsageExample() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 이메일 입력
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            inputType = InputType.EMAIL,
            placeholder = "example@email.com",
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            }
        )
        
        // 비밀번호 입력 (표시/숨김 가능)
        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            inputType = InputType.PASSWORD,
            supportingText = "최소 8자, 영문+숫자 포함",
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )
        
        // 이름 입력 (최대 길이 제한)
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "이름",
            maxLength = 20,
            supportingText = "실명을 입력해주세요"
        )
        
        // 여러 줄 입력 (자기소개)
        AppTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "자기소개",
            inputType = InputType.MULTILINE,
            singleLine = false,
            maxLines = 5,
            maxLength = 200,
            supportingText = "자신을 소개해주세요"
        )
    }
}
```

---

## 입력 검증과 에러 처리

### 실시간 검증 TextField

```kotlin
/**
 * 검증 기능이 내장된 TextField
 * 
 * @param validator 검증 함수
 * @param validateOnChange 입력 중 실시간 검증 여부
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    inputType: InputType = InputType.TEXT,
    validator: ((String) -> ValidationResult)? = null,
    validateOnChange: Boolean = false, // true면 입력 중에도 검증
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    maxLength: Int? = null
) {
    /**
     * 포커스를 잃었는지 추적
     */
    var hasLostFocus by remember { mutableStateOf(false) }
    
    /**
     * 검증 수행
     */
    val validationResult = remember(value, hasLostFocus) {
        if (validator != null && (validateOnChange || hasLostFocus)) {
            validator(value)
        } else {
            ValidationResult.Valid
        }
    }
    
    /**
     * 에러 상태와 메시지
     */
    val isError = validationResult is ValidationResult.Invalid
    val errorMessage = (validationResult as? ValidationResult.Invalid)?.message
    
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier.onFocusChanged { focusState ->
            // 포커스를 잃으면 검증 시작
            if (!focusState.isFocused && !hasLostFocus) {
                hasLostFocus = true
            }
        },
        inputType = inputType,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        isError = isError,
        errorMessage = errorMessage,
        maxLength = maxLength
    )
}
```

### 폼 검증 예제

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegistrationFormExample() {
    /**
     * 폼 상태
     */
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    /**
     * 제출 시도 여부
     */
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    
    /**
     * 각 필드별 검증 결과
     */
    val nameValidation = InputValidators.validateRequired(name, "이름")
    val emailValidation = InputValidators.validateEmail(email)
    val passwordValidation = InputValidators.validatePassword(password)
    val passwordConfirmValidation = if (password != passwordConfirm) {
        ValidationResult.Invalid("비밀번호가 일치하지 않습니다")
    } else {
        ValidationResult.Valid
    }
    val phoneValidation = InputValidators.validatePhone(phone)
    
    /**
     * 전체 폼 유효성
     */
    val isFormValid = listOf(
        nameValidation,
        emailValidation,
        passwordValidation,
        passwordConfirmValidation,
        phoneValidation
    ).all { it is ValidationResult.Valid }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "회원가입",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // 이름
        ValidatedTextField(
            value = name,
            onValueChange = { name = it },
            label = "이름",
            validator = { InputValidators.validateRequired(it, "이름") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            maxLength = 20
        )
        
        // 이메일
        ValidatedTextField(
            value = email,
            onValueChange = { email = it },
            label = "이메일",
            inputType = InputType.EMAIL,
            validator = InputValidators::validateEmail,
            placeholder = "example@email.com",
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            }
        )
        
        // 비밀번호
        ValidatedTextField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            inputType = InputType.PASSWORD,
            validator = InputValidators::validatePassword,
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )
        
        // 비밀번호 확인
        ValidatedTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = "비밀번호 확인",
            inputType = InputType.PASSWORD,
            validator = { 
                if (password != it) {
                    ValidationResult.Invalid("비밀번호가 일치하지 않습니다")
                } else {
                    ValidationResult.Valid
                }
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )
        
        // 전화번호
        ValidatedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "전화번호",
            inputType = InputType.PHONE,
            validator = InputValidators::validatePhone,
            placeholder = "010-1234-5678",
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 제출 버튼
        Button(
            onClick = {
                hasAttemptedSubmit = true
                if (isFormValid) {
                    // 폼 제출 로직
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("가입하기")
        }
        
        // 검증 상태 정보 (개발용)
        if (hasAttemptedSubmit && !isFormValid) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "다음 항목을 확인해주세요:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    // 에러 메시지 목록 표시
                    listOf(
                        nameValidation,
                        emailValidation,
                        passwordValidation,
                        passwordConfirmValidation,
                        phoneValidation
                    ).forEach { result ->
                        if (result is ValidationResult.Invalid) {
                            Text(
                                text = "• ${result.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

## 고급 패턴

### 1. 검색 TextField

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 검색 기능이 있는 TextField
 * - 실시간 검색 제안
 * - 디바운싱 (Debouncing)
 * - 검색 기록
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "검색...",
    debounceMs: Long = 300L // 디바운스 시간 (밀리초)
) {
    /**
     * 검색어 상태
     */
    var query by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }
    
    /**
     * 포커스 요청자
     */
    val focusRequester = remember { FocusRequester() }
    
    /**
     * 디바운싱 적용한 검색
     * - 입력이 멈춘 후 일정 시간 후에 검색 실행
     */
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            delay(debounceMs)
            onSearch(query)
        }
    }
    
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = modifier.focusRequester(focusRequester),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { 
                    query = ""
                    onSearch("")
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "지우기"
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge, // 둥근 모양
        colors = OutlinedTextFieldDefaults.colors()
    )
}

/**
 * 검색 바 with 제안 목록
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithSuggestions(
    suggestions: List<String>,
    onSearch: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    /**
     * 필터링된 제안 목록
     */
    val filteredSuggestions = remember(query, suggestions) {
        if (query.isEmpty()) {
            emptyList()
        } else {
            suggestions.filter { it.contains(query, ignoreCase = true) }
        }
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                expanded = it.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("검색...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "검색")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { 
                        query = ""
                        expanded = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "지우기")
                    }
                }
            },
            singleLine = true
        )
        
        // 제안 목록 드롭다운
        if (expanded && filteredSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(filteredSuggestions) { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion) },
                            leadingContent = {
                                Icon(Icons.Default.History, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                query = suggestion
                                expanded = false
                                onSuggestionClick(suggestion)
                                onSearch(suggestion)
                            }
                        )
                        if (suggestion != filteredSuggestions.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
```

### 2. 포맷팅 TextField (전화번호, 신용카드 등)

```kotlin
/**
 * 입력값을 포맷팅하는 TextField
 * 예: 전화번호 010-1234-5678
 */
@Composable
fun FormattedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    formatter: (String) -> String, // 포맷팅 함수
    rawValueFilter: (String) -> String = { it } // 원본 값 필터 (숫자만 등)
) {
    /**
     * 포맷팅된 값과 원본 값 분리 관리
     */
    var displayValue by remember { mutableStateOf(formatter(value)) }
    
    OutlinedTextField(
        value = displayValue,
        onValueChange = { newValue ->
            // 원본 값 추출 (포맷 문자 제거)
            val rawValue = rawValueFilter(newValue)
            // 부모에게 원본 값 전달
            onValueChange(rawValue)
            // 화면에는 포맷팅된 값 표시
            displayValue = formatter(rawValue)
        },
        label = { Text(label) },
        modifier = modifier
    )
}

/**
 * 전화번호 포맷터 (010-1234-5678)
 */
fun formatPhoneNumber(phone: String): String {
    val digits = phone.filter { it.isDigit() }.take(11)
    return when (digits.length) {
        in 0..3 -> digits
        in 4..7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        in 8..11 -> {
            val middle = if (digits.length == 10) 3 else 4
            "${digits.substring(0, 3)}-${digits.substring(3, 3 + middle)}-${digits.substring(3 + middle)}"
        }
        else -> digits
    }
}

/**
 * 신용카드 번호 포맷터 (1234 5678 9012 3456)
 */
fun formatCreditCard(cardNumber: String): String {
    val digits = cardNumber.filter { it.isDigit() }.take(16)
    return digits.chunked(4).joinToString(" ")
}

/**
 * 사용 예제
 */
@Composable
fun FormattedTextFieldExample() {
    var phone by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 전화번호 입력
        FormattedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "전화번호",
            formatter = ::formatPhoneNumber,
            rawValueFilter = { it.filter { char -> char.isDigit() } }
        )
        
        // 신용카드 번호 입력
        FormattedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = "카드 번호",
            formatter = ::formatCreditCard,
            rawValueFilter = { it.filter { char -> char.isDigit() } }
        )
        
        // 실제 저장되는 값 표시 (개발용)
        Text("전화번호 원본: $phone")
        Text("카드번호 원본: $cardNumber")
    }
}
```

---

## 접근성과 모범 사례

### 1. 접근성 고려사항

```kotlin
import androidx.compose.ui.semantics.*

/**
 * 접근성을 고려한 TextField
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    contentDescription: String? = null
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier.semantics {
            // 필수 입력 필드 표시
            if (isRequired) {
                stateDescription = "필수 입력"
            }
            
            // 추가 설명 제공
            contentDescription?.let {
                this.contentDescription = it
            }
        },
        supportingText = if (isRequired) "* 필수" else null
    )
}
```

### 2. 키보드 IME 액션 처리

```kotlin
/**
 * 폼의 다음/이전 필드로 이동 처리
 */
@Composable
fun FormWithKeyboardNavigation() {
    val focusManager = LocalFocusManager.current
    
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 첫 번째 필드: 다음 필드로
        OutlinedTextField(
            value = field1,
            onValueChange = { field1 = it },
            label = { Text("필드 1") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true
        )
        
        // 두 번째 필드: 다음 필드로
        OutlinedTextField(
            value = field2,
            onValueChange = { field2 = it },
            label = { Text("필드 2") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true
        )
        
        // 마지막 필드: 완료
        OutlinedTextField(
            value = field3,
            onValueChange = { field3 = it },
            label = { Text("필드 3") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    focusManager.clearFocus() // 키보드 숨김
                    // 폼 제출 로직
                }
            ),
            singleLine = true
        )
    }
}
```

---

## 실전 예제

### 로그인 화면

```kotlin
@Composable
fun LoginScreenWithTextField() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "로그인",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 이메일 입력
        ValidatedTextField(
            value = email,
            onValueChange = { 
                email = it
                loginError = null // 에러 초기화
            },
            label = "이메일",
            inputType = InputType.EMAIL,
            validator = InputValidators::validateEmail,
            placeholder = "example@email.com",
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            }
        )
        
        // 비밀번호 입력
        AppTextField(
            value = password,
            onValueChange = { 
                password = it
                loginError = null // 에러 초기화
            },
            label = "비밀번호",
            inputType = InputType.PASSWORD,
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )
        
        // 로그인 에러 표시
        if (loginError != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = loginError!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 로그인 버튼
        Button(
            onClick = {
                isLoading = true
                // 로그인 로직
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("로그인")
            }
        }
        
        // 비밀번호 찾기
        TextButton(
            onClick = { /* 비밀번호 찾기 */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("비밀번호를 잊으셨나요?")
        }
    }
}
```

---

## 요약

Material 3 TextField를 실무에서 효과적으로 사용하기 위한 핵심 포인트:

1. **적절한 TextField 선택**: `TextField` vs `OutlinedTextField`
2. **입력 타입 설정**: `KeyboardOptions`로 적절한 키보드 표시
3. **검증 로직 분리**: 재사용 가능한 검증 함수 작성
4. **사용자 피드백**: 에러 메시지, 도움말 텍스트 적절히 사용
5. **접근성 고려**: 라벨, contentDescription, 키보드 네비게이션
6. **UX 개선**: 디바운싱, 포맷팅, 자동완성 등

다음 문서에서는 Box/Row/Column을 사용한 완전 커스텀 TextField 구현을 다룹니다.
