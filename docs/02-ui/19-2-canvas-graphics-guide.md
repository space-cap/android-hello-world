# Canvas와 그래픽 완벽 가이드

> 📖 **시리즈 구성**
> - **19-1**: [Custom Layout 완벽 가이드](./19-1-custom-layout-guide.md)
> - **19-2**: Canvas와 그래픽 완벽 가이드 (현재 문서)
> - **19-3**: [Modifier 고급 기법](./19-3-modifier-advanced.md)

---

## 📚 목차

1. [Canvas 기초](#canvas-기초)
2. [기본 도형 그리기](#기본-도형-그리기)
3. [Path와 베지어 곡선](#path와-베지어-곡선)
4. [그라데이션과 효과](#그라데이션과-효과)
5. [실전 예제](#실전-예제)

---

## Canvas 기초

### Canvas란?

**Canvas는 Compose에서 커스텀 그래픽을 그리는 도구입니다.**

```kotlin
// 가장 기본적인 Canvas
@Composable
fun BasicCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // 여기에 그리기 코드 작성
        // size: Canvas의 크기 (width, height)
        // center: Canvas의 중심점
    }
}
```

**Canvas에서 사용 가능한 속성:**
```kotlin
Canvas(modifier) {
    size.width    // Canvas 너비 (px)
    size.height   // Canvas 높이 (px)
    center        // 중심점 Offset(x, y)
}
```

---

## 기본 도형 그리기

### 1. 원 그리기

```kotlin
/**
 * 원을 그리는 다양한 방법
 */
@Composable
fun CircleExamples() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 방법 1: 중심점과 반지름으로 그리기
        drawCircle(
            color = Color.Red,
            radius = 50.dp.toPx(),  // dp를 px로 변환
            center = Offset(100.dp.toPx(), 100.dp.toPx())
        )
        
        // 방법 2: 투명도 조절
        drawCircle(
            color = Color.Blue.copy(alpha = 0.5f),  // 50% 투명
            radius = 60.dp.toPx(),
            center = Offset(250.dp.toPx(), 100.dp.toPx())
        )
        
        // 방법 3: 테두리만 그리기
        drawCircle(
            color = Color.Green,
            radius = 50.dp.toPx(),
            center = Offset(100.dp.toPx(), 250.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())  // 테두리 스타일
        )
        
        // 방법 4: 채우기 + 테두리
        drawCircle(
            color = Color.Yellow,
            radius = 50.dp.toPx(),
            center = Offset(250.dp.toPx(), 250.dp.toPx()),
            style = Fill  // 채우기 (기본값)
        )
        drawCircle(
            color = Color.Black,
            radius = 50.dp.toPx(),
            center = Offset(250.dp.toPx(), 250.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
```

**Stroke vs Fill:**
```kotlin
// Fill: 도형 내부를 채움 (기본값)
style = Fill

// Stroke: 테두리만 그림
style = Stroke(
    width = 4.dp.toPx(),           // 선 두께
    cap = StrokeCap.Round,         // 선 끝 모양 (Round, Butt, Square)
    join = StrokeJoin.Round,       // 선 연결 모양 (Round, Miter, Bevel)
    pathEffect = PathEffect.dashPathEffect(...)  // 점선 효과
)
```

### 2. 사각형 그리기

```kotlin
@Composable
fun RectangleExamples() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 방법 1: 위치와 크기로 그리기
        drawRect(
            color = Color.Red,
            topLeft = Offset(50.dp.toPx(), 50.dp.toPx()),
            size = Size(100.dp.toPx(), 80.dp.toPx())
        )
        
        // 방법 2: 둥근 모서리 사각형
        drawRoundRect(
            color = Color.Blue,
            topLeft = Offset(200.dp.toPx(), 50.dp.toPx()),
            size = Size(100.dp.toPx(), 80.dp.toPx()),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        
        // 방법 3: 테두리만
        drawRect(
            color = Color.Green,
            topLeft = Offset(50.dp.toPx(), 180.dp.toPx()),
            size = Size(100.dp.toPx(), 80.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
```

### 3. 선 그리기

```kotlin
@Composable
fun LineExamples() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 일반 선
        drawLine(
            color = Color.Black,
            start = Offset(50.dp.toPx(), 50.dp.toPx()),
            end = Offset(300.dp.toPx(), 50.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
        
        // 둥근 끝 선
        drawLine(
            color = Color.Red,
            start = Offset(50.dp.toPx(), 100.dp.toPx()),
            end = Offset(300.dp.toPx(), 100.dp.toPx()),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round  // 둥근 끝
        )
        
        // 점선
        drawLine(
            color = Color.Blue,
            start = Offset(50.dp.toPx(), 150.dp.toPx()),
            end = Offset(300.dp.toPx(), 150.dp.toPx()),
            strokeWidth = 4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(10f, 10f),  // 선 10px, 공백 10px
                phase = 0f
            )
        )
    }
}
```

### 4. 호(Arc) 그리기

```kotlin
@Composable
fun ArcExamples() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 파이 차트 스타일 (중심 포함)
        drawArc(
            color = Color.Red,
            startAngle = 0f,      // 시작 각도 (0도 = 3시 방향)
            sweepAngle = 90f,     // 그릴 각도
            useCenter = true,     // 중심점 포함
            topLeft = Offset(50.dp.toPx(), 50.dp.toPx()),
            size = Size(100.dp.toPx(), 100.dp.toPx())
        )
        
        // 호 스타일 (중심 미포함)
        drawArc(
            color = Color.Blue,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,    // 중심점 미포함
            topLeft = Offset(200.dp.toPx(), 50.dp.toPx()),
            size = Size(100.dp.toPx(), 100.dp.toPx()),
            style = Stroke(width = 8.dp.toPx())
        )
        
        // 진행률 표시기
        drawArc(
            color = Color.Green,
            startAngle = -90f,    // 12시 방향부터 시작
            sweepAngle = 270f,    // 75% 진행
            useCenter = false,
            topLeft = Offset(50.dp.toPx(), 180.dp.toPx()),
            size = Size(100.dp.toPx(), 100.dp.toPx()),
            style = Stroke(
                width = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
```

**각도 설명:**
```
        -90° (12시)
           |
180° ------+------ 0° (3시)
           |
        90° (6시)
```

---

## Path와 베지어 곡선

### Path 기초

```kotlin
/**
 * Path는 복잡한 도형을 그리기 위한 도구입니다
 */
@Composable
fun PathBasics() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val path = Path().apply {
            // 시작점으로 이동
            moveTo(50.dp.toPx(), 100.dp.toPx())
            
            // 직선 그리기
            lineTo(150.dp.toPx(), 50.dp.toPx())
            lineTo(250.dp.toPx(), 100.dp.toPx())
            lineTo(200.dp.toPx(), 150.dp.toPx())
            
            // 경로 닫기 (시작점으로 선 그리기)
            close()
        }
        
        drawPath(
            path = path,
            color = Color.Blue,
            style = Fill
        )
    }
}
```

### 베지어 곡선

```kotlin
@Composable
fun BezierCurves() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val path = Path().apply {
            moveTo(50.dp.toPx(), 150.dp.toPx())
            
            // 2차 베지어 곡선 (제어점 1개)
            quadraticBezierTo(
                x1 = 150.dp.toPx(),  // 제어점 X
                y1 = 50.dp.toPx(),   // 제어점 Y
                x2 = 250.dp.toPx(),  // 끝점 X
                y2 = 150.dp.toPx()   // 끝점 Y
            )
        }
        
        drawPath(
            path = path,
            color = Color.Red,
            style = Stroke(width = 4.dp.toPx())
        )
        
        // 3차 베지어 곡선 (제어점 2개)
        val cubicPath = Path().apply {
            moveTo(50.dp.toPx(), 250.dp.toPx())
            
            cubicTo(
                x1 = 100.dp.toPx(),  // 제어점1 X
                y1 = 200.dp.toPx(),  // 제어점1 Y
                x2 = 200.dp.toPx(),  // 제어점2 X
                y2 = 300.dp.toPx(),  // 제어점2 Y
                x3 = 250.dp.toPx(),  // 끝점 X
                y3 = 250.dp.toPx()   // 끝점 Y
            )
        }
        
        drawPath(
            path = cubicPath,
            color = Color.Blue,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
```

### 물결 모양 만들기

```kotlin
@Composable
fun WaveShape() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val path = Path().apply {
            // 시작점
            moveTo(0f, size.height / 2)
            
            // 물결 그리기
            var x = 0f
            val waveLength = size.width / 4  // 한 파장의 길이
            
            while (x < size.width) {
                // 위로 올라가는 곡선
                quadraticBezierTo(
                    x1 = x + waveLength / 2,
                    y1 = size.height / 4,
                    x2 = x + waveLength,
                    y2 = size.height / 2
                )
                
                // 아래로 내려가는 곡선
                quadraticBezierTo(
                    x1 = x + waveLength * 1.5f,
                    y1 = size.height * 3 / 4,
                    x2 = x + waveLength * 2,
                    y2 = size.height / 2
                )
                
                x += waveLength * 2
            }
            
            // 아래쪽 채우기
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

## 그라데이션과 효과

### 선형 그라데이션

```kotlin
@Composable
fun LinearGradientExample() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // 가로 그라데이션
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Red, Color.Blue),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f)
            )
        )
    }
}
```

### 방사형 그라데이션

```kotlin
@Composable
fun RadialGradientExample() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Yellow,
                    Color.Orange,
                    Color.Red
                ),
                center = center,
                radius = size.minDimension / 2
            )
        )
    }
}
```

### 각도 그라데이션

```kotlin
@Composable
fun SweepGradientExample() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red
                ),
                center = center
            )
        )
    }
}
```

---

## 실전 예제

### 1. 원형 진행률 표시기

```kotlin
@Composable
fun CircularProgressIndicator(
    progress: Float,  // 0.0 ~ 1.0
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 8.dp
) {
    Canvas(modifier = modifier.size(100.dp)) {
        val diameter = size.minDimension
        val radius = diameter / 2
        val strokeWidthPx = strokeWidth.toPx()
        
        // 배경 원 (회색)
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = radius - strokeWidthPx / 2,
            style = Stroke(width = strokeWidthPx)
        )
        
        // 진행 호 (색상)
        drawArc(
            color = color,
            startAngle = -90f,  // 12시 방향부터
            sweepAngle = 360f * progress,  // 진행률만큼
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

// 사용 예제
@Composable
fun ProgressDemo() {
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            progress = (progress + 0.01f) % 1f
        }
    }
    
    CircularProgressIndicator(progress = progress)
}
```

### 2. 막대 차트

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

// 사용 예제
@Composable
fun ChartDemo() {
    BarChart(
        data = listOf(10f, 25f, 15f, 30f, 20f, 35f),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}
```

### 3. 커스텀 체크박스

```kotlin
@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkmarkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        label = "checkmark"
    )
    
    Canvas(
        modifier = modifier
            .size(24.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        // 배경 사각형
        drawRoundRect(
            color = if (checked) Color.Blue else Color.Gray,
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = if (checked) Fill else Stroke(width = 2.dp.toPx())
        )
        
        // 체크 마크
        if (checkmarkProgress > 0f) {
            val path = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.5f)
                lineTo(size.width * 0.4f, size.height * 0.65f)
                lineTo(size.width * 0.75f, size.height * 0.35f)
            }
            
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(
                            size.width * checkmarkProgress,
                            size.width * (1 - checkmarkProgress)
                        )
                    )
                )
            )
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. dp를 px로 변환

```kotlin
// ✅ 항상 dp를 px로 변환
Canvas(modifier) {
    val radiusPx = 50.dp.toPx()  // dp → px
    drawCircle(radius = radiusPx, ...)
}
```

### 2. 성능 최적화

```kotlin
// ✅ Path 재사용
val path = remember { Path() }

Canvas(modifier) {
    path.reset()  // 기존 경로 초기화
    path.moveTo(...)
    path.lineTo(...)
    drawPath(path, ...)
}
```

### 3. 애니메이션

```kotlin
// ✅ animateFloatAsState 사용
val progress by animateFloatAsState(targetValue = targetProgress)

Canvas(modifier) {
    drawArc(sweepAngle = 360f * progress, ...)
}
```

---

## 🎯 다음 단계

Canvas를 마스터했습니다! 다음으로:

1. **[19-3. Modifier 고급 기법](./19-3-modifier-advanced.md)** - 커스텀 Modifier 만들기

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Drawing! 🎨
