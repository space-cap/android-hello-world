# Kotlin Flow 기초

> 📖 **시리즈 구성**
> - **41-1**: Kotlin Flow 기초 (현재 문서)
> - **41-2**: [Kotlin Flow 고급](./41-2-flow-advanced.md)

---

## 📚 목차

1. [Flow 개요](#flow-개요)
2. [Flow 생성](#flow-생성)
3. [Flow 연산자](#flow-연산자)
4. [실전 예제](#실전-예제)

---

## Flow 개요

### Flow vs LiveData

```kotlin
/**
 * LiveData (기존 방식)
 */
val liveData: LiveData<String> = MutableLiveData("Hello")

/**
 * Flow (권장 방식)
 */
val flow: Flow<String> = flowOf("Hello")
```

---

## Flow 생성

### 기본 생성 방법

```kotlin
/**
 * flow 빌더
 */
val simpleFlow = flow {
    emit(1)
    emit(2)
    emit(3)
}

/**
 * flowOf
 */
val numbersFlow = flowOf(1, 2, 3, 4, 5)

/**
 * asFlow
 */
val listFlow = listOf(1, 2, 3).asFlow()
```

---

## Flow 연산자

### 변환 연산자

```kotlin
/**
 * map
 */
val doubled = flow.map { it * 2 }

/**
 * filter
 */
val evens = flow.filter { it % 2 == 0 }

/**
 * transform
 */
val transformed = flow.transform { value ->
    emit(value)
    emit(value * 2)
}
```

---

## 실전 예제

### 검색 기능

```kotlin
/**
 * 검색어 Flow
 */
val searchQuery = MutableStateFlow("")

val searchResults = searchQuery
    .debounce(300)
    .filter { it.isNotEmpty() }
    .flatMapLatest { query ->
        searchRepository.search(query)
    }
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
