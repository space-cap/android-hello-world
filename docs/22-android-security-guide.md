# Android 보안 가이드

> [!NOTE]
> **이 문서는 새로운 종합 가이드 시리즈로 대체되었습니다!**
> 
> 1. **[22-1. Android 보안 기초](./22-1-android-security-basics.md)** - 데이터 암호화, 네트워크 보안
> 2. **[22-2. Android 보안 고급](./22-2-android-security-advanced.md)** - API 키 관리, 인증, 코드 보호
> 3. **[22-3. Android 보안 테스팅](./22-3-android-security-testing.md)** - 보안 테스팅 도구, 실전 시나리오

---

## 🚀 빠른 시작

**[👉 22-1. Android 보안 기초로 이동](./22-1-android-security-basics.md)**

---

**마지막 업데이트**: 2024-12-03


---

## 보안 개요

> [!CAUTION]
> **보안은 선택이 아닌 필수입니다**
> 
> 2023년 기준, 모바일 앱의 **43%가 최소 1개 이상의 보안 취약점**을 가지고 있습니다.
> 
> **보안 침해 시 피해:**
> - 사용자 데이터 유출
> - 법적 책임 (GDPR, 개인정보보호법)
> - 브랜드 신뢰도 하락
> - 재정적 손실

### Android 보안 위협 모델

```
┌─────────────────────────────────────────┐
│ 위협 레벨 1: 네트워크 공격              │
│ - 중간자 공격 (MITM)                    │
│ - 패킷 스니핑                           │
│ - DNS 스푸핑                            │
├─────────────────────────────────────────┤
│ 위협 레벨 2: 앱 레벨 공격               │
│ - 리버스 엔지니어링                     │
│ - 코드 변조                             │
│ - API 키 추출                           │
├─────────────────────────────────────────┤
│ 위협 레벨 3: 데이터 공격                │
│ - 로컬 데이터 접근                      │
│ - 백업 데이터 유출                      │
│ - 메모리 덤프                           │
├─────────────────────────────────────────┤
│ 위협 레벨 4: 사용자 공격                │
│ - 피싱                                  │
│ - 소셜 엔지니어링                       │
│ - 악성 앱 설치                          │
└─────────────────────────────────────────┘
```

---

## 데이터 암호화

> [!IMPORTANT]
> **암호화가 필요한 이유**
> 
> **암호화 없이 저장된 데이터는:**
> - 루팅된 기기에서 쉽게 접근 가능
> - 백업 파일에서 평문으로 노출
> - 메모리 덤프로 추출 가능
> 
> **암호화된 데이터는:**
> - 키 없이는 해독 불가능
> - 데이터 유출 시에도 안전
> - 규정 준수 (GDPR, HIPAA 등)

### 암호화 기초 개념

#### 대칭 암호화 vs 비대칭 암호화

**대칭 암호화 (AES):**
```
평문 + 키 → [암호화] → 암호문
암호문 + 같은 키 → [복호화] → 평문

장점: 빠름 (대용량 데이터에 적합)
단점: 키 공유 문제
```

**비대칭 암호화 (RSA):**
```
평문 + 공개키 → [암호화] → 암호문
암호문 + 개인키 → [복호화] → 평문

장점: 키 공유 안전
단점: 느림 (작은 데이터만)
```

**실무 조합:**
```
1. RSA로 AES 키 암호화 (작은 데이터)
2. AES로 실제 데이터 암호화 (큰 데이터)
```

### Android Keystore 시스템

**Keystore가 중요한 이유:**

```
일반 저장소:
앱 데이터 → SharedPreferences → 파일 시스템
                ↓
         루팅 시 접근 가능!

Keystore 사용:
앱 데이터 → 암호화 → Keystore (하드웨어 보안)
                         ↓
                  추출 불가능!
```

**Keystore의 보안 레벨:**
1. **Hardware-backed** (최고): TEE/Secure Element에 저장
2. **Software-backed**: 소프트웨어로 보호
3. **StrongBox** (최신): 전용 보안 칩

#### EncryptedSharedPreferences 사용

```kotlin
// 의존성
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// 왜 EncryptedSharedPreferences인가?
// 1. 자동 암호화/복호화
// 2. Keystore 통합
// 3. 사용법이 일반 SharedPreferences와 동일

@Composable
fun SecureDataStorage() {
    val context = LocalContext.current
    
    // MasterKey 생성 (Keystore에 저장됨)
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    // EncryptedSharedPreferences 생성
    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // 사용 (일반 SharedPreferences와 동일)
    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString("auth_token", token)
            .apply()
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }
}
```

**내부 동작:**
```
저장 시:
"auth_token" → AES256_SIV 암호화 → "Xk9mP..."
"my_token_123" → AES256_GCM 암호화 → "7hQw2..."

읽기 시:
"Xk9mP..." → 복호화 → "auth_token"
"7hQw2..." → 복호화 → "my_token_123"
```

#### 파일 암호화

```kotlin
// EncryptedFile 사용
fun encryptFile(context: Context, fileName: String, data: ByteArray) {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    val file = File(context.filesDir, fileName)
    val encryptedFile = EncryptedFile.Builder(
        context,
        file,
        masterKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()
    
    // 암호화하여 저장
    encryptedFile.openFileOutput().use { outputStream ->
        outputStream.write(data)
    }
}

fun decryptFile(context: Context, fileName: String): ByteArray {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    val file = File(context.filesDir, fileName)
    val encryptedFile = EncryptedFile.Builder(
        context,
        file,
        masterKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()
    
    // 복호화하여 읽기
    return encryptedFile.openFileInput().use { inputStream ->
        inputStream.readBytes()
    }
}
```

**성능 고려사항:**
- 암호화/복호화: ~1-5ms (작은 파일)
- 대용량 파일: 스트리밍 방식 사용
- 메모리: 원본 + 암호문 = 2배 필요

---

## 안전한 API 키 관리

> [!DANGER]
> **API 키 노출의 위험성**
> 
> **실제 사례:**
> - GitHub에 업로드된 AWS 키로 $50,000 청구
> - 노출된 Firebase 키로 데이터베이스 삭제
> - API 키 탈취로 무제한 요청 (DDoS)
> 
> **공격자가 API 키를 찾는 방법:**
> 1. APK 디컴파일 (5분 소요)
> 2. strings.xml 검색
> 3. BuildConfig 확인
> 4. 네트워크 트래픽 분석

### 잘못된 API 키 저장 방법

```kotlin
// ❌ 절대 하지 말 것 #1: 코드에 하드코딩
class ApiService {
    private val API_KEY = "sk_live_51H..." // 디컴파일로 즉시 노출!
}

// ❌ 절대 하지 말 것 #2: strings.xml
<string name="api_key">sk_live_51H...</string>

// ❌ 절대 하지 말 것 #3: BuildConfig
buildConfigField("String", "API_KEY", "\"sk_live_51H...\"")

// ❌ 절대 하지 말 것 #4: SharedPreferences (평문)
sharedPreferences.edit()
    .putString("api_key", "sk_live_51H...")
    .apply()
```

**왜 안전하지 않은가?**
```
APK 다운로드
    ↓
apktool d app.apk (디컴파일)
    ↓
grep -r "api_key" (검색)
    ↓
API 키 발견! (5분 소요)
```

### 올바른 API 키 관리 방법

#### 1. NDK/JNI 사용 (중급)

**원리:** C/C++ 네이티브 코드에 저장하여 디컴파일 난이도 증가

```kotlin
// build.gradle.kts
android {
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
        }
    }
}
```

```cpp
// native-lib.cpp
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_app_NativeLib_getApiKey(
    JNIEnv* env,
    jobject /* this */) {
    
    // 난독화된 키 (여전히 추출 가능하지만 어려움)
    std::string api_key = "sk_live_51H...";
    return env->NewStringUTF(api_key.c_str());
}
```

```kotlin
// Kotlin에서 사용
object NativeLib {
    init {
        System.loadLibrary("native-lib")
    }
    
    external fun getApiKey(): String
}

// 사용
val apiKey = NativeLib.getApiKey()
```

**보안 수준:** ⭐⭐⭐ (중간)
- 장점: Java 디컴파일보다 어려움
- 단점: 여전히 추출 가능 (strings 명령어, 메모리 덤프)

#### 2. 서버 프록시 사용 (권장)

**원리:** API 키를 서버에만 저장, 앱은 서버를 통해 요청

```
[앱] → [내 서버] → [외부 API]
         ↑
      API 키 저장
```

```kotlin
// 앱에는 API 키 없음
class ApiService {
    suspend fun getData(): Result<Data> {
        // 내 서버로 요청
        return try {
            val response = httpClient.get("https://myserver.com/api/data")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

```javascript
// 서버 (Node.js 예시)
app.get('/api/data', async (req, res) => {
    // 서버에서 API 키 사용
    const API_KEY = process.env.EXTERNAL_API_KEY;
    
    const response = await fetch('https://external-api.com/data', {
        headers: { 'Authorization': `Bearer ${API_KEY}` }
    });
    
    const data = await response.json();
    res.json(data);
});
```

**보안 수준:** ⭐⭐⭐⭐⭐ (최고)
- 장점: API 키 완전히 숨김
- 단점: 서버 운영 비용

#### 3. 환경 변수 + .gitignore (개발용)

```kotlin
// local.properties (Git에 커밋하지 않음)
API_KEY=sk_live_51H...

// .gitignore
local.properties

// build.gradle.kts
android {
    defaultConfig {
        val properties = Properties()
        properties.load(FileInputStream(rootProject.file("local.properties")))
        
        buildConfigField(
            "String",
            "API_KEY",
            "\"${properties.getProperty("API_KEY")}\""
        )
    }
}
```

**보안 수준:** ⭐⭐ (낮음)
- 장점: Git에 노출 방지
- 단점: APK에는 여전히 포함됨

---

## 네트워크 보안

> [!WARNING]
> **HTTP의 위험성**
> 
> HTTP는 **평문 전송**입니다:
> ```
> 공용 WiFi
>     ↓
> [공격자] ← 모든 데이터 읽기 가능
>     ↓
> 서버
> ```
> 
> **실제 공격 시나리오:**
> 1. 카페 WiFi 접속
> 2. 공격자가 패킷 스니핑
> 3. 로그인 정보, 토큰 탈취
> 4. 계정 해킹

### HTTPS 강제

```kotlin
// AndroidManifest.xml
<application
    android:usesCleartextTraffic="false"> <!-- HTTP 차단 -->
    
    <activity ... />
</application>
```

```xml
<!-- network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### Certificate Pinning

**왜 필요한가?**

```
일반 HTTPS:
앱 → [신뢰하는 CA] → 서버
        ↑
    공격자가 가짜 인증서 발급 가능!

Certificate Pinning:
앱 → [특정 인증서만 신뢰] → 서버
        ↑
    가짜 인증서 거부!
```

```kotlin
// OkHttp Certificate Pinning
val certificatePinner = CertificatePinner.Builder()
    .add(
        "api.example.com",
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    )
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

**인증서 핀 얻기:**
```bash
openssl s_client -connect api.example.com:443 | \
openssl x509 -pubkey -noout | \
openssl pkey -pubin -outform der | \
openssl dgst -sha256 -binary | \
base64
```

---

## 인증과 인가

> [!IMPORTANT]
> **인증 vs 인가**
> 
> **인증 (Authentication):** "당신은 누구인가?"
> - 로그인, 생체 인증
> 
> **인가 (Authorization):** "무엇을 할 수 있는가?"
> - 권한, 역할

### JWT (JSON Web Token)

**JWT 구조:**
```
Header.Payload.Signature

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.  ← Header
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.  ← Payload
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature
```

**디코딩 예시:**
```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022,
  "exp": 1516242622
}
```

**보안 주의사항:**
```kotlin
// ❌ JWT를 SharedPreferences에 평문 저장
sharedPreferences.edit()
    .putString("jwt", token)
    .apply()

// ✅ EncryptedSharedPreferences 사용
encryptedPrefs.edit()
    .putString("jwt", token)
    .apply()
```

### 생체 인증 (Biometric)

```kotlin
// 의존성
implementation("androidx.biometric:biometric:1.1.0")

@Composable
fun BiometricAuthScreen() {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    
    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // 인증 성공
                    val cryptoObject = result.cryptoObject
                    // 암호화된 데이터 복호화
                }
                
                override fun onAuthenticationFailed() {
                    // 인증 실패
                }
            }
        )
    }
    
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("생체 인증")
        .setSubtitle("앱 접근을 위해 인증하세요")
        .setNegativeButtonText("취소")
        .build()
    
    Button(onClick = {
        biometricPrompt.authenticate(promptInfo)
    }) {
        Text("생체 인증")
    }
}
```

---

## 보안 베스트 프랙티스

### 1. 입력 검증

```kotlin
// ❌ SQL Injection 취약
fun getUser(userId: String): User {
    val query = "SELECT * FROM users WHERE id = $userId"
    // userId = "1 OR 1=1" → 모든 사용자 반환!
}

// ✅ Prepared Statement 사용
fun getUser(userId: String): User {
    val query = "SELECT * FROM users WHERE id = ?"
    // 파라미터 바인딩으로 안전
}
```

### 2. 로그 보안

```kotlin
// ❌ 민감한 정보 로깅
Log.d("Auth", "Password: $password")
Log.d("API", "Token: $token")

// ✅ 프로덕션에서 로그 제거
if (BuildConfig.DEBUG) {
    Log.d("Auth", "Login attempt")
}
```

### 3. 백업 보안

```xml
<!-- AndroidManifest.xml -->
<application
    android:allowBackup="false"
    android:fullBackupContent="@xml/backup_rules">
```

```xml
<!-- res/xml/backup_rules.xml -->
<full-backup-content>
    <exclude domain="sharedpref" path="secret_prefs.xml"/>
    <exclude domain="database" path="sensitive.db"/>
</full-backup-content>
```

---

## 💡 보안 체크리스트

### 출시 전 필수 확인

- [ ] HTTPS만 사용
- [ ] API 키 하드코딩 없음
- [ ] 민감한 데이터 암호화
- [ ] Certificate Pinning 적용
- [ ] ProGuard/R8 활성화
- [ ] 로그에 민감 정보 없음
- [ ] 백업 정책 설정
- [ ] 생체 인증 구현 (필요시)
- [ ] 입력 검증
- [ ] 보안 테스트 완료

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Stay Secure! 🔒
