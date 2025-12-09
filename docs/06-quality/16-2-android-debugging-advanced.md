# Android 디버깅 고급 가이드

> 📖 **시리즈 구성**
> - **16-1**: [Android 디버깅 기초](./16-1-android-debugging-basics.md) - Logcat, Breakpoint, Layout Inspector
> - **16-2**: Android 디버깅 고급 (현재 문서) - Database/Network/Background Inspector, ANR, StrictMode
> - **16-3**: [Android 디버깅 실전](./16-3-android-debugging-scenarios.md) - 실전 문제 해결 시나리오

---

## 📚 목차

1. [Database Inspector](#database-inspector)
2. [Network Inspector](#network-inspector)
3. [Background Task Inspector](#background-task-inspector)
4. [ANR 분석](#anr-분석)
5. [StrictMode](#strictmode)
6. [성능 프로파일링](#성능-프로파일링)

---

## Database Inspector

### 🎯 Database Inspector란?

**Database Inspector**는 실행 중인 앱의 Room 데이터베이스를 실시간으로 조회하고 수정할 수 있는 도구입니다.

```
실행 중인 앱 → Database Inspector 연결 → 테이블 조회 → 데이터 수정 → 앱에 즉시 반영
```

### 🚀 사용 방법

#### 1. Database Inspector 열기

1. **View → Tool Windows → App Inspection**
2. **Database Inspector** 탭 선택
3. 실행 중인 앱 선택

#### 2. 데이터베이스 연결

```kotlin
/**
 * Room 데이터베이스 예제
 * 
 * Database Inspector는 Room 데이터베이스를 자동으로 감지합니다.
 */
@Database(
    entities = [User::class, Product::class, Order::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
}

/**
 * User 엔티티
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

Database Inspector에 `users` 테이블이 자동으로 표시됩니다.

### 🔍 주요 기능

#### 1. 실시간 데이터 조회

```sql
-- Database Inspector의 쿼리 창에서 실행 가능

-- 모든 사용자 조회
SELECT * FROM users;

-- 특정 조건으로 필터링
SELECT * FROM users WHERE email LIKE '%@gmail.com';

-- 조인 쿼리
SELECT 
    orders.id,
    users.name,
    products.name as product_name,
    orders.quantity
FROM orders
INNER JOIN users ON orders.user_id = users.id
INNER JOIN products ON orders.product_id = products.id;

-- 집계 함수
SELECT 
    user_id,
    COUNT(*) as order_count,
    SUM(total_price) as total_spent
FROM orders
GROUP BY user_id
ORDER BY total_spent DESC;
```

#### 2. 데이터 수정

Database Inspector에서 직접 데이터를 수정할 수 있습니다:

1. 테이블 선택
2. 행 더블클릭
3. 값 수정
4. Enter 키 → 즉시 데이터베이스에 반영

> [!WARNING]
> **프로덕션 데이터베이스는 절대 수정하지 마세요!** Database Inspector는 개발/테스트 환경에서만 사용하세요.

#### 3. Live Updates

**Live Updates**를 활성화하면 데이터베이스 변경사항이 실시간으로 반영됩니다.

```kotlin
/**
 * 사용자 추가 예제
 * 
 * 이 코드를 실행하면 Database Inspector에서
 * 실시간으로 새 행이 추가되는 것을 볼 수 있습니다.
 */
class UserRepository(private val userDao: UserDao) {
    
    suspend fun addUser(email: String, name: String) {
        val user = User(
            email = email,
            name = name
        )
        
        // Database Inspector에서 실시간으로 확인 가능
        userDao.insert(user)
        
        Log.d(TAG, "사용자 추가 완료: $name")
    }
}
```

### 🎨 실전 활용 예제

#### 문제: 사용자가 로그인했는데 프로필이 표시되지 않음

```kotlin
// 1. Database Inspector에서 users 테이블 확인
// SELECT * FROM users WHERE email = 'user@example.com';

// 2. 데이터가 있는지 확인
// - 데이터가 없다면: 회원가입 로직 문제
// - 데이터가 있다면: 조회 로직 문제

// 3. DAO 쿼리 확인
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
}

// 4. Database Inspector에서 직접 쿼리 실행하여 검증
// SELECT * FROM users WHERE email = 'user@example.com';
```

---

## Network Inspector

### 🎯 Network Inspector란?

**Network Inspector**는 앱의 모든 네트워크 요청과 응답을 실시간으로 모니터링하는 도구입니다.

```
앱 → HTTP 요청 → Network Inspector 기록 → 요청/응답 확인
```

### 🚀 사용 방법

#### 1. Network Inspector 열기

1. **View → Tool Windows → App Inspection**
2. **Network Inspector** 탭 선택
3. 실행 중인 앱 선택

#### 2. 네트워크 요청 모니터링

```kotlin
/**
 * Retrofit API 서비스 예제
 * 
 * Network Inspector는 Retrofit 요청을 자동으로 감지합니다.
 */
interface ApiService {
    
    /**
     * 사용자 목록 조회
     * 
     * Network Inspector에서 확인 가능:
     * - URL: https://api.example.com/users
     * - Method: GET
     * - Headers: Authorization, Content-Type
     * - Response: JSON 데이터
     */
    @GET("users")
    suspend fun getUsers(): List<User>
    
    /**
     * 사용자 생성
     * 
     * Network Inspector에서 확인 가능:
     * - URL: https://api.example.com/users
     * - Method: POST
     * - Request Body: JSON
     * - Response: 생성된 User 객체
     */
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): User
    
    /**
     * 이미지 업로드
     * 
     * Network Inspector에서 확인 가능:
     * - Content-Type: multipart/form-data
     * - Request Body: 이미지 파일
     */
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): UploadResponse
}
```

### 🔍 주요 기능

#### 1. 요청/응답 상세 정보

Network Inspector에서 각 요청을 클릭하면 다음 정보를 확인할 수 있습니다:

**Request 탭**:
```
URL: https://api.example.com/users/123
Method: GET
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
  User-Agent: MyApp/1.0.0
Query Parameters:
  include: profile,orders
  page: 1
  limit: 20
```

**Response 탭**:
```
Status Code: 200 OK
Headers:
  Content-Type: application/json; charset=utf-8
  Cache-Control: max-age=3600
  ETag: "abc123"
Response Body:
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "profile": { ... },
  "orders": [ ... ]
}
```

#### 2. 타이밍 정보

각 요청의 성능을 분석할 수 있습니다:

```
Timeline:
├─ Waiting: 50ms      (서버 응답 대기)
├─ Receiving: 100ms   (데이터 수신)
└─ Total: 150ms       (전체 시간)

Size:
├─ Request: 256 bytes
└─ Response: 4.2 KB
```

#### 3. 에러 디버깅

```kotlin
/**
 * 네트워크 에러 처리 예제
 */
class UserRepository(private val apiService: ApiService) {
    
    suspend fun getUser(userId: Long): Result<User> {
        return try {
            val user = apiService.getUser(userId)
            Result.success(user)
            
        } catch (e: HttpException) {
            // Network Inspector에서 확인:
            // - Status Code: 404
            // - Response Body: {"error": "User not found"}
            
            Log.e(TAG, "HTTP 에러: ${e.code()}", e)
            Result.failure(e)
            
        } catch (e: IOException) {
            // Network Inspector에서 확인:
            // - 요청이 실패로 표시됨
            // - 타임아웃 또는 연결 실패
            
            Log.e(TAG, "네트워크 에러", e)
            Result.failure(e)
        }
    }
}
```

### 🎨 실전 활용 예제

#### 문제: API 호출이 느림

```kotlin
// 1. Network Inspector에서 느린 요청 찾기
// Timeline을 확인하여 어느 단계가 느린지 파악

// 2. 문제 원인 분석
// - Waiting이 길다면: 서버 응답이 느림 → 백엔드 최적화 필요
// - Receiving이 길다면: 데이터가 너무 큼 → 페이지네이션 또는 압축 필요

// 3. 해결 방법: 페이지네이션 적용
@GET("products")
suspend fun getProducts(
    @Query("page") page: Int,
    @Query("limit") limit: Int = 20  // 한 번에 20개씩만 로드
): ProductsResponse

// 4. Network Inspector에서 개선 확인
// Before: 5MB, 3000ms
// After: 100KB, 200ms
```

#### 문제: 인증 토큰이 만료됨

```kotlin
/**
 * 토큰 갱신 인터셉터
 * 
 * Network Inspector에서 확인:
 * 1. 401 Unauthorized 응답
 * 2. 토큰 갱신 요청
 * 3. 원래 요청 재시도
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 토큰 추가
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer ${tokenManager.getToken()}")
            .build()
        
        val response = chain.proceed(authenticatedRequest)
        
        // 401 에러 시 토큰 갱신
        if (response.code == 401) {
            response.close()
            
            // Network Inspector에서 토큰 갱신 요청 확인 가능
            val newToken = tokenManager.refreshToken()
            
            // 원래 요청 재시도
            val retryRequest = request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            
            return chain.proceed(retryRequest)
        }
        
        return response
    }
}
```

---

## Background Task Inspector

### 🎯 Background Task Inspector란?

**Background Task Inspector**는 WorkManager로 예약된 백그라운드 작업의 상태를 실시간으로 확인할 수 있는 도구입니다.

```
WorkManager 작업 예약 → Background Task Inspector → 작업 상태 확인 → 로그 확인
```

### 🚀 사용 방법

#### 1. Background Task Inspector 열기

1. **View → Tool Windows → App Inspection**
2. **Background Task Inspector** 탭 선택
3. 실행 중인 앱 선택

#### 2. WorkManager 작업 모니터링

```kotlin
/**
 * WorkManager 작업 예제
 * 
 * Background Task Inspector에서 확인 가능:
 * - 작업 상태: ENQUEUED, RUNNING, SUCCEEDED, FAILED
 * - 실행 시간
 * - 재시도 횟수
 * - 제약 조건
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // Background Task Inspector에서 로그 확인 가능
        Log.d(TAG, "동기화 작업 시작")
        
        return try {
            // 데이터 동기화
            val syncedItems = syncData()
            
            Log.d(TAG, "동기화 완료: ${syncedItems.size}개 항목")
            
            // Background Task Inspector에 SUCCEEDED로 표시됨
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "동기화 실패", e)
            
            // Background Task Inspector에 FAILED로 표시됨
            // 재시도 횟수도 확인 가능
            Result.retry()
        }
    }
    
    private suspend fun syncData(): List<Item> {
        // 실제 동기화 로직
        delay(2000) // 시뮬레이션
        return emptyList()
    }
}
```

### 🔍 주요 기능

#### 1. 작업 상태 확인

```kotlin
/**
 * 작업 예약 및 상태 확인
 */
class SyncManager(private val workManager: WorkManager) {
    
    fun scheduleSyncWork() {
        // 제약 조건 설정
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // 네트워크 필요
            .setRequiresBatteryNotLow(true)                 // 배터리 충분
            .setRequiresCharging(false)                     // 충전 불필요
            .build()
        
        // 작업 요청 생성
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,  // 지수 백오프
                10,                          // 초기 지연: 10초
                TimeUnit.SECONDS
            )
            .addTag("sync")
            .build()
        
        // 작업 예약
        workManager.enqueue(syncRequest)
        
        // Background Task Inspector에서 확인:
        // - State: ENQUEUED
        // - Constraints: Network=CONNECTED, BatteryNotLow=true
        // - Tags: sync
    }
    
    /**
     * 작업 상태 관찰
     */
    fun observeSyncWork(workId: UUID) {
        workManager.getWorkInfoByIdLiveData(workId)
            .observeForever { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        Log.d(TAG, "작업 대기 중")
                        // Background Task Inspector: ENQUEUED
                    }
                    WorkInfo.State.RUNNING -> {
                        Log.d(TAG, "작업 실행 중")
                        // Background Task Inspector: RUNNING
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        Log.d(TAG, "작업 완료")
                        // Background Task Inspector: SUCCEEDED
                    }
                    WorkInfo.State.FAILED -> {
                        Log.e(TAG, "작업 실패")
                        // Background Task Inspector: FAILED
                    }
                    WorkInfo.State.CANCELLED -> {
                        Log.d(TAG, "작업 취소됨")
                        // Background Task Inspector: CANCELLED
                    }
                    else -> {}
                }
            }
    }
}
```

#### 2. 제약 조건 확인

Background Task Inspector에서 작업이 실행되지 않는 이유를 확인할 수 있습니다:

```
Work: SyncWorker
State: ENQUEUED (대기 중)
Constraints:
  ✅ Network: CONNECTED (충족)
  ❌ Battery Not Low: Required (미충족) ← 배터리가 부족해서 실행 안 됨
  ✅ Charging: Not Required
  
→ 배터리를 충전하거나 제약 조건을 변경해야 함
```

#### 3. 주기적 작업 모니터링

```kotlin
/**
 * 주기적 동기화 작업
 * 
 * Background Task Inspector에서 확인:
 * - 다음 실행 시간
 * - 마지막 실행 결과
 * - 실행 간격
 */
fun schedulePeriodicSync() {
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        repeatInterval = 15,           // 15분마다
        repeatIntervalTimeUnit = TimeUnit.MINUTES,
        flexTimeInterval = 5,          // ±5분 유연성
        flexTimeIntervalUnit = TimeUnit.MINUTES
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()
    
    workManager.enqueueUniquePeriodicWork(
        "periodic_sync",
        ExistingPeriodicWorkPolicy.KEEP,  // 기존 작업 유지
        syncRequest
    )
    
    // Background Task Inspector에서 확인:
    // - Next Run: 2024-12-03 10:30:00
    // - Interval: 15 minutes
    // - Last Result: SUCCEEDED
}
```

---

## ANR 분석

### 🎯 ANR이란?

**ANR(Application Not Responding)**은 앱이 5초 이상 응답하지 않을 때 발생하는 에러입니다.

```
메인 스레드 블로킹 → 5초 경과 → ANR 다이얼로그 표시 → 사용자가 앱 강제 종료
```

### 🚨 ANR 발생 원인

#### 1. 메인 스레드에서 무거운 작업

```kotlin
// ❌ ANR 발생 코드
class ProductViewModel : ViewModel() {
    
    fun loadProducts() {
        // 메인 스레드에서 무거운 작업 - ANR 발생!
        val products = mutableListOf<Product>()
        
        // 10,000개 아이템 처리 (약 10초 소요)
        repeat(10000) { i ->
            products.add(
                Product(
                    id = i.toLong(),
                    name = "Product $i",
                    price = i * 1000.0
                )
            )
        }
        
        _products.value = products
    }
}

// ✅ 해결 방법: Coroutine 사용
class ProductViewModel : ViewModel() {
    
    fun loadProducts() {
        viewModelScope.launch {
            // IO 디스패처에서 실행
            val products = withContext(Dispatchers.Default) {
                List(10000) { i ->
                    Product(
                        id = i.toLong(),
                        name = "Product $i",
                        price = i * 1000.0
                    )
                }
            }
            
            // 메인 스레드에서 UI 업데이트
            _products.value = products
        }
    }
}
```

#### 2. 메인 스레드에서 네트워크 호출

```kotlin
// ❌ ANR 발생 코드
class UserRepository {
    
    fun getUser(userId: Long): User {
        // 메인 스레드에서 네트워크 호출 - ANR 발생!
        return apiService.getUser(userId).execute().body()!!
    }
}

// ✅ 해결 방법: suspend 함수 사용
class UserRepository {
    
    suspend fun getUser(userId: Long): User {
        return withContext(Dispatchers.IO) {
            apiService.getUser(userId)
        }
    }
}
```

#### 3. 메인 스레드에서 데이터베이스 작업

```kotlin
// ❌ ANR 발생 코드
class UserDao {
    
    fun getAllUsers(): List<User> {
        // 메인 스레드에서 DB 쿼리 - ANR 발생!
        return database.userDao().getAllUsers()
    }
}

// ✅ 해결 방법: suspend 함수 사용
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}
```

### 🔍 ANR 로그 분석

ANR이 발생하면 `/data/anr/traces.txt` 파일에 스택 트레이스가 기록됩니다.

```
// ANR 로그 예시
"main" prio=5 tid=1 Sleeping
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x74b37080 self=0x7f8c014c00
  | sysTid=12345 nice=-10 cgrp=default sched=0/0 handle=0x7f8c123456
  | state=S schedstat=( 1234567890 987654321 123 ) utm=123 stm=45 core=0 HZ=100
  | stack=0x7fff12345000-0x7fff12347000 stackSize=8MB
  | held mutexes=
  at java.lang.Thread.sleep(Native method)
  at com.example.app.ProductViewModel.loadProducts(ProductViewModel.kt:25)
  ← 여기서 ANR 발생!
  at com.example.app.ProductScreen.onCreate(ProductScreen.kt:15)
  ...
```

### 🛠️ ANR 방지 방법

#### 1. 무거운 작업은 백그라운드에서

```kotlin
/**
 * 이미지 처리 예제
 * 
 * 이미지 리사이징은 CPU 집약적 작업이므로
 * Default 디스패처에서 실행합니다.
 */
class ImageProcessor {
    
    suspend fun resizeImage(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap = withContext(Dispatchers.Default) {
        // CPU 집약적 작업
        Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
```

#### 2. 타임아웃 설정

```kotlin
/**
 * 타임아웃이 있는 네트워크 호출
 * 
 * 네트워크 호출이 너무 오래 걸리면 취소합니다.
 */
suspend fun fetchDataWithTimeout(): Result<Data> {
    return try {
        withTimeout(5000) { // 5초 타임아웃
            val data = apiService.getData()
            Result.success(data)
        }
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "타임아웃 발생", e)
        Result.failure(e)
    }
}
```

#### 3. 진행률 표시

```kotlin
/**
 * 긴 작업 시 진행률 표시
 * 
 * 사용자에게 작업이 진행 중임을 알려줍니다.
 */
@Composable
fun DataSyncScreen(viewModel: SyncViewModel = viewModel()) {
    val syncProgress by viewModel.syncProgress.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isSyncing) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("동기화 중... ${syncProgress}%")
            }
        }
    }
}

class SyncViewModel : ViewModel() {
    
    private val _syncProgress = MutableStateFlow(0)
    val syncProgress = _syncProgress.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()
    
    fun syncData() {
        viewModelScope.launch {
            _isSyncing.value = true
            
            try {
                val totalItems = 1000
                
                // 백그라운드에서 동기화
                withContext(Dispatchers.IO) {
                    repeat(totalItems) { i ->
                        // 아이템 동기화
                        syncItem(i)
                        
                        // 진행률 업데이트 (메인 스레드)
                        withContext(Dispatchers.Main) {
                            _syncProgress.value = ((i + 1) * 100 / totalItems)
                        }
                    }
                }
                
            } finally {
                _isSyncing.value = false
                _syncProgress.value = 0
            }
        }
    }
}
```

---

## StrictMode

### 🎯 StrictMode란?

**StrictMode**는 메인 스레드에서 실행되면 안 되는 작업을 감지하여 경고하는 개발 도구입니다.

```
메인 스레드 위반 감지 → Logcat에 경고 출력 → 앱 크래시 (옵션)
```

### 🚀 StrictMode 설정

```kotlin
/**
 * Application 클래스에서 StrictMode 설정
 * 
 * 디버그 빌드에서만 활성화합니다.
 */
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
    }
    
    /**
     * StrictMode 활성화
     * 
     * 메인 스레드와 VM 정책을 모두 설정합니다.
     */
    private fun enableStrictMode() {
        // 스레드 정책: 메인 스레드에서 하면 안 되는 작업 감지
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                // 디스크 읽기 감지
                .detectDiskReads()
                
                // 디스크 쓰기 감지
                .detectDiskWrites()
                
                // 네트워크 작업 감지
                .detectNetwork()
                
                // 커스텀 느린 호출 감지
                .detectCustomSlowCalls()
                
                // 모든 위반 감지
                .detectAll()
                
                // 위반 시 Logcat에 출력
                .penaltyLog()
                
                // 위반 시 다이얼로그 표시 (개발 중에만)
                .penaltyDialog()
                
                // 위반 시 앱 크래시 (엄격 모드)
                // .penaltyDeath()
                
                .build()
        )
        
        // VM 정책: 메모리 누수 및 리소스 누수 감지
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                // Activity 누수 감지
                .detectActivityLeaks()
                
                // Closeable 객체 누수 감지 (파일, DB 커서 등)
                .detectLeakedClosableObjects()
                
                // SQLite 객체 누수 감지
                .detectLeakedSqlLiteObjects()
                
                // 등록 해제되지 않은 리시버 감지
                .detectLeakedRegistrationObjects()
                
                // 파일 URI 노출 감지 (Android 7.0+)
                .detectFileUriExposure()
                
                // 암호화되지 않은 네트워크 감지 (Android 9.0+)
                .detectCleartextNetwork()
                
                // 모든 위반 감지
                .detectAll()
                
                // 위반 시 Logcat에 출력
                .penaltyLog()
                
                .build()
        )
    }
}
```

### 🔍 StrictMode 위반 예제

#### 1. 메인 스레드에서 디스크 읽기

```kotlin
// ❌ StrictMode 위반
class SettingsManager(private val context: Context) {
    
    fun getSettings(): Settings {
        // 메인 스레드에서 파일 읽기 - StrictMode 경고!
        val file = File(context.filesDir, "settings.json")
        val json = file.readText()
        return Json.decodeFromString(json)
    }
}

// ✅ 해결 방법
class SettingsManager(private val context: Context) {
    
    suspend fun getSettings(): Settings = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "settings.json")
        val json = file.readText()
        Json.decodeFromString(json)
    }
}
```

**StrictMode 로그**:
```
StrictMode policy violation: android.os.strictmode.DiskReadViolation
    at android.os.StrictMode$AndroidBlockGuardPolicy.onReadFromDisk
    at java.io.FileInputStream.read
    at com.example.app.SettingsManager.getSettings(SettingsManager.kt:15)
```

#### 2. 메인 스레드에서 네트워크 호출

```kotlin
// ❌ StrictMode 위반
fun checkServerStatus(): Boolean {
    // 메인 스레드에서 네트워크 호출 - StrictMode 경고!
    val url = URL("https://api.example.com/status")
    val connection = url.openConnection() as HttpURLConnection
    return connection.responseCode == 200
}

// ✅ 해결 방법
suspend fun checkServerStatus(): Boolean = withContext(Dispatchers.IO) {
    val url = URL("https://api.example.com/status")
    val connection = url.openConnection() as HttpURLConnection
    connection.responseCode == 200
}
```

#### 3. Activity 누수

```kotlin
// ❌ StrictMode 위반: Activity 누수
class LeakyViewModel : ViewModel() {
    
    // Activity를 직접 참조 - 메모리 누수!
    private var activity: Activity? = null
    
    fun setActivity(activity: Activity) {
        this.activity = activity
    }
}

// ✅ 해결 방법: Application Context 사용
class FixedViewModel(
    private val application: Application
) : ViewModel() {
    
    // Application Context는 누수 없음
    fun doSomething() {
        val appContext = application.applicationContext
        // ...
    }
}
```

**StrictMode 로그**:
```
StrictMode policy violation: android.os.strictmode.InstanceCountViolation
    Activity instance count: 2 (expected: 1)
    at com.example.app.MainActivity
```

### 🛠️ 커스텀 느린 호출 감지

```kotlin
/**
 * 커스텀 느린 호출 감지
 * 
 * 특정 작업이 너무 오래 걸리는지 확인합니다.
 */
class DataProcessor {
    
    fun processLargeData(data: List<String>) {
        // 느린 호출 시작 표시
        StrictMode.noteSlowCall("Processing large data")
        
        // 무거운 작업
        data.forEach { item ->
            processItem(item)
        }
        
        // StrictMode가 이 호출이 느리다고 경고함
    }
}
```

---

## 성능 프로파일링

### 🎯 Android Profiler

**Android Profiler**는 앱의 CPU, 메모리, 네트워크, 에너지 사용량을 실시간으로 모니터링하는 도구입니다.

### 🚀 사용 방법

1. **View → Tool Windows → Profiler**
2. 실행 중인 앱 선택
3. 프로파일링할 항목 선택 (CPU, Memory, Network, Energy)

### 📊 CPU Profiler

#### 1. CPU 사용량 확인

```kotlin
/**
 * CPU 집약적 작업 예제
 */
class ImageFilter {
    
    fun applyFilter(bitmap: Bitmap): Bitmap {
        // CPU Profiler에서 이 함수의 실행 시간 확인 가능
        
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // 각 픽셀 처리 (CPU 집약적)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            // 그레이스케일 변환
            val gray = (r + g + b) / 3
            pixels[i] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
        }
        
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
```

**CPU Profiler에서 확인**:
- `applyFilter()` 함수가 전체 CPU 시간의 80% 차지
- 픽셀 처리 루프가 가장 느림
- → 최적화 필요

#### 2. 메서드 트레이싱

CPU Profiler에서 **Record** 버튼을 눌러 메서드 호출을 기록합니다:

```
Top Down:
├─ MainActivity.onCreate() - 1000ms
│   ├─ loadData() - 800ms
│   │   ├─ fetchFromNetwork() - 500ms ← 가장 느림
│   │   └─ parseJson() - 300ms
│   └─ setupUI() - 200ms
```

### 💾 Memory Profiler

#### 1. 메모리 사용량 확인

```kotlin
/**
 * 메모리 누수 예제
 */
class ImageCache {
    
    // ❌ 메모리 누수: 이미지가 계속 쌓임
    private val cache = mutableMapOf<String, Bitmap>()
    
    fun loadImage(url: String): Bitmap {
        return cache.getOrPut(url) {
            // 이미지 로드
            loadBitmapFromUrl(url)
        }
    }
}

// ✅ 해결 방법: LruCache 사용
class ImageCache {
    
    // 최대 메모리의 1/8 사용
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    
    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // 비트맵 크기 (KB 단위)
            return bitmap.byteCount / 1024
        }
    }
    
    fun loadImage(url: String): Bitmap {
        return cache.get(url) ?: run {
            val bitmap = loadBitmapFromUrl(url)
            cache.put(url, bitmap)
            bitmap
        }
    }
}
```

**Memory Profiler에서 확인**:
- Heap Dump 촬영
- Bitmap 객체가 메모리를 많이 차지하는지 확인
- 누수된 객체 찾기

#### 2. Allocation Tracking

메모리 할당을 추적하여 어디서 객체가 생성되는지 확인:

```kotlin
/**
 * 불필요한 객체 생성 예제
 */
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            // ❌ 매번 새 객체 생성 - Memory Profiler에서 확인 가능
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val dateString = formatter.format(user.createdAt)
            
            Text(dateString)
        }
    }
}

// ✅ 해결 방법: remember 사용
@Composable
fun UserList(users: List<User>) {
    // formatter를 한 번만 생성
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd") }
    
    LazyColumn {
        items(users) { user ->
            val dateString = remember(user.createdAt) {
                formatter.format(user.createdAt)
            }
            
            Text(dateString)
        }
    }
}
```

### 🌐 Network Profiler

Network Profiler는 앞서 설명한 Network Inspector와 유사하지만, 더 상세한 타임라인을 제공합니다.

```kotlin
/**
 * 네트워크 최적화 예제
 */
class ProductRepository(private val apiService: ApiService) {
    
    // ❌ 비효율적: 각 제품을 개별적으로 로드
    suspend fun loadProducts(productIds: List<Long>): List<Product> {
        return productIds.map { id ->
            // Network Profiler에서 100개의 개별 요청 확인
            apiService.getProduct(id)
        }
    }
    
    // ✅ 효율적: 한 번에 로드
    suspend fun loadProducts(productIds: List<Long>): List<Product> {
        // Network Profiler에서 1개의 요청만 확인
        return apiService.getProducts(productIds)
    }
}
```

---

## 🎯 다음 단계

고급 디버깅 도구를 마스터했습니다! 다음 단계로:

1. **[16-3. Android 디버깅 실전](./16-3-android-debugging-scenarios.md)** - 실전 문제 해결 시나리오

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Debugging! 🐛
