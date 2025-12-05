# Box로 커스텀 버튼 만들기

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐⭐ 중급-고급  
> **예상 학습 시간**: 2-3시간

## 목차
1. [개요](#개요)
2. [왜 Box를 사용하는가?](#왜-box를-사용하는가)
3. [기본 Box 버튼 구현](#기본-box-버튼-구현)
4. [인터랙션 처리](#인터랙션-처리)
5. [리플 효과 추가](#리플-효과-추가)
6. [고급 커스텀 버튼 패턴](#고급-커스텀-버튼-패턴)
7. [실전 예제](#실전-예제)
8. [성능 최적화](#성능-최적화)

---

## 개요

Material Design의 기본 `Button`은 많은 경우에 충분하지만, 디자인이 복잡하거나 특별한 인터랙션이 필요한 경우에는 `Box`를 사용하여 처음부터 커스텀 버튼을 만들어야 합니다.

### Box 버튼이 필요한 경우

- ✅ 복잡한 레이아웃이 필요한 경우 (여러 레이어, 오버레이)
- ✅ Material Design을 따르지 않는 독특한 디자인
- ✅ 애니메이션이나 특수 효과가 필요한 경우
- ✅ 여러 터치 영역이나 복잡한 인터랙션
- ✅ 완전한 커스터마이징이 필요한 경우

---

## 왜 Box를 사용하는가?

### Material Button의 한계

```kotlin
// Material Button은 구조가 정해져 있음
Button(onClick = { }) {
    // 여기에는 단순한 콘텐츠만 가능
    Text("버튼")
}
```

### Box를 사용한 커스텀 버튼의 장점

```kotlin
// Box는 완전히 자유로운 레이아웃 가능
Box(
    modifier = Modifier
        .clickable { /* 클릭 처리 */ }
) {
    // 원하는 모든 것을 배치 가능
    Image(...)
    Text(...)
    Badge(...)
    // 등등
}
```

---

## 기본 Box 버튼 구현

### 1. 가장 단순한 Box 버튼

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))  // 먼저 clip (터치 영역도 제한됨)
            .background(Color(0xFF6200EE))   // 배경색
            .clickable { onClick() }         // 클릭 가능하게
            .padding(horizontal = 24.dp, vertical = 12.dp), // 내부 패딩
        contentAlignment = Alignment.Center  // 중앙 정렬
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}
```

> **⚠️ 중요**: Modifier 순서가 매우 중요합니다!
> - `clip` → `background` → `clickable` → `padding` 순서를 지켜야 합니다.

### 2. modifier 순서의 중요성

```kotlin
@Composable
fun ModifierOrderExample() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // ❌ 잘못된 순서 - 클릭 영역이 너무 큼
        Box(
            modifier = Modifier
                .padding(16.dp)      // 패딩을 먼저 주면
                .clickable { }       // 클릭 영역이 패딩 포함
                .background(Color.Blue)
        ) {
            Text("클릭 영역이 너무 큼")
        }
        
        // ✅ 올바른 순서 - 클릭 영역이 정확함
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))  // 1. 먼저 모양 자르기
                .background(Color.Blue)          // 2. 배경색
                .clickable { }                   // 3. 클릭 가능하게
                .padding(16.dp)                  // 4. 내부 패딩
        ) {
            Text("클릭 영역이 정확함")
        }
    }
}
```

---

## 인터랙션 처리

### 1. 터치 상태 추적

```kotlin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun InteractiveBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // InteractionSource로 터치 상태 추적
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                // 눌렀을 때 색상 변경
                if (isPressed) Color(0xFF3700B3)
                else Color(0xFF6200EE)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null  // 기본 리플 제거
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}
```

### 2. 다양한 인터랙션 상태

```kotlin
import androidx.compose.foundation.interaction.*
import androidx.compose.runtime.LaunchedEffect

@Composable
fun FullInteractionBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // 다양한 상태 추적
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()  // 마우스 오버 (데스크톱)
    val isFocused by interactionSource.collectIsFocusedAsState()  // 키보드 포커스
    
    // 배경색 결정
    val backgroundColor = when {
        isPressed -> Color(0xFF3700B3)  // 눌렀을 때
        isHovered -> Color(0xFF7C4DFF)  // 마우스 오버
        isFocused -> Color(0xFF7C4DFF)  // 포커스
        else -> Color(0xFF6200EE)       // 기본
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}
```

### 3. enabled/disabled 상태 처리

```kotlin
@Composable
fun EnabledBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    !enabled -> Color.Gray.copy(alpha = 0.4f)  // 비활성화
                    isPressed -> Color(0xFF3700B3)              // 눌림
                    else -> Color(0xFF6200EE)                   // 기본
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled  // enabled 파라미터 전달
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.Gray
        )
    }
}
```

---

## 리플 효과 추가

### 1. 기본 리플 효과

```kotlin
import androidx.compose.material.ripple.rememberRipple

@Composable
fun RippleBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF6200EE))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()  // 기본 리플 추가
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}
```

### 2. 커스텀 리플 효과

```kotlin
@Composable
fun CustomRippleBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rippleColor: Color = Color.White,
    bounded: Boolean = true  // 리플을 버튼 경계 내로 제한
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF6200EE))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = bounded,
                    color = rippleColor
                )
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}
```

### 3. 리플 없이 커스텀 애니메이션

```kotlin
import androidx.compose.animation.core.*
import androidx.compose.runtime.*

@Composable
fun CustomAnimationButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 눌렀을 때 스케일 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF6200EE))
            .clickable(
                interactionSource = interactionSource,
                indication = null  // 리플 제거
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}
```

---

## 고급 커스텀 버튼 패턴

### 1. 그라데이션 배경 버튼

```kotlin
import androidx.compose.ui.graphics.Brush

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6200EE),
            Color(0xFF3700B3)
        )
    )
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 눌렀을 때 투명도 조절
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)  // 그라데이션 배경
            .graphicsLayer { this.alpha = alpha }
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White)
            ) { onClick() }
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### 2. 테두리 애니메이션 버튼

```kotlin
import androidx.compose.foundation.border

@Composable
fun AnimatedBorderButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 테두리 두께 애니메이션
    val borderWidth by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 2.dp,
        label = "border_width"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = borderWidth,
                color = Color(0xFF6200EE),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color(0xFF6200EE))
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF6200EE)
        )
    }
}
```

### 3. 아이콘과 배지가 있는 버튼

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications

@Composable
fun BadgeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF6200EE))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // 메인 콘텐츠
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = if (badgeCount > 0) 8.dp else 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color.White)
        }
        
        // 배지
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

### 4. 다층 구조 버튼 (그림자 효과)

```kotlin
@Composable
fun LayeredButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 눌렀을 때 오프셋 변경 (눌리는 효과)
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        label = "offset_y"
    )
    
    Box(modifier = modifier) {
        // 그림자 레이어
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3700B3))
        )
        
        // 메인 버튼 레이어
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF6200EE))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

### 5. 로딩 애니메이션이 있는 버튼

```kotlin
@Composable
fun LoadingBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    // 로딩 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isLoading) Color.Gray else Color(0xFF6200EE)
            )
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // 로딩 인디케이터
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = rotation }
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Text(text = text, color = Color.White)
        }
    }
}
```

---

## 실전 예제

### 예제 1: 소셜 로그인 버튼

```kotlin
@Composable
fun SocialLoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = contentColor.copy(alpha = 0.1f))
            ) { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 텍스트
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

// 사용 예
@Composable
fun SocialLoginExample() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        SocialLoginButton(
            text = "Google로 계속하기",
            onClick = { },
            icon = {
                // Google 아이콘 (실제로는 Image 사용)
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        SocialLoginButton(
            text = "Facebook으로 계속하기",
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Facebook,
                    contentDescription = null,
                    tint = Color(0xFF1877F2)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### 예제 2: 플로팅 액션 버튼

```kotlin
@Composable
fun CustomFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    backgroundColor: Color = Color(0xFF6200EE),
    contentColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 눌렀을 때 크기 변화
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "fab_scale"
    )
    
    Box(
        modifier = modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = contentColor)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "FAB",
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
```

### 예제 3: 세그먼트 컨트롤 (iOS 스타일)

```kotlin
@Composable
fun <T> IOSSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() }
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val isSelected = option == selectedOption
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) Color.White
                            else Color.Transparent
                        )
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optionLabel(option),
                        color = if (isSelected) Color.Black else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                
                // 구분선 (마지막 제외)
                if (index < options.size - 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}
```

### 예제 4: 카드형 버튼

```kotlin
@Composable
fun CardButton(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple()
            ) { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 아이콘
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6200EE).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color(0xFF6200EE),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            // 텍스트
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // 화살표
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
```

---

## 성능 최적화

### 1. remember를 사용한 최적화

```kotlin
@Composable
fun OptimizedBoxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ InteractionSource는 remember로 재사용
    val interactionSource = remember { MutableInteractionSource() }
    
    // ✅ 불변 값들은 remember로 캐싱
    val shape = remember { RoundedCornerShape(8.dp) }
    val backgroundColor = remember { Color(0xFF6200EE) }
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple()
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}
```

### 2. 불필요한 리컴포지션 방지

```kotlin
@Composable
fun NoRecompositionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int  // 외부 상태
) {
    // ❌ 나쁜 예: count가 변할 때마다 전체 리컴포지션
    Box(
        modifier = modifier
            .background(Color(0xFF6200EE))
            .clickable { onClick() }
    ) {
        Column {
            Text("클릭 횟수: $count")
            Text(text)
        }
    }
    
    // ✅ 좋은 예: count 변경이 Text만 리컴포지션
    Box(
        modifier = modifier
            .background(Color(0xFF6200EE))
            .clickable { onClick() }
    ) {
        Column {
            // count가 변해도 이 Text만 리컴포지션
            key(count) {
                Text("클릭 횟수: $count")
            }
            // 이 Text는 리컴포지션 안 됨
            Text(text)
        }
    }
}
```

### 3. derivedStateOf 사용

```kotlin
@Composable
fun DerivedStateButton(
    items: List<String>,
    onClick: () -> Unit
) {
    // ❌ items가 변할 때마다 리컴포지션
    val itemCount = items.size
    
    // ✅ size가 실제로 변할 때만 리컴포지션
    val itemCount by remember {
        derivedStateOf { items.size }
    }
    
    Box(
        modifier = Modifier
            .background(Color(0xFF6200EE))
            .clickable { onClick() }
    ) {
        Text("아이템 수: $itemCount")
    }
}
```

---

## 요약 및 체크리스트

### ✅ Box 버튼 구현 체크리스트

- [ ] **Modifier 순서**: clip → background → clickable → padding
- [ ] **인터랙션 처리**: InteractionSource로 터치 상태 추적
- [ ] **리플 효과**: rememberRipple() 또는 커스텀 애니메이션
- [ ] **접근성**:
  - [ ] 충분한 터치 영역 (최소 48dp)
  - [ ] 명확한 시각적 피드백
  - [ ] semantics 추가
- [ ] **성능**:
  - [ ] remember로 불변 값 캐싱
  - [ ] 불필요한 리컴포지션 방지
  - [ ] derivedStateOf 활용
- [ ] **상태 관리**:
  - [ ] enabled/disabled
  - [ ] pressed/hover/focused
  - [ ] loading
- [ ] **애니메이션**: 부드러운 전환 효과

### 핵심 원칙

1. **Modifier 순서가 중요**: 실행 순서를 이해하고 적용
2. **인터랙션 피드백 필수**: 사용자에게 명확한 반응 제공
3. **성능 고려**: 불필요한 리컴포지션 최소화
4. **접근성**: 모든 사용자가 사용할 수 있도록

---

## Material Button vs Box Button

| 특징 | Material Button | Box Button |
|------|----------------|------------|
| **사용 난이도** | 쉬움 | 중급-고급 |
| **커스터마이징** | 제한적 | 완전 자유 |
| **접근성** | 자동 지원 | 수동 구현 필요 |
| **리플 효과** | 기본 제공 | 수동 추가 |
| **성능** | 최적화됨 | 신경써야 함 |
| **사용 시기** | 대부분의 경우 | 특수한 디자인 |

---

## 참고 자료

- [Compose Modifiers](https://developer.android.com/jetpack/compose/modifiers)
- [Compose Interaction](https://developer.android.com/jetpack/compose/interaction)
- [Compose Animation](https://developer.android.com/jetpack/compose/animation)
- [Custom Layouts](https://developer.android.com/jetpack/compose/layouts/custom)

---

**관련 문서**:
- [92. 실무용 Composable Button 가이드](./92-composable-button-production-guide.md)
- [10. 애니메이션](./10-jetpack-compose-animation-guide.md)
- [19-3. Modifier 고급](./19-3-modifier-advanced.md)

Happy Coding! 🚀
