# Jetpack Compose Modifier 기초 완벽 가이드

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐ 초중급  
> **예상 학습 시간**: 2-3시간

## 목차
1. [개요](#개요)
2. [Modifier란 무엇인가](#modifier란-무엇인가)
3. [Modifier 체이닝](#modifier-체이닝)
4. [순서의 중요성](#순서의-중요성)
5. [기본 Modifier](#기본-modifier)
6. [Modifier 재사용](#modifier-재사용)
7. [실전 예제](#실전-예제)

---

## 개요

Modifier는 Jetpack Compose에서 **UI 컴포넌트를 꾸미고 동작을 정의하는 핵심 도구**입니다. 크기, 색상, 패딩, 클릭 이벤트 등 거의 모든 것을 Modifier로 제어할 수 있습니다.

### 왜 Modifier가 중요한가?

```kotlin
/**
 * Modifier 없이 (불가능!)
 */
@Composable
fun WithoutModifier() {
    Text("Hello") // 꾸밀 방법이 없음!
}

/**
 * Modifier로 다양하게 꾸미기
 */
@Composable
fun WithModifier() {
    Text(
        "Hello",
        modifier = Modifier
            .size(200.dp)                    // 크기
            .background(Color.Blue)           // 배경색
            .padding(16.dp)                   // 패딩
            .clickable { /* 클릭 */ }        // 클릭 가능
    )
}
```

---

## Modifier란 무엇인가

### 기본 개념

```kotlin
/**
 * Modifier는 인터페이스
 * - Composable을 장식하고 설정하는 역할
 * - 체인처럼 연결 가능
 */
interface Modifier {
    fun <R> foldIn(initial: R, operation: (R, Element) -> R): R
    fun <R> foldOut(initial: R, operation: (Element, R) -> R): R
    fun any(predicate: (Element) -> Boolean): Boolean
    fun all(predicate: (Element) -> Boolean): Boolean
    
    // Element는 개별 Modifier 하나
    interface Element : Modifier
    
    // Companion object는 빈 Modifier
    companion object : Modifier
}

/**
 * 모든 Composable은 modifier 파라미터를 가짐
 */
@Composable
fun MyComposable(
    modifier: Modifier = Modifier // 기본값은 빈 Modifier
) {
    Box(modifier = modifier) {
        // 내용
    }
}
```

### Modifier의 역할

```kotlin
/**
 * Modifier가 할 수 있는 일
 */
@Composable
fun ModifierCapabilities() {
    Box(
        modifier = Modifier
            // 1. 크기와 레이아웃
            .size(100.dp)
            .fillMaxWidth()
            
            // 2. 외형과 스타일
            .background(Color.Blue)
            .border(2.dp, Color.Black)
            .clip(RoundedCornerShape(8.dp))
            
            // 3. 간격과 정렬
            .padding(16.dp)
            .align(Alignment.Center) // 부모가 Box일 때
            
            // 4. 인터랙션
            .clickable { /* 클릭 */ }
            .draggable(/* ... */)
            
            // 5. 변환과 애니메이션
            .rotate(45f)
            .scale(1.5f)
            .alpha(0.8f)
            
            // 6. 그래픽과 드로잉
            .shadow(8.dp)
            .drawBehind { /* 커스텀 그리기 */ }
    )
}
```

---

## Modifier 체이닝

### 체이닝의 기본

```kotlin
/**
 * Modifier는 체인으로 연결
 * - 각 Modifier는 순서대로 적용됨
 * - 마치 레이어를 쌓는 것처럼
 */
@Composable
fun ModifierChaining() {
    /**
     * 1단계: 크기 설정
     * 2단계: 패딩 추가
     * 3단계: 배경색 적용
     */
    Text(
        "체이닝 예제",
        modifier = Modifier
            .size(200.dp)         // 1. 200x200 박스
            .padding(16.dp)       // 2. 안쪽 여백 16dp
            .background(Color.Blue) // 3. 파란색 배경
    )
}

/**
 * 내부적으로 어떻게 동작하는가
 */
@Composable
fun HowChainingWorks() {
    /**
     * Modifier.size(200.dp)
     *   .padding(16.dp)
     *   .background(Color.Blue)
     * 
     * 는 다음과 같이 연결됨:
     * Modifier
     *   .then(SizeModifier(200.dp))
     *   .then(PaddingModifier(16.dp))
     *   .then(BackgroundModifier(Color.Blue))
     */
}
```

### 시각적 이해

**Modifier가 레이어처럼 쌓이는 방식:**

![Modifier 체이닝 레이어](./images/modifier_chaining_layers.png)

위 이미지는 Modifier가 어떻게 순서대로 레이어처럼 적용되는지 보여줍니다.

```kotlin
/**
 * 레이어별 설명
 */
@Composable
fun LayerByLayer() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 각 단계를 시각화
         */
        
        // 1단계: 크기만
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.LightGray)
        ) {
            Text("1. 크기만 (200x200)")
        }
        
        // 2단계: 크기 + 패딩
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(20.dp)
                .background(Color.Blue)
        ) {
            Text("2. 패딩 추가", color = Color.White)
        }
        
        // 3단계: 크기 + 패딩 + 테두리
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(20.dp)
                .border(4.dp, Color.Red)
                .padding(16.dp)
                .background(Color.Blue)
        ) {
            Text("3. 테두리 추가", color = Color.White)
        }
    }
}
```

---

## 순서의 중요성

### 핵심 원칙: 순서가 결과를 결정한다!

```kotlin
/**
 * ⚠️ 가장 중요한 개념!
 * Modifier 순서에 따라 완전히 다른 결과
 */
@Composable
fun OrderMatters() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * 예제 1: padding → background
         * 결과: 배경이 패딩 안쪽에만 적용
         */
        Text(
            "padding → background",
            modifier = Modifier
                .padding(32.dp)        // 1. 먼저 여백
                .background(Color.Blue) // 2. 그 다음 배경
        )
        
        /**
         * 예제 2: background → padding
         * 결과: 배경이 패딩 포함해서 적용
         */
        Text(
            "background → padding",
            modifier = Modifier
                .background(Color.Blue) // 1. 먼저 배경
                .padding(32.dp)        // 2. 그 다음 여백
        )
    }
}
```

**순서에 따른 시각적 차이:**

![Modifier 순서 비교](./images/modifier_order_comparison.png)

왼쪽은 padding → background, 오른쪽은 background → padding의 결과를 보여줍니다. 순서만 바뀌었는데 완전히 다른 결과가 나옵니다!

### 순서 패턴 비교

```kotlin
@Composable
fun OrderPatternComparison() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        /**
         * 패턴 A: clip → background → padding
         * - 동그란 모서리
         * - 배경색이 패딩 제외하고 적용
         */
        Text(
            "패턴 A",
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))  // 1. 모서리 둥글게
                .background(Color.Red)             // 2. 배경
                .padding(16.dp),                   // 3. 내부 여백
            color = Color.White
        )
        
        /**
         * 패턴 B: background → clip → padding
         * - 배경의 모서리만 둥글게
         */
        Text(
            "패턴 B",
            modifier = Modifier
                .background(Color.Red)             // 1. 배경 먼저
                .clip(RoundedCornerShape(16.dp))  // 2. 자르기
                .padding(16.dp),                   // 3. 내부 여백
            color = Color.White
        )
        
        /**
         * 패턴 C: padding → clip → background
         * - 패딩 영역 제외하고 둥근 배경
         */
        Text(
            "패턴 C",
            modifier = Modifier
                .padding(16.dp)                    // 1. 외부 여백
                .clip(RoundedCornerShape(16.dp))  // 2. 자르기
                .background(Color.Red)             // 3. 배경
                .padding(16.dp),                   // 4. 내부 여백
            color = Color.White
        )
    }
}
```

### clickable 순서의 중요성

```kotlin
@Composable
fun ClickableOrder() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * ❌ 잘못된 순서
         * - 클릭 영역이 패딩을 포함하지 않음
         */
        Text(
            "클릭 영역 작음",
            modifier = Modifier
                .clickable { println("Clicked!") }  // 1. 클릭 가능
                .padding(32.dp)                     // 2. 패딩 (클릭 영역 제외)
                .background(Color.Blue),
            color = Color.White
        )
        
        /**
         * ✅ 올바른 순서
         * - 클릭 영역이 패딩을 포함
         */
        Text(
            "클릭 영역 큼",
            modifier = Modifier
                .padding(32.dp)                     // 1. 패딩 먼저
                .clickable { println("Clicked!") }  // 2. 클릭 가능
                .background(Color.Blue),
            color = Color.White
        )
        
        /**
         * 💡 실무 패턴
         * - background → padding → clickable
         * - 배경 포함한 전체 영역을 클릭 가능하게
         */
        Text(
            "실무 패턴",
            modifier = Modifier
                .background(Color.Blue)             // 1. 배경
                .padding(16.dp)                     // 2. 내부 여백
                .clickable { println("Clicked!") }, // 3. 클릭 (패딩 포함)
            color = Color.White
        )
    }
}
```

---

## 기본 Modifier

### 크기 관련 Modifier

```kotlin
@Composable
fun SizeModifiers() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /**
         * size(dp) - 고정 크기
         * - width와 height를 동시에 설정
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Red)
        )
        
        /**
         * size(width, height) - 별도 지정
         */
        Box(
            modifier = Modifier
                .size(width = 150.dp, height = 80.dp)
                .background(Color.Green)
        )
        
        /**
         * width / height - 개별 설정
         */
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
                .background(Color.Blue)
        )
        
        /**
         * fillMaxWidth / fillMaxHeight - 부모 크기 채우기
         * - fraction으로 비율 조절 가능 (0.0f ~ 1.0f)
         */
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f) // 부모의 70%
                .height(50.dp)
                .background(Color.Magenta)
        )
        
        /**
         * fillMaxSize - 전체 크기
         */
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f) // 부모의 50%
                .background(Color.Cyan)
        )
    }
}
```

### 간격 관련 Modifier

```kotlin
@Composable
fun SpacingModifiers() {
    /**
     * padding - 내부 여백
     * - 콘텐츠와 테두리 사이 공간
     */
    Column(modifier = Modifier.padding(16.dp)) {
        /**
         * 모든 방향 동일한 패딩
         */
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(16.dp) // 모든 방향 16dp
                .background(Color.Blue)
        ) {
            Text("균일한 패딩", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * 수평/수직 패딩
         */
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .background(Color.Green)
        ) {
            Text("수평 32dp, 수직 8dp", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * 개별 방향 패딩
         */
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(
                    start = 8.dp,
                    top = 16.dp,
                    end = 24.dp,
                    bottom = 32.dp
                )
                .background(Color.Red)
        ) {
            Text("개별 패딩", color = Color.White)
        }
    }
}

/**
 * Spacer - 간격 만들기
 */
@Composable
fun SpacerUsage() {
    Column {
        Text("첫 번째")
        
        /**
         * 고정 높이 Spacer
         */
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("두 번째")
        
        /**
         * 가변 크기 Spacer
         * - weight와 함께 사용
         */
        Spacer(modifier = Modifier.weight(1f))
        
        Text("세 번째 (아래에 붙음)")
    }
}
```

### 배경과 테두리

```kotlin
@Composable
fun BackgroundAndBorder() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * background - 단색 배경
         */
        Text(
            "단색 배경",
            modifier = Modifier
                .background(Color.Blue)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * background - 그라데이션 배경
         */
        Text(
            "그라데이션 배경",
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Red, Color.Blue)
                    )
                )
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * background - 둥근 모서리 배경
         * clip으로 먼저 모양을 정의
         */
        Text(
            "둥근 배경",
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Green)
                .padding(16.dp),
            color = Color.White
        )
        
        /**
         * border - 테두리
         */
        Text(
            "테두리",
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = Color.Red,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        )
        
        /**
         * border + background
         * 순서 주의!
         */
        Text(
            "테두리 + 배경",
            modifier = Modifier
                .border(3.dp, Color.Red, RoundedCornerShape(12.dp))
                .padding(4.dp) // 테두리와 배경 사이 간격
                .background(Color.Yellow, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )
    }
}
```

### 모양 관련 Modifier

```kotlin
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.*

@Composable
fun ShapeModifiers() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * clip - 모양으로 자르기
         * - CircleShape: 원형
         */
        Image(
            painter = painterResource(R.drawable.sample),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape) // 원형으로 자름
        )
        
        /**
         * RoundedCornerShape - 둥근 모서리
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Blue)
        )
        
        /**
         * RoundedCornerShape - 개별 모서리 지정
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 16.dp,
                        bottomEnd = 32.dp,
                        bottomStart = 8.dp
                    )
                )
                .background(Color.Green)
        )
        
        /**
         * CutCornerShape - 잘린 모서리
         */
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CutCornerShape(16.dp))
                .background(Color.Red)
        )
    }
}
```

---

## Modifier 재사용

### 변수로 재사용

```kotlin
/**
 * 공통 Modifier를 변수로 정의
 * - 코드 중복 제거
 * - 일관성 유지
 */
@Composable
fun ModifierReuse() {
    /**
     * 공통 카드 스타일
     */
    val cardModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
        .padding(16.dp)
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /**
         * 같은 스타일을 여러 곳에서 재사용
         */
        Column(modifier = cardModifier) {
            Text("첫 번째 카드", style = MaterialTheme.typography.titleMedium)
            Text("내용...")
        }
        
        Column(modifier = cardModifier) {
            Text("두 번째 카드", style = MaterialTheme.typography.titleMedium)
            Text("내용...")
        }
        
        /**
         * 기본 스타일에 추가로 Modifier 적용
         */
        Column(
            modifier = cardModifier
                .clickable { println("Clicked!") } // 추가 기능
        ) {
            Text("클릭 가능한 카드", style = MaterialTheme.typography.titleMedium)
            Text("클릭해보세요")
        }
    }
}
```

### 확장 함수로 재사용

```kotlin
/**
 * Modifier 확장 함수
 * - 커스텀 Modifier 패턴 생성
 * - 재사용성 극대화
 */

/**
 * 카드 스타일 확장 함수
 */
fun Modifier.cardStyle(
    backgroundColor: Color = Color.White,
    cornerRadius: Dp = 12.dp,
    borderColor: Color = Color.LightGray,
    elevation: Dp = 4.dp
): Modifier = this
    .shadow(elevation, RoundedCornerShape(cornerRadius))
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
    .padding(16.dp)

/**
 * 사용 예제
 */
@Composable
fun ExtensionFunctionUsage() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        /**
         * 간단하게 카드 스타일 적용
         */
        Column(modifier = Modifier.cardStyle()) {
            Text("기본 카드")
        }
        
        /**
         * 파라미터 커스터마이징
         */
        Column(
            modifier = Modifier.cardStyle(
                backgroundColor = Color(0xFFE3F2FD),
                cornerRadius = 16.dp,
                elevation = 8.dp
            )
        ) {
            Text("커스텀 카드")
        }
    }
}

/**
 * 디버그 모드 확장 함수
 */
fun Modifier.debugBorder(
    enabled: Boolean = BuildConfig.DEBUG,
    color: Color = Color.Red
): Modifier = if (enabled) {
    this.border(2.dp, color)
} else {
    this
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

@Composable
fun ConditionalModifierExample() {
    var isSelected by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Gray)
            /**
             * 조건부로 Modifier 적용
             */
            .conditional(isSelected) {
                border(4.dp, Color.Blue, RoundedCornerShape(8.dp))
            }
            .clickable { isSelected = !isSelected }
    )
}
```

---

## 실전 예제

### 카드 UI

```kotlin
@Composable
fun ProductCard(
    product: Product,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    /**
     * 실무 카드 디자인
     * - 그림자
     * - 둥근 모서리
     * - 클릭 가능
     * - 호버 효과 (선택적)
     */
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            /**
             * 상품 이미지
             * - 비율 유지
             * - 둥근 모서리
             */
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            /**
             * 상품 정보
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${product.price}원",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                /**
                 * 좋아요 버튼
                 * - 클릭 영역 확장
                 */
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(48.dp) // 터치 타겟 크기
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) 
                            Icons.Filled.Favorite 
                        else 
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = "좋아요",
                        tint = if (product.isFavorite) 
                            Color.Red 
                        else 
                            Color.Gray
                    )
                }
            }
        }
    }
}
```

### 로딩 오버레이

```kotlin
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        /**
         * 메인 콘텐츠
         */
        content()
        
        /**
         * 로딩 오버레이
         * - 반투명 배경
         * - 중앙에 로딩 인디케이터
         */
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // 반투명
                    .clickable(enabled = false) { } // 클릭 차단
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
```

---

## 요약

### Modifier 핵심 원칙

1. **순서가 중요**: Modifier 순서에 따라 결과가 달라짐
2. **체이닝**: 여러 Modifier를 연결해서 사용
3. **재사용**: 공통 Modifier는 변수나 확장 함수로
4. **최소화**: 필요한 Modifier만 사용 (성능)

### 일반적인 패턴

```kotlin
/**
 * 추천 순서
 */
Modifier
    // 1. 크기
    .size(100.dp)
    
    // 2. 외형 (clip, shadow)
    .clip(RoundedCornerShape(8.dp))
    .shadow(4.dp)
    
    // 3. 배경
    .background(Color.Blue)
    
    // 4. 간격
    .padding(16.dp)
    
    // 5. 인터랙션
    .clickable { }
```

다음 문서에서는 **레이아웃 Modifier**를 상세히 다룹니다!
