# Jetpack Compose State 치트시트

> 📖 **State 가이드 시리즈**
> - **04**: [State 완벽 가이드](./04-jetpack-compose-state-guide.md) - 기초
> - **04-1**: [State 고급 패턴](./04-1-jetpack-compose-state-advanced.md) - 심화
> - **04-2**: [State 실전 프로젝트](./04-2-jetpack-compose-state-projects.md) - 실전
> - **04-3**: State 치트시트 (현재 문서) - 요약

---

## 🧭 State 결정 트리 (Decision Tree)

**Q. 어떤 State를 써야 할까요?**

1. **화면 회전 시에도 유지되어야 하나요?**
   - **YES** → `ViewModel` + `StateFlow`
   - **NO** → 다음 질문으로

2. **프로세스 종료(백그라운드) 후에도 유지되어야 하나요?**
   - **YES** → `rememberSaveable`
   - **NO** → `remember`

3. **다른 State로부터 계산된 값인가요?**
   - **YES** → `derivedStateOf`
   - **NO** → `mutableStateOf`

4. **비동기 작업(API, DB)이 필요한가요?**
   - **YES** → `LaunchedEffect`
   - **NO** → `SideEffect` (외부 동기화) 또는 그냥 실행

---

## ⚡ 핵심 문법 요약 (Quick Reference)

### State 선언

```kotlin
// 1. 기본 (가장 많이 씀)
var name by remember { mutableStateOf("") }

// 2. 구조 분해 (가끔 씀)
val (name, setName) = remember { mutableStateOf("") }

// 3. 객체 직접 사용 (잘 안 씀)
val nameState = remember { mutableStateOf("") }
// nameState.value 로 접근
```

### StateFlow 수집 (ViewModel → Compose)

```kotlin
// ✅ 권장 (Lifecycle 인식)
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ⚠️ 기본 (Lifecycle 인식 안 함)
val uiState by viewModel.uiState.collectAsState()
```

### Side Effect 요약

| 이름 | 실행 시점 | 용도 |
|------|----------|------|
| **LaunchedEffect** | Composition 시작 시 | API 호출, 타이머, 애니메이션 |
| **DisposableEffect** | Composition 시작 시 | 리스너 등록/해제 (정리 필요 시) |
| **SideEffect** | Recomposition 성공 시 | Analytics 전송, 외부 상태 동기화 |
| **rememberCoroutineScope** | 이벤트 발생 시 | 버튼 클릭 핸들러에서 코루틴 실행 |

---

## 📋 Copy & Paste 템플릿

### 1. 기본 ViewModel 템플릿

```kotlin
class MyViewModel : ViewModel() {
    // 내부 수정용 (Mutable)
    private val _uiState = MutableStateFlow(MyUiState())
    // 외부 노출용 (Immutable)
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    fun updateData() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true)
        }
        // 비동기 작업...
    }
}

data class MyUiState(
    val data: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### 2. 리스트 화면 템플릿 (LazyColumn)

```kotlin
@Composable
fun MyListScreen(
    items: List<MyItem>,
    onItemClick: (MyItem) -> Unit
) {
    if (items.isEmpty()) {
        // 빈 화면 처리
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("데이터가 없습니다")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // key 지정 필수! (성능 최적화)
            items(
                items = items,
                key = { item -> item.id }
            ) { item ->
                MyItemCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
```

### 3. 입력 폼 템플릿 (TextField)

```kotlin
@Composable
fun MyForm() {
    // 화면 회전 시에도 유지되도록 rememberSaveable 사용
    var text by rememberSaveable { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                isError = newText.length > 10  // 유효성 검사
            },
            label = { Text("입력하세요") },
            isError = isError,
            supportingText = {
                if (isError) Text("10자 이내로 입력해주세요")
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = { /* 전송 */ },
            enabled = text.isNotEmpty() && !isError
        ) {
            Text("전송")
        }
    }
}
```

### 4. 비동기 로딩 템플릿

```kotlin
@Composable
fun DataScreen(viewModel: MyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("에러: ${uiState.error}")
                Button(onClick = { viewModel.retry() }) { Text("재시도") }
            }
        }
        else -> {
            MyListScreen(items = uiState.data)
        }
    }
}
```

---

## 🚫 자주 하는 실수 (Anti-patterns)

### ❌ 1. 리스트에 Key 안 쓰기

```kotlin
// ❌ 나쁜 예
items(items) { item -> ... }

// ✅ 좋은 예
items(items, key = { it.id }) { item -> ... }
```
**이유**: 아이템 순서가 바뀌거나 삭제될 때 Compose가 멍청하게 전체를 다시 그립니다. Key를 주면 똑똑하게 변경된 것만 처리합니다.

### ❌ 2. ViewModel 인스턴스 직접 생성

```kotlin
// ❌ 나쁜 예
val viewModel = MyViewModel()

// ✅ 좋은 예
val viewModel: MyViewModel = viewModel()
```
**이유**: 직접 생성하면 화면 회전 시 데이터가 다 날아갑니다. `viewModel()` 델리게이트를 써야 ViewModelStore에 저장됩니다.

### ❌ 3. State를 var로 선언하고 = 사용

```kotlin
// ❌ 나쁜 예
var name = mutableStateOf("")  // name.value로 접근해야 함 (불편)

// ✅ 좋은 예
var name by remember { mutableStateOf("") }  // name으로 바로 접근 (편함)
```

### ❌ 4. 불필요한 derivedStateOf 사용

```kotlin
// ❌ 나쁜 예 (그냥 변수로 해도 됨)
val fullName by remember { derivedStateOf { "$firstName $lastName" } }

// ✅ 좋은 예 (계산 비용이 클 때만 사용)
val sortedList by remember { derivedStateOf { list.sortedBy { it.date } } }
```

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
