# Jetpack Compose Layout & UI Components 완벽 가이드

## 📚 목차
1. [Compose UI 기초](#compose-ui-기초)
2. [Layout Composables](#layout-composables)
3. [기본 UI Components](#기본-ui-components)
4. [Modifier 시스템](#modifier-시스템)
5. [실전 레이아웃 패턴](#실전-레이아웃-패턴)
6. [성능 최적화](#성능-최적화)
7. [실습 프로젝트](#실습-프로젝트)

---

## Compose UI 기초

### Declarative UI 패러다임

Jetpack Compose는 **선언형 UI(Declarative UI)** 프레임워크입니다.

#### 명령형 vs 선언형

| 명령형 UI (XML) | 선언형 UI (Compose) |
|----------------|-------------------|
| **어떻게(How)** 그릴지 명령 | **무엇을(What)** 그릴지 선언 |
| View 객체를 직접 조작 | State에 따라 UI 자동 생성 |
| `findViewById()`, `setText()` | `Text(text = state)` |

```kotlin
// ❌ 명령형 (XML + Kotlin)
val textView = findViewById<TextView>(R.id.textView)
textView.text = "Hello"
textView.setTextColor(Color.BLUE)

// ✅ 선언형 (Compose)
Text(
    text = "Hello",
    color = Color.Blue
)
```

### Composable 함수

모든 UI 요소는 `@Composable` 함수로 정의됩니다.

```kotlin
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name!")
}
```

**핵심 특징**:
- 함수 이름은 **대문자로 시작** (Pascal Case)
- 반환값이 없음 (`Unit`)
- `@Composable` 어노테이션 필수
- 다른 Composable 함수 내에서만 호출 가능

---

## Layout Composables

레이아웃 Composable은 자식 요소들을 어떻게 배치할지 결정합니다.

### 1. Column - 세로 배치

자식 요소들을 **세로(수직)**로 쌓습니다.

```kotlin
@Composable
fun ColumnExample() {
    Column {
        Text("첫 번째")
        Text("두 번째")
        Text("세 번째")
    }
}
```

**결과**:
```
┌─────────┐
│첫 번째   │
│두 번째   │
│세 번째   │
└─────────┘
```

#### Column 주요 파라미터

```kotlin
Column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    // 자식 요소들
}
```

| 파라미터 | 설명 | 기본값 |
|---------|------|--------|
| `modifier` | 스타일 및 동작 정의 | `Modifier` |
| `verticalArrangement` | 세로 방향 배치 방식 | `Arrangement.Top` |
| `horizontalAlignment` | 가로 방향 정렬 | `Alignment.Start` |

#### verticalArrangement 옵션

```kotlin
// 위쪽 정렬 (기본값)
Column(verticalArrangement = Arrangement.Top) { }

// 중앙 정렬
Column(verticalArrangement = Arrangement.Center) { }

// 아래쪽 정렬
Column(verticalArrangement = Arrangement.Bottom) { }

// 균등 분배 (요소 사이 간격 동일)
Column(verticalArrangement = Arrangement.SpaceBetween) { }

// 균등 분배 (양 끝 포함)
Column(verticalArrangement = Arrangement.SpaceEvenly) { }

// 균등 분배 (요소 주변 간격)
Column(verticalArrangement = Arrangement.SpaceAround) { }

// 고정 간격
Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { }
```

#### horizontalAlignment 옵션

```kotlin
// 왼쪽 정렬 (기본값)
Column(horizontalAlignment = Alignment.Start) { }

// 중앙 정렬
Column(horizontalAlignment = Alignment.CenterHorizontally) { }

// 오른쪽 정렬
Column(horizontalAlignment = Alignment.End) { }
```

#### 실전 예제: 로그인 화면

```kotlin
@Composable
fun LoginScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "로그인",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }
    }
}
```

---

### 2. Row - 가로 배치

자식 요소들을 **가로(수평)**로 나열합니다.

```kotlin
@Composable
fun RowExample() {
    Row {
        Text("A")
        Text("B")
        Text("C")
    }
}
```

**결과**:
```
┌─────────┐
│A  B  C  │
└─────────┘
```

#### Row 주요 파라미터

```kotlin
Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top
) {
    // 자식 요소들
}
```

#### horizontalArrangement 옵션

```kotlin
// 왼쪽 정렬 (기본값)
Row(horizontalArrangement = Arrangement.Start) { }

// 중앙 정렬
Row(horizontalArrangement = Arrangement.Center) { }

// 오른쪽 정렬
Row(horizontalArrangement = Arrangement.End) { }

// 균등 분배
Row(horizontalArrangement = Arrangement.SpaceBetween) { }
Row(horizontalArrangement = Arrangement.SpaceEvenly) { }
Row(horizontalArrangement = Arrangement.SpaceAround) { }

// 고정 간격
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { }
```

#### verticalAlignment 옵션

```kotlin
// 위쪽 정렬 (기본값)
Row(verticalAlignment = Alignment.Top) { }

// 중앙 정렬
Row(verticalAlignment = Alignment.CenterVertically) { }

// 아래쪽 정렬
Row(verticalAlignment = Alignment.Bottom) { }
```

#### 실전 예제: 프로필 카드

```kotlin
@Composable
fun ProfileCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지 (원형)
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.Gray, CircleShape)
        )
        
        // 사용자 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "홍길동",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Android Developer",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        // 팔로우 버튼
        Button(onClick = {}) {
            Text("팔로우")
        }
    }
}
```

---

### 3. Box - 겹쳐서 배치

자식 요소들을 **겹쳐서(Z축)** 배치합니다.

```kotlin
@Composable
fun BoxExample() {
    Box {
        Text("뒤")
        Text("앞")
    }
}
```

**결과**: "앞" 텍스트가 "뒤" 텍스트 위에 표시됨

#### Box 주요 파라미터

```kotlin
Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart
) {
    // 자식 요소들
}
```

#### contentAlignment 옵션

```kotlin
// 9가지 정렬 옵션
Box(contentAlignment = Alignment.TopStart) { }      // 왼쪽 위
Box(contentAlignment = Alignment.TopCenter) { }     // 가운데 위
Box(contentAlignment = Alignment.TopEnd) { }        // 오른쪽 위

Box(contentAlignment = Alignment.CenterStart) { }   // 왼쪽 중앙
Box(contentAlignment = Alignment.Center) { }        // 정중앙
Box(contentAlignment = Alignment.CenterEnd) { }     // 오른쪽 중앙

Box(contentAlignment = Alignment.BottomStart) { }   // 왼쪽 아래
Box(contentAlignment = Alignment.BottomCenter) { }  // 가운데 아래
Box(contentAlignment = Alignment.BottomEnd) { }     // 오른쪽 아래
```

#### 실전 예제: 배지가 있는 아이콘

```kotlin
@Composable
fun IconWithBadge() {
    Box {
        // 메인 아이콘
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "알림",
            modifier = Modifier.size(32.dp)
        )
        
        // 배지 (오른쪽 위)
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.Red, CircleShape)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "5",
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}
```

---

### 4. LazyColumn / LazyRow - 스크롤 가능한 리스트

많은 아이템을 효율적으로 표시하는 스크롤 가능한 리스트입니다.

> [!IMPORTANT]
> **Lazy의 의미**
> - 화면에 보이는 아이템만 렌더링 (성능 최적화)
> - RecyclerView의 Compose 버전
> - 수천 개의 아이템도 부드럽게 스크롤

#### LazyColumn 기본 사용법

```kotlin
@Composable
fun ContactList() {
    val contacts = listOf("Alice", "Bob", "Charlie", "David", "Eve")
    
    LazyColumn {
        items(contacts) { name ->
            Text(
                text = name,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
```

#### items() 함수 변형

```kotlin
// 1. List를 직접 전달
items(contacts) { contact ->
    ContactItem(contact)
}

// 2. 개수만 지정
items(100) { index ->
    Text("Item $index")
}

// 3. key를 지정 (성능 최적화)
items(
    items = contacts,
    key = { it.id }
) { contact ->
    ContactItem(contact)
}

// 4. itemsIndexed (인덱스 필요 시)
itemsIndexed(contacts) { index, contact ->
    Text("$index: $contact")
}
```

#### 실전 예제: 메시지 리스트

```kotlin
data class Message(
    val id: Int,
    val sender: String,
    val content: String,
    val time: String
)

@Composable
fun MessageList() {
    val messages = remember {
        List(50) { index ->
            Message(
                id = index,
                sender = "User $index",
                content = "메시지 내용 $index",
                time = "오후 ${index % 12}:${index % 60}"
            )
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = messages,
            key = { it.id }
        ) { message ->
            MessageItem(message)
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.sender,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = message.content)
        }
    }
}
```

---

### 5. Spacer - 공간 확보

요소 사이에 빈 공간을 만듭니다.

```kotlin
@Composable
fun SpacerExample() {
    Column {
        Text("위")
        Spacer(modifier = Modifier.height(16.dp))  // 세로 간격
        Text("아래")
    }
    
    Row {
        Text("왼쪽")
        Spacer(modifier = Modifier.width(16.dp))   // 가로 간격
        Text("오른쪽")
    }
}
```

#### Spacer vs Padding

| Spacer | Padding |
|--------|---------|
| 별도의 요소 | Modifier의 일부 |
| 요소 **사이**에 공간 | 요소 **내부**에 공간 |
| `Spacer(Modifier.height(16.dp))` | `Modifier.padding(16.dp)` |

```kotlin
// Spacer 사용
Column {
    Text("A")
    Spacer(Modifier.height(16.dp))
    Text("B")
}

// Padding 사용 (동일한 효과)
Column {
    Text("A", modifier = Modifier.padding(bottom = 16.dp))
    Text("B")
}
```

#### 가변 공간 (weight)

```kotlin
Row(modifier = Modifier.fillMaxWidth()) {
    Text("왼쪽")
    Spacer(modifier = Modifier.weight(1f))  // 남은 공간 모두 차지
    Text("오른쪽")
}
```

**결과**: "왼쪽"과 "오른쪽" 사이에 최대한의 공간

---

## 기본 UI Components

### 1. Text - 텍스트 표시

```kotlin
@Composable
fun TextExamples() {
    // 기본 텍스트
    Text(text = "Hello, World!")
    
    // 스타일 적용
    Text(
        text = "제목",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Blue,
        textAlign = TextAlign.Center
    )
    
    // 최대 줄 수 제한
    Text(
        text = "긴 텍스트...",
        maxLines = 2,
        overflow = TextOverflow.Ellipsis  // "..." 표시
    )
    
    // 폰트 패밀리
    Text(
        text = "Custom Font",
        fontFamily = FontFamily.Serif
    )
}
```

#### Text 주요 파라미터

| 파라미터 | 타입 | 설명 | 예시 |
|---------|------|------|------|
| `text` | String | 표시할 텍스트 | `"Hello"` |
| `fontSize` | TextUnit | 글자 크기 | `16.sp` |
| `fontWeight` | FontWeight | 글자 굵기 | `FontWeight.Bold` |
| `color` | Color | 글자 색상 | `Color.Red` |
| `textAlign` | TextAlign | 정렬 | `TextAlign.Center` |
| `maxLines` | Int | 최대 줄 수 | `2` |
| `overflow` | TextOverflow | 넘침 처리 | `TextOverflow.Ellipsis` |

#### 실전 예제: 기사 제목과 본문

```kotlin
@Composable
fun ArticleCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 제목
        Text(
            text = "Jetpack Compose 완벽 가이드",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 부제목
        Text(
            text = "2025년 11월 30일 · 5분 읽기",
            fontSize = 12.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 본문
        Text(
            text = "Jetpack Compose는 Android의 최신 UI 툴킷입니다...",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
```

---

### 2. Button - 버튼

```kotlin
@Composable
fun ButtonExamples() {
    // 기본 버튼
    Button(onClick = { /* 클릭 이벤트 */ }) {
        Text("클릭하세요")
    }
    
    // 비활성화된 버튼
    Button(
        onClick = {},
        enabled = false
    ) {
        Text("비활성화")
    }
    
    // 색상 커스터마이징
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.White
        )
    ) {
        Text("빨간 버튼")
    }
    
    // 아이콘과 텍스트
    Button(onClick = {}) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("추가")
    }
}
```

#### Button 변형

```kotlin
// 1. OutlinedButton - 테두리만 있는 버튼
OutlinedButton(onClick = {}) {
    Text("외곽선 버튼")
}

// 2. TextButton - 배경 없는 버튼
TextButton(onClick = {}) {
    Text("텍스트 버튼")
}

// 3. IconButton - 아이콘만 있는 버튼
IconButton(onClick = {}) {
    Icon(Icons.Default.Favorite, contentDescription = "좋아요")
}

// 4. FloatingActionButton - 플로팅 액션 버튼
FloatingActionButton(onClick = {}) {
    Icon(Icons.Default.Add, contentDescription = "추가")
}
```

---

### 3. TextField - 텍스트 입력

```kotlin
@Composable
fun TextFieldExamples() {
    var text by remember { mutableStateOf("") }
    
    // 기본 TextField
    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("이름") },
        placeholder = { Text("이름을 입력하세요") }
    )
    
    // OutlinedTextField (권장)
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("이메일") },
        placeholder = { Text("example@email.com") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        )
    )
}
```

#### TextField 주요 파라미터

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| `value` | 현재 텍스트 값 | `text` |
| `onValueChange` | 값 변경 콜백 | `{ text = it }` |
| `label` | 레이블 | `{ Text("이름") }` |
| `placeholder` | 힌트 텍스트 | `{ Text("입력...") }` |
| `singleLine` | 한 줄 입력 | `true` |
| `maxLines` | 최대 줄 수 | `3` |
| `keyboardOptions` | 키보드 타입 | `KeyboardOptions(...)` |
| `visualTransformation` | 입력 변환 | `PasswordVisualTransformation()` |

#### 키보드 타입

```kotlin
KeyboardOptions(
    keyboardType = KeyboardType.Text        // 일반 텍스트
    keyboardType = KeyboardType.Number      // 숫자
    keyboardType = KeyboardType.Phone       // 전화번호
    keyboardType = KeyboardType.Email       // 이메일
    keyboardType = KeyboardType.Password    // 비밀번호
    keyboardType = KeyboardType.Uri         // URL
)
```

---

### 4. Image - 이미지 표시

```kotlin
@Composable
fun ImageExamples() {
    // 리소스 이미지
    Image(
        painter = painterResource(id = R.drawable.profile),
        contentDescription = "프로필 이미지",
        modifier = Modifier.size(100.dp)
    )
    
    // 벡터 아이콘
    Image(
        imageVector = Icons.Default.Person,
        contentDescription = "사람 아이콘"
    )
    
    // ContentScale 옵션
    Image(
        painter = painterResource(id = R.drawable.banner),
        contentDescription = "배너",
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.Crop  // 이미지 크롭
    )
}
```

#### ContentScale 옵션

| 옵션 | 설명 |
|------|------|
| `ContentScale.Fit` | 전체가 보이도록 맞춤 |
| `ContentScale.Crop` | 영역을 채우도록 크롭 |
| `ContentScale.FillWidth` | 너비에 맞춤 |
| `ContentScale.FillHeight` | 높이에 맞춤 |
| `ContentScale.Inside` | 원본 크기 유지 |

---

### 5. Card - 카드 컨테이너

```kotlin
@Composable
fun CardExample() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "카드 제목",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "카드 내용입니다.")
        }
    }
}
```

---

## Modifier 시스템

Modifier는 Composable의 **크기, 레이아웃, 동작, 외형**을 변경합니다.

### Modifier 체이닝

```kotlin
Text(
    text = "Hello",
    modifier = Modifier
        .fillMaxWidth()           // 너비를 부모에 맞춤
        .padding(16.dp)           // 패딩 추가
        .background(Color.Blue)   // 배경색
        .clickable { }            // 클릭 가능
)
```

> [!IMPORTANT]
> **Modifier 순서가 중요합니다!**
> ```kotlin
> // ✅ 올바른 순서: padding → background
> Modifier
>     .padding(16.dp)
>     .background(Color.Blue)
> // 결과: 파란 배경이 padding 안쪽에만
> 
> // ❌ 잘못된 순서: background → padding
> Modifier
>     .background(Color.Blue)
>     .padding(16.dp)
> // 결과: 파란 배경이 padding 포함 전체
> ```

### 주요 Modifier

#### 크기 관련

```kotlin
Modifier.size(100.dp)              // 정사각형 크기
Modifier.size(width = 100.dp, height = 50.dp)  // 직사각형
Modifier.width(100.dp)             // 너비만
Modifier.height(50.dp)             // 높이만
Modifier.fillMaxSize()             // 부모 크기에 맞춤
Modifier.fillMaxWidth()            // 너비만 부모에 맞춤
Modifier.fillMaxHeight()           // 높이만 부모에 맞춤
Modifier.fillMaxWidth(0.5f)        // 부모 너비의 50%
```

#### 여백 관련

```kotlin
Modifier.padding(16.dp)                    // 모든 방향
Modifier.padding(horizontal = 16.dp)       // 좌우
Modifier.padding(vertical = 8.dp)          // 상하
Modifier.padding(start = 16.dp, end = 8.dp)  // 시작, 끝
Modifier.padding(
    start = 16.dp,
    top = 8.dp,
    end = 16.dp,
    bottom = 8.dp
)
```

#### 배경 및 테두리

```kotlin
Modifier.background(Color.Blue)                    // 배경색
Modifier.background(Color.Blue, RoundedCornerShape(8.dp))  // 둥근 모서리
Modifier.border(2.dp, Color.Red)                   // 테두리
Modifier.border(2.dp, Color.Red, CircleShape)      // 원형 테두리
```

#### 상호작용

```kotlin
Modifier.clickable { /* 클릭 */ }          // 클릭 가능
Modifier.clickable(enabled = false) { }    // 비활성화
```

#### 레이아웃

```kotlin
Modifier.weight(1f)                // Row/Column에서 가중치
Modifier.align(Alignment.Center)   // Box에서 정렬
```

---

## 실전 레이아웃 패턴

### 패턴 1: 헤더 + 컨텐츠 + 푸터

```kotlin
@Composable
fun ScreenWithHeaderFooter() {
    Column(modifier = Modifier.fillMaxSize()) {
        // 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        ) {
            Text(
                text = "헤더",
                color = Color.White,
                fontSize = 20.sp
            )
        }
        
        // 컨텐츠 (남은 공간 모두 차지)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("메인 컨텐츠")
        }
        
        // 푸터
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(16.dp)
        ) {
            Text("푸터")
        }
    }
}
```

### 패턴 2: 그리드 레이아웃

```kotlin
@Composable
fun GridLayout() {
    val items = (1..12).toList()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),  // 3열
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)  // 정사각형
                    .background(Color.Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$item",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        }
    }
}
```

### 패턴 3: 탭 레이아웃

```kotlin
@Composable
fun TabLayout() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("홈", "검색", "프로필")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 탭 바
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // 탭 컨텐츠
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> Text("홈 화면")
                1 -> Text("검색 화면")
                2 -> Text("프로필 화면")
            }
        }
    }
}
```

---

## 성능 최적화

### 1. remember 사용

```kotlin
// ❌ 나쁜 예: 매번 새로운 리스트 생성
@Composable
fun BadExample() {
    val items = listOf("A", "B", "C")  // Recomposition마다 생성
}

// ✅ 좋은 예: 한 번만 생성
@Composable
fun GoodExample() {
    val items = remember { listOf("A", "B", "C") }
}
```

### 2. key 사용 (LazyColumn/Row)

```kotlin
// ✅ key를 지정하면 아이템 재사용 최적화
LazyColumn {
    items(
        items = messages,
        key = { it.id }  // 고유 ID 지정
    ) { message ->
        MessageItem(message)
    }
}
```

### 3. Modifier 재사용

```kotlin
// ✅ 공통 Modifier를 변수로 추출
val cardModifier = Modifier
    .fillMaxWidth()
    .padding(16.dp)

Card(modifier = cardModifier) { }
Card(modifier = cardModifier) { }
```

---

## 실습 프로젝트

### 프로젝트 1: 연락처 앱
**난이도**: ⭐⭐  
**학습 내용**: Column, Row, LazyColumn, Card

**요구사항**:
- 연락처 리스트 표시
- 프로필 이미지 + 이름 + 전화번호
- 알파벳 순 정렬
- 클릭 시 상세 정보

### 프로젝트 2: 갤러리 앱
**난이도**: ⭐⭐⭐  
**학습 내용**: LazyVerticalGrid, Image, Box

**요구사항**:
- 3열 그리드 레이아웃
- 이미지 썸네일 표시
- 클릭 시 전체 화면
- 좋아요 버튼 (Box 오버레이)

### 프로젝트 3: 뉴스 리더 앱
**난이도**: ⭐⭐⭐⭐  
**학습 내용**: 복합 레이아웃, Modifier, 성능 최적화

**요구사항**:
- 헤더 + 탭 + 리스트
- 기사 카드 (이미지 + 제목 + 본문)
- 무한 스크롤
- 북마크 기능

---

## 학습 체크리스트

### Layout Composables
- [ ] Column으로 세로 배치를 할 수 있다
- [ ] Row로 가로 배치를 할 수 있다
- [ ] Box로 겹쳐서 배치할 수 있다
- [ ] LazyColumn으로 리스트를 만들 수 있다
- [ ] Spacer로 간격을 조절할 수 있다

### UI Components
- [ ] Text의 다양한 스타일을 적용할 수 있다
- [ ] Button의 종류를 구분할 수 있다
- [ ] TextField로 입력을 받을 수 있다
- [ ] Image를 표시할 수 있다
- [ ] Card를 사용할 수 있다

### Modifier
- [ ] Modifier 체이닝을 이해했다
- [ ] 순서의 중요성을 안다
- [ ] 크기 관련 Modifier를 사용할 수 있다
- [ ] 여백 관련 Modifier를 사용할 수 있다
- [ ] 배경과 테두리를 적용할 수 있다

---

## 참고 자료

### 공식 문서
- [Compose Layout Basics](https://developer.android.com/jetpack/compose/layouts/basics)
- [Compose Modifiers](https://developer.android.com/jetpack/compose/modifiers)
- [Material Design 3](https://m3.material.io/)

### 추천 학습 순서
1. **기초**: Column, Row, Text, Button
2. **중급**: Box, LazyColumn, Modifier
3. **고급**: 복합 레이아웃, 성능 최적화
4. **실전**: 프로젝트 구현

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Composing! 🎨
