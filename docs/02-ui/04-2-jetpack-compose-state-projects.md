# Jetpack Compose State 실전 프로젝트

> 📖 **State 가이드 시리즈**
> - **04**: [State 완벽 가이드](./04-jetpack-compose-state-guide.md) - 기초부터 ViewModel까지
> - **04-1**: [State 고급 패턴](./04-1-jetpack-compose-state-advanced.md) - Side Effect, 성능 최적화
> - **04-2**: State 실전 프로젝트 (현재 문서) - 메모, 타이머, 채팅 앱
> - **04-3**: [State 치트시트](./04-3-jetpack-compose-state-cheatsheet.md) - 핵심 요약, 템플릿

---

## 📚 목차

1. [프로젝트 1: 메모 앱](#프로젝트-1-메모-앱)
2. [프로젝트 2: 타이머 앱](#프로젝트-2-타이머-앱)
3. [프로젝트 3: 채팅 앱 UI](#프로젝트-3-채팅-앱-ui)

---

## 프로젝트 1: 메모 앱

### 🎯 프로젝트 개요

**난이도**: ⭐⭐⭐  
**학습 내용**: ViewModel, StateFlow, CRUD, 검색, 정렬

**기능**:
- 메모 추가/수정/삭제
- 검색 기능 (derivedStateOf)
- 정렬 (최신순, 제목순)
- 로컬 저장 (DataStore)

### 📐 데이터 모델

```kotlin
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 메모 데이터 클래스
 * 
 * @Serializable: JSON 직렬화/역직렬화 지원 (DataStore 저장용)
 */
@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),  // 고유 ID (자동 생성)
    val title: String,                                // 메모 제목
    val content: String,                              // 메모 내용
    val createdAt: Long = System.currentTimeMillis(), // 생성 시간 (밀리초)
    val updatedAt: Long = System.currentTimeMillis()  // 수정 시간 (밀리초)
)

/**
 * 정렬 순서 열거형
 */
enum class SortOrder {
    NEWEST_FIRST,   // 최신순
    OLDEST_FIRST,   // 오래된순
    TITLE_ASC,      // 제목 오름차순 (A→Z)
    TITLE_DESC      // 제목 내림차순 (Z→A)
}
```

### 🏗️ ViewModel

```kotlin
/**
 * 메모 ViewModel
 * 
 * 메모 목록, 검색, 정렬 상태를 관리합니다.
 */
class NoteViewModel : ViewModel() {
    // Private: 내부에서만 수정 가능
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    // Public: 외부에서 읽기만 가능
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    // 검색어 State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 정렬 순서 State
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    /**
     * 검색 및 정렬된 메모 목록
     * 
     * combine: 여러 Flow를 결합하여 하나의 Flow로 변환
     * - notes, searchQuery, sortOrder 중 하나라도 변경되면 재계산
     * 
     * stateIn: Flow를 StateFlow로 변환
     * - WhileSubscribed(5000): 구독자가 없으면 5초 후 중지
     */
    val filteredNotes: StateFlow<List<Note>> = combine(
        _notes,
        _searchQuery,
        _sortOrder
    ) { notes, query, order ->
        notes
            // 1. 검색 필터링
            .filter {
                query.isEmpty() ||  // 검색어가 없으면 모두 표시
                it.title.contains(query, ignoreCase = true) ||  // 제목에서 검색
                it.content.contains(query, ignoreCase = true)   // 내용에서 검색
            }
            // 2. 정렬
            .sortedWith(
                when (order) {
                    SortOrder.NEWEST_FIRST -> compareByDescending { it.createdAt }
                    SortOrder.OLDEST_FIRST -> compareBy { it.createdAt }
                    SortOrder.TITLE_ASC -> compareBy { it.title }
                    SortOrder.TITLE_DESC -> compareByDescending { it.title }
                }
            )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    /**
     * 메모 추가
     */
    fun addNote(title: String, content: String) {
        val newNote = Note(title = title, content = content)
        _notes.value = _notes.value + newNote  // 기존 리스트에 새 메모 추가
    }
    
    /**
     * 메모 수정
     */
    fun updateNote(id: String, title: String, content: String) {
        _notes.value = _notes.value.map {
            if (it.id == id) {
                // 해당 메모만 수정
                it.copy(
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis()  // 수정 시간 업데이트
                )
            } else it  // 다른 메모는 그대로 유지
        }
    }
    
    /**
     * 메모 삭제
     */
    fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }  // 해당 ID 제외
    }
    
    /**
     * 검색어 설정
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 정렬 순서 설정
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}
```

### 🎨 UI 구현

```kotlin
/**
 * 메모 목록 화면
 */
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel = viewModel()
) {
    // ViewModel State 수집
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    
    // UI 상태 (다이얼로그, 메뉴 표시 여부)
    var showAddDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메모") },
                actions = {
                    // 정렬 버튼
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, "정렬")
                    }
                    
                    // 정렬 메뉴
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("최신순") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NEWEST_FIRST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("오래된순") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.OLDEST_FIRST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("제목순 (A-Z)") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.TITLE_ASC)
                                showSortMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // 메모 추가 버튼
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "추가")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 검색 바
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("검색...") },
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            
            // 메모 목록
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("메모가 없습니다")
                }
            } else {
                LazyColumn {
                    // key를 지정하여 리스트 변경 시 성능 최적화
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onDelete = { viewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }
    
    // 메모 추가 다이얼로그
    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, content ->
                viewModel.addNote(title, content)
                showAddDialog = false
            }
        )
    }
}

/**
 * 메모 아이템 카드
 */
@Composable
fun NoteCard(
    note: Note,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 제목
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // 삭제 버튼
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "삭제")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 내용 (최대 3줄)
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 수정 시간
            Text(
                text = formatDate(note.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
```

---

## 프로젝트 2: 타이머 앱

### 🎯 프로젝트 개요

**난이도**: ⭐⭐⭐⭐  
**학습 내용**: LaunchedEffect, 백그라운드, 알림

**기능**:
- 시간 설정
- 시작/일시정지/초기화
- 백그라운드 동작
- 완료 시 알림

### 🏗️ ViewModel

```kotlin
/**
 * 타이머 ViewModel
 * 
 * 타이머 상태(남은 시간, 실행 여부)를 관리합니다.
 */
class TimerViewModel : ViewModel() {
    // 남은 시간 (초 단위)
    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()
    
    // 타이머 실행 여부
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    // 전체 시간 (초 단위)
    private val _totalTime = MutableStateFlow(60)
    val totalTime: StateFlow<Int> = _totalTime.asStateFlow()
    
    /**
     * 타이머 시간 설정
     * 
     * @param seconds 설정할 시간 (초)
     */
    fun setTime(seconds: Int) {
        _totalTime.value = seconds
        _timeLeft.value = seconds
    }
    
    /**
     * 타이머 시작
     */
    fun start() {
        _isRunning.value = true
    }
    
    /**
     * 타이머 일시정지
     */
    fun pause() {
        _isRunning.value = false
    }
    
    /**
     * 타이머 초기화
     * 
     * 실행을 멈추고 시간을 전체 시간으로 되돌립니다.
     */
    fun reset() {
        _isRunning.value = false
        _timeLeft.value = _totalTime.value
    }
    
    /**
     * 1초 감소 (LaunchedEffect에서 호출)
     * 
     * 남은 시간이 0이 되면 자동으로 멈춥니다.
     */
    fun tick() {
        if (_timeLeft.value > 0) {
            _timeLeft.value--  // 1초 감소
        } else {
            _isRunning.value = false  // 시간 종료 시 자동 정지
        }
    }
}
```

### 🎨 UI 구현

```kotlin
/**
 * 타이머 화면
 */
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel()
) {
    // ViewModel의 State를 Compose State로 변환
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    
    /**
     * 타이머 로직 (LaunchedEffect)
     * 
     * isRunning이 변경될 때마다 실행됩니다.
     * - true: while 루프 시작 (1초마다 tick 호출)
     * - false: while 루프 종료
     */
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)  // 1초 대기
            viewModel.tick()  // 1초 감소
        }
    }
    
    /**
     * 완료 시 알림 (LaunchedEffect)
     * 
     * timeLeft가 변경될 때마다 확인합니다.
     */
    LaunchedEffect(timeLeft) {
        if (timeLeft == 0 && !isRunning) {
            // 알림 표시 (실제로는 NotificationManager 사용)
            showNotification("타이머 완료!")
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 진행 표시
        CircularProgressIndicator(
            progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f,
            modifier = Modifier.size(200.dp),
            strokeWidth = 8.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 남은 시간
        Text(
            text = formatTime(timeLeft),
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 컨트롤 버튼
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (isRunning) viewModel.pause() else viewModel.start()
                }
            ) {
                Text(if (isRunning) "일시정지" else "시작")
            }
            
            Button(onClick = { viewModel.reset() }) {
                Text("초기화")
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

---

## 프로젝트 3: 채팅 앱 UI

### 🎯 프로젝트 개요

**난이도**: ⭐⭐⭐⭐  
**학습 내용**: Flow, 무한 스크롤, 성능 최적화

**기능**:
- 실시간 메시지 표시
- 무한 스크롤 (페이징)
- 입력 상태 관리
- 성능 최적화

### 📐 데이터 모델

```kotlin
/**
 * 메시지 데이터 클래스
 * 
 * @Immutable: 절대 변하지 않는 데이터임을 Compose에 알림
 * - Recomposition 최적화에 도움
 */
@Immutable
data class Message(
    val id: String,        // 메시지 고유 ID
    val text: String,      // 메시지 내용
    val senderId: String,  // 발신자 ID
    val timestamp: Long,   // 전송 시간 (밀리초)
    val isMe: Boolean      // 내가 보낸 메시지 여부
)
```

### 🏗️ ViewModel

```kotlin
/**
 * 채팅 ViewModel
 * 
 * 메시지 목록과 입력 상태를 관리합니다.
 */
class ChatViewModel : ViewModel() {
    // 메시지 목록
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    // 입력 중인 텍스트
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    init {
        /**
         * 실시간 메시지 구독 시뮬레이션
         * 
         * 실제 앱에서는:
         * - WebSocket으로 실시간 메시지 수신
         * - Firebase Realtime Database 구독
         * - Flow로 메시지 스트림 처리
         */
        viewModelScope.launch {
            delay(1000)  // 1초 후
            addMessage("안녕하세요!", false)  // 상대방 메시지 추가
        }
    }
    
    /**
     * 메시지 전송
     * 
     * 입력한 텍스트를 메시지로 추가하고 입력창을 비웁니다.
     */
    fun sendMessage() {
        if (_inputText.value.isBlank()) return  // 빈 메시지는 무시
        
        val message = Message(
            id = UUID.randomUUID().toString(),
            text = _inputText.value,
            senderId = "me",
            timestamp = System.currentTimeMillis(),
            isMe = true
        )
        
        _messages.value = _messages.value + message  // 메시지 추가
        _inputText.value = ""  // 입력창 비우기
    }
    
    /**
     * 입력 텍스트 업데이트
     */
    fun updateInput(text: String) {
        _inputText.value = text
    }
    
    /**
     * 메시지 추가 (내부 함수)
     * 
     * @param text 메시지 내용
     * @param isMe 내가 보낸 메시지 여부
     */
    private fun addMessage(text: String, isMe: Boolean) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            senderId = if (isMe) "me" else "other",
            timestamp = System.currentTimeMillis(),
            isMe = isMe
        )
        _messages.value = _messages.value + message
    }
}
```

### 🎨 UI 구현

```kotlin
/**
 * 채팅 화면
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    // State 수집
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    
    // LazyColumn의 스크롤 상태 기억
    val listState = rememberLazyListState()
    
    /**
     * 새 메시지 시 자동 스크롤
     * 
     * messages.size가 변경될 때마다 실행됩니다.
     * - 새 메시지가 추가되면 맨 아래로 스크롤
     */
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)  // 마지막 아이템으로 스크롤
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("채팅") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 메시지 목록
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    MessageBubble(message)
                }
            }
            
            // 입력 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("메시지 입력...") }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = { viewModel.sendMessage() }) {
                    Icon(Icons.Default.Send, "전송")
                }
            }
        }
    }
}

/**
 * 메시지 말풍선
 */
@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        // 내 메시지는 오른쪽, 상대방 메시지는 왼쪽 정렬
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                // 내 메시지는 Primary 색상, 상대방은 보조 컨테이너 색상
                containerColor = if (message.isMe) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // 메시지 텍스트
                Text(
                    text = message.text,
                    color = if (message.isMe) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 전송 시간
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isMe) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}
```

---

## 🎯 마무리

세 가지 실전 프로젝트를 완성했습니다!

### 배운 내용
- ✅ ViewModel과 StateFlow 활용
- ✅ 검색/정렬 구현 (combine)
- ✅ LaunchedEffect로 타이머 구현
- ✅ 실시간 데이터 처리
- ✅ 성능 최적화 (Key, Immutable)

### 다음 단계
1. 실제 백엔드 연동 (Firebase, REST API)
2. 로컬 저장소 추가 (Room, DataStore)
3. 테스트 작성
4. Google Play 배포

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy Project Building! 🚀
