# Android WorkManager 가이드

## 목차
1. [WorkManager란?](#workmanager란)
2. [기본 개념](#기본-개념)
3. [일회성 작업](#일회성-작업)
4. [주기적 작업](#주기적-작업)
5. [제약 조건](#제약-조건)
6. [작업 체이닝](#작업-체이닝)
7. [작업 상태 관찰](#작업-상태-관찰)
8. [고급 기능](#고급-기능)
9. [실전 예제](#실전-예제)
10. [Jetpack Compose 통합](#jetpack-compose-통합)
11. [문제 해결](#문제-해결)

---

## WorkManager란?

**WorkManager**는 Android에서 백그라운드 작업을 안정적으로 실행하기 위한 Jetpack 라이브러리입니다.

### 특징
- ✅ **보장된 실행**: 앱이 종료되어도 작업 실행
- 🔄 **재시도**: 실패 시 자동 재시도
- ⚡ **배터리 효율**: 시스템 최적화
- 📱 **호환성**: API 14+ 지원

### 사용 사례
- 📤 **데이터 업로드**: 로그, 분석 데이터
- 📥 **데이터 동기화**: 서버와 동기화
- 🗄️ **데이터베이스 정리**: 오래된 데이터 삭제
- 🖼️ **이미지 처리**: 압축, 필터 적용

---

## 기본 개념

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

### Worker 클래스

```kotlin
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * 기본 Worker
 */
class SimpleWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    /**
     * 백그라운드에서 실행될 작업
     */
    override fun doWork(): Result {
        try {
            // 작업 수행
            Log.d("SimpleWorker", "작업 시작")
            
            // 예: 데이터 업로드
            uploadData()
            
            Log.d("SimpleWorker", "작업 완료")
            
            // 성공
            return Result.success()
            
        } catch (e: Exception) {
            Log.e("SimpleWorker", "작업 실패", e)
            
            // 실패 (재시도)
            return Result.retry()
            
            // 또는 실패 (재시도 안 함)
            // return Result.failure()
        }
    }
    
    private fun uploadData() {
        // 실제 작업 로직
        Thread.sleep(2000)  // 시뮬레이션
    }
}
```

---

## 일회성 작업

### 기본 실행

```kotlin
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * 일회성 작업 실행
 */
class WorkManagerHelper(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * 간단한 작업 실행
     */
    fun executeSimpleWork() {
        // WorkRequest 생성
        val workRequest = OneTimeWorkRequestBuilder<SimpleWorker>()
            .build()
        
        // 작업 예약
        workManager.enqueue(workRequest)
        
        Log.d("WorkManager", "작업 예약됨: ${workRequest.id}")
    }
}
```

### 데이터 전달

```kotlin
import androidx.work.Data
import androidx.work.workDataOf

/**
 * 데이터를 전달하는 Worker
 */
class DataWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        // 입력 데이터 받기
        val userId = inputData.getString("user_id") ?: return Result.failure()
        val count = inputData.getInt("count", 0)
        
        Log.d("DataWorker", "User ID: $userId, Count: $count")
        
        // 작업 수행
        processData(userId, count)
        
        // 출력 데이터 생성
        val outputData = workDataOf(
            "result" to "success",
            "processed_count" to count
        )
        
        return Result.success(outputData)
    }
    
    private fun processData(userId: String, count: Int) {
        // 실제 처리 로직
    }
}

/**
 * 데이터와 함께 작업 실행
 */
fun executeWorkWithData() {
    // 입력 데이터 생성
    val inputData = workDataOf(
        "user_id" to "user123",
        "count" to 10
    )
    
    // WorkRequest 생성
    val workRequest = OneTimeWorkRequestBuilder<DataWorker>()
        .setInputData(inputData)
        .build()
    
    workManager.enqueue(workRequest)
}
```

---

## 주기적 작업

### PeriodicWorkRequest

```kotlin
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

/**
 * 주기적 작업
 */
class PeriodicSyncWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        Log.d("PeriodicSync", "주기적 동기화 시작")
        
        // 서버와 동기화
        syncWithServer()
        
        return Result.success()
    }
    
    private fun syncWithServer() {
        // 동기화 로직
    }
}

/**
 * 주기적 작업 예약
 */
fun schedulePeriodicWork() {
    // 15분마다 실행 (최소 간격: 15분)
    val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
        15, TimeUnit.MINUTES  // 반복 간격
    )
        .build()
    
    // 작업 예약 (고유 이름으로 중복 방지)
    workManager.enqueueUniquePeriodicWork(
        "periodic_sync",  // 고유 이름
        ExistingPeriodicWorkPolicy.KEEP,  // 기존 작업 유지
        periodicWorkRequest
    )
}
```

### Flex 간격

```kotlin
/**
 * Flex 간격을 사용한 주기적 작업
 */
fun scheduleFlexiblePeriodicWork() {
    // 2시간마다 실행, 마지막 30분 동안 실행 가능
    val flexWorkRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
        2, TimeUnit.HOURS,      // 반복 간격
        30, TimeUnit.MINUTES    // Flex 간격
    )
        .build()
    
    workManager.enqueueUniquePeriodicWork(
        "flexible_sync",
        ExistingPeriodicWorkPolicy.REPLACE,
        flexWorkRequest
    )
}
```

---

## 제약 조건

### Constraints

```kotlin
import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * 제약 조건이 있는 작업
 */
fun scheduleConstrainedWork() {
    // 제약 조건 설정
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)  // 네트워크 필요
        .setRequiresBatteryNotLow(true)  // 배터리 충분
        .setRequiresCharging(false)  // 충전 중 아니어도 됨
        .setRequiresDeviceIdle(false)  // 유휴 상태 불필요
        .setRequiresStorageNotLow(true)  // 저장 공간 충분
        .build()
    
    // WorkRequest 생성
    val workRequest = OneTimeWorkRequestBuilder<UploadWorker>()
        .setConstraints(constraints)
        .build()
    
    workManager.enqueue(workRequest)
}

/**
 * 업로드 Worker
 */
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        // 네트워크가 연결되고 배터리가 충분할 때만 실행됨
        uploadLargeFile()
        return Result.success()
    }
    
    private fun uploadLargeFile() {
        Log.d("UploadWorker", "대용량 파일 업로드 중...")
        // 업로드 로직
    }
}
```

---

## 작업 체이닝

### 순차 실행

```kotlin
/**
 * 작업 체이닝
 */
fun chainWorks() {
    // 작업 1: 데이터 다운로드
    val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>()
        .build()
    
    // 작업 2: 데이터 처리
    val processWork = OneTimeWorkRequestBuilder<ProcessWorker>()
        .build()
    
    // 작업 3: 데이터 업로드
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
        .build()
    
    // 순차 실행: 다운로드 → 처리 → 업로드
    workManager
        .beginWith(downloadWork)
        .then(processWork)
        .then(uploadWork)
        .enqueue()
}
```

### 병렬 + 순차 실행

```kotlin
/**
 * 복잡한 작업 체이닝
 */
fun complexChain() {
    // 병렬 작업 1, 2
    val work1 = OneTimeWorkRequestBuilder<Worker1>().build()
    val work2 = OneTimeWorkRequestBuilder<Worker2>().build()
    
    // 순차 작업 3
    val work3 = OneTimeWorkRequestBuilder<Worker3>().build()
    
    // 병렬 실행 후 순차 실행
    workManager
        .beginWith(listOf(work1, work2))  // 병렬
        .then(work3)  // work1, work2 완료 후 실행
        .enqueue()
}
```

---

## 작업 상태 관찰

### LiveData로 관찰

```kotlin
/**
 * 작업 상태 관찰
 */
fun observeWork(workId: UUID) {
    workManager.getWorkInfoByIdLiveData(workId)
        .observe(lifecycleOwner) { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> {
                    Log.d("WorkManager", "대기 중")
                }
                
                WorkInfo.State.RUNNING -> {
                    Log.d("WorkManager", "실행 중")
                }
                
                WorkInfo.State.SUCCEEDED -> {
                    Log.d("WorkManager", "성공")
                    
                    // 출력 데이터 가져오기
                    val result = workInfo.outputData.getString("result")
                    Log.d("WorkManager", "결과: $result")
                }
                
                WorkInfo.State.FAILED -> {
                    Log.e("WorkManager", "실패")
                }
                
                WorkInfo.State.CANCELLED -> {
                    Log.w("WorkManager", "취소됨")
                }
                
                else -> {}
            }
        }
}
```

### Flow로 관찰

```kotlin
/**
 * Flow로 작업 상태 관찰
 */
fun observeWorkWithFlow(workId: UUID) {
    lifecycleScope.launch {
        workManager.getWorkInfoByIdFlow(workId)
            .collect { workInfo ->
                updateUI(workInfo.state)
            }
    }
}
```

---

## 고급 기능

### 초기 지연

```kotlin
/**
 * 초기 지연
 */
fun scheduleDelayedWork() {
    val workRequest = OneTimeWorkRequestBuilder<SimpleWorker>()
        .setInitialDelay(10, TimeUnit.MINUTES)  // 10분 후 실행
        .build()
    
    workManager.enqueue(workRequest)
}
```

### 백오프 정책

```kotlin
import androidx.work.BackoffPolicy

/**
 * 재시도 정책
 */
fun scheduleWorkWithBackoff() {
    val workRequest = OneTimeWorkRequestBuilder<RetryWorker>()
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,  // 지수 백오프
            10, TimeUnit.SECONDS  // 초기 지연
        )
        .build()
    
    workManager.enqueue(workRequest)
}
```

### 태그

```kotlin
/**
 * 태그로 작업 관리
 */
fun scheduleWorkWithTags() {
    val workRequest = OneTimeWorkRequestBuilder<SimpleWorker>()
        .addTag("sync")
        .addTag("high_priority")
        .build()
    
    workManager.enqueue(workRequest)
}

/**
 * 태그로 작업 취소
 */
fun cancelWorkByTag() {
    workManager.cancelAllWorkByTag("sync")
}
```

---

## 실전 예제

### 이미지 압축 및 업로드

```kotlin
/**
 * 이미지 압축 Worker
 */
class ImageCompressWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 입력 데이터
                val imageUri = inputData.getString("image_uri") ?: return@withContext Result.failure()
                
                Log.d("ImageCompress", "이미지 압축 시작: $imageUri")
                
                // 이미지 압축
                val compressedPath = compressImage(imageUri)
                
                // 출력 데이터
                val outputData = workDataOf(
                    "compressed_path" to compressedPath
                )
                
                Result.success(outputData)
                
            } catch (e: Exception) {
                Log.e("ImageCompress", "압축 실패", e)
                Result.retry()
            }
        }
    }
    
    private fun compressImage(uri: String): String {
        // 실제 압축 로직
        Thread.sleep(2000)  // 시뮬레이션
        return "/compressed/$uri"
    }
}

/**
 * 이미지 업로드 Worker
 */
class ImageUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val compressedPath = inputData.getString("compressed_path") ?: return@withContext Result.failure()
                
                Log.d("ImageUpload", "이미지 업로드 시작: $compressedPath")
                
                // 서버에 업로드
                uploadToServer(compressedPath)
                
                Result.success()
                
            } catch (e: Exception) {
                Log.e("ImageUpload", "업로드 실패", e)
                Result.retry()
            }
        }
    }
    
    private fun uploadToServer(path: String) {
        // 실제 업로드 로직
        Thread.sleep(3000)  // 시뮬레이션
    }
}

/**
 * 이미지 처리 체인
 */
fun processAndUploadImage(imageUri: String) {
    // 제약 조건
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    
    // 압축 작업
    val compressWork = OneTimeWorkRequestBuilder<ImageCompressWorker>()
        .setInputData(workDataOf("image_uri" to imageUri))
        .build()
    
    // 업로드 작업
    val uploadWork = OneTimeWorkRequestBuilder<ImageUploadWorker>()
        .setConstraints(constraints)
        .build()
    
    // 체이닝: 압축 → 업로드
    workManager
        .beginWith(compressWork)
        .then(uploadWork)
        .enqueue()
}
```

---

## Jetpack Compose 통합

```kotlin
/**
 * Compose에서 WorkManager 사용
 */
@Composable
fun WorkManagerScreen() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    
    var workId by remember { mutableStateOf<UUID?>(null) }
    var workState by remember { mutableStateOf<WorkInfo.State?>(null) }
    
    // 작업 상태 관찰
    workId?.let { id ->
        val workInfo by workManager.getWorkInfoByIdLiveData(id)
            .observeAsState()
        
        LaunchedEffect(workInfo) {
            workState = workInfo?.state
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WorkManager 예제",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        workState?.let { state ->
            Text(
                text = "상태: ${getStateText(state)}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(onClick = {
            val workRequest = OneTimeWorkRequestBuilder<SimpleWorker>()
                .build()
            
            workManager.enqueue(workRequest)
            workId = workRequest.id
        }) {
            Text("작업 시작")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        workId?.let { id ->
            Button(onClick = {
                workManager.cancelWorkById(id)
            }) {
                Text("작업 취소")
            }
        }
    }
}

fun getStateText(state: WorkInfo.State): String {
    return when (state) {
        WorkInfo.State.ENQUEUED -> "대기 중"
        WorkInfo.State.RUNNING -> "실행 중"
        WorkInfo.State.SUCCEEDED -> "성공"
        WorkInfo.State.FAILED -> "실패"
        WorkInfo.State.CANCELLED -> "취소됨"
        else -> "알 수 없음"
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * WorkManager 문제 해결
 */
class WorkManagerTroubleshooter {
    
    /**
     * 1. 작업이 실행되지 않음
     */
    fun handleWorkNotRunning() {
        // 제약 조건 확인
        // 배터리 최적화 확인
        // Doze 모드 확인
    }
    
    /**
     * 2. 주기적 작업이 정확한 시간에 실행되지 않음
     */
    fun handleInexactTiming() {
        // 15분 미만 간격은 불가능
        // Doze 모드에서는 지연될 수 있음
        // setInitialDelay 사용
    }
    
    /**
     * 3. 작업이 중복 실행됨
     */
    fun handleDuplicateWork() {
        // enqueueUniqueWork 사용
        workManager.enqueueUniqueWork(
            "unique_work",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. CoroutineWorker 사용 (suspend 함수)
class MyWorker(context: Context, params: WorkerParameters) 
    : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // suspend 함수 사용 가능
    }
}

// 2. 제약 조건 설정
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

// 3. 고유 작업 이름 사용
workManager.enqueueUniqueWork("sync", ExistingWorkPolicy.KEEP, workRequest)

// 4. 작업 상태 관찰
workManager.getWorkInfoByIdLiveData(workId).observe(this) { }

// 5. 적절한 Result 반환
return when {
    success -> Result.success()
    shouldRetry -> Result.retry()
    else -> Result.failure()
}
```

### DON'Ts ❌

```kotlin
// 1. 15분 미만 주기적 작업
PeriodicWorkRequestBuilder<MyWorker>(5, TimeUnit.MINUTES)  // ❌

// 2. doWork()에서 UI 작업
override fun doWork(): Result {
    runOnUiThread { }  // ❌
}

// 3. 작업 취소 안 함
// ❌ 불필요한 작업 계속 실행

// 4. 너무 긴 작업
override fun doWork(): Result {
    Thread.sleep(60000)  // ❌ 10분 제한
}

// 5. 제약 조건 무시
// ❌ 네트워크 없이 업로드 시도
```

---

## 참고 자료

- [WorkManager 공식 문서](https://developer.android.com/topic/libraries/architecture/workmanager)
- [WorkManager Codelab](https://developer.android.com/codelabs/android-workmanager)
