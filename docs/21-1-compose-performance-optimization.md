# Compose 성능 최적화

> 📖 **시리즈 구성**
> - **21-1**: Compose 성능 최적화 (현재 문서)
> - **21-2**: [메모리 및 배터리 최적화](./21-2-memory-battery-optimization.md)
> - **21-3**: [성능 프로파일링 실전](./21-3-performance-profiling.md)

---

## Recomposition 최적화

### remember와 derivedStateOf

```kotlin
// ❌ 나쁜 예: 매번 계산
@Composable
fun BadExample(items: List<Item>) {
    val filteredItems = items.filter { it.isActive }  // 매번 실행!
}

// ✅ 좋은 예: remember 사용
@Composable
fun GoodExample(items: List<Item>) {
    val filteredItems = remember(items) {
        items.filter { it.isActive }  // items 변경 시만 실행
    }
}

// ✅ 더 좋은 예: derivedStateOf
@Composable
fun BestExample(items: List<Item>) {
    val filteredItems by remember {
        derivedStateOf {
            items.filter { it.isActive }
        }
    }
}
```

### Stable vs Unstable 파라미터

```kotlin
// ❌ 불안정한 타입
data class UnstableItem(
    var name: String,  // var는 불안정
    val items: MutableList<String>  // Mutable 컬렉션
)

// ✅ 안정적인 타입
data class StableItem(
    val name: String,  // val
    val items: List<String>  // Immutable
)

// @Stable 어노테이션
@Stable
data class CustomItem(var internalState: String)
```

---

## LazyList 최적화

```kotlin
@Composable
fun OptimizedList(items: List<Item>) {
    LazyColumn {
        items(
            items = items,
            key = { item -> item.id }  // ✅ 고유 키
        ) { item ->
            ItemRow(item)
        }
    }
}
```

---

## 🎯 다음 단계

**[21-2. 메모리 및 배터리 최적화](./21-2-memory-battery-optimization.md)**

---

**마지막 업데이트**: 2024-12-03
