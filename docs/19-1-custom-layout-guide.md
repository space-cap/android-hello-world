# Custom Layout 심화 가이드

> 📖 **시리즈 구성**
> - **19-1**: Custom Layout 심화 가이드 (현재 문서)
> - **19-2**: [Canvas & Graphics 가이드](./19-2-canvas-graphics-guide.md)
> - **19-3**: [Modifier 고급](./19-3-modifier-advanced.md)

---

## 📚 목차

1. [Custom Layout 기초](#custom-layout-기초)
2. [Layout Modifier](#layout-modifier)
3. [SubcomposeLayout](#subcomposelayout)
4. [실전 예제](#실전-예제)

---

## Custom Layout 기초

### Layout 함수 이해

```kotlin
/**
 * Custom Layout 기본 구조
 */
@Composable
fun CustomLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // 1. 자식 측정
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        
        // 2. 레이아웃 크기 결정
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.sumOf { it.height }
        
        // 3. 자식 배치
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

### 간단한 Column 구현

```kotlin
/**
 * 간단한 Column 구현
 */
@Composable
fun SimpleColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // 자식들을 측정
        val placeables = measurables.map { it.measure(constraints) }
        
        // 너비는 가장 넓은 자식, 높이는 모든 자식의 합
        val width = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = placeables.sumOf { it.height }
        
        // 배치
        layout(width, height) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height
            }
        }
    }
}
```

---

## Layout Modifier

### Custom Modifier 생성

```kotlin
/**
 * 원형 크기 Modifier
 */
fun Modifier.circle(size: Dp): Modifier = this.then(
    layout { measurable, constraints ->
        val sizePx = size.roundToPx()
        val placeable = measurable.measure(
            Constraints.fixed(sizePx, sizePx)
        )
        
        layout(sizePx, sizePx) {
            placeable.placeRelative(0, 0)
        }
    }
)

/**
 * 사용 예제
 */
@Composable
fun CircleExample() {
    Box(
        modifier = Modifier
            .circle(100.dp)
            .background(Color.Blue)
    )
}
```

---

## SubcomposeLayout

### 동적 측정

```kotlin
/**
 * SubcomposeLayout 예제
 */
@Composable
fun MeasureUnconstrainedViewWidth(
    viewToMeasure: @Composable () -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredWidth = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).width
        
        val width = measuredWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        
        layout(width, constraints.maxHeight) {
            // 배치 로직
        }
    }
}
```

---

## 실전 예제

### 그리드 레이아웃

```kotlin
/**
 * 커스텀 그리드 레이아웃
 */
@Composable
fun CustomGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val columnWidth = constraints.maxWidth / columns
        
        val placeables = measurables.map { measurable ->
            measurable.measure(Constraints.fixedWidth(columnWidth))
        }
        
        val rows = (placeables.size + columns - 1) / columns
        val height = placeables.chunked(columns).sumOf { row ->
            row.maxOfOrNull { it.height } ?: 0
        }
        
        layout(constraints.maxWidth, height) {
            var y = 0
            placeables.chunked(columns).forEach { row ->
                var x = 0
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += columnWidth
                }
                
                y += rowHeight
            }
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
