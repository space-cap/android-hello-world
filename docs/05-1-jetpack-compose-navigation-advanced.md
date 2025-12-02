# Jetpack Compose Navigation 심화 가이드

> 📖 **Navigation 가이드 시리즈**
> - **05**: [Navigation 기초](./05-jetpack-compose-navigation-guide.md) - 기본 개념, 데이터 전달
> - **05-1**: Navigation 심화 (현재 문서) - Type Safe, 중첩 그래프, BottomNav, DeepLink

---

## 📚 목차
1. [Type Safe Navigation](#type-safe-navigation)
2. [중첩 네비게이션 (Nested Navigation)](#중첩-네비게이션-nested-navigation)
3. [Bottom Navigation 완벽 구현](#bottom-navigation-완벽-구현)
4. [ViewModel 스코프 공유](#viewmodel-스코프-공유)
5. [Deep Link와 외부 연동](#deep-link와-외부-연동)

---

## Type Safe Navigation

Compose Navigation 2.8.0부터는 문자열(`"route/{arg}"`) 대신 **객체(Object/Class)**를 사용하여 경로를 정의하는 **Type Safe Navigation**을 공식 지원합니다.

### 🚫 기존 방식의 문제점
1. **오타 위험**: `"profile/{userId}"`를 `"profile/{userid}"`로 쓰면 런타임 에러 발생
2. **타입 불안정**: 모든 인자가 String으로 처리되어, Int/Boolean 변환이 번거로움
3. **유지보수 어려움**: 파라미터가 변경되면 모든 문자열을 찾아 고쳐야 함

### ✅ Type Safe 방식 (권장)

`kotlinx.serialization`을 사용하여 Route를 객체로 정의합니다.

#### 1. 의존성 설정 (libs.versions.toml)

```toml
[versions]
kotlin = "1.9.0"
serialization = "1.6.3"
navigation = "2.8.0"

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

#### 2. Route 정의 (Serializable)

```kotlin
import kotlinx.serialization.Serializable

/**
 * 화면 경로 정의
 * 
 * @Serializable: 이 객체를 직렬화하여 Route로 사용함을 의미
 */
object AppRoute {
    // 1. 인자가 없는 화면 (Object)
    @Serializable
    object Home

    // 2. 인자가 있는 화면 (Data Class)
    @Serializable
    data class Profile(val userId: String, val showDetails: Boolean = false)

    // 3. 복잡한 객체를 전달하는 화면
    @Serializable
    data class Search(val query: String, val filters: SearchFilters)
}

@Serializable
data class SearchFilters(val minPrice: Int, val maxPrice: Int)
```

#### 3. NavHost 구현

```kotlin
NavHost(navController = navController, startDestination = AppRoute.Home) {
    
    // 1. Home 화면
    composable<AppRoute.Home> {
        HomeScreen(
            onProfileClick = { userId ->
                // 객체를 생성하여 이동 (타입 안전!)
                navController.navigate(AppRoute.Profile(userId = userId))
            }
        )
    }

    // 2. Profile 화면
    composable<AppRoute.Profile> { backStackEntry ->
        // toRoute() 확장 함수로 인자를 바로 추출 (자동 파싱)
        val route: AppRoute.Profile = backStackEntry.toRoute()
        
        ProfileScreen(
            userId = route.userId,
            showDetails = route.showDetails
        )
    }
}
```

> [!TIP]
> **`toRoute()`의 마법**
> `backStackEntry.toRoute<T>()`를 호출하면, 내부적으로 URL 파싱, 타입 변환, Null 체크를 모두 수행하고 완벽한 객체 `T`를 반환합니다. 더 이상 `getString()`, `toInt()`를 할 필요가 없습니다!

---

## 중첩 네비게이션 (Nested Navigation)

앱이 커지면 수십 개의 화면을 하나의 `NavHost`에 다 넣을 수 없습니다. 기능별로 그래프를 쪼개서 관리해야 합니다.

### 구조 설계

```
RootGraph
├── AuthGraph (로그인, 회원가입, 비번찾기)
├── MainGraph (홈, 검색, 설정)
│   ├── HomeGraph
│   └── SettingGraph
└── OnboardingGraph
```

### 구현 코드

```kotlin
// 그래프 자체도 Serializable로 정의 가능
@Serializable object AuthGraph
@Serializable object MainGraph

NavHost(navController, startDestination = AuthGraph) {
    
    // 1. 인증 그래프 (로그인 관련)
    navigation<AuthGraph>(startDestination = AppRoute.Login) {
        composable<AppRoute.Login> { ... }
        composable<AppRoute.SignUp> { ... }
        composable<AppRoute.ForgotPassword> { ... }
    }

    // 2. 메인 그래프 (앱 진입 후)
    navigation<MainGraph>(startDestination = AppRoute.Home) {
        composable<AppRoute.Home> { ... }
        composable<AppRoute.Settings> { ... }
    }
}
```

### 실무 팁: 그래프 별 파일 분리

실무에서는 `NavGraphBuilder`의 확장 함수를 사용하여 파일을 분리합니다.

```kotlin
// AuthNavGraph.kt
fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation<AuthGraph>(startDestination = AppRoute.Login) {
        composable<AppRoute.Login> { 
            LoginScreen(onLoginSuccess = { 
                // 그래프 간 이동: Auth -> Main
                navController.navigate(MainGraph) {
                    // 백스택에서 AuthGraph 제거 (뒤로가기 방지)
                    popUpTo(AuthGraph) { inclusive = true }
                }
            }) 
        }
        // ...
    }
}

// MainActivity.kt
NavHost(...) {
    authNavGraph(navController)  // 깔끔하게 호출
    mainNavGraph(navController)
}
```

---

## Bottom Navigation 완벽 구현

Bottom Navigation은 단순해 보이지만, **상태 유지(State Preservation)**와 **Back Stack 관리**가 까다롭습니다.

### 1. Route 정의

```kotlin
@Serializable
sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: Any // Serializable 객체
) {
    object Home : BottomNavItem("홈", Icons.Default.Home, AppRoute.Home)
    object Search : BottomNavItem("검색", Icons.Default.Search, AppRoute.Search)
    object Profile : BottomNavItem("프로필", Icons.Default.Person, AppRoute.Profile("me"))
}
```

### 2. Scaffold와 연동

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    // 현재 보고 있는 화면의 Route를 감지
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // 하단 탭 리스트
    val items = listOf(BottomNavItem.Home, BottomNavItem.Search, BottomNavItem.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    // 현재 탭이 선택되었는지 확인 (계층 구조 지원)
                    val isSelected = currentDestination?.hierarchy?.any { 
                        it.hasRoute(item.route::class) 
                    } == true

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                // 1. 탭 클릭 시 시작점(Home)까지 백스택 정리 (쌓임 방지)
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true // 상태 저장 (스크롤 위치 등)
                                }
                                // 2. 중복 클릭 방지 (이미 해당 탭이면 리로드 안 함)
                                launchSingleTop = true
                                // 3. 탭 전환 시 상태 복원
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home,
            modifier = Modifier.padding(padding)
        ) {
            // ... 화면 정의 ...
        }
    }
}
```

> [!IMPORTANT]
> **`saveState`와 `restoreState`**
> 이 옵션들이 없으면 탭을 이동할 때마다 화면이 초기화됩니다(스크롤이 맨 위로 올라감). 사용자 경험을 위해 반드시 `true`로 설정하세요.

---

## ViewModel 스코프 공유

회원가입 프로세스(1단계 -> 2단계 -> 3단계)처럼 여러 화면이 하나의 ViewModel을 공유해야 할 때가 있습니다.

### Scoped ViewModel 구현

```kotlin
// 회원가입 그래프
navigation<AuthGraph>(startDestination = AppRoute.SignUpStep1) {
    
    // 1. 그래프 범위의 ViewModel 생성
    // 'it'은 NavBackStackEntry를 의미함
    composable<AppRoute.SignUpStep1> { entry ->
        // 부모 그래프(AuthGraph)의 BackStackEntry를 가져옴
        val parentEntry = remember(entry) {
            navController.getBackStackEntry(AuthGraph)
        }
        // 부모 Entry를 Owner로 하는 ViewModel 생성
        val sharedViewModel: SignUpViewModel = hiltViewModel(parentEntry)
        
        SignUpStep1Screen(viewModel = sharedViewModel)
    }

    composable<AppRoute.SignUpStep2> { entry ->
        val parentEntry = remember(entry) {
            navController.getBackStackEntry(AuthGraph)
        }
        // 위와 동일한 인스턴스를 반환받음 (데이터 공유!)
        val sharedViewModel: SignUpViewModel = hiltViewModel(parentEntry)
        
        SignUpStep2Screen(viewModel = sharedViewModel)
    }
}
```

이 방식을 사용하면 `AuthGraph`가 팝(Pop)될 때 ViewModel도 함께 소멸(`onCleared`)되어 메모리 누수를 방지할 수 있습니다.

---

## Deep Link와 외부 연동

외부 링크나 알림을 통해 앱의 특정 화면으로 바로 진입하는 기능입니다.

### 1. Route에 Deep Link 추가

```kotlin
composable<AppRoute.Profile>(
    deepLinks = listOf(
        navDeepLink<AppRoute.Profile>(
            basePath = "myapp://profile" 
            // 결과: myapp://profile/{userId}?showDetails={showDetails}
        )
    )
) { ... }
```

### 2. AndroidManifest.xml 설정

```xml
<activity ...>
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <!-- 스킴 설정 -->
        <data android:scheme="myapp" android:host="profile" />
    </intent-filter>
</activity>
```

### 3. 테스트 방법 (ADB)

터미널에서 다음 명령어로 딥링크를 테스트할 수 있습니다.

```bash
adb shell am start -W -a android.intent.action.VIEW -d "myapp://profile/user123?showDetails=true"
```

---

## 🎯 마무리

이제 여러분은 단순한 화면 이동을 넘어, **타입 안전성**, **모듈화**, **상태 유지**, **외부 연동**까지 고려한 견고한 네비게이션 구조를 설계할 수 있습니다.

### 핵심 요약
1. **Type Safe Navigation**을 기본으로 사용하세요. (`kotlinx.serialization`)
2. 기능 단위로 **Nested Graph**를 만들어 관리하세요.
3. Bottom Navigation 구현 시 **`saveState`, `restoreState`**를 잊지 마세요.
4. 여러 화면에서 데이터를 공유할 땐 **Graph Scoped ViewModel**을 사용하세요.

Happy Navigating! 🧭
