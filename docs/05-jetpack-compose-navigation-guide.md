# Jetpack Compose Navigation과 데이터 전달 가이드

## 📚 목차
1. [개요](#개요)
2. [기본 개념](#기본-개념)
3. [단일 데이터 전달](#단일-데이터-전달)
4. [복수 데이터 전달](#복수-데이터-전달)
5. [State 관리](#state-관리)
6. [Best Practices](#best-practices)
7. [전체 코드 예제](#전체-코드-예제)

---

## 개요

이 문서는 Android Jetpack Compose에서 Navigation을 사용하여 화면 간 이동 및 데이터 전달을 구현하는 방법을 다룹니다. 단일 필드부터 복수 필드까지 실전에서 바로 사용할 수 있는 패턴을 제공합니다.

### 학습 목표
- ✅ Jetpack Compose Navigation의 기본 구조 이해
- ✅ Navigation Arguments를 통한 데이터 전달
- ✅ Data Class와 JSON을 활용한 복잡한 데이터 전달
- ✅ State 관리 패턴 이해
- ✅ 실무에서 사용 가능한 폼 입력 구현

---

## 기본 개념

### Navigation의 핵심 구성 요소

```kotlin
// 1. NavController 생성
val navController = rememberNavController()

// 2. NavHost 설정
NavHost(navController = navController, startDestination = "page1") {
    // 3. 각 화면(destination) 정의
    composable("page1") { Page1Screen() }
    composable("page2") { Page2Screen() }
}
```

#### 주요 컴포넌트

| 컴포넌트 | 역할 | 설명 |
|---------|------|------|
| `NavController` | 네비게이션 제어 | 화면 이동, 뒤로가기 등을 관리 |
| `NavHost` | 네비게이션 그래프 | 앱의 모든 화면과 경로를 정의 |
| `composable()` | 화면 정의 | 각 route에 대응하는 Composable 함수 연결 |

---

## 단일 데이터 전달

### 시나리오
사용자가 텍스트를 입력하고, 다음 화면에서 입력한 텍스트를 표시

### 데이터 전달 흐름도

```
Page1 (송신)                    Navigation                    Page2 (수신)
─────────────                   ──────────                    ─────────────
"안녕하세요"
    ↓
[URL 인코딩]                                                  
    ↓
"%EC%95%88%EB%85%95..." ────→  URL 전달  ────→  "%EC%95%88%EB%85%95..."
                                                              ↓
                                                         [URL 디코딩]
                                                              ↓
                                                         "안녕하세요"
```

### 구현 방법

#### 1. Navigation Route 정의

```kotlin
NavHost(navController = navController, startDestination = "page1") {
    composable("page1") {
        Page1(navController = navController)
    }
    
    // URL 파라미터 방식으로 데이터 전달
    composable(
        route = "page2/{text}",
        arguments = listOf(navArgument("text") { type = NavType.StringType })
    ) { backStackEntry ->
        // ✅ 1단계: URL에서 인코딩된 문자열 가져오기
        val encodedText = backStackEntry.arguments?.getString("text") ?: ""
        
        // ✅ 2단계: URL 디코딩 (인코딩의 역과정)
        val decodedText = URLDecoder.decode(encodedText, StandardCharsets.UTF_8.toString())
        
        // ✅ 3단계: 디코딩된 원본 텍스트를 Page2에 전달
        Page2(text = decodedText)
    }
}
```

#### 2. 송신 화면 (Page1)

```kotlin
@Composable
fun Page1(navController: NavController) {
    var textInput by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("텍스트를 입력하세요") }
        )
        
        Button(
            onClick = {
                // ✅ URL 인코딩: 한글/특수문자를 URL 안전 문자열로 변환
                val encoded = URLEncoder.encode(textInput, StandardCharsets.UTF_8.toString())
                navController.navigate("page2/$encoded")
            },
            enabled = textInput.isNotEmpty()
        ) {
            Text("다음")
        }
    }
}
```

#### 3. 수신 화면 (Page2)

```kotlin
@Composable
fun Page2(text: String) {
    Column {
        Text("입력한 텍스트: $text")
    }
}
```

### 핵심 포인트

> [!IMPORTANT]
> **URL 인코딩/디코딩이 필수인 이유**
> - 한글, 공백, 특수문자는 URL에 직접 사용 불가
> - `URLEncoder.encode()`로 안전한 문자열로 변환
> - `URLDecoder.decode()`로 원본 복원

---

## 복수 데이터 전달

### 시나리오
회원가입 폼처럼 여러 필드(ID, 비밀번호, 이름, 전화번호)를 다음 화면으로 전달

### 2단계 변환 과정

복수 데이터를 전달할 때는 **2단계 변환**이 필요합니다:

```
Page1 (송신)                                                    Page2 (수신)
─────────────                                                   ─────────────

UserInfo 객체
├─ id: "user123"
├─ pw: "password"
├─ name: "홍길동"
└─ phoneNum: "010-1234-5678"
         ↓
    [1단계: JSON 직렬화]
         ↓
{"id":"user123","pw":"password","name":"홍길동",...}
         ↓
    [2단계: URL 인코딩]
         ↓
%7B%22id%22%3A%22user123%22... ────→ Navigation ────→ %7B%22id%22%3A%22user123%22...
                                                                ↓
                                                         [2단계: URL 디코딩]
                                                                ↓
                                                  {"id":"user123","pw":"password","name":"홍길동",...}
                                                                ↓
                                                         [1단계: JSON 역직렬화]
                                                                ↓
                                                         UserInfo 객체
                                                         ├─ id: "user123"
                                                         ├─ pw: "password"
                                                         ├─ name: "홍길동"
                                                         └─ phoneNum: "010-1234-5678"
```

> [!IMPORTANT]
> **왜 2단계 변환이 필요한가?**
> 1. **JSON 직렬화**: 복잡한 객체를 문자열로 변환 (객체 → 문자열)
> 2. **URL 인코딩**: 한글/특수문자를 URL에서 안전하게 전달 (문자열 → URL 안전 문자열)
> 
> 수신 시에는 **역순으로 디코딩**합니다!

### 구현 방법

#### 1. Data Class 정의

```kotlin
data class UserInfo(
    val id: String = "",
    val pw: String = "",
    val name: String = "",
    val phoneNum: String = ""
) {
    // ✅ JSON 직렬화: 객체를 JSON 문자열로 변환
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("pw", pw)
        json.put("name", name)
        json.put("phoneNum", phoneNum)
        return json.toString()
    }
    
    companion object {
        // ✅ JSON 역직렬화: JSON 문자열을 객체로 변환
        fun fromJson(jsonString: String): UserInfo {
            return try {
                val json = JSONObject(jsonString)
                UserInfo(
                    id = json.optString("id", ""),
                    pw = json.optString("pw", ""),
                    name = json.optString("name", ""),
                    phoneNum = json.optString("phoneNum", "")
                )
            } catch (e: Exception) {
                UserInfo() // 실패 시 빈 객체 반환
            }
        }
    }
}
```

#### 2. Navigation Route 정의

```kotlin
composable(
    route = "page2/{userInfo}",
    arguments = listOf(navArgument("userInfo") { type = NavType.StringType })
) { backStackEntry ->
    // ✅ 1단계: URL에서 인코딩된 JSON 문자열 가져오기
    val encodedUserInfo = backStackEntry.arguments?.getString("userInfo") ?: ""
    
    // ✅ 2단계: URL 디코딩 (URL 안전 문자열 → JSON 문자열)
    val decodedJson = URLDecoder.decode(encodedUserInfo, StandardCharsets.UTF_8.toString())
    // 결과: {"id":"user123","pw":"password","name":"홍길동",...}
    
    // ✅ 3단계: JSON 역직렬화 (JSON 문자열 → UserInfo 객체)
    val userInfo = UserInfo.fromJson(decodedJson)
    // 결과: UserInfo(id="user123", pw="password", name="홍길동", ...)
    
    // ✅ 4단계: 복원된 객체를 Page2에 전달
    Page2(userInfo = userInfo)
}
```

#### 3. 송신 화면 - 폼 입력

```kotlin
@Composable
fun Page1(navController: NavController) {
    // 각 필드별 State
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNum by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("회원 정보 입력", fontSize = 24.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ID 입력
        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 비밀번호 입력 (마스킹 처리)
        OutlinedTextField(
            value = pw,
            onValueChange = { pw = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 이름 입력
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 전화번호 입력 (숫자 키패드)
        OutlinedTextField(
            value = phoneNum,
            onValueChange = { phoneNum = it },
            label = { Text("전화번호") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 제출 버튼
        Button(
            onClick = {
                // ✅ 1단계: UserInfo 객체 생성
                val userInfo = UserInfo(id, pw, name, phoneNum)
                
                // ✅ 2단계: JSON 직렬화 (객체 → JSON 문자열)
                val jsonString = userInfo.toJson()
                // 결과 예: {"id":"user123","pw":"password","name":"홍길동",...}
                
                // ✅ 3단계: URL 인코딩 (JSON 문자열 → URL 안전 문자열)
                val encoded = URLEncoder.encode(jsonString, StandardCharsets.UTF_8.toString())
                // 결과 예: %7B%22id%22%3A%22user123%22...
                
                // ✅ 4단계: Navigation으로 전달
                navController.navigate("page2/$encoded")
            },
            enabled = id.isNotEmpty() && pw.isNotEmpty() && 
                      name.isNotEmpty() && phoneNum.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("다음")
        }
    }
}
```

#### 4. 수신 화면 - 정보 확인

```kotlin
@Composable
fun Page2(userInfo: UserInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("입력한 정보 확인", fontSize = 24.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoRow(label = "아이디", value = userInfo.id)
        InfoRow(label = "비밀번호", value = "•".repeat(userInfo.pw.length))
        InfoRow(label = "이름", value = userInfo.name)
        InfoRow(label = "전화번호", value = userInfo.phoneNum)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp)
        Text(text = value, fontSize = 18.sp)
    }
    Spacer(modifier = Modifier.height(12.dp))
}
```

---

## State 관리

### Remember와 MutableState

```kotlin
var textInput by remember { mutableStateOf("") }
```

#### 구성 요소 설명

| 요소 | 역할 |
|------|------|
| `remember` | Recomposition 시에도 값을 유지 |
| `mutableStateOf` | 변경 가능한 상태 생성 |
| `by` 키워드 | 프로퍼티 위임으로 `.value` 생략 가능 |

### State의 범위

```kotlin
@Composable
fun Page1(navController: NavController) {
    // ✅ 이 State는 Page1 내에서만 유지됨
    var textInput by remember { mutableStateOf("") }
    
    // 다른 화면으로 이동하면 State는 사라짐
    // → Navigation Arguments로 데이터 전달 필요
}
```

### State Hoisting vs Navigation Arguments

| 방식 | 사용 시기 | 장점 | 단점 |
|------|----------|------|------|
| **State Hoisting** | 부모-자식 컴포저블 간 | 간단, 직관적 | 화면 전환 시 부적합 |
| **Navigation Arguments** | 화면 간 이동 | Android 권장 패턴 | 직렬화 필요 |

> [!TIP]
> **Navigation을 사용하는 경우 Navigation Arguments를 사용하세요**
> - State Hoisting은 같은 화면 내 컴포저블 간 데이터 공유에 적합
> - 화면 전환 시에는 Navigation Arguments가 표준 패턴

---

## Best Practices

### 1. TextField 최적화

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("레이블") },
    placeholder = { Text("힌트 텍스트") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,  // 한 줄 입력
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone  // 키보드 타입 지정
    )
)
```

### 2. 비밀번호 필드

```kotlin
OutlinedTextField(
    value = password,
    onValueChange = { password = it },
    label = { Text("비밀번호") },
    visualTransformation = PasswordVisualTransformation(),  // 마스킹
    singleLine = true
)
```

### 3. 버튼 활성화 조건

```kotlin
Button(
    onClick = { /* ... */ },
    enabled = id.isNotEmpty() && pw.isNotEmpty(),  // 조건부 활성화
    modifier = Modifier.fillMaxWidth()
) {
    Text("제출")
}
```

### 4. 스크롤 가능한 폼

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())  // 스크롤 추가
) {
    // 폼 내용
}
```

### 5. 안전한 데이터 전달

```kotlin
// ✅ 올바른 방법
val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
navController.navigate("page2/$encoded")

// ❌ 잘못된 방법 (한글/특수문자 깨짐)
navController.navigate("page2/$text")
```

---

## 전체 코드 예제

### 필수 Import

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
```

### MainActivity 전체 구조

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "page1"
                ) {
                    composable("page1") {
                        Page1(navController = navController)
                    }
                    
                    composable(
                        route = "page2/{userInfo}",
                        arguments = listOf(
                            navArgument("userInfo") { 
                                type = NavType.StringType 
                            }
                        )
                    ) { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("userInfo") ?: ""
                        val decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
                        val userInfo = UserInfo.fromJson(decoded)
                        Page2(userInfo = userInfo)
                    }
                }
            }
        }
    }
}
```

---

## 학습 체크리스트

완료한 항목에 체크하세요:

- [ ] NavController와 NavHost의 역할을 이해했다
- [ ] Navigation Arguments로 단일 데이터를 전달할 수 있다
- [ ] Data Class를 사용하여 복수 데이터를 전달할 수 있다
- [ ] JSON 직렬화/역직렬화를 구현할 수 있다
- [ ] URL 인코딩/디코딩의 필요성을 이해했다
- [ ] remember와 mutableStateOf로 State를 관리할 수 있다
- [ ] TextField의 다양한 옵션을 활용할 수 있다
- [ ] 비밀번호 필드를 구현할 수 있다
- [ ] 조건부 버튼 활성화를 구현할 수 있다
- [ ] 스크롤 가능한 폼을 만들 수 있다

---

## 추가 학습 자료

### 공식 문서
- [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [State in Compose](https://developer.android.com/jetpack/compose/state)
- [TextField in Compose](https://developer.android.com/jetpack/compose/text)

### 다음 단계
1. **뒤로가기 처리**: `navController.popBackStack()`
2. **중첩 Navigation**: Nested Navigation Graphs
3. **ViewModel 통합**: Navigation + ViewModel
4. **Deep Links**: 외부에서 특정 화면으로 진입
5. **Type-safe Navigation**: Kotlin DSL 활용

---

## 문제 해결 (Troubleshooting)

### Q1: 한글이 깨져서 전달됩니다
```kotlin
// 해결: URL 인코딩/디코딩 사용
val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
val decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
```

### Q2: 화면 전환 시 State가 사라집니다
```kotlin
// 원인: remember는 해당 Composable이 사라지면 State도 사라짐
// 해결: Navigation Arguments로 데이터 전달
```

### Q3: 여러 필드를 어떻게 전달하나요?
```kotlin
// 해결: Data Class + JSON 직렬화 사용
data class UserInfo(...)
val json = userInfo.toJson()
```

### Q4: 비밀번호가 보입니다
```kotlin
// 해결: PasswordVisualTransformation 사용
OutlinedTextField(
    visualTransformation = PasswordVisualTransformation()
)
```

---

## 마치며

이 가이드는 실전에서 바로 사용할 수 있는 패턴을 제공합니다. 단순한 텍스트 전달부터 복잡한 폼 데이터까지, Navigation Arguments를 활용하면 안전하고 효율적으로 데이터를 전달할 수 있습니다.

**핵심 원칙**
1. 화면 간 데이터 전달은 Navigation Arguments 사용
2. 복잡한 데이터는 Data Class + JSON 직렬화
3. 한글/특수문자는 반드시 URL 인코딩
4. State는 필요한 범위에서만 관리

Happy Coding! 🚀
