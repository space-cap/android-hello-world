# 딥링크 및 App Links 완벽 가이드

## 📚 목차

1. [딥링크란?](#딥링크란)
2. [딥링크 vs App Links](#딥링크-vs-app-links)
3. [기본 딥링크 설정](#기본-딥링크-설정)
4. [Navigation과 통합](#navigation과-통합)
5. [App Links 설정](#app-links-설정)
6. [동적 링크 (Firebase)](#동적-링크-firebase)
7. [테스트](#테스트)
8. [실전 예제](#실전-예제)

---

## 딥링크란?

> [!NOTE]
> **딥링크 (Deep Link) = 앱의 특정 화면으로 바로 이동하는 링크**
> 
> **사용 사례:**
> - 📧 이메일 링크 → 앱 특정 페이지
> - 📱 푸시 알림 → 앱 상세 화면
> - 🌐 웹사이트 → 앱 설치/실행
> - 📲 QR 코드 → 앱 프로모션 페이지

### 왜 중요한가?

**사용자 경험:**
```
딥링크 없이:
링크 클릭 → 앱 실행 → 홈 화면 → 메뉴 → 찾기 → 목적지
(5단계, 불편함)

딥링크 사용:
링크 클릭 → 목적지 화면
(1단계, 편리함!)
```

**통계:**
- 딥링크 사용 앱의 전환율: **2배 증가**
- 사용자 재방문율: **50% 증가**
- 앱 설치율: **25% 증가**

**실제 예시:**
```
YouTube:
https://www.youtube.com/watch?v=VIDEO_ID
→ YouTube 앱의 해당 동영상 재생

Instagram:
https://www.instagram.com/p/POST_ID
→ Instagram 앱의 해당 게시물

쇼핑몰:
https://shop.example.com/products/12345
→ 쇼핑 앱의 상품 상세 페이지
```

---

## 딥링크 vs App Links

### 딥링크 (Deep Link)

**특징:**
```
- 커스텀 스킴 사용 (예: myapp://...)
- 앱이 설치되어 있어야 작동
- 앱 선택 다이얼로그 표시 가능
- 설정이 간단
```

**예시:**
```
myapp://product/12345
myapp://profile/user123
myapp://settings
```

### App Links (Android App Links)

**특징:**
```
- HTTP/HTTPS URL 사용 (예: https://example.com/...)
- 웹사이트 소유권 검증 필요
- 앱 자동 실행 (다이얼로그 없음)
- 앱 미설치 시 웹사이트로 이동
- 설정이 복잡하지만 더 강력
```

**예시:**
```
https://shop.example.com/products/12345
https://example.com/profile/user123
```

**비교표:**

| 기능 | 딥링크 | App Links |
|------|--------|-----------|
| URL 형식 | 커스텀 (myapp://) | HTTP/HTTPS |
| 검증 | 불필요 | 필요 (도메인 소유권) |
| 앱 선택 | 다이얼로그 표시 | 자동 실행 |
| 앱 미설치 | 에러 | 웹사이트로 이동 |
| 보안 | 낮음 | 높음 |
| 설정 난이도 | 쉬움 | 어려움 |

---

## 기본 딥링크 설정

### 1단계: AndroidManifest.xml 설정

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <application>
        <activity
            android:name=".MainActivity"
            android:exported="true">
            
            <!-- 기본 런처 -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- 딥링크 1: 커스텀 스킴 -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                
                <!-- myapp:// 스킴 -->
                <data
                    android:scheme="myapp"
                    android:host="open" />
            </intent-filter>
            
            <!-- 딥링크 2: 상품 상세 -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                
                <data
                    android:scheme="myapp"
                    android:host="product"
                    android:pathPrefix="/detail" />
            </intent-filter>
            
            <!-- 딥링크 3: 사용자 프로필 -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                
                <data
                    android:scheme="myapp"
                    android:host="profile" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**각 요소 설명:**
```xml
<action android:name="android.intent.action.VIEW" />
<!-- VIEW 액션: 링크를 볼 수 있음 -->

<category android:name="android.intent.category.DEFAULT" />
<!-- DEFAULT 카테고리: 암시적 인텐트 수신 -->

<category android:name="android.intent.category.BROWSABLE" />
<!-- BROWSABLE 카테고리: 브라우저에서 실행 가능 -->

<data
    android:scheme="myapp"      <!-- 스킴: myapp:// -->
    android:host="product"      <!-- 호스트: product -->
    android:pathPrefix="/detail" />  <!-- 경로: /detail -->
<!-- 결과: myapp://product/detail -->
```

**지원되는 URL 예시:**
```
myapp://open
myapp://product/detail/12345
myapp://profile/user123
```

### 2단계: Activity에서 딥링크 처리

```kotlin
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 딥링크 처리
        handleDeepLink(intent)
        
        setContent {
            MyApp()
        }
    }
    
    // 새 인텐트 수신 (앱이 이미 실행 중일 때)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        // URI 가져오기
        val uri = intent?.data
        
        if (uri != null) {
            Log.d("DeepLink", "URI: $uri")
            
            // URI 파싱
            when (uri.host) {
                "open" -> {
                    // myapp://open
                    Log.d("DeepLink", "앱 열기")
                }
                
                "product" -> {
                    // myapp://product/detail/12345
                    val productId = uri.pathSegments.getOrNull(1)
                    Log.d("DeepLink", "상품 ID: $productId")
                    
                    // 상품 상세 화면으로 이동
                    navigateToProduct(productId)
                }
                
                "profile" -> {
                    // myapp://profile/user123
                    val userId = uri.pathSegments.getOrNull(0)
                    Log.d("DeepLink", "사용자 ID: $userId")
                    
                    // 프로필 화면으로 이동
                    navigateToProfile(userId)
                }
            }
        }
    }
    
    private fun navigateToProduct(productId: String?) {
        // Navigation으로 이동 (다음 섹션에서 설명)
    }
    
    private fun navigateToProfile(userId: String?) {
        // Navigation으로 이동
    }
}
```

**URI 파싱 예시:**
```kotlin
// URI: myapp://product/detail/12345?color=red&size=M

uri.scheme      // "myapp"
uri.host        // "product"
uri.path        // "/detail/12345"
uri.pathSegments // ["detail", "12345"]
uri.getQueryParameter("color")  // "red"
uri.getQueryParameter("size")   // "M"
```

---

## Navigation과 통합

### NavDeepLink 사용

```kotlin
@Composable
fun MyApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 홈 화면
        composable("home") {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                }
            )
        }
        
        // 상품 상세 화면 (딥링크 지원)
        composable(
            route = "product/{productId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                // 딥링크 1: 커스텀 스킴
                navDeepLink {
                    uriPattern = "myapp://product/detail/{productId}"
                },
                // 딥링크 2: HTTP URL
                navDeepLink {
                    uriPattern = "https://example.com/products/{productId}"
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(productId = productId ?: "")
        }
        
        // 프로필 화면 (딥링크 지원)
        composable(
            route = "profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "myapp://profile/{userId}"
                },
                navDeepLink {
                    uriPattern = "https://example.com/users/{userId}"
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(userId = userId ?: "")
        }
        
        // 설정 화면 (딥링크 지원)
        composable(
            route = "settings",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "myapp://settings"
                }
            )
        ) {
            SettingsScreen()
        }
    }
}
```

**동작 원리:**
```
1. 사용자가 링크 클릭: myapp://product/detail/12345
   ↓
2. Android가 앱 실행 (또는 포그라운드로)
   ↓
3. MainActivity.onCreate() 또는 onNewIntent() 호출
   ↓
4. NavController가 자동으로 딥링크 처리
   ↓
5. "product/{productId}" 화면으로 이동
   ↓
6. ProductDetailScreen(productId = "12345") 표시
```

### 딥링크 생성 (공유하기)

```kotlin
@Composable
fun ProductDetailScreen(productId: String) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "상품 ID: $productId",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 공유 버튼
        Button(
            onClick = {
                // 딥링크 생성
                val deepLink = "myapp://product/detail/$productId"
                
                // 공유 인텐트
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "이 상품을 확인해보세요: $deepLink")
                }
                
                context.startActivity(
                    Intent.createChooser(shareIntent, "공유하기")
                )
            }
        ) {
            Icon(Icons.Filled.Share, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("공유하기")
        }
    }
}
```

---

## App Links 설정

> [!IMPORTANT]
> **App Links는 도메인 소유권 검증이 필요합니다!**
> 
> 웹사이트에 검증 파일을 업로드해야 합니다.

### 1단계: AndroidManifest.xml 설정

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:exported="true">
    
    <!-- App Links (HTTP/HTTPS) -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        
        <!-- HTTPS URL -->
        <data
            android:scheme="https"
            android:host="example.com"
            android:pathPrefix="/products" />
        
        <data
            android:scheme="https"
            android:host="example.com"
            android:pathPrefix="/users" />
    </intent-filter>
</activity>
```

**android:autoVerify="true":**
```
이 속성이 있으면 Android가 자동으로 도메인 검증 시도
검증 성공 시 앱이 자동으로 실행 (다이얼로그 없음)
검증 실패 시 일반 딥링크처럼 작동 (다이얼로그 표시)
```

### 2단계: Digital Asset Links 파일 생성

**웹사이트에 업로드할 파일:**
```
https://example.com/.well-known/assetlinks.json
```

**파일 내용:**
```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.example.myapp",
      "sha256_cert_fingerprints": [
        "14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"
      ]
    }
  }
]
```

**SHA-256 지문 얻기:**
```bash
# 디버그 키스토어
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# 릴리스 키스토어
keytool -list -v -keystore /path/to/release.keystore -alias your_alias

# 출력에서 SHA256 찾기
# SHA256: 14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5
```

### 3단계: 검증 테스트

```bash
# Android Studio Terminal에서 실행
adb shell pm get-app-links com.example.myapp

# 출력 예시:
# com.example.myapp:
#   ID: ***
#   Signatures: [***]
#   Domain verification state:
#     example.com: verified
```

**검증 상태:**
```
verified: 검증 성공 ✅
none: 검증 안됨
ask: 사용자에게 물어봄
always: 항상 이 앱 사용
never: 절대 사용 안함
```

---

## 동적 링크 (Firebase)

> [!TIP]
> **Firebase Dynamic Links = 더 강력한 딥링크**
> 
> **장점:**
> - 앱 미설치 시 Play Store로 이동
> - 설치 후 원래 목적지로 이동
> - 짧은 URL 생성
> - 분석 데이터 제공

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-dynamic-links")
}
```

### 동적 링크 생성

```kotlin
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase

fun createDynamicLink(productId: String): String {
    // 동적 링크 생성
    val dynamicLink = Firebase.dynamicLinks.dynamicLink {
        // 딥링크 URL
        link = Uri.parse("https://example.com/products/$productId")
        
        // 도메인 (Firebase Console에서 설정)
        domainUriPrefix = "https://example.page.link"
        
        // Android 설정
        androidParameters("com.example.myapp") {
            minimumVersion = 1  // 최소 버전
            fallbackUrl = Uri.parse("https://example.com/products/$productId")  // 앱 미설치 시
        }
        
        // iOS 설정 (선택)
        iosParameters("com.example.myapp.ios") {
            appStoreId = "123456789"
            minimumVersion = "1.0"
        }
        
        // 소셜 메타 태그
        socialMetaTagParameters {
            title = "상품 이름"
            description = "상품 설명"
            imageUrl = Uri.parse("https://example.com/image.jpg")
        }
    }
    
    // 긴 URL
    val longLink = dynamicLink.uri
    
    return longLink.toString()
}

// 짧은 URL 생성
suspend fun createShortDynamicLink(productId: String): String {
    val dynamicLink = Firebase.dynamicLinks.dynamicLink {
        link = Uri.parse("https://example.com/products/$productId")
        domainUriPrefix = "https://example.page.link"
        androidParameters("com.example.myapp") { }
    }
    
    // 짧은 URL 생성
    val shortLink = Firebase.dynamicLinks.shortLinkAsync {
        longLink = dynamicLink.uri
    }.await()
    
    return shortLink.shortLink.toString()
    // 예: https://example.page.link/AbCd
}
```

### 동적 링크 수신

```kotlin
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 동적 링크 처리
        handleDynamicLink()
        
        setContent {
            MyApp()
        }
    }
    
    private fun handleDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                // 동적 링크 데이터 가져오기
                val deepLink = pendingDynamicLinkData?.link
                
                if (deepLink != null) {
                    Log.d("DynamicLink", "Deep Link: $deepLink")
                    
                    // URI 파싱
                    // https://example.com/products/12345
                    val productId = deepLink.lastPathSegment
                    
                    // 화면 이동
                    navigateToProduct(productId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DynamicLink", "동적 링크 가져오기 실패", e)
            }
    }
}
```

**동적 링크 흐름:**
```
사용자가 동적 링크 클릭
    ↓
앱 설치되어 있음?
    ├─ Yes → 앱 실행 → 목적지 화면
    └─ No → Play Store → 앱 설치 → 앱 실행 → 목적지 화면
```

---

## 테스트

### ADB로 테스트

```bash
# 커스텀 스킴 테스트
adb shell am start -W -a android.intent.action.VIEW -d "myapp://product/detail/12345" com.example.myapp

# HTTPS URL 테스트
adb shell am start -W -a android.intent.action.VIEW -d "https://example.com/products/12345" com.example.myapp

# 파라미터 포함
adb shell am start -W -a android.intent.action.VIEW -d "myapp://product/detail/12345?color=red&size=M" com.example.myapp
```

### HTML 테스트 페이지

```html
<!DOCTYPE html>
<html>
<head>
    <title>딥링크 테스트</title>
</head>
<body>
    <h1>딥링크 테스트</h1>
    
    <!-- 커스텀 스킴 -->
    <h2>커스텀 스킴</h2>
    <a href="myapp://open">앱 열기</a><br>
    <a href="myapp://product/detail/12345">상품 12345</a><br>
    <a href="myapp://profile/user123">사용자 프로필</a><br>
    
    <!-- HTTPS URL -->
    <h2>HTTPS URL</h2>
    <a href="https://example.com/products/12345">상품 페이지</a><br>
    <a href="https://example.com/users/user123">사용자 페이지</a><br>
    
    <!-- 버튼으로 테스트 -->
    <h2>JavaScript 테스트</h2>
    <button onclick="window.location='myapp://product/detail/99999'">
        상품 99999 열기
    </button>
</body>
</html>
```

### Logcat으로 확인

```kotlin
// 딥링크 수신 시 로그
private fun handleDeepLink(intent: Intent?) {
    val uri = intent?.data
    
    if (uri != null) {
        Log.d("DeepLink", "=== 딥링크 수신 ===")
        Log.d("DeepLink", "전체 URI: $uri")
        Log.d("DeepLink", "스킴: ${uri.scheme}")
        Log.d("DeepLink", "호스트: ${uri.host}")
        Log.d("DeepLink", "경로: ${uri.path}")
        Log.d("DeepLink", "경로 세그먼트: ${uri.pathSegments}")
        
        // 쿼리 파라미터
        uri.queryParameterNames.forEach { param ->
            Log.d("DeepLink", "파라미터 $param: ${uri.getQueryParameter(param)}")
        }
    }
}
```

---

## 실전 예제

### 완전한 딥링크 시스템

```kotlin
// DeepLinkHandler.kt
class DeepLinkHandler(
    private val navController: NavHostController
) {
    // 딥링크 처리
    fun handleDeepLink(uri: Uri?) {
        if (uri == null) return
        
        Log.d("DeepLink", "URI 처리: $uri")
        
        when {
            // 상품 상세
            uri.pathSegments.firstOrNull() == "products" -> {
                val productId = uri.pathSegments.getOrNull(1)
                if (productId != null) {
                    navController.navigate("product/$productId")
                }
            }
            
            // 사용자 프로필
            uri.pathSegments.firstOrNull() == "users" -> {
                val userId = uri.pathSegments.getOrNull(1)
                if (userId != null) {
                    navController.navigate("profile/$userId")
                }
            }
            
            // 카테고리
            uri.pathSegments.firstOrNull() == "categories" -> {
                val categoryId = uri.pathSegments.getOrNull(1)
                if (categoryId != null) {
                    navController.navigate("category/$categoryId")
                }
            }
            
            // 설정
            uri.path == "/settings" -> {
                navController.navigate("settings")
            }
            
            // 기본 (홈)
            else -> {
                navController.navigate("home")
            }
        }
    }
}

// MainActivity.kt
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MyAppWithDeepLink(intent)
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Compose에서 재구성 트리거
    }
}

// MyApp.kt
@Composable
fun MyAppWithDeepLink(intent: Intent) {
    val navController = rememberNavController()
    val deepLinkHandler = remember { DeepLinkHandler(navController) }
    
    // 딥링크 처리
    LaunchedEffect(intent) {
        deepLinkHandler.handleDeepLink(intent.data)
    }
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen()
        }
        
        composable(
            route = "product/{productId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "myapp://product/detail/{productId}" },
                navDeepLink { uriPattern = "https://example.com/products/{productId}" }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(productId)
        }
        
        composable(
            route = "profile/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "myapp://profile/{userId}" },
                navDeepLink { uriPattern = "https://example.com/users/{userId}" }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(userId)
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 일관된 URL 구조

```kotlin
// ✅ 좋은 예
myapp://product/detail/12345
myapp://profile/user123
myapp://category/electronics

// ❌ 나쁜 예
myapp://12345
myapp://user
myapp://cat
```

### 2. 에러 처리

```kotlin
fun handleDeepLink(uri: Uri?) {
    try {
        // 딥링크 처리
        val productId = uri?.pathSegments?.getOrNull(1)
        
        if (productId != null) {
            navController.navigate("product/$productId")
        } else {
            // 잘못된 링크 → 홈으로
            navController.navigate("home")
        }
    } catch (e: Exception) {
        Log.e("DeepLink", "처리 실패", e)
        navController.navigate("home")
    }
}
```

### 3. 분석 추적

```kotlin
fun handleDeepLink(uri: Uri?) {
    // 분석 이벤트 로깅
    Firebase.analytics.logEvent("deep_link_opened") {
        param("uri", uri.toString())
        param("source", uri?.getQueryParameter("source") ?: "unknown")
    }
    
    // 딥링크 처리
    // ...
}
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Link to Success! 🔗
