# 지도 및 위치 기반 서비스 완벽 가이드

## 📚 목차

1. [위치 기반 서비스란?](#위치-기반-서비스란)
2. [Google Maps 시작하기](#google-maps-시작하기)
3. [현재 위치 가져오기](#현재-위치-가져오기)
4. [지도에 마커 표시](#지도에-마커-표시)
5. [위치 추적](#위치-추적)
6. [Geofencing](#geofencing)
7. [장소 검색](#장소-검색)
8. [실전 예제](#실전-예제)

---

## 위치 기반 서비스란?

> [!NOTE]
> **위치 기반 서비스 (LBS) = 사용자의 위치를 활용하는 서비스**
> 
> **주요 기능:**
> - 📍 현재 위치 표시
> - 🗺️ 지도 표시
> - 🚗 경로 안내
> - 📌 주변 장소 검색
> - 🔔 특정 위치 도착 알림

### 실제 사용 사례

**배달 앱:**
```
사용자 위치 → 주변 음식점 표시 → 배달 거리 계산
```

**택시 앱:**
```
현재 위치 → 가까운 택시 표시 → 실시간 위치 추적
```

**날씨 앱:**
```
현재 위치 → 해당 지역 날씨 표시
```

**통계:**
- 모바일 앱의 **74%**가 위치 서비스 사용
- 위치 기반 광고의 전환율: 일반 광고의 **2배**

---

## Google Maps 시작하기

> [!IMPORTANT]
> **Google Maps를 사용하려면 API 키가 필요합니다!**
> 
> API 키 없이는 지도가 표시되지 않습니다.

### 1단계: Google Cloud Console 설정

**매우 상세한 단계:**

1. **Google Cloud Console 접속**
   ```
   https://console.cloud.google.com 접속
   Google 계정으로 로그인
   ```

2. **프로젝트 생성**
   ```
   상단의 프로젝트 선택 → "새 프로젝트"
   ↓
   프로젝트 이름 입력 (예: "MyMapApp")
   ↓
   "만들기" 클릭
   ```

3. **Maps SDK 활성화**
   ```
   왼쪽 메뉴 → "API 및 서비스" → "라이브러리"
   ↓
   "Maps SDK for Android" 검색
   ↓
   클릭 → "사용 설정" 클릭
   ```

4. **API 키 생성**
   ```
   왼쪽 메뉴 → "API 및 서비스" → "사용자 인증 정보"
   ↓
   "+ 사용자 인증 정보 만들기" → "API 키"
   ↓
   API 키 생성됨! (예: AIzaSyD...)
   ```

5. **API 키 제한 (보안)**
   ```
   생성된 키 옆의 편집 아이콘 클릭
   ↓
   "애플리케이션 제한사항" → "Android 앱"
   ↓
   패키지 이름 입력: com.example.myapp
   ↓
   SHA-1 인증서 지문 추가
   ↓
   "저장" 클릭
   ```

**SHA-1 지문 얻는 방법:**
```bash
# Windows (PowerShell)
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# macOS/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# 출력에서 SHA1 찾기
# SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

### 2단계: Android 프로젝트 설정

#### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    
    // 위치 서비스
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Maps Compose (Jetpack Compose용)
    implementation("com.google.maps.android:maps-compose:4.3.0")
}
```

**각 라이브러리 설명:**
```
play-services-maps: Google Maps 핵심 기능
play-services-location: 위치 추적 기능
maps-compose: Jetpack Compose에서 지도 사용
```

#### AndroidManifest.xml 설정

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <!-- 위치 권한 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- 인터넷 권한 (지도 타일 다운로드) -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application>
        <!-- API 키 설정 -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_API_KEY_HERE" />
        
        <activity ... />
    </application>
</manifest>
```

**⚠️ 중요: API 키 보안**
```kotlin
// ❌ 하드코딩하지 마세요!
android:value="AIzaSyD..."

// ✅ local.properties 사용
// local.properties
MAPS_API_KEY=AIzaSyD...

// build.gradle.kts
android {
    defaultConfig {
        val properties = Properties()
        properties.load(FileInputStream(rootProject.file("local.properties")))
        
        manifestPlaceholders["MAPS_API_KEY"] = 
            properties.getProperty("MAPS_API_KEY", "")
    }
}

// AndroidManifest.xml
android:value="${MAPS_API_KEY}"
```

### 3단계: 권한 요청

```kotlin
@Composable
fun LocationPermissionRequest(
    onPermissionGranted: () -> Unit
) {
    // 위치 권한 상태
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // 권한 요청
    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }
    
    // 권한 상태에 따른 UI
    when {
        // 모든 권한 허용됨
        locationPermissionState.allPermissionsGranted -> {
            onPermissionGranted()
        }
        
        // 권한 설명 필요
        locationPermissionState.shouldShowRationale -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("위치 권한 필요") },
                text = {
                    Text(
                        "지도에 현재 위치를 표시하려면 위치 권한이 필요합니다.\n\n" +
                        "• 정확한 위치: 지도에 정확한 위치 표시\n" +
                        "• 대략적인 위치: 대략적인 위치만 표시"
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        locationPermissionState.launchMultiplePermissionRequest()
                    }) {
                        Text("권한 허용")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { /* 취소 */ }) {
                        Text("취소")
                    }
                }
            )
        }
        
        // 권한 거부됨
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "위치 권한이 거부되었습니다",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "설정에서 위치 권한을 허용해주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
```

### 4단계: 기본 지도 표시

```kotlin
@Composable
fun BasicMapScreen() {
    // 서울 시청 좌표
    val seoul = LatLng(37.5665, 126.9780)
    
    // 카메라 위치 상태
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(seoul, 15f)
    }
    
    // 지도 표시
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            // 지도 타입: 일반, 위성, 지형 등
            mapType = MapType.NORMAL,
            // 현재 위치 버튼 표시
            isMyLocationEnabled = false
        ),
        uiSettings = MapUiSettings(
            // 줌 컨트롤 표시
            zoomControlsEnabled = true,
            // 나침반 표시
            compassEnabled = true,
            // 내 위치 버튼 표시
            myLocationButtonEnabled = true
        )
    )
}
```

**지도 구성 요소 설명:**
```
LatLng: 위도(Latitude), 경도(Longitude) 좌표
  - 위도: -90 ~ 90 (남극 ~ 북극)
  - 경도: -180 ~ 180 (서쪽 ~ 동쪽)

CameraPosition: 지도 카메라 위치
  - target: 중심 좌표
  - zoom: 확대 레벨 (1 ~ 21)
    - 1: 세계 전체
    - 10: 도시
    - 15: 거리
    - 20: 건물

MapType:
  - NORMAL: 일반 지도
  - SATELLITE: 위성 사진
  - TERRAIN: 지형도
  - HYBRID: 위성 + 도로
```

---

## 현재 위치 가져오기

### FusedLocationProviderClient 사용

```kotlin
@Composable
fun CurrentLocationScreen() {
    val context = LocalContext.current
    
    // 위치 제공자 클라이언트
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    // 현재 위치 상태
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 현재 위치 가져오기 함수
    fun getCurrentLocation() {
        // 권한 체크
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            errorMessage = "위치 권한이 필요합니다"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        // 마지막으로 알려진 위치 가져오기 (빠름)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                isLoading = false
                
                if (location != null) {
                    // 위치 성공
                    currentLocation = LatLng(location.latitude, location.longitude)
                    Log.d("Location", "위치: ${location.latitude}, ${location.longitude}")
                } else {
                    // 위치 없음 → 새로 요청
                    requestNewLocation(fusedLocationClient, context) { newLocation ->
                        currentLocation = LatLng(newLocation.latitude, newLocation.longitude)
                        isLoading = false
                    }
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "위치를 가져올 수 없습니다: ${e.message}"
                Log.e("Location", "위치 가져오기 실패", e)
            }
    }
    
    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        // 지도
        currentLocation?.let { location ->
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(location, 17f)
            }
            
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true
                )
            ) {
                // 현재 위치에 마커 표시
                Marker(
                    state = MarkerState(position = location),
                    title = "현재 위치",
                    snippet = "여기 있어요!"
                )
            }
        }
        
        // 로딩 표시
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 에러 메시지
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(error)
            }
        }
        
        // 현재 위치 버튼
        FloatingActionButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.MyLocation, "현재 위치")
        }
    }
}

// 새 위치 요청 (정확한 위치)
@SuppressLint("MissingPermission")
fun requestNewLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationReceived: (Location) -> Unit
) {
    // 위치 요청 설정
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,  // 높은 정확도
        10000  // 10초마다 업데이트
    ).apply {
        setMinUpdateIntervalMillis(5000)  // 최소 5초 간격
        setMaxUpdates(1)  // 1번만 받기
    }.build()
    
    // 위치 콜백
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                onLocationReceived(location)
                // 콜백 제거 (1번만 받기)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }
    
    // 위치 업데이트 요청
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}
```

**위치 가져오기 과정:**
```
1. lastLocation 시도 (빠름, 캐시된 위치)
   ↓
2. 위치 있음? → 사용
   ↓
3. 위치 없음? → requestLocationUpdates (정확한 위치)
   ↓
4. GPS/네트워크로 위치 측정
   ↓
5. 콜백으로 위치 반환
```

---

## 지도에 마커 표시

### 기본 마커

```kotlin
@Composable
fun MapWithMarkers() {
    // 여러 장소 좌표
    val places = remember {
        listOf(
            Place("서울 시청", LatLng(37.5665, 126.9780)),
            Place("남산타워", LatLng(37.5512, 126.9882)),
            Place("경복궁", LatLng(37.5796, 126.9770)),
            Place("강남역", LatLng(37.4979, 127.0276))
        )
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(places[0].location, 12f)
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // 각 장소에 마커 표시
        places.forEach { place ->
            Marker(
                state = MarkerState(position = place.location),
                title = place.name,
                snippet = "탭하여 상세 정보 보기",
                // 마커 클릭 이벤트
                onClick = { marker ->
                    Log.d("Map", "${place.name} 클릭됨")
                    marker.showInfoWindow()  // 정보 창 표시
                    true  // 이벤트 소비
                }
            )
        }
    }
}

data class Place(
    val name: String,
    val location: LatLng
)
```

### 커스텀 마커

```kotlin
@Composable
fun MapWithCustomMarkers() {
    val context = LocalContext.current
    
    // 커스텀 마커 아이콘 생성
    val customIcon = remember {
        bitmapDescriptorFromVector(
            context,
            R.drawable.ic_custom_marker,
            Color.Red
        )
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 15f)
        }
    ) {
        Marker(
            state = MarkerState(position = LatLng(37.5665, 126.9780)),
            title = "커스텀 마커",
            icon = customIcon,  // 커스텀 아이콘
            alpha = 0.8f,  // 투명도
            rotation = 45f  // 회전 (도)
        )
    }
}

// Vector Drawable을 BitmapDescriptor로 변환
fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
    tint: Color? = null
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    
    // 색상 적용
    tint?.let {
        vectorDrawable.setTint(it.toArgb())
    }
    
    vectorDrawable.setBounds(
        0,
        0,
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight
    )
    
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
```

### 마커 클러스터링

```kotlin
// 의존성 추가
implementation("com.google.maps.android:android-maps-utils:3.8.0")

@Composable
fun MapWithClustering() {
    // 많은 마커들
    val items = remember {
        List(100) { index ->
            ClusterItem(
                position = LatLng(
                    37.5 + (Math.random() - 0.5) * 0.1,
                    127.0 + (Math.random() - 0.5) * 0.1
                ),
                title = "장소 $index",
                snippet = "설명 $index"
            )
        }
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(37.5, 127.0), 11f)
        }
    ) {
        // 클러스터링 (많은 마커를 그룹화)
        Clustering(
            items = items,
            onClusterClick = { cluster ->
                Log.d("Map", "클러스터 클릭: ${cluster.size}개 아이템")
                false
            },
            onClusterItemClick = { item ->
                Log.d("Map", "아이템 클릭: ${item.title}")
                false
            }
        )
    }
}
```

---

## 위치 추적

### 실시간 위치 업데이트

```kotlin
@Composable
fun LocationTrackingScreen() {
    val context = LocalContext.current
    
    // 위치 제공자
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    // 위치 경로 (이동 경로)
    var locationPath by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var isTracking by remember { mutableStateOf(false) }
    
    // 위치 추적 시작/중지
    fun toggleTracking() {
        if (isTracking) {
            // 추적 중지
            stopLocationUpdates(fusedLocationClient)
            isTracking = false
        } else {
            // 추적 시작
            startLocationUpdates(
                fusedLocationClient,
                context,
                onLocationUpdate = { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    currentLocation = newLocation
                    locationPath = locationPath + newLocation
                }
            )
            isTracking = true
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 지도
        currentLocation?.let { location ->
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(location, 17f)
            }
            
            // 현재 위치로 카메라 이동
            LaunchedEffect(location) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLng(location),
                    durationMs = 1000
                )
            }
            
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true
                )
            ) {
                // 이동 경로 그리기
                if (locationPath.size >= 2) {
                    Polyline(
                        points = locationPath,
                        color = Color.Blue,
                        width = 10f
                    )
                }
                
                // 현재 위치 마커
                Marker(
                    state = MarkerState(position = location),
                    title = "현재 위치"
                )
            }
        }
        
        // 추적 시작/중지 버튼
        FloatingActionButton(
            onClick = { toggleTracking() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (isTracking) Color.Red else Color.Blue
        ) {
            Icon(
                if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                if (isTracking) "추적 중지" else "추적 시작"
            )
        }
        
        // 추적 정보
        if (isTracking) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "위치 추적 중",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "이동 거리: ${calculateDistance(locationPath)} m",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// 위치 업데이트 시작
@SuppressLint("MissingPermission")
fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationUpdate: (Location) -> Unit
) {
    // 위치 요청 설정
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,  // 높은 정확도 (GPS 사용)
        5000  // 5초마다 업데이트
    ).apply {
        setMinUpdateIntervalMillis(2000)  // 최소 2초 간격
        setMinUpdateDistanceMeters(10f)  // 최소 10m 이동 시 업데이트
    }.build()
    
    // 위치 콜백
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                onLocationUpdate(location)
            }
        }
    }
    
    // 위치 업데이트 요청
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}

// 위치 업데이트 중지
fun stopLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
    fusedLocationClient.removeLocationUpdates(locationCallback)
}

// 이동 거리 계산
fun calculateDistance(path: List<LatLng>): Int {
    if (path.size < 2) return 0
    
    var totalDistance = 0f
    for (i in 0 until path.size - 1) {
        val results = FloatArray(1)
        Location.distanceBetween(
            path[i].latitude,
            path[i].longitude,
            path[i + 1].latitude,
            path[i + 1].longitude,
            results
        )
        totalDistance += results[0]
    }
    
    return totalDistance.toInt()
}
```

**위치 추적 설정 설명:**
```
Priority.PRIORITY_HIGH_ACCURACY:
- GPS 사용 (가장 정확)
- 배터리 소모 높음
- 실내에서 작동 안될 수 있음

Priority.PRIORITY_BALANCED_POWER_ACCURACY:
- WiFi + 기지국 사용
- 배터리 소모 중간
- 정확도 중간 (~100m)

Priority.PRIORITY_LOW_POWER:
- 기지국만 사용
- 배터리 소모 낮음
- 정확도 낮음 (~1km)

setMinUpdateDistanceMeters(10f):
- 10m 이상 이동해야 업데이트
- 불필요한 업데이트 방지
- 배터리 절약
```

---

## Geofencing

> [!NOTE]
> **Geofencing = 특정 지역 진입/이탈 감지**
> 
> **사용 사례:**
> - 집 근처 도착 시 알림
> - 매장 근처 할인 쿠폰 발송
> - 특정 지역 진입 시 자동 체크인

### Geofence 설정

```kotlin
@Composable
fun GeofencingScreen() {
    val context = LocalContext.current
    
    // Geofencing 클라이언트
    val geofencingClient = remember {
        LocationServices.getGeofencingClient(context)
    }
    
    // Geofence 추가
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float  // 반경 (미터)
    ) {
        // Geofence 생성
        val geofence = Geofence.Builder()
            .setRequestId(id)  // 고유 ID
            .setCircularRegion(
                latitude,
                longitude,
                radius  // 반경 (예: 100m)
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)  // 만료 시간
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or  // 진입
                Geofence.GEOFENCE_TRANSITION_EXIT      // 이탈
            )
            .build()
        
        // Geofence 요청
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)  // 초기 트리거
            .addGeofence(geofence)
            .build()
        
        // PendingIntent (Geofence 이벤트 수신)
        val geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        // Geofence 등록
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d("Geofence", "Geofence 추가 성공: $id")
                    Toast.makeText(context, "Geofence 추가됨", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Geofence", "Geofence 추가 실패", e)
                    Toast.makeText(context, "Geofence 추가 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Geofencing 설정",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                // 서울 시청 주변 100m Geofence
                addGeofence(
                    id = "seoul_city_hall",
                    latitude = 37.5665,
                    longitude = 126.9780,
                    radius = 100f
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("서울 시청 Geofence 추가")
        }
    }
}

// Geofence 이벤트 수신
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e("Geofence", "Geofence 에러")
            return
        }
        
        // 트랜지션 타입 (진입/이탈)
        val geofenceTransition = geofencingEvent.geofenceTransition
        
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                // 지역 진입
                Log.d("Geofence", "지역 진입!")
                showNotification(context, "지역 진입", "목적지에 도착했습니다")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                // 지역 이탈
                Log.d("Geofence", "지역 이탈!")
                showNotification(context, "지역 이탈", "목적지를 떠났습니다")
            }
        }
        
        // 트리거된 Geofence 목록
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        triggeringGeofences?.forEach { geofence ->
            Log.d("Geofence", "트리거된 Geofence: ${geofence.requestId}")
        }
    }
    
    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        
        // 알림 채널 생성 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "geofence_channel",
                "Geofence 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        // 알림 생성
        val notification = NotificationCompat.Builder(context, "geofence_channel")
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

**Geofence 동작 과정:**
```
1. Geofence 등록 (위치, 반경)
   ↓
2. 시스템이 백그라운드에서 위치 모니터링
   ↓
3. 사용자가 Geofence 진입/이탈
   ↓
4. BroadcastReceiver로 이벤트 전달
   ↓
5. 알림 표시 또는 액션 실행
```

---

## 장소 검색

### Places API 사용

```kotlin
// 의존성 추가
implementation("com.google.android.libraries.places:places:3.3.0")

// Places API 초기화 (Application 클래스)
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "YOUR_API_KEY")
        }
    }
}

@Composable
fun PlaceSearchScreen() {
    val context = LocalContext.current
    
    // Places 클라이언트
    val placesClient = remember {
        Places.createClient(context)
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    
    // 장소 검색
    fun searchPlaces(query: String) {
        // AutocompleteSessionToken 생성
        val token = AutocompleteSessionToken.newInstance()
        
        // 검색 요청
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()
        
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                searchResults = response.autocompletePredictions
            }
            .addOnFailureListener { e ->
                Log.e("Places", "검색 실패", e)
            }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 검색창
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.length >= 3) {
                    searchPlaces(query)
                }
            },
            label = { Text("장소 검색") },
            leadingIcon = {
                Icon(Icons.Filled.Search, null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 검색 결과
        LazyColumn {
            items(searchResults) { prediction ->
                PlaceResultItem(
                    prediction = prediction,
                    onClick = {
                        // 장소 선택
                        Log.d("Places", "선택: ${prediction.getFullText(null)}")
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceResultItem(
    prediction: AutocompletePrediction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = prediction.getPrimaryText(null).toString(),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = prediction.getSecondaryText(null).toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
```

---

## 실전 예제

### 완전한 지도 앱

```kotlin
@Composable
fun CompleteMapApp() {
    var currentScreen by remember { mutableStateOf(MapScreen.MAP) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == MapScreen.MAP,
                    onClick = { currentScreen = MapScreen.MAP },
                    icon = { Icon(Icons.Filled.Map, null) },
                    label = { Text("지도") }
                )
                NavigationBarItem(
                    selected = currentScreen == MapScreen.TRACKING,
                    onClick = { currentScreen = MapScreen.TRACKING },
                    icon = { Icon(Icons.Filled.DirectionsRun, null) },
                    label = { Text("추적") }
                )
                NavigationBarItem(
                    selected = currentScreen == MapScreen.SEARCH,
                    onClick = { currentScreen = MapScreen.SEARCH },
                    icon = { Icon(Icons.Filled.Search, null) },
                    label = { Text("검색") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                MapScreen.MAP -> BasicMapScreen()
                MapScreen.TRACKING -> LocationTrackingScreen()
                MapScreen.SEARCH -> PlaceSearchScreen()
            }
        }
    }
}

enum class MapScreen {
    MAP, TRACKING, SEARCH
}
```

---

## 💡 베스트 프랙티스

### 1. 배터리 최적화

```kotlin
// ✅ 적절한 우선순위 선택
val locationRequest = LocationRequest.Builder(
    Priority.PRIORITY_BALANCED_POWER_ACCURACY,  // 배터리 절약
    60000  // 1분마다 (자주 업데이트 불필요)
).build()

// ❌ 항상 높은 정확도 사용
val locationRequest = LocationRequest.Builder(
    Priority.PRIORITY_HIGH_ACCURACY,  // 배터리 소모 높음
    1000  // 1초마다 (너무 자주)
).build()
```

### 2. 권한 체크

```kotlin
// ✅ 사용 전 항상 권한 확인
if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
) {
    // 위치 사용
}
```

### 3. 생명주기 관리

```kotlin
// ✅ 화면 종료 시 위치 업데이트 중지
DisposableEffect(Unit) {
    onDispose {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Navigate the World! 🗺️
