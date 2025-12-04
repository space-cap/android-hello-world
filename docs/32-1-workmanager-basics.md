# WorkManager 기초

> 📖 **시리즈 구성**
> - **32-1**: WorkManager 기초 (현재 문서)
> - **32-2**: [WorkManager 고급 기법](./32-2-workmanager-advanced.md)
> - **32-3**: [WorkManager 실전 시나리오](./32-3-workmanager-scenarios.md)

---

## 📚 목차

1. [WorkManager란?](#workmanager란)
2. [기본 Worker 만들기](#기본-worker-만들기)
3. [일회성 작업](#일회성-작업)
4. [주기적 작업](#주기적-작업)
5. [데이터 전달](#데이터-전달)

---

## WorkManager란?

### 왜 WorkManager가 필요한가?

**문제 상황:**
```kotlin
// ❌ 일반 Coroutine - 앱 종료 시 작업 중단
viewModelScope.launch {
    uploadFile()  // 앱이 종료되면 업로드 중단!
}

// ❌ Service - Android 8.0+ 제한, 배터리 소모
startService(Intent(this, UploadService::class.java))
```

**WorkManager 해결:**
```kotlin
// ✅ WorkManager - 앱 종료되어도 작업 완료
val workRequest = OneTimeWorkRequestBuilder<UploadWorker>().build()
WorkManager.getInstance(context).enqueue(workRequest)
// 앱이 종료되어도, 재부팅되어도 작업 완료 보장!
```

### WorkManager 특징

```
✅ 보장된 실행: 앱 종료되어도 작업 완료
✅ 유연한 스케줄링: 즉시, 지연, 주기적
✅ 제약 조건: WiFi, 충전 중, 배터리 등
✅ 체이닝: 여러 작업 순차 실행
✅ 재시도: 실패 시 자동 재시도
```

---

## 기본 Worker 만들기

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    val workVersion = "2.9.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")
}
```

### Worker 클래스 생성

```kotlin
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

/**
 * 가장 기본적인 Worker
 * 
 * Worker는 백그라운드에서 실행되는 작업 단위입니다
 */
class SimpleWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    /**
     * doWork(): 백그라운드에서 실행될 작업
     * 
     * 이 메서드는 백그라운드 스레드에서 자동으로 실행됩니다
     */
    override fun doWork(): Result {
        Log.d("SimpleWorker", "작업 시작")
        
        try {
            // 실제 작업 수행
            Thread.sleep(2000)  // 2초 대기 (예시)
            Log.d("SimpleWorker", "작업 완료")
            
            // 성공 반환
            return Result.success()
            
        } catch (e: Exception) {
            Log.e("SimpleWorker", "작업 실패", e)
            
            // 재시도 반환
            return Result.retry()
        }
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
import kotlinx.coroutines.delay

/**
 * Coroutine을 사용하는 Worker (권장)
 * 
 * suspend 함수를 사용할 수 있어 더 편리합니다
 */
class MyCoroutineWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    /**
     * doWork()는 suspend 함수입니다
     */
    override suspend fun doWork(): Result {
        Log.d("MyCoroutineWorker", "작업 시작")
        
        return try {
            // Coroutine을 사용한 비동기 작업
            delay(2000)  // Thread.sleep 대신 delay 사용
            
            // 네트워크 요청 등
            performAsyncTask()
            
            Result.success()
            
        } catch (e: Exception) {
            Log.e("MyCoroutineWorker", "작업 실패", e)
            Result.retry()
        }
    }
    
    private suspend fun performAsyncTask() {
        // 비동기 작업
        delay(1000)
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
- 네트워크, DB 작업에 적합 ✅ 권장
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
            // 1. WorkRequest 생성
            val workRequest = OneTimeWorkRequestBuilder<MyCoroutineWorker>()
                .build()
            
            // 2. WorkManager에 작업 등록
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
import java.util.concurrent.TimeUnit

fun scheduleDelayedWork(context: Context) {
    // 30분 후 실행
    val workRequest = OneTimeWorkRequestBuilder<MyCoroutineWorker>()
        .setInitialDelay(30, TimeUnit.MINUTES)  // 지연 시간 설정
        .build()
    
    WorkManager.getInstance(context).enqueue(workRequest)
}

// 다양한 시간 단위
.setInitialDelay(1, TimeUnit.HOURS)      // 1시간
.setInitialDelay(30, TimeUnit.MINUTES)   // 30분
.setInitialDelay(10, TimeUnit.SECONDS)   // 10초
```

---

## 주기적 작업

### 기본 주기적 작업

```kotlin
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy

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
// 기존 작업 업데이트 (Android 12+)
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

## 데이터 전달

### Worker에 데이터 전달

```kotlin
import androidx.work.Data

/**
 * Worker에 데이터를 전달하는 방법
 */
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

/**
 * Worker에서 데이터 받기
 */
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

**Data 타입 제한:**
```kotlin
// 지원하는 타입:
.putString()
.putInt()
.putLong()
.putFloat()
.putDouble()
.putBoolean()
.putStringArray()
.putIntArray()
.putLongArray()

// ❌ 지원하지 않는 타입:
// - 커스텀 객체
// - List, Map 등
```

---

## 💡 베스트 프랙티스

### 1. CoroutineWorker 사용

```kotlin
// ✅ 네트워크, DB 작업은 CoroutineWorker
class NetworkWorker : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val response = api.getData()  // suspend 함수
        return Result.success()
    }
}
```

### 2. 고유 작업 이름 사용

```kotlin
// ✅ 중복 방지
WorkManager.getInstance(context).enqueueUniqueWork(
    "upload_work",  // 고유 이름
    ExistingWorkPolicy.KEEP,
    workRequest
)
```

### 3. 작업 취소

```kotlin
// 특정 작업 취소
WorkManager.getInstance(context).cancelUniqueWork("upload_work")

// 모든 작업 취소
WorkManager.getInstance(context).cancelAllWork()
```

---

## 🎯 다음 단계

WorkManager 기초를 마스터했습니다! 다음으로:

1. **[32-2. WorkManager 고급 기법](./32-2-workmanager-advanced.md)** - 체이닝, 제약 조건

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
