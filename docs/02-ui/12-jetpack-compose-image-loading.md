# Jetpack Compose 이미지 로딩 가이드 (Coil)

## 📚 목차

1. [Coil 소개](#coil-소개)
2. [프로젝트 설정](#프로젝트-설정)
3. [기본 이미지 로딩](#기본-이미지-로딩)
4. [플레이스홀더와 에러 처리](#플레이스홀더와-에러-처리)
5. [이미지 변환](#이미지-변환)
6. [캐싱 전략](#캐싱-전략)
7. [고급 기능](#고급-기능)
8. [실전 예제](#실전-예제)

---

## Coil 소개

**Coil**(Coroutine Image Loader)은 Kotlin Coroutines 기반의 Android 이미지 로딩 라이브러리입니다.

### 주요 특징

- ✅ **Kotlin 우선**: Coroutines와 완벽 통합
- ✅ **Jetpack Compose 지원**: AsyncImage 컴포저블 제공
- ✅ **경량**: Glide, Picasso보다 가벼움
- ✅ **빠른 성능**: 메모리 및 디스크 캐싱
- ✅ **확장 가능**: 커스텀 변환 및 인터셉터

### Coil vs 다른 라이브러리

| 기능 | Coil | Glide | Picasso |
|------|------|-------|---------|
| Kotlin 우선 | ✅ | ❌ | ❌ |
| Compose 지원 | ✅ | 제한적 | ❌ |
| Coroutines | ✅ | ❌ | ❌ |
| 크기 | 작음 | 큼 | 중간 |

---

## 프로젝트 설정

### 의존성 추가

`build.gradle.kts` (Module: app):

```kotlin
dependencies {
    // Coil for Compose
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // 선택사항: GIF 지원
    implementation("io.coil-kt:coil-gif:2.5.0")
    
    // 선택사항: SVG 지원
    implementation("io.coil-kt:coil-svg:2.5.0")
    
    // 선택사항: Video frames
    implementation("io.coil-kt:coil-video:2.5.0")
}
```

### 인터넷 권한

`AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 기본 이미지 로딩

### AsyncImage 사용

```kotlin
import coil.compose.AsyncImage

@Composable
fun BasicImageLoading() {
    AsyncImage(
        model = "https://example.com/image.jpg",
        contentDescription = "설명",
        modifier = Modifier.size(200.dp)
    )
}
```

### URL에서 이미지 로딩

```kotlin
@Composable
fun NetworkImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "네트워크 이미지",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentScale = ContentScale.Crop
    )
}
```

### 리소스에서 이미지 로딩

```kotlin
@Composable
fun ResourceImage() {
    AsyncImage(
        model = R.drawable.my_image,
        contentDescription = "로컬 이미지",
        modifier = Modifier.size(100.dp)
    )
}
```

### ContentScale 옵션

```kotlin
@Composable
fun ContentScaleExamples() {
    Column {
        // 이미지를 잘라서 채움
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(100.dp)
        )
        
        // 이미지를 맞춤 (비율 유지)
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(100.dp)
        )
        
        // 이미지를 채움 (비율 무시)
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(100.dp)
        )
    }
}
```

---

## 플레이스홀더와 에러 처리

### 플레이스홀더 이미지

```kotlin
@Composable
fun ImageWithPlaceholder() {
    AsyncImage(
        model = "https://example.com/image.jpg",
        contentDescription = null,
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.error),
        modifier = Modifier.size(200.dp)
    )
}
```

### 커스텀 로딩 상태

```kotlin
@Composable
fun ImageWithCustomLoading() {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "https://example.com/image.jpg",
            contentDescription = null,
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { 
                isLoading = false
                isError = true
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (isLoading) {
            CircularProgressIndicator()
        }
        
        if (isError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color.Red
                )
                Text("로딩 실패")
            }
        }
    }
}
```

### SubcomposeAsyncImage로 상태별 UI

```kotlin
import coil.compose.SubcomposeAsyncImage

@Composable
fun ImageWithStates() {
    SubcomposeAsyncImage(
        model = "https://example.com/image.jpg",
        contentDescription = null,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Text("이미지를 불러올 수 없습니다")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}
```

### 진행률 표시

```kotlin
@Composable
fun ImageWithProgress() {
    SubcomposeAsyncImage(
        model = "https://example.com/large-image.jpg",
        contentDescription = null,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 0.5f } // 실제로는 progress 추적 필요
                )
            }
        }
    )
}
```

---

## 이미지 변환

### 원형 이미지

```kotlin
@Composable
fun CircularImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
```

### 둥근 모서리

```kotlin
@Composable
fun RoundedImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
    )
}
```

### 블러 효과

```kotlin
import coil.request.ImageRequest
import coil.transform.BlurTransformation

@Composable
fun BlurredImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .transformations(BlurTransformation(context, radius = 25f))
            .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 그레이스케일

```kotlin
import coil.transform.GrayscaleTransformation

@Composable
fun GrayscaleImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .transformations(GrayscaleTransformation())
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 여러 변환 조합

```kotlin
@Composable
fun TransformedImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .transformations(
                listOf(
                    CircleCropTransformation(),
                    BlurTransformation(context, 10f)
                )
            )
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 커스텀 변환

```kotlin
import coil.size.Size
import coil.transform.Transformation
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

class SepiaTransformation : Transformation {
    override val cacheKey: String = "sepia"
    
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val output = Bitmap.createBitmap(
            input.width,
            input.height,
            input.config
        )
        
        val canvas = android.graphics.Canvas(output)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
            val sepiaMatrix = ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            postConcat(sepiaMatrix)
        }
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(input, 0f, 0f, paint)
        
        return output
    }
}

@Composable
fun SepiaImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .transformations(SepiaTransformation())
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

---

## 캐싱 전략

### 메모리 캐시 설정

```kotlin
import coil.ImageLoader
import coil.request.CachePolicy

@Composable
fun ImageWithCachePolicy(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED) // 메모리 캐시 활성화
            .diskCachePolicy(CachePolicy.ENABLED)   // 디스크 캐시 활성화
            .networkCachePolicy(CachePolicy.ENABLED) // 네트워크 캐시 활성화
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 캐시 비활성화

```kotlin
@Composable
fun NoCacheImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 커스텀 ImageLoader

```kotlin
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

@Composable
fun CustomImageLoader() {
    val context = LocalContext.current
    
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 메모리의 25%
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .build()
    }
    
    AsyncImage(
        model = "https://example.com/image.jpg",
        contentDescription = null,
        imageLoader = imageLoader,
        modifier = Modifier.size(200.dp)
    )
}
```

---

## 고급 기능

### 크로스페이드 애니메이션

```kotlin
@Composable
fun CrossfadeImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .crossfade(300) // 300ms 크로스페이드
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 헤더 추가 (인증 등)

```kotlin
@Composable
fun ImageWithHeaders(imageUrl: String, authToken: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .setHeader("Authorization", "Bearer $authToken")
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 이미지 크기 지정

```kotlin
import coil.size.Size

@Composable
fun SizedImage(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(500, 500)) // 500x500으로 다운로드
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### GIF 로딩

```kotlin
@Composable
fun GifImage(gifUrl: String) {
    AsyncImage(
        model = gifUrl,
        contentDescription = "GIF 이미지",
        modifier = Modifier.size(200.dp)
    )
}
```

### SVG 로딩

```kotlin
@Composable
fun SvgImage(svgUrl: String) {
    AsyncImage(
        model = svgUrl,
        contentDescription = "SVG 이미지",
        modifier = Modifier.size(200.dp)
    )
}
```

---

## 실전 예제

### 프로필 이미지

```kotlin
@Composable
fun ProfileImage(
    imageUrl: String?,
    name: String,
    size: Dp = 48.dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$name 프로필",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_avatar),
                error = painterResource(R.drawable.placeholder_avatar)
            )
        } else {
            // 이미지가 없으면 이니셜 표시
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
```

### 이미지 갤러리

```kotlin
@Composable
fun ImageGallery(images: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(images) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { /* 이미지 상세 보기 */ },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.error)
            )
        }
    }
}
```

### 상품 이미지 카드

```kotlin
@Composable
fun ProductCard(
    imageUrl: String,
    title: String,
    price: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BrokenImage,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                    }
                }
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

### 배너 슬라이더

```kotlin
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BannerSlider(banners: List<String>) {
    val pagerState = rememberPagerState()
    
    Box {
        HorizontalPager(
            count = banners.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            AsyncImage(
                model = banners[page],
                contentDescription = "배너 ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // 인디케이터
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { index ->
                val color = if (pagerState.currentPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White.copy(alpha = 0.5f)
                }
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}
```

### 줌 가능한 이미지

```kotlin
@Composable
fun ZoomableImage(imageUrl: String) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        
        if (scale > 1f) {
            offsetX += offsetChange.x
            offsetY += offsetChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = state)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}
```

### 이미지 다운로드 버튼

```kotlin
@Composable
fun DownloadableImage(imageUrl: String) {
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }
    
    Box {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        
        FloatingActionButton(
            onClick = {
                isDownloading = true
                // 다운로드 로직
                downloadImage(context, imageUrl) {
                    isDownloading = false
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "다운로드"
                )
            }
        }
    }
}

fun downloadImage(context: Context, url: String, onComplete: () -> Unit) {
    // 실제 다운로드 구현
    onComplete()
}
```

---

## 💡 베스트 프랙티스

### 1. ContentDescription 제공

```kotlin
// ✅ 좋은 예: 접근성을 위한 설명 제공
AsyncImage(
    model = imageUrl,
    contentDescription = "사용자 프로필 사진"
)

// ❌ 나쁜 예: 설명 누락
AsyncImage(
    model = imageUrl,
    contentDescription = null // 장식용 이미지만 null
)
```

### 2. 적절한 ContentScale 사용

```kotlin
// ✅ 프로필 이미지: Crop
AsyncImage(
    model = imageUrl,
    contentScale = ContentScale.Crop
)

// ✅ 전체 이미지 보기: Fit
AsyncImage(
    model = imageUrl,
    contentScale = ContentScale.Fit
)
```

### 3. 플레이스홀더 제공

```kotlin
// ✅ 좋은 예: 로딩 중 플레이스홀더
AsyncImage(
    model = imageUrl,
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

### 4. 메모리 효율적인 크기 지정

```kotlin
// ✅ 필요한 크기만 로드
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(imageUrl)
        .size(200, 200) // 실제 표시 크기
        .build()
)
```

### 5. 캐싱 활용

```kotlin
// ✅ 기본적으로 캐싱 활성화 (Coil 기본값)
AsyncImage(model = imageUrl, ...)

// 특별한 경우만 비활성화
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(imageUrl)
        .diskCachePolicy(CachePolicy.DISABLED) // 민감한 이미지
        .build()
)
```

---

## 🎯 다음 단계

이미지 로딩을 마스터했습니다! Phase 3 완료! 🎉

**Phase 4: 고급 단계**로 넘어가세요:

1. **로컬 데이터베이스** - Room Database
2. **권한 관리** - 런타임 권한 처리
3. **테스팅** - Unit Test, UI Test
4. **디버깅** - 문제 해결 기법
5. **앱 배포** - Google Play 배포

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Happy Coding! 🚀
