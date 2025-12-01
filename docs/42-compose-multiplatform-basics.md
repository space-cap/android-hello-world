# Compose Multiplatform 기본 개념 및 프로젝트 설정

## 목차
1. [Compose Multiplatform이란?](#compose-multiplatform이란)
2. [왜 Compose Multiplatform을 사용해야 할까?](#왜-compose-multiplatform을-사용해야-할까)
3. [개발 환경 설정](#개발-환경-설정)
4. [첫 번째 프로젝트 만들기](#첫-번째-프로젝트-만들기)
5. [프로젝트 구조 이해하기](#프로젝트-구조-이해하기)
6. [Gradle 설정 상세 가이드](#gradle-설정-상세-가이드)
7. [Hello World 앱 만들기](#hello-world-앱-만들기)

---

## Compose Multiplatform이란?

**Compose Multiplatform**은 JetBrains에서 개발한 선언적 UI 프레임워크로, 하나의 코드베이스로 여러 플랫폼의 네이티브 애플리케이션을 개발할 수 있게 해주는 기술입니다.

### 핵심 개념

#### 1. 선언적 UI (Declarative UI)
기존의 명령형 UI와 달리, **무엇을 보여줄지**를 선언하면 프레임워크가 알아서 UI를 업데이트합니다.

```kotlin
// ❌ 명령형 방식 (기존 Android View)
val textView = findViewById<TextView>(R.id.textView)
textView.text = "Hello"
textView.setTextColor(Color.BLUE)
textView.textSize = 16f

// ✅ 선언적 방식 (Compose)
Text(
    text = "Hello",
    color = Color.Blue,
    fontSize = 16.sp
)
```

#### 2. 크로스 플랫폼 (Cross-Platform)
하나의 Kotlin 코드로 여러 플랫폼을 지원합니다:

- **Android**: 스마트폰, 태블릿
- **iOS**: iPhone, iPad
- **Desktop**: Windows, macOS, Linux
- **Web**: 브라우저 (실험적 기능)

#### 3. Kotlin Multiplatform 기반
Kotlin의 멀티플랫폼 기능을 활용하여 비즈니스 로직과 UI를 모두 공유할 수 있습니다.

```
┌─────────────────────────────────────┐
│     Compose Multiplatform UI        │  ← 모든 플랫폼에서 공유
├─────────────────────────────────────┤
│   Kotlin Multiplatform 비즈니스 로직  │  ← 모든 플랫폼에서 공유
├──────────┬──────────┬───────────────┤
│ Android  │   iOS    │   Desktop     │  ← 플랫폼별 네이티브 코드
└──────────┴──────────┴───────────────┘
```

---

## 왜 Compose Multiplatform을 사용해야 할까?

### 장점

#### 1. 개발 시간 단축 ⏱️
```kotlin
// 한 번 작성하면 모든 플랫폼에서 동작
@Composable
fun UserProfile(user: User) {
    Column {
        Text(user.name)
        Text(user.email)
        Button(onClick = { /* ... */ }) {
            Text("Edit Profile")
        }
    }
}
// ↑ 이 코드가 Android, iOS, Desktop에서 모두 동작!
```

#### 2. 일관된 사용자 경험 🎨
모든 플랫폼에서 동일한 UI/UX를 제공할 수 있습니다.

#### 3. 코드 재사용률 극대화 ♻️
- **UI 코드**: 90-95% 공유 가능
- **비즈니스 로직**: 100% 공유 가능
- **플랫폼별 코드**: 5-10%만 필요

#### 4. 타입 안전성 🛡️
Kotlin의 강력한 타입 시스템으로 컴파일 타임에 오류를 잡을 수 있습니다.

```kotlin
// 컴파일 에러 - 타입 불일치
Text(text = 123)  // ❌ String을 기대하는데 Int를 전달

// 올바른 사용
Text(text = "123")  // ✅
Text(text = 123.toString())  // ✅
```

#### 5. 네이티브 성능 🚀
각 플랫폼의 네이티브 UI로 렌더링되어 성능이 우수합니다.

### 단점 및 고려사항

#### 1. 학습 곡선
- Kotlin 언어 학습 필요
- Compose의 선언적 패러다임 이해 필요
- 각 플랫폼의 특성 이해 필요

#### 2. 플랫폼별 기능 접근
일부 플랫폼 고유 기능은 별도 구현이 필요합니다.

```kotlin
// 플랫폼별 코드 예시
expect fun vibrate()  // 공통 인터페이스 선언

// Android 구현
actual fun vibrate() {
    val vibrator = context.getSystemService(Vibrator::class.java)
    vibrator.vibrate(100)
}

// iOS 구현
actual fun vibrate() {
    UIImpactFeedbackGenerator().impactOccurred()
}
```

#### 3. 생태계 성숙도
React Native나 Flutter에 비해 상대적으로 신생 기술입니다.

---

## 개발 환경 설정

### 필수 요구사항

#### 1. JDK (Java Development Kit)
- **버전**: JDK 17 이상 권장
- **다운로드**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 또는 [OpenJDK](https://adoptium.net/)

**설치 확인**:
```bash
java -version
# 출력 예시: openjdk version "17.0.8"
```

#### 2. IDE (통합 개발 환경)

**옵션 1: IntelliJ IDEA (권장)**
- **버전**: 2023.2 이상
- **에디션**: Community (무료) 또는 Ultimate
- **다운로드**: [JetBrains 공식 사이트](https://www.jetbrains.com/idea/download/)
- **장점**: Compose Multiplatform 최적화, Kotlin 최고 지원

**옵션 2: Android Studio**
- **버전**: Hedgehog (2023.1.1) 이상
- **다운로드**: [Android Studio](https://developer.android.com/studio)
- **장점**: Android 개발에 특화, 무료

#### 3. 플랫폼별 추가 요구사항

**Android 개발**:
- Android SDK (API 21 이상)
- Android Studio 또는 Android SDK Command-line Tools

**iOS 개발** (macOS만 가능):
- Xcode 14.0 이상
- CocoaPods: `sudo gem install cocoapods`
- Xcode Command Line Tools: `xcode-select --install`

**Desktop 개발**:
- 추가 요구사항 없음 (JDK만 있으면 됨)

### IDE 플러그인 설치

#### IntelliJ IDEA / Android Studio

1. **Kotlin Multiplatform Mobile 플러그인**
   - File → Settings → Plugins
   - "Kotlin Multiplatform Mobile" 검색
   - Install 클릭

2. **Compose Multiplatform IDE Support**
   - 자동으로 설치되지만, 최신 버전 확인
   - Compose Preview 기능 제공

---

## 첫 번째 프로젝트 만들기

### 방법 1: Kotlin Multiplatform Wizard (권장)

#### 1. 웹 브라우저로 프로젝트 생성

1. [Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/) 접속
2. 프로젝트 설정:
   ```
   Project name: MyFirstKMPApp
   Project ID: com.example.myfirstkmpapp
   ```
3. 플랫폼 선택:
   - ✅ Android
   - ✅ iOS (macOS 사용자만)
   - ✅ Desktop
4. "Download" 버튼 클릭
5. 다운로드한 ZIP 파일 압축 해제

#### 2. IDE에서 프로젝트 열기

1. IntelliJ IDEA 실행
2. File → Open
3. 압축 해제한 폴더 선택
4. "Trust Project" 클릭
5. Gradle 동기화 대기 (첫 실행 시 시간 소요)

### 방법 2: IDE에서 직접 생성

#### IntelliJ IDEA

1. File → New → Project
2. 왼쪽 메뉴에서 "Kotlin Multiplatform" 선택
3. 오른쪽에서 "Compose Multiplatform Application" 선택
4. 프로젝트 정보 입력:
   ```
   Name: MyFirstKMPApp
   Location: C:\Projects\MyFirstKMPApp
   ```
5. 타겟 플랫폼 선택
6. "Create" 버튼 클릭

### 프로젝트 생성 후 확인사항

#### Gradle 동기화 성공 확인
```
BUILD SUCCESSFUL in 1m 23s
```

#### 프로젝트 구조 확인
```
MyFirstKMPApp/
├── composeApp/           ✅ 존재해야 함
├── iosApp/              ✅ iOS 선택 시
├── gradle/              ✅ 존재해야 함
├── build.gradle.kts     ✅ 존재해야 함
└── settings.gradle.kts  ✅ 존재해야 함
```

---

## 프로젝트 구조 이해하기

### 전체 구조 개요

```
MyFirstKMPApp/
│
├── composeApp/                    # 메인 애플리케이션 모듈
│   ├── src/
│   │   ├── commonMain/           # 🌍 모든 플랫폼 공통 코드
│   │   │   ├── kotlin/
│   │   │   │   └── com/example/app/
│   │   │   │       ├── App.kt    # 메인 UI 컴포저블
│   │   │   │       └── ...
│   │   │   └── composeResources/ # 공통 리소스
│   │   │       ├── drawable/     # 이미지
│   │   │       ├── values/       # 문자열, 색상 등
│   │   │       └── font/         # 폰트
│   │   │
│   │   ├── androidMain/          # 🤖 Android 전용 코드
│   │   │   ├── kotlin/
│   │   │   │   └── com/example/app/
│   │   │   │       ├── MainActivity.kt
│   │   │   │       └── Platform.android.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/              # Android 리소스
│   │   │
│   │   ├── iosMain/              # 🍎 iOS 전용 코드
│   │   │   └── kotlin/
│   │   │       └── com/example/app/
│   │   │           ├── MainViewController.kt
│   │   │           └── Platform.ios.kt
│   │   │
│   │   └── desktopMain/          # 🖥️ Desktop 전용 코드
│   │       └── kotlin/
│   │           └── com/example/app/
│   │               ├── main.kt
│   │               └── Platform.desktop.kt
│   │
│   └── build.gradle.kts          # 모듈 빌드 설정
│
├── iosApp/                        # iOS 네이티브 래퍼 (Swift)
│   ├── iosApp/
│   │   ├── ContentView.swift     # SwiftUI 진입점
│   │   └── iosApp.swift
│   └── iosApp.xcodeproj/         # Xcode 프로젝트
│
├── gradle/                        # Gradle 래퍼
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── build.gradle.kts               # 루트 빌드 설정
├── settings.gradle.kts            # 프로젝트 설정
├── gradle.properties              # Gradle 속성
└── local.properties               # 로컬 SDK 경로 (Git 제외)
```

### 주요 디렉토리 설명

#### 1. commonMain - 공통 코드 디렉토리

**역할**: 모든 플랫폼에서 공유되는 코드

**포함 내용**:
- UI 컴포저블 (Composable functions)
- 비즈니스 로직
- 데이터 모델
- 공통 유틸리티

**예시 - App.kt**:
```kotlin
package com.example.app

import androidx.compose.material3.*
import androidx.compose.runtime.*

/**
 * 앱의 메인 진입점
 * 모든 플랫폼에서 이 컴포저블을 호출합니다
 */
@Composable
fun App() {
    MaterialTheme {
        Surface {
            Greeting("Compose Multiplatform")
        }
    }
}

/**
 * 인사말을 표시하는 컴포저블
 * @param name 표시할 이름
 */
@Composable
fun Greeting(name: String) {
    Text(
        text = "Hello, $name!",
        style = MaterialTheme.typography.headlineMedium
    )
}
```

#### 2. androidMain - Android 전용 코드

**역할**: Android 플랫폼에서만 실행되는 코드

**MainActivity.kt**:
```kotlin
package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Android 앱의 메인 액티비티
 * Compose UI를 설정합니다
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Compose UI 설정
        setContent {
            App()  // commonMain의 App() 호출
        }
    }
}
```

**AndroidManifest.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 인터넷 권한 (API 호출 시 필요) -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        
        <!-- 메인 액티비티 -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

#### 3. iosMain - iOS 전용 코드

**역할**: iOS 플랫폼에서만 실행되는 코드

**MainViewController.kt**:
```kotlin
package com.example.app

import androidx.compose.ui.window.ComposeUIViewController

/**
 * iOS에서 사용할 UIViewController 생성
 * SwiftUI에서 이 함수를 호출합니다
 */
fun MainViewController() = ComposeUIViewController {
    App()  // commonMain의 App() 호출
}
```

#### 4. desktopMain - Desktop 전용 코드

**역할**: Windows, macOS, Linux에서 실행되는 코드

**main.kt**:
```kotlin
package com.example.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Desktop 앱의 진입점
 */
fun main() = application {
    // 윈도우 생성
    Window(
        onCloseRequest = ::exitApplication,  // X 버튼 클릭 시 앱 종료
        title = "My First KMP App"           // 윈도우 제목
    ) {
        App()  // commonMain의 App() 호출
    }
}
```

#### 5. iosApp - iOS 네이티브 래퍼

**ContentView.swift**:
```swift
import SwiftUI
import ComposeApp  // Kotlin 모듈 임포트

struct ContentView: View {
    var body: some View {
        // Kotlin의 MainViewController 사용
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Kotlin 함수 호출
        return MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // 업데이트 로직 (필요 시)
    }
}
```

---

## Gradle 설정 상세 가이드

### 루트 build.gradle.kts

```kotlin
// 플러그인 정의 (실제 적용은 하지 않음)
plugins {
    // Kotlin Multiplatform 플러그인
    alias(libs.plugins.kotlinMultiplatform) apply false
    
    // Android 애플리케이션 플러그인
    alias(libs.plugins.androidApplication) apply false
    
    // Jetbrains Compose 플러그인
    alias(libs.plugins.jetbrainsCompose) apply false
}
```

### composeApp/build.gradle.kts (상세 설명)

```kotlin
plugins {
    // Kotlin Multiplatform 플러그인 적용
    alias(libs.plugins.kotlinMultiplatform)
    
    // Android 앱 플러그인 적용
    alias(libs.plugins.androidApplication)
    
    // Compose Multiplatform 플러그인 적용
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    // ========================================
    // Android 타겟 설정
    // ========================================
    androidTarget {
        compilations.all {
            kotlinOptions {
                // Java 11 바이트코드로 컴파일
                jvmTarget = "11"
            }
        }
    }
    
    // ========================================
    // iOS 타겟 설정
    // ========================================
    // iOS는 여러 아키텍처를 지원해야 합니다:
    // - iosX64: Intel Mac 시뮬레이터
    // - iosArm64: 실제 iPhone/iPad 기기
    // - iosSimulatorArm64: Apple Silicon Mac 시뮬레이터
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // 프레임워크 이름 (Swift에서 import할 때 사용)
            baseName = "ComposeApp"
            
            // 정적 프레임워크 (앱 크기 최적화)
            isStatic = true
            
            // 필요 시 동적 프레임워크로 변경 가능
            // isStatic = false
        }
    }
    
    // ========================================
    // Desktop 타겟 설정
    // ========================================
    jvm("desktop")
    
    // ========================================
    // 소스 세트 설정
    // ========================================
    sourceSets {
        // ------------------------------------
        // 공통 메인 소스 세트
        // ------------------------------------
        val commonMain by getting {
            dependencies {
                // Compose 런타임
                implementation(compose.runtime)
                
                // Compose Foundation (기본 UI 요소)
                implementation(compose.foundation)
                
                // Material 3 디자인 시스템
                implementation(compose.material3)
                
                // Compose UI 코어
                implementation(compose.ui)
                
                // Compose 리소스 (이미지, 문자열 등)
                implementation(compose.components.resources)
                
                // Compose 아이콘 (선택사항)
                // implementation(compose.materialIconsExtended)
            }
        }
        
        // ------------------------------------
        // 공통 테스트 소스 세트
        // ------------------------------------
        val commonTest by getting {
            dependencies {
                // Kotlin 테스트 라이브러리
                implementation(kotlin("test"))
            }
        }
        
        // ------------------------------------
        // Android 메인 소스 세트
        // ------------------------------------
        val androidMain by getting {
            dependencies {
                // Compose Activity (Android 통합)
                implementation(libs.androidx.activity.compose)
                
                // Android Compose UI Tooling (Preview 기능)
                implementation(libs.androidx.compose.ui.tooling.preview)
            }
        }
        
        // ------------------------------------
        // iOS 메인 소스 세트
        // ------------------------------------
        val iosMain by creating {
            dependsOn(commonMain)
        }
        
        // 각 iOS 타겟이 iosMain을 공유
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        
        // ------------------------------------
        // Desktop 메인 소스 세트
        // ------------------------------------
        val desktopMain by getting {
            dependencies {
                // Desktop Compose (현재 OS에 맞는 버전)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

// ========================================
// Android 설정
// ========================================
android {
    namespace = "com.example.app"
    compileSdk = 34  // 최신 Android SDK
    
    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 21      // Android 5.0 이상
        targetSdk = 34   // 최신 타겟 SDK
        versionCode = 1
        versionName = "1.0.0"
    }
    
    // 빌드 타입 설정
    buildTypes {
        release {
            // ProGuard/R8 최적화 (선택사항)
            isMinifyEnabled = false
        }
    }
    
    // Java 버전 설정
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

### gradle.properties (성능 최적화)

```properties
# Kotlin 설정
kotlin.code.style=official
kotlin.version=1.9.20

# Android 설정
android.useAndroidX=true
android.nonTransitiveRClass=true

# Gradle 성능 최적화
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Kotlin Multiplatform 설정
kotlin.mpp.stability.nowarn=true
kotlin.mpp.androidSourceSetLayoutVersion=2

# iOS 빌드 최적화
kotlin.native.cacheKind=none
```

---

## Hello World 앱 만들기

### 1단계: 기본 UI 작성

**commonMain/kotlin/App.kt**:
```kotlin
package com.example.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 앱의 메인 컴포저블
 * 
 * @Composable 어노테이션:
 * - 이 함수가 UI를 생성하는 Composable 함수임을 표시
 * - Compose 컴파일러가 특별하게 처리
 */
@Composable
fun App() {
    // Material 3 테마 적용
    // 앱 전체의 색상, 타이포그래피, 모양을 정의
    MaterialTheme {
        // Surface: Material Design의 기본 컨테이너
        Surface(
            modifier = Modifier.fillMaxSize(),  // 전체 화면 크기
            color = MaterialTheme.colorScheme.background  // 배경색
        ) {
            // 메인 콘텐츠
            HelloWorldScreen()
        }
    }
}

/**
 * Hello World 화면
 */
@Composable
fun HelloWorldScreen() {
    // 상태 관리: 버튼 클릭 횟수
    // remember: 리컴포지션 시에도 값을 유지
    // mutableStateOf: 값이 변경되면 UI가 자동으로 업데이트
    var clickCount by remember { mutableStateOf(0) }
    
    // 세로 방향 레이아웃
    Column(
        modifier = Modifier
            .fillMaxSize()           // 전체 화면 크기
            .padding(16.dp),         // 모든 방향에 16dp 여백
        horizontalAlignment = Alignment.CenterHorizontally,  // 가로 중앙 정렬
        verticalArrangement = Arrangement.Center             // 세로 중앙 정렬
    ) {
        // 제목 텍스트
        Text(
            text = "Hello, Compose Multiplatform!",
            style = MaterialTheme.typography.headlineLarge,  // 큰 제목 스타일
            color = MaterialTheme.colorScheme.primary        // 기본 색상
        )
        
        // 수직 간격 (16dp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // 플랫폼 정보 표시
        Text(
            text = "Running on: ${getPlatformName()}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        // 수직 간격 (32dp)
        Spacer(modifier = Modifier.height(32.dp))
        
        // 클릭 횟수 표시
        Text(
            text = "Button clicked $clickCount times",
            style = MaterialTheme.typography.titleMedium
        )
        
        // 수직 간격 (16dp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // 클릭 버튼
        Button(
            onClick = {
                // 클릭 시 카운트 증가
                clickCount++
            }
        ) {
            Text("Click Me!")
        }
    }
}

/**
 * 플랫폼 이름을 반환하는 함수
 * expect: 각 플랫폼에서 실제 구현을 제공해야 함
 */
expect fun getPlatformName(): String
```

### 2단계: 플랫폼별 구현

**androidMain/kotlin/Platform.android.kt**:
```kotlin
package com.example.app

import android.os.Build

/**
 * Android 플랫폼 이름 반환
 * actual: expect 함수의 실제 구현
 */
actual fun getPlatformName(): String {
    // Android 버전 정보 반환
    return "Android ${Build.VERSION.SDK_INT} (${Build.MODEL})"
}
```

**iosMain/kotlin/Platform.ios.kt**:
```kotlin
package com.example.app

import platform.UIKit.UIDevice

/**
 * iOS 플랫폼 이름 반환
 */
actual fun getPlatformName(): String {
    val device = UIDevice.currentDevice
    // iOS 시스템 정보 반환
    return "${device.systemName} ${device.systemVersion} (${device.model})"
}
```

**desktopMain/kotlin/Platform.desktop.kt**:
```kotlin
package com.example.app

/**
 * Desktop 플랫폼 이름 반환
 */
actual fun getPlatformName(): String {
    val os = System.getProperty("os.name")      // OS 이름
    val version = System.getProperty("os.version")  // OS 버전
    val arch = System.getProperty("os.arch")    // 아키텍처
    
    return "$os $version ($arch)"
}
```

### 3단계: 앱 실행

#### Android에서 실행

1. Android 기기 연결 또는 에뮬레이터 실행
2. IDE 상단의 실행 구성에서 "composeApp" 선택
3. 실행 버튼 클릭 (▶️) 또는 `Shift + F10`

**또는 Gradle 명령어**:
```bash
./gradlew :composeApp:installDebug
```

#### iOS에서 실행 (macOS만 가능)

1. IDE 상단의 실행 구성에서 "iosApp" 선택
2. 시뮬레이터 선택 (예: iPhone 15)
3. 실행 버튼 클릭

**또는 Xcode에서**:
1. `iosApp/iosApp.xcodeproj` 열기
2. 시뮬레이터 선택
3. Run 버튼 클릭

#### Desktop에서 실행

**Gradle 명령어**:
```bash
./gradlew :composeApp:run
```

**또는 IDE에서**:
1. `desktopMain/kotlin/main.kt` 파일 열기
2. `main` 함수 옆의 실행 버튼 클릭

---

## 다음 단계

이제 기본적인 Compose Multiplatform 프로젝트를 만들고 실행할 수 있습니다!

다음 문서에서는:
- **아키텍처 패턴** (MVVM, MVI)
- **expect/actual 패턴** 심화
- **의존성 주입**
- **플랫폼별 코드 작성 전략**

을 다룹니다.

## 참고 자료

- [Compose Multiplatform 공식 문서](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Kotlin Multiplatform 가이드](https://kotlinlang.org/docs/multiplatform.html)
- [샘플 프로젝트](https://github.com/JetBrains/compose-multiplatform)
