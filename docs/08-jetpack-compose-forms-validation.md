# Jetpack Compose 폼 입력과 유효성 검사 가이드

## 📚 목차

1. [TextField 기본](#textfield-기본)
2. [TextField 심화](#textfield-심화)
3. [입력 유효성 검사](#입력-유효성-검사)
4. [에러 메시지 표시](#에러-메시지-표시)
5. [포커스 관리](#포커스-관리)
6. [키보드 제어](#키보드-제어)
7. [폼 제출 처리](#폼-제출-처리)
8. [실전 예제: 회원가입 폼](#실전-예제-회원가입-폼)

---

## TextField 기본

### 기본 TextField

```kotlin
@Composable
fun BasicTextFieldExample() {
    var text by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("이름") },
            placeholder = { Text("이름을 입력하세요") }
        )
        
        Text(
            text = "입력한 값: $text",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
```

### OutlinedTextField (추천)

```kotlin
@Composable
fun OutlinedTextFieldExample() {
    var text by remember { mutableStateOf("") }
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("이메일") },
        placeholder = { Text("example@email.com") },
        modifier = Modifier.fillMaxWidth()
    )
}
```

> [!TIP]
> **OutlinedTextField**는 Material Design 3에서 권장하는 스타일입니다. 더 깔끔하고 현대적인 디자인을 제공합니다.

---

## TextField 심화

### 다양한 입력 타입

```kotlin
@Composable
fun AdvancedTextFieldExample() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 이메일 입력
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // 전화번호 입력
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("전화번호") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // 숫자 입력
        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("나이") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### 비밀번호 표시/숨김 토글

```kotlin
@Composable
fun PasswordTextField() {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("비밀번호") },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = if (passwordVisible) {
                        "비밀번호 숨기기"
                    } else {
                        "비밀번호 보기"
                    }
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 아이콘이 있는 TextField

```kotlin
@Composable
fun TextFieldWithIcons() {
    var search by remember { mutableStateOf("") }
    
    OutlinedTextField(
        value = search,
        onValueChange = { search = it },
        label = { Text("검색") },
        placeholder = { Text("검색어를 입력하세요") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "검색"
            )
        },
        trailingIcon = {
            if (search.isNotEmpty()) {
                IconButton(onClick = { search = "" }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "지우기"
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 여러 줄 입력 (TextArea)

```kotlin
@Composable
fun MultilineTextField() {
    var text by remember { mutableStateOf("") }
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("메모") },
        placeholder = { Text("메모를 입력하세요") },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        maxLines = 5,
        minLines = 3
    )
}
```

---

## 입력 유효성 검사

### 유효성 검사 함수

```kotlin
object ValidationRules {
    // 이메일 유효성 검사
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
    
    // 비밀번호 유효성 검사 (최소 8자, 영문+숫자 포함)
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
    
    // 전화번호 유효성 검사 (한국 형식)
    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$".toRegex()
        return phone.matches(phoneRegex)
    }
    
    // 이름 유효성 검사 (2-20자, 한글/영문만)
    fun isValidName(name: String): Boolean {
        if (name.length !in 2..20) return false
        val nameRegex = "^[가-힣a-zA-Z\\s]+$".toRegex()
        return name.matches(nameRegex)
    }
    
    // 나이 유효성 검사
    fun isValidAge(age: String): Boolean {
        val ageInt = age.toIntOrNull() ?: return false
        return ageInt in 1..150
    }
}
```

### 유효성 검사 에러 메시지

```kotlin
object ValidationMessages {
    fun getEmailError(email: String): String? {
        return when {
            email.isEmpty() -> "이메일을 입력하세요"
            !ValidationRules.isValidEmail(email) -> "올바른 이메일 형식이 아닙니다"
            else -> null
        }
    }
    
    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> "비밀번호를 입력하세요"
            password.length < 8 -> "비밀번호는 최소 8자 이상이어야 합니다"
            !password.any { it.isLetter() } -> "비밀번호에 영문자가 포함되어야 합니다"
            !password.any { it.isDigit() } -> "비밀번호에 숫자가 포함되어야 합니다"
            else -> null
        }
    }
    
    fun getNameError(name: String): String? {
        return when {
            name.isEmpty() -> "이름을 입력하세요"
            name.length < 2 -> "이름은 최소 2자 이상이어야 합니다"
            name.length > 20 -> "이름은 20자를 초과할 수 없습니다"
            !ValidationRules.isValidName(name) -> "이름은 한글 또는 영문만 입력 가능합니다"
            else -> null
        }
    }
    
    fun getPhoneError(phone: String): String? {
        return when {
            phone.isEmpty() -> "전화번호를 입력하세요"
            !ValidationRules.isValidPhone(phone) -> "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)"
            else -> null
        }
    }
}
```

---

## 에러 메시지 표시

### 기본 에러 표시

```kotlin
@Composable
fun TextFieldWithError() {
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                // 실시간 유효성 검사
                errorMessage = ValidationMessages.getEmailError(it) ?: ""
                isError = errorMessage.isNotEmpty()
            },
            label = { Text("이메일") },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (isError) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "에러",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### 포커스 해제 시 유효성 검사

```kotlin
@Composable
fun TextFieldWithBlurValidation() {
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hasBeenFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = email,
        onValueChange = { 
            email = it
            // 한 번이라도 포커스를 받았다면 실시간 검사
            if (hasBeenFocused) {
                errorMessage = ValidationMessages.getEmailError(it) ?: ""
                isError = errorMessage.isNotEmpty()
            }
        },
        label = { Text("이메일") },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(errorMessage)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (!focusState.isFocused && email.isNotEmpty()) {
                    hasBeenFocused = true
                    errorMessage = ValidationMessages.getEmailError(email) ?: ""
                    isError = errorMessage.isNotEmpty()
                }
            }
    )
}
```

### 성공 상태 표시

```kotlin
@Composable
fun TextFieldWithSuccessState() {
    var email by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = email,
        onValueChange = { 
            email = it
            isValid = ValidationRules.isValidEmail(it)
        },
        label = { Text("이메일") },
        trailingIcon = {
            when {
                email.isEmpty() -> null
                isValid -> Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "유효함",
                    tint = Color(0xFF4CAF50) // Green
                )
                else -> Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "에러",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isValid && email.isNotEmpty()) {
                Color(0xFF4CAF50)
            } else {
                MaterialTheme.colorScheme.primary
            }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
```

---

## 포커스 관리

### 자동 포커스

```kotlin
@Composable
fun AutoFocusTextField() {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("자동 포커스") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )
}
```

### 다음 필드로 이동

```kotlin
@Composable
fun FocusNavigationExample() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("전화번호") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### FocusRequester를 사용한 정밀 제어

```kotlin
@Composable
fun AdvancedFocusControl() {
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = field1,
            onValueChange = { 
                field1 = it
                if (it.length == 3) {
                    focusRequester2.requestFocus()
                }
            },
            label = { Text("필드 1 (3자 입력 시 자동 이동)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester1)
        )
        
        OutlinedTextField(
            value = field2,
            onValueChange = { 
                field2 = it
                if (it.length == 3) {
                    focusRequester3.requestFocus()
                }
            },
            label = { Text("필드 2 (3자 입력 시 자동 이동)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester2)
        )
        
        OutlinedTextField(
            value = field3,
            onValueChange = { field3 = it },
            label = { Text("필드 3") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester3)
        )
    }
}
```

---

## 키보드 제어

### 키보드 타입

```kotlin
enum class KeyboardType {
    Text,           // 일반 텍스트
    Ascii,          // ASCII 문자
    Number,         // 숫자
    Phone,          // 전화번호
    Uri,            // URL
    Email,          // 이메일
    Password,       // 비밀번호
    NumberPassword, // 숫자 비밀번호
    Decimal         // 소수점
}
```

### IME 액션

```kotlin
@Composable
fun ImeActionExample() {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("검색") },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                // 검색 실행
                println("검색: $text")
                focusManager.clearFocus()
            }
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 키보드 숨기기

```kotlin
@Composable
fun HideKeyboardExample() {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("입력") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("키보드 숨기기")
        }
    }
}
```

### 외부 클릭 시 키보드 숨기기

```kotlin
@Composable
fun DismissKeyboardOnOutsideClick() {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("입력") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "화면의 빈 곳을 클릭하면 키보드가 숨겨집니다",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

---

## 폼 제출 처리

### 폼 상태 관리

```kotlin
data class FormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneError: String? = null
) {
    fun isValid(): Boolean {
        return nameError == null &&
               emailError == null &&
               passwordError == null &&
               phoneError == null &&
               name.isNotEmpty() &&
               email.isNotEmpty() &&
               password.isNotEmpty() &&
               phone.isNotEmpty()
    }
}
```

### 폼 제출 예제

```kotlin
@Composable
fun FormSubmissionExample() {
    var formState by remember { mutableStateOf(FormState()) }
    var isSubmitting by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    fun validateAndSubmit() {
        // 모든 필드 유효성 검사
        val nameError = ValidationMessages.getNameError(formState.name)
        val emailError = ValidationMessages.getEmailError(formState.email)
        val passwordError = ValidationMessages.getPasswordError(formState.password)
        val phoneError = ValidationMessages.getPhoneError(formState.phone)
        
        formState = formState.copy(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            phoneError = phoneError
        )
        
        // 유효성 검사 통과 시 제출
        if (formState.isValid()) {
            isSubmitting = true
            focusManager.clearFocus()
            
            scope.launch {
                // 서버에 데이터 전송 시뮬레이션
                delay(2000)
                println("폼 제출 완료: $formState")
                isSubmitting = false
                
                // 성공 후 폼 초기화
                formState = FormState()
            }
        }
    }
    
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
        
        // 이름 입력
        OutlinedTextField(
            value = formState.name,
            onValueChange = { 
                formState = formState.copy(
                    name = it,
                    nameError = if (it.isNotEmpty()) {
                        ValidationMessages.getNameError(it)
                    } else null
                )
            },
            label = { Text("이름") },
            isError = formState.nameError != null,
            supportingText = {
                formState.nameError?.let { Text(it) }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 이메일 입력
        OutlinedTextField(
            value = formState.email,
            onValueChange = { 
                formState = formState.copy(
                    email = it,
                    emailError = if (it.isNotEmpty()) {
                        ValidationMessages.getEmailError(it)
                    } else null
                )
            },
            label = { Text("이메일") },
            isError = formState.emailError != null,
            supportingText = {
                formState.emailError?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 비밀번호 입력
        OutlinedTextField(
            value = formState.password,
            onValueChange = { 
                formState = formState.copy(
                    password = it,
                    passwordError = if (it.isNotEmpty()) {
                        ValidationMessages.getPasswordError(it)
                    } else null
                )
            },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            isError = formState.passwordError != null,
            supportingText = {
                formState.passwordError?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 전화번호 입력
        OutlinedTextField(
            value = formState.phone,
            onValueChange = { 
                formState = formState.copy(
                    phone = it,
                    phoneError = if (it.isNotEmpty()) {
                        ValidationMessages.getPhoneError(it)
                    } else null
                )
            },
            label = { Text("전화번호") },
            isError = formState.phoneError != null,
            supportingText = {
                formState.phoneError?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 제출 버튼
        Button(
            onClick = { validateAndSubmit() },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isSubmitting) "제출 중..." else "가입하기")
        }
    }
}
```

---

## 실전 예제: 회원가입 폼

### 완전한 회원가입 폼

```kotlin
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    fun validateForm(): Boolean {
        nameError = ValidationMessages.getNameError(name)
        emailError = ValidationMessages.getEmailError(email)
        passwordError = ValidationMessages.getPasswordError(password)
        phoneError = ValidationMessages.getPhoneError(phone)
        
        confirmPasswordError = when {
            confirmPassword.isEmpty() -> "비밀번호 확인을 입력하세요"
            confirmPassword != password -> "비밀번호가 일치하지 않습니다"
            else -> null
        }
        
        return nameError == null &&
               emailError == null &&
               passwordError == null &&
               confirmPasswordError == null &&
               phoneError == null &&
               agreeToTerms
    }
    
    fun submitForm() {
        if (validateForm()) {
            isSubmitting = true
            focusManager.clearFocus()
            
            scope.launch {
                // API 호출 시뮬레이션
                delay(2000)
                
                // 성공
                isSubmitting = false
                onSignUpSuccess()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더
        Text(
            text = "회원가입",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "새로운 계정을 만들어보세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 이름
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                if (nameError != null) {
                    nameError = ValidationMessages.getNameError(it)
                }
            },
            label = { Text("이름") },
            placeholder = { Text("홍길동") },
            isError = nameError != null,
            supportingText = {
                nameError?.let { 
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 이메일
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (emailError != null) {
                    emailError = ValidationMessages.getEmailError(it)
                }
            },
            label = { Text("이메일") },
            placeholder = { Text("example@email.com") },
            isError = emailError != null,
            supportingText = {
                emailError?.let { 
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 전화번호
        OutlinedTextField(
            value = phone,
            onValueChange = { 
                phone = it
                if (phoneError != null) {
                    phoneError = ValidationMessages.getPhoneError(it)
                }
            },
            label = { Text("전화번호") },
            placeholder = { Text("010-1234-5678") },
            isError = phoneError != null,
            supportingText = {
                phoneError?.let { 
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 비밀번호
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                if (passwordError != null) {
                    passwordError = ValidationMessages.getPasswordError(it)
                }
                // 비밀번호 확인도 다시 검사
                if (confirmPassword.isNotEmpty()) {
                    confirmPasswordError = if (confirmPassword != it) {
                        "비밀번호가 일치하지 않습니다"
                    } else null
                }
            },
            label = { Text("비밀번호") },
            placeholder = { Text("8자 이상, 영문+숫자 포함") },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { 
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "비밀번호 숨기기"
                        } else {
                            "비밀번호 보기"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 비밀번호 확인
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                if (confirmPasswordError != null) {
                    confirmPasswordError = if (it != password) {
                        "비밀번호가 일치하지 않습니다"
                    } else null
                }
            },
            label = { Text("비밀번호 확인") },
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = confirmPasswordError != null,
            supportingText = {
                confirmPasswordError?.let { 
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                } ?: if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                    Text(
                        text = "비밀번호가 일치합니다",
                        color = Color(0xFF4CAF50)
                    )
                } else null
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (confirmPasswordVisible) {
                            "비밀번호 숨기기"
                        } else {
                            "비밀번호 보기"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    focusManager.clearFocus()
                    if (agreeToTerms) submitForm()
                }
            ),
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 약관 동의
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreeToTerms,
                onCheckedChange = { agreeToTerms = it },
                enabled = !isSubmitting
            )
            Text(
                text = "이용약관 및 개인정보처리방침에 동의합니다",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(enabled = !isSubmitting) {
                    agreeToTerms = !agreeToTerms
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 가입 버튼
        Button(
            onClick = { submitForm() },
            enabled = !isSubmitting && agreeToTerms,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("가입 중...")
            } else {
                Text(
                    text = "가입하기",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        // 로그인 링크
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "이미 계정이 있으신가요?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !isSubmitting
            ) {
                Text("로그인")
            }
        }
    }
}
```

### 로그인 폼 예제

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    fun login() {
        emailError = ValidationMessages.getEmailError(email)
        passwordError = if (password.isEmpty()) {
            "비밀번호를 입력하세요"
        } else null
        
        if (emailError == null && passwordError == null) {
            isLoading = true
            focusManager.clearFocus()
            
            scope.launch {
                delay(1500)
                isLoading = false
                onLoginSuccess()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // 로고/타이틀
        Text(
            text = "로그인",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "계정에 로그인하세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 이메일
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (emailError != null) {
                    emailError = ValidationMessages.getEmailError(it)
                }
            },
            label = { Text("이메일") },
            placeholder = { Text("example@email.com") },
            isError = emailError != null,
            supportingText = {
                emailError?.let { Text(it) }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 비밀번호
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                if (passwordError != null) {
                    passwordError = if (it.isEmpty()) {
                        "비밀번호를 입력하세요"
                    } else null
                }
            },
            label = { Text("비밀번호") },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { Text(it) }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "비밀번호 숨기기"
                        } else {
                            "비밀번호 보기"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { login() }
            ),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 로그인 유지 & 비밀번호 찾기
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled = !isLoading
                )
                Text(
                    text = "로그인 유지",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            TextButton(
                onClick = onForgotPassword,
                enabled = !isLoading
            ) {
                Text("비밀번호 찾기")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 로그인 버튼
        Button(
            onClick = { login() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("로그인 중...")
            } else {
                Text(
                    text = "로그인",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 회원가입 링크
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "계정이 없으신가요?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onNavigateToSignUp,
                enabled = !isLoading
            ) {
                Text("회원가입")
            }
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 실시간 vs 포커스 해제 시 검증

```kotlin
// ❌ 나쁜 예: 항상 실시간 검증
OutlinedTextField(
    value = email,
    onValueChange = { 
        email = it
        emailError = ValidationMessages.getEmailError(it) // 타이핑할 때마다 에러
    }
)

// ✅ 좋은 예: 첫 포커스 해제 후 실시간 검증
var hasBeenFocused by remember { mutableStateOf(false) }

OutlinedTextField(
    value = email,
    onValueChange = { 
        email = it
        if (hasBeenFocused) {
            emailError = ValidationMessages.getEmailError(it)
        }
    },
    modifier = Modifier.onFocusChanged { 
        if (!it.isFocused) hasBeenFocused = true
    }
)
```

### 2. 에러 메시지는 구체적으로

```kotlin
// ❌ 나쁜 예
"잘못된 입력입니다"

// ✅ 좋은 예
"비밀번호는 최소 8자 이상이어야 합니다"
"이메일 형식이 올바르지 않습니다 (예: user@example.com)"
```

### 3. 접근성 고려

```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("이메일") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Filled.Email,
            contentDescription = "이메일 아이콘" // 스크린 리더를 위한 설명
        )
    }
)
```

### 4. 로딩 상태 처리

```kotlin
// 제출 중에는 모든 입력 비활성화
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    enabled = !isSubmitting, // 로딩 중 비활성화
    modifier = Modifier.fillMaxWidth()
)
```

### 5. 키보드 타입 최적화

```kotlin
// 각 필드에 맞는 키보드 타입 사용
OutlinedTextField(
    keyboardOptions = KeyboardOptions(
        keyboardType = when (fieldType) {
            FieldType.EMAIL -> KeyboardType.Email
            FieldType.PHONE -> KeyboardType.Phone
            FieldType.NUMBER -> KeyboardType.Number
            FieldType.PASSWORD -> KeyboardType.Password
            else -> KeyboardType.Text
        }
    )
)
```

---

## 🎯 다음 단계

이제 폼 입력과 유효성 검사를 마스터했습니다! 다음 단계로 넘어가세요:

1. **애니메이션 가이드** - 부드러운 UX 구현
2. **Side Effects 가이드** - 비동기 작업 처리
3. **네트워킹 가이드** - 실제 API 연동

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
