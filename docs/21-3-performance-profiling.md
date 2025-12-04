# 성능 프로파일링 실전

> 📖 **시리즈 구성**
> - **21-1**: [Compose 성능 최적화](./21-1-compose-performance-optimization.md)
> - **21-2**: [메모리 및 배터리 최적화](./21-2-memory-battery-optimization.md)
> - **21-3**: 성능 프로파일링 실전 (현재 문서)

---

## 📚 목차

1. [Android Profiler 개요](#android-profiler-개요)
2. [CPU Profiler](#cpu-profiler)
3. [Memory Profiler](#memory-profiler)
4. [Network Profiler](#network-profiler)
5. [Energy Profiler](#energy-profiler)
6. [고급 프로파일링 도구](#고급-프로파일링-도구)
7. [실전 분석 사례](#실전-분석-사례)

---

## Android Profiler 개요

### Profiler 열기

**Android Studio → View → Tool Windows → Profiler**

또는 **Run → Profile 'app'**

### Profiler 구성

```
┌─────────────────────────────────────────────┐
│  Timeline (시간 축)                          │
├─────────────────────────────────────────────┤
│  CPU    ████████░░░░░░                      │
│  Memory ███░░░░░░░░░░░                      │
│  Network ░░░░████░░░░░                      │
│  Energy  ██████░░░░░░░                      │
└─────────────────────────────────────────────┘
```

### Sessions 관리

```kotlin
/**
 * Profiler Session
 * 
 * 1. 새 세션 시작
 * 2. 앱 사용 (시나리오 실행)
 * 3. 세션 저장
 * 4. 나중에 분석
 */

// Session 저장 위치:
// Android Studio → File → Export → Profiler Sessions
```

---

## CPU Profiler

### CPU 사용량 확인

```
CPU Timeline:
┌─────────────────────────────────────┐
│ 100% ████                           │
│  75% ████░░░░                       │
│  50% ████░░░░░░░░                   │
│  25% ████░░░░░░░░░░░░               │
│   0% ────────────────────────────   │
└─────────────────────────────────────┘
     앱 시작  스크롤  네트워크
```

### CPU Recording 시작

**1. Record 버튼 클릭**

**2. Recording 타입 선택:**

- **Sample Java Methods**: 가벼움, 대략적인 정보
- **Trace Java Methods**: 정확함, 오버헤드 있음
- **Sample C/C++ Functions**: Native 코드 프로파일링
- **Trace System Calls**: 시스템 전체 프로파일링

### Call Chart 분석

```kotlin
/**
 * Call Chart 읽는 법
 * 
 * 가로축: 시간
 * 세로축: 호출 스택 깊이
 * 
 * 넓은 블록 = 오래 실행된 메서드
 * 깊은 스택 = 많은 중첩 호출
 */

// 예시: 느린 메서드 발견
fun slowMethod() {
    // ❌ 이 메서드가 Call Chart에서 넓게 표시됨
    repeat(10000) {
        expensiveOperation()
    }
}

// 최적화 후
fun optimizedMethod() {
    // ✅ 불필요한 반복 제거
    val result = calculateOnce()
    useResult(result)
}
```

### Flame Chart 분석

```
Flame Chart (Bottom-Up):

main()                          ████████████████████ 100%
├─ onCreate()                   ████████████░░░░░░░░  60%
│  ├─ loadData()                ████████░░░░░░░░░░░░  40%
│  │  └─ parseJson()            ████████░░░░░░░░░░░░  40% ← 병목!
│  └─ setupUI()                 ████░░░░░░░░░░░░░░░░  20%
└─ onResume()                   ████████░░░░░░░░░░░░  40%
```

**분석 방법:**
1. **가장 넓은 블록 찾기** → 가장 많은 시간을 소비하는 메서드
2. **Self Time 확인** → 메서드 자체의 실행 시간
3. **Children Time 확인** → 호출한 메서드들의 실행 시간

### 실전 예제: 느린 리스트 스크롤

```kotlin
/**
 * 문제: 리스트 스크롤이 버벅임
 * 
 * CPU Profiler로 분석:
 * 1. 스크롤 중 CPU Recording 시작
 * 2. Call Chart에서 넓은 블록 찾기
 * 3. 병목 메서드 확인
 */

// ❌ 문제 코드 (CPU Profiler에서 발견)
@Composable
fun SlowListItem(item: Item) {
    // 매번 Recompose 시 복잡한 계산
    val processedData = processComplexData(item)  // ← 병목!
    
    Text(processedData)
}

// ✅ 최적화 코드
@Composable
fun FastListItem(item: Item) {
    // remember로 계산 결과 캐싱
    val processedData = remember(item) {
        processComplexData(item)
    }
    
    Text(processedData)
}

/**
 * 최적화 결과:
 * - Before: 스크롤 시 CPU 80-100%
 * - After: 스크롤 시 CPU 20-30%
 */
```

---

## Memory Profiler

### 메모리 사용량 모니터링

```
Memory Timeline:
┌─────────────────────────────────────┐
│ 200MB ████                          │
│ 150MB ████████                      │
│ 100MB ████████████                  │
│  50MB ████████████████              │
│   0MB ──────────────────────────    │
└─────────────────────────────────────┘
      앱 시작  이미지 로드  GC
```

**메모리 구성:**
- **Java**: Java/Kotlin 객체
- **Native**: Bitmap, Native 라이브러리
- **Graphics**: GPU 메모리
- **Stack**: 스레드 스택
- **Code**: 앱 코드
- **Others**: 시스템 메모리

### Heap Dump 분석

**1. Heap Dump 캡처**

Profiler → Memory → "Capture heap dump" 버튼 클릭

**2. Heap Dump 분석**

```
Class Name              | Shallow Size | Retained Size | Instances
------------------------|--------------|---------------|----------
Bitmap                  |    8,294,400 |     8,294,400 |       10
String                  |      524,288 |       524,288 |    1,000
ArrayList               |       32,768 |     1,048,576 |      100
```

**용어 설명:**
- **Shallow Size**: 객체 자체의 크기
- **Retained Size**: 객체와 참조하는 모든 객체의 크기
- **Instances**: 인스턴스 개수

### 메모리 누수 탐지

```kotlin
/**
 * 메모리 누수 탐지 방법
 * 
 * 1. Heap Dump 캡처 (시작 시점)
 * 2. 앱 사용 (화면 이동 등)
 * 3. Heap Dump 캡처 (종료 시점)
 * 4. 두 Heap Dump 비교
 */

// 예시: Activity 누수 탐지
class LeakyActivity : AppCompatActivity() {
    
    companion object {
        // ❌ Static 변수에 Activity 저장
        private var instance: LeakyActivity? = null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this  // ❌ 누수 발생!
    }
}

/**
 * Heap Dump 분석 결과:
 * 
 * LeakyActivity 인스턴스가 여러 개 존재
 * → Activity가 종료되어도 GC되지 않음
 * → 메모리 누수!
 */

// ✅ 수정: Static 변수 제거
class FixedActivity : AppCompatActivity() {
    // Static 변수 제거
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 정상적인 코드
    }
}
```

### Allocation Tracking

**실시간 메모리 할당 추적**

```kotlin
/**
 * Allocation Tracking 사용법
 * 
 * 1. Profiler → Memory → "Record allocations"
 * 2. 앱 사용 (시나리오 실행)
 * 3. Recording 중지
 * 4. 할당된 객체 분석
 */

// 예시: 불필요한 객체 생성 발견
@Composable
fun WastefulComponent() {
    // ❌ 매번 Recompose 시 새 리스트 생성
    val items = listOf("A", "B", "C")  // ← Allocation Tracking에서 발견
    
    LazyColumn {
        items(items.size) { index ->
            Text(items[index])
        }
    }
}

// ✅ 최적화: remember 사용
@Composable
fun EfficientComponent() {
    // 한 번만 생성
    val items = remember { listOf("A", "B", "C") }
    
    LazyColumn {
        items(items.size) { index ->
            Text(items[index])
        }
    }
}

/**
 * Allocation Tracking 결과:
 * - Before: 매 Recompose마다 List 할당
 * - After: 최초 1회만 할당
 */
```

### Native Memory 분석

```kotlin
/**
 * Native Memory 프로파일링
 * 
 * Bitmap은 Native Heap에 저장됨
 */

// 예시: Bitmap 메모리 분석
class BitmapMemoryTest {
    
    fun loadLargeImage(context: Context) {
        // ❌ 큰 이미지를 원본 크기로 로드
        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.large_image  // 4000x3000, 48MB
        )
        
        /**
         * Memory Profiler 결과:
         * Native Heap: +48MB
         */
    }
    
    fun loadOptimizedImage(context: Context) {
        // ✅ 다운샘플링하여 로드
        val options = BitmapFactory.Options().apply {
            inSampleSize = 4  // 1/4 크기
        }
        
        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.large_image,
            options  // 1000x750, 3MB
        )
        
        /**
         * Memory Profiler 결과:
         * Native Heap: +3MB (16배 감소!)
         */
    }
}
```

---

## Network Profiler

### 네트워크 활동 모니터링

```
Network Timeline:
┌─────────────────────────────────────┐
│ Sent     ░░░░████░░░░░░░░░░░░       │
│ Received ████████████░░░░░░░░       │
└─────────────────────────────────────┘
          API 호출  이미지 다운로드
```

### 네트워크 요청 분석

**Profiler → Network → 요청 클릭**

```
Request Details:
┌─────────────────────────────────────┐
│ URL: https://api.example.com/users  │
│ Method: GET                         │
│ Status: 200 OK                      │
│ Size: 1.2 MB                        │
│ Duration: 850 ms                    │
├─────────────────────────────────────┤
│ Timeline:                           │
│ - Waiting: 200 ms                   │
│ - Downloading: 650 ms               │
├─────────────────────────────────────┤
│ Request Headers:                    │
│ - Content-Type: application/json    │
│ - Authorization: Bearer xxx         │
├─────────────────────────────────────┤
│ Response Body:                      │
│ { "users": [...] }                  │
└─────────────────────────────────────┘
```

### 네트워크 최적화 발견

```kotlin
/**
 * 문제: 앱이 너무 많은 네트워크 요청을 보냄
 * 
 * Network Profiler로 발견:
 * - 같은 API를 여러 번 호출
 * - 불필요한 이미지 다운로드
 */

// ❌ 문제 코드
@Composable
fun UserList(viewModel: UserViewModel) {
    val users by viewModel.users.collectAsState()
    
    LazyColumn {
        items(users) { user ->
            // 각 아이템마다 프로필 이미지 요청
            UserItem(user)  // ← 100개 아이템 = 100개 요청!
        }
    }
}

// ✅ 최적화: 배칭 및 캐싱
class UserViewModel : ViewModel() {
    
    private val imageCache = LruCache<String, Bitmap>(50)
    
    /**
     * 이미지 캐싱
     */
    fun loadUserImage(url: String): Bitmap? {
        // 캐시 확인
        imageCache.get(url)?.let { return it }
        
        // 캐시 미스 → 다운로드
        val bitmap = downloadImage(url)
        imageCache.put(url, bitmap)
        return bitmap
    }
    
    /**
     * API 요청 배칭
     */
    fun loadUsers() {
        viewModelScope.launch {
            // ✅ 한 번의 요청으로 모든 사용자 로드
            val users = api.getUsers()
            _users.value = users
        }
    }
}

/**
 * Network Profiler 결과:
 * - Before: 100개 요청, 총 5초
 * - After: 1개 요청, 총 0.5초
 */
```

### 응답 크기 최적화

```kotlin
/**
 * 문제: API 응답이 너무 큼
 * 
 * Network Profiler에서 확인:
 * - Response Size: 5 MB
 * - 불필요한 필드 포함
 */

// ❌ 문제: 모든 필드 요청
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String,
    val bio: String,
    val followers: List<User>,  // ❌ 불필요
    val following: List<User>,  // ❌ 불필요
    val posts: List<Post>       // ❌ 불필요
)

// ✅ 최적화: 필요한 필드만 요청
data class UserSummary(
    val id: String,
    val name: String,
    val profileImage: String
)

// API 호출 시 필드 지정
@GET("/users")
suspend fun getUsers(
    @Query("fields") fields: String = "id,name,profileImage"
): List<UserSummary>

/**
 * Network Profiler 결과:
 * - Before: 5 MB
 * - After: 500 KB (10배 감소!)
 */
```

---

## Energy Profiler

### 배터리 사용량 분석

```
Energy Timeline:
┌─────────────────────────────────────┐
│ High   ████░░░░░░░░░░░░░░░░         │
│ Medium ░░░░████░░░░░░░░░░░░         │
│ Low    ░░░░░░░░████████████         │
└─────────────────────────────────────┘
       GPS  네트워크  대기
```

**Energy 구성:**
- **CPU**: 연산 작업
- **Network**: 네트워크 통신
- **Location**: GPS, WiFi 위치
- **Screen**: 화면 밝기

### Wake Lock 분석

```kotlin
/**
 * 문제: 앱이 배터리를 많이 소모
 * 
 * Energy Profiler로 발견:
 * - Wake Lock이 계속 유지됨
 */

// ❌ 문제 코드
class BadService : Service() {
    
    private lateinit var wakeLock: PowerManager.WakeLock
    
    override fun onCreate() {
        super.onCreate()
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BadService::WakeLock"
        )
        
        // ❌ Wake Lock 획득 후 해제 안 함
        wakeLock.acquire()
    }
    
    // onDestroy에서 해제 안 함 → 배터리 소모!
}

// ✅ 최적화: Wake Lock 적절히 관리
class GoodService : Service() {
    
    private var wakeLock: PowerManager.WakeLock? = null
    
    private fun doWork() {
        // ✅ 작업 시작 시 획득
        acquireWakeLock()
        
        try {
            performTask()
        } finally {
            // ✅ 작업 완료 시 해제
            releaseWakeLock()
        }
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "GoodService::WakeLock"
            )
        }
        // ✅ 타임아웃 설정 (10분)
        wakeLock?.acquire(10 * 60 * 1000L)
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
        releaseWakeLock()
    }
}

/**
 * Energy Profiler 결과:
 * - Before: Wake Lock 계속 유지 (High Energy)
 * - After: 필요할 때만 유지 (Low Energy)
 */
```

### GPS 사용 최적화

```kotlin
/**
 * 문제: GPS가 계속 실행됨
 * 
 * Energy Profiler로 발견:
 * - Location 섹션이 계속 High
 */

// ❌ 문제 코드
class BadLocationTracker {
    
    fun startTracking(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // ❌ 높은 정확도 + 짧은 간격
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,  // 1초마다
            0f,
            locationListener
        )
    }
}

// ✅ 최적화: 상황에 맞는 정확도
class GoodLocationTracker {
    
    fun startTracking(context: Context, scenario: LocationScenario) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        val locationRequest = when (scenario) {
            // 내비게이션: 높은 정확도
            LocationScenario.NAVIGATION -> LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000L  // 5초
            ).build()
            
            // 날씨 앱: 낮은 정확도
            LocationScenario.WEATHER -> LocationRequest.Builder(
                Priority.PRIORITY_LOW_POWER,
                30 * 60 * 1000L  // 30분
            ).build()
            
            // 일반: 균형
            LocationScenario.GENERAL -> LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                5 * 60 * 1000L  // 5분
            ).build()
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}

/**
 * Energy Profiler 결과:
 * - Before: GPS 계속 High (배터리 10%/시간)
 * - After: GPS 간헐적 Low (배터리 2%/시간)
 */
```

---

## 고급 프로파일링 도구

### Systrace

**시스템 전체 성능 분석**

```bash
# Systrace 캡처 (명령줄)
python systrace.py --time=10 -o trace.html sched freq idle am wm gfx view

# 또는 Android Studio에서
# Run → Profile 'app' → CPU → "Trace System Calls"
```

**Systrace 분석:**

```
Timeline:
┌─────────────────────────────────────────────────────┐
│ UI Thread    ████░░░░████░░░░████                   │
│ RenderThread ░░░░████░░░░████░░░░                   │
│ Background   ████████████████████                   │
└─────────────────────────────────────────────────────┘
             Frame 1  Frame 2  Frame 3
```

**주요 확인 사항:**
- **Frame 시간**: 16.67ms 이하 (60 FPS)
- **UI Thread 블로킹**: 긴 작업이 UI Thread를 차단하는지
- **RenderThread**: GPU 렌더링 시간

### Perfetto

**차세대 시스템 프로파일링 도구**

```bash
# Perfetto 캡처
adb shell perfetto \
  -c - --txt \
  -o /data/misc/perfetto-traces/trace \
  <<EOF
buffers: {
    size_kb: 63488
    fill_policy: DISCARD
}
data_sources: {
    config {
        name: "linux.ftrace"
        ftrace_config {
            ftrace_events: "sched/sched_switch"
            ftrace_events: "power/suspend_resume"
        }
    }
}
duration_ms: 10000
EOF

# 트레이스 다운로드
adb pull /data/misc/perfetto-traces/trace trace.perfetto

# 분석: https://ui.perfetto.dev
```

### Layout Inspector

**UI 계층 구조 분석**

**Android Studio → Tools → Layout Inspector**

```
Compose Hierarchy:
┌─────────────────────────────────────┐
│ Column                              │
│ ├─ TopAppBar                        │
│ ├─ LazyColumn                       │
│ │  ├─ Item 1 (Recomposed 5x)       │ ← 많이 Recompose됨!
│ │  ├─ Item 2 (Recomposed 1x)       │
│ │  └─ Item 3 (Recomposed 1x)       │
│ └─ BottomBar                        │
└─────────────────────────────────────┘
```

**Recomposition Counts 활성화:**
- Layout Inspector → "Show Recomposition Counts" 체크
- 빨간색: 많이 Recompose됨 (최적화 필요)
- 녹색: 적게 Recompose됨

### Compose Compiler Reports

**Compose 컴파일러 메트릭**

```kotlin
// build.gradle.kts
android {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                project.buildDir.absolutePath + "/compose_metrics"
        )
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                project.buildDir.absolutePath + "/compose_metrics"
        )
    }
}
```

**생성되는 리포트:**

```
build/compose_metrics/
├─ app_debug-classes.txt        # Stable/Unstable 클래스
├─ app_debug-composables.txt    # Composable 함수 정보
└─ app_debug-module.json        # 모듈 메트릭
```

**classes.txt 예시:**

```
stable class User {
  stable val id: String
  stable val name: String
}

unstable class UserList {
  unstable var users: MutableList<User>  ← Unstable!
}
```

---

## 실전 분석 사례

### 사례 1: 앱 시작 시간 최적화

**문제: 앱 시작이 느림 (5초)**

#### 1단계: CPU Profiler로 분석

```kotlin
// CPU Profiler 결과:
// onCreate()에서 3초 소요

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ❌ UI Thread에서 무거운 작업
        loadConfiguration()  // 1초
        initializeDatabase()  // 1초
        loadUserData()  // 1초
        
        setContent {
            MyApp()
        }
    }
}
```

#### 2단계: 최적화

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ UI 먼저 표시
        setContent {
            MyApp()
        }
        
        // ✅ 백그라운드에서 초기화
        lifecycleScope.launch(Dispatchers.IO) {
            loadConfiguration()
            initializeDatabase()
            loadUserData()
        }
    }
}
```

#### 3단계: 결과 측정

```
Before:
├─ onCreate: 3000ms
├─ setContent: 500ms
└─ Total: 3500ms

After:
├─ onCreate: 100ms
├─ setContent: 500ms
├─ Background init: 2000ms (비동기)
└─ Total: 600ms (5.8배 빠름!)
```

### 사례 2: 스크롤 성능 개선

**문제: 리스트 스크롤이 버벅임 (30 FPS)**

#### 1단계: Systrace로 분석

```
Frame Timeline:
┌─────────────────────────────────────┐
│ Frame 1: 25ms ████████████████████  │ ← 16.67ms 초과!
│ Frame 2: 28ms ██████████████████████│
│ Frame 3: 22ms ████████████████      │
└─────────────────────────────────────┘
```

#### 2단계: CPU Profiler로 병목 찾기

```kotlin
// CPU Profiler 결과:
// processItem()에서 시간 소요

@Composable
fun NewsItem(article: Article) {
    // ❌ 매번 Recompose 시 계산
    val summary = article.content
        .split(" ")
        .take(50)
        .joinToString(" ")  // ← 병목!
    
    Text(summary)
}
```

#### 3단계: 최적화

```kotlin
@Composable
fun NewsItem(article: Article) {
    // ✅ remember로 캐싱
    val summary = remember(article.id) {
        article.content
            .split(" ")
            .take(50)
            .joinToString(" ")
    }
    
    Text(summary)
}
```

#### 4단계: 결과 측정

```
Before:
├─ Frame Time: 25ms (40 FPS)
├─ Dropped Frames: 30%

After:
├─ Frame Time: 12ms (83 FPS)
├─ Dropped Frames: 0%
```

### 사례 3: 메모리 누수 해결

**문제: 앱 사용 시간이 길어질수록 느려짐**

#### 1단계: Memory Profiler로 분석

```
Memory Timeline:
┌─────────────────────────────────────┐
│ 200MB ░░░░░░░░░░░░░░░░░░░░░░████   │
│ 150MB ░░░░░░░░░░░░░░░░████░░░░░░   │
│ 100MB ░░░░░░░░░░████░░░░░░░░░░░░   │
│  50MB ░░░░████░░░░░░░░░░░░░░░░░░   │
└─────────────────────────────────────┘
     시작  10분  20분  30분
     
메모리가 계속 증가 → 누수 의심!
```

#### 2단계: Heap Dump 분석

```
Heap Dump 비교:
┌─────────────────────────────────────┐
│ Class          | Instances | Change │
├─────────────────────────────────────┤
│ MainActivity   |        10 |    +9  │ ← 누수!
│ Fragment       |        30 |   +27  │ ← 누수!
│ Bitmap         |       100 |   +90  │
└─────────────────────────────────────┘
```

#### 3단계: 누수 원인 찾기

```kotlin
// ❌ 문제 코드
class MainActivity : AppCompatActivity() {
    
    companion object {
        // Static 리스트에 Activity 저장
        private val activities = mutableListOf<MainActivity>()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activities.add(this)  // ❌ 누수!
    }
}
```

#### 4단계: 수정

```kotlin
// ✅ 수정: Static 변수 제거
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 정상적인 코드
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 리소스 정리
    }
}
```

#### 5단계: 결과 확인

```
After Fix:
┌─────────────────────────────────────┐
│ 100MB ████████████████████████████  │
│  75MB ████████████████████████████  │
│  50MB ████████████████████████████  │
│  25MB ████████████████████████████  │
└─────────────────────────────────────┘
     시작  10분  20분  30분
     
메모리가 안정적으로 유지됨!
```

---

## 💡 베스트 프랙티스 요약

### CPU 프로파일링
- ✅ Call Chart로 병목 메서드 찾기
- ✅ Flame Chart로 시간 분포 확인
- ✅ 불필요한 계산 제거
- ✅ remember로 결과 캐싱

### 메모리 프로파일링
- ✅ Heap Dump로 메모리 사용량 분석
- ✅ Allocation Tracking으로 불필요한 할당 찾기
- ✅ LeakCanary로 누수 탐지
- ✅ Bitmap 최적화

### 네트워크 프로파일링
- ✅ 요청 횟수 최소화
- ✅ 응답 크기 최적화
- ✅ 캐싱 전략 적용
- ✅ 배칭 구현

### 배터리 프로파일링
- ✅ Wake Lock 최소화
- ✅ GPS 사용 최적화
- ✅ 네트워크 배칭
- ✅ Doze Mode 대응

### 고급 도구
- ✅ Systrace로 시스템 전체 분석
- ✅ Perfetto로 상세 분석
- ✅ Layout Inspector로 UI 계층 확인
- ✅ Compose Compiler Reports로 안정성 확인

---

## 🎯 다음 단계

성능 프로파일링을 마스터했습니다! 이제:

1. **정기적인 프로파일링**: 주요 기능 개발 후 프로파일링
2. **CI/CD 통합**: 자동화된 성능 테스트
3. **모니터링**: Firebase Performance Monitoring 등

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Profiling! 📊
