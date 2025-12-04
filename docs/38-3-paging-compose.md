# Paging 3 Compose 통합

> 📖 **시리즈 구성**
> - **38-1**: [Paging 3 기초](./38-1-paging-basics.md)
> - **38-2**: [Paging 3 고급](./38-2-paging-advanced.md)
> - **38-3**: Paging 3 Compose 통합 (현재 문서)

---

## 📚 목차

1. [LazyColumn 통합](#lazycolumn-통합)
2. [로딩 상태 처리](#로딩-상태-처리)
3. [Pull to Refresh](#pull-to-refresh)

---

## LazyColumn 통합

```kotlin
@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = articles.itemCount,
            key = { index -> articles[index]?.id ?: index }
        ) { index ->
            val article = articles[index]
            if (article != null) {
                ArticleItem(article)
            }
        }
        
        when (articles.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = "로드 실패",
                        onRetry = { articles.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}
```

---

## 로딩 상태 처리

```kotlin
@Composable
fun ArticleListWithStates(
    articles: LazyPagingItems<Article>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(articles.itemCount) { index ->
                articles[index]?.let { article ->
                    ArticleItem(article)
                }
            }
        }
        
        // 초기 로딩
        if (articles.loadState.refresh is LoadState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 초기 로딩 에러
        if (articles.loadState.refresh is LoadState.Error) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("로드 실패")
                Button(onClick = { articles.retry() }) {
                    Text("재시도")
                }
            }
        }
        
        // 빈 상태
        if (articles.loadState.refresh is LoadState.NotLoading && articles.itemCount == 0) {
            Text(
                "데이터가 없습니다",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

---

## Pull to Refresh

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListWithRefresh(
    articles: LazyPagingItems<Article>
) {
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            articles.refresh()
        }
    }
    
    LaunchedEffect(articles.loadState.refresh) {
        if (articles.loadState.refresh is LoadState.NotLoading) {
            pullRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        LazyColumn {
            items(articles.itemCount) { index ->
                articles[index]?.let { ArticleItem(it) }
            }
        }
        
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

---

**마지막 업데이트**: 2024-12-03
