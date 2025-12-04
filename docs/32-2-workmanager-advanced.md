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

---

## 작업 체이닝

### 순차 실행

```kotlin
fun chainWorks(context: Context) {
    // 작업 1: 데이터 다운로드
    val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>().build()
    
    // 작업 2: 데이터 처리
    val processWork = OneTimeWorkRequestBuilder<ProcessWorker>().build()
    
    // 작업 3: 데이터 업로드
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>().build()
    
    // 체인 생성: 다운로드 → 처리 → 업로드
    WorkManager.getInstance(context)
        .beginWith(downloadWork)  // 첫 번째 작업
        .then(processWork)        // 두 번째 작업
        .then(uploadWork)         // 세 번째 작업
        .enqueue()
}
```

### 병렬 + 순차 실행

```kotlin
fun complexChain(context: Context) {
    // 병렬 작업: 3개 이미지 다운로드
    val download1 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(Data.Builder().putString("url", "image1.jpg").build())
        .build()
    
    val download2 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(Data.Builder().putString("url", "image2.jpg").build())
        .build()
    
    val download3 = OneTimeWorkRequestBuilder<DownloadImageWorker>()
        .setInputData(Data.Builder().putString("url", "image3.jpg").build())
        .build()
    
    // 순차 작업: 압축 → 업로드
    val compressWork = OneTimeWorkRequestBuilder<CompressWorker>().build()
    val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>().build()
    
    // 복잡한 체인
    WorkManager.getInstance(context)
        .beginWith(listOf(download1, download2, download3))  // 병렬 실행
        .then(compressWork)  // 모두 완료 후 압축
        .then(uploadWork)    // 압축 후 업로드
        .enqueue()
}
```

---

## 제약 조건

### 다양한 제약 조건

```kotlin
import androidx.work.Constraints
import androidx.work.NetworkType

fun createConstrainedWork(context: Context) {
    val constraints = Constraints.Builder()
        // 네트워크 연결 필요
        .setRequiredNetworkType(NetworkType.CONNECTED)
        
        // 충전 중일 때만 실행
        .setRequiresCharging(true)
        
        // 배터리가 낮지 않을 때만 실행
        .setRequiresBatteryNotLow(true)
        
        // 저장 공간이 부족하지 않을 때만 실행
        .setRequiresStorageNotLow(true)
        
        .build()
    
    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
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
        
        for (i in 1..totalItems) {
            processItem(i)
            
            // 진행 상황 업데이트
            setProgress(
                Data.Builder()
                    .putInt("progress", i)
                    .putInt("total", totalItems)
                    .build()
            )
            
            delay(100)
        }
        
        return Result.success()
    }
    
    private suspend fun processItem(index: Int) {
        delay(50)
        Log.d("ProgressWorker", "아이템 $index 처리 완료")
    }
}
```

---

## 작업 관찰

### UI에서 진행 상황 관찰

```kotlin
@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    var workId by remember { mutableStateOf<UUID?>(null) }
    var progress by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(100) }
    
    fun startWork() {
        val workRequest = OneTimeWorkRequestBuilder<ProgressWorker>().build()
        workId = workRequest.id
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    workId?.let { id ->
        val workInfo by WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(id)
            .observeAsState()
        
        LaunchedEffect(workInfo) {
            workInfo?.let { info ->
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        progress = info.progress.getInt("progress", 0)
                        total = info.progress.getInt("total", 100)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        progress = total
                    }
                    else -> {}
                }
            }
        }
    }
    
    Column {
        Text("진행률: $progress / $total")
        LinearProgressIndicator(
            progress = if (total > 0) progress.toFloat() / total else 0f
        )
        Button(onClick = { startWork() }) {
            Text("작업 시작")
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-03
