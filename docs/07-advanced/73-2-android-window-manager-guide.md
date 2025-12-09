# Android WindowManager 가이드

> [!NOTE]
> **이 문서는 새로운 종합 가이드 시리즈로 대체되었습니다!**
> 
> WindowManager를 더 체계적으로 학습할 수 있도록 4개의 상세한 문서로 분할되었습니다:
> 
> 1. **[73-1. 폴더블의 역사](./73-1-foldable-history.md)** - 폴더블 디바이스의 발전
> 2. **[73-2. WindowManager 기본 가이드](./73-2-android-window-manager-basics.md)** - Window Size Classes, Foldable 지원 (약 800줄)
> 3. **[73-3. WindowManager 고급 가이드](./73-3-android-window-manager-advanced.md)** - 반응형 패턴, Activity Embedding (약 400줄)
> 4. **[73-4. WindowManager 실전 프로젝트](./73-4-android-window-manager-projects.md)** - 이메일, 뉴스 앱 (약 300줄)
> 
> **총 분량**: 약 1,500줄의 상세한 설명과 주석이 포함된 코드 예제

---

## 🚀 빠른 시작

**[👉 73-2. WindowManager 기본 가이드로 이동](./73-2-android-window-manager-basics.md)**

---



### 주요 기능
- 📱 **Window Size Classes**: 화면 크기별 레이아웃
- 📲 **Foldable 지원**: 폴더블 기기 감지
- 🖥️ **멀티 윈도우**: 여러 창 관리
- 🔄 **Activity Embedding**: 여러 Activity 동시 표시

---

## Window Size Classes

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    implementation("androidx.window:window:1.2.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
}
```

### Window Size Class 사용

```kotlin
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

/**
 * Window Size Class 기반 레이아웃
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AdaptiveLayout() {
    val windowSizeClass = calculateWindowSizeClass(activity = LocalContext.current as Activity)
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 스마트폰 (< 600dp)
            CompactLayout()
        }
        
        WindowWidthSizeClass.Medium -> {
            // 태블릿 세로 (600dp ~ 840dp)
            MediumLayout()
        }
        
        WindowWidthSizeClass.Expanded -> {
            // 태블릿 가로, 데스크톱 (> 840dp)
            ExpandedLayout()
        }
    }
}

@Composable
fun CompactLayout() {
    // 단일 패널 레이아웃
    Column {
        TopBar()
        Content()
    }
}

@Composable
fun ExpandedLayout() {
    // 듀얼 패널 레이아웃
    Row {
        NavigationRail(modifier = Modifier.width(200.dp))
        Content(modifier = Modifier.weight(1f))
    }
}
```

---

## Foldable 지원

```kotlin
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.FoldingFeature

/**
 * Foldable 상태 감지
 */
@Composable
fun FoldableAwareLayout() {
    val context = LocalContext.current
    val windowInfoTracker = remember { WindowInfoTracker.getOrCreate(context) }
    
    val windowLayoutInfo by windowInfoTracker.windowLayoutInfo(context)
        .collectAsState(initial = null)
    
    val foldingFeature = windowLayoutInfo?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull()
    
    when {
        foldingFeature == null -> {
            // 일반 기기
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

---

## 멀티 윈도우

```kotlin
/**
 * 멀티 윈도우 감지
 */
class MultiWindowHelper(private val activity: Activity) {
    
    /**
     * 멀티 윈도우 모드 확인
     */
    fun isInMultiWindowMode(): Boolean {
        return activity.isInMultiWindowMode
    }
    
    /**
     * 멀티 윈도우 모드 변경 감지
     */
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        if (isInMultiWindowMode) {
            // 멀티 윈도우 모드: 레이아웃 조정
            adjustLayoutForMultiWindow()
        } else {
            // 전체 화면 모드
            adjustLayoutForFullScreen()
        }
    }
}
```

---

## Activity Embedding

```kotlin
/**
 * Activity Embedding 설정
 */
class ActivityEmbeddingHelper(private val context: Context) {
    
    fun setupActivityEmbedding() {
        val splitController = SplitController.getInstance(context)
        
        // Split Rule 설정
        val splitPairRule = SplitPairRule.Builder(
            filters = setOf(
                SplitPairFilter(
                    ComponentName(context, MainActivity::class.java),
                    ComponentName(context, DetailActivity::class.java),
                    null
                )
            )
        )
            .setMinWidthDp(840)  // 최소 너비
            .setSplitRatio(0.3f)  // 분할 비율 (30:70)
            .build()
        
        splitController.registerRule(splitPairRule)
    }
}
```

---

## 참고 자료

- [WindowManager 공식 문서](https://developer.android.com/jetpack/androidx/releases/window)
- [Large Screen 가이드](https://developer.android.com/guide/topics/large-screens)
