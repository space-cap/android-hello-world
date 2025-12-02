# Compose Multiplatform 고급 기법 및 최적화

> [!NOTE]
> **이 문서는 보강된 종합 가이드로 대체되었습니다!**
> 
> **[👉 48. Compose Multiplatform 고급 가이드 (보강판)으로 이동](./48-compose-multiplatform-advanced-enhanced.md)**
> 
> 220줄 → 약 800줄로 확장하여 다음 내용을 추가했습니다:
> - 상세한 성능 최적화 기법
> - 플랫폼별 최적화 (Desktop, Mobile)
> - 테스팅 전략 및 예제
> - CI/CD 설정 가이드
> - 실전 프로젝트 (메모 앱)

---



---

## 성능 최적화

### Recomposition 최적화

```kotlin
// ❌ 나쁜 예 - 매번 리컴포지션
@Composable
fun BadExample(items: List<String>) {
    items.forEach { item ->
        Text(item)
    }
}

// ✅ 좋은 예 - 키를 사용하여 최적화
@Composable
fun GoodExample(items: List<String>) {
    LazyColumn {
        items(
            items = items,
            key = { it }  // 키 지정으로 리컴포지션 최소화
        ) { item ->
            Text(item)
        }
    }
}
```

### remember와 derivedStateOf

```kotlin
@Composable
fun OptimizedFiltering(items: List<String>, query: String) {
    // derivedStateOf로 계산 최적화
    val filteredItems = remember(items, query) {
        derivedStateOf {
            items.filter { it.contains(query, ignoreCase = true) }
        }
    }.value
    
    LazyColumn {
        items(filteredItems) { item ->
            Text(item)
        }
    }
}
```

---

## 메모리 관리

### DisposableEffect로 리소스 정리

```kotlin
@Composable
fun ResourceManagement() {
    val viewModel = remember { MyViewModel() }
    
    DisposableEffect(Unit) {
        viewModel.start()
        
        onDispose {
            viewModel.cleanup()
        }
    }
}
```

---

## 테스팅

### 단위 테스트

```kotlin
class UserViewModelTest {
    @Test
    fun `test user loading`() = runTest {
        val viewModel = UserViewModel()
        viewModel.loadUsers()
        
        assert(viewModel.users.isNotEmpty())
    }
}
```

### UI 테스트

```kotlin
@Test
fun testButtonClick() = runComposeUiTest {
    setContent {
        var count by remember { mutableStateOf(0) }
        
        Button(onClick = { count++ }) {
            Text("Count: $count")
        }
    }
    
    onNodeWithText("Count: 0").assertExists()
    onNodeWithText("Count: 0").performClick()
    onNodeWithText("Count: 1").assertExists()
}
```

---

## CI/CD 설정

### GitHub Actions

**.github/workflows/build.yml**:
```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: macos-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build Android
        run: ./gradlew :composeApp:assembleDebug
      
      - name: Build iOS
        run: ./gradlew :composeApp:linkDebugFrameworkIosArm64
      
      - name: Build Desktop
        run: ./gradlew :composeApp:packageDistributionForCurrentOS
```

---

## 배포

### Android 배포

```bash
# Release APK 빌드
./gradlew :composeApp:assembleRelease

# AAB 빌드 (Google Play)
./gradlew :composeApp:bundleRelease
```

### iOS 배포

```bash
# Xcode에서 Archive 생성
# Product → Archive
# Distribute App
```

### Desktop 배포

```bash
# 현재 OS용 패키지 생성
./gradlew :composeApp:packageDistributionForCurrentOS

# 결과물: build/compose/binaries/main/
```

---

## 베스트 프랙티스

### 1. 코드 구조

```
composeApp/
├── src/commonMain/
│   ├── kotlin/
│   │   ├── ui/              # UI 레이어
│   │   ├── domain/          # 비즈니스 로직
│   │   ├── data/            # 데이터 레이어
│   │   └── di/              # 의존성 주입
```

### 2. 상태 관리

- ViewModel 사용
- 단방향 데이터 플로우
- Immutable 데이터 모델

### 3. 에러 처리

- Result 패턴 사용
- 사용자 친화적 에러 메시지
- 로깅 및 모니터링

---

## 참고 자료

- [Compose Multiplatform 공식 문서](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Ktor Documentation](https://ktor.io/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
