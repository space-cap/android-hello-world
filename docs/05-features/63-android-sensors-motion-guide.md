# Android Sensors & Motion 가이드

## 목차
1. [센서란?](#센서란)
2. [센서 종류](#센서-종류)
3. [센서 기본 사용법](#센서-기본-사용법)
4. [가속도계](#가속도계)
5. [자이로스코프](#자이로스코프)
6. [나침반 (자기장 센서)](#나침반-자기장-센서)
7. [근접 센서](#근접-센서)
8. [광 센서](#광-센서)
9. [걸음 감지](#걸음-감지)
10. [센서 융합](#센서-융합)
11. [배터리 최적화](#배터리-최적화)
12. [실전 예제](#실전-예제)

---

## 센서란?

**센서(Sensor)**는 스마트폰의 물리적 상태나 환경을 감지하는 하드웨어 장치입니다.

### 사용 사례
- 🎮 **게임**: 기기를 기울여 조작
- 🏃 **피트니스**: 걸음 수, 거리 측정
- 🧭 **내비게이션**: 방향 표시
- 📱 **자동 회전**: 화면 방향 자동 전환
- 💡 **밝기 조절**: 주변 밝기에 따라 화면 밝기 조정
- 📞 **통화 중**: 얼굴 감지로 화면 끄기

---

## 센서 종류

### 모션 센서
```
가속도계 (Accelerometer):
- 3축(X, Y, Z) 가속도 측정
- 기기 움직임 감지
- 흔들기, 기울기 감지

자이로스코프 (Gyroscope):
- 3축 회전 속도 측정
- 정밀한 회전 감지
- VR, AR, 게임에 사용

중력 센서 (Gravity):
- 중력 방향 측정
- 가속도계에서 파생

선형 가속도 (Linear Acceleration):
- 중력 제외한 순수 가속도
- 가속도계에서 파생
```

### 위치 센서
```
자기장 센서 (Magnetometer):
- 지구 자기장 측정
- 나침반 기능
- 방향 감지

근접 센서 (Proximity):
- 물체와의 거리 측정
- 통화 중 화면 끄기
```

### 환경 센서
```
광 센서 (Light):
- 주변 밝기 측정
- 자동 밝기 조절

온도 센서 (Temperature):
- 주변 온도 측정

압력 센서 (Pressure):
- 대기압 측정
- 고도 계산

습도 센서 (Humidity):
- 상대 습도 측정
```

---

## 센서 기본 사용법

### 센서 매니저 초기화

```kotlin
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context

/**
 * 센서 헬퍼 클래스
 */
class SensorHelper(context: Context) {
    
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    /**
     * 사용 가능한 센서 확인
     */
    fun checkSensorAvailability(sensorType: Int): Boolean {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        return sensor != null
    }
    
    /**
     * 모든 센서 목록 가져오기
     */
    fun getAllSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }
    
    /**
     * 센서 정보 출력
     */
    fun printSensorInfo() {
        getAllSensors().forEach { sensor ->
            Log.d("Sensor", """
                이름: ${sensor.name}
                타입: ${sensor.type}
                제조사: ${sensor.vendor}
                최대 범위: ${sensor.maximumRange}
                해상도: ${sensor.resolution}
                전력 소모: ${sensor.power} mA
            """.trimIndent())
        }
    }
}
```

### 센서 리스너 등록

```kotlin
/**
 * 센서 이벤트 리스너
 */
class SensorActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 센서 매니저 초기화
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // 가속도계 센서 가져오기
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (accelerometer == null) {
            Toast.makeText(this, "가속도계를 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 센서 등록 (액티비티가 보일 때)
     */
    override fun onResume() {
        super.onResume()
        
        accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,  // 리스너
                sensor,  // 센서
                SensorManager.SENSOR_DELAY_NORMAL  // 샘플링 속도
            )
        }
    }
    
    /**
     * 센서 해제 (액티비티가 안 보일 때)
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    
    /**
     * 센서 값 변경 시 호출
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]  // X축 가속도
                val y = event.values[1]  // Y축 가속도
                val z = event.values[2]  // Z축 가속도
                
                Log.d("Sensor", "가속도: X=$x, Y=$y, Z=$z")
            }
        }
    }
    
    /**
     * 센서 정확도 변경 시 호출
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                Log.w("Sensor", "센서 정확도: 신뢰할 수 없음")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                Log.w("Sensor", "센서 정확도: 낮음")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                Log.i("Sensor", "센서 정확도: 중간")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                Log.i("Sensor", "센서 정확도: 높음")
            }
        }
    }
}
```

### 샘플링 속도

```kotlin
/**
 * 센서 샘플링 속도 선택
 */
fun registerSensorWithDelay(sensor: Sensor, delay: Int) {
    sensorManager.registerListener(this, sensor, delay)
}

// 샘플링 속도 옵션
val delays = mapOf(
    "가장 빠름" to SensorManager.SENSOR_DELAY_FASTEST,      // ~0ms (게임용)
    "게임" to SensorManager.SENSOR_DELAY_GAME,              // ~20ms
    "UI" to SensorManager.SENSOR_DELAY_UI,                  // ~60ms
    "일반" to SensorManager.SENSOR_DELAY_NORMAL             // ~200ms (배터리 절약)
)
```

---

## 가속도계

### 기본 사용

```kotlin
/**
 * 가속도계 사용 예제
 */
class AccelerometerActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    // 가속도 값
    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 0f
    
    override fun onResume() {
        super.onResume()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // 가속도 값 저장
            accelX = event.values[0]  // X축 (좌우)
            accelY = event.values[1]  // Y축 (상하)
            accelZ = event.values[2]  // Z축 (앞뒤)
            
            // 중력 가속도: 약 9.8 m/s²
            // 기기를 평평하게 놓으면 Z축이 약 9.8
            
            updateUI()
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 정확도 변경 처리
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
```

### 흔들기 감지

```kotlin
/**
 * 기기 흔들기 감지
 */
class ShakeDetector : SensorEventListener {
    
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    // 흔들기 임계값
    private val shakeThreshold = 800
    
    var onShake: (() -> Unit)? = null
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            
            // 100ms마다 체크
            if (currentTime - lastUpdate > 100) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime
                
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                // 속도 계산
                val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000
                
                if (speed > shakeThreshold) {
                    Log.d("Shake", "기기 흔들림 감지!")
                    onShake?.invoke()
                }
                
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}

// 사용 예시
val shakeDetector = ShakeDetector()
shakeDetector.onShake = {
    Toast.makeText(this, "흔들림 감지!", Toast.LENGTH_SHORT).show()
}
```

### 기울기 감지

```kotlin
/**
 * 기기 기울기 감지
 */
class TiltDetector : SensorEventListener {
    
    var onTilt: ((pitch: Float, roll: Float) -> Unit)? = null
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            // 피치 (앞뒤 기울기): -180 ~ 180도
            val pitch = Math.atan2(x, Math.sqrt(y * y + z * z)) * 180 / Math.PI
            
            // 롤 (좌우 기울기): -180 ~ 180도
            val roll = Math.atan2(y, Math.sqrt(x * x + z * z)) * 180 / Math.PI
            
            onTilt?.invoke(pitch.toFloat(), roll.toFloat())
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}
```

---

## 자이로스코프

```kotlin
/**
 * 자이로스코프 사용 예제
 */
class GyroscopeActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    
    // 회전 속도 (라디안/초)
    private var rotationX = 0f
    private var rotationY = 0f
    private var rotationZ = 0f
    
    override fun onResume() {
        super.onResume()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        if (gyroscope == null) {
            Toast.makeText(this, "자이로스코프를 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }
        
        sensorManager.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_GAME
        )
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            // 회전 속도 (라디안/초)
            rotationX = event.values[0]  // X축 회전 (피치)
            rotationY = event.values[1]  // Y축 회전 (롤)
            rotationZ = event.values[2]  // Z축 회전 (요)
            
            // 각도로 변환 (라디안 → 도)
            val angleX = Math.toDegrees(rotationX.toDouble())
            val angleY = Math.toDegrees(rotationY.toDouble())
            val angleZ = Math.toDegrees(rotationZ.toDouble())
            
            Log.d("Gyro", "회전: X=$angleX°, Y=$angleY°, Z=$angleZ°")
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
```

---

## 나침반 (자기장 센서)

```kotlin
/**
 * 나침반 구현
 */
class CompassActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    override fun onResume() {
        super.onResume()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // 가속도계와 자기장 센서 모두 필요
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
            }
        }
        
        updateOrientation()
    }
    
    /**
     * 방향 계산
     */
    private fun updateOrientation() {
        // 회전 행렬 계산
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        
        // 방향 각도 계산
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        
        // 방위각 (북쪽 기준): -180 ~ 180도
        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        
        // 피치 (앞뒤 기울기): -90 ~ 90도
        val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        
        // 롤 (좌우 기울기): -180 ~ 180도
        val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
        
        // 0~360도로 변환
        val degrees = (azimuth + 360) % 360
        
        // 방향 표시
        val direction = getDirection(degrees)
        
        Log.d("Compass", "방위각: $degrees° ($direction)")
    }
    
    /**
     * 각도를 방향으로 변환
     */
    private fun getDirection(degrees: Float): String {
        return when {
            degrees >= 337.5 || degrees < 22.5 -> "북"
            degrees >= 22.5 && degrees < 67.5 -> "북동"
            degrees >= 67.5 && degrees < 112.5 -> "동"
            degrees >= 112.5 && degrees < 157.5 -> "남동"
            degrees >= 157.5 && degrees < 202.5 -> "남"
            degrees >= 202.5 && degrees < 247.5 -> "남서"
            degrees >= 247.5 && degrees < 292.5 -> "서"
            degrees >= 292.5 && degrees < 337.5 -> "북서"
            else -> "알 수 없음"
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
```

---

## 근접 센서

```kotlin
/**
 * 근접 센서 사용 예제
 */
class ProximityActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    
    override fun onResume() {
        super.onResume()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]  // 거리 (cm)
            val maxRange = event.sensor.maximumRange
            
            if (distance < maxRange) {
                Log.d("Proximity", "물체 가까이 있음: ${distance}cm")
                // 화면 끄기 등의 동작
            } else {
                Log.d("Proximity", "물체 멀리 있음")
                // 화면 켜기 등의 동작
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
```

---

## 광 센서

```kotlin
/**
 * 광 센서 사용 예제
 */
class LightSensorActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    
    override fun onResume() {
        super.onResume()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]  // 조도 (lux)
            
            // 밝기 수준 판단
            val brightness = when {
                lux < 10 -> "매우 어두움"
                lux < 50 -> "어두움"
                lux < 500 -> "실내 조명"
                lux < 10000 -> "밝음"
                else -> "매우 밝음 (햇빛)"
            }
            
            Log.d("Light", "조도: ${lux}lux ($brightness)")
            
            // 화면 밝기 자동 조절
            adjustScreenBrightness(lux)
        }
    }
    
    /**
     * 화면 밝기 조절
     */
    private fun adjustScreenBrightness(lux: Float) {
        val brightness = when {
            lux < 10 -> 0.1f      // 10%
            lux < 50 -> 0.3f      // 30%
            lux < 500 -> 0.5f     // 50%
            lux < 10000 -> 0.7f   // 70%
            else -> 1.0f          // 100%
        }
        
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
```

---

## 걸음 감지

```kotlin
/**
 * 걸음 감지 센서
 */
class StepCounterActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var stepDetector: Sensor? = null
    
    private var totalSteps = 0
    private var previousSteps = 0
    
    override fun onResume() {
        super.onResume()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // 걸음 수 센서 (누적)
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // 걸음 감지 센서 (이벤트)
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                // 부팅 이후 총 걸음 수
                totalSteps = event.values[0].toInt()
                
                // 현재 세션의 걸음 수
                val currentSteps = totalSteps - previousSteps
                
                Log.d("Steps", "총 걸음 수: $totalSteps, 현재: $currentSteps")
            }
            
            Sensor.TYPE_STEP_DETECTOR -> {
                // 한 걸음 감지될 때마다 호출
                Log.d("Steps", "걸음 감지!")
            }
        }
    }
    
    /**
     * 걸음 수 초기화
     */
    fun resetSteps() {
        previousSteps = totalSteps
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
```

---

## 센서 융합

```kotlin
/**
 * 여러 센서를 조합하여 정확도 향상
 */
class SensorFusion : SensorEventListener {
    
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)
    
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyroscopeReading, 0, 3)
            }
        }
        
        updateFusedOrientation()
    }
    
    /**
     * 센서 융합으로 정확한 방향 계산
     */
    private fun updateFusedOrientation() {
        // 가속도계 + 자기장 센서로 회전 행렬 계산
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        
        // 방향 각도 계산
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        
        // 자이로스코프로 보정 (더 정확한 회전 감지)
        // ...
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}
```

---

## 배터리 최적화

```kotlin
/**
 * 배터리 효율적인 센서 사용
 */
class BatteryEfficientSensor {
    
    /**
     * 1. 필요할 때만 등록
     */
    fun registerOnlyWhenNeeded(sensorManager: SensorManager, sensor: Sensor, listener: SensorEventListener) {
        // ❌ onCreate에서 등록하지 말 것
        // ✅ onResume에서 등록, onPause에서 해제
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    
    /**
     * 2. 적절한 샘플링 속도 선택
     */
    fun useSuitableDelay(sensorManager: SensorManager, sensor: Sensor, listener: SensorEventListener) {
        // ❌ 항상 SENSOR_DELAY_FASTEST 사용하지 말 것
        // ✅ 용도에 맞는 속도 선택
        
        // 게임: SENSOR_DELAY_GAME
        // UI 업데이트: SENSOR_DELAY_UI
        // 일반: SENSOR_DELAY_NORMAL (가장 효율적)
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
    
    /**
     * 3. 배치 모드 사용 (Android 4.4+)
     */
    fun useBatchMode(sensorManager: SensorManager, sensor: Sensor, listener: SensorEventListener) {
        // 센서 데이터를 모아서 한번에 전달 (배터리 절약)
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            5000000  // 5초마다 배치 전달 (마이크로초)
        )
    }
}
```

---

## 실전 예제

### 수평계 (Spirit Level)

```kotlin
/**
 * 수평계 앱
 */
@Composable
fun SpiritLevelScreen() {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    
    var pitch by remember { mutableStateOf(0f) }
    var roll by remember { mutableStateOf(0f) }
    
    DisposableEffect(Unit) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
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
        
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "피치: ${pitch.toInt()}°",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "롤: ${roll.toInt()}°",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // 수평 여부 표시
        val isLevel = Math.abs(pitch) < 2 && Math.abs(roll) < 2
        
        Text(
            text = if (isLevel) "수평입니다!" else "기울어져 있습니다",
            color = if (isLevel) Color.Green else Color.Red,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
```

---

## 참고 자료

- [Sensors 공식 문서](https://developer.android.com/guide/topics/sensors/sensors_overview)
- [Motion Sensors](https://developer.android.com/guide/topics/sensors/sensors_motion)
- [Position Sensors](https://developer.android.com/guide/topics/sensors/sensors_position)
