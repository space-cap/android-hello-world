# Android 14 (API 34) 새로운 기능 가이드

## 목차
1. [Android 14 개요](#android-14-개요)
2. [선택적 사진/비디오 권한](#선택적-사진비디오-권한)
3. [전체 화면 인텐트 권한](#전체-화면-인텐트-권한)
4. [향상된 백 제스처](#향상된-백-제스처)
5. [문법적 굴절 API](#문법적-굴절-api)
6. [지역 설정 기본 설정](#지역-설정-기본-설정)
7. [비선형 폰트 스케일링](#비선형-폰트-스케일링)
8. [Ultra HDR 이미지](#ultra-hdr-이미지)
9. [OpenJDK 17 기능](#openjdk-17-기능)
10. [보안 강화](#보안-강화)

---

## Android 14 개요

**Android 14 (Upside Down Cake, API 34)**는 2023년 10월에 출시되었으며, 개인정보 보호, 접근성, 성능을 더욱 강화한 버전입니다.

### 주요 변경사항 요약

| 카테고리 | 주요 기능 |
|---------|----------|
| **개인정보 보호** | 선택적 사진 권한, 전체 화면 인텐트 권한 |
| **사용자 경험** | 향상된 백 제스처, 비선형 폰트 스케일링 |
| **다국어** | 문법적 굴절 API, 지역 설정 기본 설정 |
| **미디어** | Ultra HDR 이미지 지원 |
| **개발자** | OpenJDK 17, 향상된 보안 |

### targetSdkVersion 설정

```kotlin
// build.gradle.kts (Module: app)
android {
    compileSdk = 34
    
    defaultConfig {
        targetSdk = 34  // Android 14를 타겟으로 설정
        minSdk = 21
    }
}
```

---

## 선택적 사진/비디오 권한

### 개요

**Android 14부터 사용자가 앱에 제공할 사진과 비디오를 개별적으로 선택할 수 있습니다.**

전체 미디어 라이브러리 대신 특정 파일만 앱과 공유할 수 있어 개인정보 보호가 강화되었습니다.

### 동작 방식

```
사용자가 미디어 권한 요청 시:
┌─────────────────────────────────┐
│  사진 및 비디오 접근 허용        │
├─────────────────────────────────┤
│  ○ 모든 사진 및 비디오 허용      │  ← 기존 방식
│  ● 선택한 사진 및 비디오만 허용  │  ← 새로운 방식 (기본값)
│  ○ 허용 안 함                   │
└─────────────────────────────────┘
```

### 권한 선언

**AndroidManifest.xml**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 부분 접근 권한 (Android 14+) -->
    <uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"/>
    
    <!-- 전체 접근 권한 (Android 13+) -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
    
    <!-- Android 12 이하 호환성 -->
    <uses-permission 
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32"/>
    
    <application>
        <!-- ... -->
    </application>
</manifest>
```

### 구현 예제

```kotlin
import android.Manifest
import android.os.Build
import androidx.compose.runtime.*
import com.google.accompanist.permissions.*

/**
 * 선택적 사진 권한 요청
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PartialMediaAccessExample() {
    // Android 14 이상에서는 3개의 권한 요청
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED  // 부분 접근
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "사진 접근 권한",
            style = MaterialTheme.typography.titleLarge
        )
        
        when {
            // 모든 권한 부여됨
            permissionsState.allPermissionsGranted -> {
                Text("✅ 전체 미디어 접근 권한 부여됨")
                
                Button(onClick = { /* 갤러리 열기 */ }) {
                    Text("갤러리 열기")
                }
            }
            
            // 부분 권한만 부여됨 (Android 14+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            permissionsState.permissions.any { 
                it.permission == Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED && 
                it.status.isGranted 
            } -> {
                Text("⚠️ 선택한 사진만 접근 가능")
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { /* 선택된 사진 보기 */ }) {
                        Text("선택된 사진 보기")
                    }
                    
                    OutlinedButton(onClick = {
                        // 추가 사진 선택 요청
                        permissionsState.launchMultiplePermissionRequest()
                    }) {
                        Text("더 많은 사진 선택")
                    }
                }
            }
            
            // 권한 없음
            else -> {
                Text("사진에 접근하려면 권한이 필요합니다")
                
                Button(onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("권한 요청")
                }
            }
        }
        
        // 권한 상태 디버그 정보
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Divider()
            
            Text(
                text = "권한 상태:",
                style = MaterialTheme.typography.labelLarge
            )
            
            permissionsState.permissions.forEach { permission ->
                val permissionName = when (permission.permission) {
                    Manifest.permission.READ_MEDIA_IMAGES -> "이미지"
                    Manifest.permission.READ_MEDIA_VIDEO -> "비디오"
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED -> "선택된 항목"
                    else -> permission.permission
                }
                
                Text(
                    text = "$permissionName: ${if (permission.status.isGranted) "✅" else "❌"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

### 선택된 미디어 확인

```kotlin
import android.provider.MediaStore

/**
 * 사용자가 선택한 미디어 파일 가져오기
 */
fun getSelectedMedia(context: Context): List<Uri> {
    val selectedMedia = mutableListOf<Uri>()
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14+ - 선택된 미디어만 쿼리
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                selectedMedia.add(uri)
            }
        }
    }
    
    return selectedMedia
}
```

### 베스트 프랙티스

```kotlin
/**
 * 권한 상태에 따른 UI 표시
 */
@Composable
fun MediaAccessUI() {
    val hasFullAccess = hasFullMediaAccess()
    val hasPartialAccess = hasPartialMediaAccess()
    
    when {
        hasFullAccess -> {
            // 전체 갤러리 표시
            FullGalleryView()
        }
        
        hasPartialAccess -> {
            // 선택된 사진만 표시 + 추가 선택 버튼
            PartialGalleryView(
                onRequestMore = { requestAdditionalMedia() }
            )
        }
        
        else -> {
            // 권한 요청 화면
            RequestMediaPermissionView()
        }
    }
}

/**
 * 전체 미디어 접근 권한 확인
 */
fun hasFullMediaAccess(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * 부분 미디어 접근 권한 확인
 */
fun hasPartialMediaAccess(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        false
    }
}
```

---

## 전체 화면 인텐트 권한

### 개요

**Android 14부터 전체 화면 알림(예: 전화 수신 화면)을 표시하려면 사용자 권한이 필요합니다.**

### 사용 사례

- 📞 전화 앱 (수신 전화 화면)
- ⏰ 알람 앱 (알람 화면)
- 📹 화상 통화 앱

### 권한 선언

**AndroidManifest.xml**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 전체 화면 인텐트 권한 -->
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT"/>
    
    <application>
        <!-- ... -->
    </application>
</manifest>
```

### 권한 요청

```kotlin
import android.app.NotificationManager
import android.content.Intent
import android.provider.Settings

/**
 * 전체 화면 인텐트 권한 확인 및 요청
 */
@Composable
fun FullScreenIntentPermission() {
    val context = LocalContext.current
    val notificationManager = remember {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    // 권한 확인
    val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        notificationManager.canUseFullScreenIntent()
    } else {
        true  // Android 13 이하는 자동 부여
    }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "전체 화면 알림 권한",
            style = MaterialTheme.typography.titleLarge
        )
        
        if (hasPermission) {
            Text("✅ 전체 화면 알림 권한 부여됨")
        } else {
            Text("⚠️ 전체 화면 알림 권한 필요")
            
            Text(
                text = "전화나 알람을 받으려면 이 권한이 필요합니다.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(onClick = {
                // 설정 화면으로 이동
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                context.startActivity(intent)
            }) {
                Text("설정에서 권한 허용")
            }
        }
    }
}
```

### 전체 화면 알림 표시

```kotlin
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent

/**
 * 전체 화면 알림 생성 및 표시
 */
fun showFullScreenNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
        as NotificationManager
    
    // 채널 생성 (Android 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "call_channel",
            "전화 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "수신 전화 알림"
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    // 전체 화면 인텐트
    val fullScreenIntent = Intent(context, IncomingCallActivity::class.java)
    val fullScreenPendingIntent = PendingIntent.getActivity(
        context,
        0,
        fullScreenIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    // 알림 생성
    val notification = Notification.Builder(context, "call_channel")
        .setContentTitle("수신 전화")
        .setContentText("010-1234-5678")
        .setSmallIcon(R.drawable.ic_call)
        .setFullScreenIntent(fullScreenPendingIntent, true)  // 전체 화면 인텐트 설정
        .setCategory(Notification.CATEGORY_CALL)
        .setPriority(Notification.PRIORITY_HIGH)
        .build()
    
    // 알림 표시
    notificationManager.notify(1, notification)
}
```

---

## 향상된 백 제스처

### 개요

**Android 14에서 예측 백 제스처가 기본적으로 활성화되었습니다.**

사용자가 뒤로 가기 제스처를 시작하면 실시간으로 미리보기가 표시됩니다.

### 활성화

**AndroidManifest.xml**:
```xml
<application
    android:enableOnBackInvokedCallback="true">
    <!-- ... -->
</application>
```

### 구현

```kotlin
import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 예측 백 제스처 애니메이션
 */
@Composable
fun PredictiveBackExample() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    // 애니메이션 상태
    var backProgress by remember { mutableStateOf(0f) }
    val scale = remember { Animatable(1f) }
    val translationX = remember { Animatable(0f) }
    
    DisposableEffect(Unit) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackStarted(backEvent: BackEventCompat) {
                // 백 제스처 시작
            }
            
            override fun handleOnBackProgressed(backEvent: BackEventCompat) {
                // 제스처 진행 중
                backProgress = backEvent.progress
                
                // 애니메이션 업데이트
                kotlinx.coroutines.MainScope().launch {
                    scale.snapTo(1f - (backProgress * 0.1f))
                    translationX.snapTo(backProgress * 100f)
                }
            }
            
            override fun handleOnBackPressed() {
                // 제스처 완료 - 뒤로 가기 실행
                activity?.finish()
            }
            
            override fun handleOnBackCancelled() {
                // 제스처 취소 - 원래 상태로 복원
                kotlinx.coroutines.MainScope().launch {
                    scale.animateTo(1f)
                    translationX.animateTo(0f)
                }
            }
        }
        
        activity?.onBackPressedDispatcher?.addCallback(callback)
        
        onDispose {
            callback.remove()
        }
    }
    
    // UI with animation
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.translationX = translationX.value
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "뒤로 가기 제스처를 시도해보세요",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = backProgress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "진행률: ${(backProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

---

## 문법적 굴절 API

### 개요

**Android 14부터 언어의 문법적 성별을 지원합니다.**

일부 언어(프랑스어, 독일어 등)에서 명사가 성별에 따라 변하는 것을 처리할 수 있습니다.

### 사용 예제

**strings.xml (프랑스어)**:
```xml
<resources>
    <!-- 남성형 -->
    <string name="welcome_male">Bienvenu</string>
    
    <!-- 여성형 -->
    <string name="welcome_female">Bienvenue</string>
    
    <!-- 문법적 굴절 사용 -->
    <string name="welcome_inflected">
        <inflection gender="masculine">Bienvenu</inflection>
        <inflection gender="feminine">Bienvenue</inflection>
    </string>
</resources>
```

### 코드에서 사용

```kotlin
import android.app.GrammaticalInflectionManager

/**
 * 문법적 성별 설정
 */
fun setGrammaticalGender(context: Context, gender: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val manager = context.getSystemService(Context.GRAMMATICAL_INFLECTION_SERVICE) 
            as GrammaticalInflectionManager
        
        manager.setRequestedApplicationGrammaticalGender(gender)
        // gender: Configuration.GRAMMATICAL_GENDER_MASCULINE
        //         Configuration.GRAMMATICAL_GENDER_FEMININE
        //         Configuration.GRAMMATICAL_GENDER_NEUTRAL
    }
}
```

---

## 지역 설정 기본 설정

### 개요

**Android 14부터 사용자의 지역 설정 기본 설정을 앱에서 사용할 수 있습니다.**

온도 단위, 첫 번째 요일 등의 설정을 자동으로 적용할 수 있습니다.

### 사용 예제

```kotlin
import android.icu.util.Calendar
import androidx.core.os.LocaleListCompat

/**
 * 사용자의 첫 번째 요일 가져오기
 */
fun getFirstDayOfWeek(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek
        // Calendar.SUNDAY 또는 Calendar.MONDAY
    } else {
        Calendar.SUNDAY  // 기본값
    }
}

/**
 * 온도 단위 가져오기
 */
fun getTemperatureUnit(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val locale = LocaleListCompat.getDefault()[0]
        // 지역에 따라 섭씨 또는 화씨
        if (locale?.country == "US") "°F" else "°C"
    } else {
        "°C"  // 기본값
    }
}
```

---

## 비선형 폰트 스케일링

### 개요

**Android 14부터 큰 폰트 크기에서 비선형 스케일링을 적용합니다.**

접근성을 위해 폰트 크기를 200%까지 확대할 수 있으며, 레이아웃이 깨지지 않도록 최적화되었습니다.

### 대응 방법

```kotlin
/**
 * 폰트 스케일링 대응
 */
@Composable
fun FontScalingExample() {
    // sp 단위 사용 (자동 스케일링)
    Text(
        text = "이 텍스트는 자동으로 스케일링됩니다",
        fontSize = 16.sp  // ✅ sp 사용
    )
    
    // dp 단위는 스케일링 안 됨
    Text(
        text = "이 텍스트는 스케일링되지 않습니다",
        fontSize = 16.dp  // ❌ dp 사용하지 말 것
    )
}

/**
 * 현재 폰트 스케일 가져오기
 */
@Composable
fun getCurrentFontScale(): Float {
    val configuration = LocalConfiguration.current
    return configuration.fontScale
}
```

### 테스트

```kotlin
/**
 * 다양한 폰트 크기에서 테스트
 * 
 * 설정 → 접근성 → 글꼴 크기
 * 100% ~ 200%까지 테스트
 */
```

---

## Ultra HDR 이미지

### 개요

**Android 14부터 Ultra HDR (High Dynamic Range) 이미지를 지원합니다.**

더 넓은 색 영역과 밝기 범위를 표현할 수 있습니다.

### 사용 예제

```kotlin
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Ultra HDR 이미지 로드
 */
fun loadUltraHDRImage(context: Context, resourceId: Int): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val options = BitmapFactory.Options().apply {
            inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.DISPLAY_P3)
        }
        BitmapFactory.decodeResource(context.resources, resourceId, options)
    } else {
        BitmapFactory.decodeResource(context.resources, resourceId)
    }
}
```

---

## OpenJDK 17 기능

### 개요

**Android 14부터 OpenJDK 17 기능을 사용할 수 있습니다.**

### 새로운 기능

#### 1. Sealed Classes (봉인 클래스)

```kotlin
/**
 * Sealed 클래스 - 제한된 클래스 계층 구조
 */
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
    object Loading : Result()
}

fun handleResult(result: Result) {
    when (result) {
        is Result.Success -> println(result.data)
        is Result.Error -> println(result.message)
        Result.Loading -> println("Loading...")
        // else 불필요 - 모든 케이스 처리됨
    }
}
```

#### 2. Pattern Matching for instanceof

```java
// Java 17
if (obj instanceof String s) {
    // s를 바로 사용 가능
    System.out.println(s.length());
}
```

#### 3. Text Blocks

```java
// Java 17
String json = """
    {
        "name": "John",
        "age": 30
    }
    """;
```

---

## 보안 강화

### 1. 동적 코드 로딩 제한

```kotlin
/**
 * 동적 코드 로딩 시 보안 검증 필요
 */
```

### 2. 암시적 인텐트 제한

```kotlin
/**
 * 암시적 인텐트 사용 시 패키지 지정 필요
 */
val intent = Intent(Intent.ACTION_VIEW).apply {
    data = Uri.parse("https://example.com")
    setPackage("com.android.chrome")  // 패키지 명시
}
```

---

## 마이그레이션 체크리스트

Android 14를 타겟으로 하는 앱을 업데이트할 때 확인해야 할 사항:

- [ ] 선택적 사진/비디오 권한 처리
- [ ] 전체 화면 인텐트 권한 요청 (전화/알람 앱)
- [ ] 예측 백 제스처 지원
- [ ] 비선형 폰트 스케일링 테스트
- [ ] 암시적 인텐트에 패키지 지정
- [ ] 문법적 굴절 지원 (다국어 앱)

---

## 참고 자료

- [Android 14 공식 문서](https://developer.android.com/about/versions/14)
- [Android 14 동작 변경사항](https://developer.android.com/about/versions/14/behavior-changes-14)
- [Android 14 새로운 기능](https://developer.android.com/about/versions/14/features)
