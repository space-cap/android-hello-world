# Custom Layout 완벽 가이드

> 📖 **시리즈 구성**
> - **19-1**: Custom Layout 완벽 가이드 (현재 문서)
> - **19-2**: [Canvas와 그래픽 완벽 가이드](./19-2-canvas-graphics-guide.md)
> - **19-3**: [Modifier 고급 기법](./19-3-modifier-advanced.md)

---

## 📚 목차

1. [Custom Layout이란?](#custom-layout이란)
2. [Layout Composable 기초](#layout-composable-기초)
3. [측정과 배치](#측정과-배치)
4. [실전 Custom Layout](#실전-custom-layout)
5. [SubcomposeLayout](#subcomposelayout)

---

## Custom Layout이란?

### 왜 Custom Layout이 필요한가?

**기본 Layout의 한계:**
```kotlin
// Column, Row, Box로는 이런 레이아웃을 만들기 어렵습니다:
// - 지그재그 그리드
// - 플로우 레이아웃 (태그처럼 자동 줄바꿈)
// - 원형 배치
// - 복잡한 애니메이션 레이아웃
```

**Custom Layout의 장점:**
- ✅ 완전한 레이아웃 제어
- ✅ 성능 최적화 가능
- ✅ 독특한 UI 구현
- ✅ 재사용 가능한 컴포넌트

---

## Layout Composable 기초

### Layout의 3단계 프로세스

Compose의 모든 레이아웃은 3단계로 작동합니다:

```
1. Measure (측정)
   ↓
   각 자식의 크기를 측정합니다
   
2. Layout (배치)
   ↓
   각 자식을 어디에 놓을지 결정합니다
   
3. Draw (그리기)
   ↓
   실제로 화면에 그립니다
```

### 가장 간단한 Custom Layout

```kotlin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable

/**
 * 가장 기본적인 Custom Layout
 * 
 * 이 레이아웃은 자식들을 세로로 쌓습니다 (Column과 유사)
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
        // measurables: 측정할 자식들의 리스트
        // constraints: 부모가 제공하는 크기 제약
        
        // 1단계: 자식들을 측정
        val placeables: List<Placeable> = measurables.map { measurable ->
            // 각 자식을 주어진 제약 조건으로 측정
            measurable.measure(constraints)
        }
        
        // 2단계: 레이아웃 크기 계산
        // 너비: 가장 넓은 자식의 너비
        val width = placeables.maxOfOrNull { it.width } ?: 0
        
        // 높이: 모든 자식의 높이 합
        val height = placeables.sumOf { it.height }
        
        // 3단계: 레이아웃 배치
        layout(width, height) {
            var yPosition = 0  // 현재 Y 위치
            
            placeables.forEach { placeable ->
                // 각 자식을 (0, yPosition)에 배치
                placeable.placeRelative(x = 0, y = yPosition)
                
                // 다음 자식을 위해 Y 위치 증가
                yPosition += placeable.height
            }
        }
    }
}

// 사용 예제
@Composable
fun SimpleColumnExample() {
    SimpleColumn {
        Text("첫 번째 텍스트")
        Text("두 번째 텍스트")
        Text("세 번째 텍스트")
    }
}
```

**코드 설명:**

1. **measurables**: 측정해야 할 자식 컴포저블들
2. **constraints**: 부모가 제공하는 크기 제약 (최소/최대 너비/높이)
3. **measure()**: 자식을 측정하여 Placeable 객체 반환
4. **layout()**: 레이아웃의 최종 크기 설정
5. **placeRelative()**: 자식을 특정 위치에 배치

---

## 측정과 배치

### Constraints 이해하기

```kotlin
/**
 * Constraints는 레이아웃의 크기 제약을 나타냅니다
 */
@Composable
fun UnderstandingConstraints() {
    Layout(
        content = { /* 자식들 */ }
    ) { measurables, constraints ->
        
        // Constraints의 속성들
        val minWidth = constraints.minWidth      // 최소 너비
        val maxWidth = constraints.maxWidth      // 최대 너비
        val minHeight = constraints.minHeight    // 최소 높이
        val maxHeight = constraints.maxHeight    // 최대 높이
        
        // 유용한 메서드들
        val hasBoundedWidth = constraints.hasBoundedWidth    // 너비 제한 있음?
        val hasBoundedHeight = constraints.hasBoundedHeight  // 높이 제한 있음?
        
        // 자식 측정 시 제약 조건 수정 가능
        val childConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0
        )
        
        val placeables = measurables.map { it.measure(childConstraints) }
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            // 배치 로직
        }
    }
}
```

### 다양한 배치 방법

```kotlin
/**
 * 자식을 배치하는 여러 가지 방법
 */
@Composable
fun PlacementMethods(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                
                // 방법 1: placeRelative (RTL 지원)
                // RTL(오른쪽에서 왼쪽) 언어에서 자동으로 위치 조정
                placeable.placeRelative(x = index * 100, y = 0)
                
                // 방법 2: place (절대 위치)
                // RTL 언어에서도 위치 고정
                placeable.place(x = index * 100, y = 0)
                
                // 방법 3: placeWithLayer (레이어 사용)
                // 애니메이션이나 변형에 유용
                placeable.placeWithLayer(x = index * 100, y = 0)
            }
        }
    }
}
```

**placeRelative vs place:**
- `placeRelative`: RTL(아랍어, 히브리어 등) 언어 지원 ✅ 권장
- `place`: 절대 위치 (RTL 무시)

---

## 실전 Custom Layout

### 1. Flow Layout (태그 레이아웃)

```kotlin
/**
 * Flow Layout - 태그처럼 자동으로 줄바꿈되는 레이아웃
 * 
 * 사용 예: 해시태그, 필터 칩, 검색 키워드 등
 */
@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,  // 가로 간격
    verticalSpacing: Dp = 8.dp,    // 세로 간격
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        
        // 간격을 픽셀로 변환
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()
        
        // 자식들을 측정
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        
        // 각 줄의 정보를 저장
        data class RowInfo(
            val placeables: List<Placeable>,
            val width: Int,
            val height: Int
        )
        
        val rows = mutableListOf<RowInfo>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0
        
        // 각 자식을 줄에 배치
        placeables.forEach { placeable ->
            // 현재 줄에 추가했을 때의 너비
            val newRowWidth = if (currentRow.isEmpty()) {
                placeable.width
            } else {
                currentRowWidth + horizontalSpacingPx + placeable.width
            }
            
            // 너비 초과 시 새 줄 시작
            if (newRowWidth > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(RowInfo(currentRow.toList(), currentRowWidth, currentRowHeight))
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            
            // 현재 줄에 추가
            currentRow.add(placeable)
            currentRowWidth = if (currentRow.size == 1) {
                placeable.width
            } else {
                currentRowWidth + horizontalSpacingPx + placeable.width
            }
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        
        // 마지막 줄 추가
        if (currentRow.isNotEmpty()) {
            rows.add(RowInfo(currentRow, currentRowWidth, currentRowHeight))
        }
        
        // 전체 레이아웃 크기 계산
        val width = rows.maxOfOrNull { it.width } ?: 0
        val height = rows.sumOf { it.height } + (rows.size - 1) * verticalSpacingPx
        
        // 배치
        layout(width, height) {
            var yPosition = 0
            
            rows.forEach { row ->
                var xPosition = 0
                
                row.placeables.forEach { placeable ->
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    xPosition += placeable.width + horizontalSpacingPx
                }
                
                yPosition += row.height + verticalSpacingPx
            }
        }
    }
}

// 사용 예제
@Composable
fun FlowLayoutExample() {
    FlowLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp
    ) {
        listOf("Kotlin", "Jetpack Compose", "Android", "UI", "Material Design", "Clean Architecture")
            .forEach { tag ->
                AssistChip(
                    onClick = { },
                    label = { Text(tag) }
                )
            }
    }
}
```

### 2. Staggered Grid (지그재그 그리드)

```kotlin
/**
 * Staggered Grid - Pinterest 스타일 그리드
 * 
 * 각 열의 높이가 다를 수 있는 그리드 레이아웃
 */
@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,  // 열 개수
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        
        // 각 열의 너비 계산
        val columnWidth = constraints.maxWidth / columns
        
        // 자식들을 측정 (각 열의 너비로 제약)
        val itemConstraints = constraints.copy(
            minWidth = 0,
            maxWidth = columnWidth,
            minHeight = 0
        )
        
        val placeables = measurables.map { measurable ->
            measurable.measure(itemConstraints)
        }
        
        // 각 열의 현재 높이 추적
        val columnHeights = IntArray(columns) { 0 }
        
        // 전체 높이 계산
        placeables.forEachIndexed { index, placeable ->
            val column = index % columns
            columnHeights[column] += placeable.height
        }
        
        val height = columnHeights.maxOrNull() ?: 0
        
        // 배치
        layout(constraints.maxWidth, height) {
            // 각 열의 현재 Y 위치
            val columnY = IntArray(columns) { 0 }
            
            placeables.forEachIndexed { index, placeable ->
                val column = index % columns
                
                // 해당 열에 배치
                placeable.placeRelative(
                    x = column * columnWidth,
                    y = columnY[column]
                )
                
                // 다음 아이템을 위해 Y 위치 증가
                columnY[column] += placeable.height
            }
        }
    }
}

// 사용 예제
@Composable
fun StaggeredGridExample() {
    StaggeredGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = 2
    ) {
        repeat(10) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((100 + index * 20).dp)  // 각기 다른 높이
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item $index")
                }
            }
        }
    }
}
```

---

## SubcomposeLayout

### SubcomposeLayout이란?

**일반 Layout의 한계:**
```kotlin
// 문제: 자식의 크기를 알기 전에는 다른 자식을 측정할 수 없습니다
Layout { measurables, constraints ->
    val child1 = measurables[0].measure(constraints)
    
    // child1의 크기에 따라 child2의 제약을 변경하고 싶지만...
    // 이미 measurables가 고정되어 있어서 불가능!
}
```

**SubcomposeLayout의 해결:**
```kotlin
// 해결: 자식을 동적으로 측정하고 다시 구성할 수 있습니다
SubcomposeLayout { constraints ->
    // 먼저 child1 측정
    val child1 = subcompose("child1") { Child1() }
        .first()
        .measure(constraints)
    
    // child1의 크기를 기반으로 child2 측정
    val child2Constraints = constraints.copy(
        maxHeight = child1.height
    )
    val child2 = subcompose("child2") { Child2() }
        .first()
        .measure(child2Constraints)
}
```

### 실전 예제: 측정 후 크기 전달

```kotlin
/**
 * 첫 번째 자식의 크기를 측정한 후,
 * 그 크기를 두 번째 자식에게 전달하는 레이아웃
 */
@Composable
fun MeasureUnconstrainedView(
    modifier: Modifier = Modifier,
    mainContent: @Composable () -> Unit,
    dependentContent: @Composable (IntSize) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        
        // 1단계: mainContent 측정
        val mainPlaceables = subcompose("main", mainContent).map {
            it.measure(constraints)
        }
        
        // mainContent의 최대 크기 계산
        val maxSize = mainPlaceables.fold(IntSize.Zero) { currentMax, placeable ->
            IntSize(
                width = maxOf(currentMax.width, placeable.width),
                height = maxOf(currentMax.height, placeable.height)
            )
        }
        
        // 2단계: mainContent의 크기를 dependentContent에 전달
        val dependentPlaceables = subcompose("dependent") {
            dependentContent(maxSize)  // 크기 전달!
        }.map { it.measure(constraints) }
        
        // 3단계: 배치
        layout(constraints.maxWidth, constraints.maxHeight) {
            mainPlaceables.forEach { it.placeRelative(0, 0) }
            dependentPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}

// 사용 예제: 배경 크기를 내용에 맞추기
@Composable
fun AdaptiveBackground() {
    MeasureUnconstrainedView(
        mainContent = {
            // 메인 콘텐츠
            Column {
                Text("제목", style = MaterialTheme.typography.titleLarge)
                Text("내용입니다...")
            }
        },
        dependentContent = { size ->
            // 메인 콘텐츠의 크기에 맞춘 배경
            Box(
                modifier = Modifier
                    .size(
                        width = size.width.dp,
                        height = size.height.dp
                    )
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }
    )
}
```

---

## 💡 베스트 프랙티스

### 1. 성능 최적화

```kotlin
// ✅ 좋은 예: 계산 최소화
@Composable
fun OptimizedLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(modifier, content) { measurables, constraints ->
        // 한 번만 계산
        val placeables = measurables.map { it.measure(constraints) }
        
        // 필요한 값만 계산
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.sumOf { it.height }
        
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

### 2. 제약 조건 전파

```kotlin
// ✅ 자식에게 적절한 제약 조건 전달
Layout { measurables, constraints ->
    val childConstraints = constraints.copy(
        minWidth = 0,      // 최소 너비 제거
        minHeight = 0      // 최소 높이 제거
    )
    
    val placeables = measurables.map { it.measure(childConstraints) }
    // ...
}
```

### 3. RTL 지원

```kotlin
// ✅ placeRelative 사용으로 RTL 자동 지원
layout(width, height) {
    placeables.forEach { placeable ->
        placeable.placeRelative(x, y)  // RTL에서 자동으로 x 위치 조정
    }
}
```

---

## 🎯 다음 단계

Custom Layout을 마스터했습니다! 다음으로:

1. **[19-2. Canvas와 그래픽 완벽 가이드](./19-2-canvas-graphics-guide.md)** - 커스텀 그래픽 그리기

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Composing! 🎨
