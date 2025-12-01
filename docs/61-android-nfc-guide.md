# Android NFC 가이드

## 목차
1. [NFC란?](#nfc란)
2. [권한 및 설정](#권한-및-설정)
3. [NFC 태그 읽기](#nfc-태그-읽기)
4. [NFC 태그 쓰기](#nfc-태그-쓰기)
5. [NDEF 메시지](#ndef-메시지)
6. [Android Beam (P2P)](#android-beam-p2p)
7. [카드 에뮬레이션 (HCE)](#카드-에뮬레이션-hce)
8. [실전 예제](#실전-예제)
9. [Jetpack Compose 통합](#jetpack-compose-통합)
10. [문제 해결](#문제-해결)

---

## NFC란?

**NFC (Near Field Communication)**는 13.56MHz 주파수를 사용하는 근거리 무선 통신 기술입니다.

### 특징
- 📏 **통신 거리**: 약 4cm 이내
- ⚡ **빠른 연결**: 0.1초 이내
- 🔒 **보안**: 짧은 거리로 도청 어려움
- 💡 **전력 효율**: 수동 태그는 전원 불필요

### 사용 사례
- 💳 **결제**: 삼성페이, 구글페이
- 🎫 **티켓**: 교통카드, 입장권
- 🏷️ **제품 정보**: 스마트 포스터, 제품 태그
- 🔑 **출입 통제**: 도어락, 사무실 출입
- 📱 **기기 페어링**: Bluetooth 빠른 연결
- 📤 **파일 공유**: Android Beam

### NFC 모드

```
1. Reader/Writer Mode:
   - NFC 태그 읽기/쓰기
   - 가장 일반적인 사용

2. Peer-to-Peer Mode:
   - 기기 간 데이터 교환
   - Android Beam

3. Card Emulation Mode:
   - 스마트폰을 카드처럼 사용
   - 모바일 결제
```

---

## 권한 및 설정

### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- NFC 권한 -->
    <uses-permission android:name="android.permission.NFC"/>
    
    <!-- NFC 필수 기능 선언 -->
    <uses-feature 
        android:name="android.hardware.nfc"
        android:required="true"/>
    
    <application>
        <activity android:name=".NfcActivity">
            
            <!-- NFC 인텐트 필터 -->
            <intent-filter>
                <action android:name="android.nfc.action.NDEF_DISCOVERED"/>
                <category android:name="android.intent.category.DEFAULT"/>
                <!-- 특정 MIME 타입 -->
                <data android:mimeType="text/plain"/>
            </intent-filter>
            
            <!-- 모든 NDEF 태그 -->
            <intent-filter>
                <action android:name="android.nfc.action.TECH_DISCOVERED"/>
            </intent-filter>
            
            <!-- 기술 목록 -->
            <meta-data
                android:name="android.nfc.action.TECH_DISCOVERED"
                android:resource="@xml/nfc_tech_filter"/>
            
            <!-- 태그 발견 (최후 수단) -->
            <intent-filter>
                <action android:name="android.nfc.action.TAG_DISCOVERED"/>
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
            
        </activity>
    </application>
    
</manifest>
```

### NFC 기술 필터

**res/xml/nfc_tech_filter.xml**:
```xml
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <tech-list>
        <!-- NDEF 지원 태그 -->
        <tech>android.nfc.tech.Ndef</tech>
    </tech-list>
    
    <tech-list>
        <!-- NDEF 포맷 가능한 태그 -->
        <tech>android.nfc.tech.NdefFormatable</tech>
    </tech-list>
    
    <tech-list>
        <!-- Mifare Classic -->
        <tech>android.nfc.tech.MifareClassic</tech>
    </tech-list>
    
    <tech-list>
        <!-- Mifare Ultralight -->
        <tech>android.nfc.tech.MifareUltralight</tech>
    </tech-list>
</resources>
```

### NFC 어댑터 초기화

```kotlin
import android.nfc.NfcAdapter
import android.nfc.NfcManager

/**
 * NFC 헬퍼 클래스
 */
class NfcHelper(private val context: Context) {
    
    private val nfcManager: NfcManager by lazy {
        context.getSystemService(Context.NFC_SERVICE) as NfcManager
    }
    
    val nfcAdapter: NfcAdapter? by lazy {
        nfcManager.defaultAdapter
    }
    
    /**
     * NFC 지원 여부 확인
     */
    fun isNfcSupported(): Boolean {
        return nfcAdapter != null
    }
    
    /**
     * NFC 활성화 여부 확인
     */
    fun isNfcEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * NFC 설정 화면으로 이동
     */
    fun openNfcSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
        activity.startActivity(intent)
    }
}
```

---

## NFC 태그 읽기

### Foreground Dispatch 설정

```kotlin
import android.app.PendingIntent
import android.nfc.NfcAdapter
import android.nfc.Tag

/**
 * NFC 태그 읽기 Activity
 */
class NfcReadActivity : AppCompatActivity() {
    
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        // PendingIntent 생성 (앱이 포그라운드에 있을 때 NFC 인텐트 수신)
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        
        // 인텐트로 전달된 태그 처리
        handleIntent(intent)
    }
    
    /**
     * 앱이 포그라운드에 있을 때 NFC 활성화
     */
    override fun onResume() {
        super.onResume()
        
        // Foreground Dispatch 활성화
        nfcAdapter.enableForegroundDispatch(
            this,
            pendingIntent,
            null,  // 인텐트 필터 (null = 모든 태그)
            null   // 기술 목록 (null = 모든 기술)
        )
    }
    
    /**
     * 앱이 백그라운드로 갈 때 NFC 비활성화
     */
    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }
    
    /**
     * 새 인텐트 수신 (태그 감지)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    /**
     * NFC 태그 처리
     */
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                // 태그 객체 가져오기
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                tag?.let { readTag(it) }
            }
        }
    }
    
    /**
     * 태그 읽기
     */
    private fun readTag(tag: Tag) {
        Log.d("NFC", "태그 ID: ${tag.id.toHexString()}")
        Log.d("NFC", "지원 기술: ${tag.techList.joinToString()}")
        
        // NDEF 메시지 읽기
        readNdefMessage(tag)
    }
}

/**
 * ByteArray를 Hex 문자열로 변환
 */
fun ByteArray.toHexString(): String {
    return joinToString("") { "%02X".format(it) }
}
```

### NDEF 메시지 읽기

```kotlin
import android.nfc.tech.Ndef
import android.nfc.NdefMessage
import android.nfc.NdefRecord

/**
 * NDEF 메시지 읽기
 */
fun readNdefMessage(tag: Tag) {
    val ndef = Ndef.get(tag)
    
    if (ndef == null) {
        Log.e("NFC", "NDEF를 지원하지 않는 태그")
        return
    }
    
    try {
        // 태그 연결
        ndef.connect()
        
        // NDEF 메시지 읽기
        val ndefMessage = ndef.ndefMessage
        
        if (ndefMessage == null) {
            Log.d("NFC", "빈 태그")
            return
        }
        
        // 레코드 파싱
        ndefMessage.records.forEach { record ->
            parseNdefRecord(record)
        }
        
    } catch (e: Exception) {
        Log.e("NFC", "태그 읽기 실패", e)
    } finally {
        try {
            ndef.close()
        } catch (e: Exception) {
            Log.e("NFC", "태그 닫기 실패", e)
        }
    }
}

/**
 * NDEF 레코드 파싱
 */
fun parseNdefRecord(record: NdefRecord) {
    when (record.tnf) {
        NdefRecord.TNF_WELL_KNOWN -> {
            when {
                // 텍스트 레코드
                record.type.contentEquals(NdefRecord.RTD_TEXT) -> {
                    val text = parseTextRecord(record)
                    Log.d("NFC", "텍스트: $text")
                }
                
                // URI 레코드
                record.type.contentEquals(NdefRecord.RTD_URI) -> {
                    val uri = parseUriRecord(record)
                    Log.d("NFC", "URI: $uri")
                }
            }
        }
        
        NdefRecord.TNF_MIME_MEDIA -> {
            val mimeType = String(record.type)
            Log.d("NFC", "MIME 타입: $mimeType")
        }
    }
}

/**
 * 텍스트 레코드 파싱
 */
fun parseTextRecord(record: NdefRecord): String {
    val payload = record.payload
    
    // 언어 코드 길이
    val languageCodeLength = payload[0].toInt() and 0x3F
    
    // 텍스트 추출
    return String(
        payload,
        languageCodeLength + 1,
        payload.size - languageCodeLength - 1,
        Charsets.UTF_8
    )
}

/**
 * URI 레코드 파싱
 */
fun parseUriRecord(record: NdefRecord): String {
    val payload = record.payload
    
    // URI 식별자 코드
    val uriIdentifier = payload[0].toInt() and 0xFF
    
    // URI 프리픽스
    val uriPrefix = when (uriIdentifier) {
        0x01 -> "http://www."
        0x02 -> "https://www."
        0x03 -> "http://"
        0x04 -> "https://"
        else -> ""
    }
    
    // URI 추출
    val uriBytes = payload.copyOfRange(1, payload.size)
    return uriPrefix + String(uriBytes, Charsets.UTF_8)
}
```

---

## NFC 태그 쓰기

### NDEF 메시지 생성

```kotlin
/**
 * NDEF 메시지 생성 헬퍼
 */
class NdefMessageBuilder {
    
    /**
     * 텍스트 레코드 생성
     */
    fun createTextRecord(text: String, languageCode: String = "en"): NdefRecord {
        val languageCodeBytes = languageCode.toByteArray(Charsets.US_ASCII)
        val textBytes = text.toByteArray(Charsets.UTF_8)
        
        val payload = ByteArray(1 + languageCodeBytes.size + textBytes.size)
        
        // 상태 바이트 (언어 코드 길이)
        payload[0] = languageCodeBytes.size.toByte()
        
        // 언어 코드
        System.arraycopy(languageCodeBytes, 0, payload, 1, languageCodeBytes.size)
        
        // 텍스트
        System.arraycopy(
            textBytes, 0, payload,
            1 + languageCodeBytes.size,
            textBytes.size
        )
        
        return NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            ByteArray(0),
            payload
        )
    }
    
    /**
     * URI 레코드 생성
     */
    fun createUriRecord(uri: String): NdefRecord {
        // URI 프리픽스 확인
        val (prefix, uriIdentifier) = when {
            uri.startsWith("http://www.") -> Pair("http://www.", 0x01.toByte())
            uri.startsWith("https://www.") -> Pair("https://www.", 0x02.toByte())
            uri.startsWith("http://") -> Pair("http://", 0x03.toByte())
            uri.startsWith("https://") -> Pair("https://", 0x04.toByte())
            else -> Pair("", 0x00.toByte())
        }
        
        val uriWithoutPrefix = uri.removePrefix(prefix)
        val uriBytes = uriWithoutPrefix.toByteArray(Charsets.UTF_8)
        
        val payload = ByteArray(1 + uriBytes.size)
        payload[0] = uriIdentifier
        System.arraycopy(uriBytes, 0, payload, 1, uriBytes.size)
        
        return NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_URI,
            ByteArray(0),
            payload
        )
    }
    
    /**
     * Android 앱 레코드 생성 (앱 실행)
     */
    fun createApplicationRecord(packageName: String): NdefRecord {
        return NdefRecord.createApplicationRecord(packageName)
    }
    
    /**
     * MIME 타입 레코드 생성
     */
    fun createMimeRecord(mimeType: String, data: ByteArray): NdefRecord {
        return NdefRecord(
            NdefRecord.TNF_MIME_MEDIA,
            mimeType.toByteArray(Charsets.US_ASCII),
            ByteArray(0),
            data
        )
    }
}
```

### 태그에 쓰기

```kotlin
import android.nfc.tech.NdefFormatable

/**
 * NFC 태그에 쓰기
 */
class NfcWriter {
    
    /**
     * NDEF 메시지를 태그에 쓰기
     */
    fun writeTag(tag: Tag, message: NdefMessage): Boolean {
        // NDEF 태그인 경우
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            return writeNdefTag(ndef, message)
        }
        
        // 포맷 가능한 태그인 경우
        val ndefFormatable = NdefFormatable.get(tag)
        if (ndefFormatable != null) {
            return formatAndWriteTag(ndefFormatable, message)
        }
        
        Log.e("NFC", "쓰기를 지원하지 않는 태그")
        return false
    }
    
    /**
     * NDEF 태그에 쓰기
     */
    private fun writeNdefTag(ndef: Ndef, message: NdefMessage): Boolean {
        try {
            ndef.connect()
            
            // 쓰기 가능 여부 확인
            if (!ndef.isWritable) {
                Log.e("NFC", "읽기 전용 태그")
                return false
            }
            
            // 용량 확인
            val size = message.toByteArray().size
            if (ndef.maxSize < size) {
                Log.e("NFC", "태그 용량 부족: $size bytes > ${ndef.maxSize} bytes")
                return false
            }
            
            // 쓰기
            ndef.writeNdefMessage(message)
            
            Log.d("NFC", "쓰기 성공")
            return true
            
        } catch (e: Exception) {
            Log.e("NFC", "쓰기 실패", e)
            return false
        } finally {
            try {
                ndef.close()
            } catch (e: Exception) {
                Log.e("NFC", "태그 닫기 실패", e)
            }
        }
    }
    
    /**
     * 포맷 후 쓰기
     */
    private fun formatAndWriteTag(
        ndefFormatable: NdefFormatable,
        message: NdefMessage
    ): Boolean {
        try {
            ndefFormatable.connect()
            ndefFormatable.format(message)
            
            Log.d("NFC", "포맷 및 쓰기 성공")
            return true
            
        } catch (e: Exception) {
            Log.e("NFC", "포맷 실패", e)
            return false
        } finally {
            try {
                ndefFormatable.close()
            } catch (e: Exception) {
                Log.e("NFC", "태그 닫기 실패", e)
            }
        }
    }
}
```

---

## NDEF 메시지

### 실전 예제

```kotlin
/**
 * 다양한 NDEF 메시지 생성 예제
 */
class NdefExamples {
    
    private val builder = NdefMessageBuilder()
    
    /**
     * 텍스트 메시지
     */
    fun createTextMessage(text: String): NdefMessage {
        val record = builder.createTextRecord(text)
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * URL 메시지
     */
    fun createUrlMessage(url: String): NdefMessage {
        val record = builder.createUriRecord(url)
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * 연락처 정보 (vCard)
     */
    fun createContactMessage(name: String, phone: String, email: String): NdefMessage {
        val vCard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:$name
            TEL:$phone
            EMAIL:$email
            END:VCARD
        """.trimIndent()
        
        val record = builder.createMimeRecord("text/vcard", vCard.toByteArray())
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * Wi-Fi 설정
     */
    fun createWifiMessage(ssid: String, password: String): NdefMessage {
        // Wi-Fi Simple Configuration 포맷
        val wifiConfig = """
            WIFI:T:WPA;S:$ssid;P:$password;;
        """.trimIndent()
        
        val record = builder.createTextRecord(wifiConfig)
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * 앱 실행
     */
    fun createAppLaunchMessage(packageName: String): NdefMessage {
        val record = builder.createApplicationRecord(packageName)
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * 복합 메시지 (여러 레코드)
     */
    fun createMultiRecordMessage(): NdefMessage {
        val textRecord = builder.createTextRecord("Hello NFC!")
        val uriRecord = builder.createUriRecord("https://www.example.com")
        val appRecord = builder.createApplicationRecord("com.example.app")
        
        return NdefMessage(arrayOf(textRecord, uriRecord, appRecord))
    }
}
```

---

## Android Beam (P2P)

> **참고**: Android Beam은 Android 10 (API 29)부터 deprecated되었습니다.

```kotlin
/**
 * Android Beam (기기 간 데이터 전송)
 */
class AndroidBeamHelper(private val activity: Activity) {
    
    private val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    
    /**
     * Beam 메시지 설정
     */
    fun setupBeam(message: NdefMessage) {
        nfcAdapter?.setNdefPushMessage(message, activity)
    }
    
    /**
     * 동적 Beam 메시지
     */
    fun setupDynamicBeam() {
        nfcAdapter?.setNdefPushMessageCallback(
            object : NfcAdapter.CreateNdefMessageCallback {
                override fun createNdefMessage(event: NfcEvent): NdefMessage {
                    // 전송할 메시지 생성
                    val text = "현재 시간: ${System.currentTimeMillis()}"
                    val record = NdefMessageBuilder().createTextRecord(text)
                    return NdefMessage(arrayOf(record))
                }
            },
            activity
        )
    }
    
    /**
     * Beam 완료 콜백
     */
    fun setBeamCallback() {
        nfcAdapter?.setOnNdefPushCompleteCallback(
            object : NfcAdapter.OnNdefPushCompleteCallback {
                override fun onNdefPushComplete(event: NfcEvent) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "전송 완료", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            activity
        )
    }
}
```

---

## 카드 에뮬레이션 (HCE)

### Host Card Emulation 설정

```kotlin
import android.nfc.cardemulation.HostApduService
import android.os.Bundle

/**
 * HCE 서비스 (스마트폰을 카드처럼 사용)
 */
class MyHceService : HostApduService() {
    
    /**
     * APDU 명령 수신
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d("HCE", "명령 수신: ${commandApdu.toHexString()}")
        
        // SELECT 명령 처리
        if (isSelectCommand(commandApdu)) {
            return handleSelectCommand(commandApdu)
        }
        
        // 기타 명령 처리
        return handleOtherCommand(commandApdu)
    }
    
    /**
     * 서비스 비활성화
     */
    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "비활성화: $reason")
    }
    
    /**
     * SELECT 명령 확인
     */
    private fun isSelectCommand(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x00.toByte() && apdu[1] == 0xA4.toByte()
    }
    
    /**
     * SELECT 명령 처리
     */
    private fun handleSelectCommand(apdu: ByteArray): ByteArray {
        // 성공 응답 (0x9000)
        return byteArrayOf(0x90.toByte(), 0x00.toByte())
    }
    
    /**
     * 기타 명령 처리
     */
    private fun handleOtherCommand(apdu: ByteArray): ByteArray {
        // 명령 미지원 (0x6D00)
        return byteArrayOf(0x6D.toByte(), 0x00.toByte())
    }
}
```

**AndroidManifest.xml**:
```xml
<service
    android:name=".MyHceService"
    android:exported="true"
    android:permission="android.permission.BIND_NFC_SERVICE">
    
    <intent-filter>
        <action android:name="android.nfc.cardemulation.action.HOST_APDU_SERVICE"/>
    </intent-filter>
    
    <meta-data
        android:name="android.nfc.cardemulation.host_apdu_service"
        android:resource="@xml/apduservice"/>
        
</service>
```

**res/xml/apduservice.xml**:
```xml
<host-apdu-service 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/service_description"
    android:requireDeviceUnlock="false">
    
    <aid-group 
        android:description="@string/aid_group_description"
        android:category="other">
        
        <!-- AID (Application ID) -->
        <aid-filter android:name="F0010203040506"/>
        
    </aid-group>
    
</host-apdu-service>
```

---

## 실전 예제

### 명함 공유 앱

```kotlin
/**
 * NFC 명함 공유
 */
class BusinessCardActivity : AppCompatActivity() {
    
    private lateinit var nfcHelper: NfcHelper
    private lateinit var nfcWriter: NfcWriter
    
    data class BusinessCard(
        val name: String,
        val company: String,
        val phone: String,
        val email: String,
        val website: String
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcHelper = NfcHelper(this)
        nfcWriter = NfcWriter()
        
        if (!nfcHelper.isNfcSupported()) {
            Toast.makeText(this, "NFC를 지원하지 않는 기기", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        if (!nfcHelper.isNfcEnabled()) {
            showEnableNfcDialog()
        }
    }
    
    /**
     * 명함 정보를 NDEF 메시지로 변환
     */
    private fun createBusinessCardMessage(card: BusinessCard): NdefMessage {
        val vCard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:${card.name}
            ORG:${card.company}
            TEL:${card.phone}
            EMAIL:${card.email}
            URL:${card.website}
            END:VCARD
        """.trimIndent()
        
        val builder = NdefMessageBuilder()
        val record = builder.createMimeRecord("text/vcard", vCard.toByteArray())
        
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * 명함 쓰기
     */
    private fun writeBusinessCard(tag: Tag, card: BusinessCard) {
        val message = createBusinessCardMessage(card)
        val success = nfcWriter.writeTag(tag, message)
        
        runOnUiThread {
            if (success) {
                Toast.makeText(this, "명함 저장 완료", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "명함 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

---

## Jetpack Compose 통합

```kotlin
/**
 * Compose에서 NFC 사용
 */
@Composable
fun NfcScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    val nfcHelper = remember { NfcHelper(context) }
    var nfcEnabled by remember { mutableStateOf(nfcHelper.isNfcEnabled()) }
    var tagData by remember { mutableStateOf<String?>(null) }
    
    // NFC 상태 확인
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nfcEnabled = nfcHelper.isNfcEnabled()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // NFC 상태
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (nfcEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        ) {
            Text(
                text = if (nfcEnabled) "NFC 활성화됨" else "NFC 비활성화됨",
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 설정 버튼
        if (!nfcEnabled) {
            Button(onClick = {
                activity?.let { nfcHelper.openNfcSettings(it) }
            }) {
                Text("NFC 설정 열기")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 태그 데이터
        tagData?.let { data ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "태그 데이터",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(data)
                }
            }
        }
    }
}
```

---

## 문제 해결

### 일반적인 문제

```kotlin
/**
 * NFC 문제 해결
 */
class NfcTroubleshooter {
    
    /**
     * 태그 읽기 실패
     */
    fun handleReadFailure() {
        Log.d("NFC", """
            문제 해결 방법:
            1. 태그를 기기 뒷면 중앙에 가까이 대세요
            2. 태그를 천천히 움직여보세요
            3. 기기 케이스를 제거해보세요
            4. NFC가 활성화되어 있는지 확인하세요
        """.trimIndent())
    }
    
    /**
     * 쓰기 실패
     */
    fun handleWriteFailure(tag: Tag) {
        val ndef = Ndef.get(tag)
        
        if (ndef == null) {
            Log.e("NFC", "NDEF를 지원하지 않는 태그")
            return
        }
        
        if (!ndef.isWritable) {
            Log.e("NFC", "읽기 전용 태그")
            return
        }
        
        Log.d("NFC", "태그 용량: ${ndef.maxSize} bytes")
    }
}
```

---

## 참고 자료

- [NFC 공식 문서](https://developer.android.com/guide/topics/connectivity/nfc)
- [NDEF 스펙](https://nfc-forum.org/our-work/specification-releases/)
- [HCE 가이드](https://developer.android.com/guide/topics/connectivity/nfc/hce)
