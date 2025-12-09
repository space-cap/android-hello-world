# 결제 및 인앱 구매 완벽 가이드

## 📚 목차

1. [인앱 결제란?](#인앱-결제란)
2. [Google Play Billing 시작하기](#google-play-billing-시작하기)
3. [일회성 구매](#일회성-구매)
4. [구독 관리](#구독-관리)
5. [결제 검증](#결제-검증)
6. [환불 처리](#환불-처리)
7. [테스트](#테스트)
8. [실전 예제](#실전-예제)

---

## 인앱 결제란?

> [!NOTE]
> **인앱 결제 = 앱 내에서 상품이나 서비스를 구매하는 것**
> 
> **주요 유형:**
> - 💰 일회성 구매 (소모성/비소모성)
> - 📅 구독 (월간/연간)
> - 🎁 무료 체험
> - ⬆️ 업그레이드

### 왜 중요한가?

**수익 모델:**
```
무료 앱 + 광고: 사용자당 월 $0.50
무료 앱 + 인앱 구매: 사용자당 월 $2-5
프리미엄 앱: 사용자당 $2.99 (1회)
구독 앱: 사용자당 월 $9.99
```

**통계:**
- 모바일 앱 수익의 **98%**가 인앱 결제
- 구독 모델의 연간 성장률: **18%**
- 평균 구독 유지율: **40%** (3개월 후)

### 결제 유형

**1. 소모성 상품 (Consumable)**
```
예시: 게임 코인, 아이템
특징: 사용하면 사라짐, 재구매 가능
```

**2. 비소모성 상품 (Non-consumable)**
```
예시: 프리미엄 업그레이드, 광고 제거
특징: 한 번 구매하면 영구 소유
```

**3. 구독 (Subscription)**
```
예시: 월간 프리미엄, 연간 멤버십
특징: 주기적으로 자동 결제
```

---

## Google Play Billing 시작하기

> [!IMPORTANT]
> **Google Play Billing = Google Play의 공식 결제 시스템**
> 
> **왜 사용해야 하는가?**
> - ✅ Google Play 정책 준수 (필수)
> - ✅ 안전한 결제 처리
> - ✅ 자동 환불 처리
> - ✅ 구독 관리 자동화

### 1단계: Google Play Console 설정

**매우 상세한 단계:**

1. **Google Play Console 접속**
   ```
   https://play.google.com/console 접속
   개발자 계정으로 로그인
   (개발자 등록비 $25 1회 결제 필요)
   ```

2. **앱 선택 또는 생성**
   ```
   기존 앱 선택 또는 "앱 만들기" 클릭
   ```

3. **인앱 상품 생성**
   ```
   왼쪽 메뉴 → "수익 창출" → "인앱 상품"
   ↓
   "상품 만들기" 클릭
   ↓
   상품 정보 입력:
   - 상품 ID: premium_upgrade (고유 ID, 변경 불가!)
   - 이름: 프리미엄 업그레이드
   - 설명: 광고 제거 및 모든 기능 잠금 해제
   - 가격: ₩9,900
   ↓
   "저장" 클릭
   ```

**상품 ID 명명 규칙:**
```kotlin
// ✅ 좋은 예
"premium_upgrade"
"coins_100"
"monthly_subscription"

// ❌ 나쁜 예
"product1"  // 의미 불명확
"프리미엄"  // 영문 사용 권장
"premium upgrade"  // 공백 사용 불가
```

4. **구독 상품 생성 (선택사항)**
   ```
   "수익 창출" → "구독"
   ↓
   "구독 만들기" 클릭
   ↓
   구독 정보 입력:
   - 구독 ID: monthly_premium
   - 이름: 월간 프리미엄
   - 혜택: 모든 프리미엄 기능
   ↓
   기본 플랜 추가:
   - 갱신 유형: 자동 갱신
   - 청구 기간: 1개월
   - 가격: ₩9,900
   ↓
   무료 체험 설정 (선택):
   - 체험 기간: 7일
   ↓
   "저장" 및 "활성화"
   ```

### 2단계: Android 프로젝트 설정

#### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // Google Play Billing Library
    val billingVersion = "6.1.0"
    implementation("com.android.billingclient:billing:$billingVersion")
    
    // Kotlin Coroutines 지원
    implementation("com.android.billingclient:billing-ktx:$billingVersion")
}
```

**왜 billing-ktx를 사용하는가?**
```kotlin
// billing (일반)
billingClient.queryPurchasesAsync(...) { billingResult, purchases ->
    // 콜백 지옥
}

// billing-ktx (Kotlin)
val result = billingClient.queryPurchasesAsync(...)
// suspend 함수로 깔끔!
```

#### AndroidManifest.xml 설정

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <!-- 인터넷 권한 (결제 통신) -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- 결제 권한 -->
    <uses-permission android:name="com.android.vending.BILLING" />
    
    <application>
        <activity ... />
    </application>
</manifest>
```

### 3단계: BillingClient 초기화

```kotlin
class BillingManager(
    private val context: Context
) {
    // BillingClient 인스턴스
    private var billingClient: BillingClient? = null
    
    // 연결 상태
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    // 구매 목록
    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()
    
    init {
        setupBillingClient()
    }
    
    private fun setupBillingClient() {
        // BillingClient 생성
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                // 구매 업데이트 리스너
                // 새 구매가 발생하면 자동으로 호출됨
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases?.let { handlePurchases(it) }
                }
            }
            .enablePendingPurchases()  // 보류 중인 구매 활성화 (필수!)
            .build()
        
        // BillingClient 연결
        connectToBillingService()
    }
    
    private fun connectToBillingService() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 연결 성공!
                    Log.d("Billing", "BillingClient 연결 성공")
                    _isReady.value = true
                    
                    // 기존 구매 내역 조회
                    queryPurchases()
                } else {
                    // 연결 실패
                    Log.e("Billing", "연결 실패: ${billingResult.debugMessage}")
                    _isReady.value = false
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // 연결 끊김 (자동으로 재연결 시도)
                Log.w("Billing", "BillingClient 연결 끊김")
                _isReady.value = false
                
                // 재연결 시도
                connectToBillingService()
            }
        })
    }
    
    // 기존 구매 내역 조회
    private fun queryPurchases() {
        // 인앱 상품 구매 내역
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
        
        // 구독 구매 내역
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }
    
    private fun handlePurchases(purchases: List<Purchase>) {
        // 구매 목록 업데이트
        _purchases.value = purchases
        
        // 각 구매 처리
        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    // 구매 완료
                    if (!purchase.isAcknowledged) {
                        // 구매 확인 필요
                        acknowledgePurchase(purchase)
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    // 구매 보류 중 (결제 대기)
                    Log.d("Billing", "구매 보류 중: ${purchase.products}")
                }
                else -> {
                    // 기타 상태
                    Log.d("Billing", "구매 상태: ${purchase.purchaseState}")
                }
            }
        }
    }
    
    // 구매 확인 (필수!)
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("Billing", "구매 확인 완료")
            }
        }
    }
    
    // 정리
    fun destroy() {
        billingClient?.endConnection()
    }
}
```

**왜 acknowledgePurchase가 필요한가?**
```
구매 완료 → 3일 이내 확인 필요
           ↓
      확인 안하면?
           ↓
      자동 환불!
```

---

## 일회성 구매

### 상품 정보 조회

```kotlin
class BillingManager(private val context: Context) {
    // ... (이전 코드)
    
    // 상품 정보 조회
    suspend fun queryProductDetails(
        productIds: List<String>
    ): List<ProductDetails> {
        // 상품 목록 생성
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)  // 인앱 상품
                .build()
        }
        
        // 쿼리 파라미터
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        // 상품 정보 조회
        val result = billingClient?.queryProductDetails(params)
        
        return if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList ?: emptyList()
        } else {
            Log.e("Billing", "상품 조회 실패: ${result?.billingResult?.debugMessage}")
            emptyList()
        }
    }
}

// 사용 예시
@Composable
fun ProductListScreen(
    billingManager: BillingManager
) {
    var products by remember { mutableStateOf<List<ProductDetails>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        // 상품 ID 목록
        val productIds = listOf(
            "premium_upgrade",  // 프리미엄 업그레이드
            "coins_100",        // 코인 100개
            "coins_500"         // 코인 500개
        )
        
        // 상품 정보 조회
        products = billingManager.queryProductDetails(productIds)
    }
    
    LazyColumn {
        items(products) { product ->
            ProductCard(
                product = product,
                onPurchase = { /* 구매 처리 */ }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductDetails,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 상품 이름
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            // 상품 설명
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 가격
                Text(
                    text = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // 구매 버튼
                Button(onClick = onPurchase) {
                    Text("구매하기")
                }
            }
        }
    }
}
```

### 구매 시작

```kotlin
class BillingManager(private val context: Context) {
    // ... (이전 코드)
    
    // 구매 시작
    fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails
    ) {
        // 구매 플로우 파라미터
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        // 구매 플로우 시작
        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        when (billingResult?.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // 구매 플로우 시작 성공
                Log.d("Billing", "구매 플로우 시작")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // 사용자가 취소
                Log.d("Billing", "사용자가 구매 취소")
            }
            else -> {
                // 에러
                Log.e("Billing", "구매 플로우 실패: ${billingResult?.debugMessage}")
            }
        }
    }
}

// 사용 예시
@Composable
fun PurchaseButton(
    product: ProductDetails,
    billingManager: BillingManager
) {
    val activity = LocalContext.current as Activity
    
    Button(
        onClick = {
            // 구매 시작
            billingManager.launchPurchaseFlow(activity, product)
        }
    ) {
        Icon(Icons.Filled.ShoppingCart, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("구매하기")
    }
}
```

**구매 플로우:**
```
1. 사용자가 "구매하기" 버튼 클릭
   ↓
2. launchBillingFlow() 호출
   ↓
3. Google Play 결제 화면 표시
   ↓
4. 사용자가 결제 방법 선택 및 확인
   ↓
5. 결제 처리
   ↓
6. PurchasesUpdatedListener 콜백 호출
   ↓
7. acknowledgePurchase() 호출 (필수!)
   ↓
8. 상품 제공
```

---

## 구독 관리

### 구독 상품 조회

```kotlin
// 구독 상품 조회
suspend fun querySubscriptionDetails(
    subscriptionIds: List<String>
): List<ProductDetails> {
    val productList = subscriptionIds.map { subscriptionId ->
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(subscriptionId)
            .setProductType(BillingClient.ProductType.SUBS)  // 구독
            .build()
    }
    
    val params = QueryProductDetailsParams.newBuilder()
        .setProductList(productList)
        .build()
    
    val result = billingClient?.queryProductDetails(params)
    
    return if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
        result.productDetailsList ?: emptyList()
    } else {
        emptyList()
    }
}
```

### 구독 구매

```kotlin
// 구독 구매
fun launchSubscriptionFlow(
    activity: Activity,
    productDetails: ProductDetails,
    offerToken: String  // 구독 오퍼 토큰
) {
    val productDetailsParamsList = listOf(
        BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)  // 구독은 오퍼 토큰 필요!
            .build()
    )
    
    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .build()
    
    billingClient?.launchBillingFlow(activity, billingFlowParams)
}

// 구독 카드
@Composable
fun SubscriptionCard(
    product: ProductDetails,
    billingManager: BillingManager
) {
    val activity = LocalContext.current as Activity
    
    // 구독 오퍼 (가격 플랜)
    val subscriptionOffer = product.subscriptionOfferDetails?.firstOrNull()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 무료 체험 정보
            subscriptionOffer?.let { offer ->
                val freeTrialPhase = offer.pricingPhases.pricingPhaseList
                    .firstOrNull { it.priceAmountMicros == 0L }
                
                if (freeTrialPhase != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🎁 ${freeTrialPhase.billingPeriod} 무료 체험",
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 가격 정보
                val pricingPhase = offer.pricingPhases.pricingPhaseList.last()
                Text(
                    text = "${pricingPhase.formattedPrice} / ${pricingPhase.billingPeriod}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 구독 버튼
            Button(
                onClick = {
                    subscriptionOffer?.let { offer ->
                        billingManager.launchSubscriptionFlow(
                            activity,
                            product,
                            offer.offerToken
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("구독하기")
            }
        }
    }
}
```

### 구독 상태 확인

```kotlin
// 구독 활성 여부 확인
fun isSubscriptionActive(subscriptionId: String): Boolean {
    return purchases.value.any { purchase ->
        purchase.products.contains(subscriptionId) &&
        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
    }
}

// 사용 예시
@Composable
fun PremiumFeatureScreen(
    billingManager: BillingManager
) {
    val isPremium by remember {
        derivedStateOf {
            billingManager.isSubscriptionActive("monthly_premium")
        }
    }
    
    if (isPremium) {
        // 프리미엄 기능 표시
        PremiumContent()
    } else {
        // 구독 안내
        SubscriptionPrompt()
    }
}
```

---

## 결제 검증

> [!CAUTION]
> **클라이언트 검증만으로는 부족합니다!**
> 
> 해커가 앱을 수정하여 구매를 위조할 수 있습니다.
> 서버에서 반드시 검증해야 합니다.

### 서버 검증 (권장)

```kotlin
// 1. 클라이언트에서 구매 토큰을 서버로 전송
suspend fun verifyPurchaseOnServer(
    purchase: Purchase
): Boolean {
    return try {
        val response = apiService.verifyPurchase(
            productId = purchase.products.first(),
            purchaseToken = purchase.purchaseToken,
            orderId = purchase.orderId
        )
        
        response.isValid
    } catch (e: Exception) {
        Log.e("Billing", "서버 검증 실패", e)
        false
    }
}

// 2. 서버에서 Google Play Developer API로 검증
// (서버 코드 - Node.js 예시)
/*
const { google } = require('googleapis');

async function verifyPurchase(packageName, productId, purchaseToken) {
    const auth = new google.auth.GoogleAuth({
        keyFile: 'service-account-key.json',
        scopes: ['https://www.googleapis.com/auth/androidpublisher']
    });
    
    const androidPublisher = google.androidpublisher({
        version: 'v3',
        auth: auth
    });
    
    try {
        const result = await androidPublisher.purchases.products.get({
            packageName: packageName,
            productId: productId,
            token: purchaseToken
        });
        
        // 구매 상태 확인
        return result.data.purchaseState === 0; // 0 = 구매됨
    } catch (error) {
        console.error('검증 실패:', error);
        return false;
    }
}
*/
```

**왜 서버 검증이 필요한가?**
```
클라이언트만 검증:
해커가 앱 수정 → 구매 위조 → 무료 사용

서버 검증:
해커가 앱 수정 → 서버가 Google에 확인 → 위조 감지 → 차단
```

---

## 환불 처리

### 환불 감지

```kotlin
// 구매 목록을 주기적으로 확인
fun checkForRefunds() {
    queryPurchases()
    
    // 이전에 있던 구매가 사라졌다면 환불됨
    val previousPurchases = _purchases.value
    val currentPurchases = /* 새로 조회한 구매 */
    
    val refundedPurchases = previousPurchases.filter { previous ->
        currentPurchases.none { it.orderId == previous.orderId }
    }
    
    refundedPurchases.forEach { refunded ->
        // 환불 처리
        handleRefund(refunded)
    }
}

fun handleRefund(purchase: Purchase) {
    Log.d("Billing", "환불 감지: ${purchase.products}")
    
    // 프리미엄 기능 제거
    removePremiumFeatures()
    
    // 사용자에게 알림
    showRefundNotification()
}
```

---

## 테스트

### 테스트 계정 설정

**Google Play Console에서:**
```
1. "설정" → "라이선스 테스트"
2. "라이선스 테스터" 추가
3. 테스터 이메일 입력 (Gmail)
4. "저장"
```

**테스터 기기에서:**
```
1. 테스터 Gmail 계정으로 로그인
2. 앱 설치 (내부 테스트 트랙)
3. 구매 시도
4. "테스트 구매" 표시됨
5. 실제 결제 없이 구매 가능!
```

### 테스트 상품

```kotlin
// 테스트용 상품 ID
val TEST_PRODUCT_IDS = listOf(
    "android.test.purchased",  // 항상 성공
    "android.test.canceled",   // 항상 취소
    "android.test.refunded",   // 항상 환불
    "android.test.item_unavailable"  // 항상 실패
)
```

---

## 실전 예제

### 완전한 결제 시스템

```kotlin
// ViewModel
@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {
    
    // 상품 목록
    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()
    
    // 구독 목록
    private val _subscriptions = MutableStateFlow<List<ProductDetails>>(emptyList())
    val subscriptions: StateFlow<List<ProductDetails>> = _subscriptions.asStateFlow()
    
    // 프리미엄 상태
    val isPremium: StateFlow<Boolean> = billingManager.purchases
        .map { purchases ->
            purchases.any { it.products.contains("premium_upgrade") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    
    init {
        loadProducts()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            // 인앱 상품 로드
            _products.value = billingManager.queryProductDetails(
                listOf("premium_upgrade", "coins_100", "coins_500")
            )
            
            // 구독 상품 로드
            _subscriptions.value = billingManager.querySubscriptionDetails(
                listOf("monthly_premium", "yearly_premium")
            )
        }
    }
    
    fun purchase(activity: Activity, product: ProductDetails) {
        billingManager.launchPurchaseFlow(activity, product)
    }
}

// UI
@Composable
fun StoreScreen(
    viewModel: BillingViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 프리미엄 상태 표시
        if (isPremium) {
            PremiumBadge()
        }
        
        // 구독 섹션
        Text(
            "구독",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        subscriptions.forEach { subscription ->
            SubscriptionCard(
                product = subscription,
                billingManager = viewModel.billingManager
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 인앱 상품 섹션
        Text(
            "인앱 상품",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        products.forEach { product ->
            ProductCard(
                product = product,
                onPurchase = { /* 구매 */ }
            )
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 구매 복원

```kotlin
// 구매 복원 버튼
Button(onClick = {
    billingManager.queryPurchases()
}) {
    Text("구매 복원")
}
```

### 2. 에러 처리

```kotlin
when (billingResult.responseCode) {
    BillingClient.BillingResponseCode.OK -> {
        // 성공
    }
    BillingClient.BillingResponseCode.USER_CANCELED -> {
        // 사용자 취소
        Toast.makeText(context, "구매가 취소되었습니다", Toast.LENGTH_SHORT).show()
    }
    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
        // 이미 소유
        Toast.makeText(context, "이미 구매한 상품입니다", Toast.LENGTH_SHORT).show()
    }
    else -> {
        // 기타 에러
        Toast.makeText(context, "구매 실패: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
    }
}
```

### 3. 보안

```kotlin
// ✅ 서버에서 검증
// ✅ 난독화 (ProGuard/R8)
// ✅ 구매 토큰 암호화 저장
// ❌ 클라이언트만 검증
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Happy Selling! 💰
