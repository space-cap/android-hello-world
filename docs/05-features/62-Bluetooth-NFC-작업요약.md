# Bluetooth & NFC 문서 작성 완료

## 작업 요약

Android Bluetooth와 NFC에 대한 **초보자 친화적이고 매우 상세한** 문서를 2개로 나누어 작성했습니다.

## 생성된 문서 목록

### 1. [60-android-bluetooth-guide.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/60-android-bluetooth-guide.md) (약 35KB)

**Bluetooth 기본 및 연결**

#### 주요 내용
- ✅ Bluetooth란? (Classic vs BLE)
- ✅ 권한 설정 (Android 12 이전/이후)
- ✅ Bluetooth 기본 설정
- ✅ 기기 검색
  - 페어링된 기기 목록
  - 새 기기 검색
- ✅ 페어링
  - 페어링 요청
  - 상태 모니터링
- ✅ 연결 및 통신
  - 서버/클라이언트 구현
  - 데이터 송수신
- ✅ BLE (Bluetooth Low Energy)
  - BLE 스캔
  - GATT 연결
  - 특성 읽기/쓰기
- ✅ 실전 예제 (채팅 앱)
- ✅ Jetpack Compose 통합
- ✅ 문제 해결

---

### 2. [61-android-nfc-guide.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/61-android-nfc-guide.md) (약 32KB)

**NFC 기본 및 활용**

#### 주요 내용
- ✅ NFC란? (Reader/Writer, P2P, Card Emulation)
- ✅ 권한 및 설정
  - Manifest 설정
  - 인텐트 필터
  - 기술 필터
- ✅ NFC 태그 읽기
  - Foreground Dispatch
  - NDEF 메시지 파싱
  - 텍스트/URI 레코드
- ✅ NFC 태그 쓰기
  - NDEF 메시지 생성
  - 태그에 쓰기
  - 포맷
- ✅ NDEF 메시지
  - 다양한 레코드 타입
  - vCard, Wi-Fi 설정
- ✅ Android Beam (P2P)
- ✅ 카드 에뮬레이션 (HCE)
  - APDU 처리
  - AID 설정
- ✅ 실전 예제 (명함 공유)
- ✅ Jetpack Compose 통합
- ✅ 문제 해결

---

## 문서 통계

| 항목 | 수치 |
|------|------|
| **총 문서 수** | 2개 |
| **총 용량** | 약 67KB |
| **총 라인 수** | 약 2,100줄 |
| **코드 예제** | 45개 이상 |
| **실전 예제** | 6개 이상 |

---

## 주요 학습 포인트

### 1. Bluetooth 기본

```kotlin
// Bluetooth 어댑터
val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

// 활성화 확인
if (bluetoothAdapter?.isEnabled == true) {
    // Bluetooth 사용 가능
}
```

### 2. Bluetooth 기기 검색

```kotlin
// 페어링된 기기
val pairedDevices = bluetoothAdapter.bondedDevices

// 새 기기 검색
bluetoothAdapter.startDiscovery()
```

### 3. Bluetooth 연결

```kotlin
// 서버 (수신)
val serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid)
val socket = serverSocket.accept()

// 클라이언트 (송신)
val socket = device.createRfcommSocketToServiceRecord(uuid)
socket.connect()
```

### 4. BLE 스캔

```kotlin
val bleScanner = bluetoothAdapter.bluetoothLeScanner
bleScanner.startScan(scanCallback)
```

### 5. NFC 태그 읽기

```kotlin
// Foreground Dispatch
nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)

// 태그 읽기
val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
val ndef = Ndef.get(tag)
val message = ndef.ndefMessage
```

### 6. NFC 태그 쓰기

```kotlin
// NDEF 메시지 생성
val record = NdefRecord.createTextRecord("en", "Hello NFC!")
val message = NdefMessage(arrayOf(record))

// 쓰기
ndef.writeNdefMessage(message)
```

---

## 실전 예제 하이라이트

### 1. Bluetooth 채팅

```kotlin
// 서버
val server = BluetoothServer(bluetoothAdapter)
server.start { socket ->
    val communication = BluetoothCommunication(socket)
    communication.startReceiving { message ->
        // 메시지 수신
    }
}

// 클라이언트
client.connect(device) { socket ->
    val communication = BluetoothCommunication(socket)
    communication.send("Hello!")
}
```

### 2. NFC 명함 공유

```kotlin
// vCard 생성
val vCard = """
    BEGIN:VCARD
    VERSION:3.0
    FN:홍길동
    TEL:010-1234-5678
    EMAIL:hong@example.com
    END:VCARD
""".trimIndent()

// NDEF 메시지
val record = NdefMessageBuilder().createMimeRecord("text/vcard", vCard.toByteArray())
val message = NdefMessage(arrayOf(record))

// 쓰기
nfcWriter.writeTag(tag, message)
```

---

## 학습 경로 추천

### 초급 개발자
1. **60-android-bluetooth-guide.md** 기본 개념 학습
2. 페어링된 기기 목록 표시 실습
3. **61-android-nfc-guide.md** NFC 태그 읽기 실습

### 중급 개발자
1. Bluetooth 채팅 앱 구현
2. BLE 센서 데이터 수신
3. NFC 태그 쓰기 앱 제작

### 고급 개발자
1. 복잡한 BLE 프로토콜 구현
2. HCE 결제 시스템
3. 멀티 기기 Bluetooth 네트워크

---

## 활용 분야

### 📱 Bluetooth
- **오디오**: 헤드폰, 스피커
- **웨어러블**: 스마트워치, 피트니스 밴드
- **IoT**: 센서, 스마트홈 기기
- **헬스케어**: 심박수 모니터, 혈압계
- **파일 전송**: 기기 간 데이터 공유

### 🏷️ NFC
- **결제**: 모바일 결제 시스템
- **티켓**: 교통카드, 입장권
- **인증**: 출입 통제, 도어락
- **정보**: 스마트 포스터, 제품 태그
- **페어링**: Bluetooth 빠른 연결

---

## 베스트 프랙티스

### Bluetooth

```kotlin
// ✅ 권한 확인
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12+
    requestPermissions(arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    ))
}

// ✅ 백그라운드 스레드 사용
Thread {
    socket.connect()
}.start()

// ✅ 리소스 정리
socket.close()
```

### NFC

```kotlin
// ✅ Foreground Dispatch
override fun onResume() {
    super.onResume()
    nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
}

override fun onPause() {
    super.onPause()
    nfcAdapter.disableForegroundDispatch(this)
}

// ✅ 에러 처리
try {
    ndef.writeNdefMessage(message)
} catch (e: Exception) {
    Log.e("NFC", "쓰기 실패", e)
} finally {
    ndef.close()
}
```

---

## 참고 자료

- [Bluetooth 공식 문서](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [BLE 가이드](https://developer.android.com/guide/topics/connectivity/bluetooth-le)
- [NFC 공식 문서](https://developer.android.com/guide/topics/connectivity/nfc)
- [HCE 가이드](https://developer.android.com/guide/topics/connectivity/nfc/hce)

---

**문서 작성 완료일**: 2024년 12월 1일  
**작성자**: Gemini AI Assistant
