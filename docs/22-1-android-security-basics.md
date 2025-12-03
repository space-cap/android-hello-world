# Android 보안 기초

> 📖 **시리즈 구성**
> - **22-1**: Android 보안 기초 (현재 문서)
> - **22-2**: [Android 보안 고급](./22-2-android-security-advanced.md)
> - **22-3**: [Android 보안 테스팅](./22-3-android-security-testing.md)

---

## 데이터 암호화

### EncryptedSharedPreferences

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secret_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// 사용
encryptedPrefs.edit()
    .putString("auth_token", token)
    .apply()
```

---

## 네트워크 보안

### HTTPS 강제

```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="false">
</application>
```

```xml
<!-- network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

## 안전한 데이터 저장

```kotlin
// ❌ 평문 저장
sharedPreferences.edit()
    .putString("password", "mypassword")
    .apply()

// ✅ 암호화 저장
encryptedPrefs.edit()
    .putString("password", "mypassword")
    .apply()
```

---

**마지막 업데이트**: 2024-12-03
