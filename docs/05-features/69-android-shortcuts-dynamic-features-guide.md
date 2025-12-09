# Android App Shortcuts & Dynamic Features 가이드

## 목차
1. [App Shortcuts란?](#app-shortcuts란)
2. [Static Shortcuts](#static-shortcuts)
3. [Dynamic Shortcuts](#dynamic-shortcuts)
4. [Pinned Shortcuts](#pinned-shortcuts)
5. [Dynamic Feature Modules](#dynamic-feature-modules)
6. [On-Demand Delivery](#on-demand-delivery)
7. [실전 예제](#실전-예제)
8. [Jetpack Compose 통합](#jetpack-compose-통합)
9. [문제 해결](#문제-해결)

---

## App Shortcuts란?

**App Shortcuts**는 사용자가 앱의 특정 기능에 빠르게 접근할 수 있게 하는 기능입니다.

### Shortcuts 유형
- 📌 **Static Shortcuts**: 앱 설치 시 정의, 변경 불가
- 🔄 **Dynamic Shortcuts**: 런타임에 추가/제거/업데이트 가능
- 📍 **Pinned Shortcuts**: 홈 화면에 고정 가능

### 사용 사례
- 🔍 **검색**: 최근 검색어
- 💬 **메시지**: 자주 연락하는 사람
- 📝 **작업**: 새 메모, 새 이벤트
- 🎵 **미디어**: 재생 목록

---

## Static Shortcuts

### 정의

**res/xml/shortcuts.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 새 메시지 작성 -->
    <shortcut
        android:shortcutId="compose"
        android:enabled="true"
        android:icon="@drawable/ic_compose"
        android:shortcutShortLabel="@string/shortcut_compose_short"
        android:shortcutLongLabel="@string/shortcut_compose_long"
        android:disabledMessage="@string/shortcut_disabled_message">
        
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.example.app"
            android:targetClass="com.example.app.ComposeActivity"/>
        
        <categories android:name="android.shortcut.conversation"/>
    </shortcut>
    
    <!-- 검색 -->
    <shortcut
        android:shortcutId="search"
        android:enabled="true"
        android:icon="@drawable/ic_search"
        android:shortcutShortLabel="@string/shortcut_search_short"
        android:shortcutLongLabel="@string/shortcut_search_long">
        
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.example.app"
            android:targetClass="com.example.app.SearchActivity"/>
        
        <categories android:name="android.shortcut.conversation"/>
    </shortcut>
    
</shortcuts>
```

**strings.xml**:
```xml
<resources>
    <string name="shortcut_compose_short">새 메시지</string>
    <string name="shortcut_compose_long">새 메시지 작성</string>
    <string name="shortcut_search_short">검색</string>
    <string name="shortcut_search_long">앱 검색</string>
    <string name="shortcut_disabled_message">이 기능은 현재 사용할 수 없습니다</string>
</resources>
```

### Manifest 등록

**AndroidManifest.xml**:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
    
    <!-- Static Shortcuts 등록 -->
    <meta-data
        android:name="android.app.shortcuts"
        android:resource="@xml/shortcuts"/>
        
</activity>
```

---

## Dynamic Shortcuts

### 기본 사용법

```kotlin
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * Dynamic Shortcuts 관리자
 */
class DynamicShortcutManager(private val context: Context) {
    
    /**
     * Dynamic Shortcut 추가
     */
    fun addDynamicShortcut(
        id: String,
        shortLabel: String,
        longLabel: String,
        iconResId: Int,
        intent: Intent
    ) {
        val shortcut = ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(shortLabel)  // 짧은 레이블
            .setLongLabel(longLabel)    // 긴 레이블
            .setIcon(IconCompat.createWithResource(context, iconResId))  // 아이콘
            .setIntent(intent)  // 실행할 Intent
            .build()
        
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }
    
    /**
     * 여러 Shortcuts 추가
     */
    fun setDynamicShortcuts(shortcuts: List<ShortcutInfoCompat>) {
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
    
    /**
     * Shortcut 업데이트
     */
    fun updateShortcut(shortcut: ShortcutInfoCompat) {
        ShortcutManagerCompat.updateShortcuts(context, listOf(shortcut))
    }
    
    /**
     * Shortcut 제거
     */
    fun removeShortcut(shortcutId: String) {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcutId))
    }
    
    /**
     * 모든 Dynamic Shortcuts 제거
     */
    fun removeAllDynamicShortcuts() {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }
    
    /**
     * 최대 Shortcut 개수 확인
     */
    fun getMaxShortcutCount(): Int {
        return ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
    }
}
```

### 최근 연락처 Shortcuts

```kotlin
/**
 * 최근 연락처 Shortcuts
 */
class RecentContactsShortcuts(private val context: Context) {
    
    /**
     * 최근 연락처로 Shortcuts 생성
     */
    fun createRecentContactsShortcuts(contacts: List<Contact>) {
        val shortcuts = contacts.take(4).mapIndexed { index, contact ->
            val intent = Intent(context, ChatActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("contact_id", contact.id)
            }
            
            ShortcutInfoCompat.Builder(context, "contact_${contact.id}")
                .setShortLabel(contact.name)
                .setLongLabel("${contact.name}에게 메시지 보내기")
                .setIcon(
                    if (contact.photoUri != null) {
                        IconCompat.createWithContentUri(contact.photoUri)
                    } else {
                        IconCompat.createWithResource(context, R.drawable.ic_person)
                    }
                )
                .setIntent(intent)
                .setRank(index)  // 순서 (낮을수록 우선)
                .build()
        }
        
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
}

data class Contact(
    val id: String,
    val name: String,
    val photoUri: Uri?
)
```

---

## Pinned Shortcuts

### Pinned Shortcut 생성

```kotlin
import android.app.PendingIntent
import android.content.Intent

/**
 * Pinned Shortcuts 관리자
 */
class PinnedShortcutManager(private val context: Context) {
    
    /**
     * Pinned Shortcut 생성 가능 여부 확인
     */
    fun isPinShortcutSupported(): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }
    
    /**
     * Pinned Shortcut 요청
     */
    fun requestPinShortcut(
        id: String,
        shortLabel: String,
        longLabel: String,
        iconResId: Int,
        intent: Intent
    ) {
        if (!isPinShortcutSupported()) {
            Toast.makeText(context, "이 기기는 Pinned Shortcuts를 지원하지 않습니다", Toast.LENGTH_SHORT).show()
            return
        }
        
        val shortcut = ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(IconCompat.createWithResource(context, iconResId))
            .setIntent(intent)
            .build()
        
        // 콜백 Intent (선택적)
        val callbackIntent = Intent(context, ShortcutPinnedReceiver::class.java).apply {
            putExtra("shortcut_id", id)
        }
        
        val successCallback = PendingIntent.getBroadcast(
            context,
            0,
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Pinned Shortcut 요청
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, successCallback.intentSender)
    }
}

/**
 * Pinned Shortcut 생성 완료 리시버
 */
class ShortcutPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shortcutId = intent.getStringExtra("shortcut_id")
        Toast.makeText(context, "바로가기가 생성되었습니다: $shortcutId", Toast.LENGTH_SHORT).show()
    }
}
```

**AndroidManifest.xml**:
```xml
<receiver
    android:name=".ShortcutPinnedReceiver"
    android:enabled="true"
    android:exported="false"/>
```

---

## Dynamic Feature Modules

### Dynamic Feature란?
앱의 특정 기능을 별도 모듈로 분리하여 필요할 때만 다운로드하는 기능입니다.

### 장점
- 📦 **초기 다운로드 크기 감소**
- ⚡ **빠른 설치**
- 💾 **저장 공간 절약**

### Dynamic Feature Module 생성

**1. build.gradle.kts (app 모듈)**:
```kotlin
plugins {
    id("com.android.application")
}

android {
    // ...
    
    dynamicFeatures += setOf(":feature_camera", ":feature_premium")
}

dependencies {
    // Play Core Library
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")
}
```

**2. build.gradle.kts (feature 모듈)**:
```kotlin
plugins {
    id("com.android.dynamic-feature")
}

android {
    namespace = "com.example.app.feature.camera"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(project(":app"))
}
```

**3. AndroidManifest.xml (feature 모듈)**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:dist="http://schemas.android.com/apk/distribution">
    
    <!-- 배포 설정 -->
    <dist:module
        dist:instant="false"
        dist:title="@string/feature_camera_title">
        
        <!-- 설치 시점 -->
        <dist:delivery>
            <dist:on-demand/>  <!-- 요청 시 설치 -->
            <!-- <dist:install-time/> -->  <!-- 앱 설치 시 함께 설치 -->
        </dist:delivery>
        
        <!-- 퓨징 (APK에 포함 여부) -->
        <dist:fusing dist:include="true"/>
        
    </dist:module>
    
</manifest>
```

---

## On-Demand Delivery

### Feature Module 다운로드

```kotlin
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * Dynamic Feature 관리자
 */
class DynamicFeatureManager(private val context: Context) {
    
    private val splitInstallManager: SplitInstallManager =
        SplitInstallManagerFactory.create(context)
    
    /**
     * Feature 설치 여부 확인
     */
    fun isFeatureInstalled(featureName: String): Boolean {
        return splitInstallManager.installedModules.contains(featureName)
    }
    
    /**
     * Feature 다운로드 및 설치
     */
    fun installFeature(
        featureName: String,
        onProgress: (Int) -> Unit,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // 이미 설치되어 있으면 바로 성공
        if (isFeatureInstalled(featureName)) {
            onSuccess()
            return
        }
        
        // 설치 요청 생성
        val request = SplitInstallRequest.newBuilder()
            .addModule(featureName)
            .build()
        
        // 상태 리스너
        val listener = SplitInstallStateUpdatedListener { state ->
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    // 다운로드 중
                    val totalBytes = state.totalBytesToDownload()
                    val downloaded = state.bytesDownloaded()
                    val progress = if (totalBytes > 0) {
                        (downloaded * 100 / totalBytes).toInt()
                    } else {
                        0
                    }
                    
                    Log.d("DynamicFeature", "다운로드 중: $progress%")
                    onProgress(progress)
                }
                
                SplitInstallSessionStatus.INSTALLING -> {
                    // 설치 중
                    Log.d("DynamicFeature", "설치 중")
                }
                
                SplitInstallSessionStatus.INSTALLED -> {
                    // 설치 완료
                    Log.d("DynamicFeature", "설치 완료")
                    onSuccess()
                }
                
                SplitInstallSessionStatus.FAILED -> {
                    // 설치 실패
                    Log.e("DynamicFeature", "설치 실패: ${state.errorCode()}")
                    onFailure(Exception("설치 실패: ${state.errorCode()}"))
                }
                
                SplitInstallSessionStatus.CANCELED -> {
                    // 취소됨
                    Log.w("DynamicFeature", "설치 취소")
                    onFailure(Exception("설치 취소"))
                }
                
                else -> {
                    Log.d("DynamicFeature", "상태: ${state.status()}")
                }
            }
        }
        
        // 리스너 등록
        splitInstallManager.registerListener(listener)
        
        // 설치 시작
        splitInstallManager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                Log.d("DynamicFeature", "설치 시작: $sessionId")
            }
            .addOnFailureListener { exception ->
                Log.e("DynamicFeature", "설치 요청 실패", exception)
                splitInstallManager.unregisterListener(listener)
                onFailure(exception)
            }
    }
    
    /**
     * Feature 제거
     */
    fun uninstallFeature(featureName: String) {
        splitInstallManager.deferredUninstall(listOf(featureName))
            .addOnSuccessListener {
                Log.d("DynamicFeature", "제거 예약: $featureName")
            }
            .addOnFailureListener { exception ->
                Log.e("DynamicFeature", "제거 실패", exception)
            }
    }
    
    /**
     * 설치된 모든 Feature 목록
     */
    fun getInstalledFeatures(): Set<String> {
        return splitInstallManager.installedModules
    }
}
```

---

## 실전 예제

### 카메라 Feature 로딩

```kotlin
/**
 * 카메라 Feature 사용 예제
 */
class CameraFeatureActivity : AppCompatActivity() {
    
    private lateinit var featureManager: DynamicFeatureManager
    
    companion object {
        const val FEATURE_CAMERA = "feature_camera"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        featureManager = DynamicFeatureManager(this)
        
        // 카메라 버튼 클릭
        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            openCamera()
        }
    }
    
    /**
     * 카메라 열기
     */
    private fun openCamera() {
        if (featureManager.isFeatureInstalled(FEATURE_CAMERA)) {
            // 이미 설치됨: 바로 실행
            launchCameraActivity()
        } else {
            // 설치 필요: 다운로드
            showDownloadDialog()
        }
    }
    
    /**
     * 다운로드 다이얼로그 표시
     */
    private fun showDownloadDialog() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("카메라 기능 다운로드 중...")
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            show()
        }
        
        featureManager.installFeature(
            featureName = FEATURE_CAMERA,
            onProgress = { progress ->
                progressDialog.progress = progress
            },
            onSuccess = {
                progressDialog.dismiss()
                Toast.makeText(this, "다운로드 완료!", Toast.LENGTH_SHORT).show()
                launchCameraActivity()
            },
            onFailure = { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "다운로드 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    /**
     * 카메라 Activity 실행
     */
    private fun launchCameraActivity() {
        try {
            val intent = Intent()
            intent.setClassName(
                packageName,
                "com.example.app.feature.camera.CameraActivity"
            )
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("Camera", "카메라 실행 실패", e)
            Toast.makeText(this, "카메라를 실행할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

## Jetpack Compose 통합

### Shortcuts

```kotlin
/**
 * Compose에서 Dynamic Shortcuts 관리
 */
@Composable
fun ShortcutsManagerScreen() {
    val context = LocalContext.current
    val shortcutManager = remember { DynamicShortcutManager(context) }
    
    var shortcuts by remember { mutableStateOf<List<String>>(emptyList()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dynamic Shortcuts",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = {
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("screen", "compose")
            }
            
            shortcutManager.addDynamicShortcut(
                id = "compose_${System.currentTimeMillis()}",
                shortLabel = "새 메시지",
                longLabel = "새 메시지 작성",
                iconResId = R.drawable.ic_compose,
                intent = intent
            )
            
            Toast.makeText(context, "Shortcut 추가됨", Toast.LENGTH_SHORT).show()
        }) {
            Text("Shortcut 추가")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(onClick = {
            shortcutManager.removeAllDynamicShortcuts()
            Toast.makeText(context, "모든 Shortcuts 제거됨", Toast.LENGTH_SHORT).show()
        }) {
            Text("모든 Shortcuts 제거")
        }
    }
}
```

### Dynamic Features

```kotlin
/**
 * Compose에서 Dynamic Feature 다운로드
 */
@Composable
fun DynamicFeatureScreen() {
    val context = LocalContext.current
    val featureManager = remember { DynamicFeatureManager(context) }
    
    var downloadProgress by remember { mutableStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }
    var isInstalled by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isInstalled = featureManager.isFeatureInstalled("feature_camera")
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "카메라 기능",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isInstalled) {
            Text("카메라 기능이 설치되어 있습니다")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                // 카메라 실행
            }) {
                Text("카메라 열기")
            }
        } else {
            if (isDownloading) {
                CircularProgressIndicator()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = downloadProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("다운로드 중: $downloadProgress%")
            } else {
                Button(onClick = {
                    isDownloading = true
                    
                    featureManager.installFeature(
                        featureName = "feature_camera",
                        onProgress = { progress ->
                            downloadProgress = progress
                        },
                        onSuccess = {
                            isDownloading = false
                            isInstalled = true
                        },
                        onFailure = { exception ->
                            isDownloading = false
                            // 에러 처리
                        }
                    )
                }) {
                    Text("카메라 기능 다운로드")
                }
            }
        }
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * Shortcuts & Dynamic Features 문제 해결
 */
class TroubleshootingGuide {
    
    /**
     * 1. Shortcuts가 표시되지 않음
     */
    fun handleShortcutsNotShowing() {
        // shortcuts.xml 파일 확인
        // AndroidManifest.xml에 meta-data 등록 확인
        // 최대 개수 제한 확인 (보통 4-5개)
    }
    
    /**
     * 2. Dynamic Feature 다운로드 실패
     */
    fun handleFeatureDownloadFailed(errorCode: Int) {
        when (errorCode) {
            SplitInstallErrorCode.NETWORK_ERROR -> {
                // 네트워크 에러
                Log.e("Feature", "네트워크 연결 확인 필요")
            }
            
            SplitInstallErrorCode.NO_ERROR -> {
                // 에러 없음
            }
            
            else -> {
                Log.e("Feature", "다운로드 실패: $errorCode")
            }
        }
    }
    
    /**
     * 3. Feature Module이 실행되지 않음
     */
    fun handleFeatureNotLaunching() {
        // 설치 확인
        // 클래스 이름 확인
        // Manifest 설정 확인
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. Shortcuts 개수 제한 확인
val maxCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
if (shortcuts.size <= maxCount) {
    ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
}

// 2. Feature 설치 여부 확인
if (featureManager.isFeatureInstalled("feature_camera")) {
    launchFeature()
} else {
    downloadFeature()
}

// 3. 진행 상태 표시
featureManager.installFeature(
    onProgress = { progress -> showProgress(progress) }
)

// 4. 에러 처리
featureManager.installFeature(
    onFailure = { exception -> handleError(exception) }
)

// 5. 리소스 정리
override fun onDestroy() {
    splitInstallManager.unregisterListener(listener)
}
```

### DON'Ts ❌

```kotlin
// 1. 너무 많은 Shortcuts
// ❌ 최대 개수 초과

// 2. Feature 설치 확인 안 함
launchFeature()  // ❌ 설치 여부 확인 필요

// 3. 진행 상태 표시 안 함
// ❌ 사용자가 대기 중인지 모름

// 4. 에러 처리 안 함
// ❌ 다운로드 실패 시 아무 조치 없음

// 5. 리스너 해제 안 함
// ❌ 메모리 누수
```

---

## 참고 자료

- [App Shortcuts 공식 문서](https://developer.android.com/guide/topics/ui/shortcuts)
- [Dynamic Feature Modules](https://developer.android.com/guide/app-bundle/dynamic-delivery)
- [Play Core Library](https://developer.android.com/guide/playcore)
