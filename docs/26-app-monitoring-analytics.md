# 앱 모니터링 및 분석 완벽 가이드

## 📚 목차

1. [모니터링이란?](#모니터링이란)
2. [Firebase Analytics 심화](#firebase-analytics-심화)
3. [Firebase Crashlytics 심화](#firebase-crashlytics-심화)
4. [Performance Monitoring](#performance-monitoring)
5. [사용자 행동 분석](#사용자-행동-분석)
6. [A/B 테스팅](#ab-테스팅)
7. [대시보드 구축](#대시보드-구축)
8. [실전 분석 사례](#실전-분석-사례)

---

## 모니터링이란?

> [!IMPORTANT]
> **모니터링 = 앱의 건강 상태를 지속적으로 확인하는 것**
> 
> **왜 필요한가?**
> - 🐛 버그를 빨리 발견
> - 📊 사용자가 무엇을 하는지 이해
> - 🚀 앱 성능 개선
> - 💰 비즈니스 의사결정

### 모니터링 없이 개발하면?

**문제 발생 시나리오:**
```
사용자: 앱이 자꾸 꺼져요!
개발자: 어디서요? 언제요? 무엇을 하셨나요?
사용자: 글쎄요... 기억이 안나요.
개발자: ...😰

→ 문제를 재현할 수 없음
→ 버그 수정 불가능
→ 사용자 이탈
```

**모니터링 사용 시:**
```
Crashlytics 알림: 크래시 발생!
- 영향받은 사용자: 50명
- 발생 위치: PaymentScreen.kt:45
- 에러: NetworkTimeoutException
- 재현율: 100%

→ 정확한 원인 파악
→ 즉시 수정
→ 사용자 만족
```

### 모니터링의 3가지 축

```
┌─────────────────────────────────────┐
│ 1. 안정성 (Stability)               │
│    - 크래시율                       │
│    - ANR (앱 응답 없음)             │
│    - 에러율                         │
├─────────────────────────────────────┤
│ 2. 성능 (Performance)               │
│    - 앱 시작 시간                   │
│    - 화면 렌더링 시간               │
│    - 네트워크 응답 시간             │
├─────────────────────────────────────┤
│ 3. 사용성 (Engagement)              │
│    - 활성 사용자 수                 │
│    - 세션 시간                      │
│    - 전환율                         │
└─────────────────────────────────────┘
```

**목표 지표:**
| 지표 | 목표 | 우수 |
|------|------|------|
| **크래시 없는 사용자** | > 99% | > 99.5% |
| **앱 시작 시간** | < 2초 | < 1초 |
| **일일 활성 사용자** | 증가 추세 | 월 10% 증가 |

---

## Firebase Analytics 심화

> [!NOTE]
> **Analytics는 사용자가 무엇을 하는지 알려줍니다**
> 
> **무료로 제공:**
> - 무제한 이벤트
> - 자동 수집
> - BigQuery 연동

### 이벤트 설계 전략

**나쁜 이벤트 설계:**
```kotlin
// ❌ 너무 일반적
analytics.logEvent("button_click", null)
// 어떤 버튼? 어디서? 왜?

// ❌ 너무 구체적
analytics.logEvent("home_screen_premium_button_clicked_at_10am", null)
// 분석 불가능
```

**좋은 이벤트 설계:**
```kotlin
// ✅ 명확하고 분석 가능
analytics.logEvent("button_click") {
    param("screen_name", "home")
    param("button_name", "premium_upgrade")
    param("button_position", "top_banner")
}

// ✅ 표준 이벤트 사용 (권장)
analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
    param(FirebaseAnalytics.Param.CONTENT_TYPE, "product")
    param(FirebaseAnalytics.Param.ITEM_ID, "premium_plan")
}
```

### 이벤트 명명 규칙

**일관된 명명:**
```kotlin
// 동사_명사 패턴
"view_product"
"click_button"
"complete_purchase"
"share_content"

// 파라미터는 snake_case
param("screen_name", "home")
param("item_id", "123")
param("user_type", "premium")
```

### 사용자 여정 추적

**전체 구매 퍼널 추적:**
```kotlin
class PurchaseFunnelTracker(
    private val analytics: FirebaseAnalytics
) {
    // 1단계: 상품 조회
    fun trackProductView(productId: String, productName: String, price: Double) {
        analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, productId)
            param(FirebaseAnalytics.Param.ITEM_NAME, productName)
            param(FirebaseAnalytics.Param.PRICE, price)
        }
    }
    
    // 2단계: 장바구니 추가
    fun trackAddToCart(productId: String, productName: String, price: Double) {
        analytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART) {
            param(FirebaseAnalytics.Param.ITEM_ID, productId)
            param(FirebaseAnalytics.Param.ITEM_NAME, productName)
            param(FirebaseAnalytics.Param.PRICE, price)
        }
    }
    
    // 3단계: 결제 시작
    fun trackBeginCheckout(totalPrice: Double, itemCount: Int) {
        analytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT) {
            param(FirebaseAnalytics.Param.VALUE, totalPrice)
            param(FirebaseAnalytics.Param.CURRENCY, "KRW")
            param("item_count", itemCount.toLong())
        }
    }
    
    // 4단계: 결제 완료
    fun trackPurchase(
        transactionId: String,
        totalPrice: Double,
        items: List<PurchaseItem>
    ) {
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.TRANSACTION_ID, transactionId)
            param(FirebaseAnalytics.Param.VALUE, totalPrice)
            param(FirebaseAnalytics.Param.CURRENCY, "KRW")
            param(FirebaseAnalytics.Param.ITEMS, items.toBundle())
        }
    }
}

// 사용
@Composable
fun ProductDetailScreen(
    product: Product,
    tracker: PurchaseFunnelTracker
) {
    LaunchedEffect(product.id) {
        tracker.trackProductView(
            productId = product.id,
            productName = product.name,
            price = product.price
        )
    }
    
    Button(onClick = {
        tracker.trackAddToCart(
            productId = product.id,
            productName = product.name,
            price = product.price
        )
    }) {
        Text("장바구니 담기")
    }
}
```

**Firebase 콘솔에서 보이는 것:**
```
구매 퍼널 분석:
1. 상품 조회: 1000명 (100%)
2. 장바구니: 300명 (30%)  ← 70% 이탈!
3. 결제 시작: 200명 (20%)  ← 10% 이탈
4. 결제 완료: 150명 (15%)  ← 5% 이탈

→ 장바구니에서 가장 많이 이탈
→ 장바구니 UX 개선 필요!
```

### 사용자 속성 활용

```kotlin
class UserPropertiesManager(
    private val analytics: FirebaseAnalytics
) {
    fun setUserProperties(user: User) {
        // 사용자 타입
        analytics.setUserProperty("user_type", 
            if (user.isPremium) "premium" else "free"
        )
        
        // 가입 기간
        val daysSinceSignup = calculateDaysSince(user.signupDate)
        analytics.setUserProperty("user_age_days", 
            when {
                daysSinceSignup < 7 -> "new"
                daysSinceSignup < 30 -> "active"
                else -> "veteran"
            }
        )
        
        // 선호 카테고리
        analytics.setUserProperty("favorite_category", 
            user.mostViewedCategory
        )
        
        // 구매 횟수
        analytics.setUserProperty("purchase_count",
            when (user.purchaseCount) {
                0 -> "none"
                in 1..3 -> "low"
                in 4..10 -> "medium"
                else -> "high"
            }
        )
    }
}

// 분석 예시:
// "premium 사용자는 어떤 기능을 많이 쓰는가?"
// "new 사용자의 이탈률은?"
// "purchase_count가 high인 사용자의 특징은?"
```

### 화면 추적

```kotlin
// 자동 화면 추적 (권장)
@Composable
fun MyScreen() {
    val analytics = Firebase.analytics
    
    DisposableEffect(Unit) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "MyScreen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MyScreen")
        }
        
        onDispose { }
    }
    
    // UI 코드...
}

// 또는 Navigation Compose와 통합
NavHost(navController, startDestination = "home") {
    composable("home") {
        // 화면 진입 시 자동 로깅
        TrackScreen("home")
        HomeScreen()
    }
}

@Composable
fun TrackScreen(screenName: String) {
    val analytics = Firebase.analytics
    
    DisposableEffect(screenName) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        onDispose { }
    }
}
```

---

## Firebase Crashlytics 심화

> [!WARNING]
> **크래시는 사용자 이탈의 주요 원인입니다**
> 
> **통계:**
> - 크래시 1회 경험 → 이탈률 **23% 증가**
> - 크래시 2회 이상 → 이탈률 **71% 증가**
> - 크래시 없는 앱 → 평점 **0.5점 높음**

### 커스텀 로그 전략

```kotlin
class CrashlyticsLogger {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    // 사용자 식별
    fun setUser(userId: String, email: String) {
        crashlytics.setUserId(userId)
        crashlytics.setCustomKey("user_email", email)
    }
    
    // 앱 상태 기록
    fun logAppState(state: AppState) {
        crashlytics.setCustomKey("app_state", state.name)
        crashlytics.setCustomKey("is_premium", state.isPremium)
        crashlytics.setCustomKey("last_sync", state.lastSyncTime)
    }
    
    // 네트워크 요청 기록
    fun logNetworkRequest(
        url: String,
        method: String,
        statusCode: Int,
        duration: Long
    ) {
        crashlytics.log("Network: $method $url - $statusCode (${duration}ms)")
        
        if (statusCode >= 400) {
            crashlytics.setCustomKey("last_failed_request", url)
            crashlytics.setCustomKey("last_error_code", statusCode)
        }
    }
    
    // 사용자 액션 기록
    fun logUserAction(action: String, screen: String) {
        crashlytics.log("User: $action on $screen")
        crashlytics.setCustomKey("last_action", action)
        crashlytics.setCustomKey("last_screen", screen)
    }
    
    // 비치명적 에러 기록
    fun recordNonFatalError(
        error: Throwable,
        context: String,
        additionalInfo: Map<String, String> = emptyMap()
    ) {
        crashlytics.log("Non-fatal error in $context")
        additionalInfo.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(error)
    }
}

// 사용 예시
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    logger: CrashlyticsLogger
) {
    LaunchedEffect(Unit) {
        logger.logUserAction("entered", "PaymentScreen")
    }
    
    Button(onClick = {
        logger.logUserAction("clicked_pay_button", "PaymentScreen")
        
        viewModelScope.launch {
            try {
                val result = paymentService.processPayment(amount)
                logger.logNetworkRequest(
                    url = "/api/payment",
                    method = "POST",
                    statusCode = 200,
                    duration = result.duration
                )
            } catch (e: Exception) {
                logger.recordNonFatalError(
                    error = e,
                    context = "PaymentScreen.processPayment",
                    additionalInfo = mapOf(
                        "amount" to amount.toString(),
                        "payment_method" to paymentMethod
                    )
                )
            }
        }
    }) {
        Text("결제하기")
    }
}
```

**Crashlytics 콘솔에서 보이는 것:**
```
크래시 발생!

스택 트레이스:
PaymentService.processPayment (line 123)
NetworkException: Connection timeout

커스텀 키:
- user_email: user@example.com
- last_action: clicked_pay_button
- last_screen: PaymentScreen
- amount: 10000
- payment_method: credit_card

로그:
- User: entered on PaymentScreen
- User: clicked_pay_button on PaymentScreen
- Network: POST /api/payment - 500 (3000ms)

→ 결제 버튼 클릭 후 네트워크 타임아웃
→ 서버 응답 시간 개선 필요!
```

### 크래시 우선순위 설정

```kotlin
// 심각도별 분류
enum class CrashSeverity {
    CRITICAL,  // 결제, 로그인 등
    HIGH,      // 주요 기능
    MEDIUM,    // 일반 기능
    LOW        // 부가 기능
}

fun recordError(error: Throwable, severity: CrashSeverity) {
    val crashlytics = FirebaseCrashlytics.getInstance()
    
    crashlytics.setCustomKey("severity", severity.name)
    
    when (severity) {
        CrashSeverity.CRITICAL -> {
            // 즉시 알림
            crashlytics.setCustomKey("alert_team", true)
        }
        else -> {
            crashlytics.setCustomKey("alert_team", false)
        }
    }
    
    crashlytics.recordException(error)
}
```

---

## Performance Monitoring

> [!TIP]
> **Performance Monitoring은 앱이 얼마나 빠른지 측정합니다**
> 
> **측정 항목:**
> - 앱 시작 시간
> - 화면 렌더링 시간
> - 네트워크 요청 시간
> - 커스텀 작업 시간

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-perf")
}

// 자동 측정 (설정 불필요!)
// - 앱 시작 시간
// - 화면 렌더링
// - 네트워크 요청 (OkHttp, HttpURLConnection)
```

### 커스텀 트레이스

```kotlin
class PerformanceTracker {
    private val performance = Firebase.performance
    
    // 데이터 로딩 시간 측정
    suspend fun <T> trackDataLoading(
        name: String,
        block: suspend () -> T
    ): T {
        val trace = performance.newTrace("data_loading_$name")
        trace.start()
        
        return try {
            val result = block()
            trace.putMetric("success", 1)
            result
        } catch (e: Exception) {
            trace.putMetric("success", 0)
            throw e
        } finally {
            trace.stop()
        }
    }
    
    // 이미지 로딩 시간 측정
    fun trackImageLoading(imageUrl: String) {
        val trace = performance.newTrace("image_loading")
        trace.putAttribute("image_url", imageUrl)
        trace.start()
        
        // Coil 콜백에서 stop() 호출
        return trace
    }
}

// 사용
@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel,
    tracker: PerformanceTracker
) {
    LaunchedEffect(Unit) {
        tracker.trackDataLoading("articles") {
            viewModel.loadArticles()
        }
    }
}
```

**실제 측정 예시:**
```kotlin
// Repository
class ArticleRepository(
    private val api: ApiService,
    private val tracker: PerformanceTracker
) {
    suspend fun getArticles(): List<Article> {
        return tracker.trackDataLoading("api_articles") {
            api.getArticles()
        }
    }
}

// Firebase 콘솔에서:
// data_loading_api_articles
// - 평균 시간: 1.2초
// - 90번째 백분위수: 2.5초
// - 성공률: 98%
```

### 네트워크 요청 모니터링

```kotlin
// OkHttp Interceptor로 자동 측정
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        val trace = Firebase.performance.newHttpMetric(
            request.url.toString(),
            request.method
        )
        
        trace.start()
        
        try {
            val response = chain.proceed(request)
            
            trace.setHttpResponseCode(response.code)
            trace.setResponseContentType(response.header("Content-Type"))
            trace.setResponsePayloadSize(response.body?.contentLength() ?: 0)
            
            trace.stop()
            response
        } catch (e: Exception) {
            trace.stop()
            throw e
        }
    }
    .build()
```

---

## 사용자 행동 분석

### 코호트 분석

**코호트란?**
```
같은 시기에 가입한 사용자 그룹

예시:
- 1월 가입 코호트: 1000명
- 2월 가입 코호트: 1500명
- 3월 가입 코호트: 2000명

각 코호트의 유지율 비교:
1월: 30일 유지율 45%
2월: 30일 유지율 52%  ← 개선됨!
3월: 30일 유지율 58%  ← 더 개선됨!
```

```kotlin
// 가입 시 코호트 설정
fun setUserCohort(signupDate: Date) {
    val cohort = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        .format(signupDate)
    
    Firebase.analytics.setUserProperty("signup_cohort", cohort)
}

// 분석:
// "2024-01 코호트의 구매 전환율은?"
// "2024-02 코호트는 어떤 기능을 많이 쓰는가?"
```

### 리텐션 분석

```kotlin
// 앱 실행 시 마지막 방문 기록
fun trackAppOpen() {
    val analytics = Firebase.analytics
    val prefs = getSharedPreferences()
    
    val lastVisit = prefs.getLong("last_visit", 0)
    val now = System.currentTimeMillis()
    
    if (lastVisit > 0) {
        val daysSinceLastVisit = ((now - lastVisit) / (24 * 60 * 60 * 1000)).toInt()
        
        analytics.logEvent("app_open") {
            param("days_since_last_visit", daysSinceLastVisit.toLong())
        }
        
        // 재방문 사용자
        if (daysSinceLastVisit >= 7) {
            analytics.logEvent("user_returned", null)
        }
    }
    
    prefs.edit().putLong("last_visit", now).apply()
}
```

### 세션 분석

```kotlin
class SessionTracker {
    private var sessionStart: Long = 0
    private val analytics = Firebase.analytics
    
    fun startSession() {
        sessionStart = System.currentTimeMillis()
        analytics.logEvent("session_start", null)
    }
    
    fun endSession() {
        val sessionDuration = System.currentTimeMillis() - sessionStart
        
        analytics.logEvent("session_end") {
            param("session_duration", sessionDuration)
        }
        
        // 세션 길이별 분류
        val category = when {
            sessionDuration < 30_000 -> "very_short"  // < 30초
            sessionDuration < 120_000 -> "short"      // < 2분
            sessionDuration < 300_000 -> "medium"     // < 5분
            else -> "long"                            // >= 5분
        }
        
        analytics.setUserProperty("typical_session", category)
    }
}
```

---

## A/B 테스팅

> [!NOTE]
> **A/B 테스팅 = 두 가지 버전을 비교하여 더 나은 것을 선택**
> 
> **예시:**
> - 버튼 색상: 파란색 vs 빨간색
> - 가격: 9,900원 vs 10,000원
> - 문구: "구매하기" vs "지금 구매"

### Firebase Remote Config + Analytics

```kotlin
@Composable
fun PurchaseButton(
    onPurchase: () -> Unit
) {
    val remoteConfig = Firebase.remoteConfig
    val analytics = Firebase.analytics
    
    // Remote Config에서 버튼 색상 가져오기
    var buttonColor by remember { mutableStateOf(Color.Blue) }
    var buttonText by remember { mutableStateOf("구매하기") }
    
    LaunchedEffect(Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            // A/B 테스트 변형
            val variant = remoteConfig.getString("purchase_button_variant")
            
            when (variant) {
                "variant_a" -> {
                    buttonColor = Color.Blue
                    buttonText = "구매하기"
                }
                "variant_b" -> {
                    buttonColor = Color.Red
                    buttonText = "지금 구매"
                }
            }
            
            // 변형 기록
            analytics.setUserProperty("ab_test_variant", variant)
        }
    }
    
    Button(
        onClick = {
            // 클릭 이벤트 기록
            analytics.logEvent("purchase_button_click") {
                param("button_variant", 
                    remoteConfig.getString("purchase_button_variant")
                )
            }
            onPurchase()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
    ) {
        Text(buttonText)
    }
}

// Firebase 콘솔에서 분석:
// variant_a: 클릭률 3.2%, 구매 전환율 1.5%
// variant_b: 클릭률 4.1%, 구매 전환율 2.1%
// → variant_b가 더 효과적!
```

---

## 대시보드 구축

### 주요 지표 모니터링

```kotlin
// 앱 건강 지표
data class AppHealthMetrics(
    val crashFreeUsers: Double,      // 크래시 없는 사용자 %
    val avgSessionDuration: Long,    // 평균 세션 시간
    val dailyActiveUsers: Int,       // 일일 활성 사용자
    val retentionRate: Double,       // 유지율
    val avgAppStartTime: Long        // 평균 앱 시작 시간
)

// 실시간 모니터링
@Composable
fun AdminDashboard() {
    var metrics by remember { mutableStateOf<AppHealthMetrics?>(null) }
    
    LaunchedEffect(Unit) {
        // Firebase에서 지표 가져오기
        metrics = fetchMetricsFromFirebase()
    }
    
    metrics?.let { m ->
        Column {
            MetricCard(
                title = "크래시 없는 사용자",
                value = "${m.crashFreeUsers}%",
                isHealthy = m.crashFreeUsers > 99.0
            )
            
            MetricCard(
                title = "평균 세션 시간",
                value = "${m.avgSessionDuration / 1000}초",
                isHealthy = m.avgSessionDuration > 120_000
            )
            
            MetricCard(
                title = "일일 활성 사용자",
                value = "${m.dailyActiveUsers}명",
                isHealthy = true
            )
        }
    }
}
```

---

## 실전 분석 사례

### 사례 1: 이탈 지점 발견

**문제:**
```
회원가입 시작: 1000명
이메일 입력: 800명 (20% 이탈)
비밀번호 입력: 600명 (20% 이탈)
완료: 400명 (33% 이탈!)  ← 문제!
```

**분석:**
```kotlin
// 각 단계 추적
analytics.logEvent("signup_started", null)
analytics.logEvent("signup_email_entered", null)
analytics.logEvent("signup_password_entered", null)
analytics.logEvent("signup_completed", null)

// Firebase 콘솔에서 퍼널 분석
// → 비밀번호 입력 후 이탈률 높음
// → 비밀번호 규칙이 너무 복잡한가?
```

**해결:**
```kotlin
// 비밀번호 규칙 완화
// 8자 이상 + 특수문자 → 6자 이상

// 결과:
// 완료율: 40% → 60% (50% 개선!)
```

### 사례 2: 성능 병목 발견

**문제:**
```
사용자 불만: "앱이 느려요"
```

**분석:**
```kotlin
// Performance Monitoring 확인
// 홈 화면 로딩: 평균 3.5초 (너무 느림!)
// 90번째 백분위수: 5.2초

// 원인: 이미지 10개를 동시에 로드
```

**해결:**
```kotlin
// 이미지 지연 로딩
LazyColumn {
    items(articles) { article ->
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(article.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null
        )
    }
}

// 결과:
// 홈 화면 로딩: 3.5초 → 1.2초 (66% 개선!)
```

---

## 💡 모니터링 베스트 프랙티스

### 1. 개인정보 보호

```kotlin
// ❌ 민감한 정보 로깅 금지
analytics.logEvent("login") {
    param("email", user.email)  // 개인정보!
    param("password", password) // 절대 금지!
}

// ✅ 익명화된 정보만
analytics.logEvent("login") {
    param("user_id_hash", user.id.hashCode().toString())
    param("login_method", "email")
}
```

### 2. 이벤트 제한

```kotlin
// Firebase 무료 플랜:
// - 이벤트: 무제한
// - 파라미터: 이벤트당 25개
// - 사용자 속성: 25개

// 중요한 이벤트만 로깅
// 불필요한 이벤트는 제거
```

### 3. 테스트 데이터 분리

```kotlin
// 개발/테스트 데이터 필터링
if (!BuildConfig.DEBUG) {
    analytics.logEvent("production_event", null)
}

// 또는 별도 Firebase 프로젝트 사용
// - 개발용 프로젝트
// - 프로덕션 프로젝트
```

---

## 🎯 모니터링 체크리스트

### 기본 설정
- [ ] Firebase Analytics 설정
- [ ] Crashlytics 설정
- [ ] Performance Monitoring 설정
- [ ] 주요 이벤트 로깅

### 고급 분석
- [ ] 사용자 속성 설정
- [ ] 구매 퍼널 추적
- [ ] 커스텀 트레이스
- [ ] A/B 테스팅

### 모니터링
- [ ] 일일 지표 확인
- [ ] 크래시 알림 설정
- [ ] 성능 저하 알림
- [ ] 주간 리포트 검토

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Monitor Everything! 📊
