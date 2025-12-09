# Android DataStore 가이드

## 목차
1. [DataStore란?](#datastore란)
2. [Preferences DataStore](#preferences-datastore)
3. [Proto DataStore](#proto-datastore)
4. [마이그레이션](#마이그레이션)
5. [실전 예제](#실전-예제)
6. [Jetpack Compose 통합](#jetpack-compose-통합)
7. [문제 해결](#문제-해결)

---

## DataStore란?

**DataStore**는 SharedPreferences를 대체하는 최신 데이터 저장 솔루션입니다.

### 특징
- 🔄 **비동기**: Coroutines/Flow 기반
- 🛡️ **타입 안전**: Proto DataStore
- ⚡ **성능**: 효율적인 I/O
- 🔒 **트랜잭션**: 원자적 업데이트

### SharedPreferences vs DataStore

| 기능 | SharedPreferences | DataStore |
|------|-------------------|-----------|
| 비동기 | ❌ | ✅ |
| 타입 안전 | ❌ | ✅ (Proto) |
| 에러 처리 | ❌ | ✅ |
| 트랜잭션 | ❌ | ✅ |

---

## Preferences DataStore

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### 기본 사용법

```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore 확장 프로퍼티
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Settings Manager
 */
class SettingsManager(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    companion object {
        // 키 정의
        val THEME_KEY = stringPreferencesKey("theme")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val FONT_SIZE_KEY = intPreferencesKey("font_size")
    }
    
    /**
     * 테마 저장
     */
    suspend fun saveTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
    
    /**
     * 테마 읽기
     */
    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "light"
    }
    
    /**
     * 알림 설정 저장
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 알림 설정 읽기
     */
    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }
    
    /**
     * 폰트 크기 저장
     */
    suspend fun saveFontSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size
        }
    }
    
    /**
     * 폰트 크기 읽기
     */
    val fontSizeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FONT_SIZE_KEY] ?: 14
    }
    
    /**
     * 모든 설정 초기화
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
```

---

## Proto DataStore

### 의존성 추가

**build.gradle.kts**:
```kotlin
plugins {
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    // Proto DataStore
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
}

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

### Proto 파일 정의

**app/src/main/proto/user_preferences.proto**:
```protobuf
syntax = "proto3";

option java_package = "com.example.app";
option java_multiple_files = true;

message UserPreferences {
  string theme = 1;
  bool notifications_enabled = 2;
  int32 font_size = 3;
  string language = 4;
}
```

### Serializer 구현

```kotlin
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * Proto Serializer
 */
object UserPreferencesSerializer : Serializer<UserPreferences> {
    
    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()
    
    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            UserPreferences.parseFrom(input)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}
```

### Proto DataStore 사용

```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

/**
 * Proto DataStore 확장 프로퍼티
 */
val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer
)

/**
 * Proto Settings Manager
 */
class ProtoSettingsManager(private val context: Context) {
    
    private val dataStore = context.userPreferencesStore
    
    /**
     * 테마 저장
     */
    suspend fun saveTheme(theme: String) {
        dataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder()
                .setTheme(theme)
                .build()
        }
    }
    
    /**
     * 설정 읽기
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
    
    /**
     * 여러 설정 한 번에 업데이트
     */
    suspend fun updateSettings(
        theme: String? = null,
        notificationsEnabled: Boolean? = null,
        fontSize: Int? = null
    ) {
        dataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().apply {
                theme?.let { setTheme(it) }
                notificationsEnabled?.let { setNotificationsEnabled(it) }
                fontSize?.let { setFontSize(it) }
            }.build()
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

/**
 * SharedPreferences 마이그레이션
 */
val Context.migratedDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "old_prefs",
                migrate = { sharedPrefs: SharedPreferencesView, currentData: Preferences ->
                    val mutablePreferences = currentData.toMutablePreferences()
                    
                    // 기존 데이터 마이그레이션
                    if (currentData[THEME_KEY] == null) {
                        mutablePreferences[THEME_KEY] = sharedPrefs.getString("theme", "light") ?: "light"
                    }
                    
                    if (currentData[NOTIFICATIONS_ENABLED_KEY] == null) {
                        mutablePreferences[NOTIFICATIONS_ENABLED_KEY] = sharedPrefs.getBoolean("notifications", true)
                    }
                    
                    mutablePreferences.toPreferences()
                }
            )
        )
    }
)
```

---

## Jetpack Compose 통합

```kotlin
/**
 * Compose에서 DataStore 사용
 */
@Composable
fun SettingsScreen(settingsManager: SettingsManager = SettingsManager(LocalContext.current)) {
    val theme by settingsManager.themeFlow.collectAsState(initial = "light")
    val notificationsEnabled by settingsManager.notificationsEnabledFlow.collectAsState(initial = true)
    val fontSize by settingsManager.fontSizeFlow.collectAsState(initial = 14)
    
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.padding(16.dp)) {
        // 테마 선택
        Text("테마: $theme")
        Row {
            Button(onClick = {
                scope.launch { settingsManager.saveTheme("light") }
            }) {
                Text("라이트")
            }
            
            Button(onClick = {
                scope.launch { settingsManager.saveTheme("dark") }
            }) {
                Text("다크")
            }
        }
        
        // 알림 토글
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("알림")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { enabled ->
                    scope.launch { settingsManager.setNotificationsEnabled(enabled) }
                }
            )
        }
        
        // 폰트 크기
        Text("폰트 크기: $fontSize")
        Slider(
            value = fontSize.toFloat(),
            onValueChange = { size ->
                scope.launch { settingsManager.saveFontSize(size.toInt()) }
            },
            valueRange = 12f..24f
        )
    }
}
```

---

## 베스트 프랙티스

### DO's ✅

```kotlin
// 1. Flow 사용
val themeFlow: Flow<String> = dataStore.data.map { it[THEME_KEY] ?: "light" }

// 2. 트랜잭션 사용
dataStore.updateData { preferences ->
    preferences.toBuilder()
        .setTheme("dark")
        .setFontSize(16)
        .build()
}

// 3. 에러 처리
val themeFlow = dataStore.data
    .catch { exception ->
        emit(emptyPreferences())
    }
    .map { it[THEME_KEY] ?: "light" }
```

### DON'Ts ❌

```kotlin
// 1. 메인 스레드에서 읽기
val theme = runBlocking { dataStore.data.first()[THEME_KEY] }  // ❌

// 2. SharedPreferences와 혼용
// ❌ 마이그레이션 후 SharedPreferences 계속 사용

// 3. 대용량 데이터 저장
// ❌ DataStore는 작은 데이터용 (큰 데이터는 Room 사용)
```

---

## 참고 자료

- [DataStore 공식 문서](https://developer.android.com/topic/libraries/architecture/datastore)
- [Proto DataStore](https://developer.android.com/codelabs/android-proto-datastore)
