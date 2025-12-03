# 메모리 및 배터리 최적화

> 📖 **시리즈 구성**
> - **21-1**: [Compose 성능 최적화](./21-1-compose-performance-optimization.md)
> - **21-2**: 메모리 및 배터리 최적화 (현재 문서)
> - **21-3**: [성능 프로파일링 실전](./21-3-performance-profiling.md)

---

## 메모리 관리

### Context 누수 방지

```kotlin
// ❌ 나쁜 예: Context 누수
class BadViewModel(private val context: Context) : ViewModel()

// ✅ 좋은 예: Application Context
class GoodViewModel(private val application: Application) : ViewModel()

// ✅ 더 좋은 예: 이벤트로 처리
class BestViewModel : ViewModel() {
    private val _showToast = MutableSharedFlow<String>()
    val showToast: SharedFlow<String> = _showToast.asSharedFlow()
}
```

### Bitmap 최적화

```kotlin
fun loadOptimizedBitmap(
    context: Context,
    resourceId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, resourceId, this)
        
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        
        inJustDecodeBounds = false
        BitmapFactory.decodeResource(context.resources, resourceId, this)
    }
}
```

---

## 배터리 최적화

### Wake Lock 최소화

```kotlin
// ❌ Wake Lock 남용
val wakeLock = powerManager.newWakeLock(
    PowerManager.PARTIAL_WAKE_LOCK,
    "MyApp::MyWakeLock"
)
wakeLock.acquire()  // 절대 해제 안함!

// ✅ 타임아웃 설정
wakeLock.acquire(10*60*1000L)  // 10분 후 자동 해제
```

---

## APK 크기 최적화

```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true  // ProGuard/R8
            isShrinkResources = true  // 리소스 축소
        }
    }
}
```

---

**마지막 업데이트**: 2024-12-03
