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
4. [이미지 처리 파이프라인](#이미지-처리-파이프라인)
5. [트러블슈팅](#트러블슈팅)

---

## 데이터 동기화 시스템

### 양방향 동기화

```kotlin
/**
 * 양방향 데이터 동기화 Worker
 * 
 * 기능:
 * 1. 로컬 변경사항 → 서버 업로드
 * 2. 서버 변경사항 → 로컬 다운로드
 * 3. 충돌 해결
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val database = AppDatabase.getInstance(context)
    private val apiService = RetrofitClient.apiService
    
    override suspend fun doWork(): Result {
        return try {
            // 1. 로컬 변경사항 가져오기
            val localChanges = database.changeDao().getPendingChanges()
            
            if (localChanges.isNotEmpty()) {
                // 2. 서버에 업로드
                uploadChanges(localChanges)
                
                // 3. 업로드 성공 시 로컬 플래그 업데이트
                database.changeDao().markAsSynced(localChanges.map { it.id })
            }
            
            // 4. 서버에서 최신 데이터 가져오기
            val lastSyncTime = getLastSyncTime()
            val serverChanges = apiService.getChanges(since = lastSyncTime)
            
            // 5. 로컬에 저장
            database.withTransaction {
                serverChanges.forEach { change ->
                    applyChange(change)
                }
            }
            
            // 6. 마지막 동기화 시간 저장
            saveLastSyncTime(System.currentTimeMillis())
            
            Result.success()
            
        } catch (e: NetworkException) {
            // 네트워크 오류: 재시도
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
            
        } catch (e: Exception) {
            Log.e("SyncWorker", "동기화 실패", e)
            Result.failure()
        }
    }
    
    /**
     * 변경사항 업로드
     */
    private suspend fun uploadChanges(changes: List<Change>) {
        val response = apiService.uploadChanges(changes)
        if (!response.isSuccessful) {
            throw Exception("업로드 실패: ${response.code()}")
        }
    }
    
    /**
     * 변경사항 적용
     */
    private suspend fun applyChange(change: ServerChange) {
        when (change.type) {
            ChangeType.INSERT -> database.itemDao().insert(change.data)
            ChangeType.UPDATE -> database.itemDao().update(change.data)
            ChangeType.DELETE -> database.itemDao().delete(change.data.id)
        }
    }
    
    private fun getLastSyncTime(): Long {
        return PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getLong("last_sync_time", 0)
    }
    
    private fun saveLastSyncTime(time: Long) {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .edit()
            .putLong("last_sync_time", time)
            .apply()
    }
}

/**
 * 동기화 스케줄링
 */
fun scheduleSyncWork(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    
    // 주기적 동기화 (15분마다)
    val periodicSync = PeriodicWorkRequestBuilder<SyncWorker>(
        15, TimeUnit.MINUTES
    )
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "periodic_sync",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicSync
    )
}
```

---

## 파일 업로드

### 대용량 파일 업로드

```kotlin
/**
 * 대용량 파일 업로드 Worker
 * 
 * 기능:
 * - 청크 단위 업로드
 * - 진행 상황 추적
 * - 재시도 로직
 */
class FileUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val KEY_FILE_PATH = "file_path"
        const val KEY_FILE_ID = "file_id"
        const val CHUNK_SIZE = 1024 * 1024  // 1MB
    }
    
    override suspend fun doWork(): Result {
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        val fileId = inputData.getString(KEY_FILE_ID) ?: UUID.randomUUID().toString()
        
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return Result.failure(
                    Data.Builder()
                        .putString("error", "파일을 찾을 수 없습니다")
                        .build()
                )
            }
            
            // 파일 업로드
            uploadFileInChunks(file, fileId)
            
            Result.success(
                Data.Builder()
                    .putString("file_id", fileId)
                    .putString("file_url", "https://example.com/files/$fileId")
                    .build()
            )
            
        } catch (e: Exception) {
            Log.e("FileUploadWorker", "업로드 실패", e)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(
                    Data.Builder()
                        .putString("error", e.message)
                        .build()
                )
            }
        }
    }
    
    /**
     * 청크 단위로 파일 업로드
     */
    private suspend fun uploadFileInChunks(file: File, fileId: String) {
        val fileSize = file.length()
        val totalChunks = (fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE
        
        file.inputStream().use { input ->
            val buffer = ByteArray(CHUNK_SIZE)
            var chunkIndex = 0
            
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead == -1) break
                
                // 청크 업로드
                val chunk = buffer.copyOf(bytesRead)
                uploadChunk(fileId, chunkIndex, chunk)
                
                chunkIndex++
                
                // 진행 상황 업데이트
                val progress = (chunkIndex * 100 / totalChunks).toInt()
                setProgress(
                    Data.Builder()
                        .putInt("progress", progress)
                        .putInt("uploaded_chunks", chunkIndex)
                        .putInt("total_chunks", totalChunks.toInt())
                        .build()
                )
            }
        }
    }
    
    /**
     * 개별 청크 업로드
     */
    private suspend fun uploadChunk(fileId: String, chunkIndex: Int, data: ByteArray) {
        val requestBody = data.toRequestBody("application/octet-stream".toMediaType())
        
        val response = RetrofitClient.apiService.uploadChunk(
            fileId = fileId,
            chunkIndex = chunkIndex,
            chunk = requestBody
        )
        
        if (!response.isSuccessful) {
            throw Exception("청크 업로드 실패: ${response.code()}")
        }
    }
}

/**
 * 파일 업로드 시작
 */
fun uploadFile(context: Context, filePath: String) {
    val uploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
        .setInputData(
            Data.Builder()
                .putString(FileUploadWorker.KEY_FILE_PATH, filePath)
                .build()
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueue(uploadRequest)
}
```

---

## 주기적 백업

### 데이터베이스 백업

```kotlin
/**
 * 데이터베이스 백업 Worker
 */
class BackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 1. 데이터베이스 파일 찾기
            val dbFile = getDatabaseFile()
            
            // 2. 백업 파일 생성
            val backupFile = createBackupFile()
            
            // 3. 데이터베이스 복사
            dbFile.copyTo(backupFile, overwrite = true)
            
            // 4. 압축
            val compressedFile = compressBackup(backupFile)
            
            // 5. 클라우드 업로드
            uploadToCloud(compressedFile)
            
            // 6. 오래된 백업 삭제
            deleteOldBackups()
            
            Result.success(
                Data.Builder()
                    .putString("backup_file", compressedFile.name)
                    .putLong("backup_size", compressedFile.length())
                    .build()
            )
            
        } catch (e: Exception) {
            Log.e("BackupWorker", "백업 실패", e)
            Result.failure()
        }
    }
    
    private fun getDatabaseFile(): File {
        return applicationContext.getDatabasePath("app_database.db")
    }
    
    private fun createBackupFile(): File {
        val backupDir = File(applicationContext.filesDir, "backups")
        backupDir.mkdirs()
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        
        return File(backupDir, "backup_$timestamp.db")
    }
    
    private suspend fun compressBackup(file: File): File {
        val compressedFile = File(file.parent, "${file.nameWithoutExtension}.zip")
        
        ZipOutputStream(FileOutputStream(compressedFile)).use { zip ->
            FileInputStream(file).use { input ->
                zip.putNextEntry(ZipEntry(file.name))
                input.copyTo(zip)
                zip.closeEntry()
            }
        }
        
        // 원본 파일 삭제
        file.delete()
        
        return compressedFile
    }
    
    private suspend fun uploadToCloud(file: File) {
        // 클라우드 스토리지에 업로드
        delay(2000)  // 시뮬레이션
    }
    
    private fun deleteOldBackups() {
        val backupDir = File(applicationContext.filesDir, "backups")
        val backups = backupDir.listFiles() ?: return
        
        // 최근 7개만 유지
        backups.sortedByDescending { it.lastModified() }
            .drop(7)
            .forEach { it.delete() }
    }
}

/**
 * 백업 스케줄링
 */
fun schedulePeriodicBackup(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi만
        .setRequiresCharging(true)  // 충전 중
        .setRequiresBatteryNotLow(true)  // 배터리 충분
        .build()
    
    val backupWork = PeriodicWorkRequestBuilder<BackupWorker>(
        1, TimeUnit.DAYS  // 매일
    )
        .setConstraints(constraints)
        .setInitialDelay(2, TimeUnit.HOURS)  // 2시간 후 시작
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_backup",
        ExistingPeriodicWorkPolicy.KEEP,
        backupWork
    )
}
```

---

## 이미지 처리 파이프라인

### 다중 이미지 처리

```kotlin
/**
 * 이미지 처리 파이프라인
 * 
 * 플로우:
 * 1. 여러 이미지 다운로드 (병렬)
 * 2. 이미지 리사이즈
 * 3. 워터마크 추가
 * 4. 압축
 * 5. 업로드
 */
fun createImageProcessingPipeline(context: Context, imageUrls: List<String>) {
    // 1단계: 이미지 다운로드 (병렬)
    val downloadWorks = imageUrls.mapIndexed { index, url ->
        OneTimeWorkRequestBuilder<DownloadImageWorker>()
            .setInputData(
                Data.Builder()
                    .putString("url", url)
                    .putInt("index", index)
                    .build()
            )
            .build()
    }
    
    // 2단계: 리사이즈
    val resizeWork = OneTimeWorkRequestBuilder<ResizeImageWorker>().build()
    
    // 3단계: 워터마크
    val watermarkWork = OneTimeWorkRequestBuilder<WatermarkWorker>().build()
    
    // 4단계: 압축
    val compressWork = OneTimeWorkRequestBuilder<CompressImageWorker>().build()
    
    // 5단계: 업로드
    val uploadWork = OneTimeWorkRequestBuilder<UploadImagesWorker>().build()
    
    // 파이프라인 구성
    WorkManager.getInstance(context)
        .beginWith(downloadWorks)  // 병렬 다운로드
        .then(resizeWork)          // 리사이즈
        .then(watermarkWork)       // 워터마크
        .then(compressWork)        // 압축
        .then(uploadWork)          // 업로드
        .enqueue()
}

/**
 * 이미지 리사이즈 Worker
 */
class ResizeImageWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 다운로드된 이미지 찾기
            val imageDir = File(applicationContext.filesDir, "downloaded_images")
            val images = imageDir.listFiles() ?: emptyArray()
            
            val resizedPaths = mutableListOf<String>()
            
            // 각 이미지 리사이즈
            images.forEach { imageFile ->
                val resizedFile = resizeImage(imageFile, 1920, 1080)
                resizedPaths.add(resizedFile.absolutePath)
            }
            
            // 다음 Worker로 경로 전달
            Result.success(
                Data.Builder()
                    .putStringArray("image_paths", resizedPaths.toTypedArray())
                    .build()
            )
            
        } catch (e: Exception) {
            Log.e("ResizeWorker", "리사이즈 실패", e)
            Result.failure()
        }
    }
    
    private suspend fun resizeImage(file: File, maxWidth: Int, maxHeight: Int): File {
        return withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            
            // 비율 계산
            val ratio = minOf(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height
            )
            
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            
            // 리사이즈
            val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            
            // 저장
            val resizedFile = File(file.parent, "resized_${file.name}")
            FileOutputStream(resizedFile).use { out ->
                resized.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            bitmap.recycle()
            resized.recycle()
            
            resizedFile
        }
    }
}
```

---

## 트러블슈팅

### 일반적인 문제와 해결

```kotlin
/**
 * 문제 1: Worker가 실행되지 않음
 * 
 * 원인:
 * - 제약 조건 미충족
 * - Doze 모드
 * - 앱 배터리 최적화
 * 
 * 해결:
 */
fun troubleshootWorkerNotRunning(context: Context) {
    // 1. 제약 조건 확인
    val workInfo = WorkManager.getInstance(context)
        .getWorkInfosByTag("my_work")
        .get()
    
    workInfo.forEach { info ->
        Log.d("WorkManager", "상태: ${info.state}")
        Log.d("WorkManager", "실행 횟수: ${info.runAttemptCount}")
        
        if (info.state == WorkInfo.State.BLOCKED) {
            Log.d("WorkManager", "제약 조건 미충족")
        }
    }
    
    // 2. 배터리 최적화 제외 요청
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

/**
 * 문제 2: Worker가 너무 자주 실패함
 * 
 * 원인:
 * - 네트워크 불안정
 * - 타임아웃
 * - 메모리 부족
 * 
 * 해결:
 */
class RobustWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 타임아웃 설정
            withTimeout(5 * 60 * 1000) {  // 5분
                performTask()
            }
            
            Result.success()
            
        } catch (e: TimeoutCancellationException) {
            Log.e("Worker", "타임아웃")
            Result.retry()
            
        } catch (e: OutOfMemoryError) {
            Log.e("Worker", "메모리 부족")
            // 메모리 정리
            System.gc()
            Result.retry()
            
        } catch (e: Exception) {
            Log.e("Worker", "에러", e)
            
            // 재시도 횟수 제한
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun performTask() {
        // 작업 로직
    }
}

/**
 * 문제 3: 작업이 중복 실행됨
 * 
 * 해결: 고유 작업 사용
 */
fun preventDuplicateWork(context: Context) {
    val work = OneTimeWorkRequestBuilder<SyncWorker>().build()
    
    // 고유 작업으로 등록
    WorkManager.getInstance(context).enqueueUniqueWork(
        "sync_work",
        ExistingWorkPolicy.KEEP,  // 기존 작업 유지
        work
    )
}
```

### 디버깅 팁

```kotlin
/**
 * WorkManager 디버깅
 */
class WorkManagerDebugger {
    
    /**
     * 모든 작업 상태 출력
     */
    fun printAllWorkStatus(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        // 모든 작업 정보 가져오기
        val workInfos = workManager.getWorkInfos(
            WorkQuery.Builder
                .fromStates(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.FAILED,
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.CANCELLED
                )
                .build()
        ).get()
        
        workInfos.forEach { info ->
            Log.d("WorkManager", """
                ID: ${info.id}
                상태: ${info.state}
                태그: ${info.tags}
                실행 횟수: ${info.runAttemptCount}
                출력 데이터: ${info.outputData}
            """.trimIndent())
        }
    }
    
    /**
     * 특정 작업 취소
     */
    fun cancelWork(context: Context, workId: UUID) {
        WorkManager.getInstance(context).cancelWorkById(workId)
    }
    
    /**
     * 모든 작업 취소
     */
    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}
```

---

## 💡 베스트 프랙티스 요약

### 데이터 동기화
- ✅ 충돌 해결 로직 구현
- ✅ 마지막 동기화 시간 추적
- ✅ 네트워크 오류 재시도
- ✅ 트랜잭션 사용

### 파일 업로드
- ✅ 청크 단위 업로드
- ✅ 진행 상황 추적
- ✅ 재시도 로직
- ✅ 네트워크 타입 제약

### 백업
- ✅ WiFi + 충전 중 제약
- ✅ 압축으로 용량 절약
- ✅ 오래된 백업 자동 삭제
- ✅ 주기적 실행

### 트러블슈팅
- ✅ 로그 활용
- ✅ 제약 조건 확인
- ✅ 재시도 횟수 제한
- ✅ 고유 작업 사용

---

## 🎯 완료!

WorkManager 시리즈를 모두 마스터했습니다! 🎉

**학습한 내용:**
1. **32-1. WorkManager 기초** - Worker, 스케줄링, 데이터 전달
2. **32-2. WorkManager 고급** - 체이닝, 제약 조건, 진행 추적
3. **32-3. 실전 시나리오** - 동기화, 업로드, 백업, 트러블슈팅

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
