# Compose Multiplatform 리소스 관리 및 다국어 지원

## 목차
1. [Compose Resources 시스템](#compose-resources-시스템)
2. [문자열 리소스](#문자열-리소스)
3. [이미지 리소스](#이미지-리소스)
4. [폰트 리소스](#폰트-리소스)
5. [색상 및 테마](#색상-및-테마)
6. [다국어 지원 (i18n)](#다국어-지원-i18n)
7. [플랫폼별 리소스](#플랫폼별-리소스)
8. [실전 예제](#실전-예제)

---

## Compose Resources 시스템

### 리소스 폴더 구조

```
composeApp/src/commonMain/composeResources/
├── drawable/              # 이미지 리소스
│   ├── ic_logo.xml       # 벡터 이미지
│   ├── image.png         # 래스터 이미지
│   └── background.jpg
├── values/               # 값 리소스
│   ├── strings.xml       # 문자열
│   └── colors.xml        # 색상
└── font/                 # 폰트 리소스
    ├── roboto_regular.ttf
    └── roboto_bold.ttf
```

### 의존성 추가

**build.gradle.kts**:
```kotlin
commonMain.dependencies {
    implementation(compose.components.resources)
}
```

---

## 문자열 리소스

### strings.xml 정의

**composeResources/values/strings.xml**:
```xml
<resources>
    <!-- 기본 문자열 -->
    <string name="app_name">My Multiplatform App</string>
    <string name="welcome">Welcome!</string>
    <string name="hello">Hello, %s!</string>
    
    <!-- 버튼 텍스트 -->
    <string name="button_ok">OK</string>
    <string name="button_cancel">Cancel</string>
    <string name="button_save">Save</string>
    <string name="button_delete">Delete</string>
    
    <!-- 메시지 -->
    <string name="message_success">Operation completed successfully</string>
    <string name="message_error">An error occurred</string>
    
    <!-- 복수형 -->
    <plurals name="items_count">
        <item quantity="one">%d item</item>
        <item quantity="other">%d items</item>
    </plurals>
</resources>
```

### 문자열 사용

```kotlin
import org.jetbrains.compose.resources.*

@Composable
fun StringResourceExample() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 기본 문자열
        Text(stringResource(Res.string.welcome))
        
        // 포맷팅된 문자열
        Text(stringResource(Res.string.hello, "John"))
        
        // 복수형
        Text(pluralStringResource(Res.plurals.items_count, 1, 1))
        Text(pluralStringResource(Res.plurals.items_count, 5, 5))
    }
}
```

---

## 이미지 리소스

### 이미지 파일 추가

```
composeResources/drawable/
├── ic_home.xml           # 벡터 이미지 (Android Vector Drawable)
├── logo.png              # PNG 이미지
├── logo@2x.png           # 고해상도 (iOS 스타일)
└── logo@3x.png           # 초고해상도
```

### 벡터 이미지 (XML)

**ic_home.xml**:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/>
</vector>
```

### 이미지 사용

```kotlin
import org.jetbrains.compose.resources.painterResource

@Composable
fun ImageResourceExample() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 벡터 이미지
        Image(
            painter = painterResource(Res.drawable.ic_home),
            contentDescription = "Home",
            modifier = Modifier.size(48.dp)
        )
        
        // PNG 이미지
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )
        
        // 아이콘으로 사용
        Icon(
            painter = painterResource(Res.drawable.ic_home),
            contentDescription = "Home",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
```

### 비동기 이미지 로딩 (Coil)

**의존성 추가**:
```kotlin
commonMain.dependencies {
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha01")
    implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha01")
}
```

**사용 예시**:
```kotlin
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun AsyncImageExample() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 기본 사용
        AsyncImage(
            model = "https://example.com/image.jpg",
            contentDescription = "Remote image",
            modifier = Modifier.size(200.dp)
        )
        
        // 고급 설정
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data("https://example.com/image.jpg")
                .crossfade(true)
                .build(),
            contentDescription = "Remote image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(Res.drawable.placeholder),
            error = painterResource(Res.drawable.error)
        )
    }
}
```

---

## 폰트 리소스

### 폰트 파일 추가

```
composeResources/font/
├── roboto_regular.ttf
├── roboto_bold.ttf
├── roboto_italic.ttf
└── roboto_bold_italic.ttf
```

### 폰트 사용

```kotlin
import org.jetbrains.compose.resources.Font

@Composable
fun FontResourceExample() {
    // 폰트 패밀리 정의
    val robotoFamily = FontFamily(
        Font(Res.font.roboto_regular, FontWeight.Normal),
        Font(Res.font.roboto_bold, FontWeight.Bold),
        Font(Res.font.roboto_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.roboto_bold_italic, FontWeight.Bold, FontStyle.Italic)
    )
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Regular Text",
            fontFamily = robotoFamily,
            fontWeight = FontWeight.Normal
        )
        
        Text(
            text = "Bold Text",
            fontFamily = robotoFamily,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Italic Text",
            fontFamily = robotoFamily,
            fontStyle = FontStyle.Italic
        )
    }
}
```

### 커스텀 테마에 폰트 적용

```kotlin
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val robotoFamily = FontFamily(
        Font(Res.font.roboto_regular, FontWeight.Normal),
        Font(Res.font.roboto_bold, FontWeight.Bold)
    )
    
    val typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = robotoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        titleLarge = TextStyle(
            fontFamily = robotoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    )
    
    MaterialTheme(
        typography = typography,
        content = content
    )
}
```

---

## 색상 및 테마

### colors.xml 정의

**composeResources/values/colors.xml**:
```xml
<resources>
    <color name="primary">#6200EE</color>
    <color name="primary_variant">#3700B3</color>
    <color name="secondary">#03DAC6</color>
    <color name="background">#FFFFFF</color>
    <color name="surface">#FFFFFF</color>
    <color name="error">#B00020</color>
</resources>
```

### 색상 사용

```kotlin
@Composable
fun ColorResourceExample() {
    val primaryColor = colorResource(Res.color.primary)
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(primaryColor)
    )
}
```

### Material 3 테마 정의

```kotlin
/**
 * 라이트 테마 색상
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBB86FC),
    onPrimaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB00020),
    onError = Color.White
)

/**
 * 다크 테마 색상
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

/**
 * 앱 테마
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 다국어 지원 (i18n)

### 언어별 strings.xml

```
composeResources/values/
├── strings.xml           # 기본 (영어)
├── strings-ko.xml        # 한국어
├── strings-ja.xml        # 일본어
└── strings-es.xml        # 스페인어
```

**strings.xml (기본 - 영어)**:
```xml
<resources>
    <string name="app_name">My App</string>
    <string name="welcome">Welcome!</string>
    <string name="greeting">Hello, %s!</string>
    <string name="items_count">%d items</string>
</resources>
```

**strings-ko.xml (한국어)**:
```xml
<resources>
    <string name="app_name">내 앱</string>
    <string name="welcome">환영합니다!</string>
    <string name="greeting">안녕하세요, %s님!</string>
    <string name="items_count">%d개 항목</string>
</resources>
```

**strings-ja.xml (일본어)**:
```xml
<resources>
    <string name="app_name">私のアプリ</string>
    <string name="welcome">ようこそ!</string>
    <string name="greeting">こんにちは、%sさん!</string>
    <string name="items_count">%d件のアイテム</string>
</resources>
```

### 언어 전환

```kotlin
/**
 * 언어 설정 관리
 */
class LanguageManager {
    private var currentLanguage by mutableStateOf("en")
    
    /**
     * 언어 변경
     */
    fun setLanguage(languageCode: String) {
        currentLanguage = languageCode
        // 플랫폼별 언어 설정 적용
        applyLanguage(languageCode)
    }
    
    /**
     * 현재 언어 가져오기
     */
    fun getCurrentLanguage(): String = currentLanguage
}

/**
 * 플랫폼별 언어 적용
 */
expect fun applyLanguage(languageCode: String)
```

**Android 구현**:
```kotlin
import android.content.res.Configuration
import java.util.Locale

actual fun applyLanguage(languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    
    val config = Configuration()
    config.setLocale(locale)
    
    // Context에서 리소스 업데이트
    // 실제로는 Activity recreate 필요
}
```

**iOS 구현**:
```kotlin
actual fun applyLanguage(languageCode: String) {
    // iOS는 앱 재시작 필요
    // UserDefaults에 저장
}
```

### 언어 선택 UI

```kotlin
@Composable
fun LanguageSelector() {
    val languageManager = remember { LanguageManager() }
    var currentLanguage by remember { mutableStateOf(languageManager.getCurrentLanguage()) }
    
    val languages = listOf(
        "en" to "English",
        "ko" to "한국어",
        "ja" to "日本語",
        "es" to "Español"
    )
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Language",
            style = MaterialTheme.typography.titleLarge
        )
        
        languages.forEach { (code, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentLanguage = code
                        languageManager.setLanguage(code)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLanguage == code,
                    onClick = {
                        currentLanguage = code
                        languageManager.setLanguage(code)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(name)
            }
        }
    }
}
```

---

## 플랫폼별 리소스

### Android 전용 리소스

**androidMain/res/values/strings.xml**:
```xml
<resources>
    <string name="android_specific">Android Only String</string>
</resources>
```

### iOS 전용 리소스

**iosMain/Resources/** 폴더에 리소스 추가

### 플랫폼별 리소스 접근

```kotlin
/**
 * 플랫폼별 리소스 접근
 */
expect fun getPlatformSpecificString(key: String): String

// Android
actual fun getPlatformSpecificString(key: String): String {
    return when (key) {
        "platform_name" -> "Android"
        else -> ""
    }
}

// iOS
actual fun getPlatformSpecificString(key: String): String {
    return when (key) {
        "platform_name" -> "iOS"
        else -> ""
    }
}
```

---

## 실전 예제

### 다국어 지원 앱

```kotlin
/**
 * 다국어 지원 메인 앱
 */
@Composable
fun MultilingualApp() {
    var currentLanguage by remember { mutableStateOf("en") }
    
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                    actions = {
                        // 언어 선택 버튼
                        IconButton(onClick = { /* 언어 선택 다이얼로그 표시 */ }) {
                            Icon(Icons.Default.Language, contentDescription = "Language")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // 환영 메시지
                Text(
                    text = stringResource(Res.string.welcome),
                    style = MaterialTheme.typography.headlineLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 인사말
                Text(
                    text = stringResource(Res.string.greeting, "User"),
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 아이템 카운트
                val itemCount = 5
                Text(
                    text = stringResource(Res.string.items_count, itemCount),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```

---

## 다음 단계

다음 문서에서는:
- **네트워킹 (Ktor)**
- **API 통신**
- **JSON 파싱**

를 다룹니다.
