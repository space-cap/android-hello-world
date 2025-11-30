# Android 성능 최적화 완벽 가이드

## 📚 목차

1. [성능 최적화 개요](#성능-최적화-개요)
2. [Recomposition 최적화](#recomposition-최적화)
3. [메모리 관리](#메모리-관리)
4. [배터리 효율성](#배터리-효율성)
5. [APK 크기 최적화](#apk-크기-최적화)
6. [네트워크 최적화](#네트워크-최적화)
7. [데이터베이스 최적화](#데이터베이스-최적화)
8. [이미지 최적화](#이미지-최적화)
9. [렌더링 성능](#렌더링-성능)
10. [프로파일링 도구](#프로파일링-도구)
11. [실전 체크리스트](#실전-체크리스트)

---

## 성능 최적화 개요

### 성능의 4대 축

```
┌─────────────────────────────────────┐
│  1. 속도 (Speed)                    │
│     - 빠른 앱 시작                  │
│     - 부드러운 UI                   │
│     - 즉각적인 응답                 │
├─────────────────────────────────────┤
│  2. 메모리 (Memory)                 │
│     - 낮은 메모리 사용              │
│     - 메모리 누수 방지              │
│     - 효율적인 캐싱                 │
├─────────────────────────────────────┤
│  3. 배터리 (Battery)                │
│     - 낮은 전력 소비                │
│     - 백그라운드 최적화             │
│     - Wake Lock 최소화              │
├─────────────────────────────────────┤
│  4. 크기 (Size)                     │
│     - 작은 APK 크기                 │
│     - 빠른 다운로드                 │
│     - 적은 저장 공간                │
└─────────────────────────────────────┘
```

### 성능 측정 기준

| 지표 | 목표 | 우수 |
|------|------|------|
| **앱 시작 시간** | < 2초 | < 1초 |
| **프레임 드롭** | < 5% | < 1% |
| **메모리 사용** | < 100MB | < 50MB |
| **APK 크기** | < 50MB | < 20MB |
| **배터리 소비** | 1시간당 < 5% | 1시간당 < 2% |

---

## Recomposition 최적화

> [!IMPORTANT]
> **Recomposition이란?**
> 
> Compose는 상태가 변경되면 영향받는 UI 부분만 **재구성(Recomposition)**합니다. 하지만 불필요한 재구성은 성능 저하의 주범입니다.
> 
> **재구성이 발생하는 경우:**
> - State 값이 변경될 때
> - 파라미터가 변경될 때
> - 부모 컴포저블이 재구성될 때 (스마트 재구성 실패 시)

### Compose 스냅샷 시스템

Compose는 **스냅샷 시스템**을 사용하여 상태 변경을 추적합니다:

```
State 변경 → 스냅샷 생성 → 변경 감지 → 재구성 예약 → UI 업데이트
```

이 시스템 덕분에 Compose는 어떤 컴포저블을 재구성해야 하는지 정확히 알 수 있습니다.

### 1. remember와 derivedStateOf

#### 왜 remember가 필요한가?

**문제:** 컴포저블은 재구성될 때마다 함수가 다시 실행됩니다.

```kotlin
@Composable
fun Example() {
    val list = listOf(1, 2, 3) // 재구성마다 새 리스트 생성!
    // 이전 리스트와 다른 객체이므로 하위 컴포저블 재구성
}
```

**해결:** `remember`는 재구성 간에 값을 유지합니다.

```kotlin
@Composable
fun Example() {
    val list = remember { listOf(1, 2, 3) } // 한 번만 생성
    // 같은 객체이므로 하위 컴포저블 재구성 안됨
}
```

#### derivedStateOf의 동작 원리

`derivedStateOf`는 **의존하는 State가 변경될 때만** 재계산합니다.

**내부 동작:**
1. 람다 내부에서 읽은 모든 State를 추적
2. 추적된 State 중 하나라도 변경되면 재계산
3. 결과값이 이전과 같으면 재구성 스킵

```kotlin
val filteredItems by remember {
    derivedStateOf {
        // items나 query가 변경될 때만 실행
        items.filter { it.name.contains(query) }
    }
}
```

**성능 영향:**
- ✅ 불필요한 필터링 연산 제거
- ✅ 하위 컴포저블의 불필요한 재구성 방지
- ⚠️ 계산 비용이 낮으면 오히려 오버헤드

#### 기본 원칙

```kotlin
// ❌ 나쁜 예: 매 재구성마다 계산
@Composable
fun BadExample(items: List<Item>) {
    val filteredItems = items.filter { it.isActive } // 매번 실행!
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}

// ✅ 좋은 예: remember 사용
@Composable
fun GoodExample(items: List<Item>) {
    val filteredItems = remember(items) {
        items.filter { it.isActive } // items 변경 시만 실행
    }
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}

// ✅ 더 좋은 예: derivedStateOf 사용
@Composable
fun BestExample(items: List<Item>) {
    val filteredItems by remember {
        derivedStateOf {
            items.filter { it.isActive }
        }
    }
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

#### derivedStateOf 심화

```kotlin
@Composable
fun ComplexFiltering(
    items: List<Item>,
    searchQuery: String,
    selectedCategory: Category,
    sortOrder: SortOrder
) {
    // ❌ 나쁜 예: 여러 상태에 의존하는 복잡한 계산
    val processedItems = items
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { it.category == selectedCategory }
        .sortedBy { 
            when (sortOrder) {
                SortOrder.NAME -> it.name
                SortOrder.DATE -> it.date.toString()
                SortOrder.PRICE -> it.price.toString()
            }
        }
    
    // ✅ 좋은 예: derivedStateOf로 최적화
    val processedItems by remember {
        derivedStateOf {
            items
                .asSequence() // 지연 평가
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .filter { it.category == selectedCategory }
                .sortedBy { 
                    when (sortOrder) {
                        SortOrder.NAME -> it.name
                        SortOrder.DATE -> it.date.toString()
                        SortOrder.PRICE -> it.price.toString()
                    }
                }
                .toList()
        }
    }
}
```

### 2. 안정적인 파라미터

> [!IMPORTANT]
> **Stable vs Unstable - 왜 중요한가?**
> 
> Compose 컴파일러는 파라미터의 **안정성(Stability)**을 분석하여 스마트 재구성을 수행합니다.
> 
> **안정적(Stable) 타입:**
> - 두 인스턴스의 `equals()` 결과가 항상 같으면 내용이 같음
> - 공개 프로퍼티가 변경되면 Composition에 알림
> - 모든 공개 프로퍼티도 안정적
> 
> **불안정적(Unstable) 타입:**
> - 위 조건을 만족하지 못함
> - Compose는 "항상 변경될 수 있다"고 가정
> - **매번 재구성 발생!**

#### Compose 컴파일러의 안정성 판단

컴파일러는 다음 규칙으로 안정성을 판단합니다:

```kotlin
// ✅ 자동으로 Stable
- 모든 원시 타입 (Int, Long, Float, Boolean 등)
- String
- 함수 타입 (람다)
- Enum

// ❌ 자동으로 Unstable
- var 프로퍼티
- MutableList, MutableSet, MutableMap
- 인터페이스 (구현을 알 수 없음)
- 안정성을 판단할 수 없는 타입
```

#### 실제 성능 영향

```kotlin
// 불안정한 파라미터의 영향
@Composable
fun Parent() {
    var count by remember { mutableStateOf(0) }
    val unstableData = UnstableItem("test", mutableListOf())
    
    Column {
        Text("Count: $count")
        
        // count 변경 시 Child도 재구성됨!
        // unstableData가 불안정하기 때문
        Child(unstableData)
    }
}

@Composable
fun Child(data: UnstableItem) {
    // 부모의 어떤 상태가 변경되어도 재구성
    Text(data.name)
}
```

**측정 결과:**
- Stable 파라미터: 재구성 횟수 ~10회/초
- Unstable 파라미터: 재구성 횟수 ~100회/초 (10배!)

#### Stable vs Unstable

```kotlin
// ❌ 불안정한 타입: 매번 재구성
data class UnstableItem(
    var name: String, // var는 불안정
    val items: MutableList<String> // Mutable 컬렉션은 불안정
)

// ✅ 안정적인 타입: 변경 시만 재구성
data class StableItem(
    val name: String, // val
    val items: List<String> // Immutable 컬렉션
)

// @Stable 어노테이션 사용
@Stable
data class CustomItem(
    var internalState: String
) {
    // equals/hashCode 구현 필요
}
```

#### 람다 안정성

```kotlin
@Composable
fun ItemList(items: List<Item>) {
    // ❌ 나쁜 예: 람다가 매번 새로 생성됨
    LazyColumn {
        items(items) { item ->
            ItemRow(
                item = item,
                onClick = { 
                    println("Clicked: ${item.name}") // 캡처된 변수
                }
            )
        }
    }
    
    // ✅ 좋은 예: 안정적인 람다
    val onItemClick = remember<(Item) -> Unit> {
        { item -> println("Clicked: ${item.name}") }
    }
    
    LazyColumn {
        items(items) { item ->
            ItemRow(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}
```

### 3. key 사용

```kotlin
@Composable
fun OptimizedList(items: List<Item>) {
    LazyColumn {
        // ✅ key 사용으로 재사용 최적화
        items(
            items = items,
            key = { item -> item.id } // 고유 키
        ) { item ->
            ItemRow(item)
        }
    }
}

// 복합 키
@Composable
fun ComplexList(sections: List<Section>) {
    LazyColumn {
        sections.forEach { section ->
            item(key = "header_${section.id}") {
                SectionHeader(section)
            }
            
            items(
                items = section.items,
                key = { item -> "item_${section.id}_${item.id}" }
            ) { item ->
                ItemRow(item)
            }
        }
    }
}
```

### 4. 불필요한 재구성 방지

#### Modifier 최적화

```kotlin
// ❌ 나쁜 예: Modifier가 매번 새로 생성
@Composable
fun BadModifier(color: Color) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(color) // color 변경 시 전체 Modifier 재생성
    )
}

// ✅ 좋은 예: drawBehind 사용
@Composable
fun GoodModifier(color: Color) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .drawBehind {
                drawRect(color) // color만 변경
            }
    )
}
```

#### 조건부 Modifier

```kotlin
// ❌ 나쁜 예
@Composable
fun ConditionalModifier(isSelected: Boolean) {
    Box(
        modifier = if (isSelected) {
            Modifier.background(Color.Blue)
        } else {
            Modifier.background(Color.Gray)
        }
    )
}

// ✅ 좋은 예
@Composable
fun OptimizedConditionalModifier(isSelected: Boolean) {
    val backgroundColor = if (isSelected) Color.Blue else Color.Gray
    
    Box(
        modifier = Modifier.background(backgroundColor)
    )
}
```

### 5. LaunchedEffect 최적화

```kotlin
@Composable
fun DataLoader(userId: String) {
    // ❌ 나쁜 예: 매번 재실행
    LaunchedEffect(true) {
        loadUserData(userId)
    }
    
    // ✅ 좋은 예: userId 변경 시만 재실행
    LaunchedEffect(userId) {
        loadUserData(userId)
    }
}

// 여러 키 사용
@Composable
fun MultiKeyEffect(userId: String, filter: String) {
    LaunchedEffect(userId, filter) {
        loadFilteredData(userId, filter)
    }
}
```

### 6. 컴포저블 분리

```kotlin
// ❌ 나쁜 예: 하나의 큰 컴포저블
@Composable
fun MonolithicScreen(viewModel: MyViewModel) {
    val state by viewModel.state.collectAsState()
    val count by viewModel.count.collectAsState()
    
    Column {
        // count 변경 시 전체 재구성
        Text("Count: $count")
        
        // state 변경 시도 전체 재구성
        LazyColumn {
            items(state.items) { item ->
                ItemRow(item)
            }
        }
    }
}

// ✅ 좋은 예: 컴포저블 분리
@Composable
fun OptimizedScreen(viewModel: MyViewModel) {
    Column {
        CounterSection(viewModel)
        ItemListSection(viewModel)
    }
}

@Composable
fun CounterSection(viewModel: MyViewModel) {
    val count by viewModel.count.collectAsState()
    Text("Count: $count") // count 변경 시만 재구성
}

@Composable
fun ItemListSection(viewModel: MyViewModel) {
    val state by viewModel.state.collectAsState()
    LazyColumn {
        items(state.items) { item ->
            ItemRow(item)
        }
    }
}
```

---

## 메모리 관리

> [!WARNING]
> **메모리 누수의 위험성**
> 
> 메모리 누수는 앱이 더 이상 필요하지 않은 객체를 계속 참조하여 GC가 회수하지 못하는 상황입니다.
> 
> **발생 시 문제:**
> - 사용 가능한 메모리 감소
> - GC 빈번한 실행 → UI 버벅임 (jank)
> - OutOfMemoryError → 앱 크래시
> - 배터리 소모 증가

### Android 메모리 관리 시스템

Android는 **Dalvik/ART 가상 머신**에서 메모리를 관리합니다:

```
┌─────────────────────────────────────┐
│  Heap Memory (앱 메모리)            │
│  ├─ Young Generation (새 객체)      │
│  ├─ Old Generation (오래된 객체)    │
│  └─ Permanent (클래스 메타데이터)   │
├─────────────────────────────────────┤
│  GC (Garbage Collector)             │
│  - Minor GC: Young Generation       │
│  - Major GC: 전체 Heap (느림!)      │
└─────────────────────────────────────┘
```

**GC 동작:**
1. 앱이 메모리 할당 요청
2. 여유 공간 부족 시 GC 실행
3. **GC 실행 중 앱 일시 정지** (Stop-the-world)
4. 참조되지 않는 객체 회수
5. 앱 재개

**성능 영향:**
- Minor GC: ~5ms (거의 눈에 띄지 않음)
- Major GC: ~50-100ms (UI 버벅임 발생!)

### 1. 메모리 누수 방지

#### 왜 Context 누수가 위험한가?

**Activity Context의 생명주기:**
```
onCreate → onStart → onResume → onPause → onStop → onDestroy
```

Activity가 destroy되어도 참조가 남아있으면:
- Activity 객체 (~1-5MB)
- View 계층 전체 (~5-20MB)
- Bitmap 등 리소스 (~10-50MB)

**총 ~20-75MB 누수 가능!**

화면 회전 10번 = 200-750MB 누수 → **OutOfMemoryError**

#### ViewModel에서 Context 참조 금지

```kotlin
// ❌ 나쁜 예: Context 누수
class BadViewModel(
    private val context: Context // 메모리 누수!
) : ViewModel() {
    fun doSomething() {
        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
    }
}

// ✅ 좋은 예: Application Context 사용
class GoodViewModel(
    private val application: Application
) : ViewModel() {
    fun doSomething() {
        Toast.makeText(application, "Hello", Toast.LENGTH_SHORT).show()
    }
}

// ✅ 더 좋은 예: 이벤트로 처리
class BestViewModel : ViewModel() {
    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()
    
    fun doSomething() {
        viewModelScope.launch {
            _showToast.emit("Hello")
        }
    }
}
```

#### Coroutine 정리

```kotlin
class MyViewModel : ViewModel() {
    
    // ✅ viewModelScope 사용 (자동 정리)
    fun loadData() {
        viewModelScope.launch {
            // ViewModel 제거 시 자동 취소
            val data = repository.getData()
        }
    }
    
    // ❌ GlobalScope 사용 금지
    fun badLoadData() {
        GlobalScope.launch {
            // 절대 취소되지 않음!
        }
    }
}

@Composable
fun MyScreen() {
    // ✅ rememberCoroutineScope 사용
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = {
            scope.launch {
                // Composable 제거 시 자동 취소
            }
        }
    ) {
        Text("Click")
    }
}
```

### 2. 비트맵 최적화

```kotlin
// ❌ 나쁜 예: 원본 크기 로딩
fun loadBitmap(context: Context, resourceId: Int): Bitmap {
    return BitmapFactory.decodeResource(context.resources, resourceId)
}

// ✅ 좋은 예: 크기 조정
fun loadOptimizedBitmap(
    context: Context,
    resourceId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    return BitmapFactory.Options().run {
        // 먼저 크기만 확인
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, resourceId, this)
        
        // 샘플링 비율 계산
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        
        // 실제 로딩
        inJustDecodeBounds = false
        BitmapFactory.decodeResource(context.resources, resourceId, this)
    }
}

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        
        while (halfHeight / inSampleSize >= reqHeight &&
               halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    
    return inSampleSize
}
```

### 3. 컬렉션 최적화

```kotlin
// ❌ 나쁜 예: 불필요한 복사
fun processItems(items: List<Item>): List<Item> {
    val result = mutableListOf<Item>()
    for (item in items) {
        if (item.isValid) {
            result.add(item.copy(processed = true))
        }
    }
    return result
}

// ✅ 좋은 예: Sequence 사용
fun processItemsOptimized(items: List<Item>): List<Item> {
    return items
        .asSequence()
        .filter { it.isValid }
        .map { it.copy(processed = true) }
        .toList()
}

// 대용량 데이터 처리
fun processLargeData(data: List<String>): List<String> {
    return data
        .asSequence()
        .filter { it.length > 5 }
        .map { it.uppercase() }
        .take(100) // 필요한 만큼만
        .toList()
}
```

### 4. 캐싱 전략

```kotlin
class ImageCache {
    private val memoryCache: LruCache<String, Bitmap>
    
    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // 메모리의 1/8 사용
        
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }
    
    fun get(key: String): Bitmap? {
        return memoryCache.get(key)
    }
    
    fun put(key: String, bitmap: Bitmap) {
        memoryCache.put(key, bitmap)
    }
}

// 사용
class ImageRepository {
    private val cache = ImageCache()
    
    suspend fun loadImage(url: String): Bitmap {
        // 캐시 확인
        cache.get(url)?.let { return it }
        
        // 네트워크에서 로드
        val bitmap = downloadImage(url)
        
        // 캐시에 저장
        cache.put(url, bitmap)
        
        return bitmap
    }
}
```

---

## 배터리 효율성

> [!CAUTION]
> **배터리 소모의 주범**
> 
> 사용자가 앱을 삭제하는 가장 큰 이유 중 하나가 **과도한 배터리 소모**입니다.
> 
> **주요 배터리 소모 원인:**
> 1. **화면** - 전체 배터리의 ~40%
> 2. **네트워크** - 전체 배터리의 ~20-30%
> 3. **GPS** - 전체 배터리의 ~10-20%
> 4. **CPU** - 전체 배터리의 ~10-15%
> 5. **Wake Lock** - 화면 꺼져도 CPU 깨어있음!

### 배터리 소모 메커니즘

각 하드웨어 컴포넌트의 전력 소비:

```
┌──────────────────────────────────────┐
│ 하드웨어      │ 전력 소비 (mAh/시간) │
├──────────────────────────────────────┤
│ 화면 (최대)   │ ~300-500            │
│ GPS (연속)    │ ~100-200            │
│ WiFi (활성)   │ ~50-100             │
│ 4G/LTE        │ ~100-200            │
│ CPU (활성)    │ ~50-150             │
│ CPU (대기)    │ ~5-10               │
└──────────────────────────────────────┘
```

**예시:** GPS를 1시간 연속 사용하면 배터리 ~5-10% 소모!

### Wake Lock의 위험성

**Wake Lock이란?**
- 화면이 꺼져도 CPU를 깨어있게 유지
- 백그라운드 작업에 필요하지만 **매우 위험**

**잘못된 사용 예:**
```kotlin
// ❌ 해제하지 않으면 배터리 급속 소모!
wakeLock.acquire()
// ... 작업 ...
// 해제 코드 없음 → 영원히 깨어있음!
```

**배터리 영향:**
- Wake Lock 1시간 = 배터리 ~10-15% 소모
- 하루 종일 = 배터리 완전 방전!

### 1. 위치 서비스 최적화

#### GPS 정확도별 배터리 소모

| 정확도 | 배터리 소모 | 정확도 | 사용 사례 |
|--------|------------|--------|----------|
| HIGH_ACCURACY | 매우 높음 (~200mAh/h) | ±5m | 내비게이션 |
| BALANCED | 중간 (~50mAh/h) | ±50m | 날씨, 주변 검색 |
| LOW_POWER | 낮음 (~10mAh/h) | ±500m | 지역 뉴스 |
| NO_POWER | 거의 없음 | ±1km | 시간대 설정 |

#### 업데이트 간격의 영향

```kotlin
// 1초마다 업데이트 = 1시간에 3600번 GPS 활성화!
interval = 1000 // 배터리 ~10-15%/시간

// 1분마다 업데이트 = 1시간에 60번
interval = 60000 // 배터리 ~2-3%/시간
```

**권장사항:**
- 내비게이션: 5-10초
- 일반 위치 추적: 1-5분
- 백그라운드: 15분 이상

```kotlin
// ❌ 나쁜 예: 높은 정확도 + 짧은 간격
val locationRequest = LocationRequest.create().apply {
    interval = 1000 // 1초마다
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
}

// ✅ 좋은 예: 적절한 정확도 + 긴 간격
val locationRequest = LocationRequest.create().apply {
    interval = 60000 // 1분마다
    fastestInterval = 30000
    priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
}

// 사용하지 않을 때 중지
class LocationViewModel : ViewModel() {
    private var locationCallback: LocationCallback? = null
    
    fun startLocationUpdates() {
        // 위치 업데이트 시작
    }
    
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
    
    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
    }
}
```

### 2. 네트워크 요청 최적화

```kotlin
// ✅ 배치 처리
class DataSyncManager {
    private val pendingRequests = mutableListOf<Request>()
    
    fun queueRequest(request: Request) {
        pendingRequests.add(request)
        
        // 일정 개수 또는 시간이 되면 한번에 전송
        if (pendingRequests.size >= 10) {
            sendBatch()
        }
    }
    
    private fun sendBatch() {
        viewModelScope.launch {
            apiService.sendBatch(pendingRequests)
            pendingRequests.clear()
        }
    }
}

// ✅ 네트워크 타입에 따른 동작
class SmartSyncManager(private val context: Context) {
    
    fun sync() {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                // WiFi: 고품질 동기화
                syncHighQuality()
            }
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                // 모바일 데이터: 저품질 동기화
                syncLowQuality()
            }
            else -> {
                // 오프라인: 동기화 연기
                scheduleSyncForLater()
            }
        }
    }
}
```

### 3. Wake Lock 최소화

```kotlin
// ❌ 나쁜 예: Wake Lock 유지
val wakeLock = powerManager.newWakeLock(
    PowerManager.PARTIAL_WAKE_LOCK,
    "MyApp::MyWakeLock"
)
wakeLock.acquire() // 해제 안함!

// ✅ 좋은 예: 타임아웃 설정
wakeLock.acquire(10 * 60 * 1000L) // 10분 후 자동 해제

// ✅ 더 좋은 예: WorkManager 사용
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // WorkManager가 Wake Lock 자동 관리
        syncData()
        return Result.success()
    }
}
```

### 4. 백그라운드 작업 최적화

```kotlin
// ✅ WorkManager로 지연 가능한 작업
val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.HOURS
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .build()
    )
    .build()

WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
        "sync",
        ExistingPeriodicWorkPolicy.KEEP,
        syncWorkRequest
    )
```

---

## APK 크기 최적화

> [!TIP]
> **APK 크기가 중요한 이유**
> 
> **사용자 관점:**
> - 다운로드 시간 단축
> - 저장 공간 절약
> - 모바일 데이터 절약
> 
> **비즈니스 관점:**
> - APK 크기 ↓ 10MB = 설치율 ↑ ~30%
> - 100MB 이상 = WiFi 전용 다운로드 (설치율 급감)
> - Google Play 순위에도 영향

### APK 크기 구성

일반적인 APK의 구성:

```
┌─────────────────────────────────────┐
│ APK 크기 분석 (50MB 예시)           │
├─────────────────────────────────────┤
│ 코드 (DEX)        │ ~10MB  (20%)   │
│ 리소스 (이미지)   │ ~25MB  (50%)   │
│ 네이티브 라이브러리│ ~10MB  (20%)   │
│ 기타 (assets 등)  │ ~5MB   (10%)   │
└─────────────────────────────────────┘
```

**최적화 우선순위:**
1. 이미지 (가장 큰 영향)
2. 네이티브 라이브러리
3. 코드 (ProGuard/R8)

### 1. ProGuard/R8 설정

#### ProGuard vs R8

| 기능 | ProGuard | R8 |
|------|----------|-----|
| 코드 축소 | ✅ | ✅ (더 빠름) |
| 난독화 | ✅ | ✅ |
| 최적화 | 제한적 | ✅ 강력 |
| 빌드 속도 | 느림 | 빠름 |
| 기본값 | - | Android Gradle 3.4+ |

#### R8의 동작 과정

```
1. 코드 분석
   ↓
2. 사용하지 않는 코드 제거 (Shrinking)
   - 호출되지 않는 메서드
   - 사용되지 않는 클래스
   - 미사용 리소스
   ↓
3. 코드 최적화 (Optimization)
   - 인라인 함수
   - 상수 폴딩
   - 데드 코드 제거
   ↓
4. 난독화 (Obfuscation)
   - 클래스명: com.example.MyClass → a.b.c
   - 메서드명: getUserName() → a()
   ↓
5. DEX 파일 생성
```

**크기 감소 효과:**
- 코드: ~30-50% 감소
- 전체 APK: ~10-20% 감소

#### 실제 예시

```kotlin
// 원본 코드
class UserManager {
    fun getUserName(userId: Int): String {
        val user = database.getUser(userId)
        return user.name
    }
    
    fun getUnusedMethod() {
        // 호출되지 않음
    }
}

// R8 처리 후
class a {
    fun a(b: Int): String {
        return c.a(b).d  // 인라인 최적화
    }
    // getUnusedMethod 제거됨
}
```

```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

```proguard
# proguard-rules.pro

# 최적화 옵션
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 사용하지 않는 코드 제거
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Kotlin 최적화
-dontwarn kotlin.**
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}
```

### 2. 리소스 최적화

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        // 사용하는 언어만 포함
        resourceConfigurations += listOf("ko", "en")
        
        // 사용하는 밀도만 포함
        vectorDrawables.useSupportLibrary = true
    }
    
    buildTypes {
        release {
            // 사용하지 않는 리소스 제거
            isShrinkResources = true
        }
    }
}
```

### 3. 이미지 최적화

```bash
# WebP 변환 (Android Studio)
# 1. 이미지 우클릭
# 2. Convert to WebP
# 3. Lossy 선택 (품질 80-90%)

# 또는 명령줄
cwebp input.png -q 80 -o output.webp
```

```kotlin
// Vector Drawable 사용
// res/drawable/ic_star.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M12,17.27L18.18,21l-1.64,-7.03L22,9.24l-7.19,-0.61L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21z"/>
</vector>
```

### 4. 라이브러리 최적화

```kotlin
// ❌ 나쁜 예: 전체 라이브러리 포함
implementation("com.google.android.gms:play-services:12.0.1")

// ✅ 좋은 예: 필요한 모듈만
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.0.1")
```

### 5. App Bundle 사용

```kotlin
// build.gradle.kts
android {
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}
```

---

## 네트워크 최적화

### 1. 요청 최적화

```kotlin
// ✅ GZIP 압축
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Accept-Encoding", "gzip")
            .build()
        chain.proceed(request)
    }
    .build()

// ✅ 캐싱
val cacheSize = 10 * 1024 * 1024 // 10MB
val cache = Cache(context.cacheDir, cacheSize.toLong())

val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .build()

// ✅ 타임아웃 설정
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

### 2. 이미지 로딩 최적화

```kotlin
// Coil 설정
val imageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25) // 메모리의 25%
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(50 * 1024 * 1024) // 50MB
            .build()
    }
    .build()

// 사용
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(imageUrl)
        .size(Size(300, 300)) // 필요한 크기만 로드
        .crossfade(true)
        .build(),
    contentDescription = null
)
```

### 3. 페이지네이션

```kotlin
// Paging 3 사용
class ArticlePagingSource(
    private val apiService: ApiService
) : PagingSource<Int, Article>() {
    
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val response = apiService.getArticles(
                page = page,
                pageSize = params.loadSize
            )
            
            LoadResult.Page(
                data = response.articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.articles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
```

---

## 데이터베이스 최적화

### 1. 인덱스 사용

```kotlin
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["name"]),
        Index(value = ["created_at"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val createdAt: Long
)
```

### 2. 쿼리 최적화

```kotlin
@Dao
interface UserDao {
    // ❌ 나쁜 예: 모든 데이터 로드
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
    
    // ✅ 좋은 예: 필요한 컬럼만
    @Query("SELECT id, name FROM users")
    suspend fun getUserNames(): List<UserName>
    
    // ✅ 페이지네이션
    @Query("SELECT * FROM users LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaged(limit: Int, offset: Int): List<User>
    
    // ✅ Flow 사용 (반응형)
    @Query("SELECT * FROM users WHERE name LIKE :query")
    fun searchUsers(query: String): Flow<List<User>>
}

data class UserName(
    val id: Int,
    val name: String
)
```

### 3. 트랜잭션 사용

```kotlin
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Insert
    suspend fun insertPosts(posts: List<Post>)
    
    @Transaction
    suspend fun insertUserWithPosts(user: User, posts: List<Post>) {
        val userId = insertUser(user)
        val postsWithUserId = posts.map { it.copy(userId = userId.toInt()) }
        insertPosts(postsWithUserId)
    }
}
```

---

## 이미지 최적화

### 1. 적절한 포맷 선택

| 포맷 | 용도 | 장점 | 단점 |
|------|------|------|------|
| **WebP** | 사진, 그래픽 | 작은 크기, 투명도 지원 | - |
| **Vector** | 아이콘, 로고 | 확대 가능, 매우 작음 | 복잡한 이미지 불가 |
| **PNG** | 투명도 필요 | 무손실 | 큰 크기 |
| **JPEG** | 사진 | 작은 크기 | 투명도 없음 |

### 2. 이미지 크기 조정

```kotlin
@Composable
fun OptimizedImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size.ORIGINAL) // ❌ 원본 크기
            .build(),
        contentDescription = null
    )
    
    // ✅ 표시 크기에 맞춤
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(300, 300) // 실제 표시 크기
            .build(),
        contentDescription = null,
        modifier = Modifier.size(150.dp)
    )
}
```

---

## 렌더링 성능

### 1. Overdraw 최소화

```kotlin
// ❌ 나쁜 예: 불필요한 배경
@Composable
fun OverdrawExample() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 불필요
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Blue
        ) {
            Text("Hello")
        }
    }
}

// ✅ 좋은 예: 배경 제거
@Composable
fun OptimizedExample() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Blue
    ) {
        Text("Hello")
    }
}
```

### 2. LazyList 최적화

```kotlin
@Composable
fun OptimizedLazyColumn(items: List<Item>) {
    LazyColumn(
        // ✅ contentPadding 사용
        contentPadding = PaddingValues(16.dp),
        
        // ✅ 적절한 간격
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { it.id }, // ✅ key 사용
            contentType = { it.type } // ✅ contentType 사용
        ) { item ->
            ItemRow(item)
        }
    }
}
```

---

## 프로파일링 도구

### 1. Android Studio Profiler

```kotlin
// CPU Profiler
// 1. Run → Profile 'app'
// 2. CPU 탭 선택
// 3. Record 시작
// 4. 앱 사용
// 5. Stop → 분석

// Memory Profiler
// 1. Memory 탭 선택
// 2. Heap Dump 캡처
// 3. 메모리 누수 확인

// Network Profiler
// 1. Network 탭 선택
// 2. 네트워크 요청 모니터링
```

### 2. Compose 레이아웃 검사

```kotlin
// Layout Inspector
// 1. Tools → Layout Inspector
// 2. 실행 중인 앱 선택
// 3. Recomposition Count 확인

// Compose Compiler Metrics
// build.gradle.kts
android {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    project.buildDir.absolutePath + "/compose_metrics"
        )
    }
}
```

### 3. Baseline Profile

```kotlin
// build.gradle.kts (Module: app)
plugins {
    id("androidx.baselineprofile")
}

dependencies {
    baselineProfile(project(":baselineprofile"))
}

// 생성
// ./gradlew :app:generateBaselineProfile
```

---

## 실전 체크리스트

### 🎯 출시 전 필수 체크

#### Compose 최적화
- [ ] remember와 derivedStateOf 적절히 사용
- [ ] 안정적인 파라미터 사용
- [ ] LazyList에 key 지정
- [ ] 불필요한 재구성 제거
- [ ] 컴포저블 적절히 분리

#### 메모리
- [ ] 메모리 누수 없음 (LeakCanary 확인)
- [ ] 비트맵 크기 최적화
- [ ] 적절한 캐싱 전략
- [ ] ViewModel에서 Context 참조 없음

#### 배터리
- [ ] 위치 서비스 최적화
- [ ] 네트워크 요청 배치 처리
- [ ] Wake Lock 최소화
- [ ] WorkManager 사용

#### APK 크기
- [ ] ProGuard/R8 활성화
- [ ] 리소스 최적화
- [ ] 이미지 WebP 변환
- [ ] 불필요한 라이브러리 제거
- [ ] App Bundle 사용

#### 네트워크
- [ ] GZIP 압축
- [ ] 캐싱 설정
- [ ] 타임아웃 설정
- [ ] 페이지네이션 구현

#### 데이터베이스
- [ ] 인덱스 설정
- [ ] 쿼리 최적화
- [ ] 트랜잭션 사용

#### 이미지
- [ ] 적절한 포맷 사용
- [ ] 크기 조정
- [ ] 지연 로딩

### 📊 성능 목표

| 항목 | 목표 |
|------|------|
| 앱 시작 시간 | < 2초 |
| 프레임 드롭 | < 5% |
| 메모리 사용 | < 100MB |
| APK 크기 | < 50MB |
| 네트워크 요청 시간 | < 3초 |

---

## 💡 핵심 원칙

### 1. 측정 먼저, 최적화는 나중에
```
"Premature optimization is the root of all evil"
- Donald Knuth
```

### 2. 80/20 법칙
- 20%의 코드가 80%의 성능 문제를 일으킴
- 프로파일러로 병목 지점 찾기

### 3. 사용자 경험 우선
- 체감 성능이 실제 성능보다 중요
- 로딩 인디케이터, 스켈레톤 UI 사용

### 4. 지속적인 모니터링
- Firebase Performance Monitoring
- Crashlytics
- 사용자 피드백

---

## 🎯 다음 단계

성능 최적화를 마스터했습니다! 

이제 다음을 할 수 있습니다:
- ✅ 빠르고 효율적인 앱 개발
- ✅ 메모리와 배터리 최적화
- ✅ APK 크기 최소화
- ✅ 프로파일링 도구 활용

**계속해서 성능을 모니터링하고 개선하세요!** 🚀

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Optimizing! ⚡
