# Jetpack Compose 고급 Modifier 완벽 가이드

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐⭐ 고급  
> **예상 학습 시간**: 3-4시간
> **선행 학습**: [99-modifier-fundamentals](./99-compose-modifier-fundamentals.md), [100-modifier-layout](./100-compose-modifier-layout.md)

## 목차
1. [개요](#개요)
2. [Graphics Modifier](#graphics-modifier)
3. [인터랙션 Modifier](#인터랙션-modifier)
4. [애니메이션 Modifier](#애니메이션-modifier)
5. [Drawing Modifier](#drawing-modifier)
6. [접근성 Modifier](#접근성-modifier)
7. [커스텀 Modifier 작성](#커스텀-modifier-작성)
8. [성능 최적화](#성능-최적화)

---

## 개요

고급 Modifier는 **그래픽 효과, 복잡한 인터랙션, 애니메이션, 커스텀 드로잉**을 다룹니다. 이를 통해 네이티브 수준의 UI를 구현할 수 있습니다.

---

## Graphics Modifier

### clip & shadow

```kotlin
import androidx.compose.ui.draw.*

@Composable
fun ClipAndShadow() {
    Column(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /**
         * clip - 모양으로 자르기
         * - 테두리 밖 콘텐츠 숨김
         */
        Image(
            painter = painterResource(R.drawable.sample),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape) // 원형으로 자름
        )
        
        /**
         * shadow - 그림자 효과
         * - elevation: 그림자 크기
         * - shape: 그림자 모양
         */
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color.White, RoundedCornerShape(16.dp))
        )
        
        /**
         * shadow + clip 조합
         * - 순서 주의!
         * - shadow → clip → background
         */
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(12.dp, CircleShape) // 1. 그림자
                .clip(CircleShape)          // 2. 자르기
                .background(Color.Blue)      // 3. 배경
        )
    }
}
```

### alpha & rotate & scale

```kotlin
@Composable
fun GraphicsTransform() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * alpha - 투명도
         * - 0.0f (완전 투명) ~ 1.0f (불투명)
         */
        Text(
            "투명도 50%",
            modifier = Modifier
                .alpha(0.5f)
                .background(Color.Blue)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * rotate - 회전
         * - 각도 (degree)
         */
        Text(
            "45도 회전",
            modifier = Modifier
                .rotate(45f)
                .background(Color.Green)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * scale - 크기 조절
         * - 1.0f 기준 (원본 크기)
         */
        Text(
            "150% 확대",
            modifier = Modifier
                .scale(1.5f)
                .background(Color.Red)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * scaleX, scaleY - 축별 크기 조절
         */
        Text(
            "가로만 2배",
            modifier = Modifier
                .scale(scaleX = 2f, scaleY = 1f)
                .background(Color.Magenta)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * 조합 사용
         */
        Text(
            "회전+크기+투명도",
            modifier = Modifier
                .rotate(30f)
                .scale(1.2f)
                .alpha(0.8f)
                .background(Color.Cyan)
                .padding(16.dp)
        )
    }
}
```

### graphicsLayer

```kotlin
@Composable
fun GraphicsLayerModifier() {
    /**
     * graphicsLayer - 고급 그래픽 변환
     * - 하드웨어 가속 (성능 좋음)
     * - alpha, scale, rotation 등을 한 번에
     */
    var rotation by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    var alpha by remember { mutableStateOf(1f) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * graphicsLayer 사용
         * - GPU에서 처리 (매우 빠름)
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    // 회전
                    rotationZ = rotation
                    
                    // 크기
                    scaleX = scale
                    scaleY = scale
                    
                    // 투명도
                    this.alpha = alpha
                    
                    // 3D 회전 (고급)
                    rotationX = 30f
                    rotationY = 15f
                    
                    // 카메라 거리 (3D 효과)
                    cameraDistance = 12f * density
                    
                    // 변환 원점
                    transformOrigin = TransformOrigin.Center
                }
                .background(Color.Blue)
        )
        
        /**
         * 컨트롤
         */
        Slider(
            value = rotation,
            onValueChange = { rotation = it },
            valueRange = 0f..360f
        )
        Text("회전: ${rotation.toInt()}°")
        
        Slider(
            value = scale,
            onValueChange = { scale = it },
            valueRange = 0.5f..2f
        )
        Text("크기: ${String.format("%.2f", scale)}")
        
        Slider(
            value = alpha,
            onValueChange = { alpha = it },
            valueRange = 0f..1f
        )
        Text("투명도: ${String.format("%.2f", alpha)}")
    }
}
```

---

## 인터랙션 Modifier

### clickable

```kotlin
@Composable
fun ClickableModifiers() {
    /**
     * clickable - 클릭 가능하게
     */
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 기본 clickable
         * - 리플 효과 포함
         */
        Text(
            "클릭하세요",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { println("Clicked!") }
                .background(Color.Blue)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * clickable - 리플 제거
         */
        val interactionSource = remember { MutableInteractionSource() }
        
        Text(
            "리플 없음",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null // 리플 제거
                ) { println("Clicked without ripple!") }
                .background(Color.Green)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * combinedClickable - 길게 누르기, 더블 클릭
         */
        Text(
            "길게 누르기/더블 클릭",
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { println("Click") },
                    onLongClick = { println("Long Click") },
                    onDoubleClick = { println("Double Click") }
                )
                .background(Color.Red)
                .padding(16.dp),
            color = Color.White
        )
    }
}
```

### toggleable & selectable

```kotlin
@Composable
fun ToggleableModifiers() {
    var isChecked by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * toggleable - 토글 가능 (체크박스 같은)
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = isChecked,
                    onValueChange = { isChecked = it }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isChecked, onCheckedChange = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("토글 가능한 아이템")
        }
        
        /**
         * selectable - 선택 가능 (라디오 버튼 같은)
         */
        listOf("옵션 A", "옵션 B", "옵션 C").forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedOption == index,
                        onClick = { selectedOption = index }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == index,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(option)
            }
        }
    }
}
```

### Pointer & Gestures

```kotlin
import androidx.compose.foundation.gestures.*

@Composable
fun GestureModifiers() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    
    /**
     * pointerInput - 터치 이벤트 직접 처리
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(100.dp)
                .scale(scale)
                .background(Color.Blue)
                .pointerInput(Unit) {
                    /**
                     * 제스처 감지
                     */
                    detectTapGestures(
                        onTap = { println("Tap!") },
                        onDoubleTap = { println("Double Tap!") },
                        onLongPress = { println("Long Press!") },
                        onPress = { println("Press!") }
                    )
                }
                .pointerInput(Unit) {
                    /**
                     * 드래그 감지
                     */
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .pointerInput(Unit) {
                    /**
                     * 변환 제스처 (확대/축소, 회전 등)
                     */
                    detectTransformGestures { _, pan, zoom, _ ->
                        offsetX += pan.x
                        offsetY += pan.y
                        scale *= zoom
                    }
                }
        )
    }
}

/**
 * draggable - 한 방향 드래그
 */
@Composable
fun DraggableExample() {
    var offsetX by remember { mutableStateOf(0f) }
    
    /**
     * 가로 방향으로만 드래그 가능
     */
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .size(100.dp)
            .background(Color.Red)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    offsetX += delta
                }
            )
    )
}

/**
 * swipeable - 스와이프 감지
 */
@Composable
fun SwipeableExample() {
    val swipeableState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { 100.dp.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1, sizePx * 2 to 2)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .background(Color.Green)
    ) {
        Text(
            "Swipe: ${swipeableState.currentValue}",
            color = Color.White,
            modifier = Modifier
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .padding(16.dp)
        )
    }
}
```

---

## 애니메이션 Modifier

### animateContentSize

```kotlin
@Composable
fun AnimateContentSizeExample() {
    /**
     * animateContentSize - 크기 변경 애니메이션
     * - 콘텐츠 크기가 변하면 자동 애니메이션
     */
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Blue)
            .clickable { expanded = !expanded }
            .animateContentSize( // 크기 변경 애니메이션
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .padding(16.dp)
    ) {
        Text("제목", style = MaterialTheme.typography.titleMedium, color = Color.White)
        
        if (expanded) {
            Text(
                "상세 내용이 여기에 표시됩니다. " +
                "animateContentSize 덕분에 부드럽게 확장/축소됩니다.",
                modifier = Modifier.padding(top = 8.dp),
                color = Color.White
            )
        }
    }
}
```

### Animated Modifier Values

```kotlin
@Composable
fun AnimatedModifiers() {
    var targetSize by remember { mutableStateOf(100.dp) }
    var targetColor by remember { mutableStateOf(Color.Blue) }
    
    /**
     * 애니메이션되는 값들
     */
    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "size"
    )
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "color"
    )
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 애니메이션되는 Box
         */
        Box(
            modifier = Modifier
                .size(animatedSize) // 애니메이션
                .background(animatedColor, RoundedCornerShape(16.dp))
        )
        
        /**
         * 컨트롤
         */
        Button(onClick = {
            targetSize = if (targetSize == 100.dp) 200.dp else 100.dp
            targetColor = if (targetColor == Color.Blue) Color.Red else Color.Blue
        }) {
            Text("애니메이션!")
        }
    }
}
```

---

## Drawing Modifier

### drawBehind & drawWithContent

```kotlin
@Composable
fun DrawModifiers() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * drawBehind - 콘텐츠 뒤에 그리기
         */
        Text(
            "커스텀 배경",
            modifier = Modifier
                .drawBehind {
                    // 원형 그라데이션 배경
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Blue, Color.Cyan)
                        ),
                        radius = size.minDimension / 2
                    )
                }
                .padding(32.dp),
            color = Color.White
        )
        
        /**
         * drawWithContent - 콘텐츠 앞뒤로 그리기
         */
        Text(
            "테두리 효과",
            modifier = Modifier
                .drawWithContent {
                    // 1. 먼저 배경 그리기
                    drawRect(Color.Yellow)
                    
                    // 2. 콘텐츠 그리기
                    drawContent()
                    
                    // 3. 콘텐츠 위에 그리기
                    drawCircle(
                        color = Color.Red.copy(alpha = 0.3f),
                        radius = 20f,
                        center = Offset(size.width - 20f, 20f)
                    )
                }
                .padding(32.dp)
        )
        
        /**
         * drawBehind - 커스텀 모양
         */
        Box(
            modifier = Modifier
                .size(150.dp)
                .drawBehind {
                    val path = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    
                    drawPath(
                        path = path,
                        color = Color.Green
                    )
                }
        ) {
            Text(
                "삼각형 배경",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }
}
```

### 커스텀 Border

```kotlin
@Composable
fun CustomBorderEffect() {
    /**
     * 그라데이션 테두리
     */
    Box(
        modifier = Modifier
            .size(150.dp)
            .drawBehind {
                // 그라데이션 테두리 그리기
                val borderWidth = 4.dp.toPx()
                val gradient = Brush.linearGradient(
                    colors = listOf(Color.Red, Color.Blue, Color.Green)
                )
                
                drawRoundRect(
                    brush = gradient,
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
                
                // 내부 흰색 배경
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(borderWidth, borderWidth),
                    size = Size(
                        size.width - borderWidth * 2,
                        size.height - borderWidth * 2
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
    ) {
        Text(
            "그라데이션 테두리",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
```

---

## 접근성 Modifier

### semantics

```kotlin
import androidx.compose.ui.semantics.*

@Composable
fun AccessibilityModifiers() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * contentDescription - 스크린 리더용 설명
         */
        Image(
            painter = painterResource(R.drawable.sample),
            contentDescription = "프로필 사진", // 접근성 설명
            modifier = Modifier.size(100.dp)
        )
        
        /**
         * semantics - 추가 접근성 정보
         */
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.Blue)
                .clickable { /* 좋아요 */ }
                .semantics {
                    // 역할 지정
                    role = Role.Button
                    
                    // 설명
                    contentDescription = "좋아요 버튼"
                    
                    // 상태
                    stateDescription = "활성화됨"
                }
        )
        
        /**
         * semantics - 병합
         * - 여러 요소를 하나로 취급
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .semantics(mergeDescendants = true) {
                    // 자식들의 접근성 정보 병합
                }
        ) {
            Image(
                painter = painterResource(R.drawable.sample),
                contentDescription = null, // 병합되므로 null
                modifier = Modifier.size(50.dp)
            )
            
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("제목")
                Text("부제목")
            }
        }
        
        /**
         * clearAndSetSemantics - 접근성 재정의
         */
        Row(
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "사용자: John Doe, 이메일: john@example.com"
            }
        ) {
            Text("John Doe")
            Spacer(modifier = Modifier.width(8.dp))
            Text("john@example.com")
        }
    }
}
```

---

## 커스텀 Modifier 작성

### 확장 함수로 커스텀 Modifier

```kotlin
/**
 * 간단한 커스텀 Modifier
 */
fun Modifier.badge(
    text: String,
    backgroundColor: Color = Color.Red,
    textColor: Color = Color.White
): Modifier = this.drawWithContent {
    // 원본 콘텐츠 그리기
    drawContent()
    
    // 배지 그리기
    val badgeSize = 24.dp.toPx()
    val offset = Offset(size.width - badgeSize / 2, badgeSize / 2)
    
    drawCircle(
        color = backgroundColor,
        radius = badgeSize / 2,
        center = offset
    )
    
    // 텍스트는 DrawScope에서 직접 그리기 어려움
    // 실제로는 Canvas 사용 필요
}

/**
 * 조건부 Modifier
 */
fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier = if (condition) {
    then(modifier(Modifier))
} else {
    this
}

/**
 * 사용 예제
 */
@Composable
fun CustomModifierUsage() {
    var showBorder by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Blue)
            .conditional(showBorder) {
                border(4.dp, Color.Red)
            }
    )
}
```

### composed Modifier

```kotlin
/**
 * composed - 상태를 가진 Modifier
 * - remember 등 Composable 함수 사용 가능
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    /**
     * 애니메이션 상태
     */
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )
    
    /**
     * 쉬머 효과 그리기
     */
    this.drawWithContent {
        drawContent()
        
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                start = Offset(offset, 0f),
                end = Offset(offset + 200f, size.height)
            )
        )
    }
}

/**
 * 사용 예제
 */
@Composable
fun ShimmerExample() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.LightGray)
            .shimmerEffect() // 쉬머 효과
    )
}
```

---

## 성능 최적화

### Modifier 재사용

```kotlin
/**
 * ❌ 나쁜 예: 매번 새로 생성
 */
@Composable
fun BadModifierUsage() {
    LazyColumn {
        items(1000) { index ->
            Text(
                "Item $index",
                modifier = Modifier // 매번 새로 생성!
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Blue)
            )
        }
    }
}

/**
 * ✅ 좋은 예: 재사용
 */
@Composable
fun GoodModifierUsage() {
    /**
     * 공통 Modifier를 한 번만 생성
     */
    val itemModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .background(Color.Blue)
    
    LazyColumn {
        items(1000) { index ->
            Text(
                "Item $index",
                modifier = itemModifier // 재사용!
            )
        }
    }
}
```

### 불필요한 리컴포지션 방지

```kotlin
/**
 * remember로 Modifier 캐싱
 */
@Composable
fun RememberModifier(index: Int) {
    /**
     * index가 변경될 때만 Modifier 재생성
     */
    val modifier = remember(index) {
        Modifier
            .fillMaxWidth()
            .background(if (index % 2 == 0) Color.Gray else Color.White)
            .padding(16.dp)
    }
    
    Text(
        "Item $index",
        modifier = modifier
    )
}
```

---

## 요약

### 고급 Modifier 체크리스트

```kotlin
/**
 * Graphics
 */
.clip(shape)              // 모양 자르기
.shadow(elevation)        // 그림자
.alpha(0.5f)             // 투명도
.rotate(45f)             // 회전
.scale(1.5f)             // 크기
.graphicsLayer { }       // 고급 변환

/**
 * 인터랙션
 */
.clickable { }           // 클릭
.combinedClickable { }   // 다중 클릭
.draggable()             // 드래그
.swipeable()             // 스와이프
.pointerInput { }        // 터치

/**
 * 애니메이션
 */
.animateContentSize()    // 크기 애니메이션

/**
 * 그리기
 */
.drawBehind { }          // 뒤에 그리기
.drawWithContent { }     // 앞뒤 그리기

/**
 * 접근성
 */
.semantics { }           // 접근성 정보

/**
 * 커스텀
 */
.composed { }            // 상태 있는 Modifier
```

Modifier를 마스터하면 Jetpack Compose의 진정한 힘을 발휘할 수 있습니다! 🚀
