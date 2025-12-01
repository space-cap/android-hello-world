# Compose Multiplatform 가이드

## 목차
1. [Compose Multiplatform이란?](#compose-multiplatform이란)
2. [프로젝트 설정](#프로젝트-설정)
3. [기본 구조](#기본-구조)
4. [플랫폼별 코드 작성](#플랫폼별-코드-작성)
5. [리소스 관리](#리소스-관리)
6. [네비게이션](#네비게이션)
7. [실전 예제](#실전-예제)

## Compose Multiplatform이란?

**Compose Multiplatform**은 JetBrains에서 개발한 크로스 플랫폼 UI 프레임워크입니다. Jetpack Compose를 기반으로 하나의 코드베이스로 Android, iOS, Desktop, Web 앱을 개발할 수 있습니다.

### 주요 특징
- **코드 재사용**: UI 코드를 여러 플랫폼에서 공유
- **네이티브 성능**: 각 플랫폼의 네이티브 UI로 렌더링
- **Kotlin 기반**: 타입 안전성과 현대적인 언어 기능
- **선언적 UI**: Compose의 직관적인 UI 작성 방식

### 지원 플랫폼
- Android (API 21+)
- iOS (iOS 14+)
- Desktop (Windows, macOS, Linux)
- Web (실험적)

## 프로젝트 설정

### 1. 새 프로젝트 생성

**IntelliJ IDEA 또는 Android Studio**에서:
1. File → New → Project
2. "Kotlin Multiplatform" 선택
3. "Compose Multiplatform Application" 템플릿 선택
4. 타겟 플랫폼 선택 (Android, iOS, Desktop)

### 2. 프로젝트 구조

```
MyApp/
├── composeApp/              # 공유 UI 코드
│   ├── src/
│   │   ├── commonMain/      # 모든 플랫폼 공통 코드
│   │   ├── androidMain/     # Android 전용 코드
│   │   ├── iosMain/         # iOS 전용 코드
│   │   └── desktopMain/     # Desktop 전용 코드
│   └── build.gradle.kts
├── iosApp/                  # iOS 네이티브 래퍼
└── build.gradle.kts
```

### 3. Gradle 설정

**composeApp/build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    // Android 타겟
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    // iOS 타겟
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    // Desktop 타겟
    jvm("desktop")
    
    sourceSets {
        // 공통 의존성
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
            }
        }
        
        // Android 의존성
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
        
        // Desktop 의존성
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
```

## 기본 구조

### 공통 UI 코드 (commonMain)

**App.kt**:
```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 앱의 메인 컴포저블
 * 모든 플랫폼에서 공유되는 UI 진입점
 */
@Composable
fun App() {
    // Material 3 테마 적용
    MaterialTheme {
        // 전체 화면을 채우는 Surface
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // 메인 화면 표시
            MainScreen()
        }
    }
}

/**
 * 메인 화면 컴포저블
 */
@Composable
fun MainScreen() {
    // 상태 관리: 카운터 값
    var count by remember { mutableStateOf(0) }
    
    // 세로 방향 레이아웃
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 타이틀
        Text(
            text = "Compose Multiplatform",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 플랫폼 정보 표시
        Text(
            text = "Platform: ${getPlatformName()}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 카운터 표시
        Text(
            text = "Count: $count",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 증가 버튼
        Button(onClick = { count++ }) {
            Text("Increment")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 감소 버튼
        OutlinedButton(onClick = { count-- }) {
            Text("Decrement")
        }
    }
}
```

## 플랫폼별 코드 작성

### expect/actual 패턴

**공통 코드 (commonMain/Platform.kt)**:
```kotlin
/**
 * 플랫폼 이름을 반환하는 함수
 * expect: 각 플랫폼에서 구현을 제공해야 함
 */
expect fun getPlatformName(): String

/**
 * 플랫폼별 설정을 제공하는 인터페이스
 */
expect class PlatformConfig {
    val isDebug: Boolean
    val appVersion: String
}
```

**Android 구현 (androidMain/Platform.android.kt)**:
```kotlin
import android.os.Build

/**
 * Android 플랫폼 이름 반환
 */
actual fun getPlatformName(): String {
    return "Android ${Build.VERSION.SDK_INT}"
}

/**
 * Android 플랫폼 설정
 */
actual class PlatformConfig {
    actual val isDebug: Boolean = BuildConfig.DEBUG
    actual val appVersion: String = BuildConfig.VERSION_NAME
}
```

**iOS 구현 (iosMain/Platform.ios.kt)**:
```kotlin
import platform.UIKit.UIDevice

/**
 * iOS 플랫폼 이름 반환
 */
actual fun getPlatformName(): String {
    val device = UIDevice.currentDevice
    return "${device.systemName} ${device.systemVersion}"
}

/**
 * iOS 플랫폼 설정
 */
actual class PlatformConfig {
    actual val isDebug: Boolean = Platform.isDebugBinary
    actual val appVersion: String = "1.0.0" // Info.plist에서 읽기
}
```

**Desktop 구현 (desktopMain/Platform.desktop.kt)**:
```kotlin
/**
 * Desktop 플랫폼 이름 반환
 */
actual fun getPlatformName(): String {
    val os = System.getProperty("os.name")
    val version = System.getProperty("os.version")
    return "$os $version"
}

/**
 * Desktop 플랫폼 설정
 */
actual class PlatformConfig {
    actual val isDebug: Boolean = true
    actual val appVersion: String = "1.0.0"
}
```

## 리소스 관리

### Compose Resources 사용

**composeResources 폴더 구조**:
```
composeApp/src/commonMain/composeResources/
├── drawable/
│   ├── ic_logo.xml
│   └── image.png
├── values/
│   └── strings.xml
└── font/
    └── custom_font.ttf
```

**strings.xml**:
```xml
<resources>
    <string name="app_name">My Multiplatform App</string>
    <string name="welcome">Welcome to Compose Multiplatform!</string>
    <string name="button_click">Click Me</string>
</resources>
```

**리소스 사용 예제**:
```kotlin
import org.jetbrains.compose.resources.*

@Composable
fun ResourceExample() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 문자열 리소스
        Text(
            text = stringResource(Res.string.welcome)
        )
        
        // 이미지 리소스
        Image(
            painter = painterResource(Res.drawable.ic_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )
        
        // 버튼
        Button(onClick = { /* 액션 */ }) {
            Text(stringResource(Res.string.button_click))
        }
    }
}
```

## 네비게이션

### Voyager 라이브러리 사용

**의존성 추가**:
```kotlin
commonMain.dependencies {
    implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
    implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
}
```

**Screen 정의**:
```kotlin
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * 홈 화면
 */
class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Home Screen",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 상세 화면으로 이동
            Button(onClick = {
                navigator.push(DetailScreen(id = 1))
            }) {
                Text("Go to Detail")
            }
        }
    }
}

/**
 * 상세 화면
 */
data class DetailScreen(val id: Int) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Detail Screen #$id",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 뒤로 가기
            Button(onClick = {
                navigator.pop()
            }) {
                Text("Go Back")
            }
        }
    }
}
```

**Navigator 설정**:
```kotlin
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun App() {
    MaterialTheme {
        // Navigator로 화면 관리
        Navigator(HomeScreen())
    }
}
```

## 실전 예제

### 할 일 목록 앱

**데이터 모델**:
```kotlin
/**
 * 할 일 아이템 데이터 클래스
 */
data class TodoItem(
    val id: Int,
    val title: String,
    val isCompleted: Boolean = false
)

/**
 * 할 일 상태 관리 클래스
 */
class TodoViewModel {
    // 할 일 목록 상태
    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> = _todos
    
    // 다음 ID
    private var nextId = 1
    
    /**
     * 할 일 추가
     */
    fun addTodo(title: String) {
        if (title.isNotBlank()) {
            _todos.add(
                TodoItem(
                    id = nextId++,
                    title = title.trim()
                )
            )
        }
    }
    
    /**
     * 할 일 완료 상태 토글
     */
    fun toggleTodo(id: Int) {
        val index = _todos.indexOfFirst { it.id == id }
        if (index != -1) {
            _todos[index] = _todos[index].copy(
                isCompleted = !_todos[index].isCompleted
            )
        }
    }
    
    /**
     * 할 일 삭제
     */
    fun deleteTodo(id: Int) {
        _todos.removeAll { it.id == id }
    }
}
```

**UI 구현**:
```kotlin
@Composable
fun TodoApp() {
    // ViewModel 인스턴스
    val viewModel = remember { TodoViewModel() }
    
    // 입력 필드 상태
    var inputText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 바
        TopAppBar(
            title = { Text("Todo List") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        // 입력 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 텍스트 입력 필드
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("New Todo") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 추가 버튼
            Button(
                onClick = {
                    viewModel.addTodo(inputText)
                    inputText = "" // 입력 필드 초기화
                },
                enabled = inputText.isNotBlank()
            ) {
                Text("Add")
            }
        }
        
        // 할 일 목록
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(
                items = viewModel.todos,
                key = { it.id }
            ) { todo ->
                TodoItemRow(
                    todo = todo,
                    onToggle = { viewModel.toggleTodo(todo.id) },
                    onDelete = { viewModel.deleteTodo(todo.id) }
                )
            }
        }
    }
}

/**
 * 할 일 아이템 행
 */
@Composable
fun TodoItemRow(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 체크박스
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 제목 (완료시 취소선)
            Text(
                text = todo.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (todo.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    null
                },
                color = if (todo.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            // 삭제 버튼
            IconButton(onClick = onDelete) {
                Text("🗑️")
            }
        }
    }
}
```

## 플랫폼별 실행

### Android
```kotlin
// androidMain/MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App() // 공통 UI 호출
        }
    }
}
```

### iOS
```kotlin
// iosMain/MainViewController.kt
fun MainViewController() = ComposeUIViewController {
    App() // 공통 UI 호출
}
```

### Desktop
```kotlin
// desktopMain/main.kt
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "My Multiplatform App"
    ) {
        App() // 공통 UI 호출
    }
}
```

## 베스트 프랙티스

### 1. 코드 공유 전략
- **UI 로직**: commonMain에 최대한 많이
- **플랫폼 API**: expect/actual 패턴 사용
- **비즈니스 로직**: 100% 공유 가능

### 2. 성능 최적화
```kotlin
// remember로 재구성 최소화
@Composable
fun OptimizedComponent(data: List<String>) {
    val processedData = remember(data) {
        data.map { it.uppercase() }
    }
    
    LazyColumn {
        items(processedData) { item ->
            Text(item)
        }
    }
}
```

### 3. 플랫폼별 UI 조정
```kotlin
@Composable
fun PlatformAdaptiveButton(onClick: () -> Unit) {
    val buttonHeight = when (getPlatformName()) {
        "iOS" -> 50.dp
        "Android" -> 48.dp
        else -> 40.dp
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier.height(buttonHeight)
    ) {
        Text("Click Me")
    }
}
```

## 다음 단계

1. **네트워킹**: Ktor로 API 통신
2. **데이터베이스**: SQLDelight로 로컬 저장
3. **상태 관리**: MVI 패턴 적용
4. **테스팅**: 공통 테스트 작성

## 참고 자료

- [공식 문서](https://www.jetbrains.com/lp/compose-multiplatform/)
- [샘플 프로젝트](https://github.com/JetBrains/compose-multiplatform)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
