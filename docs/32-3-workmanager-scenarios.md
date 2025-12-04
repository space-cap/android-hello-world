# WorkManager 실전 시나리오

> 📖 **시리즈 구성**
> - **32-1**: [WorkManager 기초](./32-1-workmanager-basics.md)
> - **32-2**: [WorkManager 고급 기법](./32-2-workmanager-advanced.md)
> - **32-3**: WorkManager 실전 시나리오 (현재 문서)

---

## 📚 목차

1. [데이터 동기화 시스템](#데이터-동기화-시스템)
2. [파일 업로드](#파일-업로드)
3. [주기적 백업](#주기적-백업)

---

## 데이터 동기화 시스템

```kotlin
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 1. 로컬 데이터 가져오기
            val localData = getLocalData()
            
            // 2. 서버에 업로드
            uploadToServer(localData)
            
            // 3. 서버에서 다운로드
            val serverData = downloadFromServer()
            
            // 4. 로컬에 저장
            saveToLocal(serverData)
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun getLocalData(): List<String> {
        delay(500)
        return listOf("data1", "data2")
    }
    
    private suspend fun uploadToServer(data: List<String>) {
        delay(1000)
    }
    
    private suspend fun downloadFromServer(): List<String> {
        delay(1000)
        return listOf("server1", "server2")
    }
    
    private suspend fun saveToLocal(data: List<String>) {
        delay(500)
    }
}
```

---

## 파일 업로드

```kotlin
class FileUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val filePath = inputData.getString("file_path") ?: return Result.failure()
        
        return try {
            val file = File(filePath)
            uploadFile(file)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun uploadFile(file: File) {
        // 파일 업로드 로직
        delay(2000)
    }
}
```

---

## 주기적 백업

```kotlin
fun schedulePeriodicBackup(context: Context) {
    val backupWork = PeriodicWorkRequestBuilder<BackupWorker>(
        repeatInterval = 1,
        repeatIntervalTimeUnit = TimeUnit.DAYS
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi만
                .setRequiresCharging(true)  // 충전 중
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_backup",
        ExistingPeriodicWorkPolicy.KEEP,
        backupWork
    )
}
```

---

**마지막 업데이트**: 2024-12-03
