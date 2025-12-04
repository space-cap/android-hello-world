# WorkManager 고급 기법

> 📖 **시리즈 구성**
> - **32-1**: [WorkManager 기초](./32-1-workmanager-basics.md)
> - **32-2**: WorkManager 고급 기법 (현재 문서)
> - **32-3**: [WorkManager 실전 시나리오](./32-3-workmanager-scenarios.md)

---

## 📚 목차

1. [작업 체이닝](#작업-체이닝)
2. [제약 조건](#제약-조건)
3. [진행 상황 추적](#진행-상황-추적)
4. [작업 관찰](#작업-관찰)
5. [고급 패턴](#고급-패턴)

---

## 작업 체이닝

### 순차 실행

```kotlin
/**
 * 순차적으로 작업 실행
 * 
 * 사용 사례:
 * - 데이터 다운로드 → 처리 → 업로드
 * - 이미지 다운로드 → 압축 → 저장
 */
fun chainSequentialWorks(context: Context) {
    // 작업 1: 데이터 다운로드
    val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setInputData(
            Data.Builder()
                .putString("url", "https://api.example.com/data")
                .build()
        )
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
        .then(processWork)        // 두 번째 작업 (downloadWork 성공 후)
        .then(uploadWork)         // 세 번째 작업 (processWork 성공 후)
        .enqueue()
    
    /**
     * 중요:
     * - 이전 작업이 Result.success()를 반환해야 다음 작업 실행
     * - 하나라도 실패하면 체인 중단
     */
}

/**
 * DownloadWorker 예제
 */
class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // URL 가져오기
            val url = inputData.getString("url") ?: return Result.failure()
            
            // 데이터 다운로드
            val data = downloadData(url)
            
            // 다음 작업으로 데이터 전달
            val outputData = Data.Builder()
                .putString("downloaded_data", data)
                .build()
            
            Result.success(outputData)
            
        } catch (e: Exception) {
            Log.e("DownloadWorker", "다운로드 실패", e)
            Result.failure()
        }
    }
    
    private suspend fun downloadData(url: String): String {
        // 실제 다운로드 로직
        delay(1000)
        return "Downloaded data from $url"
    }
}

/**
 * ProcessWorker 예제
 */
class ProcessWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 이전 작업의 출력 데이터 가져오기
            val downloadedData = inputData.getString("downloaded_data")
                ?: return Result.failure()
            
            // 데이터 처리
            val processedData = processData(downloadedData)
            
            // 다음 작업으로 전달
            val outputData = Data.Builder()
                .putString("processed_data", processedData)
                .build()
            
            Result.success(outputData)
            
        } catch (e: Exception) {
            Log.e("ProcessWorker", "처리 실패", e)
            Result.failure()
        }
    }
    
    private suspend fun processData(data: String): String {
        delay(500)
        return data.uppercase()  // 예: 대문자 변환
    }
}
```

### 병렬 + 순차 실행

```kotlin
/**
 * 병렬 작업 후 순차 작업
 * 
 * 패턴:
 * ┌─ Work1 ─┐
 * ├─ Work2 ─┤→ CombineWork → UploadWork
 * └─ Work3 ─┘
 */
fun complexChain(context: Context) {
    // 병렬 작업: 3개 이미지 동시 다운로드
    val download1 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(
            Data.Builder()
                .putString("url", "https://example.com/image1.jpg")
                .putString("filename", "image1.jpg")
                .build()
        )
        .build()
    
    val download2 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(
            Data.Builder()
                .putString("url", "https://example.com/image2.jpg")
                .putString("filename", "image2.jpg")
                .build()
        )
        .build()
    
    val download3 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(
            Data.Builder()
                .putString("url", "https://example.com/image3.jpg")
                .putString("filename", "image3.jpg")
                .build()
        )
        .build()
    
    // 순차 작업: 압축 → 업로드
    val compressWork = OneTimeWorkRequestBuilder<CompressImagesWorker>().build()
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>().build()
    
    // 복잡한 체인 구성
    WorkManager.getInstance(context)
        .beginWith(listOf(download1, download2, download3))  // 병렬 실행
        .then(compressWork)  // 모든 다운로드 완료 후 압축
        .then(uploadWork)    // 압축 완료 후 업로드
        .enqueue()
    
    /**
     * 실행 순서:
     * 1. download1, download2, download3 동시 실행
     * 2. 3개 모두 성공하면 compressWork 실행
     * 3. compressWork 성공하면 uploadWork 실행
     * 
     * 주의:
     * - 병렬 작업 중 하나라도 실패하면 전체 체인 중단
     */
}

/**
 * CompressImagesWorker 예제
 */
class CompressImagesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 다운로드된 이미지 파일 찾기
            val imageFiles = findDownloadedImages()
            
            // 각 이미지 압축
            val compressedFiles = imageFiles.map { file ->
                compressImage(file)
            }
            
            // 압축된 파일 경로 전달
            val outputData = Data.Builder()
                .putStringArray("compressed_files", compressedFiles.toTypedArray())
                .build()
            
            Result.success(outputData)
            
        } catch (e: Exception) {
            Log.e("CompressWorker", "압축 실패", e)
            Result.failure()
        }
    }
    
    private fun findDownloadedImages(): List<File> {
        // 다운로드된 이미지 찾기
        return emptyList()
    }
    
    private suspend fun compressImage(file: File): String {
        delay(200)
        // 실제 압축 로직
        return file.absolutePath
    }
}
```

### 조건부 체이닝

```kotlin
/**
 * 조건에 따라 다른 작업 실행
 */
fun conditionalChain(context: Context, needsProcessing: Boolean) {
    val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>().build()
    
    val continuation = WorkManager.getInstance(context)
        .beginWith(downloadWork)
    
    // 조건에 따라 다른 작업 추가
    if (needsProcessing) {
        val processWork = OneTimeWorkRequestBuilder<ProcessWorker>().build()
        continuation.then(processWork)
    }
    
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>().build()
    continuation.then(uploadWork).enqueue()
}
```

---

## 제약 조건

### 다양한 제약 조건

```kotlin
import androidx.work.Constraints
import androidx.work.NetworkType

/**
 * 제약 조건이 있는 작업 생성
 */
fun createConstrainedWork(context: Context) {
    // 제약 조건 빌더
    val constraints = Constraints.Builder()
        // 1. 네트워크 연결 필요
        .setRequiredNetworkType(NetworkType.CONNECTED)
        
        // 2. 충전 중일 때만 실행
        .setRequiresCharging(true)
        
        // 3. 배터리가 낮지 않을 때만 실행
        .setRequiresBatteryNotLow(true)
        
        // 4. 저장 공간이 부족하지 않을 때만 실행
        .setRequiresStorageNotLow(true)
        
        // 5. 기기가 유휴 상태일 때만 실행 (Android 6.0+)
        .setRequiresDeviceIdle(true)
        
        .build()
    
    val workRequest = OneTimeWorkRequestBuilder<HeavySyncWorker>()
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
    
    /**
     * 작업 실행 시점:
     * - 모든 제약 조건이 충족될 때까지 대기
     * - 조건 충족 시 자동 실행
     * - 실행 중 조건 불충족 시 중단 후 재시도
     */
}
```

### NetworkType 옵션

```kotlin
/**
 * 네트워크 타입별 사용 사례
 */
enum class NetworkTypeUsage {
    /**
     * NOT_REQUIRED: 네트워크 불필요
     * 사용 사례: 로컬 데이터 처리, 캐시 정리
     */
    NOT_REQUIRED,
    
    /**
     * CONNECTED: 네트워크 연결 필요 (WiFi/모바일 모두)
     * 사용 사례: 일반 API 호출, 작은 파일 업로드
     */
    CONNECTED,
    
    /**
     * UNMETERED: WiFi 연결 필요 (무제한)
     * 사용 사례: 대용량 파일 다운로드, 백업
     */
    UNMETERED,
    
    /**
     * NOT_ROAMING: 로밍 아닐 때
     * 사용 사례: 데이터 동기화 (로밍 비용 절약)
     */
    NOT_ROAMING,
    
    /**
     * METERED: 모바일 데이터 연결
     * 사용 사례: 긴급 데이터 전송
     */
    METERED
}

/**
 * 실전 예제: 대용량 파일 다운로드
 */
fun downloadLargeFile(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi만
        .setRequiresCharging(true)  // 충전 중
        .setRequiresBatteryNotLow(true)  // 배터리 충분
        .build()
    
    val downloadRequest = OneTimeWorkRequestBuilder<LargeFileDownloadWorker>()
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context).enqueue(downloadRequest)
}

/**
 * 실전 예제: 긴급 데이터 동기화
 */
fun urgentSync(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)  // 모든 네트워크
        .build()
    
    val syncRequest = OneTimeWorkRequestBuilder<UrgentSyncWorker>()
        .setConstraints(constraints)
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
    
    WorkManager.getInstance(context).enqueue(syncRequest)
}
```

---

## 진행 상황 추적

### Worker에서 진행 상황 업데이트

```kotlin
/**
 * 진행 상황을 추적하는 Worker
 */
class ProgressWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val totalItems = 100
        
        // 초기 진행 상황 설정
        setProgress(
            Data.Builder()
                .putInt("progress", 0)
                .putInt("total", totalItems)
                .putString("status", "시작")
                .build()
        )
        
        try {
            for (i in 1..totalItems) {
                // 아이템 처리
                processItem(i)
                
                // 진행 상황 업데이트
                setProgress(
                    Data.Builder()
                        .putInt("progress", i)
                        .putInt("total", totalItems)
                        .putString("status", "처리 중: $i/$totalItems")
                        .build()
                )
                
                delay(100)
            }
            
            // 완료 상태
            setProgress(
                Data.Builder()
                    .putInt("progress", totalItems)
                    .putInt("total", totalItems)
                    .putString("status", "완료")
                    .build()
            )
            
            return Result.success()
            
        } catch (e: Exception) {
            // 에러 상태
            setProgress(
                Data.Builder()
                    .putString("status", "에러: ${e.message}")
                    .build()
            )
            
            return Result.failure()
        }
    }
    
    private suspend fun processItem(index: Int) {
        delay(50)
        Log.d("ProgressWorker", "아이템 $index 처리 완료")
    }
}
```

### 파일 다운로드 진행 상황

```kotlin
/**
 * 파일 다운로드 진행 상황 추적
 */
class FileDownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val filename = inputData.getString("filename") ?: "download.dat"
        
        return try {
            downloadFileWithProgress(url, filename)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun downloadFileWithProgress(url: String, filename: String) {
        // 파일 크기 가져오기
        val totalBytes = getFileSize(url)
        var downloadedBytes = 0L
        
        // 다운로드 시뮬레이션
        val chunkSize = totalBytes / 100
        
        for (i in 1..100) {
            // 청크 다운로드
            downloadChunk()
            downloadedBytes += chunkSize
            
            // 진행 상황 업데이트
            val percentage = (downloadedBytes * 100 / totalBytes).toInt()
            
            setProgress(
                Data.Builder()
                    .putLong("downloaded_bytes", downloadedBytes)
                    .putLong("total_bytes", totalBytes)
                    .putInt("percentage", percentage)
                    .putString("filename", filename)
                    .build()
            )
            
            delay(50)
        }
    }
    
    private fun getFileSize(url: String): Long = 10_000_000L  // 10MB
    
    private suspend fun downloadChunk() {
        delay(50)
    }
}
```

---

## 작업 관찰

### UI에서 진행 상황 관찰

```kotlin
/**
 * Compose UI에서 작업 진행 상황 관찰
 */
@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    var workId by remember { mutableStateOf<UUID?>(null) }
    var progress by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(100) }
    var status by remember { mutableStateOf("대기 중") }
    var workState by remember { mutableStateOf<WorkInfo.State?>(null) }
    
    /**
     * 작업 시작 함수
     */
    fun startWork() {
        val workRequest = OneTimeWorkRequestBuilder<ProgressWorker>().build()
        workId = workRequest.id
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    /**
     * 작업 관찰
     */
    workId?.let { id ->
        val workInfo by WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(id)
            .observeAsState()
        
        LaunchedEffect(workInfo) {
            workInfo?.let { info ->
                workState = info.state
                
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        progress = info.progress.getInt("progress", 0)
                        total = info.progress.getInt("total", 100)
                        status = info.progress.getString("status") ?: "실행 중"
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        progress = total
                        status = "완료"
                    }
                    WorkInfo.State.FAILED -> {
                        status = "실패"
                    }
                    WorkInfo.State.CANCELLED -> {
                        status = "취소됨"
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * UI 구성
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "작업 상태: $status",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text("진행률: $progress / $total")
        
        Spacer(Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = if (total > 0) progress.toFloat() / total else 0f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text("${(progress * 100 / total.coerceAtLeast(1))}%")
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { startWork() },
            enabled = workState != WorkInfo.State.RUNNING
        ) {
            Text("작업 시작")
        }
        
        if (workState == WorkInfo.State.RUNNING) {
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = {
                    workId?.let { id ->
                        WorkManager.getInstance(context).cancelWorkById(id)
                    }
                }
            ) {
                Text("작업 취소")
            }
        }
    }
}
```

### Flow로 작업 관찰

```kotlin
/**
 * Flow를 사용한 작업 관찰
 */
@Composable
fun FlowBasedProgressScreen() {
    val context = LocalContext.current
    var workId by remember { mutableStateOf<UUID?>(null) }
    
    /**
     * WorkInfo를 Flow로 변환
     */
    val workInfoFlow = remember(workId) {
        workId?.let { id ->
            WorkManager.getInstance(context)
                .getWorkInfoByIdFlow(id)
        } ?: flowOf(null)
    }
    
    val workInfo by workInfoFlow.collectAsState(initial = null)
    
    /**
     * 진행 상황 추출
     */
    val progress = workInfo?.progress?.getInt("progress", 0) ?: 0
    val total = workInfo?.progress?.getInt("total", 100) ?: 100
    val status = workInfo?.progress?.getString("status") ?: "대기 중"
    
    Column {
        Text("상태: $status")
        LinearProgressIndicator(
            progress = if (total > 0) progress.toFloat() / total else 0f
        )
        Button(onClick = {
            val request = OneTimeWorkRequestBuilder<ProgressWorker>().build()
            workId = request.id
            WorkManager.getInstance(context).enqueue(request)
        }) {
            Text("시작")
        }
    }
}
```

---

## 고급 패턴

### 재시도 정책

```kotlin
/**
 * 재시도 정책 설정
 */
fun createRetryableWork(context: Context) {
    val workRequest = OneTimeWorkRequestBuilder<RetryableWorker>()
        // 백오프 정책 설정
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,  // 지수 백오프
            WorkRequest.MIN_BACKOFF_MILLIS,  // 초기 대기 시간 (10초)
            TimeUnit.MILLISECONDS
        )
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
    
    /**
     * 재시도 동작:
     * - Worker가 Result.retry() 반환 시 재시도
     * - 백오프 정책에 따라 대기 후 재시도
     * - EXPONENTIAL: 10초 → 20초 → 40초 → ...
     * - LINEAR: 10초 → 20초 → 30초 → ...
     */
}

class RetryableWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 작업 수행
            performTask()
            Result.success()
            
        } catch (e: NetworkException) {
            // 네트워크 오류: 재시도
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Result.failure()
            }
            
        } catch (e: Exception) {
            // 기타 오류: 실패
            Result.failure()
        }
    }
    
    private suspend fun performTask() {
        // 작업 로직
    }
}
```

### 고유 작업 (Unique Work)

```kotlin
/**
 * 고유 작업: 중복 실행 방지
 */
fun enqueueUniqueWork(context: Context) {
    val syncWork = OneTimeWorkRequestBuilder<SyncWorker>().build()
    
    WorkManager.getInstance(context).enqueueUniqueWork(
        "daily_sync",  // 고유 이름
        ExistingWorkPolicy.KEEP,  // 기존 작업 유지
        syncWork
    )
    
    /**
     * ExistingWorkPolicy 옵션:
     * 
     * REPLACE: 기존 작업 취소하고 새 작업 시작
     * KEEP: 기존 작업이 있으면 새 작업 무시
     * APPEND: 기존 작업 뒤에 새 작업 추가
     * APPEND_OR_REPLACE: 기존 작업 실패 시 교체, 성공 시 추가
     */
}

/**
 * 고유 주기 작업
 */
fun enqueueUniquePeriodicWork(context: Context) {
    val periodicWork = PeriodicWorkRequestBuilder<BackupWorker>(
        1, TimeUnit.DAYS
    ).build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_backup",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWork
    )
}
```

---

## 💡 베스트 프랙티스 요약

### 작업 체이닝
- ✅ 작업 간 데이터 전달 활용
- ✅ 병렬 작업으로 성능 향상
- ✅ 실패 처리 고려
- ✅ 체인 복잡도 관리

### 제약 조건
- ✅ 적절한 NetworkType 선택
- ✅ 배터리 절약 고려
- ✅ 사용자 경험 우선
- ✅ 제약 조건 조합 최적화

### 진행 상황
- ✅ setProgress() 주기적 호출
- ✅ 의미 있는 진행 정보 제공
- ✅ UI 업데이트 최적화
- ✅ 에러 상태 표시

### 작업 관찰
- ✅ Flow 사용 권장
- ✅ 메모리 누수 방지
- ✅ 작업 취소 기능 제공
- ✅ 상태별 UI 처리

---

## 🎯 다음 단계

고급 기법을 마스터했습니다! 다음으로:

1. **[32-3. WorkManager 실전 시나리오](./32-3-workmanager-scenarios.md)** - 실전 예제, 트러블슈팅

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
