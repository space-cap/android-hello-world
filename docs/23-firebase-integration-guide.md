# Firebase 통합 완벽 가이드

## 📚 목차

1. [Firebase란?](#firebase란)
2. [Firebase 프로젝트 설정](#firebase-프로젝트-설정)
3. [Analytics](#analytics)
4. [Crashlytics](#crashlytics)
5. [Cloud Messaging](#cloud-messaging)
6. [Authentication](#authentication)
7. [Firestore Database](#firestore-database)
8. [Remote Config](#remote-config)
9. [실전 예제](#실전-예제)

---

## Firebase란?

> [!NOTE]
> **Firebase는 Google의 모바일 백엔드 플랫폼입니다**
> 
> 서버 없이도 다음 기능을 사용할 수 있습니다:
> - 📊 사용자 행동 분석 (Analytics)
> - 🐛 크래시 리포팅 (Crashlytics)
> - 📱 푸시 알림 (Cloud Messaging)
> - 🔐 사용자 인증 (Authentication)
> - 💾 실시간 데이터베이스 (Firestore)
> - ⚙️ 원격 설정 (Remote Config)

### 왜 Firebase를 사용하는가?

**서버 개발 없이:**
```
전통적인 방법:
앱 → 백엔드 서버 (개발 필요) → 데이터베이스
     ↓
   시간과 비용 많이 소요

Firebase 사용:
앱 → Firebase (즉시 사용) → 자동 확장
     ↓
   빠르고 저렴함
```

**실제 사용 통계:**
- Google Play 상위 1000개 앱 중 **67%가 Firebase 사용**
- 월간 활성 앱: **300만 개 이상**

---

## Firebase 프로젝트 설정

### 1단계: Firebase 콘솔에서 프로젝트 생성

**매우 상세한 단계:**

1. **Firebase 콘솔 접속**
   - 브라우저에서 https://console.firebase.google.com 접속
   - Google 계정으로 로그인

2. **프로젝트 추가 클릭**
   - "프로젝트 추가" 버튼 클릭
   - 프로젝트 이름 입력 (예: "MyAwesomeApp")
   - 계속 클릭

3. **Google Analytics 설정**
   - "이 프로젝트에 Google Analytics 사용 설정" 체크
   - 계속 클릭
   - Analytics 계정 선택 또는 새로 만들기
   - 프로젝트 만들기 클릭

4. **Android 앱 추가**
   - 프로젝트 개요 페이지에서 Android 아이콘 클릭
   - **Android 패키지 이름** 입력
     ```kotlin
     // build.gradle.kts에서 확인
     android {
         namespace = "com.example.myapp" // 이것!
     }
     ```
   - 앱 닉네임 입력 (선택사항)
   - 앱 등록 클릭

5. **google-services.json 다운로드**
   - `google-services.json` 파일 다운로드
   - **중요:** 이 파일을 `app/` 폴더에 복사
   ```
   MyApp/
   ├── app/
   │   ├── google-services.json  ← 여기!
   │   ├── build.gradle.kts
   │   └── src/
   ```

### 2단계: Gradle 설정

**왜 이렇게 설정하는가?**
- Firebase SDK를 앱에 포함시키기 위함
- `google-services.json` 파일을 빌드 시 읽어서 설정 자동 생성

```kotlin
// build.gradle.kts (Project 레벨)
plugins {
    // Google Services 플러그인 추가
    // 이것이 google-services.json을 읽어서 설정을 생성합니다
    id("com.google.gms.google-services") version "4.4.0" apply false
}

// build.gradle.kts (Module: app)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // 여기에 적용!
}

dependencies {
    // Firebase BOM (Bill of Materials)
    // BOM을 사용하면 모든 Firebase 라이브러리 버전이 호환됩니다
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // 필요한 Firebase 서비스만 추가
    // BOM 사용 시 버전 명시 불필요!
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
}
```

**BOM이란?**
```
BOM 없이:
implementation("com.google.firebase:firebase-analytics:21.5.0")
implementation("com.google.firebase:firebase-crashlytics:18.6.0")
↑ 버전 충돌 가능성!

BOM 사용:
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-crashlytics")
↑ 자동으로 호환되는 버전 사용!
```

---

## Analytics

> [!TIP]
> **Analytics는 사용자 행동을 추적합니다**
> 
> **알 수 있는 것:**
> - 어떤 화면을 가장 많이 보는가?
> - 사용자가 어디서 이탈하는가?
> - 어떤 기능을 많이 사용하는가?
> - 사용자 인구통계 (나이, 성별, 지역)

### 자동 수집 이벤트

Firebase는 **자동으로** 다음을 추적합니다:
- `first_open`: 앱 최초 실행
- `session_start`: 세션 시작
- `screen_view`: 화면 조회
- `app_update`: 앱 업데이트

**설정 불필요! 자동으로 작동합니다.**

### 커스텀 이벤트 로깅

```kotlin
// Firebase Analytics 인스턴스 가져오기
val analytics = Firebase.analytics

// 간단한 이벤트
analytics.logEvent("button_clicked", null)

// 파라미터가 있는 이벤트
analytics.logEvent("purchase") {
    param("item_name", "Premium Subscription")
    param("price", 9.99)
    param("currency", "USD")
}

// 실전 예제: 상품 조회
@Composable
fun ProductDetailScreen(productId: String) {
    val analytics = Firebase.analytics
    
    LaunchedEffect(productId) {
        // 상품 조회 이벤트 로깅
        analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, productId)
            param(FirebaseAnalytics.Param.ITEM_NAME, "Premium Plan")
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "Subscription")
        }
    }
    
    // UI 코드...
}
```

**왜 이렇게 하는가?**
```
이벤트 로깅 → Firebase 서버 전송 → 대시보드에서 분석
                                    ↓
                          "Premium Plan을 100명이 봤는데
                           구매는 10명만 했네? 
                           가격이 비싼가?"
```

### 사용자 속성 설정

```kotlin
// 사용자 속성 설정
analytics.setUserProperty("user_type", "premium")
analytics.setUserProperty("favorite_category", "technology")

// 왜 설정하는가?
// → "premium 사용자는 어떤 기능을 많이 쓰는가?" 분석 가능
```

---

## Crashlytics

> [!IMPORTANT]
> **Crashlytics는 앱 크래시를 자동으로 리포팅합니다**
> 
> **크래시 발생 시:**
> 1. 자동으로 크래시 정보 수집
> 2. Firebase 콘솔로 전송
> 3. 어떤 코드에서 크래시가 났는지 확인 가능
> 4. 몇 명의 사용자가 영향받았는지 확인

### 설정

```kotlin
// build.gradle.kts (Project)
plugins {
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

// build.gradle.kts (Module: app)
plugins {
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics")
}
```

### 사용법

```kotlin
// 자동 크래시 리포팅 (설정 불필요!)
// 앱이 크래시하면 자동으로 리포트됨

// 수동으로 예외 기록
try {
    riskyOperation()
} catch (e: Exception) {
    // Crashlytics에 기록 (앱은 크래시하지 않음)
    FirebaseCrashlytics.getInstance().recordException(e)
}

// 커스텀 키 추가 (디버깅에 유용)
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("user_id", "12345")
    setCustomKey("screen", "ProductDetail")
    setCustomKey("last_action", "purchase_button_clicked")
}

// 로그 추가
FirebaseCrashlytics.getInstance().log("User clicked purchase button")
```

**실제 사용 예:**
```kotlin
@Composable
fun PaymentScreen(viewModel: PaymentViewModel) {
    val crashlytics = FirebaseCrashlytics.getInstance()
    
    LaunchedEffect(Unit) {
        crashlytics.setCustomKey("screen", "Payment")
    }
    
    Button(onClick = {
        crashlytics.log("Payment button clicked")
        
        try {
            viewModel.processPayment()
        } catch (e: Exception) {
            crashlytics.recordException(e)
            // 사용자에게 에러 메시지 표시
        }
    }) {
        Text("결제하기")
    }
}
```

**Firebase 콘솔에서 보이는 것:**
```
크래시 발생!
- 영향받은 사용자: 5명
- 발생 횟수: 12번
- 스택 트레이스:
  PaymentViewModel.processPayment (line 45)
  ↓
  NetworkService.sendRequest (line 123)
  ↓
  IOException: Network timeout

커스텀 키:
- screen: Payment
- user_id: 12345

로그:
- Payment button clicked
```

---

## Cloud Messaging

> [!NOTE]
> **FCM (Firebase Cloud Messaging)은 푸시 알림을 보냅니다**
> 
> **사용 사례:**
> - 새 메시지 알림
> - 프로모션 안내
> - 앱 업데이트 알림
> - 사용자 재참여 유도

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-messaging")
}

// AndroidManifest.xml
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### FCM 서비스 구현

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    // 새 토큰 생성 시 호출
    // 토큰 = 이 기기를 식별하는 고유 ID
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Log.d("FCM", "New token: $token")
        
        // 서버에 토큰 전송 (나중에 이 기기로 알림 보내기 위함)
        sendTokenToServer(token)
    }
    
    // 메시지 수신 시 호출
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // 알림 제목과 내용
        val title = message.notification?.title ?: "새 알림"
        val body = message.notification?.body ?: ""
        
        // 데이터 페이로드 (커스텀 데이터)
        val data = message.data
        
        // 알림 표시
        showNotification(title, body, data)
    }
    
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        // 알림 채널 생성 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "기본 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        // 알림 생성
        val notification = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        // 알림 표시
        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

### 토큰 가져오기

```kotlin
@Composable
fun GetFCMToken() {
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Token: $token")
                
                // 이 토큰을 서버에 저장
                // 나중에 이 기기로 알림을 보낼 수 있음
            }
        }
    }
}
```

### Firebase 콘솔에서 알림 보내기

**초보자를 위한 상세 단계:**

1. Firebase 콘솔 → Cloud Messaging
2. "첫 번째 캠페인 만들기" 클릭
3. "Firebase 알림 메시지" 선택
4. 알림 작성:
   - 제목: "특별 할인!"
   - 텍스트: "지금 50% 할인 중입니다"
5. "테스트 메시지 전송" 클릭
6. FCM 토큰 입력 (위에서 얻은 토큰)
7. "테스트" 클릭
8. 앱에 알림이 표시됨!

---

## Authentication

> [!TIP]
> **Firebase Authentication은 사용자 로그인을 쉽게 만듭니다**
> 
> **지원하는 로그인 방법:**
> - 📧 이메일/비밀번호
> - 📱 전화번호
> - 🔵 Google
> - 📘 Facebook
> - 🐦 Twitter
> - 🍎 Apple

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-auth")
}
```

### 이메일/비밀번호 로그인

```kotlin
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val auth = Firebase.auth
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                
                // Firebase Authentication으로 로그인
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        
                        if (task.isSuccessful) {
                            // 로그인 성공!
                            val user = auth.currentUser
                            Log.d("Auth", "로그인 성공: ${user?.email}")
                        } else {
                            // 로그인 실패
                            errorMessage = task.exception?.message
                        }
                    }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("로그인")
            }
        }
        
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

### 회원가입

```kotlin
fun signUp(email: String, password: String) {
    Firebase.auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 회원가입 성공!
                val user = Firebase.auth.currentUser
                
                // 이메일 인증 메일 보내기
                user?.sendEmailVerification()
            } else {
                // 실패
                Log.e("Auth", "회원가입 실패", task.exception)
            }
        }
}
```

### 로그인 상태 확인

```kotlin
@Composable
fun App() {
    val auth = Firebase.auth
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    
    // 로그인 상태 변경 감지
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        auth.addAuthStateListener(listener)
        
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }
    
    if (currentUser != null) {
        // 로그인됨
        HomeScreen(user = currentUser!!)
    } else {
        // 로그인 안됨
        LoginScreen()
    }
}
```

---

## Firestore Database

> [!IMPORTANT]
> **Firestore는 NoSQL 클라우드 데이터베이스입니다**
> 
> **특징:**
> - 실시간 동기화
> - 오프라인 지원
> - 자동 확장
> - 서버 코드 불필요

### 데이터 구조

```
Firestore는 컬렉션과 문서로 구성됩니다:

users (컬렉션)
├── user1 (문서)
│   ├── name: "John"
│   ├── email: "john@example.com"
│   └── age: 25
├── user2 (문서)
│   ├── name: "Jane"
│   └── email: "jane@example.com"
```

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-firestore")
}
```

### 데이터 쓰기

```kotlin
// Firestore 인스턴스
val db = Firebase.firestore

// 데이터 모델
data class User(
    val name: String = "",
    val email: String = "",
    val age: Int = 0
)

// 데이터 추가
fun addUser(user: User) {
    db.collection("users")
        .add(user)
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "문서 추가됨: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "추가 실패", e)
        }
}

// 특정 ID로 데이터 설정
fun setUser(userId: String, user: User) {
    db.collection("users")
        .document(userId)
        .set(user)
}
```

### 데이터 읽기

```kotlin
// 단일 문서 읽기
fun getUser(userId: String) {
    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject<User>()
                Log.d("Firestore", "사용자: $user")
            }
        }
}

// 모든 문서 읽기
fun getAllUsers() {
    db.collection("users")
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                val user = document.toObject<User>()
                Log.d("Firestore", "사용자: $user")
            }
        }
}

// 실시간 리스너
@Composable
fun UserListScreen() {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    
    DisposableEffect(Unit) {
        val listener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "리스너 에러", error)
                    return@addSnapshotListener
                }
                
                users = snapshot?.documents?.mapNotNull {
                    it.toObject<User>()
                } ?: emptyList()
            }
        
        onDispose {
            listener.remove()
        }
    }
    
    LazyColumn {
        items(users) { user ->
            Text("${user.name} - ${user.email}")
        }
    }
}
```

---

## Remote Config

> [!TIP]
> **Remote Config는 앱을 업데이트하지 않고 설정을 변경합니다**
> 
> **사용 사례:**
> - 기능 플래그 (A/B 테스트)
> - 긴급 공지사항
> - 최소 지원 버전 설정
> - UI 색상/텍스트 변경

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-config")
}
```

### 사용법

```kotlin
@Composable
fun RemoteConfigExample() {
    val remoteConfig = Firebase.remoteConfig
    var welcomeMessage by remember { mutableStateOf("환영합니다!") }
    var showNewFeature by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // 기본값 설정
        remoteConfig.setDefaultsAsync(
            mapOf(
                "welcome_message" to "환영합니다!",
                "show_new_feature" to false
            )
        )
        
        // 서버에서 최신 값 가져오기
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 값 읽기
                    welcomeMessage = remoteConfig.getString("welcome_message")
                    showNewFeature = remoteConfig.getBoolean("show_new_feature")
                }
            }
    }
    
    Column {
        Text(welcomeMessage)
        
        if (showNewFeature) {
            NewFeatureCard()
        }
    }
}
```

**Firebase 콘솔에서 값 변경:**
1. Remote Config → 파라미터 추가
2. `welcome_message`: "특별 이벤트 진행 중!"
3. `show_new_feature`: true
4. 변경사항 게시
5. 앱 재시작 시 새 값 적용!

---

## 💡 Firebase 베스트 프랙티스

### 1. 보안 규칙 설정

```javascript
// Firestore 보안 규칙
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 인증된 사용자만 읽기 가능
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
```

### 2. 오프라인 지원

```kotlin
// Firestore 오프라인 지속성 활성화
Firebase.firestore.firestoreSettings = firestoreSettings {
    isPersistenceEnabled = true
}
```

### 3. 비용 최적화

```kotlin
// 불필요한 읽기 줄이기
db.collection("users")
    .limit(10) // 10개만 가져오기
    .get()
```

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Firebase! 🔥
