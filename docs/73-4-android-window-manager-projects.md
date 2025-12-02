# Android WindowManager 실전 프로젝트

> 📖 **시리즈 구성**
> - **73-1**: [폴더블의 역사](./73-1-foldable-history.md)
> - **73-2**: [WindowManager 기본 가이드](./73-2-android-window-manager-basics.md)
> - **73-3**: [WindowManager 고급 가이드](./73-3-android-window-manager-advanced.md)
> - **73-4**: WindowManager 실전 프로젝트 (현재 문서)

---

## 📚 목차

1. [프로젝트 1: 이메일 앱](#프로젝트-1-이메일-앱)
2. [프로젝트 2: 뉴스 앱](#프로젝트-2-뉴스-앱)

---

## 프로젝트 1: 이메일 앱

### 🎯 Master-Detail 패턴 구현

```kotlin
/**
 * 이메일 앱 - Master-Detail 패턴
 */
@Composable
fun EmailApp() {
    val windowSizeClass = calculateWindowSizeClass()
    var selectedEmail by remember { mutableStateOf<Email?>(null) }
    val emails = remember { getEmailList() }
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 스마트폰: 네비게이션
            if (selectedEmail == null) {
                EmailList(emails) { selectedEmail = it }
            } else {
                EmailDetail(selectedEmail!!) { selectedEmail = null }
            }
        }
        
        WindowWidthSizeClass.Expanded -> {
            // 태블릿: 듀얼 패널
            Row {
                EmailList(
                    emails = emails,
                    onEmailClick = { selectedEmail = it },
                    modifier = Modifier.weight(0.4f)
                )
                selectedEmail?.let { email ->
                    EmailDetail(
                        email = email,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmailList(
    emails: List<Email>,
    onEmailClick: (Email) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(emails) { email ->
            EmailListItem(email, onClick = { onEmailClick(email) })
        }
    }
}

@Composable
fun EmailDetail(
    email: Email,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        onBack?.let {
            IconButton(onClick = it) {
                Icon(Icons.Default.ArrowBack, "뒤로")
            }
        }
        Text(email.subject, style = MaterialTheme.typography.headlineSmall)
        Text(email.sender, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(email.body)
    }
}

data class Email(
    val id: String,
    val subject: String,
    val sender: String,
    val body: String
)
```

---

## 프로젝트 2: 뉴스 앱

### 📰 List-Detail 패턴 구현

```kotlin
/**
 * 뉴스 앱 - List-Detail 패턴
 */
@Composable
fun NewsApp() {
    val windowSizeClass = calculateWindowSizeClass()
    val categories = remember { getNewsCategories() }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 스마트폰: 탭 네비게이션
            Column {
                ScrollableTabRow(selectedTabIndex = categories.indexOf(selectedCategory)) {
                    categories.forEach { category ->
                        Tab(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            text = { Text(category.name) }
                        )
                    }
                }
                NewsList(selectedCategory.articles)
            }
        }
        
        WindowWidthSizeClass.Expanded -> {
            // 태블릿: 사이드바 + 콘텐츠
            Row {
                NavigationRail(modifier = Modifier.width(200.dp)) {
                    categories.forEach { category ->
                        NavigationRailItem(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            icon = { Icon(category.icon, null) },
                            label = { Text(category.name) }
                        )
                    }
                }
                NewsList(
                    articles = selectedCategory.articles,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun NewsList(articles: List<Article>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(articles) { article ->
            ArticleCard(article)
        }
    }
}

data class NewsCategory(
    val name: String,
    val icon: ImageVector,
    val articles: List<Article>
)

data class Article(
    val title: String,
    val summary: String,
    val imageUrl: String
)
```

---

**마지막 업데이트**: 2024-12-02  
**작성자**: Antigravity AI Assistant
