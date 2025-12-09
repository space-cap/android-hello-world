# Android 디버깅 기초 가이드

> 📖 **시리즈 구성**
> - **16-1**: Android 디버깅 기초 (현재 문서) - Logcat, Breakpoint, Layout Inspector
> - **16-2**: [Android 디버깅 고급](./16-2-android-debugging-advanced.md) - Database/Network/Background Inspector, ANR, StrictMode
> - **16-3**: [Android 디버깅 실전](./16-3-android-debugging-scenarios.md) - 실전 문제 해결 시나리오

---

## 📚 목차

1. [Logcat 사용법](#logcat-사용법)
2. [Breakpoint 디버깅](#breakpoint-디버깅)
3. [Layout Inspector](#layout-inspector)
4. [Compose Layout Inspector](#compose-layout-inspector)
5. [자주 발생하는 에러](#자주-발생하는-에러)

---

## Logcat 사용법

### 🎯 Logcat이란?

**Logcat**은 Android의 시스템 로그를 실시간으로 확인할 수 있는 도구입니다. 앱의 동작을 추적하고 문제를 진단하는 가장 기본적이면서도 강력한 도구입니다.

```
앱 실행 → 로그 메시지 출력 → Logcat에 표시 → 개발자가 확인
```

### 📝 기본 로깅

```kotlin
import android.util.Log

/**
 * 로깅 예제
 * 
 * TAG: 로그를 필터링할 때 사용하는 식별자
 * 일반적으로 클래스 이름을 사용합니다.
 */
class MainActivity : ComponentActivity() {
    
    companion object {
        // TAG는 companion object에 정의하여 클래스 전체에서 사용
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verbose: 가장 상세한 로그 (개발 중에만 사용)
        Log.v(TAG, "onCreate() 시작")
        
        // Debug: 디버깅 정보 (개발 중에만 사용)
        Log.d(TAG, "사용자 ID: ${getUserId()}")
        
        // Info: 일반 정보 (중요한 이벤트)
        Log.i(TAG, "앱 초기화 완료")
        
        // Warning: 경고 (잠재적 문제)
        Log.w(TAG, "네트워크 연결이 느립니다")
        
        // Error: 에러 (실제 문제 발생)
        Log.e(TAG, "데이터 로드 실패")
    }
}
```

### 📊 로그 레벨 상세

| 레벨 | 메서드 | 용도 | 프로덕션 포함 여부 |
|------|--------|------|-------------------|
| **Verbose** | `Log.v()` | 매우 상세한 정보 (모든 동작) | ❌ 제거 |
| **Debug** | `Log.d()` | 디버깅 정보 (변수 값, 흐름) | ❌ 제거 |
| **Info** | `Log.i()` | 일반 정보 (중요 이벤트) | ✅ 포함 가능 |
| **Warning** | `Log.w()` | 경고 (잠재적 문제) | ✅ 포함 |
| **Error** | `Log.e()` | 에러 (실제 문제) | ✅ 포함 |

> [!TIP]
> **프로덕션 빌드에서는 Verbose와 Debug 로그를 자동으로 제거**하도록 ProGuard/R8을 설정하세요.

### 🔍 예외 로깅

예외가 발생했을 때는 스택 트레이스를 함께 로깅하는 것이 중요합니다.

```kotlin
/**
 * 예외 로깅 예제
 * 
 * 두 번째 파라미터로 Throwable을 전달하면
 * 스택 트레이스가 함께 출력됩니다.
 */
fun loadUserData(userId: String) {
    try {
        // 위험한 작업
        val userData = database.getUser(userId)
        Log.d(TAG, "사용자 데이터 로드 성공: ${userData.name}")
        
    } catch (e: SQLException) {
        // SQL 예외 - 스택 트레이스와 함께 로깅
        Log.e(TAG, "데이터베이스 에러 발생", e)
        
    } catch (e: Exception) {
        // 일반 예외
        Log.e(TAG, "사용자 데이터 로드 실패: userId=$userId", e)
    }
}
```

### 🎨 Logcat 필터링

Android Studio의 Logcat 창에서 로그를 효율적으로 필터링하는 방법:

#### 1. 로그 레벨 필터
```
Verbose: 모든 로그 표시
Debug: Debug 이상만 표시
Info: Info 이상만 표시
Warn: Warning 이상만 표시
Error: Error만 표시
Assert: Assert만 표시
```

#### 2. TAG 필터
```
// Logcat 검색창에 입력
tag:MainActivity        // MainActivity TAG만 표시
tag:MainActivity|Network // MainActivity 또는 Network TAG 표시
```

#### 3. 정규식 필터
```
// 특정 패턴 검색
userId:\d+              // userId: 뒤에 숫자가 오는 로그
Error|Exception         // Error 또는 Exception 포함 로그
```

### 📦 Timber 라이브러리

**Timber**는 Android 로깅을 더 쉽고 안전하게 만들어주는 라이브러리입니다.

#### 설치

**build.gradle.kts (Module: app)**:
```kotlin
dependencies {
    // Timber - 향상된 로깅 라이브러리
    implementation("com.jakewharton.timber:timber:5.0.1")
}
```

#### 초기화

**Application 클래스**:
```kotlin
import android.app.Application
import timber.log.Timber

/**
 * Application 클래스
 * 
 * 앱이 시작될 때 한 번만 실행됩니다.
 * Timber를 여기서 초기화합니다.
 */
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 디버그 빌드에서만 로그 출력
        if (BuildConfig.DEBUG) {
            // DebugTree: 자동으로 TAG를 생성 (클래스 이름 사용)
            Timber.plant(Timber.DebugTree())
        } else {
            // 프로덕션에서는 Crashlytics 등으로 전송
            Timber.plant(CrashlyticsTree())
        }
    }
}
```

**AndroidManifest.xml**에 Application 클래스 등록:
```xml
<application
    android:name=".MyApp"
    ...>
</application>
```

#### 사용법

```kotlin
import timber.log.Timber

class UserViewModel : ViewModel() {
    
    fun loadUser(userId: String) {
        // TAG를 자동으로 생성 (UserViewModel)
        Timber.d("사용자 로드 시작: userId=$userId")
        
        viewModelScope.launch {
            try {
                val user = repository.getUser(userId)
                
                // 문자열 포맷팅 자동 처리
                Timber.i("사용자 로드 완료: %s", user.name)
                
            } catch (e: Exception) {
                // 예외와 메시지를 함께 로깅
                Timber.e(e, "사용자 로드 실패: userId=$userId")
            }
        }
    }
}
```

#### Timber의 장점

1. **자동 TAG 생성**: 클래스 이름을 자동으로 TAG로 사용
2. **프로덕션 안전**: 디버그 빌드에서만 로그 출력
3. **문자열 포맷팅**: String.format() 스타일 지원
4. **커스텀 Tree**: Crashlytics, Firebase 등으로 로그 전송 가능

---

## Breakpoint 디버깅

### 🎯 Breakpoint란?

**Breakpoint**는 코드 실행을 특정 지점에서 일시정지시켜 변수 값과 프로그램 상태를 확인할 수 있게 해주는 도구입니다.

```
코드 실행 → Breakpoint 도달 → 일시정지 → 변수 확인 → 단계별 실행
```

### 📍 기본 Breakpoint 설정

#### 1. Breakpoint 추가
1. 코드 라인 번호 왼쪽 여백 클릭
2. 빨간 점(●)이 표시됨
3. 디버그 모드로 앱 실행 (Shift + F9 또는 디버그 버튼)

#### 2. Breakpoint 제거
- 빨간 점 다시 클릭
- 또는 Ctrl + Shift + F8로 모든 Breakpoint 관리

```kotlin
class ShoppingCartViewModel : ViewModel() {
    
    fun addToCart(product: Product) {
        // 여기에 Breakpoint 설정 (라인 번호 옆 클릭)
        val currentCart = _cartItems.value.toMutableList()
        
        // 실행이 여기서 멈추면 currentCart 값을 확인 가능
        currentCart.add(product)
        
        _cartItems.value = currentCart
        Log.d(TAG, "장바구니에 추가: ${product.name}")
    }
}
```

### 🎛️ 조건부 Breakpoint

특정 조건에서만 실행을 멈추는 Breakpoint입니다.

```kotlin
/**
 * 조건부 Breakpoint 예제
 * 
 * 100개의 아이템 중 특정 아이템에서만 멈추고 싶을 때 유용합니다.
 */
fun processItems(items: List<Item>) {
    items.forEach { item ->
        // Breakpoint 우클릭 → "Edit Breakpoint" 또는 Ctrl + Shift + F8
        // Condition 입력: item.id == 5 || item.price > 10000
        
        processItem(item)
    }
}
```

#### 조건부 Breakpoint 설정 방법
1. Breakpoint 우클릭
2. **"Edit Breakpoint"** 선택
3. **Condition** 체크박스 활성화
4. 조건 입력 (예: `item.id == 5`, `userName == "admin"`)

### 📝 로그 Breakpoint

코드 실행을 멈추지 않고 로그만 출력하는 Breakpoint입니다.

```kotlin
fun calculateDiscount(price: Double, discountRate: Double): Double {
    // 로그 Breakpoint 설정
    // Breakpoint 우클릭 → "Edit Breakpoint"
    // 1. "Suspend" 체크 해제 (실행을 멈추지 않음)
    // 2. "Evaluate and log" 체크
    // 3. 표현식 입력: "Price: $price, Rate: $discountRate"
    
    return price * (1 - discountRate)
}
```

이렇게 하면 코드를 수정하지 않고도 로그를 추가할 수 있습니다!

### ⌨️ 디버거 단축키

| 단축키 | 기능 | 설명 |
|--------|------|------|
| **F8** | Step Over | 현재 줄 실행 후 다음 줄로 이동 |
| **F7** | Step Into | 함수 내부로 들어가기 |
| **Shift + F8** | Step Out | 현재 함수에서 빠져나오기 |
| **F9** | Resume | 다음 Breakpoint까지 실행 |
| **Alt + F9** | Run to Cursor | 커서 위치까지 실행 |
| **Ctrl + F8** | Toggle Breakpoint | Breakpoint 추가/제거 |

### 🔍 변수 검사

Breakpoint에서 멈췄을 때 변수를 검사하는 방법:

#### 1. Variables 창
- 현재 스코프의 모든 변수 표시
- 변수 값 실시간 확인
- 변수 값 수정 가능 (우클릭 → Set Value)

#### 2. Watches 창
```kotlin
fun calculateTotal(items: List<Item>) {
    // Breakpoint에서 멈춤
    val subtotal = items.sumOf { it.price }
    val tax = subtotal * 0.1
    val total = subtotal + tax
    
    // Watches 창에 추가할 표현식:
    // - items.size
    // - items.filter { it.price > 1000 }.size
    // - total.toString()
}
```

#### 3. Evaluate Expression (Alt + F8)
- 임의의 코드를 실행하고 결과 확인
- 예: `items.maxByOrNull { it.price }?.name`

---

## Layout Inspector

### 🎯 Layout Inspector란?

**Layout Inspector**는 실행 중인 앱의 UI 계층 구조를 실시간으로 확인할 수 있는 도구입니다.

```
실행 중인 앱 → Layout Inspector 연결 → UI 계층 확인 → 속성 검사
```

### 🚀 사용 방법

#### 1. Layout Inspector 열기
1. **View → Tool Windows → Layout Inspector**
2. 또는 **Ctrl + Shift + A** → "Layout Inspector" 검색

#### 2. 기기 연결
1. 실행 중인 앱이 있는 기기 선택
2. 프로세스 선택 (앱 패키지 이름)
3. UI 계층 구조가 표시됨

### 🔍 주요 기능

#### 1. 3D 뷰
```
UI를 3D로 회전하여 계층 구조를 시각적으로 확인
- 어떤 View가 다른 View 위에 있는지
- 불필요하게 중첩된 레이아웃이 있는지
```

#### 2. 속성 검사
```kotlin
// 예: Button의 속성 확인
Button(onClick = { /*...*/ }) {
    Text("클릭하세요")
}

// Layout Inspector에서 확인 가능한 속성:
// - 크기: width, height
// - 위치: x, y
// - 패딩: padding
// - 배경색: backgroundColor
// - 텍스트: text, fontSize, fontWeight
```

#### 3. 레이아웃 경계 표시
- 각 컴포넌트의 경계선을 시각적으로 표시
- 패딩과 마진을 구분하여 표시
- 겹치는 영역 확인

### 🎨 실전 활용 예제

```kotlin
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Layout Inspector에서 이 Text의 실제 크기 확인
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 이 Text가 잘리는지 확인
            Text(
                text = product.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

**Layout Inspector로 확인할 사항**:
1. Text가 Card 밖으로 벗어나지 않는지
2. Spacer의 실제 높이가 8dp인지
3. padding이 올바르게 적용되었는지

---

## Compose Layout Inspector

### 🎯 Compose 전용 기능

Jetpack Compose 앱을 위한 특별한 Layout Inspector 기능입니다.

### 🔍 Recomposition 카운트

```kotlin
@Composable
fun UserProfile(user: User) {
    // Layout Inspector에서 이 Composable이
    // 몇 번 recompose 되었는지 확인 가능
    
    Column {
        Text(user.name)  // Recomposition 카운트: 1
        Text(user.email) // Recomposition 카운트: 1
    }
}
```

**Recomposition 카운트가 높다면?**
- 불필요한 재구성이 발생하고 있음
- `remember`, `derivedStateOf` 등으로 최적화 필요

### 📊 Composable 계층 구조

```kotlin
@Composable
fun ShoppingCart() {
    // Layout Inspector에서 계층 구조 확인:
    // ShoppingCart
    //   └─ LazyColumn
    //       ├─ CartItem (id=1)
    //       ├─ CartItem (id=2)
    //       └─ CartItem (id=3)
    
    LazyColumn {
        items(cartItems, key = { it.id }) { item ->
            CartItem(item)
        }
    }
}
```

### 🎨 Modifier 검사

```kotlin
@Composable
fun CustomButton() {
    Button(
        onClick = { /*...*/ },
        modifier = Modifier
            .fillMaxWidth()      // Layout Inspector에서 확인
            .padding(16.dp)      // 실제 적용된 패딩 확인
            .height(56.dp)       // 실제 높이 확인
    ) {
        Text("버튼")
    }
}
```

Layout Inspector의 **Attributes** 패널에서 각 Modifier의 실제 값을 확인할 수 있습니다.

---

## 자주 발생하는 에러

### 1. NullPointerException (NPE)

**원인**: null 값을 가진 변수에 접근

```kotlin
// ❌ 문제 코드
class UserViewModel : ViewModel() {
    fun loadUser(userId: String) {
        val user: User? = repository.getUser(userId)
        
        // user가 null이면 NPE 발생!
        val userName = user.name
    }
}

// ✅ 해결 방법 1: Safe Call (?.)
val userName = user?.name

// ✅ 해결 방법 2: Elvis Operator (?:)
val userName = user?.name ?: "Unknown"

// ✅ 해결 방법 3: let 사용
user?.let { 
    val userName = it.name
    Log.d(TAG, "사용자: $userName")
}

// ✅ 해결 방법 4: requireNotNull (null이면 안 되는 경우)
val userName = requireNotNull(user) { "User must not be null" }.name
```

### 2. IndexOutOfBoundsException

**원인**: 리스트의 범위를 벗어난 인덱스 접근

```kotlin
// ❌ 문제 코드
fun getFirstItem(items: List<Item>): Item {
    // items가 비어있으면 에러!
    return items[0]
}

// ✅ 해결 방법 1: getOrNull
fun getFirstItem(items: List<Item>): Item? {
    return items.getOrNull(0)
}

// ✅ 해결 방법 2: firstOrNull
fun getFirstItem(items: List<Item>): Item? {
    return items.firstOrNull()
}

// ✅ 해결 방법 3: 크기 확인
fun getFirstItem(items: List<Item>): Item? {
    return if (items.isNotEmpty()) items[0] else null
}
```

### 3. ConcurrentModificationException

**원인**: 반복 중인 컬렉션을 수정

```kotlin
// ❌ 문제 코드
fun removeExpiredItems(items: MutableList<Item>) {
    items.forEach { item ->
        if (item.isExpired) {
            items.remove(item) // 에러 발생!
        }
    }
}

// ✅ 해결 방법 1: removeAll 사용
fun removeExpiredItems(items: MutableList<Item>) {
    items.removeAll { it.isExpired }
}

// ✅ 해결 방법 2: filter로 새 리스트 생성
fun removeExpiredItems(items: List<Item>): List<Item> {
    return items.filter { !it.isExpired }
}

// ✅ 해결 방법 3: iterator 사용
fun removeExpiredItems(items: MutableList<Item>) {
    val iterator = items.iterator()
    while (iterator.hasNext()) {
        if (iterator.next().isExpired) {
            iterator.remove()
        }
    }
}
```

### 4. NetworkOnMainThreadException

**원인**: 메인 스레드에서 네트워크 작업 수행

```kotlin
// ❌ 문제 코드
class UserRepository {
    fun getUser(userId: String): User {
        // 메인 스레드에서 네트워크 호출 - 에러!
        return apiService.getUser(userId)
    }
}

// ✅ 해결 방법: Coroutine 사용
class UserRepository {
    // suspend 함수로 변경
    suspend fun getUser(userId: String): User {
        // IO 디스패처에서 실행
        return withContext(Dispatchers.IO) {
            apiService.getUser(userId)
        }
    }
}

// ViewModel에서 사용
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            try {
                // 백그라운드에서 실행됨
                val user = repository.getUser(userId)
                _userState.value = user
            } catch (e: Exception) {
                Log.e(TAG, "사용자 로드 실패", e)
            }
        }
    }
}
```

### 5. Compose Recomposition 문제

**원인**: 재구성마다 새로운 객체 생성

```kotlin
// ❌ 문제 코드: ViewModel이 매번 새로 생성됨
@Composable
fun UserScreen() {
    // 재구성마다 새 ViewModel 생성 - 상태 손실!
    val viewModel = UserViewModel()
    
    // ...
}

// ✅ 해결 방법 1: viewModel() 사용
@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel()
) {
    // viewModel()은 재구성 시에도 같은 인스턴스 유지
    // ...
}

// ✅ 해결 방법 2: remember 사용 (일반 객체)
@Composable
fun UserScreen() {
    val calculator = remember { PriceCalculator() }
    
    // calculator는 재구성되어도 유지됨
}

// ❌ 문제 코드: 람다가 매번 새로 생성됨
@Composable
fun ItemList(items: List<Item>) {
    LazyColumn {
        items(items) { item ->
            // onClick 람다가 매번 새로 생성됨
            ItemRow(
                item = item,
                onClick = { processItem(item) }
            )
        }
    }
}

// ✅ 해결 방법: remember로 람다 캐싱
@Composable
fun ItemList(items: List<Item>) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            val onClick = remember(item.id) {
                { processItem(item) }
            }
            
            ItemRow(
                item = item,
                onClick = onClick
            )
        }
    }
}
```

---

## 💡 디버깅 베스트 프랙티스

### 1. 의미 있는 로그 작성

```kotlin
// ❌ 나쁜 예
Log.d(TAG, "here")
Log.d(TAG, "test")
Log.d(TAG, "1")

// ✅ 좋은 예
Log.d(TAG, "사용자 로그인 시작: email=${user.email}")
Log.d(TAG, "API 응답 수신: statusCode=${response.code}, items=${response.data.size}")
Log.d(TAG, "장바구니 업데이트 완료: 총 ${cart.items.size}개 아이템")
```

### 2. 구체적인 에러 처리

```kotlin
// ❌ 나쁜 예
try {
    loadData()
} catch (e: Exception) {
    Log.e(TAG, "에러 발생")
}

// ✅ 좋은 예
try {
    loadData()
} catch (e: IOException) {
    // 네트워크 에러
    Log.e(TAG, "네트워크 연결 실패: ${e.message}", e)
    showNetworkError()
} catch (e: JsonParseException) {
    // JSON 파싱 에러
    Log.e(TAG, "데이터 형식 오류: ${e.message}", e)
    showDataError()
} catch (e: Exception) {
    // 기타 에러
    Log.e(TAG, "알 수 없는 에러: ${e.message}", e)
    showGenericError()
}
```

### 3. TODO 주석 활용

```kotlin
class ShoppingCartViewModel : ViewModel() {
    
    // TODO: 성능 최적화 필요 - 아이템이 많을 때 느림
    fun calculateTotal(): Double {
        return items.sumOf { it.price }
    }
    
    // FIXME: 동시성 문제 - 여러 스레드에서 접근 시 충돌
    private val _cartItems = mutableListOf<Item>()
    
    // HACK: 임시 해결책 - API 수정 후 제거 예정
    fun parseDate(dateString: String): Date {
        return SimpleDateFormat("yyyy-MM-dd").parse(dateString)!!
    }
}
```

---

## 🎯 다음 단계

기본 디버깅 도구를 마스터했습니다! 다음 단계로:

1. **[16-2. Android 디버깅 고급](./16-2-android-debugging-advanced.md)** - Database/Network Inspector, ANR, StrictMode
2. **[16-3. Android 디버깅 실전](./16-3-android-debugging-scenarios.md)** - 실전 문제 해결 시나리오

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Debugging! 🐛
