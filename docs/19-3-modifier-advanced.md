# Modifier 고급 기법

> 📖 **시리즈 구성**
> - **19-1**: [Custom Layout 완벽 가이드](./19-1-custom-layout-guide.md)
> - **19-2**: [Canvas와 그래픽 완벽 가이드](./19-2-canvas-graphics-guide.md)
> - **19-3**: Modifier 고급 기법 (현재 문서)

---

## 📚 목차

1. [Modifier 기초 복습](#modifier-기초-복습)
2. [커스텀 Modifier 만들기](#커스텀-modifier-만들기)
3. [Modifier.composed](#modifiercomposed)
4. [제스처 처리](#제스처-처리)
5. [실전 예제](#실전-예제)

---

## Modifier 기초 복습

### Modifier 체이닝 순서의 중요성

```kotlin
// 순서가 다르면 결과도 다릅니다!

// 예제 1: padding → background
Box(
    Modifier
        .padding(16.dp)      // 먼저 패딩
        .background(Color.Red)  // 그 다음 배경
) // 결과: 빨간 배경이 패딩 안쪽에만

// 예제 2: background → padding
Box(
    Modifier
        .background(Color.Red)  // 먼저 배경
        .padding(16.dp)      // 그 다음 패딩
) // 결과: 빨간 배경이 패딩 포함 전체

// 권장 순서:
Modifier
    .size(100.dp)           // 1. 크기
    .padding(16.dp)         // 2. 패딩
    .background(Color.Red)  // 3. 배경
    .border(2.dp, Color.Black)  // 4. 테두리
    .clickable { }          // 5. 클릭 영역
```

---

## 커스텀 Modifier 만들기

### drawBehind를 사용한 커스텀 Modifier

```kotlin
/**
 * 점선 테두리 Modifier
 */
fun Modifier.dashedBorder(
    width: Dp = 2.dp,
    color: Color = Color.Black,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp,
    cornerRadius: Dp = 0.dp
) = this.drawBehind {
    // PathEffect로 점선 패턴 생성
    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(
            dashLength.toPx(),
            gapLength.toPx()
        ),
        phase = 0f
    )
    
    // 둥근 모서리 사각형 그리기
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = width.toPx(),
            pathEffect = pathEffect
        )
    )
}

// 사용 예제
@Composable
fun DashedBorderExample() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .dashedBorder(
                width = 2.dp,
                color = Color.Blue,
                dashLength = 8.dp,
                gapLength = 4.dp,
                cornerRadius = 8.dp
            )
    )
}
```

### drawWithContent를 사용한 Modifier

```kotlin
/**
 * 그림자 효과 Modifier
 */
fun Modifier.customShadow(
    color: Color = Color.Black.copy(alpha = 0.3f),
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    blurRadius: Dp = 8.dp
) = this.drawWithContent {
    // 먼저 그림자 그리기
    drawRect(
        color = color,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size,
        blurRadius = blurRadius.toPx()
    )
    
    // 그 다음 원본 콘텐츠 그리기
    drawContent()
}
```

---

## Modifier.composed

### composed가 필요한 이유

```kotlin
// ❌ 문제: remember가 Modifier에서 직접 사용 불가
fun Modifier.shimmer(): Modifier = this.drawWithContent {
    // remember { } // 에러! Composable 함수가 아님
}

// ✅ 해결: composed 사용
fun Modifier.shimmer(): Modifier = composed {
    // composed 안에서는 Composable 함수 사용 가능!
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
        
        // 반짝이는 효과
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

// 사용 예제
@Composable
fun ShimmerExample() {
    Box(
        modifier = Modifier
            .size(200.dp, 100.dp)
            .background(Color.Gray)
            .shimmer()  // 반짝이는 효과
    )
}
```

### 실전 예제: 펄스 애니메이션

```kotlin
/**
 * 펄스(맥박) 애니메이션 Modifier
 */
fun Modifier.pulse(
    minScale: Float = 0.9f,
    maxScale: Float = 1.1f,
    duration: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// 사용
@Composable
fun PulseExample() {
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = null,
        tint = Color.Red,
        modifier = Modifier
            .size(48.dp)
            .pulse()  // 펄스 효과
    )
}
```

---

## 제스처 처리

### pointerInput으로 드래그 구현

```kotlin
/**
 * 드래그 가능한 Modifier
 */
fun Modifier.draggable2D(): Modifier = composed {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    this
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                offsetX += dragAmount.x
                offsetY += dragAmount.y
            }
        }
}

// 사용 예제
@Composable
fun DraggableExample() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Blue)
                .draggable2D()  // 드래그 가능
        )
    }
}
```

### 스와이프 제스처

```kotlin
/**
 * 스와이프하여 삭제 Modifier
 */
fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit,
    threshold: Dp = 200.dp
): Modifier = composed {
    var offsetX by remember { mutableStateOf(0f) }
    val thresholdPx = threshold.toPx()
    
    this
        .offset { IntOffset(offsetX.roundToInt(), 0) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (offsetX < -thresholdPx) {
                        onDismiss()
                    } else {
                        offsetX = 0f
                    }
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                }
            )
        }
}

// 사용
@Composable
fun SwipeExample() {
    var items by remember { mutableStateOf(listOf("Item 1", "Item 2", "Item 3")) }
    
    LazyColumn {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .swipeToDismiss(
                        onDismiss = { items = items - item }
                    )
            ) {
                Text(item, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
```

---

## 실전 예제

### 1. 그라데이션 테두리

```kotlin
fun Modifier.gradientBorder(
    width: Dp = 2.dp,
    brush: Brush,
    shape: Shape = RectangleShape
) = this
    .border(width, brush, shape)

// 사용
@Composable
fun GradientBorderExample() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .gradientBorder(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.Red, Color.Blue, Color.Green)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    )
}
```

### 2. 네온 효과

```kotlin
fun Modifier.neonGlow(
    color: Color = Color.Cyan,
    glowRadius: Dp = 16.dp
): Modifier = this.drawBehind {
    val glowRadiusPx = glowRadius.toPx()
    
    // 여러 겹의 그림자로 네온 효과
    for (i in 1..5) {
        drawRoundRect(
            color = color.copy(alpha = 0.1f / i),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = glowRadiusPx / i)
        )
    }
}
```

### 3. 물결 효과

```kotlin
fun Modifier.ripple(
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    var ripples by remember { mutableStateOf(listOf<Offset>()) }
    
    this
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                ripples = ripples + offset
            }
        }
        .drawWithContent {
            drawContent()
            
            ripples.forEach { center ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = 50.dp.toPx(),
                    center = center
                )
            }
        }
}
```

---

## 💡 베스트 프랙티스

### 1. Modifier 재사용

```kotlin
// ✅ 공통 Modifier를 변수로 추출
val cardModifier = Modifier
    .fillMaxWidth()
    .padding(16.dp)
    .shadow(4.dp, RoundedCornerShape(8.dp))

@Composable
fun MyScreen() {
    Card(modifier = cardModifier) { }
    Card(modifier = cardModifier) { }
}
```

### 2. 조건부 Modifier

```kotlin
// ✅ then 사용
fun Modifier.conditionalModifier(condition: Boolean): Modifier {
    return if (condition) {
        this.background(Color.Red)
    } else {
        this
    }
}

// 또는
Modifier.then(
    if (condition) Modifier.background(Color.Red)
    else Modifier
)
```

---

## 🎯 완료!

고급 Compose 기법 시리즈를 모두 마스터했습니다! 🎉

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Composing! 🎨
