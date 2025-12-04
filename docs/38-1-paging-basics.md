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

---

## Paging 3란?

### 왜 Paging이 필요한가?

**문제:**
```kotlin
// ❌ 모든 데이터를 한 번에 로드
val allItems = api.getAllItems()  // 10,000개!
// 메모리 부족, 느린 로딩
```

**해결:**
```kotlin
// ✅ 필요한 만큼만 로드
val pagedItems = Pager(config).flow  // 20개씩
// 빠른 로딩, 메모리 효율적
```

---

## 기본 설정

### 의존성 추가

```kotlin
dependencies {
    val pagingVersion = "3.2.1"
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingVersion")
}
```

---

## PagingSource 만들기

```kotlin
import androidx.paging.PagingSource
import androidx.paging.PagingState

data class Article(
    val id: Int,
    val title: String,
    val content: String
)

class ArticlePagingSource(
    private val api: ApiService
) : PagingSource<Int, Article>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            
            val response = api.getArticles(
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

## Pager 생성

```kotlin
class ArticleRepository(private val api: ApiService) {
    fun getArticlesPager(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ArticlePagingSource(api)
            }
        ).flow
    }
}

class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    val articlesFlow: Flow<PagingData<Article>> = repository.getArticlesPager()
        .cachedIn(viewModelScope)
}
```

---

**마지막 업데이트**: 2024-12-03
