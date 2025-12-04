# Paging 3 고급

> 📖 **시리즈 구성**
> - **38-1**: [Paging 3 기초](./38-1-paging-basics.md)
> - **38-2**: Paging 3 고급 (현재 문서)
> - **38-3**: [Paging 3 Compose 통합](./38-3-paging-compose.md)

---

## 📚 목차

1. [RemoteMediator](#remotemediator)
2. [검색 및 필터링](#검색-및-필터링)
3. [데이터 변환](#데이터-변환)

---

## RemoteMediator

### Room Database 설정

```kotlin
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String
)

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY id ASC")
    fun getAllArticles(): PagingSource<Int, ArticleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)
    
    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
```

### RemoteMediator 구현

```kotlin
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val api: ApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, ArticleEntity>() {
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    lastItem.id / state.config.pageSize + 1
                }
            }
            
            val response = api.getArticles(page, state.config.pageSize)
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.articleDao().clearAll()
                }
                database.articleDao().insertAll(response.articles)
            }
            
            MediatorResult.Success(endOfPaginationReached = response.articles.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
```

---

## 검색 및 필터링

```kotlin
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    
    val articlesFlow: Flow<PagingData<Article>> = _searchQuery
        .debounce(300)
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

## 데이터 변환

```kotlin
val transformedArticles: Flow<PagingData<ArticleUiModel>> = articlesFlow
    .map { pagingData ->
        pagingData.map { article ->
            ArticleUiModel(
                id = article.id,
                title = article.title.uppercase()
            )
        }
    }
```

---

**마지막 업데이트**: 2024-12-03
