# 완전한 앱 예제: 뉴스 리더 앱

## 📚 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [아키텍처 설계](#아키텍처-설계)
3. [데이터 레이어](#데이터-레이어)
4. [도메인 레이어](#도메인-레이어)
5. [프레젠테이션 레이어](#프레젠테이션-레이어)
6. [Navigation](#navigation)
7. [의존성 주입](#의존성-주입)
8. [테스팅](#테스팅)

---

## 프로젝트 개요

### 기능 목록

- ✅ 뉴스 기사 목록 표시
- ✅ 기사 상세 보기
- ✅ 검색 기능
- ✅ 카테고리별 필터링
- ✅ 북마크 기능 (로컬 저장)
- ✅ 다크 모드 지원
- ✅ Pull to Refresh

### 사용 기술

- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Network**: Retrofit + OkHttp
- **Database**: Room
- **Image**: Coil
- **Async**: Coroutines + Flow

---

## 아키텍처 설계

### 프로젝트 구조

```
com.example.newsreader/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── ArticleDao.kt
│   │   ├── entity/
│   │   │   └── ArticleEntity.kt
│   │   └── database/
│   │       └── NewsDatabase.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── NewsApiService.kt
│   │   └── dto/
│   │       └── ArticleDto.kt
│   ├── repository/
│   │   └── NewsRepositoryImpl.kt
│   └── datasource/
│       ├── NewsRemoteDataSource.kt
│       └── NewsLocalDataSource.kt
│
├── domain/
│   ├── model/
│   │   ├── Article.kt
│   │   └── Category.kt
│   ├── repository/
│   │   └── NewsRepository.kt
│   └── usecase/
│       ├── GetNewsUseCase.kt
│       ├── SearchNewsUseCase.kt
│       ├── GetArticleByIdUseCase.kt
│       ├── BookmarkArticleUseCase.kt
│       └── GetBookmarkedArticlesUseCase.kt
│
├── presentation/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── components/
│   │       └── ArticleCard.kt
│   ├── detail/
│   │   ├── DetailScreen.kt
│   │   └── DetailViewModel.kt
│   ├── search/
│   │   ├── SearchScreen.kt
│   │   └── SearchViewModel.kt
│   ├── bookmarks/
│   │   ├── BookmarksScreen.kt
│   │   └── BookmarksViewModel.kt
│   └── common/
│       ├── components/
│       └── theme/
│
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
└── navigation/
    └── NavGraph.kt
```

---

## 데이터 레이어

### Domain Model

```kotlin
data class Article(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val author: String?,
    val imageUrl: String?,
    val publishedAt: Long,
    val source: String,
    val url: String,
    val category: Category,
    val isBookmarked: Boolean = false
)

enum class Category {
    GENERAL, BUSINESS, TECHNOLOGY, SPORTS, ENTERTAINMENT, HEALTH, SCIENCE
}
```

### Remote DTO

```kotlin
@Serializable
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDto>
)

@Serializable
data class ArticleDto(
    val title: String,
    val description: String?,
    val content: String?,
    val author: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val source: SourceDto,
    val url: String
)

@Serializable
data class SourceDto(
    val id: String?,
    val name: String
)

// Mapper
fun ArticleDto.toDomain(category: Category): Article {
    return Article(
        id = url.hashCode().toString(),
        title = title,
        description = description ?: "",
        content = content ?: "",
        author = author,
        imageUrl = urlToImage,
        publishedAt = parseDate(publishedAt),
        source = source.name,
        url = url,
        category = category
    )
}
```

### Local Entity

```kotlin
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val author: String?,
    val imageUrl: String?,
    val publishedAt: Long,
    val source: String,
    val url: String,
    val category: String,
    val isBookmarked: Boolean = false
)

// Mapper
fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        description = description,
        content = content,
        author = author,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        source = source,
        url = url,
        category = Category.valueOf(category),
        isBookmarked = isBookmarked
    )
}

fun Article.toEntity(): ArticleEntity {
    return ArticleEntity(
        id = id,
        title = title,
        description = description,
        content = content,
        author = author,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        source = source,
        url = url,
        category = category.name,
        isBookmarked = isBookmarked
    )
}
```

### API Service

```kotlin
interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String,
        @Query("country") country: String = "kr",
        @Query("apiKey") apiKey: String
    ): NewsResponse
    
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
```

### DAO

```kotlin
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>
    
    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): ArticleEntity?
    
    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)
    
    @Update
    suspend fun updateArticle(article: ArticleEntity)
    
    @Query("DELETE FROM articles WHERE category = :category AND isBookmarked = 0")
    suspend fun deleteNonBookmarkedByCategory(category: String)
}
```

### Repository Implementation

```kotlin
class NewsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val localDataSource: NewsLocalDataSource
) : NewsRepository {
    
    override fun getNews(category: Category): Flow<Result<List<Article>>> = flow {
        // 먼저 로컬 데이터 emit
        localDataSource.getArticlesByCategory(category).collect { localArticles ->
            if (localArticles.isNotEmpty()) {
                emit(Result.success(localArticles))
            }
            
            // 네트워크에서 최신 데이터 가져오기
            try {
                val remoteArticles = remoteDataSource.getNews(category)
                localDataSource.saveArticles(remoteArticles, category)
                emit(Result.success(remoteArticles))
            } catch (e: Exception) {
                if (localArticles.isEmpty()) {
                    emit(Result.failure(e))
                }
            }
        }
    }
    
    override suspend fun searchNews(query: String): Result<List<Article>> {
        return try {
            val articles = remoteDataSource.searchNews(query)
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getArticleById(id: String): Result<Article> {
        return try {
            val article = localDataSource.getArticleById(id)
            if (article != null) {
                Result.success(article)
            } else {
                Result.failure(Exception("Article not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun bookmarkArticle(article: Article) {
        localDataSource.updateArticle(article.copy(isBookmarked = !article.isBookmarked))
    }
    
    override fun getBookmarkedArticles(): Flow<List<Article>> {
        return localDataSource.getBookmarkedArticles()
    }
}
```

---

## 도메인 레이어

### Use Cases

```kotlin
class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(category: Category): Flow<Result<List<Article>>> {
        return repository.getNews(category)
    }
}

class SearchNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(query: String): Result<List<Article>> {
        return repository.searchNews(query)
    }
}

class GetArticleByIdUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(id: String): Result<Article> {
        return repository.getArticleById(id)
    }
}

class BookmarkArticleUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(article: Article) {
        repository.bookmarkArticle(article)
    }
}

class GetBookmarkedArticlesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(): Flow<List<Article>> {
        return repository.getBookmarkedArticles()
    }
}
```

---

## 프레젠테이션 레이어

### Home Screen

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val bookmarkArticleUseCase: BookmarkArticleUseCase
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow(Category.GENERAL)
    val selectedCategory: StateFlow<Category> = _selectedCategory.asStateFlow()
    
    val articles: StateFlow<UiState<List<Article>>> = selectedCategory
        .flatMapLatest { category ->
            getNewsUseCase(category).map { result ->
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unknown error") }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
    
    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }
    
    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            bookmarkArticleUseCase(article)
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onArticleClick: (String) -> Unit
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val articlesState by viewModel.articles.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("News Reader") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 카테고리 탭
            CategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )
            
            // 기사 목록
            when (val state = articlesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    ArticleList(
                        articles = state.data,
                        onArticleClick = onArticleClick,
                        onBookmarkClick = { viewModel.toggleBookmark(it) }
                    )
                }
                is UiState.Error -> {
                    ErrorView(message = state.message)
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = Category.values().indexOf(selectedCategory)
    ) {
        Category.values().forEach { category ->
            Tab(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                text = { Text(category.name) }
            )
        }
    }
}

@Composable
fun ArticleList(
    articles: List<Article>,
    onArticleClick: (String) -> Unit,
    onBookmarkClick: (Article) -> Unit
) {
    LazyColumn {
        items(articles, key = { it.id }) { article ->
            ArticleCard(
                article = article,
                onClick = { onArticleClick(article.id) },
                onBookmarkClick = { onBookmarkClick(article) }
            )
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            // 이미지
            article.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            // 내용
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (article.isBookmarked) {
                                Icons.Filled.Bookmark
                            } else {
                                Icons.Filled.BookmarkBorder
                            },
                            contentDescription = "북마크"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = formatDate(article.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### Detail Screen

```kotlin
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    private val bookmarkArticleUseCase: BookmarkArticleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val articleId: String = savedStateHandle["articleId"] ?: ""
    
    private val _article = MutableStateFlow<UiState<Article>>(UiState.Loading)
    val article: StateFlow<UiState<Article>> = _article.asStateFlow()
    
    init {
        loadArticle()
    }
    
    private fun loadArticle() {
        viewModelScope.launch {
            _article.value = UiState.Loading
            
            getArticleByIdUseCase(articleId)
                .onSuccess { article ->
                    _article.value = UiState.Success(article)
                }
                .onFailure { error ->
                    _article.value = UiState.Error(error.message ?: "Unknown error")
                }
        }
    }
    
    fun toggleBookmark() {
        viewModelScope.launch {
            val currentArticle = (_article.value as? UiState.Success)?.data ?: return@launch
            bookmarkArticleUseCase(currentArticle)
            _article.value = UiState.Success(
                currentArticle.copy(isBookmarked = !currentArticle.isBookmarked)
            )
        }
    }
}

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val articleState by viewModel.article.collectAsState()
    
    when (val state = articleState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            ArticleDetailContent(
                article = state.data,
                onBackClick = onBackClick,
                onBookmarkClick = { viewModel.toggleBookmark() }
            )
        }
        is UiState.Error -> {
            ErrorView(message = state.message)
        }
    }
}

@Composable
fun ArticleDetailContent(
    article: Article,
    onBackClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기사 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (article.isBookmarked) {
                                Icons.Filled.Bookmark
                            } else {
                                Icons.Filled.BookmarkBorder
                            },
                            contentDescription = "북마크"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 이미지
            article.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            // 내용
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = formatDate(article.publishedAt),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                article.author?.let { author ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "By $author",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```

---

## Navigation

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{articleId}") {
        fun createRoute(articleId: String) = "detail/$articleId"
    }
    object Search : Screen("search")
    object Bookmarks : Screen("bookmarks")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.Detail.createRoute(articleId))
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) {
            DetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen()
        }
        
        composable(Screen.Bookmarks.route) {
            BookmarksScreen()
        }
    }
}
```

---

## 의존성 주입

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewsDatabase {
        return Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            "news_database"
        ).build()
    }
    
    @Provides
    fun provideArticleDao(database: NewsDatabase): ArticleDao {
        return database.articleDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository
}
```

---

## 테스팅

```kotlin
class NewsViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val getNewsUseCase = mockk<GetNewsUseCase>()
    private val bookmarkArticleUseCase = mockk<BookmarkArticleUseCase>()
    private lateinit var viewModel: HomeViewModel
    
    @Before
    fun setup() {
        viewModel = HomeViewModel(getNewsUseCase, bookmarkArticleUseCase)
    }
    
    @Test
    fun `loadNews updates articles state`() = runTest {
        val testArticles = listOf(
            Article(
                id = "1",
                title = "Test Article",
                description = "Description",
                content = "Content",
                author = "Author",
                imageUrl = null,
                publishedAt = System.currentTimeMillis(),
                source = "Source",
                url = "url",
                category = Category.GENERAL
            )
        )
        
        every { getNewsUseCase(any()) } returns flowOf(Result.success(testArticles))
        
        viewModel.selectCategory(Category.GENERAL)
        
        advanceUntilIdle()
        
        val state = viewModel.articles.value
        assertTrue(state is UiState.Success)
        assertEquals(testArticles, (state as UiState.Success).data)
    }
}
```

---

## 💡 학습 포인트

이 예제에서 배운 것:

1. ✅ **Clean Architecture** 적용
2. ✅ **MVVM 패턴** 구현
3. ✅ **Hilt**로 의존성 주입
4. ✅ **Retrofit**으로 API 연동
5. ✅ **Room**으로 로컬 캐싱
6. ✅ **Flow**로 반응형 데이터
7. ✅ **Compose Navigation**
8. ✅ **Coil**로 이미지 로딩
9. ✅ **테스팅** 작성

---

## 🎯 다음 단계

축하합니다! 모든 학습 문서를 완료했습니다! 🎉

이제 다음을 할 수 있습니다:

- ✅ 완전한 Android 앱 개발
- ✅ Clean Architecture 적용
- ✅ 테스트 작성
- ✅ 앱 배포

**자신만의 앱을 만들어보세요!** 🚀

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Building! 🎊
