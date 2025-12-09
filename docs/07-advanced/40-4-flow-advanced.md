# Kotlin Flow 고급

> 📖 **시리즈 구성**
> - **41-1**: [Kotlin Flow 기초](./41-1-flow-basics.md)
> - **41-2**: Kotlin Flow 고급 (현재 문서)

---

## 📚 목차

1. [StateFlow & SharedFlow](#stateflow--sharedflow)
2. [Flow 조합](#flow-조합)
3. [에러 처리](#에러-처리)
4. [테스팅](#테스팅)

---

## StateFlow & SharedFlow

### StateFlow

```kotlin
/**
 * StateFlow: 상태 관리
 */
class ViewModel : ViewModel() {
    private val _state = MutableStateFlow(0)
    val state: StateFlow<Int> = _state.asStateFlow()
    
    fun increment() {
        _state.value++
    }
}
```

### SharedFlow

```kotlin
/**
 * SharedFlow: 이벤트 전달
 */
class EventBus {
    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()
    
    suspend fun emit(event: Event) {
        _events.emit(event)
    }
}
```

---

## Flow 조합

### combine

```kotlin
/**
 * 여러 Flow 조합
 */
val combined = combine(flow1, flow2, flow3) { a, b, c ->
    "$a $b $c"
}
```

---

## 에러 처리

### catch 연산자

```kotlin
/**
 * 에러 처리
 */
val safeFlow = flow
    .catch { e ->
        emit(defaultValue)
    }
```

---

## 테스팅

### Flow 테스트

```kotlin
/**
 * Turbine을 사용한 테스트
 */
@Test
fun testFlow() = runTest {
    flow.test {
        assertEquals(1, awaitItem())
        assertEquals(2, awaitItem())
        awaitComplete()
    }
}
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
