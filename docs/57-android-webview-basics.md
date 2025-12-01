# Android WebView 기본 가이드

## 목차
1. [WebView란?](#webview란)
2. [WebView 기본 설정](#webview-기본-설정)
3. [웹 페이지 로드하기](#웹-페이지-로드하기)
4. [WebView 설정 옵션](#webview-설정-옵션)
5. [WebViewClient 사용](#webviewclient-사용)
6. [WebChromeClient 사용](#webchromeclient-사용)
7. [쿠키 관리](#쿠키-관리)
8. [파일 다운로드](#파일-다운로드)
9. [보안 고려사항](#보안-고려사항)
10. [Jetpack Compose에서 WebView 사용](#jetpack-compose에서-webview-사용)

---

## WebView란?

**WebView**는 Android 앱 안에서 **웹 페이지를 표시할 수 있는 컴포넌트**입니다.

### 왜 WebView를 사용하나요?

#### 사용 사례
- 📱 **하이브리드 앱**: 웹 기술(HTML/CSS/JS)로 만든 콘텐츠를 앱에 포함
- 📰 **뉴스/블로그**: 웹 콘텐츠를 앱에서 표시
- 💳 **결제 페이지**: 외부 결제 시스템 연동
- 📄 **약관/정책**: 자주 변경되는 문서를 웹으로 관리
- 🎮 **웹 게임**: HTML5 게임을 앱에 통합

### WebView vs 브라우저 앱

```
WebView:
- 앱 안에 내장
- 커스터마이징 가능
- 앱과 통신 가능

브라우저 앱:
- 별도 앱 실행
- 사용자가 벗어날 수 있음
- 앱과 통신 어려움
```

---

## WebView 기본 설정

### 1. 권한 추가

**AndroidManifest.xml**:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- 인터넷 권한 (필수) -->
    <uses-permission android:name="android.permission.INTERNET"/>
    
    <!-- 네트워크 상태 확인 (선택) -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    
    <application
        android:usesCleartextTraffic="true">  <!-- HTTP 허용 (개발용) -->
        <!-- ... -->
    </application>
</manifest>
```

### 2. 기본 WebView 생성

**XML 레이아웃**:
```xml
<!-- res/layout/activity_web.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
        
</LinearLayout>
```

**Activity 코드**:
```kotlin
import android.webkit.WebView

/**
 * 기본 WebView 사용 예제
 */
class WebActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        
        // WebView 초기화
        webView = findViewById(R.id.webView)
        
        // 기본 설정
        webView.settings.javaScriptEnabled = true  // JavaScript 활성화
        
        // 웹 페이지 로드
        webView.loadUrl("https://www.google.com")
    }
    
    /**
     * 뒤로 가기 버튼 처리
     */
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()  // WebView 히스토리 뒤로 가기
        } else {
            super.onBackPressed()  // 액티비티 종료
        }
    }
}
```

---

## 웹 페이지 로드하기

### 1. URL 로드

```kotlin
/**
 * URL로 웹 페이지 로드
 */
fun loadWebPage() {
    // 외부 URL
    webView.loadUrl("https://www.example.com")
    
    // HTTP URL (usesCleartextTraffic=true 필요)
    webView.loadUrl("http://www.example.com")
}
```

### 2. HTML 문자열 로드

```kotlin
/**
 * HTML 문자열 직접 로드
 */
fun loadHtmlString() {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    padding: 20px;
                    background-color: #f0f0f0;
                }
                h1 {
                    color: #333;
                }
            </style>
        </head>
        <body>
            <h1>Hello from HTML!</h1>
            <p>이것은 HTML 문자열로 로드된 페이지입니다.</p>
            <button onclick="alert('버튼 클릭!')">클릭</button>
        </body>
        </html>
    """.trimIndent()
    
    webView.loadData(
        htmlContent,
        "text/html",
        "UTF-8"
    )
    
    // 또는 Base64 인코딩 사용
    webView.loadDataWithBaseURL(
        null,  // baseUrl
        htmlContent,
        "text/html",
        "UTF-8",
        null  // historyUrl
    )
}
```

### 3. 로컬 파일 로드

```kotlin
/**
 * assets 폴더의 HTML 파일 로드
 */
fun loadLocalFile() {
    // assets/index.html 파일 로드
    webView.loadUrl("file:///android_asset/index.html")
}
```

**assets/index.html**:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로컬 페이지</title>
</head>
<body>
    <h1>로컬 HTML 파일</h1>
    <p>assets 폴더에서 로드됨</p>
</body>
</html>
```

---

## WebView 설정 옵션

### 기본 설정

```kotlin
/**
 * WebView 기본 설정
 */
fun setupWebView() {
    webView.settings.apply {
        // JavaScript 활성화 (필수)
        javaScriptEnabled = true
        
        // DOM Storage 활성화
        domStorageEnabled = true
        
        // 캐시 모드 설정
        cacheMode = WebSettings.LOAD_DEFAULT
        
        // 줌 컨트롤 활성화
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false  // 줌 버튼 숨기기
        
        // 뷰포트 설정
        useWideViewPort = true
        loadWithOverviewMode = true
        
        // 파일 접근 허용
        allowFileAccess = true
        allowContentAccess = true
        
        // 멀티 윈도우 지원
        setSupportMultipleWindows(false)
        
        // 텍스트 인코딩
        defaultTextEncodingName = "UTF-8"
    }
}
```

### 고급 설정

```kotlin
/**
 * WebView 고급 설정
 */
fun advancedSetup() {
    webView.settings.apply {
        // 혼합 콘텐츠 모드 (HTTPS 페이지에서 HTTP 리소스 로드)
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        
        // 데이터베이스 활성화
        databaseEnabled = true
        
        // 위치 정보 활성화
        setGeolocationEnabled(true)
        
        // 미디어 재생 설정
        mediaPlaybackRequiresUserGesture = false
        
        // 텍스트 크기 조정
        textZoom = 100  // 기본 100%
        
        // 폰트 크기 설정
        minimumFontSize = 8
        minimumLogicalFontSize = 8
        defaultFontSize = 16
        defaultFixedFontSize = 13
    }
}
```

---

## WebViewClient 사용

**WebViewClient**는 페이지 로딩, 리다이렉션, 에러 처리 등을 제어합니다.

### 기본 WebViewClient

```kotlin
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError

/**
 * 기본 WebViewClient 설정
 */
fun setupWebViewClient() {
    webView.webViewClient = object : WebViewClient() {
        
        /**
         * 페이지 로딩 시작
         */
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d("WebView", "페이지 로딩 시작: $url")
            
            // 로딩 인디케이터 표시
            showLoading()
        }
        
        /**
         * 페이지 로딩 완료
         */
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("WebView", "페이지 로딩 완료: $url")
            
            // 로딩 인디케이터 숨기기
            hideLoading()
        }
        
        /**
         * URL 로딩 가로채기
         * return true: WebView에서 처리 안 함
         * return false: WebView에서 처리함 (기본)
         */
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()
            
            return when {
                // 외부 브라우저로 열기
                url.startsWith("market://") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    true
                }
                
                // 전화 걸기
                url.startsWith("tel:") -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                    startActivity(intent)
                    true
                }
                
                // 이메일
                url.startsWith("mailto:") -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                    startActivity(intent)
                    true
                }
                
                // 일반 URL은 WebView에서 처리
                else -> false
            }
        }
        
        /**
         * 에러 처리
         */
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("WebView", "에러 발생: ${error?.description}")
                
                // 에러 페이지 표시
                showErrorPage(error?.errorCode, error?.description.toString())
            }
        }
        
        /**
         * SSL 에러 처리
         */
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            // ⚠️ 보안 경고: 프로덕션에서는 절대 proceed() 호출하지 말 것!
            
            // 개발 환경에서만 사용
            if (BuildConfig.DEBUG) {
                handler?.proceed()  // SSL 에러 무시
            } else {
                handler?.cancel()  // SSL 에러 시 로딩 중단
                showSslErrorDialog()
            }
        }
    }
}

/**
 * 에러 페이지 표시
 */
fun showErrorPage(errorCode: Int?, description: String) {
    val errorHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    text-align: center;
                    padding: 50px;
                }
                h1 { color: #e74c3c; }
            </style>
        </head>
        <body>
            <h1>페이지를 로드할 수 없습니다</h1>
            <p>에러 코드: $errorCode</p>
            <p>$description</p>
            <button onclick="location.reload()">다시 시도</button>
        </body>
        </html>
    """.trimIndent()
    
    webView.loadData(errorHtml, "text/html", "UTF-8")
}
```

---

## WebChromeClient 사용

**WebChromeClient**는 JavaScript 다이얼로그, 프로그레스, 파일 선택 등을 처리합니다.

```kotlin
import android.webkit.WebChromeClient
import android.webkit.JsResult
import android.webkit.ValueCallback

/**
 * WebChromeClient 설정
 */
fun setupWebChromeClient() {
    webView.webChromeClient = object : WebChromeClient() {
        
        /**
         * 로딩 진행률
         */
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            
            // 프로그레스 바 업데이트
            progressBar.progress = newProgress
            
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
            } else {
                progressBar.visibility = View.VISIBLE
            }
        }
        
        /**
         * 페이지 타이틀 변경
         */
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            
            // 액션바 타이틀 업데이트
            supportActionBar?.title = title
        }
        
        /**
         * JavaScript alert() 처리
         */
        override fun onJsAlert(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            AlertDialog.Builder(this@WebActivity)
                .setTitle("알림")
                .setMessage(message)
                .setPositiveButton("확인") { _, _ ->
                    result?.confirm()
                }
                .setOnCancelListener {
                    result?.cancel()
                }
                .show()
            
            return true  // 직접 처리함
        }
        
        /**
         * JavaScript confirm() 처리
         */
        override fun onJsConfirm(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            AlertDialog.Builder(this@WebActivity)
                .setTitle("확인")
                .setMessage(message)
                .setPositiveButton("확인") { _, _ ->
                    result?.confirm()
                }
                .setNegativeButton("취소") { _, _ ->
                    result?.cancel()
                }
                .setOnCancelListener {
                    result?.cancel()
                }
                .show()
            
            return true
        }
        
        /**
         * JavaScript prompt() 처리
         */
        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            val input = EditText(this@WebActivity).apply {
                setText(defaultValue)
            }
            
            AlertDialog.Builder(this@WebActivity)
                .setTitle("입력")
                .setMessage(message)
                .setView(input)
                .setPositiveButton("확인") { _, _ ->
                    result?.confirm(input.text.toString())
                }
                .setNegativeButton("취소") { _, _ ->
                    result?.cancel()
                }
                .setOnCancelListener {
                    result?.cancel()
                }
                .show()
            
            return true
        }
        
        /**
         * 파일 선택 (Android 5.0+)
         */
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            // 파일 선택 인텐트
            val intent = fileChooserParams?.createIntent()
            
            try {
                fileChooserLauncher.launch(intent)
                this@WebActivity.filePathCallback = filePathCallback
            } catch (e: Exception) {
                filePathCallback?.onReceiveValue(null)
                return false
            }
            
            return true
        }
        
        /**
         * 위치 정보 권한 요청
         */
        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
        ) {
            // 위치 권한 요청
            if (ContextCompat.checkSelfPermission(
                    this@WebActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callback?.invoke(origin, true, false)
            } else {
                // 권한 요청
                ActivityCompat.requestPermissions(
                    this@WebActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}

/**
 * 파일 선택 결과 처리
 */
private val fileChooserLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val uri = result.data?.data
        filePathCallback?.onReceiveValue(arrayOf(uri!!))
    } else {
        filePathCallback?.onReceiveValue(null)
    }
    filePathCallback = null
}
```

---

## 쿠키 관리

```kotlin
import android.webkit.CookieManager

/**
 * 쿠키 관리
 */
class CookieHelper {
    
    private val cookieManager = CookieManager.getInstance()
    
    /**
     * 쿠키 설정
     */
    fun setCookie(url: String, cookie: String) {
        cookieManager.setCookie(url, cookie)
        cookieManager.flush()  // 즉시 저장
    }
    
    /**
     * 쿠키 가져오기
     */
    fun getCookie(url: String): String? {
        return cookieManager.getCookie(url)
    }
    
    /**
     * 모든 쿠키 삭제
     */
    fun removeAllCookies() {
        cookieManager.removeAllCookies { success ->
            Log.d("Cookie", "쿠키 삭제: $success")
        }
    }
    
    /**
     * 세션 쿠키 삭제
     */
    fun removeSessionCookies() {
        cookieManager.removeSessionCookies { success ->
            Log.d("Cookie", "세션 쿠키 삭제: $success")
        }
    }
    
    /**
     * 쿠키 활성화
     */
    fun enableCookies() {
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }
}
```

---

## 파일 다운로드

```kotlin
import android.app.DownloadManager
import android.os.Environment

/**
 * 파일 다운로드 처리
 */
fun setupDownloadListener() {
    webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
        
        // 파일 이름 추출
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        
        // DownloadManager 사용
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType)
            addRequestHeader("User-Agent", userAgent)
            setDescription("파일 다운로드 중...")
            setTitle(fileName)
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        Toast.makeText(this, "다운로드 시작: $fileName", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 보안 고려사항

### 1. JavaScript 인터페이스 보안

```kotlin
// ❌ 위험: 모든 메서드 노출
webView.addJavascriptInterface(MyJavaScriptInterface(), "Android")

// ✅ 안전: @JavascriptInterface 어노테이션만 노출
class SafeJavaScriptInterface {
    @JavascriptInterface
    fun showToast(message: String) {
        // 안전하게 노출된 메서드
    }
    
    // 이 메서드는 JavaScript에서 호출 불가
    fun dangerousMethod() {
        // ...
    }
}
```

### 2. HTTPS 사용

```kotlin
// ✅ 권장: HTTPS 사용
webView.loadUrl("https://secure-site.com")

// ⚠️ 주의: HTTP는 개발 환경에서만
if (BuildConfig.DEBUG) {
    webView.loadUrl("http://localhost:3000")
}
```

### 3. 파일 접근 제한

```kotlin
webView.settings.apply {
    // ✅ 프로덕션: 파일 접근 비활성화
    allowFileAccess = false
    allowFileAccessFromFileURLs = false
    allowUniversalAccessFromFileURLs = false
}
```

---

## Jetpack Compose에서 WebView 사용

```kotlin
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Compose에서 WebView 사용
 */
@Composable
fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}

/**
 * 사용 예시
 */
@Composable
fun WebViewScreen() {
    var url by remember { mutableStateOf("https://www.google.com") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // URL 입력
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // WebView
        ComposeWebView(
            url = url,
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

---

## 다음 단계

다음 문서에서는:
- **JavaScript Bridge**: 앱 ↔ 웹 통신
- **고급 기법**: 캐시 관리, 성능 최적화
- **실전 예제**: 하이브리드 앱 구현

를 다룹니다.

## 참고 자료

- [WebView 공식 문서](https://developer.android.com/reference/android/webkit/WebView)
- [WebSettings 레퍼런스](https://developer.android.com/reference/android/webkit/WebSettings)
