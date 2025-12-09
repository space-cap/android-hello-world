# WebView & JavaScript Bridge 문서 작성 완료

## 작업 요약

Android WebView와 JavaScript Bridge에 대한 **초보자 친화적이고 매우 상세한** 문서를 2개로 나누어 작성했습니다.

## 생성된 문서 목록

### 1. [57-android-webview-basics.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/57-android-webview-basics.md) (약 30KB)

**WebView 기본 및 설정**

#### 주요 내용
- ✅ WebView란? (개념 및 사용 사례)
- ✅ WebView 기본 설정 (권한, 초기화)
- ✅ 웹 페이지 로드하기
  - URL 로드
  - HTML 문자열 로드
  - 로컬 파일 로드
- ✅ WebView 설정 옵션
  - 기본 설정
  - 고급 설정
- ✅ WebViewClient 사용
  - 페이지 로딩 제어
  - URL 가로채기
  - 에러 처리
- ✅ WebChromeClient 사용
  - JavaScript 다이얼로그
  - 프로그레스 바
  - 파일 선택
- ✅ 쿠키 관리
- ✅ 파일 다운로드
- ✅ 보안 고려사항
- ✅ Jetpack Compose 통합

---

### 2. [58-android-webview-javascript-bridge.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/58-android-webview-javascript-bridge.md) (약 28KB)

**JavaScript Bridge 및 고급 기법**

#### 주요 내용
- ✅ JavaScript Bridge란?
- ✅ Android → JavaScript 호출
  - 기본 JavaScript 실행
  - 복잡한 데이터 전달
  - 결과 받기
- ✅ JavaScript → Android 호출
  - JavaScript Interface 생성
  - Interface 등록
  - HTML에서 Android 함수 호출
- ✅ 양방향 통신
  - 콜백 패턴
  - Promise 패턴
- ✅ 데이터 전달
  - JSON 객체
  - 파일/이미지 (Base64)
- ✅ 콜백 처리
  - 진행률 콜백
  - 완료 콜백
- ✅ 에러 처리
- ✅ 실전 예제
  - 카메라 연동
  - 위치 정보
- ✅ 성능 최적화
- ✅ 디버깅 (Chrome DevTools)

---

## 문서 통계

| 항목 | 수치 |
|------|------|
| **총 문서 수** | 2개 |
| **총 용량** | 약 58KB |
| **총 라인 수** | 약 1,800줄 |
| **코드 예제** | 40개 이상 |
| **실전 예제** | 8개 이상 |

---

## 주요 학습 포인트

### 1. WebView 기본 설정

```kotlin
webView.settings.apply {
    javaScriptEnabled = true
    domStorageEnabled = true
    cacheMode = WebSettings.LOAD_DEFAULT
}
```

### 2. Android → JavaScript

```kotlin
webView.evaluateJavascript("showUserInfo('홍길동', 30)") { result ->
    Log.d("WebView", "결과: $result")
}
```

### 3. JavaScript → Android

```kotlin
class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

webView.addJavascriptInterface(WebAppInterface(this), "Android")
```

**HTML**:
```javascript
Android.showToast('Hello from JavaScript!');
```

### 4. 양방향 통신

```kotlin
// Android에서 콜백 함수 호출
webView.evaluateJavascript("onDataReceived($jsonData)", null)
```

```javascript
// JavaScript에서 Android 함수 호출 후 콜백
Android.fetchData('onDataReceived');
```

---

## 실전 예제 하이라이트

### 1. 카메라 연동

```kotlin
@JavascriptInterface
fun takePhoto(callbackName: String) {
    // 카메라 실행
    launchCamera()
    
    // 사진 촬영 후
    webView.evaluateJavascript(
        "$callbackName('data:image/jpeg;base64,$base64')",
        null
    )
}
```

### 2. 위치 정보

```kotlin
@JavascriptInterface
fun getCurrentLocation(callbackName: String) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        val data = JSONObject().apply {
            put("latitude", location.latitude)
            put("longitude", location.longitude)
        }
        webView.evaluateJavascript("$callbackName($data)", null)
    }
}
```

### 3. 파일 다운로드

```kotlin
webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
    val request = DownloadManager.Request(Uri.parse(url))
    downloadManager.enqueue(request)
}
```

---

## 학습 경로 추천

### 초급 개발자
1. **57-android-webview-basics.md** 전체 읽기
2. 기본 WebView 설정 실습
3. 간단한 HTML 페이지 로드

### 중급 개발자
1. **58-android-webview-javascript-bridge.md** 학습
2. JavaScript Interface 구현
3. 양방향 통신 실습

### 고급 개발자
1. 복잡한 하이브리드 앱 구현
2. 성능 최적화 적용
3. 보안 강화

---

## 활용 분야

### 📱 하이브리드 앱
- 웹 기술로 개발한 콘텐츠를 앱에 통합
- 네이티브 기능 활용

### 💳 결제 시스템
- 외부 결제 페이지 연동
- PG사 결제 모듈

### 📰 콘텐츠 앱
- 뉴스/블로그 표시
- 동적 콘텐츠 업데이트

### 🎮 웹 게임
- HTML5 게임 통합
- 게임 데이터 저장

---

## 베스트 프랙티스

### 1. 보안

```kotlin
// ✅ @JavascriptInterface만 노출
class SecureInterface {
    @JavascriptInterface
    fun publicMethod() { }
    
    private fun privateMethod() { }
}

// ✅ HTTPS 사용
webView.loadUrl("https://secure-site.com")
```

### 2. 에러 처리

```kotlin
// ✅ 항상 try-catch
@JavascriptInterface
fun safeMethod(data: String): String {
    return try {
        processData(data)
    } catch (e: Exception) {
        createErrorResponse(e)
    }
}
```

### 3. 성능

```kotlin
// ✅ 메시지 큐잉
private val messageQueue = mutableListOf<String>()

// 100ms 후 일괄 전송
handler.postDelayed({ flushMessages() }, 100)
```

---

## 디버깅 팁

### Chrome DevTools 사용

```kotlin
// 디버깅 활성화
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}

// Chrome에서 접속: chrome://inspect
```

### 로깅

```kotlin
@JavascriptInterface
fun debug(tag: String, message: String) {
    Log.d("WebView-$tag", message)
}
```

```javascript
// JavaScript에서 사용
Android.debug('JS', 'Hello from JavaScript');
```

---

## 참고 자료

- [WebView 공식 문서](https://developer.android.com/reference/android/webkit/WebView)
- [JavaScript Interface](https://developer.android.com/guide/webapps/webview#BindingJavaScript)
- [WebView Best Practices](https://developer.android.com/guide/webapps/best-practices)

---

**문서 작성 완료일**: 2024년 12월 1일  
**작성자**: Gemini AI Assistant
