# 카메라 및 미디어 완벽 가이드

## 📚 목차

1. [카메라와 미디어란?](#카메라와-미디어란)
2. [CameraX 시작하기](#camerax-시작하기)
3. [사진 촬영](#사진-촬영)
4. [동영상 촬영](#동영상-촬영)
5. [갤러리에서 선택](#갤러리에서-선택)
6. [이미지 처리](#이미지-처리)
7. [동영상 재생 (ExoPlayer)](#동영상-재생-exoplayer)
8. [실전 예제](#실전-예제)

---

## 카메라와 미디어란?

> [!NOTE]
> **카메라와 미디어는 앱에서 사진/동영상을 다루는 기능입니다**
> 
> **주요 기능:**
> - 📷 사진 촬영
> - 🎥 동영상 촬영
> - 🖼️ 갤러리에서 선택
> - ✂️ 이미지 편집
> - ▶️ 동영상 재생

### 왜 중요한가?

**사용 사례:**
```
SNS 앱: 사진/동영상 업로드
쇼핑 앱: 상품 사진 촬영
화상 통화: 실시간 카메라
AR 앱: 카메라 + 증강현실
```

**통계:**
- 모바일 앱의 **68%**가 카메라 기능 사용
- 사용자의 **85%**가 카메라 기능을 중요하게 생각

---

## CameraX 시작하기

> [!IMPORTANT]
> **CameraX = Google의 최신 카메라 라이브러리**
> 
> **왜 CameraX를 사용하는가?**
> - 🎯 간단한 API
> - 📱 모든 기기에서 일관된 동작
> - 🔄 자동 생명주기 관리
> - 🚀 최신 기능 자동 지원

### Camera2 vs CameraX

**Camera2 (구버전):**
```kotlin
// 복잡한 설정 (100줄 이상)
val cameraManager = getSystemService(CAMERA_SERVICE)
val cameraId = cameraManager.cameraIdList[0]
val characteristics = cameraManager.getCameraCharacteristics(cameraId)
// ... 수십 줄의 설정 코드 ...
```

**CameraX (최신):**
```kotlin
// 간단한 설정 (10줄)
val cameraProvider = ProcessCameraProvider.getInstance(context)
val preview = Preview.Builder().build()
cameraProvider.bindToLifecycle(this, cameraSelector, preview)
```

### 설정

#### 1. 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // CameraX 핵심
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    
    // 동영상 촬영 (선택사항)
    implementation("androidx.camera:camera-video:$cameraxVersion")
}
```

**각 라이브러리가 하는 일:**
```
camera-core: CameraX 핵심 기능
camera-camera2: Camera2 구현체 (실제 카메라 제어)
camera-lifecycle: 생명주기 자동 관리
camera-view: PreviewView (카메라 미리보기 UI)
camera-video: 동영상 촬영 기능
```

#### 2. 권한 설정

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <!-- 카메라 권한 -->
    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />
    
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- 동영상 녹화 시 오디오 권한 -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    
    <!-- 저장 권한 (Android 12 이하) -->
    <uses-permission
        android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
</manifest>
```

**왜 required="false"인가?**
```
required="true": 카메라 없는 기기는 Play Store에서 설치 불가
required="false": 카메라 없어도 설치 가능 (코드에서 체크)
```

#### 3. 권한 요청

```kotlin
// Accompanist Permissions 사용
implementation("com.google.accompanist:accompanist-permissions:0.32.0")

@Composable
fun CameraPermissionRequest(
    onPermissionGranted: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    when {
        cameraPermissionState.status.isGranted -> {
            onPermissionGranted()
        }
        cameraPermissionState.status.shouldShowRationale -> {
            // 권한이 필요한 이유 설명
            AlertDialog(
                onDismissRequest = {},
                title = { Text("카메라 권한 필요") },
                text = { Text("사진을 촬영하려면 카메라 권한이 필요합니다.") },
                confirmButton = {
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text("권한 허용")
                    }
                }
            )
        }
        else -> {
            // 권한 거부됨
            Text("카메라 권한이 거부되었습니다.")
        }
    }
}
```

---

## 사진 촬영

### 기본 카메라 미리보기

```kotlin
@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // PreviewView 생성
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    
    // CameraX 설정
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        
        // 미리보기 설정
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        
        // 후면 카메라 선택
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            // 기존 바인딩 해제
            cameraProvider.unbindAll()
            
            // 카메라 바인딩
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        } catch (e: Exception) {
            Log.e("CameraX", "카메라 바인딩 실패", e)
        }
    }
    
    // UI
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
```

**동작 원리:**
```
1. ProcessCameraProvider 가져오기
   ↓
2. Preview 객체 생성
   ↓
3. CameraSelector로 카메라 선택 (전면/후면)
   ↓
4. bindToLifecycle로 연결
   ↓
5. PreviewView에 표시
```

### 사진 촬영 구현

```kotlin
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // 카메라 상태
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCameraReady by remember { mutableStateOf(false) }
    
    val previewView = remember { PreviewView(context) }
    
    // CameraX 초기화
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        
        // ImageCapture 설정
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture  // 사진 촬영 추가
            )
            isCameraReady = true
        } catch (e: Exception) {
            Log.e("CameraX", "초기화 실패", e)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 카메라 미리보기
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // 촬영 버튼
        if (isCameraReady) {
            Button(
                onClick = {
                    takePicture(context, imageCapture)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, "사진 촬영")
            }
        }
    }
}

// 사진 촬영 함수
fun takePicture(
    context: Context,
    imageCapture: ImageCapture?
) {
    val imageCapture = imageCapture ?: return
    
    // 저장할 파일 생성
    val photoFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "photo_${System.currentTimeMillis()}.jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    // 사진 촬영
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d("CameraX", "사진 저장: ${photoFile.absolutePath}")
                Toast.makeText(context, "사진이 저장되었습니다", Toast.LENGTH_SHORT).show()
            }
            
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraX", "사진 촬영 실패", exception)
                Toast.makeText(context, "촬영 실패", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
```

**촬영 과정:**
```
버튼 클릭
    ↓
파일 생성 (photo_timestamp.jpg)
    ↓
takePicture() 호출
    ↓
카메라 센서에서 이미지 캡처
    ↓
JPEG 인코딩
    ↓
파일 저장
    ↓
콜백 호출 (성공/실패)
```

### 전면/후면 카메라 전환

```kotlin
@Composable
fun CameraWithSwitch() {
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraSelector = remember(lensFacing) {
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(cameraSelector)
        
        // 카메라 전환 버튼
        IconButton(
            onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Cameraswitch, "카메라 전환")
        }
    }
}
```

---

## 동영상 촬영

### VideoCapture 설정

```kotlin
@Composable
fun VideoRecordingScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var recording: Recording? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }
    
    val previewView = remember { PreviewView(context) }
    
    // CameraX 초기화
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        
        // Recorder 설정
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(Quality.HIGHEST)
            )
            .build()
        
        videoCapture = VideoCapture.withOutput(recorder)
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
        } catch (e: Exception) {
            Log.e("CameraX", "초기화 실패", e)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // 녹화 버튼
        Button(
            onClick = {
                if (isRecording) {
                    // 녹화 중지
                    recording?.stop()
                    recording = null
                    isRecording = false
                } else {
                    // 녹화 시작
                    recording = startRecording(context, videoCapture) {
                        isRecording = false
                    }
                    isRecording = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color.Red else Color.Blue
            )
        ) {
            Icon(
                if (isRecording) Icons.Filled.Stop else Icons.Filled.Videocam,
                if (isRecording) "중지" else "녹화"
            )
        }
        
        // 녹화 시간 표시
        if (isRecording) {
            RecordingTimer(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )
        }
    }
}

// 녹화 시작
@SuppressLint("MissingPermission")
fun startRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>?,
    onStopped: () -> Unit
): Recording? {
    val videoCapture = videoCapture ?: return null
    
    // 저장할 파일
    val videoFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
        "video_${System.currentTimeMillis()}.mp4"
    )
    
    val outputOptions = FileOutputOptions.Builder(videoFile).build()
    
    // 녹화 시작
    return videoCapture.output
        .prepareRecording(context, outputOptions)
        .withAudioEnabled()  // 오디오 포함
        .start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Log.d("CameraX", "녹화 시작")
                }
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        Log.e("CameraX", "녹화 실패: ${event.error}")
                    } else {
                        Log.d("CameraX", "녹화 완료: ${videoFile.absolutePath}")
                        Toast.makeText(
                            context,
                            "동영상이 저장되었습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onStopped()
                }
            }
        }
}

// 녹화 시간 표시
@Composable
fun RecordingTimer(modifier: Modifier = Modifier) {
    var seconds by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }
    
    Surface(
        modifier = modifier,
        color = Color.Red,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%02d:%02d", seconds / 60, seconds % 60),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

**녹화 과정:**
```
녹화 시작 버튼 클릭
    ↓
파일 생성 (video_timestamp.mp4)
    ↓
prepareRecording() 호출
    ↓
withAudioEnabled() - 오디오 포함
    ↓
start() - 녹화 시작
    ↓
VideoRecordEvent.Start 이벤트
    ↓
... 녹화 중 ...
    ↓
stop() 호출
    ↓
VideoRecordEvent.Finalize 이벤트
    ↓
파일 저장 완료
```

---

## 갤러리에서 선택

### 이미지 선택

```kotlin
@Composable
fun ImagePickerScreen() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 선택된 이미지 표시
        selectedImageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "선택된 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 이미지 선택 버튼
        Button(
            onClick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        ) {
            Icon(Icons.Filled.PhotoLibrary, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("갤러리에서 선택")
        }
    }
}
```

### 여러 이미지 선택

```kotlin
@Composable
fun MultipleImagePickerScreen() {
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // 여러 이미지 선택
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedImages = uris
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 선택된 이미지 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(selectedImages) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Button(
            onClick = {
                multipleImagePickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("이미지 선택 (최대 5개)")
        }
    }
}
```

### 동영상 선택

```kotlin
val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    selectedVideoUri = uri
}

Button(
    onClick = {
        videoPickerLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.VideoOnly
            )
        )
    }
) {
    Text("동영상 선택")
}
```

---

## 이미지 처리

### 이미지 크기 조정

```kotlin
fun resizeBitmap(
    context: Context,
    uri: Uri,
    maxWidth: Int,
    maxHeight: Int
): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options()
        
        // 1단계: 이미지 크기만 확인
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()
        
        // 2단계: 샘플링 비율 계산
        val (width, height) = options.run { outWidth to outHeight }
        var inSampleSize = 1
        
        if (width > maxWidth || height > maxHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            
            while (halfWidth / inSampleSize >= maxWidth &&
                   halfHeight / inSampleSize >= maxHeight) {
                inSampleSize *= 2
            }
        }
        
        // 3단계: 실제 로딩
        val inputStream2 = context.contentResolver.openInputStream(uri)
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        
        val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
        inputStream2?.close()
        
        bitmap
    } catch (e: Exception) {
        Log.e("ImageProcessing", "크기 조정 실패", e)
        null
    }
}
```

**왜 이렇게 하는가?**
```
원본 이미지: 4000x3000 (12MP) = ~36MB
메모리 부족 가능성!

inSampleSize = 2: 2000x1500 = ~9MB
inSampleSize = 4: 1000x750 = ~2.25MB

→ 메모리 절약!
```

### 이미지 회전

```kotlin
fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    
    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}

// EXIF 정보로 자동 회전
fun rotateImageIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    val exif = ExifInterface(inputStream!!)
    inputStream.close()
    
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
        else -> bitmap
    }
}
```

---

## 동영상 재생 (ExoPlayer)

> [!TIP]
> **ExoPlayer = Google의 강력한 미디어 플레이어**
> 
> **장점:**
> - 다양한 포맷 지원
> - 스트리밍 최적화
> - 커스터마이징 가능

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
}
```

### 기본 동영상 재생

```kotlin
@Composable
fun VideoPlayerScreen(videoUri: Uri) {
    val context = LocalContext.current
    
    // ExoPlayer 생성
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    
    // 생명주기 관리
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    // PlayerView
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = true  // 컨트롤러 표시
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

### 재생 컨트롤

```kotlin
@Composable
fun VideoPlayerWithControls(videoUri: Uri) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { PlayerView(context).apply { player = exoPlayer } },
            modifier = Modifier.fillMaxSize()
        )
        
        // 커스텀 컨트롤
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 재생/일시정지
            IconButton(onClick = {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            
            // 10초 뒤로
            IconButton(onClick = {
                exoPlayer.seekTo(exoPlayer.currentPosition - 10000)
            }) {
                Icon(Icons.Filled.Replay10, null, tint = Color.White)
            }
            
            // 10초 앞으로
            IconButton(onClick = {
                exoPlayer.seekTo(exoPlayer.currentPosition + 10000)
            }) {
                Icon(Icons.Filled.Forward10, null, tint = Color.White)
            }
        }
    }
}
```

---

## 실전 예제

### 완전한 카메라 앱

```kotlin
@Composable
fun CompleteCameraApp() {
    var currentScreen by remember { mutableStateOf(CameraScreen.CAMERA) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    when (currentScreen) {
        CameraScreen.CAMERA -> {
            CameraPermissionRequest {
                FullCameraScreen(
                    onImageCaptured = { uri ->
                        capturedImageUri = uri
                        currentScreen = CameraScreen.PREVIEW
                    },
                    onGalleryClick = {
                        currentScreen = CameraScreen.GALLERY
                    }
                )
            }
        }
        CameraScreen.PREVIEW -> {
            ImagePreviewScreen(
                imageUri = capturedImageUri!!,
                onBack = { currentScreen = CameraScreen.CAMERA },
                onSave = {
                    // 이미지 저장 로직
                    currentScreen = CameraScreen.CAMERA
                }
            )
        }
        CameraScreen.GALLERY -> {
            GalleryScreen(
                onImageSelected = { uri ->
                    capturedImageUri = uri
                    currentScreen = CameraScreen.PREVIEW
                },
                onBack = { currentScreen = CameraScreen.CAMERA }
            )
        }
    }
}

enum class CameraScreen {
    CAMERA, PREVIEW, GALLERY
}

@Composable
fun FullCameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onGalleryClick: () -> Unit
) {
    // 완전한 카메라 UI 구현
    // - 미리보기
    // - 촬영 버튼
    // - 전면/후면 전환
    // - 플래시 토글
    // - 갤러리 버튼
}
```

---

## 💡 베스트 프랙티스

### 1. 메모리 관리

```kotlin
// ✅ 사용 후 해제
DisposableEffect(Unit) {
    onDispose {
        exoPlayer.release()
        bitmap?.recycle()
    }
}
```

### 2. 권한 체크

```kotlin
// ✅ 사용 전 항상 권한 확인
if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
) {
    // 카메라 사용
}
```

### 3. 에러 처리

```kotlin
try {
    cameraProvider.bindToLifecycle(...)
} catch (e: Exception) {
    Log.e("CameraX", "카메라 초기화 실패", e)
    // 사용자에게 에러 메시지 표시
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Capture the Moment! 📸
