# Android 네트워킹과 API 연동 가이드 (Retrofit)

## 📚 목차

1. [Retrofit 소개](#retrofit-소개)
2. [프로젝트 설정](#프로젝트-설정)
3. [기본 API 호출](#기본-api-호출)
4. [데이터 모델과 JSON 파싱](#데이터-모델과-json-파싱)
5. [Coroutines와 Flow](#coroutines와-flow)
6. [에러 처리](#에러-처리)
7. [로딩 상태 관리](#로딩-상태-관리)
8. [실전 예제](#실전-예제)

---

## Retrofit 소개

**Retrofit**은 Android에서 가장 많이 사용되는 HTTP 클라이언트 라이브러리입니다.

### 주요 특징

- ✅ **타입 안전성**: 컴파일 타임에 API 인터페이스 검증
- ✅ **간결한 코드**: 어노테이션 기반 API 정의
- ✅ **Coroutines 지원**: 비동기 처리 간편화
- ✅ **자동 JSON 변환**: Gson, Moshi 등과 통합
- ✅ **인터셉터 지원**: 로깅, 인증 등 추가 가능

---

## 프로젝트 설정

### 의존성 추가

`build.gradle.kts` (Module: app)에 다음을 추가:

```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp (로깅용)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
}
```

### 인터넷 권한 추가

`AndroidManifest.xml`에 추가:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application>
        ...
    </application>
</manifest>
```

---

## 기본 API 호출

### 1. 데이터 모델 정의

```kotlin
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String
)
```

### 2. API 인터페이스 정의

```kotlin
import retrofit2.http.*

interface ApiService {
    // GET 요청
    @GET("posts")
    suspend fun getPosts(): List<Post>
    
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") postId: Int): Post
    
    // Query 파라미터
    @GET("posts")
    suspend fun getPostsByUser(@Query("userId") userId: Int): List<Post>
    
    // POST 요청
    @POST("posts")
    suspend fun createPost(@Body post: Post): Post
    
    // PUT 요청
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: Int,
        @Body post: Post
    ): Post
    
    // DELETE 요청
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int)
    
    // 여러 Query 파라미터
    @GET("posts")
    suspend fun searchPosts(
        @Query("userId") userId: Int?,
        @Query("title") title: String?
    ): List<Post>
}
```

### 3. Retrofit 인스턴스 생성

```kotlin
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    
    // 로깅 인터셉터
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // OkHttp 클라이언트
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Retrofit 인스턴스
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // API 서비스
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

### 4. 기본 사용 예제

```kotlin
@Composable
fun PostListScreen() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            posts = RetrofitClient.apiService.getPosts()
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("에러: $error")
            }
        }
        else -> {
            LazyColumn {
                items(posts) { post ->
                    PostItem(post = post)
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

---

## 데이터 모델과 JSON 파싱

### Gson 사용 (기본)

```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String,
    
    // JSON 키와 다른 이름 사용
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    // Nullable 필드
    val website: String? = null
)
```

### 중첩된 JSON 처리

```kotlin
// JSON:
// {
//   "id": 1,
//   "name": "John",
//   "address": {
//     "street": "123 Main St",
//     "city": "Seoul"
//   }
// }

data class Address(
    val street: String,
    val city: String,
    val zipcode: String
)

data class User(
    val id: Int,
    val name: String,
    val address: Address
)
```

### 리스트 응답 처리

```kotlin
// 직접 리스트로 받기
@GET("posts")
suspend fun getPosts(): List<Post>

// 래퍼 객체로 받기
data class PostsResponse(
    val data: List<Post>,
    val total: Int,
    val page: Int
)

@GET("posts")
suspend fun getPosts(): PostsResponse
```

### 제네릭 응답 래퍼

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

@GET("posts")
suspend fun getPosts(): ApiResponse<List<Post>>

@GET("posts/{id}")
suspend fun getPost(@Path("id") id: Int): ApiResponse<Post>
```

---

## Coroutines와 Flow

### Repository 패턴

```kotlin
class PostRepository(
    private val apiService: ApiService
) {
    // 단순 호출
    suspend fun getPosts(): Result<List<Post>> {
        return try {
            val posts = apiService.getPosts()
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Flow 사용
    fun getPostsFlow(): Flow<Result<List<Post>>> = flow {
        emit(Result.success(emptyList())) // 초기 상태
        try {
            val posts = apiService.getPosts()
            emit(Result.success(posts))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    // 특정 게시물 가져오기
    suspend fun getPost(id: Int): Result<Post> {
        return try {
            val post = apiService.getPost(id)
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 게시물 생성
    suspend fun createPost(post: Post): Result<Post> {
        return try {
            val createdPost = apiService.createPost(post)
            Result.success(createdPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### ViewModel 사용

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class PostViewModel(
    private val repository: PostRepository = PostRepository(RetrofitClient.apiService)
) : ViewModel() {
    
    private val _postsState = MutableStateFlow<UiState<List<Post>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<Post>>> = _postsState.asStateFlow()
    
    fun loadPosts() {
        viewModelScope.launch {
            _postsState.value = UiState.Loading
            
            repository.getPosts()
                .onSuccess { posts ->
                    _postsState.value = UiState.Success(posts)
                }
                .onFailure { exception ->
                    _postsState.value = UiState.Error(
                        exception.message ?: "알 수 없는 오류"
                    )
                }
        }
    }
    
    fun createPost(title: String, body: String) {
        viewModelScope.launch {
            val newPost = Post(
                userId = 1,
                id = 0,
                title = title,
                body = body
            )
            
            repository.createPost(newPost)
                .onSuccess {
                    loadPosts() // 목록 새로고침
                }
                .onFailure { exception ->
                    _postsState.value = UiState.Error(
                        exception.message ?: "게시물 생성 실패"
                    )
                }
        }
    }
}
```

### Compose에서 ViewModel 사용

```kotlin
@Composable
fun PostListScreen(
    viewModel: PostViewModel = viewModel()
) {
    val postsState by viewModel.postsState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }
    
    when (val state = postsState) {
        is UiState.Idle -> {
            // 초기 상태
        }
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            LazyColumn {
                items(state.data) { post ->
                    PostItem(post = post)
                }
            }
        }
        is UiState.Error -> {
            ErrorView(
                message = state.message,
                onRetry = { viewModel.loadPosts() }
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
```

---

## 에러 처리

### 에러 타입 정의

```kotlin
sealed class NetworkError {
    object NoInternet : NetworkError()
    object Timeout : NetworkError()
    data class ServerError(val code: Int, val message: String) : NetworkError()
    data class Unknown(val message: String) : NetworkError()
}
```

### 에러 처리 확장 함수

```kotlin
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: Exception) {
        when (e) {
            is IOException -> {
                Result.failure(Exception("네트워크 연결을 확인해주세요"))
            }
            is HttpException -> {
                val errorMessage = when (e.code()) {
                    400 -> "잘못된 요청입니다"
                    401 -> "인증이 필요합니다"
                    403 -> "접근 권한이 없습니다"
                    404 -> "요청한 리소스를 찾을 수 없습니다"
                    500 -> "서버 오류가 발생했습니다"
                    else -> "알 수 없는 오류: ${e.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
            else -> {
                Result.failure(Exception(e.message ?: "알 수 없는 오류"))
            }
        }
    }
}

// 사용 예제
class PostRepository(private val apiService: ApiService) {
    suspend fun getPosts(): Result<List<Post>> {
        return safeApiCall {
            apiService.getPosts()
        }
    }
}
```

### HTTP 상태 코드 처리

```kotlin
import retrofit2.Response

@GET("posts/{id}")
suspend fun getPost(@Path("id") id: Int): Response<Post>

// 사용
suspend fun getPost(id: Int): Result<Post> {
    return try {
        val response = apiService.getPost(id)
        if (response.isSuccessful) {
            response.body()?.let {
                Result.success(it)
            } ?: Result.failure(Exception("응답 본문이 없습니다"))
        } else {
            val errorMessage = when (response.code()) {
                404 -> "게시물을 찾을 수 없습니다"
                500 -> "서버 오류"
                else -> "오류: ${response.code()}"
            }
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 로딩 상태 관리

### 통합 UI 상태

```kotlin
data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

class PostViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()
    
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getPosts()
                .onSuccess { posts ->
                    _uiState.update { 
                        it.copy(
                            posts = posts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            repository.getPosts()
                .onSuccess { posts ->
                    _uiState.update { 
                        it.copy(
                            posts = posts,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
        }
    }
}
```

### Pull to Refresh

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListWithRefresh(
    viewModel: PostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }
    
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        LazyColumn {
            items(uiState.posts) { post ->
                PostItem(post = post)
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

## 실전 예제

### 완전한 뉴스 앱 예제

```kotlin
// 1. 데이터 모델
data class Article(
    val id: Int,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val author: String?
)

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

// 2. API 인터페이스
interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "kr",
        @Query("apiKey") apiKey: String
    ): NewsResponse
    
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}

// 3. Repository
class NewsRepository(
    private val apiService: NewsApiService,
    private val apiKey: String
) {
    suspend fun getTopHeadlines(): Result<List<Article>> {
        return try {
            val response = apiService.getTopHeadlines(apiKey = apiKey)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchNews(query: String): Result<List<Article>> {
        return try {
            val response = apiService.searchNews(query, apiKey)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// 4. ViewModel
class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {
    
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadTopHeadlines()
    }
    
    fun loadTopHeadlines() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getTopHeadlines()
                .onSuccess { articles ->
                    _articles.value = articles
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun searchNews(query: String) {
        if (query.isBlank()) {
            loadTopHeadlines()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.searchNews(query)
                .onSuccess { articles ->
                    _articles.value = articles
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
}

// 5. UI
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = viewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 검색 바
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { viewModel.searchNews(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // 콘텐츠
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                ErrorView(
                    message = error ?: "오류 발생",
                    onRetry = { viewModel.loadTopHeadlines() }
                )
            }
            articles.isEmpty() -> {
                EmptyView()
            }
            else -> {
                LazyColumn {
                    items(articles) { article ->
                        ArticleItem(article = article)
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleItem(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* 상세 화면으로 이동 */ }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 이미지 (Coil 사용 시)
            article.urlToImage?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 제목
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 설명
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 메타 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                article.author?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = article.publishedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("뉴스 검색...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "지우기"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(query) }
        ),
        singleLine = true
    )
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Article,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "뉴스가 없습니다",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. Repository 패턴 사용

```kotlin
// ✅ 좋은 예: Repository로 분리
class PostRepository(private val apiService: ApiService) {
    suspend fun getPosts() = apiService.getPosts()
}

class PostViewModel(private val repository: PostRepository) : ViewModel()

// ❌ 나쁜 예: ViewModel에서 직접 API 호출
class PostViewModel : ViewModel() {
    fun loadPosts() {
        viewModelScope.launch {
            val posts = RetrofitClient.apiService.getPosts()
        }
    }
}
```

### 2. 에러 처리는 필수

```kotlin
// ✅ 좋은 예
try {
    val posts = apiService.getPosts()
    _state.value = UiState.Success(posts)
} catch (e: Exception) {
    _state.value = UiState.Error(e.message ?: "오류 발생")
}

// ❌ 나쁜 예
val posts = apiService.getPosts() // 에러 처리 없음
```

### 3. 로딩 상태 표시

```kotlin
// ✅ 사용자에게 로딩 중임을 알림
_isLoading.value = true
val data = apiService.getData()
_isLoading.value = false
```

### 4. 타임아웃 설정

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

### 5. 로깅은 개발 환경에서만

```kotlin
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
```

---

## 🎯 다음 단계

네트워킹을 마스터했습니다! 다음으로:

1. **애니메이션 가이드** - 부드러운 UX 구현
2. **Side Effects 가이드** - LaunchedEffect, DisposableEffect 등
3. **이미지 로딩 가이드** - Coil로 네트워크 이미지 로딩

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
