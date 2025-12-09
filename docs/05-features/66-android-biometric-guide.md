# Android Biometric Authentication 가이드

## 목차
1. [생체 인증이란?](#생체-인증이란)
2. [BiometricPrompt API](#biometricprompt-api)
3. [기본 구현](#기본-구현)
4. [암호화 통합](#암호화-통합)
5. [Fallback 처리](#fallback-처리)
6. [보안 고려사항](#보안-고려사항)
7. [실전 예제](#실전-예제)
8. [Jetpack Compose 통합](#jetpack-compose-통합)
9. [문제 해결](#문제-해결)

---

## 생체 인증이란?

**생체 인증(Biometric Authentication)**은 지문, 얼굴, 홍채 등 사용자의 생체 정보를 사용하여 신원을 확인하는 방법입니다.

### 지원되는 생체 인증
- 👆 **지문 인식** (Fingerprint)
- 😊 **얼굴 인식** (Face Recognition)
- 👁️ **홍채 인식** (Iris Recognition)

### 사용 사례
- 🔐 **로그인**: 앱 로그인, 재인증
- 💳 **결제**: 결제 승인
- 🔒 **데이터 보호**: 민감한 데이터 접근
- 📱 **기기 잠금 해제**

---

## BiometricPrompt API

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
}
```

### 권한 설정

**AndroidManifest.xml**:
```xml
<manifest>
    <!-- 지문 인식 (선택적) -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC"/>
    
    <!-- 하드웨어 기능 선언 (선택적) -->
    <uses-feature
        android:name="android.hardware.fingerprint"
        android:required="false"/>
</manifest>
```

---

## 기본 구현

### 생체 인증 가능 여부 확인

```kotlin
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*

/**
 * 생체 인증 헬퍼 클래스
 */
class BiometricHelper(private val context: Context) {
    
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * 생체 인증 가능 여부 확인
     */
    fun canAuthenticate(): BiometricStatus {
        val result = biometricManager.canAuthenticate(
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        )
        
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricStatus.Available
            }
            
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                BiometricStatus.NoHardware
            }
            
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                BiometricStatus.HardwareUnavailable
            }
            
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricStatus.NoneEnrolled
            }
            
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                BiometricStatus.SecurityUpdateRequired
            }
            
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                BiometricStatus.Unsupported
            }
            
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                BiometricStatus.Unknown
            }
            
            else -> BiometricStatus.Unknown
        }
    }
}

/**
 * 생체 인증 상태
 */
sealed class BiometricStatus {
    object Available : BiometricStatus()
    object NoHardware : BiometricStatus()
    object HardwareUnavailable : BiometricStatus()
    object NoneEnrolled : BiometricStatus()
    object SecurityUpdateRequired : BiometricStatus()
    object Unsupported : BiometricStatus()
    object Unknown : BiometricStatus()
}
```

### BiometricPrompt 생성

```kotlin
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * BiometricPrompt 생성 및 실행
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {
    
    /**
     * BiometricPrompt 생성
     */
    fun createBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ): BiometricPrompt {
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            
            /**
             * 인증 성공
             */
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                Log.d("Biometric", "인증 성공")
                onSuccess()
            }
            
            /**
             * 인증 에러
             */
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        Log.d("Biometric", "사용자가 취소 버튼 클릭")
                    }
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        Log.d("Biometric", "사용자가 인증 취소")
                    }
                    BiometricPrompt.ERROR_LOCKOUT -> {
                        Log.e("Biometric", "너무 많은 시도로 잠김")
                    }
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Log.e("Biometric", "영구적으로 잠김")
                    }
                    else -> {
                        Log.e("Biometric", "인증 에러: $errString")
                    }
                }
                
                onError(errString.toString())
            }
            
            /**
             * 인증 실패 (재시도 가능)
             */
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w("Biometric", "인증 실패 (재시도 가능)")
                onFailed()
            }
        }
        
        return BiometricPrompt(activity, executor, callback)
    }
    
    /**
     * PromptInfo 생성
     */
    fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증")  // 제목
            .setSubtitle("지문 또는 얼굴로 인증하세요")  // 부제목
            .setDescription("앱에 접근하려면 생체 인증이 필요합니다")  // 설명
            .setNegativeButtonText("취소")  // 취소 버튼
            .setConfirmationRequired(true)  // 명시적 확인 필요
            .build()
    }
    
    /**
     * 생체 인증 실행
     */
    fun authenticate() {
        val biometricPrompt = createBiometricPrompt(
            onSuccess = {
                Toast.makeText(activity, "인증 성공!", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(activity, "에러: $error", Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                Toast.makeText(activity, "인증 실패", Toast.LENGTH_SHORT).show()
            }
        )
        
        val promptInfo = createPromptInfo()
        
        biometricPrompt.authenticate(promptInfo)
    }
}
```

---

## 암호화 통합

### CryptoObject 사용

```kotlin
import androidx.biometric.BiometricPrompt.CryptoObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * 암호화된 생체 인증
 */
class BiometricCrypto(private val activity: FragmentActivity) {
    
    companion object {
        private const val KEY_NAME = "biometric_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }
    
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    /**
     * 암호화 키 생성
     */
    fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)  // 생체 인증 필요
            .setInvalidatedByBiometricEnrollment(true)  // 새 지문 등록 시 키 무효화
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * 암호화 키 가져오기
     */
    fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_NAME, null) as SecretKey
    }
    
    /**
     * Cipher 생성
     */
    fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }
    
    /**
     * 암호화 모드로 Cipher 초기화
     */
    fun initCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }
    
    /**
     * CryptoObject로 인증
     */
    fun authenticateWithCrypto(
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit
    ) {
        // 키가 없으면 생성
        if (!keyStore.containsAlias(KEY_NAME)) {
            generateKey()
        }
        
        val cipher = initCipherForEncryption()
        val cryptoObject = CryptoObject(cipher)
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                
                // 인증된 Cipher 사용
                result.cryptoObject?.cipher?.let { authenticatedCipher ->
                    onSuccess(authenticatedCipher)
                }
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
        }
        
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("암호화된 데이터 접근")
            .setSubtitle("생체 인증이 필요합니다")
            .setNegativeButtonText("취소")
            .build()
        
        // CryptoObject와 함께 인증
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }
    
    /**
     * 데이터 암호화
     */
    fun encryptData(data: String, cipher: Cipher): ByteArray {
        return cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    }
}
```

---

## Fallback 처리

### PIN/패턴/비밀번호 대체 수단

```kotlin
/**
 * 생체 인증 + 기기 인증 (PIN/패턴/비밀번호)
 */
fun authenticateWithDeviceCredential() {
    val executor = ContextCompat.getMainExecutor(this)
    
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            Toast.makeText(this@MainActivity, "인증 성공", Toast.LENGTH_SHORT).show()
        }
        
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Toast.makeText(this@MainActivity, "에러: $errString", Toast.LENGTH_SHORT).show()
        }
    }
    
    val biometricPrompt = BiometricPrompt(this, executor, callback)
    
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("인증 필요")
        .setSubtitle("생체 인증 또는 기기 인증을 사용하세요")
        // setNegativeButtonText 없음 (기기 인증 사용 시)
        .setAllowedAuthenticators(
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL  // 생체 인증 또는 기기 인증
        )
        .build()
    
    biometricPrompt.authenticate(promptInfo)
}
```

### 생체 인증 등록 유도

```kotlin
/**
 * 생체 인증 등록 화면으로 이동
 */
fun promptBiometricEnrollment() {
    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(
            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        )
    }
    
    startActivityForResult(enrollIntent, BIOMETRIC_ENROLLMENT_REQUEST_CODE)
}

companion object {
    const val BIOMETRIC_ENROLLMENT_REQUEST_CODE = 100
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == BIOMETRIC_ENROLLMENT_REQUEST_CODE) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "생체 인증 등록 완료", Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

## 보안 고려사항

### 1. 강력한 생체 인증 사용

```kotlin
/**
 * 보안 수준별 인증
 */
class BiometricSecurityLevels {
    
    /**
     * Class 3 (강력): 지문, 얼굴 (3D), 홍채
     * 결제 등 높은 보안 필요
     */
    fun strongBiometric(): Int {
        return BIOMETRIC_STRONG
    }
    
    /**
     * Class 2 (약함): 얼굴 (2D)
     * 일반 로그인
     */
    fun weakBiometric(): Int {
        return BIOMETRIC_WEAK
    }
    
    /**
     * 기기 인증: PIN, 패턴, 비밀번호
     */
    fun deviceCredential(): Int {
        return DEVICE_CREDENTIAL
    }
}
```

### 2. 타임아웃 설정

```kotlin
/**
 * 인증 유효 시간 설정
 */
fun generateKeyWithTimeout() {
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        "key_with_timeout",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setUserAuthenticationRequired(true)
        .setUserAuthenticationValidityDurationSeconds(30)  // 30초 동안 유효
        .build()
    
    // 키 생성...
}
```

### 3. 루팅 감지

```kotlin
/**
 * 루팅 감지 (간단한 방법)
 */
fun isDeviceRooted(): Boolean {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )
    
    return paths.any { File(it).exists() }
}
```

---

## 실전 예제

### 로그인 화면

```kotlin
/**
 * 생체 인증 로그인
 */
class BiometricLoginActivity : AppCompatActivity() {
    
    private lateinit var biometricAuthenticator: BiometricAuthenticator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        biometricAuthenticator = BiometricAuthenticator(this)
        
        // 생체 인증 가능 여부 확인
        val biometricHelper = BiometricHelper(this)
        when (biometricHelper.canAuthenticate()) {
            BiometricStatus.Available -> {
                showBiometricLogin()
            }
            
            BiometricStatus.NoneEnrolled -> {
                showEnrollmentPrompt()
            }
            
            BiometricStatus.NoHardware,
            BiometricStatus.HardwareUnavailable -> {
                showPasswordLogin()
            }
            
            else -> {
                showPasswordLogin()
            }
        }
    }
    
    /**
     * 생체 인증 로그인 표시
     */
    private fun showBiometricLogin() {
        val biometricPrompt = biometricAuthenticator.createBiometricPrompt(
            onSuccess = {
                // 로그인 성공
                navigateToMainScreen()
            },
            onError = { error ->
                // 에러 처리
                showPasswordLogin()
            },
            onFailed = {
                // 재시도
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("로그인")
            .setSubtitle("지문 또는 얼굴로 로그인하세요")
            .setNegativeButtonText("비밀번호 사용")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * 비밀번호 로그인 표시
     */
    private fun showPasswordLogin() {
        // 비밀번호 입력 UI 표시
    }
    
    /**
     * 생체 인증 등록 유도
     */
    private fun showEnrollmentPrompt() {
        AlertDialog.Builder(this)
            .setTitle("생체 인증 등록")
            .setMessage("더 편리한 로그인을 위해 생체 인증을 등록하시겠습니까?")
            .setPositiveButton("등록") { _, _ ->
                promptBiometricEnrollment()
            }
            .setNegativeButton("나중에") { _, _ ->
                showPasswordLogin()
            }
            .show()
    }
    
    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
```

---

## Jetpack Compose 통합

```kotlin
/**
 * Compose에서 생체 인증 사용
 */
@Composable
fun BiometricLoginScreen() {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return
    
    var authStatus by remember { mutableStateOf<String?>(null) }
    
    val biometricPrompt = remember {
        val executor = ContextCompat.getMainExecutor(context)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                authStatus = "인증 성공!"
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authStatus = "에러: $errString"
            }
            
            override fun onAuthenticationFailed() {
                authStatus = "인증 실패"
            }
        }
        
        BiometricPrompt(activity, executor, callback)
    }
    
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("로그인")
            .setSubtitle("생체 인증을 사용하세요")
            .setNegativeButtonText("취소")
            .build()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "지문",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "생체 인증 로그인",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        authStatus?.let { status ->
            Text(
                text = status,
                color = if (status.contains("성공")) Color.Green else Color.Red
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(onClick = {
            biometricPrompt.authenticate(promptInfo)
        }) {
            Text("지문으로 로그인")
        }
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * 생체 인증 문제 해결
 */
class BiometricTroubleshooter {
    
    /**
     * 1. BIOMETRIC_ERROR_NO_HARDWARE
     */
    fun handleNoHardware() {
        // 기기가 생체 인증을 지원하지 않음
        // 대체 인증 방법 제공 (비밀번호, PIN 등)
    }
    
    /**
     * 2. BIOMETRIC_ERROR_NONE_ENROLLED
     */
    fun handleNoneEnrolled() {
        // 등록된 생체 정보 없음
        // 생체 인증 등록 유도
        promptBiometricEnrollment()
    }
    
    /**
     * 3. BIOMETRIC_ERROR_LOCKOUT
     */
    fun handleLockout() {
        // 너무 많은 시도로 일시적 잠김
        // 30초 후 재시도 또는 기기 인증 사용
    }
    
    /**
     * 4. BIOMETRIC_ERROR_LOCKOUT_PERMANENT
     */
    fun handlePermanentLockout() {
        // 영구적으로 잠김
        // 기기 인증만 사용 가능
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. 생체 인증 가능 여부 확인
val biometricHelper = BiometricHelper(context)
when (biometricHelper.canAuthenticate()) {
    BiometricStatus.Available -> authenticate()
    else -> showAlternative()
}

// 2. 강력한 생체 인증 사용 (결제 등)
.setAllowedAuthenticators(BIOMETRIC_STRONG)

// 3. 대체 수단 제공
.setNegativeButtonText("비밀번호 사용")

// 4. 암호화와 함께 사용 (민감한 데이터)
biometricPrompt.authenticate(promptInfo, cryptoObject)

// 5. 명확한 UI 메시지
.setTitle("결제 승인")
.setSubtitle("지문으로 결제를 승인하세요")
```

### DON'Ts ❌

```kotlin
// 1. 생체 인증만 강제
// ❌ 대체 수단 없음

// 2. 약한 생체 인증으로 결제
.setAllowedAuthenticators(BIOMETRIC_WEAK)  // ❌

// 3. 에러 처리 안 함
// ❌ onAuthenticationError 무시

// 4. 루팅된 기기에서 민감한 작업
if (isDeviceRooted()) {
    // ❌ 그대로 진행
}

// 5. 생체 정보 직접 저장
// ❌ 절대 하지 말 것!
```

---

## 참고 자료

- [BiometricPrompt 공식 문서](https://developer.android.com/training/sign-in/biometric-auth)
- [Android Keystore](https://developer.android.com/training/articles/keystore)
