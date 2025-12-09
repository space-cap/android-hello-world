# Paging 3 고급

> 📖 **시리즈 구성**
> - **38-1**: [Paging 3 기초](./38-1-paging-basics.md)
> - **38-2**: Paging 3 고급 (현재 문서)
> - **38-3**: [Paging 3 Compose 통합](./38-3-paging-compose.md)

---

## 📚 목차

1. [RemoteMediator](#remotemediator)
2. [Room 통합](#room-통합)
3. [데이터 변환](#데이터-변환)
4. [캐싱 전략](#캐싱-전략)
5. [실전 예제](#실전-예제)

---

## RemoteMediator

### RemoteMediator란?

**RemoteMediator는 네트워크와 로컬 DB를 연결하는 중간 계층입니다.**

```
┌─────────────────────────────────────┐
│  UI Layer                           │
├─────────────────────────────────────┤
│  Pager                              │
├─────────────────────────────────────┤
│  RemoteMediator                     │
│  - 네트워크 데이터 가져오기          │
│  - DB에 저장                         │
├─────────────────────────────────────┤
│  Room Database                      │
│  - PagingSource 제공                │
└─────────────────────────────────────┘
```

### 기본 RemoteMediator 구현

```kotlin
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator

/**
 * RemoteMediator: 네트워크 + DB 통합
 */
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val apiService: ApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, ArticleEntity>() {
    
    private val articleDao = database.articleDao()
    private val remoteKeyDao = database.remoteKeyDao()
    
    /**
     * load(): 데이터 로드 및 DB 저장
     * 
     * @param loadType: 로드 타입 (REFRESH, APPEND, PREPEND)
     * @param state: Paging 상태
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        
        return try {
            // 1. 로드할 페이지 결정
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    // 새로고침: 첫 페이지
                    val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKey?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    // 이전 페이지: 지원 안 함
                    val remoteKey = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKey?.prevKey
                    prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    // 다음 페이지
                    val remoteKey = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKey?.nextKey
                    nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            
            // 2. API 호출
            val response = apiService.getArticles(
                page = page,
                pageSize = state.config.pageSize
            )
            
            val articles = response.articles
            val endOfPaginationReached = articles.isEmpty()
            
            // 3. DB 트랜잭션으로 저장
            database.withTransaction {
                // REFRESH 시 기존 데이터 삭제
                if (loadType == LoadType.REFRESH) {
                    articleDao.clearAll()
                    remoteKeyDao.clearAll()
                }
                
                // RemoteKey 저장
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                
                val keys = articles.map { article ->
                    RemoteKey(
                        articleId = article.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                
                remoteKeyDao.insertAll(keys)
                
                // Article 저장
                articleDao.insertAll(articles.map { it.toEntity() })
            }
            
            // 4. 성공 결과 반환
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            
        } catch (e: Exception) {
            // 5. 에러 결과 반환
            MediatorResult.Error(e)
        }
    }
    
    /**
     * 현재 위치 근처의 RemoteKey 가져오기
     */
    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ArticleEntity>
    ): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { articleId ->
                remoteKeyDao.getRemoteKey(articleId)
            }
        }
    }
    
    /**
     * 첫 번째 아이템의 RemoteKey 가져오기
     */
    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, ArticleEntity>
    ): RemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { article ->
            remoteKeyDao.getRemoteKey(article.id)
        }
    }
    
    /**
     * 마지막 아이템의 RemoteKey 가져오기
     */
    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, ArticleEntity>
    ): RemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { article ->
            remoteKeyDao.getRemoteKey(article.id)
        }
    }
}

/**
 * RemoteKey: 페이지 키 저장
 */
@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey
    val articleId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<RemoteKey>)
    
    @Query("SELECT * FROM remote_keys WHERE articleId = :articleId")
    suspend fun getRemoteKey(articleId: Int): RemoteKey?
    
    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}
```

---

## Room 통합

### Entity 정의

```kotlin
/**
 * Article Entity
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val imageUrl: String?
)

/**
 * DAO
 */
@Dao
interface ArticleDao {
    /**
     * PagingSource 반환
     */
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun pagingSource(): PagingSource<Int, ArticleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)
    
    @Query("DELETE FROM articles")
    suspend fun clearAll()
    
    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: Int): ArticleEntity?
}

/**
 * Database
 */
@Database(
    entities = [ArticleEntity::class, RemoteKey::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
```

### Repository with RemoteMediator

```kotlin
/**
 * Repository: RemoteMediator 사용
 */
class ArticleRepository(
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    
    @OptIn(ExperimentalPagingApi::class)
    fun getArticlesPager(): Flow<PagingData<ArticleEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            // RemoteMediator 설정
            remoteMediator = ArticleRemoteMediator(
                apiService = apiService,
                database = database
            ),
            // Room PagingSource
            pagingSourceFactory = {
                database.articleDao().pagingSource()
            }
        ).flow
    }
}
```

---

## 데이터 변환

### map() 사용

```kotlin
/**
 * PagingData 변환
 */
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    /**
     * Entity를 UI 모델로 변환
     */
    val articlesFlow: Flow<PagingData<ArticleUiModel>> = repository.getArticlesPager()
        .map { pagingData ->
            pagingData.map { entity ->
                entity.toUiModel()
            }
        }
        .cachedIn(viewModelScope)
}

/**
 * UI 모델
 */
data class ArticleUiModel(
    val id: Int,
    val title: String,
    val summary: String,
    val author: String,
    val publishedDate: String,
    val imageUrl: String?
)

/**
 * Entity -> UI 모델 변환
 */
fun ArticleEntity.toUiModel(): ArticleUiModel {
    return ArticleUiModel(
        id = id,
        title = title,
        summary = content.take(100) + "...",
        author = author,
        publishedDate = formatDate(publishedAt),
        imageUrl = imageUrl
    )
}

fun formatDate(dateString: String): String {
    // 날짜 포맷팅 로직
    return dateString
}
```

### insertSeparators() 사용

```kotlin
/**
 * 구분자 삽입
 */
val articlesWithSeparators: Flow<PagingData<UiModel>> = repository.getArticlesPager()
    .map { pagingData ->
        pagingData
            .map { UiModel.ArticleItem(it.toUiModel()) }
            .insertSeparators { before, after ->
                // 날짜가 바뀔 때 구분자 삽입
                if (after == null) {
                    return@insertSeparators null
                }
                
                if (before == null) {
                    // 첫 아이템 전에 날짜 헤더
                    return@insertSeparators UiModel.DateSeparator(after.article.publishedDate)
                }
                
                if (before.article.publishedDate != after.article.publishedDate) {
                    // 날짜가 바뀔 때 구분자
                    UiModel.DateSeparator(after.article.publishedDate)
                } else {
                    null
                }
            }
    }
    .cachedIn(viewModelScope)

/**
 * UI 모델 (아이템 + 구분자)
 */
sealed class UiModel {
    data class ArticleItem(val article: ArticleUiModel) : UiModel()
    data class DateSeparator(val date: String) : UiModel()
}
```

---

## 캐싱 전략

### 오프라인 우선 전략

```kotlin
/**
 * 오프라인 우선 RemoteMediator
 */
@OptIn(ExperimentalPagingApi::class)
class OfflineFirstRemoteMediator(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val connectivityManager: ConnectivityManager
) : RemoteMediator<Int, ArticleEntity>() {
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        
        // 네트워크 연결 확인
        if (!isNetworkAvailable()) {
            // 오프라인: DB 데이터만 사용
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        
        // 온라인: 네트워크에서 데이터 가져오기
        return try {
            val page = getPageToLoad(loadType, state)
            val response = apiService.getArticles(page, state.config.pageSize)
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.articleDao().clearAll()
                    database.remoteKeyDao().clearAll()
                }
                
                saveToDatabase(response, page)
            }
            
            MediatorResult.Success(endOfPaginationReached = response.articles.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    private fun getPageToLoad(loadType: LoadType, state: PagingState<Int, ArticleEntity>): Int {
        // 페이지 로드 로직
        return 1
    }
    
    private suspend fun saveToDatabase(response: ArticleResponse, page: Int) {
        // DB 저장 로직
    }
}
```

### 캐시 만료 전략

```kotlin
/**
 * 캐시 만료가 있는 RemoteMediator
 */
@OptIn(ExperimentalPagingApi::class)
class CacheExpiringRemoteMediator(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val cacheTimeout: Long = 5 * 60 * 1000  // 5분
) : RemoteMediator<Int, ArticleEntity>() {
    
    override suspend fun initialize(): InitializeAction {
        // 캐시 만료 확인
        val cacheCreatedTime = database.articleDao().getOldestArticleTime()
        val currentTime = System.currentTimeMillis()
        
        return if (cacheCreatedTime != null && currentTime - cacheCreatedTime < cacheTimeout) {
            // 캐시 유효: 네트워크 요청 건너뛰기
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // 캐시 만료: 새로고침 필요
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        // 로드 로직
        return MediatorResult.Success(endOfPaginationReached = true)
    }
}

@Dao
interface ArticleDao {
    @Query("SELECT MIN(createdAt) FROM articles")
    suspend fun getOldestArticleTime(): Long?
    
    // 다른 메서드들...
}
```

---

## 실전 예제

### 예제: 뉴스 앱 (완전한 구현)

```kotlin
/**
 * 1. Entity
 */
@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val content: String,
    val author: String,
    val publishedAt: Long,
    val imageUrl: String?,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 2. DAO
 */
@Dao
interface NewsArticleDao {
    @Query("SELECT * FROM news_articles WHERE category = :category ORDER BY publishedAt DESC")
    fun pagingSource(category: String): PagingSource<Int, NewsArticleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticleEntity>)
    
    @Query("DELETE FROM news_articles WHERE category = :category")
    suspend fun clearByCategory(category: String)
    
    @Query("SELECT MIN(createdAt) FROM news_articles WHERE category = :category")
    suspend fun getOldestArticleTime(category: String): Long?
}

/**
 * 3. RemoteMediator
 */
@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val category: String,
    private val apiService: NewsApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, NewsArticleEntity>() {
    
    private val articleDao = database.newsArticleDao()
    private val remoteKeyDao = database.remoteKeyDao()
    
    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.HOURS.toMillis(1)  // 1시간
        val cacheCreatedTime = articleDao.getOldestArticleTime(category)
        val currentTime = System.currentTimeMillis()
        
        return if (cacheCreatedTime != null && currentTime - cacheCreatedTime < cacheTimeout) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticleEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)
                    remoteKey?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            
            val response = apiService.getNewsByCategory(
                category = category,
                page = page,
                pageSize = state.config.pageSize
            )
            
            val endOfPaginationReached = response.articles.isEmpty()
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.clearByCategory(category)
                    remoteKeyDao.clearByCategory(category)
                }
                
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                
                val keys = response.articles.map {
                    RemoteKey(
                        articleId = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                
                remoteKeyDao.insertAll(keys)
                articleDao.insertAll(response.articles.map { it.toEntity(category) })
            }
            
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
    
    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, NewsArticleEntity>
    ): RemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { article ->
            remoteKeyDao.getRemoteKey(article.id)
        }
    }
}

/**
 * 4. Repository
 */
class NewsRepository(
    private val apiService: NewsApiService,
    private val database: AppDatabase
) {
    
    @OptIn(ExperimentalPagingApi::class)
    fun getNewsByCategory(category: String): Flow<PagingData<NewsArticleEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = NewsRemoteMediator(
                category = category,
                apiService = apiService,
                database = database
            ),
            pagingSourceFactory = {
                database.newsArticleDao().pagingSource(category)
            }
        ).flow
    }
}

/**
 * 5. ViewModel
 */
class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {
    
    private val _category = MutableStateFlow("technology")
    
    val newsFlow: Flow<PagingData<NewsUiModel>> = _category
        .flatMapLatest { category ->
            repository.getNewsByCategory(category)
        }
        .map { pagingData ->
            pagingData.map { it.toUiModel() }
        }
        .cachedIn(viewModelScope)
    
    fun setCategory(category: String) {
        _category.value = category
    }
}

/**
 * 6. UI 모델
 */
data class NewsUiModel(
    val id: Int,
    val title: String,
    val summary: String,
    val author: String,
    val publishedDate: String,
    val imageUrl: String?,
    val category: String
)

fun NewsArticleEntity.toUiModel(): NewsUiModel {
    return NewsUiModel(
        id = id,
        title = title,
        summary = description,
        author = author,
        publishedDate = formatTimestamp(publishedAt),
        imageUrl = imageUrl,
        category = category
    )
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
```

---

## 💡 베스트 프랙티스 요약

### RemoteMediator
- ✅ initialize()로 캐시 만료 확인
- ✅ 트랜잭션으로 DB 저장
- ✅ RemoteKey로 페이지 추적
- ✅ 에러 처리 철저히

### Room 통합
- ✅ Entity 설계 신중히
- ✅ PagingSource 반환
- ✅ 인덱스 추가 (성능)
- ✅ 마이그레이션 계획

### 데이터 변환
- ✅ map()으로 UI 모델 변환
- ✅ insertSeparators()로 구분자 추가
- ✅ 불필요한 변환 최소화

### 캐싱 전략
- ✅ 오프라인 우선
- ✅ 캐시 만료 설정
- ✅ 네트워크 상태 확인
- ✅ 적절한 타임아웃

---

## 🎯 다음 단계

Paging 3 고급을 마스터했습니다! 다음으로:

1. **[38-3. Paging 3 Compose 통합](./38-3-paging-compose.md)** - LazyPagingItems, UI 통합

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Paging! 📄
