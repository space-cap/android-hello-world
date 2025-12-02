# Android WindowManager 고급 가이드

> 📖 **시리즈 구성**
> - **73-1**: [폴더블의 역사](./73-1-foldable-history.md)
> - **73-2**: [WindowManager 기본 가이드](./73-2-android-window-manager-basics.md)
> - **73-3**: WindowManager 고급 가이드 (현재 문서)
> - **73-4**: [WindowManager 실전 프로젝트](./73-4-android-window-manager-projects.md)

---

## 📚 목차

1. [반응형 레이아웃 패턴](#반응형-레이아웃-패턴)
2. [Activity Embedding](#activity-embedding)
3. [폴더블 최적화](#폴더블-최적화)
4. [성능 최적화](#성능-최적화)

---

## 반응형 레이아웃 패턴

### 🎨 Master-Detail 패턴

이메일, 메시지 앱에 적합한 패턴입니다.

```kotlin
/**
 * Master-Detail 패턴 구현
 */
@Composable
fun MasterDetailLayout(
    windowSizeClass: WindowSizeClass,
    items: List<Item>,
    selectedItem: Item?,
    onItemSelected: (Item) -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 스마트폰: 네비게이션으로 전환
            if (selectedItem == null) {
                MasterPane(items, onItemSelected)
            } else {
                DetailPane(selectedItem, onBack = { onItemSelected(null) })
            }
        }
        
        WindowWidthSizeClass.Expanded -> {
            // 태블릿: 동시 표시
            Row {
                MasterPane(
                    items = items,
                    onItemSelected = onItemSelected,
                    modifier = Modifier.weight(0.4f)
                )
                DetailPane(
                    item = selectedItem,
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}
```

### 📋 List-Detail 패턴

뉴스, 설정 앱에 적합한 패턴입니다.

```kotlin
/**
 * List-Detail 패턴
 */
@Composable
fun ListDetailLayout(
    windowSizeClass: WindowSizeClass,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 단일 패널
            if (selectedCategory == null) {
                CategoryList(categories, onCategorySelected)
            } else {
                CategoryDetail(selectedCategory)
            }
        }
        
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> {
            // 듀얼 패널
            Row {
                CategoryList(
                    categories = categories,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.width(300.dp)
                )
                CategoryDetail(
                    category = selectedCategory,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
```

---

## Activity Embedding

### 🔧 Activity Embedding 설정

```kotlin
/**
 * Activity Embedding 초기화
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Activity Embedding 설정
        setupActivityEmbedding()
    }
    
    private fun setupActivityEmbedding() {
        val splitController = SplitController.getInstance(this)
        
        // Split Pair Rule
        val splitPairRule = SplitPairRule.Builder(
            filters = setOf(
                SplitPairFilter(
                    ComponentName(this, MainActivity::class.java),
                    ComponentName(this, DetailActivity::class.java),
                    null
                )
            )
        )
            .setMinWidthDp(840)  // 최소 너비
            .setSplitRatio(0.3f)  // 분할 비율
            .build()
        
        splitController.registerRule(splitPairRule)
    }
}
```

---

## 폴더블 최적화

### 📱 Galaxy Fold 최적화

```kotlin
/**
 * 폴더블 디바이스별 최적화
 */
@Composable
fun FoldableOptimizedLayout() {
    val windowLayoutInfo by rememberWindowLayoutInfo()
    val foldingFeature = windowLayoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull()
    
    when {
        // 접힌 상태: Compact UI
        foldingFeature == null -> CompactUI()
        
        // 반쯤 접힌 상태: Flex Mode
        foldingFeature.state == FoldingFeature.State.HALF_OPENED -> {
            FlexModeUI(foldingFeature)
        }
        
        // 완전히 펼친 상태: Expanded UI
        else -> ExpandedUI()
    }
}

@Composable
fun rememberWindowLayoutInfo(): State<WindowLayoutInfo> {
    val context = LocalContext.current
    return WindowInfoTracker.getOrCreate(context)
        .windowLayoutInfo(context)
        .collectAsState(initial = WindowLayoutInfo(emptyList()))
}
```

---

## 성능 최적화

### ⚡ 레이아웃 재구성 최소화

```kotlin
/**
 * 성능 최적화된 반응형 레이아웃
 */
@Composable
fun OptimizedAdaptiveLayout() {
    val windowSizeClass = calculateWindowSizeClass()
    
    // Window Size Class를 키로 사용하여 재구성 최소화
    key(windowSizeClass.widthSizeClass) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactLayout()
            WindowWidthSizeClass.Medium -> MediumLayout()
            WindowWidthSizeClass.Expanded -> ExpandedLayout()
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant
