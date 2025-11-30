# Jetpack Compose 애니메이션 가이드

## 📚 목차

1. [애니메이션 소개](#애니메이션-소개)
2. [animate*AsState](#animateasstate)
3. [AnimatedVisibility](#animatedvisibility)
4. [Transition](#transition)
5. [animateContentSize](#animatecontentsize)
6. [커스텀 애니메이션](#커스텀-애니메이션)
7. [제스처 애니메이션](#제스처-애니메이션)
8. [실전 예제](#실전-예제)

---

## 애니메이션 소개

Jetpack Compose는 강력하고 사용하기 쉬운 애니메이션 API를 제공합니다.

### 애니메이션의 중요성

- ✅ **사용자 경험 향상**: 부드러운 전환으로 앱이 더 자연스럽게 느껴짐
- ✅ **피드백 제공**: 사용자 액션에 대한 시각적 피드백
- ✅ **주의 집중**: 중요한 변화에 사용자의 주의를 끌음
- ✅ **전문성**: 세련된 애니메이션은 앱의 품질을 높임

### Compose 애니메이션 종류

1. **animate*AsState** - 단일 값 애니메이션
2. **AnimatedVisibility** - 컴포저블 표시/숨김
3. **Transition** - 여러 값 동시 애니메이션
4. **animateContentSize** - 크기 변화 애니메이션
5. **커스텀 애니메이션** - 완전한 제어

---

## animate*AsState

가장 간단한 애니메이션 방법입니다. 값이 변경될 때 자동으로 애니메이션됩니다.

### animateDpAsState

```kotlin
@Composable
fun AnimatedBoxSize() {
    var isExpanded by remember { mutableStateOf(false) }
    
    val size by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        label = "size"
    )
    
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.Blue)
            .clickable { isExpanded = !isExpanded }
    )
}
```

### animateFloatAsState

```kotlin
@Composable
fun AnimatedAlpha() {
    var isVisible by remember { mutableStateOf(true) }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = "alpha"
    )
    
    Column {
        Box(
            modifier = Modifier
                .size(100.dp)
                .alpha(alpha)
                .background(Color.Red)
        )
        
        Button(onClick = { isVisible = !isVisible }) {
            Text(if (isVisible) "숨기기" else "보이기")
        }
    }
}
```

### animateColorAsState

```kotlin
@Composable
fun AnimatedColor() {
    var isSelected by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color(0xFF6200EE)
        } else {
            Color(0xFFBBBBBB)
        },
        label = "backgroundColor"
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(backgroundColor)
            .clickable { isSelected = !isSelected }
    )
}
```

### 애니메이션 스펙 커스터마이징

```kotlin
@Composable
fun CustomAnimationSpec() {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 1. Tween (시간 기반)
    val size1 by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "size1"
    )
    
    // 2. Spring (물리 기반)
    val size2 by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "size2"
    )
    
    // 3. Repeatable (반복)
    val size3 by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 100.dp,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size3"
    )
}
```

### 회전 애니메이션

```kotlin
@Composable
fun RotatingIcon() {
    var rotated by remember { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "rotation"
    )
    
    Icon(
        imageVector = Icons.Filled.ArrowDropDown,
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .rotate(rotation)
            .clickable { rotated = !rotated }
    )
}
```

---

## AnimatedVisibility

컴포저블을 부드럽게 나타내거나 사라지게 합니다.

### 기본 사용법

```kotlin
@Composable
fun BasicAnimatedVisibility() {
    var visible by remember { mutableStateOf(true) }
    
    Column {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "숨기기" else "보이기")
        }
        
        AnimatedVisibility(visible = visible) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Blue)
            )
        }
    }
}
```

### 진입/퇴장 애니메이션 커스터마이징

```kotlin
@Composable
fun CustomEnterExit() {
    var visible by remember { mutableStateOf(false) }
    
    Column {
        Button(onClick = { visible = !visible }) {
            Text("토글")
        }
        
        // 1. Fade
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text("Fade 애니메이션")
        }
        
        // 2. Slide
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Text("Slide 애니메이션")
        }
        
        // 3. Expand/Shrink
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text("Expand 애니메이션")
        }
        
        // 4. 조합
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text("조합 애니메이션")
        }
    }
}
```

### 방향 지정

```kotlin
@Composable
fun DirectionalAnimation() {
    var visible by remember { mutableStateOf(false) }
    
    Column {
        Button(onClick = { visible = !visible }) {
            Text("토글")
        }
        
        // 왼쪽에서 슬라이드
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { -it }
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it }
            )
        ) {
            Text("왼쪽에서 슬라이드")
        }
        
        // 오른쪽에서 슬라이드
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { it }
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it }
            )
        ) {
            Text("오른쪽에서 슬라이드")
        }
    }
}
```

### 자식 애니메이션

```kotlin
@Composable
fun AnimatedVisibilityChildren() {
    var visible by remember { mutableStateOf(false) }
    
    Column {
        Button(onClick = { visible = !visible }) {
            Text("토글")
        }
        
        AnimatedVisibility(visible = visible) {
            Column {
                // 각 자식이 순차적으로 나타남
                Text(
                    "첫 번째",
                    modifier = Modifier.animateEnterExit(
                        enter = fadeIn(
                            animationSpec = tween(300, delayMillis = 0)
                        )
                    )
                )
                Text(
                    "두 번째",
                    modifier = Modifier.animateEnterExit(
                        enter = fadeIn(
                            animationSpec = tween(300, delayMillis = 100)
                        )
                    )
                )
                Text(
                    "세 번째",
                    modifier = Modifier.animateEnterExit(
                        enter = fadeIn(
                            animationSpec = tween(300, delayMillis = 200)
                        )
                    )
                )
            }
        }
    }
}
```

---

## Transition

여러 값을 동시에 애니메이션하고 상태 간 전환을 관리합니다.

### updateTransition

```kotlin
enum class BoxState {
    Small, Large
}

@Composable
fun TransitionExample() {
    var currentState by remember { mutableStateOf(BoxState.Small) }
    val transition = updateTransition(
        targetState = currentState,
        label = "box transition"
    )
    
    val size by transition.animateDp(
        label = "size"
    ) { state ->
        when (state) {
            BoxState.Small -> 100.dp
            BoxState.Large -> 200.dp
        }
    }
    
    val color by transition.animateColor(
        label = "color"
    ) { state ->
        when (state) {
            BoxState.Small -> Color.Blue
            BoxState.Large -> Color.Red
        }
    }
    
    val cornerRadius by transition.animateDp(
        label = "cornerRadius"
    ) { state ->
        when (state) {
            BoxState.Small -> 0.dp
            BoxState.Large -> 50.dp
        }
    }
    
    Box(
        modifier = Modifier
            .size(size)
            .background(color, RoundedCornerShape(cornerRadius))
            .clickable {
                currentState = when (currentState) {
                    BoxState.Small -> BoxState.Large
                    BoxState.Large -> BoxState.Small
                }
            }
    )
}
```

### 복잡한 상태 전환

```kotlin
enum class ComponentState {
    Idle, Pressed, Dragging
}

@Composable
fun ComplexTransition() {
    var currentState by remember { mutableStateOf(ComponentState.Idle) }
    val transition = updateTransition(currentState, label = "state")
    
    val scale by transition.animateFloat(
        label = "scale",
        transitionSpec = {
            when {
                ComponentState.Idle isTransitioningTo ComponentState.Pressed ->
                    spring(stiffness = Spring.StiffnessHigh)
                else ->
                    tween(durationMillis = 300)
            }
        }
    ) { state ->
        when (state) {
            ComponentState.Idle -> 1f
            ComponentState.Pressed -> 0.9f
            ComponentState.Dragging -> 1.1f
        }
    }
    
    val elevation by transition.animateDp(
        label = "elevation"
    ) { state ->
        when (state) {
            ComponentState.Idle -> 2.dp
            ComponentState.Pressed -> 0.dp
            ComponentState.Dragging -> 8.dp
        }
    }
    
    Card(
        modifier = Modifier
            .size(100.dp)
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    currentState = when (currentState) {
                        ComponentState.Idle -> ComponentState.Pressed
                        ComponentState.Pressed -> ComponentState.Dragging
                        ComponentState.Dragging -> ComponentState.Idle
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(currentState.name)
        }
    }
}
```

---

## animateContentSize

컨텐츠 크기가 변경될 때 자동으로 애니메이션됩니다.

### 기본 사용법

```kotlin
@Composable
fun ExpandableCard() {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize() // 크기 변화 애니메이션
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "제목",
                style = MaterialTheme.typography.titleLarge
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "여기에 긴 설명 텍스트가 들어갑니다. " +
                          "카드가 확장되면서 부드럽게 크기가 변합니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "접기" else "더보기")
            }
        }
    }
}
```

### 커스텀 애니메이션 스펙

```kotlin
@Composable
fun CustomAnimateContentSize() {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("제목")
            if (expanded) {
                Text("내용")
            }
            Button(onClick = { expanded = !expanded }) {
                Text(if (expanded) "접기" else "펼치기")
            }
        }
    }
}
```

---

## 커스텀 애니메이션

### Animatable

```kotlin
@Composable
fun AnimatableExample() {
    val color = remember { Animatable(Color.Gray) }
    
    LaunchedEffect(Unit) {
        color.animateTo(
            targetValue = Color.Blue,
            animationSpec = tween(durationMillis = 2000)
        )
    }
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(color.value)
    )
}
```

### 무한 반복 애니메이션

```kotlin
@Composable
fun InfiniteAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .alpha(alpha)
            .background(Color.Blue)
    )
}
```

### 로딩 인디케이터

```kotlin
@Composable
fun PulsingLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(scale)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
    }
}
```

### 회전 로딩

```kotlin
@Composable
fun RotatingLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Icon(
        imageVector = Icons.Filled.Refresh,
        contentDescription = "로딩 중",
        modifier = Modifier
            .size(48.dp)
            .rotate(rotation)
    )
}
```

---

## 제스처 애니메이션

### 드래그 애니메이션

```kotlin
@Composable
fun DraggableBox() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(100.dp)
                .background(Color.Blue)
        )
    }
}
```

### 스와이프로 삭제

```kotlin
@Composable
fun SwipeToDismiss() {
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // 배경 (삭제 버튼)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "삭제",
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
        
        // 전경 (아이템)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(Color.White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 일정 거리 이상 스와이프하면 삭제
                            if (offsetX < -200f) {
                                // 삭제 로직
                            } else {
                                // 원위치로 복귀
                                offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            // 왼쪽으로만 드래그 가능
                            offsetX = newOffset.coerceAtMost(0f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "왼쪽으로 스와이프하여 삭제",
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
```

### 스프링 효과가 있는 드래그

```kotlin
@Composable
fun SpringDraggable() {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offsetX"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // 드래그 종료 시 원위치
                        offsetX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .size(100.dp)
                .background(Color.Blue)
        )
    }
}
```

---

## 실전 예제

### 좋아요 버튼 애니메이션

```kotlin
@Composable
fun AnimatedLikeButton() {
    var isLiked by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val color by animateColorAsState(
        targetValue = if (isLiked) Color.Red else Color.Gray,
        label = "color"
    )
    
    IconButton(
        onClick = { isLiked = !isLiked }
    ) {
        Icon(
            imageVector = if (isLiked) {
                Icons.Filled.Favorite
            } else {
                Icons.Filled.FavoriteBorder
            },
            contentDescription = "좋아요",
            tint = color,
            modifier = Modifier.scale(scale)
        )
    }
}
```

### 확장 가능한 FAB

```kotlin
@Composable
fun ExpandableFab() {
    var expanded by remember { mutableStateOf(false) }
    
    val fabSize by animateDpAsState(
        targetValue = if (expanded) 200.dp else 56.dp,
        label = "fabSize"
    )
    
    FloatingActionButton(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .size(width = fabSize, height = 56.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "추가"
            )
            
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = "새 항목 추가",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
```

### 알림 배지 애니메이션

```kotlin
@Composable
fun AnimatedBadge(count: Int) {
    val scale by animateFloatAsState(
        targetValue = if (count > 0) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )
    
    Box {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "알림",
            modifier = Modifier.size(24.dp)
        )
        
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .scale(scale)
                    .size(16.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

### 스켈레톤 로딩

```kotlin
@Composable
fun SkeletonLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 제목 스켈레톤
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(24.dp)
                .alpha(alpha)
                .background(
                    Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(4.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 본문 스켈레톤
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .alpha(alpha)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
```

### 페이지 전환 애니메이션

```kotlin
@Composable
fun PageTransitionExample() {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf("페이지 1", "페이지 2", "페이지 3")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 페이지 인디케이터
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.forEachIndexed { index, _ ->
                val size by animateDpAsState(
                    targetValue = if (index == currentPage) 12.dp else 8.dp,
                    label = "indicatorSize"
                )
                
                val color by animateColorAsState(
                    targetValue = if (index == currentPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Gray
                    },
                    label = "indicatorColor"
                )
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(size)
                        .background(color, CircleShape)
                )
            }
        }
        
        // 페이지 콘텐츠
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "pageTransition"
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pages[page],
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
        
        // 네비게이션 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0
            ) {
                Text("이전")
            }
            
            Button(
                onClick = { if (currentPage < pages.size - 1) currentPage++ },
                enabled = currentPage < pages.size - 1
            ) {
                Text("다음")
            }
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. 적절한 애니메이션 선택

```kotlin
// ✅ 단순한 값 변화 → animate*AsState
val size by animateDpAsState(targetValue = if (expanded) 200.dp else 100.dp)

// ✅ 표시/숨김 → AnimatedVisibility
AnimatedVisibility(visible = isVisible) { ... }

// ✅ 여러 값 동시 애니메이션 → Transition
val transition = updateTransition(state)
```

### 2. 성능 고려

```kotlin
// ❌ 나쁜 예: 너무 많은 동시 애니메이션
LazyColumn {
    items(1000) { item ->
        AnimatedItem() // 1000개 동시 애니메이션
    }
}

// ✅ 좋은 예: 필요한 것만 애니메이션
LazyColumn {
    items(1000) { item ->
        if (item.isVisible) {
            AnimatedItem()
        } else {
            StaticItem()
        }
    }
}
```

### 3. 일관된 타이밍

```kotlin
// 앱 전체에서 일관된 애니메이션 시간 사용
object AnimationConstants {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
}

val size by animateDpAsState(
    targetValue = targetSize,
    animationSpec = tween(AnimationConstants.NORMAL)
)
```

### 4. 접근성 고려

```kotlin
// 사용자가 애니메이션을 비활성화한 경우 고려
val animationSpec = if (/* 접근성 설정 확인 */) {
    snap() // 즉시 변경
} else {
    tween(300) // 애니메이션
}
```

### 5. 의미 있는 애니메이션

```kotlin
// ❌ 나쁜 예: 불필요한 애니메이션
Text("Hello", modifier = Modifier.rotate(rotation)) // 왜?

// ✅ 좋은 예: 사용자 피드백
Icon(
    Icons.Filled.Favorite,
    modifier = Modifier.scale(scale) // 좋아요 클릭 시
)
```

---

## 🎯 다음 단계

애니메이션을 마스터했습니다! 다음으로:

1. **Side Effects 가이드** - LaunchedEffect, DisposableEffect 등
2. **이미지 로딩 가이드** - Coil로 네트워크 이미지 로딩
3. **고급 제스처** - 복잡한 터치 인터랙션

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Animating! 🎨
