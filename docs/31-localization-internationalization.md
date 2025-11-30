# 다국어 지원 및 현지화 완벽 가이드

## 📚 목차

1. [다국어 지원이란?](#다국어-지원이란)
2. [strings.xml 다국어화](#stringsxml-다국어화)
3. [Compose에서 다국어](#compose에서-다국어)
4. [Locale 처리](#locale-처리)
5. [RTL 지원](#rtl-지원)
6. [날짜/시간/통화 포맷](#날짜시간통화-포맷)
7. [이미지 및 리소스](#이미지-및-리소스)
8. [실전 예제](#실전-예제)

---

## 다국어 지원이란?

> [!NOTE]
> **다국어 지원 (Localization) = 앱을 여러 언어로 제공하는 것**
> 
> **주요 요소:**
> - 🌍 텍스트 번역
> - 📅 날짜/시간 형식
> - 💰 통화 형식
> - ↔️ RTL (오른쪽에서 왼쪽) 지원
> - 🖼️ 지역별 이미지

### 왜 중요한가?

**글로벌 시장:**
```
영어만 지원: 15억 명 (20%)
다국어 지원: 75억 명 (100%)
→ 5배 더 많은 사용자!
```

**통계:**
- 다국어 지원 앱의 다운로드: **128% 증가**
- 사용자의 **72%**가 모국어 앱 선호
- 다국어 지원 앱의 수익: **26% 증가**

---

## strings.xml 다국어화

### 기본 구조

**프로젝트 구조:**
```
app/src/main/res/
├── values/
│   └── strings.xml          ← 기본 (영어)
├── values-ko/
│   └── strings.xml          ← 한국어
├── values-ja/
│   └── strings.xml          ← 일본어
├── values-zh/
│   └── strings.xml          ← 중국어
└── values-es/
    └── strings.xml          ← 스페인어
```

### 기본 strings.xml

```xml
<!-- res/values/strings.xml (기본 - 영어) -->
<resources>
    <!-- 앱 이름 -->
    <string name="app_name">My App</string>
    
    <!-- 환영 메시지 -->
    <string name="welcome_message">Welcome!</string>
    <string name="welcome_description">Thank you for using our app</string>
    
    <!-- 버튼 -->
    <string name="button_login">Login</string>
    <string name="button_signup">Sign Up</string>
    <string name="button_cancel">Cancel</string>
    <string name="button_save">Save</string>
    
    <!-- 폼 -->
    <string name="label_email">Email</string>
    <string name="label_password">Password</string>
    <string name="hint_email">Enter your email</string>
    <string name="hint_password">Enter your password</string>
    
    <!-- 에러 메시지 -->
    <string name="error_email_invalid">Invalid email address</string>
    <string name="error_password_short">Password must be at least 6 characters</string>
    
    <!-- 파라미터가 있는 문자열 -->
    <string name="greeting">Hello, %1$s!</string>
    <string name="items_count">You have %1$d items</string>
    <string name="price_format">Price: %1$s</string>
</resources>
```

### 한국어 strings.xml

```xml
<!-- res/values-ko/strings.xml (한국어) -->
<resources>
    <string name="app_name">내 앱</string>
    
    <string name="welcome_message">환영합니다!</string>
    <string name="welcome_description">앱을 사용해 주셔서 감사합니다</string>
    
    <string name="button_login">로그인</string>
    <string name="button_signup">회원가입</string>
    <string name="button_cancel">취소</string>
    <string name="button_save">저장</string>
    
    <string name="label_email">이메일</string>
    <string name="label_password">비밀번호</string>
    <string name="hint_email">이메일을 입력하세요</string>
    <string name="hint_password">비밀번호를 입력하세요</string>
    
    <string name="error_email_invalid">유효하지 않은 이메일 주소입니다</string>
    <string name="error_password_short">비밀번호는 최소 6자 이상이어야 합니다</string>
    
    <string name="greeting">안녕하세요, %1$s님!</string>
    <string name="items_count">%1$d개의 항목이 있습니다</string>
    <string name="price_format">가격: %1$s</string>
</resources>
```

### 복수형 처리

```xml
<!-- res/values/strings.xml -->
<resources>
    <!-- 복수형 (영어) -->
    <plurals name="number_of_messages">
        <item quantity="zero">No messages</item>
        <item quantity="one">1 message</item>
        <item quantity="other">%d messages</item>
    </plurals>
</resources>

<!-- res/values-ko/strings.xml -->
<resources>
    <!-- 복수형 (한국어는 단수/복수 구분 없음) -->
    <plurals name="number_of_messages">
        <item quantity="other">메시지 %d개</item>
    </plurals>
</resources>
```

**사용:**
```kotlin
// Compose에서 복수형 사용
val messageCount = 5
val text = pluralStringResource(
    R.plurals.number_of_messages,
    messageCount,
    messageCount
)
// 영어: "5 messages"
// 한국어: "메시지 5개"
```

---

## Compose에서 다국어

### 기본 사용

```kotlin
@Composable
fun WelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // strings.xml에서 문자열 가져오기
        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = stringResource(R.string.welcome_description),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* 로그인 */ }
        ) {
            Text(stringResource(R.string.button_login))
        }
    }
}
```

### 파라미터가 있는 문자열

```kotlin
@Composable
fun GreetingScreen(userName: String) {
    // 파라미터 전달
    Text(
        text = stringResource(R.string.greeting, userName)
    )
    // 영어: "Hello, John!"
    // 한국어: "안녕하세요, John님!"
}

@Composable
fun ItemCountScreen(count: Int) {
    Text(
        text = stringResource(R.string.items_count, count)
    )
    // 영어: "You have 5 items"
    // 한국어: "5개의 항목이 있습니다"
}
```

### 문자열 배열

```xml
<!-- res/values/strings.xml -->
<resources>
    <string-array name="days_of_week">
        <item>Monday</item>
        <item>Tuesday</item>
        <item>Wednesday</item>
        <item>Thursday</item>
        <item>Friday</item>
        <item>Saturday</item>
        <item>Sunday</item>
    </string-array>
</resources>

<!-- res/values-ko/strings.xml -->
<resources>
    <string-array name="days_of_week">
        <item>월요일</item>
        <item>화요일</item>
        <item>수요일</item>
        <item>목요일</item>
        <item>금요일</item>
        <item>토요일</item>
        <item>일요일</item>
    </string-array>
</resources>
```

**사용:**
```kotlin
@Composable
fun DaySelector() {
    val days = stringArrayResource(R.array.days_of_week)
    
    LazyColumn {
        items(days) { day ->
            Text(day)
        }
    }
}
```

---

## Locale 처리

### 현재 언어 가져오기

```kotlin
@Composable
fun CurrentLanguageDisplay() {
    // 현재 Locale 가져오기
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    
    Column {
        Text("언어 코드: ${locale.language}")  // "ko", "en", "ja" 등
        Text("국가 코드: ${locale.country}")   // "KR", "US", "JP" 등
        Text("표시 이름: ${locale.displayName}")  // "한국어", "English" 등
    }
}
```

### 언어 변경

```kotlin
class LocaleHelper {
    companion object {
        // 언어 변경
        fun setLocale(context: Context, languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            
            context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
        
        // 저장된 언어 가져오기
        fun getSavedLanguage(context: Context): String {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            return prefs.getString("language", "en") ?: "en"
        }
        
        // 언어 저장
        fun saveLanguage(context: Context, languageCode: String) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putString("language", languageCode).apply()
        }
    }
}

// 사용
@Composable
fun LanguageSelector() {
    val context = LocalContext.current
    var selectedLanguage by remember { 
        mutableStateOf(LocaleHelper.getSavedLanguage(context)) 
    }
    
    Column {
        Text("언어 선택", style = MaterialTheme.typography.titleMedium)
        
        // 언어 옵션
        LanguageOption(
            language = "en",
            displayName = "English",
            isSelected = selectedLanguage == "en",
            onClick = {
                selectedLanguage = "en"
                LocaleHelper.setLocale(context, "en")
                LocaleHelper.saveLanguage(context, "en")
                
                // 액티비티 재시작 (언어 적용)
                (context as? Activity)?.recreate()
            }
        )
        
        LanguageOption(
            language = "ko",
            displayName = "한국어",
            isSelected = selectedLanguage == "ko",
            onClick = {
                selectedLanguage = "ko"
                LocaleHelper.setLocale(context, "ko")
                LocaleHelper.saveLanguage(context, "ko")
                (context as? Activity)?.recreate()
            }
        )
        
        LanguageOption(
            language = "ja",
            displayName = "日本語",
            isSelected = selectedLanguage == "ja",
            onClick = {
                selectedLanguage = "ja"
                LocaleHelper.setLocale(context, "ja")
                LocaleHelper.saveLanguage(context, "ja")
                (context as? Activity)?.recreate()
            }
        )
    }
}

@Composable
fun LanguageOption(
    language: String,
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(displayName)
    }
}
```

---

## RTL 지원

> [!NOTE]
> **RTL (Right-to-Left) = 오른쪽에서 왼쪽으로 쓰는 언어**
> 
> **RTL 언어:**
> - 아랍어 (ar)
> - 히브리어 (he)
> - 페르시아어 (fa)
> - 우르두어 (ur)

### RTL 활성화

```xml
<!-- AndroidManifest.xml -->
<application
    android:supportsRtl="true">
    <!-- ... -->
</application>
```

### Compose에서 RTL 처리

```kotlin
@Composable
fun RtlAwareLayout() {
    // 현재 레이아웃 방향
    val layoutDirection = LocalLayoutDirection.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start  // RTL에서 자동으로 End로 변경
    ) {
        // 아이콘 (항상 왼쪽)
        Icon(Icons.Filled.Person, null)
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 텍스트
        Text("사용자 이름")
    }
}

// 절대 위치 (RTL 무시)
@Composable
fun AbsolutePositioning() {
    Box(modifier = Modifier.fillMaxSize()) {
        // RTL에서도 항상 왼쪽
        Text(
            "왼쪽",
            modifier = Modifier.align(AbsoluteAlignment.TopLeft)
        )
        
        // RTL에서도 항상 오른쪽
        Text(
            "오른쪽",
            modifier = Modifier.align(AbsoluteAlignment.TopRight)
        )
    }
}
```

### 미러링 이미지

```xml
<!-- res/values/bools.xml -->
<resources>
    <bool name="is_rtl">false</bool>
</resources>

<!-- res/values-ar/bools.xml (아랍어) -->
<resources>
    <bool name="is_rtl">true</bool>
</resources>
```

```kotlin
@Composable
fun MirroredImage() {
    val isRtl = booleanResource(R.bool.is_rtl)
    
    Image(
        painter = painterResource(R.drawable.arrow_forward),
        contentDescription = null,
        modifier = Modifier.scale(
            scaleX = if (isRtl) -1f else 1f,  // RTL에서 좌우 반전
            scaleY = 1f
        )
    )
}
```

---

## 날짜/시간/통화 포맷

### 날짜 포맷

```kotlin
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateDisplay() {
    val currentDate = Date()
    
    // 현재 Locale에 맞는 날짜 형식
    val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
    val formattedDate = dateFormat.format(currentDate)
    
    Column {
        Text("날짜: $formattedDate")
        // 영어: "December 1, 2025"
        // 한국어: "2025년 12월 1일"
        // 일본어: "2025年12月1日"
    }
}

// 커스텀 날짜 포맷
@Composable
fun CustomDateFormat() {
    val currentDate = Date()
    val locale = Locale.getDefault()
    
    // 짧은 형식
    val shortFormat = SimpleDateFormat("yyyy-MM-dd", locale)
    
    // 긴 형식
    val longFormat = SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREAN)
    
    Column {
        Text("짧은 형식: ${shortFormat.format(currentDate)}")
        Text("긴 형식: ${longFormat.format(currentDate)}")
    }
}
```

### 시간 포맷

```kotlin
@Composable
fun TimeDisplay() {
    val currentTime = Date()
    
    // 현재 Locale에 맞는 시간 형식
    val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    val formattedTime = timeFormat.format(currentTime)
    
    Text("시간: $formattedTime")
    // 영어 (12시간): "2:30 PM"
    // 한국어 (24시간): "오후 2:30"
}
```

### 통화 포맷

```kotlin
import java.text.NumberFormat
import java.util.Currency

@Composable
fun PriceDisplay(amount: Double) {
    val locale = Locale.getDefault()
    
    // 현재 Locale에 맞는 통화 형식
    val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    val formattedPrice = currencyFormat.format(amount)
    
    Text("가격: $formattedPrice")
    // 한국: "₩10,000"
    // 미국: "$10.00"
    // 일본: "¥1,000"
}

// 특정 통화 지정
@Composable
fun SpecificCurrencyDisplay(amount: Double, currencyCode: String) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    currencyFormat.currency = Currency.getInstance(currencyCode)
    
    val formattedPrice = currencyFormat.format(amount)
    
    Text("가격: $formattedPrice")
}

// 사용
SpecificCurrencyDisplay(10000.0, "USD")  // "$10,000.00"
SpecificCurrencyDisplay(10000.0, "KRW")  // "₩10,000"
SpecificCurrencyDisplay(10000.0, "JPY")  // "¥10,000"
```

### 숫자 포맷

```kotlin
@Composable
fun NumberDisplay(number: Double) {
    val locale = Locale.getDefault()
    val numberFormat = NumberFormat.getNumberInstance(locale)
    
    Text("숫자: ${numberFormat.format(number)}")
    // 영어: "1,234,567.89"
    // 한국어: "1,234,567.89"
    // 독일어: "1.234.567,89" (쉼표와 점이 반대!)
}
```

---

## 이미지 및 리소스

### 지역별 이미지

**프로젝트 구조:**
```
res/
├── drawable/
│   └── banner.png           ← 기본 이미지
├── drawable-ko/
│   └── banner.png           ← 한국어 이미지
├── drawable-ja/
│   └── banner.png           ← 일본어 이미지
└── drawable-ar/
    └── banner.png           ← 아랍어 이미지
```

```kotlin
@Composable
fun LocalizedBanner() {
    // 현재 Locale에 맞는 이미지 자동 선택
    Image(
        painter = painterResource(R.drawable.banner),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 지역별 색상

```xml
<!-- res/values/colors.xml -->
<resources>
    <color name="primary_color">#6200EE</color>
</resources>

<!-- res/values-zh/colors.xml (중국) -->
<resources>
    <color name="primary_color">#FF0000</color>  <!-- 빨간색 선호 -->
</resources>
```

---

## 실전 예제

### 완전한 다국어 앱

```kotlin
// Application 클래스
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 저장된 언어 적용
        val savedLanguage = LocaleHelper.getSavedLanguage(this)
        LocaleHelper.setLocale(this, savedLanguage)
    }
}

// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 저장된 언어 적용
        val savedLanguage = LocaleHelper.getSavedLanguage(this)
        LocaleHelper.setLocale(this, savedLanguage)
        
        setContent {
            MyApp()
        }
    }
}

// 메인 앱
@Composable
fun MyApp() {
    var currentScreen by remember { mutableStateOf("home") }
    
    when (currentScreen) {
        "home" -> HomeScreen(
            onSettingsClick = { currentScreen = "settings" }
        )
        "settings" -> SettingsScreen(
            onBackClick = { currentScreen = "home" }
        )
    }
}

// 홈 화면
@Composable
fun HomeScreen(onSettingsClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, "설정")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 환영 메시지
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.welcome_description),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 현재 날짜/시간
            val currentDate = Date()
            val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
            val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dateFormat.format(currentDate),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = timeFormat.format(currentDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 가격 표시
            val price = 10000.0
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.price_format, currencyFormat.format(price)),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// 설정 화면
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LanguageSelector()
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 모든 텍스트를 strings.xml에

```kotlin
// ❌ 하드코딩
Text("안녕하세요")

// ✅ strings.xml 사용
Text(stringResource(R.string.greeting))
```

### 2. 번역 품질 확인

```
- 전문 번역가 사용
- 네이티브 스피커 검토
- 문화적 차이 고려
```

### 3. 테스트

```kotlin
// 다양한 언어로 테스트
// 긴 텍스트 테스트 (독일어는 영어보다 30% 길음)
// RTL 레이아웃 테스트
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Go Global! 🌍
