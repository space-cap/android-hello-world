# Jetpack Compose Canvas & Custom Drawing 문서 작성 완료

## 작업 요약

Jetpack Compose Canvas와 Custom Drawing에 대한 **초보자 친화적이고 매우 상세한** 문서를 2개로 나누어 작성했습니다.

## 생성된 문서 목록

### 1. [54-jetpack-compose-canvas-basics.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/54-jetpack-compose-canvas-basics.md) (약 35KB)

**Canvas 기본 및 도형 그리기**

#### 주요 내용
- ✅ Canvas란? (개념 및 사용 사례)
- ✅ Canvas 기본 사용법 (DrawScope 이해)
- ✅ 기본 도형 그리기
  - 원 (Circle)
  - 사각형 (Rectangle)
  - 둥근 사각형 (Rounded Rectangle)
  - 선 (Line)
  - 호 (Arc)
  - 타원 (Oval)
- ✅ 색상과 스타일
  - 색상 지정 방법
  - 그라데이션 (Linear, Radial, Sweep)
  - Fill vs Stroke
- ✅ Path를 사용한 복잡한 도형
  - 삼각형
  - 별
  - 베지어 곡선
- ✅ 텍스트 그리기
- ✅ 이미지 그리기
- ✅ 좌표계 이해하기
- ✅ 실전 예제 (프로그레스 바, 막대 차트)

#### 문서 특징
- Canvas의 기본 개념을 쉽게 설명
- 모든 코드에 상세한 주석
- 단계별 예제 (간단 → 복잡)
- 시각적 설명 (좌표계 다이어그램)

---

### 2. [55-jetpack-compose-canvas-advanced.md](file:///c:/workdir/space-cap/AndroidStudioProjects/HelloWorld/docs/55-jetpack-compose-canvas-advanced.md) (약 28KB)

**고급 기법 및 실전 예제**

#### 주요 내용
- ✅ 변환 (Transform)
  - 회전 (Rotation)
  - 크기 조정 (Scale)
  - 이동 (Translation)
  - 복합 변환
- ✅ 애니메이션
  - 회전 애니메이션
  - 크기 애니메이션 (펄스 효과)
  - 경로 애니메이션
- ✅ 터치 이벤트 처리
  - 기본 터치 감지
  - 드래그로 그리기 (그림판)
  - 색상 선택 가능한 그림판
- ✅ 블렌드 모드
  - 다양한 블렌드 모드 비교
- ✅ 클리핑 (Clipping)
  - 사각형 클리핑
  - 원형 클리핑
- ✅ 성능 최적화
  - drawWithCache 사용
  - remember 활용
- ✅ 실전 예제
  - 아날로그 시계
  - 원형 차트 (Pie Chart)
  - 웨이브 애니메이션
- ✅ 베스트 프랙티스

#### 문서 특징
- 고급 기법을 단계별로 설명
- 실용적인 예제 (시계, 차트, 그림판)
- 성능 최적화 팁
- 베스트 프랙티스 가이드

---

## 문서 통계

| 항목 | 수치 |
|------|------|
| **총 문서 수** | 2개 |
| **총 용량** | 약 63KB |
| **총 라인 수** | 약 2,000줄 |
| **코드 예제** | 50개 이상 |
| **실전 예제** | 10개 이상 |

---

## 주요 학습 포인트

### 1. Canvas 기본 개념

```kotlin
Canvas(modifier = Modifier.size(200.dp)) {
    // DrawScope 영역
    // size: Canvas 크기
    // center: 중심점
    
    drawCircle(color = Color.Blue, radius = 50.dp.toPx())
}
```

### 2. 기본 도형

- **원**: `drawCircle()`
- **사각형**: `drawRect()`
- **선**: `drawLine()`
- **호**: `drawArc()`

### 3. Path로 복잡한 도형

```kotlin
val path = Path().apply {
    moveTo(x, y)    // 시작점
    lineTo(x, y)    // 선으로 연결
    close()         // 경로 닫기
}
drawPath(path, color = Color.Blue)
```

### 4. 그라데이션

```kotlin
val gradient = Brush.linearGradient(
    colors = listOf(Color.Red, Color.Blue)
)
drawRect(brush = gradient)
```

### 5. 변환

```kotlin
rotate(degrees = 45f) {
    drawRect(...)
}

scale(scale = 2f) {
    drawCircle(...)
}
```

### 6. 애니메이션

```kotlin
val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(...)
)

rotate(degrees = rotation) {
    drawRect(...)
}
```

### 7. 터치 이벤트

```kotlin
Canvas(
    modifier = Modifier.pointerInput(Unit) {
        detectDragGestures { change, _ ->
            // 드래그 처리
        }
    }
) {
    // 그리기
}
```

---

## 실전 예제 하이라이트

### 1. 그림판 앱

```kotlin
@Composable
fun SimpleDrawingBoard() {
    val paths = remember { mutableStateListOf<Path>() }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { /* 새 경로 시작 */ },
                    onDrag = { /* 경로에 점 추가 */ },
                    onDragEnd = { /* 경로 저장 */ }
                )
            }
    ) {
        paths.forEach { path ->
            drawPath(path, color = Color.Black, style = Stroke(...))
        }
    }
}
```

### 2. 아날로그 시계

```kotlin
@Composable
fun AnalogClock() {
    var time by remember { mutableStateOf(Calendar.getInstance()) }
    
    Canvas(modifier = Modifier.size(200.dp)) {
        // 시계 테두리
        drawCircle(...)
        
        // 시침, 분침, 초침
        drawLine(...)
    }
}
```

### 3. 원형 차트

```kotlin
@Composable
fun PieChart(data: List<Pair<String, Float>>) {
    Canvas(modifier = Modifier.size(200.dp)) {
        var startAngle = -90f
        
        data.forEach { (_, value) ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }
}
```

---

## 학습 경로 추천

### 초급 개발자
1. **54-jetpack-compose-canvas-basics.md** 전체 읽기
2. 기본 도형 그리기 실습
3. 간단한 프로그레스 바 만들기

### 중급 개발자
1. **55-jetpack-compose-canvas-advanced.md** 학습
2. 애니메이션 적용 실습
3. 간단한 그림판 앱 만들기

### 고급 개발자
1. 복잡한 차트 구현
2. 게임 그래픽 개발
3. 커스텀 UI 컴포넌트 라이브러리 제작

---

## 활용 분야

### 📊 데이터 시각화
- 막대 차트
- 선 그래프
- 원형 차트
- 레이더 차트

### 🎨 크리에이티브 앱
- 그림판
- 사진 편집
- 필터 효과

### 🎮 게임
- 캐릭터 렌더링
- 배경 그리기
- 파티클 효과

### 📱 커스텀 UI
- 독특한 버튼
- 애니메이션 아이콘
- 인터랙티브 위젯

---

## 베스트 프랙티스

### 1. 성능 최적화

```kotlin
// ✅ 좋은 예: Path 재사용
val path = remember { Path() }

// ❌ 나쁜 예: 매번 새로 생성
Canvas {
    val path = Path()  // 매 리컴포지션마다 생성
}
```

### 2. 좌표 변환

```kotlin
// ✅ 좋은 예: dp를 px로 변환
val radius = 50.dp.toPx()

// ❌ 나쁜 예: 하드코딩
val radius = 150f  // 기기마다 다름
```

### 3. 색상 관리

```kotlin
// ✅ 좋은 예: MaterialTheme 사용
drawCircle(color = MaterialTheme.colorScheme.primary)
```

---

## 다음 단계

Canvas를 마스터한 후:
- **커스텀 Modifier** 학습
- **Shader** 사용법
- **OpenGL/Vulkan** 연동 (고급)

---

## 참고 자료

- [Compose Graphics 공식 문서](https://developer.android.com/jetpack/compose/graphics)
- [Canvas API 레퍼런스](https://developer.android.com/reference/kotlin/androidx/compose/foundation/Canvas)
- [DrawScope 레퍼런스](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/drawscope/DrawScope)

---

**문서 작성 완료일**: 2024년 12월 1일  
**작성자**: Gemini AI Assistant
