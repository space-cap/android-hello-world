# Compose Multiplatform 네비게이션 및 화면 관리

## 목차
1. [Voyager 라이브러리 소개](#voyager-라이브러리-소개)
2. [기본 네비게이션](#기본-네비게이션)
3. [Tab 네비게이션](#tab-네비게이션)
4. [Bottom Navigation](#bottom-navigation)
5. [화면 전환 애니메이션](#화면-전환-애니메이션)
6. [딥링크 처리](#딥링크-처리)
7. [백 스택 관리](#백-스택-관리)
8. [실전 예제: 완전한 네비게이션 시스템](#실전-예제-완전한-네비게이션-시스템)

---

## Voyager 라이브러리 소개

**Voyager**는 Compose Multiplatform을 위한 네비게이션 라이브러리입니다.

### 주요 특징

- ✅ **멀티플랫폼 지원**: Android, iOS, Desktop 모두 지원
- ✅ **타입 안전성**: 컴파일 타임에 오류 검출
- ✅ **간단한 API**: 직관적이고 사용하기 쉬움
- ✅ **전환 애니메이션**: 커스텀 가능한 화면 전환
- ✅ **백 스택 관리**: 자동 백 스택 처리

### 의존성 추가

**composeApp/build.gradle.kts**:
```kotlin
sourceSets {
    val commonMain by getting {
        dependencies {
            // Voyager 코어
            implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
            
            // 화면 전환 애니메이션
            implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
            
            // Tab 네비게이션
            implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.0")
            
            // Bottom Sheet 네비게이션
            implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:1.0.0")
            
            // Koin 통합 (선택사항)
            implementation("cafe.adriel.voyager:voyager-koin:1.0.0")
        }
    }
}
```

---

## 기본 네비게이션

### Screen 인터페이스

```kotlin
import cafe.adriel.voyager.core.screen.Screen
import androidx.compose.runtime.Composable

/**
 * Voyager의 Screen 인터페이스
 * 모든 화면은 이 인터페이스를 구현해야 합니다
 */
interface Screen {
    /**
     * 화면의 UI를 정의하는 Composable 함수
     */
    @Composable
    fun Content()
}
```

### 첫 번째 Screen 만들기

```kotlin
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * 홈 화면
 */
class HomeScreen : Screen {
    @Composable
    override fun Content() {
        // Navigator 인스턴스 가져오기
        val navigator = LocalNavigator.currentOrThrow
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Home Screen",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 다른 화면으로 이동
            Button(onClick = {
                navigator.push(DetailScreen(id = 1))
            }) {
                Text("Go to Detail")
            }
        }
    }
}

/**
 * 상세 화면
 * 
 * @param id 아이템 ID
 */
data class DetailScreen(val id: Int) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Detail Screen",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Item ID: $id",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 뒤로 가기
            Button(onClick = {
                navigator.pop()
            }) {
                Text("Go Back")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 다른 상세 화면으로 이동
            Button(onClick = {
                navigator.push(DetailScreen(id = id + 1))
            }) {
                Text("Next Item")
            }
        }
    }
}
```

### Navigator 설정

```kotlin
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun App() {
    MaterialTheme {
        // Navigator로 앱 감싸기
        // 첫 화면으로 HomeScreen 설정
        Navigator(HomeScreen())
    }
}
```

### Navigator 메서드

```kotlin
@Composable
fun NavigatorMethodsExample() {
    val navigator = LocalNavigator.currentOrThrow
    
    Column {
        // 화면 추가 (스택에 푸시)
        Button(onClick = {
            navigator.push(DetailScreen(1))
        }) {
            Text("Push Screen")
        }
        
        // 여러 화면 한번에 추가
        Button(onClick = {
            navigator.push(
                listOf(
                    DetailScreen(1),
                    DetailScreen(2),
                    DetailScreen(3)
                )
            )
        }) {
            Text("Push Multiple Screens")
        }
        
        // 현재 화면 제거 (뒤로 가기)
        Button(onClick = {
            navigator.pop()
        }) {
            Text("Pop Screen")
        }
        
        // 현재 화면을 다른 화면으로 교체
        Button(onClick = {
            navigator.replace(DetailScreen(2))
        }) {
            Text("Replace Screen")
        }
        
        // 모든 화면 제거하고 새 화면으로 이동
        Button(onClick = {
            navigator.replaceAll(HomeScreen())
        }) {
            Text("Replace All")
        }
        
        // 특정 화면까지 뒤로 가기
        Button(onClick = {
            navigator.popUntil { screen ->
                screen is HomeScreen
            }
        }) {
            Text("Pop Until Home")
        }
        
        // 특정 화면까지 모두 제거하고 새 화면 추가
        Button(onClick = {
            navigator.popUntilRoot()  // 루트까지 제거
            navigator.push(DetailScreen(1))
        }) {
            Text("Pop to Root and Push")
        }
    }
}
```

---

## Tab 네비게이션

### Tab 인터페이스

```kotlin
import cafe.adriel.voyager.navigator.tab.Tab
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tab 인터페이스
 */
interface Tab {
    /**
     * Tab 옵션 (아이콘, 제목 등)
     */
    val options: TabOptions
        @Composable get
    
    /**
     * Tab 내용
     */
    @Composable
    fun Content()
}
```

### Tab 구현

```kotlin
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * 홈 탭
 */
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            
            return TabOptions(
                index = 0u,
                title = "Home",
                icon = icon
            )
        }
    
    @Composable
    override fun Content() {
        // Navigator를 사용하여 탭 내에서도 네비게이션 가능
        Navigator(HomeScreen())
    }
}

/**
 * 검색 탭
 */
object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Search)
            
            return TabOptions(
                index = 1u,
                title = "Search",
                icon = icon
            )
        }
    
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Search Tab",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 프로필 탭
 */
object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            
            return TabOptions(
                index = 2u,
                title = "Profile",
                icon = icon
            )
        }
    
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Profile Tab",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
```

### TabNavigator 사용

```kotlin
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator

@Composable
fun App() {
    MaterialTheme {
        // TabNavigator로 감싸기
        TabNavigator(HomeTab) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        // 탭 아이템들
                        TabNavigationItem(HomeTab)
                        TabNavigationItem(SearchTab)
                        TabNavigationItem(ProfileTab)
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    // 현재 선택된 탭의 Content 표시
                    CurrentTab()
                }
            }
        }
    }
}

/**
 * 탭 네비게이션 아이템
 */
@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                painter = tab.options.icon!!,
                contentDescription = tab.options.title
            )
        },
        label = { Text(tab.options.title) }
    )
}
```

---

## Bottom Navigation

### Bottom Navigation 구현

```kotlin
@Composable
fun BottomNavigationExample() {
    TabNavigator(HomeTab) { tabNavigator ->
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    // 모든 탭 표시
                    listOf(HomeTab, SearchTab, ProfileTab).forEach { tab ->
                        NavigationBarItem(
                            selected = tabNavigator.current.key == tab.key,
                            onClick = { tabNavigator.current = tab },
                            icon = {
                                Icon(
                                    painter = tab.options.icon!!,
                                    contentDescription = tab.options.title
                                )
                            },
                            label = { Text(tab.options.title) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CurrentTab()
            }
        }
    }
}
```

### Navigation Rail (Desktop/Tablet)

```kotlin
@Composable
fun NavigationRailExample() {
    TabNavigator(HomeTab) { tabNavigator ->
        Row(modifier = Modifier.fillMaxSize()) {
            // 왼쪽 Navigation Rail
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                listOf(HomeTab, SearchTab, ProfileTab).forEach { tab ->
                    NavigationRailItem(
                        selected = tabNavigator.current.key == tab.key,
                        onClick = { tabNavigator.current = tab },
                        icon = {
                            Icon(
                                painter = tab.options.icon!!,
                                contentDescription = tab.options.title
                            )
                        },
                        label = { Text(tab.options.title) }
                    )
                }
            }
            
            // 오른쪽 콘텐츠
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                CurrentTab()
            }
        }
    }
}
```

---

## 화면 전환 애니메이션

### 기본 전환 애니메이션

```kotlin
import cafe.adriel.voyager.transitions.SlideTransition
import cafe.adriel.voyager.transitions.FadeTransition
import cafe.adriel.voyager.transitions.ScaleTransition

@Composable
fun App() {
    MaterialTheme {
        Navigator(HomeScreen()) { navigator ->
            // 슬라이드 전환
            SlideTransition(navigator)
            
            // 또는 페이드 전환
            // FadeTransition(navigator)
            
            // 또는 스케일 전환
            // ScaleTransition(navigator)
        }
    }
}
```

### 커스텀 전환 애니메이션

```kotlin
import cafe.adriel.voyager.transitions.ScreenTransition
import androidx.compose.animation.*
import androidx.compose.animation.core.*

@Composable
fun CustomTransition(navigator: Navigator) {
    ScreenTransition(
        navigator = navigator,
        transition = {
            // 진입 애니메이션
            val enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
            
            // 퇴장 애니메이션
            val exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
            
            // 팝 진입 애니메이션 (뒤로 갈 때)
            val popEnter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
            
            // 팝 퇴장 애니메이션 (뒤로 갈 때)
            val popExit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
            
            ContentTransform(
                targetContentEnter = enter,
                initialContentExit = exit,
                targetContentZIndex = 1f
            )
        }
    )
}
```

---

## 딥링크 처리

### 딥링크 파싱

```kotlin
/**
 * 딥링크 파서
 */
object DeepLinkParser {
    /**
     * URL을 파싱하여 Screen 반환
     * 
     * @param url 딥링크 URL
     * @return 해당하는 Screen, 없으면 null
     */
    fun parse(url: String): Screen? {
        return when {
            url.startsWith("myapp://home") -> HomeScreen()
            
            url.startsWith("myapp://detail/") -> {
                val id = url.substringAfter("myapp://detail/").toIntOrNull()
                if (id != null) DetailScreen(id) else null
            }
            
            url.startsWith("myapp://profile") -> {
                // Profile 탭으로 이동하는 특별한 Screen
                ProfileDeepLinkScreen()
            }
            
            else -> null
        }
    }
}

/**
 * 프로필 딥링크 Screen
 */
class ProfileDeepLinkScreen : Screen {
    @Composable
    override fun Content() {
        // TabNavigator를 사용하여 Profile 탭으로 전환
        val tabNavigator = LocalTabNavigator.current
        
        LaunchedEffect(Unit) {
            tabNavigator.current = ProfileTab
        }
        
        // 빈 화면 (탭 전환 후 자동으로 Profile 탭 표시됨)
        Box(modifier = Modifier.fillMaxSize())
    }
}
```

### 딥링크 처리

```kotlin
@Composable
fun App() {
    MaterialTheme {
        Navigator(HomeScreen()) { navigator ->
            // 딥링크 처리
            HandleDeepLink(navigator)
            
            SlideTransition(navigator)
        }
    }
}

@Composable
fun HandleDeepLink(navigator: Navigator) {
    // 플랫폼별로 딥링크 URL 가져오기
    val deepLinkUrl = getDeepLinkUrl()
    
    LaunchedEffect(deepLinkUrl) {
        if (deepLinkUrl != null) {
            val screen = DeepLinkParser.parse(deepLinkUrl)
            if (screen != null) {
                navigator.push(screen)
            }
        }
    }
}

/**
 * 플랫폼별 딥링크 URL 가져오기
 */
expect fun getDeepLinkUrl(): String?
```

**Android 구현**:
```kotlin
// androidMain
actual fun getDeepLinkUrl(): String? {
    // Intent에서 딥링크 URL 추출
    // 실제로는 Activity에서 전달받아야 함
    return null
}
```

---

## 백 스택 관리

### 백 스택 상태 확인

```kotlin
@Composable
fun BackStackExample() {
    val navigator = LocalNavigator.currentOrThrow
    
    Column(modifier = Modifier.padding(16.dp)) {
        // 현재 백 스택 크기
        Text("Back stack size: ${navigator.size}")
        
        // 뒤로 갈 수 있는지 확인
        Text("Can go back: ${navigator.canPop}")
        
        // 현재 화면
        Text("Current screen: ${navigator.lastItem::class.simpleName}")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 백 스택의 모든 화면 표시
        Text("Back stack:", style = MaterialTheme.typography.titleMedium)
        navigator.items.forEachIndexed { index, screen ->
            Text("$index: ${screen::class.simpleName}")
        }
    }
}
```

### 백 핸들러 커스터마이징

```kotlin
@Composable
fun CustomBackHandlerExample() {
    val navigator = LocalNavigator.currentOrThrow
    var showExitDialog by remember { mutableStateOf(false) }
    
    // 뒤로 가기 버튼 처리
    BackHandler(enabled = !navigator.canPop) {
        // 루트 화면에서 뒤로 가기 시 종료 확인 다이얼로그 표시
        showExitDialog = true
    }
    
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App?") },
            text = { Text("Do you want to exit the app?") },
            confirmButton = {
                TextButton(onClick = {
                    // 앱 종료 로직
                    exitApp()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // 화면 내용...
}

/**
 * 앱 종료
 */
expect fun exitApp()
```

---

## 실전 예제: 완전한 네비게이션 시스템

### 앱 구조

```kotlin
/**
 * 메인 앱
 */
@Composable
fun App() {
    MaterialTheme {
        // 전체 앱을 TabNavigator로 감싸기
        TabNavigator(HomeTab) {
            MainScaffold()
        }
    }
}

/**
 * 메인 Scaffold
 */
@Composable
fun MainScaffold() {
    val tabNavigator = LocalTabNavigator.current
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                TabNavigationItem(HomeTab)
                TabNavigationItem(SearchTab)
                TabNavigationItem(ProfileTab)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CurrentTab()
        }
    }
}
```

### 홈 탭 (네비게이션 포함)

```kotlin
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Home",
            icon = rememberVectorPainter(Icons.Default.Home)
        )
    
    @Composable
    override fun Content() {
        // 탭 내에서 Navigator 사용
        Navigator(HomeListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

/**
 * 홈 리스트 화면
 */
class HomeListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val items = remember {
            (1..20).map { "Item #$it" }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items.size) { index ->
                    Card(
                        onClick = {
                            navigator.push(ItemDetailScreen(index + 1))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = items[index],
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 아이템 상세 화면
 */
data class ItemDetailScreen(val itemId: Int) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Item #$itemId") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Item Details",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("ID: $itemId")
                Text("Name: Item #$itemId")
                Text("Description: This is item number $itemId")
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        navigator.push(ItemDetailScreen(itemId + 1))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Item")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = {
                        navigator.popUntil { it is HomeListScreen }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to List")
                }
            }
        }
    }
}
```

### 검색 탭

```kotlin
object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Search",
            icon = rememberVectorPainter(Icons.Default.Search)
        )
    
    @Composable
    override fun Content() {
        var searchQuery by remember { mutableStateOf("") }
        val searchResults = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                (1..100).filter { "Item #$it".contains(searchQuery, ignoreCase = true) }
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Search") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 검색 바
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search items...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
                
                // 검색 결과
                if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults.size) { index ->
                            val itemId = searchResults[index]
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Item #$itemId",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### 프로필 탭

```kotlin
object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Profile",
            icon = rememberVectorPainter(Icons.Default.Person)
        )
    
    @Composable
    override fun Content() {
        Navigator(ProfileScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 프로필 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "John Doe",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "john.doe@example.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 메뉴 아이템들
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    onClick = {
                        navigator.push(SettingsScreen())
                    }
                )
                
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = { /* TODO */ }
                )
                
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("Settings content goes here")
            }
        }
    }
}
```

---

## 다음 단계

다음 문서에서는:
- **리소스 관리**
- **다국어 지원**
- **이미지 로딩**

를 다룹니다.

## 참고 자료

- [Voyager 공식 문서](https://voyager.adriel.cafe/)
- [Voyager GitHub](https://github.com/adrielcafe/voyager)
