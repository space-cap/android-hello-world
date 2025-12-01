# Jetpack Compose Canvas 기본 가이드

## 목차
1. [Canvas란?](#canvas란)
2. [Canvas 기본 사용법](#canvas-기본-사용법)
3. [기본 도형 그리기](#기본-도형-그리기)
4. [색상과 스타일](#색상과-스타일)
5. [Path를 사용한 복잡한 도형](#path를-사용한-복잡한-도형)
6. [텍스트 그리기](#텍스트-그리기)
7. [이미지 그리기](#이미지-그리기)
8. [좌표계 이해하기](#좌표계-이해하기)

---

## Canvas란?

**Canvas**는 Jetpack Compose에서 **자유롭게 그림을 그릴 수 있는 영역**입니다.

### 왜 Canvas를 사용하나요?

기본 Compose 컴포넌트(Button, Text 등)로는 만들 수 없는 **커스텀 UI**를 만들 때 사용합니다.

#### 사용 사례
- 📊 **차트 및 그래프**: 막대 차트, 원형 차트, 선 그래프
- 🎨 **그림판 앱**: 손가락으로 그리기
- 🎮 **게임**: 캐릭터, 배경, 애니메이션
- 📈 **데이터 시각화**: 복잡한 데이터 표현
- 🎭 **커스텀 아이콘**: 독특한 디자인의 아이콘

### Canvas vs 일반 Composable

```kotlin
// ❌ 일반 Composable로는 어려움
@Composable
fun ComplexShape() {
    // 복잡한 도형을 만들기 어려움
}

// ✅ Canvas로 쉽게 구현
@Composable
fun ComplexShape() {
    Canvas(modifier = Modifier.size(200.dp)) {
        // 자유롭게 그리기 가능!
        drawCircle(color = Color.Blue)
        drawLine(...)
        drawPath(...)
    }
}
```

---

## Canvas 기본 사용법

### 기본 구조

```kotlin
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 가장 기본적인 Canvas 사용 예제
 */
@Composable
fun BasicCanvasExample() {
    Canvas(
        modifier = Modifier
            .size(200.dp)  // Canvas 크기 지정
            .background(Color.LightGray)  // 배경색 (선택사항)
    ) {
        // 여기에 그리기 코드 작성
        // size: Canvas의 크기 (Size 객체)
        // center: Canvas의 중심점 (Offset 객체)
        
        println("Canvas 크기: ${size.width} x ${size.height}")
        println("Canvas 중심: ${center.x}, ${center.y}")
    }
}
```

### DrawScope 이해하기

Canvas 블록 안에서는 **DrawScope**라는 특별한 영역에서 작업합니다.

```kotlin
@Composable
fun DrawScopeExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        // 이 블록 안이 DrawScope
        
        // DrawScope에서 사용 가능한 속성들
        val canvasWidth = size.width      // Canvas 너비 (px)
        val canvasHeight = size.height    // Canvas 높이 (px)
        val centerX = center.x            // 중심 X 좌표
        val centerY = center.y            // 중심 Y 좌표
        
        // DrawScope에서 사용 가능한 함수들
        drawCircle(...)   // 원 그리기
        drawRect(...)     // 사각형 그리기
        drawLine(...)     // 선 그리기
        drawPath(...)     // 경로 그리기
        // 등등...
    }
}
```

---

## 기본 도형 그리기

### 1. 원 (Circle)

```kotlin
/**
 * 원 그리기 예제
 */
@Composable
fun CircleExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        // 기본 원 (중앙에 그려짐)
        drawCircle(
            color = Color.Blue,
            radius = 50.dp.toPx()  // dp를 px로 변환
        )
        
        // 위치를 지정한 원
        drawCircle(
            color = Color.Red,
            radius = 30.dp.toPx(),
            center = Offset(x = 100.dp.toPx(), y = 100.dp.toPx())
        )
        
        // 투명도가 있는 원
        drawCircle(
            color = Color.Green.copy(alpha = 0.5f),  // 50% 투명
            radius = 40.dp.toPx()
        )
    }
}
```

### 2. 사각형 (Rectangle)

```kotlin
/**
 * 사각형 그리기 예제
 */
@Composable
fun RectangleExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 기본 사각형 (전체 Canvas 크기)
        drawRect(
            color = Color.Blue
        )
        
        // 크기와 위치를 지정한 사각형
        drawRect(
            color = Color.Red,
            topLeft = Offset(x = 50.dp.toPx(), y = 50.dp.toPx()),
            size = Size(width = 100.dp.toPx(), height = 80.dp.toPx())
        )
        
        // 테두리만 있는 사각형
        drawRect(
            color = Color.Green,
            topLeft = Offset(x = 150.dp.toPx(), y = 150.dp.toPx()),
            size = Size(width = 100.dp.toPx(), height = 100.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())  // 테두리 스타일
        )
    }
}
```

### 3. 둥근 사각형 (Rounded Rectangle)

```kotlin
/**
 * 둥근 사각형 그리기 예제
 */
@Composable
fun RoundedRectExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        // 모서리가 둥근 사각형
        drawRoundRect(
            color = Color.Blue,
            topLeft = Offset(x = 20.dp.toPx(), y = 20.dp.toPx()),
            size = Size(width = 160.dp.toPx(), height = 100.dp.toPx()),
            cornerRadius = CornerRadius(x = 16.dp.toPx(), y = 16.dp.toPx())
        )
        
        // 완전히 둥근 사각형 (캡슐 모양)
        drawRoundRect(
            color = Color.Red,
            topLeft = Offset(x = 50.dp.toPx(), y = 130.dp.toPx()),
            size = Size(width = 100.dp.toPx(), height = 50.dp.toPx()),
            cornerRadius = CornerRadius(x = 25.dp.toPx(), y = 25.dp.toPx())
        )
    }
}
```

### 4. 선 (Line)

```kotlin
/**
 * 선 그리기 예제
 */
@Composable
fun LineExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 기본 선
        drawLine(
            color = Color.Black,
            start = Offset(x = 0f, y = 0f),           // 시작점
            end = Offset(x = size.width, y = size.height),  // 끝점
            strokeWidth = 2.dp.toPx()                 // 선 두께
        )
        
        // 수평선
        drawLine(
            color = Color.Red,
            start = Offset(x = 0f, y = center.y),
            end = Offset(x = size.width, y = center.y),
            strokeWidth = 4.dp.toPx()
        )
        
        // 수직선
        drawLine(
            color = Color.Blue,
            start = Offset(x = center.x, y = 0f),
            end = Offset(x = center.x, y = size.height),
            strokeWidth = 4.dp.toPx()
        )
        
        // 점선
        drawLine(
            color = Color.Green,
            start = Offset(x = 50.dp.toPx(), y = 50.dp.toPx()),
            end = Offset(x = 250.dp.toPx(), y = 250.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(10f, 10f),  // 선 10px, 공백 10px
                phase = 0f
            )
        )
    }
}
```

### 5. 호 (Arc)

```kotlin
/**
 * 호 그리기 예제
 */
@Composable
fun ArcExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val rect = Rect(
            left = 20.dp.toPx(),
            top = 20.dp.toPx(),
            right = 180.dp.toPx(),
            bottom = 180.dp.toPx()
        )
        
        // 부채꼴 (채워진 호)
        drawArc(
            color = Color.Blue,
            startAngle = 0f,      // 시작 각도 (0도 = 3시 방향)
            sweepAngle = 90f,     // 그릴 각도 (90도)
            useCenter = true,     // 중심점 사용 (부채꼴)
            topLeft = rect.topLeft,
            size = rect.size
        )
        
        // 호 (테두리만)
        drawArc(
            color = Color.Red,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,    // 중심점 미사용 (호)
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}
```

### 6. 타원 (Oval)

```kotlin
/**
 * 타원 그리기 예제
 */
@Composable
fun OvalExample() {
    Canvas(modifier = Modifier.size(300.dp, 200.dp)) {
        // 가로로 긴 타원
        drawOval(
            color = Color.Blue,
            topLeft = Offset(x = 20.dp.toPx(), y = 20.dp.toPx()),
            size = Size(width = 260.dp.toPx(), height = 80.dp.toPx())
        )
        
        // 세로로 긴 타원
        drawOval(
            color = Color.Red,
            topLeft = Offset(x = 100.dp.toPx(), y = 50.dp.toPx()),
            size = Size(width = 100.dp.toPx(), height = 140.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
```

---

## 색상과 스타일

### 1. 색상 지정

```kotlin
/**
 * 다양한 색상 지정 방법
 */
@Composable
fun ColorExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 기본 색상
        drawCircle(
            color = Color.Red,
            radius = 30.dp.toPx(),
            center = Offset(x = 50.dp.toPx(), y = 50.dp.toPx())
        )
        
        // RGB 색상
        drawCircle(
            color = Color(red = 0, green = 128, blue = 255),
            radius = 30.dp.toPx(),
            center = Offset(x = 150.dp.toPx(), y = 50.dp.toPx())
        )
        
        // Hex 색상
        drawCircle(
            color = Color(0xFF00FF00),  // 녹색
            radius = 30.dp.toPx(),
            center = Offset(x = 250.dp.toPx(), y = 50.dp.toPx())
        )
        
        // 투명도가 있는 색상
        drawCircle(
            color = Color.Blue.copy(alpha = 0.5f),  // 50% 투명
            radius = 30.dp.toPx(),
            center = Offset(x = 100.dp.toPx(), y = 150.dp.toPx())
        )
    }
}
```

### 2. 그라데이션 (Gradient)

```kotlin
import androidx.compose.ui.graphics.Brush

/**
 * 그라데이션 예제
 */
@Composable
fun GradientExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // 선형 그라데이션 (위에서 아래로)
        val linearGradient = Brush.linearGradient(
            colors = listOf(Color.Red, Color.Blue),
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = 0f, y = size.height)
        )
        
        drawRect(
            brush = linearGradient,
            size = Size(width = 100.dp.toPx(), height = 200.dp.toPx())
        )
        
        // 방사형 그라데이션 (중심에서 바깥으로)
        val radialGradient = Brush.radialGradient(
            colors = listOf(Color.Yellow, Color.Red, Color.Black),
            center = Offset(x = 200.dp.toPx(), y = 100.dp.toPx()),
            radius = 80.dp.toPx()
        )
        
        drawCircle(
            brush = radialGradient,
            radius = 80.dp.toPx(),
            center = Offset(x = 200.dp.toPx(), y = 100.dp.toPx())
        )
        
        // 각도 그라데이션 (회전)
        val sweepGradient = Brush.sweepGradient(
            colors = listOf(
                Color.Red,
                Color.Yellow,
                Color.Green,
                Color.Cyan,
                Color.Blue,
                Color.Magenta,
                Color.Red
            ),
            center = Offset(x = 150.dp.toPx(), y = 250.dp.toPx())
        )
        
        drawCircle(
            brush = sweepGradient,
            radius = 40.dp.toPx(),
            center = Offset(x = 150.dp.toPx(), y = 250.dp.toPx())
        )
    }
}
```

### 3. 스타일 (Fill vs Stroke)

```kotlin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Fill과 Stroke 비교
 */
@Composable
fun StyleExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // Fill (채우기) - 기본값
        drawCircle(
            color = Color.Blue,
            radius = 40.dp.toPx(),
            center = Offset(x = 60.dp.toPx(), y = 60.dp.toPx())
        )
        
        // Stroke (테두리만)
        drawCircle(
            color = Color.Red,
            radius = 40.dp.toPx(),
            center = Offset(x = 180.dp.toPx(), y = 60.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
        
        // Fill + Stroke (채우기 + 테두리)
        drawCircle(
            color = Color.Green,
            radius = 40.dp.toPx(),
            center = Offset(x = 120.dp.toPx(), y = 180.dp.toPx())
        )
        drawCircle(
            color = Color.Black,
            radius = 40.dp.toPx(),
            center = Offset(x = 120.dp.toPx(), y = 180.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // 점선 테두리
        drawCircle(
            color = Color.Magenta,
            radius = 40.dp.toPx(),
            center = Offset(x = 240.dp.toPx(), y = 180.dp.toPx()),
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 5f)  // 선 10px, 공백 5px
                )
            )
        )
    }
}
```

---

## Path를 사용한 복잡한 도형

### Path란?

**Path**는 여러 점을 연결하여 복잡한 도형을 만드는 방법입니다.

### 기본 Path 사용

```kotlin
import androidx.compose.ui.graphics.Path

/**
 * 기본 Path 예제
 */
@Composable
fun BasicPathExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        val path = Path().apply {
            // 시작점으로 이동
            moveTo(x = 50.dp.toPx(), y = 50.dp.toPx())
            
            // 선으로 연결
            lineTo(x = 250.dp.toPx(), y = 50.dp.toPx())
            lineTo(x = 250.dp.toPx(), y = 250.dp.toPx())
            lineTo(x = 50.dp.toPx(), y = 250.dp.toPx())
            
            // 경로 닫기 (시작점으로 연결)
            close()
        }
        
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
```

### 삼각형 그리기

```kotlin
/**
 * 삼각형 그리기
 */
@Composable
fun TriangleExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val path = Path().apply {
            // 꼭짓점 1 (상단 중앙)
            moveTo(x = center.x, y = 20.dp.toPx())
            
            // 꼭짓점 2 (우측 하단)
            lineTo(x = 180.dp.toPx(), y = 180.dp.toPx())
            
            // 꼭짓점 3 (좌측 하단)
            lineTo(x = 20.dp.toPx(), y = 180.dp.toPx())
            
            // 시작점으로 연결
            close()
        }
        
        // 채워진 삼각형
        drawPath(
            path = path,
            color = Color.Blue
        )
        
        // 테두리
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
```

### 별 그리기

```kotlin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * 별 그리기
 */
@Composable
fun StarExample() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val path = Path()
        val outerRadius = 80.dp.toPx()  // 바깥쪽 반지름
        val innerRadius = 40.dp.toPx()  // 안쪽 반지름
        val centerX = center.x
        val centerY = center.y
        
        // 5개의 꼭짓점을 가진 별
        for (i in 0 until 10) {
            val angle = (i * 36 - 90) * PI / 180  // 각도 (도 → 라디안)
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            
            val x = centerX + (radius * cos(angle)).toFloat()
            val y = centerY + (radius * sin(angle)).toFloat()
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        path.close()
        
        // 그라데이션으로 채우기
        val gradient = Brush.linearGradient(
            colors = listOf(Color.Yellow, Color(0xFFFFD700))
        )
        
        drawPath(
            path = path,
            brush = gradient
        )
        
        // 테두리
        drawPath(
            path = path,
            color = Color(0xFFFF8C00),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
```

### 곡선 그리기 (Bezier Curve)

```kotlin
/**
 * 베지어 곡선 예제
 */
@Composable
fun BezierCurveExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        val path = Path()
        
        // 시작점
        path.moveTo(x = 50.dp.toPx(), y = 150.dp.toPx())
        
        // 2차 베지어 곡선 (제어점 1개)
        path.quadraticBezierTo(
            x1 = 150.dp.toPx(), y1 = 50.dp.toPx(),   // 제어점
            x2 = 250.dp.toPx(), y2 = 150.dp.toPx()   // 끝점
        )
        
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // 제어점 표시 (참고용)
        drawCircle(
            color = Color.Red,
            radius = 5.dp.toPx(),
            center = Offset(x = 150.dp.toPx(), y = 50.dp.toPx())
        )
    }
}
```

---

## 텍스트 그리기

```kotlin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint

/**
 * Canvas에 텍스트 그리기
 */
@Composable
fun TextOnCanvasExample() {
    Canvas(modifier = Modifier.size(300.dp)) {
        // Android Paint 사용
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            
            canvas.nativeCanvas.drawText(
                "Hello Canvas!",
                center.x,
                center.y,
                paint
            )
        }
    }
}
```

---

## 이미지 그리기

```kotlin
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource

/**
 * Canvas에 이미지 그리기
 */
@Composable
fun ImageOnCanvasExample() {
    val image = ImageBitmap.imageResource(id = R.drawable.sample_image)
    
    Canvas(modifier = Modifier.size(300.dp)) {
        // 이미지 그리기
        drawImage(
            image = image,
            topLeft = Offset(x = 50.dp.toPx(), y = 50.dp.toPx())
        )
        
        // 크기 조정하여 그리기
        drawImage(
            image = image,
            dstOffset = IntOffset(x = 150, y = 150),
            dstSize = IntSize(width = 100, height = 100)
        )
    }
}
```

---

## 좌표계 이해하기

### Canvas 좌표계

```
(0, 0) ────────────────→ X축
  │
  │
  │
  │
  ↓
 Y축

- 원점 (0, 0)은 좌측 상단
- X축은 오른쪽으로 증가
- Y축은 아래쪽으로 증가
```

### 좌표 변환 예제

```kotlin
/**
 * 좌표계 이해를 위한 예제
 */
@Composable
fun CoordinateSystemExample() {
    Canvas(
        modifier = Modifier
            .size(300.dp)
            .background(Color.LightGray)
    ) {
        // 원점 (0, 0) - 좌측 상단
        drawCircle(
            color = Color.Red,
            radius = 10.dp.toPx(),
            center = Offset(x = 0f, y = 0f)
        )
        
        // 중심점
        drawCircle(
            color = Color.Blue,
            radius = 10.dp.toPx(),
            center = center  // Offset(x = size.width/2, y = size.height/2)
        )
        
        // 우측 하단
        drawCircle(
            color = Color.Green,
            radius = 10.dp.toPx(),
            center = Offset(x = size.width, y = size.height)
        )
        
        // 좌표 눈금 그리기
        for (i in 0..10) {
            val x = i * 30.dp.toPx()
            drawLine(
                color = Color.Gray,
                start = Offset(x = x, y = 0f),
                end = Offset(x = x, y = size.height),
                strokeWidth = 1f
            )
            
            val y = i * 30.dp.toPx()
            drawLine(
                color = Color.Gray,
                start = Offset(x = 0f, y = y),
                end = Offset(x = size.width, y = y),
                strokeWidth = 1f
            )
        }
    }
}
```

---

## 실전 예제

### 1. 프로그레스 바

```kotlin
/**
 * 원형 프로그레스 바
 */
@Composable
fun CircularProgressBar(
    progress: Float,  // 0.0 ~ 1.0
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(100.dp)) {
        val strokeWidth = 8.dp.toPx()
        
        // 배경 원
        drawCircle(
            color = Color.LightGray,
            radius = (size.minDimension / 2) - strokeWidth / 2,
            style = Stroke(width = strokeWidth)
        )
        
        // 진행률 호
        drawArc(
            color = Color.Blue,
            startAngle = -90f,  // 12시 방향부터 시작
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round  // 둥근 끝
            ),
            size = Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth
            ),
            topLeft = Offset(x = strokeWidth / 2, y = strokeWidth / 2)
        )
    }
}

// 사용 예시
@Composable
fun ProgressExample() {
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress += 0.01f
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressBar(progress = progress)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("${(progress * 100).toInt()}%")
    }
}
```

### 2. 간단한 차트

```kotlin
/**
 * 막대 차트
 */
@Composable
fun SimpleBarChart(
    data: List<Float>,  // 데이터 값들
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val barWidth = size.width / (data.size * 2)
        val maxValue = data.maxOrNull() ?: 1f
        
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height * 0.8f
            val x = index * barWidth * 2 + barWidth / 2
            
            drawRect(
                color = Color.Blue,
                topLeft = Offset(
                    x = x,
                    y = size.height - barHeight
                ),
                size = Size(
                    width = barWidth,
                    height = barHeight
                )
            )
        }
    }
}

// 사용 예시
@Composable
fun ChartExample() {
    val data = remember { listOf(10f, 25f, 15f, 30f, 20f) }
    
    SimpleBarChart(
        data = data,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    )
}
```

---

## 다음 단계

다음 문서에서는:
- **변환 (Transform)**: 회전, 크기 조정, 이동
- **애니메이션**: Canvas 애니메이션
- **터치 이벤트**: 그림판 만들기
- **고급 기법**: 블렌드 모드, 클리핑

를 다룹니다.

## 참고 자료

- [Compose Graphics 공식 문서](https://developer.android.com/jetpack/compose/graphics)
- [Canvas API 레퍼런스](https://developer.android.com/reference/kotlin/androidx/compose/foundation/Canvas)
