# Android In-App Updates 가이드

## 목차
1. [In-App Updates란?](#in-app-updates란)
2. [업데이트 유형](#업데이트-유형)
3. [기본 구현](#기본-구현)
4. [Immediate Update](#immediate-update)
5. [Flexible Update](#flexible-update)
6. [업데이트 상태 모니터링](#업데이트-상태-모니터링)
7. [사용자 경험 최적화](#사용자-경험-최적화)
8. [실전 예제](#실전-예제)
9. [Jetpack Compose 통합](#jetpack-compose-통합)
10. [문제 해결](#문제-해결)

---

## In-App Updates란?

**In-App Updates**는 사용자가 앱을 사용하는 중에 Google Play에서 업데이트를 다운로드하고 설치할 수 있게 하는 기능입니다.

### 장점
- ✅ **사용자 편의성**: Play Store로 이동하지 않아도 됨
- ✅ **업데이트 촉진**: 최신 버전 사용률 증가
- ✅ **중요 업데이트 강제**: 보안 패치 등 필수 업데이트

### 사용 사례
- 🔒 **보안 패치**: 즉시 업데이트 필요
- 🐛 **버그 수정**: 중요한 버그 수정
- ✨ **새 기능**: 선택적 업데이트

---

## 업데이트 유형

### 1. Immediate Update (즉시 업데이트)
- 전체 화면 UI
- 업데이트 완료까지 앱 사용 불가
- 중요한 업데이트에 사용

### 2. Flexible Update (유연한 업데이트)
- 백그라운드 다운로드
- 앱 계속 사용 가능
- 다운로드 완료 후 재시작 요청
- 일반적인 업데이트에 사용

---

## 기본 구현

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
}
```

### AppUpdateManager 초기화

```kotlin
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * In-App Update 헬퍼 클래스
 */
class InAppUpdateHelper(private val activity: Activity) {
    
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    
    /**
     * 업데이트 가능 여부 확인
     */
    fun checkForUpdate(
        onUpdateAvailable: (AppUpdateInfo) -> Unit,
        onNoUpdate: () -> Unit
    ) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // 업데이트 가능
                onUpdateAvailable(appUpdateInfo)
            } else {
                // 업데이트 없음
                onNoUpdate()
            }
        }
        
        appUpdateInfoTask.addOnFailureListener { exception ->
            Log.e("InAppUpdate", "업데이트 확인 실패", exception)
        }
    }
    
    /**
     * 업데이트 타입 지원 여부 확인
     */
    fun isUpdateTypeAllowed(
        appUpdateInfo: AppUpdateInfo,
        updateType: Int
    ): Boolean {
        return appUpdateInfo.isUpdateTypeAllowed(updateType)
    }
}
```

---

## Immediate Update

### 즉시 업데이트 시작

```kotlin
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus

/**
 * 즉시 업데이트 (Immediate Update)
 */
class ImmediateUpdateManager(private val activity: Activity) {
    
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    
    companion object {
        const val IMMEDIATE_UPDATE_REQUEST_CODE = 100
    }
    
    /**
     * 즉시 업데이트 시작
     */
    fun startImmediateUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)
            ) {
                // 즉시 업데이트 시작
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    IMMEDIATE,
                    activity,
                    IMMEDIATE_UPDATE_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * 업데이트 결과 처리
     */
    fun handleUpdateResult(requestCode: Int, resultCode: Int) {
        if (requestCode == IMMEDIATE_UPDATE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    // 업데이트 성공 (앱 재시작됨)
                    Log.d("InAppUpdate", "업데이트 성공")
                }
                
                Activity.RESULT_CANCELED -> {
                    // 사용자가 업데이트 취소
                    Log.w("InAppUpdate", "사용자가 업데이트 취소")
                    
                    // 중요한 업데이트라면 다시 요청
                    startImmediateUpdate()
                }
                
                else -> {
                    // 업데이트 실패
                    Log.e("InAppUpdate", "업데이트 실패: $resultCode")
                }
            }
        }
    }
    
    /**
     * 진행 중인 업데이트 재개
     */
    fun resumeImmediateUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // 진행 중인 업데이트 재개
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    IMMEDIATE,
                    activity,
                    IMMEDIATE_UPDATE_REQUEST_CODE
                )
            }
        }
    }
}

/**
 * Activity에서 사용
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var immediateUpdateManager: ImmediateUpdateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        immediateUpdateManager = ImmediateUpdateManager(this)
        immediateUpdateManager.startImmediateUpdate()
    }
    
    override fun onResume() {
        super.onResume()
        
        // 진행 중인 업데이트 재개
        immediateUpdateManager.resumeImmediateUpdate()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        immediateUpdateManager.handleUpdateResult(requestCode, resultCode)
    }
}
```

---

## Flexible Update

### 유연한 업데이트 시작

```kotlin
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.InstallStateUpdatedListener

/**
 * 유연한 업데이트 (Flexible Update)
 */
class FlexibleUpdateManager(private val activity: Activity) {
    
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    
    companion object {
        const val FLEXIBLE_UPDATE_REQUEST_CODE = 200
    }
    
    /**
     * 설치 상태 리스너
     */
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                // 다운로드 중
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = (bytesDownloaded * 100 / totalBytesToDownload).toInt()
                
                Log.d("InAppUpdate", "다운로드 중: $progress%")
                showDownloadProgress(progress)
            }
            
            InstallStatus.DOWNLOADED -> {
                // 다운로드 완료
                Log.d("InAppUpdate", "다운로드 완료")
                showInstallPrompt()
            }
            
            InstallStatus.INSTALLING -> {
                // 설치 중
                Log.d("InAppUpdate", "설치 중")
            }
            
            InstallStatus.INSTALLED -> {
                // 설치 완료
                Log.d("InAppUpdate", "설치 완료")
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            }
            
            InstallStatus.FAILED -> {
                // 설치 실패
                Log.e("InAppUpdate", "설치 실패")
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            }
            
            InstallStatus.CANCELED -> {
                // 취소됨
                Log.w("InAppUpdate", "설치 취소")
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            }
            
            else -> {
                Log.d("InAppUpdate", "상태: ${state.installStatus()}")
            }
        }
    }
    
    /**
     * 유연한 업데이트 시작
     */
    fun startFlexibleUpdate() {
        // 리스너 등록
        appUpdateManager.registerListener(installStateUpdatedListener)
        
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)
            ) {
                // 유연한 업데이트 시작
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    FLEXIBLE,
                    activity,
                    FLEXIBLE_UPDATE_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * 다운로드 진행률 표시
     */
    private fun showDownloadProgress(progress: Int) {
        // UI 업데이트 (Snackbar, ProgressBar 등)
    }
    
    /**
     * 설치 프롬프트 표시
     */
    private fun showInstallPrompt() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "업데이트가 다운로드되었습니다.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("재시작") {
                completeUpdate()
            }
            show()
        }
    }
    
    /**
     * 업데이트 완료 (앱 재시작)
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
    
    /**
     * 리스너 해제
     */
    fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}

/**
 * Activity에서 사용
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var flexibleUpdateManager: FlexibleUpdateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        flexibleUpdateManager = FlexibleUpdateManager(this)
        flexibleUpdateManager.startFlexibleUpdate()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        flexibleUpdateManager.unregisterListener()
    }
}
```

---

## 업데이트 상태 모니터링

```kotlin
/**
 * 업데이트 상태 모니터링
 */
class UpdateStatusMonitor(private val activity: Activity) {
    
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    
    /**
     * 현재 업데이트 상태 확인
     */
    fun checkUpdateStatus() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    // 다운로드 완료, 설치 대기 중
                    showCompleteUpdatePrompt()
                }
                
                InstallStatus.DOWNLOADING -> {
                    // 다운로드 중
                    val progress = calculateProgress(appUpdateInfo)
                    showDownloadProgress(progress)
                }
                
                InstallStatus.PENDING -> {
                    // 대기 중
                }
                
                else -> {
                    // 기타 상태
                }
            }
        }
    }
    
    /**
     * 진행률 계산
     */
    private fun calculateProgress(appUpdateInfo: AppUpdateInfo): Int {
        val bytesDownloaded = appUpdateInfo.bytesDownloaded()
        val totalBytes = appUpdateInfo.totalBytesToDownload()
        
        return if (totalBytes > 0) {
            (bytesDownloaded * 100 / totalBytes).toInt()
        } else {
            0
        }
    }
    
    private fun showCompleteUpdatePrompt() {
        // 설치 완료 프롬프트 표시
    }
    
    private fun showDownloadProgress(progress: Int) {
        // 진행률 표시
    }
}
```

---

## 사용자 경험 최적화

### 1. 업데이트 우선순위 결정

```kotlin
/**
 * 업데이트 우선순위
 */
class UpdatePriority {
    
    /**
     * 버전 코드 기반 우선순위 결정
     */
    fun determineUpdateType(
        currentVersion: Int,
        availableVersion: Int
    ): Int {
        val versionDifference = availableVersion - currentVersion
        
        return when {
            versionDifference >= 5 -> IMMEDIATE  // 5버전 이상 차이: 즉시 업데이트
            versionDifference >= 2 -> FLEXIBLE   // 2-4버전 차이: 유연한 업데이트
            else -> -1  // 업데이트 안 함
        }
    }
    
    /**
     * 서버에서 우선순위 가져오기
     */
    suspend fun fetchUpdatePriority(): UpdateConfig {
        // 서버 API 호출
        // return UpdateConfig(minVersion, recommendedVersion, forceUpdate)
        return UpdateConfig(100, 105, false)
    }
}

data class UpdateConfig(
    val minVersion: Int,        // 최소 지원 버전
    val recommendedVersion: Int, // 권장 버전
    val forceUpdate: Boolean     // 강제 업데이트 여부
)
```

### 2. 업데이트 빈도 제한

```kotlin
/**
 * 업데이트 프롬프트 빈도 제한
 */
class UpdateThrottler(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LAST_PROMPT_TIME = "last_prompt_time"
        private const val PROMPT_INTERVAL_DAYS = 7  // 7일에 한 번
    }
    
    /**
     * 업데이트 프롬프트 표시 가능 여부
     */
    fun canShowUpdatePrompt(): Boolean {
        val lastPromptTime = prefs.getLong(KEY_LAST_PROMPT_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val daysSinceLastPrompt = (currentTime - lastPromptTime) / (1000 * 60 * 60 * 24)
        
        return daysSinceLastPrompt >= PROMPT_INTERVAL_DAYS
    }
    
    /**
     * 프롬프트 표시 시간 기록
     */
    fun recordPromptShown() {
        prefs.edit()
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .apply()
    }
}
```

---

## 실전 예제

### 스마트 업데이트 매니저

```kotlin
/**
 * 스마트 업데이트 매니저
 */
class SmartUpdateManager(private val activity: Activity) {
    
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val updateThrottler = UpdateThrottler(activity)
    private val updatePriority = UpdatePriority()
    
    /**
     * 업데이트 확인 및 실행
     */
    fun checkAndUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
                return@addOnSuccessListener
            }
            
            // 현재 버전
            val currentVersion = BuildConfig.VERSION_CODE
            
            // 사용 가능한 버전
            val availableVersion = appUpdateInfo.availableVersionCode()
            
            // 업데이트 타입 결정
            val updateType = updatePriority.determineUpdateType(currentVersion, availableVersion)
            
            when (updateType) {
                IMMEDIATE -> {
                    // 즉시 업데이트 (강제)
                    startImmediateUpdate(appUpdateInfo)
                }
                
                FLEXIBLE -> {
                    // 유연한 업데이트 (선택적)
                    if (updateThrottler.canShowUpdatePrompt()) {
                        startFlexibleUpdate(appUpdateInfo)
                        updateThrottler.recordPromptShown()
                    }
                }
                
                else -> {
                    // 업데이트 안 함
                }
            }
        }
    }
    
    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        if (appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                IMMEDIATE,
                activity,
                100
            )
        }
    }
    
    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        if (appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)) {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                FLEXIBLE,
                activity,
                200
            )
        }
    }
}
```

---

## Jetpack Compose 통합

```kotlin
/**
 * Compose에서 In-App Update 사용
 */
@Composable
fun InAppUpdateScreen() {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    
    var updateStatus by remember { mutableStateOf<String>("확인 중...") }
    var downloadProgress by remember { mutableStateOf(0) }
    var showInstallButton by remember { mutableStateOf(false) }
    
    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    
    val installStateUpdatedListener = remember {
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val progress = (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toInt()
                    downloadProgress = progress
                    updateStatus = "다운로드 중: $progress%"
                }
                
                InstallStatus.DOWNLOADED -> {
                    updateStatus = "다운로드 완료"
                    showInstallButton = true
                }
                
                else -> {}
            }
        }
    }
    
    DisposableEffect(Unit) {
        appUpdateManager.registerListener(installStateUpdatedListener)
        
        onDispose {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
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
            text = updateStatus,
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (downloadProgress > 0) {
            LinearProgressIndicator(
                progress = downloadProgress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (showInstallButton) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = {
                appUpdateManager.completeUpdate()
            }) {
                Text("지금 설치")
            }
        }
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * In-App Update 문제 해결
 */
class UpdateTroubleshooter {
    
    /**
     * 1. 업데이트를 찾을 수 없음
     */
    fun handleUpdateNotFound() {
        // Play Console에서 업데이트 배포 확인
        // Internal Test Track에서 테스트
        // 버전 코드가 증가했는지 확인
    }
    
    /**
     * 2. 업데이트가 시작되지 않음
     */
    fun handleUpdateNotStarting() {
        // Google Play 서비스 업데이트 확인
        // 앱이 Play Store에서 설치되었는지 확인
        // 디버그 빌드가 아닌지 확인
    }
    
    /**
     * 3. 업데이트 실패
     */
    fun handleUpdateFailed(errorCode: Int) {
        when (errorCode) {
            Activity.RESULT_CANCELED -> {
                // 사용자가 취소
                Log.w("Update", "사용자가 업데이트 취소")
            }
            
            else -> {
                // 기타 에러
                Log.e("Update", "업데이트 실패: $errorCode")
            }
        }
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. onResume에서 진행 중인 업데이트 확인
override fun onResume() {
    super.onResume()
    checkForStalledUpdate()
}

// 2. 업데이트 빈도 제한
if (updateThrottler.canShowUpdatePrompt()) {
    startFlexibleUpdate()
}

// 3. 명확한 UI 메시지
Snackbar.make(view, "새 버전이 준비되었습니다", Snackbar.LENGTH_INDEFINITE)
    .setAction("재시작") { completeUpdate() }
    .show()

// 4. 리스너 해제
override fun onDestroy() {
    appUpdateManager.unregisterListener(listener)
}

// 5. 에러 처리
appUpdateInfoTask.addOnFailureListener { exception ->
    Log.e("Update", "업데이트 확인 실패", exception)
}
```

### DON'Ts ❌

```kotlin
// 1. 너무 자주 업데이트 프롬프트 표시
// ❌ 매번 앱 시작 시 표시

// 2. 리스너 해제 안 함
// ❌ 메모리 누수

// 3. 사용자에게 선택권 없음 (Flexible에서)
// ❌ 강제로 재시작

// 4. 진행 중인 업데이트 무시
// ❌ onResume에서 확인 안 함

// 5. 에러 처리 안 함
// ❌ 실패 시 아무 조치 없음
```

---

## 참고 자료

- [In-App Updates 공식 문서](https://developer.android.com/guide/playcore/in-app-updates)
- [Play Core Library](https://developer.android.com/guide/playcore)
