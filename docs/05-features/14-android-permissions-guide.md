# Android 권한 관리 가이드

## 📚 목차

1. [권한 시스템 개요](#권한-시스템-개요)
2. [권한 종류](#권한-종류)
3. [권한 선언](#권한-선언)
4. [런타임 권한 요청](#런타임-권한-요청)
5. [Accompanist Permissions](#accompanist-permissions)
6. [실전 예제](#실전-예제)

---

## 권한 시스템 개요

Android 6.0 (API 23)부터 위험한 권한은 **런타임에 사용자에게 요청**해야 합니다.

### 권한이 필요한 이유

- ✅ **사용자 프라이버시 보호**
- ✅ **민감한 데이터 접근 제어**
- ✅ **사용자에게 투명성 제공**

---

## 권한 종류

### 일반 권한 (Normal Permissions)

Manifest에 선언만 하면 자동 승인:
- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `VIBRATE`
- `WAKE_LOCK`

### 위험 권한 (Dangerous Permissions)

런타임에 사용자 승인 필요:

| 권한 그룹 | 권한 |
|----------|------|
| **위치** | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` |
| **카메라** | `CAMERA` |
| **마이크** | `RECORD_AUDIO` |
| **저장소** | `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` |
| **연락처** | `READ_CONTACTS`, `WRITE_CONTACTS` |
| **전화** | `CALL_PHONE`, `READ_PHONE_STATE` |
| **SMS** | `SEND_SMS`, `RECEIVE_SMS` |

---

## 권한 선언

### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 일반 권한 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- 위험 권한 -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    
    <application>
        ...
    </application>
</manifest>
```

### Android 13+ 알림 권한

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Android 13+ 미디어 권한

```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
```

---

## 런타임 권한 요청

### 기본 방법 (Activity Result API)

```kotlin
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 승인됨
            openCamera()
        } else {
            // 권한 거부됨
            showPermissionDeniedMessage()
        }
    }
    
    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 이미 권한이 있음
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // 권한 설명 표시
                showPermissionRationale()
            }
            else -> {
                // 권한 요청
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
```

### 여러 권한 동시 요청

```kotlin
private val requestMultiplePermissions = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    permissions.entries.forEach { entry ->
        val permission = entry.key
        val isGranted = entry.value
        
        when (permission) {
            Manifest.permission.CAMERA -> {
                if (isGranted) {
                    // 카메라 권한 승인
                }
            }
            Manifest.permission.RECORD_AUDIO -> {
                if (isGranted) {
                    // 오디오 권한 승인
                }
            }
        }
    }
}

fun requestPermissions() {
    requestMultiplePermissions.launch(
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
}
```

---

## Accompanist Permissions

Compose에서 권한을 쉽게 처리하는 라이브러리입니다.

### 의존성 추가

```kotlin
dependencies {
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
}
```

### 단일 권한 요청

```kotlin
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionScreen() {
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    
    when {
        cameraPermissionState.status.isGranted -> {
            // 권한이 승인됨
            CameraPreview()
        }
        else -> {
            // 권한 요청 UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("카메라 권한이 필요합니다")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                ) {
                    Text("권한 요청")
                }
            }
        }
    }
}
```

### 여러 권한 요청

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MultiplePermissionsScreen() {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    when {
        permissionsState.allPermissionsGranted -> {
            // 모든 권한 승인됨
            CameraWithAudio()
        }
        permissionsState.shouldShowRationale -> {
            // 권한 설명 표시
            PermissionRationale(
                onRequestPermission = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }
        else -> {
            // 권한 요청
            PermissionRequest(
                onRequestPermission = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }
    }
}

@Composable
fun PermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "카메라와 마이크 권한이 필요합니다",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "동영상 녹화를 위해 카메라와 마이크 접근 권한이 필요합니다.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("권한 허용")
        }
    }
}
```

### 권한 상태 확인

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionStatusExample() {
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    
    Column {
        when {
            permissionState.status.isGranted -> {
                Text("✅ 권한 승인됨")
            }
            permissionState.status.shouldShowRationale -> {
                Text("ℹ️ 권한 설명 필요")
            }
            else -> {
                Text("❌ 권한 없음")
            }
        }
    }
}
```

---

## 실전 예제

### 위치 권한

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionScreen() {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    when {
        locationPermissionsState.allPermissionsGranted -> {
            // 위치 정보 사용
            LocationContent()
        }
        locationPermissionsState.shouldShowRationale -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("위치 권한 필요") },
                text = {
                    Text("현재 위치를 표시하기 위해 위치 권한이 필요합니다.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    ) {
                        Text("허용")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { /* 닫기 */ }) {
                        Text("취소")
                    }
                }
            )
        }
        else -> {
            PermissionDeniedContent()
        }
    }
}

@Composable
fun LocationContent() {
    // 위치 정보 표시
    Text("위치 정보를 사용할 수 있습니다")
}

@Composable
fun PermissionDeniedContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("위치 권한이 거부되었습니다")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "설정에서 권한을 허용해주세요",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

### 카메라 권한

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("카메라") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    // 카메라 프리뷰
                    CameraPreview()
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "카메라 권한이 필요합니다",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "사진을 촬영하려면 카메라 접근 권한이 필요합니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        ) {
                            Text("권한 허용")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "카메라 프리뷰",
            color = Color.White
        )
    }
}
```

### 알림 권한 (Android 13+)

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionScreen() {
    val notificationPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }
    
    // 나머지 UI
}
```

### 설정으로 이동

```kotlin
@Composable
fun OpenSettingsButton() {
    val context = LocalContext.current
    
    Button(
        onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    ) {
        Text("설정 열기")
    }
}
```

### 완전한 권한 플로우

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CompletePermissionFlow() {
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    
    var showRationale by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    
    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> {
                showRationale = false
                permanentlyDenied = false
            }
            permissionState.status.shouldShowRationale -> {
                showRationale = true
                permanentlyDenied = false
            }
            else -> {
                // 권한이 영구적으로 거부되었는지 확인
                // (실제로는 더 복잡한 로직 필요)
                permanentlyDenied = true
            }
        }
    }
    
    when {
        permissionState.status.isGranted -> {
            // 권한 승인됨
            CameraScreen()
        }
        showRationale -> {
            // 권한 설명 다이얼로그
            AlertDialog(
                onDismissRequest = { showRationale = false },
                title = { Text("카메라 권한 필요") },
                text = {
                    Text(
                        "이 기능을 사용하려면 카메라 권한이 필요합니다. " +
                        "권한을 허용해주세요."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRationale = false
                            permissionState.launchPermissionRequest()
                        }
                    ) {
                        Text("허용")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRationale = false }) {
                        Text("취소")
                    }
                }
            )
        }
        permanentlyDenied -> {
            // 설정으로 이동 안내
            PermanentlyDeniedContent()
        }
        else -> {
            // 초기 권한 요청
            InitialPermissionRequest(
                onRequestPermission = {
                    permissionState.launchPermissionRequest()
                }
            )
        }
    }
}

@Composable
fun PermanentlyDeniedContent() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Block,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Red
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "권한이 거부되었습니다",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "설정에서 카메라 권한을 허용해주세요.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                ).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        ) {
            Text("설정 열기")
        }
    }
}

@Composable
fun InitialPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "카메라 권한 필요",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermission) {
            Text("권한 요청")
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 필요한 시점에 요청

```kotlin
// ✅ 좋은 예: 기능 사용 직전에 요청
Button(onClick = {
    if (hasPermission) {
        openCamera()
    } else {
        requestPermission()
    }
})

// ❌ 나쁜 예: 앱 시작 시 모든 권한 요청
```

### 2. 권한 설명 제공

```kotlin
// ✅ 사용자에게 왜 필요한지 설명
if (shouldShowRationale) {
    showExplanationDialog()
}
```

### 3. 우아한 거부 처리

```kotlin
// ✅ 권한 없이도 일부 기능 제공
if (!hasPermission) {
    showLimitedFeatures()
} else {
    showFullFeatures()
}
```

### 4. 최소 권한 원칙

```kotlin
// ✅ 필요한 최소한의 권한만 요청
ACCESS_COARSE_LOCATION // 대략적인 위치만 필요한 경우

// ❌ 불필요하게 강한 권한 요청
ACCESS_FINE_LOCATION // 정확한 위치가 꼭 필요하지 않은데 요청
```

---

## 🎯 다음 단계

권한 관리를 마스터했습니다! 다음으로:

1. **테스팅** - Unit Test, UI Test
2. **디버깅** - 문제 해결 기법
3. **앱 배포** - Google Play 배포

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
