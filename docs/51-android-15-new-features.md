# Android 15 (API 35) 새로운 기능 가이드

## 목차
1. [Android 15 개요](#android-15-개요)
2. [개인정보 보호 샌드박스](#개인정보-보호-샌드박스)
3. [향상된 카메라 및 미디어](#향상된-카메라-및-미디어)
4. [대형 화면 최적화](#대형-화면-최적화)
5. [성능 개선](#성능-개선)
6. [보안 강화](#보안-강화)
7. [개발자 생산성](#개발자-생산성)
8. [앱 아카이빙](#앱-아카이빙)
9. [부분 화면 공유](#부분-화면-공유)
10. [위성 연결 지원](#위성-연결-지원)

---

## Android 15 개요

**Android 15 (Vanilla Ice Cream, API 35)**는 2024년에 출시되었으며, 개인정보 보호, 대형 화면 지원, 성능을 더욱 강화한 버전입니다.

### 주요 변경사항 요약

| 카테고리 | 주요 기능 |
|---------|----------|
| **개인정보 보호** | 개인정보 보호 샌드박스, 부분 화면 공유 |
| **미디어** | 향상된 카메라 기능, HDR 헤드룸 제어 |
| **대형 화면** | 앱 아카이빙, 향상된 멀티태스킹 |
| **성능** | 16KB 페이지 크기 지원, ANGLE 기본 활성화 |
| **연결성** | 위성 연결, NFC 개선 |

### targetSdkVersion 설정

```kotlin
// build.gradle.kts (Module: app)
android {
    compileSdk = 35
    
    defaultConfig {
        targetSdk = 35  // Android 15를 타겟으로 설정
        minSdk = 21
    }
}
```

---

## 개인정보 보호 샌드박스

### 개요

**Android 15부터 개인정보 보호 샌드박스가 도입되어 사용자 추적을 제한합니다.**

광고 ID 대신 개인정보를 보호하는 새로운 API를 사용해야 합니다.

### Topics API

```kotlin
import android.adservices.topics.TopicsManager
import android.adservices.topics.GetTopicsRequest

/**
 * Topics API를 사용한 관심사 기반 광고
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
suspend fun getTopics(context: Context): List<Topic> {
    val topicsManager = context.getSystemService(TopicsManager::class.java)
    
    val request = GetTopicsRequest.Builder()
        .setAdsSdkName("com.example.sdk")
        .build()
    
    return try {
        val response = topicsManager.getTopics(request)
        response.topics
    } catch (e: Exception) {
        emptyList()
    }
}
```

### Attribution Reporting API

```kotlin
/**
 * 광고 전환 추적 (개인정보 보호)
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun trackConversion(context: Context, conversionData: ConversionData) {
    // 개인정보를 보호하면서 광고 효과 측정
    // 구현은 Privacy Sandbox SDK 사용
}
```

---

## 향상된 카메라 및 미디어

### 1. 저조도 부스트

```kotlin
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest

/**
 * 저조도 부스트 기능 사용
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun enableLowLightBoost(captureRequestBuilder: CaptureRequest.Builder) {
    // 저조도 환경에서 자동으로 밝기 향상
    captureRequestBuilder.set(
        CaptureRequest.CONTROL_LOW_LIGHT_BOOST_MODE,
        CaptureRequest.CONTROL_LOW_LIGHT_BOOST_MODE_ON
    )
}

/**
 * 저조도 부스트 지원 확인
 */
fun isLowLightBoostSupported(characteristics: CameraCharacteristics): Boolean {
    val modes = characteristics.get(
        CameraCharacteristics.CONTROL_AVAILABLE_LOW_LIGHT_BOOST_MODES
    )
    return modes?.contains(CaptureRequest.CONTROL_LOW_LIGHT_BOOST_MODE_ON) == true
}
```

### 2. HDR 헤드룸 제어

```kotlin
import android.view.Display

/**
 * HDR 헤드룸 제어
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun HDRHeadroomControl() {
    val context = LocalContext.current
    val display = (context as? Activity)?.display
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "HDR 헤드룸 설정",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // HDR 헤드룸 값 (1.0 ~ 10000.0)
        var headroom by remember { mutableStateOf(1.0f) }
        
        Slider(
            value = headroom,
            onValueChange = { headroom = it },
            valueRange = 1.0f..10.0f
        )
        
        Text("헤드룸: ${headroom.toInt()}")
        
        Button(onClick = {
            display?.setHdrSdrRatio(headroom)
        }) {
            Text("적용")
        }
    }
}
```

### 3. 인앱 카메라 컨트롤

```kotlin
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector

/**
 * 향상된 카메라 컨트롤
 */
@Composable
fun EnhancedCameraControls() {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var zoomRatio by remember { mutableStateOf(1.0f) }
    var exposureCompensation by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 줌 컨트롤
        Text("줌: ${String.format("%.1f", zoomRatio)}x")
        Slider(
            value = zoomRatio,
            onValueChange = { zoomRatio = it },
            valueRange = 1.0f..10.0f
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 노출 보정
        Text("노출 보정: $exposureCompensation")
        Slider(
            value = exposureCompensation.toFloat(),
            onValueChange = { exposureCompensation = it.toInt() },
            valueRange = -2f..2f,
            steps = 3
        )
    }
}
```

---

## 대형 화면 최적화

### 1. 향상된 멀티태스킹

```kotlin
/**
 * 멀티 윈도우 모드 최적화
 */
@Composable
fun MultiWindowOptimization() {
    val configuration = LocalConfiguration.current
    val isInMultiWindowMode = remember(configuration) {
        configuration.screenWidthDp < 600
    }
    
    if (isInMultiWindowMode) {
        // 멀티 윈도우 모드용 컴팩트 레이아웃
        CompactLayout()
    } else {
        // 전체 화면용 확장 레이아웃
        ExpandedLayout()
    }
}

@Composable
fun CompactLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        // 세로 레이아웃
    }
}

@Composable
fun ExpandedLayout() {
    Row(modifier = Modifier.fillMaxSize()) {
        // 가로 레이아웃 (사이드바 + 메인)
    }
}
```

### 2. 폴더블 기기 지원

```kotlin
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.FoldingFeature

/**
 * 폴더블 기기 대응
 */
@Composable
fun FoldableDeviceSupport() {
    val context = LocalContext.current
    val windowInfoTracker = remember { WindowInfoTracker.getOrCreate(context) }
    
    val windowLayoutInfo by windowInfoTracker.windowLayoutInfo(context)
        .collectAsState(initial = null)
    
    val foldingFeature = windowLayoutInfo?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull()
    
    when (foldingFeature?.state) {
        FoldingFeature.State.HALF_OPENED -> {
            // 반쯤 접힌 상태 (Flex Mode)
            FlexModeLayout()
        }
        FoldingFeature.State.FLAT -> {
            // 완전히 펼쳐진 상태
            FlatLayout()
        }
        else -> {
            // 일반 레이아웃
            NormalLayout()
        }
    }
}

@Composable
fun FlexModeLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단: 비디오 재생
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text("비디오 영역")
        }
        
        // 하단: 컨트롤
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text("컨트롤 영역")
        }
    }
}
```

---

## 성능 개선

### 1. 16KB 페이지 크기 지원

```kotlin
/**
 * 16KB 페이지 크기 최적화
 * 
 * Android 15부터 일부 기기에서 16KB 페이지 크기 사용
 * 메모리 정렬 및 할당 최적화 필요
 */

// build.gradle.kts
android {
    defaultConfig {
        // 16KB 페이지 크기 지원
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }
}
```

### 2. ANGLE 기본 활성화

```kotlin
/**
 * ANGLE (OpenGL ES on Vulkan)
 * 
 * Android 15부터 ANGLE이 기본적으로 활성화됨
 * OpenGL ES 앱의 성능과 호환성 향상
 */

// AndroidManifest.xml
<application
    android:usesCleartextTraffic="false">
    
    <!-- ANGLE 사용 (기본값) -->
    <meta-data
        android:name="com.android.graphics.internalformat.angle"
        android:value="true"/>
</application>
```

---

## 보안 강화

### 1. 향상된 파일 무결성

```kotlin
import java.security.MessageDigest

/**
 * 파일 무결성 검증
 */
fun verifyFileIntegrity(file: File, expectedHash: String): Boolean {
    val digest = MessageDigest.getInstance("SHA-256")
    val fileBytes = file.readBytes()
    val hash = digest.digest(fileBytes)
    val hashString = hash.joinToString("") { "%02x".format(it) }
    
    return hashString == expectedHash
}
```

### 2. 향상된 생체 인증

```kotlin
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

/**
 * 생체 인증 (Android 15 향상)
 */
@Composable
fun BiometricAuthExample() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    
    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // 인증 성공
                }
                
                override fun onAuthenticationFailed() {
                    // 인증 실패
                }
            }
        )
    }
    
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("생체 인증")
        .setSubtitle("지문 또는 얼굴 인식으로 로그인")
        .setNegativeButtonText("취소")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    
    Button(onClick = {
        biometricPrompt.authenticate(promptInfo)
    }) {
        Text("생체 인증 시작")
    }
}
```

---

## 개발자 생산성

### 1. 향상된 로깅

```kotlin
import android.util.Log

/**
 * 구조화된 로깅
 */
fun structuredLogging() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        // 향상된 로깅 API
        Log.d("MyApp", "User action", mapOf(
            "action" to "button_click",
            "screen" to "home",
            "timestamp" to System.currentTimeMillis()
        ))
    } else {
        // 기존 로깅
        Log.d("MyApp", "User action: button_click")
    }
}
```

### 2. 개선된 프로파일링

```kotlin
import android.os.Trace

/**
 * 성능 프로파일링
 */
fun performanceTracing() {
    Trace.beginSection("MyOperation")
    try {
        // 측정할 작업
        expensiveOperation()
    } finally {
        Trace.endSection()
    }
}
```

---

## 앱 아카이빙

### 개요

**Android 15부터 사용하지 않는 앱을 자동으로 아카이빙하여 저장 공간을 절약합니다.**

### 구현

```kotlin
/**
 * 앱 아카이빙 지원
 * 
 * Google Play에서 자동으로 처리
 * 별도 구현 불필요
 */

// build.gradle.kts
android {
    bundle {
        // 앱 아카이빙 활성화
        enableArchiving = true
    }
}
```

### 아카이빙 상태 확인

```kotlin
import android.content.pm.PackageManager

/**
 * 앱이 아카이빙되었는지 확인
 */
fun isAppArchived(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        packageInfo.applicationInfo.flags and 
            ApplicationInfo.FLAG_ARCHIVED != 0
    } else {
        false
    }
}
```

---

## 부분 화면 공유

### 개요

**Android 15부터 전체 화면 대신 특정 앱 창만 공유할 수 있습니다.**

### 구현

```kotlin
import android.media.projection.MediaProjectionManager

/**
 * 부분 화면 공유
 */
@Composable
fun PartialScreenSharing() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    
    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) 
            as MediaProjectionManager
    }
    
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // 화면 공유 시작
        }
    }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                // Android 15+ - 부분 화면 공유
                val intent = mediaProjectionManager.createScreenCaptureIntent(
                    MediaProjectionManager.ScreenCaptureMode.SINGLE_APP
                )
                screenCaptureLauncher.launch(intent)
            } else {
                // Android 14 이하 - 전체 화면 공유
                val intent = mediaProjectionManager.createScreenCaptureIntent()
                screenCaptureLauncher.launch(intent)
            }
        }) {
            Text("화면 공유 시작")
        }
    }
}
```

---

## 위성 연결 지원

### 개요

**Android 15부터 위성 연결을 지원하여 오지에서도 통신이 가능합니다.**

### 구현

```kotlin
import android.telephony.TelephonyManager
import android.telephony.satellite.SatelliteManager

/**
 * 위성 연결 상태 확인
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun checkSatelliteConnection(context: Context): Boolean {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) 
        as TelephonyManager
    
    return telephonyManager.isSatelliteConnected
}

/**
 * 위성 메시지 전송
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
suspend fun sendSatelliteMessage(
    context: Context,
    message: String
): Boolean {
    val satelliteManager = context.getSystemService(Context.SATELLITE_SERVICE) 
        as SatelliteManager
    
    return try {
        satelliteManager.sendMessage(message.toByteArray())
        true
    } catch (e: Exception) {
        false
    }
}
```

---

## NFC 개선

### 개요

**Android 15에서 NFC 기능이 향상되었습니다.**

### 구현

```kotlin
import android.nfc.NfcAdapter
import android.nfc.Tag

/**
 * 향상된 NFC 읽기
 */
@Composable
fun EnhancedNFCReader() {
    val context = LocalContext.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        
        val callback = NfcAdapter.ReaderCallback { tag ->
            // NFC 태그 읽기
            handleNFCTag(tag)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Android 15+ - 향상된 NFC API
            nfcAdapter?.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V,
                null
            )
        }
        
        onDispose {
            nfcAdapter?.disableReaderMode(activity)
        }
    }
}

fun handleNFCTag(tag: Tag) {
    // NFC 태그 처리
}
```

---

## 마이그레이션 체크리스트

Android 15를 타겟으로 하는 앱을 업데이트할 때 확인해야 할 사항:

- [ ] 개인정보 보호 샌드박스 API 마이그레이션
- [ ] 16KB 페이지 크기 지원 확인
- [ ] 대형 화면 및 폴더블 기기 최적화
- [ ] 부분 화면 공유 지원 (선택사항)
- [ ] 앱 아카이빙 활성화
- [ ] 향상된 카메라 기능 활용 (선택사항)
- [ ] 위성 연결 지원 (해당 시)

---

## 버전별 주요 기능 비교

| 기능 | Android 13 | Android 14 | Android 15 |
|------|-----------|-----------|-----------|
| **알림 권한** | 런타임 권한 | - | - |
| **미디어 권한** | 세분화 | 선택적 접근 | - |
| **화면 공유** | - | - | 부분 공유 |
| **카메라** | - | - | 저조도 부스트 |
| **대형 화면** | - | 개선 | 폴더블 최적화 |
| **성능** | - | - | 16KB 페이지 |
| **개인정보** | - | - | 샌드박스 |

---

## 참고 자료

- [Android 15 공식 문서](https://developer.android.com/about/versions/15)
- [Android 15 동작 변경사항](https://developer.android.com/about/versions/15/behavior-changes-15)
- [Android 15 새로운 기능](https://developer.android.com/about/versions/15/features)
- [개인정보 보호 샌드박스](https://developer.android.com/design-for-safety/privacy-sandbox)
