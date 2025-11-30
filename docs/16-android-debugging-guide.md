# Android 디버깅 가이드

## 📚 목차

1. [Logcat 사용법](#logcat-사용법)
2. [Breakpoint 디버깅](#breakpoint-디버깅)
3. [Layout Inspector](#layout-inspector)
4. [자주 발생하는 에러](#자주-발생하는-에러)
5. [성능 프로파일링](#성능-프로파일링)

---

## Logcat 사용법

### 기본 로깅

```kotlin
import android.util.Log

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.v(TAG, "Verbose log")
        Log.d(TAG, "Debug log")
        Log.i(TAG, "Info log")
        Log.w(TAG, "Warning log")
        Log.e(TAG, "Error log")
    }
}
```

### 로그 레벨

| 레벨 | 메서드 | 용도 |
|------|--------|------|
| Verbose | `Log.v()` | 상세한 정보 |
| Debug | `Log.d()` | 디버깅 정보 |
| Info | `Log.i()` | 일반 정보 |
| Warning | `Log.w()` | 경고 |
| Error | `Log.e()` | 에러 |

### 예외 로깅

```kotlin
try {
    // 위험한 작업
    riskyOperation()
} catch (e: Exception) {
    Log.e(TAG, "Error occurred", e)
}
```

### Timber 라이브러리

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.jakewharton.timber:timber:5.0.1")
}

// Application 클래스
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

// 사용
Timber.d("Debug message")
Timber.e(exception, "Error message")
```

---

## Breakpoint 디버깅

### Breakpoint 설정

1. 코드 라인 번호 옆 클릭
2. 빨간 점이 표시됨
3. 디버그 모드로 앱 실행

### 조건부 Breakpoint

```kotlin
fun processItems(items: List<Item>) {
    items.forEach { item ->
        // Breakpoint 우클릭 → Condition
        // 조건: item.id == 5
        processItem(item)
    }
}
```

### 로그 Breakpoint

Breakpoint 우클릭 → "Breakpoint Properties"
- ✅ Suspend: 체크 해제
- ✅ Log: "Evaluate and log" 체크
- 표현식 입력: `"Item: ${item.name}"`

### 디버거 단축키

| 단축키 | 기능 |
|--------|------|
| F8 | Step Over (다음 줄) |
| F7 | Step Into (함수 안으로) |
| Shift+F8 | Step Out (함수 밖으로) |
| F9 | Resume (다음 Breakpoint까지) |

---

## Layout Inspector

### 사용 방법

1. **View → Tool Windows → Layout Inspector**
2. 실행 중인 앱 선택
3. UI 계층 구조 확인

### 주요 기능

- **3D 뷰**: UI 계층을 3D로 확인
- **속성 검사**: 선택한 컴포넌트의 속성 확인
- **레이아웃 경계**: 각 요소의 크기와 위치 확인

---

## 자주 발생하는 에러

### 1. NullPointerException

```kotlin
// ❌ 문제
val user: User? = getUser()
val name = user.name // NPE 발생 가능

// ✅ 해결
val name = user?.name ?: "Unknown"
```

### 2. IndexOutOfBoundsException

```kotlin
// ❌ 문제
val item = list[5] // 리스트 크기가 5 이하면 에러

// ✅ 해결
val item = list.getOrNull(5)
```

### 3. ConcurrentModificationException

```kotlin
// ❌ 문제
list.forEach { item ->
    if (item.shouldRemove) {
        list.remove(item) // 에러!
    }
}

// ✅ 해결
list.removeAll { it.shouldRemove }
```

### 4. NetworkOnMainThreadException

```kotlin
// ❌ 문제
fun loadData() {
    val data = apiService.getData() // 메인 스레드에서 네트워크 호출
}

// ✅ 해결
fun loadData() {
    viewModelScope.launch {
        val data = apiService.getData()
    }
}
```

### 5. Compose Recomposition 문제

```kotlin
// ❌ 문제: 매번 새로운 객체 생성
@Composable
fun MyScreen() {
    val viewModel = MyViewModel() // 재구성마다 새로 생성
}

// ✅ 해결
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    // viewModel()은 재구성 시 유지됨
}
```

---

## 성능 프로파일링

### CPU Profiler

1. **View → Tool Windows → Profiler**
2. **CPU** 선택
3. 앱 실행 및 프로파일링 시작
4. 느린 함수 찾기

### Memory Profiler

1. **Memory** 탭 선택
2. 메모리 누수 확인
3. Heap Dump 분석

### 성능 최적화 팁

```kotlin
// ✅ remember 사용
@Composable
fun ExpensiveComposable() {
    val expensiveValue = remember {
        calculateExpensiveValue()
    }
}

// ✅ derivedStateOf 사용
@Composable
fun FilteredList(items: List<Item>, query: String) {
    val filteredItems = remember(items, query) {
        items.filter { it.name.contains(query) }
    }
}

// ✅ key 사용
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemRow(item)
    }
}
```

---

## 💡 디버깅 팁

### 1. 로그는 의미 있게

```kotlin
// ❌ 나쁜 예
Log.d(TAG, "here")

// ✅ 좋은 예
Log.d(TAG, "User login successful: ${user.email}")
```

### 2. 에러 처리는 구체적으로

```kotlin
// ✅ 구체적인 에러 처리
try {
    loadData()
} catch (e: IOException) {
    Log.e(TAG, "Network error", e)
} catch (e: JsonParseException) {
    Log.e(TAG, "JSON parsing error", e)
}
```

### 3. TODO 주석 활용

```kotlin
// TODO: 성능 최적화 필요
// FIXME: 버그 수정 필요
// HACK: 임시 해결책
```

---

## 🎯 다음 단계

디버깅을 마스터했습니다! 마지막으로:

1. **앱 배포** - Google Play 배포

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Debugging! 🐛
