# Android ARCore 가이드

## 목차
1. [ARCore란?](#arcore란)
2. [기본 설정](#기본-설정)
3. [평면 감지](#평면-감지)
4. [3D 객체 배치](#3d-객체-배치)
5. [조명 추정](#조명-추정)
6. [Cloud Anchors](#cloud-anchors)
7. [실전 예제](#실전-예제)
8. [문제 해결](#문제-해결)

---

## ARCore란?

**ARCore**는 Google의 증강 현실(AR) 플랫폼으로, 실제 환경에 가상 객체를 배치할 수 있습니다.

### 주요 기능
- 🎯 **평면 감지**: 바닥, 벽, 테이블 등 감지
- 📦 **3D 객체 배치**: 가상 객체 배치 및 상호작용
- 💡 **조명 추정**: 실제 조명에 맞춘 렌더링
- ☁️ **Cloud Anchors**: 여러 기기 간 AR 경험 공유

### 사용 사례
- 🛋️ **가구 배치**: IKEA Place
- 🎮 **AR 게임**: Pokemon GO
- 📏 **측정**: Google Measure
- 🎨 **AR 아트**: 가상 그래피티

---

## 기본 설정

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // ARCore
    implementation("com.google.ar:core:1.41.0")
    
    // Sceneform (3D 렌더링)
    implementation("com.google.ar.sceneform.ux:sceneform-ux:1.17.1")
}
```

### AndroidManifest.xml

```xml
<manifest>
    <!-- ARCore 필수 -->
    <uses-permission android:name="android.permission.CAMERA"/>
    
    <!-- ARCore 기능 선언 -->
    <uses-feature android:name="android.hardware.camera.ar" android:required="true"/>
    
    <application>
        <!-- ARCore 메타데이터 -->
        <meta-data
            android:name="com.google.ar.core"
            android:value="required"/>
    </application>
</manifest>
```

### ARCore 세션 초기화

```kotlin
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.Config

/**
 * ARCore 헬퍼
 */
class ARCoreHelper(private val activity: Activity) {
    
    private var session: Session? = null
    
    /**
     * ARCore 지원 확인
     */
    fun checkARCoreSupport(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(activity)
        
        return when (availability) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                // ARCore 설치 요청
                requestARCoreInstall()
                false
            }
            else -> false
        }
    }
    
    /**
     * ARCore 설치 요청
     */
    private fun requestARCoreInstall() {
        try {
            ArCoreApk.getInstance().requestInstall(activity, true)
        } catch (e: Exception) {
            Log.e("ARCore", "ARCore 설치 실패", e)
        }
    }
    
    /**
     * 세션 생성
     */
    fun createSession(): Session? {
        return try {
            Session(activity).also { session ->
                // 설정
                val config = Config(session).apply {
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
                }
                
                session.configure(config)
                this.session = session
            }
        } catch (e: Exception) {
            Log.e("ARCore", "세션 생성 실패", e)
            null
        }
    }
    
    /**
     * 세션 해제
     */
    fun destroySession() {
        session?.close()
        session = null
    }
}
```

---

## 평면 감지

```kotlin
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState

/**
 * 평면 감지
 */
fun detectPlanes(frame: Frame): List<PlaneInfo> {
    val planes = frame.getUpdatedTrackables(Plane::class.java)
    
    return planes
        .filter { it.trackingState == TrackingState.TRACKING }
        .map { plane ->
            PlaneInfo(
                type = when (plane.type) {
                    Plane.Type.HORIZONTAL_UPWARD_FACING -> "수평 (위)"
                    Plane.Type.HORIZONTAL_DOWNWARD_FACING -> "수평 (아래)"
                    Plane.Type.VERTICAL -> "수직"
                    else -> "알 수 없음"
                },
                centerPose = plane.centerPose,
                extentX = plane.extentX,
                extentZ = plane.extentZ,
                polygon = plane.polygon
            )
        }
}

data class PlaneInfo(
    val type: String,
    val centerPose: Pose,
    val extentX: Float,
    val extentZ: Float,
    val polygon: FloatBuffer
)
```

---

## 3D 객체 배치

```kotlin
/**
 * 3D 객체 배치
 */
class ARObjectPlacer(private val session: Session) {
    
    /**
     * 평면에 객체 배치
     */
    fun placeObject(
        hitResult: HitResult,
        modelUri: Uri,
        onSuccess: (Anchor) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Anchor 생성
            val anchor = hitResult.createAnchor()
            
            // 3D 모델 로드 및 렌더링
            // (Sceneform 사용)
            
            onSuccess(anchor)
        } catch (e: Exception) {
            Log.e("ARObjectPlacer", "객체 배치 실패", e)
            onFailure(e)
        }
    }
}
```

---

## 조명 추정

```kotlin
/**
 * 조명 추정
 */
fun estimateLighting(frame: Frame): LightEstimate? {
    val lightEstimate = frame.lightEstimate
    
    return if (lightEstimate.state == LightEstimate.State.VALID) {
        LightEstimate(
            pixelIntensity = lightEstimate.pixelIntensity,
            colorCorrection = lightEstimate.colorCorrection
        )
    } else {
        null
    }
}

data class LightEstimate(
    val pixelIntensity: Float,
    val colorCorrection: FloatArray
)
```

---

## 참고 자료

- [ARCore 공식 문서](https://developers.google.com/ar)
- [ARCore Samples](https://github.com/google-ar/arcore-android-sdk)
