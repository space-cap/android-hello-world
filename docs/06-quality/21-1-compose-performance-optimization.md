# Compose 성능 최적화

> 📖 **시리즈 구성**
> - **21-1**: Compose 성능 최적화 (현재 문서)
> - **21-2**: [메모리 및 배터리 최적화](./21-2-memory-battery-optimization.md)
> - **21-3**: [성능 프로파일링 실전](./21-3-performance-profiling.md)

---

## 📚 목차

1. [Recomposition 이해하기](#recomposition-이해하기)
2. [remember와 derivedStateOf](#remember와-derivedstateof)
3. [Stability 시스템](#stability-시스템)
4. [LazyList 최적화](#lazylist-최적화)
5. [실전 최적화 사례](#실전-최적화-사례)
6. [성능 측정](#성능-측정)

---

## Recomposition 이해하기

### Recomposition이란?

**Recomposition**은 Compose가 상태 변경에 반응하여 UI를 다시 그리는 과정입니다.

```kotlin
@Composable
fun Counter() {
    // count가 변경되면 Counter 전체가 Recompose됩니다
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("Count: $count")  // ← 이 부분만 변경되어도
        Button(onClick = { count++ }) {
            Text("Increment")  // ← 전체가 다시 실행됨
        }
    }
}
```

### Smart Recomposition

Compose는 **변경된 부분만** 다시 그리려고 노력합니다.

```kotlin
@Composable
fun SmartRecomposition() {
    var count by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("홍길동") }
    
    Column {
        // count가 변경되면 이 Text만 Recompose
        Text("Count: $count")
        
        // name이 변경되면 이 Text만 Recompose
        Text("Name: $name")
        
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}
```

**Recomposition Scope**:
```
Column (Scope 1)
├─ Text("Count: $count")  ← count 변경 시 여기만 Recompose
├─ Text("Name: $name")    ← name 변경 시 여기만 Recompose
└─ Button                 ← 변경 없으면 Recompose 안 함
```

### 불필요한 Recomposition 방지

#### ❌ 나쁜 예: 매번 새 객체 생성

```kotlin
@Composable
fun BadExample(items: List<Item>) {
    // ❌ 매번 Recompose 시 새로운 리스트 생성
    val filteredItems = items.filter { it.isActive }
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

**문제점**:
- `BadExample`이 Recompose될 때마다 `filter` 실행
- `items` 리스트가 크면 성능 저하
- 불필요한 계산 반복

#### ✅ 좋은 예: remember 사용

```kotlin
@Composable
fun GoodExample(items: List<Item>) {
    // ✅ items가 변경될 때만 필터링
    val filteredItems = remember(items) {
        items.filter { it.isActive }
    }
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

**개선점**:
- `items`가 변경될 때만 `filter` 실행
- 불필요한 계산 방지
- 성능 향상

### Layout Inspector로 Recomposition 추적

**Android Studio → View → Tool Windows → Layout Inspector**

```kotlin
@Composable
fun DebugRecomposition() {
    var count by remember { mutableStateOf(0) }
    
    // Recomposition 횟수 추적
    val recompositionCount = remember { mutableStateOf(0) }
    
    SideEffect {
        recompositionCount.value++
        Log.d("Recomposition", "Count: ${recompositionCount.value}")
    }
    
    Column {
        Text("Recomposed ${recompositionCount.value} times")
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}
```

---

## remember와 derivedStateOf

### remember 기본

```kotlin
@Composable
fun RememberExample() {
    // ✅ Recomposition 간 값 유지
    val counter = remember { mutableStateOf(0) }
    
    // ❌ 매번 새로 생성됨
    val wrongCounter = mutableStateOf(0)
    
    Button(onClick = { counter.value++ }) {
        Text("Count: ${counter.value}")
    }
}
```

### remember vs rememberSaveable

```kotlin
@Composable
fun RememberComparison() {
    // remember: 화면 회전 시 초기화됨
    var count1 by remember { mutableStateOf(0) }
    
    // rememberSaveable: 화면 회전 후에도 유지됨
    var count2 by rememberSaveable { mutableStateOf(0) }
    
    Column {
        Text("remember: $count1")
        Text("rememberSaveable: $count2")
        
        Button(onClick = { 
            count1++
            count2++
        }) {
            Text("Increment")
        }
    }
}
```

**사용 시나리오**:
- `remember`: 임시 UI 상태 (스크롤 위치, 확장/축소 상태)
- `rememberSaveable`: 사용자 입력 (텍스트, 선택 항목)

### remember의 key 파라미터

```kotlin
@Composable
fun RememberKeyExample(userId: String) {
    // userId가 변경되면 userData 다시 로드
    val userData = remember(userId) {
        loadUserData(userId)  // 비용이 큰 작업
    }
    
    // 여러 key 사용 가능
    val combinedData = remember(userId, currentDate) {
        loadDataForUserAndDate(userId, currentDate)
    }
    
    UserProfile(userData)
}

// 비용이 큰 작업 (예시)
fun loadUserData(userId: String): UserData {
    // 네트워크 요청, 데이터베이스 쿼리 등
    return UserData(userId, "홍길동", "hong@example.com")
}
```

### derivedStateOf 활용

`derivedStateOf`는 **다른 상태로부터 파생된 상태**를 만들 때 사용합니다.

#### ❌ 나쁜 예: 매번 계산

```kotlin
@Composable
fun BadDerivedState(items: List<Item>) {
    var searchQuery by remember { mutableStateOf("") }
    
    // ❌ 매번 Recompose 시 필터링 실행
    val filteredItems = items.filter { 
        it.name.contains(searchQuery, ignoreCase = true) 
    }
    
    SearchBar(searchQuery) { searchQuery = it }
    ItemList(filteredItems)
}
```

#### ✅ 좋은 예: derivedStateOf 사용

```kotlin
@Composable
fun GoodDerivedState(items: List<Item>) {
    var searchQuery by remember { mutableStateOf("") }
    
    // ✅ searchQuery나 items가 변경될 때만 필터링
    val filteredItems by remember {
        derivedStateOf {
            items.filter { 
                it.name.contains(searchQuery, ignoreCase = true) 
            }
        }
    }
    
    SearchBar(searchQuery) { searchQuery = it }
    ItemList(filteredItems)
}
```

**derivedStateOf의 장점**:
- 의존하는 상태가 변경될 때만 재계산
- 불필요한 Recomposition 방지
- 성능 향상

### 실전 예제: 정렬과 필터링

```kotlin
data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val category: String,
    val inStock: Boolean
)

@Composable
fun ProductList(products: List<Product>) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf(SortOption.NAME) }
    var showOnlyInStock by remember { mutableStateOf(false) }
    
    // ✅ 모든 필터/정렬 로직을 derivedStateOf로 최적화
    val processedProducts by remember {
        derivedStateOf {
            products
                // 1. 검색 필터
                .filter { product ->
                    if (searchQuery.isEmpty()) true
                    else product.name.contains(searchQuery, ignoreCase = true)
                }
                // 2. 재고 필터
                .filter { product ->
                    if (showOnlyInStock) product.inStock
                    else true
                }
                // 3. 정렬
                .sortedBy { product ->
                    when (sortBy) {
                        SortOption.NAME -> product.name
                        SortOption.PRICE -> product.price.toString()
                        SortOption.CATEGORY -> product.category
                    }
                }
        }
    }
    
    Column {
        // 검색바
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )
        
        // 필터 옵션
        Row {
            FilterChip(
                selected = showOnlyInStock,
                onClick = { showOnlyInStock = !showOnlyInStock },
                label = { Text("재고 있음만") }
            )
            
            // 정렬 옵션
            SortDropdown(
                currentSort = sortBy,
                onSortChange = { sortBy = it }
            )
        }
        
        // 상품 리스트
        LazyColumn {
            items(processedProducts, key = { it.id }) { product ->
                ProductItem(product)
            }
        }
    }
}

enum class SortOption {
    NAME, PRICE, CATEGORY
}
```

---

## Stability 시스템

### Stable vs Unstable 타입

Compose는 타입의 **안정성(Stability)**을 기반으로 Recomposition을 최적화합니다.

#### Stable 타입의 조건

1. **동일한 인스턴스의 두 호출 결과가 항상 같음**
2. **public 프로퍼티가 변경되면 Composition에 알림**
3. **모든 public 프로퍼티도 Stable**

#### 기본 Stable 타입

```kotlin
// ✅ 기본적으로 Stable한 타입들
val primitives: Int = 42
val string: String = "Hello"
val immutableList: List<String> = listOf("A", "B")

// ❌ Unstable한 타입들
val mutableList: MutableList<String> = mutableListOf("A", "B")
var mutableProperty: String = "Hello"
```

### Unstable 타입의 문제

```kotlin
// ❌ Unstable 타입
data class UnstableUser(
    var name: String,  // var는 불안정
    val emails: MutableList<String>  // Mutable 컬렉션
)

@Composable
fun UserProfile(user: UnstableUser) {
    // user가 변경되지 않아도 매번 Recompose될 수 있음
    Column {
        Text(user.name)
        user.emails.forEach { email ->
            Text(email)
        }
    }
}
```

**문제점**:
- Compose가 `UnstableUser`의 변경을 추적할 수 없음
- 불필요한 Recomposition 발생
- 성능 저하

### Stable 타입으로 변경

```kotlin
// ✅ Stable 타입
data class StableUser(
    val name: String,  // val로 변경
    val emails: List<String>  // Immutable 컬렉션
)

@Composable
fun UserProfile(user: StableUser) {
    // user가 실제로 변경될 때만 Recompose
    Column {
        Text(user.name)
        user.emails.forEach { email ->
            Text(email)
        }
    }
}
```

### @Stable과 @Immutable 어노테이션

#### @Stable

```kotlin
/**
 * @Stable: 타입이 안정적임을 Compose에 알림
 * 
 * 사용 시나리오:
 * - 내부적으로 mutable하지만 변경 시 알림을 보장
 * - 예: MutableState, ViewModel
 */
@Stable
class Counter {
    private var _count = mutableStateOf(0)
    val count: State<Int> = _count
    
    fun increment() {
        _count.value++  // 변경 시 자동으로 알림
    }
}

@Composable
fun CounterDisplay(counter: Counter) {
    // counter.count가 변경될 때만 Recompose
    Text("Count: ${counter.count.value}")
}
```

#### @Immutable

```kotlin
/**
 * @Immutable: 타입이 불변임을 Compose에 알림
 * 
 * 사용 시나리오:
 * - 생성 후 절대 변경되지 않는 타입
 * - 더 강력한 최적화 가능
 */
@Immutable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String
)

@Composable
fun UserCard(user: User) {
    // user 인스턴스가 변경될 때만 Recompose
    Card {
        Column {
            AsyncImage(url = user.profileImageUrl)
            Text(user.name)
            Text(user.email)
        }
    }
}
```

### 컬렉션 안정성

```kotlin
// ❌ Unstable: Mutable 컬렉션
@Composable
fun UnstableList(items: MutableList<String>) {
    // items의 내용이 변경되어도 Compose가 감지 못함
    LazyColumn {
        items(items.size) { index ->
            Text(items[index])
        }
    }
}

// ✅ Stable: Immutable 컬렉션
@Composable
fun StableList(items: List<String>) {
    // items가 변경되면 새 리스트 인스턴스 생성됨
    LazyColumn {
        items(items.size) { index ->
            Text(items[index])
        }
    }
}

// ✅ 더 나은 방법: State로 감싸기
@Composable
fun BestList(items: State<List<String>>) {
    // items.value가 변경될 때만 Recompose
    LazyColumn {
        items(items.value.size) { index ->
            Text(items.value[index])
        }
    }
}
```

### 커스텀 타입 최적화

```kotlin
/**
 * 복잡한 비즈니스 로직을 가진 클래스
 * 
 * 내부적으로는 mutable하지만 외부에는 immutable하게 노출
 */
@Stable
class ShoppingCart {
    // 내부 상태는 mutable
    private val _items = mutableStateListOf<CartItem>()
    
    // 외부에는 읽기 전용으로 노출
    val items: List<CartItem> = _items
    
    private val _totalPrice = mutableStateOf(0)
    val totalPrice: State<Int> = _totalPrice
    
    /**
     * 아이템 추가
     * 
     * 변경 시 자동으로 Composition에 알림
     */
    fun addItem(item: CartItem) {
        _items.add(item)
        recalculateTotal()
    }
    
    /**
     * 아이템 제거
     */
    fun removeItem(itemId: String) {
        _items.removeAll { it.id == itemId }
        recalculateTotal()
    }
    
    /**
     * 총 가격 재계산
     */
    private fun recalculateTotal() {
        _totalPrice.value = _items.sumOf { it.price * it.quantity }
    }
}

@Immutable
data class CartItem(
    val id: String,
    val name: String,
    val price: Int,
    val quantity: Int
)

@Composable
fun ShoppingCartScreen(cart: ShoppingCart) {
    Column {
        // totalPrice가 변경될 때만 Recompose
        Text(
            text = "총 금액: ${cart.totalPrice.value}원",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // items가 변경될 때만 Recompose
        LazyColumn {
            items(cart.items, key = { it.id }) { item ->
                CartItemRow(
                    item = item,
                    onRemove = { cart.removeItem(item.id) }
                )
            }
        }
    }
}
```

---

## LazyList 최적화

### key 파라미터의 중요성

```kotlin
data class Message(
    val id: String,
    val text: String,
    val timestamp: Long
)

// ❌ 나쁜 예: key 없음
@Composable
fun BadMessageList(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageItem(message)
        }
    }
}

// ✅ 좋은 예: key 사용
@Composable
fun GoodMessageList(messages: List<Message>) {
    LazyColumn {
        // key를 사용하면 아이템 재사용 최적화
        items(messages, key = { it.id }) { message ->
            MessageItem(message)
        }
    }
}
```

**key의 장점**:
1. **아이템 재사용**: 같은 key의 아이템은 재사용됨
2. **애니메이션**: 아이템 이동 시 부드러운 애니메이션
3. **상태 유지**: 아이템의 상태가 유지됨

### contentType 활용

```kotlin
sealed class FeedItem {
    data class TextPost(val id: String, val text: String) : FeedItem()
    data class ImagePost(val id: String, val imageUrl: String) : FeedItem()
    data class VideoPost(val id: String, val videoUrl: String) : FeedItem()
}

@Composable
fun FeedList(items: List<FeedItem>) {
    LazyColumn {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is FeedItem.TextPost -> item.id
                    is FeedItem.ImagePost -> item.id
                    is FeedItem.VideoPost -> item.id
                }
            },
            // contentType으로 뷰 타입 구분
            contentType = { item ->
                when (item) {
                    is FeedItem.TextPost -> "text"
                    is FeedItem.ImagePost -> "image"
                    is FeedItem.VideoPost -> "video"
                }
            }
        ) { item ->
            when (item) {
                is FeedItem.TextPost -> TextPostItem(item)
                is FeedItem.ImagePost -> ImagePostItem(item)
                is FeedItem.VideoPost -> VideoPostItem(item)
            }
        }
    }
}
```

**contentType의 장점**:
- 같은 타입의 아이템만 재사용
- 메모리 효율 향상
- 레이아웃 성능 개선

### prefetchDistance 조정

```kotlin
@Composable
fun OptimizedList(items: List<Item>) {
    // prefetchDistance: 화면 밖 아이템을 미리 로드하는 거리
    // 기본값: 2 (화면 밖 2개 아이템 미리 로드)
    
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        // 더 많은 아이템을 미리 로드 (스크롤이 빠른 경우)
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) {
        items(items, key = { it.id }) { item ->
            ItemRow(item)
        }
    }
}
```

### 아이템 크기 고정

```kotlin
@Composable
fun FixedSizeList(items: List<Item>) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            // ✅ 고정 크기 사용
            ItemRow(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)  // 고정 높이
            )
        }
    }
}

@Composable
fun ItemRow(item: Item, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_item),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(item.name)
    }
}
```

**고정 크기의 장점**:
- 레이아웃 계산 최적화
- 스크롤 성능 향상
- 메모리 사용 예측 가능

### 복잡한 아이템 최적화

```kotlin
@Composable
fun ComplexItemList(items: List<ComplexItem>) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            // ✅ 복잡한 아이템은 별도 Composable로 분리
            ComplexItemRow(item)
        }
    }
}

/**
 * 복잡한 아이템 컴포넌트
 * 
 * 별도 Composable로 분리하면:
 * - Recomposition Scope 최적화
 * - 코드 가독성 향상
 * - 재사용 가능
 */
@Composable
fun ComplexItemRow(item: ComplexItem) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            // 헤더 (항상 표시)
            ItemHeader(
                item = item,
                expanded = expanded,
                onExpandClick = { expanded = !expanded }
            )
            
            // 상세 정보 (확장 시만 표시)
            AnimatedVisibility(visible = expanded) {
                ItemDetails(item)
            }
        }
    }
}

@Composable
private fun ItemHeader(
    item: ComplexItem,
    expanded: Boolean,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = "확장"
        )
    }
}

@Composable
private fun ItemDetails(item: ComplexItem) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("상세 정보", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Text(item.description)
        
        // 추가 정보들...
        item.tags.forEach { tag ->
            Chip(text = tag)
        }
    }
}
```

---

## 실전 최적화 사례

### 사례 1: 뉴스 피드 앱

```kotlin
/**
 * 뉴스 피드 화면
 * 
 * 최적화 포인트:
 * 1. 검색/필터링은 derivedStateOf 사용
 * 2. LazyColumn에 key 사용
 * 3. 이미지 로딩 최적화
 * 4. 페이징 구현
 */
@Composable
fun NewsFeedScreen(
    viewModel: NewsFeedViewModel = viewModel()
) {
    val articles by viewModel.articles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    // ✅ 검색/필터링 최적화
    val filteredArticles by remember {
        derivedStateOf {
            articles
                .filter { article ->
                    // 검색어 필터
                    if (searchQuery.isNotEmpty()) {
                        article.title.contains(searchQuery, ignoreCase = true) ||
                        article.content.contains(searchQuery, ignoreCase = true)
                    } else true
                }
                .filter { article ->
                    // 카테고리 필터
                    selectedCategory?.let { article.category == it } ?: true
                }
        }
    }
    
    Column {
        // 검색바
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 카테고리 필터
        CategoryFilter(
            selectedCategory = selectedCategory,
            onCategorySelect = { selectedCategory = it }
        )
        
        // 뉴스 리스트
        LazyColumn {
            items(
                items = filteredArticles,
                key = { it.id },  // ✅ key 사용
                contentType = { "article" }
            ) { article ->
                ArticleCard(
                    article = article,
                    onClick = { viewModel.onArticleClick(article) }
                )
            }
        }
    }
}

/**
 * 뉴스 카드 컴포넌트
 * 
 * 최적화:
 * - 이미지 로딩 최적화 (Coil)
 * - 고정 높이 사용
 */
@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)  // ✅ 고정 높이
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row {
            // 썸네일 이미지
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.thumbnailUrl)
                    .crossfade(true)
                    .size(120)  // ✅ 이미지 크기 제한
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Crop
            )
            
            // 기사 정보
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.weight(1f))
                
                Row {
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = article.publishedAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val thumbnailUrl: String,
    val source: String,
    val category: String,
    val publishedAt: String
)
```

### 사례 2: 채팅 앱

```kotlin
/**
 * 채팅 화면
 * 
 * 최적화 포인트:
 * 1. 역순 리스트 (최신 메시지가 아래)
 * 2. 메시지 타입별 contentType
 * 3. 자동 스크롤
 * 4. 메시지 그룹화
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    
    // ✅ 새 메시지 도착 시 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column {
        // 메시지 리스트
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = false  // 최신 메시지가 아래
        ) {
            items(
                items = messages,
                key = { it.id },
                // ✅ 메시지 타입별 contentType
                contentType = { message ->
                    when (message) {
                        is Message.Text -> "text"
                        is Message.Image -> "image"
                        is Message.System -> "system"
                    }
                }
            ) { message ->
                when (message) {
                    is Message.Text -> TextMessageBubble(message)
                    is Message.Image -> ImageMessageBubble(message)
                    is Message.System -> SystemMessage(message)
                }
            }
        }
        
        // 입력창
        MessageInput(
            onSendMessage = { text ->
                viewModel.sendMessage(text)
            }
        )
    }
}

/**
 * 텍스트 메시지 버블
 */
@Composable
fun TextMessageBubble(message: Message.Text) {
    val isMyMessage = message.senderId == "me"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isMyMessage) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isMyMessage) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMyMessage) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

sealed class Message {
    abstract val id: String
    abstract val timestamp: String
    
    data class Text(
        override val id: String,
        override val timestamp: String,
        val senderId: String,
        val text: String
    ) : Message()
    
    data class Image(
        override val id: String,
        override val timestamp: String,
        val senderId: String,
        val imageUrl: String
    ) : Message()
    
    data class System(
        override val id: String,
        override val timestamp: String,
        val text: String
    ) : Message()
}
```

### 사례 3: 이커머스 앱

```kotlin
/**
 * 상품 목록 화면
 * 
 * 최적화 포인트:
 * 1. Grid 레이아웃
 * 2. 이미지 로딩 최적화
 * 3. 정렬/필터링 최적화
 * 4. 무한 스크롤
 */
@Composable
fun ProductGridScreen(
    viewModel: ProductViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    var sortOption by remember { mutableStateOf(SortOption.POPULAR) }
    var priceRange by remember { mutableStateOf(0f..100000f) }
    
    // ✅ 정렬/필터링 최적화
    val filteredProducts by remember {
        derivedStateOf {
            products
                .filter { it.price in priceRange }
                .sortedBy { product ->
                    when (sortOption) {
                        SortOption.POPULAR -> -product.popularity
                        SortOption.PRICE_LOW -> product.price
                        SortOption.PRICE_HIGH -> -product.price
                        SortOption.NEWEST -> -product.createdAt
                    }
                }
        }
    }
    
    Column {
        // 정렬/필터 바
        FilterBar(
            sortOption = sortOption,
            onSortChange = { sortOption = it },
            priceRange = priceRange,
            onPriceRangeChange = { priceRange = it }
        )
        
        // 상품 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = filteredProducts,
                key = { it.id },
                contentType = { "product" }
            ) { product ->
                ProductCard(
                    product = product,
                    onClick = { viewModel.onProductClick(product) }
                )
            }
        }
    }
}

/**
 * 상품 카드
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)  // ✅ 비율 고정
            .clickable(onClick = onClick)
    ) {
        Column {
            // 상품 이미지
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .size(400)  // ✅ 이미지 크기 제한
                    .build(),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
            
            // 상품 정보
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = "${product.price}원",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // 평점
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = product.rating.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val rating: Float,
    val popularity: Int,
    val createdAt: Long
)

enum class SortOption {
    POPULAR, PRICE_LOW, PRICE_HIGH, NEWEST
}
```

---

## 성능 측정

### Recomposition 횟수 측정

```kotlin
/**
 * Recomposition 횟수를 측정하는 유틸리티
 */
@Composable
fun RecompositionCounter(tag: String) {
    val count = remember { mutableStateOf(0) }
    
    SideEffect {
        count.value++
        Log.d("Recomposition", "$tag: ${count.value}")
    }
}

// 사용 예
@Composable
fun MyScreen() {
    RecompositionCounter("MyScreen")
    
    Column {
        Header()
        Content()
    }
}

@Composable
fun Header() {
    RecompositionCounter("Header")
    // ...
}
```

### Layout Inspector 활용

**Android Studio → View → Tool Windows → Layout Inspector**

1. **Recomposition Counts 활성화**
   - Layout Inspector 열기
   - "Show Recomposition Counts" 체크

2. **Recomposition 확인**
   - 각 Composable의 Recomposition 횟수 표시
   - 빨간색: 많이 Recompose됨 (최적화 필요)
   - 녹색: 적게 Recompose됨 (최적화됨)

### Compose Compiler Reports

```kotlin
// build.gradle.kts
android {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                project.buildDir.absolutePath + "/compose_metrics"
        )
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                project.buildDir.absolutePath + "/compose_metrics"
        )
    }
}
```

**생성되는 리포트**:
- `*-classes.txt`: Stable/Unstable 클래스 목록
- `*-composables.txt`: Composable 함수 정보
- `*-module.json`: 모듈 메트릭

---

## 💡 베스트 프랙티스 요약

### 1. Recomposition 최적화
- ✅ `remember`로 계산 결과 캐싱
- ✅ `derivedStateOf`로 파생 상태 최적화
- ✅ Composable을 작게 분리
- ✅ 불필요한 람다 재생성 방지

### 2. Stability 최적화
- ✅ `val` 사용 (var 대신)
- ✅ Immutable 컬렉션 사용
- ✅ `@Stable`, `@Immutable` 어노테이션 활용
- ✅ 커스텀 타입의 안정성 보장

### 3. LazyList 최적화
- ✅ `key` 파라미터 사용
- ✅ `contentType` 지정
- ✅ 아이템 크기 고정
- ✅ 복잡한 아이템은 별도 Composable로 분리

### 4. 이미지 최적화
- ✅ 이미지 크기 제한
- ✅ 적절한 캐싱 전략
- ✅ Placeholder 사용
- ✅ Crossfade 애니메이션

### 5. 성능 측정
- ✅ Layout Inspector 활용
- ✅ Compose Compiler Reports 확인
- ✅ Recomposition 횟수 모니터링

---

## 🎯 다음 단계

성능 최적화 기초를 마스터했습니다! 다음으로:

1. **[21-2. 메모리 및 배터리 최적화](./21-2-memory-battery-optimization.md)** - 메모리 프로파일링, 배터리 최적화
2. **[21-3. 성능 프로파일링 실전](./21-3-performance-profiling.md)** - Android Profiler, 실전 분석

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Optimizing! 🚀
