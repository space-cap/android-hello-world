# 성능 프로파일링 실전

> 📖 **시리즈 구성**
> - **21-1**: [Compose 성능 최적화](./21-1-compose-performance-optimization.md)
> - **21-2**: [메모리 및 배터리 최적화](./21-2-memory-battery-optimization.md)
> - **21-3**: 성능 프로파일링 실전 (현재 문서)

---

## Android Profiler 사용법

### CPU Profiler

```
1. Android Studio → View → Tool Windows → Profiler
2. 앱 실행
3. CPU 섹션 클릭
4. Record 버튼 클릭
5. 앱 사용
6. Stop 버튼 클릭
7. 결과 분석
```

### Memory Profiler

```
메모리 누수 찾기:
1. Memory Profiler 열기
2. Dump Java Heap 클릭
3. Analyzer Tasks → Detect Leaked Activities
4. 누수된 Activity 확인
```

---

## 실전 프로파일링 시나리오

### 1. 앱 시작 시간 최적화

```kotlin
// Application.onCreate() 최적화
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // ❌ 나쁜 예: 모든 초기화를 메인 스레드에서
        initLibraryA()
        initLibraryB()
        initLibraryC()
        
        // ✅ 좋은 예: 백그라운드에서 초기화
        lifecycleScope.launch(Dispatchers.IO) {
            initLibraryA()
            initLibraryB()
        }
    }
}
```

### 2. 스크롤 버벅임 해결

```kotlin
// LazyColumn 최적화
LazyColumn {
    items(
        items = items,
        key = { it.id },  // ✅ key 사용
        contentType = { "item" }  // ✅ contentType 사용
    ) { item ->
        ItemRow(item)
    }
}
```

---

## Baseline Profiles

```bash
# Baseline Profile 생성
./gradlew generateBaselineProfile

# 결과 확인
app/src/main/baseline-prof.txt
```

---

**마지막 업데이트**: 2024-12-03
