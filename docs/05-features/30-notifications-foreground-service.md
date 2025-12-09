# 알림 및 포그라운드 서비스 완벽 가이드

## 📚 목차

1. [알림이란?](#알림이란)
2. [기본 알림 만들기](#기본-알림-만들기)
3. [알림 채널](#알림-채널)
4. [알림 스타일](#알림-스타일)
5. [알림 액션](#알림-액션)
6. [포그라운드 서비스](#포그라운드-서비스)
7. [예약 알림](#예약-알림)
8. [실전 예제](#실전-예제)

---

## 알림이란?

> [!NOTE]
> **알림 = 사용자에게 중요한 정보를 전달하는 메시지**
> 
> **주요 유형:**
> - 📱 로컬 알림 (앱에서 직접 생성)
> - ☁️ 푸시 알림 (서버에서 전송)
> - 🔔 예약 알림 (특정 시간에 표시)
> - 🎵 포그라운드 서비스 (음악 재생 등)

### 왜 중요한가?

**사용 통계:**
- 모바일 앱의 **92%**가 알림 사용
- 알림을 통한 앱 재방문율: **88% 증가**
- 적절한 알림: 사용자 참여도 **3배 증가**

**사용 사례:**
```
메신저: 새 메시지 알림
쇼핑: 배송 상태 알림
음악: 재생 컨트롤
운동: 목표 달성 알림
```

---

## 기본 알림 만들기

### 1단계: 권한 설정 (Android 13+)

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <!-- Android 13 이상에서 알림 권한 필요 -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application>
        <activity ... />
    </application>
</manifest>
```

### 2단계: 권한 요청

```kotlin
@Composable
fun NotificationPermissionRequest(
    onPermissionGranted: () -> Unit
) {
    // Android 13 이상에서만 권한 필요
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
        
        when {
            notificationPermissionState.status.isGranted -> {
                onPermissionGranted()
            }
            notificationPermissionState.status.shouldShowRationale -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("알림 권한 필요") },
                    text = { Text("중요한 업데이트를 받으려면 알림 권한이 필요합니다.") },
                    confirmButton = {
                        Button(onClick = {
                            notificationPermissionState.launchPermissionRequest()
                        }) {
                            Text("허용")
                        }
                    }
                )
            }
        }
    } else {
        // Android 12 이하는 권한 불필요
        onPermissionGranted()
    }
}
```

### 3단계: 기본 알림 생성

```kotlin
class NotificationHelper(private val context: Context) {
    
    // NotificationManager 가져오기
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
        as NotificationManager
    
    // 기본 알림 표시
    fun showBasicNotification(
        title: String,
        message: String
    ) {
        // 알림 ID (고유 식별자)
        val notificationId = System.currentTimeMillis().toInt()
        
        // 알림 빌더 생성
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // 작은 아이콘 (필수!)
            .setContentTitle(title)  // 제목
            .setContentText(message)  // 내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 우선순위
            .setAutoCancel(true)  // 탭하면 자동으로 사라짐
            .build()
        
        // 알림 표시
        notificationManager.notify(notificationId, notification)
    }
    
    companion object {
        const val CHANNEL_ID = "default_channel"
    }
}

// 사용 예시
@Composable
fun NotificationDemoScreen() {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    
    Button(
        onClick = {
            notificationHelper.showBasicNotification(
                title = "안녕하세요!",
                message = "첫 번째 알림입니다."
            )
        }
    ) {
        Text("알림 보내기")
    }
}
```

**알림 구성 요소:**
```
┌─────────────────────────────┐
│ 🔔 [앱 아이콘] 앱 이름      │ ← Small Icon
│                             │
│ 안녕하세요!                 │ ← Title
│ 첫 번째 알림입니다.         │ ← Text
│                             │
│ 방금                        │ ← Timestamp
└─────────────────────────────┘
```

---

## 알림 채널

> [!IMPORTANT]
> **Android 8.0 이상에서는 알림 채널이 필수입니다!**
> 
> 채널이 없으면 알림이 표시되지 않습니다.

### 알림 채널이란?

**채널 = 알림을 그룹화하는 카테고리**

```
앱: 메신저
├── 채널 1: 메시지 (중요도: 높음, 소리: O)
├── 채널 2: 그룹 채팅 (중요도: 중간, 소리: O)
└── 채널 3: 프로모션 (중요도: 낮음, 소리: X)

사용자가 채널별로 설정 가능!
```

### 채널 생성

```kotlin
class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
        as NotificationManager
    
    init {
        // 앱 시작 시 채널 생성
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        // Android 8.0 이상에서만 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            
            // 채널 1: 기본 알림
            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT,  // 채널 ID
                "기본 알림",  // 채널 이름 (사용자에게 표시)
                NotificationManager.IMPORTANCE_DEFAULT  // 중요도
            ).apply {
                description = "일반적인 알림"  // 채널 설명
                enableLights(true)  // LED 표시등
                lightColor = Color.BLUE  // LED 색상
                enableVibration(true)  // 진동
                vibrationPattern = longArrayOf(0, 500, 200, 500)  // 진동 패턴
            }
            
            // 채널 2: 중요 알림
            val importantChannel = NotificationChannel(
                CHANNEL_IMPORTANT,
                "중요 알림",
                NotificationManager.IMPORTANCE_HIGH  // 높은 중요도
            ).apply {
                description = "중요한 알림 (소리 + 헤드업)"
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
            
            // 채널 3: 조용한 알림
            val silentChannel = NotificationChannel(
                CHANNEL_SILENT,
                "조용한 알림",
                NotificationManager.IMPORTANCE_LOW  // 낮은 중요도
            ).apply {
                description = "소리 없는 알림"
                setSound(null, null)  // 소리 없음
                enableVibration(false)  // 진동 없음
            }
            
            // 채널 등록
            notificationManager.createNotificationChannels(
                listOf(defaultChannel, importantChannel, silentChannel)
            )
        }
    }
    
    companion object {
        const val CHANNEL_DEFAULT = "default_channel"
        const val CHANNEL_IMPORTANT = "important_channel"
        const val CHANNEL_SILENT = "silent_channel"
    }
}
```

**중요도 레벨:**
```kotlin
IMPORTANCE_NONE:    // 알림 표시 안함
IMPORTANCE_MIN:     // 상태바에만 표시, 소리/진동 없음
IMPORTANCE_LOW:     // 상태바 + 알림창, 소리/진동 없음
IMPORTANCE_DEFAULT: // 상태바 + 알림창 + 소리
IMPORTANCE_HIGH:    // 상태바 + 알림창 + 소리 + 헤드업 (화면 위에 팝업)
```

---

## 알림 스타일

### BigTextStyle (긴 텍스트)

```kotlin
fun showBigTextNotification(
    title: String,
    shortText: String,
    longText: String
) {
    val notification = NotificationCompat.Builder(context, CHANNEL_DEFAULT)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(shortText)  // 축소 시 표시
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(longText)  // 확장 시 표시
                .setBigContentTitle("확장된 제목")  // 확장 시 제목 (선택)
                .setSummaryText("요약 텍스트")  // 하단 요약 (선택)
        )
        .build()
    
    notificationManager.notify(1, notification)
}

// 사용
showBigTextNotification(
    title = "새 메시지",
    shortText = "안녕하세요...",
    longText = "안녕하세요! 오늘 저녁에 시간 있으신가요? " +
               "같이 저녁 식사하면서 프로젝트에 대해 이야기하면 좋을 것 같아요."
)
```

### BigPictureStyle (이미지)

```kotlin
fun showBigPictureNotification(
    title: String,
    text: String,
    imageUrl: String
) {
    // 이미지 로드 (Coil 사용)
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .target { drawable ->
            val bitmap = (drawable as BitmapDrawable).bitmap
            
            val notification = NotificationCompat.Builder(context, CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setLargeIcon(bitmap)  // 큰 아이콘 (왼쪽)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)  // 큰 이미지
                        .bigLargeIcon(null)  // 확장 시 큰 아이콘 숨김
                )
                .build()
            
            notificationManager.notify(2, notification)
        }
        .build()
    
    imageLoader.enqueue(request)
}
```

### InboxStyle (여러 줄)

```kotlin
fun showInboxNotification(
    title: String,
    messages: List<String>
) {
    val inboxStyle = NotificationCompat.InboxStyle()
        .setBigContentTitle("$title (${messages.size})")
    
    // 각 메시지 추가 (최대 6개)
    messages.take(6).forEach { message ->
        inboxStyle.addLine(message)
    }
    
    if (messages.size > 6) {
        inboxStyle.setSummaryText("+${messages.size - 6}개 더")
    }
    
    val notification = NotificationCompat.Builder(context, CHANNEL_DEFAULT)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText("${messages.size}개의 새 메시지")
        .setStyle(inboxStyle)
        .build()
    
    notificationManager.notify(3, notification)
}

// 사용
showInboxNotification(
    title = "새 메시지",
    messages = listOf(
        "철수: 안녕하세요!",
        "영희: 오늘 회의 있나요?",
        "민수: 점심 같이 먹어요",
        "지영: 보고서 확인 부탁드려요"
    )
)
```

---

## 알림 액션

### 기본 액션

```kotlin
fun showNotificationWithActions(
    title: String,
    message: String
) {
    // 액션 1: 확인
    val confirmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_CONFIRM"
        putExtra("notification_id", 4)
    }
    val confirmPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        confirmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    // 액션 2: 취소
    val cancelIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_CANCEL"
        putExtra("notification_id", 4)
    }
    val cancelPendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        cancelIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    val notification = NotificationCompat.Builder(context, CHANNEL_DEFAULT)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(message)
        // 액션 추가 (최대 3개)
        .addAction(
            R.drawable.ic_check,  // 아이콘
            "확인",  // 텍스트
            confirmPendingIntent  // PendingIntent
        )
        .addAction(
            R.drawable.ic_cancel,
            "취소",
            cancelPendingIntent
        )
        .build()
    
    notificationManager.notify(4, notification)
}

// 액션 처리
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", -1)
        
        when (intent.action) {
            "ACTION_CONFIRM" -> {
                // 확인 처리
                Toast.makeText(context, "확인되었습니다", Toast.LENGTH_SHORT).show()
                
                // 알림 제거
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as NotificationManager
                notificationManager.cancel(notificationId)
            }
            "ACTION_CANCEL" -> {
                // 취소 처리
                Toast.makeText(context, "취소되었습니다", Toast.LENGTH_SHORT).show()
                
                // 알림 제거
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }
}
```

**AndroidManifest.xml에 Receiver 등록:**
```xml
<receiver
    android:name=".NotificationActionReceiver"
    android:exported="false" />
```

### 직접 답장 (Direct Reply)

```kotlin
fun showReplyNotification(
    title: String,
    message: String
) {
    // 답장 입력 설정
    val replyLabel = "답장 입력"
    val remoteInput = RemoteInput.Builder("key_text_reply")
        .setLabel(replyLabel)
        .build()
    
    // 답장 액션
    val replyIntent = Intent(context, NotificationReplyReceiver::class.java)
    val replyPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        replyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    
    val replyAction = NotificationCompat.Action.Builder(
        R.drawable.ic_reply,
        "답장",
        replyPendingIntent
    )
        .addRemoteInput(remoteInput)  // 입력 필드 추가
        .build()
    
    val notification = NotificationCompat.Builder(context, CHANNEL_DEFAULT)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(message)
        .addAction(replyAction)
        .build()
    
    notificationManager.notify(5, notification)
}

// 답장 처리
class NotificationReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 답장 텍스트 가져오기
        val replyText = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence("key_text_reply")
            ?.toString()
        
        if (replyText != null) {
            // 답장 처리
            Log.d("Notification", "답장: $replyText")
            
            // 답장 완료 알림
            val notification = NotificationCompat.Builder(context, CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText("답장이 전송되었습니다")
                .build()
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.notify(5, notification)
        }
    }
}
```

---

## 포그라운드 서비스

> [!NOTE]
> **포그라운드 서비스 = 사용자에게 보이는 서비스**
> 
> **사용 사례:**
> - 🎵 음악 재생
> - 📍 위치 추적
> - ⏱️ 운동 기록
> - 📥 파일 다운로드

### 권한 설정

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <!-- 포그라운드 서비스 권한 -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    
    <!-- Android 14 이상: 서비스 타입별 권한 -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    
    <application>
        <!-- 서비스 등록 -->
        <service
            android:name=".MusicPlayerService"
            android:foregroundServiceType="mediaPlayback"
            android:exported="false" />
    </application>
</manifest>
```

### 음악 재생 서비스 예제

```kotlin
class MusicPlayerService : Service() {
    
    private val notificationId = 100
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> startForegroundService()
            ACTION_PAUSE -> pauseMusic()
            ACTION_STOP -> stopForegroundService()
        }
        
        return START_STICKY  // 서비스가 종료되면 자동으로 재시작
    }
    
    private fun startForegroundService() {
        // 포그라운드 알림 생성
        val notification = createMusicNotification(
            title = "노래 제목",
            artist = "아티스트",
            isPlaying = true
        )
        
        // 포그라운드 서비스 시작
        startForeground(notificationId, notification)
        
        // 음악 재생 시작
        playMusic()
    }
    
    private fun createMusicNotification(
        title: String,
        artist: String,
        isPlaying: Boolean
    ): Notification {
        // 이전 곡 액션
        val previousIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val previousPendingIntent = PendingIntent.getService(
            this, 0, previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 재생/일시정지 액션
        val playPauseIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 다음 곡 액션
        val nextIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 2, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 미디어 스타일 알림
        return NotificationCompat.Builder(this, CHANNEL_MEDIA)
            .setSmallIcon(R.drawable.ic_music)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(/* 앨범 아트 */)
            // 미디어 스타일
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)  // 축소 시 표시할 액션
            )
            // 액션 추가
            .addAction(R.drawable.ic_previous, "이전", previousPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "일시정지" else "재생",
                playPausePendingIntent
            )
            .addAction(R.drawable.ic_next, "다음", nextPendingIntent)
            .setOngoing(true)  // 스와이프로 제거 불가
            .build()
    }
    
    private fun playMusic() {
        // 음악 재생 로직
        Log.d("MusicService", "음악 재생 시작")
    }
    
    private fun pauseMusic() {
        // 음악 일시정지 로직
        Log.d("MusicService", "음악 일시정지")
        
        // 알림 업데이트
        val notification = createMusicNotification(
            title = "노래 제목",
            artist = "아티스트",
            isPlaying = false
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    
    private fun stopForegroundService() {
        // 포그라운드 서비스 중지
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        const val CHANNEL_MEDIA = "media_channel"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        const val ACTION_NEXT = "ACTION_NEXT"
    }
}

// 서비스 시작
fun startMusicService(context: Context) {
    val intent = Intent(context, MusicPlayerService::class.java).apply {
        action = MusicPlayerService.ACTION_PLAY
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}
```

---

## 예약 알림

### AlarmManager 사용

```kotlin
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    // 특정 시간에 알림 예약
    fun scheduleNotification(
        hour: Int,  // 시 (0-23)
        minute: Int,  // 분 (0-59)
        title: String,
        message: String
    ) {
        // 알림 시간 설정
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // 이미 지난 시간이면 다음 날로 설정
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        // PendingIntent 생성
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 알람 설정
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,  // 절전 모드에서도 작동
            calendar.timeInMillis,
            pendingIntent
        )
        
        Log.d("Alarm", "알림 예약: ${calendar.time}")
    }
    
    // 매일 반복 알림
    fun scheduleRepeatingNotification(
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 매일 반복
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,  // 24시간마다
            pendingIntent
        )
    }
}

// 알람 수신
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "알림"
        val message = intent.getStringExtra("message") ?: ""
        
        // 알림 표시
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showBasicNotification(title, message)
    }
}
```

**AndroidManifest.xml:**
```xml
<receiver
    android:name=".AlarmReceiver"
    android:exported="false" />
```

---

## 실전 예제

### 완전한 알림 시스템

```kotlin
@Composable
fun NotificationDemoApp() {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "알림 데모",
            style = MaterialTheme.typography.titleLarge
        )
        
        // 기본 알림
        Button(
            onClick = {
                notificationHelper.showBasicNotification(
                    "기본 알림",
                    "간단한 알림입니다"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("기본 알림")
        }
        
        // 긴 텍스트 알림
        Button(
            onClick = {
                notificationHelper.showBigTextNotification(
                    "긴 텍스트",
                    "짧은 미리보기...",
                    "이것은 매우 긴 텍스트입니다. " +
                    "알림을 확장하면 전체 내용을 볼 수 있습니다."
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("긴 텍스트 알림")
        }
        
        // 액션 알림
        Button(
            onClick = {
                notificationHelper.showNotificationWithActions(
                    "확인 필요",
                    "이 작업을 계속하시겠습니까?"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("액션 알림")
        }
        
        // 예약 알림
        Button(
            onClick = {
                alarmScheduler.scheduleNotification(
                    hour = 9,
                    minute = 0,
                    title = "좋은 아침!",
                    message = "오늘도 화이팅!"
                )
                Toast.makeText(context, "오전 9시 알림 예약됨", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("오전 9시 알림 예약")
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 적절한 타이밍

```kotlin
// ✅ 중요한 이벤트에만 알림
// ✅ 사용자가 설정한 시간에 알림
// ❌ 너무 자주 알림 (스팸)
```

### 2. 명확한 내용

```kotlin
// ✅ 명확한 제목과 내용
showNotification("새 메시지", "철수: 안녕하세요!")

// ❌ 모호한 내용
showNotification("알림", "확인하세요")
```

### 3. 액션 제공

```kotlin
// ✅ 사용자가 바로 행동할 수 있게
.addAction(R.drawable.ic_reply, "답장", replyIntent)

// ❌ 액션 없이 정보만 제공
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Stay Notified! 🔔
