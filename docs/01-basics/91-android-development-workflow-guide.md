# 안드로이드 앱 개발 실전 워크플로우 (피그마 to 앱)

> 📖 **문서 개요**
>
> 이 문서는 디자인(Figma)을 전달받은 시점부터 실제 안드로이드 앱을 구현하는 과정을 단계별로 안내합니다. "Food Recipe App"을 예시로 들어, 초보자도 따라 할 수 있는 구체적인 액션 플랜을 제공합니다.

---

## 📚 전체 프로세스 요약

1.  **분석 (Analysis)**: 피그마 뜯어보기 (디자인 시스템, 화면 흐름)
2.  **설정 (Setup)**: 프로젝트 생성 및 기본 구조 잡기
3.  **기초 공사 (Foundation)**: 테마, 컬러, 폰트, 리소스 등록
4.  **UI 구현 (Implementation)**: 컴포넌트 → 화면 단위 개발
5.  **네비게이션 (Navigation)**: 화면 연결
6.  **데이터 (Data)**: 모델링 및 더미 데이터 연결
7.  **기능 연동 (Integration)**: ViewModel 및 로직 구현
8.  **꿀팁 (Pro Tips)**: 초보자가 자주 묻는 실무 디테일 🆕

---

## 1단계: 피그마 분석 (Design Analysis)

코드를 치기 전에 디자인을 분석하여 "무엇을 만들어야 하는지" 정의합니다.

### 1-1. 디자인 시스템 추출
피그마의 우측 패널(Inspect/Properties)을 보며 다음 정보를 수집합니다.

*   **Color Palette**: 주색상(Primary), 보조색상(Secondary), 배경색, 텍스트 색상.
    *   *예: Primary Orange (#FF6B00), Background White (#FFFFFF)*
*   **Typography**: 폰트 종류(Family), 크기(Size), 굵기(Weight).
    *   *예: Poppins (Bold 24sp, Medium 16sp, Regular 14sp)*
*   **Icons**: 필요한 아이콘을 SVG(Vector Drawable)로 추출(Export)합니다.
    *   *예: ic_search.svg, ic_home.svg, ic_bookmark.svg*

### 1-2. 화면 흐름(User Flow) 파악
어떤 화면들이 있고 어떻게 연결되는지 파악합니다.

*   **Splash**: 앱 로고 화면
*   **Onboarding**: 앱 소개 (로그인 전)
*   **Login/Sign up**: 회원가입/로그인
*   **Home**: 추천 레시피 목록, 카테고리
*   **Search**: 레시피 검색
*   **Detail**: 레시피 상세 정보 (재료, 조리법)
*   **Profile**: 내 정보, 저장한 레시피

### 1-3. 컴포넌트 분리 (Componentizing)
재사용 가능한 UI 요소를 식별합니다.

*   **공통 버튼**: 둥근 모서리, 주황색 배경
*   **레시피 카드**: 이미지 + 제목 + 평점 + 시간
*   **입력 필드**: 검색창, 이메일 입력창
*   **하단 네비게이션 바**: 탭 메뉴

---

## 2단계: 프로젝트 설정 (Project Setup)

### 2-1. 안드로이드 스튜디오 프로젝트 생성
*   **Template**: `Empty Activity` (Compose)
*   **Name**: `FoodRecipeApp`
*   **Package name**: `com.example.foodrecipe`
*   **Language**: Kotlin
*   **Build configuration language**: Kotlin DSL (Recommended)

### 2-2. 패키지 구조 잡기 (Feature-based + Clean Architecture)
`com.example.foodrecipe` 아래에 패키지를 생성합니다. 정석적인 구조는 다음과 같습니다.

```
├── core/                  # [공통 모듈]
│   ├── designsystem/      # 테마, 컬러, 폰트
│   ├── model/             # 전역 데이터 모델 (User)
│   ├── data/              # Repository 구현체 (UserRepository)
│   ├── network/           # DataSource: API (UserNetworkDataSource)
│   └── database/          # DataSource: DB (UserDao)
├── feature/               # [기능 모듈]
│   ├── home/
│   │   ├── ui/            # 화면 (HomeScreen, HomeViewModel)
│   │   └── data/          # (선택) 이 기능 전용 Repository, DataSource
│   ├── search/
│   ├── detail/
│   └── profile/
└── MainActivity.kt
```

### 📂 최종 폴더 구조 (Project View 기준)

안드로이드 스튜디오에서 보게 될 최종 구조입니다.

```text
app
 └── java
      └── com.example.foodrecipe
           ├── MainActivity.kt
           ├── core               <-- 공통 모듈
           │    ├── data          (Repository 폴더)
           │    ├── network       (API DataSource 폴더)
           │    ├── database      (DB DataSource 폴더)
           │    ├── model         (Data Model 폴더)
           │    └── designsystem  (Theme, Type, Color 폴더)
           └── feature            <-- 기능별 화면
                ├── home
                │    └── ui
                ├── detail
                │    └── ui
                └── search
                     └── ui
```

### 2-3. 의존성 추가 (build.gradle.kts)
필요한 라이브러리를 추가합니다.
*   **Navigation**: `androidx.navigation:navigation-compose`
*   **Image Loading**: `io.coil-kt:coil-compose`
*   **Icons**: `androidx.compose.material:material-icons-extended`

---

## 3단계: 디자인 시스템 구현 (Foundation)

`core/designsystem` 패키지에서 작업합니다.

### 3-1. Color.kt
피그마에서 추출한 색상을 정의합니다.

```kotlin
val OrangePrimary = Color(0xFFFF6B00)
val TextBlack = Color(0xFF121212)
val TextGray = Color(0xFFAAAAAA)
```

### 3-2. Type.kt
폰트 스타일을 정의합니다.

```kotlin
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    // ...
)
```

### 3-3. Theme.kt
앱 전체에 적용될 테마를 설정합니다.

---

## 4단계: UI 구현 (UI Implementation)

작은 단위(Atom)부터 큰 단위(Page) 순서로 만듭니다.

### 4-1. 공통 컴포넌트 (Components)
`core/designsystem/component`에 만듭니다.

*   `RecipeButton.kt`: 앱 전반에 쓰이는 버튼
*   `RecipeCard.kt`: 홈 화면 등에서 쓰이는 카드

```kotlin
@Composable
fun RecipeCard(
    title: String,
    imageUrl: String,
    rating: Float,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        // Image (Coil), Text, Icon 배치
    }
}
```

### 4-2. 화면 구현 (Screens)
`feature` 패키지별로 화면을 구현합니다. 처음에는 데이터 없이 **하드코딩**으로 UI만 그립니다.

*   `feature/home/ui/HomeScreen.kt`
*   `feature/detail/ui/DetailScreen.kt`

> 💡 **Tip**: `@Preview`를 적극 활용하여 에뮬레이터 실행 없이 UI를 확인하세요.

---

## 5단계: 네비게이션 연결 (Navigation)

화면 간 이동을 구현합니다.

### 5-1. Route 정의
어떤 화면으로 갈 수 있는지 정의합니다.

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{recipeId}") {
        fun createRoute(recipeId: String) = "detail/$recipeId"
    }
}
```

### 5-2. NavHost 설정
`MainActivity.kt` 또는 별도의 `AppNavHost.kt`에서 연결합니다.

```kotlin
NavHost(navController, startDestination = Screen.Home.route) {
    composable(Screen.Home.route) {
        HomeScreen(onRecipeClick = { id -> 
            navController.navigate(Screen.Detail.createRoute(id)) 
        })
    }
    composable(Screen.Detail.route) { backStackEntry ->
        val recipeId = backStackEntry.arguments?.getString("recipeId")
        DetailScreen(recipeId = recipeId)
    }
}
```

---

## 6단계: 데이터 레이어 및 로직 (Data & Logic)

실제 데이터가 흐르도록 만듭니다.

### 6-1. 모델 정의 (Model)
`core/model/Recipe.kt`

```kotlin
data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String,
    val ingredients: List<String>
)
```

### 6-2. Repository 및 Mock Data
서버 API가 아직 없다면 가짜 데이터(Mock Data)를 만들어 사용합니다.

```kotlin
object MockData {
    val recipes = listOf(
        Recipe("1", "파스타", "url...", listOf("면", "소스")),
        Recipe("2", "스테이크", "url...", listOf("고기", "소금"))
    )
}

class RecipeRepository {
    fun getRecipes(): List<Recipe> = MockData.recipes
    fun getRecipe(id: String): Recipe? = MockData.recipes.find { it.id == id }
}
```

### 6-3. ViewModel 연결
`feature/home/ui/HomeViewModel.kt`

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadRecipes()
    }
    // ...
}
```

### 6-4. (심화) DataSource와 Mapper 분리하기
앱이 커지면 **Repository** 하나에 모든 코드를 넣기 힘들어집니다. 이때 역할을 나눕니다.

*   **DataSource**: 실제 데이터(API, DB)를 가져오는 역할. (예: `RecipeNetworkDataSource`, `RecipeLocalDataSource`)
*   **Mapper**: 서버 데이터(DTO)를 앱 데이터(Domain Model)로 변환하는 역할.
*   **Repository**: DataSource에서 데이터를 가져와 Mapper로 변환한 뒤 ViewModel에 전달.

```kotlin
// 1. DataSource (서버 통신)
class RecipeDataSource(private val api: RecipeApi) {
    suspend fun fetchRecipes(): List<RecipeDto> = api.getRecipes()
}

// 2. Mapper (변환)
fun RecipeDto.toDomain(): Recipe {
    return Recipe(
        id = this.id,
        title = this.name, // 서버엔 name, 앱엔 title일 경우
        imageUrl = this.image_url
    )
}

// 3. Repository (중재)
class RecipeRepository(
    private val dataSource: RecipeDataSource
) {
    suspend fun getRecipes(): List<Recipe> {
        return dataSource.fetchRecipes().map { it.toDomain() }
    }
}
```

---

## 7단계: 마무리 및 다듬기 (Polish)

*   **Edge-to-Edge**: 상태바, 네비게이션바 영역까지 UI 확장 (`enableEdgeToEdge()`).
*   **다크 모드**: 다크 테마 색상 점검.
*   **접근성**: 이미지에 `contentDescription` 추가.
*   **테스트**: 기기에서 직접 실행하며 버그 찾기.

---

## 8단계: 놓치기 쉬운 실무 디테일 (Pro Tips)

개발하다 보면 반드시 마주치는 문제들입니다. 미리 알아두면 시간을 아낄 수 있습니다.

### 8-1. 라이브러리 추가는 어디에? (Version Catalog)
최신 안드로이드 프로젝트는 `build.gradle.kts`에 직접 버전을 적지 않고, `libs.versions.toml` 파일을 사용합니다.

1.  **파일 위치**: `gradle/libs.versions.toml`
2.  **작성 방법**:
    ```toml
    [versions]
    coil = "2.5.0"

    [libraries]
    coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
    ```
3.  **적용 방법**: `build.gradle.kts` (Module: app)
    ```kotlin
    dependencies {
        implementation(libs.coil.compose)
    }
    ```

### 8-2. 앱 이름과 아이콘 바꾸기
*   **앱 이름**: `res/values/strings.xml` 파일의 `app_name`을 수정하세요.
*   **앱 아이콘**:
    1.  `res` 폴더 우클릭 -> **New** -> **Image Asset** 선택.
    2.  준비한 로고 이미지를 선택하고 배경색을 조절합니다.
    3.  Next -> Finish를 누르면 다양한 해상도의 아이콘이 자동 생성됩니다.

### 8-3. 앱이 죽었을 때 (Logcat)
앱이 강제 종료되면 당황하지 말고 하단의 **Logcat** 탭을 엽니다.
1.  검색창에 `FATAL`이라고 입력합니다.
2.  빨간색 글씨 중 `Caused by:`로 시작하는 줄을 찾습니다.
3.  그 줄에 적힌 에러 메시지가 정답입니다. (예: `NullPointerException`, `NetworkOnMainThreadException`)

---

## 🚀 요약: 초보자를 위한 3줄 팁

1.  **디자인부터 분석해라**: 무작정 코드 치지 말고, 색상/폰트/컴포넌트부터 정리하세요.
2.  **작은 것부터 만들어라**: 버튼 하나, 카드 하나를 먼저 만들고 화면을 조립하세요.
3.  **데이터는 나중에**: UI 껍데기부터 완벽하게 만들고, 나중에 실제 데이터를 연결하세요.

---

**마지막 업데이트**: 2024-12-04
**작성자**: Antigravity AI Assistant
