# Compose Multiplatform 고급 가이드 (보강판)

> [!NOTE]
> **이 문서는 기존 Compose Multiplatform 고급 가이드를 대폭 보강한 버전입니다!**
> 
> 220줄 → 약 800줄로 확장하여 실전 활용 가능한 내용을 추가했습니다.

---

## 📚 목차

1. [성능 최적화](#성능-최적화)
2. [플랫폼별 최적화](#플랫폼별-최적화)
3. [테스팅 전략](#테스팅-전략)
4. [CI/CD 설정](#cicd-설정)
5. [배포](#배포)
6. [실전 프로젝트](#실전-프로젝트)

---

## 성능 최적화

### ⚡ Recomposition 최적화

```kotlin
/**
 * Recomposition 최적화 기법
 */
object RecompositionOptimization {
    
    /**
     * 1. remember를 활용한 계산 캐싱
     */
    @Composable
    fun ExpensiveCalculation(items: List<Item>) {
        // ❌ 나쁜 예: 매번 재계산
        val result = items.filter { it.isValid }.map { it.value }
        
        // ✅ 좋은 예: 캐싱
        val result = remember(items) {
            items.filter { it.isValid }.map { it.value }
        }
    }
    
    /**
     * 2. derivedStateOf 사용
     */
    @Composable
    fun FilteredList(items: List<Item>, query: String) {
        val filteredItems = remember(items, query) {
            derivedStateOf {
                items.filter { it.name.contains(query, ignoreCase = true) }
            }
        }.value
    }
    
    /**
     * 3. key를 사용한 리스트 최적화
     */
    @Composable
    fun OptimizedList(items: List<Item>) {
        LazyColumn {
            items(
                items = items,
                key = { it.id }  // 안정적인 키 제공
            ) { item ->
                ItemRow(item)
            }
        }
    }
    
    /**
     * 4. Immutable 데이터 클래스
     */
    @Immutable
    data class User(
        val id: String,
        val name: String
    )
}
```

### 🎯 LazyList 최적화

```kotlin
/**
 * LazyList 성능 최적화
 */
@Composable
fun OptimizedLazyList(items: List<Item>) {
    LazyColumn(
        // 1. contentPadding으로 오버스크롤 영역 확보
        contentPadding = PaddingValues(16.dp),
        
        // 2. verticalArrangement로 간격 설정
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { it.id },  // 안정적인 키
            contentType = { it.type }  // 타입별 재사용
        ) { item ->
            // 3. 무거운 작업은 remember로 캐싱
            val processedData = remember(item) {
                processItem(item)
            }
            
            ItemCard(processedData)
        }
    }
}
```

---

## 플랫폼별 최적화

### 🖥️ Desktop 최적화

```kotlin
/**
 * Desktop 전용 최적화
 */
@Composable
fun DesktopOptimizedUI() {
    // 1. 마우스 호버 효과
    var isHovered by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .background(
                if (isHovered) Color.LightGray else Color.White
            )
    ) {
        Text("Hover me!")
    }
    
    // 2. 키보드 단축키
    LaunchedEffect(Unit) {
        // Ctrl+S로 저장
        onKeyEvent { event ->
            if (event.isCtrlPressed && event.key == Key.S) {
                save()
                true
            } else {
                false
            }
        }
    }
    
    // 3. 창 크기 조절 감지
    val windowState = rememberWindowState()
    LaunchedEffect(windowState.size) {
        // 창 크기 변경 시 처리
        onWindowResize(windowState.size)
    }
}
```

### 📱 Mobile 최적화

```kotlin
/**
 * Mobile 전용 최적화
 */
@Composable
fun MobileOptimizedUI() {
    // 1. 터치 영역 최적화 (최소 48dp)
    Box(
        modifier = Modifier
            .size(48.dp)  // 최소 터치 영역
            .clickable { onClick() }
    )
    
    // 2. 스와이프 제스처
    val swipeableState = rememberSwipeableState(0)
    Box(
        modifier = Modifier
            .swipeable(
                state = swipeableState,
                anchors = mapOf(0f to 0, 1000f to 1),
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
    )
}
```

---

## 테스팅 전략

### 🧪 공통 코드 테스트

```kotlin
/**
 * 공통 모듈 단위 테스트
 */
class UserViewModelTest {
    
    @Test
    fun `사용자 로딩 테스트`() = runTest {
        // Given
        val repository = FakeUserRepository()
        val viewModel = UserViewModel(repository)
        
        // When
        viewModel.loadUsers()
        
        // Then
        assertEquals(3, viewModel.users.value.size)
    }
    
    @Test
    fun `에러 처리 테스트`() = runTest {
        // Given
        val repository = FakeUserRepository(shouldFail = true)
        val viewModel = UserViewModel(repository)
        
        // When
        viewModel.loadUsers()
        
        // Then
        assertTrue(viewModel.error.value != null)
    }
}

/**
 * Fake Repository (테스트용)
 */
class FakeUserRepository(
    private val shouldFail: Boolean = false
) : UserRepository {
    override suspend fun getUsers(): Result<List<User>> {
        return if (shouldFail) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(listOf(
                User("1", "Alice"),
                User("2", "Bob"),
                User("3", "Charlie")
            ))
        }
    }
}
```

### 🎨 UI 테스트

```kotlin
/**
 * Compose UI 테스트
 */
@Test
fun testUserList() = runComposeUiTest {
    setContent {
        val users = listOf(
            User("1", "Alice"),
            User("2", "Bob")
        )
        UserList(users)
    }
    
    // 사용자 이름 확인
    onNodeWithText("Alice").assertExists()
    onNodeWithText("Bob").assertExists()
    
    // 클릭 테스트
    onNodeWithText("Alice").performClick()
}
```

---

## CI/CD 설정

### 🔄 GitHub Actions

```yaml
# .github/workflows/build.yml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: macos-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build Android
        run: ./gradlew :composeApp:assembleDebug
      
      - name: Run Tests
        run: ./gradlew :shared:allTests
      
      - name: Build iOS
        run: ./gradlew :composeApp:linkDebugFrameworkIosArm64
      
      - name: Build Desktop
        run: ./gradlew :composeApp:packageDistributionForCurrentOS
```

---

## 배포

### 📦 Android 배포

```bash
# Release APK 빌드
./gradlew :composeApp:assembleRelease

# AAB 빌드 (Google Play)
./gradlew :composeApp:bundleRelease
```

### 🍎 iOS 배포

```bash
# Xcode에서:
# 1. Product → Archive
# 2. Distribute App
# 3. App Store Connect 업로드
```

### 🖥️ Desktop 배포

```bash
# 현재 OS용 패키지 생성
./gradlew :composeApp:packageDistributionForCurrentOS

# 결과물:
# - Windows: .msi, .exe
# - macOS: .dmg
# - Linux: .deb, .rpm
```

---

## 실전 프로젝트

### 📝 크로스 플랫폼 메모 앱

```kotlin
/**
 * 공통 ViewModel
 */
class NoteViewModel(
    private val repository: NoteRepository
) {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    fun loadNotes() {
        viewModelScope.launch {
            repository.getNotes().collect { notes ->
                _notes.value = notes
            }
        }
    }
    
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                createdAt = Clock.System.now()
            )
            repository.addNote(note)
        }
    }
}

/**
 * 공통 UI
 */
@Composable
fun NoteApp(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 새 노트 */ }
            ) {
                Icon(Icons.Default.Add, "새 노트")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(notes, key = { it.id }) { note ->
                NoteCard(note)
            }
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 코드 구조

```
composeApp/
├── src/commonMain/
│   ├── kotlin/
│   │   ├── ui/              # UI 레이어
│   │   ├── domain/          # 비즈니스 로직
│   │   ├── data/            # 데이터 레이어
│   │   └── di/              # 의존성 주입
```

### 2. 상태 관리

- ViewModel 사용
- 단방향 데이터 플로우
- Immutable 데이터 모델

### 3. 에러 처리

- Result 패턴 사용
- 사용자 친화적 에러 메시지
- 로깅 및 모니터링

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant

Happy Multiplatform Development! 🚀
