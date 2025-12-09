# 💡 토이 프로젝트 아이디어 모음

학습한 내용을 바탕으로 실제로 만들어 볼 수 있는 앱 아이디어 목록입니다. 각 프로젝트는 특정 기술 스택을 깊이 있게 연습할 수 있도록 구성되었습니다.

## 📝 목록

| 순서 | 프로젝트명 | 핵심 기술 (Tech Stack) | 난이도 | 상태 |
|:---:|:---|:---|:---:|:---:|
| 1 | [랜덤 점심 메뉴 추천기](./01-랜덤-점심-메뉴-추천기.md) | **Animation**, **Room DB (CRUD)** | ⭐ | 초안 |
| 2 | [습관 추적 앱](./02-습관-추적.md) | **Room (Relations)**, **Date Handling** | ⭐⭐ | 초안 |
| 3 | [심플 디데이](./03-심플-디데이.md) | **Glance (Widget)**, **WorkManager** | ⭐⭐ | 초안 |
| 4 | [심플 뽀모도로 타이머](./04-심플-뽀모도로-타이머.md) | **Foreground Service**, **Canvas** | ⭐⭐⭐ | 초안 |
| 5 | [오늘 한 줄 감사](./05-오늘-한-줄-감사.md) | **View Capture (Share)**, **Emotional UI** | ⭐⭐ | 초안 |

## 🎯 기술 포인트 상세

### 1. 랜덤 점심 메뉴 추천기
- **Animation**: `Animatable`이나 `updateTransition`을 사용하여 룰렛이 돌아가는 듯한 역동적인 효과 구현.
- **Room DB**: 메뉴 데이터를 저장하고 불러오는 가장 기초적인 CRUD(Create, Read, Update, Delete) 연습.

### 2. 습관 추적 앱 (Habit Tracker)
- **Room Relations**: '습관(Habit)'과 '체크인(CheckIn)' 테이블 간의 1:N 관계 설정 및 쿼리 작성 연습.
- **Data Handling**: `LocalDate` 등 날짜 관련 클래스를 다루며 매일 초기화되는 로직 구현.

### 3. 심플 디데이 (Simple D-Day)
- **Jetpack Glance**: 기존 XML 방식이 아닌, Compose 스타일로 홈 화면 위젯을 만드는 최신 기술 습득.
- **WorkManager**: 자정이 지났을 때 위젯의 날짜(D-Day)를 자동으로 갱신하기 위한 백그라운드 작업 처리.

### 4. 심플 뽀모도로 타이머
- **Foreground Service**: 앱 화면을 닫아도 타이머가 계속 작동하고 알림창에 상태를 표시하는 서비스 로직.
- **Canvas API**: 원형 프로그레스 바나 물결 효과 등 커스텀 UI를 직접 그리는 그래픽 처리 능력 향상.

### 5. 오늘 한 줄 감사
- **View Capture**: Compose로 그려진 카드 UI를 비트맵 이미지로 변환하여 인스타그램 등에 공유하는 기능.
- **Emotional UI**: 사용자 경험을 극대화하는 감성적인 디자인과 인터랙션 구현 연습.
