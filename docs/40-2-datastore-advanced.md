# DataStore 고급

> 📖 **시리즈 구성**
> - **40-1**: [DataStore 기초](./40-1-datastore-basics.md)
> - **40-2**: DataStore 고급 (현재 문서)

---

## 📚 목차

1. [마이그레이션](#마이그레이션)
2. [에러 처리](#에러-처리)
3. [테스팅](#테스팅)
4. [베스트 프랙티스](#베스트-프랙티스)

---

## 마이그레이션

### SharedPreferences에서 마이그레이션

```kotlin
/**
 * SharedPreferences → DataStore 마이그레이션
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "old_prefs"
            )
        )
    }
)
```

---

## 에러 처리

### 안전한 읽기

```kotlin
/**
 * 에러 처리가 포함된 읽기
 */
val theme: Flow<String> = dataStore.data
    .catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
    .map { prefs ->
        prefs[THEME_KEY] ?: "light"
    }
```

---

## 테스팅

### DataStore 테스트

```kotlin
/**
 * 테스트용 DataStore
 */
@Test
fun testDataStore() = runTest {
    val testDataStore = PreferenceDataStoreFactory.create(
        scope = TestScope(UnconfinedTestDispatcher())
    )
    
    // 테스트 로직
}
```

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant
