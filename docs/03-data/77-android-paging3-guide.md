# Android Paging3 가이드

## 목차
1. [Paging3란?](#paging3란)
2. [기본 구성](#기본-구성)
3. [PagingSource](#pagingsource)
4. [RemoteMediator](#remotemediator)
5. [Paging Data 표시](#paging-data-표시)
6. [로딩 상태](#로딩-상태)
7. [에러 처리](#에러-처리)
8. [실전 예제](#실전-예제)
9. [Jetpack Compose 통합](#jetpack-compose-통합)
10. [문제 해결](#문제-해결)

---

## Paging3란?

**Paging3**는 대용량 데이터를 효율적으로 로드하고 표시하기 위한 Jetpack 라이브러리입니다.

### 특징
- 📜 **무한 스크롤**: 자동 페이징
- 💾 **메모리 효율**: 필요한 데이터만 로드
- 🔄 **자동 재시도**: 네트워크 오류 시
- 🗄️ **캐싱**: Room과 통합

### 사용 사례
- 📱 **소셜 피드**: 무한 스크롤
- 🛒 **상품 목록**: 대량 데이터
- 📰 **뉴스 앱**: 기사 목록
- 💬 **채팅**: 메시지 히스토리

---

## 기본 구성

### 의존성 추가

**build.gradle.kts**:
```kotlin
dependencies {
    // Paging3
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    
    // Compose 통합
    implementation("androidx.paging:paging-compose:3.2.1")
}
```

### 아키텍처

```
Repository → PagingSource → Pager → Flow<PagingData> → UI
```

---

## PagingSource

### 기본 PagingSource

```kotlin
import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 네트워크 PagingSource
 */
class UserPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, User>() {
    
    /**
     * 데이터 로드
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            // 현재 페이지 (null이면 첫 페이지)
            val page = params.key ?: 1
            
            // API 호출
            val response = apiService.getUsers(
                page = page,
                pageSize = params.loadSize
            )
            
            // 성공
            LoadResult.Page(
                data = response.users,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.users.isEmpty()) null else page + 1
            )
            
        } catch (e: Exception) {
            // 실패
            LoadResult.Error(e)
        }
    }
    
    /**
     * 새로고침 시 키 반환
     */
    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

data class User(
    val id: String,
    val name: String,
    val email: String
)
```

### Repository

```kotlin
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/**
 * Repository
 */
class UserRepository(private val apiService: ApiService) {
    
    /**
     * Paging Data Flow
     */
    fun getUsersPagingData(): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,  // 페이지 크기
                enablePlaceholders = false,  // Placeholder 사용 안 함
                initialLoadSize = 20  // 초기 로드 크기
            ),
            pagingSourceFactory = { UserPagingSource(apiService) }
        ).flow
    }
}
```

---

## RemoteMediator

### 네트워크 + 로컬 DB

```kotlin
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator

/**
 * RemoteMediator (네트워크 + Room)
 */
@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val apiService: ApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, UserEntity>() {
    
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserEntity>
    ): MediatorResult {
        return try {
            // 로드 타입에 따라 페이지 결정
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    
                    (lastItem.page ?: 0) + 1
                }
            }
            
            // API 호출
            val response = apiService.getUsers(page, state.config.pageSize)
            
            // DB에 저장
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.userDao().clearAll()
                }
                
                val userEntities = response.users.map { user ->
                    UserEntity(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        page = page
                    )
                }
                
                database.userDao().insertAll(userEntities)
            }
            
            MediatorResult.Success(endOfPaginationReached = response.users.isEmpty())
            
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
```

---

## Paging Data 표시

### RecyclerView

```kotlin
/**
 * PagingDataAdapter
 */
class UserAdapter : PagingDataAdapter<User, UserViewHolder>(UserComparator) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        user?.let { holder.bind(it) }
    }
    
    object UserComparator : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}

/**
 * Activity/Fragment
 */
class UserListFragment : Fragment() {
    
    private val viewModel: UserViewModel by viewModels()
    private val adapter = UserAdapter()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.adapter = adapter
        
        // Paging Data 수집
        lifecycleScope.launch {
            viewModel.usersPagingData.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
}
```

---

## 로딩 상태

### LoadStateAdapter

```kotlin
/**
 * 로딩 상태 Adapter
 */
class LoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoadStateViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_load_state, parent, false)
        return LoadStateViewHolder(view, retry)
    }
    
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}

/**
 * RecyclerView에 연결
 */
recyclerView.adapter = adapter.withLoadStateFooter(
    footer = LoadStateAdapter { adapter.retry() }
)
```

---

## Jetpack Compose 통합

```kotlin
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items

/**
 * Compose에서 Paging 사용
 */
@Composable
fun UserListScreen(viewModel: UserViewModel = viewModel()) {
    val users = viewModel.usersPagingData.collectAsLazyPagingItems()
    
    LazyColumn {
        items(users) { user ->
            user?.let {
                UserItem(user = it)
            }
        }
        
        // 로딩 상태
        when (users.loadState.append) {
            is LoadState.Loading -> {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorItem(onRetry = { users.retry() })
                }
            }
            else -> {}
        }
    }
}

@Composable
fun UserItem(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = user.name)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = user.email, color = Color.Gray)
    }
}
```

---

## 참고 자료

- [Paging3 공식 문서](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
- [Paging Codelab](https://developer.android.com/codelabs/android-paging)
