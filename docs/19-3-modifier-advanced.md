# Modifier 고급 기법

> 📖 **시리즈 구성**
> - **19-1**: [Custom Layout 심화 가이드](./19-1-custom-layout-guide.md)
> - **19-2**: [Canvas & Graphics 가이드](./19-2-canvas-graphics-guide.md)
> - **19-3**: Modifier 고급 기법 (현재 문서)

---

## 📚 목차

1. [Modifier 체이닝](#modifier-체이닝)
2. [Custom Modifier](#custom-modifier)
3. [Modifier.Node](#modifiernode)
4. [실전 예제](#실전-예제)

---

## Modifier 체이닝

### 순서의 중요성

```kotlin
/**
 * Modifier 순서에 따른 차이
 */
@Composable
fun ModifierOrderExample() {
    Column {
        // 예제 1: padding → background
        Box(
            Modifier
                .padding(16.dp)  // 먼저 패딩
                .background(Color.Blue)  // 그 다음 배경
                .size(100.dp)
        )
        
        // 예제 2: background → padding
        Box(
            Modifier
                .background(Color.Blue)  // 먼저 배경
                .padding(16.dp)  // 그 다음 패딩
                .size(100.dp)
        )
    }
}
```

---

## Custom Modifier

### 재사용 가능한 Modifier

```kotlin
/**
 * 커스텀 Modifier 확장 함수
 */
fun Modifier.dashedBorder(
    width: Dp = 1.dp,
    color: Color = Color.Black,
    dashWidth: Dp = 4.dp,
    gapWidth: Dp = 4.dp
) = this.then(
    drawBehind {
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashWidth.toPx(), gapWidth.toPx())
        )
        
        drawRoundRect(
            color = color,
            style = Stroke(width = width.toPx(), pathEffect = pathEffect)
        )
    }
)

/**
 * 사용 예제
 */
@Composable
fun DashedBorderExample() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .dashedBorder(
                width = 2.dp,
                color = Color.Red,
                dashWidth = 8.dp,
                gapWidth = 4.dp
            )
    )
}
```

---

## Modifier.Node

### 고성능 Modifier

```kotlin
/**
 * Modifier.Node를 사용한 고성능 Modifier
 */
class CircleNode : DrawModifierNode, Modifier.Node() {
    override fun ContentDrawScope.draw() {
        drawCircle(
            color = Color.Blue,
            radius = size.minDimension / 2
        )
        drawContent()
    }
}

fun Modifier.circle() = this.then(
    object : ModifierNodeElement<CircleNode>() {
        override fun create() = CircleNode()
        override fun update(node: CircleNode) {}
        override fun hashCode() = System.identityHashCode(this)
        override fun equals(other: Any?) = other === this
    }
)
```

---

## 실전 예제

### 그라데이션 배경

```kotlin
/**
 * 그라데이션 배경 Modifier
 */
fun Modifier.gradientBackground(
    colors: List<Color>,
    angle: Float = 0f
) = this.then(
    drawBehind {
        val angleRad = angle * PI / 180f
        val x = cos(angleRad).toFloat()
        val y = sin(angleRad).toFloat()
        
        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f
        val offset = center + Offset(x * radius, y * radius)
        
        val exactOffset = Offset(
            x = min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
        )
        
        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
            size = size
        )
    }
)
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
