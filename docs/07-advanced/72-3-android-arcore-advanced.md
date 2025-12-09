# Android ARCore 고급 가이드

> 📖 **시리즈 구성**
> - **72-1**: [ARCore의 역사](./72-1-arcore-history.md) - AR 기술의 발전과 ARCore 탄생 배경
> - **72-2**: [ARCore 기본 가이드](./72-2-android-arcore-basics.md) - 개발 환경 설정부터 첫 AR 앱까지
> - **72-3**: ARCore 고급 가이드 (현재 문서) - 이미지 추적, 얼굴 추적, Depth API
> - **72-4**: [ARCore 실전 프로젝트](./72-4-android-arcore-project.md) - 가구 배치, 측정, 명함 앱

---

## 📚 목차

1. [이미지 추적 (Augmented Images)](#이미지-추적-augmented-images)
2. [얼굴 추적 (Augmented Faces)](#얼굴-추적-augmented-faces)
3. [Depth API](#depth-api)
4. [Cloud Anchors](#cloud-anchors)
5. [성능 최적화](#성능-최적화)
6. [고급 문제 해결](#고급-문제-해결)

---

## 이미지 추적 (Augmented Images)

### 🎯 Augmented Images란?

**Augmented Images**는 특정 이미지를 인식하고 그 위에 AR 콘텐츠를 표시하는 기능입니다.

```
사용 사례:
📇 명함 → 연락처 정보 3D 표시
🎨 포스터 → 동영상 재생
📦 제품 패키지 → 사용 설명서 AR 표시
🗺️ 지도 → 3D 건물 표시
```

### 🔧 이미지 데이터베이스 생성

ARCore가 인식할 이미지를 미리 등록해야 합니다.

#### 1. 이미지 준비

좋은 추적 이미지의 조건:
- **해상도**: 최소 300 x 300 픽셀
- **형식**: PNG 또는 JPEG
- **특징**: 고대비, 복잡한 패턴
- **피할 것**: 단색, 반복 패턴, QR 코드

```kotlin
/**
 * 이미지 품질 평가
 * 
 * ARCore는 이미지의 추적 품질을 0-100으로 평가합니다.
 * 75 이상이 권장됩니다.
 */
fun evaluateImageQuality(imagePath: String): Int {
    // arcoreimg 도구 사용 (명령줄)
    // $ arcoreimg eval-img --input_image_path=image.png
    
    // 결과:
    // Image quality score: 85/100
    // 이미지가 추적에 적합함
    
    return 85 // 예시
}
```

#### 2. 이미지 데이터베이스 생성

**방법 1: 런타임에 생성**

```kotlin
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Session
import android.graphics.BitmapFactory

/**
 * 런타임에 이미지 데이터베이스 생성
 * 
 * @param session ARCore 세션
 * @return 이미지 데이터베이스
 */
fun createImageDatabase(session: Session, context: Context): AugmentedImageDatabase {
    // 빈 데이터베이스 생성
    val database = AugmentedImageDatabase(session)
    
    // 이미지 추가
    // assets 폴더의 이미지 로드
    val inputStream = context.assets.open("business_card.png")
    val bitmap = BitmapFactory.decodeStream(inputStream)
    
    // 데이터베이스에 이미지 추가
    // 매개변수:
    // - name: 이미지 식별자 (나중에 사용)
    // - bitmap: 이미지 비트맵
    // - widthInMeters: 실제 이미지의 너비 (미터 단위)
    //   예: 명함은 약 0.09m (9cm)
    val imageIndex = database.addImage(
        "business_card",  // 이미지 이름
        bitmap,           // 비트맵
        0.09f             // 실제 너비 (9cm)
    )
    
    Log.d("ImageDatabase", "이미지 추가됨: index=$imageIndex")
    
    return database
}
```

**방법 2: 사전 빌드된 데이터베이스 사용**

```kotlin
/**
 * 사전 빌드된 이미지 데이터베이스 로드
 * 
 * 장점: 앱 시작 시간 단축
 * 
 * 빌드 방법:
 * $ arcoreimg build-db \
 *     --input_images_directory=images/ \
 *     --output_db_path=myimages.imgdb
 */
fun loadPrebuiltDatabase(session: Session, context: Context): AugmentedImageDatabase {
    val inputStream = context.assets.open("myimages.imgdb")
    return AugmentedImageDatabase.deserialize(session, inputStream)
}
```

#### 3. ARCore 세션에 적용

```kotlin
import com.google.ar.core.Config

/**
 * 이미지 추적 활성화
 */
fun enableImageTracking(session: Session, database: AugmentedImageDatabase) {
    val config = session.config
    
    // 이미지 데이터베이스 설정
    config.augmentedImageDatabase = database
    
    // 동시에 추적할 최대 이미지 수
    // 기본값: 20
    // 많을수록 성능 저하
    config.maxNumberOfTrackedImages = 5
    
    // 설정 적용
    session.configure(config)
    
    Log.d("ImageTracking", "이미지 추적 활성화: ${database.numImages}개 이미지")
}
```

### 📸 이미지 인식 및 추적

```kotlin
import com.google.ar.core.Frame
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState

/**
 * 프레임에서 인식된 이미지 가져오기
 * 
 * @param frame ARCore 프레임
 * @return 인식된 이미지 리스트
 */
fun getTrackedImages(frame: Frame): List<TrackedImageInfo> {
    // 업데이트된 Augmented Images 가져오기
    val updatedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
    
    return updatedImages
        // 추적 중인 이미지만 필터링
        .filter { it.trackingState == TrackingState.TRACKING }
        .map { image ->
            TrackedImageInfo(
                // 이미지 이름 (데이터베이스에 등록한 이름)
                name = image.name,
                
                // 이미지 인덱스
                index = image.index,
                
                // 이미지 중심 위치 (3D 공간)
                centerPose = image.centerPose,
                
                // 이미지 크기 (미터 단위)
                extentX = image.extentX,  // 너비
                extentZ = image.extentZ,  // 높이
                
                // 추적 방법
                // FULL_TRACKING: 이미지 전체 추적
                // LAST_KNOWN_POSE: 마지막 알려진 위치 사용
                trackingMethod = image.trackingMethod,
                
                // Anchor 생성 (이미지 위치에 고정)
                anchor = image.createAnchor(image.centerPose)
            )
        }
}

/**
 * 추적된 이미지 정보
 */
data class TrackedImageInfo(
    val name: String,
    val index: Int,
    val centerPose: Pose,
    val extentX: Float,
    val extentZ: Float,
    val trackingMethod: AugmentedImage.TrackingMethod,
    val anchor: Anchor
)
```

### 🎨 이미지 위에 AR 콘텐츠 표시

```kotlin
/**
 * 인식된 이미지 위에 3D 콘텐츠 표시
 */
@Composable
fun AugmentedImageScreen() {
    var trackedImages by remember { mutableStateOf<List<TrackedImageInfo>>(emptyList()) }
    
    ARCameraView(
        onFrameUpdate = { frame ->
            // 인식된 이미지 가져오기
            trackedImages = getTrackedImages(frame)
            
            // 각 이미지에 대해 처리
            trackedImages.forEach { imageInfo ->
                when (imageInfo.name) {
                    "business_card" -> {
                        // 명함 위에 연락처 정보 표시
                        displayContactInfo(imageInfo)
                    }
                    
                    "poster" -> {
                        // 포스터 위에 동영상 재생
                        playVideo(imageInfo)
                    }
                    
                    "product" -> {
                        // 제품 위에 3D 모델 표시
                        display3DModel(imageInfo)
                    }
                }
            }
        }
    )
    
    // UI 오버레이
    if (trackedImages.isEmpty()) {
        Text(
            text = "등록된 이미지를 카메라로 비추세요",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        Text(
            text = "이미지 인식됨: ${trackedImages.size}개",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Green
        )
    }
}

/**
 * 명함 위에 연락처 정보 표시 (예시)
 */
fun displayContactInfo(imageInfo: TrackedImageInfo) {
    // imageInfo.anchor 위치에 3D 텍스트 또는 UI 표시
    // 실제로는 OpenGL/Sceneform으로 렌더링
    
    Log.d("AR", "명함 인식: ${imageInfo.name}")
    // 3D 공간에 다음 정보 표시:
    // - 이름
    // - 전화번호
    // - 이메일
    // - 회사 로고 (3D)
}
```

### 💡 이미지 추적 팁

```kotlin
/**
 * Augmented Images 베스트 프랙티스
 */
object AugmentedImagesTips {
    
    /**
     * 1. 이미지 품질 확인
     * 
     * arcoreimg 도구로 사전 검증
     */
    fun validateImage(imagePath: String) {
        // $ arcoreimg eval-img --input_image_path=image.png
        // 75점 이상 권장
    }
    
    /**
     * 2. 실제 크기 정확히 지정
     * 
     * 실제 이미지 크기를 정확히 측정하여 입력
     */
    fun addImageWithCorrectSize(
        database: AugmentedImageDatabase,
        name: String,
        bitmap: Bitmap
    ) {
        // 명함: 9cm x 5cm
        // 포스터: 60cm x 90cm
        // 제품 패키지: 실제 측정
        
        database.addImage(name, bitmap, 0.09f) // 9cm
    }
    
    /**
     * 3. 동시 추적 이미지 수 제한
     * 
     * 성능을 위해 필요한 만큼만
     */
    fun setMaxTrackedImages(config: Config) {
        // 1-5개: 좋은 성능
        // 10개 이상: 성능 저하 가능
        config.maxNumberOfTrackedImages = 5
    }
    
    /**
     * 4. 이미지 추적 상태 확인
     */
    fun checkTrackingState(image: AugmentedImage) {
        when (image.trackingState) {
            TrackingState.TRACKING -> {
                // 이미지 추적 중
                when (image.trackingMethod) {
                    AugmentedImage.TrackingMethod.FULL_TRACKING -> {
                        // 전체 추적: 가장 정확
                        Log.d("AR", "전체 추적 중")
                    }
                    AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> {
                        // 마지막 위치 사용: 이미지가 가려짐
                        Log.d("AR", "마지막 위치 사용")
                    }
                }
            }
            
            TrackingState.PAUSED -> {
                // 추적 일시정지: 이미지를 찾을 수 없음
                Log.w("AR", "이미지를 찾을 수 없습니다")
            }
            
            TrackingState.STOPPED -> {
                // 추적 중지
                Log.e("AR", "추적 중지")
            }
        }
    }
}
```

---

## 얼굴 추적 (Augmented Faces)

### 😊 Augmented Faces란?

**Augmented Faces**는 사용자의 얼굴을 인식하고 3D 마스크나 효과를 적용하는 기능입니다.

```
사용 사례:
😎 AR 선글라스
💄 가상 메이크업
🎭 얼굴 필터
👑 3D 액세서리
```

### 📱 지원 기기 확인

Augmented Faces는 특별한 하드웨어가 필요합니다:

```kotlin
import com.google.ar.core.Config

/**
 * Augmented Faces 지원 확인
 * 
 * 요구사항:
 * - 전면 카메라
 * - ARCore 1.7 이상
 * - 특정 기기만 지원 (Pixel 3 이상 등)
 */
fun checkAugmentedFacesSupport(session: Session): Boolean {
    val config = Config(session)
    
    // Augmented Faces 모드 설정 시도
    config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
    
    return try {
        session.configure(config)
        true
    } catch (e: Exception) {
        Log.e("AugmentedFaces", "지원하지 않는 기기", e)
        false
    }
}
```

### 🎭 얼굴 추적 설정

```kotlin
/**
 * Augmented Faces 활성화
 */
fun enableAugmentedFaces(session: Session) {
    val config = session.config
    
    // 얼굴 추적 모드 설정
    // MESH3D: 3D 메쉬 제공 (468개 정점)
    // DISABLED: 비활성화
    config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
    
    // 전면 카메라 사용
    config.cameraConfig = session.supportedCameraConfigs
        .first { it.facingDirection == CameraConfig.FacingDirection.FRONT }
    
    session.configure(config)
    
    Log.d("AugmentedFaces", "얼굴 추적 활성화")
}
```

### 👤 얼굴 데이터 가져오기

```kotlin
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState

/**
 * 프레임에서 추적된 얼굴 가져오기
 * 
 * @param frame ARCore 프레임
 * @return 추적된 얼굴 리스트
 */
fun getTrackedFaces(frame: Frame): List<FaceInfo> {
    // 업데이트된 얼굴들 가져오기
    val updatedFaces = frame.getUpdatedTrackables(AugmentedFace::class.java)
    
    return updatedFaces
        .filter { it.trackingState == TrackingState.TRACKING }
        .map { face ->
            FaceInfo(
                // 얼굴 중심 위치
                centerPose = face.centerPose,
                
                // 얼굴 영역 (Region)
                // NOSE_TIP: 코끝
                // FOREHEAD_LEFT: 왼쪽 이마
                // FOREHEAD_RIGHT: 오른쪽 이마
                noseTipPose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP),
                foreheadLeftPose = face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT),
                foreheadRightPose = face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT),
                
                // 얼굴 메쉬 (3D 모델)
                // 468개의 정점으로 구성
                meshVertices = face.meshVertices,
                
                // 메쉬 법선 벡터 (조명 계산용)
                meshNormals = face.meshNormals,
                
                // 메쉬 삼각형 인덱스
                meshTriangleIndices = face.meshTriangleIndices,
                
                // 텍스처 좌표 (2D 이미지 매핑용)
                meshTextureCoordinates = face.meshTextureCoordinates
            )
        }
}

/**
 * 얼굴 정보 데이터 클래스
 */
data class FaceInfo(
    val centerPose: Pose,
    val noseTipPose: Pose,
    val foreheadLeftPose: Pose,
    val foreheadRightPose: Pose,
    val meshVertices: FloatBuffer,
    val meshNormals: FloatBuffer,
    val meshTriangleIndices: ShortBuffer,
    val meshTextureCoordinates: FloatBuffer
)
```

### 🎨 얼굴에 3D 마스크 적용

```kotlin
/**
 * 얼굴에 AR 효과 적용
 */
@Composable
fun AugmentedFaceScreen() {
    var trackedFaces by remember { mutableStateOf<List<FaceInfo>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("sunglasses") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // AR 카메라 뷰
        Box(modifier = Modifier.weight(1f)) {
            ARCameraView(
                onFrameUpdate = { frame ->
                    // 얼굴 추적
                    trackedFaces = getTrackedFaces(frame)
                    
                    // 각 얼굴에 필터 적용
                    trackedFaces.forEach { face ->
                        applyFaceFilter(face, selectedFilter)
                    }
                }
            )
            
            // 얼굴 감지 상태 표시
            if (trackedFaces.isEmpty()) {
                Text(
                    text = "얼굴을 카메라에 맞춰주세요",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
        
        // 필터 선택 UI
        FaceFilterSelector(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
    }
}

/**
 * 얼굴 필터 적용 (예시)
 */
fun applyFaceFilter(face: FaceInfo, filterType: String) {
    when (filterType) {
        "sunglasses" -> {
            // 선글라스 3D 모델을 눈 위치에 배치
            // face.foreheadLeftPose와 foreheadRightPose 사이에 배치
            renderSunglasses(face)
        }
        
        "makeup" -> {
            // 얼굴 메쉬에 메이크업 텍스처 적용
            applyMakeupTexture(face)
        }
        
        "mask" -> {
            // 전체 얼굴 마스크 (동물 얼굴 등)
            renderFaceMask(face)
        }
        
        "hat" -> {
            // 모자를 이마 위에 배치
            renderHat(face.foreheadLeftPose, face.foreheadRightPose)
        }
    }
}

/**
 * 선글라스 렌더링 (의사 코드)
 */
fun renderSunglasses(face: FaceInfo) {
    // 1. 눈 위치 계산
    val leftEyePos = face.foreheadLeftPose
    val rightEyePos = face.foreheadRightPose
    
    // 2. 선글라스 3D 모델 로드
    // val sunglassesModel = load3DModel("sunglasses.obj")
    
    // 3. 눈 위치에 맞춰 배치
    // sunglassesModel.position = (leftEyePos + rightEyePos) / 2
    
    // 4. 얼굴 방향에 맞춰 회전
    // sunglassesModel.rotation = face.centerPose.rotation
    
    // 5. 렌더링
    // render(sunglassesModel)
}

/**
 * 필터 선택 UI
 */
@Composable
fun FaceFilterSelector(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterButton("😎", "sunglasses", selectedFilter, onFilterSelected)
        FilterButton("💄", "makeup", selectedFilter, onFilterSelected)
        FilterButton("🎭", "mask", selectedFilter, onFilterSelected)
        FilterButton("👑", "hat", selectedFilter, onFilterSelected)
    }
}

@Composable
fun FilterButton(
    emoji: String,
    filterType: String,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Button(
        onClick = { onFilterSelected(filterType) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedFilter == filterType) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineMedium)
    }
}
```

### 💡 얼굴 추적 팁

```kotlin
/**
 * Augmented Faces 베스트 프랙티스
 */
object AugmentedFacesTips {
    
    /**
     * 1. 조명 확인
     * 
     * 얼굴 추적은 조명에 매우 민감합니다
     */
    fun checkLighting(frame: Frame): Boolean {
        val lightEstimate = frame.lightEstimate
        val brightness = lightEstimate.pixelIntensity
        
        return when {
            brightness < 0.2f -> {
                Log.w("AR", "너무 어두움: 조명을 켜주세요")
                false
            }
            brightness > 0.9f -> {
                Log.w("AR", "너무 밝음: 직사광선을 피하세요")
                false
            }
            else -> true
        }
    }
    
    /**
     * 2. 얼굴 거리 확인
     * 
     * 카메라와 얼굴 사이의 적절한 거리 유지
     */
    fun checkFaceDistance(face: AugmentedFace): Boolean {
        // 얼굴 중심까지의 거리 (미터)
        val distance = face.centerPose.tz()
        
        return when {
            distance < 0.2f -> {
                Log.w("AR", "너무 가까움: 뒤로 물러나세요")
                false
            }
            distance > 1.5f -> {
                Log.w("AR", "너무 멀음: 가까이 오세요")
                false
            }
            else -> true
        }
    }
    
    /**
     * 3. 메쉬 품질 확인
     */
    fun checkMeshQuality(face: AugmentedFace): Boolean {
        // 메쉬 정점 수 확인
        val vertexCount = face.meshVertices.limit() / 3
        
        // 468개 정점이 표준
        return vertexCount == 468
    }
}
```

---

## Depth API

### 🌊 Depth API란?

**Depth API**는 카메라로부터 각 픽셀까지의 거리(깊이) 정보를 제공합니다.

```
활용:
🎮 오클루전 (가상 객체가 실제 물체 뒤에 숨김)
📏 정확한 거리 측정
🎨 현실감 있는 AR 경험
```

### 🔧 Depth API 활성화

```kotlin
/**
 * Depth API 설정
 */
fun enableDepthAPI(session: Session) {
    val config = session.config
    
    // Depth 모드 설정
    // DISABLED: Depth 사용 안 함
    // AUTOMATIC: 기기가 지원하면 자동으로 사용
    config.depthMode = Config.DepthMode.AUTOMATIC
    
    session.configure(config)
    
    // Depth 지원 확인
    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
        Log.d("Depth", "Depth API 지원됨")
    } else {
        Log.w("Depth", "Depth API 미지원 기기")
    }
}
```

### 📊 깊이 정보 가져오기

```kotlin
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException

/**
 * 프레임에서 깊이 이미지 가져오기
 * 
 * @param frame ARCore 프레임
 * @return 깊이 이미지 또는 null
 */
fun getDepthImage(frame: Frame): DepthImageInfo? {
    return try {
        // 깊이 이미지 가져오기
        val depthImage = frame.acquireDepthImage16Bits()
        
        DepthImageInfo(
            // 이미지 크기
            width = depthImage.width,
            height = depthImage.height,
            
            // 깊이 데이터 (16비트)
            // 각 픽셀의 값 = 거리(mm)
            depthData = depthImage.planes[0].buffer,
            
            // 깊이 이미지 객체 (사용 후 반드시 close 호출)
            image = depthImage
        )
    } catch (e: NotYetAvailableException) {
        // 깊이 정보가 아직 준비되지 않음
        Log.d("Depth", "깊이 정보 준비 중...")
        null
    } catch (e: Exception) {
        Log.e("Depth", "깊이 정보 가져오기 실패", e)
        null
    }
}

/**
 * 깊이 이미지 정보
 */
data class DepthImageInfo(
    val width: Int,
    val height: Int,
    val depthData: ByteBuffer,
    val image: Image
) {
    /**
     * 특정 픽셀의 깊이 값 가져오기
     * 
     * @param x 픽셀 X 좌표
     * @param y 픽셀 Y 좌표
     * @return 깊이 (미터 단위)
     */
    fun getDepthAt(x: Int, y: Int): Float {
        // 픽셀 인덱스 계산
        val index = y * width + x
        
        // 16비트 값 읽기 (밀리미터)
        depthData.position(index * 2)
        val depthMm = depthData.short.toInt() and 0xFFFF
        
        // 미터로 변환
        return depthMm / 1000f
    }
    
    /**
     * 리소스 해제 (반드시 호출!)
     */
    fun close() {
        image.close()
    }
}
```

### 🎭 오클루전 구현

오클루전은 가상 객체가 실제 물체 뒤에 숨겨지는 효과입니다:

```kotlin
/**
 * 오클루전을 사용한 AR 렌더링
 */
fun renderWithOcclusion(
    virtualObject: VirtualObject,
    depthInfo: DepthImageInfo,
    cameraImage: Image
) {
    // 1. 가상 객체의 화면 좌표 계산
    val screenX = virtualObject.getScreenX()
    val screenY = virtualObject.getScreenY()
    
    // 2. 해당 위치의 실제 깊이 가져오기
    val realDepth = depthInfo.getDepthAt(screenX, screenY)
    
    // 3. 가상 객체의 깊이
    val virtualDepth = virtualObject.getDepth()
    
    // 4. 깊이 비교
    if (virtualDepth > realDepth) {
        // 가상 객체가 실제 물체보다 뒤에 있음
        // → 가상 객체를 숨김 또는 반투명하게 표시
        virtualObject.setVisibility(false)
    } else {
        // 가상 객체가 실제 물체보다 앞에 있음
        // → 정상 표시
        virtualObject.setVisibility(true)
    }
}
```

### 📏 정확한 거리 측정

```kotlin
/**
 * 두 점 사이의 거리 측정
 * 
 * Depth API를 사용하면 더 정확한 측정 가능
 */
class DepthBasedMeasurement(private val session: Session) {
    
    /**
     * 두 화면 좌표 사이의 실제 거리 계산
     * 
     * @param x1, y1 첫 번째 점
     * @param x2, y2 두 번째 점
     * @param frame ARCore 프레임
     * @return 거리 (미터) 또는 null
     */
    fun measureDistance(
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        frame: Frame
    ): Float? {
        val depthInfo = getDepthImage(frame) ?: return null
        
        try {
            // 첫 번째 점의 3D 좌표
            val point1 = get3DPoint(x1, y1, depthInfo, frame)
            
            // 두 번째 점의 3D 좌표
            val point2 = get3DPoint(x2, y2, depthInfo, frame)
            
            // 유클리드 거리 계산
            val dx = point2.x - point1.x
            val dy = point2.y - point1.y
            val dz = point2.z - point1.z
            
            return sqrt(dx*dx + dy*dy + dz*dz)
            
        } finally {
            depthInfo.close()
        }
    }
    
    /**
     * 화면 좌표를 3D 좌표로 변환
     */
    private fun get3DPoint(
        screenX: Float,
        screenY: Float,
        depthInfo: DepthImageInfo,
        frame: Frame
    ): Vector3 {
        // 1. 화면 좌표를 깊이 이미지 좌표로 변환
        val depthX = (screenX / screenWidth * depthInfo.width).toInt()
        val depthY = (screenY / screenHeight * depthInfo.height).toInt()
        
        // 2. 깊이 값 가져오기
        val depth = depthInfo.getDepthAt(depthX, depthY)
        
        // 3. 카메라 내부 파라미터 사용하여 3D 좌표 계산
        val camera = frame.camera
        val intrinsics = camera.imageIntrinsics
        
        // 화면 좌표 → 정규화된 좌표 → 3D 좌표
        // (복잡한 수학 계산 생략)
        
        return Vector3(x, y, depth)
    }
}

data class Vector3(val x: Float, val y: Float, val z: Float)
```

---

## Cloud Anchors

### ☁️ Cloud Anchors란?

**Cloud Anchors**는 AR 경험을 여러 사용자가 공유할 수 있게 해주는 기능입니다.

```
사용 사례:
🎮 멀티플레이어 AR 게임
🏠 협업 인테리어 디자인
🎨 공유 AR 아트
📍 위치 기반 AR 콘텐츠
```

### 🔧 Cloud Anchors 설정

```kotlin
/**
 * Cloud Anchors 활성화
 */
fun enableCloudAnchors(session: Session) {
    val config = session.config
    
    // Cloud Anchors 모드 설정
    // ENABLED: Cloud Anchors 사용
    // DISABLED: 사용 안 함
    config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
    
    session.configure(config)
    
    Log.d("CloudAnchors", "Cloud Anchors 활성화")
}
```

### 📤 Anchor 호스팅 (업로드)

```kotlin
import com.google.ar.core.Anchor
import com.google.ar.core.HostCloudAnchorFuture

/**
 * Anchor를 클라우드에 호스팅
 * 
 * @param anchor 호스팅할 Anchor
 * @param ttlDays 유효 기간 (일)
 * @return Cloud Anchor ID
 */
suspend fun hostCloudAnchor(
    session: Session,
    anchor: Anchor,
    ttlDays: Int = 1
): String? = suspendCoroutine { continuation ->
    
    // Anchor 호스팅 시작
    val future: HostCloudAnchorFuture = session.hostCloudAnchorAsync(
        anchor,
        ttlDays
    )
    
    // 호스팅 완료 대기
    future.addListener({
        val result = future.get()
        
        when (result.cloudAnchorState) {
            Anchor.CloudAnchorState.SUCCESS -> {
                // 호스팅 성공
                val cloudAnchorId = result.cloudAnchor.cloudAnchorId
                Log.d("CloudAnchors", "호스팅 성공: $cloudAnchorId")
                continuation.resume(cloudAnchorId)
            }
            
            Anchor.CloudAnchorState.ERROR_INTERNAL -> {
                Log.e("CloudAnchors", "내부 오류")
                continuation.resume(null)
            }
            
            Anchor.CloudAnchorState.ERROR_NOT_AUTHORIZED -> {
                Log.e("CloudAnchors", "권한 없음: API 키 확인 필요")
                continuation.resume(null)
            }
            
            Anchor.CloudAnchorState.ERROR_RESOURCE_EXHAUSTED -> {
                Log.e("CloudAnchors", "할당량 초과")
                continuation.resume(null)
            }
            
            else -> {
                Log.e("CloudAnchors", "호스팅 실패: ${result.cloudAnchorState}")
                continuation.resume(null)
            }
        }
    }, ContextCompat.getMainExecutor(context))
}
```

### 📥 Anchor 해결 (다운로드)

```kotlin
import com.google.ar.core.ResolveCloudAnchorFuture

/**
 * Cloud Anchor ID로 Anchor 해결
 * 
 * @param cloudAnchorId Cloud Anchor ID
 * @return 해결된 Anchor 또는 null
 */
suspend fun resolveCloudAnchor(
    session: Session,
    cloudAnchorId: String
): Anchor? = suspendCoroutine { continuation ->
    
    // Anchor 해결 시작
    val future: ResolveCloudAnchorFuture = session.resolveCloudAnchorAsync(
        cloudAnchorId
    )
    
    // 해결 완료 대기
    future.addListener({
        val result = future.get()
        
        when (result.cloudAnchorState) {
            Anchor.CloudAnchorState.SUCCESS -> {
                // 해결 성공
                val anchor = result.cloudAnchor
                Log.d("CloudAnchors", "해결 성공")
                continuation.resume(anchor)
            }
            
            Anchor.CloudAnchorState.ERROR_RESOLVING -> {
                Log.e("CloudAnchors", "해결 실패: Anchor를 찾을 수 없음")
                continuation.resume(null)
            }
            
            else -> {
                Log.e("CloudAnchors", "해결 실패: ${result.cloudAnchorState}")
                continuation.resume(null)
            }
        }
    }, ContextCompat.getMainExecutor(context))
}
```

### 🎮 멀티플레이어 AR 구현

```kotlin
/**
 * 멀티플레이어 AR 앱 예시
 */
@Composable
fun MultiplayerARScreen() {
    var isHost by remember { mutableStateOf(false) }
    var cloudAnchorId by remember { mutableStateOf<String?>(null) }
    var sharedAnchor by remember { mutableStateOf<Anchor?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // AR 화면
        Box(modifier = Modifier.weight(1f)) {
            ARCameraView(
                onFrameUpdate = { frame ->
                    // 공유된 Anchor 위치에 객체 표시
                    sharedAnchor?.let { anchor ->
                        renderSharedObject(anchor)
                    }
                }
            )
        }
        
        // 컨트롤 UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 호스트 버튼
            Button(
                onClick = {
                    isHost = true
                    // Anchor 생성 및 호스팅
                    // cloudAnchorId = hostAnchor()
                }
            ) {
                Text("방 만들기")
            }
            
            // 참가 버튼
            Button(
                onClick = {
                    // Cloud Anchor ID 입력받기
                    // sharedAnchor = resolveAnchor(cloudAnchorId)
                }
            ) {
                Text("방 참가")
            }
        }
        
        // Cloud Anchor ID 표시
        cloudAnchorId?.let { id ->
            Text(
                text = "방 코드: $id",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

---

## 성능 최적화

### ⚡ 프레임 레이트 최적화

```kotlin
/**
 * AR 성능 최적화 팁
 */
object ARPerformanceOptimization {
    
    /**
     * 1. 프레임 처리 빈도 조절
     */
    class FrameProcessor(private val targetFps: Int = 30) {
        private val frameInterval = 1000L / targetFps
        private var lastFrameTime = 0L
        
        fun shouldProcessFrame(): Boolean {
            val currentTime = System.currentTimeMillis()
            return if (currentTime - lastFrameTime >= frameInterval) {
                lastFrameTime = currentTime
                true
            } else {
                false
            }
        }
    }
    
    /**
     * 2. 평면 감지 빈도 조절
     */
    class PlaneDetectionThrottler {
        private var lastDetectionTime = 0L
        private val detectionInterval = 500L // 0.5초마다
        
        fun shouldDetectPlanes(): Boolean {
            val currentTime = System.currentTimeMillis()
            return if (currentTime - lastDetectionTime >= detectionInterval) {
                lastDetectionTime = currentTime
                true
            } else {
                false
            }
        }
    }
    
    /**
     * 3. 객체 수 제한
     */
    class ObjectLimiter(private val maxObjects: Int = 20) {
        private val objects = mutableListOf<Anchor>()
        
        fun addObject(anchor: Anchor): Boolean {
            return if (objects.size < maxObjects) {
                objects.add(anchor)
                true
            } else {
                Log.w("Performance", "최대 객체 수 도달")
                false
            }
        }
    }
    
    /**
     * 4. 메모리 관리
     */
    fun releaseUnusedResources(session: Session) {
        // 사용하지 않는 Anchor 제거
        session.allAnchors.forEach { anchor ->
            if (anchor.trackingState == TrackingState.STOPPED) {
                anchor.detach()
            }
        }
    }
}
```

---

## 고급 문제 해결

### 🔍 디버깅 도구

```kotlin
/**
 * AR 디버깅 헬퍼
 */
class ARDebugHelper(private val session: Session) {
    
    /**
     * 세션 상태 로깅
     */
    fun logSessionState(frame: Frame) {
        Log.d("ARDebug", """
            === AR 세션 상태 ===
            추적 상태: ${frame.camera.trackingState}
            추적 실패 이유: ${frame.camera.trackingFailureReason}
            감지된 평면 수: ${session.getAllTrackables(Plane::class.java).size}
            배치된 Anchor 수: ${session.allAnchors.size}
            조명 밝기: ${frame.lightEstimate.pixelIntensity}
            FPS: ${calculateFPS()}
        """.trimIndent())
    }
    
    /**
     * FPS 계산
     */
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    
    private fun calculateFPS(): Int {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastFpsTime
        
        return if (elapsed >= 1000) {
            val fps = frameCount
            frameCount = 0
            lastFpsTime = currentTime
            fps
        } else {
            0
        }
    }
}
```

---

## 🎯 다음 단계

고급편을 완료했습니다! 이제 실전 프로젝트로 넘어가세요:

**[ARCore 실전 프로젝트](./72-4-android-arcore-project.md)** - 가구 배치 앱, 측정 앱, 명함 앱 만들기

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy Advanced AR Coding! 🚀
