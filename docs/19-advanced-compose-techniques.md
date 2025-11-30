# Advanced Jetpack Compose 기법

## 📚 목차

1. [Custom Layout](#custom-layout)
2. [Canvas와 그래픽](#canvas와-그래픽)
3. [Modifier 심화](#modifier-심화)
4. [CompositionLocal](#compositionlocal)
5. [성능 최적화](#성능-최적화)
6. [실전 예제](#실전-예제)

---

## Custom Layout

### Layout Composable

```kotlin
@Composable
fun CustomColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // 1. 자식들을 측정
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        
        // 2. 레이아웃 크기 계산
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.sumOf { it.height }
        
        // 3. 레이아웃 배치
        layout(width, height) {
            var yPosition = 0
            
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}
```

### Staggered Grid Layout

```kotlin
@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val columnWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(
            maxWidth = columnWidth
        )
        
        val placeables = measurables.map { measurable ->
            measurable.measure(itemConstraints)
        }
        
        val columnHeights = IntArray(columns) { 0 }
        
        val width = constraints.maxWidth
        val height = placeables.foldIndexed(0) { index, maxHeight, placeable ->
            val column = index % columns
            columnHeights[column] += placeable.height
            columnHeights.maxOrNull() ?: 0
        }
        
        layout(width, height) {
            val columnY = IntArray(columns) { 0 }
            
            placeables.forEachIndexed { index, placeable ->
                val column = index % columns
                placeable.placeRelative(
                    x = column * columnWidth,
                    y = columnY[column]
                )
                columnY[column] += placeable.height
            }
        }
    }
}

// 사용 예제
@Composable
fun StaggeredGridExample() {
    StaggeredGrid(columns = 2) {
        repeat(10) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((100 + index * 20).dp)
                    .padding(4.dp)
            ) {
                Text("Item $index")
            }
        }
    }
}
```

### SubcomposeLayout

```kotlin
@Composable
fun MeasureUnconstrainedView(
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
    dependentContent: @Composable (IntSize) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        // 1. 먼저 mainContent 측정
        val mainPlaceables = subcompose("main", mainContent).map {
            it.measure(constraints)
        }
        
        val maxSize = mainPlaceables.fold(IntSize.Zero) { currentMax, placeable ->
            IntSize(
                width = maxOf(currentMax.width, placeable.width),
                height = maxOf(currentMax.height, placeable.height)
            )
        }
        
        // 2. mainContent의 크기를 기반으로 dependentContent 측정
        val dependentPlaceables = subcompose("dependent") {
            dependentContent(maxSize)
        }.map { it.measure(constraints) }
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            mainPlaceables.forEach { it.placeRelative(0, 0) }
            dependentPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}
```

---

## Canvas와 그래픽

### 기본 도형 그리기

```kotlin
@Composable
fun BasicShapes() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 원
        drawCircle(
            color = Color.Red,
            radius = 50.dp.toPx(),
            center = Offset(100.dp.toPx(), 100.dp.toPx())
        )
        
        // 사각형
        drawRect(
            color = Color.Blue,
            topLeft = Offset(200.dp.toPx(), 50.dp.toPx()),
            size = Size(100.dp.toPx(), 100.dp.toPx())
        )
        
        // 선
        drawLine(
            color = Color.Green,
            start = Offset(50.dp.toPx(), 200.dp.toPx()),
            end = Offset(300.dp.toPx(), 200.dp.toPx()),
            strokeWidth = 5.dp.toPx()
        )
        
        // 호
        drawArc(
            color = Color.Yellow,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(50.dp.toPx(), 250.dp.toPx()),
            size = Size(100.dp.toPx(), 100.dp.toPx())
        )
    }
}
```

### 커스텀 Progress Bar

```kotlin
@Composable
fun CircularProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 8.dp
) {
    Canvas(modifier = modifier.size(100.dp)) {
        val diameter = size.minDimension
        val radius = diameter / 2
        val strokeWidthPx = strokeWidth.toPx()
        
        // 배경 원
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius - strokeWidthPx / 2,
            style = Stroke(width = strokeWidthPx)
        )
        
        // 진행 호
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
            size = Size(diameter - strokeWidthPx, diameter - strokeWidthPx),
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}
```

### 그라데이션

```kotlin
@Composable
fun GradientBox() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // 선형 그라데이션
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Red, Color.Blue),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f)
            )
        )
        
        // 방사형 그라데이션
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Yellow, Color.Red),
                center = center,
                radius = size.minDimension / 2
            )
        )
    }
}
```

### Path 그리기

```kotlin
@Composable
fun CustomShape() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val path = Path().apply {
            moveTo(0f, size.height / 2)
            
            // 물결 모양
            var x = 0f
            while (x < size.width) {
                quadraticBezierTo(
                    x + size.width / 8,
                    size.height / 4,
                    x + size.width / 4,
                    size.height / 2
                )
                quadraticBezierTo(
                    x + size.width * 3 / 8,
                    size.height * 3 / 4,
                    x + size.width / 2,
                    size.height / 2
                )
                x += size.width / 2
            }
            
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        
        drawPath(
            path = path,
            color = Color.Blue.copy(alpha = 0.5f)
        )
    }
}
```

---

## Modifier 심화

### 커스텀 Modifier

```kotlin
fun Modifier.dashedBorder(
    width: Dp = 2.dp,
    color: Color = Color.Black,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp
) = this.drawBehind {
    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(
            dashLength.toPx(),
            gapLength.toPx()
        )
    )
    
    drawRoundRect(
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = pathEffect
        )
    )
}

// 사용
Box(
    modifier = Modifier
        .size(100.dp)
        .dashedBorder()
)
```

### Modifier.composed

```kotlin
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    this.drawWithContent {
        drawContent()
        
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.3f),
                Color.Transparent
            ),
            start = Offset(translateAnim - 200f, 0f),
            end = Offset(translateAnim, size.height)
        )
        
        drawRect(brush = brush)
    }
}
```

### Modifier.pointerInput 심화

```kotlin
fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit
) = this.pointerInput(Unit) {
    detectHorizontalDragGestures(
        onDragEnd = { /* 드래그 종료 */ },
        onHorizontalDrag = { change, dragAmount ->
            change.consume()
            
            if (dragAmount < -200f) {
                onDismiss()
            }
        }
    )
}
```

---

## CompositionLocal

### 커스텀 CompositionLocal

```kotlin
data class AppSettings(
    val isDarkMode: Boolean,
    val language: String,
    val fontSize: TextUnit
)

val LocalAppSettings = compositionLocalOf {
    AppSettings(
        isDarkMode = false,
        language = "ko",
        fontSize = 14.sp
    )
}

@Composable
fun MyApp() {
    val settings = remember {
        mutableStateOf(
            AppSettings(
                isDarkMode = false,
                language = "ko",
                fontSize = 14.sp
            )
        )
    }
    
    CompositionLocalProvider(LocalAppSettings provides settings.value) {
        // 앱 콘텐츠
        MainScreen()
    }
}

@Composable
fun SomeDeepComponent() {
    val settings = LocalAppSettings.current
    
    Text(
        text = "Hello",
        fontSize = settings.fontSize
    )
}
```

### 테마 커스터마이징

```kotlin
data class CustomColors(
    val success: Color,
    val warning: Color,
    val info: Color
)

val LocalCustomColors = compositionLocalOf {
    CustomColors(
        success = Color(0xFF4CAF50),
        warning = Color(0xFFFFC107),
        info = Color(0xFF2196F3)
    )
}

@Composable
fun CustomTheme(
    content: @Composable () -> Unit
) {
    val customColors = CustomColors(
        success = Color(0xFF4CAF50),
        warning = Color(0xFFFFC107),
        info = Color(0xFF2196F3)
    )
    
    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme {
            content()
        }
    }
}

// 사용
@Composable
fun SuccessButton() {
    val customColors = LocalCustomColors.current
    
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            containerColor = customColors.success
        )
    ) {
        Text("Success")
    }
}
```

---

## 성능 최적화

### remember와 derivedStateOf

```kotlin
@Composable
fun OptimizedList(items: List<Item>, query: String) {
    // ❌ 나쁜 예: 매 재구성마다 필터링
    val filteredItems = items.filter { it.name.contains(query) }
    
    // ✅ 좋은 예: items나 query가 변경될 때만 필터링
    val filteredItems = remember(items, query) {
        items.filter { it.name.contains(query) }
    }
    
    // ✅ 더 좋은 예: derivedStateOf 사용
    val filteredItems by remember {
        derivedStateOf {
            items.filter { it.name.contains(query) }
        }
    }
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

### key를 사용한 최적화

```kotlin
@Composable
fun OptimizedLazyColumn(items: List<Item>) {
    LazyColumn {
        // ✅ key 사용으로 재사용 최적화
        items(
            items = items,
            key = { item -> item.id }
        ) { item ->
            ItemRow(item)
        }
    }
}
```

### 불필요한 재구성 방지

```kotlin
// ❌ 나쁜 예: 람다가 매번 새로 생성됨
@Composable
fun BadExample(items: List<Item>) {
    LazyColumn {
        items(items) { item ->
            ItemRow(
                item = item,
                onClick = { println("Clicked: ${item.name}") }
            )
        }
    }
}

// ✅ 좋은 예: 안정적인 람다
@Composable
fun GoodExample(
    items: List<Item>,
    onItemClick: (Item) -> Unit
) {
    LazyColumn {
        items(items) { item ->
            ItemRow(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}
```

---

## 실전 예제

### 커스텀 Rating Bar

```kotlin
@Composable
fun RatingBar(
    rating: Float,
    maxRating: Int = 5,
    onRatingChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentRating by remember { mutableStateOf(rating) }
    
    Row(modifier = modifier) {
        repeat(maxRating) { index ->
            val filled = index < currentRating.toInt()
            val halfFilled = index < currentRating && index >= currentRating.toInt()
            
            Icon(
                imageVector = when {
                    filled -> Icons.Filled.Star
                    halfFilled -> Icons.Filled.StarHalf
                    else -> Icons.Filled.StarOutline
                },
                contentDescription = null,
                tint = if (filled || halfFilled) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        currentRating = (index + 1).toFloat()
                        onRatingChanged(currentRating)
                    }
            )
        }
    }
}
```

### 커스텀 차트

```kotlin
@Composable
fun BarChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / data.size
        val maxValue = data.maxOrNull() ?: 1f
        
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            
            drawRect(
                color = color,
                topLeft = Offset(
                    x = index * barWidth + barWidth * 0.1f,
                    y = size.height - barHeight
                ),
                size = Size(
                    width = barWidth * 0.8f,
                    height = barHeight
                )
            )
        }
    }
}
```

### 드래그 가능한 컴포넌트

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

### 확대/축소 가능한 이미지

```kotlin
@Composable
fun ZoomableImage(
    painter: Painter,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        
        if (scale > 1f) {
            offsetX += offsetChange.x
            offsetY += offsetChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }
    
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .transformable(state = state)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
    )
}
```

---

## 💡 베스트 프랙티스

### 1. Layout 성능

```kotlin
// ✅ 필요한 경우에만 Custom Layout 사용
// 기본 Layout (Column, Row 등)으로 충분한 경우 사용하지 않음
```

### 2. Canvas 최적화

```kotlin
// ✅ drawBehind 사용으로 재구성 최소화
Modifier.drawBehind {
    // 그리기 로직
}
```

### 3. Modifier 순서

```kotlin
// ✅ 올바른 순서
Modifier
    .size(100.dp)      // 크기 먼저
    .padding(16.dp)    // 패딩
    .background(Color.Red)  // 배경
    .clickable {}      // 클릭 영역
```

---

## 🎯 다음 단계

Advanced Compose를 마스터했습니다! 마지막으로:

1. **Complete App Example** - 모든 개념을 통합한 실전 프로젝트

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Composing! 🎨
