# 📱 Android Development Learning Hub

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

**초보자부터 전문가까지, Android 개발의 모든 것을 배우는 완전한 학습 로드맵**

[📚 문서 보기](./docs) | [🚀 시작하기](#시작하기) | [📖 학습 로드맵](./docs/00-learning-roadmap.md)

</div>

---

## 📋 목차

- [소개](#-소개)
- [주요 특징](#-주요-특징)
- [문서 구조](#-문서-구조)
- [시작하기](#-시작하기)
- [학습 경로](#-학습-경로)
- [문서 목록](#-문서-목록)
- [기여하기](#-기여하기)
- [라이선스](#-라이선스)

---

## 🎯 소개

이 저장소는 **Android 개발을 처음 시작하는 초보자부터 전문가까지** 모두를 위한 포괄적인 학습 자료를 제공합니다. 총 **79개의 상세한 가이드**를 통해 Kotlin, Jetpack Compose, Android 아키텍처, 최신 기술 스택까지 모든 것을 배울 수 있습니다.

### ✨ 왜 이 저장소인가?

- 📚 **체계적인 학습**: 기초부터 고급까지 단계별 학습 경로
- 💡 **실전 중심**: 모든 개념에 실제 코드 예제 포함
- 🇰🇷 **한국어 지원**: 완전한 한국어 설명과 주석
- 🔄 **최신 기술**: Jetpack Compose, KMM, ML Kit 등 최신 기술 포함
- 🎓 **초보자 친화적**: 복잡한 개념도 쉽게 설명

---

## 🌟 주요 특징

### 📖 79개의 상세한 가이드
- **기본 개념**: Kotlin, Android 프로젝트 구조
- **UI/UX**: Jetpack Compose, Material Design
- **아키텍처**: MVVM, MVI, Clean Architecture
- **데이터**: Room, DataStore, Paging3
- **네트워크**: Retrofit, Ktor
- **고급 기능**: ML Kit, ARCore, Biometric, WorkManager
- **크로스 플랫폼**: Kotlin Multiplatform Mobile

### 💻 풍부한 코드 예제
- **1,000개 이상**의 실전 코드 예제
- 모든 코드에 **한글 주석** 포함
- **베스트 프랙티스** 및 안티패턴 설명

### 🎯 실전 프로젝트
- 각 주제별 **실전 예제** 포함
- 완전한 앱 예제
- 단계별 구현 가이드

---

## 📁 문서 구조

```
docs/
├── 00-learning-roadmap.md          # 학습 로드맵
├── 01-20/                          # 기초 (Kotlin, Compose 기본)
├── 21-40/                          # 중급 (아키텍처, 데이터, 네트워크)
├── 41-60/                          # 고급 (성능, 보안, 배포)
├── 61-79/                          # 최신 기술 (ML, AR, KMM)
└── README.md                       # 문서 인덱스
```

---

## 🚀 시작하기

### 1️⃣ 저장소 클론

```bash
git clone https://github.com/yourusername/android-hello-world.git
cd android-hello-world
```

### 2️⃣ Android Studio 설치

- [Android Studio](https://developer.android.com/studio) 다운로드 및 설치
- 최소 버전: Arctic Fox (2020.3.1) 이상 권장

### 3️⃣ 프로젝트 열기

```bash
# Android Studio에서 프로젝트 열기
File > Open > android-hello-world 폴더 선택
```

### 4️⃣ 학습 시작

1. [학습 로드맵](./docs/00-learning-roadmap.md) 확인
2. 자신의 수준에 맞는 문서부터 시작
3. 코드 예제를 직접 실행하며 학습

---

## 🎓 학습 경로

### 🌱 초급 (1-20번 문서)
**목표**: Android 개발 기초 다지기

```
01. Kotlin 기초
02. Android 프로젝트 구조
03. Jetpack Compose 레이아웃
04. 상태 관리
05. 네비게이션
...
```

👉 [초급 학습 경로 자세히 보기](./docs/00-learning-roadmap.md#초급)

### 🌿 중급 (21-40번 문서)
**목표**: 실전 앱 개발 능력 향상

```
21. 성능 최적화
22. 보안
23. Firebase 통합
24. Room 데이터베이스
25. Retrofit 네트워킹
...
```

👉 [중급 학습 경로 자세히 보기](./docs/00-learning-roadmap.md#중급)

### 🌳 고급 (41-60번 문서)
**목표**: 전문가 수준의 앱 개발

```
41. MVVM 아키텍처
42. MVI 패턴
43. Hilt 의존성 주입
44. Coroutines & Flow
45. 고급 테스팅
...
```

👉 [고급 학습 경로 자세히 보기](./docs/00-learning-roadmap.md#고급)

### 🚀 전문가 (61-79번 문서)
**목표**: 최신 기술 스택 마스터

```
61. NFC & Bluetooth
63. Sensors & Motion
66. Biometric Authentication
71. ML Kit
72. ARCore
74. Kotlin Multiplatform Mobile
...
```

👉 [전문가 학습 경로 자세히 보기](./docs/00-learning-roadmap.md#전문가)

---

## 📚 문서 목록

### 🎯 핵심 주제

| 번호 | 주제 | 설명 |
|------|------|------|
| [01](./docs/01-kotlin-basics-for-compose.md) | Kotlin 기초 | Compose를 위한 Kotlin 필수 개념 |
| [03](./docs/03-jetpack-compose-layout-guide.md) | Compose 레이아웃 | Column, Row, Box 등 기본 레이아웃 |
| [04](./docs/04-jetpack-compose-state-guide.md) | 상태 관리 | State, remember, ViewModel |
| [09](./docs/09-android-networking-retrofit.md) | Retrofit | REST API 통신 |
| [13](./docs/13-android-room-database.md) | Room | 로컬 데이터베이스 |
| [41](./docs/41-android-mvvm-architecture.md) | MVVM | 아키텍처 패턴 |
| [43](./docs/43-android-hilt-guide.md) | Hilt | 의존성 주입 |
| [76](./docs/76-android-work-manager-guide.md) | WorkManager | 백그라운드 작업 |
| [77](./docs/77-android-paging3-guide.md) | Paging3 | 대용량 데이터 페이징 |

### 📱 최신 기술

| 번호 | 주제 | 설명 |
|------|------|------|
| [66](./docs/66-android-biometric-guide.md) | Biometric | 생체 인증 |
| [71](./docs/71-android-ml-kit-guide.md) | ML Kit | 머신러닝 |
| [72](./docs/72-android-arcore-guide.md) | ARCore | 증강 현실 |
| [74](./docs/74-kotlin-multiplatform-mobile-guide.md) | KMM | 멀티플랫폼 |

👉 [전체 문서 목록 보기](./docs/README.md)

---

## 📊 문서 통계

<div align="center">

| 항목 | 수치 |
|------|------|
| 📄 **총 문서 수** | 79개 |
| 📝 **총 라인 수** | 약 25,000줄 |
| 💾 **총 용량** | 약 2MB |
| 💻 **코드 예제** | 1,000개 이상 |
| 🎯 **실전 프로젝트** | 50개 이상 |

</div>

---

## 🎨 주요 기술 스택

<div align="center">

### 언어 & 프레임워크
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)

### 아키텍처 & 패턴
![MVVM](https://img.shields.io/badge/MVVM-00897B?style=flat-square)
![Clean Architecture](https://img.shields.io/badge/Clean%20Architecture-009688?style=flat-square)

### 라이브러리
![Room](https://img.shields.io/badge/Room-4285F4?style=flat-square)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat-square)
![Hilt](https://img.shields.io/badge/Hilt-FF6F00?style=flat-square)
![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=flat-square)

### 최신 기술
![ML Kit](https://img.shields.io/badge/ML%20Kit-4285F4?style=flat-square)
![ARCore](https://img.shields.io/badge/ARCore-FF6F00?style=flat-square)
![KMM](https://img.shields.io/badge/KMM-7F52FF?style=flat-square)

</div>

---

## 🤝 기여하기

이 프로젝트에 기여하고 싶으신가요? 환영합니다! 🎉

### 기여 방법

1. **Fork** 이 저장소
2. **Feature 브랜치** 생성 (`git checkout -b feature/AmazingFeature`)
3. **변경사항 커밋** (`git commit -m 'Add some AmazingFeature'`)
4. **브랜치에 Push** (`git push origin feature/AmazingFeature`)
5. **Pull Request** 생성

### 기여 가이드라인

- 모든 문서는 **한국어**로 작성
- 코드 예제에는 **주석** 필수
- **실전 예제** 포함 권장
- **베스트 프랙티스** 설명

---

## 📞 문의 및 지원

- 📧 **이메일**: your.email@example.com
- 💬 **이슈**: [GitHub Issues](https://github.com/yourusername/android-hello-world/issues)
- 📖 **위키**: [GitHub Wiki](https://github.com/yourusername/android-hello-world/wiki)

---

## 📄 라이선스

이 프로젝트는 [MIT License](LICENSE) 하에 배포됩니다.

```
MIT License

Copyright (c) 2024 Your Name

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 🙏 감사의 말

이 프로젝트는 다음 리소스들을 참고하여 작성되었습니다:

- [Android Developers](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

## ⭐ 스타 히스토리

[![Star History Chart](https://api.star-history.com/svg?repos=yourusername/android-hello-world&type=Date)](https://star-history.com/#yourusername/android-hello-world&Date)

---

<div align="center">

**🌟 이 프로젝트가 도움이 되었다면 Star를 눌러주세요! 🌟**

Made with ❤️ by Android Developers

[⬆ 맨 위로 이동](#-android-development-learning-hub)

</div>
