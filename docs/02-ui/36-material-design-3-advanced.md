# Material Design 3 심화 가이드

## 📚 목차

1. [Material Design 3란?](#material-design-3란)
2. [Dynamic Color](#dynamic-color)
3. [커스텀 테마](#커스텀-테마)
4. [다크 모드](#다크-모드)
5. [컴포넌트 심화](#컴포넌트-심화)
6. [애니메이션](#애니메이션)
7. [접근성](#접근성)
8. [실전 예제](#실전-예제)

---

## Material Design 3란?

> [!NOTE]
> **Material Design 3 (Material You) = Google의 최신 디자인 시스템**
> 
> **주요 특징:**
> - 🎨 Dynamic Color (동적 색상)
> - 🌓 개선된 다크 모드
> - 📐 새로운 컴포넌트
> - ✨ 부드러운 애니메이션
> - ♿ 향상된 접근성

### Material 2 vs Material 3

**Material 2:**
```
- 고정된 색상 팔레트
- 기본 다크 모드
- 각진 모서리
```

**Material 3:**
```
- 동적 색상 (배경화면 기반)
- 개선된 다크 모드
- 둥근 모서리
- 더 부드러운 애니메이션
```

**시각적 차이:**
```
Material 2:
┌─────────────┐
│   Button    │  ← 각진 모서리
└─────────────┘

Material 3:
╭─────────────╮
│   Button    │  ← 둥근 모서리
╰─────────────╯
```

### 의존성 설정

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // Material 3
    implementation("androidx.compose.material3:material3:1.2.0")
    
    // Material 3 Window Size Class
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    
    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}
```

---

## Dynamic Color

> [!IMPORTANT]
> **Dynamic Color = 배경화면에서 추출한 색상으로 앱 테마 생성**
> 
> **장점:**
> - 🎨 개인화된 색상
> - 🔄 자동 조화
> - 📱 시스템 통합

### Dynamic Color 적용

```kotlin
import androidx.compose.material3.*
import android.os.Build

@Composable
fun MyApp() {
    // Dynamic Color 지원 여부 확인 (Android 12+)
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    
    val colorScheme = when {
        // Dynamic Color 사용 (Android 12+)
        dynamicColor -> {
            val context = LocalContext.current
            if (isSystemInDarkTheme()) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // 기본 색상 사용
        isSystemInDarkTheme() -> darkColorScheme()
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**동작 원리:**
```
1. 사용자의 배경화면 분석
   ↓
2. 주요 색상 추출
   ↓
3. 조화로운 색상 팔레트 생성
   ↓
4. 앱에 자동 적용
```

### Dynamic Color 테스트

```kotlin
@Composable
fun DynamicColorDemo() {
    val colorScheme = MaterialTheme.colorScheme
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Dynamic Color 팔레트",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        // Primary
        item {
            ColorSwatch(
                name = "Primary",
                color = colorScheme.primary,
                onColor = colorScheme.onPrimary
            )
        }
        
        // Secondary
        item {
            ColorSwatch(
                name = "Secondary",
                color = colorScheme.secondary,
                onColor = colorScheme.onSecondary
            )
        }
        
        // Tertiary
        item {
            ColorSwatch(
                name = "Tertiary",
                color = colorScheme.tertiary,
                onColor = colorScheme.onTertiary
            )
        }
        
        // Surface
        item {
            ColorSwatch(
                name = "Surface",
                color = colorScheme.surface,
                onColor = colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ColorSwatch(
    name: String,
    color: Color,
    onColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = name,
                    color = onColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "#${color.toArgb().toUInt().toString(16).uppercase()}",
                    color = onColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

---

## 커스텀 테마

### 색상 팔레트 생성

```kotlin
// Material Theme Builder 사용
// https://m3.material.io/theme-builder

// 생성된 색상 팔레트
private val md_theme_light_primary = Color(0xFF6750A4)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFEADDFF)
private val md_theme_light_onPrimaryContainer = Color(0xFF21005D)
private val md_theme_light_secondary = Color(0xFF625B71)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
private val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)
private val md_theme_light_tertiary = Color(0xFF7D5260)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
private val md_theme_light_onTertiaryContainer = Color(0xFF31111D)
private val md_theme_light_error = Color(0xFFB3261E)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_errorContainer = Color(0xFFF9DEDC)
private val md_theme_light_onErrorContainer = Color(0xFF410E0B)
private val md_theme_light_background = Color(0xFFFFFBFE)
private val md_theme_light_onBackground = Color(0xFF1C1B1F)
private val md_theme_light_surface = Color(0xFFFFFBFE)
private val md_theme_light_onSurface = Color(0xFF1C1B1F)
private val md_theme_light_surfaceVariant = Color(0xFFE7E0EC)
private val md_theme_light_onSurfaceVariant = Color(0xFF49454F)
private val md_theme_light_outline = Color(0xFF79747E)

// 다크 모드 색상
private val md_theme_dark_primary = Color(0xFFD0BCFF)
private val md_theme_dark_onPrimary = Color(0xFF381E72)
private val md_theme_dark_primaryContainer = Color(0xFF4F378B)
private val md_theme_dark_onPrimaryContainer = Color(0xFFEADDFF)
// ... (나머지 다크 모드 색상)

// ColorScheme 생성
val LightColorScheme = lightColorScheme(
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
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    // ... (나머지)
)
```

### 타이포그래피 커스터마이징

```kotlin
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// 커스텀 폰트
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

// 타이포그래피 정의
val Typography = Typography(
    // Display (가장 큰 텍스트)
    displayLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline
    headlineLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label
    labelLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Shapes 커스터마이징

```kotlin
val Shapes = Shapes(
    // Extra Small (4dp)
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small (8dp)
    small = RoundedCornerShape(8.dp),
    
    // Medium (12dp)
    medium = RoundedCornerShape(12.dp),
    
    // Large (16dp)
    large = RoundedCornerShape(16.dp),
    
    // Extra Large (28dp)
    extraLarge = RoundedCornerShape(28.dp)
)

// 커스텀 Shape
val CustomShapes = Shapes(
    small = CutCornerShape(topStart = 8.dp),  // 잘린 모서리
    medium = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    ),  // 상단만 둥글게
    large = CircleShape  // 원형
)
```

### 테마 적용

```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

---

## 다크 모드

### 다크 모드 전환

```kotlin
@Composable
fun ThemeSettingsScreen() {
    // 테마 설정 저장
    val context = LocalContext.current
    val dataStore = remember { context.dataStore }
    
    val isDarkMode by dataStore.data
        .map { it[PreferencesKeys.DARK_MODE] ?: false }
        .collectAsState(initial = false)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "테마 설정",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 다크 모드 토글
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    lifecycleScope.launch {
                        dataStore.edit { preferences ->
                            preferences[PreferencesKeys.DARK_MODE] = !isDarkMode
                        }
                    }
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "다크 모드",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    if (isDarkMode) "켜짐" else "꺼짐",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isDarkMode,
                onCheckedChange = { enabled ->
                    lifecycleScope.launch {
                        dataStore.edit { preferences ->
                            preferences[PreferencesKeys.DARK_MODE] = enabled
                        }
                    }
                }
            )
        }
    }
}

// DataStore 설정
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferencesKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
}
```

### 다크 모드 최적화

```kotlin
@Composable
fun DarkModeOptimizedScreen() {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        // Surface는 자동으로 다크 모드 색상 적용
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 텍스트 색상 자동 조정
            Text(
                "제목",
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                // 다크 모드에서 적절한 elevation
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "카드 내용",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

---

## 컴포넌트 심화

### 고급 Button

```kotlin
@Composable
fun AdvancedButtons() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filled Button (기본)
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Filled Button")
        }
        
        // Filled Tonal Button
        FilledTonalButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Filled Tonal Button")
        }
        
        // Outlined Button
        OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Outlined Button")
        }
        
        // Text Button
        TextButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Text Button")
        }
        
        // Elevated Button
        ElevatedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elevated Button")
        }
        
        // 커스텀 색상 Button
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("Custom Color Button")
        }
    }
}
```

### 고급 Card

```kotlin
@Composable
fun AdvancedCards() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filled Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Filled Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "기본 카드 스타일",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Elevated Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Elevated Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "그림자가 있는 카드",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Outlined Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Outlined Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "테두리가 있는 카드",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 클릭 가능한 Card
        Card(
            onClick = { /* 클릭 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Clickable Card",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "탭하여 상세 보기",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
```

### Navigation Bar & Rail

```kotlin
@Composable
fun NavigationExample() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("홈", "검색", "프로필")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Search,
        Icons.Default.Person
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> SearchScreen()
                2 -> ProfileScreen()
            }
        }
    }
}
```

---

## 애니메이션

### Material Motion

```kotlin
@Composable
fun MaterialMotionExample() {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Animated Visibility
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "애니메이션 콘텐츠",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Animated Button
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (expanded) "숨기기" else "보이기")
        }
    }
}
```

---

## 접근성

### 접근성 개선

```kotlin
@Composable
fun AccessibleComponents() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 의미 있는 contentDescription
        Icon(
            Icons.Default.Favorite,
            contentDescription = "좋아요",  // 스크린 리더가 읽음
            modifier = Modifier.size(48.dp)
        )
        
        // 최소 터치 영역 (48dp)
        IconButton(
            onClick = { },
            modifier = Modifier.size(48.dp)  // 최소 크기
        ) {
            Icon(Icons.Default.Delete, "삭제")
        }
        
        // 충분한 색상 대비
        Text(
            "읽기 쉬운 텍스트",
            color = MaterialTheme.colorScheme.onBackground  // 자동 대비
        )
    }
}
```

---

## 실전 예제

### 완전한 Material 3 앱

```kotlin
@Composable
fun CompleteMaterial3App() {
    var darkMode by remember { mutableStateOf(false) }
    
    MyAppTheme(darkTheme = darkMode) {
        var selectedTab by remember { mutableStateOf(0) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Material 3 App") },
                    actions = {
                        IconButton(onClick = { darkMode = !darkMode }) {
                            Icon(
                                if (darkMode) Icons.Default.LightMode
                                else Icons.Default.DarkMode,
                                "테마 전환"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("홈") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("좋아요") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("추가") }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(10) { index ->
                    ElevatedCard(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Article,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "항목 ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "설명 텍스트",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Design with Material! 🎨
