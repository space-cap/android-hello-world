# Android Media Player & Audio 가이드

## 목차
1. [미디어 재생이란?](#미디어-재생이란)
2. [ExoPlayer](#exoplayer)
3. [MediaPlayer](#mediaplayer)
4. [오디오 포커스](#오디오-포커스)
5. [백그라운드 재생](#백그라운드-재생)
6. [미디어 세션](#미디어-세션)
7. [오디오 녹음](#오디오-녹음)
8. [비디오 스트리밍](#비디오-스트리밍)
9. [Picture-in-Picture](#picture-in-picture)
10. [실전 예제](#실전-예제)

---

## 미디어 재생이란?

**미디어 재생**은 오디오나 비디오 파일을 재생하는 기능입니다.

### 사용 사례
- 🎵 **음악 플레이어**: Spotify, YouTube Music
- 🎬 **비디오 플레이어**: Netflix, YouTube
- 📻 **라디오/팟캐스트**: 스트리밍 재생
- 🎮 **게임**: 배경음악, 효과음
- 📞 **통화**: VoIP, 음성 메시지

---

## ExoPlayer

### ExoPlayer란?
Google이 개발한 강력한 미디어 플레이어 라이브러리입니다.

**장점**:
- 다양한 포맷 지원 (MP4, MP3, HLS, DASH 등)
- 적응형 스트리밍
- 커스터마이징 가능
- MediaPlayer보다 성능 우수

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")
    
    // HLS 지원 (선택)
    implementation("androidx.media3:media3-exoplayer-hls:1.2.0")
    
    // DASH 지원 (선택)
    implementation("androidx.media3:media3-exoplayer-dash:1.2.0")
}
```

### 기본 사용법

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/**
 * ExoPlayer 기본 사용
 */
class AudioPlayerActivity : AppCompatActivity() {
    
    private var player: ExoPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ExoPlayer 초기화
        player = ExoPlayer.Builder(this).build()
        
        // 미디어 아이템 생성
        val mediaItem = MediaItem.fromUri("https://example.com/audio.mp3")
        
        // 미디어 설정
        player?.setMediaItem(mediaItem)
        
        // 준비
        player?.prepare()
        
        // 재생
        player?.play()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 리소스 해제
        player?.release()
        player = null
    }
}
```

### UI 연결

```kotlin
/**
 * PlayerView와 연결
 */
@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("https://example.com/video.mp4")
            setMediaItem(mediaItem)
            prepare()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
    
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

### 재생 제어

```kotlin
/**
 * 재생 제어 예제
 */
class PlayerController(private val player: ExoPlayer) {
    
    /**
     * 재생/일시정지
     */
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
    
    /**
     * 정지
     */
    fun stop() {
        player.stop()
    }
    
    /**
     * 특정 위치로 이동
     */
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }
    
    /**
     * 앞으로 10초
     */
    fun seekForward() {
        val newPosition = player.currentPosition + 10000
        player.seekTo(newPosition.coerceAtMost(player.duration))
    }
    
    /**
     * 뒤로 10초
     */
    fun seekBackward() {
        val newPosition = player.currentPosition - 10000
        player.seekTo(newPosition.coerceAtLeast(0))
    }
    
    /**
     * 볼륨 조절 (0.0 ~ 1.0)
     */
    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }
    
    /**
     * 재생 속도 조절
     */
    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)  // 0.5x, 1.0x, 1.5x, 2.0x 등
    }
}
```

### 재생 상태 모니터링

```kotlin
import androidx.media3.common.Player

/**
 * 재생 상태 리스너
 */
class PlayerEventListener : Player.Listener {
    
    /**
     * 재생 상태 변경
     */
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                Log.d("Player", "대기 중")
            }
            Player.STATE_BUFFERING -> {
                Log.d("Player", "버퍼링 중")
            }
            Player.STATE_READY -> {
                Log.d("Player", "재생 준비 완료")
            }
            Player.STATE_ENDED -> {
                Log.d("Player", "재생 종료")
            }
        }
    }
    
    /**
     * 재생/일시정지 상태 변경
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            Log.d("Player", "재생 중")
        } else {
            Log.d("Player", "일시정지")
        }
    }
    
    /**
     * 에러 발생
     */
    override fun onPlayerError(error: PlaybackException) {
        Log.e("Player", "재생 오류: ${error.message}")
    }
}

// 리스너 등록
player.addListener(PlayerEventListener())
```

---

## MediaPlayer

### 기본 사용법

```kotlin
import android.media.MediaPlayer

/**
 * MediaPlayer 사용 예제
 */
class SimpleAudioPlayer {
    
    private var mediaPlayer: MediaPlayer? = null
    
    /**
     * URL에서 재생
     */
    fun playFromUrl(url: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()  // 비동기 준비
            
            setOnPreparedListener {
                start()  // 준비 완료 후 재생
            }
            
            setOnCompletionListener {
                Log.d("MediaPlayer", "재생 완료")
            }
            
            setOnErrorListener { mp, what, extra ->
                Log.e("MediaPlayer", "에러: what=$what, extra=$extra")
                true
            }
        }
    }
    
    /**
     * 리소스에서 재생
     */
    fun playFromResource(context: Context, resId: Int) {
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.start()
    }
    
    /**
     * 일시정지
     */
    fun pause() {
        mediaPlayer?.pause()
    }
    
    /**
     * 재개
     */
    fun resume() {
        mediaPlayer?.start()
    }
    
    /**
     * 정지
     */
    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
    }
    
    /**
     * 리소스 해제
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
```

---

## 오디오 포커스

### 오디오 포커스란?
여러 앱이 동시에 소리를 재생하지 않도록 조율하는 시스템입니다.

```kotlin
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

/**
 * 오디오 포커스 관리
 */
class AudioFocusManager(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    /**
     * 오디오 포커스 요청
     */
    fun requestAudioFocus(onFocusChange: (Int) -> Unit): Boolean {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener { focusChange ->
                onFocusChange(focusChange)
            }
            .build()
        
        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    
    /**
     * 오디오 포커스 해제
     */
    fun abandonAudioFocus() {
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
    }
}

/**
 * 오디오 포커스 변경 처리
 */
fun handleAudioFocusChange(focusChange: Int, player: ExoPlayer) {
    when (focusChange) {
        AudioManager.AUDIOFOCUS_GAIN -> {
            // 포커스 획득: 재생 재개
            player.play()
            player.volume = 1.0f
        }
        
        AudioManager.AUDIOFOCUS_LOSS -> {
            // 포커스 영구 손실: 재생 중지
            player.pause()
        }
        
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
            // 포커스 일시 손실: 일시정지
            player.pause()
        }
        
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
            // 포커스 일시 손실 (볼륨 낮춤 가능): 볼륨 낮춤
            player.volume = 0.3f
        }
    }
}
```

---

## 백그라운드 재생

### Foreground Service

```kotlin
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * 백그라운드 음악 재생 서비스
 */
class MusicService : Service() {
    
    private var player: ExoPlayer? = null
    
    companion object {
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // ExoPlayer 초기화
        player = ExoPlayer.Builder(this).build()
        
        // 알림 채널 생성
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Service 시작
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // 음악 재생
        val mediaItem = MediaItem.fromUri("https://example.com/music.mp3")
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
    
    /**
     * 알림 채널 생성
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "음악 재생",
            NotificationManager.IMPORTANCE_LOW
        )
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * 알림 생성
     */
    private fun createNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("음악 재생 중")
            .setContentText("노래 제목")
            .setSmallIcon(R.drawable.ic_music)
            .build()
    }
}
```

**AndroidManifest.xml**:
```xml
<manifest>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
    
    <application>
        <service
            android:name=".MusicService"
            android:foregroundServiceType="mediaPlayback"
            android:exported="false"/>
    </application>
</manifest>
```

---

## 미디어 세션

```kotlin
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * 미디어 세션 서비스
 */
class PlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    
    override fun onCreate() {
        super.onCreate()
        
        val player = ExoPlayer.Builder(this).build()
        
        mediaSession = MediaSession.Builder(this, player).build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
```

---

## 오디오 녹음

```kotlin
import android.media.MediaRecorder
import java.io.File

/**
 * 오디오 녹음
 */
class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    
    /**
     * 녹음 시작
     */
    fun startRecording() {
        // 출력 파일 생성
        outputFile = File(context.getExternalFilesDir(null), "recording_${System.currentTimeMillis()}.m4a")
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile!!.absolutePath)
            
            prepare()
            start()
        }
        
        Log.d("Recorder", "녹음 시작: ${outputFile!!.absolutePath}")
    }
    
    /**
     * 녹음 중지
     */
    fun stopRecording(): File? {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        
        Log.d("Recorder", "녹음 완료")
        return outputFile
    }
    
    /**
     * 녹음 취소
     */
    fun cancelRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        
        outputFile?.delete()
        outputFile = null
    }
}
```

**권한 요청**:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

---

## 비디오 스트리밍

### HLS 스트리밍

```kotlin
/**
 * HLS 스트리밍 재생
 */
fun playHlsStream(player: ExoPlayer, url: String) {
    val mediaItem = MediaItem.Builder()
        .setUri(url)
        .setMimeType(MimeTypes.APPLICATION_M3U8)  // HLS
        .build()
    
    player.setMediaItem(mediaItem)
    player.prepare()
    player.play()
}
```

### DASH 스트리밍

```kotlin
/**
 * DASH 스트리밍 재생
 */
fun playDashStream(player: ExoPlayer, url: String) {
    val mediaItem = MediaItem.Builder()
        .setUri(url)
        .setMimeType(MimeTypes.APPLICATION_MPD)  // DASH
        .build()
    
    player.setMediaItem(mediaItem)
    player.prepare()
    player.play()
}
```

---

## Picture-in-Picture

```kotlin
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.util.Rational

/**
 * Picture-in-Picture 모드
 */
class PipActivity : AppCompatActivity() {
    
    private var player: ExoPlayer? = null
    
    /**
     * PiP 모드 진입
     */
    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)  // 16:9 비율
            
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            
            enterPictureInPictureMode(params)
        }
    }
    
    /**
     * PiP 모드 변경 감지
     */
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        
        if (isInPictureInPictureMode) {
            // PiP 모드: UI 숨기기
            Log.d("PiP", "PiP 모드 진입")
        } else {
            // 일반 모드: UI 표시
            Log.d("PiP", "PiP 모드 종료")
        }
    }
    
    /**
     * 사용자가 뒤로 가기 버튼을 눌렀을 때
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        
        // 비디오 재생 중이면 자동으로 PiP 모드 진입
        if (player?.isPlaying == true) {
            enterPipMode()
        }
    }
}
```

**AndroidManifest.xml**:
```xml
<activity
    android:name=".PipActivity"
    android:supportsPictureInPicture="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"/>
```

---

## 실전 예제

### 음악 플레이어

```kotlin
/**
 * 간단한 음악 플레이어
 */
@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    
    // 재생 상태 업데이트
    LaunchedEffect(player) {
        while (true) {
            delay(100)
            isPlaying = player.isPlaying
            currentPosition = player.currentPosition
            duration = player.duration.coerceAtLeast(0)
        }
    }
    
    DisposableEffect(Unit) {
        val mediaItem = MediaItem.fromUri("https://example.com/music.mp3")
        player.setMediaItem(mediaItem)
        player.prepare()
        
        onDispose {
            player.release()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 앨범 아트
        Image(
            painter = painterResource(R.drawable.album_art),
            contentDescription = "앨범 아트",
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 제목
        Text(
            text = "노래 제목",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "아티스트",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 진행 바
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { player.seekTo(it.toLong()) },
            valueRange = 0f..duration.toFloat()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition))
            Text(formatTime(duration))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 컨트롤 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 이전 곡
            IconButton(onClick = { /* 이전 곡 */ }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "이전 곡")
            }
            
            // 재생/일시정지
            IconButton(onClick = {
                if (isPlaying) player.pause() else player.play()
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "일시정지" else "재생"
                )
            }
            
            // 다음 곡
            IconButton(onClick = { /* 다음 곡 */ }) {
                Icon(Icons.Default.SkipNext, contentDescription = "다음 곡")
            }
        }
    }
}

/**
 * 시간 포맷팅 (ms → mm:ss)
 */
fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. 오디오 포커스 요청
audioFocusManager.requestAudioFocus { focusChange ->
    handleAudioFocusChange(focusChange, player)
}

// 2. 백그라운드 재생 시 Foreground Service 사용
startForegroundService(Intent(this, MusicService::class.java))

// 3. 리소스 해제
override fun onDestroy() {
    super.onDestroy()
    player?.release()
}

// 4. 에러 처리
player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        Log.e("Player", "에러: ${error.message}")
        showErrorToUser()
    }
})

// 5. 적절한 오디오 속성 설정
val audioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_MEDIA)
    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    .build()
```

### DON'Ts ❌

```kotlin
// 1. 오디오 포커스 무시
player.play()  // ❌ 포커스 요청 없이 재생

// 2. 백그라운드에서 일반 Service 사용
startService(Intent(this, MusicService::class.java))  // ❌

// 3. 리소스 해제 안 함
// ❌ player.release() 호출 안 함

// 4. 메인 스레드에서 동기 준비
mediaPlayer.prepare()  // ❌ prepareAsync() 사용

// 5. 에러 처리 안 함
// ❌ 에러 리스너 없음
```

---

## 참고 자료

- [ExoPlayer 공식 문서](https://developer.android.com/guide/topics/media/exoplayer)
- [MediaPlayer 가이드](https://developer.android.com/guide/topics/media/mediaplayer)
- [Audio Focus](https://developer.android.com/guide/topics/media-apps/audio-focus)
- [Media Session](https://developer.android.com/guide/topics/media-apps/working-with-a-media-session)
