# 안드로이드 앱 개발 종합 가이드

> 📖 **문서 개요**
>
> 이 문서는 안드로이드 앱 개발 시 고려해야 할 전반적인 전략, 아키텍처, 폴더 구조, 그리고 필수 체크리스트를 다룹니다. 앱의 규모(소형, 중형, 대형)에 따른 차별화된 접근 방식을 제시하여 상황에 맞는 최적의 개발 방향을 잡는 데 도움을 줍니다.

---

## 📚 목차

1. [앱 규모별 개발 전략](#앱-규모별-개발-전략)
2. [폴더 및 패키지 구조](#폴더-및-패키지-구조)
3. [필수 체크리스트](#필수-체크리스트)
4. [유용한 라이브러리 및 도구](#유용한-라이브러리-및-도구)

---

## 앱 규모별 개발 전략

앱의 복잡도와 팀 규모에 따라 적절한 아키텍처와 전략을 선택해야 합니다.

### 1. 소형 앱 (Toy Project, MVP)

*   **특징**: 기능이 적고, 개발 기간이 짧으며, 1-2명의 개발자가 참여합니다.
*   **목표**: 빠른 프로토타이핑 및 출시.
*   **아키텍처**:
    *   **Monolithic Module**: `app` 모듈 하나로 구성.
    *   **MVVM 패턴**: ViewModel과 Screen(Compose) 분리.
    *   **간단한 DI**: Hilt를 사용하되 복잡한 모듈 분리는 지양.
*   **데이터**: Room Database 또는 DataStore 사용.
*   **주의사항**: 과도한 엔지니어링(Over-engineering)을 피하고 기능 구현에 집중하세요.

### 2. 중형 앱 (Startups, Series A/B)

*   **특징**: 기능이 다양해지고, 유지보수가 중요해지며, 3-10명의 개발자가 협업합니다.
*   **목표**: 확장성, 테스트 용이성, 안정성.
*   **아키텍처**:
    *   **Modularization 도입**: `core` (공통 유틸, 디자인 시스템)와 `feature` (기능별) 모듈 분리 시작.
    *   **Clean Architecture**: Domain, Data, Presentation 레이어 분리.
    *   **CI/CD**: GitHub Actions 등을 통한 자동 빌드 및 테스트 구축.
*   **협업**: 코드 리뷰 필수, 코딩 컨벤션 확립 (ktlint 등).

### 3. 대형 앱 (Enterprise, Super Apps)

*   **특징**: 수십 개 이상의 기능, 수십 명 이상의 개발자, 레거시 코드 존재.
*   **목표**: 빌드 속도 최적화, 모듈 간 의존성 관리, 대규모 트래픽 처리.
*   **아키텍처**:
    *   **Layered + Feature Modularization**: 수십 개의 모듈로 세분화.
    *   **Design System Module**: UI 컴포넌트의 엄격한 표준화 및 재사용.
    *   **BFF (Backend For Frontend)**: 클라이언트에 최적화된 API 응답 구조 설계.
*   **운영**:
    *   **Feature Flag**: 기능의 점진적 배포 및 롤백 제어.
    *   **A/B Testing**: 데이터 기반 의사결정 시스템.
    *   **Monitoring**: Crashlytics, Performance Monitoring 등의 철저한 관제.

---

## 폴더 및 패키지 구조

패키지 구조는 크게 **Layer-based(계층 기반)**와 **Feature-based(기능 기반)**로 나뉩니다. 최근에는 **Feature-based**가 더 선호되는 추세입니다.

### 1. Feature-based Structure (권장)

기능 단위로 패키지를 묶어 관련 코드를 한곳에서 관리합니다. 모듈화로 전환하기 쉽습니다.

**Q: Repository, Model, DataSource는 어디에 두나요?**
두 가지 접근 방식이 있습니다.

#### A. Core Data Module (공유 데이터)
여러 기능에서 함께 사용하는 데이터(예: User, Product)는 `core` 모듈에 둡니다.

```
com.example.app
├── core/
│   ├── model/             # 전역 데이터 모델 (User, Product)
│   ├── data/              # Repository 구현체
│   │   ├── UserRepository.kt
│   │   └── ProductRepository.kt
│   ├── network/           # DataSource (API)
│   └── database/          # DataSource (DAO)
├── feature/
│   ├── login/             # UI와 ViewModel만 포함
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   └── home/
```

#### B. Feature-scoped Data (기능 전용 데이터)
특정 기능에서만 사용하는 데이터는 해당 기능 패키지 내부에 `data` 패키지를 만듭니다.

```
com.example.app
├── feature/
│   ├── chat/
│   │   ├── data/          # 이 기능 전용 데이터 계층
│   │   │   ├── ChatRepository.kt
│   │   │   ├── model/
│   │   │   └── source/
│   │   ├── ui/
│   │   │   ├── ChatScreen.kt
│   │   │   └── ChatViewModel.kt
│   │   └── domain/        # (선택적) UseCase
```

#### 종합 예시 구조

```
com.example.app
├── core/                  # 공통 기능
│   ├── model/             # 전역 모델 (User)
│   ├── data/              # 전역 리포지토리 (UserRepository)
│   ├── network/           # Retrofit, OkHttp
│   └── database/          # Room
├── feature/               # 기능별 패키지
│   ├── login/             # 로그인 기능
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   ├── home/              # 홈 화면 기능
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   └── post/              # 게시글 작성 (복잡한 기능)
│       ├── data/          # 게시글 전용 데이터
│       │   ├── PostRepository.kt
│       │   └── model/
│       └── ui/
│           └── PostScreen.kt
└── MainActivity.kt
```

### 2. Layer-based Structure (전통적 방식)

역할(Layer)에 따라 패키지를 나눕니다. 프로젝트가 커지면 관련 파일을 찾기 어려워질 수 있습니다.

```
com.example.app
├── data/
│   ├── api/
│   ├── repository/
│   └── model/
├── domain/
│   ├── usecase/
│   └── model/
├── presentation/
│   ├── ui/
│   │   ├── login/
│   │   └── home/
│   └── viewmodel/
└── di/
```

### 3. 리소스 폴더 구조 (res)

```
res/
├── drawable/              # 벡터 아이콘, 이미지
├── layout/                # (Compose 사용 시 거의 안 씀)
├── mipmap/                # 앱 아이콘
├── values/
│   ├── colors.xml         # 색상 정의
│   ├── strings.xml        # 문자열 (다국어 지원 시 strings-ko.xml 등 추가)
│   └── themes.xml         # 테마 정의
└── xml/                   # 네트워크 보안 설정 등 기타 XML
```

---

## 필수 체크리스트

앱 출시 전 반드시 확인해야 할 사항들입니다.

### 🔒 보안 (Security)
- [ ] **난독화 (R8/ProGuard)**: `minifyEnabled true` 설정으로 코드 역공학 방지.
- [ ] **API Key 관리**: `local.properties` 또는 Secrets Gradle Plugin을 사용하여 키 숨기기. (절대 Git에 커밋 금지!)
- [ ] **네트워크 보안**: HTTPS 사용 필수, `network_security_config.xml` 설정.
- [ ] **데이터 암호화**: 민감한 정보는 `EncryptedSharedPreferences` 또는 `Keystore` 사용하여 저장.

### ⚡ 성능 (Performance)
- [ ] **메모리 누수 확인**: LeakCanary 등을 사용하여 Activity/Fragment 누수 점검.
- [ ] **리스트 최적화**: `LazyColumn` 사용 시 `key` 설정, 무거운 연산 피하기.
- [ ] **이미지 최적화**: Coil/Glide 사용, 적절한 리사이징, WebP 형식 사용 권장.
- [ ] **Baseline Profiles**: 앱 시작 속도 및 스크롤 성능 향상을 위한 프로필 생성.

### ♿ 접근성 (Accessibility)
- [ ] **Content Description**: 이미지, 아이콘에 적절한 설명 추가.
- [ ] **Touch Target Size**: 버튼 등 터치 영역은 최소 48dp 이상 확보.
- [ ] **Color Contrast**: 텍스트와 배경의 명암비 준수.

### 🚀 배포 (Deployment)
- [ ] **Signing Key**: 릴리즈 키스토어 안전하게 보관.
- [ ] **Version Code/Name**: 업데이트 시 버전 코드 증가 필수.
- [ ] **Google Play 정책**: 개인정보처리방침, 타겟 SDK 버전 등 정책 준수 확인.

---

## 유용한 라이브러리 및 도구

### 필수 라이브러리
*   **Jetpack Compose**: 최신 UI 툴킷.
*   **Hilt**: 의존성 주입 (DI).
*   **Coroutines & Flow**: 비동기 프로그래밍.
*   **Retrofit & OkHttp**: 네트워크 통신.
*   **Room**: 로컬 데이터베이스.
*   **Coil**: 이미지 로딩 (Compose 친화적).

### 개발 생산성 도구
*   **Timber**: 로그 출력 유틸리티.
*   **LeakCanary**: 메모리 누수 탐지.
*   **Chucker**: 기기 내 네트워크 트래픽 모니터링.
*   **Detekt / Ktlint**: 코틀린 정적 분석 및 포맷팅 도구.

---

**마지막 업데이트**: 2024-12-04
**작성자**: Antigravity AI Assistant
