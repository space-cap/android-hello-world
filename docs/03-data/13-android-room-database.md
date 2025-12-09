# Android Room Database 가이드

## 📚 목차

1. [Room 소개](#room-소개)
2. [프로젝트 설정](#프로젝트-설정)
3. [Entity 정의](#entity-정의)
4. [DAO 생성](#dao-생성)
5. [Database 클래스](#database-클래스)
6. [CRUD 작업](#crud-작업)
7. [관계 처리](#관계-처리)
8. [Migration](#migration)
9. [실전 예제](#실전-예제)

---

## Room 소개

**Room**은 SQLite 위에 구축된 Android의 공식 데이터베이스 라이브러리입니다.

### 주요 특징

- ✅ **컴파일 타임 검증**: SQL 쿼리 오류를 컴파일 시점에 발견
- ✅ **보일러플레이트 감소**: 자동 코드 생성
- ✅ **LiveData/Flow 지원**: 반응형 데이터
- ✅ **Migration 지원**: 데이터베이스 버전 관리
- ✅ **테스트 용이**: 인메모리 데이터베이스 지원

### Room의 3가지 주요 컴포넌트

1. **Entity**: 데이터베이스 테이블
2. **DAO** (Data Access Object): 데이터베이스 작업 메서드
3. **Database**: 데이터베이스 홀더

---

## 프로젝트 설정

### 의존성 추가

`build.gradle.kts` (Module: app):

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

dependencies {
    val roomVersion = "2.6.1"
    
    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}
```

`build.gradle.kts` (Project):

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

---

## Entity 정의

### 기본 Entity

```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val age: Int
)
```

### 컬럼 이름 커스터마이징

```kotlin
import androidx.room.ColumnInfo

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_name")
    val name: String,
    
    @ColumnInfo(name = "user_email")
    val email: String,
    
    val age: Int
)
```

### 필드 무시

```kotlin
import androidx.room.Ignore

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    
    @Ignore
    val profileImage: Bitmap? = null // DB에 저장되지 않음
)
```

### 복합 기본 키

```kotlin
@Entity(primaryKeys = ["firstName", "lastName"])
data class User(
    val firstName: String,
    val lastName: String,
    val age: Int
)
```

### 인덱스 추가

```kotlin
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String
)
```

### 타임스탬프

```kotlin
@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## DAO 생성

### 기본 DAO

```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
    
    @Insert
    suspend fun insertAll(users: List<User>)
    
    // 업데이트
    @Update
    suspend fun update(user: User)
    
    // 삭제
    @Delete
    suspend fun delete(user: User)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Int)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
    
    // 조회
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
    
    @Query("SELECT * FROM users WHERE name LIKE :searchQuery")
    fun searchUsers(searchQuery: String): Flow<List<User>>
    
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getUsersSortedByName(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE age >= :minAge")
    fun getUsersOlderThan(minAge: Int): Flow<List<User>>
}
```

### 복잡한 쿼리

```kotlin
@Dao
interface UserDao {
    // 조건부 정렬
    @Query("""
        SELECT * FROM users 
        WHERE age >= :minAge 
        ORDER BY 
            CASE WHEN :sortByName = 1 THEN name END ASC,
            CASE WHEN :sortByAge = 1 THEN age END DESC
    """)
    fun getFilteredUsers(
        minAge: Int,
        sortByName: Boolean,
        sortByAge: Boolean
    ): Flow<List<User>>
    
    // 집계 함수
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT AVG(age) FROM users")
    suspend fun getAverageAge(): Double
    
    @Query("SELECT * FROM users LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaginated(limit: Int, offset: Int): List<User>
}
```

### Transaction

```kotlin
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Insert
    suspend fun insertProfile(profile: UserProfile)
    
    @Transaction
    suspend fun insertUserWithProfile(user: User, profile: UserProfile) {
        val userId = insertUser(user)
        insertProfile(profile.copy(userId = userId.toInt()))
    }
}
```

---

## Database 클래스

### 기본 Database

```kotlin
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

### Database 인스턴스 생성

```kotlin
import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null
    
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
```

### 여러 Entity가 있는 Database

```kotlin
@Database(
    entities = [
        User::class,
        Note::class,
        Category::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
}
```

---

## CRUD 작업

### Repository 패턴

```kotlin
class UserRepository(private val userDao: UserDao) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    
    suspend fun insert(user: User) {
        userDao.insert(user)
    }
    
    suspend fun update(user: User) {
        userDao.update(user)
    }
    
    suspend fun delete(user: User) {
        userDao.delete(user)
    }
    
    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }
    
    fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers("%$query%")
    }
}
```

### ViewModel

```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun addUser(name: String, email: String, age: Int) {
        viewModelScope.launch {
            val user = User(
                name = name,
                email = email,
                age = age
            )
            repository.insert(user)
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.update(user)
        }
    }
    
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.delete(user)
        }
    }
}
```

### Compose UI

```kotlin
@Composable
fun UserListScreen(
    viewModel: UserViewModel = viewModel()
) {
    val users by viewModel.allUsers.collectAsState()
    
    Column {
        LazyColumn {
            items(users) { user ->
                UserItem(
                    user = user,
                    onDelete = { viewModel.deleteUser(user) }
                )
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "나이: ${user.age}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "삭제"
                )
            }
        }
    }
}
```

---

## 관계 처리

### 일대다 관계 (One-to-Many)

```kotlin
// 부모 Entity
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

// 자식 Entity
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val categoryId: Int
)

// 관계 데이터 클래스
data class CategoryWithNotes(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val notes: List<Note>
)

// DAO
@Dao
interface CategoryDao {
    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithNotes(): Flow<List<CategoryWithNotes>>
    
    @Transaction
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryWithNotes(categoryId: Int): CategoryWithNotes?
}
```

### 다대다 관계 (Many-to-Many)

```kotlin
// Entity 1
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

// Entity 2
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

// 교차 참조 테이블
@Entity(
    tableName = "student_course_cross_ref",
    primaryKeys = ["studentId", "courseId"],
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudentCourseCrossRef(
    val studentId: Int,
    val courseId: Int
)

// 관계 데이터 클래스
data class StudentWithCourses(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            StudentCourseCrossRef::class,
            parentColumn = "studentId",
            entityColumn = "courseId"
        )
    )
    val courses: List<Course>
)

data class CourseWithStudents(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            StudentCourseCrossRef::class,
            parentColumn = "courseId",
            entityColumn = "studentId"
        )
    )
    val students: List<Student>
)
```

---

## Migration

### 버전 업그레이드

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 새 컬럼 추가
        database.execSQL(
            "ALTER TABLE users ADD COLUMN phone TEXT DEFAULT '' NOT NULL"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 새 테이블 생성
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """)
    }
}

// Database 생성 시 Migration 추가
val database = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app_database"
)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

### 파괴적 Migration

```kotlin
// 개발 중에만 사용! 모든 데이터 삭제됨
val database = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app_database"
)
    .fallbackToDestructiveMigration()
    .build()
```

---

## 실전 예제

### Todo 앱

```kotlin
// Entity
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class Priority {
    LOW, MEDIUM, HIGH
}

// DAO
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC")
    fun getActiveTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 1")
    fun getCompletedTodos(): Flow<List<Todo>>
    
    @Insert
    suspend fun insert(todo: Todo)
    
    @Update
    suspend fun update(todo: Todo)
    
    @Delete
    suspend fun delete(todo: Todo)
    
    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :todoId")
    suspend fun updateCompleted(todoId: Int, isCompleted: Boolean)
}

// Database
@Database(entities = [Todo::class], version = 1)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}

// Type Converter (Enum 저장용)
class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
}

// Repository
class TodoRepository(private val todoDao: TodoDao) {
    val allTodos = todoDao.getAllTodos()
    val activeTodos = todoDao.getActiveTodos()
    val completedTodos = todoDao.getCompletedTodos()
    
    suspend fun insert(todo: Todo) = todoDao.insert(todo)
    suspend fun update(todo: Todo) = todoDao.update(todo)
    suspend fun delete(todo: Todo) = todoDao.delete(todo)
    suspend fun toggleCompleted(todoId: Int, isCompleted: Boolean) {
        todoDao.updateCompleted(todoId, isCompleted)
    }
}

// ViewModel
class TodoViewModel(
    private val repository: TodoRepository
) : ViewModel() {
    
    val allTodos = repository.allTodos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun addTodo(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            repository.insert(
                Todo(
                    title = title,
                    description = description,
                    priority = priority
                )
            )
        }
    }
    
    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            repository.toggleCompleted(todo.id, !todo.isCompleted)
        }
    }
    
    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }
}

// UI
@Composable
fun TodoListScreen(viewModel: TodoViewModel = viewModel()) {
    val todos by viewModel.allTodos.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 새 Todo 추가 화면으로 */ }
            ) {
                Icon(Icons.Filled.Add, "추가")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    onToggle = { viewModel.toggleTodo(todo) },
                    onDelete = { viewModel.deleteTodo(todo) }
                )
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.isCompleted) {
                        TextDecoration.LineThrough
                    } else null
                )
                Text(
                    text = todo.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "우선순위: ${todo.priority}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (todo.priority) {
                        Priority.HIGH -> Color.Red
                        Priority.MEDIUM -> Color.Orange
                        Priority.LOW -> Color.Green
                    }
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "삭제")
            }
        }
    }
}
```

---

## 💡 베스트 프랙티스

### 1. Repository 패턴 사용

```kotlin
// ✅ 좋은 예: Repository로 분리
class UserRepository(private val userDao: UserDao)

// ❌ 나쁜 예: ViewModel에서 직접 DAO 사용
class UserViewModel(private val userDao: UserDao)
```

### 2. Flow 사용

```kotlin
// ✅ 반응형 데이터
@Query("SELECT * FROM users")
fun getAllUsers(): Flow<List<User>>

// ❌ 일회성 조회만 가능
@Query("SELECT * FROM users")
suspend fun getAllUsers(): List<User>
```

### 3. suspend 함수 사용

```kotlin
// ✅ Coroutine 사용
@Insert
suspend fun insert(user: User)

// ❌ 메인 스레드 블로킹
@Insert
fun insert(user: User)
```

### 4. Transaction 활용

```kotlin
// ✅ 여러 작업을 하나의 트랜잭션으로
@Transaction
suspend fun insertUserWithProfile(user: User, profile: Profile)
```

### 5. 인덱스 추가

```kotlin
// ✅ 자주 검색하는 컬럼에 인덱스
@Entity(indices = [Index(value = ["email"])])
```

---

## 🎯 다음 단계

Room Database를 마스터했습니다! 다음으로:

1. **권한 관리** - 런타임 권한 처리
2. **테스팅** - Unit Test, UI Test
3. **디버깅** - 문제 해결 기법
4. **앱 배포** - Google Play 배포

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
