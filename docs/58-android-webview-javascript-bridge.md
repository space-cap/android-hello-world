# Android WebView JavaScript Bridge 가이드

## 목차
1. [JavaScript Bridge란?](#javascript-bridge란)
2. [Android → JavaScript 호출](#android--javascript-호출)
3. [JavaScript → Android 호출](#javascript--android-호출)
4. [양방향 통신](#양방향-통신)
5. [데이터 전달](#데이터-전달)
6. [콜백 처리](#콜백-처리)
7. [에러 처리](#에러-처리)
8. [실전 예제](#실전-예제)
9. [성능 최적화](#성능-최적화)
10. [디버깅](#디버깅)

---

## JavaScript Bridge란?

**JavaScript Bridge**는 Android 앱과 WebView 내의 JavaScript 코드 간에 **양방향 통신**을 가능하게 하는 기술입니다.

### 왜 필요한가요?

```
Android 앱 ←→ JavaScript Bridge ←→ 웹 페이지

예시:
- 앱에서 웹의 함수 호출
- 웹에서 앱의 기능 사용 (카메라, 위치 등)
- 데이터 주고받기
```

### 사용 사례
- 📱 **하이브리드 앱**: 웹 콘텐츠에서 네이티브 기능 사용
- 📷 **카메라/갤러리**: 웹에서 사진 촬영/선택
- 📍 **위치 정보**: 웹에서 GPS 데이터 받기
- 💾 **로컬 저장소**: 웹에서 앱 데이터 저장
- 🔔 **푸시 알림**: 웹에서 알림 요청

---

## Android → JavaScript 호출

### 1. 기본 JavaScript 실행

```kotlin
/**
 * Android에서 JavaScript 함수 호출
 */
fun callJavaScript() {
    // JavaScript 코드 실행
    webView.evaluateJavascript(
        "alert('Hello from Android!')",
        null  // 결과 콜백 (필요 없으면 null)
    )
}

/**
 * JavaScript 함수 호출 (파라미터 전달)
 */
fun callJavaScriptFunction(name: String, age: Int) {
    val script = "showUserInfo('$name', $age)"
    
    webView.evaluateJavascript(script, null)
}

/**
 * JavaScript 함수 호출 (결과 받기)
 */
fun getJavaScriptResult() {
    webView.evaluateJavascript(
        "getUserName()"
    ) { result ->
        // result는 JSON 문자열로 반환됨
        Log.d("WebView", "결과: $result")
        
        // 따옴표 제거
        val userName = result.trim('"')
        Toast.makeText(this, "사용자: $userName", Toast.LENGTH_SHORT).show()
    }
}
```

### 2. 복잡한 데이터 전달

```kotlin
import org.json.JSONObject

/**
 * JSON 데이터를 JavaScript로 전달
 */
fun sendDataToJavaScript() {
    // 데이터 준비
    val userData = JSONObject().apply {
        put("name", "홍길동")
        put("age", 30)
        put("email", "hong@example.com")
    }
    
    // JavaScript 함수 호출
    val script = "receiveUserData($userData)"
    webView.evaluateJavascript(script, null)
}

/**
 * 배열 데이터 전달
 */
fun sendArrayToJavaScript() {
    val items = listOf("사과", "바나나", "오렌지")
    val jsonArray = JSONArray(items)
    
    val script = "displayItems($jsonArray)"
    webView.evaluateJavascript(script, null)
}
```

### 3. HTML 파일 예제

**assets/index.html**:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JavaScript Bridge</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
        }
        .info {
            background: #e3f2fd;
            padding: 15px;
            border-radius: 5px;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <h1>JavaScript Bridge 예제</h1>
    
    <div id="userInfo" class="info">
        사용자 정보가 여기에 표시됩니다.
    </div>
    
    <script>
        // Android에서 호출할 JavaScript 함수
        function showUserInfo(name, age) {
            const div = document.getElementById('userInfo');
            div.innerHTML = `
                <h3>사용자 정보</h3>
                <p>이름: ${name}</p>
                <p>나이: ${age}</p>
            `;
        }
        
        // 사용자 이름 반환
        function getUserName() {
            return "홍길동";
        }
        
        // JSON 데이터 수신
        function receiveUserData(data) {
            const div = document.getElementById('userInfo');
            div.innerHTML = `
                <h3>사용자 정보</h3>
                <p>이름: ${data.name}</p>
                <p>나이: ${data.age}</p>
                <p>이메일: ${data.email}</p>
            `;
        }
        
        // 배열 데이터 수신
        function displayItems(items) {
            const div = document.getElementById('userInfo');
            const listHtml = items.map(item => `<li>${item}</li>`).join('');
            div.innerHTML = `
                <h3>항목 목록</h3>
                <ul>${listHtml}</ul>
            `;
        }
    </script>
</body>
</html>
```

---

## JavaScript → Android 호출

### 1. JavaScript Interface 생성

```kotlin
import android.webkit.JavascriptInterface
import android.widget.Toast

/**
 * JavaScript에서 호출할 수 있는 Android 인터페이스
 */
class WebAppInterface(private val context: Context) {
    
    /**
     * Toast 메시지 표시
     * @JavascriptInterface 어노테이션 필수!
     */
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 로그 출력
     */
    @JavascriptInterface
    fun log(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    /**
     * 데이터 저장
     */
    @JavascriptInterface
    fun saveData(key: String, value: String) {
        val prefs = context.getSharedPreferences("WebViewData", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }
    
    /**
     * 데이터 불러오기
     */
    @JavascriptInterface
    fun loadData(key: String): String {
        val prefs = context.getSharedPreferences("WebViewData", Context.MODE_PRIVATE)
        return prefs.getString(key, "") ?: ""
    }
    
    /**
     * 액티비티 종료
     */
    @JavascriptInterface
    fun closeApp() {
        (context as? Activity)?.finish()
    }
    
    /**
     * JSON 데이터 수신
     */
    @JavascriptInterface
    fun receiveData(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val name = json.getString("name")
            val age = json.getInt("age")
            
            Log.d("WebView", "이름: $name, 나이: $age")
        } catch (e: Exception) {
            Log.e("WebView", "JSON 파싱 에러", e)
        }
    }
}
```

### 2. Interface 등록

```kotlin
/**
 * JavaScript Interface 등록
 */
fun setupJavaScriptInterface() {
    // JavaScript에서 "Android" 이름으로 접근 가능
    webView.addJavascriptInterface(
        WebAppInterface(this),
        "Android"  // JavaScript에서 사용할 이름
    )
}
```

### 3. HTML에서 Android 함수 호출

**assets/index.html**:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Android 호출</title>
</head>
<body>
    <h1>Android 함수 호출</h1>
    
    <!-- Toast 표시 -->
    <button onclick="showToastMessage()">Toast 표시</button>
    
    <!-- 데이터 저장 -->
    <input type="text" id="dataInput" placeholder="데이터 입력">
    <button onclick="saveToAndroid()">저장</button>
    
    <!-- 데이터 불러오기 -->
    <button onclick="loadFromAndroid()">불러오기</button>
    
    <!-- 앱 종료 -->
    <button onclick="closeApplication()">앱 종료</button>
    
    <script>
        // Toast 표시
        function showToastMessage() {
            Android.showToast('Hello from JavaScript!');
        }
        
        // 데이터 저장
        function saveToAndroid() {
            const data = document.getElementById('dataInput').value;
            Android.saveData('myData', data);
            Android.showToast('데이터 저장됨');
        }
        
        // 데이터 불러오기
        function loadFromAndroid() {
            const data = Android.loadData('myData');
            document.getElementById('dataInput').value = data;
            Android.showToast('데이터 불러옴: ' + data);
        }
        
        // 앱 종료
        function closeApplication() {
            if (confirm('앱을 종료하시겠습니까?')) {
                Android.closeApp();
            }
        }
        
        // JSON 데이터 전송
        function sendDataToAndroid() {
            const data = {
                name: '홍길동',
                age: 30,
                email: 'hong@example.com'
            };
            
            Android.receiveData(JSON.stringify(data));
        }
    </script>
</body>
</html>
```

---

## 양방향 통신

### 1. 콜백 패턴

```kotlin
/**
 * 콜백을 사용한 양방향 통신
 */
class CallbackInterface(private val context: Context) {
    
    /**
     * 비동기 작업 후 JavaScript 콜백 호출
     */
    @JavascriptInterface
    fun getUserInfo(callbackName: String) {
        // 백그라운드에서 데이터 가져오기
        Thread {
            Thread.sleep(1000)  // 네트워크 요청 시뮬레이션
            
            val userData = JSONObject().apply {
                put("name", "홍길동")
                put("age", 30)
            }
            
            // 메인 스레드에서 JavaScript 콜백 호출
            (context as? Activity)?.runOnUiThread {
                val webView = (context as? WebActivity)?.webView
                webView?.evaluateJavascript(
                    "$callbackName($userData)",
                    null
                )
            }
        }.start()
    }
}
```

**HTML**:
```html
<script>
    // Android 함수 호출 (콜백 함수 이름 전달)
    function requestUserInfo() {
        Android.getUserInfo('onUserInfoReceived');
    }
    
    // 콜백 함수
    function onUserInfoReceived(data) {
        console.log('사용자 정보:', data);
        document.getElementById('result').innerHTML = `
            <p>이름: ${data.name}</p>
            <p>나이: ${data.age}</p>
        `;
    }
</script>

<button onclick="requestUserInfo()">사용자 정보 요청</button>
<div id="result"></div>
```

### 2. Promise 패턴

```kotlin
/**
 * Promise 스타일 통신
 */
class PromiseInterface(private val context: Context, private val webView: WebView) {
    
    private var requestId = 0
    
    @JavascriptInterface
    fun fetchData(requestIdStr: String) {
        Thread {
            // 데이터 가져오기
            val result = JSONObject().apply {
                put("success", true)
                put("data", "결과 데이터")
            }
            
            (context as? Activity)?.runOnUiThread {
                webView.evaluateJavascript(
                    "resolvePromise($requestIdStr, $result)",
                    null
                )
            }
        }.start()
    }
}
```

**HTML**:
```html
<script>
    let requestId = 0;
    const pendingPromises = {};
    
    // Promise 래퍼
    function fetchDataFromAndroid() {
        return new Promise((resolve, reject) => {
            const id = requestId++;
            pendingPromises[id] = { resolve, reject };
            
            Android.fetchData(id.toString());
        });
    }
    
    // Promise 해결
    function resolvePromise(id, result) {
        const promise = pendingPromises[id];
        if (promise) {
            if (result.success) {
                promise.resolve(result.data);
            } else {
                promise.reject(result.error);
            }
            delete pendingPromises[id];
        }
    }
    
    // 사용 예시
    async function loadData() {
        try {
            const data = await fetchDataFromAndroid();
            console.log('데이터:', data);
        } catch (error) {
            console.error('에러:', error);
        }
    }
</script>
```

---

## 데이터 전달

### 1. 복잡한 객체 전달

```kotlin
/**
 * 복잡한 데이터 구조 전달
 */
class DataInterface(private val context: Context) {
    
    @JavascriptInterface
    fun getComplexData(): String {
        val data = JSONObject().apply {
            put("user", JSONObject().apply {
                put("id", 1)
                put("name", "홍길동")
                put("email", "hong@example.com")
            })
            
            put("items", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1)
                    put("title", "아이템 1")
                })
                put(JSONObject().apply {
                    put("id", 2)
                    put("title", "아이템 2")
                })
            })
            
            put("settings", JSONObject().apply {
                put("theme", "dark")
                put("notifications", true)
            })
        }
        
        return data.toString()
    }
}
```

**HTML**:
```html
<script>
    function loadComplexData() {
        const jsonString = Android.getComplexData();
        const data = JSON.parse(jsonString);
        
        console.log('사용자:', data.user);
        console.log('아이템:', data.items);
        console.log('설정:', data.settings);
    }
</script>
```

### 2. 파일 데이터 전달

```kotlin
/**
 * Base64로 인코딩된 이미지 전달
 */
class FileInterface(private val context: Context) {
    
    @JavascriptInterface
    fun getImageBase64(): String {
        val bitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.sample_image
        )
        
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
```

**HTML**:
```html
<script>
    function loadImage() {
        const base64 = Android.getImageBase64();
        const img = document.getElementById('image');
        img.src = 'data:image/png;base64,' + base64;
    }
</script>

<img id="image" alt="이미지">
<button onclick="loadImage()">이미지 로드</button>
```

---

## 콜백 처리

### 고급 콜백 시스템

```kotlin
/**
 * 고급 콜백 시스템
 */
class AdvancedCallbackInterface(
    private val context: Context,
    private val webView: WebView
) {
    
    /**
     * 진행률과 함께 비동기 작업
     */
    @JavascriptInterface
    fun downloadFile(url: String, progressCallback: String, completeCallback: String) {
        Thread {
            for (i in 0..100 step 10) {
                Thread.sleep(200)
                
                // 진행률 콜백
                (context as? Activity)?.runOnUiThread {
                    webView.evaluateJavascript(
                        "$progressCallback($i)",
                        null
                    )
                }
            }
            
            // 완료 콜백
            (context as? Activity)?.runOnUiThread {
                webView.evaluateJavascript(
                    "$completeCallback('다운로드 완료')",
                    null
                )
            }
        }.start()
    }
}
```

**HTML**:
```html
<script>
    function startDownload() {
        Android.downloadFile(
            'https://example.com/file.zip',
            'onProgress',
            'onComplete'
        );
    }
    
    function onProgress(percent) {
        document.getElementById('progress').style.width = percent + '%';
        document.getElementById('progressText').textContent = percent + '%';
    }
    
    function onComplete(message) {
        alert(message);
    }
</script>

<div style="width: 100%; background: #eee; height: 30px;">
    <div id="progress" style="width: 0%; background: #4CAF50; height: 100%;"></div>
</div>
<div id="progressText">0%</div>
<button onclick="startDownload()">다운로드 시작</button>
```

---

## 에러 처리

```kotlin
/**
 * 에러 처리가 포함된 인터페이스
 */
class SafeInterface(private val context: Context) {
    
    @JavascriptInterface
    fun safeOperation(data: String): String {
        return try {
            val json = JSONObject(data)
            
            // 작업 수행
            val result = performOperation(json)
            
            // 성공 응답
            JSONObject().apply {
                put("success", true)
                put("data", result)
            }.toString()
            
        } catch (e: JSONException) {
            // JSON 파싱 에러
            JSONObject().apply {
                put("success", false)
                put("error", "Invalid JSON format")
                put("message", e.message)
            }.toString()
            
        } catch (e: Exception) {
            // 기타 에러
            JSONObject().apply {
                put("success", false)
                put("error", "Operation failed")
                put("message", e.message)
            }.toString()
        }
    }
    
    private fun performOperation(json: JSONObject): String {
        // 실제 작업 수행
        return "작업 완료"
    }
}
```

**HTML**:
```html
<script>
    function callSafeOperation() {
        const data = {
            action: 'save',
            value: 'test data'
        };
        
        const resultJson = Android.safeOperation(JSON.stringify(data));
        const result = JSON.parse(resultJson);
        
        if (result.success) {
            console.log('성공:', result.data);
        } else {
            console.error('에러:', result.error, result.message);
        }
    }
</script>
```

---

## 실전 예제

### 1. 카메라 연동

```kotlin
/**
 * 카메라 인터페이스
 */
class CameraInterface(private val activity: Activity) {
    
    private var photoCallback: String? = null
    
    @JavascriptInterface
    fun takePhoto(callbackName: String) {
        photoCallback = callbackName
        
        // 카메라 권한 확인
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }
    
    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }
    
    fun onPhotoTaken(bitmap: Bitmap) {
        // 이미지를 Base64로 변환
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64 = Base64.encodeToString(
            outputStream.toByteArray(),
            Base64.NO_WRAP
        )
        
        // JavaScript 콜백 호출
        val webView = (activity as? WebActivity)?.webView
        webView?.evaluateJavascript(
            "$photoCallback('data:image/jpeg;base64,$base64')",
            null
        )
    }
}
```

**HTML**:
```html
<script>
    function capturePhoto() {
        Android.takePhoto('onPhotoReceived');
    }
    
    function onPhotoReceived(base64Image) {
        const img = document.getElementById('photo');
        img.src = base64Image;
        img.style.display = 'block';
    }
</script>

<button onclick="capturePhoto()">사진 촬영</button>
<img id="photo" style="display:none; max-width:100%;">
```

### 2. 위치 정보

```kotlin
/**
 * 위치 정보 인터페이스
 */
class LocationInterface(private val context: Context) {
    
    @JavascriptInterface
    fun getCurrentLocation(callbackName: String) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val locationData = JSONObject().apply {
                        put("latitude", it.latitude)
                        put("longitude", it.longitude)
                        put("accuracy", it.accuracy)
                    }
                    
                    val webView = (context as? WebActivity)?.webView
                    webView?.evaluateJavascript(
                        "$callbackName($locationData)",
                        null
                    )
                }
            }
        }
    }
}
```

---

## 성능 최적화

### 1. 메시지 큐잉

```kotlin
/**
 * 메시지를 큐에 모아서 한번에 전송
 */
class OptimizedInterface(private val webView: WebView) {
    
    private val messageQueue = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    
    @JavascriptInterface
    fun queueMessage(message: String) {
        synchronized(messageQueue) {
            messageQueue.add(message)
        }
        
        // 100ms 후에 일괄 전송
        handler.removeCallbacks(flushRunnable)
        handler.postDelayed(flushRunnable, 100)
    }
    
    private val flushRunnable = Runnable {
        synchronized(messageQueue) {
            if (messageQueue.isNotEmpty()) {
                val messages = JSONArray(messageQueue)
                messageQueue.clear()
                
                webView.evaluateJavascript(
                    "processMessages($messages)",
                    null
                )
            }
        }
    }
}
```

### 2. 데이터 압축

```kotlin
/**
 * 큰 데이터를 압축하여 전송
 */
class CompressedDataInterface {
    
    @JavascriptInterface
    fun getLargeData(): String {
        val largeData = generateLargeData()
        
        // GZIP 압축
        val compressed = compress(largeData)
        
        // Base64 인코딩
        return Base64.encodeToString(compressed, Base64.NO_WRAP)
    }
    
    private fun compress(data: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzip ->
            gzip.write(data.toByteArray())
        }
        return outputStream.toByteArray()
    }
}
```

---

## 디버깅

### 1. Chrome DevTools 사용

```kotlin
/**
 * WebView 디버깅 활성화
 */
fun enableWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

// Chrome에서 접속: chrome://inspect
```

### 2. 로깅

```kotlin
/**
 * 디버그 로깅
 */
class DebugInterface {
    
    @JavascriptInterface
    fun debug(tag: String, message: String) {
        Log.d("WebView-$tag", message)
    }
    
    @JavascriptInterface
    fun error(tag: String, message: String, stack: String) {
        Log.e("WebView-$tag", "$message\n$stack")
    }
}
```

**HTML**:
```html
<script>
    // 콘솔 로그를 Android로 전송
    console.log = function(message) {
        Android.debug('JS', message);
    };
    
    console.error = function(message) {
        const stack = new Error().stack;
        Android.error('JS', message, stack);
    };
</script>
```

---

## 베스트 프랙티스

### 1. 보안

```kotlin
// ✅ 좋은 예: @JavascriptInterface만 노출
class SecureInterface {
    @JavascriptInterface
    fun publicMethod() { }
    
    private fun privateMethod() { }  // JavaScript에서 접근 불가
}

// ❌ 나쁜 예: 모든 메서드 노출
class UnsafeInterface {
    fun dangerousMethod() { }  // 보안 위험!
}
```

### 2. 에러 처리

```kotlin
// ✅ 항상 try-catch 사용
@JavascriptInterface
fun safeMethod(data: String): String {
    return try {
        processData(data)
    } catch (e: Exception) {
        createErrorResponse(e)
    }
}
```

### 3. 스레드 안전성

```kotlin
// ✅ UI 작업은 메인 스레드에서
@JavascriptInterface
fun updateUI() {
    (context as? Activity)?.runOnUiThread {
        // UI 업데이트
    }
}
```

---

## 참고 자료

- [WebView JavaScript Interface](https://developer.android.com/guide/webapps/webview#BindingJavaScript)
- [WebView Best Practices](https://developer.android.com/guide/webapps/best-practices)
