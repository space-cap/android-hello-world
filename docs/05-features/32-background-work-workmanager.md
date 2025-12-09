# 백그라운드 작업 및 WorkManager 가이드

> [!NOTE]
> **이 문서는 새로운 종합 가이드 시리즈로 대체되었습니다!**
> 
> 1. **[32-1. WorkManager 기초](./32-1-workmanager-basics.md)** - Worker 만들기, 일회성/주기적 작업
> 2. **[32-2. WorkManager 고급 기법](./32-2-workmanager-advanced.md)** - 체이닝, 제약 조건, 진행 상황
> 3. **[32-3. WorkManager 실전 시나리오](./32-3-workmanager-scenarios.md)** - 동기화, 업로드, 백업

---

## 🚀 빠른 시작

**[👉 32-1. WorkManager 기초로 이동](./32-1-workmanager-basics.md)**

---

**마지막 업데이트**: 2024-12-03


---

## 백그라운드 작업이란?

> [!NOTE]
> **백그라운드 작업 = 앱이 보이지 않을 때도 실행되는 작업**
> 
> **주요 사용 사례:**
> - 📥 데이터 동기화
> - 🔄 주기적인 업데이트
> - 📤 파일 업로드
> - 🧹 캐시 정리
> - 📊 분석 데이터 전송

### 왜 WorkManager를 사용하는가?

**다른 방법들과 비교:**

```
Thread/Coroutine:
- 앱 종료 시 작업 중단
- 재부팅 후 실행 안됨
❌ 신뢰성 낮음

Service:
- Android 8.0+ 제한
- 배터리 소모 높음
❌ 권장하지 않음

AlarmManager:
- 정확한 시간 필요
- 복잡한 설정
⚠️ 특정 용도만

WorkManager:
- 앱 종료되어도 실행
- 재부팅 후에도 실행
- 배터리 효율적
- 제약 조건 설정 가능
✅ 권장!
```

**WorkManager 특징:**
```
보장된 실행: 앱이 종료되어도 작업 완료
유연한 스케줄링: 즉시, 지연, 주기적
제약 조건: WiFi, 충전 중, 배터리 등
체이닝: 여러 작업 순차 실행
재시도: 실패 시 자동 재시도
```

---

## WorkManager 시작하기

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // WorkManager
    val workVersion = "2.9.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")
}
```

### 기본 Worker 생성

```kotlin
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

// Worker 클래스 정의
class MyWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    // 백그라운드에서 실행될 작업
    override fun doWork(): Result {
        // 작업 수행
        Log.d("MyWorker", "작업 시작")
        
        try {
            // 실제 작업 (예: 데이터 동기화)
            performTask()
            
            // 성공
            return Result.success()
        } catch (e: Exception) {
            Log.e("MyWorker", "작업 실패", e)
            
            // 실패 (재시도)
            return Result.retry()
        }
    }
    
    private fun performTask() {
        // 실제 작업 로직
        Thread.sleep(2000)  // 2초 대기 (예시)
        Log.d("MyWorker", "작업 완료")
    }
}
```

**Result 타입:**
```kotlin
Result.success()  // 성공 → 작업 완료
Result.failure()  // 실패 → 작업 종료 (재시도 안함)
Result.retry()    // 재시도 → 나중에 다시 실행
```

### CoroutineWorker (권장)

```kotlin
import androidx.work.CoroutineWorker

// Coroutine을 사용하는 Worker
class MyCoroutineWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    // suspend 함수로 작업 수행
    override suspend fun doWork(): Result {
        Log.d("MyCoroutineWorker", "작업 시작")
        
        return try {
            // Coroutine을 사용한 비동기 작업
            performAsyncTask()
            
            Result.success()
        } catch (e: Exception) {
            Log.e("MyCoroutineWorker", "작업 실패", e)
            Result.retry()
        }
    }
    
    private suspend fun performAsyncTask() {
        // 네트워크 요청 등
        delay(2000)
        Log.d("MyCoroutineWorker", "비동기 작업 완료")
    }
}
```

**Worker vs CoroutineWorker:**
```
Worker:
- 동기 작업
- Thread.sleep() 사용
- 간단한 작업에 적합

CoroutineWorker:
- 비동기 작업
- suspend 함수 사용
- 네트워크, DB 작업에 적합 ✅
```

---

## 일회성 작업

### 즉시 실행

```kotlin
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

@Composable
fun WorkManagerDemo() {
    val context = LocalContext.current
    
    Button(
        onClick = {
            // WorkRequest 생성
            val workRequest = OneTimeWorkRequestBuilder<MyCoroutineWorker>()
                .build()
            
            // WorkManager에 작업 등록
            WorkManager.getInstance(context).enqueue(workRequest)
            
            Toast.makeText(context, "작업 시작됨", Toast.LENGTH_SHORT).show()
        }
    ) {
        Text("작업 시작")
    }
}
```

### 지연 실행

```kotlin
import androidx.work.OneTimeWorkRequestBuilder
import java.util.concurrent.TimeUnit

fun scheduleDelayedWork(context: Context) {
    // 30분 후 실행
    val workRequest = OneTimeWorkRequestBuilder<MyCoroutineWorker>()
        .setInitialDelay(30, TimeUnit.MINUTES)  // 지연 시간
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}
```

### 데이터 전달

```kotlin
import androidx.work.Data

// Worker에 데이터 전달
fun startWorkWithData(context: Context) {
    // 입력 데이터 생성
    val inputData = Data.Builder()
        .putString("user_id", "12345")
        .putInt("count", 10)
        .putBoolean("is_premium", true)
        .build()
    
    val workRequest = OneTimeWorkRequestBuilder<DataWorker>()
        .setInputData(inputData)  // 데이터 설정
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}

// Worker에서 데이터 받기
class DataWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        // 입력 데이터 가져오기
        val userId = inputData.getString("user_id") ?: ""
        val count = inputData.getInt("count", 0)
        val isPremium = inputData.getBoolean("is_premium", false)
        
        Log.d("DataWorker", "userId: $userId, count: $count, isPremium: $isPremium")
        
        // 작업 수행
        performTask(userId, count, isPremium)
        
        // 출력 데이터 생성
        val outputData = Data.Builder()
            .putString("result", "작업 완료")
            .putInt("processed_count", count)
            .build()
        
        return Result.success(outputData)  // 결과 데이터 반환
    }
    
    private suspend fun performTask(userId: String, count: Int, isPremium: Boolean) {
        // 실제 작업
        delay(1000)
    }
}
```

---

## 주기적 작업

### 기본 주기적 작업

```kotlin
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

fun schedulePeriodicWork(context: Context) {
    // 15분마다 실행 (최소 간격: 15분)
    val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        repeatInterval = 15,  // 간격
        repeatIntervalTimeUnit = TimeUnit.MINUTES  // 단위
    ).build()
    
    // 고유한 작업으로 등록 (중복 방지)
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "sync_work",  // 고유 이름
        ExistingPeriodicWorkPolicy.KEEP,  // 기존 작업 유지
        periodicWorkRequest
    )
}
```

**주기적 작업 정책:**
```kotlin
ExistingPeriodicWorkPolicy.KEEP:
// 기존 작업이 있으면 유지, 없으면 새로 생성

ExistingPeriodicWorkPolicy.REPLACE:
// 기존 작업 취소하고 새로 생성

ExistingPeriodicWorkPolicy.UPDATE:
// 기존 작업 업데이트
```

### 유연한 주기

```kotlin
fun scheduleFlexiblePeriodicWork(context: Context) {
    // 1시간마다 실행, 마지막 15분 동안 실행 가능
    val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        repeatInterval = 1,  // 1시간
        repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 15,  // 유연한 시간 (15분)
        flexTimeIntervalUnit = TimeUnit.MINUTES
    ).build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "flexible_sync",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest
    )
}
```

**유연한 주기 설명:**
```
1시간 주기, 15분 유연:

0분                45분              60분
|------------------|===============|
   대기 시간          실행 가능 시간

→ 45~60분 사이에 실행
→ 배터리 효율적!
```

---

## 작업 체이닝

### 순차 실행

```kotlin
fun chainWorks(context: Context) {
    // 작업 1: 데이터 다운로드
    val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>()
        .build()
    
    // 작업 2: 데이터 처리
    val processWork = OneTimeWorkRequestBuilder<ProcessWorker>()
        .build()
    
    // 작업 3: 데이터 업로드
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
        .build()
    
    // 체인 생성: 다운로드 → 처리 → 업로드
    WorkManager.getInstance(context)
        .beginWith(downloadWork)  // 첫 번째 작업
        .then(processWork)        // 두 번째 작업
        .then(uploadWork)         // 세 번째 작업
        .enqueue()
}
```

**작업 흐름:**
```
DownloadWorker (성공)
    ↓
ProcessWorker (성공)
    ↓
UploadWorker (성공)
    ↓
완료!

만약 중간에 실패하면?
DownloadWorker (성공)
    ↓
ProcessWorker (실패)
    ↓
중단! (UploadWorker 실행 안됨)
```

### 병렬 + 순차 실행

```kotlin
fun complexChain(context: Context) {
    // 병렬 작업 1: 이미지 다운로드
    val downloadImage1 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(Data.Builder().putString("url", "image1.jpg").build())
        .build()
    
    // 병렬 작업 2: 이미지 다운로드
    val downloadImage2 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(Data.Builder().putString("url", "image2.jpg").build())
        .build()
    
    // 병렬 작업 3: 이미지 다운로드
    val downloadImage3 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(Data.Builder().putString("url", "image3.jpg").build())
        .build()
    
    // 순차 작업: 이미지 압축
    val compressWork = OneTimeWorkRequestBuilder<CompressWorker>()
        .build()
    
    // 순차 작업: 업로드
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
        .build()
    
    // 복잡한 체인: 3개 병렬 → 압축 → 업로드
    WorkManager.getInstance(context)
        .beginWith(listOf(downloadImage1, downloadImage2, downloadImage3))  // 병렬 실행
        .then(compressWork)  // 모두 완료 후 압축
        .then(uploadWork)    // 압축 후 업로드
        .enqueue()
}
```

**작업 흐름:**
```
┌─ DownloadImage1 ─┐
├─ DownloadImage2 ─┤ (병렬)
└─ DownloadImage3 ─┘
         ↓
   CompressWorker (모두 완료 후)
         ↓
    UploadWorker
         ↓
       완료!
```

---

## 제약 조건

### 다양한 제약 조건

```kotlin
import androidx.work.Constraints
import androidx.work.NetworkType

fun createConstrainedWork(context: Context) {
    // 제약 조건 설정
    val constraints = Constraints.Builder()
        // 네트워크 연결 필요
        .setRequiredNetworkType(NetworkType.CONNECTED)
        
        // WiFi 연결 필요 (데이터 절약)
        // .setRequiredNetworkType(NetworkType.UNMETERED)
        
        // 충전 중일 때만 실행
        .setRequiresCharging(true)
        
        // 배터리가 낮지 않을 때만 실행
        .setRequiresBatteryNotLow(true)
        
        // 저장 공간이 부족하지 않을 때만 실행
        .setRequiresStorageNotLow(true)
        
        // 기기가 유휴 상태일 때만 실행 (Android 6.0+)
        .setRequiresDeviceIdle(true)
        
        .build()
    
    // 제약 조건이 있는 작업
    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)  // 제약 조건 적용
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}
```

**NetworkType 옵션:**
```kotlin
NetworkType.NOT_REQUIRED:  // 네트워크 불필요
NetworkType.CONNECTED:     // 네트워크 연결 필요 (WiFi/모바일)
NetworkType.UNMETERED:     // WiFi 연결 필요 (무제한)
NetworkType.NOT_ROAMING:   // 로밍 아닐 때
NetworkType.METERED:       // 모바일 데이터 연결
```

### 실용적인 제약 조건 예시

```kotlin
// 대용량 파일 다운로드 (WiFi + 충전 중)
fun scheduleLargeDownload(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi만
        .setRequiresCharging(true)  // 충전 중
        .setRequiresBatteryNotLow(true)  // 배터리 충분
        .build()
    
    val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}

// 일반 데이터 동기화 (네트워크만)
fun scheduleSync(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)  // 네트워크만
        .build()
    
    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}
```

---

## 진행 상황 추적

### Worker에서 진행 상황 업데이트

```kotlin
class ProgressWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val totalItems = 100
        
        // 각 아이템 처리
        for (i in 1..totalItems) {
            // 작업 수행
            processItem(i)
            
            // 진행 상황 업데이트
            setProgress(
                Data.Builder()
                    .putInt("progress", i)
                    .putInt("total", totalItems)
                    .build()
            )
            
            delay(100)  // 시뮬레이션
        }
        
        return Result.success()
    }
    
    private suspend fun processItem(index: Int) {
        // 실제 작업
        delay(50)
        Log.d("ProgressWorker", "아이템 $index 처리 완료")
    }
}
```

### UI에서 진행 상황 관찰

```kotlin
@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    var workId by remember { mutableStateOf<UUID?>(null) }
    var progress by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(100) }
    var isRunning by remember { mutableStateOf(false) }
    
    // 작업 시작
    fun startWork() {
        val workRequest = OneTimeWorkRequestBuilder<ProgressWorker>()
            .build()
        
        workId = workRequest.id
        WorkManager.getInstance(context).enqueue(workRequest)
        isRunning = true
    }
    
    // 진행 상황 관찰
    workId?.let { id ->
        val workInfo by WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(id)
            .observeAsState()
        
        LaunchedEffect(workInfo) {
            workInfo?.let { info ->
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        // 진행 상황 가져오기
                        progress = info.progress.getInt("progress", 0)
                        total = info.progress.getInt("total", 100)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        isRunning = false
                        progress = total
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        isRunning = false
                    }
                    else -> {}
                }
            }
        }
    }
    
    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 진행률 표시
        Text(
            text = "진행률: $progress / $total",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 프로그레스 바
        LinearProgressIndicator(
            progress = if (total > 0) progress.toFloat() / total else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${(progress.toFloat() / total * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 시작 버튼
        Button(
            onClick = { startWork() },
            enabled = !isRunning
        ) {
            Text(if (isRunning) "실행 중..." else "작업 시작")
        }
    }
}
```

---

## 실전 예제

### 완전한 데이터 동기화 시스템

```kotlin
// 동기화 Worker
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "동기화 시작")
        
        return try {
            // 1. 로컬 데이터 가져오기
            val localData = getLocalData()
            
            // 2. 서버에 업로드
            uploadToServer(localData)
            
            // 3. 서버에서 다운로드
            val serverData = downloadFromServer()
            
            // 4. 로컬에 저장
            saveToLocal(serverData)
            
            // 5. 성공 알림
            showNotification("동기화 완료", "${serverData.size}개 항목 동기화됨")
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "동기화 실패", e)
            
            // 재시도 (최대 3회)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                showNotification("동기화 실패", "나중에 다시 시도해주세요")
                Result.failure()
            }
        }
    }
    
    private suspend fun getLocalData(): List<String> {
        delay(500)
        return listOf("data1", "data2", "data3")
    }
    
    private suspend fun uploadToServer(data: List<String>) {
        delay(1000)
        Log.d("SyncWorker", "업로드 완료: ${data.size}개")
    }
    
    private suspend fun downloadFromServer(): List<String> {
        delay(1000)
        return listOf("server1", "server2", "server3", "server4")
    }
    
    private suspend fun saveToLocal(data: List<String>) {
        delay(500)
        Log.d("SyncWorker", "저장 완료: ${data.size}개")
    }
    
    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        
        val notification = NotificationCompat.Builder(applicationContext, "sync_channel")
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        notificationManager.notify(1, notification)
    }
}

// 동기화 스케줄러
class SyncScheduler(private val context: Context) {
    
    // 즉시 동기화
    fun syncNow() {
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    // 주기적 동기화 (1시간마다)
    fun schedulePeriodicSync() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
    
    // 동기화 취소
    fun cancelSync() {
        WorkManager.getInstance(context).cancelUniqueWork("periodic_sync")
    }
}

// UI
@Composable
fun SyncScreen() {
    val context = LocalContext.current
    val syncScheduler = remember { SyncScheduler(context) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "데이터 동기화",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 즉시 동기화
        Button(
            onClick = { syncScheduler.syncNow() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Sync, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("지금 동기화")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 자동 동기화 설정
        Button(
            onClick = { syncScheduler.schedulePeriodicSync() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Schedule, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("자동 동기화 활성화")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 자동 동기화 취소
        OutlinedButton(
            onClick = { syncScheduler.cancelSync() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Cancel, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("자동 동기화 비활성화")
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 적절한 Worker 선택

```kotlin
// ✅ 네트워크/DB 작업 → CoroutineWorker
class ApiWorker : CoroutineWorker(...)

// ✅ 간단한 작업 → Worker
class SimpleWorker : Worker(...)
```

### 2. 제약 조건 활용

```kotlin
// ✅ 대용량 다운로드 → WiFi + 충전
.setRequiredNetworkType(NetworkType.UNMETERED)
.setRequiresCharging(true)

// ✅ 일반 동기화 → 네트워크만
.setRequiredNetworkType(NetworkType.CONNECTED)
```

### 3. 재시도 정책

```kotlin
// ✅ 재시도 횟수 제한
if (runAttemptCount < 3) {
    Result.retry()
} else {
    Result.failure()
}

// ✅ 백오프 정책
.setBackoffCriteria(
    BackoffPolicy.EXPONENTIAL,
    WorkRequest.MIN_BACKOFF_MILLIS,
    TimeUnit.MILLISECONDS
)
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Work Smart, Not Hard! ⚙️
