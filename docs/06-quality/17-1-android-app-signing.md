# Android 앱 서명 가이드

> 📖 **시리즈 구성**
> - **17-1**: Android 앱 서명 (현재 문서) - 앱 서명 및 보안
> - **17-2**: [Android Play Console 가이드](./17-2-android-play-console-guide.md) - Play Console 상세 가이드
> - **17-3**: [Android 배포 체크리스트](./17-3-android-deployment-checklist.md) - 배포 체크리스트 및 베스트 프랙티스

---

## 📚 목차

1. [앱 서명이란?](#앱-서명이란)
2. [키스토어 생성](#키스토어-생성)
3. [앱 서명 설정](#앱-서명-설정)
4. [Play App Signing](#play-app-signing)
5. [보안 베스트 프랙티스](#보안-베스트-프랙티스)

---

## 앱 서명이란?

### 🎯 앱 서명의 목적

**앱 서명(App Signing)**은 앱의 출처를 증명하고 무결성을 보장하는 보안 메커니즘입니다.

```
개발자 → 앱 빌드 → 개인 키로 서명 → 서명된 APK/AAB → Google Play
```

### 🔐 왜 앱 서명이 필요한가?

#### 1. 앱 출처 증명
```
사용자가 다운로드하는 앱이 진짜 개발자가 만든 것임을 보장
→ 악의적인 제3자가 가짜 앱을 배포하는 것을 방지
```

#### 2. 앱 무결성 보장
```
앱이 서명 후 변조되지 않았음을 보장
→ 앱 코드나 리소스가 수정되면 서명이 무효화됨
```

#### 3. 업데이트 보안
```
같은 키로 서명된 앱만 업데이트 가능
→ 다른 개발자가 내 앱을 업데이트할 수 없음
```

### 📊 서명 프로세스

```mermaid
graph LR
    A[앱 코드] --> B[빌드]
    B --> C[서명되지 않은<br/>APK/AAB]
    C --> D[개인 키로<br/>서명]
    D --> E[서명된<br/>APK/AAB]
    E --> F[Google Play<br/>업로드]
```

---

## 키스토어 생성

### 🔑 키스토어란?

**키스토어(Keystore)**는 앱 서명에 사용되는 개인 키를 안전하게 저장하는 파일입니다.

```
키스토어 파일 (.jks 또는 .keystore)
└─ 개인 키 (Private Key)
    ├─ 키 별칭 (Key Alias)
    ├─ 키 비밀번호 (Key Password)
    └─ 공개 인증서 (Public Certificate)
```

> [!IMPORTANT]
> **키스토어를 잃어버리면 앱을 업데이트할 수 없습니다!** 안전한 곳에 백업하세요.

### 🛠️ 키스토어 생성 방법

#### 방법 1: 명령줄 (keytool)

```bash
# 키스토어 생성
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias my-key-alias

# 옵션 설명:
# -genkey: 새 키 생성
# -v: 상세 출력 (verbose)
# -keystore: 키스토어 파일 이름
# -keyalg: 암호화 알고리즘 (RSA 권장)
# -keysize: 키 크기 (2048비트 권장, 최소 2048)
# -validity: 유효 기간 (일 단위, 10000일 = 약 27년)
# -alias: 키 별칭 (키를 식별하는 이름)
```

**실행 예시**:
```
키 저장소 비밀번호 입력: ********
새 비밀번호 다시 입력: ********

이름과 성을 입력하십시오.
  [Unknown]:  John Doe

조직 단위 이름을 입력하십시오.
  [Unknown]:  Development

조직 이름을 입력하십시오.
  [Unknown]:  My Company

구/군/시 이름을 입력하십시오?
  [Unknown]:  Seoul

시/도 이름을 입력하십시오.
  [Unknown]:  Seoul

이 조직의 두 자리 국가 코드를 입력하십시오.
  [Unknown]:  KR

CN=John Doe, OU=Development, O=My Company, L=Seoul, ST=Seoul, C=KR이(가) 맞습니까?
  [아니오]:  예

<my-key-alias>에 대한 키 비밀번호를 입력하십시오.
        (키 저장소 비밀번호와 같은 경우 Enter를 누르십시오): ********

[my-release-key.jks 키 저장소를 저장하는 중]
```

#### 방법 2: Android Studio

1. **Build → Generate Signed Bundle / APK**
2. **Android App Bundle** 선택 → **Next**
3. **Create new...** 클릭
4. 키스토어 정보 입력:
   - **Key store path**: 저장 위치 선택
   - **Password**: 키스토어 비밀번호
   - **Key alias**: 키 별칭
   - **Key password**: 키 비밀번호
   - **Validity (years)**: 유효 기간 (25년 권장)
   - **Certificate**: 인증서 정보 (이름, 조직 등)
5. **OK** → 키스토어 생성 완료

### 🔍 키스토어 정보 확인

```bash
# 키스토어 정보 조회
keytool -list -v -keystore my-release-key.jks

# 출력 예시:
# 키 저장소 유형: jks
# 키 저장소 제공자: SUN
# 
# 키 저장소에 1개의 항목이 포함되어 있습니다.
# 
# 별칭 이름: my-key-alias
# 생성 날짜: 2024. 12. 3.
# 항목 유형: PrivateKeyEntry
# 인증서 체인 길이: 1
# 인증서[1]:
# 소유자: CN=John Doe, OU=Development, O=My Company, L=Seoul, ST=Seoul, C=KR
# 발행자: CN=John Doe, OU=Development, O=My Company, L=Seoul, ST=Seoul, C=KR
# 일련 번호: 12345678
# 적합한 시작 날짜: Tue Dec 03 10:00:00 KST 2024
# 종료 날짜: Sat Nov 09 10:00:00 KST 2051
# 인증서 지문:
#          SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
#          SHA256: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90
```

---

## 앱 서명 설정

### 🔧 Gradle에 서명 설정

#### 방법 1: 직접 설정 (권장하지 않음)

```kotlin
// build.gradle.kts (Module: app)

android {
    // 서명 설정
    signingConfigs {
        create("release") {
            // ❌ 보안 위험: 비밀번호가 코드에 노출됨
            storeFile = file("../my-release-key.jks")
            storePassword = "myStorePassword"
            keyAlias = "my-key-alias"
            keyPassword = "myKeyPassword"
        }
    }
    
    buildTypes {
        release {
            // 서명 설정 적용
            signingConfig = signingConfigs.getByName("release")
            
            // 코드 난독화 활성화
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

> [!WARNING]
> **절대 비밀번호를 코드에 직접 작성하지 마세요!** Git에 커밋되면 보안 위험이 있습니다.

#### 방법 2: 별도 파일로 분리 (권장)

**1단계: keystore.properties 파일 생성**

프로젝트 루트에 `keystore.properties` 파일 생성:

```properties
# keystore.properties
storePassword=myStorePassword
keyPassword=myKeyPassword
keyAlias=my-key-alias
storeFile=../my-release-key.jks
```

**2단계: .gitignore에 추가**

```gitignore
# .gitignore
keystore.properties
*.jks
*.keystore
```

**3단계: build.gradle.kts에서 읽기**

```kotlin
// build.gradle.kts (Module: app)

import java.util.Properties
import java.io.FileInputStream

/**
 * 키스토어 설정 파일 로드
 * 
 * keystore.properties 파일에서 서명 정보를 읽어옵니다.
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            // keystore.properties에서 읽은 값 사용
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
            storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
            storePassword = keystoreProperties["storePassword"] as String?
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        // 디버그 빌드는 자동 서명
        debug {
            // Android Studio가 자동으로 생성한 디버그 키 사용
            // 위치: ~/.android/debug.keystore
        }
    }
}
```

#### 방법 3: 환경 변수 사용 (CI/CD)

```kotlin
// build.gradle.kts (Module: app)

android {
    signingConfigs {
        create("release") {
            // 환경 변수에서 읽기
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            storeFile = System.getenv("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

**GitHub Actions 예시**:
```yaml
# .github/workflows/build.yml
name: Build Release APK

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Decode Keystore
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > my-release-key.jks
      
      - name: Build Release AAB
        env:
          RELEASE_STORE_FILE: my-release-key.jks
          RELEASE_STORE_PASSWORD: ${{ secrets.RELEASE_STORE_PASSWORD }}
          RELEASE_KEY_ALIAS: ${{ secrets.RELEASE_KEY_ALIAS }}
          RELEASE_KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
        run: ./gradlew bundleRelease
```

### 📦 서명된 APK/AAB 빌드

#### AAB (Android App Bundle) 빌드 - 권장

```bash
# 명령줄에서 빌드
./gradlew bundleRelease

# 출력 위치
# app/build/outputs/bundle/release/app-release.aab
```

**AAB의 장점**:
- Google Play가 기기별로 최적화된 APK 생성
- 앱 크기 감소 (평균 15% 작음)
- Dynamic Delivery 지원 (필요한 모듈만 다운로드)

#### APK 빌드

```bash
# 명령줄에서 빌드
./gradlew assembleRelease

# 출력 위치
# app/build/outputs/apk/release/app-release.apk
```

#### Android Studio에서 빌드

1. **Build → Generate Signed Bundle / APK**
2. **Android App Bundle** 선택 (또는 APK)
3. 키스토어 선택
4. **release** 빌드 타입 선택
5. **Finish**

---

## Play App Signing

### 🎯 Play App Signing이란?

**Play App Signing**은 Google Play가 앱 서명 키를 관리해주는 서비스입니다.

```
개발자                    Google Play              사용자
   │                          │                      │
   │  1. Upload Key로 서명    │                      │
   │ ─────────────────────→   │                      │
   │                          │                      │
   │                          │  2. App Signing Key  │
   │                          │     로 재서명         │
   │                          │                      │
   │                          │  3. 서명된 APK 배포  │
   │                          │ ──────────────────→  │
```

### 🔐 두 가지 키

#### 1. Upload Key (업로드 키)
```
개발자가 보유
→ AAB를 Google Play에 업로드할 때 사용
→ 분실 시 Google에 재설정 요청 가능
```

#### 2. App Signing Key (앱 서명 키)
```
Google Play가 보유
→ 사용자에게 배포되는 APK 서명에 사용
→ Google이 안전하게 관리
```

### ✅ Play App Signing의 장점

#### 1. 키 분실 위험 감소
```
Upload Key를 잃어버려도 Google에 재설정 요청 가능
→ 앱 업데이트를 계속할 수 있음
```

#### 2. 보안 강화
```
App Signing Key는 Google이 안전하게 관리
→ 개발자 PC가 해킹되어도 안전
```

#### 3. 키 업그레이드
```
Google이 최신 암호화 표준으로 키 업그레이드
→ 보안 강화
```

### 🚀 Play App Signing 설정

#### 신규 앱

1. **Play Console → 앱 선택**
2. **출시 → 설정 → 앱 무결성**
3. **Play App Signing 사용 설정**
4. Google이 자동으로 App Signing Key 생성

#### 기존 앱 (이미 출시된 앱)

1. **기존 키스토어를 Google에 업로드**
2. **새 Upload Key 생성**
3. **이후 업데이트는 Upload Key로 서명**

> [!IMPORTANT]
> **Play App Signing을 활성화하면 비활성화할 수 없습니다!**

---

## 보안 베스트 프랙티스

### 🔒 키스토어 보안

#### 1. 안전한 저장

```
✅ 권장:
- 비밀번호 관리자 (1Password, LastPass 등)
- 클라우드 암호화 저장소 (Google Drive, Dropbox + 암호화)
- 오프라인 백업 (USB, 외장 하드)

❌ 피해야 할 것:
- Git 저장소에 커밋
- 이메일로 전송
- 암호화되지 않은 클라우드 저장소
```

#### 2. 비밀번호 관리

```kotlin
// ❌ 나쁜 예: 약한 비밀번호
storePassword = "1234"
keyPassword = "password"

// ✅ 좋은 예: 강력한 비밀번호
storePassword = "Xk9$mP2#vL8@qR5&"
keyPassword = "nT7!wQ3%bF6^hJ9*"
```

**강력한 비밀번호 조건**:
- 최소 12자 이상
- 대문자, 소문자, 숫자, 특수문자 조합
- 사전에 없는 단어

#### 3. 접근 제한

```
팀 환경에서:
- 키스토어는 최소한의 인원만 접근
- 역할 기반 접근 제어 (RBAC)
- 키스토어 사용 로그 기록
```

### 🛡️ 코드 보안

#### 1. 민감한 정보 제거

```kotlin
// ❌ 나쁜 예: API 키가 코드에 노출
class ApiClient {
    companion object {
        const val API_KEY = "sk_live_1234567890abcdef"  // 위험!
    }
}

// ✅ 좋은 예: BuildConfig 사용
// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${System.getenv("API_KEY")}\"")
    }
}

// 코드에서 사용
class ApiClient {
    companion object {
        val API_KEY = BuildConfig.API_KEY
    }
}
```

#### 2. ProGuard/R8 난독화

```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            // 코드 난독화 활성화
            isMinifyEnabled = true
            
            // 사용하지 않는 리소스 제거
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### 3. 디버그 로그 제거

```kotlin
// ❌ 나쁜 예: 프로덕션에 디버그 로그 포함
Log.d(TAG, "User password: ${user.password}")

// ✅ 좋은 예: 디버그 빌드에서만 로그 출력
if (BuildConfig.DEBUG) {
    Log.d(TAG, "User logged in: ${user.email}")
}

// ✅ 더 좋은 예: Timber 사용
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 디버그 빌드에서만 로그 출력
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

### 📝 체크리스트

배포 전 보안 체크리스트:

- [ ] 키스토어를 안전한 곳에 백업
- [ ] 키스토어 비밀번호를 비밀번호 관리자에 저장
- [ ] `keystore.properties`를 `.gitignore`에 추가
- [ ] API 키 등 민감한 정보를 환경 변수로 분리
- [ ] ProGuard/R8 난독화 활성화
- [ ] 디버그 로그 제거 또는 조건부 처리
- [ ] Play App Signing 활성화
- [ ] 앱 권한을 최소화
- [ ] HTTPS 사용 (HTTP 비활성화)

---

## 🎯 다음 단계

앱 서명을 마스터했습니다! 다음 단계로:

1. **[17-2. Android Play Console 가이드](./17-2-android-play-console-guide.md)** - Play Console 상세 가이드
2. **[17-3. Android 배포 체크리스트](./17-3-android-deployment-checklist.md)** - 배포 체크리스트

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Deploying! 🚀
