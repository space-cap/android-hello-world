# Android 보안 고급

> 📖 **시리즈 구성**
> - **22-1**: [Android 보안 기초](./22-1-android-security-basics.md)
> - **22-2**: Android 보안 고급 (현재 문서)
> - **22-3**: [Android 보안 테스팅](./22-3-android-security-testing.md)

---

## API 키 관리

### 잘못된 방법

```kotlin
// ❌ 절대 하지 말 것
class ApiService {
    private val API_KEY = "sk_live_51H..."  // 디컴파일로 즉시 노출!
}
```

### 올바른 방법

```kotlin
// ✅ 서버 프록시 사용 (권장)
class ApiService {
    suspend fun getData(): Result<Data> {
        // 내 서버로 요청 (API 키는 서버에만 저장)
        return httpClient.get("https://myserver.com/api/data")
    }
}
```

---

## 인증 및 인가

### JWT 토큰 관리

```kotlin
// ❌ 평문 저장
sharedPreferences.edit()
    .putString("jwt", token)
    .apply()

// ✅ 암호화 저장
encryptedPrefs.edit()
    .putString("jwt", token)
    .apply()
```

### 생체 인증

```kotlin
val biometricPrompt = BiometricPrompt(
    activity,
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            // 인증 성공
        }
    }
)

val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("생체 인증")
    .setNegativeButtonText("취소")
    .build()

biometricPrompt.authenticate(promptInfo)
```

---

## 코드 보호

### ProGuard/R8

```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-03
