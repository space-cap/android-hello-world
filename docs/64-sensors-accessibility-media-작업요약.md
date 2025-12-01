# Sensors, Accessibility, Media 가이드 작업 완료

## 작업 요약

Android Sensors & Motion, Accessibility, Media Player & Audio에 대한 **초보자 친화적이고 매우 상세한** 문서를 3개 작성했습니다.

## 생성된 문서 목록

### 1. [63-android-sensors-motion-guide.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/63-android-sensors-motion-guide.md) (약 32KB)

**Sensors & Motion 가이드**

#### 주요 내용
- ✅ 센서 종류 (모션, 위치, 환경)
- ✅ 센서 기본 사용법
  - SensorManager 초기화
  - 센서 리스너 등록
  - 샘플링 속도
- ✅ 가속도계 (Accelerometer)
  - 기본 사용
  - 흔들기 감지
  - 기울기 감지
- ✅ 자이로스코프 (Gyroscope)
  - 회전 속도 측정
  - 각도 계산
- ✅ 나침반 (자기장 센서)
  - 방위각 계산
  - 방향 표시
- ✅ 근접 센서 (Proximity)
- ✅ 광 센서 (Light)
  - 조도 측정
  - 화면 밝기 자동 조절
- ✅ 걸음 감지 (Step Counter/Detector)
- ✅ 센서 융합
- ✅ 배터리 최적화
- ✅ 실전 예제 (수평계)

---

### 2. [65-android-accessibility-guide.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/65-android-accessibility-guide.md) (약 30KB)

**Accessibility 가이드**

#### 주요 내용
- ✅ 접근성 중요성
  - 법적 요구사항
  - 사용자 확대
  - UX 개선
- ✅ Content Description
  - 기본 사용법
  - 좋은 설명 vs 나쁜 설명
  - 상태에 따른 설명
- ✅ TalkBack 지원
  - 커스텀 동작
  - 라이브 영역
  - 그룹화
- ✅ 시맨틱 속성
  - 역할 (Role)
  - 상태 정보
  - 진행 상태
- ✅ 키보드 네비게이션
  - 포커스 순서
  - 포커스 표시
- ✅ 색상 대비
  - WCAG 기준
  - 대비 계산
  - 색맹 고려
- ✅ 폰트 크기 조정
  - 동적 폰트 크기
  - 최소 터치 영역
- ✅ 접근성 서비스
- ✅ 테스트 (수동/자동)
- ✅ 실전 예제 (로그인 화면)

---

### 3. [67-android-media-audio-guide.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/67-android-media-audio-guide.md) (약 28KB)

**Media Player & Audio 가이드**

#### 주요 내용
- ✅ ExoPlayer
  - 의존성 추가
  - 기본 사용법
  - UI 연결 (PlayerView)
  - 재생 제어
  - 재생 상태 모니터링
- ✅ MediaPlayer
  - 기본 사용법
  - URL/리소스 재생
- ✅ 오디오 포커스
  - 포커스 요청
  - 포커스 변경 처리
- ✅ 백그라운드 재생
  - Foreground Service
  - 알림 생성
- ✅ 미디어 세션
- ✅ 오디오 녹음
  - MediaRecorder 사용
- ✅ 비디오 스트리밍
  - HLS 스트리밍
  - DASH 스트리밍
- ✅ Picture-in-Picture (PiP)
- ✅ 실전 예제 (음악 플레이어)

---

## 문서 통계

| 항목 | 수치 |
|------|------|
| **총 문서 수** | 3개 |
| **총 용량** | 약 90KB |
| **총 라인 수** | 약 2,800줄 |
| **코드 예제** | 60개 이상 |
| **실전 예제** | 9개 이상 |

---

## 주요 학습 포인트

### 1. Sensors - 가속도계

```kotlin
// 센서 매니저
val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

// 가속도계 센서
val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

// 리스너 등록
sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

// 센서 값 읽기
override fun onSensorChanged(event: SensorEvent) {
    val x = event.values[0]  // X축
    val y = event.values[1]  // Y축
    val z = event.values[2]  // Z축
}
```

### 2. Accessibility - Content Description

```kotlin
// 이미지 설명
Image(
    painter = painterResource(R.drawable.profile),
    contentDescription = "사용자 프로필 사진"  // ✅ 필수
)

// 상태에 따른 설명
Icon(
    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
    contentDescription = if (isPlaying) "일시정지" else "재생"
)

// 그룹화
Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
    Text("홍길동")
    Text("소프트웨어 엔지니어")
}
```

### 3. Media - ExoPlayer

```kotlin
// ExoPlayer 초기화
val player = ExoPlayer.Builder(context).build()

// 미디어 설정
val mediaItem = MediaItem.fromUri("https://example.com/music.mp3")
player.setMediaItem(mediaItem)
player.prepare()

// 재생
player.play()

// 리소스 해제
player.release()
```

---

## 실전 예제 하이라이트

### 1. 수평계 (Sensors)

```kotlin
@Composable
fun SpiritLevelScreen() {
    var pitch by remember { mutableStateOf(0f) }
    var roll by remember { mutableStateOf(0f) }
    
    // 센서 리스너
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                pitch = (Math.atan2(x, Math.sqrt(y * y + z * z)) * 180 / Math.PI).toFloat()
                roll = (Math.atan2(y, Math.sqrt(x * x + z * z)) * 180 / Math.PI).toFloat()
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        // 등록
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    // UI
    val isLevel = Math.abs(pitch) < 2 && Math.abs(roll) < 2
    Text(
        text = if (isLevel) "수평입니다!" else "기울어져 있습니다",
        color = if (isLevel) Color.Green else Color.Red
    )
}
```

### 2. 접근성 좋은 로그인 화면 (Accessibility)

```kotlin
@Composable
fun AccessibleLoginScreen() {
    Column {
        // 제목
        Text(
            text = "로그인",
            modifier = Modifier.semantics { heading() }
        )
        
        // 이메일 입력
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.semantics {
                contentDescription = "이메일 입력 필드"
            }
        )
        
        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.semantics {
                contentDescription = "비밀번호 입력 필드"
                password()
            }
        )
        
        // 에러 메시지
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )
        }
        
        // 로그인 버튼
        Button(
            onClick = { /* 로그인 */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)  // 최소 터치 영역
        ) {
            Text("로그인")
        }
    }
}
```

### 3. 음악 플레이어 (Media)

```kotlin
@Composable
fun MusicPlayerScreen() {
    val player = remember { ExoPlayer.Builder(context).build() }
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
    
    Column {
        // 앨범 아트
        Image(
            painter = painterResource(R.drawable.album_art),
            contentDescription = "앨범 아트"
        )
        
        // 진행 바
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { player.seekTo(it.toLong()) },
            valueRange = 0f..duration.toFloat()
        )
        
        // 컨트롤 버튼
        Row {
            IconButton(onClick = { /* 이전 곡 */ }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "이전 곡")
            }
            
            IconButton(onClick = {
                if (isPlaying) player.pause() else player.play()
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "일시정지" else "재생"
                )
            }
            
            IconButton(onClick = { /* 다음 곡 */ }) {
                Icon(Icons.Default.SkipNext, contentDescription = "다음 곡")
            }
        }
    }
}
```

---

## 학습 경로 추천

### 초급 개발자
1. **Sensors**: 가속도계 기본 사용법 학습
2. **Accessibility**: Content Description 추가 실습
3. **Media**: MediaPlayer로 간단한 오디오 재생

### 중급 개발자
1. **Sensors**: 센서 융합으로 나침반 구현
2. **Accessibility**: TalkBack 지원 및 시맨틱 속성 활용
3. **Media**: ExoPlayer로 음악 플레이어 제작

### 고급 개발자
1. **Sensors**: 복잡한 모션 감지 및 배터리 최적화
2. **Accessibility**: 완전한 접근성 지원 앱 개발
3. **Media**: 백그라운드 재생 및 미디어 세션 구현

---

## 활용 분야

### 📱 Sensors
- **게임**: 기울기 조작, 흔들기 감지
- **피트니스**: 걸음 수 측정, 운동 추적
- **내비게이션**: 나침반, 방향 표시
- **AR/VR**: 정밀한 모션 추적

### ♿ Accessibility
- **모든 앱**: 필수 요구사항
- **정부/공공 앱**: 법적 의무
- **교육 앱**: 다양한 학습자 지원
- **엔터프라이즈 앱**: 포용적 디자인

### 🎵 Media
- **음악 플레이어**: Spotify, YouTube Music
- **비디오 플레이어**: Netflix, YouTube
- **팟캐스트**: 오디오 스트리밍
- **교육 앱**: 강의 영상 재생

---

## 베스트 프랙티스

### Sensors

```kotlin
// ✅ onResume에서 등록, onPause에서 해제
override fun onResume() {
    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
}

override fun onPause() {
    sensorManager.unregisterListener(this)
}

// ✅ 적절한 샘플링 속도 선택
// 게임: SENSOR_DELAY_GAME
// UI: SENSOR_DELAY_UI
// 일반: SENSOR_DELAY_NORMAL (배터리 절약)
```

### Accessibility

```kotlin
// ✅ 모든 이미지에 contentDescription
Image(
    painter = painterResource(R.drawable.icon),
    contentDescription = "아이콘 설명"
)

// ✅ 최소 터치 영역 (48dp)
IconButton(
    onClick = {},
    modifier = Modifier.size(48.dp)
) {
    Icon(Icons.Default.Menu, contentDescription = "메뉴")
}

// ✅ 충분한 색상 대비 (4.5:1 이상)
Text(
    text = "읽기 쉬운 텍스트",
    color = Color.White,
    modifier = Modifier.background(Color.Black)
)
```

### Media

```kotlin
// ✅ 오디오 포커스 요청
audioFocusManager.requestAudioFocus { focusChange ->
    handleAudioFocusChange(focusChange, player)
}

// ✅ 백그라운드 재생 시 Foreground Service
startForegroundService(Intent(this, MusicService::class.java))

// ✅ 리소스 해제
override fun onDestroy() {
    player?.release()
}
```

---

## 참고 자료

### Sensors
- [Sensors 공식 문서](https://developer.android.com/guide/topics/sensors/sensors_overview)
- [Motion Sensors](https://developer.android.com/guide/topics/sensors/sensors_motion)

### Accessibility
- [Accessibility 공식 문서](https://developer.android.com/guide/topics/ui/accessibility)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

### Media
- [ExoPlayer 공식 문서](https://developer.android.com/guide/topics/media/exoplayer)
- [Audio Focus](https://developer.android.com/guide/topics/media-apps/audio-focus)

---

**문서 작성 완료일**: 2024년 12월 1일  
**작성자**: Gemini AI Assistant
