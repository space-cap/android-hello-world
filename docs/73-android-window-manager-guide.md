# Android WindowManager 가이드

## 목차
1. [WindowManager란?](#windowmanager란)
2. [Window Size Classes](#window-size-classes)
3. [Foldable 지원](#foldable-지원)
4. [멀티 윈도우](#멀티-윈도우)
5. [Activity Embedding](#activity-embedding)
6. [반응형 레이아웃](#반응형-레이아웃)
7. [실전 예제](#실전-예제)
8. [문제 해결](#문제-해결)

---

## WindowManager란?

**WindowManager**는 대형 화면(태블릿, 폴더블, 데스크톱)에 최적화된 앱을 만들기 위한 Jetpack 라이브러리입니다.

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
