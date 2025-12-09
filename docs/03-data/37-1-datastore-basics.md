# DataStore 기초

> 📖 **시리즈 구성**
> - **40-1**: DataStore 기초 (현재 문서)
> - **40-2**: [DataStore 고급](./40-2-datastore-advanced.md)

---

## 📚 목차

1. [DataStore 개요](#datastore-개요)
2. [Preferences DataStore](#preferences-datastore)
3. [Proto DataStore](#proto-datastore)
4. [실전 예제](#실전-예제)

---

## DataStore 개요

### SharedPreferences vs DataStore

```kotlin
/**
 * SharedPreferences의 문제점
 */
// ❌ 동기 API (메인 스레드 블로킹)
val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
val value = prefs.getString("key", "default")  // 메인 스레드에서 I/O!

/**
 * DataStore의 장점
 */
// ✅ 비동기 API (코루틴 기반)
val dataStore = context.dataStore
val value = dataStore.data.first()["key"]  // suspend 함수
```

---

## Preferences DataStore

### 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}

/**
 * DataStore 인스턴스 생성
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

### 기본 사용법

```kotlin
/**
 * 데이터 저장
 */
suspend fun saveData(context: Context, value: String) {
    val KEY = stringPreferencesKey("my_key")
    
    context.dataStore.edit { preferences ->
        preferences[KEY] = value
    }
}

/**
 * 데이터 읽기
 */
suspend fun readData(context: Context): String? {
    val KEY = stringPreferencesKey("my_key")
    
    val preferences = context.dataStore.data.first()
    return preferences[KEY]
}
```

---

## Proto DataStore

### Proto 정의

```protobuf
// user_prefs.proto
syntax = "proto3";

option java_package = "com.example.app";
option java_multiple_files = true;

message UserPreferences {
  string username = 1;
  int32 age = 2;
  bool is_premium = 3;
}
```

### 사용법

```kotlin
/**
 * Proto DataStore 생성
 */
val Context.userPrefsDataStore: DataStore<UserPreferences> by dataStore(
    fileName = "user_prefs.pb",
    serializer = UserPreferencesSerializer
)

/**
 * 데이터 저장
 */
suspend fun saveUserPrefs(context: Context, username: String, age: Int) {
    context.userPrefsDataStore.updateData { prefs ->
        prefs.toBuilder()
            .setUsername(username)
            .setAge(age)
            .build()
    }
}
```

---

## 실전 예제

### 설정 관리

```kotlin
/**
 * 앱 설정 관리
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
    }
    
    val theme: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.THEME] ?: "light"
    }
    
    suspend fun setTheme(theme: String) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
