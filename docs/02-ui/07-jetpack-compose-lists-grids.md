# Jetpack Compose 리스트와 그리드 완벽 가이드

## 📚 목차
1. [LazyColumn 기초](#lazycolumn-기초)
2. [LazyRow 기초](#lazyrow-기초)
3. [LazyVerticalGrid](#lazyverticalgrid)
4. [LazyHorizontalGrid](#lazyhorizontalgrid)
5. [고급 기능](#고급-기능)
6. [성능 최적화](#성능-최적화)
7. [실전 예제](#실전-예제)

---

## LazyColumn 기초

### LazyColumn이란?

**LazyColumn**은 RecyclerView의 Compose 버전으로, 많은 아이템을 효율적으로 표시합니다.

#### 일반 Column vs LazyColumn

| 특징 | Column | LazyColumn |
|------|--------|------------|
| **렌더링** | 모든 아이템 | 화면에 보이는 아이템만 |
| **성능** | 아이템 많으면 느림 | 항상 빠름 |
| **스크롤** | `verticalScroll()` 필요 | 자동 스크롤 |
| **사용 시기** | 아이템 < 10개 | 아이템 많을 때 |

```kotlin
// ❌ Column: 1000개 아이템 모두 렌더링 (느림)
Column {
    repeat(1000) { index ->
        Text("Item $index")
    }
}

// ✅ LazyColumn: 화면에 보이는 것만 렌더링 (빠름)
LazyColumn {
    items(1000) { index ->
        Text("Item $index")
    }
}
```

### 기본 사용법

```kotlin
@Composable
fun BasicLazyColumn() {
    LazyColumn {
        // 1. 단일 아이템
        item {
            Text("헤더")
        }
        
        // 2. 여러 아이템 (개수로)
        items(10) { index ->
            Text("Item $index")
        }
        
        // 3. 리스트로부터
        val fruits = listOf("사과", "바나나", "체리")
        items(fruits) { fruit ->
            Text(fruit)
        }
        
        // 4. 인덱스와 함께
        itemsIndexed(fruits) { index, fruit ->
            Text("$index: $fruit")
        }
    }
}
```

### items() 함수 변형

```kotlin
@Composable
fun ItemsVariations() {
    val contacts = listOf(
        Contact(1, "Alice", "010-1111-1111"),
        Contact(2, "Bob", "010-2222-2222"),
        Contact(3, "Charlie", "010-3333-3333")
    )
    
    LazyColumn {
        // 1. 기본
        items(contacts) { contact ->
            ContactItem(contact)
        }
        
        // 2. key 지정 (성능 최적화)
        items(
            items = contacts,
            key = { it.id }  // 고유 ID 지정
        ) { contact ->
            ContactItem(contact)
        }
        
        // 3. contentType 지정 (다른 타입 아이템)
        items(
            items = contacts,
            key = { it.id },
            contentType = { "contact" }
        ) { contact ->
            ContactItem(contact)
        }
    }
}
```

### LazyColumn 파라미터

```kotlin
LazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true
) {
    // 아이템들
}
```

#### 주요 파라미터 설명

```kotlin
@Composable
fun LazyColumnParameters() {
    LazyColumn(
        // 1. 패딩 (리스트 전체에 적용)
        contentPadding = PaddingValues(16.dp),
        
        // 2. 아이템 간격
        verticalArrangement = Arrangement.spacedBy(8.dp),
        
        // 3. 가로 정렬
        horizontalAlignment = Alignment.CenterHorizontally,
        
        // 4. 역순 배치
        reverseLayout = true  // 아래에서 위로
    ) {
        items(10) { index ->
            Text("Item $index")
        }
    }
}
```

### LazyListState

스크롤 위치를 제어하고 관찰할 수 있습니다.

```kotlin
@Composable
fun LazyColumnWithState() {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    Column {
        // 맨 위로 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        ) {
            Text("맨 위로")
        }
        
        LazyColumn(state = listState) {
            items(100) { index ->
                Text("Item $index")
            }
        }
    }
    
    // 스크롤 위치 관찰
    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
}
```

### 실전 예제: 연락처 리스트

```kotlin
data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String = ""
)

@Composable
fun ContactList() {
    val contacts = remember {
        List(50) { index ->
            Contact(
                id = index,
                name = "Contact ${index + 1}",
                phone = "010-${1000 + index}-${2000 + index}",
                email = "contact${index + 1}@example.com"
            )
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 헤더
        item {
            Text(
                text = "연락처 (${contacts.size})",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // 연락처 아이템
        items(
            items = contacts,
            key = { it.id }
        ) { contact ->
            ContactCard(contact)
        }
    }
}

@Composable
fun ContactCard(contact: Contact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.first().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 전화 버튼
            IconButton(onClick = { /* 전화 걸기 */ }) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "전화",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

---

## LazyRow 기초

### LazyRow란?

**LazyRow**는 가로 스크롤 리스트입니다.

```kotlin
@Composable
fun BasicLazyRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(20) { index ->
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item $index")
                }
            }
        }
    }
}
```

### 실전 예제: 카테고리 칩

```kotlin
@Composable
fun CategoryChips() {
    val categories = listOf(
        "전체", "음식", "카페", "쇼핑", "여행", "운동", "영화", "음악"
    )
    var selectedCategory by remember { mutableStateOf("전체") }
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { selectedCategory = category },
                label = { Text(category) }
            )
        }
    }
}
```

### 실전 예제: 이미지 갤러리

```kotlin
@Composable
fun ImageGallery() {
    val images = remember {
        List(10) { index -> "https://picsum.photos/200/300?random=$index" }
    }
    
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(images) { imageUrl ->
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                // AsyncImage (Coil 라이브러리 사용)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image")
                }
            }
        }
    }
}
```

---

## LazyVerticalGrid

### LazyVerticalGrid란?

**LazyVerticalGrid**는 그리드 레이아웃을 만듭니다.

```kotlin
@Composable
fun BasicGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),  // 3열 고정
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(30) { index ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)  // 정사각형
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("$index")
            }
        }
    }
}
```

### GridCells 옵션

```kotlin
// 1. 고정 열 개수
GridCells.Fixed(3)  // 3열

// 2. 최소 크기 지정
GridCells.Adaptive(minSize = 100.dp)  // 최소 100dp

// 3. FixedSize (고정 크기)
GridCells.FixedSize(120.dp)  // 각 셀 120dp
```

### 실전 예제: 사진 그리드

```kotlin
data class Photo(
    val id: Int,
    val title: String,
    val color: Color
)

@Composable
fun PhotoGrid() {
    val photos = remember {
        List(50) { index ->
            Photo(
                id = index,
                title = "Photo ${index + 1}",
                color = Color(
                    red = (0..255).random(),
                    green = (0..255).random(),
                    blue = (0..255).random()
                )
            )
        }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = photos,
            key = { it.id }
        ) { photo ->
            PhotoItem(photo)
        }
    }
}

@Composable
fun PhotoItem(photo: Photo) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(photo.color),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = photo.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

### 실전 예제: 앱 아이콘 그리드

```kotlin
data class App(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AppGrid() {
    val apps = remember {
        listOf(
            App("홈", Icons.Default.Home, Color(0xFF2196F3)),
            App("검색", Icons.Default.Search, Color(0xFF4CAF50)),
            App("설정", Icons.Default.Settings, Color(0xFF9C27B0)),
            App("프로필", Icons.Default.Person, Color(0xFFFF9800)),
            App("알림", Icons.Default.Notifications, Color(0xFFF44336)),
            App("메시지", Icons.Default.Email, Color(0xFF00BCD4)),
            App("사진", Icons.Default.Image, Color(0xFFE91E63)),
            App("음악", Icons.Default.MusicNote, Color(0xFF673AB7))
        )
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps) { app ->
            AppIcon(app)
        }
    }
}

@Composable
fun AppIcon(app: App) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { /* 앱 실행 */ }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(app.color, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.name,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
```

---

## LazyHorizontalGrid

### LazyHorizontalGrid란?

가로 스크롤 그리드입니다.

```kotlin
@Composable
fun HorizontalGrid() {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),  // 2행
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(20) { index ->
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("$index")
            }
        }
    }
}
```

---

## 고급 기능

### 1. Sticky Headers

섹션 헤더를 고정할 수 있습니다.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickyHeaderList() {
    val groupedContacts = listOf(
        "A" to listOf("Alice", "Andrew", "Anna"),
        "B" to listOf("Bob", "Betty", "Brian"),
        "C" to listOf("Charlie", "Chris", "Claire")
    )
    
    LazyColumn {
        groupedContacts.forEach { (initial, contacts) ->
            // Sticky Header
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 아이템들
            items(contacts) { name ->
                Text(
                    text = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
```

### 2. Pull to Refresh

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshList() {
    var isRefreshing by remember { mutableStateOf(false) }
    val items = remember { mutableStateListOf<String>() }
    
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            // 새로고침 로직
            delay(2000)
            items.add(0, "New Item ${items.size}")
            isRefreshing = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true }
            )
    ) {
        LazyColumn {
            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}
```

### 3. 무한 스크롤

```kotlin
@Composable
fun InfiniteScrollList() {
    var items by remember { mutableStateOf((1..20).toList()) }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // 마지막 아이템에 도달했는지 감지
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == items.size - 1
        }
    }
    
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading) {
            isLoading = true
            delay(1000)  // 네트워크 요청 시뮬레이션
            items = items + ((items.size + 1)..(items.size + 20))
            isLoading = false
        }
    }
    
    LazyColumn(state = listState) {
        items(items) { item ->
            Text(
                text = "Item $item",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
```

### 4. 아이템 애니메이션

```kotlin
@Composable
fun AnimatedList() {
    var items by remember { mutableStateOf(listOf("A", "B", "C")) }
    
    Column {
        Button(onClick = { items = items + "Item ${items.size}" }) {
            Text("추가")
        }
        
        LazyColumn {
            items(
                items = items,
                key = { it }
            ) { item ->
                // animateItemPlacement로 애니메이션 추가
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()  // 애니메이션!
                        .padding(16.dp)
                )
            }
        }
    }
}
```

---

## 성능 최적화

### 1. key 사용 (필수!)

```kotlin
// ❌ 나쁜 예: key 없음
LazyColumn {
    items(contacts) { contact ->
        ContactItem(contact)
    }
}

// ✅ 좋은 예: 고유 key 지정
LazyColumn {
    items(
        items = contacts,
        key = { it.id }  // 고유 ID 사용
    ) { contact ->
        ContactItem(contact)
    }
}
```

**key를 사용하는 이유**:
- 아이템 재사용 최적화
- 애니메이션 정확성
- 스크롤 위치 유지

### 2. contentType 사용

다른 타입의 아이템이 섞여 있을 때 사용합니다.

```kotlin
sealed class ListItem {
    data class Header(val title: String) : ListItem()
    data class Content(val text: String) : ListItem()
}

LazyColumn {
    items(
        items = mixedItems,
        key = { it.hashCode() },
        contentType = { item ->
            when (item) {
                is ListItem.Header -> "header"
                is ListItem.Content -> "content"
            }
        }
    ) { item ->
        when (item) {
            is ListItem.Header -> HeaderItem(item)
            is ListItem.Content -> ContentItem(item)
        }
    }
}
```

### 3. remember 사용

```kotlin
// ❌ 나쁜 예: 매번 새로운 객체 생성
LazyColumn {
    items(100) { index ->
        val color = Color(Random.nextInt())  // 매번 생성!
        Box(modifier = Modifier.background(color))
    }
}

// ✅ 좋은 예: remember로 캐싱
LazyColumn {
    items(100) { index ->
        val color = remember(index) {
            Color(Random.nextInt())
        }
        Box(modifier = Modifier.background(color))
    }
}
```

### 4. 고정 크기 사용

```kotlin
// 아이템 크기가 고정되어 있으면 성능 향상
LazyColumn {
    items(1000) { index ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)  // 고정 높이
        ) {
            Text("Item $index")
        }
    }
}
```

---

## 실전 예제

### 예제 1: 뉴스 피드

```kotlin
data class NewsItem(
    val id: Int,
    val title: String,
    val summary: String,
    val imageUrl: String,
    val timestamp: String
)

@Composable
fun NewsFeed() {
    val news = remember {
        List(20) { index ->
            NewsItem(
                id = index,
                title = "뉴스 제목 ${index + 1}",
                summary = "뉴스 요약 내용입니다. " + "더 많은 내용... ".repeat(5),
                imageUrl = "https://picsum.photos/400/200?random=$index",
                timestamp = "${index + 1}시간 전"
            )
        }
    }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = news,
            key = { it.id }
        ) { item ->
            NewsCard(item)
        }
    }
}

@Composable
fun NewsCard(news: NewsItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
            ) {
                // AsyncImage 사용
            }
            
            // 내용
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = news.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = news.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### 예제 2: 쇼핑 상품 그리드

```kotlin
data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val rating: Float
)

@Composable
fun ProductGrid() {
    val products = remember {
        List(50) { index ->
            Product(
                id = index,
                name = "상품 ${index + 1}",
                price = (10000..100000).random(),
                imageUrl = "https://picsum.photos/300/300?random=$index",
                rating = (3.0..5.0).random().toFloat()
            )
        }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = products,
            key = { it.id }
        ) { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* 상품 상세 */ }
    ) {
        Column {
            // 상품 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.LightGray)
            )
            
            // 상품 정보
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${product.price}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", product.rating),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
```

---

## 학습 체크리스트

### LazyColumn
- [ ] LazyColumn과 Column의 차이를 안다
- [ ] items() 함수를 사용할 수 있다
- [ ] key를 지정할 수 있다
- [ ] LazyListState를 사용할 수 있다
- [ ] 스크롤 위치를 제어할 수 있다

### LazyRow
- [ ] LazyRow를 사용할 수 있다
- [ ] 가로 스크롤 리스트를 만들 수 있다

### LazyVerticalGrid
- [ ] GridCells 옵션을 안다
- [ ] 그리드 레이아웃을 만들 수 있다
- [ ] aspectRatio를 사용할 수 있다

### 고급 기능
- [ ] Sticky Headers를 구현할 수 있다
- [ ] Pull to Refresh를 구현할 수 있다
- [ ] 무한 스크롤을 구현할 수 있다
- [ ] 아이템 애니메이션을 추가할 수 있다

### 성능 최적화
- [ ] key의 중요성을 이해한다
- [ ] contentType을 사용할 수 있다
- [ ] remember로 최적화할 수 있다

---

## 다음 단계

### 추천 학습 순서

1. ✅ Kotlin 기초
2. ✅ Android 프로젝트 구조
3. ✅ Layout & UI
4. ✅ State 관리
5. ✅ Navigation
6. ✅ 테마와 스타일링
7. ✅ 리스트와 그리드 (완료)
8. ➡️ 폼 입력과 유효성 검사
9. ➡️ 애니메이션

---

## 참고 자료

### 공식 문서
- [Lists and grids](https://developer.android.com/jetpack/compose/lists)
- [Lazy layouts](https://developer.android.com/jetpack/compose/layouts/lazy)
- [Performance best practices](https://developer.android.com/jetpack/compose/performance)

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

**학습 예상 시간**: 2-3시간  
**난이도**: ⭐⭐⭐

리스트와 그리드를 마스터하면 대부분의 앱 UI를 만들 수 있습니다! 📱
