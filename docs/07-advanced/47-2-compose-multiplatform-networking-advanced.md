# Compose Multiplatform 네트워킹 고급

> 📖 **시리즈 구성**
> - **47-1**: [Compose Multiplatform 네트워킹 기초](./47-1-compose-multiplatform-networking-basics.md) - Ktor Client, HTTP 요청
> - **47-2**: Compose Multiplatform 네트워킹 고급 (현재 문서) - 인증, 캐싱, WebSocket
> - **47-3**: [Compose Multiplatform 네트워킹 플랫폼](./47-3-compose-multiplatform-networking-platform.md) - 플랫폼별 구현

---

## 📚 목차

1. [인증 및 토큰 관리](#인증-및-토큰-관리)
2. [캐싱 전략](#캐싱-전략)
3. [WebSocket 실시간 통신](#websocket-실시간-통신)
4. [파일 업로드/다운로드](#파일-업로드다운로드)

---

## 인증 및 토큰 관리

### 🔐 Bearer Token 인증

```kotlin
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*

/**
 * Bearer Token 인증이 설정된 HTTP 클라이언트
 */
fun createAuthenticatedClient(token: String): HttpClient {
    return HttpClient {
        install(Auth) {
            bearer {
                // Bearer Token 설정
                loadTokens {
                    BearerTokens(
                        accessToken = token,
                        refreshToken = ""
                    )
                }
            }
        }
        
        install(ContentNegotiation) {
            json()
        }
    }
}
```

### 🔄 토큰 자동 갱신

```kotlin
/**
 * 토큰 자동 갱신 기능이 있는 클라이언트
 */
fun createClientWithRefreshToken(
    accessToken: String,
    refreshToken: String,
    onTokenRefresh: suspend (String, String) -> Pair<String, String>
): HttpClient {
    return HttpClient {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(accessToken, refreshToken)
                }
                
                // 토큰 갱신 로직
                refreshTokens {
                    val (newAccess, newRefresh) = onTokenRefresh(
                        oldTokens?.accessToken ?: "",
                        oldTokens?.refreshToken ?: ""
                    )
                    
                    BearerTokens(newAccess, newRefresh)
                }
            }
        }
    }
}
```

---

## 캐싱 전략

### 💾 메모리 캐싱

```kotlin
/**
 * 간단한 메모리 캐시 구현
 */
class MemoryCache<K, V> {
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    
    data class CacheEntry<V>(
        val value: V,
        val timestamp: Long,
        val ttl: Long  // Time To Live (밀리초)
    )
    
    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        
        // TTL 확인
        if (System.currentTimeMillis() - entry.timestamp > entry.ttl) {
            cache.remove(key)
            return null
        }
        
        return entry.value
    }
    
    fun put(key: K, value: V, ttl: Long = 60_000) {
        cache[key] = CacheEntry(value, System.currentTimeMillis(), ttl)
    }
    
    fun clear() {
        cache.clear()
    }
}

/**
 * 캐싱이 적용된 Repository
 */
class CachedUserRepository(private val client: HttpClient) {
    private val cache = MemoryCache<Int, User>()
    
    suspend fun getUser(id: Int): ApiResult<User> {
        // 캐시 확인
        cache.get(id)?.let {
            return ApiResult.Success(it)
        }
        
        // 캐시 미스 - API 호출
        return try {
            val user = client.get("https://api.example.com/users/$id")
                .body<User>()
            
            // 캐시에 저장 (5분)
            cache.put(id, user, ttl = 300_000)
            
            ApiResult.Success(user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "에러")
        }
    }
}
```

---

## WebSocket 실시간 통신

### 🔌 WebSocket 연결

```kotlin
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*

/**
 * WebSocket 클라이언트
 */
class WebSocketClient(private val client: HttpClient) {
    
    /**
     * WebSocket 연결 및 메시지 수신
     */
    fun connectToChat(roomId: String): Flow<String> = flow {
        client.webSocket("wss://api.example.com/chat/$roomId") {
            // 메시지 수신
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val message = frame.readText()
                        emit(message)
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * 메시지 전송
     */
    suspend fun sendMessage(roomId: String, message: String) {
        client.webSocket("wss://api.example.com/chat/$roomId") {
            send(Frame.Text(message))
        }
    }
}

/**
 * WebSocket 사용 예시
 */
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.connectToChat("room123")
    }
    
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                Text(message)
            }
        }
        
        var input by remember { mutableStateOf("") }
        
        Row {
            TextField(
                value = input,
                onValueChange = { input = it }
            )
            
            Button(onClick = {
                viewModel.sendMessage(input)
                input = ""
            }) {
                Text("전송")
            }
        }
    }
}
```

---

## 파일 업로드/다운로드

### 📤 파일 업로드

```kotlin
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * 멀티파트 파일 업로드
 */
suspend fun uploadFile(
    filePath: String,
    fileName: String,
    fileBytes: ByteArray
): ApiResult<String> {
    val client = createHttpClient()
    
    return try {
        val response = client.post("https://api.example.com/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        // 파일 추가
                        append("file", fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                        
                        // 추가 필드
                        append("description", "My uploaded file")
                    }
                )
            )
        }
        
        ApiResult.Success(response.bodyAsText())
        
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "업로드 실패")
        
    } finally {
        client.close()
    }
}
```

### 📥 파일 다운로드

```kotlin
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.*

/**
 * 진행률과 함께 파일 다운로드
 */
suspend fun downloadFile(
    url: String,
    onProgress: (Float) -> Unit
): ApiResult<ByteArray> {
    val client = createHttpClient()
    
    return try {
        val response: HttpResponse = client.get(url)
        val contentLength = response.contentLength() ?: 0L
        
        val channel: ByteReadChannel = response.bodyAsChannel()
        val buffer = ByteArray(1024)
        val result = mutableListOf<Byte>()
        var downloadedBytes = 0L
        
        while (!channel.isClosedForRead) {
            val bytes = channel.readAvailable(buffer)
            if (bytes > 0) {
                result.addAll(buffer.take(bytes))
                downloadedBytes += bytes
                
                // 진행률 계산
                if (contentLength > 0) {
                    val progress = downloadedBytes.toFloat() / contentLength
                    onProgress(progress)
                }
            }
        }
        
        ApiResult.Success(result.toByteArray())
        
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "다운로드 실패")
        
    } finally {
        client.close()
    }
}
```

---

## 🎯 다음 단계

고급 네트워킹을 마스터했습니다! 다음 단계로:

1. **[47-3. Compose Multiplatform 네트워킹 플랫폼](./47-3-compose-multiplatform-networking-platform.md)** - 플랫폼별 구현

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
