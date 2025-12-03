# Android 디버깅 실전 시나리오

> 📖 **시리즈 구성**
> - **16-1**: [Android 디버깅 기초](./16-1-android-debugging-basics.md) - Logcat, Breakpoint, Layout Inspector
> - **16-2**: [Android 디버깅 고급](./16-2-android-debugging-advanced.md) - Database/Network/Background Inspector, ANR, StrictMode
> - **16-3**: Android 디버깅 실전 (현재 문서) - 실전 문제 해결 시나리오

---

## 📚 목차

1. [앱 크래시 디버깅](#앱-크래시-디버깅)
2. [UI 버그 해결](#ui-버그-해결)
3. [성능 문제 해결](#성능-문제-해결)
4. [네트워크 문제 해결](#네트워크-문제-해결)
5. [메모리 누수 해결](#메모리-누수-해결)
6. [Compose 특화 디버깅](#compose-특화-디버깅)

---

## 앱 크래시 디버깅

### 시나리오 1: 앱이 시작하자마자 크래시

**증상**: 앱을 실행하면 즉시 종료됨

#### 1단계: Logcat에서 스택 트레이스 확인

```
// Logcat 필터: level:ERROR
FATAL EXCEPTION: main
Process: com.example.myapp, PID: 12345
java.lang.RuntimeException: Unable to start activity ComponentInfo{...}: 
android.view.InflateException: Binary XML file line #12: Error inflating class ImageView
    at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2913)
    at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3048)
    ...
Caused by: android.content.res.Resources$NotFoundException: 
    Drawable com.example.myapp:drawable/ic_logo with resource ID #0x7f080123
    at android.content.res.ResourcesImpl.loadDrawableForCookie(ResourcesImpl.java:878)
    ...
```

**문제 파악**: `ic_logo` 이미지 리소스를 찾을 수 없음

#### 2단계: 문제 원인 찾기

```kotlin
// MainActivity.kt
@Composable
fun MainScreen() {
    Column {
        // 문제가 되는 코드
        Image(
            painter = painterResource(id = R.drawable.ic_logo), // ← 여기서 크래시
            contentDescription = "Logo"
        )
    }
}
```

#### 3단계: 해결

```kotlin
// ✅ 해결 방법 1: 리소스 파일 확인
// res/drawable/ 폴더에 ic_logo.xml 또는 ic_logo.png 파일이 있는지 확인

// ✅ 해결 방법 2: 안전한 리소스 로딩
@Composable
fun MainScreen() {
    Column {
        // 리소스가 없을 때 대체 이미지 표시
        val logoResource = try {
            painterResource(id = R.drawable.ic_logo)
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Logo resource not found", e)
            painterResource(id = R.drawable.ic_placeholder)
        }
        
        Image(
            painter = logoResource,
            contentDescription = "Logo"
        )
    }
}
```

### 시나리오 2: 특정 화면에서만 크래시

**증상**: 제품 상세 화면으로 이동하면 크래시

#### 1단계: Breakpoint 설정

```kotlin
class ProductDetailViewModel(
    private val productId: Long,
    private val repository: ProductRepository
) : ViewModel() {
    
    init {
        // Breakpoint 설정 ←
        loadProduct()
    }
    
    private fun loadProduct() {
        viewModelScope.launch {
            try {
                // Breakpoint 설정 ←
                val product = repository.getProduct(productId)
                
                // Breakpoint 설정 ← product 값 확인
                _product.value = product
                
            } catch (e: Exception) {
                // Breakpoint 설정 ← 예외 확인
                Log.e(TAG, "Failed to load product", e)
            }
        }
    }
}
```

#### 2단계: 변수 값 확인

Breakpoint에서 멈췄을 때 Variables 창에서 확인:
```
productId = 0  ← 문제 발견! 유효하지 않은 ID
repository = ProductRepository@12345
product = null
```

#### 3단계: 호출 스택 추적

```kotlin
// ProductListScreen.kt
@Composable
fun ProductListScreen(navController: NavController) {
    LazyColumn {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = {
                    // ❌ 문제: product.id가 0일 수 있음
                    navController.navigate("product/${product.id}")
                }
            )
        }
    }
}

// ✅ 해결 방법: 유효성 검사
@Composable
fun ProductListScreen(navController: NavController) {
    LazyColumn {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = {
                    if (product.id > 0) {
                        navController.navigate("product/${product.id}")
                    } else {
                        Log.e(TAG, "Invalid product ID: ${product.id}")
                    }
                }
            )
        }
    }
}
```

### 시나리오 3: 간헐적 크래시

**증상**: 가끔씩 크래시가 발생하지만 재현하기 어려움

#### 1단계: Crashlytics 통합

```kotlin
// build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

```kotlin
/**
 * Application 클래스
 */
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Crashlytics 초기화
        FirebaseCrashlytics.getInstance().apply {
            // 디버그 빌드에서는 비활성화
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        }
    }
}
```

#### 2단계: 커스텀 로그 추가

```kotlin
/**
 * 크래시 발생 시 추가 정보 수집
 */
class ShoppingCartViewModel : ViewModel() {
    
    fun checkout() {
        // Crashlytics에 커스텀 키 추가
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("cart_items_count", cartItems.size)
            setCustomKey("total_price", calculateTotal())
            setUserId(currentUserId)
        }
        
        try {
            processCheckout()
        } catch (e: Exception) {
            // 예외를 Crashlytics에 기록
            FirebaseCrashlytics.getInstance().recordException(e)
            
            // 사용자에게 에러 표시
            _errorState.value = "결제 처리 중 오류가 발생했습니다"
        }
    }
}
```

#### 3단계: Firebase Console에서 분석

```
Crashlytics Dashboard:
├─ 크래시 발생 횟수: 23회
├─ 영향받은 사용자: 15명
├─ 주요 기기: Samsung Galaxy S21 (Android 13)
└─ 스택 트레이스:
    java.lang.NullPointerException: Attempt to invoke virtual method 
    'double com.example.Payment.getAmount()' on a null object reference
    at com.example.ShoppingCartViewModel.processCheckout(...)
    
커스텀 키:
├─ cart_items_count: 5
├─ total_price: 125000.0
└─ user_id: user_12345
```

**문제 파악**: `Payment` 객체가 null일 때 크래시 발생

#### 4단계: 해결

```kotlin
// ❌ 문제 코드
fun processCheckout() {
    val payment = createPayment() // null 반환 가능
    val amount = payment.getAmount() // NPE 발생!
}

// ✅ 해결 방법
fun processCheckout() {
    val payment = createPayment()
    
    if (payment == null) {
        Log.e(TAG, "Failed to create payment")
        _errorState.value = "결제 정보를 생성할 수 없습니다"
        return
    }
    
    val amount = payment.getAmount()
    // ...
}
```

---

## UI 버그 해결

### 시나리오 4: 텍스트가 잘림

**증상**: 긴 제품 이름이 화면에 다 표시되지 않음

#### 1단계: Layout Inspector로 확인

```kotlin
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_product),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 문제: 텍스트가 잘림
            Column {
                Text(
                    text = product.name, // "매우 긴 제품 이름이 여기에 표시됩니다..."
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
```

**Layout Inspector에서 확인**:
- Text의 실제 너비: 200dp
- 사용 가능한 공간: 180dp
- → 텍스트가 잘림

#### 2단계: 해결

```kotlin
// ✅ 해결 방법 1: maxLines와 overflow 사용
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_product),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                // Row에서 남은 공간 모두 사용
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,  // 최대 2줄
                    overflow = TextOverflow.Ellipsis  // 넘치면 "..." 표시
                )
            }
        }
    }
}

// ✅ 해결 방법 2: 자동 크기 조절
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_product),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // 텍스트 크기 자동 조절
                AutoSizeText(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    maxLines: Int = 1
) {
    var textStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }
    
    Text(
        text = text,
        style = textStyle,
        maxLines = maxLines,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                // 텍스트가 넘치면 폰트 크기 줄이기
                textStyle = textStyle.copy(
                    fontSize = textStyle.fontSize * 0.9
                )
            } else {
                readyToDraw = true
            }
        },
        modifier = Modifier.drawWithContent {
            if (readyToDraw) drawContent()
        }
    )
}
```

### 시나리오 5: 리스트 스크롤이 끊김

**증상**: LazyColumn을 스크롤할 때 버벅거림

#### 1단계: Compose Layout Inspector로 Recomposition 확인

```kotlin
@Composable
fun ProductList(products: List<Product>) {
    LazyColumn {
        items(products) { product ->
            // Recomposition 카운트 확인
            // Layout Inspector에서 이 Composable이 몇 번 재구성되는지 확인
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    // ❌ 문제: 매번 새 객체 생성
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
    val formattedDate = dateFormatter.format(product.createdAt)
    
    Card {
        Column {
            Text(product.name)
            Text(formattedDate)
        }
    }
}
```

**Layout Inspector에서 확인**:
- ProductCard Recomposition 카운트: 50+ (스크롤할 때마다 증가)
- → 불필요한 재구성 발생

#### 2단계: 해결

```kotlin
// ✅ 해결 방법 1: remember 사용
@Composable
fun ProductCard(product: Product) {
    // dateFormatter를 한 번만 생성
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd") }
    
    // product.createdAt이 변경될 때만 재계산
    val formattedDate = remember(product.createdAt) {
        dateFormatter.format(product.createdAt)
    }
    
    Card {
        Column {
            Text(product.name)
            Text(formattedDate)
        }
    }
}

// ✅ 해결 방법 2: key 사용
@Composable
fun ProductList(products: List<Product>) {
    LazyColumn {
        items(
            items = products,
            key = { it.id }  // 각 아이템을 고유하게 식별
        ) { product ->
            ProductCard(product)
        }
    }
}

// ✅ 해결 방법 3: 데이터 변환을 ViewModel에서
class ProductListViewModel : ViewModel() {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
    
    val products: StateFlow<List<ProductUiModel>> = repository.getProducts()
        .map { productList ->
            productList.map { product ->
                ProductUiModel(
                    id = product.id,
                    name = product.name,
                    formattedDate = dateFormatter.format(product.createdAt)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

data class ProductUiModel(
    val id: Long,
    val name: String,
    val formattedDate: String  // 이미 포맷된 날짜
)

@Composable
fun ProductCard(product: ProductUiModel) {
    Card {
        Column {
            Text(product.name)
            Text(product.formattedDate)  // 그냥 표시만 하면 됨
        }
    }
}
```

---

## 성능 문제 해결

### 시나리오 6: 앱 시작이 느림

**증상**: 앱을 실행하면 흰 화면이 3초 이상 표시됨

#### 1단계: CPU Profiler로 분석

```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // CPU Profiler 기록 시작
        
        // 무거운 초기화 작업들
        initializeFirebase()      // 500ms
        initializeAnalytics()     // 300ms
        loadUserPreferences()     // 800ms  ← 가장 느림!
        setupNotifications()      // 200ms
        
        // CPU Profiler 기록 종료
    }
}
```

**CPU Profiler 결과**:
```
Total Time: 1800ms
├─ loadUserPreferences(): 800ms (44%)  ← 병목 지점
├─ initializeFirebase(): 500ms (28%)
├─ initializeAnalytics(): 300ms (17%)
└─ setupNotifications(): 200ms (11%)
```

#### 2단계: 해결

```kotlin
// ✅ 해결 방법 1: 백그라운드에서 초기화
class MyApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // 필수 초기화만 메인 스레드에서
        initializeFirebase()
        
        // 나머지는 백그라운드에서
        applicationScope.launch {
            initializeAnalytics()
            loadUserPreferences()
            setupNotifications()
        }
    }
}

// ✅ 해결 방법 2: Lazy 초기화
class MyApp : Application() {
    
    // 필요할 때만 초기화
    val userPreferences: UserPreferences by lazy {
        loadUserPreferences()
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 필수 초기화만
        initializeFirebase()
    }
}

// ✅ 해결 방법 3: App Startup 라이브러리 사용
class FirebaseInitializer : Initializer<Unit> {
    
    override fun create(context: Context) {
        // 자동으로 백그라운드에서 초기화
        Firebase.initialize(context)
    }
    
    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
```

### 시나리오 7: 이미지 로딩이 느림

**증상**: 제품 이미지가 표시되는 데 시간이 오래 걸림

#### 1단계: Network Inspector로 확인

```
Request: GET https://api.example.com/images/product_12345.jpg
Response Size: 5.2 MB  ← 너무 큼!
Time: 3000ms
```

#### 2단계: 해결

```kotlin
// ❌ 문제: 원본 이미지를 그대로 로드
@Composable
fun ProductImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,  // 5MB 이미지
        contentDescription = null,
        modifier = Modifier.size(100.dp)  // 실제로는 100dp만 필요
    )
}

// ✅ 해결 방법 1: 이미지 크기 조절
@Composable
fun ProductImage(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .size(300, 300)  // 300x300으로 리사이징
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(100.dp)
    )
}

// ✅ 해결 방법 2: 썸네일 URL 사용
data class Product(
    val id: Long,
    val name: String,
    val imageUrl: String,        // 원본: 5MB
    val thumbnailUrl: String     // 썸네일: 50KB
)

@Composable
fun ProductCard(product: Product) {
    AsyncImage(
        model = product.thumbnailUrl,  // 썸네일 사용
        contentDescription = null,
        modifier = Modifier.size(100.dp)
    )
}

// ✅ 해결 방법 3: 메모리 캐싱
@Composable
fun ProductImage(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCacheKey(imageUrl)  // 메모리 캐시 키
            .diskCacheKey(imageUrl)    // 디스크 캐시 키
            .build(),
        contentDescription = null,
        modifier = Modifier.size(100.dp)
    )
}
```

---

## 네트워크 문제 해결

### 시나리오 8: API 호출이 실패함

**증상**: 사용자 목록을 불러올 수 없음

#### 1단계: Network Inspector로 확인

```
Request: GET https://api.example.com/users
Status: 401 Unauthorized
Response:
{
  "error": "Invalid token",
  "message": "Authentication token has expired"
}
```

#### 2단계: Breakpoint로 토큰 확인

```kotlin
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        
        // Breakpoint 설정 ← token 값 확인
        Log.d(TAG, "Token: $token")
        
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(request)
    }
}
```

**Variables 창에서 확인**:
```
token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
isExpired = true  ← 문제 발견!
```

#### 3단계: 해결

```kotlin
// ✅ 토큰 갱신 로직 추가
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        
        // 토큰이 만료되었는지 확인
        if (tokenManager.isTokenExpired()) {
            // 토큰 갱신
            val newToken = runBlocking {
                tokenManager.refreshToken()
            }
            
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            
            return chain.proceed(request)
        }
        
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        val response = chain.proceed(request)
        
        // 401 응답 시 토큰 갱신 후 재시도
        if (response.code == 401) {
            response.close()
            
            val newToken = runBlocking {
                tokenManager.refreshToken()
            }
            
            val retryRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            
            return chain.proceed(retryRequest)
        }
        
        return response
    }
}
```

### 시나리오 9: 타임아웃 에러

**증상**: 네트워크 요청이 자주 타임아웃됨

#### 1단계: Network Inspector로 타이밍 확인

```
Request: GET https://api.example.com/products?page=1&limit=100
Timeline:
├─ Waiting: 15000ms  ← 타임아웃 (기본 10초)
└─ Status: Timeout
```

#### 2단계: 해결

```kotlin
// ✅ 해결 방법 1: 타임아웃 시간 증가
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)  // 연결 타임아웃: 30초
    .readTimeout(30, TimeUnit.SECONDS)     // 읽기 타임아웃: 30초
    .writeTimeout(30, TimeUnit.SECONDS)    // 쓰기 타임아웃: 30초
    .build()

// ✅ 해결 방법 2: 페이지 크기 줄이기
@GET("products")
suspend fun getProducts(
    @Query("page") page: Int,
    @Query("limit") limit: Int = 20  // 100 → 20으로 줄임
): ProductsResponse

// ✅ 해결 방법 3: 재시도 로직 추가
class RetryInterceptor(
    private val maxRetries: Int = 3
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        repeat(maxRetries) { attempt ->
            try {
                response = chain.proceed(request)
                
                if (response!!.isSuccessful) {
                    return response!!
                }
                
                response!!.close()
                
            } catch (e: IOException) {
                exception = e
                Log.w(TAG, "Request failed, attempt ${attempt + 1}/$maxRetries", e)
                
                if (attempt < maxRetries - 1) {
                    // 지수 백오프: 1초, 2초, 4초...
                    Thread.sleep((1000 * (1 shl attempt)).toLong())
                }
            }
        }
        
        throw exception ?: IOException("Request failed after $maxRetries attempts")
    }
}
```

---

## 메모리 누수 해결

### 시나리오 10: 메모리 사용량이 계속 증가

**증상**: 앱을 오래 사용하면 느려지고 결국 크래시

#### 1단계: Memory Profiler로 확인

```
Heap Dump:
├─ Bitmap: 45 MB  ← 비정상적으로 많음
├─ String: 5 MB
└─ Other: 10 MB

Total: 60 MB (계속 증가 중)
```

#### 2단계: Heap Dump 분석

```
Bitmap 객체들:
├─ ImageCache.cache: 30 MB (15개 Bitmap)
├─ ProductListScreen: 10 MB (5개 Bitmap)
└─ ProductDetailScreen: 5 MB (2개 Bitmap)

→ ImageCache에 이미지가 계속 쌓이고 있음
```

#### 3단계: 코드 확인

```kotlin
// ❌ 문제: 이미지가 무제한으로 캐시됨
object ImageCache {
    private val cache = mutableMapOf<String, Bitmap>()
    
    fun getImage(url: String): Bitmap? {
        return cache[url]
    }
    
    fun putImage(url: String, bitmap: Bitmap) {
        cache[url] = bitmap  // 계속 추가만 됨!
    }
}
```

#### 4단계: 해결

```kotlin
// ✅ 해결 방법: LruCache 사용
object ImageCache {
    
    // 최대 메모리의 1/8 사용
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    
    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        
        // Bitmap 크기 계산
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024  // KB 단위
        }
        
        // 캐시에서 제거될 때 호출
        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            // Bitmap 리소스 해제
            if (evicted && !oldValue.isRecycled) {
                oldValue.recycle()
            }
        }
    }
    
    fun getImage(url: String): Bitmap? {
        return cache.get(url)
    }
    
    fun putImage(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }
}
```

### 시나리오 11: Activity 누수

**증상**: StrictMode에서 Activity 누수 경고

#### 1단계: StrictMode 로그 확인

```
StrictMode policy violation: android.os.strictmode.InstanceCountViolation
    Activity instance count: 3 (expected: 1)
    at com.example.app.MainActivity
```

#### 2단계: Heap Dump에서 누수 찾기

```
MainActivity 인스턴스:
├─ Instance 1: DESTROYED (누수!)
│   └─ Referenced by: MyViewModel.activity
├─ Instance 2: DESTROYED (누수!)
│   └─ Referenced by: EventBus.listeners
└─ Instance 3: ACTIVE (정상)
```

#### 3단계: 코드 확인

```kotlin
// ❌ 문제 1: ViewModel이 Activity 참조
class MyViewModel : ViewModel() {
    private var activity: Activity? = null  // 메모리 누수!
    
    fun setActivity(activity: Activity) {
        this.activity = activity
    }
}

// ❌ 문제 2: 리스너 등록 해제 안 함
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 리스너 등록
        EventBus.register(this)  // 등록 해제 안 함!
    }
}
```

#### 4단계: 해결

```kotlin
// ✅ 해결 방법 1: Application Context 사용
class MyViewModel(
    private val application: Application
) : ViewModel() {
    
    fun doSomething() {
        // Application Context는 누수 없음
        val context = application.applicationContext
        // ...
    }
}

// ✅ 해결 방법 2: 리스너 등록 해제
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        EventBus.register(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 리스너 등록 해제
        EventBus.unregister(this)
    }
}

// ✅ 해결 방법 3: WeakReference 사용
class EventBus {
    
    // Activity를 직접 참조하지 않고 WeakReference 사용
    private val listeners = mutableListOf<WeakReference<EventListener>>()
    
    fun register(listener: EventListener) {
        listeners.add(WeakReference(listener))
    }
    
    fun post(event: Event) {
        listeners.removeAll { it.get() == null }  // null 참조 제거
        
        listeners.forEach { ref ->
            ref.get()?.onEvent(event)
        }
    }
}
```

---

## Compose 특화 디버깅

### 시나리오 12: State가 업데이트되지 않음

**증상**: 버튼을 클릭해도 UI가 변경되지 않음

#### 1단계: Compose Layout Inspector로 확인

```kotlin
@Composable
fun CounterScreen() {
    // ❌ 문제: 일반 변수 사용
    var count = 0  // Recomposition 트리거 안 됨!
    
    Column {
        Text("Count: $count")
        
        Button(onClick = {
            count++  // 값은 증가하지만 UI는 업데이트 안 됨
            Log.d(TAG, "Count: $count")  // 로그에는 증가된 값 출력
        }) {
            Text("Increment")
        }
    }
}
```

**Layout Inspector에서 확인**:
- CounterScreen Recomposition 카운트: 1 (변하지 않음)
- Text 값: "Count: 0" (변하지 않음)

#### 2단계: 해결

```kotlin
// ✅ 해결 방법 1: remember + mutableStateOf
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("Count: $count")
        
        Button(onClick = {
            count++  // Recomposition 트리거됨!
        }) {
            Text("Increment")
        }
    }
}

// ✅ 해결 방법 2: ViewModel 사용
class CounterViewModel : ViewModel() {
    
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    fun increment() {
        _count.value++
    }
}

@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val count by viewModel.count.collectAsState()
    
    Column {
        Text("Count: $count")
        
        Button(onClick = {
            viewModel.increment()
        }) {
            Text("Increment")
        }
    }
}
```

### 시나리오 13: LaunchedEffect가 무한 루프

**증상**: 앱이 멈추고 Logcat에 같은 로그가 계속 출력됨

#### 1단계: 로그 확인

```
D/ProductScreen: Loading products...
D/ProductScreen: Loading products...
D/ProductScreen: Loading products...
D/ProductScreen: Loading products...
... (무한 반복)
```

#### 2단계: 코드 확인

```kotlin
@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()
    
    // ❌ 문제: key가 없어서 매번 실행됨
    LaunchedEffect(Unit) {
        Log.d(TAG, "Loading products...")
        viewModel.loadProducts()
    }
    
    // products가 변경되면 Recomposition 발생
    // → LaunchedEffect 재실행
    // → loadProducts() 호출
    // → products 변경
    // → 무한 루프!
    
    LazyColumn {
        items(products) { product ->
            ProductCard(product)
        }
    }
}
```

#### 3단계: 해결

```kotlin
// ✅ 해결 방법 1: 올바른 key 사용
@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()
    
    // Unit을 key로 사용하면 한 번만 실행됨
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    
    LazyColumn {
        items(products) { product ->
            ProductCard(product)
        }
    }
}

// ✅ 해결 방법 2: ViewModel에서 초기화
class ProductViewModel : ViewModel() {
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    init {
        // ViewModel 생성 시 한 번만 실행
        loadProducts()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            val products = repository.getProducts()
            _products.value = products
        }
    }
}

@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()
    
    // LaunchedEffect 불필요
    
    LazyColumn {
        items(products) { product ->
            ProductCard(product)
        }
    }
}
```

---

## 💡 디버깅 체크리스트

### 크래시 발생 시
- [ ] Logcat에서 스택 트레이스 확인
- [ ] 예외 타입과 메시지 확인
- [ ] 크래시가 발생한 코드 라인 찾기
- [ ] Breakpoint 설정하여 변수 값 확인
- [ ] Crashlytics로 프로덕션 크래시 추적

### UI 버그 발생 시
- [ ] Layout Inspector로 UI 계층 확인
- [ ] Compose Layout Inspector로 Recomposition 확인
- [ ] 실제 크기와 예상 크기 비교
- [ ] Modifier 순서 확인

### 성능 문제 발생 시
- [ ] CPU Profiler로 병목 지점 찾기
- [ ] Memory Profiler로 메모리 사용량 확인
- [ ] Network Inspector로 네트워크 요청 확인
- [ ] StrictMode로 메인 스레드 위반 감지

### 네트워크 문제 발생 시
- [ ] Network Inspector로 요청/응답 확인
- [ ] 상태 코드와 에러 메시지 확인
- [ ] 타임아웃 설정 확인
- [ ] 재시도 로직 확인

### 메모리 누수 발생 시
- [ ] Memory Profiler로 Heap Dump 촬영
- [ ] 누수된 객체 찾기
- [ ] StrictMode로 Activity 누수 감지
- [ ] WeakReference 또는 Lifecycle 사용

---

## 🎯 다음 단계

실전 디버깅 시나리오를 마스터했습니다! 이제 다음을 학습하세요:

1. **[17-1. Android 앱 서명](./17-1-android-app-signing.md)** - 앱 서명 및 보안
2. **[21. 성능 최적화](./21-android-performance-optimization.md)** - 앱 성능 최적화

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Debugging! 🐛
