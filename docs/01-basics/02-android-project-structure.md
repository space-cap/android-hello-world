# Android 프로젝트 구조 완벽 가이드

## 📚 목차
1. [프로젝트 구조 개요](#프로젝트-구조-개요)
2. [최상위 디렉토리](#최상위-디렉토리)
3. [app 모듈 구조](#app-모듈-구조)
4. [Gradle 파일들](#gradle-파일들)
5. [리소스 폴더 (res)](#리소스-폴더-res)
6. [AndroidManifest.xml](#androidmanifestxml)
7. [의존성 관리](#의존성-관리)
8. [실전 팁](#실전-팁)

---

## 프로젝트 구조 개요

Android Studio에서 새 프로젝트를 만들면 다음과 같은 구조가 생성됩니다:

```
HelloWorld/
├── .gradle/                    # Gradle 캐시 (자동 생성)
├── .idea/                      # Android Studio 설정 (자동 생성)
├── app/                        # 메인 앱 모듈 ⭐
│   ├── build/                  # 빌드 출력 (자동 생성)
│   ├── src/                    # 소스 코드 ⭐
│   └── build.gradle.kts        # 앱 모듈 빌드 설정 ⭐
├── gradle/                     # Gradle 래퍼 파일
│   ├── libs.versions.toml      # 버전 카탈로그 ⭐
│   └── wrapper/
├── .gitignore                  # Git 무시 파일
├── build.gradle.kts            # 프로젝트 빌드 설정 ⭐
├── gradle.properties           # Gradle 속성
├── gradlew                     # Gradle 래퍼 (Unix)
├── gradlew.bat                 # Gradle 래퍼 (Windows)
└── settings.gradle.kts         # 프로젝트 설정 ⭐
```

> [!IMPORTANT]
> **⭐ 표시된 파일/폴더가 가장 중요합니다**
> - `app/src/` - 코드를 작성하는 곳
> - `build.gradle.kts` - 의존성과 설정을 관리
> - `libs.versions.toml` - 라이브러리 버전 관리

---

## 최상위 디렉토리

### 1. .gradle/ 와 .idea/

```
.gradle/        # Gradle 빌드 시스템 캐시
.idea/          # Android Studio IDE 설정
```

**특징**:
- ✅ 자동으로 생성됨
- ✅ Git에 커밋하지 않음 (`.gitignore`에 포함)
- ⚠️ 직접 수정하지 않음

### 2. app/

메인 애플리케이션 모듈입니다. 모든 코드가 여기에 들어갑니다.

```
app/
├── build/                  # 빌드 결과물 (APK 등)
├── src/                    # 소스 코드
│   ├── main/              # 메인 소스셋
│   ├── test/              # 단위 테스트
│   └── androidTest/       # 안드로이드 테스트
└── build.gradle.kts       # 앱 빌드 설정
```

### 3. gradle/

Gradle 관련 파일들이 있습니다.

```
gradle/
├── libs.versions.toml     # 버전 카탈로그 (중요!)
└── wrapper/               # Gradle 래퍼
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

#### libs.versions.toml (버전 카탈로그)

모든 라이브러리 버전을 한 곳에서 관리합니다.

```toml
[versions]
agp = "8.2.0"
kotlin = "1.9.20"
compose = "1.5.4"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui", version.ref = "compose" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

**장점**:
- ✅ 버전을 한 곳에서 관리
- ✅ 여러 모듈에서 같은 버전 사용
- ✅ 업데이트가 쉬움

### 4. 빌드 관련 파일들

```
build.gradle.kts            # 프로젝트 레벨 빌드 설정
settings.gradle.kts         # 프로젝트 설정 (모듈 포함)
gradle.properties           # Gradle 속성
gradlew / gradlew.bat       # Gradle 래퍼 스크립트
```

---

## app 모듈 구조

### src/ 디렉토리 상세

```
app/src/
├── main/                           # 메인 소스셋
│   ├── java/                       # Java/Kotlin 소스 코드
│   │   └── com/example/helloworld/ # 패키지
│   │       └── MainActivity.kt     # 메인 액티비티
│   ├── res/                        # 리소스 파일
│   │   ├── drawable/               # 이미지, 아이콘
│   │   ├── mipmap/                 # 앱 아이콘
│   │   ├── values/                 # 값 리소스
│   │   │   ├── colors.xml          # 색상
│   │   │   ├── strings.xml         # 문자열
│   │   │   └── themes.xml          # 테마
│   │   └── xml/                    # XML 설정
│   └── AndroidManifest.xml         # 앱 매니페스트
├── test/                           # 단위 테스트
│   └── java/
│       └── com/example/helloworld/
│           └── ExampleUnitTest.kt
└── androidTest/                    # 안드로이드 테스트
    └── java/
        └── com/example/helloworld/
            └── ExampleInstrumentedTest.kt
```

### 패키지 구조

```
com/example/helloworld/
├── MainActivity.kt         # 메인 화면
├── ui/                    # UI 관련
│   └── theme/            # Compose 테마
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── data/                 # 데이터 레이어 (추가 가능)
│   ├── model/           # 데이터 모델
│   └── repository/      # 데이터 저장소
├── viewmodel/           # ViewModel (추가 가능)
└── navigation/          # Navigation (추가 가능)
```

> [!TIP]
> **패키지 구조는 프로젝트 규모에 따라 조정하세요**
> - 작은 프로젝트: 모든 코드를 루트 패키지에
> - 중간 프로젝트: ui, data, viewmodel로 분리
> - 큰 프로젝트: feature별로 모듈 분리

---

## Gradle 파일들

### 1. settings.gradle.kts (프로젝트 설정)

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HelloWorld"
include(":app")  // app 모듈 포함
```

**역할**:
- 플러그인 저장소 설정
- 의존성 저장소 설정
- 포함할 모듈 정의

### 2. build.gradle.kts (프로젝트 레벨)

```kotlin
// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}
```

**역할**:
- 프로젝트 전체에 적용되는 플러그인 정의
- 모든 모듈에서 사용할 수 있는 설정

### 3. app/build.gradle.kts (앱 모듈)

가장 중요한 파일입니다!

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.helloworld"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.helloworld"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

#### 주요 섹션 설명

| 섹션 | 설명 | 예시 |
|------|------|------|
| `namespace` | 앱의 고유 식별자 | `com.example.helloworld` |
| `compileSdk` | 컴파일에 사용할 SDK 버전 | `34` |
| `minSdk` | 최소 지원 SDK 버전 | `24` (Android 7.0) |
| `targetSdk` | 타겟 SDK 버전 | `34` |
| `versionCode` | 앱 버전 코드 (정수) | `1` |
| `versionName` | 앱 버전 이름 (문자열) | `"1.0"` |

#### buildFeatures

```kotlin
buildFeatures {
    compose = true  // Jetpack Compose 활성화
}
```

#### composeOptions

```kotlin
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.1"  // Compose 컴파일러 버전
}
```

---

## 리소스 폴더 (res)

### 디렉토리 구조

```
res/
├── drawable/           # 이미지, 벡터 드로어블
│   └── ic_launcher_background.xml
├── mipmap-*/          # 앱 아이콘 (다양한 해상도)
│   ├── mipmap-hdpi/
│   ├── mipmap-mdpi/
│   ├── mipmap-xhdpi/
│   ├── mipmap-xxhdpi/
│   └── mipmap-xxxhdpi/
├── values/            # 값 리소스
│   ├── colors.xml     # 색상 정의
│   ├── strings.xml    # 문자열 정의
│   └── themes.xml     # 테마 정의
└── xml/               # XML 설정 파일
```

### 1. drawable/

이미지와 벡터 그래픽을 저장합니다.

```xml
<!-- drawable/ic_launcher_background.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#3DDC84"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

**파일 명명 규칙**:
- 소문자만 사용
- 언더스코어(`_`)로 단어 구분
- 예: `ic_home.xml`, `bg_button.png`

### 2. values/colors.xml

색상을 정의합니다.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

**Compose에서 사용**:
```kotlin
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
```

### 3. values/strings.xml

문자열을 정의합니다.

```xml
<resources>
    <string name="app_name">HelloWorld</string>
    <string name="welcome_message">환영합니다!</string>
    <string name="button_submit">제출</string>
</resources>
```

**Compose에서 사용**:
```kotlin
import androidx.compose.ui.res.stringResource

@Composable
fun Greeting() {
    Text(text = stringResource(R.string.welcome_message))
}
```

> [!TIP]
> **왜 strings.xml을 사용할까?**
> - 다국어 지원이 쉬움
> - 문자열을 한 곳에서 관리
> - 하드코딩 방지

### 4. values/themes.xml

Material Design 테마를 정의합니다.

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.HelloWorld" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/purple_500</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/white</item>
    </style>

    <style name="Theme.HelloWorld" parent="Base.Theme.HelloWorld" />
</resources>
```

---

## AndroidManifest.xml

앱의 필수 정보를 정의하는 파일입니다.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- 권한 선언 -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.HelloWorld"
        tools:targetApi="31">
        
        <!-- 메인 액티비티 -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.HelloWorld">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

### 주요 요소

| 요소 | 설명 |
|------|------|
| `<uses-permission>` | 앱이 필요한 권한 선언 |
| `<application>` | 앱 전체 설정 |
| `android:icon` | 앱 아이콘 |
| `android:label` | 앱 이름 |
| `android:theme` | 앱 테마 |
| `<activity>` | 액티비티 선언 |
| `android:exported` | 외부에서 접근 가능 여부 |
| `<intent-filter>` | 앱 실행 방법 정의 |

### 자주 사용하는 권한

```xml
<!-- 인터넷 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 카메라 -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- 위치 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 저장소 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 의존성 관리

### 의존성 추가 방법

#### 1. libs.versions.toml에 버전 추가

```toml
[versions]
retrofit = "2.9.0"

[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
```

#### 2. build.gradle.kts에 의존성 추가

```kotlin
dependencies {
    implementation(libs.retrofit)
}
```

### 의존성 타입

| 타입 | 설명 | 사용 시기 |
|------|------|----------|
| `implementation` | 컴파일 + 런타임 | 대부분의 경우 |
| `api` | 컴파일 + 런타임 + 노출 | 라이브러리 모듈 |
| `compileOnly` | 컴파일만 | 런타임에 제공되는 경우 |
| `runtimeOnly` | 런타임만 | 컴파일 불필요 |
| `testImplementation` | 테스트 코드 | 단위 테스트 |
| `androidTestImplementation` | 안드로이드 테스트 | UI 테스트 |
| `debugImplementation` | 디버그 빌드만 | 디버깅 도구 |

### 자주 사용하는 라이브러리

```kotlin
dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Retrofit (네트워킹)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    
    // Coil (이미지 로딩)
    implementation(libs.coil.compose)
    
    // Room (데이터베이스)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
}
```

---

## 실전 팁

### 1. 프로젝트 뷰 vs Android 뷰

Android Studio에는 두 가지 뷰가 있습니다:

**Android 뷰 (권장)**:
```
app
├── manifests
│   └── AndroidManifest.xml
├── java
│   └── com.example.helloworld
│       └── MainActivity.kt
└── res
    ├── drawable
    ├── mipmap
    └── values
```

**Project 뷰**:
```
app
├── build
├── src
│   └── main
│       ├── java
│       ├── res
│       └── AndroidManifest.xml
└── build.gradle.kts
```

> [!TIP]
> **Android 뷰를 사용하세요**
> - 더 직관적
> - 불필요한 파일 숨김
> - 빠른 네비게이션

### 2. 자주 수정하는 파일

| 파일 | 수정 빈도 | 용도 |
|------|----------|------|
| `MainActivity.kt` | ⭐⭐⭐⭐⭐ | 코드 작성 |
| `build.gradle.kts` (app) | ⭐⭐⭐⭐ | 의존성 추가 |
| `libs.versions.toml` | ⭐⭐⭐ | 버전 관리 |
| `AndroidManifest.xml` | ⭐⭐ | 권한, 액티비티 추가 |
| `strings.xml` | ⭐⭐ | 문자열 추가 |
| `colors.xml` | ⭐ | 색상 추가 |

### 3. 빌드 관련 폴더

**절대 수정하지 마세요**:
- `.gradle/`
- `.idea/`
- `app/build/`

**Git에 커밋하지 마세요**:
```gitignore
# .gitignore
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

### 4. 클린 빌드

문제가 생기면 클린 빌드를 시도하세요:

```bash
# Gradle 캐시 삭제
./gradlew clean

# 또는 Android Studio에서
Build > Clean Project
Build > Rebuild Project
```

### 5. Gradle Sync

의존성을 추가하거나 설정을 변경하면 Sync가 필요합니다:

```
File > Sync Project with Gradle Files
```

또는 상단 바의 "Sync Now" 클릭

---

## 학습 체크리스트

### 프로젝트 구조
- [ ] 최상위 디렉토리 구조를 안다
- [ ] `app/` 모듈의 역할을 안다
- [ ] `src/main/` 구조를 이해한다
- [ ] 패키지 구조를 이해한다

### Gradle
- [ ] `settings.gradle.kts`의 역할을 안다
- [ ] `build.gradle.kts` (프로젝트)의 역할을 안다
- [ ] `build.gradle.kts` (앱)의 역할을 안다
- [ ] `libs.versions.toml`을 사용할 수 있다
- [ ] 의존성을 추가할 수 있다

### 리소스
- [ ] `res/` 폴더 구조를 안다
- [ ] `strings.xml`을 사용할 수 있다
- [ ] `colors.xml`을 사용할 수 있다
- [ ] 이미지를 `drawable/`에 추가할 수 있다

### AndroidManifest
- [ ] `AndroidManifest.xml`의 역할을 안다
- [ ] 권한을 추가할 수 있다
- [ ] 액티비티를 등록할 수 있다

---

## 다음 단계

이제 프로젝트 구조를 이해했으니:

1. ✅ Kotlin 기초 (완료)
2. ✅ Android 프로젝트 구조 (완료)
3. ➡️ Jetpack Compose Layout
4. ➡️ State 관리
5. ➡️ Navigation

---

## 참고 자료

### 공식 문서
- [Android 프로젝트 개요](https://developer.android.com/studio/projects)
- [Gradle 빌드 구성](https://developer.android.com/build)
- [AndroidManifest.xml](https://developer.android.com/guide/topics/manifest/manifest-intro)

### 추천 읽기
- [Version Catalog](https://developer.android.com/build/migrate-to-catalogs)
- [앱 리소스 개요](https://developer.android.com/guide/topics/resources/providing-resources)

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

**학습 예상 시간**: 1-2일  
**난이도**: ⭐⭐

프로젝트 구조를 이해하면 파일을 찾고 수정하는 것이 훨씬 쉬워집니다! 🎯
