# Android 배포 체크리스트

> 📖 **시리즈 구성**
> - **17-1**: [Android 앱 서명](./17-1-android-app-signing.md) - 앱 서명 및 보안
> - **17-2**: [Android Play Console 가이드](./17-2-android-play-console-guide.md) - Play Console 상세 가이드
> - **17-3**: Android 배포 체크리스트 (현재 문서) - 배포 체크리스트 및 베스트 프랙티스

---

## 📚 목차

1. [배포 전 체크리스트](#배포-전-체크리스트)
2. [버전 관리 전략](#버전-관리-전략)
3. [AAB vs APK](#aab-vs-apk)
4. [ProGuard/R8 설정](#proguardr8-설정)
5. [배포 전략](#배포-전략)
6. [출시 후 모니터링](#출시-후-모니터링)

---

## 배포 전 체크리스트

### 🔍 코드 품질

#### 1. 테스트 완료

```kotlin
// ✅ 단위 테스트
class UserRepositoryTest {
    @Test
    fun `사용자 로그인 성공`() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        // When
        val result = repository.login(email, password)
        
        // Then
        assertTrue(result.isSuccess)
    }
}

// ✅ UI 테스트
@Test
fun loginScreen_validCredentials_navigatesToHome() {
    composeTestRule.setContent {
        LoginScreen()
    }
    
    composeTestRule.onNodeWithText("이메일").performTextInput("test@example.com")
    composeTestRule.onNodeWithText("비밀번호").performTextInput("password123")
    composeTestRule.onNodeWithText("로그인").performClick()
    
    composeTestRule.onNodeWithText("홈").assertExists()
}
```

**테스트 체크리스트**:
- [ ] 모든 단위 테스트 통과
- [ ] 주요 UI 흐름 테스트 완료
- [ ] 엣지 케이스 테스트 완료
- [ ] 다양한 기기에서 테스트 (에뮬레이터 + 실제 기기)
- [ ] 다양한 Android 버전에서 테스트

#### 2. 디버그 코드 제거

```kotlin
// ❌ 제거해야 할 코드
Log.d(TAG, "User password: ${user.password}")
Log.v(TAG, "API response: $response")
println("Debug: $data")

// ✅ 조건부 로깅으로 변경
if (BuildConfig.DEBUG) {
    Log.d(TAG, "User logged in: ${user.email}")
}

// ✅ Timber 사용 (자동으로 프로덕션에서 제거)
Timber.d("User logged in: ${user.email}")
```

**디버그 코드 체크리스트**:
- [ ] 모든 `Log.d()`, `Log.v()` 제거 또는 조건부 처리
- [ ] `println()` 제거
- [ ] 테스트용 하드코딩 값 제거
- [ ] TODO, FIXME 주석 확인 및 처리

#### 3. 민감한 정보 제거

```kotlin
// ❌ 제거해야 할 코드
const val API_KEY = "sk_live_1234567890"
const val DATABASE_PASSWORD = "mypassword"

// ✅ BuildConfig 또는 환경 변수 사용
val apiKey = BuildConfig.API_KEY
val dbPassword = System.getenv("DB_PASSWORD")
```

**민감한 정보 체크리스트**:
- [ ] API 키를 환경 변수로 분리
- [ ] 비밀번호 하드코딩 제거
- [ ] 서버 URL 확인 (프로덕션 서버 사용)
- [ ] 테스트 계정 정보 제거

### 📱 앱 설정

#### 1. build.gradle.kts 확인

```kotlin
android {
    namespace = "com.example.myapp"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.myapp"  // ✅ 고유한 ID
        minSdk = 24                          // ✅ 최소 지원 버전
        targetSdk = 34                       // ✅ 최신 버전
        versionCode = 1                      // ✅ 증가 확인
        versionName = "1.0.0"                // ✅ 시맨틱 버저닝
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true           // ✅ 코드 난독화
            isShrinkResources = true         // ✅ 리소스 축소
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")  // ✅ 서명 설정
        }
    }
}
```

**build.gradle.kts 체크리스트**:
- [ ] `applicationId` 확인 (고유해야 함)
- [ ] `versionCode` 증가 (이전 버전보다 커야 함)
- [ ] `versionName` 업데이트
- [ ] `minSdk`, `targetSdk` 확인
- [ ] `isMinifyEnabled = true` 설정
- [ ] `isShrinkResources = true` 설정
- [ ] 서명 설정 확인

#### 2. AndroidManifest.xml 확인

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- ✅ 필요한 권한만 요청 -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    
    <!-- ❌ 불필요한 권한 제거 -->
    <!-- <uses-permission android:name="android.permission.READ_CONTACTS"/> -->
    
    <application
        android:name=".MyApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.MyApp"
        android:usesCleartextTraffic="false">  <!-- ✅ HTTPS만 사용 -->
        
        <!-- ✅ 네트워크 보안 설정 -->
        <meta-data
            android:name="android.net.http.cleartext_traffic_allowed"
            android:value="false"/>
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**AndroidManifest.xml 체크리스트**:
- [ ] 불필요한 권한 제거
- [ ] `usesCleartextTraffic="false"` 설정 (HTTPS 강제)
- [ ] 앱 이름 확인
- [ ] 아이콘 설정 확인
- [ ] `android:debuggable` 제거 (자동 제거됨)

### 🎨 리소스 최적화

#### 1. 이미지 최적화

```
이미지 압축:
- PNG: TinyPNG, ImageOptim
- JPG: JPEGmini
- WebP: Android Studio 변환 도구

권장 사항:
✅ WebP 형식 사용 (PNG/JPG보다 작음)
✅ 벡터 드로어블 사용 (크기 조절 가능)
✅ 사용하지 않는 이미지 제거
❌ 고해상도 이미지 그대로 사용
```

#### 2. 문자열 리소스

```xml
<!-- strings.xml -->
<resources>
    <!-- ✅ 모든 문자열을 리소스로 관리 -->
    <string name="app_name">My App</string>
    <string name="login_button">로그인</string>
    
    <!-- ✅ 다국어 지원 -->
    <!-- values-en/strings.xml -->
    <string name="login_button">Login</string>
</resources>
```

**리소스 체크리스트**:
- [ ] 모든 문자열을 `strings.xml`로 이동
- [ ] 사용하지 않는 리소스 제거 (`Analyze → Run Inspection by Name → Unused resources`)
- [ ] 이미지 최적화 완료
- [ ] 다국어 지원 확인

### 🔒 보안

#### 1. 네트워크 보안

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <!-- ✅ HTTPS만 허용 -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
    
    <!-- ❌ 개발 중에만 HTTP 허용 (프로덕션에서 제거) -->
    <!-- <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
    </domain-config> -->
</network-security-config>
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config">
</application>
```

#### 2. 데이터 암호화

```kotlin
// ✅ EncryptedSharedPreferences 사용
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

// 민감한 데이터 저장
encryptedPrefs.edit()
    .putString("auth_token", token)
    .apply()
```

**보안 체크리스트**:
- [ ] HTTPS 강제 (`usesCleartextTraffic="false"`)
- [ ] 민감한 데이터 암호화
- [ ] 루팅 감지 (선택)
- [ ] SSL Pinning (선택)

---

## 버전 관리 전략

### 📊 시맨틱 버저닝

```
MAJOR.MINOR.PATCH

예: 1.2.3
- MAJOR (1): 호환되지 않는 API 변경
- MINOR (2): 하위 호환되는 기능 추가
- PATCH (3): 하위 호환되는 버그 수정
```

**예시**:
```
1.0.0 → 첫 출시
1.0.1 → 버그 수정
1.1.0 → 새 기능 추가
2.0.0 → 대규모 변경 (하위 호환 안 됨)
```

### 🔢 versionCode 관리

```kotlin
// ✅ 권장: 날짜 기반
// 형식: YYYYMMDDNN (NN: 당일 빌드 번호)
versionCode = 2024120301  // 2024년 12월 3일, 첫 번째 빌드

// ✅ 권장: 순차적 증가
versionCode = 1  // 1.0.0
versionCode = 2  // 1.0.1
versionCode = 3  // 1.1.0

// ❌ 비권장: versionName과 동일
versionCode = 100  // 1.0.0
versionCode = 110  // 1.1.0
```

### 📝 출시 노트 작성

```
버전 1.2.0 업데이트:

새로운 기능:
• 다크 모드 지원
• 위젯 추가
• 백업 및 복원 기능

개선 사항:
• 앱 시작 속도 30% 향상
• UI/UX 개선
• 배터리 사용량 감소

버그 수정:
• 알림이 작동하지 않던 문제 해결
• 로그인 시 크래시 수정
• 이미지 로딩 오류 수정

알려진 문제:
• 일부 기기에서 위젯이 표시되지 않을 수 있음
  (다음 업데이트에서 수정 예정)
```

---

## AAB vs APK

### 📦 Android App Bundle (AAB)

**장점**:
```
✅ 앱 크기 감소 (평균 15%)
✅ Dynamic Delivery (필요한 모듈만 다운로드)
✅ Google Play가 기기별 최적화 APK 생성
✅ 언어별, 화면 밀도별 리소스 분리
```

**단점**:
```
❌ Google Play에서만 사용 가능
❌ 직접 배포 불가 (APK 필요)
```

**빌드 방법**:
```bash
# AAB 빌드
./gradlew bundleRelease

# 출력 위치
app/build/outputs/bundle/release/app-release.aab
```

### 📱 APK

**장점**:
```
✅ 직접 배포 가능
✅ 모든 스토어에서 사용 가능
✅ 사이드로딩 가능
```

**단점**:
```
❌ 앱 크기 큼
❌ 모든 리소스 포함
❌ 기기별 최적화 없음
```

**빌드 방법**:
```bash
# APK 빌드
./gradlew assembleRelease

# 출력 위치
app/build/outputs/apk/release/app-release.apk
```

### 🎯 권장 사항

```
Google Play 배포: AAB 사용 (필수)
직접 배포: APK 사용
기업 내부 배포: APK 사용
```

---

## ProGuard/R8 설정

### 🔧 기본 설정

```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            // R8 활성화 (ProGuard의 개선 버전)
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

### 📝 ProGuard 규칙

```proguard
# proguard-rules.pro

# ===== Retrofit =====
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ===== Gson =====
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ===== 데이터 클래스 유지 (JSON 직렬화용) =====
-keep class com.example.myapp.data.model.** { *; }
-keep class com.example.myapp.network.response.** { *; }

# ===== Kotlin Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===== Jetpack Compose =====
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }

# ===== Room =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ===== 크래시 리포트 (스택 트레이스 유지) =====
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

### 🧪 ProGuard 테스트

```bash
# Release 빌드 후 테스트
./gradlew assembleRelease

# APK 설치 및 테스트
adb install app/build/outputs/apk/release/app-release.apk

# 모든 주요 기능 테스트:
# - 로그인/로그아웃
# - API 호출
# - 데이터베이스 작업
# - 화면 전환
```

**ProGuard 문제 해결**:
```
문제: 특정 클래스가 난독화되어 크래시 발생
해결: proguard-rules.pro에 -keep 규칙 추가

문제: API 응답 파싱 실패
해결: 데이터 클래스를 -keep으로 유지

문제: Reflection 사용 시 오류
해결: 해당 클래스/메서드를 -keep으로 유지
```

---

## 배포 전략

### 🎯 단계적 출시

```
Day 1: 1% 출시
- 소수 사용자에게 먼저 배포
- 크래시, ANR 모니터링
- 심각한 문제 없으면 다음 단계

Day 3: 5% 출시
- 안정성 확인
- 서버 부하 확인
- 사용자 피드백 수집

Day 5: 20% 출시
- 더 많은 사용자에게 배포
- 통계 분석

Day 7: 50% 출시
- 절반 사용자에게 배포

Day 10: 100% 출시
- 전체 사용자에게 배포
```

**단계적 출시 장점**:
```
✅ 위험 최소화
✅ 문제 조기 발견
✅ 즉시 롤백 가능
✅ 서버 부하 분산
```

### 🚨 롤백 계획

```
심각한 버그 발견 시:
1. Play Console → "프로덕션" → "출시 중지"
2. 이전 버전으로 롤백 (가능한 경우)
3. 버그 수정
4. 긴급 업데이트 출시

롤백 불가능한 경우:
- 데이터베이스 스키마 변경
- API 버전 변경
→ 긴급 수정 버전 출시 필요
```

### 📅 출시 일정

```
권장 출시 시간:
✅ 화요일~목요일 (문제 발생 시 대응 가능)
✅ 오전 10시~오후 2시 (업무 시간)
❌ 금요일 오후 (주말 대응 어려움)
❌ 공휴일 전날
```

---

## 출시 후 모니터링

### 📊 주요 지표

#### 1. 크래시 및 ANR

```
Play Console → "품질" → "Android Vitals"

모니터링 항목:
- 크래시율: < 1.09% 목표
- ANR율: < 0.47% 목표
- 크래시가 많은 기기/OS 버전 확인
```

**대응 방법**:
```kotlin
// Firebase Crashlytics 통합
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 크래시 리포팅
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        }
    }
}

// 크래시 발생 시 추가 정보 수집
try {
    riskyOperation()
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().apply {
        setCustomKey("user_id", userId)
        setCustomKey("operation", "checkout")
        recordException(e)
    }
}
```

#### 2. 성능 지표

```
모니터링 항목:
- 앱 시작 시간
- 화면 렌더링 시간
- 배터리 사용량
- 네트워크 사용량
```

#### 3. 사용자 피드백

```
Play Console → "평점 및 리뷰"

주의사항:
- 1-2점 리뷰 우선 확인
- 공통 불만사항 파악
- 24시간 내 응답 목표
```

### 🔔 알림 설정

```
Play Console → "설정" → "이메일 기본 설정"

알림 활성화:
✅ 새 리뷰
✅ 평점 변경
✅ 크래시 급증
✅ ANR 급증
✅ 정책 위반 경고
```

---

## 💡 최종 체크리스트

### 출시 전 (D-7)

- [ ] 모든 기능 테스트 완료
- [ ] 디버그 코드 제거
- [ ] ProGuard/R8 테스트 완료
- [ ] 다양한 기기에서 테스트
- [ ] 스크린샷 준비
- [ ] 앱 설명 작성
- [ ] 개인정보처리방침 준비

### 출시 전 (D-3)

- [ ] AAB 빌드 및 서명
- [ ] Play Console 정보 입력 완료
- [ ] 내부 테스트 완료
- [ ] 콘텐츠 등급 완료
- [ ] 출시 노트 작성

### 출시 당일 (D-Day)

- [ ] 최종 빌드 업로드
- [ ] 단계적 출시 설정 (1%)
- [ ] 모니터링 시작
- [ ] 팀원에게 공지

### 출시 후 (D+1 ~ D+7)

- [ ] 크래시/ANR 모니터링
- [ ] 리뷰 응답
- [ ] 단계적 출시 확대
- [ ] 성능 지표 확인
- [ ] 사용자 피드백 수집

### 출시 후 (D+7 ~ D+30)

- [ ] 100% 출시 완료
- [ ] 다음 업데이트 계획
- [ ] 개선 사항 정리
- [ ] 마케팅 활동

---

## 🎯 축하합니다!

앱 배포를 완료했습니다! 🎉

이제 다음을 할 수 있습니다:
- ✅ 앱을 안전하게 서명
- ✅ Play Console 설정
- ✅ 체계적으로 배포
- ✅ 출시 후 모니터링

**계속 학습하고 멋진 앱을 만드세요!** 🚀

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Deploying! 🎊
