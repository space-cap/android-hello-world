# 40. Kotlin Coroutines & Flow 완벽 가이드

## 📚 목차
1. [Coroutines란?](#coroutines란)
2. [왜 Coroutines가 필요한가?](#왜-coroutines가-필요한가)
3. [Coroutines 기초](#coroutines-기초)
4. [Suspend Functions](#suspend-functions)
5. [Coroutine Scope와 Context](#coroutine-scope와-context)
6. [Flow 소개](#flow-소개)
7. [StateFlow vs SharedFlow](#stateflow-vs-sharedflow)
8. [Flow Operators](#flow-operators)
9. [에러 처리와 취소](#에러-처리와-취소)
10. [실전 예제](#실전-예제)

---

## Coroutines란?

### 🤔 쉬운 설명

**Coroutines**는 비동기 작업을 쉽게 처리할 수 있게 해주는 Kotlin의 기능입니다.

#### 일상 생활의 비유

```
❌ 동기 방식 (Blocking):
빨래를 돌린다
→ 빨래가 끝날 때까지 세탁기 앞에서 기다린다 (30분 낭비)
→ 빨래가 끝나면 요리를 시작한다
→ 요리가 끝날 때까지 기다린다 (1시간 낭비)

✅ 비동기 방식 (Non-blocking with Coroutines):
빨래를 돌린다
→ 빨래가 돌아가는 동안 요리를 시작한다 (동시에 진행)
→ 빨래가 끝나면 알림을 받는다
→ 요리가 끝나면 알림을 받는다
→ 시간 절약!
```

### 코드로 비교하기

#### ❌ 동기 방식 (UI가 멈춤)

```kotlin
// 메인 스레드에서 실행 - UI가 멈춘다!
fun loadData() {
    val data = downloadFromServer() // 5초 걸림 - UI 멈춤!
    showData(data)
}
```

#### ✅ Coroutines 사용 (UI가 부드럽게 동작)

```kotlin
// Coroutine으로 실행 - UI는 계속 동작
fun loadData() {
    // viewModelScope: ViewModel이 살아있는 동안만 실행
    viewModelScope.launch {
        // suspend 함수: 다른 작업을 기다릴 수 있다
        val data = downloadFromServer() // 5초 걸려도 UI 안 멈춤!
        showData(data)
    }
}
```

---

## 왜 Coroutines가 필요한가?

### Android에서 비동기 작업이 필요한 경우

1. **네트워크 요청** - 서버에서 데이터 가져오기
2. **데이터베이스 작업** - 로컬 DB 읽기/쓰기
3. **파일 입출력** - 큰 파일 읽기/쓰기
4. **이미지 처리** - 이미지 크기 조정, 필터 적용
5. **복잡한 계산** - 무거운 연산 작업

### Coroutines의 장점

✅ **간단한 코드**: 동기 코드처럼 작성하지만 비동기로 동작  
✅ **가벼움**: 수천 개의 Coroutine을 동시에 실행 가능  
✅ **취소 가능**: 필요 없어지면 쉽게 취소  
✅ **구조화된 동시성**: 메모리 누수 방지  
✅ **예외 처리**: try-catch로 간단하게 처리

### 다른 방법과 비교

```kotlin
// 1. 콜백 방식 (Callback Hell - 복잡함)
downloadData(url, object : Callback {
    override fun onSuccess(data: Data) {
        processData(data, object : Callback {
            override fun onSuccess(result: Result) {
                saveToDatabase(result, object : Callback {
                    override fun onSuccess() {
                        // 콜백 지옥!
                    }
                })
            }
        })
    }
})

// 2. Coroutines (간단하고 읽기 쉬움)
viewModelScope.launch {
    val data = downloadData(url)
    val result = processData(data)
    saveToDatabase(result)
    // 순차적으로 읽기 쉽다!
}
```

---

## Coroutines 기초

### 1️⃣ 첫 번째 Coroutine

```kotlin
import kotlinx.coroutines.*

fun main() {
    // GlobalScope: 앱 전체 생명주기 동안 실행
    // launch: 새로운 Coroutine 시작
    GlobalScope.launch {
        // delay: 일시 중지 (다른 작업 가능)
        delay(1000) // 1초 대기
        println("World!")
    }
    
    println("Hello,")
    
    // 메인 스레드가 종료되지 않도록 대기
    Thread.sleep(2000)
}

// 출력:
// Hello,
// World! (1초 후)
```

### 2️⃣ runBlocking - 테스트용

```kotlin
fun main() = runBlocking {
    // runBlocking: Coroutine이 끝날 때까지 현재 스레드를 차단
    // 주로 main 함수나 테스트에서 사용
    
    launch {
        delay(1000)
        println("World!")
    }
    
    println("Hello,")
    
    // runBlocking은 내부 Coroutine이 모두 끝날 때까지 기다린다
}
```

### 3️⃣ launch vs async

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    
    // launch: 결과를 반환하지 않는다 (Job 반환)
    // "실행만 하고 결과는 필요 없어"
    val job: Job = launch {
        delay(1000)
        println("Launch 완료")
    }
    
    // async: 결과를 반환한다 (Deferred 반환)
    // "실행하고 결과를 받을 거야"
    val deferred: Deferred<String> = async {
        delay(1000)
        "Async 결과" // 반환값
    }
    
    // await(): async의 결과를 기다린다
    val result = deferred.await()
    println(result) // "Async 결과"
}
```

### 4️⃣ 여러 작업 동시 실행

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    
    // 순차 실행 (느림)
    val time1 = measureTimeMillis {
        val one = doSomethingUsefulOne() // 1초
        val two = doSomethingUsefulTwo() // 1초
        println("결과: ${one + two}") // 총 2초
    }
    println("순차 실행: $time1 ms")
    
    // 동시 실행 (빠름)
    val time2 = measureTimeMillis {
        // 두 작업을 동시에 시작
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        
        // 두 결과를 기다린다
        println("결과: ${one.await() + two.await()}") // 총 1초
    }
    println("동시 실행: $time2 ms")
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000) // 1초 걸리는 작업
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000) // 1초 걸리는 작업
    return 29
}
```

---

## Suspend Functions

### Suspend 함수란?

**suspend** 키워드가 붙은 함수는 "일시 중지 가능한 함수"입니다.

```kotlin
// suspend 키워드: 이 함수는 Coroutine 안에서만 호출 가능
suspend fun fetchUser(): User {
    // delay는 suspend 함수
    delay(1000) // 1초 대기 (다른 작업 가능)
    return User("John")
}

// 일반 함수에서는 호출 불가
fun normalFunction() {
    fetchUser() // ❌ 컴파일 에러!
}

// Coroutine 안에서만 호출 가능
fun coroutineFunction() {
    viewModelScope.launch {
        val user = fetchUser() // ✅ OK!
    }
}
```

### Suspend 함수 만들기

```kotlin
import kotlinx.coroutines.*

/**
 * 사용자 정보를 가져오는 suspend 함수
 * 
 * suspend 키워드를 붙이면:
 * 1. Coroutine 안에서만 호출 가능
 * 2. 다른 suspend 함수를 호출할 수 있다
 * 3. 일시 중지/재개가 가능하다
 */
suspend fun getUserFromServer(userId: String): User {
    // withContext: 다른 스레드에서 실행
    // Dispatchers.IO: 네트워크/파일 작업에 적합한 스레드
    return withContext(Dispatchers.IO) {
        // 네트워크 요청 (예시)
        delay(2000) // 2초 걸리는 작업
        
        // 결과 반환
        User(
            id = userId,
            name = "John Doe",
            email = "john@example.com"
        )
    }
}

/**
 * 여러 suspend 함수를 조합
 */
suspend fun getUserProfile(userId: String): UserProfile {
    // 순차적으로 실행
    val user = getUserFromServer(userId)
    val posts = getUserPosts(userId)
    val friends = getUserFriends(userId)
    
    return UserProfile(user, posts, friends)
}

/**
 * 병렬로 실행하여 속도 향상
 */
suspend fun getUserProfileFast(userId: String): UserProfile {
    // coroutineScope: 모든 자식 Coroutine이 끝날 때까지 기다린다
    return coroutineScope {
        // 세 작업을 동시에 시작
        val userDeferred = async { getUserFromServer(userId) }
        val postsDeferred = async { getUserPosts(userId) }
        val friendsDeferred = async { getUserFriends(userId) }
        
        // 모든 결과를 기다린다
        UserProfile(
            user = userDeferred.await(),
            posts = postsDeferred.await(),
            friends = friendsDeferred.await()
        )
    }
}

data class User(val id: String, val name: String, val email: String)
data class UserProfile(val user: User, val posts: List<String>, val friends: List<String>)

suspend fun getUserPosts(userId: String): List<String> {
    delay(1000)
    return listOf("Post 1", "Post 2")
}

suspend fun getUserFriends(userId: String): List<String> {
    delay(1000)
    return listOf("Friend 1", "Friend 2")
}
```

---

## Coroutine Scope와 Context

### Scope란?

**Scope**는 Coroutine의 생명주기를 관리합니다.

```kotlin
// Android에서 자주 사용하는 Scope

// 1. viewModelScope
// - ViewModel이 살아있는 동안만 실행
// - ViewModel이 제거되면 자동으로 취소
class MyViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch {
            // ViewModel이 제거되면 자동 취소
        }
    }
}

// 2. lifecycleScope
// - Activity/Fragment의 생명주기 동안 실행
// - Activity가 종료되면 자동으로 취소
class MyActivity : AppCompatActivity() {
    fun loadData() {
        lifecycleScope.launch {
            // Activity가 종료되면 자동 취소
        }
    }
}

// 3. GlobalScope (사용 주의!)
// - 앱 전체 생명주기 동안 실행
// - 자동으로 취소되지 않음 (메모리 누수 위험)
GlobalScope.launch {
    // 앱이 종료될 때까지 실행
}
```

### Context란?

**Context**는 Coroutine이 어떤 스레드에서 실행될지 결정합니다.

```kotlin
import kotlinx.coroutines.*

fun example() = runBlocking {
    
    // Dispatchers.Main
    // - UI 스레드에서 실행
    // - UI 업데이트할 때 사용
    launch(Dispatchers.Main) {
        // UI 업데이트
        textView.text = "Loading..."
    }
    
    // Dispatchers.IO
    // - 네트워크, 파일, 데이터베이스 작업에 적합
    // - 많은 스레드 사용 가능
    launch(Dispatchers.IO) {
        // 네트워크 요청
        val data = downloadData()
    }
    
    // Dispatchers.Default
    // - CPU 집약적 작업에 적합 (계산, 정렬 등)
    // - CPU 코어 수만큼 스레드 사용
    launch(Dispatchers.Default) {
        // 복잡한 계산
        val result = complexCalculation()
    }
}
```

### withContext로 스레드 전환

```kotlin
import kotlinx.coroutines.*

/**
 * 스레드를 전환하면서 작업 수행
 */
suspend fun loadDataAndUpdateUI() {
    // IO 스레드에서 데이터 로드
    val data = withContext(Dispatchers.IO) {
        // 네트워크 요청 (IO 스레드)
        downloadData()
    }
    
    // 자동으로 Main 스레드로 돌아옴
    // UI 업데이트 (Main 스레드)
    updateUI(data)
}

/**
 * 실제 사용 예제
 */
class UserViewModel : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            // Main 스레드에서 시작
            
            // IO 스레드로 전환하여 데이터 로드
            val userData = withContext(Dispatchers.IO) {
                // 네트워크 요청
                apiService.getUser(userId)
            }
            
            // 자동으로 Main 스레드로 돌아옴
            // StateFlow 업데이트 (Main 스레드)
            _user.value = userData
        }
    }
}
```

---

## Flow 소개

### Flow란?

**Flow**는 여러 값을 순차적으로 방출(emit)하는 비동기 스트림입니다.

#### 비유

```
일반 함수: 사과 하나를 반환
suspend 함수: 사과 하나를 비동기로 반환
Flow: 사과를 하나씩 계속 보내주는 컨베이어 벨트
```

### Flow 기본 사용법

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Flow 생성하기
 */
fun simpleFlow(): Flow<Int> = flow {
    // flow 빌더: Flow를 만든다
    println("Flow 시작")
    
    // emit: 값을 방출한다
    for (i in 1..3) {
        delay(1000) // 1초 대기
        emit(i) // 값 방출
    }
}

/**
 * Flow 사용하기
 */
fun main() = runBlocking {
    println("Flow 수집 시작")
    
    // collect: Flow에서 값을 받는다
    simpleFlow().collect { value ->
        println("받은 값: $value")
    }
    
    println("Flow 수집 완료")
}

// 출력:
// Flow 수집 시작
// Flow 시작
// 받은 값: 1 (1초 후)
// 받은 값: 2 (2초 후)
// 받은 값: 3 (3초 후)
// Flow 수집 완료
```

### Flow의 특징

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    
    // 1. Cold Stream: collect를 호출할 때마다 새로 시작
    val flow = flow {
        println("Flow 시작")
        emit(1)
        emit(2)
    }
    
    println("첫 번째 수집")
    flow.collect { println(it) }
    
    println("두 번째 수집")
    flow.collect { println(it) }
    
    // 출력:
    // 첫 번째 수집
    // Flow 시작
    // 1
    // 2
    // 두 번째 수집
    // Flow 시작 (다시 시작!)
    // 1
    // 2
}
```

### Room Database와 Flow

```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO에서 Flow 사용
 * 데이터가 변경되면 자동으로 새 값을 방출
 */
@Dao
interface UserDao {
    
    /**
     * Flow를 반환하면 데이터베이스가 변경될 때마다 자동으로 업데이트
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<User>
    
    /**
     * 모든 사용자 목록을 Flow로 관찰
     */
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>
    
    @Insert
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
}

/**
 * Repository에서 Flow 사용
 */
class UserRepository(private val userDao: UserDao) {
    
    /**
     * Flow를 그대로 반환
     * ViewModel에서 collect하면 DB 변경사항을 실시간으로 받는다
     */
    fun getUser(userId: String): Flow<User> {
        return userDao.getUserFlow(userId)
    }
}

/**
 * ViewModel에서 Flow 수집
 */
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    // StateFlow로 변환하여 UI에 노출
    val user: StateFlow<User?> = repository.getUser("123")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
```

---

## StateFlow vs SharedFlow

### StateFlow

**StateFlow**는 현재 상태를 가지고 있는 Flow입니다.

```kotlin
import kotlinx.coroutines.flow.*

/**
 * StateFlow 특징:
 * 1. 항상 현재 값을 가지고 있다
 * 2. 새로운 구독자는 즉시 현재 값을 받는다
 * 3. 같은 값을 연속으로 emit하면 무시된다
 * 4. UI 상태 관리에 적합
 */
class CounterViewModel : ViewModel() {
    
    // MutableStateFlow: 값을 변경할 수 있는 StateFlow
    private val _count = MutableStateFlow(0)
    
    // StateFlow: 읽기 전용으로 노출
    val count: StateFlow<Int> = _count
    
    fun increment() {
        // value로 현재 값 접근 및 변경
        _count.value = _count.value + 1
    }
    
    fun decrement() {
        _count.value = _count.value - 1
    }
}

/**
 * Compose에서 StateFlow 사용
 */
@Composable
fun CounterScreen(viewModel: CounterViewModel = hiltViewModel()) {
    // StateFlow를 State로 변환
    val count by viewModel.count.collectAsState()
    
    Column {
        Text("Count: $count")
        
        Button(onClick = { viewModel.increment() }) {
            Text("증가")
        }
        
        Button(onClick = { viewModel.decrement() }) {
            Text("감소")
        }
    }
}
```

### SharedFlow

**SharedFlow**는 이벤트를 방출하는 Flow입니다.

```kotlin
import kotlinx.coroutines.flow.*

/**
 * SharedFlow 특징:
 * 1. 현재 값을 가지지 않는다
 * 2. 모든 구독자에게 이벤트를 전달
 * 3. 일회성 이벤트에 적합 (토스트, 네비게이션 등)
 */
class LoginViewModel : ViewModel() {
    
    // MutableSharedFlow: 이벤트를 방출할 수 있는 SharedFlow
    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    
    // SharedFlow: 읽기 전용으로 노출
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                // 로그인 시도
                val result = authRepository.login(username, password)
                
                // 성공 이벤트 방출
                _loginEvent.emit(LoginEvent.Success)
            } catch (e: Exception) {
                // 실패 이벤트 방출
                _loginEvent.emit(LoginEvent.Error(e.message ?: "로그인 실패"))
            }
        }
    }
}

/**
 * 로그인 이벤트 정의
 */
sealed class LoginEvent {
    object Success : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

/**
 * Compose에서 SharedFlow 사용
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    // 이벤트 수집
    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect { event ->
            when (event) {
                is LoginEvent.Success -> {
                    // 성공 시 다음 화면으로 이동
                    onLoginSuccess()
                }
                is LoginEvent.Error -> {
                    // 에러 토스트 표시
                    // showToast(event.message)
                }
            }
        }
    }
    
    // UI 코드...
}
```

### StateFlow vs SharedFlow 비교

| 특징 | StateFlow | SharedFlow |
|------|-----------|------------|
| **현재 값** | 있음 (항상) | 없음 |
| **초기값** | 필수 | 선택 |
| **새 구독자** | 즉시 현재 값 받음 | 새 이벤트만 받음 |
| **중복 값** | 무시됨 | 모두 방출됨 |
| **사용 예** | UI 상태 | 일회성 이벤트 |

```kotlin
/**
 * 실제 사용 예제
 */
class UserViewModel : ViewModel() {
    
    // StateFlow: UI 상태 (현재 사용자 정보)
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    
    // StateFlow: 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // SharedFlow: 일회성 이벤트 (토스트 메시지)
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage
    
    // SharedFlow: 네비게이션 이벤트
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val userData = repository.getUser(userId)
                _user.value = userData // 상태 업데이트
                _toastMessage.emit("사용자 정보 로드 완료") // 이벤트 방출
            } catch (e: Exception) {
                _toastMessage.emit("에러: ${e.message}") // 이벤트 방출
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

## Flow Operators

### 변환 Operators

```kotlin
import kotlinx.coroutines.flow.*

/**
 * map: 각 값을 변환
 */
fun mapExample() = flow {
    emit(1)
    emit(2)
    emit(3)
}.map { value ->
    value * 2 // 각 값을 2배로
}
// 결과: 2, 4, 6

/**
 * filter: 조건에 맞는 값만 통과
 */
fun filterExample() = flow {
    emit(1)
    emit(2)
    emit(3)
    emit(4)
}.filter { value ->
    value % 2 == 0 // 짝수만
}
// 결과: 2, 4

/**
 * transform: 복잡한 변환
 */
fun transformExample() = flow {
    emit(1)
    emit(2)
}.transform { value ->
    emit("Start: $value") // 여러 값을 emit 가능
    delay(100)
    emit("End: $value")
}
// 결과: "Start: 1", "End: 1", "Start: 2", "End: 2"
```

### 결합 Operators

```kotlin
import kotlinx.coroutines.flow.*

/**
 * combine: 두 Flow를 결합 (최신 값 사용)
 */
fun combineExample() {
    val flow1 = flowOf(1, 2, 3)
    val flow2 = flowOf("A", "B", "C")
    
    flow1.combine(flow2) { num, letter ->
        "$num$letter"
    }
    // 결과: "1A", "2A", "2B", "3B", "3C"
}

/**
 * zip: 두 Flow를 쌍으로 결합
 */
fun zipExample() {
    val flow1 = flowOf(1, 2, 3)
    val flow2 = flowOf("A", "B", "C", "D")
    
    flow1.zip(flow2) { num, letter ->
        "$num$letter"
    }
    // 결과: "1A", "2B", "3C" (짧은 쪽에 맞춤)
}

/**
 * flatMapConcat: Flow를 순차적으로 평탄화
 */
fun flatMapConcatExample() = flow {
    emit(1)
    emit(2)
}.flatMapConcat { value ->
    flow {
        emit("$value-A")
        delay(100)
        emit("$value-B")
    }
}
// 결과: "1-A", "1-B", "2-A", "2-B" (순차적)
```

### 실전 예제: 검색 기능

```kotlin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.FlowPreview

/**
 * 검색어 입력을 처리하는 Flow
 */
class SearchViewModel : ViewModel() {
    
    // 검색어 입력
    private val _searchQuery = MutableStateFlow("")
    
    /**
     * 검색 결과 Flow
     * 
     * 여러 Operator를 조합하여 효율적인 검색 구현
     */
    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<String>> = _searchQuery
        // debounce: 입력이 멈춘 후 300ms 대기
        // 사용자가 타이핑하는 동안은 검색하지 않음
        .debounce(300)
        // filter: 빈 문자열 제외
        .filter { query ->
            query.isNotEmpty()
        }
        // distinctUntilChanged: 같은 검색어 중복 제거
        .distinctUntilChanged()
        // map: 검색 실행
        .map { query ->
            searchRepository.search(query)
        }
        // catch: 에러 처리
        .catch { exception ->
            emit(emptyList()) // 에러 시 빈 목록
        }
        // StateFlow로 변환
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * 검색어 업데이트
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

/**
 * Compose UI
 */
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    
    Column {
        // 검색 입력
        TextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
                viewModel.onSearchQueryChanged(newText)
            },
            placeholder = { Text("검색...") }
        )
        
        // 검색 결과
        LazyColumn {
            items(searchResults) { result ->
                Text(result)
            }
        }
    }
}
```

---

## 에러 처리와 취소

### 에러 처리

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * try-catch로 에러 처리
 */
fun errorHandlingExample1() = viewModelScope.launch {
    try {
        val data = fetchDataFromServer()
        updateUI(data)
    } catch (e: Exception) {
        showError(e.message)
    }
}

/**
 * Flow에서 에러 처리
 */
fun errorHandlingExample2() = flow {
    emit(1)
    emit(2)
    throw RuntimeException("에러 발생!")
    emit(3) // 실행되지 않음
}.catch { exception ->
    // 에러를 잡아서 처리
    println("에러: ${exception.message}")
    emit(-1) // 기본값 emit
}.collect { value ->
    println("값: $value")
}
// 출력: 값: 1, 값: 2, 에러: 에러 발생!, 값: -1

/**
 * ViewModel에서 에러 처리
 */
class UserViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                // 데이터 로드
                val user = repository.getUser(userId)
                _uiState.value = UiState.Success(user)
            } catch (e: IOException) {
                // 네트워크 에러
                _uiState.value = UiState.Error("네트워크 연결을 확인해주세요")
            } catch (e: HttpException) {
                // HTTP 에러
                _uiState.value = UiState.Error("서버 에러: ${e.code()}")
            } catch (e: Exception) {
                // 기타 에러
                _uiState.value = UiState.Error("알 수 없는 에러: ${e.message}")
            }
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val user: User) : UiState()
    data class Error(val message: String) : UiState()
}
```

### Coroutine 취소

```kotlin
import kotlinx.coroutines.*

/**
 * Job으로 취소하기
 */
fun cancellationExample1() {
    val job = viewModelScope.launch {
        repeat(1000) { i ->
            println("작업 중: $i")
            delay(500)
        }
    }
    
    // 3초 후 취소
    viewModelScope.launch {
        delay(3000)
        job.cancel() // Coroutine 취소
        println("작업 취소됨")
    }
}

/**
 * 취소 가능한 Coroutine 만들기
 */
suspend fun cancellableWork() {
    withContext(Dispatchers.Default) {
        repeat(1000) { i ->
            // isActive로 취소 여부 확인
            if (!isActive) {
                println("취소됨!")
                return@withContext
            }
            
            println("작업 중: $i")
            // delay는 자동으로 취소를 확인
            delay(100)
        }
    }
}

/**
 * finally로 정리 작업
 */
fun cancellationExample2() = viewModelScope.launch {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("작업 중: $i")
                delay(500)
            }
        } finally {
            // 취소되어도 실행됨
            println("정리 작업 실행")
            
            // 취소된 Coroutine에서 suspend 함수 호출하려면
            withContext(NonCancellable) {
                delay(1000)
                println("정리 완료")
            }
        }
    }
    
    delay(2000)
    job.cancel()
}

/**
 * ViewModel에서 자동 취소
 */
class MyViewModel : ViewModel() {
    
    fun loadData() {
        // viewModelScope는 ViewModel이 제거되면 자동으로 취소
        viewModelScope.launch {
            try {
                val data = repository.getData()
                updateUI(data)
            } catch (e: CancellationException) {
                // 취소 예외는 다시 던져야 함
                throw e
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}
```

### Timeout 처리

```kotlin
import kotlinx.coroutines.*

/**
 * withTimeout: 시간 초과 시 예외 발생
 */
suspend fun timeoutExample1() {
    try {
        withTimeout(2000) { // 2초 제한
            // 5초 걸리는 작업
            delay(5000)
            println("완료")
        }
    } catch (e: TimeoutCancellationException) {
        println("시간 초과!")
    }
}

/**
 * withTimeoutOrNull: 시간 초과 시 null 반환
 */
suspend fun timeoutExample2() {
    val result = withTimeoutOrNull(2000) {
        delay(5000)
        "완료"
    }
    
    if (result == null) {
        println("시간 초과!")
    } else {
        println(result)
    }
}

/**
 * 실전 예제: 네트워크 요청 타임아웃
 */
class UserRepository {
    
    suspend fun getUserWithTimeout(userId: String): User? {
        return try {
            // 5초 제한
            withTimeout(5000) {
                apiService.getUser(userId)
            }
        } catch (e: TimeoutCancellationException) {
            // 타임아웃 로깅
            Log.e("Repository", "사용자 로드 타임아웃")
            null
        }
    }
}
```

---

## 실전 예제

### 완전한 MVVM 예제

```kotlin
// ========== Data Layer ==========

/**
 * API Service
 */
interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
    
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post
}

/**
 * Data Model
 */
data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val userId: Int
)

/**
 * Repository
 */
class PostRepository @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 게시물 목록을 Flow로 반환
     */
    fun getPosts(): Flow<List<Post>> = flow {
        // 로딩 시작
        emit(emptyList())
        
        // API 호출
        val posts = apiService.getPosts()
        
        // 결과 emit
        emit(posts)
    }.catch { exception ->
        // 에러 처리
        throw PostException("게시물 로드 실패: ${exception.message}")
    }
    
    /**
     * 특정 게시물 가져오기
     */
    suspend fun getPost(id: Int): Post {
        return withContext(Dispatchers.IO) {
            apiService.getPost(id)
        }
    }
}

class PostException(message: String) : Exception(message)

// ========== ViewModel Layer ==========

/**
 * UI 상태
 */
sealed class PostListUiState {
    object Loading : PostListUiState()
    data class Success(val posts: List<Post>) : PostListUiState()
    data class Error(val message: String) : PostListUiState()
}

/**
 * ViewModel
 */
@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    
    // UI 상태
    private val _uiState = MutableStateFlow<PostListUiState>(PostListUiState.Loading)
    val uiState: StateFlow<PostListUiState> = _uiState
    
    // 새로고침 이벤트
    private val _refreshEvent = MutableSharedFlow<Unit>()
    
    init {
        // 초기 로드
        loadPosts()
    }
    
    /**
     * 게시물 로드
     */
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = PostListUiState.Loading
            
            repository.getPosts()
                .catch { exception ->
                    _uiState.value = PostListUiState.Error(
                        exception.message ?: "알 수 없는 에러"
                    )
                }
                .collect { posts ->
                    _uiState.value = PostListUiState.Success(posts)
                }
        }
    }
    
    /**
     * 새로고침
     */
    fun refresh() {
        loadPosts()
    }
}

// ========== UI Layer ==========

/**
 * Compose 화면
 */
@Composable
fun PostListScreen(
    viewModel: PostListViewModel = hiltViewModel(),
    onPostClick: (Int) -> Unit
) {
    // UI 상태 관찰
    val uiState by viewModel.uiState.collectAsState()
    
    // Pull to Refresh 상태
    val isRefreshing = uiState is PostListUiState.Loading
    
    // Pull to Refresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        // UI 상태에 따라 다른 화면 표시
        when (val state = uiState) {
            is PostListUiState.Loading -> {
                // 로딩 화면
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is PostListUiState.Success -> {
                // 게시물 목록
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.posts) { post ->
                        PostItem(
                            post = post,
                            onClick = { onPostClick(post.id) }
                        )
                    }
                }
            }
            
            is PostListUiState.Error -> {
                // 에러 화면
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadPosts() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        // Pull to Refresh 인디케이터
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * 게시물 아이템
 */
@Composable
fun PostItem(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 에러 뷰
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
```

---

## 📝 요약

### Coroutines 핵심 개념

| 개념 | 설명 | 예제 |
|------|------|------|
| **Coroutine** | 비동기 작업을 쉽게 처리 | `launch { }` |
| **suspend** | 일시 중지 가능한 함수 | `suspend fun fetch()` |
| **Scope** | Coroutine 생명주기 관리 | `viewModelScope` |
| **Dispatcher** | 실행 스레드 지정 | `Dispatchers.IO` |
| **Flow** | 비동기 데이터 스트림 | `flow { emit() }` |
| **StateFlow** | 상태를 가진 Flow | `MutableStateFlow(0)` |
| **SharedFlow** | 이벤트용 Flow | `MutableSharedFlow()` |

### 자주 사용하는 패턴

```kotlin
// 1. 데이터 로드
viewModelScope.launch {
    val data = withContext(Dispatchers.IO) {
        repository.getData()
    }
    updateUI(data)
}

// 2. Flow 수집
repository.getDataFlow()
    .catch { handleError(it) }
    .collect { data ->
        updateUI(data)
    }

// 3. StateFlow로 변환
val uiState: StateFlow<UiState> = repository.getData()
    .map { UiState.Success(it) }
    .catch { emit(UiState.Error(it.message)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), UiState.Loading)
```

---

## 🎯 다음 단계

1. **간단한 프로젝트로 연습**
   - API 호출하여 데이터 표시
   - Flow로 실시간 데이터 관찰

2. **관련 문서 학습**
   - [39-dependency-injection-hilt-guide.md](./39-dependency-injection-hilt-guide.md)
   - [41-mvvm-mvi-architecture-guide.md](./41-mvvm-mvi-architecture-guide.md)

3. **공식 문서 참고**
   - [Kotlin Coroutines 공식 문서](https://kotlinlang.org/docs/coroutines-overview.html)
   - [Android Coroutines 가이드](https://developer.android.com/kotlin/coroutines)

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
