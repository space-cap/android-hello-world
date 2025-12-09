# Android WindowManager 기본 가이드

> 📖 **시리즈 구성**
> - **73-1**: [폴더블의 역사](./73-1-foldable-history.md) - 폴더블 디바이스의 발전과 대형 화면 시대
> - **73-2**: WindowManager 기본 가이드 (현재 문서) - Window Size Classes부터 Foldable 지원까지
> - **73-3**: [WindowManager 고급 가이드](./73-3-android-window-manager-advanced.md) - 반응형 패턴과 최적화
> - **73-4**: [WindowManager 실전 프로젝트](./73-4-android-window-manager-projects.md) - 이메일, 뉴스, 설정 앱

---

## 📚 목차

1. [WindowManager란?](#windowmanager란)
2. [Window Size Classes](#window-size-classes)
3. [Foldable 기기 지원](#foldable-기기-지원)
4. [멀티 윈도우](#멀티-윈도우)
5. [기본 반응형 레이아웃](#기본-반응형-레이아웃)

---

## WindowManager란?

### 🌟 대형 화면 시대의 도래

스마트폰만의 시대는 끝났습니다. 이제는 다양한 화면 크기를 지원해야 합니다:

```
📱 스마트폰 (Compact)
    ↓
📱📱 폴더블 (Medium/Expanded)
    ↓
📱📱📱 태블릿 (Expanded)
    ↓
🖥️ 데스크톱 (Expanded)
```

### 📊 시장 현황

| 디바이스 유형 | 시장 점유율 | 성장률 |
|-------------|-----------|--------|
| 스마트폰 | 70% | 정체 |
| 태블릿 | 20% | ↗️ 증가 |
| 폴더블 | 5% | ↗️↗️ 급증 |
| 데스크톱 (ChromeOS) | 5% | ↗️ 증가 |

> [!IMPORTANT]
> 2024년 기준, Android 앱의 **30%**가 태블릿이나 대형 화면에서 실행됩니다.
> 대형 화면 최적화는 선택이 아닌 **필수**입니다!

### 🎯 WindowManager란?

**Jetpack WindowManager**는 대형 화면(태블릿, 폴더블, 데스크톱)에 최적화된 앱을 만들기 위한 라이브러리입니다.

#### 주요 기능

| 기능 | 설명 | 사용 사례 |
|------|------|----------|
| 🪟 **Window Size Classes** | 화면 크기 분류 | 반응형 레이아웃 |
| 📲 **Foldable 지원** | 폴더블 상태 감지 | Flex Mode UI |
| 🖥️ **멀티 윈도우** | 여러 창 관리 | 분할 화면 |
| 🔄 **Activity Embedding** | 여러 Activity 동시 표시 | Master-Detail |

### 🤔 왜 WindowManager가 필요한가?

#### Before: 스마트폰만 고려

```kotlin
// ❌ 나쁜 예: 모든 화면에서 동일한 UI
@Composable
fun MyApp() {
    Column {
        TopBar()
        Content()  // 태블릿에서는 공간 낭비!
    }
}
```

#### After: 모든 화면 크기 고려

```kotlin
// ✅ 좋은 예: 화면 크기에 따라 다른 UI
@Composable
fun MyApp() {
    val windowSizeClass = calculateWindowSizeClass()
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 스마트폰: 단일 패널
            SinglePaneLayout()
        }
        WindowWidthSizeClass.Expanded -> {
            // 태블릿: 듀얼 패널
            DualPaneLayout()
        }
    }
}
```

---

## Window Size Classes

### 📐 Window Size Classes란?

화면 크기를 **3가지 카테고리**로 분류하는 시스템입니다.

```
Compact     Medium      Expanded
(< 600dp)   (600-840dp) (> 840dp)
   📱          📱📱         📱📱📱
```

### 🔧 의존성 추가

**build.gradle.kts (Module: app)**:

```kotlin
dependencies {
    // WindowManager
    implementation("androidx.window:window:1.2.0")
    
    // Compose와 통합
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
}
```

### 📊 Window Size Classes 종류

#### 너비 기준 (Width Size Class)

```kotlin
enum class WindowWidthSizeClass {
    /**
     * Compact: 좁은 화면 (< 600dp)
     * 
     * 디바이스:
     * - 스마트폰 (세로/가로 모두)
     * - 폴더블 (접힌 상태)
     * 
     * UI 패턴:
     * - 단일 패널
     * - Bottom Navigation
     * - 전체 화면 다이얼로그
     */
    Compact,
    
    /**
     * Medium: 중간 화면 (600dp ~ 840dp)
     * 
     * 디바이스:
     * - 태블릿 (세로 모드)
     * - 폴더블 (펼친 상태, 세로)
     * 
     * UI 패턴:
     * - 단일 패널 (여백 추가)
     * - Navigation Rail
     * - 모달 다이얼로그
     */
    Medium,
    
    /**
     * Expanded: 넓은 화면 (> 840dp)
     * 
     * 디바이스:
     * - 태블릿 (가로 모드)
     * - 폴더블 (펼친 상태, 가로)
     * - 데스크톱
     * 
     * UI 패턴:
     * - 듀얼 패널 (Master-Detail)
     * - Navigation Rail + Content
     * - 인라인 다이얼로그
     */
    Expanded
}
```

#### 높이 기준 (Height Size Class)

```kotlin
enum class WindowHeightSizeClass {
    Compact,   // < 480dp
    Medium,    // 480dp ~ 900dp
    Expanded   // > 900dp
}
```

### 💻 Window Size Class 계산

```kotlin
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

/**
 * Window Size Class를 계산하는 Composable
 * 
 * 화면 크기가 변경될 때마다 자동으로 재계산됩니다.
 * (예: 폴더블 접기/펼치기, 화면 회전, 멀티 윈도우)
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MyApp() {
    // Activity 컨텍스트 필요
    val activity = LocalContext.current as Activity
    
    // Window Size Class 계산
    val windowSizeClass = calculateWindowSizeClass(activity)
    
    // 너비 클래스
    val widthClass = windowSizeClass.widthSizeClass
    
    // 높이 클래스
    val heightClass = windowSizeClass.heightSizeClass
    
    // 로깅
    Log.d("WindowSize", """
        너비: ${widthClass.name}
        높이: ${heightClass.name}
    """.trimIndent())
    
    // 레이아웃 선택
    AdaptiveLayout(windowSizeClass)
}
```

### 🎨 반응형 레이아웃 구현

#### 기본 패턴

```kotlin
/**
 * Window Size Class에 따라 레이아웃 선택
 */
@Composable
fun AdaptiveLayout(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 📱 스마트폰: 단일 패널
            CompactLayout()
        }
        
        WindowWidthSizeClass.Medium -> {
            // 📱📱 태블릿 세로: 단일 패널 + 여백
            MediumLayout()
        }
        
        WindowWidthSizeClass.Expanded -> {
            // 📱📱📱 태블릿 가로: 듀얼 패널
            ExpandedLayout()
        }
    }
}

/**
 * Compact 레이아웃 (스마트폰)
 */
@Composable
fun CompactLayout() {
    Scaffold(
        // Bottom Navigation Bar
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("홈") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("검색") },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        // 전체 화면 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopBar()
            Content(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Medium 레이아웃 (태블릿 세로)
 */
@Composable
fun MediumLayout() {
    Row {
        // Navigation Rail (세로 네비게이션)
        NavigationRail {
            NavigationRailItem(
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("홈") },
                selected = true,
                onClick = { }
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.Search, null) },
                label = { Text("검색") },
                selected = false,
                onClick = { }
            )
        }
        
        // 콘텐츠 (중앙 정렬, 최대 너비 제한)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            TopBar()
            Content(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)  // 최대 너비 제한
            )
        }
    }
}

/**
 * Expanded 레이아웃 (태블릿 가로)
 */
@Composable
fun ExpandedLayout() {
    Row {
        // Navigation Rail
        NavigationRail(
            modifier = Modifier.width(80.dp)
        ) {
            NavigationRailItem(
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("홈") },
                selected = true,
                onClick = { }
            )
        }
        
        // 듀얼 패널
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 왼쪽 패널 (목록)
            Card(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                ListPane()
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 오른쪽 패널 (상세)
            Card(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                DetailPane()
            }
        }
    }
}
```

### 💡 Window Size Class 활용 팁

```kotlin
/**
 * Window Size Class 베스트 프랙티스
 */
object WindowSizeClassTips {
    
    /**
     * 1. 상태 호이스팅
     * 
     * Window Size Class를 최상위에서 계산하고
     * 하위 Composable에 전달
     */
    @Composable
    fun GoodExample() {
        val windowSizeClass = calculateWindowSizeClass()
        
        // 하위 Composable에 전달
        MyScreen(windowSizeClass = windowSizeClass)
    }
    
    /**
     * 2. 중단점(Breakpoint) 활용
     * 
     * 필요시 커스텀 중단점 사용
     */
    @Composable
    fun CustomBreakpoints() {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        
        when {
            screenWidth < 600 -> CompactLayout()
            screenWidth < 840 -> MediumLayout()
            else -> ExpandedLayout()
        }
    }
    
    /**
     * 3. 점진적 향상
     * 
     * Compact부터 시작해서 점진적으로 확장
     */
    fun developmentOrder() {
        // 1단계: Compact 레이아웃 완성
        // 2단계: Medium 레이아웃 추가
        // 3단계: Expanded 레이아웃 추가
    }
    
    /**
     * 4. 테스트
     * 
     * 다양한 화면 크기에서 테스트
     */
    fun testDevices() {
        // - Pixel 5 (Compact)
        // - Pixel Tablet (Medium/Expanded)
        // - Galaxy Fold (Compact/Expanded)
    }
}
```

---

## Foldable 기기 지원

### 📱 Foldable 기기란?

접을 수 있는 스마트폰으로, 화면 크기가 동적으로 변합니다.

#### 주요 Foldable 기기

| 기기 | 제조사 | 화면 크기 (펼침) | 특징 |
|------|--------|----------------|------|
| Galaxy Z Fold | Samsung | 7.6" | 책처럼 접힘 |
| Galaxy Z Flip | Samsung | 6.7" | 조개처럼 접힘 |
| Pixel Fold | Google | 7.6" | 책처럼 접힘 |

### 🔧 Foldable 상태 감지

```kotlin
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo

/**
 * Foldable 상태를 감지하는 Composable
 */
@Composable
fun FoldableAwareLayout() {
    val context = LocalContext.current
    
    // WindowInfoTracker 생성
    val windowInfoTracker = remember {
        WindowInfoTracker.getOrCreate(context)
    }
    
    // WindowLayoutInfo 수집
    val windowLayoutInfo by windowInfoTracker
        .windowLayoutInfo(context)
        .collectAsState(initial = null)
    
    // FoldingFeature 추출
    val foldingFeature = windowLayoutInfo?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull()
    
    // 폴더블 상태에 따라 레이아웃 선택
    when {
        foldingFeature == null -> {
            // 일반 기기 또는 완전히 펼쳐진 상태
            NormalLayout()
        }
        
        foldingFeature.state == FoldingFeature.State.HALF_OPENED -> {
            // 반쯤 접힌 상태 (Flex Mode)
            FlexModeLayout(foldingFeature)
        }
        
        foldingFeature.state == FoldingFeature.State.FLAT -> {
            // 완전히 펼쳐진 상태
            FlatLayout(foldingFeature)
        }
    }
}
```

### 📐 FoldingFeature 속성

```kotlin
/**
 * FoldingFeature의 주요 속성
 */
fun analyzeFoldingFeature(feature: FoldingFeature) {
    // 1. 상태 (State)
    when (feature.state) {
        FoldingFeature.State.FLAT -> {
            // 완전히 펼쳐진 상태 (180도)
            Log.d("Foldable", "완전히 펼쳐짐")
        }
        
        FoldingFeature.State.HALF_OPENED -> {
            // 반쯤 접힌 상태 (90도 ~ 180도 사이)
            Log.d("Foldable", "반쯤 접힘 (Flex Mode)")
        }
    }
    
    // 2. 방향 (Orientation)
    when (feature.orientation) {
        FoldingFeature.Orientation.HORIZONTAL -> {
            // 가로 힌지 (위아래로 접힘)
            // 예: Galaxy Z Flip
            Log.d("Foldable", "가로 힌지")
        }
        
        FoldingFeature.Orientation.VERTICAL -> {
            // 세로 힌지 (좌우로 접힘)
            // 예: Galaxy Z Fold
            Log.d("Foldable", "세로 힌지")
        }
    }
    
    // 3. 경계 (Bounds)
    val bounds = feature.bounds
    Log.d("Foldable", """
        힌지 위치:
        - Left: ${bounds.left}
        - Top: ${bounds.top}
        - Right: ${bounds.right}
        - Bottom: ${bounds.bottom}
    """.trimIndent())
    
    // 4. 가림 여부 (Occlusion Type)
    when (feature.occlusionType) {
        FoldingFeature.OcclusionType.NONE -> {
            // 힌지가 화면을 가리지 않음
            Log.d("Foldable", "힌지 영역 투명")
        }
        
        FoldingFeature.OcclusionType.FULL -> {
            // 힌지가 화면을 완전히 가림
            Log.d("Foldable", "힌지 영역 불투명")
        }
    }
}
```

### 🎮 Flex Mode 구현

**Flex Mode**는 기기를 반쯤 접었을 때의 특별한 UI 모드입니다.

```kotlin
/**
 * Flex Mode 레이아웃
 * 
 * 사용 사례:
 * - 동영상 플레이어: 위쪽에 영상, 아래쪽에 컨트롤
 * - 카메라: 위쪽에 프리뷰, 아래쪽에 버튼
 * - 화상 통화: 위쪽에 상대방, 아래쪽에 나
 */
@Composable
fun FlexModeLayout(foldingFeature: FoldingFeature) {
    // 힌지가 가로인지 세로인지 확인
    if (foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL) {
        // 가로 힌지: 위아래로 분할
        Column(modifier = Modifier.fillMaxSize()) {
            // 위쪽 패널 (힌지 위)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "메인 콘텐츠\n(동영상, 카메라 프리뷰 등)",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            
            // 힌지 영역 (비워둠)
            Spacer(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        foldingFeature.bounds.height().toDp()
                    }
                )
            )
            
            // 아래쪽 패널 (힌지 아래)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("컨트롤 영역")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { }) { Text("재생") }
                        Button(onClick = { }) { Text("일시정지") }
                    }
                }
            }
        }
    } else {
        // 세로 힌지: 좌우로 분할
        Row(modifier = Modifier.fillMaxSize()) {
            // 왼쪽 패널
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text("왼쪽 패널", modifier = Modifier.align(Alignment.Center))
            }
            
            // 힌지 영역
            Spacer(
                modifier = Modifier.width(
                    with(LocalDensity.current) {
                        foldingFeature.bounds.width().toDp()
                    }
                )
            )
            
            // 오른쪽 패널
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text("오른쪽 패널", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
```

### 🎬 실전 예제: 동영상 플레이어

```kotlin
/**
 * Flex Mode를 활용한 동영상 플레이어
 */
@Composable
fun VideoPlayerWithFlexMode() {
    val context = LocalContext.current
    val windowInfoTracker = remember { WindowInfoTracker.getOrCreate(context) }
    val windowLayoutInfo by windowInfoTracker
        .windowLayoutInfo(context)
        .collectAsState(initial = null)
    
    val foldingFeature = windowLayoutInfo?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull()
    
    // Flex Mode 여부 확인
    val isFlexMode = foldingFeature?.state == FoldingFeature.State.HALF_OPENED &&
                     foldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
    
    if (isFlexMode && foldingFeature != null) {
        // Flex Mode: 위아래 분할
        Column(modifier = Modifier.fillMaxSize()) {
            // 위쪽: 동영상
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                // 동영상 플레이어 (실제로는 ExoPlayer 등 사용)
                Text(
                    text = "🎬 동영상 재생 중",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // 힌지 영역
            Spacer(
                modifier = Modifier.height(
                    with(LocalDensity.current) {
                        foldingFeature.bounds.height().toDp()
                    }
                )
            )
            
            // 아래쪽: 컨트롤
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "동영상 제목",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 재생 컨트롤
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.SkipPrevious, "이전")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.PlayArrow, "재생")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.SkipNext, "다음")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 진행 바
                Slider(
                    value = 0.5f,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        // 일반 모드: 전체 화면
        Box(modifier = Modifier.fillMaxSize()) {
            // 동영상 전체 화면
            Text(
                text = "🎬 전체 화면 동영상",
                modifier = Modifier.align(Alignment.Center)
            )
            
            // 컨트롤 오버레이
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White)
                }
            }
        }
    }
}
```

---

## 멀티 윈도우

### 🖥️ 멀티 윈도우란?

여러 앱을 동시에 화면에 표시하는 기능입니다.

```
┌─────────────────┐
│   앱 A (50%)    │
├─────────────────┤
│   앱 B (50%)    │
└─────────────────┘
```

### 📱 멀티 윈도우 모드 감지

```kotlin
/**
 * 멀티 윈도우 모드 확인
 */
class MultiWindowHelper(private val activity: Activity) {
    
    /**
     * 현재 멀티 윈도우 모드인지 확인
     * 
     * @return true: 멀티 윈도우 모드, false: 전체 화면
     */
    fun isInMultiWindowMode(): Boolean {
        return activity.isInMultiWindowMode
    }
    
    /**
     * Picture-in-Picture 모드인지 확인
     * 
     * @return true: PiP 모드, false: 일반 모드
     */
    fun isInPictureInPictureMode(): Boolean {
        return activity.isInPictureInPictureMode
    }
}
```

### 🔄 멀티 윈도우 모드 변경 처리

```kotlin
/**
 * 멀티 윈도우 모드 변경을 처리하는 Activity
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MultiWindowAwareApp()
        }
    }
    
    /**
     * 멀티 윈도우 모드 변경 콜백
     * 
     * 이 메서드는 다음 경우에 호출됩니다:
     * - 전체 화면 → 멀티 윈도우
     * - 멀티 윈도우 → 전체 화면
     * - 분할 비율 변경
     */
    override fun onMultiWindowModeChanged(
        isInMultiWindowMode: Boolean,
        newConfig: Configuration
    ) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        
        if (isInMultiWindowMode) {
            Log.d("MultiWindow", "멀티 윈도우 모드 진입")
            // 레이아웃 조정: 더 컴팩트한 UI
        } else {
            Log.d("MultiWindow", "전체 화면 모드")
            // 레이아웃 조정: 전체 화면 UI
        }
    }
}

/**
 * 멀티 윈도우를 인식하는 Composable
 */
@Composable
fun MultiWindowAwareApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // 멀티 윈도우 상태
    var isMultiWindow by remember {
        mutableStateOf(activity?.isInMultiWindowMode ?: false)
    }
    
    // 설정 변경 감지
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        isMultiWindow = activity?.isInMultiWindowMode ?: false
    }
    
    if (isMultiWindow) {
        // 멀티 윈도우: 컴팩트한 UI
        CompactUI()
    } else {
        // 전체 화면: 일반 UI
        NormalUI()
    }
}
```

---

## 기본 반응형 레이아웃

### 🎨 네비게이션 패턴

화면 크기에 따라 다른 네비게이션을 사용합니다.

```kotlin
/**
 * 반응형 네비게이션
 */
@Composable
fun AdaptiveNavigation(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Bottom Navigation
            BottomNavigationLayout()
        }
        
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> {
            // Navigation Rail
            NavigationRailLayout()
        }
    }
}

@Composable
fun BottomNavigationLayout() {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("홈") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("즐겨찾기") },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("설정") },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        Content(modifier = Modifier.padding(padding))
    }
}

@Composable
fun NavigationRailLayout() {
    Row {
        NavigationRail {
            NavigationRailItem(
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("홈") },
                selected = true,
                onClick = { }
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.Favorite, null) },
                label = { Text("즐겨찾기") },
                selected = false,
                onClick = { }
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.Settings, null) },
                label = { Text("설정") },
                selected = false,
                onClick = { }
            )
        }
        
        Content(modifier = Modifier.fillMaxSize())
    }
}
```

---

## 🎯 다음 단계

기본편을 완료했습니다! 이제 다음 문서로 넘어가세요:

1. **[WindowManager 고급 가이드](./73-3-android-window-manager-advanced.md)** - 반응형 패턴, Activity Embedding
2. **[WindowManager 실전 프로젝트](./73-4-android-window-manager-projects.md)** - 이메일, 뉴스, 설정 앱

---

## 📚 참고 자료

- [WindowManager 공식 문서](https://developer.android.com/jetpack/androidx/releases/window)
- [Large Screen 가이드](https://developer.android.com/guide/topics/large-screens)
- [Material Design 3 - Adaptive layouts](https://m3.material.io/foundations/adaptive-design)

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy Large Screen Development! 🚀
