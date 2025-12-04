# Paging 3 Compose 통합

> 📖 **시리즈 구성**
> - **38-1**: [Paging 3 기초](./38-1-paging-basics.md)
> - **38-2**: [Paging 3 고급](./38-2-paging-advanced.md)
> - **38-3**: Paging 3 Compose 통합 (현재 문서)

---

## 📚 목차

1. [LazyPagingItems](#lazypagingitems)
2. [로딩 상태 UI](#로딩-상태-ui)
3. [에러 처리 UI](#에러-처리-ui)
4. [Pull to Refresh](#pull-to-refresh)
5. [실전 예제](#실전-예제)

---

## LazyPagingItems

### collectAsLazyPagingItems()

```kotlin
/**
 * Compose에서 PagingData 사용
 */
@Composable
fun ArticleListScreen(viewModel: ArticleViewModel = viewModel()) {
    // Flow<PagingData>를 LazyPagingItems로 변환
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        items(articles.itemCount) { index ->
            articles[index]?.let { article ->
                ArticleItem(article)
            }
        }
    }
}
```

### LazyPagingItems 메서드

```kotlin
/**
 * LazyPagingItems 주요 메서드
 */
@Composable
fun PagingItemsExample() {
    val items = viewModel.itemsFlow.collectAsLazyPagingItems()
    
    // 1. itemCount: 현재 로드된 아이템 개수
    val count = items.itemCount
    
    // 2. get(index): 특정 인덱스의 아이템 가져오기
    val item = items[0]  // null 가능
    
    // 3. peek(index): 로드 트리거 없이 아이템 가져오기
    val peekedItem = items.peek(0)
    
    // 4. refresh(): 새로고침
    items.refresh()
    
    // 5. retry(): 실패한 로드 재시도
    items.retry()
    
    // 6. loadState: 로딩 상태
    val loadState = items.loadState
}
```

### items() 확장 함수

```kotlin
/**
 * LazyListScope.items() 확장 함수
 */
@Composable
fun ArticleList() {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        // 방법 1: itemCount 사용
        items(articles.itemCount) { index ->
            articles[index]?.let { article ->
                ArticleItem(article)
            }
        }
        
        // 방법 2: items() 확장 함수 (권장)
        items(
            count = articles.itemCount,
            key = { index -> articles.peek(index)?.id ?: index }
        ) { index ->
            articles[index]?.let { article ->
                ArticleItem(article)
            }
        }
    }
}

/**
 * ArticleItem Composable
 */
@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

---

## 로딩 상태 UI

### LoadState 처리

```kotlin
/**
 * 로딩 상태 UI
 */
@Composable
fun ArticleListWithLoadingState() {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        items(articles.itemCount) { index ->
            articles[index]?.let { article ->
                ArticleItem(article)
            }
        }
        
        // 로딩 상태에 따른 UI
        when (articles.loadState.append) {
            is LoadState.Loading -> {
                item {
                    LoadingItem()
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = (articles.loadState.append as LoadState.Error).error.message,
                        onRetry = { articles.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

/**
 * 로딩 아이템
 */
@Composable
fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 에러 아이템
 */
@Composable
fun ErrorItem(
    message: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message ?: "알 수 없는 오류",
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("재시도")
        }
    }
}
```

### 초기 로딩 상태

```kotlin
/**
 * 초기 로딩 및 빈 상태 처리
 */
@Composable
fun ArticleListWithStates() {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // 초기 로딩
            articles.loadState.refresh is LoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // 초기 로딩 에러
            articles.loadState.refresh is LoadState.Error -> {
                ErrorScreen(
                    message = (articles.loadState.refresh as LoadState.Error).error.message,
                    onRetry = { articles.refresh() }
                )
            }
            
            // 빈 상태
            articles.itemCount == 0 -> {
                EmptyScreen()
            }
            
            // 정상 상태
            else -> {
                LazyColumn {
                    items(articles.itemCount) { index ->
                        articles[index]?.let { article ->
                            ArticleItem(article)
                        }
                    }
                    
                    // Append 로딩
                    if (articles.loadState.append is LoadState.Loading) {
                        item { LoadingItem() }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("데이터가 없습니다")
    }
}

@Composable
fun ErrorScreen(
    message: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message ?: "오류가 발생했습니다",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("재시도")
        }
    }
}
```

---

## 에러 처리 UI

### Snackbar로 에러 표시

```kotlin
/**
 * Snackbar로 에러 표시
 */
@Composable
fun ArticleListWithSnackbar() {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 에러 감지
    LaunchedEffect(articles.loadState) {
        val error = when {
            articles.loadState.refresh is LoadState.Error -> 
                (articles.loadState.refresh as LoadState.Error).error
            articles.loadState.append is LoadState.Error -> 
                (articles.loadState.append as LoadState.Error).error
            else -> null
        }
        
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it.message ?: "오류가 발생했습니다",
                    actionLabel = "재시도",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(articles.itemCount) { index ->
                articles[index]?.let { article ->
                    ArticleItem(article)
                }
            }
        }
    }
}
```

---

## Pull to Refresh

### SwipeRefresh 통합

```kotlin
/**
 * Pull to Refresh
 */
@Composable
fun ArticleListWithRefresh() {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    val isRefreshing = articles.loadState.refresh is LoadState.Loading
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { articles.refresh() }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            items(articles.itemCount) { index ->
                articles[index]?.let { article ->
                    ArticleItem(article)
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

---

## 실전 예제

### 예제: 완전한 뉴스 앱 UI

```kotlin
/**
 * 완전한 뉴스 앱 화면
 */
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = viewModel()
) {
    val articles = viewModel.newsFlow.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 에러 처리
    LaunchedEffect(articles.loadState) {
        val error = when {
            articles.loadState.refresh is LoadState.Error -> 
                (articles.loadState.refresh as LoadState.Error).error
            articles.loadState.append is LoadState.Error -> 
                (articles.loadState.append as LoadState.Error).error
            else -> null
        }
        
        error?.let {
            val result = snackbarHostState.showSnackbar(
                message = it.message ?: "오류가 발생했습니다",
                actionLabel = "재시도",
                duration = SnackbarDuration.Long
            )
            
            if (result == SnackbarResult.ActionPerformed) {
                articles.retry()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("뉴스") },
                actions = {
                    IconButton(onClick = { articles.refresh() }) {
                        Icon(Icons.Default.Refresh, "새로고침")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // 초기 로딩
                articles.loadState.refresh is LoadState.Loading && articles.itemCount == 0 -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // 빈 상태
                articles.itemCount == 0 -> {
                    EmptyNewsScreen()
                }
                
                // 정상 상태
                else -> {
                    NewsList(articles)
                }
            }
        }
    }
}

@Composable
fun NewsList(articles: LazyPagingItems<NewsUiModel>) {
    LazyColumn {
        items(
            count = articles.itemCount,
            key = { index -> articles.peek(index)?.id ?: index }
        ) { index ->
            articles[index]?.let { article ->
                NewsItem(article)
                
                if (index < articles.itemCount - 1) {
                    HorizontalDivider()
                }
            }
        }
        
        // Append 로딩
        when (articles.loadState.append) {
            is LoadState.Loading -> {
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
            is LoadState.Error -> {
                item {
                    AppendErrorItem(
                        message = (articles.loadState.append as LoadState.Error).error.message,
                        onRetry = { articles.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun NewsItem(article: NewsUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* 상세 화면으로 이동 */ }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // 이미지
            article.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
            }
            
            // 텍스트
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        text = article.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = article.publishedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNewsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Article,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "뉴스가 없습니다",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AppendErrorItem(
    message: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message ?: "오류가 발생했습니다",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
}
```

### 예제: 검색 화면

```kotlin
/**
 * 검색 화면
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 검색 바
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.search(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // 검색 결과
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                searchQuery.isBlank() -> {
                    SearchPlaceholder()
                }
                searchResults.loadState.refresh is LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                searchResults.itemCount == 0 -> {
                    NoResultsScreen(query = searchQuery)
                }
                else -> {
                    SearchResultsList(searchResults)
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("검색어를 입력하세요") },
        leadingIcon = {
            Icon(Icons.Default.Search, "검색")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "지우기")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun SearchPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "검색어를 입력하세요",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun NoResultsScreen(query: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "'$query'에 대한 검색 결과가 없습니다",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SearchResultsList(results: LazyPagingItems<Article>) {
    LazyColumn {
        items(results.itemCount) { index ->
            results[index]?.let { article ->
                ArticleItem(article)
            }
        }
        
        if (results.loadState.append is LoadState.Loading) {
            item { LoadingItem() }
        }
    }
}
```

---

## 💡 베스트 프랙티스 요약

### LazyPagingItems
- ✅ collectAsLazyPagingItems() 사용
- ✅ key 파라미터 지정 (성능)
- ✅ peek()으로 로드 트리거 방지
- ✅ null 체크 필수

### 로딩 상태
- ✅ loadState.refresh 확인
- ✅ loadState.append 확인
- ✅ 초기 로딩 UI 제공
- ✅ 빈 상태 UI 제공

### 에러 처리
- ✅ Snackbar로 에러 표시
- ✅ 재시도 버튼 제공
- ✅ 사용자 친화적 메시지
- ✅ 에러 로깅

### 성능 최적화
- ✅ key 파라미터 사용
- ✅ 불필요한 Recomposition 방지
- ✅ remember 활용
- ✅ LaunchedEffect로 부수효과 처리

---

## 🎯 완료!

Paging 3 시리즈를 모두 마스터했습니다!

**학습한 내용:**
1. **38-1. Paging 3 기초** - PagingSource, Pager, PagingConfig
2. **38-2. Paging 3 고급** - RemoteMediator, Room 통합, 캐싱
3. **38-3. Paging 3 Compose 통합** - LazyPagingItems, UI 통합

**다음 단계:**
- 실제 프로젝트에 적용
- 성능 모니터링
- 사용자 피드백 수집

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Paging! 📄
