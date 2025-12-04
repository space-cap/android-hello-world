# Paging 3 기초

> 📖 **시리즈 구성**
> - **38-1**: Paging 3 기초 (현재 문서)
> - **38-2**: [Paging 3 고급](./38-2-paging-advanced.md)
> - **38-3**: [Paging 3 Compose 통합](./38-3-paging-compose.md)

---

## 📚 목차

1. [Paging 3란?](#paging-3란)
2. [기본 설정](#기본-설정)
3. [PagingSource 만들기](#pagingsource-만들기)
4. [Pager 생성](#pager-생성)
5. [에러 처리](#에러-처리)
6. [로딩 상태](#로딩-상태)
7. [PagingConfig 최적화](#pagingconfig-최적화)
8. [실전 예제](#실전-예제)

---

## Paging 3란?

### 왜 Paging이 필요한가?

**문제 상황:**

```kotlin
// ❌ 나쁜 예: 모든 데이터를 한 번에 로드
@Composable
fun BadNewsList(viewModel: NewsViewModel) {
    val allNews by viewModel.allNews.collectAsState()  // 10,000개!
    
    LazyColumn {
        items(allNews) { news ->
            NewsItem(news)
        }
    }
}

/**
 * 문제점:
 * - 메모리 부족 (OOM)
 * - 느린 로딩 시간
 * - 불필요한 네트워크 사용
 * - 배터리 소모
 */
```

**Paging 해결:**

```kotlin
// ✅ 좋은 예: 필요한 만큼만 로드
@Composable
fun GoodNewsList(viewModel: NewsViewModel) {
    val newsPagingItems = viewModel.newsPagingFlow.collectAsLazyPagingItems()
    
    LazyColumn {
        items(newsPagingItems.itemCount) { index ->
            newsPagingItems[index]?.let { news ->
                NewsItem(news)
            }
        }
    }
}

/**
 * 장점:
 * - 메모리 효율적 (20개씩 로드)
 * - 빠른 초기 로딩
 * - 네트워크 최적화
 * - 배터리 절약
 */
```

### Paging 3 아키텍처

```
┌─────────────────────────────────────────────┐
│  UI Layer (Compose)                         │
│  - LazyColumn                               │
│  - collectAsLazyPagingItems()               │
├─────────────────────────────────────────────┤
│  ViewModel                                  │
│  - Pager                                    │
│  - Flow<PagingData>                         │
├─────────────────────────────────────────────┤
│  Repository                                 │
│  - PagingSource                             │
│  - RemoteMediator (선택사항)                │
├─────────────────────────────────────────────┤
│  Data Source                                │
│  - API (Retrofit)                           │
│  - Database (Room)                          │
└─────────────────────────────────────────────┘
```

### Paging 3의 주요 개념

```kotlin
/**
 * 1. PagingSource
 * - 데이터를 페이지 단위로 로드
 * - load() 메서드 구현
 */

/**
 * 2. PagingData
 * - 페이징된 데이터의 컨테이너
 * - Flow<PagingData<T>>로 전달
 */

/**
 * 3. Pager
 * - PagingSource를 Flow<PagingData>로 변환
 * - PagingConfig 설정
 */

/**
 * 4. LazyPagingItems
 * - Compose에서 PagingData 사용
 * - collectAsLazyPagingItems()
 */
```

---

## 기본 설정

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    val pagingVersion = "3.2.1"
    
    // Paging 3 Core
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    
    // Compose 통합
    implementation("androidx.paging:paging-compose:$pagingVersion")
    
    // 테스트 (선택사항)
    testImplementation("androidx.paging:paging-common:$pagingVersion")
}
```

---

## PagingSource 만들기

### 기본 PagingSource

```kotlin
import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * PagingSource: 데이터를 페이지 단위로 로드
 * 
 * @param Key: 페이지 키 타입 (Int, String 등)
 * @param Value: 데이터 아이템 타입
 */
class ArticlePagingSource(
    private val apiService: ApiService
) : PagingSource<Int, Article>() {
    
    /**
     * load(): 데이터 로드 메서드
     * 
     * @param params: 로드 파라미터 (key, loadSize)
     * @return LoadResult: 로드 결과 (Page 또는 Error)
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            // 1. 페이지 번호 가져오기
            val page = params.key ?: 1  // 첫 페이지는 1
            
            // 2. API 호출
            val response = apiService.getArticles(
                page = page,
                pageSize = params.loadSize
            )
            
            // 3. 성공 결과 반환
            LoadResult.Page(
                data = response.articles,  // 로드된 데이터
                prevKey = if (page == 1) null else page - 1,  // 이전 페이지 키
                nextKey = if (response.articles.isEmpty()) null else page + 1  // 다음 페이지 키
            )
            
        } catch (e: Exception) {
            // 4. 에러 결과 반환
            LoadResult.Error(e)
        }
    }
    
    /**
     * getRefreshKey(): 새로고침 시 시작 키 결정
     * 
     * @param state: 현재 Paging 상태
     * @return 새로고침 시작 키
     */
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // 현재 보고 있는 위치 근처에서 새로고침
        return state.anchorPosition?.let { anchorPosition ->
            // 가장 가까운 페이지의 키 반환
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

/**
 * API 응답 모델
 */
data class ArticleResponse(
    val articles: List<Article>,
    val totalCount: Int
)

data class Article(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: String
)
```

### LoadParams 이해하기

```kotlin
/**
 * LoadParams: 로드 파라미터
 */
sealed class LoadParams<Key : Any> {
    /**
     * key: 로드할 페이지의 키
     * - 첫 로드 시: null
     * - 이후 로드: prevKey 또는 nextKey
     */
    abstract val key: Key?
    
    /**
     * loadSize: 로드할 아이템 개수
     * - 첫 로드: initialLoadSize (기본: pageSize * 3)
     * - 이후 로드: pageSize
     */
    abstract val loadSize: Int
    
    /**
     * Refresh: 새로고침
     */
    class Refresh<Key : Any>(
        override val key: Key?,
        override val loadSize: Int,
        val placeholdersEnabled: Boolean
    ) : LoadParams<Key>()
    
    /**
     * Append: 다음 페이지 로드
     */
    class Append<Key : Any>(
        override val key: Key,
        override val loadSize: Int,
        val placeholdersEnabled: Boolean
    ) : LoadParams<Key>()
    
    /**
     * Prepend: 이전 페이지 로드
     */
    class Prepend<Key : Any>(
        override val key: Key,
        override val loadSize: Int,
        val placeholdersEnabled: Boolean
    ) : LoadParams<Key>()
}
```

### LoadResult 이해하기

```kotlin
/**
 * LoadResult: 로드 결과
 */
sealed class LoadResult<Key : Any, Value : Any> {
    
    /**
     * Page: 성공적으로 로드됨
     */
    data class Page<Key : Any, Value : Any>(
        val data: List<Value>,  // 로드된 데이터
        val prevKey: Key?,      // 이전 페이지 키 (null이면 첫 페이지)
        val nextKey: Key?       // 다음 페이지 키 (null이면 마지막 페이지)
    ) : LoadResult<Key, Value>()
    
    /**
     * Error: 로드 실패
     */
    data class Error<Key : Any, Value : Any>(
        val throwable: Throwable  // 에러 정보
    ) : LoadResult<Key, Value>()
    
    /**
     * Invalid: 무효화됨 (새로고침 필요)
     */
    class Invalid<Key : Any, Value : Any> : LoadResult<Key, Value>()
}
```

---

## Pager 생성

### Repository에서 Pager 생성

```kotlin
/**
 * Repository: Pager 생성
 */
class ArticleRepository(
    private val apiService: ApiService
) {
    
    /**
     * Pager를 Flow<PagingData>로 반환
     */
    fun getArticlesPager(): Flow<PagingData<Article>> {
        return Pager(
            // PagingConfig: 페이징 설정
            config = PagingConfig(
                pageSize = 20,              // 페이지 크기
                enablePlaceholders = false,  // Placeholder 비활성화
                initialLoadSize = 20         // 초기 로드 크기
            ),
            // PagingSource 팩토리
            pagingSourceFactory = {
                ArticlePagingSource(apiService)
            }
        ).flow
    }
}
```

### ViewModel에서 사용

```kotlin
/**
 * ViewModel: Pager를 UI에 노출
 */
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    /**
     * Flow<PagingData>를 cachedIn()으로 캐싱
     * 
     * cachedIn(): 
     * - ViewModel 스코프에서 캐싱
     * - 화면 회전 시 데이터 유지
     * - 중복 로드 방지
     */
    val articlesFlow: Flow<PagingData<Article>> = repository.getArticlesPager()
        .cachedIn(viewModelScope)
}
```

---

## 에러 처리

### PagingSource에서 에러 처리

```kotlin
/**
 * 에러 처리가 강화된 PagingSource
 */
class RobustArticlePagingSource(
    private val apiService: ApiService
) : PagingSource<Int, Article>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        
        return try {
            // API 호출
            val response = apiService.getArticles(page, params.loadSize)
            
            LoadResult.Page(
                data = response.articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.articles.isEmpty()) null else page + 1
            )
            
        } catch (e: IOException) {
            // 네트워크 에러
            Log.e("PagingSource", "Network error", e)
            LoadResult.Error(e)
            
        } catch (e: HttpException) {
            // HTTP 에러 (4xx, 5xx)
            Log.e("PagingSource", "HTTP error: ${e.code()}", e)
            LoadResult.Error(e)
            
        } catch (e: Exception) {
            // 기타 에러
            Log.e("PagingSource", "Unknown error", e)
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

### 재시도 로직

```kotlin
/**
 * 재시도 로직이 있는 PagingSource
 */
class RetryablePagingSource(
    private val apiService: ApiService,
    private val maxRetries: Int = 3
) : PagingSource<Int, Article>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        var lastException: Exception? = null
        
        // 재시도 로직
        repeat(maxRetries) { attempt ->
            try {
                val response = apiService.getArticles(page, params.loadSize)
                
                return LoadResult.Page(
                    data = response.articles,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.articles.isEmpty()) null else page + 1
                )
                
            } catch (e: Exception) {
                lastException = e
                
                // 마지막 시도가 아니면 대기 후 재시도
                if (attempt < maxRetries - 1) {
                    delay(1000L * (attempt + 1))  // 지수 백오프
                }
            }
        }
        
        // 모든 재시도 실패
        return LoadResult.Error(lastException ?: Exception("Unknown error"))
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

## 로딩 상태

### LoadState 이해하기

```kotlin
/**
 * LoadState: 로딩 상태
 */
sealed class LoadState {
    /**
     * NotLoading: 로딩 중이 아님
     */
    data class NotLoading(
        val endOfPaginationReached: Boolean  // 마지막 페이지 도달 여부
    ) : LoadState()
    
    /**
     * Loading: 로딩 중
     */
    object Loading : LoadState()
    
    /**
     * Error: 에러 발생
     */
    data class Error(
        val error: Throwable  // 에러 정보
    ) : LoadState()
}

/**
 * CombinedLoadStates: 모든 로딩 상태
 */
data class CombinedLoadStates(
    val refresh: LoadState,   // 새로고침 상태
    val prepend: LoadState,   // 이전 페이지 로드 상태
    val append: LoadState     // 다음 페이지 로드 상태
)
```

### LoadState 사용 예제

```kotlin
/**
 * ViewModel에서 LoadState 처리
 */
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    val articlesFlow = repository.getArticlesPager()
        .cachedIn(viewModelScope)
    
    /**
     * 로딩 상태를 별도 Flow로 노출
     */
    private val _loadState = MutableStateFlow<LoadState>(LoadState.NotLoading(false))
    val loadState: StateFlow<LoadState> = _loadState
    
    /**
     * 에러 메시지
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    /**
     * LoadState 업데이트
     */
    fun updateLoadState(loadStates: CombinedLoadStates) {
        // Refresh 상태 확인
        when (val refresh = loadStates.refresh) {
            is LoadState.Loading -> {
                _loadState.value = LoadState.Loading
                _errorMessage.value = null
            }
            is LoadState.Error -> {
                _loadState.value = refresh
                _errorMessage.value = refresh.error.message
            }
            is LoadState.NotLoading -> {
                _loadState.value = refresh
                _errorMessage.value = null
            }
        }
    }
}
```

---

## PagingConfig 최적화

### PagingConfig 파라미터

```kotlin
/**
 * PagingConfig: 페이징 설정
 */
data class PagingConfig(
    /**
     * pageSize: 페이지 크기
     * - 한 번에 로드할 아이템 개수
     * - 권장: 20-50
     */
    val pageSize: Int,
    
    /**
     * prefetchDistance: 프리페치 거리
     * - 끝에서 몇 개 남았을 때 다음 페이지 로드
     * - 기본값: pageSize
     */
    val prefetchDistance: Int = pageSize,
    
    /**
     * enablePlaceholders: Placeholder 활성화
     * - true: 로드 전 빈 공간 표시
     * - false: 로드된 아이템만 표시 (권장)
     */
    val enablePlaceholders: Boolean = false,
    
    /**
     * initialLoadSize: 초기 로드 크기
     * - 첫 로드 시 아이템 개수
     * - 기본값: pageSize * 3
     */
    val initialLoadSize: Int = pageSize * 3,
    
    /**
     * maxSize: 최대 아이템 개수
     * - 메모리에 유지할 최대 아이템 수
     * - 기본값: Int.MAX_VALUE (무제한)
     */
    val maxSize: Int = Int.MAX_VALUE,
    
    /**
     * jumpThreshold: 점프 임계값
     * - 스크롤 점프 시 무효화 임계값
     * - 기본값: Int.MIN_VALUE (비활성화)
     */
    val jumpThreshold: Int = Int.MIN_VALUE
)
```

### 최적화된 PagingConfig 예제

```kotlin
/**
 * 시나리오별 최적화된 PagingConfig
 */
class PagingConfigExamples {
    
    /**
     * 1. 뉴스 피드 (일반적인 경우)
     */
    fun newsFeedConfig() = PagingConfig(
        pageSize = 20,                    // 20개씩 로드
        prefetchDistance = 5,             // 5개 남았을 때 다음 페이지
        enablePlaceholders = false,       // Placeholder 비활성화
        initialLoadSize = 20,             // 초기에도 20개만
        maxSize = 200                     // 최대 200개 유지
    )
    
    /**
     * 2. 이미지 갤러리 (메모리 제한)
     */
    fun imageGalleryConfig() = PagingConfig(
        pageSize = 30,                    // 30개씩 로드
        prefetchDistance = 10,            // 10개 남았을 때 다음 페이지
        enablePlaceholders = false,
        initialLoadSize = 30,
        maxSize = 100                     // 메모리 절약을 위해 100개만
    )
    
    /**
     * 3. 채팅 메시지 (빠른 스크롤)
     */
    fun chatMessagesConfig() = PagingConfig(
        pageSize = 50,                    // 50개씩 로드
        prefetchDistance = 20,            // 빠른 프리페치
        enablePlaceholders = false,
        initialLoadSize = 50,
        maxSize = 500                     // 많은 메시지 유지
    )
    
    /**
     * 4. 검색 결과 (작은 페이지)
     */
    fun searchResultsConfig() = PagingConfig(
        pageSize = 10,                    // 10개씩 로드
        prefetchDistance = 3,             // 3개 남았을 때 다음 페이지
        enablePlaceholders = false,
        initialLoadSize = 10,
        maxSize = 100
    )
}
```

---

## 실전 예제

### 예제 1: REST API 페이징

```kotlin
/**
 * REST API 페이징 예제
 */

// 1. API 서비스 정의
interface NewsApiService {
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): ArticleResponse
}

// 2. PagingSource 구현
class NewsArticlePagingSource(
    private val apiService: NewsApiService,
    private val category: String?
) : PagingSource<Int, Article>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        
        return try {
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

// 3. Repository
class NewsRepository(private val apiService: NewsApiService) {
    
    fun getNewsPager(category: String?): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                NewsArticlePagingSource(apiService, category)
            }
        ).flow
    }
}

// 4. ViewModel
class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {
    
    private val _category = MutableStateFlow<String?>(null)
    
    val newsFlow: Flow<PagingData<Article>> = _category
        .flatMapLatest { category ->
            repository.getNewsPager(category)
        }
        .cachedIn(viewModelScope)
    
    fun setCategory(category: String?) {
        _category.value = category
    }
}
```

### 예제 2: 커서 기반 페이징

```kotlin
/**
 * 커서 기반 페이징 예제
 * 
 * 페이지 번호 대신 커서(마지막 아이템 ID) 사용
 */

// 1. API 응답
data class CursorResponse(
    val items: List<Post>,
    val nextCursor: String?  // 다음 커서 (null이면 마지막)
)

data class Post(
    val id: String,
    val title: String,
    val content: String
)

// 2. API 서비스
interface PostApiService {
    @GET("posts")
    suspend fun getPosts(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int
    ): CursorResponse
}

// 3. PagingSource (커서 기반)
class PostPagingSource(
    private val apiService: PostApiService
) : PagingSource<String, Post>() {
    
    override suspend fun load(params: LoadParams<String>): LoadResult<String, Post> {
        return try {
            val cursor = params.key  // 첫 로드 시 null
            
            val response = apiService.getPosts(
                cursor = cursor,
                limit = params.loadSize
            )
            
            LoadResult.Page(
                data = response.items,
                prevKey = null,  // 커서 기반은 이전 페이지 없음
                nextKey = response.nextCursor  // 다음 커서
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<String, Post>): String? {
        // 새로고침 시 처음부터
        return null
    }
}
```

### 예제 3: 검색 기능 통합

```kotlin
/**
 * 검색 기능이 있는 페이징
 */
class SearchArticlePagingSource(
    private val apiService: NewsApiService,
    private val query: String
) : PagingSource<Int, Article>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        if (query.isBlank()) {
            // 검색어가 없으면 빈 결과
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
        
        val page = params.key ?: 1
        
        return try {
            val response = apiService.searchArticles(
                query = query,
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

// ViewModel
class SearchViewModel(
    private val repository: NewsRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    val searchResults: Flow<PagingData<Article>> = _searchQuery
        .debounce(300)  // 300ms 디바운스
        .flatMapLatest { query ->
            repository.searchArticles(query)
        }
        .cachedIn(viewModelScope)
    
    fun search(query: String) {
        _searchQuery.value = query
    }
}
```

---

## 💡 베스트 프랙티스 요약

### PagingSource
- ✅ 에러 처리 철저히
- ✅ 재시도 로직 구현
- ✅ getRefreshKey() 올바르게 구현
- ✅ 로그 추가 (디버깅용)

### PagingConfig
- ✅ pageSize: 20-50 권장
- ✅ enablePlaceholders: false 권장
- ✅ maxSize: 메모리 고려
- ✅ prefetchDistance: 적절히 설정

### ViewModel
- ✅ cachedIn(viewModelScope) 사용
- ✅ Flow 조합 활용
- ✅ LoadState 처리

### 성능 최적화
- ✅ 적절한 페이지 크기
- ✅ 프리페치 거리 조정
- ✅ 최대 아이템 수 제한
- ✅ 디바운스 적용 (검색)

---

## 🎯 다음 단계

Paging 3 기초를 마스터했습니다! 다음으로:

1. **[38-2. Paging 3 고급](./38-2-paging-advanced.md)** - RemoteMediator, 캐싱 전략
2. **[38-3. Paging 3 Compose 통합](./38-3-paging-compose.md)** - LazyPagingItems, UI 통합

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Paging! 📄
