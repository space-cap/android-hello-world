# Android Bluetooth 가이드

## 목차
1. [Bluetooth란?](#bluetooth란)
2. [권한 설정](#권한-설정)
3. [Bluetooth 기본 설정](#bluetooth-기본-설정)
4. [기기 검색](#기기-검색)
5. [페어링](#페어링)
6. [연결 및 통신](#연결-및-통신)
7. [BLE (Bluetooth Low Energy)](#ble-bluetooth-low-energy)
8. [실전 예제](#실전-예제)
9. [Jetpack Compose 통합](#jetpack-compose-통합)
10. [문제 해결](#문제-해결)

---

## Bluetooth란?

**Bluetooth**는 근거리 무선 통신 기술로, Android 기기 간 또는 기기와 주변 장치 간 데이터를 주고받을 수 있습니다.

### 사용 사례
- 🎧 **오디오 기기**: 헤드폰, 스피커 연결
- ⌚ **웨어러블**: 스마트워치, 피트니스 밴드
- 🎮 **게임 컨트롤러**: 게임패드 연결
- 📱 **파일 전송**: 기기 간 파일 공유
- 🏥 **헬스케어**: 심박수 모니터, 혈압계
- 🚗 **자동차**: 핸즈프리, 차량 진단

### Bluetooth 종류

```
Classic Bluetooth:
- 높은 데이터 전송률
- 오디오 스트리밍
- 파일 전송
- 배터리 소모 많음

BLE (Bluetooth Low Energy):
- 낮은 전력 소비
- 센서 데이터 전송
- 비콘, IoT 기기
- 배터리 효율적
```

---

## 권한 설정

### Android 12 이전

**AndroidManifest.xml**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Bluetooth 권한 -->
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    
    <!-- 기기 검색을 위한 위치 권한 (Android 6.0+) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    
    <!-- Bluetooth 필수 기능 선언 (선택) -->
    <uses-feature 
        android:name="android.hardware.bluetooth"
        android:required="true"/>
        
</manifest>
```

### Android 12 이상 (API 31+)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Bluetooth 스캔 (기기 검색) -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation"/>
    
    <!-- Bluetooth 연결 -->
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
    
    <!-- Bluetooth 광고 (주변 기기에 알림) -->
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE"/>
    
    <!-- 위치 권한 (neverForLocation 플래그 없으면 필요) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    
</manifest>
```

### 런타임 권한 요청

```kotlin
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Bluetooth 권한 요청
 */
class BluetoothActivity : AppCompatActivity() {
    
    /**
     * 권한 요청 런처
     */
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        
        if (allGranted) {
            Log.d("Bluetooth", "모든 권한 허용됨")
            initializeBluetooth()
        } else {
            Log.e("Bluetooth", "권한 거부됨")
            showPermissionDeniedDialog()
        }
    }
    
    /**
     * 필요한 권한 확인 및 요청
     */
    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Android 11 이하
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        
        // 권한 확인
        val needsPermission = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needsPermission) {
            bluetoothPermissionLauncher.launch(permissions)
        } else {
            initializeBluetooth()
        }
    }
}
```

---

## Bluetooth 기본 설정

### BluetoothAdapter 초기화

```kotlin
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager

/**
 * Bluetooth 어댑터 초기화
 */
class BluetoothHelper(private val context: Context) {
    
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }
    
    /**
     * Bluetooth 지원 여부 확인
     */
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }
    
    /**
     * Bluetooth 활성화 여부 확인
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Bluetooth 활성화 요청
     */
    fun requestEnableBluetooth(activity: Activity) {
        if (!isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
    
    companion object {
        const val REQUEST_ENABLE_BT = 1
    }
}
```

### Bluetooth 활성화 처리

```kotlin
/**
 * Bluetooth 활성화 결과 처리
 */
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    when (requestCode) {
        BluetoothHelper.REQUEST_ENABLE_BT -> {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("Bluetooth", "Bluetooth 활성화됨")
                startBluetoothOperations()
            } else {
                Log.e("Bluetooth", "Bluetooth 활성화 거부됨")
                Toast.makeText(this, "Bluetooth가 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

---

## 기기 검색

### 페어링된 기기 목록

```kotlin
import android.bluetooth.BluetoothDevice

/**
 * 페어링된 기기 목록 가져오기
 */
@SuppressLint("MissingPermission")
fun getPairedDevices(): List<BluetoothDevice> {
    return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
}

/**
 * 페어링된 기기 정보 출력
 */
fun showPairedDevices() {
    val pairedDevices = getPairedDevices()
    
    if (pairedDevices.isEmpty()) {
        Log.d("Bluetooth", "페어링된 기기 없음")
        return
    }
    
    pairedDevices.forEach { device ->
        Log.d("Bluetooth", """
            이름: ${device.name}
            주소: ${device.address}
            타입: ${getDeviceType(device.type)}
        """.trimIndent())
    }
}

/**
 * 기기 타입 문자열 변환
 */
private fun getDeviceType(type: Int): String {
    return when (type) {
        BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
        BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
        BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
        else -> "Unknown"
    }
}
```

### 새 기기 검색

```kotlin
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Bluetooth 기기 검색
 */
class BluetoothScanner(private val context: Context) {
    
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val foundDevices = mutableListOf<BluetoothDevice>()
    
    /**
     * 기기 발견 리스너
     */
    interface OnDeviceFoundListener {
        fun onDeviceFound(device: BluetoothDevice)
        fun onDiscoveryFinished()
    }
    
    private var listener: OnDeviceFoundListener? = null
    
    /**
     * BroadcastReceiver: 기기 발견 및 검색 완료 처리
     */
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                // 기기 발견
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = 
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    
                    device?.let {
                        if (!foundDevices.contains(it)) {
                            foundDevices.add(it)
                            listener?.onDeviceFound(it)
                            
                            Log.d("Bluetooth", "기기 발견: ${it.name} (${it.address})")
                        }
                    }
                }
                
                // 검색 완료
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Bluetooth", "검색 완료")
                    listener?.onDiscoveryFinished()
                }
            }
        }
    }
    
    /**
     * 기기 검색 시작
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery(listener: OnDeviceFoundListener) {
        this.listener = listener
        foundDevices.clear()
        
        // BroadcastReceiver 등록
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)
        
        // 이전 검색 취소
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        // 검색 시작 (약 12초 소요)
        val started = bluetoothAdapter.startDiscovery()
        
        if (started) {
            Log.d("Bluetooth", "기기 검색 시작")
        } else {
            Log.e("Bluetooth", "기기 검색 실패")
        }
    }
    
    /**
     * 기기 검색 중지
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // 이미 해제됨
        }
    }
    
    /**
     * 발견된 기기 목록
     */
    fun getFoundDevices(): List<BluetoothDevice> {
        return foundDevices.toList()
    }
}
```

---

## 페어링

### 페어링 요청

```kotlin
/**
 * 기기 페어링 요청
 */
@SuppressLint("MissingPermission")
fun pairDevice(device: BluetoothDevice) {
    // 이미 페어링된 경우
    if (device.bondState == BluetoothDevice.BOND_BONDED) {
        Log.d("Bluetooth", "이미 페어링된 기기")
        return
    }
    
    // 페어링 요청
    val paired = device.createBond()
    
    if (paired) {
        Log.d("Bluetooth", "페어링 요청 전송")
    } else {
        Log.e("Bluetooth", "페어링 요청 실패")
    }
}
```

### 페어링 상태 모니터링

```kotlin
/**
 * 페어링 상태 변경 리스너
 */
class PairingReceiver : BroadcastReceiver() {
    
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            val device: BluetoothDevice? = 
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            
            val state = intent.getIntExtra(
                BluetoothDevice.EXTRA_BOND_STATE,
                BluetoothDevice.ERROR
            )
            
            when (state) {
                BluetoothDevice.BOND_BONDING -> {
                    Log.d("Bluetooth", "페어링 진행 중: ${device?.name}")
                }
                
                BluetoothDevice.BOND_BONDED -> {
                    Log.d("Bluetooth", "페어링 완료: ${device?.name}")
                    // 페어링 완료 후 연결 시도
                }
                
                BluetoothDevice.BOND_NONE -> {
                    Log.d("Bluetooth", "페어링 해제: ${device?.name}")
                }
            }
        }
    }
}

// 등록
val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
registerReceiver(pairingReceiver, filter)
```

---

## 연결 및 통신

### 서버 (수신) 측

```kotlin
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Bluetooth 서버 (연결 수락)
 */
class BluetoothServer(private val bluetoothAdapter: BluetoothAdapter) {
    
    private var serverSocket: BluetoothServerSocket? = null
    private var isRunning = false
    
    // UUID: 앱 고유 식별자 (클라이언트와 동일해야 함)
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    /**
     * 서버 시작 (연결 대기)
     */
    @SuppressLint("MissingPermission")
    fun start(onConnected: (BluetoothSocket) -> Unit) {
        isRunning = true
        
        Thread {
            try {
                // 서버 소켓 생성
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "BluetoothServer",
                    uuid
                )
                
                Log.d("Bluetooth", "서버 시작, 연결 대기 중...")
                
                while (isRunning) {
                    try {
                        // 클라이언트 연결 대기 (블로킹)
                        val socket = serverSocket?.accept()
                        
                        socket?.let {
                            Log.d("Bluetooth", "클라이언트 연결됨: ${it.remoteDevice.name}")
                            onConnected(it)
                        }
                        
                    } catch (e: IOException) {
                        if (isRunning) {
                            Log.e("Bluetooth", "연결 수락 실패", e)
                        }
                        break
                    }
                }
                
            } catch (e: IOException) {
                Log.e("Bluetooth", "서버 시작 실패", e)
            }
        }.start()
    }
    
    /**
     * 서버 중지
     */
    fun stop() {
        isRunning = false
        
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.e("Bluetooth", "서버 소켓 닫기 실패", e)
        }
    }
}
```

### 클라이언트 (송신) 측

```kotlin
/**
 * Bluetooth 클라이언트 (연결 시도)
 */
class BluetoothClient {
    
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    /**
     * 기기에 연결
     */
    @SuppressLint("MissingPermission")
    fun connect(
        device: BluetoothDevice,
        onConnected: (BluetoothSocket) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Thread {
            try {
                // 소켓 생성
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                
                // 연결 시도 (블로킹)
                socket.connect()
                
                Log.d("Bluetooth", "연결 성공: ${device.name}")
                onConnected(socket)
                
            } catch (e: IOException) {
                Log.e("Bluetooth", "연결 실패", e)
                onError(e)
            }
        }.start()
    }
}
```

### 데이터 송수신

```kotlin
/**
 * Bluetooth 통신 관리
 */
class BluetoothCommunication(private val socket: BluetoothSocket) {
    
    private val inputStream: InputStream = socket.inputStream
    private val outputStream: OutputStream = socket.outputStream
    private var isRunning = false
    
    /**
     * 데이터 수신 시작
     */
    fun startReceiving(onDataReceived: (String) -> Unit) {
        isRunning = true
        
        Thread {
            val buffer = ByteArray(1024)
            
            while (isRunning) {
                try {
                    // 데이터 읽기 (블로킹)
                    val bytes = inputStream.read(buffer)
                    
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        Log.d("Bluetooth", "수신: $message")
                        onDataReceived(message)
                    }
                    
                } catch (e: IOException) {
                    if (isRunning) {
                        Log.e("Bluetooth", "수신 실패", e)
                    }
                    break
                }
            }
        }.start()
    }
    
    /**
     * 데이터 전송
     */
    fun send(message: String) {
        Thread {
            try {
                val bytes = message.toByteArray()
                outputStream.write(bytes)
                outputStream.flush()
                
                Log.d("Bluetooth", "전송: $message")
                
            } catch (e: IOException) {
                Log.e("Bluetooth", "전송 실패", e)
            }
        }.start()
    }
    
    /**
     * 연결 종료
     */
    fun close() {
        isRunning = false
        
        try {
            inputStream.close()
            outputStream.close()
            socket.close()
            
            Log.d("Bluetooth", "연결 종료")
            
        } catch (e: IOException) {
            Log.e("Bluetooth", "소켓 닫기 실패", e)
        }
    }
}
```

---

## BLE (Bluetooth Low Energy)

### BLE 스캔

```kotlin
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings

/**
 * BLE 기기 스캔
 */
class BleScanner(private val bluetoothAdapter: BluetoothAdapter) {
    
    private val bleScanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
    private val foundDevices = mutableListOf<ScanResult>()
    
    /**
     * 스캔 콜백
     */
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            if (!foundDevices.any { it.device.address == result.device.address }) {
                foundDevices.add(result)
                
                Log.d("BLE", """
                    기기 발견: ${result.device.name ?: "Unknown"}
                    주소: ${result.device.address}
                    신호 강도: ${result.rssi} dBm
                """.trimIndent())
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE", "스캔 실패: $errorCode")
        }
    }
    
    /**
     * BLE 스캔 시작
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        foundDevices.clear()
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)  // 빠른 스캔
            .build()
        
        bleScanner?.startScan(null, settings, scanCallback)
        
        Log.d("BLE", "스캔 시작")
    }
    
    /**
     * BLE 스캔 중지
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        Log.d("BLE", "스캔 중지")
    }
    
    fun getFoundDevices() = foundDevices.toList()
}
```

### BLE 연결 및 통신

```kotlin
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile

/**
 * BLE 기기 연결
 */
class BleConnection(private val context: Context) {
    
    private var bluetoothGatt: BluetoothGatt? = null
    
    /**
     * GATT 콜백
     */
    private val gattCallback = object : BluetoothGattCallback() {
        
        // 연결 상태 변경
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BLE", "연결됨")
                    // 서비스 검색
                    gatt.discoverServices()
                }
                
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BLE", "연결 해제됨")
                }
            }
        }
        
        // 서비스 발견
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "서비스 발견 완료")
                
                // 사용 가능한 서비스 출력
                gatt.services.forEach { service ->
                    Log.d("BLE", "서비스: ${service.uuid}")
                    
                    service.characteristics.forEach { characteristic ->
                        Log.d("BLE", "  특성: ${characteristic.uuid}")
                    }
                }
            }
        }
        
        // 데이터 읽기
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                Log.d("BLE", "데이터 읽기: ${data.contentToString()}")
            }
        }
        
        // 데이터 쓰기
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "데이터 쓰기 성공")
            }
        }
        
        // 알림 수신
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value
            Log.d("BLE", "알림 수신: ${data.contentToString()}")
        }
    }
    
    /**
     * BLE 기기에 연결
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }
    
    /**
     * 연결 해제
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
```

---

## 실전 예제

### 채팅 앱

```kotlin
/**
 * Bluetooth 채팅 앱
 */
class BluetoothChatActivity : AppCompatActivity() {
    
    private lateinit var bluetoothHelper: BluetoothHelper
    private var communication: BluetoothCommunication? = null
    private val messages = mutableListOf<ChatMessage>()
    
    data class ChatMessage(
        val text: String,
        val isSent: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bluetoothHelper = BluetoothHelper(this)
        
        // 권한 확인
        checkAndRequestPermissions()
    }
    
    /**
     * 서버 모드 (연결 대기)
     */
    @SuppressLint("MissingPermission")
    private fun startServer() {
        val server = BluetoothServer(bluetoothHelper.bluetoothAdapter!!)
        
        server.start { socket ->
            runOnUiThread {
                Toast.makeText(this, "상대방이 연결되었습니다", Toast.LENGTH_SHORT).show()
            }
            
            communication = BluetoothCommunication(socket)
            startCommunication()
        }
    }
    
    /**
     * 클라이언트 모드 (연결 시도)
     */
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        val client = BluetoothClient()
        
        client.connect(
            device = device,
            onConnected = { socket ->
                runOnUiThread {
                    Toast.makeText(this, "연결되었습니다", Toast.LENGTH_SHORT).show()
                }
                
                communication = BluetoothCommunication(socket)
                startCommunication()
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this, "연결 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    /**
     * 통신 시작
     */
    private fun startCommunication() {
        communication?.startReceiving { message ->
            runOnUiThread {
                messages.add(ChatMessage(message, isSent = false))
                updateChatUI()
            }
        }
    }
    
    /**
     * 메시지 전송
     */
    private fun sendMessage(text: String) {
        communication?.send(text)
        messages.add(ChatMessage(text, isSent = true))
        updateChatUI()
    }
    
    private fun updateChatUI() {
        // RecyclerView 업데이트
    }
}
```

---

## Jetpack Compose 통합

```kotlin
/**
 * Compose에서 Bluetooth 사용
 */
@Composable
fun BluetoothScreen() {
    val context = LocalContext.current
    val bluetoothHelper = remember { BluetoothHelper(context) }
    
    var isBluetoothEnabled by remember { mutableStateOf(bluetoothHelper.isBluetoothEnabled()) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Bluetooth 상태
        Text(
            text = if (isBluetoothEnabled) "Bluetooth 활성화됨" else "Bluetooth 비활성화됨",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 페어링된 기기 목록
        Text("페어링된 기기", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn {
            items(pairedDevices) { device ->
                DeviceItem(device = device)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(device: BluetoothDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.name ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = device.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * Bluetooth 문제 해결
 */
class BluetoothTroubleshooter {
    
    /**
     * 연결 실패 시
     */
    fun handleConnectionFailure(device: BluetoothDevice) {
        // 1. 페어링 상태 확인
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            Log.e("Bluetooth", "기기가 페어링되지 않음")
            // 페어링 재시도
        }
        
        // 2. 기기 거리 확인
        Log.d("Bluetooth", "기기가 범위 내에 있는지 확인하세요 (약 10m)")
        
        // 3. 재연결 시도
        Thread.sleep(1000)
        // 재연결 로직
    }
    
    /**
     * 검색 실패 시
     */
    fun handleDiscoveryFailure() {
        // 1. 위치 서비스 확인
        // 2. 권한 확인
        // 3. Bluetooth 재시작
    }
}
```

---

## 참고 자료

- [Bluetooth 공식 문서](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [BLE 가이드](https://developer.android.com/guide/topics/connectivity/bluetooth-le)
