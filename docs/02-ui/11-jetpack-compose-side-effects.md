# Jetpack Compose Side Effects 가이드

## 📚 목차

1. [Side Effects란?](#side-effects란)
2. [LaunchedEffect](#launchedeffect)
3. [DisposableEffect](#disposableeffect)
4. [SideEffect](#sideeffect)
5. [rememberCoroutineScope](#remembercoroutinescope)
6. [rememberUpdatedState](#rememberupdatedstate)
7. [produceState](#producestate)
8. [derivedStateOf](#derivedstateof)
9. [실전 예제](#실전-예제)

---

## Side Effects란?

**Side Effect**는 Composable 함수의 범위를 벗어나 발생하는 앱 상태의 변경을 의미합니다.

### Side Effects가 필요한 경우

- ✅ **API 호출**: 네트워크 요청
- ✅ **데이터베이스 작업**: Room 쿼리
- ✅ **이벤트 리스너 등록**: 센서, 위치 등
- ✅ **타이머/딜레이**: 일정 시간 후 작업 실행
- ✅ **외부 상태 변경**: SharedPreferences, Analytics 등

### Compose의 Side Effect API

| API | 사용 시기 |
|-----|----------|
| **LaunchedEffect** | Coroutine 실행 (키 변경 시 재시작) |
| **DisposableEffect** | 리소스 정리가 필요한 경우 |
| **SideEffect** | Compose 상태를 비-Compose 코드에 전달 |
| **rememberCoroutineScope** | 이벤트 핸들러에서 Coroutine 실행 |
| **rememberUpdatedState** | 최신 값 참조 (재구성 방지) |
| **produceState** | 비-Compose 상태를 Compose State로 변환 |
| **derivedStateOf** | 다른 State로부터 파생된 State |

---

## LaunchedEffect

Composable이 처음 구성될 때 또는 키가 변경될 때 Coroutine을 실행합니다.

### 기본 사용법

```kotlin
@Composable
fun TimerExample() {
    var seconds by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }
    
    Text("경과 시간: $seconds 초")
}
```

### 키를 사용한 재시작

```kotlin
@Composable
fun SearchExample(query: String) {
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // query가 변경될 때마다 새로운 검색 실행
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            delay(300) // 디바운스
            searchResults = performSearch(query)
        }
    }
    
    LazyColumn {
        items(searchResults) { result ->
            Text(result)
        }
    }
}

suspend fun performSearch(query: String): List<String> {
    // API 호출 시뮬레이션
    delay(500)
    return listOf("$query 결과 1", "$query 결과 2")
}
```

### API 호출

```kotlin
@Composable
fun UserProfileScreen(userId: Int) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(userId) {
        isLoading = true
        error = null
        
        try {
            user = apiService.getUser(userId)
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    when {
        isLoading -> CircularProgressIndicator()
        error != null -> Text("에러: $error")
        user != null -> UserProfile(user!!)
    }
}
```

### 여러 키 사용

```kotlin
@Composable
fun DataLoader(userId: Int, category: String) {
    var data by remember { mutableStateOf<List<Item>>(emptyList()) }
    
    // userId 또는 category가 변경되면 재실행
    LaunchedEffect(userId, category) {
        data = loadData(userId, category)
    }
    
    ItemList(data)
}
```

### 취소 처리

```kotlin
@Composable
fun CancellableEffect() {
    var count by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        try {
            while (true) {
                delay(1000)
                count++
            }
        } catch (e: CancellationException) {
            // Composable이 제거될 때 자동으로 취소됨
            println("Effect cancelled")
            throw e // 반드시 다시 throw
        }
    }
    
    Text("Count: $count")
}
```

---

## DisposableEffect

리소스를 등록하고 정리해야 할 때 사용합니다.

### 기본 사용법

```kotlin
@Composable
fun LifecycleAwareComponent() {
    DisposableEffect(Unit) {
        println("Component created")
        
        onDispose {
            println("Component disposed")
        }
    }
}
```

### 이벤트 리스너 등록/해제

```kotlin
@Composable
fun BackPressHandler(onBackPressed: () -> Unit) {
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    
    DisposableEffect(backDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
        
        backDispatcher?.addCallback(callback)
        
        onDispose {
            callback.remove()
        }
    }
}
```

### Lifecycle 관찰

```kotlin
@Composable
fun LifecycleObserver(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    onEvent: (Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            onEvent(event)
        }
        
        lifecycle.addObserver(observer)
        
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

// 사용 예제
@Composable
fun MyScreen() {
    LifecycleObserver { event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                println("Screen resumed")
            }
            Lifecycle.Event.ON_PAUSE -> {
                println("Screen paused")
            }
            else -> {}
        }
    }
}
```

### 센서 리스너

```kotlin
@Composable
fun AccelerometerSensor(
    onSensorChanged: (FloatArray) -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    
    DisposableEffect(Unit) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                onSensorChanged(event.values)
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}
```

---

## SideEffect

Compose 상태를 비-Compose 코드에 전달할 때 사용합니다.

### 기본 사용법

```kotlin
@Composable
fun AnalyticsScreen(screenName: String) {
    SideEffect {
        // 매 재구성마다 실행됨
        Analytics.logScreenView(screenName)
    }
}
```

### 외부 상태 동기화

```kotlin
@Composable
fun SyncWithExternalState(value: Int) {
    val externalObject = remember { ExternalObject() }
    
    SideEffect {
        // Compose 상태를 외부 객체에 동기화
        externalObject.value = value
    }
}
```

> [!WARNING]
> **SideEffect**는 매 재구성마다 실행됩니다. 성능에 민감한 작업은 피하세요.

---

## rememberCoroutineScope

이벤트 핸들러에서 Coroutine을 실행할 때 사용합니다.

### 기본 사용법

```kotlin
@Composable
fun SnackbarExample() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Button(
            onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("버튼 클릭됨!")
                }
            }
        ) {
            Text("Snackbar 표시")
        }
    }
}
```

### 스크롤 제어

```kotlin
@Composable
fun ScrollToTopButton() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    Box {
        LazyColumn(state = listState) {
            items(100) { index ->
                Text("아이템 $index")
            }
        }
        
        // 스크롤 위치가 0이 아닐 때만 버튼 표시
        if (listState.firstVisibleItemIndex > 0) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.ArrowUpward, "맨 위로")
            }
        }
    }
}
```

### 비동기 작업

```kotlin
@Composable
fun AsyncButtonExample() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Column {
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        delay(2000) // 비동기 작업 시뮬레이션
                        result = "작업 완료!"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "로딩 중..." else "시작")
        }
        
        if (result.isNotEmpty()) {
            Text(result)
        }
    }
}
```

---

## rememberUpdatedState

최신 값을 참조하면서 재구성을 방지합니다.

### 기본 사용법

```kotlin
@Composable
fun TimerWithCallback(
    onTimeout: () -> Unit
) {
    // onTimeout이 변경되어도 LaunchedEffect가 재시작되지 않음
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    
    LaunchedEffect(Unit) {
        delay(5000)
        currentOnTimeout()
    }
}
```

### 실전 예제

```kotlin
@Composable
fun AutoSaveForm(
    content: String,
    onSave: (String) -> Unit
) {
    val currentOnSave by rememberUpdatedState(onSave)
    
    LaunchedEffect(content) {
        // content가 변경된 후 2초 대기
        delay(2000)
        currentOnSave(content)
    }
    
    Text("자동 저장 중...")
}
```

---

## produceState

비-Compose 상태를 Compose State로 변환합니다.

### 기본 사용법

```kotlin
@Composable
fun LoadImageUrl(url: String): State<Result<Bitmap>> {
    return produceState<Result<Bitmap>>(
        initialValue = Result.Loading,
        url
    ) {
        value = try {
            Result.Success(loadImage(url))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
```

### Flow를 State로 변환

```kotlin
@Composable
fun FlowToState(flow: Flow<Int>): State<Int> {
    return produceState(initialValue = 0, flow) {
        flow.collect { newValue ->
            value = newValue
        }
    }
}

// 사용 예제
@Composable
fun CounterFromFlow() {
    val counterFlow = remember {
        flow {
            var count = 0
            while (true) {
                emit(count++)
                delay(1000)
            }
        }
    }
    
    val count by FlowToState(counterFlow)
    
    Text("Count: $count")
}
```

### 실시간 데이터 구독

```kotlin
@Composable
fun RealtimeData(userId: Int): State<User?> {
    return produceState<User?>(
        initialValue = null,
        userId
    ) {
        // WebSocket 또는 실시간 데이터베이스 구독
        val subscription = subscribeToUser(userId) { user ->
            value = user
        }
        
        awaitDispose {
            subscription.cancel()
        }
    }
}
```

---

## derivedStateOf

다른 State로부터 계산된 State를 만듭니다.

### 기본 사용법

```kotlin
@Composable
fun FilteredList() {
    var searchQuery by remember { mutableStateOf("") }
    val items = remember { List(1000) { "Item $it" } }
    
    // searchQuery가 변경될 때만 재계산
    val filteredItems by remember {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                items
            } else {
                items.filter { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    
    Column {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("검색") }
        )
        
        LazyColumn {
            items(filteredItems) { item ->
                Text(item)
            }
        }
    }
}
```

### 스크롤 상태 파생

```kotlin
@Composable
fun ScrollDerivedState() {
    val listState = rememberLazyListState()
    
    // 스크롤 위치에 따라 계산
    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    
    Box {
        LazyColumn(state = listState) {
            items(100) { Text("Item $it") }
        }
        
        AnimatedVisibility(
            visible = showButton,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(onClick = { /* scroll to top */ }) {
                Icon(Icons.Filled.ArrowUpward, null)
            }
        }
    }
}
```

### 복잡한 계산 최적화

```kotlin
@Composable
fun ExpensiveCalculation() {
    var input1 by remember { mutableStateOf(0) }
    var input2 by remember { mutableStateOf(0) }
    var irrelevantState by remember { mutableStateOf(0) }
    
    // input1과 input2가 변경될 때만 재계산
    val result by remember {
        derivedStateOf {
            // 매우 비싼 계산
            expensiveOperation(input1, input2)
        }
    }
    
    Column {
        Text("결과: $result")
        Button(onClick = { input1++ }) { Text("Input1 증가") }
        Button(onClick = { input2++ }) { Text("Input2 증가") }
        Button(onClick = { irrelevantState++ }) { 
            Text("무관한 상태 (재계산 안됨)")
        }
    }
}

fun expensiveOperation(a: Int, b: Int): Int {
    println("비싼 계산 실행!")
    return a + b
}
```

---

## 실전 예제

### 자동 저장 기능

```kotlin
@Composable
fun AutoSaveEditor() {
    var text by remember { mutableStateOf("") }
    var lastSaved by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    // 텍스트가 변경되면 3초 후 자동 저장
    LaunchedEffect(text) {
        if (text != lastSaved && text.isNotEmpty()) {
            delay(3000)
            isSaving = true
            
            try {
                // 저장 로직
                saveToServer(text)
                lastSaved = text
            } catch (e: Exception) {
                // 에러 처리
            } finally {
                isSaving = false
            }
        }
    }
    
    Column {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("내용") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("저장 중...")
            } else if (text == lastSaved && text.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("저장됨")
            }
        }
    }
}

suspend fun saveToServer(text: String) {
    delay(1000) // 서버 저장 시뮬레이션
}
```

### 실시간 검색 (디바운스)

```kotlin
@Composable
fun DebouncedSearch() {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    
    // 디바운스: 입력이 멈춘 후 500ms 후에 검색
    LaunchedEffect(query) {
        if (query.isEmpty()) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        
        isSearching = true
        delay(500) // 디바운스 시간
        
        try {
            searchResults = performSearch(query)
        } finally {
            isSearching = false
        }
    }
    
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("검색") },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        LazyColumn {
            items(searchResults) { result ->
                Text(
                    text = result,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}
```

### 타이머 카운트다운

```kotlin
@Composable
fun CountdownTimer(
    initialSeconds: Int,
    onFinish: () -> Unit
) {
    var remainingSeconds by remember { mutableStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    val currentOnFinish by rememberUpdatedState(onFinish)
    
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            currentOnFinish()
            isRunning = false
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(remainingSeconds),
            style = MaterialTheme.typography.displayLarge
        )
        
        Row {
            Button(
                onClick = { isRunning = !isRunning },
                enabled = remainingSeconds > 0
            ) {
                Text(if (isRunning) "일시정지" else "시작")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    isRunning = false
                    remainingSeconds = initialSeconds
                }
            ) {
                Text("리셋")
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}
```

### 페이지네이션

```kotlin
@Composable
fun PaginatedList() {
    var items by remember { mutableStateOf<List<String>>(emptyList()) }
    var page by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    
    val listState = rememberLazyListState()
    
    // 페이지 변경 시 데이터 로드
    LaunchedEffect(page) {
        if (hasMore && !isLoading) {
            isLoading = true
            
            try {
                val newItems = loadPage(page)
                items = items + newItems
                hasMore = newItems.isNotEmpty()
            } finally {
                isLoading = false
            }
        }
    }
    
    // 스크롤이 끝에 도달하면 다음 페이지 로드
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= items.size - 5 &&
                    hasMore &&
                    !isLoading
                ) {
                    page++
                }
            }
    }
    
    LazyColumn(state = listState) {
        items(items) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

suspend fun loadPage(page: Int): List<String> {
    delay(1000) // API 호출 시뮬레이션
    return List(20) { "Item ${page * 20 + it}" }
}
```

### 위치 추적

```kotlin
@Composable
fun LocationTracker() {
    var location by remember { mutableStateOf<Location?>(null) }
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) 
            as LocationManager
        
        val listener = object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                location = newLocation
            }
            
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                10f,
                listener
            )
        } catch (e: SecurityException) {
            // 권한 에러 처리
        }
        
        onDispose {
            locationManager.removeUpdates(listener)
        }
    }
    
    location?.let {
        Column {
            Text("위도: ${it.latitude}")
            Text("경도: ${it.longitude}")
        }
    } ?: Text("위치 정보를 가져오는 중...")
}
```

---

## 💡 베스트 프랙티스

### 1. 적절한 Side Effect 선택

```kotlin
// ✅ Coroutine 실행 → LaunchedEffect
LaunchedEffect(key) {
    loadData()
}

// ✅ 리소스 정리 필요 → DisposableEffect
DisposableEffect(Unit) {
    val listener = registerListener()
    onDispose { listener.unregister() }
}

// ✅ 이벤트 핸들러 → rememberCoroutineScope
val scope = rememberCoroutineScope()
Button(onClick = { scope.launch { doWork() } })

// ✅ 파생 상태 → derivedStateOf
val filtered by remember { derivedStateOf { filter(items) } }
```

### 2. 키 관리

```kotlin
// ❌ 나쁜 예: 불필요한 재시작
LaunchedEffect(true) { ... } // 매번 재시작

// ✅ 좋은 예: 적절한 키 사용
LaunchedEffect(userId) { ... } // userId 변경 시만 재시작
LaunchedEffect(Unit) { ... } // 한 번만 실행
```

### 3. 취소 처리

```kotlin
// ✅ CancellationException은 다시 throw
LaunchedEffect(Unit) {
    try {
        while (true) {
            delay(1000)
            doWork()
        }
    } catch (e: CancellationException) {
        cleanup()
        throw e // 필수!
    }
}
```

### 4. 메모리 누수 방지

```kotlin
// ✅ DisposableEffect로 리소스 정리
DisposableEffect(Unit) {
    val listener = registerListener()
    onDispose {
        listener.unregister() // 반드시 정리
    }
}
```

### 5. 성능 최적화

```kotlin
// ✅ derivedStateOf로 불필요한 재구성 방지
val expensiveValue by remember {
    derivedStateOf {
        expensiveCalculation(input)
    }
}
```

---

## 🎯 다음 단계

Side Effects를 마스터했습니다! 다음으로:

1. **이미지 로딩 가이드** - Coil로 네트워크 이미지 로딩
2. **로컬 데이터베이스** - Room Database
3. **권한 관리** - 런타임 권한 처리

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
