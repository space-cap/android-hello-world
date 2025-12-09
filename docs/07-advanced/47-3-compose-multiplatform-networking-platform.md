# Compose Multiplatform 네트워킹 플랫폼별 구현

> 📖 **시리즈 구성**
> - **47-1**: [Compose Multiplatform 네트워킹 기초](./47-1-compose-multiplatform-networking-basics.md) - Ktor Client, HTTP 요청
> - **47-2**: [Compose Multiplatform 네트워킹 고급](./47-2-compose-multiplatform-networking-advanced.md) - 인증, 캐싱, WebSocket
> - **47-3**: Compose Multiplatform 네트워킹 플랫폼 (현재 문서) - 플랫폼별 구현

---

## 📚 목차

1. [플랫폼별 HTTP 엔진](#플랫폼별-http-엔진)
2. [Android 특화 구현](#android-특화-구현)
3. [iOS 특화 구현](#ios-특화-구현)
4. [Desktop 특화 구현](#desktop-특화-구현)

---

## 플랫폼별 HTTP 엔진

### 🔧 엔진 선택 가이드

```kotlin
/**
 * 플랫폼별 권장 엔진
 */

// Android: OkHttp
// - 안정적이고 성능이 좋음
// - HTTP/2 지원
androidMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
}

// iOS: Darwin
// - iOS 네이티브 URLSession 사용
// - 애플 생태계와 완벽한 통합
iosMain.dependencies {
    implementation("io.ktor:ktor-client-darwin:$ktorVersion")
}

// Desktop: OkHttp 또는 CIO
// - OkHttp: 안정적
// - CIO: 순수 Kotlin 구현
desktopMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    // 또는
    // implementation("io.ktor:ktor-client-cio:$ktorVersion")
}
```

---

## Android 특화 구현

### 📱 네트워크 상태 확인

```kotlin
// androidMain/kotlin/NetworkMonitor.kt

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Android 네트워크 상태 모니터
 */
actual class NetworkMonitor(private val context: Context) {
    
    private val _isConnected = MutableStateFlow(false)
    actual val isConnected: StateFlow<Boolean> = _isConnected
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    init {
        // 네트워크 콜백 등록
        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                }
                
                override fun onLost(network: Network) {
                    _isConnected.value = false
                }
            }
        )
    }
    
    actual fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
```

---

## iOS 특화 구현

### 🍎 네트워크 상태 확인

```kotlin
// iosMain/kotlin/NetworkMonitor.kt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Network.*
import platform.darwin.dispatch_get_main_queue

/**
 * iOS 네트워크 상태 모니터
 */
actual class NetworkMonitor {
    
    private val _isConnected = MutableStateFlow(false)
    actual val isConnected: StateFlow<Boolean> = _isConnected
    
    private val monitor = nw_path_monitor_create()
    
    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            _isConnected.value = (status == nw_path_status_satisfied)
        }
        
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }
    
    actual fun isNetworkAvailable(): Boolean {
        return _isConnected.value
    }
}
```

---

## Desktop 특화 구현

### 💻 네트워크 상태 확인

```kotlin
// desktopMain/kotlin/NetworkMonitor.kt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress

/**
 * Desktop 네트워크 상태 모니터
 */
actual class NetworkMonitor {
    
    private val _isConnected = MutableStateFlow(false)
    actual val isConnected: StateFlow<Boolean> = _isConnected
    
    actual fun isNetworkAvailable(): Boolean {
        return try {
            val address = InetAddress.getByName("www.google.com")
            !address.equals("")
        } catch (e: Exception) {
            false
        }
    }
}
```

---

## 공통 인터페이스

### 🌐 expect/actual 패턴

```kotlin
// commonMain/kotlin/NetworkMonitor.kt

import kotlinx.coroutines.flow.StateFlow

/**
 * 플랫폼별 네트워크 모니터 인터페이스
 */
expect class NetworkMonitor {
    val isConnected: StateFlow<Boolean>
    fun isNetworkAvailable(): Boolean
}

/**
 * 네트워크 상태를 확인하는 Repository
 */
class NetworkAwareRepository(
    private val client: HttpClient,
    private val networkMonitor: NetworkMonitor
) {
    
    suspend fun fetchData(): ApiResult<List<User>> {
        // 네트워크 확인
        if (!networkMonitor.isNetworkAvailable()) {
            return ApiResult.Error("네트워크 연결이 없습니다")
        }
        
        return try {
            val users = client.get("https://api.example.com/users")
                .body<List<User>>()
            ApiResult.Success(users)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "에러")
        }
    }
}
```

---

## 💡 플랫폼별 최적화 팁

### Android
```kotlin
// ✅ OkHttp 연결 풀 설정
fun createAndroidClient(): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            }
        }
    }
}
```

### iOS
```kotlin
// ✅ Darwin 엔진 설정
fun createIOSClient(): HttpClient {
    return HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}
```

### Desktop
```kotlin
// ✅ CIO 엔진으로 순수 Kotlin 구현
fun createDesktopClient(): HttpClient {
    return HttpClient(CIO) {
        engine {
            maxConnectionsCount = 1000
            endpoint {
                maxConnectionsPerRoute = 100
            }
        }
    }
}
```

---

## 🎯 완료!

Compose Multiplatform 네트워킹 시리즈를 모두 마스터했습니다! 🎉

이제 다음을 할 수 있습니다:
- ✅ Ktor Client로 HTTP 요청
- ✅ JSON 자동 직렬화/역직렬화
- ✅ 인증 및 토큰 관리
- ✅ 캐싱 전략 구현
- ✅ WebSocket 실시간 통신
- ✅ 플랫폼별 최적화

---

**마지막 업데이트**: 2024-12-03  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
