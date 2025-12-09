# Jetpack Compose Canvas 고급 가이드

## 목차
1. [변환 (Transform)](#변환-transform)
2. [애니메이션](#애니메이션)
3. [터치 이벤트 처리](#터치-이벤트-처리)
4. [블렌드 모드](#블렌드-모드)
5. [클리핑 (Clipping)](#클리핑-clipping)
6. [성능 최적화](#성능-최적화)
7. [실전 예제](#실전-예제)

---

## 변환 (Transform)

### 1. 회전 (Rotation)

```kotlin
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * 회전 변환 예제
 */
@Composable
fun RotationExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 중심점을 기준으로 45도 회전
        rotate(degrees = 45f, pivot = center) {
            drawRect(
                color = Color.Blue,
                topLeft = Offset(x = center.x - 50.dp.toPx(), y = center.y - 50.dp.toPx()),
                size = Size(width = 100.dp.toPx(), height = 100.dp.toPx())
            )
        }
        
        // 원본 (회전 안 함) - 비교용
        drawRect(
            color = Color.Red.copy(alpha = 0.3f),
            topLeft = Offset(x = center.x - 50.dp.toPx(), y = center.y - 50.dp.toPx()),
            size = Size(width = 100.dp.toPx(), height = 100.dp.toPx())
        )
    }
}
```

### 2. 크기 조정 (Scale)

```kotlin
import androidx.compose.ui.graphics.drawscope.scale

/**
 * 크기 조정 예제
 */
@Composable
fun ScaleExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 2배 확대
        scale(scale = 2f, pivot = center) {
            drawCircle(
                color = Color.Blue,
                radius = 25.dp.toPx(),
                center = center
            )
        }
        
        // 원본 크기 (비교용)
        drawCircle(
            color = Color.Red.copy(alpha = 0.3f),
            radius = 25.dp.toPx(),
            center = center
        )
    }
}
```

### 3. 이동 (Translation)

```kotlin
import androidx.compose.ui.graphics.drawscope.translate

/**
 * 이동 변환 예제
 */
@Composable
fun TranslateExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 원본
        drawCircle(
            color = Color.Red.copy(alpha = 0.3f),
            radius = 30.dp.toPx(),
            center = Offset(x = 50.dp.toPx(), y = 50.dp.toPx())
        )
        
        // 오른쪽으로 100px, 아래로 100px 이동
        translate(left = 100.dp.toPx(), top = 100.dp.toPx()) {
            drawCircle(
                color = Color.Blue,
                radius = 30.dp.toPx(),
                center = Offset(x = 50.dp.toPx(), y = 50.dp.toPx())
            )
        }
    }
}
```

### 4. 복합 변환

```kotlin
/**
 * 여러 변환을 조합한 예제
 */
@Composable
fun CombinedTransformExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 이동 → 회전 → 크기 조정
        translate(left = center.x, top = center.y) {
            rotate(degrees = 45f) {
                scale(scale = 1.5f) {
                    drawRect(
                        color = Color.Blue,
                        topLeft = Offset(x = -40.dp.toPx(), y = -40.dp.toPx()),
                        size = Size(width = 80.dp.toPx(), height = 80.dp.toPx())
                    )
                }
            }
        }
    }
}
```

---

## 애니메이션

### 1. 기본 애니메이션

```kotlin
import androidx.compose.animation.core.*

/**
 * 회전 애니메이션
 */
@Composable
fun RotatingSquare() {
    // 무한 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = Modifier.size(200.dp)) {
        rotate(degrees = rotation, pivot = center) {
            drawRect(
                color = Color.Blue,
                topLeft = Offset(x = center.x - 40.dp.toPx(), y = center.y - 40.dp.toPx()),
                size = Size(width = 80.dp.toPx(), height = 80.dp.toPx())
            )
        }
    }
}
```

### 2. 크기 애니메이션

```kotlin
/**
 * 펄스 효과 (크기 변화)
 */
@Composable
fun PulsingCircle() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Canvas(modifier = Modifier.size(200.dp)) {
        scale(scale = scale, pivot = center) {
            drawCircle(
                color = Color.Blue.copy(alpha = alpha),
                radius = 40.dp.toPx(),
                center = center
            )
        }
    }
}
```

### 3. 경로 애니메이션

```kotlin
/**
 * 경로를 따라 움직이는 애니메이션
 */
@Composable
fun PathAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "path")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    Canvas(modifier = Modifier.size(300.dp)) {
        // 경로 그리기
        val path = Path().apply {
            moveTo(x = 50.dp.toPx(), y = 150.dp.toPx())
            quadraticBezierTo(
                x1 = 150.dp.toPx(), y1 = 50.dp.toPx(),
                x2 = 250.dp.toPx(), y2 = 150.dp.toPx()
            )
        }
        
        drawPath(
            path = path,
            color = Color.Gray,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // 경로를 따라 움직이는 원
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        
        val position = FloatArray(2)
        pathMeasure.getPosTan(pathMeasure.length * progress, position, null)
        
        drawCircle(
            color = Color.Blue,
            radius = 10.dp.toPx(),
            center = Offset(x = position[0], y = position[1])
        )
    }
}
```

---

## 터치 이벤트 처리

### 1. 기본 터치 감지

```kotlin
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 터치 위치에 원 그리기
 */
@Composable
fun TouchDrawingExample() {
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    
    Canvas(
        modifier = Modifier
            .size(300.dp)
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    touchPoint = offset
                }
            }
    ) {
        touchPoint?.let { point ->
            drawCircle(
                color = Color.Blue,
                radius = 20.dp.toPx(),
                center = point
            )
        }
    }
}
```

### 2. 드래그로 그리기 (그림판)

```kotlin
import androidx.compose.foundation.gestures.detectDragGestures

/**
 * 간단한 그림판
 */
@Composable
fun SimpleDrawingBoard() {
    // 그린 경로들을 저장
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf(Path()) }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 새 경로 시작
                        currentPath = Path().apply {
                            moveTo(offset.x, offset.y)
                        }
                    },
                    onDrag = { change, _ ->
                        // 경로에 점 추가
                        currentPath.lineTo(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        // 경로 저장
                        paths.add(currentPath)
                        currentPath = Path()
                    }
                )
            }
    ) {
        // 저장된 모든 경로 그리기
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // 현재 그리는 경로
        drawPath(
            path = currentPath,
            color = Color.Black,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
```

### 3. 색상 선택 가능한 그림판

```kotlin
/**
 * 색상과 두께를 선택할 수 있는 그림판
 */
@Composable
fun AdvancedDrawingBoard() {
    data class DrawPath(
        val path: Path,
        val color: Color,
        val strokeWidth: Float
    )
    
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(5f) }
    
    Column {
        // 컨트롤 패널
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 색상 선택
            listOf(Color.Black, Color.Red, Color.Blue, Color.Green).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (selectedColor == color) 3.dp else 0.dp,
                            color = Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = color }
                )
            }
            
            // 지우기 버튼
            Button(onClick = { paths.clear() }) {
                Text("Clear")
            }
        }
        
        // 두께 조절
        Slider(
            value = strokeWidth,
            onValueChange = { strokeWidth = it },
            valueRange = 1f..20f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // 캔버스
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let { path ->
                                paths.add(DrawPath(path, selectedColor, strokeWidth))
                            }
                            currentPath = null
                        }
                    )
                }
        ) {
            // 저장된 경로들
            paths.forEach { drawPath ->
                drawPath(
                    path = drawPath.path,
                    color = drawPath.color,
                    style = Stroke(
                        width = drawPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            
            // 현재 그리는 경로
            currentPath?.let { path ->
                drawPath(
                    path = path,
                    color = selectedColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
```

---

## 블렌드 모드

```kotlin
import androidx.compose.ui.graphics.BlendMode

/**
 * 블렌드 모드 예제
 */
@Composable
fun BlendModeExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 첫 번째 원
        drawCircle(
            color = Color.Red,
            radius = 60.dp.toPx(),
            center = Offset(x = 100.dp.toPx(), y = 100.dp.toPx())
        )
        
        // 두 번째 원 (블렌드 모드 적용)
        drawCircle(
            color = Color.Blue,
            radius = 60.dp.toPx(),
            center = Offset(x = 140.dp.toPx(), y = 100.dp.toPx()),
            blendMode = BlendMode.Multiply  // 곱하기 모드
        )
    }
}

/**
 * 다양한 블렌드 모드 비교
 */
@Composable
fun BlendModesComparison() {
    val blendModes = listOf(
        BlendMode.SrcOver to "SrcOver (기본)",
        BlendMode.Multiply to "Multiply",
        BlendMode.Screen to "Screen",
        BlendMode.Overlay to "Overlay",
        BlendMode.Darken to "Darken",
        BlendMode.Lighten to "Lighten"
    )
    
    LazyColumn {
        items(blendModes) { (mode, name) ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                
                Canvas(modifier = Modifier.size(200.dp, 100.dp)) {
                    drawCircle(
                        color = Color.Red,
                        radius = 40.dp.toPx(),
                        center = Offset(x = 60.dp.toPx(), y = 50.dp.toPx())
                    )
                    
                    drawCircle(
                        color = Color.Blue,
                        radius = 40.dp.toPx(),
                        center = Offset(x = 100.dp.toPx(), y = 50.dp.toPx()),
                        blendMode = mode
                    )
                }
            }
        }
    }
}
```

---

## 클리핑 (Clipping)

### 1. 사각형 클리핑

```kotlin
import androidx.compose.ui.graphics.drawscope.clipRect

/**
 * 사각형 영역으로 클리핑
 */
@Composable
fun ClipRectExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        // 클리핑 영역 설정
        clipRect(
            left = 50.dp.toPx(),
            top = 50.dp.toPx(),
            right = 150.dp.toPx(),
            bottom = 150.dp.toPx()
        ) {
            // 이 영역 안에서만 그려짐
            drawCircle(
                color = Color.Blue,
                radius = 80.dp.toPx(),
                center = center
            )
        }
        
        // 클리핑 영역 표시 (참고용)
        drawRect(
            color = Color.Red,
            topLeft = Offset(x = 50.dp.toPx(), y = 50.dp.toPx()),
            size = Size(width = 100.dp.toPx(), height = 100.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
```

### 2. 원형 클리핑

```kotlin
import androidx.compose.ui.graphics.drawscope.clipPath

/**
 * 원형 영역으로 클리핑
 */
@Composable
fun ClipCircleExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val clipPath = Path().apply {
            addOval(Rect(center, 80.dp.toPx()))
        }
        
        clipPath(path = clipPath) {
            // 그라데이션이 원형으로 잘림
            val gradient = Brush.linearGradient(
                colors = listOf(Color.Red, Color.Blue, Color.Green)
            )
            
            drawRect(brush = gradient)
        }
    }
}
```

---

## 성능 최적화

### 1. drawWithCache 사용

```kotlin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

/**
 * 복잡한 그리기를 캐싱하여 성능 향상
 */
@Composable
fun CachedDrawingExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 복잡한 Path는 한 번만 생성하고 재사용
        drawWithCache {
            val complexPath = Path().apply {
                // 복잡한 경로 생성 (한 번만 실행됨)
                for (i in 0 until 100) {
                    val x = (i * 3).dp.toPx()
                    val y = center.y + kotlin.math.sin(i * 0.1) * 50.dp.toPx()
                    
                    if (i == 0) moveTo(x, y.toFloat())
                    else lineTo(x, y.toFloat())
                }
            }
            
            onDrawBehind {
                drawPath(
                    path = complexPath,
                    color = Color.Blue,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
```

### 2. remember 활용

```kotlin
/**
 * Path를 remember로 캐싱
 */
@Composable
fun RememberedPathExample() {
    // Path를 remember로 저장하여 재사용
    val starPath = remember {
        Path().apply {
            // 별 모양 경로 생성
            // (한 번만 생성됨)
        }
    }
    
    Canvas(modifier = Modifier.size(200.dp)) {
        drawPath(
            path = starPath,
            color = Color.Yellow
        )
    }
}
```

---

## 실전 예제

### 1. 아날로그 시계

```kotlin
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * 아날로그 시계
 */
@Composable
fun AnalogClock() {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    
    // 1초마다 업데이트
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            time = Calendar.getInstance()
        }
    }
    
    Canvas(modifier = Modifier.size(200.dp)) {
        val radius = size.minDimension / 2
        
        // 시계 테두리
        drawCircle(
            color = Color.Black,
            radius = radius,
            style = Stroke(width = 4.dp.toPx())
        )
        
        // 시간 눈금
        for (i in 0 until 12) {
            val angle = (i * 30 - 90) * PI / 180
            val startRadius = radius * 0.85f
            val endRadius = radius * 0.95f
            
            drawLine(
                color = Color.Black,
                start = Offset(
                    x = center.x + (startRadius * cos(angle)).toFloat(),
                    y = center.y + (startRadius * sin(angle)).toFloat()
                ),
                end = Offset(
                    x = center.x + (endRadius * cos(angle)).toFloat(),
                    y = center.y + (endRadius * sin(angle)).toFloat()
                ),
                strokeWidth = 3.dp.toPx()
            )
        }
        
        // 시침
        val hourAngle = ((time.get(Calendar.HOUR) % 12 + time.get(Calendar.MINUTE) / 60f) * 30 - 90) * PI / 180
        drawLine(
            color = Color.Black,
            start = center,
            end = Offset(
                x = center.x + (radius * 0.5f * cos(hourAngle)).toFloat(),
                y = center.y + (radius * 0.5f * sin(hourAngle)).toFloat()
            ),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // 분침
        val minuteAngle = (time.get(Calendar.MINUTE) * 6 - 90) * PI / 180
        drawLine(
            color = Color.Black,
            start = center,
            end = Offset(
                x = center.x + (radius * 0.7f * cos(minuteAngle)).toFloat(),
                y = center.y + (radius * 0.7f * sin(minuteAngle)).toFloat()
            ),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // 초침
        val secondAngle = (time.get(Calendar.SECOND) * 6 - 90) * PI / 180
        drawLine(
            color = Color.Red,
            start = center,
            end = Offset(
                x = center.x + (radius * 0.8f * cos(secondAngle)).toFloat(),
                y = center.y + (radius * 0.8f * sin(secondAngle)).toFloat()
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // 중심점
        drawCircle(
            color = Color.Black,
            radius = 6.dp.toPx()
        )
    }
}
```

### 2. 원형 차트 (Pie Chart)

```kotlin
/**
 * 원형 차트
 */
@Composable
fun PieChart(
    data: List<Pair<String, Float>>,  // (라벨, 값)
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(200.dp)) {
        val total = data.sumOf { it.second.toDouble() }.toFloat()
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, value) ->
            val sweepAngle = (value / total) * 360f
            
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(x = 20.dp.toPx(), y = 20.dp.toPx()),
                size = Size(
                    width = size.width - 40.dp.toPx(),
                    height = size.height - 40.dp.toPx()
                )
            )
            
            startAngle += sweepAngle
        }
    }
}

// 사용 예시
@Composable
fun PieChartExample() {
    val data = listOf(
        "Android" to 40f,
        "iOS" to 30f,
        "Web" to 20f,
        "Other" to 10f
    )
    
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFFF9800),
        Color(0xFF9C27B0)
    )
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        PieChart(data = data, colors = colors)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 범례
        data.forEachIndexed { index, (label, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(colors[index])
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$label: ${value.toInt()}%")
            }
        }
    }
}
```

### 3. 웨이브 애니메이션

```kotlin
/**
 * 물결 애니메이션
 */
@Composable
fun WaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val path = Path()
        val amplitude = 30.dp.toPx()
        val wavelength = size.width / 2
        
        path.moveTo(0f, center.y)
        
        for (x in 0..size.width.toInt()) {
            val y = center.y + amplitude * sin((x / wavelength * 2 * PI + phase).toFloat())
            path.lineTo(x.toFloat(), y)
        }
        
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2196F3).copy(alpha = 0.7f),
                    Color(0xFF2196F3).copy(alpha = 0.3f)
                )
            )
        )
    }
}
```

---

## 베스트 프랙티스

### 1. 성능 고려사항

```kotlin
// ✅ 좋은 예: Path를 재사용
val path = remember { Path() }

Canvas(modifier = Modifier.size(200.dp)) {
    path.reset()  // 기존 경로 초기화
    path.moveTo(...)
    path.lineTo(...)
    drawPath(path, color = Color.Blue)
}

// ❌ 나쁜 예: 매번 새로 생성
Canvas(modifier = Modifier.size(200.dp)) {
    val path = Path()  // 매 리컴포지션마다 생성
    path.moveTo(...)
    drawPath(path, color = Color.Blue)
}
```

### 2. 좌표 변환

```kotlin
// ✅ 좋은 예: dp를 px로 변환
val radius = 50.dp.toPx()

// ❌ 나쁜 예: 하드코딩된 px 값
val radius = 150f  // 기기마다 다르게 보임
```

### 3. 색상 관리

```kotlin
// ✅ 좋은 예: MaterialTheme 색상 사용
drawCircle(
    color = MaterialTheme.colorScheme.primary
)

// ✅ 좋은 예: 상수로 정의
val ChartColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3)
)
```

---

## 참고 자료

- [Compose Graphics 공식 문서](https://developer.android.com/jetpack/compose/graphics)
- [Canvas API 레퍼런스](https://developer.android.com/reference/kotlin/androidx/compose/foundation/Canvas)
- [DrawScope 레퍼런스](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/drawscope/DrawScope)
