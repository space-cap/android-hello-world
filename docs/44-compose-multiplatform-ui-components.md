# Compose Multiplatform UI 컴포넌트 및 레이아웃

## 목차
1. [Material 3 컴포넌트](#material-3-컴포넌트)
2. [레이아웃 시스템](#레이아웃-시스템)
3. [커스텀 컴포넌트 작성](#커스텀-컴포넌트-작성)
4. [리스트와 그리드](#리스트와-그리드)
5. [제스처 처리](#제스처-처리)
6. [애니메이션](#애니메이션)
7. [플랫폼별 UI 조정](#플랫폼별-ui-조정)

---

## Material 3 컴포넌트

### 기본 컴포넌트

#### 1. Button 계열

```kotlin
@Composable
fun ButtonExamples() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 기본 Button (Filled)
        Button(onClick = { /* 액션 */ }) {
            Text("Filled Button")
        }
        
        // FilledTonalButton (약간 덜 강조)
        FilledTonalButton(onClick = { /* 액션 */ }) {
            Text("Tonal Button")
        }
        
        // OutlinedButton (테두리만)
        OutlinedButton(onClick = { /* 액션 */ }) {
            Text("Outlined Button")
        }
        
        // ElevatedButton (그림자 있음)
        ElevatedButton(onClick = { /* 액션 */ }) {
            Text("Elevated Button")
        }
        
        // TextButton (텍스트만)
        TextButton(onClick = { /* 액션 */ }) {
            Text("Text Button")
        }
        
        // 아이콘이 있는 버튼
        Button(
            onClick = { /* 액션 */ }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Item")
        }
        
        // 비활성화된 버튼
        Button(
            onClick = { /* 액션 */ },
            enabled = false
        ) {
            Text("Disabled Button")
        }
        
        // 커스텀 색상 버튼
        Button(
            onClick = { /* 액션 */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            )
        ) {
            Text("Custom Color")
        }
    }
}
```

#### 2. TextField 계열

```kotlin
@Composable
fun TextFieldExamples() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 기본 TextField (Filled)
        TextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("Label") },
            placeholder = { Text("Placeholder") }
        )
        
        // OutlinedTextField (테두리 스타일)
        OutlinedTextField(
            value = text2,
            onValueChange = { text2 = it },
            label = { Text("Outlined") },
            supportingText = { Text("Helper text") }
        )
        
        // 비밀번호 입력 필드
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
        )
        
        // 에러 상태
        OutlinedTextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("Email") },
            isError = text1.isNotEmpty() && !text1.contains("@"),
            supportingText = {
                if (text1.isNotEmpty() && !text1.contains("@")) {
                    Text("Invalid email format")
                }
            }
        )
        
        // 아이콘이 있는 TextField
        OutlinedTextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("Search") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (text1.isNotEmpty()) {
                    IconButton(onClick = { text1 = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )
    }
}
```

#### 3. Card

```kotlin
@Composable
fun CardExamples() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 기본 Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Card Title",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Card content goes here",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // ElevatedCard (그림자 있음)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Elevated Card")
            }
        }
        
        // OutlinedCard (테두리만)
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Outlined Card")
            }
        }
        
        // 클릭 가능한 Card
        Card(
            onClick = { /* 클릭 액션 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Clickable Card",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap to see more",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
```

#### 4. Chip

```kotlin
@Composable
fun ChipExamples() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AssistChip (보조 액션)
        AssistChip(
            onClick = { /* 액션 */ },
            label = { Text("Assist Chip") },
            leadingIcon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
        
        // FilterChip (필터 선택)
        var selected by remember { mutableStateOf(false) }
        FilterChip(
            selected = selected,
            onClick = { selected = !selected },
            label = { Text("Filter Chip") },
            leadingIcon = if (selected) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            }
        )
        
        // InputChip (입력 값 표시)
        InputChip(
            selected = false,
            onClick = { /* 액션 */ },
            label = { Text("Input Chip") },
            trailingIcon = {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(InputChipDefaults.IconSize)
                )
            }
        )
        
        // SuggestionChip (제안)
        SuggestionChip(
            onClick = { /* 액션 */ },
            label = { Text("Suggestion Chip") }
        )
        
        // Chip 그룹 예제
        Text("Select categories:", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("Technology", "Design", "Business", "Health", "Sports")
            val selectedCategories = remember { mutableStateListOf<String>() }
            
            categories.forEach { category ->
                FilterChip(
                    selected = category in selectedCategories,
                    onClick = {
                        if (category in selectedCategories) {
                            selectedCategories.remove(category)
                        } else {
                            selectedCategories.add(category)
                        }
                    },
                    label = { Text(category) }
                )
            }
        }
    }
}
```

#### 5. Switch, Checkbox, RadioButton

```kotlin
@Composable
fun SelectionControlsExamples() {
    var switchChecked by remember { mutableStateOf(false) }
    var checkboxChecked by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Option 1") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable notifications")
            Switch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it }
            )
        }
        
        // Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checkboxChecked,
                onCheckedChange = { checkboxChecked = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("I agree to the terms and conditions")
        }
        
        // RadioButton 그룹
        Text("Select an option:", style = MaterialTheme.typography.titleMedium)
        Column {
            listOf("Option 1", "Option 2", "Option 3").forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedOption == option,
                        onClick = { selectedOption = option }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option)
                }
            }
        }
    }
}
```

---

## 레이아웃 시스템

### Column, Row, Box

```kotlin
@Composable
fun LayoutBasics() {
    // Column: 세로 방향 레이아웃
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),  // 아이템 간격
        horizontalAlignment = Alignment.CenterHorizontally  // 가로 정렬
    ) {
        Text("Item 1")
        Text("Item 2")
        Text("Item 3")
    }
    
    // Row: 가로 방향 레이아웃
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,  // 양 끝 정렬
        verticalAlignment = Alignment.CenterVertically     // 세로 정렬
    ) {
        Text("Left")
        Text("Center")
        Text("Right")
    }
    
    // Box: 겹치는 레이아웃
    Box(
        modifier = Modifier.size(200.dp)
    ) {
        // 배경
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.LightGray
        ) {}
        
        // 중앙 텍스트
        Text(
            text = "Centered",
            modifier = Modifier.align(Alignment.Center)
        )
        
        // 우측 하단 버튼
        Button(
            onClick = { /* 액션 */ },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text("Action")
        }
    }
}
```

### Arrangement와 Alignment

```kotlin
@Composable
fun ArrangementExamples() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Arrangement.SpaceBetween
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.size(40.dp).background(Color.Red))
            Box(Modifier.size(40.dp).background(Color.Green))
            Box(Modifier.size(40.dp).background(Color.Blue))
        }
        
        // Arrangement.SpaceEvenly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(Modifier.size(40.dp).background(Color.Red))
            Box(Modifier.size(40.dp).background(Color.Green))
            Box(Modifier.size(40.dp).background(Color.Blue))
        )
        
        // Arrangement.SpaceAround
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(Modifier.size(40.dp).background(Color.Red))
            Box(Modifier.size(40.dp).background(Color.Green))
            Box(Modifier.size(40.dp).background(Color.Blue))
        }
        
        // Arrangement.spacedBy (고정 간격)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.size(40.dp).background(Color.Red))
            Box(Modifier.size(40.dp).background(Color.Green))
            Box(Modifier.size(40.dp).background(Color.Blue))
        }
    }
}
```

### Modifier 체이닝

```kotlin
@Composable
fun ModifierExamples() {
    // Modifier는 순서가 중요합니다!
    Box(
        modifier = Modifier
            .size(200.dp)              // 1. 크기 설정
            .background(Color.Blue)    // 2. 배경색 (200x200)
            .padding(16.dp)            // 3. 내부 여백 (168x168)
            .background(Color.Red)     // 4. 배경색 (168x168)
            .padding(16.dp)            // 5. 내부 여백 (136x136)
            .background(Color.Green)   // 6. 배경색 (136x136)
    )
    
    // 클릭 가능한 영역
    Text(
        text = "Click me",
        modifier = Modifier
            .clickable { /* 클릭 액션 */ }
            .padding(16.dp)  // 클릭 영역이 패딩 포함
    )
    
    // vs
    
    Text(
        text = "Click me",
        modifier = Modifier
            .padding(16.dp)  // 클릭 영역이 패딩 제외
            .clickable { /* 클릭 액션 */ }
    )
}
```

---

## 커스텀 컴포넌트 작성

### 재사용 가능한 컴포넌트

```kotlin
/**
 * 프로필 카드 컴포넌트
 * 
 * @param name 사용자 이름
 * @param subtitle 부제목 (직책, 이메일 등)
 * @param imageUrl 프로필 이미지 URL (선택사항)
 * @param onClick 클릭 시 호출되는 콜백
 * @param modifier Modifier
 */
@Composable
fun ProfileCard(
    name: String,
    subtitle: String,
    imageUrl: String? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지 또는 기본 아이콘
            if (imageUrl != null) {
                // TODO: 실제로는 AsyncImage 사용
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 텍스트 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 화살표 아이콘
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 사용 예시
@Composable
fun ProfileCardExample() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileCard(
            name = "John Doe",
            subtitle = "Software Engineer",
            onClick = { /* 프로필 보기 */ }
        )
        
        ProfileCard(
            name = "Jane Smith",
            subtitle = "jane.smith@example.com",
            onClick = { /* 프로필 보기 */ }
        )
    }
}
```

### 상태를 가진 컴포넌트

```kotlin
/**
 * 확장 가능한 카드 컴포넌트
 * 
 * @param title 제목
 * @param content 내용 (Composable)
 * @param initiallyExpanded 초기 확장 상태
 * @param modifier Modifier
 */
@Composable
fun ExpandableCard(
    title: String,
    content: @Composable () -> Unit,
    initiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 내부 상태 관리
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // 헤더 (항상 표시)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            
            // 내용 (확장 시에만 표시)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

// 사용 예시
@Composable
fun ExpandableCardExample() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExpandableCard(
            title = "Section 1",
            content = {
                Text("This is the content of section 1.")
            }
        )
        
        ExpandableCard(
            title = "Section 2",
            initiallyExpanded = true,
            content = {
                Column {
                    Text("This is the content of section 2.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /* 액션 */ }) {
                        Text("Action Button")
                    }
                }
            }
        )
    }
}
```

---

## 리스트와 그리드

### LazyColumn (세로 스크롤 리스트)

```kotlin
@Composable
fun LazyColumnExample() {
    val items = remember {
        (1..100).map { "Item #$it" }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 헤더
        item {
            Text(
                text = "Header",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        // 아이템 리스트
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // 푸터
        item {
            Text(
                text = "End of list",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### LazyRow (가로 스크롤 리스트)

```kotlin
@Composable
fun LazyRowExample() {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(10) { index ->
                Card(
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Category ${index + 1}")
                    }
                }
            }
        }
    }
}
```

### LazyVerticalGrid (그리드)

```kotlin
@Composable
fun LazyGridExample() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),  // 반응형 그리드
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(20) { index ->
            Card(
                modifier = Modifier.aspectRatio(1f)  // 정사각형
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item ${index + 1}")
                }
            }
        }
    }
}
```

### Sticky Header (고정 헤더)

```kotlin
@Composable
fun StickyHeaderExample() {
    val groupedItems = remember {
        ('A'..'Z').map { letter ->
            letter to (1..10).map { "$letter$it" }
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        groupedItems.forEach { (letter, items) ->
            // Sticky Header
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = letter.toString(),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            // 아이템들
            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
```

---

## 제스처 처리

### 클릭 제스처

```kotlin
@Composable
fun ClickGestureExamples() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 일반 클릭
        Text(
            text = "Click me",
            modifier = Modifier
                .clickable { /* 클릭 액션 */ }
                .padding(16.dp)
                .background(Color.LightGray)
        )
        
        // 리플 효과 없는 클릭
        Text(
            text = "Click without ripple",
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* 클릭 액션 */ }
                .padding(16.dp)
                .background(Color.LightGray)
        )
        
        // 더블 클릭, 롱 클릭
        var clickCount by remember { mutableStateOf(0) }
        Text(
            text = "Clicks: $clickCount",
            modifier = Modifier
                .combinedClickable(
                    onClick = { clickCount++ },
                    onDoubleClick = { clickCount += 2 },
                    onLongClick = { clickCount = 0 }
                )
                .padding(16.dp)
                .background(Color.LightGray)
        )
    }
}
```

### 드래그 제스처

```kotlin
@Composable
fun DraggableExample() {
    var offsetX by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(100.dp)
                .background(Color.Blue)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX += delta
                    }
                )
        )
    }
}
```

### 스와이프 제스처

```kotlin
@Composable
fun SwipeToDeleteExample() {
    val items = remember { mutableStateListOf("Item 1", "Item 2", "Item 3") }
    
    LazyColumn {
        items(
            items = items,
            key = { it }
        ) { item ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        items.remove(item)
                        true
                    } else {
                        false
                    }
                }
            )
            
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.padding(16.dp),
                            tint = Color.White
                        )
                    }
                }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
```

---

## 애니메이션

### 기본 애니메이션

```kotlin
@Composable
fun BasicAnimationExamples() {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 크기 애니메이션
        val size by animateDpAsState(
            targetValue = if (expanded) 200.dp else 100.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        Box(
            modifier = Modifier
                .size(size)
                .background(Color.Blue)
                .clickable { expanded = !expanded }
        )
        
        // 색상 애니메이션
        val color by animateColorAsState(
            targetValue = if (expanded) Color.Red else Color.Blue
        )
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(color)
                .clickable { expanded = !expanded }
        )
        
        // 투명도 애니메이션
        val alpha by animateFloatAsState(
            targetValue = if (expanded) 1f else 0.3f
        )
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .alpha(alpha)
                .background(Color.Green)
                .clickable { expanded = !expanded }
        )
    }
}
```

### AnimatedVisibility

```kotlin
@Composable
fun AnimatedVisibilityExample() {
    var visible by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "Hide" else "Show")
        }
        
        // 기본 페이드 인/아웃
        AnimatedVisibility(visible = visible) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "This content can be hidden",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // 슬라이드 애니메이션
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Slide animation",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
```

---

## 플랫폼별 UI 조정

### 플랫폼별 스타일링

```kotlin
/**
 * 플랫폼에 따라 다른 패딩 적용
 */
@Composable
fun PlatformAdaptivePadding() {
    val padding = when (getPlatformName()) {
        "iOS" -> 20.dp
        "Android" -> 16.dp
        else -> 12.dp
    }
    
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Text("Platform-specific padding")
    }
}

/**
 * 플랫폼별 버튼 높이
 */
@Composable
fun PlatformAdaptiveButton(
    text: String,
    onClick: () -> Unit
) {
    val height = when (getPlatformName()) {
        "iOS" -> 50.dp
        "Android" -> 48.dp
        else -> 40.dp
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier.height(height)
    ) {
        Text(text)
    }
}
```

---

## 다음 단계

다음 문서에서는:
- **네비게이션 시스템**
- **화면 전환**
- **딥링크**

를 다룹니다.
