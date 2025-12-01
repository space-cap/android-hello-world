# Compose Multiplatform 아키텍처 및 플랫폼별 코드

## 목차
1. [expect/actual 패턴 완벽 가이드](#expectactual-패턴-완벽-가이드)
2. [플랫폼별 코드 작성 전략](#플랫폼별-코드-작성-전략)
3. [아키텍처 패턴 (MVVM)](#아키텍처-패턴-mvvm)
4. [상태 관리](#상태-관리)
5. [의존성 주입](#의존성-주입)
6. [실전 예제: 멀티플랫폼 설정 관리](#실전-예제-멀티플랫폼-설정-관리)

---

## expect/actual 패턴 완벽 가이드

### expect/actual이란?

**expect/actual**은 Kotlin Multiplatform의 핵심 메커니즘으로, 플랫폼별로 다른 구현이 필요한 코드를 작성할 때 사용합니다.

```
┌─────────────────────────────────────┐
│  commonMain: expect 선언            │  ← "이런 기능이 필요해요"
│  fun getPlatformName(): String      │
└─────────────────────────────────────┘
           ↓         ↓         ↓
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │ Android  │ │   iOS    │ │ Desktop  │
    │ actual   │ │ actual   │ │ actual   │  ← "이렇게 구현할게요"
    └──────────┘ └──────────┘ └──────────┘
```

### 기본 사용법

#### 1. 함수 (Function)

**commonMain/Platform.kt**:
```kotlin
/**
 * 플랫폼 이름을 반환하는 함수 선언
 * expect: 각 플랫폼에서 구현을 제공해야 함
 */
expect fun getPlatformName(): String

/**
 * 앱 버전을 반환하는 함수
 */
expect fun getAppVersion(): String
```

**androidMain/Platform.android.kt**:
```kotlin
import android.os.Build

/**
 * Android 구현
 */
actual fun getPlatformName(): String {
    return "Android ${Build.VERSION.SDK_INT}"
}

actual fun getAppVersion(): String {
    // BuildConfig는 Gradle에서 자동 생성
    return BuildConfig.VERSION_NAME
}
```

**iosMain/Platform.ios.kt**:
```kotlin
import platform.UIKit.UIDevice
import platform.Foundation.NSBundle

/**
 * iOS 구현
 */
actual fun getPlatformName(): String {
    val device = UIDevice.currentDevice
    return "${device.systemName} ${device.systemVersion}"
}

actual fun getAppVersion(): String {
    // Info.plist에서 버전 읽기
    val bundle = NSBundle.mainBundle
    return bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String 
        ?: "Unknown"
}
```

**desktopMain/Platform.desktop.kt**:
```kotlin
/**
 * Desktop 구현
 */
actual fun getPlatformName(): String {
    return "${System.getProperty("os.name")} ${System.getProperty("os.version")}"
}

actual fun getAppVersion(): String {
    // 리소스 파일에서 읽거나 하드코딩
    return "1.0.0"
}
```

#### 2. 클래스 (Class)

**commonMain/Storage.kt**:
```kotlin
/**
 * 키-값 저장소 인터페이스
 * 각 플랫폼의 네이티브 저장소를 사용
 */
expect class KeyValueStorage {
    /**
     * 문자열 값 저장
     * @param key 키
     * @param value 값
     */
    fun putString(key: String, value: String)
    
    /**
     * 문자열 값 읽기
     * @param key 키
     * @return 저장된 값, 없으면 null
     */
    fun getString(key: String): String?
    
    /**
     * 정수 값 저장
     */
    fun putInt(key: String, value: Int)
    
    /**
     * 정수 값 읽기
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * 값 삭제
     */
    fun remove(key: String)
    
    /**
     * 모든 값 삭제
     */
    fun clear()
}
```

**androidMain/Storage.android.kt**:
```kotlin
import android.content.Context
import android.content.SharedPreferences

/**
 * Android SharedPreferences 구현
 */
actual class KeyValueStorage(context: Context) {
    // SharedPreferences 인스턴스
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_prefs",  // 파일 이름
        Context.MODE_PRIVATE
    )
    
    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    actual fun getString(key: String): String? {
        return prefs.getString(key, null)
    }
    
    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    actual fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
    
    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
```

**iosMain/Storage.ios.kt**:
```kotlin
import platform.Foundation.NSUserDefaults

/**
 * iOS NSUserDefaults 구현
 */
actual class KeyValueStorage {
    // NSUserDefaults 인스턴스
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
        userDefaults.synchronize()  // 즉시 저장
    }
    
    actual fun getString(key: String): String? {
        return userDefaults.stringForKey(key)
    }
    
    actual fun putInt(key: String, value: Int) {
        userDefaults.setInteger(value.toLong(), forKey = key)
        userDefaults.synchronize()
    }
    
    actual fun getInt(key: String, defaultValue: Int): Int {
        val value = userDefaults.integerForKey(key)
        return if (value == 0L && !userDefaults.objectForKey(key)) {
            defaultValue  // 값이 없으면 기본값 반환
        } else {
            value.toInt()
        }
    }
    
    actual fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
        userDefaults.synchronize()
    }
    
    actual fun clear() {
        val domain = NSBundle.mainBundle.bundleIdentifier
        if (domain != null) {
            userDefaults.removePersistentDomainForName(domain)
        }
    }
}
```

**desktopMain/Storage.desktop.kt**:
```kotlin
import java.util.prefs.Preferences

/**
 * Desktop Java Preferences 구현
 */
actual class KeyValueStorage {
    // Java Preferences 인스턴스
    private val prefs = Preferences.userRoot().node("com.example.app")
    
    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()  // 디스크에 저장
    }
    
    actual fun getString(key: String): String? {
        return prefs.get(key, null)
    }
    
    actual fun putInt(key: String, value: Int) {
        prefs.putInt(key, value)
        prefs.flush()
    }
    
    actual fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    actual fun remove(key: String) {
        prefs.remove(key)
        prefs.flush()
    }
    
    actual fun clear() {
        prefs.clear()
        prefs.flush()
    }
}
```

#### 3. 인터페이스와 구현 분리 (권장 패턴)

**commonMain/Logger.kt**:
```kotlin
/**
 * 로깅 인터페이스 (공통)
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * 플랫폼별 Logger 인스턴스 생성 함수
 */
expect fun createLogger(): Logger
```

**androidMain/Logger.android.kt**:
```kotlin
import android.util.Log

/**
 * Android Logcat 구현
 */
class AndroidLogger : Logger {
    override fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    override fun info(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    override fun warning(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}

/**
 * Android Logger 생성
 */
actual fun createLogger(): Logger = AndroidLogger()
```

**iosMain/Logger.ios.kt**:
```kotlin
import platform.Foundation.NSLog

/**
 * iOS NSLog 구현
 */
class IosLogger : Logger {
    override fun debug(tag: String, message: String) {
        NSLog("[$tag] DEBUG: $message")
    }
    
    override fun info(tag: String, message: String) {
        NSLog("[$tag] INFO: $message")
    }
    
    override fun warning(tag: String, message: String) {
        NSLog("[$tag] WARNING: $message")
    }
    
    override fun error(tag: String, message: String, throwable: Throwable?) {
        val errorMsg = if (throwable != null) {
            "$message\n${throwable.stackTraceToString()}"
        } else {
            message
        }
        NSLog("[$tag] ERROR: $errorMsg")
    }
}

/**
 * iOS Logger 생성
 */
actual fun createLogger(): Logger = IosLogger()
```

**desktopMain/Logger.desktop.kt**:
```kotlin
/**
 * Desktop Console 구현
 */
class DesktopLogger : Logger {
    override fun debug(tag: String, message: String) {
        println("[$tag] DEBUG: $message")
    }
    
    override fun info(tag: String, message: String) {
        println("[$tag] INFO: $message")
    }
    
    override fun warning(tag: String, message: String) {
        println("[$tag] WARNING: $message")
    }
    
    override fun error(tag: String, message: String, throwable: Throwable?) {
        System.err.println("[$tag] ERROR: $message")
        throwable?.printStackTrace()
    }
}

/**
 * Desktop Logger 생성
 */
actual fun createLogger(): Logger = DesktopLogger()
```

**공통 코드에서 사용**:
```kotlin
// 어디서든 사용 가능
val logger = createLogger()

@Composable
fun MyScreen() {
    LaunchedEffect(Unit) {
        logger.info("MyScreen", "Screen loaded")
    }
    
    Button(onClick = {
        logger.debug("MyScreen", "Button clicked")
    }) {
        Text("Click Me")
    }
}
```

---

## 플랫폼별 코드 작성 전략

### 전략 1: 최소한의 플랫폼 코드

**원칙**: 플랫폼별 코드는 최소화하고, 가능한 한 공통 코드로 작성

```kotlin
// ✅ 좋은 예: 공통 코드로 작성 가능
@Composable
fun UserProfile(user: User) {
    Column {
        Text(user.name)
        Text(user.email)
    }
}

// ❌ 나쁜 예: 불필요하게 플랫폼별로 분리
expect @Composable fun UserProfile(user: User)
```

### 전략 2: 플랫폼 추상화 계층

**commonMain/platform/PlatformContext.kt**:
```kotlin
/**
 * 플랫폼별 컨텍스트를 추상화
 */
expect class PlatformContext

/**
 * 전역 컨텍스트 접근
 */
expect fun getPlatformContext(): PlatformContext
```

**androidMain/platform/PlatformContext.android.kt**:
```kotlin
import android.content.Context

/**
 * Android Context 래퍼
 */
actual typealias PlatformContext = Context

// 전역 변수로 저장 (Application에서 초기화)
private lateinit var applicationContext: Context

fun initPlatformContext(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getPlatformContext(): PlatformContext {
    return applicationContext
}
```

**MainActivity.kt에서 초기화**:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 플랫폼 컨텍스트 초기화
        initPlatformContext(this)
        
        setContent {
            App()
        }
    }
}
```

**iosMain/platform/PlatformContext.ios.kt**:
```kotlin
/**
 * iOS는 컨텍스트가 필요 없으므로 빈 클래스
 */
actual class PlatformContext

actual fun getPlatformContext(): PlatformContext {
    return PlatformContext()
}
```

**desktopMain/platform/PlatformContext.desktop.kt**:
```kotlin
/**
 * Desktop도 컨텍스트가 필요 없음
 */
actual class PlatformContext

actual fun getPlatformContext(): PlatformContext {
    return PlatformContext()
}
```

### 전략 3: 플랫폼별 기능 제공자

**commonMain/platform/PlatformCapabilities.kt**:
```kotlin
/**
 * 플랫폼 기능 제공 인터페이스
 */
interface PlatformCapabilities {
    /**
     * 진동 기능 지원 여부
     */
    val supportsVibration: Boolean
    
    /**
     * 카메라 지원 여부
     */
    val supportsCamera: Boolean
    
    /**
     * 위치 서비스 지원 여부
     */
    val supportsLocation: Boolean
    
    /**
     * 진동 실행
     * @param durationMs 진동 시간 (밀리초)
     */
    fun vibrate(durationMs: Long)
    
    /**
     * 시스템 공유 다이얼로그 표시
     * @param text 공유할 텍스트
     */
    fun shareText(text: String)
    
    /**
     * 브라우저에서 URL 열기
     * @param url 열 URL
     */
    fun openUrl(url: String)
}

/**
 * 플랫폼 기능 제공자 생성
 */
expect fun createPlatformCapabilities(): PlatformCapabilities
```

**androidMain/platform/PlatformCapabilities.android.kt**:
```kotlin
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build

class AndroidPlatformCapabilities(
    private val context: Context
) : PlatformCapabilities {
    
    override val supportsVibration: Boolean = true
    override val supportsCamera: Boolean = true
    override val supportsLocation: Boolean = true
    
    override fun vibrate(durationMs: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) 
                as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
    
    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
    
    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

actual fun createPlatformCapabilities(): PlatformCapabilities {
    return AndroidPlatformCapabilities(getPlatformContext())
}
```

**iosMain/platform/PlatformCapabilities.ios.kt**:
```kotlin
import platform.UIKit.*
import platform.Foundation.NSURL

class IosPlatformCapabilities : PlatformCapabilities {
    
    override val supportsVibration: Boolean = true
    override val supportsCamera: Boolean = true
    override val supportsLocation: Boolean = true
    
    override fun vibrate(durationMs: Long) {
        // iOS는 시스템 햅틱 사용
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyleMedium)
        generator.prepare()
        generator.impactOccurred()
    }
    
    override fun shareText(text: String) {
        val activityController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(
            activityController,
            animated = true,
            completion = null
        )
    }
    
    override fun openUrl(url: String) {
        val nsUrl = NSURL(string = url)
        if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}

actual fun createPlatformCapabilities(): PlatformCapabilities {
    return IosPlatformCapabilities()
}
```

**desktopMain/platform/PlatformCapabilities.desktop.kt**:
```kotlin
import java.awt.Desktop
import java.net.URI

class DesktopPlatformCapabilities : PlatformCapabilities {
    
    override val supportsVibration: Boolean = false  // Desktop은 진동 미지원
    override val supportsCamera: Boolean = false
    override val supportsLocation: Boolean = false
    
    override fun vibrate(durationMs: Long) {
        // Desktop은 진동 미지원
        println("Vibration not supported on Desktop")
    }
    
    override fun shareText(text: String) {
        // 클립보드에 복사
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = java.awt.datatransfer.StringSelection(text)
        clipboard.setContents(stringSelection, null)
        println("Text copied to clipboard: $text")
    }
    
    override fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

actual fun createPlatformCapabilities(): PlatformCapabilities {
    return DesktopPlatformCapabilities()
}
```

**공통 코드에서 사용**:
```kotlin
@Composable
fun ShareButton(text: String) {
    val platform = remember { createPlatformCapabilities() }
    
    Button(onClick = {
        platform.shareText(text)
        
        // 진동 지원 시에만 실행
        if (platform.supportsVibration) {
            platform.vibrate(50)
        }
    }) {
        Text("Share")
    }
}
```

---

## 아키텍처 패턴 (MVVM)

### MVVM 구조

```
┌─────────────────────────────────────┐
│          View (Composable)          │  ← UI 표시
│  - 사용자 입력 받기                   │
│  - ViewModel의 상태 관찰              │
└─────────────────────────────────────┘
                 ↓ ↑
┌─────────────────────────────────────┐
│           ViewModel                 │  ← 비즈니스 로직
│  - UI 상태 관리                       │
│  - 사용자 액션 처리                    │
│  - Model과 통신                       │
└─────────────────────────────────────┘
                 ↓ ↑
┌─────────────────────────────────────┐
│            Model                    │  ← 데이터 계층
│  - 데이터 저장/로드                    │
│  - API 통신                          │
│  - 비즈니스 규칙                       │
└─────────────────────────────────────┘
```

### ViewModel 구현

**commonMain/viewmodel/CounterViewModel.kt**:
```kotlin
import androidx.compose.runtime.*

/**
 * 카운터 화면의 ViewModel
 * 
 * ViewModel의 역할:
 * 1. UI 상태 관리
 * 2. 비즈니스 로직 처리
 * 3. UI 이벤트 처리
 */
class CounterViewModel {
    // ========================================
    // UI 상태 (State)
    // ========================================
    
    /**
     * 현재 카운터 값
     * private set: 외부에서 직접 수정 불가
     */
    var count by mutableStateOf(0)
        private set
    
    /**
     * 증가 단계 (1, 5, 10 등)
     */
    var step by mutableStateOf(1)
        private set
    
    /**
     * 히스토리 목록
     */
    var history by mutableStateOf<List<String>>(emptyList())
        private set
    
    // ========================================
    // 사용자 액션 (Actions)
    // ========================================
    
    /**
     * 카운터 증가
     */
    fun increment() {
        count += step
        addHistory("Incremented by $step to $count")
    }
    
    /**
     * 카운터 감소
     */
    fun decrement() {
        count -= step
        addHistory("Decremented by $step to $count")
    }
    
    /**
     * 카운터 리셋
     */
    fun reset() {
        count = 0
        addHistory("Reset to 0")
    }
    
    /**
     * 증가 단계 변경
     * @param newStep 새로운 단계 값
     */
    fun setStep(newStep: Int) {
        if (newStep > 0) {
            step = newStep
            addHistory("Changed step to $newStep")
        }
    }
    
    /**
     * 히스토리 추가
     */
    private fun addHistory(action: String) {
        history = history + action
        
        // 최대 10개까지만 유지
        if (history.size > 10) {
            history = history.takeLast(10)
        }
    }
    
    /**
     * 히스토리 클리어
     */
    fun clearHistory() {
        history = emptyList()
    }
}
```

**View (Composable)**:
```kotlin
@Composable
fun CounterScreen() {
    // ViewModel 인스턴스 생성 및 유지
    // remember: 리컴포지션 시에도 동일한 인스턴스 유지
    val viewModel = remember { CounterViewModel() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카운터 표시
        Text(
            text = "Count: ${viewModel.count}",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 증가 단계 선택
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 5, 10).forEach { stepValue ->
                FilterChip(
                    selected = viewModel.step == stepValue,
                    onClick = { viewModel.setStep(stepValue) },
                    label = { Text("Step: $stepValue") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 버튼들
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.decrement() }) {
                Text("-")
            }
            
            Button(onClick = { viewModel.reset() }) {
                Text("Reset")
            }
            
            Button(onClick = { viewModel.increment() }) {
                Text("+")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 히스토리
        Text(
            text = "History",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(viewModel.history.reversed()) { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        if (viewModel.history.isNotEmpty()) {
            Button(onClick = { viewModel.clearHistory() }) {
                Text("Clear History")
            }
        }
    }
}
```

### 비동기 작업 처리

**ViewModel with Coroutines**:
```kotlin
import kotlinx.coroutines.*

/**
 * 비동기 작업을 처리하는 ViewModel
 */
class AsyncViewModel {
    // ViewModel의 Coroutine Scope
    // ViewModel이 파괴될 때 자동으로 취소됨
    private val viewModelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )
    
    // UI 상태
    var isLoading by mutableStateOf(false)
        private set
    
    var data by mutableStateOf<String?>(null)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set
    
    /**
     * 데이터 로드 (비동기)
     */
    fun loadData() {
        viewModelScope.launch {
            // 로딩 시작
            isLoading = true
            error = null
            
            try {
                // 백그라운드 작업 (예: API 호출)
                val result = withContext(Dispatchers.Default) {
                    delay(2000)  // 네트워크 지연 시뮬레이션
                    "Loaded data at ${System.currentTimeMillis()}"
                }
                
                // 성공
                data = result
            } catch (e: Exception) {
                // 에러 처리
                error = e.message ?: "Unknown error"
            } finally {
                // 로딩 종료
                isLoading = false
            }
        }
    }
    
    /**
     * ViewModel 정리
     * 화면이 파괴될 때 호출
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}
```

**View**:
```kotlin
@Composable
fun AsyncScreen() {
    val viewModel = remember { AsyncViewModel() }
    
    // 화면이 파괴될 때 ViewModel 정리
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            // 로딩 중
            viewModel.isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...")
            }
            
            // 에러 발생
            viewModel.error != null -> {
                Text(
                    text = "Error: ${viewModel.error}",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadData() }) {
                    Text("Retry")
                }
            }
            
            // 데이터 표시
            viewModel.data != null -> {
                Text(viewModel.data!!)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadData() }) {
                    Text("Reload")
                }
            }
            
            // 초기 상태
            else -> {
                Button(onClick = { viewModel.loadData() }) {
                    Text("Load Data")
                }
            }
        }
    }
}
```

---

## 상태 관리

### State Hoisting (상태 끌어올리기)

**나쁜 예 - 상태가 분산됨**:
```kotlin
@Composable
fun BadExample() {
    // ❌ 각 컴포넌트가 자체 상태를 가짐
    Column {
        NameInput()  // 내부에 name 상태
        GreetingDisplay()  // name에 접근 불가!
    }
}
```

**좋은 예 - 상태를 상위로 끌어올림**:
```kotlin
@Composable
fun GoodExample() {
    // ✅ 상태를 상위에서 관리
    var name by remember { mutableStateOf("") }
    
    Column {
        NameInput(
            value = name,
            onValueChange = { name = it }
        )
        GreetingDisplay(name = name)
    }
}

/**
 * Stateless 컴포넌트 - 상태를 받아서 표시만 함
 */
@Composable
fun NameInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Name") }
    )
}

@Composable
fun GreetingDisplay(name: String) {
    if (name.isNotBlank()) {
        Text("Hello, $name!")
    }
}
```

### 복잡한 상태 관리

**UI State 클래스**:
```kotlin
/**
 * 사용자 프로필 화면의 UI 상태
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isEditing: Boolean = false
) {
    /**
     * 로딩 중인지 여부
     */
    val showLoading: Boolean
        get() = isLoading && user == null
    
    /**
     * 콘텐츠를 표시할지 여부
     */
    val showContent: Boolean
        get() = !isLoading && user != null && error == null
    
    /**
     * 에러를 표시할지 여부
     */
    val showError: Boolean
        get() = error != null
}

/**
 * 사용자 데이터 모델
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val bio: String
)
```

**ViewModel**:
```kotlin
class ProfileViewModel {
    // 단일 상태 객체로 관리
    var uiState by mutableStateOf(ProfileUiState())
        private set
    
    /**
     * 사용자 프로필 로드
     */
    fun loadProfile(userId: String) {
        // 로딩 시작
        uiState = uiState.copy(isLoading = true, error = null)
        
        // TODO: 실제 API 호출
        // 여기서는 시뮬레이션
        val user = User(
            id = userId,
            name = "John Doe",
            email = "john@example.com",
            bio = "Software Developer"
        )
        
        uiState = uiState.copy(
            isLoading = false,
            user = user
        )
    }
    
    /**
     * 편집 모드 토글
     */
    fun toggleEditMode() {
        uiState = uiState.copy(isEditing = !uiState.isEditing)
    }
    
    /**
     * 프로필 업데이트
     */
    fun updateProfile(name: String, bio: String) {
        val currentUser = uiState.user ?: return
        
        uiState = uiState.copy(isLoading = true)
        
        // TODO: API 호출
        val updatedUser = currentUser.copy(
            name = name,
            bio = bio
        )
        
        uiState = uiState.copy(
            isLoading = false,
            user = updatedUser,
            isEditing = false
        )
    }
}
```

**View**:
```kotlin
@Composable
fun ProfileScreen(userId: String) {
    val viewModel = remember { ProfileViewModel() }
    val state = viewModel.uiState
    
    // 초기 로드
    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.showLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            state.showError -> {
                ErrorView(
                    message = state.error!!,
                    onRetry = { viewModel.loadProfile(userId) }
                )
            }
            
            state.showContent -> {
                if (state.isEditing) {
                    ProfileEditView(
                        user = state.user!!,
                        onSave = { name, bio ->
                            viewModel.updateProfile(name, bio)
                        },
                        onCancel = { viewModel.toggleEditMode() }
                    )
                } else {
                    ProfileDisplayView(
                        user = state.user!!,
                        onEdit = { viewModel.toggleEditMode() }
                    )
                }
            }
        }
    }
}
```

---

## 의존성 주입

### 수동 의존성 주입 (Simple DI)

**commonMain/di/AppContainer.kt**:
```kotlin
/**
 * 앱의 의존성 컨테이너
 * 모든 의존성을 중앙에서 관리
 */
class AppContainer {
    // 싱글톤 인스턴스들
    val logger: Logger by lazy { createLogger() }
    val storage: KeyValueStorage by lazy { createStorage() }
    val platformCapabilities: PlatformCapabilities by lazy { createPlatformCapabilities() }
    
    // Repository
    val userRepository: UserRepository by lazy {
        UserRepository(storage, logger)
    }
    
    // ViewModel Factory
    fun createUserViewModel(): UserViewModel {
        return UserViewModel(userRepository, logger)
    }
}

/**
 * 전역 컨테이너 인스턴스
 */
lateinit var appContainer: AppContainer

/**
 * 앱 초기화 시 호출
 */
fun initializeApp() {
    appContainer = AppContainer()
}
```

**사용 예시**:
```kotlin
@Composable
fun UserScreen() {
    // 컨테이너에서 ViewModel 생성
    val viewModel = remember { appContainer.createUserViewModel() }
    
    // UI 구현...
}
```

---

## 실전 예제: 멀티플랫폼 설정 관리

완전한 설정 관리 시스템을 구현해보겠습니다.

**commonMain/settings/AppSettings.kt**:
```kotlin
/**
 * 앱 설정 관리 클래스
 */
class AppSettings(
    private val storage: KeyValueStorage,
    private val logger: Logger
) {
    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_FONT_SIZE = "font_size"
    }
    
    /**
     * 테마 설정
     */
    var theme: Theme
        get() {
            val value = storage.getString(KEY_THEME) ?: Theme.SYSTEM.name
            return try {
                Theme.valueOf(value)
            } catch (e: Exception) {
                logger.warning("AppSettings", "Invalid theme value: $value")
                Theme.SYSTEM
            }
        }
        set(value) {
            storage.putString(KEY_THEME, value.name)
            logger.info("AppSettings", "Theme changed to $value")
        }
    
    /**
     * 언어 설정
     */
    var language: String
        get() = storage.getString(KEY_LANGUAGE) ?: "en"
        set(value) {
            storage.putString(KEY_LANGUAGE, value)
            logger.info("AppSettings", "Language changed to $value")
        }
    
    /**
     * 알림 활성화 여부
     */
    var notificationsEnabled: Boolean
        get() = storage.getInt(KEY_NOTIFICATIONS, 1) == 1
        set(value) {
            storage.putInt(KEY_NOTIFICATIONS, if (value) 1 else 0)
            logger.info("AppSettings", "Notifications ${if (value) "enabled" else "disabled"}")
        }
    
    /**
     * 폰트 크기
     */
    var fontSize: FontSize
        get() {
            val value = storage.getInt(KEY_FONT_SIZE, FontSize.MEDIUM.value)
            return FontSize.fromValue(value)
        }
        set(value) {
            storage.putInt(KEY_FONT_SIZE, value.value)
            logger.info("AppSettings", "Font size changed to $value")
        }
    
    /**
     * 모든 설정 초기화
     */
    fun resetToDefaults() {
        theme = Theme.SYSTEM
        language = "en"
        notificationsEnabled = true
        fontSize = FontSize.MEDIUM
        logger.info("AppSettings", "Settings reset to defaults")
    }
}

/**
 * 테마 열거형
 */
enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * 폰트 크기 열거형
 */
enum class FontSize(val value: Int, val scale: Float) {
    SMALL(0, 0.85f),
    MEDIUM(1, 1.0f),
    LARGE(2, 1.15f),
    EXTRA_LARGE(3, 1.3f);
    
    companion object {
        fun fromValue(value: Int): FontSize {
            return values().find { it.value == value } ?: MEDIUM
        }
    }
}
```

**ViewModel**:
```kotlin
class SettingsViewModel(
    private val appSettings: AppSettings
) {
    // UI 상태
    var theme by mutableStateOf(appSettings.theme)
        private set
    
    var language by mutableStateOf(appSettings.language)
        private set
    
    var notificationsEnabled by mutableStateOf(appSettings.notificationsEnabled)
        private set
    
    var fontSize by mutableStateOf(appSettings.fontSize)
        private set
    
    /**
     * 테마 변경
     */
    fun setTheme(newTheme: Theme) {
        theme = newTheme
        appSettings.theme = newTheme
    }
    
    /**
     * 언어 변경
     */
    fun setLanguage(newLanguage: String) {
        language = newLanguage
        appSettings.language = newLanguage
    }
    
    /**
     * 알림 토글
     */
    fun toggleNotifications() {
        notificationsEnabled = !notificationsEnabled
        appSettings.notificationsEnabled = notificationsEnabled
    }
    
    /**
     * 폰트 크기 변경
     */
    fun setFontSize(newSize: FontSize) {
        fontSize = newSize
        appSettings.fontSize = newSize
    }
    
    /**
     * 초기화
     */
    fun resetSettings() {
        appSettings.resetToDefaults()
        theme = appSettings.theme
        language = appSettings.language
        notificationsEnabled = appSettings.notificationsEnabled
        fontSize = appSettings.fontSize
    }
}
```

**View**:
```kotlin
@Composable
fun SettingsScreen() {
    val viewModel = remember {
        SettingsViewModel(appContainer.appSettings)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 테마 설정
        SettingSection(title = "Appearance") {
            SettingItem(title = "Theme") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Theme.values().forEach { themeOption ->
                        FilterChip(
                            selected = viewModel.theme == themeOption,
                            onClick = { viewModel.setTheme(themeOption) },
                            label = { Text(themeOption.name) }
                        )
                    }
                }
            }
            
            SettingItem(title = "Font Size") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FontSize.values().forEach { sizeOption ->
                        FilterChip(
                            selected = viewModel.fontSize == sizeOption,
                            onClick = { viewModel.setFontSize(sizeOption) },
                            label = { Text(sizeOption.name) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 알림 설정
        SettingSection(title = "Notifications") {
            SettingItem(title = "Enable Notifications") {
                Switch(
                    checked = viewModel.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 언어 설정
        SettingSection(title = "Language") {
            SettingItem(title = "App Language") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("en" to "English", "ko" to "한국어", "ja" to "日本語").forEach { (code, name) ->
                        FilterChip(
                            selected = viewModel.language == code,
                            onClick = { viewModel.setLanguage(code) },
                            label = { Text(name) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 초기화 버튼
        OutlinedButton(
            onClick = { viewModel.resetSettings() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Defaults")
        }
    }
}

@Composable
fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingItem(
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        content()
    }
}
```

---

## 다음 단계

다음 문서에서는:
- **UI 컴포넌트 심화**
- **커스텀 컴포넌트 작성**
- **애니메이션**
- **제스처 처리**

를 다룹니다.

## 참고 자료

- [Kotlin Multiplatform 공식 문서](https://kotlinlang.org/docs/multiplatform.html)
- [expect/actual 메커니즘](https://kotlinlang.org/docs/multiplatform-connect-to-apis.html)
- [MVVM 패턴](https://developer.android.com/topic/architecture)
