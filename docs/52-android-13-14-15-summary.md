# Android 13/14/15 새로운 기능 빠른 요약

> 💡 **빠른 참조 가이드**: Android 13, 14, 15의 주요 변경사항을 한눈에 확인하세요.
> 
> 상세한 내용은 각 버전별 문서를 참조하세요:
> - [Android 13 상세 가이드](./49-android-13-new-features.md)
> - [Android 14 상세 가이드](./50-android-14-new-features.md)
> - [Android 15 상세 가이드](./51-android-15-new-features.md)

---

## 📱 Android 13 (API 33) - 2022년 8월

### 🔐 개인정보 보호 & 권한

| 기능 | 설명 | 필수 여부 |
|------|------|----------|
| **런타임 알림 권한** | `POST_NOTIFICATIONS` 권한 필요 | ✅ 필수 |
| **세분화된 미디어 권한** | `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` | ✅ 필수 |
| **포토 피커** | 권한 없이 사진 선택 가능 | 🔶 권장 |

```kotlin
// 알림 권한 요청
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

// 미디어 권한 (Android 13+)
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO"/>
```

### 🎨 사용자 경험

- **테마별 앱 아이콘**: 시스템 테마에 맞춰 아이콘 색상 변경
- **예측 백 제스처**: 뒤로 가기 미리보기
- **클립보드 미리보기**: 복사 시 토스트 표시 (자동)

### 🌍 다국어

- **앱별 언어 설정**: 시스템 언어와 별도로 앱 언어 설정 가능

---

## 📱 Android 14 (API 34) - 2023년 10월

### 🔐 개인정보 보호 강화

| 기능 | 설명 | 필수 여부 |
|------|------|----------|
| **선택적 사진 권한** | 전체 대신 선택한 사진만 접근 | ✅ 필수 |
| **전체 화면 인텐트 권한** | 전화/알람 앱에서 필요 | 🔶 해당 시 |

```kotlin
// 선택적 미디어 접근 (Android 14+)
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"/>

// 전체 화면 인텐트 (전화/알람 앱)
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT"/>
```

### 🎯 사용자 경험

- **향상된 백 제스처**: 예측 백 제스처 기본 활성화
- **비선형 폰트 스케일링**: 최대 200% 확대 지원
- **Ultra HDR 이미지**: 더 넓은 색 영역 지원

### 🌍 다국어 & 접근성

- **문법적 굴절 API**: 언어별 문법 성별 지원 (프랑스어, 독일어 등)
- **지역 설정 기본 설정**: 온도 단위, 첫 번째 요일 등

### 💻 개발자

- **OpenJDK 17**: Sealed Classes, Pattern Matching 등

---

## 📱 Android 15 (API 35) - 2024년

### 🔐 개인정보 보호 샌드박스

| 기능 | 설명 | 필수 여부 |
|------|------|----------|
| **Topics API** | 광고 ID 대신 관심사 기반 광고 | 🔶 광고 앱 |
| **Attribution Reporting** | 개인정보 보호 전환 추적 | 🔶 광고 앱 |

### 📸 카메라 & 미디어

- **저조도 부스트**: 어두운 환경에서 자동 밝기 향상
- **HDR 헤드룸 제어**: HDR 밝기 범위 조정
- **인앱 카메라 컨트롤**: 향상된 줌, 노출 제어

### 📺 대형 화면 & 폴더블

- **향상된 멀티태스킹**: 멀티 윈도우 최적화
- **폴더블 기기 지원**: Flex Mode, 반쯤 접힌 상태 대응
- **앱 아카이빙**: 사용하지 않는 앱 자동 아카이빙

### 🚀 성능

- **16KB 페이지 크기**: 메모리 최적화
- **ANGLE 기본 활성화**: OpenGL ES 성능 향상

### 🔧 새로운 기능

- **부분 화면 공유**: 특정 앱 창만 공유
- **위성 연결**: 오지에서도 통신 가능
- **향상된 NFC**: 더 빠르고 안정적인 NFC

---

## 🔄 버전별 주요 변경사항 비교

| 영역 | Android 13 | Android 14 | Android 15 |
|------|-----------|-----------|-----------|
| **알림** | 런타임 권한 도입 ✅ | - | - |
| **미디어 권한** | 세분화 (이미지/비디오/오디오) | 선택적 접근 | - |
| **사진 선택** | 포토 피커 도입 | - | - |
| **전체 화면** | - | 인텐트 권한 필요 | - |
| **백 제스처** | 예측 백 도입 | 기본 활성화 ✅ | - |
| **카메라** | - | - | 저조도 부스트, HDR |
| **화면 공유** | - | - | 부분 공유 |
| **대형 화면** | - | 개선 | 폴더블 최적화 ✅ |
| **개인정보** | 권한 강화 | 더 세분화 | 샌드박스 ✅ |
| **성능** | - | - | 16KB 페이지 |
| **연결성** | - | - | 위성 연결 |

---

## ✅ 마이그레이션 체크리스트

### 모든 앱 (필수)

#### Android 13 (API 33)
- [ ] `POST_NOTIFICATIONS` 권한 요청 구현
- [ ] 미디어 권한 세분화 (`READ_MEDIA_*`)
- [ ] 포토 피커 API 사용 (권장)

#### Android 14 (API 34)
- [ ] 선택적 미디어 권한 처리 (`READ_MEDIA_VISUAL_USER_SELECTED`)
- [ ] 비선형 폰트 스케일링 테스트 (최대 200%)
- [ ] 암시적 인텐트에 패키지 지정

#### Android 15 (API 35)
- [ ] 16KB 페이지 크기 지원 확인
- [ ] 대형 화면 및 폴더블 기기 최적화
- [ ] 앱 아카이빙 활성화

### 특정 앱 유형

#### 📞 전화/알람 앱
- [ ] Android 14: `USE_FULL_SCREEN_INTENT` 권한 요청

#### 📸 미디어/카메라 앱
- [ ] Android 13: 포토 피커 우선 사용
- [ ] Android 14: 선택적 미디어 접근 UI 구현
- [ ] Android 15: 저조도 부스트, HDR 기능 활용

#### 📺 화상 회의 앱
- [ ] Android 15: 부분 화면 공유 지원

#### 📱 광고 SDK 사용 앱
- [ ] Android 15: Privacy Sandbox API 마이그레이션

---

## 🚀 빠른 시작 코드

### 권한 요청 (Android 13+)

```kotlin
// AndroidManifest.xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>

// Compose에서 권한 요청
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions() {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
    if (!permissionsState.allPermissionsGranted) {
        Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
            Text("권한 허용")
        }
    }
}
```

### 포토 피커 (Android 13+)

```kotlin
@Composable
fun PhotoPickerExample() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // 선택된 이미지 URI
    }
    
    Button(onClick = {
        launcher.launch(PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageOnly
        ))
    }) {
        Text("사진 선택")
    }
}
```

### 버전별 분기 처리

```kotlin
fun requestMediaPermission(): String {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            // Android 15 (API 35)
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            // Android 13 (API 33)
            Manifest.permission.READ_MEDIA_IMAGES
        }
        else -> {
            // Android 12 이하
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}
```

---

## 📊 영향도 분석

### 높은 영향 (대부분의 앱)
- ✅ **Android 13 알림 권한**: 모든 앱에 영향
- ✅ **Android 13 미디어 권한**: 사진/비디오 접근하는 모든 앱
- ✅ **Android 14 폰트 스케일링**: 모든 앱의 UI

### 중간 영향 (특정 앱)
- 🔶 **Android 14 전체 화면 인텐트**: 전화/알람 앱
- 🔶 **Android 14 선택적 미디어**: 갤러리/사진 앱
- 🔶 **Android 15 Privacy Sandbox**: 광고 SDK 사용 앱

### 낮은 영향 (선택적)
- ⚪ **Android 13 테마별 아이콘**: 브랜딩 강화
- ⚪ **Android 15 저조도 부스트**: 카메라 앱
- ⚪ **Android 15 위성 연결**: 특수 통신 앱

---

## 🎯 우선순위별 작업 순서

### 1단계: 필수 (즉시)
1. Android 13 알림 권한 구현
2. Android 13 미디어 권한 세분화
3. Android 14 폰트 스케일링 테스트

### 2단계: 권장 (1개월 내)
1. 포토 피커 API 마이그레이션
2. Android 14 선택적 미디어 권한 처리
3. 예측 백 제스처 지원

### 3단계: 선택적 (필요 시)
1. 테마별 앱 아이콘 추가
2. 대형 화면 최적화
3. Android 15 새 기능 활용

---

## 📚 추가 리소스

### 공식 문서
- [Android 13 개발자 가이드](https://developer.android.com/about/versions/13)
- [Android 14 개발자 가이드](https://developer.android.com/about/versions/14)
- [Android 15 개발자 가이드](https://developer.android.com/about/versions/15)

### 상세 가이드 (이 프로젝트)
- [49-android-13-new-features.md](./49-android-13-new-features.md) - Android 13 상세 가이드
- [50-android-14-new-features.md](./50-android-14-new-features.md) - Android 14 상세 가이드
- [51-android-15-new-features.md](./51-android-15-new-features.md) - Android 15 상세 가이드

---

## 💡 팁

### 테스트 환경 설정
```bash
# Android 13 테스트
adb shell setprop debug.target_sdk_version 33

# Android 14 테스트
adb shell setprop debug.target_sdk_version 34

# Android 15 테스트
adb shell setprop debug.target_sdk_version 35
```

### 권한 상태 확인
```bash
# 알림 권한 확인
adb shell dumpsys notification

# 미디어 권한 확인
adb shell dumpsys package [패키지명] | grep permission
```

---

**마지막 업데이트**: 2024년 12월
**다음 업데이트 예정**: Android 16 출시 시
