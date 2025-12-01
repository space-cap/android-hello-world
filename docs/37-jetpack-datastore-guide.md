# Jetpack DataStore 완벽 가이드

## 📚 목차

1. [DataStore란?](#datastore란)
2. [Preferences DataStore](#preferences-datastore)
3. [Proto DataStore](#proto-datastore)
4. [마이그레이션](#마이그레이션)
5. [고급 사용법](#고급-사용법)
6. [실전 예제](#실전-예제)

---

## DataStore란?

> [!NOTE]
> **DataStore = SharedPreferences의 최신 대체품**
> 
> **주요 특징:**
> - 🔄 Kotlin Coroutines 기반
> - 🛡️ 타입 안전성
> - ⚡ 비동기 처리
> - 🔒 스레드 안전
> - 📊 Flow 지원

### SharedPreferences vs DataStore

**SharedPreferences (구식):**
```kotlin
// 동기 처리 (메인 스레드 블로킹)
val prefs = getSharedPreferences("settings", MODE_PRIVATE)
val name = prefs.getString("name", "")  // 메인 스레드에서 실행!

// 타입 안전성 없음
val age = prefs.getInt("age", 0)  // 키 오타 가능
```

**DataStore (최신):**
```kotlin
// 비동기 처리 (메인 스레드 안전)
val nameFlow: Flow<String> = dataStore.data
    .map { it[NAME_KEY] ?: "" }

// 타입 안전성
val NAME_KEY = stringPreferencesKey("name")  // 컴파일 타임 체크
```

**비교표:**

| 기능 | SharedPreferences | DataStore |
|------|-------------------|-----------|
| 동기/비동기 | 동기 (블로킹) | 비동기 (Non-blocking) |
| 타입 안전성 | ❌ 없음 | ✅ 있음 |
| 에러 처리 | ❌ 어려움 | ✅ 쉬움 |
| Flow 지원 | ❌ 없음 | ✅ 있음 |
| 스레드 안전 | ⚠️ 제한적 | ✅ 완전 |

---

## Preferences DataStore

> [!TIP]
> **Preferences DataStore = 키-값 쌍 저장 (SharedPreferences 대체)**
> 
> **사용 사례:**
> - 사용자 설정
> - 앱 환경설정
> - 간단한 데이터

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
dependencies {
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### DataStore 생성

```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

// Context 확장 프로퍼티로 DataStore 생성
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// 키 정의
object PreferencesKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_AGE = intPreferencesKey("user_age")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val THEME_MODE = stringPreferencesKey("theme_mode")  // "light", "dark", "system"
}
```

### 데이터 저장

```kotlin
class SettingsRepository(private val context: Context) {
    
    // 데이터 저장
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            // edit 블록 내에서 데이터 수정
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }
    
    // 여러 값 동시 저장
    suspend fun saveUserInfo(name: String, age: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_AGE] = age
        }
    }
    
    // 로그인 상태 저장
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }
}
```

### 데이터 읽기

```kotlin
class SettingsRepository(private val context: Context) {
    
    // Flow로 데이터 읽기 (실시간 업데이트)
    val userNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // 기본값 제공
            preferences[PreferencesKeys.USER_NAME] ?: ""
        }
    
    // 여러 값 읽기
    val userInfoFlow: Flow<UserInfo> = context.dataStore.data
        .map { preferences ->
            UserInfo(
                name = preferences[PreferencesKeys.USER_NAME] ?: "",
                age = preferences[PreferencesKeys.USER_AGE] ?: 0
            )
        }
    
    // 단일 값 읽기 (suspend 함수)
    suspend fun getUserName(): String {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.USER_NAME] ?: ""
    }
    
    // 로그인 상태 확인
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }
}

data class UserInfo(
    val name: String,
    val age: Int
)
```

### Compose에서 사용

```kotlin
@Composable
fun SettingsScreen(
    repository: SettingsRepository = remember { SettingsRepository(LocalContext.current) }
) {
    // Flow를 State로 변환
    val userName by repository.userNameFlow.collectAsState(initial = "")
    val isLoggedIn by repository.isLoggedInFlow.collectAsState(initial = false)
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 사용자 이름 표시
        Text(
            text = "사용자: $userName",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 이름 변경 버튼
        OutlinedTextField(
            value = userName,
            onValueChange = { newName ->
                // 코루틴에서 저장
                scope.launch {
                    repository.saveUserName(newName)
                }
            },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 로그인 상태 토글
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("로그인 상태")
            Switch(
                checked = isLoggedIn,
                onCheckedChange = { checked ->
                    scope.launch {
                        repository.setLoggedIn(checked)
                    }
                }
            )
        }
    }
}
```

### 데이터 삭제

```kotlin
class SettingsRepository(private val context: Context) {
    
    // 특정 키 삭제
    suspend fun deleteUserName() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_NAME)
        }
    }
    
    // 모든 데이터 삭제 (초기화)
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
```

---

## Proto DataStore

> [!IMPORTANT]
> **Proto DataStore = 타입 안전한 객체 저장 (Protocol Buffers 사용)**
> 
> **장점:**
> - ✅ 완전한 타입 안전성
> - ✅ 복잡한 데이터 구조
> - ✅ 스키마 정의
> - ✅ 버전 관리

### 의존성 추가

```kotlin
// build.gradle.kts (Module: app)
plugins {
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    // Proto DataStore
    implementation("androidx.datastore:datastore:1.0.0")
    
    // Protocol Buffers
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
}

// Protobuf 설정
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
```

### Proto 스키마 정의

```protobuf
// app/src/main/proto/user_settings.proto
syntax = "proto3";

option java_package = "com.example.myapp";
option java_multiple_files = true;

message UserSettings {
  string name = 1;
  int32 age = 2;
  bool is_logged_in = 3;
  
  enum ThemeMode {
    LIGHT = 0;
    DARK = 1;
    SYSTEM = 2;
  }
  
  ThemeMode theme_mode = 4;
  
  repeated string favorite_items = 5;  // 리스트
}
```

### Serializer 생성

```kotlin
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

object UserSettingsSerializer : Serializer<UserSettings> {
    
    // 기본값
    override val defaultValue: UserSettings = UserSettings.getDefaultInstance()
    
    // 읽기
    override suspend fun readFrom(input: InputStream): UserSettings {
        try {
            return UserSettings.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }
    
    // 쓰기
    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        t.writeTo(output)
    }
}
```

### Proto DataStore 생성

```kotlin
val Context.userSettingsDataStore: DataStore<UserSettings> by dataStore(
    fileName = "user_settings.pb",
    serializer = UserSettingsSerializer
)
```

### 데이터 저장 및 읽기

```kotlin
class UserSettingsRepository(private val context: Context) {
    
    // 데이터 읽기
    val userSettingsFlow: Flow<UserSettings> = context.userSettingsDataStore.data
    
    // 이름 저장
    suspend fun updateUserName(name: String) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setName(name)
                .build()
        }
    }
    
    // 여러 필드 업데이트
    suspend fun updateUserInfo(name: String, age: Int) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setName(name)
                .setAge(age)
                .build()
        }
    }
    
    // 테마 변경
    suspend fun updateThemeMode(mode: UserSettings.ThemeMode) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setThemeMode(mode)
                .build()
        }
    }
    
    // 리스트 추가
    suspend fun addFavoriteItem(item: String) {
        context.userSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .addFavoriteItems(item)
                .build()
        }
    }
}
```

### Compose에서 사용

```kotlin
@Composable
fun ProtoDataStoreExample(
    repository: UserSettingsRepository = remember { 
        UserSettingsRepository(LocalContext.current) 
    }
) {
    val userSettings by repository.userSettingsFlow.collectAsState(
        initial = UserSettings.getDefaultInstance()
    )
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "이름: ${userSettings.name}",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "나이: ${userSettings.age}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "테마: ${userSettings.themeMode}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                scope.launch {
                    repository.updateUserInfo("홍길동", 25)
                }
            }
        ) {
            Text("정보 업데이트")
        }
    }
}
```

---

## 마이그레이션

### SharedPreferences → DataStore

```kotlin
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView

// 마이그레이션 정의
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "old_prefs",  // 기존 SharedPreferences 이름
                keysToMigrate = setOf("user_name", "user_age")  // 마이그레이션할 키
            )
        )
    }
)

// 커스텀 마이그레이션
val Context.dataStoreWithCustomMigration: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            object : DataMigration<Preferences> {
                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    // 마이그레이션 필요 여부 확인
                    return currentData[PreferencesKeys.USER_NAME] == null
                }
                
                override suspend fun migrate(currentData: Preferences): Preferences {
                    // 마이그레이션 로직
                    val prefs = context.getSharedPreferences("old_prefs", MODE_PRIVATE)
                    val name = prefs.getString("user_name", "") ?: ""
                    
                    return currentData.toMutablePreferences().apply {
                        this[PreferencesKeys.USER_NAME] = name
                    }.toPreferences()
                }
                
                override suspend fun cleanUp() {
                    // 마이그레이션 후 정리
                    context.getSharedPreferences("old_prefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                }
            }
        )
    }
)
```

---

## 고급 사용법

### 에러 처리

```kotlin
class SettingsRepository(private val context: Context) {
    
    val userNameFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            // 에러 처리
            if (exception is IOException) {
                Log.e("DataStore", "Error reading preferences", exception)
                emit(emptyPreferences())  // 빈 Preferences 반환
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: ""
        }
}
```

### 복잡한 데이터 변환

```kotlin
data class AppSettings(
    val userName: String,
    val themeMode: ThemeMode,
    val notificationsEnabled: Boolean,
    val fontSize: Float
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class SettingsRepository(private val context: Context) {
    
    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            AppSettings(
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                themeMode = when (preferences[PreferencesKeys.THEME_MODE]) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                },
                notificationsEnabled = preferences[booleanPreferencesKey("notifications")] ?: true,
                fontSize = preferences[floatPreferencesKey("font_size")] ?: 16f
            )
        }
    
    suspend fun updateAppSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = settings.userName
            preferences[PreferencesKeys.THEME_MODE] = when (settings.themeMode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
            preferences[booleanPreferencesKey("notifications")] = settings.notificationsEnabled
            preferences[floatPreferencesKey("font_size")] = settings.fontSize
        }
    }
}
```

---

## 실전 예제

### 완전한 설정 시스템

```kotlin
// Repository
class SettingsRepository(private val context: Context) {
    
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LANGUAGE = stringPreferencesKey("language")
    }
    
    val settingsFlow: Flow<Settings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            Settings(
                themeMode = preferences[Keys.THEME_MODE] ?: "system",
                notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true,
                fontSize = preferences[Keys.FONT_SIZE] ?: 16f,
                language = preferences[Keys.LANGUAGE] ?: "ko"
            )
        }
    
    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }
    
    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
    
    suspend fun updateFontSize(size: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size }
    }
    
    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }
}

data class Settings(
    val themeMode: String,
    val notificationsEnabled: Boolean,
    val fontSize: Float,
    val language: String
)

// UI
@Composable
fun SettingsScreen(
    repository: SettingsRepository = remember { SettingsRepository(LocalContext.current) }
) {
    val settings by repository.settingsFlow.collectAsState(
        initial = Settings("system", true, 16f, "ko")
    )
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "설정",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        // 테마 설정
        item {
            SettingItem(
                title = "테마",
                subtitle = when (settings.themeMode) {
                    "light" -> "라이트 모드"
                    "dark" -> "다크 모드"
                    else -> "시스템 설정"
                }
            ) {
                // 테마 선택 다이얼로그
            }
        }
        
        // 알림 설정
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("알림", style = MaterialTheme.typography.titleMedium)
                    Text("푸시 알림 받기", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            repository.updateNotifications(enabled)
                        }
                    }
                )
            }
        }
        
        // 폰트 크기
        item {
            Column {
                Text("폰트 크기", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = settings.fontSize,
                    onValueChange = { size ->
                        scope.launch {
                            repository.updateFontSize(size)
                        }
                    },
                    valueRange = 12f..24f,
                    steps = 11
                )
                Text("${settings.fontSize.toInt()}sp")
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. Repository 패턴 사용

```kotlin
// ✅ Repository로 캡슐화
class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore
    // ...
}

// ❌ 직접 접근
context.dataStore.edit { ... }
```

### 2. Flow 활용

```kotlin
// ✅ Flow로 실시간 업데이트
val settingsFlow: Flow<Settings> = dataStore.data.map { ... }

// ❌ 매번 읽기
suspend fun getSettings(): Settings { ... }
```

### 3. 에러 처리

```kotlin
// ✅ catch로 에러 처리
dataStore.data
    .catch { emit(emptyPreferences()) }
    .map { ... }
```

---

**마지막 업데이트**: 2025-12-01  
**작성자**: Antigravity AI Assistant

Store Data Safely! 💾
