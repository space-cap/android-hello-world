# Jetpack Compose State 실전 프로젝트

> 📖 **State 가이드 시리즈**
> - **04**: [State 완벽 가이드](./04-jetpack-compose-state-guide.md) - 기초부터 ViewModel까지
> - **04-1**: [State 고급 패턴](./04-1-jetpack-compose-state-advanced.md) - Side Effect, 성능 최적화
> - **04-2**: State 실전 프로젝트 (현재 문서) - 메모, 타이머, 채팅 앱

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

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    TITLE_ASC,
    TITLE_DESC
}
```

### 🏗️ ViewModel

```kotlin
class NoteViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    // 검색 및 정렬된 메모 (derivedStateOf 대신 combine 사용)
    val filteredNotes: StateFlow<List<Note>> = combine(
        _notes,
        _searchQuery,
        _sortOrder
    ) { notes, query, order ->
        notes
            .filter {
                query.isEmpty() ||
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
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
    
    fun addNote(title: String, content: String) {
        val newNote = Note(title = title, content = content)
        _notes.value = _notes.value + newNote
    }
    
    fun updateNote(id: String, title: String, content: String) {
        _notes.value = _notes.value.map {
            if (it.id == id) {
                it.copy(
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
    }
    
    fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}
```

### 🎨 UI 구현

```kotlin
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel = viewModel()
) {
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메모") },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, "정렬")
                    }
                    
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
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "삭제")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
class TimerViewModel : ViewModel() {
    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _totalTime = MutableStateFlow(60)
    val totalTime: StateFlow<Int> = _totalTime.asStateFlow()
    
    fun setTime(seconds: Int) {
        _totalTime.value = seconds
        _timeLeft.value = seconds
    }
    
    fun start() {
        _isRunning.value = true
    }
    
    fun pause() {
        _isRunning.value = false
    }
    
    fun reset() {
        _isRunning.value = false
        _timeLeft.value = _totalTime.value
    }
    
    fun tick() {
        if (_timeLeft.value > 0) {
            _timeLeft.value--
        } else {
            _isRunning.value = false
        }
    }
}
```

### 🎨 UI 구현

```kotlin
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel()
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    
    // 타이머 로직
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            viewModel.tick()
        }
    }
    
    // 완료 시 알림
    LaunchedEffect(timeLeft) {
        if (timeLeft == 0 && !isRunning) {
            // 알림 표시
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
@Immutable
data class Message(
    val id: String,
    val text: String,
    val senderId: String,
    val timestamp: Long,
    val isMe: Boolean
)
```

### 🏗️ ViewModel

```kotlin
class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    init {
        // 실시간 메시지 구독
        viewModelScope.launch {
            // 실제로는 WebSocket이나 Firebase 사용
            delay(1000)
            addMessage("안녕하세요!", false)
        }
    }
    
    fun sendMessage() {
        if (_inputText.value.isBlank()) return
        
        val message = Message(
            id = UUID.randomUUID().toString(),
            text = _inputText.value,
            senderId = "me",
            timestamp = System.currentTimeMillis(),
            isMe = true
        )
        
        _messages.value = _messages.value + message
        _inputText.value = ""
    }
    
    fun updateInput(text: String) {
        _inputText.value = text
    }
    
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
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val listState = rememberLazyListState()
    
    // 새 메시지 시 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isMe) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isMe) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
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
