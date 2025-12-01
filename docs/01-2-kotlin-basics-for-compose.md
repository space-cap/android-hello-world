# Kotlin 기초 - Compose를 위한 필수 문법

> [!NOTE]
> **시작하기 전에**
> 
> Kotlin의 탄생 배경과 역사가 궁금하다면 먼저 읽어보세요:  
> ➡️ [01-1-kotlin-history.md](./01-1-kotlin-history.md) - Kotlin의 역사와 탄생 배경

## 📚 목차
1. [왜 Kotlin인가?](#왜-kotlin인가)
2. [변수와 상수](#변수와-상수)
3. [함수](#함수)
4. [데이터 타입](#데이터-타입)
5. [Null Safety](#null-safety)
6. [클래스와 데이터 클래스](#클래스와-데이터-클래스)
7. [컬렉션](#컬렉션)
8. [람다와 고차 함수](#람다와-고차-함수)
9. [확장 함수](#확장-함수)
10. [실습 예제](#실습-예제)

---

## 왜 Kotlin인가?

### Kotlin의 장점

| 특징 | 설명 | 예시 |
|------|------|------|
| **간결함** | Java보다 코드가 짧음 | 40% 적은 코드 |
| **안전함** | Null 안전성 내장 | NullPointerException 방지 |
| **상호운용성** | Java와 100% 호환 | 기존 Java 라이브러리 사용 가능 |
| **현대적** | 최신 프로그래밍 패러다임 | 함수형 프로그래밍 지원 |

### Jetpack Compose와 Kotlin

```kotlin
// Compose는 Kotlin의 기능을 적극 활용
@Composable
fun Greeting(name: String) {  // 함수
    Text(text = "Hello, $name!")  // 문자열 템플릿
}
```

> [!IMPORTANT]
> **Jetpack Compose는 Kotlin 전용입니다**
> - Java로는 Compose를 사용할 수 없습니다
> - Kotlin 기초를 먼저 학습해야 Compose를 이해할 수 있습니다

---

## 변수와 상수

### val vs var

```kotlin
// val = 불변 (읽기 전용, 권장)
val name = "홍길동"
name = "김철수"  // ❌ 에러! 변경 불가

// var = 가변 (변경 가능)
var age = 25
age = 26  // ✅ 가능
```

#### 언제 무엇을 사용할까?

| 상황 | 사용 | 이유 |
|------|------|------|
| 값이 변하지 않음 | `val` | 안전하고 예측 가능 |
| 값이 변해야 함 | `var` | 필요한 경우에만 |
| **기본 원칙** | **`val` 우선** | 불변성이 버그를 줄임 |

### 타입 추론

Kotlin은 타입을 자동으로 추론합니다.

```kotlin
// 타입 명시 (선택적)
val name: String = "홍길동"
val age: Int = 25

// 타입 추론 (권장)
val name = "홍길동"  // String으로 자동 추론
val age = 25        // Int로 자동 추론
```

### Compose에서의 사용

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }  // var: 값이 변함
    val buttonText = "클릭"  // val: 값이 고정
    
    Button(onClick = { count++ }) {
        Text("$buttonText: $count")
    }
}
```

---

## 함수

### 기본 함수 정의

```kotlin
// 반환값이 있는 함수
fun add(a: Int, b: Int): Int {
    return a + b
}

// 표현식 함수 (간결한 형태)
fun add(a: Int, b: Int): Int = a + b

// 타입 추론 (더 간결)
fun add(a: Int, b: Int) = a + b

// 반환값이 없는 함수
fun printSum(a: Int, b: Int) {
    println(a + b)
}
```

### 기본 매개변수

```kotlin
fun greet(name: String = "손님", greeting: String = "안녕하세요") {
    println("$greeting, $name!")
}

greet()                          // "안녕하세요, 손님!"
greet("홍길동")                   // "안녕하세요, 홍길동!"
greet("홍길동", "환영합니다")      // "환영합니다, 홍길동!"
```

### 명명된 인자

```kotlin
fun createUser(name: String, age: Int, email: String) {
    // ...
}

// 순서를 바꿔도 OK
createUser(
    email = "test@example.com",
    name = "홍길동",
    age = 25
)
```

### Compose에서의 함수

```kotlin
// Composable 함수
@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier  // 기본 매개변수
) {
    Text(
        text = "Hello, $name!",
        modifier = modifier
    )
}

// 사용
Greeting(name = "홍길동")  // 명명된 인자
```

---

## 데이터 타입

### 기본 타입

```kotlin
// 숫자
val byte: Byte = 127
val short: Short = 32767
val int: Int = 2147483647
val long: Long = 9223372036854775807L

val float: Float = 3.14f
val double: Double = 3.141592

// 문자와 문자열
val char: Char = 'A'
val string: String = "Hello"

// 불린
val isTrue: Boolean = true
val isFalse: Boolean = false
```

### 문자열 템플릿

```kotlin
val name = "홍길동"
val age = 25

// 변수 삽입
println("이름: $name")  // "이름: 홍길동"

// 표현식 삽입
println("내년 나이: ${age + 1}")  // "내년 나이: 26"

// 여러 줄 문자열
val message = """
    안녕하세요,
    $name님!
    나이: $age세
""".trimIndent()
```

### Compose에서의 사용

```kotlin
@Composable
fun UserProfile(name: String, age: Int) {
    Column {
        Text("이름: $name")
        Text("나이: ${age}세")
        Text("성인: ${if (age >= 19) "예" else "아니오"}")
    }
}
```

---

## Null Safety

Kotlin의 가장 강력한 기능 중 하나입니다.

### Nullable vs Non-nullable

```kotlin
// Non-nullable (기본)
var name: String = "홍길동"
name = null  // ❌ 에러!

// Nullable (? 추가)
var name: String? = "홍길동"
name = null  // ✅ 가능
```

### Null 처리 방법

#### 1. Safe Call (?.)

```kotlin
val name: String? = null
val length = name?.length  // null이면 null 반환
println(length)  // null
```

#### 2. Elvis 연산자 (?:)

```kotlin
val name: String? = null
val length = name?.length ?: 0  // null이면 0 반환
println(length)  // 0
```

#### 3. Non-null 단언 (!!)

```kotlin
val name: String? = "홍길동"
val length = name!!.length  // null이 아님을 보장
// ⚠️ 주의: null이면 예외 발생!
```

#### 4. let 함수

```kotlin
val name: String? = "홍길동"

name?.let {
    // name이 null이 아닐 때만 실행
    println("이름: $it")
}
```

### Compose에서의 Null Safety

```kotlin
@Composable
fun UserProfile(user: User?) {
    // Safe call과 Elvis 연산자
    Text(text = user?.name ?: "이름 없음")
    
    // let 사용
    user?.let {
        Column {
            Text("이름: ${it.name}")
            Text("나이: ${it.age}")
        }
    } ?: Text("사용자 정보 없음")
}
```

---

## 클래스와 데이터 클래스

### 기본 클래스

```kotlin
class Person(val name: String, var age: Int) {
    fun introduce() {
        println("안녕하세요, $name입니다. ${age}세입니다.")
    }
}

// 사용
val person = Person("홍길동", 25)
person.introduce()
person.age = 26  // var이므로 변경 가능
```

### 데이터 클래스

데이터를 담는 클래스에 최적화되어 있습니다.

```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String
)

val user1 = User(1, "홍길동", "hong@example.com")
val user2 = User(1, "홍길동", "hong@example.com")

// 자동으로 생성되는 기능들
println(user1 == user2)  // true (내용 비교)
println(user1)           // User(id=1, name=홍길동, email=hong@example.com)

// copy: 일부만 변경한 새 객체 생성
val user3 = user1.copy(name = "김철수")
println(user3)  // User(id=1, name=김철수, email=hong@example.com)
```

### 데이터 클래스의 자동 생성 기능

| 기능 | 설명 |
|------|------|
| `equals()` | 내용 비교 |
| `hashCode()` | 해시 코드 생성 |
| `toString()` | 문자열 표현 |
| `copy()` | 복사본 생성 |
| `componentN()` | 구조 분해 |

### 구조 분해 (Destructuring)

```kotlin
val user = User(1, "홍길동", "hong@example.com")

// 구조 분해
val (id, name, email) = user
println("ID: $id, 이름: $name")
```

### Compose에서의 데이터 클래스

```kotlin
// 사용자 정보를 담는 데이터 클래스
data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    val age: Int
)

@Composable
fun UserCard(user: UserInfo) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("이름: ${user.name}")
            Text("이메일: ${user.email}")
            Text("나이: ${user.age}세")
        }
    }
}

// Navigation에서 데이터 전달 시 사용
fun UserInfo.toJson(): String { /* ... */ }
```

---

## 컬렉션

### List - 순서가 있는 목록

```kotlin
// 불변 리스트 (읽기 전용)
val numbers = listOf(1, 2, 3, 4, 5)
val names = listOf("Alice", "Bob", "Charlie")

// 가변 리스트
val mutableNumbers = mutableListOf(1, 2, 3)
mutableNumbers.add(4)
mutableNumbers.remove(1)

// 접근
println(numbers[0])        // 1
println(numbers.first())   // 1
println(numbers.last())    // 5
```

### Set - 중복 없는 집합

```kotlin
val uniqueNumbers = setOf(1, 2, 3, 2, 1)  // {1, 2, 3}
val mutableSet = mutableSetOf(1, 2, 3)
mutableSet.add(4)
```

### Map - 키-값 쌍

```kotlin
// 불변 맵
val ages = mapOf(
    "Alice" to 25,
    "Bob" to 30,
    "Charlie" to 35
)

// 접근
println(ages["Alice"])  // 25
println(ages.get("Alice"))  // 25

// 가변 맵
val mutableAges = mutableMapOf("Alice" to 25)
mutableAges["Bob"] = 30
```

### 컬렉션 연산

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// filter: 조건에 맞는 요소만
val evenNumbers = numbers.filter { it % 2 == 0 }  // [2, 4]

// map: 각 요소를 변환
val doubled = numbers.map { it * 2 }  // [2, 4, 6, 8, 10]

// forEach: 각 요소에 대해 실행
numbers.forEach { println(it) }

// any: 조건을 만족하는 요소가 있는지
val hasEven = numbers.any { it % 2 == 0 }  // true

// all: 모든 요소가 조건을 만족하는지
val allPositive = numbers.all { it > 0 }  // true

// find: 조건을 만족하는 첫 요소
val firstEven = numbers.find { it % 2 == 0 }  // 2

// count: 조건을 만족하는 요소 개수
val evenCount = numbers.count { it % 2 == 0 }  // 2

// sum: 합계
val sum = numbers.sum()  // 15
```

### Compose에서의 컬렉션

```kotlin
@Composable
fun ContactList() {
    val contacts = listOf(
        "Alice",
        "Bob",
        "Charlie"
    )
    
    LazyColumn {
        items(contacts) { name ->
            Text(name)
        }
    }
}

@Composable
fun FilteredList() {
    val allItems = listOf("Apple", "Banana", "Cherry", "Date")
    var searchText by remember { mutableStateOf("") }
    
    // filter 사용
    val filteredItems = allItems.filter {
        it.contains(searchText, ignoreCase = true)
    }
    
    Column {
        TextField(
            value = searchText,
            onValueChange = { searchText = it }
        )
        
        LazyColumn {
            items(filteredItems) { item ->
                Text(item)
            }
        }
    }
}
```

---

## 람다와 고차 함수

### 람다 표현식

람다는 익명 함수입니다.

```kotlin
// 일반 함수
fun double(x: Int): Int {
    return x * 2
}

// 람다로 표현
val double = { x: Int -> x * 2 }

// 사용
println(double(5))  // 10
```

### 람다 문법

```kotlin
// 기본 형태
val sum = { a: Int, b: Int -> a + b }

// 타입 추론
val numbers = listOf(1, 2, 3)
val doubled = numbers.map { it * 2 }  // it: 단일 매개변수

// 여러 줄
val complexLambda = { x: Int ->
    val result = x * 2
    result + 1
}
```

### 고차 함수

함수를 매개변수로 받거나 반환하는 함수입니다.

```kotlin
// 함수를 매개변수로 받음
fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

// 사용
val sum = calculate(5, 3) { a, b -> a + b }      // 8
val product = calculate(5, 3) { a, b -> a * b }  // 15
```

### Trailing Lambda

마지막 매개변수가 람다면 괄호 밖으로 뺄 수 있습니다.

```kotlin
// 일반 형태
numbers.filter({ it % 2 == 0 })

// Trailing lambda (권장)
numbers.filter { it % 2 == 0 }

// 여러 매개변수가 있을 때
Button(
    onClick = { /* 클릭 이벤트 */ },
    modifier = Modifier.padding(16.dp)
) {
    Text("클릭")  // 마지막 람다
}
```

### Compose에서의 람다

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    // onClick은 람다를 받음
    Button(onClick = { count++ }) {
        Text("카운트: $count")
    }
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit  // 함수 타입 매개변수
) {
    Button(onClick = onClick) {
        Text(text)
    }
}

// 사용
CustomButton(
    text = "저장",
    onClick = { 
        println("저장됨")
    }
)
```

---

## 확장 함수

기존 클래스에 새로운 함수를 추가할 수 있습니다.

### 기본 확장 함수

```kotlin
// String에 함수 추가
fun String.addExclamation(): String {
    return this + "!"
}

// 사용
val greeting = "안녕하세요"
println(greeting.addExclamation())  // "안녕하세요!"
```

### 실용적인 예제

```kotlin
// Int 확장
fun Int.isEven(): Boolean = this % 2 == 0

println(4.isEven())  // true
println(5.isEven())  // false

// List 확장
fun <T> List<T>.secondOrNull(): T? {
    return if (this.size >= 2) this[1] else null
}

val numbers = listOf(1, 2, 3)
println(numbers.secondOrNull())  // 2
```

### Compose에서의 확장 함수

```kotlin
// Modifier 확장
fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

// 사용
@Composable
fun ConditionalBox(isHighlighted: Boolean) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .conditional(isHighlighted) {
                background(Color.Yellow)
            }
    )
}
```

---

## 실습 예제

### 예제 1: 간단한 계산기

```kotlin
data class Calculator(
    var result: Double = 0.0
) {
    fun add(value: Double) {
        result += value
    }
    
    fun subtract(value: Double) {
        result -= value
    }
    
    fun multiply(value: Double) {
        result *= value
    }
    
    fun divide(value: Double) {
        if (value != 0.0) {
            result /= value
        }
    }
    
    fun clear() {
        result = 0.0
    }
}

// 사용
val calc = Calculator()
calc.add(10.0)
calc.multiply(2.0)
println(calc.result)  // 20.0
```

### 예제 2: 학생 성적 관리

```kotlin
data class Student(
    val name: String,
    val scores: List<Int>
) {
    fun average(): Double {
        return scores.average()
    }
    
    fun isPassing(): Boolean {
        return average() >= 60
    }
}

val students = listOf(
    Student("Alice", listOf(85, 90, 78)),
    Student("Bob", listOf(55, 60, 58)),
    Student("Charlie", listOf(95, 92, 98))
)

// 합격한 학생만 필터링
val passingStudents = students.filter { it.isPassing() }

// 평균 점수로 정렬
val sortedByAverage = students.sortedByDescending { it.average() }

// 각 학생의 평균 출력
students.forEach { student ->
    println("${student.name}: ${student.average()}")
}
```

### 예제 3: Todo 리스트

```kotlin
data class Todo(
    val id: Int,
    val title: String,
    var isDone: Boolean = false
)

class TodoList {
    private val todos = mutableListOf<Todo>()
    private var nextId = 1
    
    fun add(title: String) {
        todos.add(Todo(nextId++, title))
    }
    
    fun toggle(id: Int) {
        todos.find { it.id == id }?.let {
            it.isDone = !it.isDone
        }
    }
    
    fun remove(id: Int) {
        todos.removeIf { it.id == id }
    }
    
    fun getAll(): List<Todo> = todos.toList()
    
    fun getCompleted(): List<Todo> = todos.filter { it.isDone }
    
    fun getPending(): List<Todo> = todos.filter { !it.isDone }
}

// 사용
val todoList = TodoList()
todoList.add("Kotlin 공부하기")
todoList.add("Compose 학습하기")
todoList.toggle(1)

println("전체: ${todoList.getAll().size}")
println("완료: ${todoList.getCompleted().size}")
```

---

## 학습 체크리스트

### 기초 개념
- [ ] `val`과 `var`의 차이를 안다
- [ ] 타입 추론을 이해한다
- [ ] 함수를 정의할 수 있다
- [ ] 기본 매개변수를 사용할 수 있다
- [ ] 명명된 인자를 사용할 수 있다

### Null Safety
- [ ] Nullable 타입을 이해한다
- [ ] Safe Call (`?.`)을 사용할 수 있다
- [ ] Elvis 연산자 (`?:`)를 사용할 수 있다
- [ ] `let` 함수를 사용할 수 있다

### 클래스와 데이터
- [ ] 클래스를 정의할 수 있다
- [ ] 데이터 클래스를 만들 수 있다
- [ ] `copy()` 함수를 사용할 수 있다
- [ ] 구조 분해를 사용할 수 있다

### 컬렉션
- [ ] List, Set, Map을 사용할 수 있다
- [ ] `filter`, `map`을 사용할 수 있다
- [ ] `forEach`로 반복할 수 있다
- [ ] 컬렉션 연산을 조합할 수 있다

### 함수형 프로그래밍
- [ ] 람다 표현식을 작성할 수 있다
- [ ] 고차 함수를 이해한다
- [ ] Trailing lambda를 사용할 수 있다
- [ ] 확장 함수를 만들 수 있다

---

## 다음 단계

### Compose에서 자주 사용하는 Kotlin 패턴

이제 Kotlin 기초를 배웠으니, Compose에서 자주 보게 될 패턴들입니다:

```kotlin
// 1. Trailing lambda
Button(onClick = { /* ... */ }) {
    Text("클릭")
}

// 2. 람다와 State
var text by remember { mutableStateOf("") }

// 3. 데이터 클래스
data class User(val name: String, val age: Int)

// 4. 컬렉션 연산
items.filter { it.isActive }.map { it.name }

// 5. Null Safety
user?.name ?: "이름 없음"
```

### 추천 학습 순서

1. ✅ Kotlin 기초 (완료)
2. ➡️ Android 프로젝트 구조
3. ➡️ Jetpack Compose Layout
4. ➡️ State 관리

---

## 참고 자료

### 공식 문서
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Koans](https://play.kotlinlang.org/koans) - 대화형 학습
- [Kotlin by Example](https://play.kotlinlang.org/byExample)

### 연습 사이트
- [Kotlin Playground](https://play.kotlinlang.org/)
- [Exercism Kotlin Track](https://exercism.org/tracks/kotlin)

### 추천 도서
- "Kotlin in Action" - Dmitry Jemerov, Svetlana Isakova
- "Head First Kotlin" - Dawn Griffiths, David Griffiths

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

**학습 예상 시간**: 3-4일  
**난이도**: ⭐⭐

Kotlin을 마스터하면 Compose 학습이 훨씬 쉬워집니다! 🚀
