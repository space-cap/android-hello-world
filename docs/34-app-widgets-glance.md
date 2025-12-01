# 위젯 (App Widgets) 완벽 가이드

## 📚 목차

1. [위젯이란?](#위젯이란)
2. [Glance 시작하기](#glance-시작하기)
3. [기본 위젯 만들기](#기본-위젯-만들기)
4. [위젯 업데이트](#위젯-업데이트)
5. [위젯 크기 조정](#위젯-크기-조정)
6. [위젯 상호작용](#위젯-상호작용)
7. [고급 위젯](#고급-위젯)
8. [실전 예제](#실전-예제)

---

## 위젯이란?

> [!NOTE]
> **위젯 (App Widget) = 홈 화면에 표시되는 미니 앱**
> 
> **주요 특징:**
> - 📱 홈 화면에 상주
> - 👀 앱 실행 없이 정보 확인
> - 🔄 자동 업데이트
> - 👆 탭하여 앱 실행

### 왜 중요한가?

**사용자 경험:**
```
위젯 없이:
홈 화면 → 앱 실행 → 정보 확인
(3단계)

위젯 사용:
홈 화면 → 정보 확인
(1단계!)
```

**통계:**
- Android 사용자의 **68%**가 위젯 사용
- 위젯이 있는 앱의 사용 빈도: **3배 증가**
- 위젯을 통한 앱 실행: **40%**

**인기 위젯 예시:**
```
날씨: 현재 날씨와 예보
시계: 시간과 알람
음악: 재생 컨트롤
캘린더: 오늘 일정
할 일: 체크리스트
```

---

## Glance 시작하기

> [!IMPORTANT]
> **Glance = Jetpack Compose for Widgets**
> 
> **왜 Glance를 사용하는가?**
> - ✅ Compose와 유사한 문법
> - ✅ 간단한 코드
> - ✅ 자동 레이아웃 관리
> - ✅ 다크 모드 지원

### 기존 방식 vs Glance

**기존 방식 (RemoteViews):**
```kotlin
// 복잡한 XML 레이아웃
// RemoteViews로 수동 업데이트
val views = RemoteViews(context.packageName, R.layout.widget_layout)
views.setTextViewText(R.id.widget_text, "Hello")
views.setImageViewResource(R.id.widget_icon, R.drawable.icon)
// ... 수십 줄의 코드
```

**Glance (최신):**
```kotlin
// Compose와 유사한 코드
Text("Hello")
Image(ImageProvider(R.drawable.icon))
// 간단!
```

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // Glance (Jetpack Compose for Widgets)
    val glanceVersion = "1.0.0"
    implementation("androidx.glance:glance-appwidget:$glanceVersion")
    
    // Material 3 for Glance
    implementation("androidx.glance:glance-material3:$glanceVersion")
}
```

---

## 기본 위젯 만들기

### 1단계: 위젯 정보 XML 생성

```xml
<!-- res/xml/my_widget_info.xml -->
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/widget_loading"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_description"
    android:previewImage="@drawable/widget_preview" />
```

**각 속성 설명:**
```xml
android:minWidth="180dp"
<!-- 최소 너비 (3x2 셀 = 180dp x 110dp) -->

android:minHeight="110dp"
<!-- 최소 높이 -->

android:updatePeriodMillis="1800000"
<!-- 업데이트 주기 (밀리초, 최소 30분 = 1800000) -->

android:initialLayout="@layout/widget_loading"
<!-- 로딩 중 표시할 레이아웃 -->

android:resizeMode="horizontal|vertical"
<!-- 크기 조정 방향 (가로, 세로, 둘 다) -->

android:widgetCategory="home_screen"
<!-- 위젯 카테고리 (홈 화면, 잠금 화면) -->

android:description="@string/widget_description"
<!-- 위젯 설명 (위젯 선택 시 표시) -->

android:previewImage="@drawable/widget_preview"
<!-- 미리보기 이미지 -->
```

### 2단계: Glance 위젯 클래스 생성

```kotlin
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent

// Glance 위젯 클래스
class MyWidget : GlanceAppWidget() {
    
    // 위젯 UI 정의
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 데이터 로드 (선택사항)
        val data = loadData()
        
        // UI 제공
        provideContent {
            // Compose와 유사한 UI
            MyWidgetContent(data)
        }
    }
    
    private suspend fun loadData(): String {
        // 데이터 로드 (예: API 호출, DB 조회)
        delay(100)
        return "Hello, Widget!"
    }
}

// 위젯 UI
@Composable
fun MyWidgetContent(data: String) {
    // Glance Composable 사용
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 텍스트
        Text(
            text = data,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.Black)
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // 아이콘
        Image(
            provider = ImageProvider(R.drawable.ic_widget),
            contentDescription = "위젯 아이콘",
            modifier = GlanceModifier.size(48.dp)
        )
    }
}
```

**Glance vs Compose 차이:**
```kotlin
// Compose
Modifier.fillMaxSize()
Color.White
Text("Hello")

// Glance
GlanceModifier.fillMaxSize()
ColorProvider(Color.White)  // 다크 모드 지원
Text("Hello")  // 동일!
```

### 3단계: 위젯 Receiver 생성

```kotlin
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// 위젯 Receiver
class MyWidgetReceiver : GlanceAppWidgetReceiver() {
    
    // 위젯 인스턴스 제공
    override val glanceAppWidget: GlanceAppWidget = MyWidget()
}
```

### 4단계: AndroidManifest.xml 등록

```xml
<!-- AndroidManifest.xml -->
<application>
    <!-- 위젯 Receiver 등록 -->
    <receiver
        android:name=".MyWidgetReceiver"
        android:exported="true">
        
        <!-- 위젯 업데이트 인텐트 -->
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        
        <!-- 위젯 정보 메타데이터 -->
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/my_widget_info" />
    </receiver>
</application>
```

---

## 위젯 업데이트

### 수동 업데이트

```kotlin
import androidx.glance.appwidget.updateAll

// 위젯 클래스
class MyWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // 현재 시간 표시
            val currentTime = remember {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            }
            
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("현재 시간: $currentTime")
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                // 새로고침 버튼
                Button(
                    text = "새로고침",
                    onClick = actionRunCallback<RefreshAction>()
                )
            }
        }
    }
}

// 새로고침 액션
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 위젯 업데이트
        MyWidget().update(context, glanceId)
    }
}
```

### 모든 위젯 업데이트

```kotlin
// 모든 위젯 인스턴스 업데이트
suspend fun updateAllWidgets(context: Context) {
    MyWidget().updateAll(context)
}

// 사용 예시 (Activity에서)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            Button(
                onClick = {
                    // 코루틴에서 실행
                    lifecycleScope.launch {
                        updateAllWidgets(this@MainActivity)
                    }
                }
            ) {
                Text("위젯 업데이트")
            }
        }
    }
}
```

### WorkManager로 주기적 업데이트

```kotlin
import androidx.work.*
import java.util.concurrent.TimeUnit

// Worker 클래스
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // 위젯 업데이트
        MyWidget().updateAll(applicationContext)
        
        return Result.success()
    }
}

// 주기적 업데이트 스케줄
fun scheduleWidgetUpdates(context: Context) {
    // 15분마다 업데이트
    val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
        repeatInterval = 15,
        repeatIntervalTimeUnit = TimeUnit.MINUTES
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)  // 배터리 절약
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "widget_update",
        ExistingPeriodicWorkPolicy.KEEP,
        updateRequest
    )
}
```

---

## 위젯 크기 조정

### 다양한 크기 지원

```kotlin
import androidx.glance.LocalSize
import androidx.glance.appwidget.SizeMode

class MyWidget : GlanceAppWidget() {
    
    // 크기 모드 설정
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),  // 작은 크기 (3x2)
            DpSize(270.dp, 110.dp),  // 중간 크기 (4x2)
            DpSize(270.dp, 280.dp)   // 큰 크기 (4x4)
        )
    )
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // 현재 크기에 따라 다른 UI
            val size = LocalSize.current
            
            when {
                size.width < 200.dp -> SmallWidgetContent()
                size.width < 300.dp -> MediumWidgetContent()
                else -> LargeWidgetContent()
            }
        }
    }
}

// 작은 위젯
@Composable
fun SmallWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6200EE)))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "작은 위젯",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 14.sp
            )
        )
    }
}

// 중간 위젯
@Composable
fun MediumWidgetContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6200EE)))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget),
            contentDescription = null,
            modifier = GlanceModifier.size(40.dp)
        )
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        Column {
            Text(
                text = "중간 위젯",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "더 많은 정보",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp
                )
            )
        }
    }
}

// 큰 위젯
@Composable
fun LargeWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF6200EE)))
            .padding(16.dp)
    ) {
        Text(
            text = "큰 위젯",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // 리스트 표시
        repeat(5) { index ->
            Text(
                text = "항목 ${index + 1}",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
    }
}
```

**위젯 크기 가이드:**
```
1x1: 40dp x 40dp
2x1: 110dp x 40dp
3x2: 180dp x 110dp (최소 권장)
4x2: 250dp x 110dp
4x4: 250dp x 250dp
```

---

## 위젯 상호작용

### 버튼 클릭

```kotlin
@Composable
fun InteractiveWidget() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var count by remember { mutableStateOf(0) }
        
        Text("카운트: $count")
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // 증가 버튼
        Button(
            text = "증가",
            onClick = actionRunCallback<IncrementAction>()
        )
    }
}

// 증가 액션
class IncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 데이터 저장 (SharedPreferences)
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("count", 0)
        prefs.edit().putInt("count", currentCount + 1).apply()
        
        // 위젯 업데이트
        MyWidget().update(context, glanceId)
    }
}
```

### 앱 실행

```kotlin
@Composable
fun WidgetWithAppLaunch() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(
                // 앱 실행
                onClick = actionStartActivity<MainActivity>()
            )
            .padding(16.dp)
    ) {
        Text("탭하여 앱 열기")
    }
}

// 특정 화면으로 이동
@Composable
fun WidgetWithDeepLink() {
    Button(
        text = "상품 보기",
        onClick = actionStartActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("myapp://product/detail/12345")
            )
        )
    )
}
```

### 리스트 위젯

```kotlin
@Composable
fun ListWidget() {
    LazyColumn(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        items(10) { index ->
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = actionRunCallback<ItemClickAction>(
                            actionParametersOf(
                                ItemClickAction.ItemIdKey to index
                            )
                        )
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_item),
                    contentDescription = null,
                    modifier = GlanceModifier.size(32.dp)
                )
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                Text("항목 ${index + 1}")
            }
        }
    }
}

// 항목 클릭 액션
class ItemClickAction : ActionCallback {
    companion object {
        val ItemIdKey = ActionParameters.Key<Int>("item_id")
    }
    
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val itemId = parameters[ItemIdKey] ?: return
        
        // 항목 클릭 처리
        Toast.makeText(context, "항목 $itemId 클릭됨", Toast.LENGTH_SHORT).show()
        
        // 앱 실행
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("item_id", itemId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
```

---

## 고급 위젯

### 다크 모드 지원

```kotlin
@Composable
fun DarkModeWidget() {
    // ColorProvider로 자동 다크 모드 지원
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    day = Color.White,    // 라이트 모드
                    night = Color.Black   // 다크 모드
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = "다크 모드 지원",
            style = TextStyle(
                color = ColorProvider(
                    day = Color.Black,
                    night = Color.White
                )
            )
        )
    }
}
```

### 이미지 로딩

```kotlin
@Composable
fun ImageWidget() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 리소스 이미지
        Image(
            provider = ImageProvider(R.drawable.image),
            contentDescription = "이미지",
            modifier = GlanceModifier.size(100.dp)
        )
        
        // URL 이미지 (Glance는 직접 지원 안함)
        // WorkManager로 다운로드 후 표시
    }
}
```

### 설정 화면

```kotlin
// 위젯 설정 Activity
class WidgetConfigActivity : ComponentActivity() {
    
    private val glanceId by lazy {
        GlanceAppWidgetManager(this)
            .getGlanceIdBy(intent)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var selectedColor by remember { mutableStateOf(Color.Blue) }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "위젯 색상 선택",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 색상 선택
                ColorOption(Color.Blue, "파란색", selectedColor) {
                    selectedColor = it
                }
                ColorOption(Color.Red, "빨간색", selectedColor) {
                    selectedColor = it
                }
                ColorOption(Color.Green, "초록색", selectedColor) {
                    selectedColor = it
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 저장 버튼
                Button(
                    onClick = {
                        // 설정 저장
                        saveWidgetConfig(selectedColor)
                        
                        // 위젯 업데이트
                        lifecycleScope.launch {
                            MyWidget().update(this@WidgetConfigActivity, glanceId)
                        }
                        
                        // 결과 반환
                        setResult(RESULT_OK)
                        finish()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("저장")
                }
            }
        }
    }
    
    private fun saveWidgetConfig(color: Color) {
        val prefs = getSharedPreferences("widget_config", MODE_PRIVATE)
        prefs.edit()
            .putInt("color", color.toArgb())
            .apply()
    }
}

@Composable
fun ColorOption(
    color: Color,
    name: String,
    selectedColor: Color,
    onSelect: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(color) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = color == selectedColor,
            onClick = { onSelect(color) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(name)
    }
}
```

**AndroidManifest.xml에 설정 Activity 등록:**
```xml
<receiver android:name=".MyWidgetReceiver">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/my_widget_info" />
</receiver>

<!-- 설정 Activity -->
<activity
    android:name=".WidgetConfigActivity"
    android:exported="false">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>
```

**위젯 정보 XML에 설정 추가:**
```xml
<appwidget-provider
    ...
    android:configure="com.example.myapp.WidgetConfigActivity" />
```

---

## 실전 예제

### 완전한 날씨 위젯

```kotlin
// 날씨 데이터 클래스
data class WeatherData(
    val temperature: Int,
    val condition: String,
    val icon: Int
)

// 날씨 위젯
class WeatherWidget : GlanceAppWidget() {
    
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(270.dp, 180.dp)
        )
    )
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 날씨 데이터 로드
        val weather = loadWeatherData(context)
        
        provideContent {
            val size = LocalSize.current
            
            if (size.width < 200.dp) {
                SmallWeatherWidget(weather)
            } else {
                LargeWeatherWidget(weather)
            }
        }
    }
    
    private suspend fun loadWeatherData(context: Context): WeatherData {
        // API 호출 또는 캐시된 데이터 로드
        return WeatherData(
            temperature = 22,
            condition = "맑음",
            icon = R.drawable.ic_sunny
        )
    }
}

// 작은 날씨 위젯
@Composable
fun SmallWeatherWidget(weather: WeatherData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    day = Color(0xFF2196F3),
                    night = Color(0xFF1565C0)
                )
            )
            .padding(12.dp)
            .clickable(onClick = actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(weather.icon),
            contentDescription = weather.condition,
            modifier = GlanceModifier.size(48.dp)
        )
        
        Spacer(modifier = GlanceModifier.height(4.dp))
        
        Text(
            text = "${weather.temperature}°",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = weather.condition,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 12.sp
            )
        )
    }
}

// 큰 날씨 위젯
@Composable
fun LargeWeatherWidget(weather: WeatherData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    day = Color(0xFF2196F3),
                    night = Color(0xFF1565C0)
                )
            )
            .padding(16.dp)
    ) {
        // 헤더
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "서울",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "오늘",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 12.sp
                    )
                )
            }
            
            Button(
                text = "새로고침",
                onClick = actionRunCallback<RefreshWeatherAction>(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(Color.White.copy(alpha = 0.2f)),
                    contentColor = ColorProvider(Color.White)
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // 현재 날씨
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(weather.icon),
                contentDescription = null,
                modifier = GlanceModifier.size(64.dp)
            )
            
            Spacer(modifier = GlanceModifier.width(16.dp))
            
            Column {
                Text(
                    text = "${weather.temperature}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = weather.condition,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // 추가 정보
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.SpaceEvenly
        ) {
            WeatherInfo("습도", "65%")
            WeatherInfo("바람", "5m/s")
            WeatherInfo("강수", "0%")
        }
    }
}

@Composable
fun WeatherInfo(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                fontSize = 12.sp
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// 새로고침 액션
class RefreshWeatherAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 날씨 데이터 새로고침
        // API 호출...
        
        // 위젯 업데이트
        WeatherWidget().update(context, glanceId)
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 배터리 효율

```kotlin
// ✅ 적절한 업데이트 주기
android:updatePeriodMillis="1800000"  // 30분 (최소값)

// ✅ 제약 조건 사용
.setConstraints(
    Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()
)

// ❌ 너무 자주 업데이트
android:updatePeriodMillis="60000"  // 1분 (배터리 소모!)
```

### 2. 간단한 UI

```kotlin
// ✅ 간단하고 명확한 정보
Text("22°")
Image(icon)

// ❌ 복잡한 UI (위젯에 부적합)
LazyColumn { /* 100개 항목 */ }
```

### 3. 빠른 로딩

```kotlin
// ✅ 캐시된 데이터 사용
val cachedData = loadFromCache()

// ✅ 비동기 로딩
suspend fun loadData() {
    withContext(Dispatchers.IO) {
        // 네트워크 요청
    }
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Widget Your Way! 📱
