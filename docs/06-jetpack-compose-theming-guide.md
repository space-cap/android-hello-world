# Jetpack Compose 테마와 스타일링 완벽 가이드

## 📚 목차
1. [Material Design 3 소개](#material-design-3-소개)
2. [테마 시스템 이해하기](#테마-시스템-이해하기)
3. [Color Scheme](#color-scheme)
4. [Typography](#typography)
5. [Shape](#shape)
6. [다크 모드](#다크-모드)
7. [커스텀 테마 만들기](#커스텀-테마-만들기)
8. [실전 예제](#실전-예제)

---

## Material Design 3 소개

### Material Design 3 (Material You)란?

Google의 최신 디자인 시스템으로, **개인화**와 **적응형 디자인**에 초점을 맞춥니다.

#### 주요 특징

| 특징 | 설명 |
|------|------|
| **Dynamic Color** | 사용자의 배경화면에서 색상 추출 |
| **Adaptive Design** | 다양한 화면 크기에 자동 적응 |
| **Accessibility** | 접근성 향상 |
| **Expressive** | 더 풍부한 표현력 |

### Material Design 2 vs 3

```kotlin
// Material Design 2
import androidx.compose.material.*

// Material Design 3 (권장)
import androidx.compose.material3.*
```

> [!IMPORTANT]
> **새 프로젝트는 Material 3를 사용하세요**
> - Android Studio의 기본 템플릿
> - 최신 디자인 가이드라인
> - 더 나은 접근성

---

## 테마 시스템 이해하기

### Compose 테마의 3대 요소

```
Theme
├── ColorScheme    (색상 체계)
├── Typography     (타이포그래피)
└── Shapes         (도형 스타일)
```

### 기본 테마 구조

```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

### 테마 적용

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {  // 테마 적용
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 앱 컨텐츠
                }
            }
        }
    }
}
```

---

## Color Scheme

### Material 3 색상 역할

Material 3는 **역할 기반 색상 시스템**을 사용합니다.

#### 주요 색상 역할

| 색상 역할 | 용도 | 예시 |
|----------|------|------|
| `primary` | 주요 UI 요소 | 버튼, FAB |
| `onPrimary` | primary 위의 텍스트 | 버튼 텍스트 |
| `secondary` | 보조 UI 요소 | 칩, 토글 |
| `tertiary` | 강조 요소 | 필터, 입력 필드 |
| `background` | 화면 배경 | 스크린 배경 |
| `surface` | 카드, 시트 배경 | Card, Dialog |
| `error` | 에러 상태 | 에러 메시지 |

### Color.kt 파일

```kotlin
// ui/theme/Color.kt
package com.example.helloworld.ui.theme

import androidx.compose.ui.graphics.Color

// Light 테마 색상
val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)

val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)

val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)

val md_theme_light_error = Color(0xFFB3261E)
val md_theme_light_onError = Color(0xFFFFFFFF)

val md_theme_light_background = Color(0xFFFFFBFE)
val md_theme_light_onBackground = Color(0xFF1C1B1F)

val md_theme_light_surface = Color(0xFFFFFBFE)
val md_theme_light_onSurface = Color(0xFF1C1B1F)

// Dark 테마 색상
val md_theme_dark_primary = Color(0xFFD0BCFF)
val md_theme_dark_onPrimary = Color(0xFF381E72)
val md_theme_dark_primaryContainer = Color(0xFF4F378B)
val md_theme_dark_onPrimaryContainer = Color(0xFFEADDFF)

val md_theme_dark_secondary = Color(0xFFCCC2DC)
val md_theme_dark_onSecondary = Color(0xFF332D41)
val md_theme_dark_secondaryContainer = Color(0xFF4A4458)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8DEF8)

val md_theme_dark_tertiary = Color(0xFFEFB8C8)
val md_theme_dark_onTertiary = Color(0xFF492532)

val md_theme_dark_error = Color(0xFFF2B8B5)
val md_theme_dark_onError = Color(0xFF601410)

val md_theme_dark_background = Color(0xFF1C1B1F)
val md_theme_dark_onBackground = Color(0xFFE6E1E5)

val md_theme_dark_surface = Color(0xFF1C1B1F)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
```

### Theme.kt 파일

```kotlin
// ui/theme/Theme.kt
package com.example.helloworld.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

@Composable
fun HelloWorldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Android 12+ Dynamic Color
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

### 색상 사용하기

```kotlin
@Composable
fun ColorExample() {
    Column {
        // Primary 색상 사용
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Primary Button")
        }
        
        // Surface 색상 사용
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Card Content")
        }
        
        // Error 색상 사용
        Text(
            text = "에러 메시지",
            color = MaterialTheme.colorScheme.error
        )
    }
}
```

### 커스텀 색상 추가

```kotlin
// Color.kt에 추가
val CustomBlue = Color(0xFF2196F3)
val CustomGreen = Color(0xFF4CAF50)

// 사용
Text(
    text = "커스텀 색상",
    color = CustomBlue
)
```

---

## Typography

### Material 3 타이포그래피 스케일

Material 3는 **15개의 타이포그래피 스타일**을 제공합니다.

#### Display (큰 제목)

```kotlin
displayLarge    // 57sp
displayMedium   // 45sp
displaySmall    // 36sp
```

#### Headline (제목)

```kotlin
headlineLarge   // 32sp
headlineMedium  // 28sp
headlineSmall   // 24sp
```

#### Title (부제목)

```kotlin
titleLarge      // 22sp
titleMedium     // 16sp
titleSmall      // 14sp
```

#### Body (본문)

```kotlin
bodyLarge       // 16sp
bodyMedium      // 14sp
bodySmall       // 12sp
```

#### Label (레이블)

```kotlin
labelLarge      // 14sp
labelMedium     // 12sp
labelSmall      // 11sp
```

### Type.kt 파일

```kotlin
// ui/theme/Type.kt
package com.example.helloworld.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 커스텀 폰트 (선택사항)
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

val Typography = Typography(
    // Display
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    
    // Headline
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    
    // Title
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // Body
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    // Label
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
```

### 타이포그래피 사용하기

```kotlin
@Composable
fun TypographyExample() {
    Column {
        Text(
            text = "Display Large",
            style = MaterialTheme.typography.displayLarge
        )
        
        Text(
            text = "Headline Medium",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Title Large",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "Body Medium - 본문 텍스트입니다.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Label Small",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
```

### 커스텀 폰트 추가

#### 1. 폰트 파일 추가

```
res/
└── font/
    ├── pretendard_regular.ttf
    ├── pretendard_medium.ttf
    └── pretendard_bold.ttf
```

#### 2. FontFamily 정의

```kotlin
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)
```

#### 3. Typography에 적용

```kotlin
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Pretendard,  // 커스텀 폰트 적용
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)
```

---

## Shape

### Material 3 Shape 시스템

도형의 모서리 둥글기를 정의합니다.

```kotlin
// ui/theme/Shape.kt
package com.example.helloworld.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // 칩
    small = RoundedCornerShape(8.dp),        // 버튼
    medium = RoundedCornerShape(12.dp),      // 카드
    large = RoundedCornerShape(16.dp),       // FAB
    extraLarge = RoundedCornerShape(28.dp)   // 다이얼로그
)
```

### Shape 사용하기

```kotlin
@Composable
fun ShapeExample() {
    Column {
        // Small shape
        Button(
            onClick = {},
            shape = MaterialTheme.shapes.small
        ) {
            Text("Small Shape")
        }
        
        // Medium shape
        Card(
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Medium Shape Card")
        }
        
        // Custom shape
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        )
    }
}
```

---

## 다크 모드

### 시스템 설정 따르기

```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // 시스템 설정 자동 감지
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

### 수동으로 다크 모드 전환

```kotlin
@Composable
fun ThemeToggleExample() {
    var isDarkTheme by remember { mutableStateOf(false) }
    
    MyAppTheme(darkTheme = isDarkTheme) {
        Column {
            Text("현재 모드: ${if (isDarkTheme) "다크" else "라이트"}")
            
            Button(onClick = { isDarkTheme = !isDarkTheme }) {
                Text("테마 전환")
            }
        }
    }
}
```

### 다크 모드 색상 디자인 팁

```kotlin
// ✅ 좋은 예: 충분한 대비
val LightPrimary = Color(0xFF6750A4)  // 밝은 배경에 어두운 색
val DarkPrimary = Color(0xFFD0BCFF)   // 어두운 배경에 밝은 색

// ❌ 나쁜 예: 대비 부족
val LightPrimary = Color(0xFFE0E0E0)  // 밝은 배경에 밝은 색
val DarkPrimary = Color(0xFF303030)   // 어두운 배경에 어두운 색
```

### 상태 바 색상 변경

```kotlin
@Composable
fun HelloWorldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

---

## 커스텀 테마 만들기

### 1. 브랜드 색상 정의

```kotlin
// Color.kt
val BrandPrimary = Color(0xFF1976D2)      // 파란색
val BrandSecondary = Color(0xFFFF9800)    // 주황색
val BrandTertiary = Color(0xFF4CAF50)     // 초록색

val BrandPrimaryDark = Color(0xFF90CAF9)
val BrandSecondaryDark = Color(0xFFFFB74D)
val BrandTertiaryDark = Color(0xFF81C784)
```

### 2. ColorScheme 생성

```kotlin
// Theme.kt
private val BrandLightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFFE65100),
    
    tertiary = BrandTertiary,
    onTertiary = Color.White,
    
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1C1C),
    
    surface = Color.White,
    onSurface = Color(0xFF1C1C1C),
    
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val BrandDarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    
    secondary = BrandSecondaryDark,
    onSecondary = Color(0xFFE65100),
    secondaryContainer = Color(0xFFF57C00),
    onSecondaryContainer = Color(0xFFFFF3E0),
    
    tertiary = BrandTertiaryDark,
    onTertiary = Color(0xFF1B5E20),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    
    error = Color(0xFFEF5350),
    onError = Color(0xFFFFEBEE)
)
```

### 3. 커스텀 테마 함수

```kotlin
@Composable
fun BrandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        BrandDarkColorScheme
    } else {
        BrandLightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

---

## 실전 예제

### 예제 1: 완전한 테마 적용

```kotlin
@Composable
fun ThemedApp() {
    HelloWorldTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 제목
                Text(
                    text = "테마 예제",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // 카드
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "카드 제목",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "카드 내용입니다.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // 버튼
                Button(
                    onClick = {},
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Primary Button")
                }
                
                OutlinedButton(onClick = {}) {
                    Text("Outlined Button")
                }
            }
        }
    }
}
```

### 예제 2: 다크 모드 토글

```kotlin
@Composable
fun DarkModeToggleApp() {
    var isDarkMode by remember { mutableStateOf(false) }
    
    HelloWorldTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isDarkMode) {
                        Icons.Default.DarkMode
                    } else {
                        Icons.Default.LightMode
                    },
                    contentDescription = "테마 아이콘",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isDarkMode) "다크 모드" else "라이트 모드",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
            }
        }
    }
}
```

### 예제 3: 색상 팔레트 미리보기

```kotlin
@Composable
fun ColorPalettePreview() {
    HelloWorldTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { ColorItem("Primary", MaterialTheme.colorScheme.primary) }
            item { ColorItem("Secondary", MaterialTheme.colorScheme.secondary) }
            item { ColorItem("Tertiary", MaterialTheme.colorScheme.tertiary) }
            item { ColorItem("Background", MaterialTheme.colorScheme.background) }
            item { ColorItem("Surface", MaterialTheme.colorScheme.surface) }
            item { ColorItem("Error", MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun ColorItem(name: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = if (color.luminance() > 0.5f) Color.Black else Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

---

## 학습 체크리스트

### Material Design 3
- [ ] Material Design 3의 특징을 안다
- [ ] Material 2와 3의 차이를 이해한다
- [ ] 역할 기반 색상 시스템을 이해한다

### Color Scheme
- [ ] lightColorScheme()를 만들 수 있다
- [ ] darkColorScheme()를 만들 수 있다
- [ ] 색상 역할(primary, secondary 등)을 안다
- [ ] MaterialTheme.colorScheme을 사용할 수 있다

### Typography
- [ ] Typography 스케일을 안다
- [ ] 커스텀 폰트를 추가할 수 있다
- [ ] MaterialTheme.typography를 사용할 수 있다

### Shape
- [ ] Shapes를 정의할 수 있다
- [ ] MaterialTheme.shapes를 사용할 수 있다
- [ ] 커스텀 Shape를 만들 수 있다

### 다크 모드
- [ ] 다크 모드를 구현할 수 있다
- [ ] 시스템 설정을 따를 수 있다
- [ ] 수동으로 테마를 전환할 수 있다

---

## 다음 단계

### 추천 학습 순서

1. ✅ Kotlin 기초
2. ✅ Android 프로젝트 구조
3. ✅ Layout & UI
4. ✅ State 관리
5. ✅ Navigation
6. ✅ 테마와 스타일링 (완료)
7. ➡️ 리스트와 그리드
8. ➡️ 폼 입력과 유효성 검사

---

## 참고 자료

### 공식 문서
- [Material Design 3](https://m3.material.io/)
- [Compose Theming](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Color System](https://m3.material.io/styles/color/overview)

### 도구
- [Material Theme Builder](https://m3.material.io/theme-builder) - 테마 생성 도구
- [Color Tool](https://material.io/resources/color/) - 색상 팔레트 생성

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

**학습 예상 시간**: 2-3시간  
**난이도**: ⭐⭐⭐

일관된 테마를 적용하면 앱이 훨씬 전문적으로 보입니다! 🎨
