# Android ML Kit 가이드

## 목차
1. [ML Kit이란?](#ml-kit이란)
2. [텍스트 인식 (OCR)](#텍스트-인식-ocr)
3. [바코드 스캔](#바코드-스캔)
4. [얼굴 감지](#얼굴-감지)
5. [이미지 라벨링](#이미지-라벨링)
6. [언어 식별](#언어-식별)
7. [번역](#번역)
8. [실전 예제](#실전-예제)
9. [Jetpack Compose 통합](#jetpack-compose-통합)
10. [문제 해결](#문제-해결)

---

## ML Kit이란?

**ML Kit**은 Google이 제공하는 모바일 머신러닝 SDK로, 강력한 ML 기능을 쉽게 앱에 통합할 수 있습니다.

### 주요 기능
- 📝 **텍스트 인식** (OCR): 이미지에서 텍스트 추출
- 📷 **바코드 스캔**: QR 코드, 바코드 인식
- 😊 **얼굴 감지**: 얼굴 위치, 표정 분석
- 🏷️ **이미지 라벨링**: 이미지 내용 분류
- 🌍 **언어 식별**: 텍스트 언어 감지
- 🔤 **번역**: 다국어 번역

### 사용 사례
- 📄 **문서 스캔**: 명함, 영수증, 문서
- 🛒 **쇼핑**: 바코드 스캔으로 제품 정보
- 📸 **사진 앱**: 자동 태그, 검색
- 💬 **채팅**: 자동 번역

---

## 텍스트 인식 (OCR)

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")
    
    // 한국어 지원 (선택적)
    implementation("com.google.mlkit:text-recognition-korean:16.0.0")
}
```

### 기본 사용법

```kotlin
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * 텍스트 인식 헬퍼
 */
class TextRecognitionHelper {
    
    // 텍스트 인식기 (라틴 문자)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * 이미지에서 텍스트 인식
     */
    fun recognizeText(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // InputImage 생성
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // 텍스트 인식 실행
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // 인식된 텍스트
                val text = visionText.text
                
                Log.d("TextRecognition", "인식된 텍스트: $text")
                onSuccess(text)
            }
            .addOnFailureListener { exception ->
                Log.e("TextRecognition", "텍스트 인식 실패", exception)
                onFailure(exception)
            }
    }
    
    /**
     * 상세 정보와 함께 텍스트 인식
     */
    fun recognizeTextWithDetails(
        bitmap: Bitmap,
        onSuccess: (List<TextBlock>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val blocks = visionText.textBlocks.map { block ->
                    TextBlock(
                        text = block.text,
                        boundingBox = block.boundingBox,
                        confidence = block.confidence,
                        lines = block.lines.map { line ->
                            TextLine(
                                text = line.text,
                                boundingBox = line.boundingBox
                            )
                        }
                    )
                }
                
                onSuccess(blocks)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    /**
     * 리소스 해제
     */
    fun close() {
        recognizer.close()
    }
}

/**
 * 텍스트 블록 데이터 클래스
 */
data class TextBlock(
    val text: String,
    val boundingBox: Rect?,
    val confidence: Float?,
    val lines: List<TextLine>
)

data class TextLine(
    val text: String,
    val boundingBox: Rect?
)
```

---

## 바코드 스캔

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
}
```

### 바코드 스캐너 구현

```kotlin
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * 바코드 스캐너
 */
class BarcodeScanner {
    
    private val scanner = BarcodeScanning.getClient()
    
    /**
     * 바코드 스캔
     */
    fun scanBarcode(
        bitmap: Bitmap,
        onSuccess: (List<BarcodeResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val results = barcodes.map { barcode ->
                    BarcodeResult(
                        rawValue = barcode.rawValue ?: "",
                        displayValue = barcode.displayValue ?: "",
                        format = getBarcodeFormat(barcode.format),
                        valueType = getBarcodeValueType(barcode.valueType),
                        boundingBox = barcode.boundingBox,
                        // 특정 타입별 데이터
                        url = barcode.url?.url,
                        email = barcode.email?.address,
                        phone = barcode.phone?.number,
                        wifi = barcode.wifi?.let {
                            WifiInfo(it.ssid ?: "", it.password ?: "", it.encryptionType)
                        }
                    )
                }
                
                onSuccess(results)
            }
            .addOnFailureListener { exception ->
                Log.e("BarcodeScanner", "바코드 스캔 실패", exception)
                onFailure(exception)
            }
    }
    
    /**
     * 바코드 포맷 변환
     */
    private fun getBarcodeFormat(format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            else -> "Unknown"
        }
    }
    
    /**
     * 바코드 값 타입 변환
     */
    private fun getBarcodeValueType(valueType: Int): String {
        return when (valueType) {
            Barcode.TYPE_URL -> "URL"
            Barcode.TYPE_EMAIL -> "Email"
            Barcode.TYPE_PHONE -> "Phone"
            Barcode.TYPE_SMS -> "SMS"
            Barcode.TYPE_WIFI -> "WiFi"
            Barcode.TYPE_TEXT -> "Text"
            else -> "Unknown"
        }
    }
    
    fun close() {
        scanner.close()
    }
}

/**
 * 바코드 결과
 */
data class BarcodeResult(
    val rawValue: String,
    val displayValue: String,
    val format: String,
    val valueType: String,
    val boundingBox: Rect?,
    val url: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val wifi: WifiInfo? = null
)

data class WifiInfo(
    val ssid: String,
    val password: String,
    val encryptionType: Int
)
```

---

## 얼굴 감지

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.6")
}
```

### 얼굴 감지 구현

```kotlin
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * 얼굴 감지기
 */
class FaceDetector {
    
    // 얼굴 감지 옵션
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  // 정확도 우선
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)  // 랜드마크 감지 (눈, 코, 입 등)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  // 분류 (웃음, 눈 뜸 등)
        .setMinFaceSize(0.15f)  // 최소 얼굴 크기 (이미지 대비 15%)
        .enableTracking()  // 얼굴 추적
        .build()
    
    private val detector = FaceDetection.getClient(options)
    
    /**
     * 얼굴 감지
     */
    fun detectFaces(
        bitmap: Bitmap,
        onSuccess: (List<FaceInfo>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        detector.process(image)
            .addOnSuccessListener { faces ->
                val faceInfoList = faces.map { face ->
                    FaceInfo(
                        boundingBox = face.boundingBox,
                        trackingId = face.trackingId,
                        // 회전 각도
                        headEulerAngleX = face.headEulerAngleX,  // 위아래 (pitch)
                        headEulerAngleY = face.headEulerAngleY,  // 좌우 (yaw)
                        headEulerAngleZ = face.headEulerAngleZ,  // 기울기 (roll)
                        // 표정
                        smilingProbability = face.smilingProbability,
                        leftEyeOpenProbability = face.leftEyeOpenProbability,
                        rightEyeOpenProbability = face.rightEyeOpenProbability,
                        // 랜드마크
                        landmarks = mapOf(
                            "LEFT_EYE" to face.getLandmark(FaceLandmark.LEFT_EYE)?.position,
                            "RIGHT_EYE" to face.getLandmark(FaceLandmark.RIGHT_EYE)?.position,
                            "NOSE_BASE" to face.getLandmark(FaceLandmark.NOSE_BASE)?.position,
                            "MOUTH_LEFT" to face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position,
                            "MOUTH_RIGHT" to face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
                        )
                    )
                }
                
                Log.d("FaceDetector", "${faces.size}개의 얼굴 감지됨")
                onSuccess(faceInfoList)
            }
            .addOnFailureListener { exception ->
                Log.e("FaceDetector", "얼굴 감지 실패", exception)
                onFailure(exception)
            }
    }
    
    fun close() {
        detector.close()
    }
}

/**
 * 얼굴 정보
 */
data class FaceInfo(
    val boundingBox: Rect,
    val trackingId: Int?,
    val headEulerAngleX: Float,  // -90 ~ 90
    val headEulerAngleY: Float,  // -90 ~ 90
    val headEulerAngleZ: Float,  // -180 ~ 180
    val smilingProbability: Float?,  // 0.0 ~ 1.0
    val leftEyeOpenProbability: Float?,  // 0.0 ~ 1.0
    val rightEyeOpenProbability: Float?,  // 0.0 ~ 1.0
    val landmarks: Map<String, PointF?>
)
```

---

## 이미지 라벨링

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ML Kit Image Labeling
    implementation("com.google.mlkit:image-labeling:17.0.8")
}
```

### 이미지 라벨링 구현

```kotlin
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

/**
 * 이미지 라벨러
 */
class ImageLabeler {
    
    // 라벨링 옵션
    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)  // 신뢰도 임계값 (70% 이상)
        .build()
    
    private val labeler = ImageLabeling.getClient(options)
    
    /**
     * 이미지 라벨링
     */
    fun labelImage(
        bitmap: Bitmap,
        onSuccess: (List<ImageLabel>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        labeler.process(image)
            .addOnSuccessListener { labels ->
                val imageLabels = labels.map { label ->
                    ImageLabel(
                        text = label.text,
                        confidence = label.confidence,
                        index = label.index
                    )
                }
                
                Log.d("ImageLabeler", "${labels.size}개의 라벨 감지됨")
                imageLabels.forEach { label ->
                    Log.d("ImageLabeler", "${label.text}: ${(label.confidence * 100).toInt()}%")
                }
                
                onSuccess(imageLabels)
            }
            .addOnFailureListener { exception ->
                Log.e("ImageLabeler", "이미지 라벨링 실패", exception)
                onFailure(exception)
            }
    }
    
    fun close() {
        labeler.close()
    }
}

/**
 * 이미지 라벨
 */
data class ImageLabel(
    val text: String,
    val confidence: Float,  // 0.0 ~ 1.0
    val index: Int
)
```

---

## 언어 식별

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ML Kit Language Identification
    implementation("com.google.mlkit:language-id:17.0.5")
}
```

### 언어 식별 구현

```kotlin
import com.google.mlkit.nl.languageid.LanguageIdentification

/**
 * 언어 식별기
 */
class LanguageIdentifier {
    
    private val languageIdentifier = LanguageIdentification.getClient()
    
    /**
     * 언어 식별
     */
    fun identifyLanguage(
        text: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Log.w("LanguageId", "언어를 식별할 수 없습니다")
                    onSuccess("알 수 없음")
                } else {
                    val languageName = getLanguageName(languageCode)
                    Log.d("LanguageId", "식별된 언어: $languageName ($languageCode)")
                    onSuccess(languageName)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LanguageId", "언어 식별 실패", exception)
                onFailure(exception)
            }
    }
    
    /**
     * 가능한 언어 목록 (신뢰도 포함)
     */
    fun identifyPossibleLanguages(
        text: String,
        onSuccess: (List<LanguageConfidence>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        languageIdentifier.identifyPossibleLanguages(text)
            .addOnSuccessListener { identifiedLanguages ->
                val languages = identifiedLanguages.map { language ->
                    LanguageConfidence(
                        languageCode = language.languageTag,
                        languageName = getLanguageName(language.languageTag),
                        confidence = language.confidence
                    )
                }
                
                onSuccess(languages)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    /**
     * 언어 코드를 이름으로 변환
     */
    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "ko" -> "한국어"
            "en" -> "영어"
            "ja" -> "일본어"
            "zh" -> "중국어"
            "es" -> "스페인어"
            "fr" -> "프랑스어"
            "de" -> "독일어"
            else -> Locale(languageCode).displayLanguage
        }
    }
    
    fun close() {
        languageIdentifier.close()
    }
}

data class LanguageConfidence(
    val languageCode: String,
    val languageName: String,
    val confidence: Float
)
```

---

## 번역

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ML Kit Translate
    implementation("com.google.mlkit:translate:17.0.2")
}
```

### 번역 구현

```kotlin
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * 번역기
 */
class Translator {
    
    /**
     * 번역 (한국어 → 영어)
     */
    fun translateKoreanToEnglish(
        text: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onDownloadProgress: (Int) -> Unit = {}
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        
        val translator = Translation.getClient(options)
        
        // 모델 다운로드 확인
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                // 번역 실행
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        Log.d("Translator", "번역 완료: $translatedText")
                        onSuccess(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Translator", "번역 실패", exception)
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("Translator", "모델 다운로드 실패", exception)
                onFailure(exception)
            }
    }
    
    /**
     * 다국어 번역
     */
    fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()
        
        val translator = Translation.getClient(options)
        
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }
}
```

---

## 실전 예제

### 명함 스캐너

```kotlin
/**
 * 명함 스캐너
 */
class BusinessCardScanner(private val context: Context) {
    
    private val textRecognizer = TextRecognitionHelper()
    
    /**
     * 명함 스캔
     */
    fun scanBusinessCard(
        bitmap: Bitmap,
        onSuccess: (BusinessCard) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        textRecognizer.recognizeText(
            bitmap = bitmap,
            onSuccess = { text ->
                // 텍스트에서 정보 추출
                val businessCard = extractBusinessCardInfo(text)
                onSuccess(businessCard)
            },
            onFailure = onFailure
        )
    }
    
    /**
     * 명함 정보 추출
     */
    private fun extractBusinessCardInfo(text: String): BusinessCard {
        val lines = text.split("\n")
        
        var name: String? = null
        var company: String? = null
        var email: String? = null
        var phone: String? = null
        var address: String? = null
        
        lines.forEach { line ->
            when {
                // 이메일 패턴
                line.contains("@") -> {
                    email = line.trim()
                }
                
                // 전화번호 패턴
                line.matches(Regex(".*\\d{2,4}-\\d{3,4}-\\d{4}.*")) -> {
                    phone = line.trim()
                }
                
                // 회사명 (예: "주식회사", "Co.", "Inc." 포함)
                line.contains("주식회사") || line.contains("Co.") || line.contains("Inc.") -> {
                    company = line.trim()
                }
                
                // 첫 번째 줄을 이름으로 추정
                name == null && line.isNotBlank() -> {
                    name = line.trim()
                }
            }
        }
        
        return BusinessCard(
            name = name,
            company = company,
            email = email,
            phone = phone,
            address = address,
            rawText = text
        )
    }
}

data class BusinessCard(
    val name: String?,
    val company: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val rawText: String
)
```

---

## Jetpack Compose 통합

### 바코드 스캐너 화면

```kotlin
/**
 * Compose 바코드 스캐너
 */
@Composable
fun BarcodeScannerScreen() {
    val context = LocalContext.current
    val barcodeScanner = remember { BarcodeScanner() }
    
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var barcodeType by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            barcodeScanner.scanBarcode(
                bitmap = it,
                onSuccess = { results ->
                    if (results.isNotEmpty()) {
                        scannedCode = results[0].displayValue
                        barcodeType = results[0].format
                    }
                },
                onFailure = { exception ->
                    Log.e("Barcode", "스캔 실패", exception)
                }
            )
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "바코드",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        scannedCode?.let { code ->
            Text(
                text = "스캔 결과",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = code,
                style = MaterialTheme.typography.headlineSmall
            )
            
            barcodeType?.let { type ->
                Text(
                    text = "타입: $type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(onClick = {
            launcher.launch(null)
        }) {
            Text("바코드 스캔")
        }
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * ML Kit 문제 해결
 */
class MLKitTroubleshooter {
    
    /**
     * 1. 모델 다운로드 실패
     */
    fun handleModelDownloadFailed() {
        // 네트워크 연결 확인
        // Google Play 서비스 업데이트 확인
        // 저장 공간 확인
    }
    
    /**
     * 2. 인식 정확도 낮음
     */
    fun improveAccuracy() {
        // 이미지 품질 향상 (해상도, 조명)
        // 적절한 옵션 설정 (PERFORMANCE_MODE_ACCURATE)
        // 신뢰도 임계값 조정
    }
    
    /**
     * 3. 성능 문제
     */
    fun improvePerformance() {
        // PERFORMANCE_MODE_FAST 사용
        // 이미지 크기 축소
        // 백그라운드 스레드에서 처리
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. 리소스 해제
override fun onDestroy() {
    textRecognizer.close()
    barcodeScanner.close()
}

// 2. 에러 처리
recognizer.process(image)
    .addOnFailureListener { exception ->
        handleError(exception)
    }

// 3. 백그라운드 처리
viewModelScope.launch(Dispatchers.IO) {
    recognizeText(bitmap)
}

// 4. 신뢰도 확인
if (label.confidence > 0.7f) {
    // 높은 신뢰도
}

// 5. 모델 다운로드 확인
translator.downloadModelIfNeeded()
    .addOnSuccessListener { translate() }
```

### DON'Ts ❌

```kotlin
// 1. 리소스 해제 안 함
// ❌ 메모리 누수

// 2. 메인 스레드에서 처리
recognizeText(bitmap)  // ❌ UI 블록킹

// 3. 에러 처리 안 함
// ❌ 앱 크래시

// 4. 너무 큰 이미지
val hugeBitmap = loadHugeImage()  // ❌ 성능 저하

// 5. 모델 다운로드 확인 안 함
translator.translate(text)  // ❌ 모델 없으면 실패
```

---

## 참고 자료

- [ML Kit 공식 문서](https://developers.google.com/ml-kit)
- [Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
