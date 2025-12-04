# Android 보안 고급

> 📖 **시리즈 구성**
> - **22-1**: [Android 보안 기초](./22-1-android-security-basics.md)
> - **22-2**: Android 보안 고급 (현재 문서)
> - **22-3**: [Android 보안 테스팅](./22-3-android-security-testing.md)

---

## 📚 목차

1. [코드 보호](#코드-보호)
2. [앱 무결성](#앱-무결성)
3. [보안 아키텍처](#보안-아키텍처)
4. [고급 암호화](#고급-암호화)
5. [실전 사례](#실전-사례)

---

## 코드 보호

### ProGuard 설정

**코드 난독화 및 최적화**

```groovy
// build.gradle.kts
android {
    buildTypes {
        release {
            // ✅ ProGuard/R8 활성화
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**proguard-rules.pro:**

```proguard
# ===== 기본 설정 =====

# 최적화 옵션
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ===== 난독화 제외 =====

# Parcelable 유지
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Serializable 유지
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Enum 유지
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===== 라이브러리 설정 =====

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 데이터 클래스 유지 (API 모델)
-keep class com.example.app.data.model.** { *; }

# ===== 보안 강화 =====

# 스택 트레이스 제거 (Release)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 소스 파일 정보 제거
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ===== Compose 설정 =====

# Compose 유지
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
```

### R8 최적화

**R8은 ProGuard의 개선 버전**

```groovy
// gradle.properties
android.enableR8.fullMode=true
```

**R8 전용 규칙:**

```proguard
# R8 최적화
-allowaccessmodification
-repackageclasses ''

# 사용하지 않는 코드 제거
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkParameterIsNotNull(...);
}
```

### 난독화 맵 저장

```groovy
android {
    buildTypes {
        release {
            // ✅ 난독화 맵 저장 (크래시 분석용)
            proguardFiles(...)
        }
    }
}
```

**mapping.txt 사용:**
- 위치: `app/build/outputs/mapping/release/mapping.txt`
- Firebase Crashlytics에 업로드
- 크래시 리포트 디코딩에 사용

### 문자열 암호화

```kotlin
/**
 * 중요한 문자열 암호화
 * 
 * ProGuard만으로는 문자열이 노출됨
 */
object SecureStrings {
    
    // ❌ 나쁜 예: 평문 저장
    const val API_KEY = "sk_live_1234567890abcdef"
    
    // ✅ 좋은 예: 난독화된 문자열
    private val ENCODED_API_KEY = byteArrayOf(
        0x73, 0x6b, 0x5f, 0x6c, 0x69, 0x76, 0x65, 0x5f,
        // ... (실제로는 XOR 등으로 인코딩)
    )
    
    /**
     * API 키 복호화
     */
    fun getApiKey(): String {
        return decode(ENCODED_API_KEY)
    }
    
    /**
     * 간단한 XOR 디코딩
     */
    private fun decode(encoded: ByteArray): String {
        val key = 0x42  // XOR 키
        return String(encoded.map { (it.toInt() xor key).toByte() }.toByteArray())
    }
}

/**
 * Native 코드로 문자열 보호 (더 안전)
 */
class NativeSecureStrings {
    
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    
    /**
     * Native 메서드로 API 키 가져오기
     */
    external fun getApiKey(): String
}

// native-lib.cpp
/*
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_NativeSecureStrings_getApiKey(JNIEnv* env, jobject) {
    // C++ 코드에서 문자열 반환
    // 디컴파일이 더 어려움
    const char* apiKey = "sk_live_1234567890abcdef";
    return env->NewStringUTF(apiKey);
}
*/
```

### 리플렉션 방지

```kotlin
/**
 * 리플렉션 공격 방지
 */
class SecureClass {
    
    // ❌ 나쁜 예: private 필드가 리플렉션으로 접근 가능
    private var secretKey: String = "secret"
    
    // ✅ 좋은 예: 검증 추가
    private var _secureKey: String = "secret"
        get() {
            // 리플렉션 감지
            if (isReflectionAccess()) {
                throw SecurityException("Reflection access detected")
            }
            return field
        }
    
    /**
     * 리플렉션 접근 감지
     */
    private fun isReflectionAccess(): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        
        // 스택 트레이스에서 리플렉션 호출 확인
        return stackTrace.any { element ->
            element.className.contains("java.lang.reflect")
        }
    }
}

/**
 * ProGuard로 리플렉션 방지
 */
// proguard-rules.pro
/*
# 특정 클래스의 리플렉션 차단
-keepclassmembers class com.example.SecureClass {
    !private *;
}
*/
```

---

## 앱 무결성

### 루팅 탐지

```kotlin
/**
 * 루팅된 기기 탐지
 */
class RootDetector(private val context: Context) {
    
    /**
     * 루팅 여부 확인
     */
    fun isDeviceRooted(): Boolean {
        return checkRootBinaries() ||
               checkSuperuserApk() ||
               checkRootProperties() ||
               checkRWPaths() ||
               checkDangerousApps()
    }
    
    /**
     * 1. 루팅 바이너리 확인
     */
    private fun checkRootBinaries(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        return paths.any { path ->
            File(path).exists()
        }
    }
    
    /**
     * 2. Superuser 앱 확인
     */
    private fun checkSuperuserApk(): Boolean {
        val packageManager = context.packageManager
        val packages = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk"
        )
        
        return packages.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    
    /**
     * 3. 시스템 속성 확인
     */
    private fun checkRootProperties(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    
    /**
     * 4. 쓰기 가능한 시스템 경로 확인
     */
    private fun checkRWPaths(): Boolean {
        val paths = arrayOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
            "/etc"
        )
        
        return paths.any { path ->
            val file = File(path)
            file.exists() && file.canWrite()
        }
    }
    
    /**
     * 5. 위험한 앱 확인
     */
    private fun checkDangerousApps(): Boolean {
        val dangerousApps = arrayOf(
            "com.chelpus.lackypatch",
            "com.dimonvideo.luckypatcher",
            "com.forpda.lp",
            "com.android.vending.billing.InAppBillingService.COIN",
            "com.android.vending.billing.InAppBillingService.LUCK"
        )
        
        val packageManager = context.packageManager
        return dangerousApps.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    
    /**
     * 루팅 감지 시 조치
     */
    fun handleRootedDevice() {
        if (isDeviceRooted()) {
            // 옵션 1: 경고 표시
            showRootWarning()
            
            // 옵션 2: 기능 제한
            disableSensitiveFeatures()
            
            // 옵션 3: 앱 종료
            // exitProcess(0)
        }
    }
    
    private fun showRootWarning() {
        Log.w("Security", "Rooted device detected")
    }
    
    private fun disableSensitiveFeatures() {
        // 결제, 민감한 데이터 접근 등 제한
    }
}
```

### 에뮬레이터 탐지

```kotlin
/**
 * 에뮬레이터 탐지
 */
class EmulatorDetector {
    
    /**
     * 에뮬레이터 여부 확인
     */
    fun isEmulator(): Boolean {
        return checkBuildInfo() ||
               checkTelephony() ||
               checkHardware() ||
               checkFiles()
    }
    
    /**
     * 1. Build 정보 확인
     */
    private fun checkBuildInfo(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * 2. 전화 기능 확인
     */
    private fun checkTelephony(): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val deviceId = telephonyManager.deviceId
        
        // 에뮬레이터는 특정 IMEI 사용
        return deviceId == null ||
               deviceId == "000000000000000" ||
               deviceId == "e21833235b6eef10"
    }
    
    /**
     * 3. 하드웨어 정보 확인
     */
    private fun checkHardware(): Boolean {
        return (Build.HARDWARE == "goldfish" ||
                Build.HARDWARE == "ranchu" ||
                Build.HARDWARE.contains("nox") ||
                Build.HARDWARE.contains("vbox"))
    }
    
    /**
     * 4. 에뮬레이터 파일 확인
     */
    private fun checkFiles(): Boolean {
        val emulatorFiles = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
        )
        
        return emulatorFiles.any { path ->
            File(path).exists()
        }
    }
}
```

### 디버깅 방지

```kotlin
/**
 * 디버깅 탐지 및 방지
 */
class DebugDetector(private val context: Context) {
    
    /**
     * 디버깅 여부 확인
     */
    fun isDebuggable(): Boolean {
        return checkDebugFlag() ||
               checkDebugger() ||
               checkTracerPid()
    }
    
    /**
     * 1. Debug 플래그 확인
     */
    private fun checkDebugFlag(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    
    /**
     * 2. 디버거 연결 확인
     */
    private fun checkDebugger(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }
    
    /**
     * 3. TracerPid 확인
     */
    private fun checkTracerPid(): Boolean {
        try {
            val statusFile = File("/proc/self/status")
            val lines = statusFile.readLines()
            
            lines.forEach { line ->
                if (line.startsWith("TracerPid:")) {
                    val pid = line.substring(10).trim().toIntOrNull()
                    // TracerPid가 0이 아니면 디버깅 중
                    return pid != null && pid != 0
                }
            }
        } catch (e: Exception) {
            // 파일 읽기 실패
        }
        
        return false
    }
    
    /**
     * 디버깅 감지 시 조치
     */
    fun handleDebugging() {
        if (isDebuggable()) {
            // 옵션 1: 앱 종료
            exitProcess(0)
            
            // 옵션 2: 무한 루프 (디버거 방해)
            // while (true) { }
            
            // 옵션 3: 잘못된 데이터 표시
            // showFakeData()
        }
    }
}

/**
 * Native 코드로 디버깅 방지 (더 강력)
 */
class NativeDebugDetector {
    
    companion object {
        init {
            System.loadLibrary("anti-debug")
        }
    }
    
    /**
     * Native 메서드로 디버깅 확인
     */
    external fun isDebuggerAttached(): Boolean
}

// anti-debug.cpp
/*
#include <sys/ptrace.h>
#include <unistd.h>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_NativeDebugDetector_isDebuggerAttached(JNIEnv* env, jobject) {
    // ptrace로 디버거 탐지
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) < 0) {
        // 이미 디버거가 연결됨
        return JNI_TRUE;
    }
    
    // 자신을 detach
    ptrace(PTRACE_DETACH, 0, 1, 0);
    return JNI_FALSE;
}
*/
```

### 앱 변조 탐지

```kotlin
/**
 * APK 무결성 검증
 */
class IntegrityChecker(private val context: Context) {
    
    /**
     * 앱 서명 확인
     */
    fun verifySignature(): Boolean {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            // 예상되는 서명과 비교
            val expectedSignature = getExpectedSignature()
            
            return signatures.any { signature ->
                val currentSignature = signature.toCharsString()
                currentSignature == expectedSignature
            }
            
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * 예상되는 서명 (Release 키스토어)
     */
    private fun getExpectedSignature(): String {
        // 실제 앱의 서명으로 교체
        return "308204a830820390..."
    }
    
    /**
     * APK 체크섬 확인
     */
    fun verifyChecksum(): Boolean {
        try {
            val apkPath = context.packageCodePath
            val apkFile = File(apkPath)
            
            // APK 파일의 SHA-256 해시 계산
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream = FileInputStream(apkFile)
            val buffer = ByteArray(8192)
            var read: Int
            
            while (inputStream.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
            
            inputStream.close()
            
            val hash = digest.digest()
            val currentChecksum = hash.joinToString("") { "%02x".format(it) }
            
            // 예상되는 체크섬과 비교
            val expectedChecksum = getExpectedChecksum()
            
            return currentChecksum == expectedChecksum
            
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * 예상되는 체크섬
     */
    private fun getExpectedChecksum(): String {
        // 빌드 시 계산된 체크섬으로 교체
        return "a1b2c3d4e5f6..."
    }
    
    /**
     * 설치 소스 확인
     */
    fun verifyInstaller(): Boolean {
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        
        // Google Play에서 설치되었는지 확인
        val validInstallers = listOf(
            "com.android.vending",  // Google Play
            "com.google.android.feedback"  // Google Play (일부 기기)
        )
        
        return installer in validInstallers
    }
}
```

---

## 보안 아키텍처

### Multi-Layer 보안

```kotlin
/**
 * 다층 보안 아키텍처
 * 
 * Layer 1: 네트워크 보안
 * Layer 2: 데이터 암호화
 * Layer 3: 코드 보호
 * Layer 4: 런타임 보안
 */
class SecurityArchitecture(private val context: Context) {
    
    private val rootDetector = RootDetector(context)
    private val debugDetector = DebugDetector(context)
    private val integrityChecker = IntegrityChecker(context)
    
    /**
     * 앱 시작 시 보안 검사
     */
    fun performSecurityChecks(): SecurityCheckResult {
        val checks = mutableListOf<SecurityCheck>()
        
        // 1. 루팅 확인
        if (rootDetector.isDeviceRooted()) {
            checks.add(SecurityCheck.ROOTED_DEVICE)
        }
        
        // 2. 디버깅 확인
        if (debugDetector.isDebuggable()) {
            checks.add(SecurityCheck.DEBUGGABLE)
        }
        
        // 3. 앱 무결성 확인
        if (!integrityChecker.verifySignature()) {
            checks.add(SecurityCheck.INVALID_SIGNATURE)
        }
        
        // 4. 설치 소스 확인
        if (!integrityChecker.verifyInstaller()) {
            checks.add(SecurityCheck.INVALID_INSTALLER)
        }
        
        return SecurityCheckResult(checks)
    }
    
    /**
     * 보안 수준에 따른 기능 제어
     */
    fun getSecurityLevel(): SecurityLevel {
        val result = performSecurityChecks()
        
        return when {
            result.hasCheck(SecurityCheck.ROOTED_DEVICE) -> SecurityLevel.CRITICAL
            result.hasCheck(SecurityCheck.DEBUGGABLE) -> SecurityLevel.HIGH
            result.hasCheck(SecurityCheck.INVALID_SIGNATURE) -> SecurityLevel.CRITICAL
            result.hasCheck(SecurityCheck.INVALID_INSTALLER) -> SecurityLevel.MEDIUM
            else -> SecurityLevel.NORMAL
        }
    }
}

enum class SecurityCheck {
    ROOTED_DEVICE,
    DEBUGGABLE,
    INVALID_SIGNATURE,
    INVALID_INSTALLER
}

data class SecurityCheckResult(
    val checks: List<SecurityCheck>
) {
    fun hasCheck(check: SecurityCheck) = checks.contains(check)
    fun isSecure() = checks.isEmpty()
}

enum class SecurityLevel {
    NORMAL,    // 모든 기능 사용 가능
    MEDIUM,    // 일부 기능 제한
    HIGH,      // 민감한 기능 차단
    CRITICAL   // 앱 사용 불가
}

/**
 * 보안 수준에 따른 기능 제어
 */
class FeatureController(private val securityArchitecture: SecurityArchitecture) {
    
    /**
     * 결제 기능 사용 가능 여부
     */
    fun canUsePayment(): Boolean {
        val level = securityArchitecture.getSecurityLevel()
        return level == SecurityLevel.NORMAL
    }
    
    /**
     * 민감한 데이터 접근 가능 여부
     */
    fun canAccessSensitiveData(): Boolean {
        val level = securityArchitecture.getSecurityLevel()
        return level == SecurityLevel.NORMAL || level == SecurityLevel.MEDIUM
    }
    
    /**
     * 앱 사용 가능 여부
     */
    fun canUseApp(): Boolean {
        val level = securityArchitecture.getSecurityLevel()
        return level != SecurityLevel.CRITICAL
    }
}
```

### Secure Enclave (TEE)

```kotlin
/**
 * Trusted Execution Environment 사용
 * 
 * Android Keystore의 하드웨어 지원
 */
class SecureEnclaveManager {
    
    /**
     * 하드웨어 지원 키 생성
     */
    fun generateHardwareBackedKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // ✅ 하드웨어 지원 요구
            .setIsStrongBoxBacked(true)  // Android 9+
            // ✅ 사용자 인증 필요
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(
                30,  // 30초 동안 유효
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        
        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }
    
    /**
     * 하드웨어 지원 여부 확인
     */
    fun isHardwareBackedKeystore(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            
            // 테스트 키 생성
            generateHardwareBackedKey("test_key")
            
            val entry = keyStore.getEntry("test_key", null) as KeyStore.SecretKeyEntry
            val key = entry.secretKey
            
            // 하드웨어 지원 확인
            val factory = SecretKeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            
            keyInfo.isInsideSecureHardware
            
        } catch (e: Exception) {
            false
        }
    }
}
```

### Zero Trust 모델

```kotlin
/**
 * Zero Trust 보안 모델
 * 
 * "절대 신뢰하지 말고, 항상 검증하라"
 */
class ZeroTrustManager(
    private val context: Context,
    private val securityArchitecture: SecurityArchitecture
) {
    
    /**
     * 모든 요청에 대해 검증
     */
    suspend fun executeSecureAction(action: suspend () -> Unit) {
        // 1. 환경 검증
        if (!verifyEnvironment()) {
            throw SecurityException("Insecure environment")
        }
        
        // 2. 사용자 인증
        if (!verifyUser()) {
            throw SecurityException("User not authenticated")
        }
        
        // 3. 세션 검증
        if (!verifySession()) {
            throw SecurityException("Invalid session")
        }
        
        // 4. 작업 실행
        try {
            action()
        } finally {
            // 5. 감사 로그
            logSecurityEvent("Action executed")
        }
    }
    
    /**
     * 환경 검증
     */
    private fun verifyEnvironment(): Boolean {
        val result = securityArchitecture.performSecurityChecks()
        return result.isSecure()
    }
    
    /**
     * 사용자 인증 검증
     */
    private fun verifyUser(): Boolean {
        val securePrefs = SecurePreferences(context)
        val token = securePrefs.getAuthToken()
        
        // 토큰 유효성 검증
        return token != null && !isTokenExpired(token)
    }
    
    /**
     * 세션 검증
     */
    private fun verifySession(): Boolean {
        // 세션 타임아웃 확인
        val lastActivity = getLastActivityTime()
        val now = System.currentTimeMillis()
        val timeout = 15 * 60 * 1000  // 15분
        
        return (now - lastActivity) < timeout
    }
    
    /**
     * 보안 이벤트 로깅
     */
    private fun logSecurityEvent(event: String) {
        val timestamp = System.currentTimeMillis()
        val userId = getCurrentUserId()
        
        Log.d("Security", "[$timestamp] User $userId: $event")
        
        // 서버로 전송 (선택사항)
        // sendSecurityLog(timestamp, userId, event)
    }
    
    private fun isTokenExpired(token: String): Boolean = false
    private fun getLastActivityTime(): Long = System.currentTimeMillis()
    private fun getCurrentUserId(): String = "user123"
}
```

---

## 고급 암호화

### AES-GCM 암호화

```kotlin
/**
 * AES-GCM 암호화
 * 
 * - 인증된 암호화 (AEAD)
 * - 무결성 보장
 */
class AesGcmEncryption {
    
    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
    }
    
    /**
     * 키 생성
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }
    
    /**
     * 암호화
     */
    fun encrypt(plainText: ByteArray, key: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText)
        
        return EncryptedData(cipherText, iv)
    }
    
    /**
     * 복호화
     */
    fun decrypt(encryptedData: EncryptedData, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return cipher.doFinal(encryptedData.cipherText)
    }
}

data class EncryptedData(
    val cipherText: ByteArray,
    val iv: ByteArray
)
```

### RSA 키 교환

```kotlin
/**
 * RSA 비대칭 암호화
 * 
 * 용도: 대칭 키 교환
 */
class RsaKeyExchange {
    
    companion object {
        private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val KEY_SIZE = 2048
    }
    
    /**
     * RSA 키 쌍 생성
     */
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }
    
    /**
     * 공개 키로 암호화
     */
    fun encrypt(data: ByteArray, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }
    
    /**
     * 개인 키로 복호화
     */
    fun decrypt(encryptedData: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(encryptedData)
    }
}
```

### ECDH 키 교환

```kotlin
/**
 * Elliptic Curve Diffie-Hellman
 * 
 * 용도: 안전한 키 교환
 */
class EcdhKeyExchange {
    
    /**
     * EC 키 쌍 생성
     */
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        val ecSpec = ECGenParameterSpec("secp256r1")
        keyPairGenerator.initialize(ecSpec)
        return keyPairGenerator.generateKeyPair()
    }
    
    /**
     * 공유 비밀 생성
     */
    fun generateSharedSecret(
        myPrivateKey: PrivateKey,
        theirPublicKey: PublicKey
    ): ByteArray {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(myPrivateKey)
        keyAgreement.doPhase(theirPublicKey, true)
        
        return keyAgreement.generateSecret()
    }
    
    /**
     * 공유 비밀로 AES 키 생성
     */
    fun deriveAesKey(sharedSecret: ByteArray): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(sharedSecret)
        
        return SecretKeySpec(keyBytes, "AES")
    }
}
```

### 하이브리드 암호화

```kotlin
/**
 * 하이브리드 암호화
 * 
 * RSA + AES 조합
 * - RSA: 대칭 키 암호화
 * - AES: 데이터 암호화
 */
class HybridEncryption(
    private val rsaKeyExchange: RsaKeyExchange,
    private val aesEncryption: AesGcmEncryption
) {
    
    /**
     * 암호화
     */
    fun encrypt(data: ByteArray, recipientPublicKey: PublicKey): HybridEncryptedData {
        // 1. AES 키 생성
        val aesKey = aesEncryption.generateKey()
        
        // 2. 데이터를 AES로 암호화
        val encryptedData = aesEncryption.encrypt(data, aesKey)
        
        // 3. AES 키를 RSA로 암호화
        val encryptedKey = rsaKeyExchange.encrypt(aesKey.encoded, recipientPublicKey)
        
        return HybridEncryptedData(
            encryptedData = encryptedData,
            encryptedKey = encryptedKey
        )
    }
    
    /**
     * 복호화
     */
    fun decrypt(hybridData: HybridEncryptedData, privateKey: PrivateKey): ByteArray {
        // 1. RSA로 AES 키 복호화
        val aesKeyBytes = rsaKeyExchange.decrypt(hybridData.encryptedKey, privateKey)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")
        
        // 2. AES로 데이터 복호화
        return aesEncryption.decrypt(hybridData.encryptedData, aesKey)
    }
}

data class HybridEncryptedData(
    val encryptedData: EncryptedData,
    val encryptedKey: ByteArray
)
```

---

## 실전 사례

### 사례 1: 금융 앱 보안

```kotlin
/**
 * 금융 앱 보안 구현
 */
class BankingAppSecurity(private val context: Context) {
    
    private val securityArchitecture = SecurityArchitecture(context)
    private val zeroTrust = ZeroTrustManager(context, securityArchitecture)
    
    /**
     * 앱 시작 시 보안 검사
     */
    suspend fun initializeSecurity(): Boolean {
        // 1. 보안 검사
        val securityLevel = securityArchitecture.getSecurityLevel()
        
        when (securityLevel) {
            SecurityLevel.CRITICAL -> {
                // 앱 사용 불가
                showSecurityAlert("보안 위협이 감지되어 앱을 사용할 수 없습니다.")
                return false
            }
            SecurityLevel.HIGH -> {
                // 경고 표시
                showSecurityWarning("보안 위험이 감지되었습니다. 일부 기능이 제한됩니다.")
            }
            else -> {
                // 정상
            }
        }
        
        // 2. 인증서 피닝 설정
        setupCertificatePinning()
        
        // 3. 보안 로깅 시작
        startSecurityLogging()
        
        return true
    }
    
    /**
     * 송금 실행
     */
    suspend fun transferMoney(
        amount: Int,
        recipientAccount: String
    ): TransferResult {
        return try {
            zeroTrust.executeSecureAction {
                // 1. 추가 인증 (생체 인증)
                requireBiometricAuth()
                
                // 2. 거래 한도 확인
                if (amount > getTransferLimit()) {
                    throw SecurityException("Transfer limit exceeded")
                }
                
                // 3. 송금 실행
                executeTransfer(amount, recipientAccount)
            }
            
            TransferResult.Success
            
        } catch (e: SecurityException) {
            TransferResult.SecurityError(e.message ?: "Security check failed")
        } catch (e: Exception) {
            TransferResult.Error(e.message ?: "Transfer failed")
        }
    }
    
    private fun setupCertificatePinning() {
        // Certificate Pinning 설정
    }
    
    private fun startSecurityLogging() {
        // 보안 이벤트 로깅 시작
    }
    
    private suspend fun requireBiometricAuth() {
        // 생체 인증 요구
    }
    
    private fun getTransferLimit(): Int = 1000000  // 100만원
    
    private suspend fun executeTransfer(amount: Int, recipient: String) {
        // 실제 송금 로직
    }
    
    private fun showSecurityAlert(message: String) {}
    private fun showSecurityWarning(message: String) {}
}

sealed class TransferResult {
    object Success : TransferResult()
    data class SecurityError(val message: String) : TransferResult()
    data class Error(val message: String) : TransferResult()
}
```

### 사례 2: 의료 앱 보안 (HIPAA 준수)

```kotlin
/**
 * 의료 앱 보안 (HIPAA 준수)
 */
class HealthcareAppSecurity(private val context: Context) {
    
    private val encryption = AesGcmEncryption()
    private val secureFileManager = SecureFileManager(context)
    
    /**
     * 환자 데이터 저장
     */
    fun savePatientData(patientData: PatientData) {
        // 1. 데이터 암호화
        val json = Gson().toJson(patientData)
        val key = getEncryptionKey()
        val encryptedData = encryption.encrypt(json.toByteArray(), key)
        
        // 2. 암호화된 파일로 저장
        val filename = "patient_${patientData.id}.enc"
        saveEncryptedFile(filename, encryptedData)
        
        // 3. 접근 로그 기록
        logDataAccess("SAVE", patientData.id)
    }
    
    /**
     * 환자 데이터 읽기
     */
    fun getPatientData(patientId: String): PatientData? {
        return try {
            // 1. 암호화된 파일 읽기
            val filename = "patient_${patientId}.enc"
            val encryptedData = readEncryptedFile(filename)
            
            // 2. 데이터 복호화
            val key = getEncryptionKey()
            val decryptedBytes = encryption.decrypt(encryptedData, key)
            val json = String(decryptedBytes)
            
            // 3. 접근 로그 기록
            logDataAccess("READ", patientId)
            
            Gson().fromJson(json, PatientData::class.java)
            
        } catch (e: Exception) {
            Log.e("Healthcare", "Failed to read patient data", e)
            null
        }
    }
    
    /**
     * 데이터 접근 로그
     */
    private fun logDataAccess(action: String, patientId: String) {
        val timestamp = System.currentTimeMillis()
        val userId = getCurrentUserId()
        val deviceId = getDeviceId()
        
        val logEntry = AccessLog(
            timestamp = timestamp,
            userId = userId,
            deviceId = deviceId,
            action = action,
            patientId = patientId
        )
        
        // 로그를 암호화하여 저장
        saveAccessLog(logEntry)
        
        // 서버로 전송 (감사 추적)
        sendAuditLog(logEntry)
    }
    
    /**
     * 데이터 보존 기간 관리
     */
    fun enforceDataRetention() {
        val retentionPeriod = 7 * 365 * 24 * 60 * 60 * 1000L  // 7년
        val now = System.currentTimeMillis()
        
        // 보존 기간이 지난 데이터 삭제
        getAllPatientIds().forEach { patientId ->
            val createdAt = getPatientCreatedTime(patientId)
            if (now - createdAt > retentionPeriod) {
                deletePatientData(patientId)
            }
        }
    }
    
    private fun getEncryptionKey(): SecretKey = encryption.generateKey()
    private fun saveEncryptedFile(filename: String, data: EncryptedData) {}
    private fun readEncryptedFile(filename: String): EncryptedData = EncryptedData(byteArrayOf(), byteArrayOf())
    private fun getCurrentUserId(): String = "doctor123"
    private fun getDeviceId(): String = "device456"
    private fun saveAccessLog(log: AccessLog) {}
    private fun sendAuditLog(log: AccessLog) {}
    private fun getAllPatientIds(): List<String> = emptyList()
    private fun getPatientCreatedTime(patientId: String): Long = 0L
    private fun deletePatientData(patientId: String) {}
}

data class PatientData(
    val id: String,
    val name: String,
    val diagnosis: String,
    val medications: List<String>
)

data class AccessLog(
    val timestamp: Long,
    val userId: String,
    val deviceId: String,
    val action: String,
    val patientId: String
)
```

### 사례 3: 메신저 앱 보안 (E2E 암호화)

```kotlin
/**
 * 메신저 앱 End-to-End 암호화
 */
class MessengerSecurity {
    
    private val ecdhKeyExchange = EcdhKeyExchange()
    private val aesEncryption = AesGcmEncryption()
    
    /**
     * 채팅방 초기화
     */
    fun initializeChatRoom(otherUserPublicKey: PublicKey): ChatSession {
        // 1. 내 키 쌍 생성
        val myKeyPair = ecdhKeyExchange.generateKeyPair()
        
        // 2. 공유 비밀 생성
        val sharedSecret = ecdhKeyExchange.generateSharedSecret(
            myKeyPair.private,
            otherUserPublicKey
        )
        
        // 3. 공유 비밀로 AES 키 생성
        val sessionKey = ecdhKeyExchange.deriveAesKey(sharedSecret)
        
        return ChatSession(
            myPublicKey = myKeyPair.public,
            sessionKey = sessionKey
        )
    }
    
    /**
     * 메시지 암호화
     */
    fun encryptMessage(message: String, session: ChatSession): EncryptedMessage {
        val encryptedData = aesEncryption.encrypt(
            message.toByteArray(),
            session.sessionKey
        )
        
        return EncryptedMessage(
            cipherText = Base64.encodeToString(encryptedData.cipherText, Base64.DEFAULT),
            iv = Base64.encodeToString(encryptedData.iv, Base64.DEFAULT),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 메시지 복호화
     */
    fun decryptMessage(encryptedMessage: EncryptedMessage, session: ChatSession): String {
        val encryptedData = EncryptedData(
            cipherText = Base64.decode(encryptedMessage.cipherText, Base64.DEFAULT),
            iv = Base64.decode(encryptedMessage.iv, Base64.DEFAULT)
        )
        
        val decryptedBytes = aesEncryption.decrypt(encryptedData, session.sessionKey)
        return String(decryptedBytes)
    }
}

data class ChatSession(
    val myPublicKey: PublicKey,
    val sessionKey: SecretKey
)

data class EncryptedMessage(
    val cipherText: String,
    val iv: String,
    val timestamp: Long
)
```

---

## 💡 베스트 프랙티스 요약

### 코드 보호
- ✅ ProGuard/R8 활성화
- ✅ 문자열 암호화
- ✅ Native 코드 사용
- ✅ 리플렉션 방지

### 앱 무결성
- ✅ 루팅 탐지
- ✅ 에뮬레이터 탐지
- ✅ 디버깅 방지
- ✅ 앱 변조 탐지

### 보안 아키텍처
- ✅ Multi-Layer 보안
- ✅ Secure Enclave 사용
- ✅ Zero Trust 모델
- ✅ 보안 수준별 기능 제어

### 고급 암호화
- ✅ AES-GCM 사용
- ✅ RSA 키 교환
- ✅ ECDH 키 교환
- ✅ 하이브리드 암호화

---

## 🎯 다음 단계

보안 고급을 마스터했습니다! 다음으로:

1. **[22-3. Android 보안 테스팅](./22-3-android-security-testing.md)** - 보안 테스트 자동화, 취약점 스캔

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Securing! 🛡️
