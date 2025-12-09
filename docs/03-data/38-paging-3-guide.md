# Paging 3 가이드

> [!NOTE]
> **이 문서는 새로운 종합 가이드 시리즈로 대체되었습니다!**
> 
> 1. **[38-1. Paging 3 기초](./38-1-paging-basics.md)** - PagingSource, Pager 생성
> 2. **[38-2. Paging 3 고급](./38-2-paging-advanced.md)** - RemoteMediator, 검색/필터링
> 3. **[38-3. Paging 3 Compose 통합](./38-3-paging-compose.md)** - LazyColumn, Pull to Refresh

---

## 🚀 빠른 시작

**[👉 38-1. Paging 3 기초로 이동](./38-1-paging-basics.md)**

---

**마지막 업데이트**: 2024-12-03


---

## Paging 3란?

> [!NOTE]
> **Paging 3 = 대량 데이터를 효율적으로 로드하는 라이브러리**
> 
> **주요 특징:**
> - 📊 무한 스크롤
> - ⚡ 메모리 효율적
> - 🔄 자동 재시도
> - 💾 캐싱 지원
> - 🎯 Compose 통합

### 왜 Paging이 필요한가?

**Paging 없이:**
```kotlin
// 모든 데이터를 한 번에 로드
val allItems = api.getAllItems()  // 10,000개!
// 메모리 부족, 느린 로딩, 나쁜 UX
```

**Paging 사용:**
```kotlin
// 필요한 만큼만 로드
val pagedItems = Pager(config).flow  // 20개씩
// 빠른 로딩, 메모리 효율적, 좋은 UX
```

**통계:**
- 메모리 사용량: **90% 감소**
- 초기 로딩 시간: **80% 단축**
- 사용자 경험: **크게 개선**

---

## 기본 설정

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // Paging 3
    val pagingVersion = "3.2.1"
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    
    // Compose 통합
    implementation("androidx.paging:paging-compose:$pagingVersion")
    
    // 기존 의존성
    implementation("androidx.room:room-paging:2.6.1")  // Room 통합
}
```

### 핵심 개념

**Paging 3 아키텍처:**
```
┌─────────────┐
│   UI Layer  │  ← LazyColumn + PagingData
└──────┬──────┘
       │
┌──────▼──────┐
│  ViewModel  │  ← Pager + Flow<PagingData>
└──────┬──────┘
       │
┌──────▼──────┐
│ Repository  │  ← PagingSource
└──────┬──────┘
       │
┌──────▼──────┐
│ Data Source │  ← API / Database
└─────────────┘
```

**주요 컴포넌트:**
```kotlin
// 1. PagingSource
// - 데이터 로드 로직
// - 페이지 키 관리

// 2. Pager
// - PagingData 생성
// - 설정 관리

// 3. PagingData
// - UI에 전달되는 데이터
// - Flow로 제공

// 4. LazyPagingItems
// - Compose에서 사용
// - LazyColumn 통합
```

---

## PagingSource

### 기본 PagingSource

```kotlin
import androidx.paging.PagingSource
import androidx.paging.PagingState

// 데이터 모델
data class Article(
    val id: Int,
    val title: String,
    val content: String
)

// PagingSource 구현
class ArticlePagingSource(
    private val api: ApiService
) : PagingSource<Int, Article>() {
    
    // 데이터 로드
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            // 현재 페이지 (null이면 첫 페이지)
            val page = params.key ?: 1
            
            // API 호출
            val response = api.getArticles(
                page = page,
                pageSize = params.loadSize
            )
            
            // 성공
            LoadResult.Page(
                data = response.articles,  // 로드된 데이터
                prevKey = if (page == 1) null else page - 1,  // 이전 페이지 키
                nextKey = if (response.articles.isEmpty()) null else page + 1  // 다음 페이지 키
            )
        } catch (e: Exception) {
            // 실패
            LoadResult.Error(e)
        }
    }
    
    // 새로고침 시 시작 키
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // 현재 위치에서 가장 가까운 페이지
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
```

**LoadParams 설명:**
```kotlin
params.key          // 현재 페이지 키 (Int, String 등)
params.loadSize     // 로드할 항목 수
params.refresh      // 새로고침 여부
```

**LoadResult 타입:**
```kotlin
// 성공
LoadResult.Page(
    data = items,      // 로드된 데이터
    prevKey = prevKey, // 이전 페이지 키 (null이면 첫 페이지)
    nextKey = nextKey  // 다음 페이지 키 (null이면 마지막 페이지)
)

// 실패
LoadResult.Error(exception)

// 잘못된 요청
LoadResult.Invalid()
```

### Repository에서 Pager 생성

```kotlin
class ArticleRepository(
    private val api: ApiService
) {
    // Pager 생성
    fun getArticlesPager(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,              // 페이지당 항목 수
                enablePlaceholders = false, // 플레이스홀더 사용 여부
                initialLoadSize = 20        // 초기 로드 크기
            ),
            pagingSourceFactory = {
                // PagingSource 생성
                ArticlePagingSource(api)
            }
        ).flow
    }
}
```

**PagingConfig 옵션:**
```kotlin
PagingConfig(
    pageSize = 20,              // 페이지당 항목 수 (필수)
    
    prefetchDistance = 5,       // 미리 로드할 거리 (기본: pageSize)
    
    enablePlaceholders = false, // 플레이스홀더 사용 (기본: true)
    
    initialLoadSize = 60,       // 초기 로드 크기 (기본: pageSize * 3)
    
    maxSize = 200,              // 최대 메모리 항목 수
    
    jumpThreshold = Int.MIN_VALUE  // 점프 임계값
)
```

### ViewModel에서 사용

```kotlin
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    // PagingData Flow
    val articlesFlow: Flow<PagingData<Article>> = repository.getArticlesPager()
        .cachedIn(viewModelScope)  // ViewModel 범위에서 캐싱
}
```

**cachedIn() 설명:**
```kotlin
// cachedIn()이 하는 일:
// 1. 구성 변경 시 데이터 유지
// 2. 여러 구독자 지원
// 3. 메모리 효율적 캐싱
```

---

## RemoteMediator

> [!IMPORTANT]
> **RemoteMediator = 네트워크 + 로컬 DB 통합**
> 
> **사용 사례:**
> - 오프라인 지원
> - 캐싱
> - 네트워크 + DB

### Room Database 설정

```kotlin
// Entity
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val page: Int
)

// RemoteKeys (페이지 정보 저장)
@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val articleId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)

// DAO
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY page ASC, id ASC")
    fun getAllArticles(): PagingSource<Int, ArticleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)
    
    @Query("DELETE FROM articles")
    suspend fun clearAll()
}

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<RemoteKeys>)
    
    @Query("SELECT * FROM remote_keys WHERE articleId = :id")
    suspend fun remoteKeysArticleId(id: Int): RemoteKeys?
    
    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}

// Database
@Database(
    entities = [ArticleEntity::class, RemoteKeys::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}
```

### RemoteMediator 구현

```kotlin
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val api: ApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, ArticleEntity>() {
    
    private val articleDao = database.articleDao()
    private val remoteKeysDao = database.remoteKeysDao()
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {
            // 로드할 페이지 결정
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    // 새로고침
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    // 이전 페이지
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKeys?.prevKey
                    prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // 다음 페이지
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                    nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            
            // API 호출
            val response = api.getArticles(page = page, pageSize = state.config.pageSize)
            val articles = response.articles
            val endOfPaginationReached = articles.isEmpty()
            
            // Database 트랜잭션
            database.withTransaction {
                // 새로고침 시 기존 데이터 삭제
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearRemoteKeys()
                    articleDao.clearAll()
                }
                
                // RemoteKeys 생성
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = articles.map {
                    RemoteKeys(
                        articleId = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                
                // 데이터 저장
                remoteKeysDao.insertAll(keys)
                articleDao.insertAll(articles.map { it.copy(page = page) })
            }
            
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
    
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ArticleEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { article ->
                remoteKeysDao.remoteKeysArticleId(article.id)
            }
    }
    
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ArticleEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { article ->
                remoteKeysDao.remoteKeysArticleId(article.id)
            }
    }
    
    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ArticleEntity>
    ): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                remoteKeysDao.remoteKeysArticleId(id)
            }
        }
    }
}
```

### RemoteMediator 사용

```kotlin
class ArticleRepository(
    private val api: ApiService,
    private val database: AppDatabase
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getArticlesPager(): Flow<PagingData<ArticleEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = ArticleRemoteMediator(api, database),
            pagingSourceFactory = { database.articleDao().getAllArticles() }
        ).flow
    }
}
```

---

## Compose 통합

### LazyColumn에서 사용

```kotlin
@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel = hiltViewModel()
) {
    // PagingData를 LazyPagingItems로 변환
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 항목 표시
        items(
            count = articles.itemCount,
            key = { index -> articles[index]?.id ?: index }
        ) { index ->
            val article = articles[index]
            if (article != null) {
                ArticleItem(article)
            }
        }
        
        // 로딩 상태
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
        onClick = { /* 상세 보기 */ }
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("재시도")
            }
        }
    }
}
```

### 로딩 상태 처리

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

### Pull to Refresh

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

## 고급 기능

### 검색 및 필터링

```kotlin
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    val articlesFlow: Flow<PagingData<Article>> = searchQuery
        .debounce(300)  // 300ms 디바운스
        .flatMapLatest { query ->
            repository.searchArticles(query)
        }
        .cachedIn(viewModelScope)
    
    fun search(query: String) {
        _searchQuery.value = query
    }
}

class ArticleRepository(private val api: ApiService) {
    fun searchArticles(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                SearchArticlePagingSource(api, query)
            }
        ).flow
    }
}
```

### 데이터 변환

```kotlin
// PagingData 변환
val transformedArticles: Flow<PagingData<ArticleUiModel>> = articlesFlow
    .map { pagingData ->
        pagingData.map { article ->
            ArticleUiModel(
                id = article.id,
                title = article.title.uppercase(),
                formattedDate = formatDate(article.date)
            )
        }
    }
```

---

## 실전 예제

### 완전한 Paging 시스템

```kotlin
// API Service
interface ApiService {
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): ArticleResponse
}

data class ArticleResponse(
    val articles: List<Article>,
    val totalPages: Int
)

// PagingSource
class ArticlePagingSource(
    private val api: ApiService,
    private val query: String
) : PagingSource<Int, Article>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val response = api.getArticles(page, params.loadSize)
            
            LoadResult.Page(
                data = response.articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= response.totalPages) null else page + 1
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

// Repository
class ArticleRepository(private val api: ApiService) {
    fun getArticles(query: String = ""): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { ArticlePagingSource(api, query) }
        ).flow
    }
}

// ViewModel
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    
    val articlesFlow: Flow<PagingData<Article>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.getArticles(query)
        }
        .cachedIn(viewModelScope)
    
    fun search(query: String) {
        _searchQuery.value = query
    }
}

// UI
@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val articles = viewModel.articlesFlow.collectAsLazyPagingItems()
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 검색창
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.search(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("검색...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )
        
        // 리스트
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = articles.itemCount,
                key = { articles[it]?.id ?: it }
            ) { index ->
                articles[index]?.let { article ->
                    ArticleItem(article)
                }
            }
            
            // 로딩 상태
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
}
```

---

## 💡 베스트 프랙티스

### 1. cachedIn() 사용

```kotlin
// ✅ ViewModel에서 캐싱
val articlesFlow = repository.getArticles()
    .cachedIn(viewModelScope)

// ❌ 캐싱 없음 (구성 변경 시 재로드)
val articlesFlow = repository.getArticles()
```

### 2. 적절한 페이지 크기

```kotlin
// ✅ 적절한 크기 (20-50)
PagingConfig(pageSize = 20)

// ❌ 너무 작음 (네트워크 요청 많음)
PagingConfig(pageSize = 5)

// ❌ 너무 큼 (메모리 많이 사용)
PagingConfig(pageSize = 200)
```

### 3. 에러 처리

```kotlin
// ✅ 재시도 제공
when (articles.loadState.append) {
    is LoadState.Error -> {
        ErrorItem(onRetry = { articles.retry() })
    }
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Page Through Data! 📄
