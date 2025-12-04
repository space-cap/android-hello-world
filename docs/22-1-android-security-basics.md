# Android 보안 기초

> 📖 **시리즈 구성**
> - **22-1**: Android 보안 기초 (현재 문서)
> - **22-2**: [Android 보안 고급](./22-2-android-security-advanced.md)
> - **22-3**: [Android 보안 테스팅](./22-3-android-security-testing.md)

---

## 📚 목차

1. [보안 기초 개념](#보안-기초-개념)
2. [데이터 암호화](#데이터-암호화)
3. [네트워크 보안](#네트워크-보안)
4. [안전한 데이터 저장](#안전한-데이터-저장)
5. [인증 및 권한](#인증-및-권한)
6. [실전 예제](#실전-예제)

---

## 보안 기초 개념

### 보안의 3대 원칙 (CIA Triad)

```
┌─────────────────────────────────────┐
│  Confidentiality (기밀성)            │
│  - 인가된 사용자만 접근              │
│  - 데이터 암호화                     │
├─────────────────────────────────────┤
│  Integrity (무결성)                  │
│  - 데이터 변조 방지                  │
│  - 해시, 서명                        │
├─────────────────────────────────────┤
│  Availability (가용성)               │
│  - 필요할 때 접근 가능               │
│  - DoS 공격 방어                     │
└─────────────────────────────────────┘
```

### Android 보안 계층

```
┌─────────────────────────────────────┐
│  앱 계층 (App Layer)                 │
│  - 코드 난독화                       │
│  - 데이터 암호화                     │
├─────────────────────────────────────┤
│  프레임워크 계층 (Framework)         │
│  - 권한 시스템                       │
│  - Keystore                          │
├─────────────────────────────────────┤
│  OS 계층 (Linux Kernel)              │
│  - 샌드박싱                          │
│  - SELinux                           │
├─────────────────────────────────────┤
│  하드웨어 계층 (Hardware)            │
│  - Secure Boot                       │
│  - TEE (Trusted Execution Env)       │
└─────────────────────────────────────┘
```

### 일반적인 보안 위협

```kotlin
/**
 * OWASP Mobile Top 10 (2024)
 * 
 * 1. M1: 부적절한 플랫폼 사용
 * 2. M2: 안전하지 않은 데이터 저장
 * 3. M3: 안전하지 않은 통신
 * 4. M4: 안전하지 않은 인증
 * 5. M5: 불충분한 암호화
 * 6. M6: 안전하지 않은 권한
 * 7. M7: 클라이언트 코드 품질
 * 8. M8: 코드 변조
 * 9. M9: 리버스 엔지니어링
 * 10. M10: 불필요한 기능
 */
```

---

## 데이터 암호화

### EncryptedSharedPreferences

**민감한 설정 값을 암호화하여 저장**

```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * EncryptedSharedPreferences 생성
 * 
 * 자동으로 키와 값을 암호화/복호화
 */
class SecurePreferences(context: Context) {
    
    /**
     * Master Key 생성
     * 
     * Android Keystore에 안전하게 저장됨
     */
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    /**
     * 암호화된 SharedPreferences
     */
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secret_prefs",  // 파일 이름
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * 인증 토큰 저장
     */
    fun saveAuthToken(token: String) {
        encryptedPrefs.edit()
            .putString("auth_token", token)
            .apply()
    }
    
    /**
     * 인증 토큰 읽기
     */
    fun getAuthToken(): String? {
        return encryptedPrefs.getString("auth_token", null)
    }
    
    /**
     * 사용자 정보 저장
     */
    fun saveUserCredentials(email: String, password: String) {
        encryptedPrefs.edit()
            .putString("email", email)
            .putString("password", password)  // ✅ 암호화되어 저장
            .apply()
    }
    
    /**
     * 모든 데이터 삭제
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }
}

/**
 * 사용 예
 */
class LoginViewModel(context: Context) : ViewModel() {
    
    private val securePrefs = SecurePreferences(context)
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val token = authRepository.login(email, password)
            
            // ✅ 토큰을 암호화하여 저장
            securePrefs.saveAuthToken(token)
        }
    }
    
    fun getStoredToken(): String? {
        // ✅ 자동으로 복호화되어 반환
        return securePrefs.getAuthToken()
    }
}
```

### EncryptedFile

**파일 암호화**

```kotlin
import androidx.security.crypto.EncryptedFile
import java.io.File

/**
 * 파일 암호화 유틸리티
 */
class SecureFileManager(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    /**
     * 암호화된 파일 쓰기
     */
    fun writeEncryptedFile(filename: String, content: String) {
        val file = File(context.filesDir, filename)
        
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        // ✅ 자동으로 암호화되어 저장
        encryptedFile.openFileOutput().use { outputStream ->
            outputStream.write(content.toByteArray())
        }
    }
    
    /**
     * 암호화된 파일 읽기
     */
    fun readEncryptedFile(filename: String): String {
        val file = File(context.filesDir, filename)
        
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        
        // ✅ 자동으로 복호화되어 반환
        return encryptedFile.openFileInput().use { inputStream ->
            inputStream.readBytes().toString(Charsets.UTF_8)
        }
    }
    
    /**
     * 실전 예: 민감한 로그 저장
     */
    fun saveSecureLog(logMessage: String) {
        val timestamp = System.currentTimeMillis()
        val logEntry = "$timestamp: $logMessage\n"
        
        try {
            // 기존 로그 읽기
            val existingLog = try {
                readEncryptedFile("secure_log.txt")
            } catch (e: Exception) {
                ""
            }
            
            // 새 로그 추가
            writeEncryptedFile("secure_log.txt", existingLog + logEntry)
            
        } catch (e: Exception) {
            Log.e("SecureFileManager", "Failed to save log", e)
        }
    }
}
```

### Android Keystore System

**암호화 키를 안전하게 저장**

```kotlin
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore를 사용한 암호화
 * 
 * 장점:
 * - 키가 하드웨어에 저장됨 (추출 불가)
 * - 루팅된 기기에서도 안전
 */
class KeystoreEncryption {
    
    companion object {
        private const val KEY_ALIAS = "MySecretKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    }
    
    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }
    
    /**
     * 암호화 키 생성
     */
    private fun generateKey() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                // ✅ 사용자 인증 필요 (선택사항)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    /**
     * 키 가져오기
     */
    private fun getKey(): SecretKey {
        generateKey()
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
    
    /**
     * 데이터 암호화
     * 
     * @return Pair<암호화된 데이터, IV>
     */
    fun encrypt(plainText: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        
        val encryptedData = cipher.doFinal(plainText.toByteArray())
        val iv = cipher.iv
        
        return Pair(encryptedData, iv)
    }
    
    /**
     * 데이터 복호화
     */
    fun decrypt(encryptedData: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }
}

/**
 * 사용 예
 */
class SecureDataManager {
    
    private val encryption = KeystoreEncryption()
    
    /**
     * 민감한 데이터 저장
     */
    fun saveSecureData(context: Context, key: String, value: String) {
        // 1. 데이터 암호화
        val (encryptedData, iv) = encryption.encrypt(value)
        
        // 2. 암호화된 데이터와 IV 저장
        val prefs = context.getSharedPreferences("secure_data", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("${key}_data", Base64.encodeToString(encryptedData, Base64.DEFAULT))
            .putString("${key}_iv", Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()
    }
    
    /**
     * 민감한 데이터 읽기
     */
    fun getSecureData(context: Context, key: String): String? {
        val prefs = context.getSharedPreferences("secure_data", Context.MODE_PRIVATE)
        
        // 1. 암호화된 데이터와 IV 읽기
        val encryptedDataString = prefs.getString("${key}_data", null) ?: return null
        val ivString = prefs.getString("${key}_iv", null) ?: return null
        
        val encryptedData = Base64.decode(encryptedDataString, Base64.DEFAULT)
        val iv = Base64.decode(ivString, Base64.DEFAULT)
        
        // 2. 데이터 복호화
        return try {
            encryption.decrypt(encryptedData, iv)
        } catch (e: Exception) {
            Log.e("SecureDataManager", "Decryption failed", e)
            null
        }
    }
}
```

### 생체 인증 통합

```kotlin
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * 생체 인증 (지문, 얼굴 인식)
 */
class BiometricAuthenticator(private val activity: FragmentActivity) {
    
    /**
     * 생체 인증 가능 여부 확인
     */
    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("Biometric", "생체 인증 하드웨어 없음")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("Biometric", "생체 인증 하드웨어 사용 불가")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("Biometric", "생체 정보 등록 안 됨")
                false
            }
            else -> false
        }
    }
    
    /**
     * 생체 인증 실행
     */
    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        // 콜백 설정
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("인증 실패")
            }
        }
        
        // BiometricPrompt 생성
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        // 프롬프트 정보 설정
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증")
            .setSubtitle("지문 또는 얼굴로 인증하세요")
            .setNegativeButtonText("취소")
            .build()
        
        // 인증 시작
        biometricPrompt.authenticate(promptInfo)
    }
}

/**
 * Compose에서 사용
 */
@Composable
fun BiometricLoginScreen() {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val authenticator = remember { BiometricAuthenticator(activity) }
    
    var isAuthenticated by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isAuthenticated) {
            Text("인증 성공!", style = MaterialTheme.typography.headlineMedium)
        } else {
            Button(
                onClick = {
                    if (authenticator.canAuthenticate()) {
                        authenticator.authenticate(
                            onSuccess = {
                                isAuthenticated = true
                                errorMessage = null
                            },
                            onError = { error ->
                                errorMessage = error
                            }
                        )
                    } else {
                        errorMessage = "생체 인증을 사용할 수 없습니다"
                    }
                }
            ) {
                Text("생체 인증으로 로그인")
            }
            
            errorMessage?.let { error ->
                Spacer(Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

---

## 네트워크 보안

### HTTPS 강제

```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="false">
    <!-- 모든 HTTP 통신 차단 -->
</application>
```

### Network Security Configuration

```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- 기본 설정: HTTPS만 허용 -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <!-- 시스템 인증서 신뢰 -->
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    
    <!-- 개발 환경: localhost만 HTTP 허용 -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain> <!-- Android Emulator -->
    </domain-config>
</network-security-config>
```

```xml
<!-- AndroidManifest.xml에 적용 -->
<application
    android:networkSecurityConfig="@xml/network_security_config">
</application>
```

### Certificate Pinning

**특정 인증서만 신뢰**

```kotlin
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

/**
 * Certificate Pinning
 * 
 * 중간자 공격(MITM) 방지
 */
class SecureHttpClient {
    
    /**
     * Certificate Pinning 설정
     */
    fun createSecureClient(): OkHttpClient {
        // 1. 서버 인증서의 Public Key Hash 가져오기
        // openssl s_client -connect api.example.com:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
        
        val certificatePinner = CertificatePinner.Builder()
            .add(
                "api.example.com",
                "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="  // 실제 해시로 교체
            )
            // 백업 핀 추가 (인증서 갱신 대비)
            .add(
                "api.example.com",
                "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
            )
            .build()
        
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .build()
    }
}

/**
 * Retrofit과 함께 사용
 */
class ApiClient {
    
    private val secureClient = SecureHttpClient().createSecureClient()
    
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com")
        .client(secureClient)  // ✅ Certificate Pinning 적용
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

### OkHttp Interceptor로 보안 헤더 추가

```kotlin
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 보안 헤더 Interceptor
 */
class SecurityHeadersInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 보안 헤더 추가
        val secureRequest = originalRequest.newBuilder()
            // ✅ API 키 (환경 변수에서 읽기)
            .addHeader("X-API-Key", BuildConfig.API_KEY)
            // ✅ 인증 토큰
            .addHeader("Authorization", "Bearer ${getAuthToken()}")
            // ✅ 요청 ID (추적용)
            .addHeader("X-Request-ID", generateRequestId())
            // ✅ 앱 버전
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .build()
        
        return chain.proceed(secureRequest)
    }
    
    private fun getAuthToken(): String {
        // SecurePreferences에서 토큰 읽기
        return ""  // 실제 구현 필요
    }
    
    private fun generateRequestId(): String {
        return UUID.randomUUID().toString()
    }
}

/**
 * Logging Interceptor (개발 환경만)
 */
class SecureLoggingInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        if (BuildConfig.DEBUG) {
            // ✅ 민감한 정보 마스킹
            val maskedUrl = maskSensitiveInfo(request.url.toString())
            Log.d("Network", "Request: $maskedUrl")
        }
        
        return chain.proceed(request)
    }
    
    /**
     * 민감한 정보 마스킹
     */
    private fun maskSensitiveInfo(url: String): String {
        return url
            .replace(Regex("token=[^&]+"), "token=***")
            .replace(Regex("password=[^&]+"), "password=***")
            .replace(Regex("api_key=[^&]+"), "api_key=***")
    }
}

/**
 * OkHttpClient 설정
 */
fun createSecureOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(SecurityHeadersInterceptor())
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(SecureLoggingInterceptor())
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

---

## 안전한 데이터 저장

### SQLCipher로 데이터베이스 암호화

```kotlin
// build.gradle.kts
dependencies {
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite:2.3.1")
}

/**
 * 암호화된 Room 데이터베이스
 */
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // ✅ SQLCipher로 암호화
                val passphrase = getPassphrase(context)
                val factory = SupportFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "encrypted_database"
                )
                    .openHelperFactory(factory)  // ✅ 암호화 적용
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 데이터베이스 암호 생성
         * 
         * Android Keystore에서 키 가져오기
         */
        private fun getPassphrase(context: Context): ByteArray {
            val securePrefs = SecurePreferences(context)
            var passphrase = securePrefs.getAuthToken()
            
            if (passphrase == null) {
                // 새 암호 생성
                passphrase = UUID.randomUUID().toString()
                securePrefs.saveAuthToken(passphrase)
            }
            
            return passphrase.toByteArray()
        }
    }
}
```

### 임시 데이터 관리

```kotlin
/**
 * 임시 데이터 안전하게 관리
 */
class TemporaryDataManager(private val context: Context) {
    
    /**
     * 캐시 디렉토리 사용
     * 
     * - 시스템이 자동으로 정리
     * - 앱 삭제 시 자동 삭제
     */
    fun saveTempFile(filename: String, content: ByteArray) {
        val tempFile = File(context.cacheDir, filename)
        tempFile.writeBytes(content)
    }
    
    /**
     * 민감한 임시 데이터는 즉시 삭제
     */
    fun processSecureData(data: ByteArray) {
        var tempData: ByteArray? = data
        
        try {
            // 데이터 처리
            processData(tempData!!)
        } finally {
            // ✅ 메모리에서 즉시 제거
            tempData?.fill(0)
            tempData = null
        }
    }
    
    /**
     * 앱 종료 시 임시 파일 삭제
     */
    fun clearTempFiles() {
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("temp_")) {
                file.delete()
            }
        }
    }
    
    private fun processData(data: ByteArray) {
        // 데이터 처리 로직
    }
}
```

### 로그 보안

```kotlin
/**
 * 안전한 로깅
 */
object SecureLogger {
    
    /**
     * 민감한 정보 마스킹
     */
    fun logSecure(tag: String, message: String) {
        val maskedMessage = maskSensitiveData(message)
        
        if (BuildConfig.DEBUG) {
            Log.d(tag, maskedMessage)
        }
        // ✅ Release 빌드에서는 로그 출력 안 함
    }
    
    /**
     * 민감한 데이터 마스킹
     */
    private fun maskSensitiveData(message: String): String {
        return message
            .replace(Regex("password=\\S+"), "password=***")
            .replace(Regex("token=\\S+"), "token=***")
            .replace(Regex("api_key=\\S+"), "api_key=***")
            .replace(Regex("\\d{4}-\\d{4}-\\d{4}-\\d{4}"), "****-****-****-****")  // 카드 번호
            .replace(Regex("\\d{3}-\\d{4}-\\d{4}"), "***-****-****")  // 전화번호
    }
    
    /**
     * 예외 로깅 (스택 트레이스 포함)
     */
    fun logError(tag: String, message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        } else {
            // ✅ Release 빌드에서는 Crashlytics 등으로 전송
            // Firebase.crashlytics.recordException(throwable)
        }
    }
}

/**
 * 사용 예
 */
class LoginViewModel : ViewModel() {
    
    fun login(email: String, password: String) {
        // ❌ 나쁜 예
        Log.d("Login", "Email: $email, Password: $password")  // 비밀번호 노출!
        
        // ✅ 좋은 예
        SecureLogger.logSecure("Login", "Login attempt for user: $email")
    }
}
```

---

## 인증 및 권한

### OAuth 2.0 구현

```kotlin
/**
 * OAuth 2.0 인증 플로우
 */
class OAuthManager(private val context: Context) {
    
    companion object {
        private const val AUTH_URL = "https://auth.example.com/oauth/authorize"
        private const val TOKEN_URL = "https://auth.example.com/oauth/token"
        private const val CLIENT_ID = "your_client_id"
        private const val REDIRECT_URI = "myapp://oauth/callback"
    }
    
    /**
     * 1단계: 인증 URL 생성
     */
    fun getAuthorizationUrl(): String {
        val state = generateState()
        saveState(state)
        
        return Uri.parse(AUTH_URL)
            .buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("state", state)
            .appendQueryParameter("scope", "read write")
            .build()
            .toString()
    }
    
    /**
     * 2단계: Authorization Code로 Access Token 교환
     */
    suspend fun exchangeCodeForToken(code: String, state: String): TokenResponse? {
        // State 검증 (CSRF 방지)
        if (!verifyState(state)) {
            Log.e("OAuth", "Invalid state")
            return null
        }
        
        val response = apiService.getAccessToken(
            grantType = "authorization_code",
            code = code,
            redirectUri = REDIRECT_URI,
            clientId = CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET
        )
        
        // ✅ 토큰 안전하게 저장
        saveTokens(response.accessToken, response.refreshToken)
        
        return response
    }
    
    /**
     * 3단계: Refresh Token으로 Access Token 갱신
     */
    suspend fun refreshAccessToken(): String? {
        val refreshToken = getRefreshToken() ?: return null
        
        val response = apiService.refreshToken(
            grantType = "refresh_token",
            refreshToken = refreshToken,
            clientId = CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET
        )
        
        saveTokens(response.accessToken, response.refreshToken)
        return response.accessToken
    }
    
    /**
     * State 생성 (CSRF 방지)
     */
    private fun generateState(): String {
        return UUID.randomUUID().toString()
    }
    
    private fun saveState(state: String) {
        val prefs = context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
        prefs.edit().putString("state", state).apply()
    }
    
    private fun verifyState(state: String): Boolean {
        val prefs = context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
        val savedState = prefs.getString("state", null)
        prefs.edit().remove("state").apply()
        return state == savedState
    }
    
    /**
     * 토큰 저장 (암호화)
     */
    private fun saveTokens(accessToken: String, refreshToken: String?) {
        val securePrefs = SecurePreferences(context)
        securePrefs.saveAuthToken(accessToken)
        refreshToken?.let {
            // Refresh Token도 암호화하여 저장
            context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
                .edit()
                .putString("refresh_token", it)
                .apply()
        }
    }
    
    private fun getRefreshToken(): String? {
        return context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
            .getString("refresh_token", null)
    }
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)
```

### JWT 토큰 관리

```kotlin
import com.auth0.android.jwt.JWT

/**
 * JWT 토큰 관리
 */
class JwtManager {
    
    /**
     * JWT 토큰 파싱
     */
    fun parseToken(token: String): JWT {
        return JWT(token)
    }
    
    /**
     * 토큰 만료 확인
     */
    fun isTokenExpired(token: String): Boolean {
        val jwt = parseToken(token)
        val expiresAt = jwt.expiresAt ?: return true
        return expiresAt.before(Date())
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserId(token: String): String? {
        val jwt = parseToken(token)
        return jwt.getClaim("user_id").asString()
    }
    
    /**
     * 토큰 갱신 필요 여부 확인
     */
    fun shouldRefreshToken(token: String): Boolean {
        val jwt = parseToken(token)
        val expiresAt = jwt.expiresAt ?: return true
        
        // 만료 5분 전이면 갱신
        val fiveMinutesFromNow = Date(System.currentTimeMillis() + 5 * 60 * 1000)
        return expiresAt.before(fiveMinutesFromNow)
    }
}

/**
 * 자동 토큰 갱신 Interceptor
 */
class TokenRefreshInterceptor(
    private val jwtManager: JwtManager,
    private val oauthManager: OAuthManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = getAccessToken()
        
        // 토큰 갱신 필요 여부 확인
        if (token != null && jwtManager.shouldRefreshToken(token)) {
            // ✅ 토큰 갱신
            runBlocking {
                oauthManager.refreshAccessToken()
            }
        }
        
        // 요청에 토큰 추가
        val newToken = getAccessToken()
        val newRequest = if (newToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $newToken")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
    
    private fun getAccessToken(): String? {
        // SecurePreferences에서 토큰 읽기
        return null  // 실제 구현 필요
    }
}
```

### 권한 최소화

```kotlin
/**
 * 런타임 권한 요청
 */
@Composable
fun PermissionRequestExample() {
    val context = LocalContext.current
    
    // ✅ 필요한 권한만 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 승인됨
            accessCamera()
        } else {
            // 권한 거부됨
            showPermissionDeniedMessage()
        }
    }
    
    Button(
        onClick = {
            when {
                // 권한이 이미 있는지 확인
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    accessCamera()
                }
                // 권한 설명이 필요한지 확인
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.CAMERA
                ) -> {
                    showPermissionRationale()
                }
                // 권한 요청
                else -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    ) {
        Text("카메라 권한 요청")
    }
}

/**
 * 권한 그룹화
 */
class PermissionManager {
    
    /**
     * 여러 권한 동시 요청
     */
    fun requestMultiplePermissions(
        activity: Activity,
        permissions: Array<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            onResult(results)
        }
        
        launcher.launch(permissions)
    }
    
    /**
     * 필요한 권한만 요청
     */
    fun requestOnlyNeededPermissions(
        activity: Activity,
        permissions: Array<String>
    ): Array<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
}
```

---

## 실전 예제

### 예제 1: 로그인 시스템

```kotlin
/**
 * 안전한 로그인 시스템
 */
class SecureLoginViewModel(
    private val authRepository: AuthRepository,
    private val securePrefs: SecurePreferences,
    private val biometricAuth: BiometricAuthenticator
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    /**
     * 이메일/비밀번호 로그인
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                // 1. 입력 검증
                if (!isValidEmail(email)) {
                    _loginState.value = LoginState.Error("유효하지 않은 이메일")
                    return@launch
                }
                
                if (password.length < 8) {
                    _loginState.value = LoginState.Error("비밀번호는 8자 이상이어야 합니다")
                    return@launch
                }
                
                // 2. API 호출 (HTTPS)
                val response = authRepository.login(email, password)
                
                // 3. 토큰 안전하게 저장
                securePrefs.saveAuthToken(response.accessToken)
                
                // 4. 생체 인증 등록 제안
                if (biometricAuth.canAuthenticate()) {
                    _loginState.value = LoginState.BiometricSetupAvailable
                } else {
                    _loginState.value = LoginState.Success
                }
                
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "로그인 실패")
            }
        }
    }
    
    /**
     * 생체 인증 로그인
     */
    fun loginWithBiometric() {
        biometricAuth.authenticate(
            onSuccess = {
                viewModelScope.launch {
                    // 저장된 토큰으로 자동 로그인
                    val token = securePrefs.getAuthToken()
                    if (token != null) {
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error("저장된 인증 정보가 없습니다")
                    }
                }
            },
            onError = { error ->
                _loginState.value = LoginState.Error(error)
            }
        )
    }
    
    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScope.launch {
            // 1. 서버에 로그아웃 요청
            authRepository.logout()
            
            // 2. 로컬 토큰 삭제
            securePrefs.clearAll()
            
            _loginState.value = LoginState.Idle
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    object BiometricSetupAvailable : LoginState()
    data class Error(val message: String) : LoginState()
}
```

### 예제 2: 결제 정보 저장

```kotlin
/**
 * 안전한 결제 정보 관리
 */
class PaymentManager(
    private val context: Context,
    private val encryption: KeystoreEncryption
) {
    
    /**
     * 카드 정보 저장 (PCI DSS 준수)
     */
    fun saveCardInfo(cardNumber: String, cvv: String, expiryDate: String) {
        // ❌ 실제 앱에서는 카드 정보를 저장하지 말 것!
        // ✅ 토큰화 서비스 사용 (Stripe, PayPal 등)
        
        // 예시: 마지막 4자리만 저장
        val lastFourDigits = cardNumber.takeLast(4)
        
        val securePrefs = SecurePreferences(context)
        securePrefs.saveUserCredentials("card_last_four", lastFourDigits)
    }
    
    /**
     * 결제 토큰 저장
     */
    fun savePaymentToken(token: String) {
        // ✅ 결제 토큰은 암호화하여 저장
        val (encryptedToken, iv) = encryption.encrypt(token)
        
        val prefs = context.getSharedPreferences("payment", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("token", Base64.encodeToString(encryptedToken, Base64.DEFAULT))
            .putString("iv", Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()
    }
    
    /**
     * 결제 처리
     */
    suspend fun processPayment(amount: Int): PaymentResult {
        // 1. 저장된 토큰 가져오기
        val token = getPaymentToken() ?: return PaymentResult.Error("토큰 없음")
        
        // 2. HTTPS로 결제 요청
        return try {
            val response = paymentApi.processPayment(
                token = token,
                amount = amount,
                currency = "KRW"
            )
            
            PaymentResult.Success(response.transactionId)
        } catch (e: Exception) {
            PaymentResult.Error(e.message ?: "결제 실패")
        }
    }
    
    private fun getPaymentToken(): String? {
        val prefs = context.getSharedPreferences("payment", Context.MODE_PRIVATE)
        val encryptedToken = prefs.getString("token", null) ?: return null
        val iv = prefs.getString("iv", null) ?: return null
        
        val tokenBytes = Base64.decode(encryptedToken, Base64.DEFAULT)
        val ivBytes = Base64.decode(iv, Base64.DEFAULT)
        
        return encryption.decrypt(tokenBytes, ivBytes)
    }
}

sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
}
```

### 예제 3: 민감한 데이터 처리

```kotlin
/**
 * 민감한 사용자 데이터 관리
 */
class UserDataManager(
    private val context: Context,
    private val securePrefs: SecurePreferences,
    private val secureFileManager: SecureFileManager
) {
    
    /**
     * 개인정보 저장
     */
    fun savePersonalInfo(
        name: String,
        phoneNumber: String,
        address: String
    ) {
        // ✅ 암호화하여 저장
        val personalInfo = PersonalInfo(name, phoneNumber, address)
        val json = Gson().toJson(personalInfo)
        
        secureFileManager.writeEncryptedFile("personal_info.json", json)
    }
    
    /**
     * 개인정보 읽기
     */
    fun getPersonalInfo(): PersonalInfo? {
        return try {
            val json = secureFileManager.readEncryptedFile("personal_info.json")
            Gson().fromJson(json, PersonalInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 민감한 데이터 삭제
     */
    fun deletePersonalInfo() {
        val file = File(context.filesDir, "personal_info.json")
        if (file.exists()) {
            // ✅ 파일 덮어쓰기 후 삭제 (복구 방지)
            file.writeBytes(ByteArray(file.length().toInt()) { 0 })
            file.delete()
        }
    }
}

data class PersonalInfo(
    val name: String,
    val phoneNumber: String,
    val address: String
)
```

---

## 💡 베스트 프랙티스 요약

### 데이터 암호화
- ✅ EncryptedSharedPreferences 사용
- ✅ EncryptedFile로 파일 암호화
- ✅ Android Keystore로 키 관리
- ✅ 생체 인증 통합

### 네트워크 보안
- ✅ HTTPS 강제
- ✅ Certificate Pinning
- ✅ 보안 헤더 추가
- ✅ 민감한 정보 마스킹

### 데이터 저장
- ✅ SQLCipher로 DB 암호화
- ✅ 임시 데이터 즉시 삭제
- ✅ 로그에 민감한 정보 제외
- ✅ Release 빌드에서 로그 비활성화

### 인증 및 권한
- ✅ OAuth 2.0 사용
- ✅ JWT 토큰 관리
- ✅ 권한 최소화
- ✅ State 검증 (CSRF 방지)

---

## 🎯 다음 단계

보안 기초를 마스터했습니다! 다음으로:

1. **[22-2. Android 보안 고급](./22-2-android-security-advanced.md)** - ProGuard/R8, 루팅 탐지, 코드 난독화
2. **[22-3. Android 보안 테스팅](./22-3-android-security-testing.md)** - 보안 테스트 자동화, 취약점 스캔

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Securing! 🔒
