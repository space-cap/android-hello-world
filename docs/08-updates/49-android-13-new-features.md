# Android 13 (API 33) 새로운 기능 가이드

## 목차
1. [Android 13 개요](#android-13-개요)
2. [런타임 알림 권한](#런타임-알림-권한)
3. [세분화된 미디어 권한](#세분화된-미디어-권한)
4. [테마별 앱 아이콘](#테마별-앱-아이콘)
5. [예측 백 제스처](#예측-백-제스처)
6. [클립보드 미리보기](#클립보드-미리보기)
7. [향상된 다국어 지원](#향상된-다국어-지원)
8. [포토 피커](#포토-피커)
9. [근처 Wi-Fi 기기 권한](#근처-wi-fi-기기-권한)
10. [프로그래밍 방식 인텐트 필터](#프로그래밍-방식-인텐트-필터)

---

## Android 13 개요

**Android 13 (Tiramisu, API 33)**은 2022년 8월에 출시되었으며, 개인정보 보호와 보안을 강화하고 사용자 경험을 개선하는 데 중점을 둔 버전입니다.

### 주요 변경사항 요약

| 카테고리 | 주요 기능 |
|---------|----------|
| **개인정보 보호** | 런타임 알림 권한, 세분화된 미디어 권한 |
| **사용자 경험** | 테마별 앱 아이콘, 예측 백 제스처 |
| **미디어** | 포토 피커, 향상된 미디어 컨트롤 |
| **다국어** | 앱별 언어 설정 |
| **연결성** | 근처 Wi-Fi 기기 권한 |

### targetSdkVersion 설정

```kotlin
// build.gradle.kts (Module: app)
android {
    compileSdk = 33
    
    defaultConfig {
        targetSdk = 33  // Android 13을 타겟으로 설정
        minSdk = 21
    }
}
```

---

## 런타임 알림 권한

### 개요

**Android 13부터 알림을 표시하려면 사용자의 명시적인 권한이 필요합니다.**

이전 버전에서는 앱을 설치하면 자동으로 알림 권한이 부여되었지만, Android 13부터는 런타임에 권한을 요청해야 합니다.

### 왜 변경되었나요?

- **사용자 제어 강화**: 사용자가 원하지 않는 알림을 받지 않도록 보호
- **스팸 방지**: 불필요한 알림을 보내는 앱 차단
- **개인정보 보호**: 알림을 통한 정보 노출 방지

### 권한 선언

**AndroidManifest.xml**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Android 13 이상에서 알림 권한 필요 -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    
    <application>
        <!-- ... -->
    </application>
</manifest>
```

### 권한 요청 구현

#### 1. 기본 구현 (Jetpack Compose)

```kotlin
import android.Manifest
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.material3.*
import com.google.accompanist.permissions.*

/**
 * 알림 권한 요청 화면
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionScreen() {
    // Android 13 이상에서만 권한 요청
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // 알림 권한 상태
        val notificationPermissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                // 권한이 이미 부여됨
                notificationPermissionState.status.isGranted -> {
                    Text("✅ 알림 권한이 부여되었습니다")
                    
                    Button(onClick = { sendNotification() }) {
                        Text("테스트 알림 보내기")
                    }
                }
                
                // 권한이 거부되었고, 다시 요청할 수 있음
                notificationPermissionState.status.shouldShowRationale -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "알림을 받으려면 권한이 필요합니다",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "중요한 업데이트와 메시지를 놓치지 마세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = {
                            notificationPermissionState.launchPermissionRequest()
                        }) {
                            Text("권한 허용하기")
                        }
                    }
                }
                
                // 권한이 거부됨 (처음 요청)
                else -> {
                    Button(onClick = {
                        notificationPermissionState.launchPermissionRequest()
                    }) {
                        Text("알림 권한 요청")
                    }
                }
            }
        }
    } else {
        // Android 12 이하에서는 권한 요청 불필요
        Text("알림 권한이 자동으로 부여됩니다 (Android 12 이하)")
    }
}

/**
 * 테스트 알림 전송
 */
fun sendNotification() {
    // NotificationManager를 사용하여 알림 전송
    // (구현 생략)
}
```

#### 2. 권한 요청 타이밍

```kotlin
/**
 * 앱 시작 시 권한 요청
 */
@Composable
fun App() {
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Android 13 이상에서만 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 권한이 없으면 다이얼로그 표시
            if (!hasNotificationPermission()) {
                showPermissionDialog = true
            }
        }
    }
    
    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onDismiss = { showPermissionDialog = false }
        )
    }
    
    // 메인 콘텐츠
    MainScreen()
}

/**
 * 알림 권한 확인
 */
fun hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true  // Android 12 이하는 자동 부여
    }
}
```

#### 3. 권한 요청 다이얼로그

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit
) {
    val permissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text("알림 권한 필요")
        },
        text = {
            Text(
                "이 앱은 다음과 같은 경우 알림을 보냅니다:\n" +
                "• 새로운 메시지 도착\n" +
                "• 중요한 업데이트\n" +
                "• 작업 완료 알림"
            )
        },
        confirmButton = {
            TextButton(onClick = {
                permissionState.launchPermissionRequest()
                onDismiss()
            }) {
                Text("허용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("나중에")
            }
        }
    )
}
```

### 베스트 프랙티스

#### 1. 적절한 타이밍에 요청

```kotlin
// ❌ 나쁜 예: 앱 시작 즉시 요청
@Composable
fun BadExample() {
    LaunchedEffect(Unit) {
        requestNotificationPermission()  // 사용자가 이유를 모름
    }
}

// ✅ 좋은 예: 사용자가 기능을 사용하려 할 때 요청
@Composable
fun GoodExample() {
    Button(onClick = {
        // 사용자가 알림 설정을 켜려고 할 때 권한 요청
        if (!hasNotificationPermission()) {
            requestNotificationPermission()
        } else {
            enableNotifications()
        }
    }) {
        Text("알림 켜기")
    }
}
```

#### 2. 권한 거부 시 대응

```kotlin
@Composable
fun HandlePermissionDenied() {
    val context = LocalContext.current
    
    Column {
        Text("알림 권한이 거부되었습니다")
        
        Button(onClick = {
            // 설정 화면으로 이동
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }) {
            Text("설정에서 권한 허용하기")
        }
    }
}
```

---

## 세분화된 미디어 권한

### 개요

**Android 13부터 미디어 파일 접근 권한이 세분화되었습니다.**

이전에는 `READ_EXTERNAL_STORAGE` 하나의 권한으로 모든 미디어에 접근했지만, 이제는 파일 유형별로 권한을 요청해야 합니다.

### 새로운 권한

| 권한 | 접근 가능한 파일 |
|------|----------------|
| `READ_MEDIA_IMAGES` | 이미지 파일 (.jpg, .png 등) |
| `READ_MEDIA_VIDEO` | 비디오 파일 (.mp4, .avi 등) |
| `READ_MEDIA_AUDIO` | 오디오 파일 (.mp3, .wav 등) |

### 권한 선언

**AndroidManifest.xml**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Android 13 이상 -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO"/>
    
    <!-- Android 12 이하 호환성 -->
    <uses-permission 
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32"/>
    
    <application>
        <!-- ... -->
    </application>
</manifest>
```

### 권한 요청 구현

```kotlin
import android.Manifest
import android.os.Build

/**
 * 이미지 권한 요청
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePermissionExample() {
    // Android 버전에 따라 다른 권한 요청
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES  // Android 13+
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE  // Android 12 이하
    }
    
    val permissionState = rememberPermissionState(permission)
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        when {
            permissionState.status.isGranted -> {
                Text("✅ 이미지 접근 권한 부여됨")
                
                // 이미지 선택 버튼
                Button(onClick = { pickImage() }) {
                    Text("이미지 선택")
                }
            }
            
            else -> {
                Button(onClick = {
                    permissionState.launchPermissionRequest()
                }) {
                    Text("이미지 권한 요청")
                }
            }
        }
    }
}

/**
 * 여러 미디어 권한 한번에 요청
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultipleMediaPermissions() {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        when {
            permissionsState.allPermissionsGranted -> {
                Text("✅ 모든 미디어 권한 부여됨")
            }
            
            permissionsState.shouldShowRationale -> {
                Text("미디어 파일에 접근하려면 권한이 필요합니다")
                Button(onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("권한 허용")
                }
            }
            
            else -> {
                Button(onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("미디어 권한 요청")
                }
            }
        }
    }
}
```

### 마이그레이션 가이드

```kotlin
/**
 * Android 버전별 호환성을 고려한 권한 요청
 */
class MediaPermissionHelper(private val context: Context) {
    
    /**
     * 이미지 권한 확인
     */
    fun hasImagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 이하
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 비디오 권한 확인
     */
    fun hasVideoPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 오디오 권한 확인
     */
    fun hasAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
```

---

## 테마별 앱 아이콘

### 개요

**Android 13부터 앱 아이콘이 시스템 테마(라이트/다크)에 따라 자동으로 변경될 수 있습니다.**

### 구현 방법

#### 1. 모노크롬 아이콘 추가

**res/drawable/ic_launcher_monochrome.xml**:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- 단색 아이콘 경로 -->
    <path
        android:fillColor="#000000"
        android:pathData="M50,50m-48,0a48,48 0,1,1 96,0a48,48 0,1,1 -96,0"/>
</vector>
```

#### 2. AndroidManifest.xml 설정

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round">
    
    <!-- 테마별 아이콘 활성화 -->
    <meta-data
        android:name="android.app.ICON_THEME"
        android:resource="@mipmap/ic_launcher_monochrome"/>
    
</application>
```

#### 3. 적응형 아이콘 설정

**res/mipmap-anydpi-v26/ic_launcher.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    
    <!-- 모노크롬 레이어 추가 (Android 13+) -->
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```

### 테스트

```kotlin
/**
 * 테마별 아이콘 테스트
 * 
 * 1. 설정 → 배경화면 및 스타일 → 테마 색상 변경
 * 2. 홈 화면에서 앱 아이콘 확인
 * 3. 아이콘이 선택한 테마 색상으로 변경되는지 확인
 */
```

---

## 예측 백 제스처

### 개요

**Android 13부터 사용자가 뒤로 가기 제스처를 시작하면 미리보기가 표시됩니다.**

### 구현 방법

```kotlin
import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback

/**
 * 예측 백 제스처 처리
 */
@Composable
fun PredictiveBackGestureExample() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    DisposableEffect(Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로 가기 처리
            }
            
            override fun handleOnBackProgressed(backEvent: BackEventCompat) {
                // 제스처 진행 중 (미리보기 업데이트)
                val progress = backEvent.progress
                // progress: 0.0 (시작) ~ 1.0 (완료)
            }
            
            override fun handleOnBackCancelled() {
                // 제스처 취소됨
            }
        }
        
        activity?.onBackPressedDispatcher?.addCallback(callback)
        
        onDispose {
            callback.remove()
        }
    }
}
```

---

## 클립보드 미리보기

### 개요

**Android 13부터 클립보드에 복사된 내용을 미리보기로 표시합니다.**

사용자가 텍스트나 이미지를 복사하면 화면 하단에 토스트 메시지가 표시됩니다.

### 구현 (자동)

별도 구현 불필요 - 시스템이 자동으로 처리합니다.

```kotlin
/**
 * 클립보드에 복사
 */
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
    
    // Android 13+에서는 자동으로 미리보기 토스트 표시
}
```

---

## 향상된 다국어 지원

### 개요

**Android 13부터 앱별로 언어를 설정할 수 있습니다.**

사용자가 시스템 언어와 다른 언어로 특정 앱을 사용할 수 있습니다.

### 구현 방법

#### 1. Manifest 설정

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <application
        android:supportsRtl="true">
        
        <!-- 지원하는 언어 목록 -->
        <property
            android:name="android.app.LANGUAGES"
            android:resource="@xml/locales_config"/>
        
    </application>
</manifest>
```

#### 2. 언어 목록 정의

**res/xml/locales_config.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en"/>  <!-- 영어 -->
    <locale android:name="ko"/>  <!-- 한국어 -->
    <locale android:name="ja"/>  <!-- 일본어 -->
    <locale android:name="es"/>  <!-- 스페인어 -->
</locale-config>
```

#### 3. 프로그래밍 방식으로 언어 변경

```kotlin
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * 앱 언어 변경
 */
fun setAppLanguage(languageCode: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ - 시스템 API 사용
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    } else {
        // Android 12 이하 - 수동 설정
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration()
        config.setLocale(locale)
        
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

/**
 * 현재 앱 언어 가져오기
 */
fun getAppLanguage(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"
    } else {
        Locale.getDefault().language
    }
}
```

#### 4. 언어 선택 UI

```kotlin
@Composable
fun LanguageSelector() {
    val languages = listOf(
        "en" to "English",
        "ko" to "한국어",
        "ja" to "日本語",
        "es" to "Español"
    )
    
    var selectedLanguage by remember { mutableStateOf(getAppLanguage()) }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "앱 언어 선택",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        languages.forEach { (code, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedLanguage = code
                        setAppLanguage(code)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedLanguage == code,
                    onClick = {
                        selectedLanguage = code
                        setAppLanguage(code)
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(name)
            }
        }
    }
}
```

---

## 포토 피커

### 개요

**Android 13부터 새로운 포토 피커 API를 사용하여 사진과 동영상을 선택할 수 있습니다.**

기존 방식보다 더 안전하고 사용자 친화적입니다.

### 장점

- ✅ **권한 불필요**: 저장소 권한 없이 미디어 선택 가능
- ✅ **개인정보 보호**: 선택한 파일만 앱에 공유
- ✅ **일관된 UI**: 모든 앱에서 동일한 선택 화면

### 구현 방법

```kotlin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 포토 피커 사용 예제
 */
@Composable
fun PhotoPickerExample() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // 단일 이미지 선택
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }
    
    // 여러 이미지 선택
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        // 선택된 이미지 URI 목록
    }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 단일 이미지 선택 버튼
        Button(onClick = {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("이미지 1개 선택")
        }
        
        // 여러 이미지 선택 버튼
        Button(onClick = {
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("이미지 여러 개 선택 (최대 5개)")
        }
        
        // 비디오 선택 버튼
        Button(onClick = {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }) {
            Text("비디오 선택")
        }
        
        // 이미지 또는 비디오 선택
        Button(onClick = {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        }) {
            Text("이미지 또는 비디오 선택")
        }
        
        // 선택된 이미지 표시
        selectedImageUri?.let { uri ->
            Spacer(modifier = Modifier.height(16.dp))
            
            AsyncImage(
                model = uri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
```

### 하위 호환성

```kotlin
/**
 * Android 버전에 따라 다른 방식 사용
 */
@Composable
fun BackwardCompatiblePhotoPicker() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ - 포토 피커 사용
        PhotoPickerExample()
    } else {
        // Android 12 이하 - 기존 방식 사용
        LegacyImagePicker()
    }
}
```

---

## 근처 Wi-Fi 기기 권한

### 개요

**Android 13부터 근처 Wi-Fi 기기를 검색하려면 새로운 권한이 필요합니다.**

### 권한 선언

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Android 13+ -->
    <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES"/>
    
    <!-- Android 12 이하 호환성 -->
    <uses-permission 
        android:name="android.permission.ACCESS_FINE_LOCATION"
        android:maxSdkVersion="32"/>
    
</manifest>
```

---

## 프로그래밍 방식 인텐트 필터

### 개요

**Android 13부터 동적으로 브로드캐스트 리시버를 등록할 때 플래그를 명시해야 합니다.**

### 구현

```kotlin
/**
 * 브로드캐스트 리시버 등록
 */
fun registerReceiver(context: Context) {
    val receiver = MyBroadcastReceiver()
    val filter = IntentFilter("com.example.ACTION")
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ - 플래그 필수
        context.registerReceiver(
            receiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED  // 또는 RECEIVER_EXPORTED
        )
    } else {
        // Android 12 이하
        context.registerReceiver(receiver, filter)
    }
}
```

---

## 마이그레이션 체크리스트

Android 13을 타겟으로 하는 앱을 업데이트할 때 확인해야 할 사항:

- [ ] 알림 권한 요청 구현
- [ ] 미디어 권한을 세분화된 권한으로 변경
- [ ] 테마별 앱 아이콘 추가 (선택사항)
- [ ] 예측 백 제스처 지원 (선택사항)
- [ ] 포토 피커 API 사용 (권장)
- [ ] 앱별 언어 설정 지원 (선택사항)
- [ ] 브로드캐스트 리시버 등록 시 플래그 추가

---

## 참고 자료

- [Android 13 공식 문서](https://developer.android.com/about/versions/13)
- [Android 13 동작 변경사항](https://developer.android.com/about/versions/13/behavior-changes-13)
- [Android 13 새로운 기능](https://developer.android.com/about/versions/13/features)
