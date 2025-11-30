# 오프라인 우선 아키텍처 완벽 가이드

## 📚 목차

1. [오프라인 우선이란?](#오프라인-우선이란)
2. [Single Source of Truth](#single-source-of-truth)
3. [Room Database 심화](#room-database-심화)
4. [동기화 전략](#동기화-전략)
5. [충돌 해결](#충돌-해결)
6. [WorkManager 심화](#workmanager-심화)
7. [실전 예제](#실전-예제)

---

## 오프라인 우선이란?

> [!IMPORTANT]
> **오프라인 우선 = 네트워크 없이도 앱이 작동합니다**
> 
> **전통적인 방식:**
> ```
> 앱 → 네트워크 → 서버 → 데이터
>      ↓ (연결 끊김)
>    에러!
> ```
> 
> **오프라인 우선:**
> ```
> 앱 → 로컬 DB → 데이터 (즉시!)
>      ↓ (백그라운드)
>    서버 동기화
> ```

### 왜 오프라인 우선이 중요한가?

**사용자 경험:**
```
일반 앱:
지하철 진입 → 네트워크 끊김 → 앱 사용 불가 → 답답함

오프라인 우선 앱:
지하철 진입 → 네트워크 끊김 → 앱 정상 작동 → 만족!
```

**실제 통계:**
- 모바일 사용자의 **23%**가 불안정한 네트워크 환경
- 오프라인 지원 앱의 사용자 만족도: **40% 높음**
- 사용 시간: **평균 2배 증가**

**성공 사례:**
- **Google Maps**: 오프라인 지도
- **Spotify**: 오프라인 음악
- **Notion**: 오프라인 노트

---

## Single Source of Truth

> [!NOTE]
> **SSOT (Single Source of Truth) = 단일 진실 공급원**
> 
> **핵심 원칙:**
> - 데이터는 **하나의 소스**에서만 제공
> - UI는 **항상 같은 곳**에서 데이터를 읽음
> - 데이터 일관성 보장

### SSOT 패턴

**잘못된 방법:**
```kotlin
// ❌ 여러 소스에서 데이터 읽기
@Composable
fun UserScreen(viewModel: UserViewModel) {
    var user by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(Unit) {
        // 네트워크에서 직접 읽기
        user = api.getUser()
    }
    
    // 문제: 
    // 1. 오프라인 시 동작 안함
    // 2. 캐시 없음
    // 3. 데이터 일관성 없음
}
```

**올바른 방법 (SSOT):**
```kotlin
// ✅ Repository가 SSOT
class UserRepository(
    private val api: ApiService,
    private val dao: UserDao
) {
    // 데이터는 항상 DB에서 (SSOT)
    fun getUser(userId: Int): Flow<User?> {
        return dao.getUserFlow(userId)
    }
    
    // 네트워크에서 가져와서 DB에 저장
    suspend fun refreshUser(userId: Int) {
        try {
            val user = api.getUser(userId)
            dao.insertUser(user)  // DB 업데이트
        } catch (e: Exception) {
            // 네트워크 실패해도 DB 데이터는 유효
        }
    }
}

@Composable
fun UserScreen(viewModel: UserViewModel) {
    // UI는 항상 DB에서만 읽음 (SSOT)
    val user by viewModel.user.collectAsState()
    
    LaunchedEffect(Unit) {
        // 백그라운드에서 새로고침
        viewModel.refreshUser()
    }
    
    // user는 항상 최신 DB 데이터
    // 오프라인이어도 마지막 데이터 표시
}
```

**데이터 흐름:**
```
┌─────────────────────────────────────┐
│           UI (Compose)              │
│                ↑                    │
│         Flow<User> (읽기만)         │
│                ↑                    │
│         Room Database               │
│         (Single Source)             │
│                ↑                    │
│         Repository                  │
│         ↙          ↘                │
│    Network      Cache               │
└─────────────────────────────────────┘
```

---

## Room Database 심화

### Flow를 사용한 반응형 데이터

**왜 Flow를 사용하는가?**
```
일반 함수:
fun getUser(): User
→ 한 번만 읽음
→ 데이터 변경 시 UI 업데이트 안됨

Flow:
fun getUser(): Flow<User>
→ 데이터 변경 시 자동으로 emit
→ UI 자동 업데이트!
```

```kotlin
@Dao
interface ArticleDao {
    // Flow 사용 - 데이터 변경 시 자동 emit
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticlesFlow(): Flow<List<Article>>
    
    // 특정 조건
    @Query("SELECT * FROM articles WHERE category = :category")
    fun getArticlesByCategory(category: String): Flow<List<Article>>
    
    // 검색
    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%'")
    fun searchArticles(query: String): Flow<List<Article>>
    
    // 삽입/업데이트/삭제
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<Article>)
    
    @Update
    suspend fun updateArticle(article: Article)
    
    @Delete
    suspend fun deleteArticle(article: Article)
}
```

### Repository 패턴

```kotlin
class ArticleRepository(
    private val api: NewsApiService,
    private val dao: ArticleDao
) {
    // SSOT: DB에서만 읽기
    fun getArticles(category: String): Flow<List<Article>> {
        return dao.getArticlesByCategory(category)
    }
    
    // 네트워크에서 새로고침
    suspend fun refreshArticles(category: String): Result<Unit> {
        return try {
            // 1. API에서 데이터 가져오기
            val response = api.getArticles(category)
            
            // 2. DB에 저장 (SSOT 업데이트)
            dao.insertArticles(response.articles)
            
            Result.success(Unit)
        } catch (e: Exception) {
            // 네트워크 실패해도 DB 데이터는 유효
            Result.failure(e)
        }
    }
}
```

### ViewModel

```kotlin
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {
    
    private val _category = MutableStateFlow("technology")
    
    // DB에서 자동으로 업데이트되는 데이터
    val articles: StateFlow<List<Article>> = _category
        .flatMapLatest { category ->
            repository.getArticles(category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 새로고침 상태
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // 에러 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 새로고침
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            repository.refreshArticles(_category.value)
                .onSuccess {
                    // 성공 - DB가 자동으로 업데이트되어 UI 갱신
                }
                .onFailure { e ->
                    _error.value = e.message
                }
            
            _isRefreshing.value = false
        }
    }
    
    fun selectCategory(category: String) {
        _category.value = category
    }
}
```

### UI

```kotlin
@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // 초기 로드
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    
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
        LazyColumn {
            items(articles) { article ->
                ArticleCard(article)
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        error?.let {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(it)
            }
        }
    }
}
```

**동작 흐름:**
```
앱 시작
    ↓
DB에서 데이터 읽기 (즉시 표시)
    ↓
백그라운드에서 API 호출
    ↓
새 데이터를 DB에 저장
    ↓
Flow가 자동으로 emit
    ↓
UI 자동 업데이트!
```

---

## 동기화 전략

### 1. 즉시 동기화 (Eager Sync)

**언제 사용?**
- 중요한 데이터
- 실시간성이 중요한 경우

```kotlin
class EagerSyncRepository(
    private val api: ApiService,
    private val dao: DataDao
) {
    suspend fun saveData(data: Data) {
        // 1. 로컬에 즉시 저장 (오프라인 지원)
        dao.insert(data)
        
        // 2. 즉시 서버에 동기화 시도
        try {
            api.uploadData(data)
            dao.markAsSynced(data.id)
        } catch (e: Exception) {
            // 실패 시 나중에 재시도
            dao.markAsPendingSync(data.id)
        }
    }
}
```

### 2. 지연 동기화 (Lazy Sync)

**언제 사용?**
- 덜 중요한 데이터
- 배터리 절약이 중요한 경우

```kotlin
class LazySyncRepository(
    private val api: ApiService,
    private val dao: DataDao,
    private val workManager: WorkManager
) {
    suspend fun saveData(data: Data) {
        // 1. 로컬에만 저장
        dao.insert(data)
        
        // 2. WorkManager로 나중에 동기화 예약
        val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        
        workManager.enqueue(syncWork)
    }
}
```

### 3. 배치 동기화 (Batch Sync)

**언제 사용?**
- 대량의 데이터
- 네트워크 효율성이 중요한 경우

```kotlin
class BatchSyncRepository(
    private val api: ApiService,
    private val dao: DataDao
) {
    suspend fun syncPendingData() {
        // 1. 동기화 대기 중인 데이터 모두 가져오기
        val pendingData = dao.getPendingSync()
        
        if (pendingData.isEmpty()) return
        
        // 2. 한 번에 전송
        try {
            api.uploadBatch(pendingData)
            
            // 3. 성공 시 모두 동기화 완료 표시
            dao.markAllAsSynced(pendingData.map { it.id })
        } catch (e: Exception) {
            // 실패 시 그대로 유지 (나중에 재시도)
        }
    }
}
```

---

## 충돌 해결

> [!WARNING]
> **충돌이 발생하는 경우**
> 
> ```
> 사용자 A: 문서 수정 (오프라인)
> 사용자 B: 같은 문서 수정 (오프라인)
>     ↓
> 둘 다 온라인 복귀
>     ↓
> 충돌! 어떤 버전이 맞는가?
> ```

### 충돌 해결 전략

#### 1. Last Write Wins (마지막 쓰기 우선)

**가장 간단하지만 데이터 손실 가능**

```kotlin
data class Article(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long  // 타임스탬프
)

suspend fun syncArticle(local: Article) {
    try {
        // 서버에서 최신 버전 가져오기
        val server = api.getArticle(local.id)
        
        if (local.updatedAt > server.updatedAt) {
            // 로컬이 더 최신 → 서버에 업로드
            api.updateArticle(local)
        } else {
            // 서버가 더 최신 → 로컬 업데이트
            dao.updateArticle(server)
        }
    } catch (e: Exception) {
        // 네트워크 에러
    }
}
```

**문제점:**
```
사용자 A: 제목 수정 (10:00)
사용자 B: 내용 수정 (10:01)
    ↓
B의 변경사항만 남음
A의 제목 수정은 손실!
```

#### 2. Version Vector (버전 벡터)

**각 필드별로 버전 관리**

```kotlin
data class Article(
    val id: String,
    val title: String,
    val titleVersion: Int,
    val content: String,
    val contentVersion: Int
)

suspend fun syncArticle(local: Article) {
    val server = api.getArticle(local.id)
    
    val merged = Article(
        id = local.id,
        // 각 필드별로 최신 버전 선택
        title = if (local.titleVersion > server.titleVersion) {
            local.title
        } else {
            server.title
        },
        titleVersion = maxOf(local.titleVersion, server.titleVersion),
        content = if (local.contentVersion > server.contentVersion) {
            local.content
        } else {
            server.content
        },
        contentVersion = maxOf(local.contentVersion, server.contentVersion)
    )
    
    dao.updateArticle(merged)
    api.updateArticle(merged)
}
```

#### 3. Operational Transformation (OT)

**실시간 협업 (Google Docs 방식)**

```kotlin
// 매우 복잡하므로 라이브러리 사용 권장
// 예: ShareDB, Yjs

sealed class Operation {
    data class Insert(val position: Int, val text: String) : Operation()
    data class Delete(val position: Int, val length: Int) : Operation()
}

fun transform(op1: Operation, op2: Operation): Pair<Operation, Operation> {
    // 두 작업을 변환하여 충돌 해결
    // 매우 복잡한 로직...
}
```

#### 4. 사용자에게 선택 요청

**가장 안전하지만 UX 저하**

```kotlin
@Composable
fun ConflictResolutionDialog(
    local: Article,
    server: Article,
    onResolve: (Article) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("충돌 발생") },
        text = {
            Column {
                Text("로컬 버전:")
                Text(local.content)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("서버 버전:")
                Text(server.content)
            }
        },
        confirmButton = {
            Button(onClick = { onResolve(local) }) {
                Text("로컬 사용")
            }
        },
        dismissButton = {
            Button(onClick = { onResolve(server) }) {
                Text("서버 사용")
            }
        }
    )
}
```

---

## WorkManager 심화

> [!TIP]
> **WorkManager는 백그라운드 작업을 안정적으로 실행합니다**
> 
> **특징:**
> - 앱 종료되어도 실행
> - 재부팅 후에도 실행
> - 조건 설정 가능 (WiFi, 충전 중 등)

### 동기화 Worker

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // 1. 동기화 대기 중인 데이터 가져오기
            val pendingData = dao.getPendingSync()
            
            if (pendingData.isEmpty()) {
                return Result.success()
            }
            
            // 2. 서버에 업로드
            api.uploadBatch(pendingData)
            
            // 3. 성공 표시
            dao.markAllAsSynced(pendingData.map { it.id })
            
            Result.success()
        } catch (e: Exception) {
            // 재시도
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
```

### 주기적 동기화

```kotlin
fun scheduleSyncWork(context: Context) {
    val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
        repeatInterval = 15,
        repeatIntervalTimeUnit = TimeUnit.MINUTES
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()
    
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
}
```

**백오프 정책:**
```
1차 실패 → 30초 후 재시도
2차 실패 → 1분 후 재시도
3차 실패 → 2분 후 재시도
4차 실패 → 4분 후 재시도
...
```

---

## 실전 예제

### 완전한 오프라인 우선 노트 앱

```kotlin
// 1. Entity
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

// 2. DAO
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotesFlow(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getPendingSync(): List<Note>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("UPDATE notes SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}

// 3. Repository
class NoteRepository(
    private val api: NoteApiService,
    private val dao: NoteDao
) {
    // SSOT: DB에서만 읽기
    fun getNotes(): Flow<List<Note>> {
        return dao.getAllNotesFlow()
    }
    
    // 노트 저장 (오프라인 우선)
    suspend fun saveNote(note: Note) {
        // 1. 로컬에 즉시 저장
        dao.insertNote(note.copy(isSynced = false))
        
        // 2. 서버에 동기화 시도
        try {
            api.uploadNote(note)
            dao.insertNote(note.copy(isSynced = true))
        } catch (e: Exception) {
            // 실패해도 로컬에는 저장됨
        }
    }
    
    // 서버에서 새로고침
    suspend fun refreshNotes() {
        try {
            val serverNotes = api.getNotes()
            serverNotes.forEach { note ->
                dao.insertNote(note.copy(isSynced = true))
            }
        } catch (e: Exception) {
            // 실패해도 로컬 데이터는 유효
        }
    }
    
    // 대기 중인 노트 동기화
    suspend fun syncPendingNotes() {
        val pending = dao.getPendingSync()
        if (pending.isEmpty()) return
        
        try {
            api.uploadBatch(pending)
            dao.markAsSynced(pending.map { it.id })
        } catch (e: Exception) {
            // 나중에 재시도
        }
    }
}

// 4. ViewModel
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    
    val notes: StateFlow<List<Note>> = repository.getNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content
            )
            repository.saveNote(note)
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            repository.refreshNotes()
        }
    }
}

// 5. UI
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 새 노트 */ }
            ) {
                Icon(Icons.Filled.Add, "추가")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(notes) { note ->
                NoteCard(
                    note = note,
                    showSyncStatus = !note.isSynced
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, showSyncStatus: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (showSyncStatus) {
                    Icon(
                        imageVector = Icons.Filled.CloudOff,
                        contentDescription = "동기화 대기",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

---

## 💡 오프라인 우선 베스트 프랙티스

### 1. 사용자에게 상태 표시

```kotlin
@Composable
fun SyncStatusIndicator(isSynced: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isSynced) {
                Icons.Filled.CloudDone
            } else {
                Icons.Filled.CloudOff
            },
            contentDescription = null,
            tint = if (isSynced) Color.Green else Color.Gray
        )
        
        Text(
            text = if (isSynced) "동기화됨" else "동기화 대기",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
```

### 2. 낙관적 UI 업데이트

```kotlin
fun deleteNote(note: Note) {
    viewModelScope.launch {
        // 1. UI에서 즉시 제거 (낙관적)
        dao.deleteNote(note)
        
        // 2. 서버에 삭제 요청
        try {
            api.deleteNote(note.id)
        } catch (e: Exception) {
            // 실패 시 복원
            dao.insertNote(note)
            // 사용자에게 에러 표시
        }
    }
}
```

### 3. 적절한 캐시 정책

```kotlin
// 자주 변경되지 않는 데이터
suspend fun getCategories(): List<Category> {
    val cached = dao.getCategories()
    
    // 캐시가 있고 1시간 이내면 사용
    if (cached.isNotEmpty() && 
        System.currentTimeMillis() - cached.first().cachedAt < 3600000) {
        return cached
    }
    
    // 아니면 새로 가져오기
    val fresh = api.getCategories()
    dao.insertCategories(fresh)
    return fresh
}
```

---

## 🎯 체크리스트

### 기본 오프라인 지원
- [ ] Room Database 설정
- [ ] Repository 패턴 구현
- [ ] Flow를 사용한 반응형 데이터
- [ ] 네트워크 에러 처리

### 고급 동기화
- [ ] 동기화 상태 표시
- [ ] 충돌 해결 전략
- [ ] WorkManager 백그라운드 동기화
- [ ] 배치 동기화

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Work Offline, Sync Later! 📴
