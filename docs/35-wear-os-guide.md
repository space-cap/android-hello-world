# Wear OS 완벽 가이드

## 📚 목차

1. [Wear OS란?](#wear-os란)
2. [Wear OS 프로젝트 설정](#wear-os-프로젝트-설정)
3. [Wear Compose 기본](#wear-compose-기본)
4. [워치 페이스](#워치-페이스)
5. [타일 (Tiles)](#타일-tiles)
6. [컴플리케이션](#컴플리케이션)
7. [건강 및 피트니스](#건강-및-피트니스)
8. [실전 예제](#실전-예제)

---

## Wear OS란?

> [!NOTE]
> **Wear OS = Google의 스마트워치 운영체제**
> 
> **주요 기능:**
> - ⌚ 워치 앱
> - 🕐 워치 페이스 (시계 화면)
> - 📊 타일 (빠른 정보)
> - 🔢 컴플리케이션 (시계 위젯)
> - 💪 건강 및 피트니스

### Wear OS의 특징

**작은 화면:**
```
스마트폰: 6인치 이상
스마트워치: 1.2~1.4인치
→ 10배 작음!
```

**원형 화면:**
```
대부분의 스마트워치는 원형
→ 특별한 레이아웃 필요
```

**제한된 입력:**
```
터치: 작은 화면
음성: 주요 입력 방법
회전 베젤: 스크롤
버튼: 제한적
```

**통계:**
- Wear OS 기기: **1억 대 이상**
- 워치 페이스 다운로드: **10억 회 이상**
- 건강 앱 사용률: **85%**

---

## Wear OS 프로젝트 설정

### 새 프로젝트 생성

**Android Studio에서:**
```
1. File → New → New Project
2. "Wear OS" 탭 선택
3. "Empty Wear OS App" 선택
4. 프로젝트 이름 입력
5. "Finish" 클릭
```

### 기존 프로젝트에 Wear 모듈 추가

```
1. File → New → New Module
2. "Wear OS Module" 선택
3. 모듈 이름: "wear"
4. "Finish" 클릭
```

### 의존성 설정

```kotlin
// wear/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapp.wear"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 30  // Wear OS 3.0 이상
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // Wear Compose
    val wearComposeVersion = "1.3.0"
    implementation("androidx.wear.compose:compose-foundation:$wearComposeVersion")
    implementation("androidx.wear.compose:compose-material:$wearComposeVersion")
    implementation("androidx.wear.compose:compose-navigation:$wearComposeVersion")
    
    // Wear 기본
    implementation("androidx.wear:wear:1.3.0")
    
    // Activity
    implementation("androidx.activity:activity-compose:1.8.2")
}
```

### AndroidManifest.xml 설정

```xml
<!-- wear/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Wear OS 기능 선언 -->
    <uses-feature android:name="android.hardware.type.watch" />
    
    <!-- 권한 -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <application
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@android:style/Theme.DeviceDefault">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.DeviceDefault">
            
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Wear OS 앱임을 선언 -->
        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="true" />
    </application>
</manifest>
```

---

## Wear Compose 기본

### 기본 구조

```kotlin
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    // Wear Material Theme
    MaterialTheme {
        // Scaffold: Wear OS 레이아웃 기본 구조
        Scaffold(
            timeText = {
                // 상단 시간 표시
                TimeText()
            },
            vignette = {
                // 화면 가장자리 그라데이션
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            }
        ) {
            // 메인 콘텐츠
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    // ScalingLazyColumn: Wear OS용 스크롤 리스트
    // 중앙 항목이 크고, 위/아래로 갈수록 작아짐
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 32.dp,
            bottom = 32.dp
        )
    ) {
        item {
            Text(
                text = "Wear OS 앱",
                style = MaterialTheme.typography.title1
            )
        }
        
        item {
            Chip(
                label = { Text("버튼 1") },
                onClick = { /* 클릭 */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            Chip(
                label = { Text("버튼 2") },
                onClick = { /* 클릭 */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

**Wear Compose 주요 컴포넌트:**
```kotlin
// 1. ScalingLazyColumn
// - Wear OS용 리스트
// - 중앙 항목이 크게 표시
// - 회전 베젤로 스크롤 가능

// 2. Chip
// - Wear OS용 버튼
// - 아이콘, 레이블, 보조 레이블 지원

// 3. Card
// - 정보 카드
// - 이미지 + 텍스트

// 4. TimeText
// - 상단 시간 표시

// 5. Vignette
// - 화면 가장자리 그라데이션
// - 원형 화면에 적합
```

### 원형 화면 대응

```kotlin
@Composable
fun CircularScreen() {
    // Box로 중앙 정렬
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 원형 화면에 맞는 레이아웃
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "좋아요",
                style = MaterialTheme.typography.title2
            )
        }
    }
}
```

### 입력 처리

```kotlin
@Composable
fun InputScreen() {
    var count by remember { mutableStateOf(0) }
    
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "카운트: $count",
                style = MaterialTheme.typography.title1
            )
        }
        
        item {
            // 버튼
            Chip(
                label = { Text("증가") },
                onClick = { count++ },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            // 스위치
            ToggleChip(
                checked = count > 5,
                onCheckedChange = { /* 토글 */ },
                label = { Text("5 이상") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            // 슬라이더
            InlineSlider(
                value = count.toFloat(),
                onValueChange = { count = it.toInt() },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## 워치 페이스

> [!NOTE]
> **워치 페이스 = 시계 화면**
> 
> **구성 요소:**
> - 🕐 시간 표시
> - 📅 날짜 표시
> - 🔢 컴플리케이션 (추가 정보)
> - 🎨 커스텀 디자인

### 워치 페이스 서비스 생성

```kotlin
import androidx.wear.watchface.*

// 워치 페이스 서비스
class MyWatchFaceService : WatchFaceService() {
    
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        
        // 렌더러 생성
        val renderer = MyWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            complicationSlotsManager = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository
        )
        
        // 워치 페이스 생성
        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,  // 디지털 시계
            renderer = renderer
        )
    }
}

// 렌더러
class MyWatchFaceRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository
) : Renderer.CanvasRenderer2<MyWatchFaceRenderer.MySharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = 16L,  // 60 FPS
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {
    
    // 공유 자산
    class MySharedAssets : SharedAssets {
        override fun onDestroy() {}
    }
    
    override suspend fun createSharedAssets(): MySharedAssets {
        return MySharedAssets()
    }
    
    // 렌더링
    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: MySharedAssets
    ) {
        // 배경 그리기
        canvas.drawColor(Color.BLACK)
        
        // 시간 그리기
        val timeText = String.format(
            "%02d:%02d",
            zonedDateTime.hour,
            zonedDateTime.minute
        )
        
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        canvas.drawText(
            timeText,
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            paint
        )
        
        // 날짜 그리기
        val dateText = String.format(
            "%04d-%02d-%02d",
            zonedDateTime.year,
            zonedDateTime.monthValue,
            zonedDateTime.dayOfMonth
        )
        
        paint.textSize = 20f
        canvas.drawText(
            dateText,
            bounds.exactCenterX(),
            bounds.exactCenterY() + 80f,
            paint
        )
    }
    
    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: MySharedAssets
    ) {
        // 하이라이트 레이어 (선택사항)
    }
}
```

**AndroidManifest.xml에 서비스 등록:**
```xml
<service
    android:name=".MyWatchFaceService"
    android:exported="true"
    android:permission="android.permission.BIND_WALLPAPER">
    
    <intent-filter>
        <action android:name="android.service.wallpaper.WatchFaceService" />
    </intent-filter>
    
    <meta-data
        android:name="com.google.android.wearable.watchface.preview"
        android:resource="@drawable/watch_face_preview" />
</service>
```

---

## 타일 (Tiles)

> [!NOTE]
> **타일 = 빠른 정보 카드**
> 
> **특징:**
> - 📊 한눈에 보는 정보
> - 🔄 자동 업데이트
> - 👆 탭하여 앱 실행

### 타일 서비스 생성

```kotlin
import androidx.wear.tiles.*

// 타일 서비스
class MyTileService : TileService() {
    
    // 타일 요청
    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        
        // 데이터 로드
        val data = loadData()
        
        // 타일 레이아웃 생성
        val layout = LayoutElementBuilders.Layout.Builder()
            .setRoot(
                LayoutElementBuilders.Box.Builder()
                    .setWidth(DimensionBuilders.expand())
                    .setHeight(DimensionBuilders.expand())
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(
                                        ColorBuilders.argb(0xFF6200EE.toInt())
                                    )
                                    .build()
                            )
                            .setPadding(
                                ModifiersBuilders.Padding.Builder()
                                    .setAll(DimensionBuilders.dp(16f))
                                    .build()
                            )
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                    .setOnClick(
                                        ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName(packageName)
                                                    .setClassName(MainActivity::class.java.name)
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Column.Builder()
                            .setWidth(DimensionBuilders.expand())
                            .setHeight(DimensionBuilders.expand())
                            .setHorizontalAlignment(
                                LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER
                            )
                            .setVerticalAlignment(
                                LayoutElementBuilders.VERTICAL_ALIGN_CENTER
                            )
                            .addContent(
                                LayoutElementBuilders.Text.Builder()
                                    .setText(data.title)
                                    .setFontStyle(
                                        LayoutElementBuilders.FontStyle.Builder()
                                            .setSize(DimensionBuilders.sp(18f))
                                            .setColor(
                                                ColorBuilders.argb(0xFFFFFFFF.toInt())
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .addContent(
                                LayoutElementBuilders.Spacer.Builder()
                                    .setHeight(DimensionBuilders.dp(8f))
                                    .build()
                            )
                            .addContent(
                                LayoutElementBuilders.Text.Builder()
                                    .setText(data.value)
                                    .setFontStyle(
                                        LayoutElementBuilders.FontStyle.Builder()
                                            .setSize(DimensionBuilders.sp(24f))
                                            .setColor(
                                                ColorBuilders.argb(0xFFFFFFFF.toInt())
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
        
        // 타일 생성
        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(layout)
                            .build()
                    )
                    .build()
            )
            .build()
    }
    
    // 리소스 요청
    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1")
            .build()
    }
    
    private suspend fun loadData(): TileData {
        // 데이터 로드
        delay(100)
        return TileData(
            title = "날씨",
            value = "22°"
        )
    }
}

data class TileData(
    val title: String,
    val value: String
)
```

**AndroidManifest.xml에 서비스 등록:**
```xml
<service
    android:name=".MyTileService"
    android:exported="true"
    android:permission="com.google.android.wearable.permission.BIND_TILE_PROVIDER">
    
    <intent-filter>
        <action android:name="androidx.wear.tiles.action.BIND_TILE_PROVIDER" />
    </intent-filter>
    
    <meta-data
        android:name="androidx.wear.tiles.PREVIEW"
        android:resource="@drawable/tile_preview" />
</service>
```

---

## 컴플리케이션

> [!NOTE]
> **컴플리케이션 = 워치 페이스 위젯**
> 
> **타입:**
> - 📊 SHORT_TEXT: 짧은 텍스트
> - 📈 LONG_TEXT: 긴 텍스트
> - 🔢 RANGED_VALUE: 범위 값 (진행률)
> - 🖼️ SMALL_IMAGE: 작은 이미지

### 컴플리케이션 데이터 소스

```kotlin
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.*

// 컴플리케이션 데이터 소스
class MyComplicationDataSourceService : ComplicationDataSourceService() {
    
    // 지원하는 타입
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("22°").build(),
                    contentDescription = PlainComplicationText.Builder("날씨").build()
                )
                    .setTitle(PlainComplicationText.Builder("날씨").build())
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("맑음, 22도").build(),
                    contentDescription = PlainComplicationText.Builder("날씨").build()
                )
                    .setTitle(PlainComplicationText.Builder("오늘 날씨").build())
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 65f,
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder("습도").build()
                )
                    .setText(PlainComplicationText.Builder("65%").build())
                    .setTitle(PlainComplicationText.Builder("습도").build())
                    .build()
            }
            else -> null
        }
    }
    
    // 실제 데이터 제공
    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        // 데이터 로드
        val data = loadComplicationData(request.complicationType)
        
        // 데이터 전달
        listener.onComplicationData(data)
    }
    
    private fun loadComplicationData(type: ComplicationType): ComplicationData? {
        // 실제 데이터 로드 (API, DB 등)
        val temperature = 22
        
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("${temperature}°").build(),
                    contentDescription = PlainComplicationText.Builder("날씨").build()
                )
                    .setTapAction(
                        PendingIntent.getActivity(
                            this,
                            0,
                            Intent(this, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            }
            else -> null
        }
    }
}
```

**AndroidManifest.xml에 서비스 등록:**
```xml
<service
    android:name=".MyComplicationDataSourceService"
    android:exported="true"
    android:permission="com.google.android.wearable.permission.BIND_COMPLICATION_PROVIDER">
    
    <intent-filter>
        <action android:name="android.support.wearable.complications.ACTION_COMPLICATION_UPDATE_REQUEST" />
    </intent-filter>
    
    <meta-data
        android:name="android.support.wearable.complications.SUPPORTED_TYPES"
        android:value="SHORT_TEXT,LONG_TEXT,RANGED_VALUE" />
    
    <meta-data
        android:name="android.support.wearable.complications.UPDATE_PERIOD_SECONDS"
        android:value="1800" />
</service>
```

---

## 건강 및 피트니스

### Health Services 사용

```kotlin
// 의존성 추가
implementation("androidx.health:health-services-client:1.0.0-beta03")

// 권한
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

// 심박수 측정
class HeartRateMonitor(private val context: Context) {
    
    private val healthServicesClient = HealthServices.getClient(context)
    private val measureClient = healthServicesClient.measureClient
    
    suspend fun startHeartRateMeasurement(
        onHeartRate: (Int) -> Unit
    ) {
        // 심박수 측정 시작
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // 가용성 변경
            }
            
            override fun onDataReceived(data: DataPointContainer) {
                // 심박수 데이터 수신
                val heartRateData = data.getData(DataType.HEART_RATE_BPM)
                heartRateData.forEach { dataPoint ->
                    val heartRate = dataPoint.value.toInt()
                    onHeartRate(heartRate)
                }
            }
        }
        
        measureClient.registerMeasureCallback(
            DataType.HEART_RATE_BPM,
            callback
        )
    }
}

// UI
@Composable
fun HeartRateScreen() {
    var heartRate by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val heartRateMonitor = remember { HeartRateMonitor(context) }
    
    LaunchedEffect(Unit) {
        heartRateMonitor.startHeartRateMeasurement { rate ->
            heartRate = rate
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$heartRate",
                style = MaterialTheme.typography.display1
            )
            
            Text(
                text = "BPM",
                style = MaterialTheme.typography.title3
            )
        }
    }
}
```

---

## 실전 예제

### 완전한 운동 추적 앱

```kotlin
// 운동 데이터
data class WorkoutData(
    val duration: Long,  // 초
    val heartRate: Int,
    val calories: Int
)

// 운동 화면
@Composable
fun WorkoutScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var workoutData by remember {
        mutableStateOf(
            WorkoutData(
                duration = 0,
                heartRate = 0,
                calories = 0
            )
        )
    }
    
    // 타이머
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            workoutData = workoutData.copy(
                duration = workoutData.duration + 1,
                calories = workoutData.calories + 1
            )
        }
    }
    
    Scaffold(
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 시간
            item {
                WorkoutMetric(
                    label = "시간",
                    value = formatDuration(workoutData.duration),
                    icon = Icons.Default.Timer
                )
            }
            
            // 심박수
            item {
                WorkoutMetric(
                    label = "심박수",
                    value = "${workoutData.heartRate} BPM",
                    icon = Icons.Default.Favorite
                )
            }
            
            // 칼로리
            item {
                WorkoutMetric(
                    label = "칼로리",
                    value = "${workoutData.calories} kcal",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
            
            // 시작/중지 버튼
            item {
                Chip(
                    label = { Text(if (isRunning) "중지" else "시작") },
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.primaryChipColors()
                )
            }
        }
    }
}

@Composable
fun WorkoutMetric(
    label: String,
    value: String,
    icon: ImageVector
) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.caption1
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.title2
                )
            }
        }
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
```

---

## 💡 베스트 프랙티스

### 1. 작은 화면 최적화

```kotlin
// ✅ 큰 텍스트, 큰 버튼
Text(
    text = "22°",
    style = MaterialTheme.typography.display1  // 큰 폰트
)

Chip(
    modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)  // 충분한 높이
)

// ❌ 작은 텍스트, 작은 버튼
Text(text = "22°", fontSize = 12.sp)
```

### 2. 배터리 효율

```kotlin
// ✅ 적절한 업데이트 주기
android:updatePeriodMillis="1800000"  // 30분

// ❌ 너무 자주 업데이트
android:updatePeriodMillis="1000"  // 1초 (배터리 소모!)
```

### 3. 간단한 UI

```kotlin
// ✅ 핵심 정보만
Column {
    Text("22°")
    Text("맑음")
}

// ❌ 복잡한 UI
LazyColumn {
    items(100) { /* 너무 많음 */ }
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Time is on Your Wrist! ⌚
