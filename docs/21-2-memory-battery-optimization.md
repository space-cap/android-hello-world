# 메모리 및 배터리 최적화

> 📖 **시리즈 구성**
> - **21-1**: [Compose 성능 최적화](./21-1-compose-performance-optimization.md)
> - **21-2**: 메모리 및 배터리 최적화 (현재 문서)
> - **21-3**: [성능 프로파일링 실전](./21-3-performance-profiling.md)

---

## 📚 목차

1. [메모리 관리](#메모리-관리)
2. [메모리 누수 방지](#메모리-누수-방지)
3. [Bitmap 최적화](#bitmap-최적화)
4. [배터리 최적화](#배터리-최적화)
5. [리소스 관리](#리소스-관리)
6. [실전 예제](#실전-예제)

---

## 메모리 관리

### Android 메모리 구조

```
┌─────────────────────────────────┐
│     Java Heap (앱 메모리)        │
│  - 객체, 배열                    │
│  - Garbage Collection 대상       │
├─────────────────────────────────┤
│     Native Heap                 │
│  - Bitmap, Native 코드           │
│  - 수동 관리 필요                │
├─────────────────────────────────┤
│     Stack                       │
│  - 로컬 변수, 메서드 호출        │
└─────────────────────────────────┘
```

### 메모리 프로파일링

**Android Studio → View → Tool Windows → Profiler**

```kotlin
/**
 * 메모리 사용량 모니터링
 * 
 * Profiler에서 확인할 수 있는 정보:
 * - Java Heap: 앱 객체가 사용하는 메모리
 * - Native Heap: Bitmap, Native 라이브러리
 * - Graphics: GPU 메모리
 * - Stack: 스레드 스택
 * - Code: 앱 코드와 리소스
 */
class MemoryMonitor {
    
    /**
     * 현재 메모리 사용량 로깅
     */
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val availableMemory = maxMemory - usedMemory
        
        Log.d("Memory", """
            사용 중: ${usedMemory}MB
            최대: ${maxMemory}MB
            사용 가능: ${availableMemory}MB
        """.trimIndent())
    }
    
    /**
     * 메모리 부족 경고
     */
    fun isLowMemory(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        return memoryInfo.lowMemory
    }
}
```

### 메모리 할당 최소화

```kotlin
// ❌ 나쁜 예: 매번 새 객체 생성
@Composable
fun BadMemoryUsage(items: List<String>) {
    LazyColumn {
        items(items.size) { index ->
            // 매번 새 Modifier 객체 생성
            Text(
                text = items[index],
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

// ✅ 좋은 예: Modifier 재사용
@Composable
fun GoodMemoryUsage(items: List<String>) {
    // Modifier를 한 번만 생성
    val itemModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    
    LazyColumn {
        items(items.size) { index ->
            Text(
                text = items[index],
                modifier = itemModifier  // 재사용
            )
        }
    }
}
```

### 컬렉션 최적화

```kotlin
/**
 * 컬렉션 크기 미리 지정
 */
// ❌ 나쁜 예: 크기 지정 안 함
fun processItems(count: Int): List<String> {
    val result = mutableListOf<String>()  // 기본 크기: 10
    repeat(count) {
        result.add("Item $it")  // 크기 초과 시 재할당
    }
    return result
}

// ✅ 좋은 예: 초기 크기 지정
fun processItemsOptimized(count: Int): List<String> {
    val result = ArrayList<String>(count)  // 초기 크기 지정
    repeat(count) {
        result.add("Item $it")  // 재할당 없음
    }
    return result
}

/**
 * 적절한 컬렉션 타입 선택
 */
class CollectionOptimization {
    
    // ✅ 순차 접근: List
    fun sequentialAccess(): List<String> {
        return listOf("A", "B", "C")
    }
    
    // ✅ 빠른 검색: Set
    fun fastLookup(): Set<String> {
        return setOf("A", "B", "C")
    }
    
    // ✅ 키-값 매핑: Map
    fun keyValueMapping(): Map<String, Int> {
        return mapOf("A" to 1, "B" to 2)
    }
    
    // ✅ 대용량 데이터: Sequence
    fun largeDataProcessing(items: List<Int>): Sequence<Int> {
        return items.asSequence()
            .filter { it > 0 }
            .map { it * 2 }
            // Sequence는 lazy evaluation
            // 실제 사용 시점에 계산
    }
}
```

---

## 메모리 누수 방지

### 메모리 누수란?

**메모리 누수(Memory Leak)**: 더 이상 사용하지 않는 객체가 Garbage Collection되지 않고 메모리에 남아있는 현상

### 일반적인 메모리 누수 패턴

#### 1. Context 누수

```kotlin
// ❌ 나쁜 예: Activity Context를 오래 보관
class BadSingleton private constructor(context: Context) {
    companion object {
        private var instance: BadSingleton? = null
        
        fun getInstance(context: Context): BadSingleton {
            if (instance == null) {
                // Activity Context를 저장하면 누수 발생
                instance = BadSingleton(context)
            }
            return instance!!
        }
    }
    
    private val activityContext = context  // ❌ Activity 누수
}

// ✅ 좋은 예: Application Context 사용
class GoodSingleton private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: GoodSingleton? = null
        
        fun getInstance(context: Context): GoodSingleton {
            return instance ?: synchronized(this) {
                instance ?: GoodSingleton(
                    context.applicationContext  // ✅ Application Context
                ).also { instance = it }
            }
        }
    }
    
    private val appContext = context
}
```

#### 2. 리스너 누수

```kotlin
// ❌ 나쁜 예: 리스너 해제 안 함
class BadActivity : AppCompatActivity() {
    
    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    private val locationListener = LocationListener { location ->
        // 위치 업데이트 처리
        updateLocation(location)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 리스너 등록
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            10f,
            locationListener
        )
        
        // ❌ onDestroy에서 해제 안 함 → 누수
    }
}

// ✅ 좋은 예: 리스너 해제
class GoodActivity : AppCompatActivity() {
    
    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    private val locationListener = LocationListener { location ->
        updateLocation(location)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            10f,
            locationListener
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // ✅ 리스너 해제
        locationManager.removeUpdates(locationListener)
    }
}
```

#### 3. Handler 누수

```kotlin
// ❌ 나쁜 예: Inner Class Handler
class BadActivity : AppCompatActivity() {
    
    // ❌ Inner class는 외부 클래스 참조를 보유
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // Activity 참조 사용
            updateUI()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 지연된 메시지 전송
        handler.sendEmptyMessageDelayed(0, 60000)  // 1분 후
        // Activity가 종료되어도 Handler는 메시지 큐에 남음
    }
}

// ✅ 좋은 예: Static Handler + WeakReference
class GoodActivity : AppCompatActivity() {
    
    private val handler = MyHandler(this)
    
    // ✅ Static class + WeakReference
    private class MyHandler(activity: GoodActivity) : Handler(Looper.getMainLooper()) {
        private val activityRef = WeakReference(activity)
        
        override fun handleMessage(msg: Message) {
            // WeakReference로 Activity 접근
            activityRef.get()?.let { activity ->
                activity.updateUI()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.sendEmptyMessageDelayed(0, 60000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // ✅ 메시지 제거
        handler.removeCallbacksAndMessages(null)
    }
}
```

#### 4. ViewModel 누수

```kotlin
// ❌ 나쁜 예: ViewModel에서 Context 보관
class BadViewModel(
    private val context: Context  // ❌ Activity Context 누수
) : ViewModel() {
    
    fun loadData() {
        // Context 사용
        val data = context.getString(R.string.app_name)
    }
}

// ✅ 좋은 예: Application 사용
class GoodViewModel(
    private val application: Application  // ✅ Application
) : AndroidViewModel(application) {
    
    fun loadData() {
        // Application 사용
        val data = application.getString(R.string.app_name)
    }
}

// ✅ 더 나은 방법: Dependency Injection
class BestViewModel(
    private val repository: DataRepository  // Context 불필요
) : ViewModel() {
    
    fun loadData() {
        // Repository 사용
        viewModelScope.launch {
            val data = repository.getData()
        }
    }
}
```

### LeakCanary로 누수 탐지

```kotlin
// build.gradle.kts
dependencies {
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}

/**
 * LeakCanary는 자동으로 메모리 누수를 탐지합니다
 * 
 * 누수 발견 시:
 * 1. 알림 표시
 * 2. Leak Trace 제공
 * 3. 누수 원인 분석
 */
```

---

## Bitmap 최적화

### Bitmap 메모리 계산

```kotlin
/**
 * Bitmap 메모리 사용량 계산
 * 
 * 메모리 = 너비 × 높이 × 바이트/픽셀
 * 
 * 예: 1920×1080 이미지 (ARGB_8888)
 * = 1920 × 1080 × 4 bytes
 * = 8,294,400 bytes
 * ≈ 8MB
 */
fun calculateBitmapSize(width: Int, height: Int, config: Bitmap.Config): Long {
    val bytesPerPixel = when (config) {
        Bitmap.Config.ARGB_8888 -> 4  // 32bit
        Bitmap.Config.RGB_565 -> 2    // 16bit
        Bitmap.Config.ARGB_4444 -> 2  // 16bit (deprecated)
        Bitmap.Config.ALPHA_8 -> 1    // 8bit
        else -> 4
    }
    
    return (width * height * bytesPerPixel).toLong()
}
```

### 이미지 다운샘플링

```kotlin
/**
 * 이미지 크기 줄여서 로드
 * 
 * 큰 이미지를 작은 ImageView에 표시할 때 필수
 */
fun decodeSampledBitmapFromResource(
    res: Resources,
    resId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    // 1단계: 이미지 크기만 먼저 읽기
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, this)
        
        // 2단계: 샘플링 비율 계산
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        
        // 3단계: 실제 이미지 로드
        inJustDecodeBounds = false
        BitmapFactory.decodeResource(res, resId, this)
    }
}

/**
 * 샘플링 비율 계산
 * 
 * inSampleSize = 2: 이미지를 1/2 크기로 로드
 * inSampleSize = 4: 이미지를 1/4 크기로 로드
 */
fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        
        // 요청 크기보다 작아질 때까지 반복
        while (halfHeight / inSampleSize >= reqHeight &&
            halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    
    return inSampleSize
}

/**
 * 사용 예
 */
@Composable
fun OptimizedImage() {
    val context = LocalContext.current
    
    // ✅ 필요한 크기만큼만 로드
    val bitmap = remember {
        decodeSampledBitmapFromResource(
            context.resources,
            R.drawable.large_image,
            reqWidth = 200,  // ImageView 크기
            reqHeight = 200
        )
    }
    
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### Coil 라이브러리 활용

```kotlin
/**
 * Coil: 이미지 로딩 라이브러리
 * 
 * 자동 최적화:
 * - 메모리 캐싱
 * - 디스크 캐싱
 * - 다운샘플링
 * - Placeholder
 */
@Composable
fun CoilImageExample(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            // ✅ 크기 지정 (다운샘플링)
            .size(400)
            // ✅ 메모리 캐시 설정
            .memoryCachePolicy(CachePolicy.ENABLED)
            // ✅ 디스크 캐시 설정
            .diskCachePolicy(CachePolicy.ENABLED)
            // ✅ Crossfade 애니메이션
            .crossfade(true)
            // ✅ Placeholder
            .placeholder(R.drawable.placeholder)
            // ✅ 에러 이미지
            .error(R.drawable.error)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}

/**
 * Coil 전역 설정
 */
class MyApplication : Application(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // ✅ 메모리 캐시 크기 설정
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)  // 앱 메모리의 25%
                    .build()
            }
            // ✅ 디스크 캐시 크기 설정
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)  // 50MB
                    .build()
            }
            // ✅ 로깅 (디버그용)
            .logger(DebugLogger())
            .build()
    }
}
```

### Bitmap 재활용

```kotlin
/**
 * Bitmap 재활용 (API 29 이하)
 * 
 * API 29+에서는 자동으로 관리됨
 */
class BitmapRecycler {
    
    private var bitmap: Bitmap? = null
    
    /**
     * Bitmap 로드
     */
    fun loadBitmap(res: Resources, resId: Int) {
        // 기존 Bitmap 재활용
        bitmap?.recycle()
        
        bitmap = BitmapFactory.decodeResource(res, resId)
    }
    
    /**
     * 정리
     */
    fun cleanup() {
        bitmap?.recycle()
        bitmap = null
    }
}

/**
 * Compose에서 Bitmap 관리
 */
@Composable
fun BitmapImage(imageRes: Int) {
    val context = LocalContext.current
    
    val bitmap = remember(imageRes) {
        BitmapFactory.decodeResource(context.resources, imageRes)
    }
    
    // ✅ Composable이 제거될 때 Bitmap 재활용
    DisposableEffect(bitmap) {
        onDispose {
            bitmap.recycle()
        }
    }
    
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null
    )
}
```

---

## 배터리 최적화

### Doze Mode 이해

```
사용자가 기기를 사용하지 않을 때:

화면 꺼짐
    ↓
30분 대기
    ↓
Doze Mode 진입
    ↓
┌─────────────────────────────┐
│  네트워크 차단               │
│  Wake Lock 무시              │
│  알람 지연                   │
│  GPS/WiFi 스캔 중단          │
└─────────────────────────────┘
    ↓
주기적으로 Maintenance Window
(짧은 시간 동안 작업 허용)
```

### Doze Mode 대응

```kotlin
/**
 * Doze Mode에서도 실행되어야 하는 작업
 * 
 * 방법 1: WorkManager 사용 (권장)
 */
class DozeOptimizedWork : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // WorkManager는 Doze Mode를 자동으로 처리
        syncData()
        return Result.success()
    }
    
    private suspend fun syncData() {
        // 데이터 동기화
    }
}

/**
 * 방법 2: AlarmManager (긴급한 경우만)
 */
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * Doze Mode에서도 실행되는 알람
     * 
     * 주의: 배터리 소모가 크므로 꼭 필요한 경우만 사용
     */
    fun scheduleExactAlarm(triggerTime: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // ✅ Doze Mode에서도 실행
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

/**
 * 방법 3: Firebase Cloud Messaging (FCM)
 * 
 * FCM 고우선순위 메시지는 Doze Mode를 우회
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(message: RemoteMessage) {
        // Doze Mode에서도 수신 가능
        if (message.priority == RemoteMessage.PRIORITY_HIGH) {
            handleUrgentMessage(message)
        }
    }
}
```

### 배터리 효율적인 위치 추적

```kotlin
/**
 * 위치 서비스 최적화
 */
class LocationTracker(private val context: Context) {
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * 배터리 효율적인 위치 요청
     */
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            // ✅ 우선순위: 배터리 절약
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            // ✅ 업데이트 간격: 5분
            TimeUnit.MINUTES.toMillis(5)
        )
            // ✅ 최소 간격: 1분
            .setMinUpdateIntervalMillis(TimeUnit.MINUTES.toMillis(1))
            // ✅ 최대 대기 시간: 10분 (배칭)
            .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(10))
            .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    handleLocation(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    
    /**
     * 상황별 위치 정확도 조정
     */
    fun requestLocationByScenario(scenario: LocationScenario) {
        val priority = when (scenario) {
            // 내비게이션: 높은 정확도
            LocationScenario.NAVIGATION -> Priority.PRIORITY_HIGH_ACCURACY
            
            // 날씨 앱: 낮은 정확도
            LocationScenario.WEATHER -> Priority.PRIORITY_LOW_POWER
            
            // 일반: 균형
            LocationScenario.GENERAL -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        
        // 우선순위에 따라 요청
    }
}

enum class LocationScenario {
    NAVIGATION, WEATHER, GENERAL
}
```

### 네트워크 배칭

```kotlin
/**
 * 네트워크 요청 배칭
 * 
 * 여러 요청을 모아서 한 번에 전송
 */
class NetworkBatcher {
    
    private val pendingRequests = mutableListOf<Request>()
    private val batchInterval = 5 * 60 * 1000L  // 5분
    
    /**
     * 요청 추가
     */
    fun addRequest(request: Request) {
        synchronized(pendingRequests) {
            pendingRequests.add(request)
        }
        
        // 일정 개수 이상이면 즉시 전송
        if (pendingRequests.size >= 10) {
            sendBatch()
        }
    }
    
    /**
     * 배치 전송
     */
    private fun sendBatch() {
        synchronized(pendingRequests) {
            if (pendingRequests.isEmpty()) return
            
            // ✅ 한 번의 네트워크 연결로 모든 요청 전송
            val batch = pendingRequests.toList()
            pendingRequests.clear()
            
            // API 호출
            sendBatchToServer(batch)
        }
    }
    
    /**
     * WorkManager로 주기적 배치 전송
     */
    fun scheduleBatchWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<BatchWorker>(
            repeatInterval = 15,  // 최소 15분
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            // ✅ WiFi 연결 시에만 실행
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "batch_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

class BatchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // 배치 전송
        return Result.success()
    }
}
```

### 센서 사용 최적화

```kotlin
/**
 * 센서 리스너 최적화
 */
class SensorOptimizer(context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    /**
     * 센서 리스너 등록
     */
    fun startSensorUpdates() {
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                handleSensorData(event)
            }
            
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        // ✅ 샘플링 속도 조절
        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            // SENSOR_DELAY_NORMAL: 200,000 μs (5 Hz)
            // SENSOR_DELAY_UI: 60,000 μs (16.67 Hz)
            // SENSOR_DELAY_GAME: 20,000 μs (50 Hz)
            // SENSOR_DELAY_FASTEST: 0 μs (최대 속도)
            SensorManager.SENSOR_DELAY_NORMAL  // ✅ 배터리 절약
        )
    }
    
    /**
     * 화면 꺼질 때 센서 중지
     */
    fun registerScreenOffReceiver(context: Context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        // ✅ 화면 꺼지면 센서 중지
                        sensorManager.unregisterListener(this@SensorOptimizer as SensorEventListener)
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        // 화면 켜지면 센서 재시작
                        startSensorUpdates()
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        
        context.registerReceiver(receiver, filter)
    }
}
```

---

## 리소스 관리

### Drawable 최적화

```kotlin
/**
 * Vector Drawable 사용
 * 
 * 장점:
 * - 크기 조절 시 품질 유지
 * - 파일 크기 작음
 * - 다양한 화면 밀도 지원
 */

// res/drawable/ic_star.xml
/*
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M12,17.27L18.18,21l-1.64,-7.03L22,9.24l-7.19,-0.61L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21z"/>
</vector>
*/

@Composable
fun VectorDrawableExample() {
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "별",
        modifier = Modifier.size(100.dp)  // 크기 자유롭게 조절
    )
}

/**
 * WebP 포맷 사용
 * 
 * PNG/JPEG 대비 25-35% 작은 파일 크기
 */
// Android Studio → 이미지 우클릭 → Convert to WebP
```

### 폰트 최적화

```kotlin
/**
 * Downloadable Fonts 사용
 * 
 * 장점:
 * - APK 크기 감소
 * - 여러 앱이 폰트 공유
 * - 자동 업데이트
 */

// res/font/my_font.xml
/*
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:app="http://schemas.android.com/apk/res-auto"
    app:fontProviderAuthority="com.google.android.gms.fonts"
    app:fontProviderPackage="com.google.android.gms"
    app:fontProviderQuery="Roboto"
    app:fontProviderCerts="@array/com_google_android_gms_fonts_certs">
</font-family>
*/

@Composable
fun DownloadableFontExample() {
    val fontFamily = FontFamily(
        Font(R.font.my_font)
    )
    
    Text(
        text = "Hello, World!",
        fontFamily = fontFamily
    )
}

/**
 * 폰트 서브셋 사용
 * 
 * 필요한 문자만 포함하여 파일 크기 감소
 */
// 도구: https://fonts.google.com/
// "Customize" → 필요한 문자 선택 → 다운로드
```

### 애니메이션 최적화

```kotlin
/**
 * 하드웨어 가속 애니메이션
 */
@Composable
fun HardwareAcceleratedAnimation() {
    var visible by remember { mutableStateOf(true) }
    
    // ✅ Compose 애니메이션은 자동으로 하드웨어 가속
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card {
            Text("애니메이션 컨텐츠")
        }
    }
}

/**
 * 애니메이션 프레임 레이트 제한
 */
@Composable
fun ThrottledAnimation() {
    var progress by remember { mutableStateOf(0f) }
    
    // ✅ 애니메이션 속도 조절
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    
    LinearProgressIndicator(progress = animatedProgress)
}
```

---

## 실전 예제

### 예제 1: 이미지 갤러리 앱

```kotlin
/**
 * 메모리 효율적인 이미지 갤러리
 * 
 * 최적화:
 * - 썸네일 다운샘플링
 * - 메모리 캐시
 * - 디스크 캐시
 * - Lazy Loading
 */
@Composable
fun ImageGalleryScreen(
    viewModel: GalleryViewModel = viewModel()
) {
    val images by viewModel.images.collectAsState()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = images,
            key = { it.id }
        ) { image ->
            GalleryThumbnail(image)
        }
    }
}

@Composable
fun GalleryThumbnail(image: GalleryImage) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.url)
            // ✅ 썸네일 크기로 다운샘플링
            .size(200)
            // ✅ 메모리 캐시
            .memoryCacheKey(image.id)
            // ✅ 디스크 캐시
            .diskCacheKey(image.id)
            .crossfade(true)
            .build(),
        contentDescription = image.description,
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { /* 전체 이미지 보기 */ },
        contentScale = ContentScale.Crop
    )
}

/**
 * ViewModel: 페이징 구현
 */
class GalleryViewModel : ViewModel() {
    
    private val _images = MutableStateFlow<List<GalleryImage>>(emptyList())
    val images: StateFlow<List<GalleryImage>> = _images
    
    private var currentPage = 0
    private val pageSize = 30
    
    init {
        loadNextPage()
    }
    
    /**
     * 다음 페이지 로드
     */
    fun loadNextPage() {
        viewModelScope.launch {
            val newImages = loadImagesFromServer(currentPage, pageSize)
            _images.value = _images.value + newImages
            currentPage++
        }
    }
    
    private suspend fun loadImagesFromServer(page: Int, size: Int): List<GalleryImage> {
        // API 호출
        return emptyList()
    }
}
```

### 예제 2: 음악 플레이어 앱

```kotlin
/**
 * 배터리 효율적인 음악 플레이어
 * 
 * 최적화:
 * - Foreground Service
 * - Wake Lock 최소화
 * - 오디오 포커스 관리
 */
class MusicPlayerService : Service() {
    
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private var wakeLock: PowerManager.WakeLock? = null
    
    override fun onCreate() {
        super.onCreate()
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer = MediaPlayer()
        
        // ✅ 오디오 포커스 리스너
        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        // 포커스 완전 상실 → 정지
                        stopPlayback()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        // 일시적 상실 → 일시정지
                        pausePlayback()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        // 볼륨 낮춤
                        mediaPlayer.setVolume(0.3f, 0.3f)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        // 포커스 회복 → 재생
                        resumePlayback()
                        mediaPlayer.setVolume(1.0f, 1.0f)
                    }
                }
            }
            .build()
        
        audioManager.requestAudioFocus(audioFocusRequest)
    }
    
    /**
     * 재생 시작
     */
    private fun startPlayback(audioUrl: String) {
        // ✅ Wake Lock 획득 (재생 중에만)
        acquireWakeLock()
        
        mediaPlayer.apply {
            reset()
            setDataSource(audioUrl)
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                // ✅ 재생 완료 시 Wake Lock 해제
                releaseWakeLock()
            }
            prepareAsync()
        }
    }
    
    /**
     * Wake Lock 관리
     */
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MusicPlayer::WakeLock"
            )
        }
        wakeLock?.acquire(10 * 60 * 1000L)  // 최대 10분
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        releaseWakeLock()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
```

### 예제 3: 지도 앱

```kotlin
/**
 * 메모리 및 배터리 효율적인 지도 앱
 * 
 * 최적화:
 * - 위치 업데이트 간격 조절
 * - 지도 타일 캐싱
 * - 마커 클러스터링
 */
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val locationState by viewModel.locationState.collectAsState()
    val markers by viewModel.markers.collectAsState()
    
    // ✅ 지도 상태 저장
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            locationState.latLng,
            15f
        )
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            // ✅ 지도 타일 캐싱
            isBuildingEnabled = false,
            isIndoorEnabled = false
        )
    ) {
        // ✅ 마커 클러스터링 (많은 마커 처리)
        MarkerClusterer(
            items = markers,
            onClusterClick = { cluster ->
                // 클러스터 클릭 처리
                true
            }
        ) { marker ->
            Marker(
                state = MarkerState(position = marker.position),
                title = marker.title
            )
        }
    }
}

class MapViewModel(
    private val locationTracker: LocationTracker
) : ViewModel() {
    
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState
    
    init {
        startLocationUpdates()
    }
    
    /**
     * 위치 업데이트 시작
     */
    private fun startLocationUpdates() {
        viewModelScope.launch {
            locationTracker.getLocationUpdates(
                // ✅ 배터리 효율적인 간격
                intervalMillis = 5 * 60 * 1000L,  // 5분
                priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            ).collect { location ->
                _locationState.value = LocationState(
                    latLng = LatLng(location.latitude, location.longitude)
                )
            }
        }
    }
}
```

---

## 💡 베스트 프랙티스 요약

### 메모리 관리
- ✅ 메모리 프로파일러로 주기적 모니터링
- ✅ 메모리 누수 방지 (Context, Listener, Handler)
- ✅ LeakCanary로 누수 탐지
- ✅ 적절한 컬렉션 타입 선택

### Bitmap 최적화
- ✅ 이미지 다운샘플링
- ✅ Coil 라이브러리 활용
- ✅ 메모리/디스크 캐싱
- ✅ Bitmap 재활용 (필요 시)

### 배터리 최적화
- ✅ Doze Mode 대응
- ✅ WorkManager 사용
- ✅ 네트워크 배칭
- ✅ 센서 사용 최소화
- ✅ Wake Lock 최소화

### 리소스 관리
- ✅ Vector Drawable 사용
- ✅ WebP 포맷 사용
- ✅ Downloadable Fonts
- ✅ 하드웨어 가속 애니메이션

---

## 🎯 다음 단계

메모리 및 배터리 최적화를 마스터했습니다! 다음으로:

1. **[21-3. 성능 프로파일링 실전](./21-3-performance-profiling.md)** - Android Profiler, 실전 분석

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Optimizing! 🔋
