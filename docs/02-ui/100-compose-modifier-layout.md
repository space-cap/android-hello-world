# Jetpack Compose Layout Modifier 완벽 가이드

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐ 중급  
> **예상 학습 시간**: 2-3시간
> **선행 학습**: [99-compose-modifier-fundamentals.md](./99-compose-modifier-fundamentals.md)

## 목차
1. [개요](#개요)
2. [크기 조절 Modifier](#크기-조절-modifier)
3. [정렬 Modifier](#정렬-modifier)
4. [가중치와 비율](#가중치와-비율)
5. [오프셋과 위치](#오프셋과-위치)
6. [스크롤 Modifier](#스크롤-modifier)
7. [레이아웃 제약](#레이아웃-제약)
8. [실무 레이아웃 패턴](#실무-레이아웃-패턴)

---

## 개요

Layout Modifier는 **Composable의 크기, 위치, 정렬을 제어**하는 Modifier입니다. 반응형 UI를 만들고 다양한 화면 크기에 대응하는 핵심 도구입니다.

---

## 크기 조절 Modifier

### fillMax 시리즈

```kotlin
@Composable
fun FillMaxModifiers() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /**
         * fillMaxWidth - 가로를 부모 크기만큼
         * fraction으로 비율 조절 (0.0f ~ 1.0f)
         */
        Box(
            modifier = Modifier
                .fillMaxWidth() // 100% (기본값)
                .height(50.dp)
                .background(Color.Red)
        ) {
            Text("100% 너비", color = Color.White)
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f) // 80%
                .height(50.dp)
                .background(Color.Green)
        ) {
            Text("80% 너비", color = Color.White)
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f) // 50%
                .height(50.dp)
                .background(Color.Blue)
        ) {
            Text("50% 너비", color = Color.White)
        }
        
        /**
         * fillMaxHeight - 세로를 부모 크기만큼
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(0.6f) // 60% 높이
                    .background(Color.Magenta)
            )
            
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(1f) // 100% 높이
                    .background(Color.Cyan)
            )
        }
        
        /**
         * fillMaxSize - 가로세로 모두
         * - fillMaxWidth() + fillMaxHeight()와 동일
         */
        Box(
            modifier = Modifier
                .fillMaxSize(0.3f) // 30%
                .background(Color.Yellow)
        ) {
            Text("30% 크기")
        }
    }
}
```

### wrapContentSize

```kotlin
@Composable
fun WrapContentModifiers() {
    /**
     * wrapContentSize - 콘텐츠 크기에 맞춤
     * - 콘텐츠를 감싸는 최소 크기
     */
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * wrapContentWidth - 콘텐츠 너비에 맞춤
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {
            Text(
                "짧은 텍스트",
                modifier = Modifier
                    .wrapContentWidth() // 텍스트 너비만큼
                    .background(Color.Blue)
                    .padding(16.dp),
                color = Color.White
            )
        }
        
        /**
         * wrapContentHeight - 콘텐츠 높이에 맞춤
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
        ) {
            Text(
                "한 줄",
                modifier = Modifier
                    .wrapContentHeight() // 한 줄 높이만큼
                    .background(Color.Green)
                    .padding(16.dp),
                color = Color.White
            )
        }
    }
}
```

### requiredSize vs size

```kotlin
@Composable
fun RequiredSizeComparison() {
    /**
     * size vs requiredSize 차이
     * - size: 부모의 제약을 따름
     * - requiredSize: 부모 제약 무시
     */
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * size - 부모 제약 준수
         */
        Box(
            modifier = Modifier
                .width(100.dp) // 부모가 100dp로 제한
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp) // 150dp 원하지만
                    .background(Color.Red)
            ) {
                Text("100dp로 제한됨")
            }
        }
        
        /**
         * requiredSize - 부모 제약 무시
         */
        Box(
            modifier = Modifier
                .width(100.dp) // 부모가 100dp로 제한
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(150.dp) // 강제로 150dp
                    .background(Color.Blue)
            ) {
                Text("150dp 강제 적용", color = Color.White)
            }
        }
    }
}
```

---

## 정렬 Modifier

### align (Box 전용)

```kotlin
@Composable
fun AlignInBox() {
    /**
     * Box 안에서 align 사용
     * - 자식을 특정 위치에 배치
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        /**
         * TopStart - 왼쪽 위
         */
        Text(
            "TopStart",
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Red)
                .padding(8.dp),
            color = Color.White
        )
        
        /**
         * TopCenter - 상단 중앙
         */
        Text(
            "TopCenter",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Green)
                .padding(8.dp),
            color = Color.White
        )
        
        /**
         * TopEnd - 오른쪽 위
         */
        Text(
            "TopEnd",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.Blue)
                .padding(8.dp),
            color = Color.White
        )
        
        /**
         * Center - 정중앙
         */
        Text(
            "Center",
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Magenta)
                .padding(8.dp),
            color = Color.White
        )
        
        /**
         * BottomStart - 왼쪽 아래
         */
        Text(
            "BottomStart",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.Cyan)
                .padding(8.dp)
        )
        
        /**
         * BottomCenter - 하단 중앙
         */
        Text(
            "BottomCenter",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Yellow)
                .padding(8.dp)
        )
        
        /**
         * BottomEnd - 오른쪽 아래
         */
        Text(
            "BottomEnd",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Gray)
                .padding(8.dp),
            color = Color.White
        )
    }
}
```

### align (Row/Column)

```kotlin
@Composable
fun AlignInRowColumn() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        /**
         * Row에서 align (수직 정렬)
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
        ) {
            Text(
                "Top",
                modifier = Modifier
                    .align(Alignment.Top) // 위쪽
                    .background(Color.Red)
                    .padding(8.dp),
                color = Color.White
            )
            
            Text(
                "CenterVertically",
                modifier = Modifier
                    .align(Alignment.CenterVertically) // 중앙
                    .background(Color.Green)
                    .padding(8.dp),
                color = Color.White
            )
            
            Text(
                "Bottom",
                modifier = Modifier
                    .align(Alignment.Bottom) // 아래쪽
                    .background(Color.Blue)
                    .padding(8.dp),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * Column에서 align (수평 정렬)
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
        ) {
            Text(
                "Start",
                modifier = Modifier
                    .align(Alignment.Start) // 왼쪽
                    .background(Color.Magenta)
                    .padding(8.dp),
                color = Color.White
            )
            
            Text(
                "CenterHorizontally",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // 중앙
                    .background(Color.Cyan)
                    .padding(8.dp)
            )
            
            Text(
                "End",
                modifier = Modifier
                    .align(Alignment.End) // 오른쪽
                    .background(Color.Yellow)
                    .padding(8.dp)
            )
        }
    }
}
```

---

## 가중치와 비율

### weight (Row/Column)

```kotlin
@Composable
fun WeightModifier() {
    /**
     * weight - 가용 공간을 비율로 분배
     * - Row/Column 전용
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * Row에서 weight 사용
         * - 가로 공간을 비율로 나눔
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f) // 1/4
                    .fillMaxHeight()
                    .background(Color.Red)
            ) {
                Text("1f", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            
            Box(
                modifier = Modifier
                    .weight(2f) // 2/4
                    .fillMaxHeight()
                    .background(Color.Green)
            ) {
                Text("2f", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            
            Box(
                modifier = Modifier
                    .weight(1f) // 1/4
                    .fillMaxHeight()
                    .background(Color.Blue)
            ) {
                Text("1f", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }
        
        /**
         * Column에서 weight 사용
         * - 세로 공간을 비율로 나눔
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 나머지 공간 모두 차지
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .background(Color.Magenta)
            ) {
                Text("3f", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Cyan)
            ) {
                Text("1f", modifier = Modifier.align(Alignment.Center))
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .background(Color.Yellow)
            ) {
                Text("2f", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

/**
 * weight(fill = false)
 * - 최소 크기만 차지
 */
@Composable
fun WeightFillFalse() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(16.dp)
    ) {
        /**
         * fill = true (기본값)
         * - 할당된 공간을 모두 채움
         */
        Box(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxHeight()
                .background(Color.Red)
        ) {
            Text("fill=true", color = Color.White)
        }
        
        /**
         * fill = false
         * - 콘텐츠 크기만큼만 차지
         * - 나머지는 빈 공간
         */
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .background(Color.Blue)
        ) {
            Text("fill=false", color = Color.White)
        }
    }
}
```

### aspectRatio

```kotlin
@Composable
fun AspectRatioModifier() {
    /**
     * aspectRatio - 가로세로 비율 유지
     * - 한 쪽 크기가 정해지면 다른 쪽 자동 계산
     */
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 16:9 비율 (동영상)
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f) // 16:9
                .background(Color.Blue)
        ) {
            Text(
                "16:9 비율",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        /**
         * 1:1 비율 (정사각형)
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // 1:1
                .background(Color.Green)
        ) {
            Text(
                "1:1 비율 (정사각형)",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        /**
         * 4:3 비율 (구형 TV)
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f) // 4:3
                .background(Color.Red)
        ) {
            Text(
                "4:3 비율",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        /**
         * matchHeightConstraintsFirst
         * - true: 높이 기준으로 너비 계산
         * - false: 너비 기준으로 높이 계산 (기본값)
         */
        Box(
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(2f, matchHeightConstraintsFirst = true)
                .background(Color.Magenta)
        ) {
            Text(
                "높이 기준",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

---

## 오프셋과 위치

### offset

```kotlin
@Composable
fun OffsetModifier() {
    /**
     * offset - 위치 이동
     * - 레이아웃에는 영향 없음
     * - 시각적으로만 이동
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        /**
         * 기준 위치
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Red.copy(alpha = 0.3f))
        ) {
            Text("원본 위치", color = Color.White)
        }
        
        /**
         * offset으로 이동
         * - 오른쪽 50dp, 아래 50dp
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 50.dp, y = 50.dp)
                .background(Color.Blue)
        ) {
            Text("offset 50dp", color = Color.White)
        }
        
        /**
         * 음수 offset
         * - 왼쪽, 위로 이동
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .offset(x = (-30).dp, y = (-30).dp)
                .background(Color.Green)
        ) {
            Text("offset -30dp", color = Color.White)
        }
    }
}

/**
 * absoluteOffset - RTL 무시
 * offset - RTL 고려
 */
@Composable
fun OffsetRTL() {
    /**
     * offset vs absoluteOffset
     * - offset: RTL 환경에서 x값 반전
     * - absoluteOffset: RTL 영향 없음
     */
    Column {
        // offset (RTL 고려)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 50.dp)
                .background(Color.Red)
        ) {
            Text("offset", color = Color.White)
        }
        
        // absoluteOffset (RTL 무시)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .absoluteOffset(x = 50.dp)
                .background(Color.Blue)
        ) {
            Text("absoluteOffset", color = Color.White)
        }
    }
}
```

---

## 스크롤 Modifier

### verticalScroll / horizontalScroll

```kotlin
@Composable
fun ScrollModifiers() {
    /**
     * 스크롤 상태 기억
     */
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        /**
         * 세로 스크롤
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .verticalScroll(verticalScrollState)
                .background(Color.LightGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(20) { index ->
                Text(
                    "아이템 $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * 가로 스크롤
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .horizontalScroll(horizontalScrollState)
                .background(Color.LightGray)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(20) { index ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Blue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$index", color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * 스크롤 위치 정보
         */
        Text("세로 스크롤: ${verticalScrollState.value}px")
        Text("가로 스크롤: ${horizontalScrollState.value}px")
        
        /**
         * 프로그래밍 방식 스크롤
         */
        Button(onClick = {
            // Coroutine에서 실행
            coroutineScope.launch {
                verticalScrollState.animateScrollTo(0) // 맨 위로
            }
        }) {
            Text("맨 위로")
        }
    }
}
```

---

## 레이아웃 제약

### defaultMinSize

```kotlin
@Composable
fun DefaultMinSizeModifier() {
    /**
     * defaultMinSize - 최소 크기 설정
     * - 콘텐츠가 작아도 최소 크기 보장
     */
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 최소 크기 없음
         */
        Button(onClick = { }) {
            Text("A") // 작은 텍스트
        }
        
        /**
         * 최소 크기 지정
         * - 터치 타겟 크기 보장 (접근성)
         */
        Button(
            onClick = { },
            modifier = Modifier.defaultMinSize(
                minWidth = 100.dp,
                minHeight = 48.dp
            )
        ) {
            Text("A")
        }
    }
}
```

### widthIn / heightIn

```kotlin
@Composable
fun SizeConstraints() {
    /**
     * widthIn / heightIn - 크기 범위 제한
     * - 최소/최대 크기 설정
     */
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 너비 범위: 100dp ~ 300dp
         */
        Box(
            modifier = Modifier
                .widthIn(min = 100.dp, max = 300.dp)
                .height(50.dp)
                .background(Color.Blue)
        ) {
            Text("너비 제한", color = Color.White)
        }
        
        /**
         * 높이 범위: 50dp ~ 200dp
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp, max = 200.dp)
                .background(Color.Green)
        ) {
            Text("높이 제한", color = Color.White)
        }
        
        /**
         * 최대 너비만 제한
         */
        Box(
            modifier = Modifier
                .widthIn(max = 200.dp)
                .height(50.dp)
                .background(Color.Red)
        ) {
            Text("최대 너비 200dp", color = Color.White)
        }
    }
}
```

---

## 실무 레이아웃 패턴

### 반응형 그리드

```kotlin
@Composable
fun ResponsiveGrid(items: List<Item>) {
    /**
     * 화면 너비에 따라 열 개수 조절
     */
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    /**
     * 열 개수 계산
     * - 작은 화면: 2열
     * - 중간 화면: 3열
     * - 큰 화면: 4열
     */
    val columns = when {
        screenWidth < 600.dp -> 2
        screenWidth < 840.dp -> 3
        else -> 4
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ItemCard(
                item = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // 정사각형
            )
        }
    }
}
```

### 스티키 헤더 레이아웃

```kotlin
@Composable
fun StickyHeaderLayout() {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        /**
         * 스크롤 가능한 콘텐츠
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 상단 여백 (헤더 높이만큼)
            Spacer(modifier = Modifier.height(56.dp))
            
            // 콘텐츠
            repeat(50) { index ->
                Text(
                    "아이템 $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }
        
        /**
         * 고정 헤더
         * - 스크롤과 무관하게 상단 고정
         */
        TopAppBar(
            title = { Text("스티키 헤더") },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

### 마스터-디테일 레이아웃

```kotlin
@Composable
fun MasterDetailLayout() {
    /**
     * 화면 크기에 따라 레이아웃 변경
     * - 작은 화면: 리스트 또는 상세
     * - 큰 화면: 리스트 + 상세 나란히
     */
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp >= 600
    
    if (isLargeScreen) {
        /**
         * 큰 화면: 리스트와 상세를 나란히
         */
        Row(modifier = Modifier.fillMaxSize()) {
            // 리스트 (1/3)
            MasterList(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            
            // 상세 (2/3)
            DetailScreen(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            )
        }
    } else {
        /**
         * 작은 화면: 리스트 또는 상세 (전환)
         */
        if (selectedItem == null) {
            MasterList(modifier = Modifier.fillMaxSize())
        } else {
            DetailScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
```

---

## 요약

### Layout Modifier 핵심 패턴

```kotlin
/**
 * 크기 관련
 */
.fillMaxWidth()         // 가로 채우기
.fillMaxHeight()        // 세로 채우기
.fillMaxSize()          // 전체 채우기
.size(100.dp)           // 고정 크기
.weight(1f)             // 비율 분배
.aspectRatio(16f/9f)    // 비율 유지

/**
 * 정렬 관련
 */
.align(Alignment.Center) // 정렬
.offset(x, y)           // 위치 이동

/**
 * 스크롤 관련
 */
.verticalScroll()       // 세로 스크롤
.horizontalScroll()     // 가로 스크롤

/**
 * 제약 관련
 */
.defaultMinSize()       // 최소 크기
.widthIn(min, max)      // 너비 범위
.heightIn(min, max)     // 높이 범위
```

다음 문서에서는 **고급 Modifier**(그래픽, 애니메이션, 인터랙션)를 다룹니다!
