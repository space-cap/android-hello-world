# CI/CD 자동화 완벽 가이드

## 📚 목차

1. [CI/CD란?](#cicd란)
2. [GitHub Actions 시작하기](#github-actions-시작하기)
3. [자동 빌드 설정](#자동-빌드-설정)
4. [자동 테스트](#자동-테스트)
5. [코드 품질 검사](#코드-품질-검사)
6. [자동 배포](#자동-배포)
7. [실전 워크플로우](#실전-워크플로우)

---

## CI/CD란?

> [!NOTE]
> **CI/CD = 지속적 통합 / 지속적 배포**
> 
> **CI (Continuous Integration):**
> - 코드 변경 시 자동으로 빌드
> - 자동으로 테스트 실행
> - 문제를 빨리 발견
> 
> **CD (Continuous Deployment):**
> - 테스트 통과 시 자동 배포
> - 수동 작업 최소화
> - 빠른 릴리스

### 왜 CI/CD가 필요한가?

**수동 프로세스 (CI/CD 없이):**
```
1. 코드 작성
2. 로컬에서 빌드 (5분)
3. 수동으로 테스트 실행 (10분)
4. APK 생성 (3분)
5. 수동으로 Play Console 업로드 (5분)
6. 릴리스 노트 작성 (5분)
───────────────────────────
총 28분 + 사람이 계속 지켜봐야 함
```

**자동화 프로세스 (CI/CD 사용):**
```
1. 코드 작성
2. Git push
3. 나머지는 자동!
   - 빌드
   - 테스트
   - APK 생성
   - 배포
───────────────────────────
총 0분 (사람 개입 불필요)
```

**실제 효과:**
- 개발자 시간 절약: **주당 2-3시간**
- 버그 발견 속도: **10배 빠름**
- 배포 빈도: **주 1회 → 일 1회**

---

## GitHub Actions 시작하기

> [!TIP]
> **GitHub Actions는 GitHub에 내장된 CI/CD 도구입니다**
> 
> **장점:**
> - GitHub과 완벽 통합
> - 무료 (공개 저장소)
> - 설정이 간단

### GitHub Actions 기본 개념

**워크플로우 구조:**
```yaml
name: 워크플로우 이름

on: [트리거 이벤트]

jobs:
  job1:
    runs-on: [실행 환경]
    steps:
      - name: 단계 이름
        run: 명령어
```

**실행 흐름:**
```
코드 push
    ↓
GitHub Actions 트리거
    ↓
가상 머신 생성 (Ubuntu/macOS/Windows)
    ↓
워크플로우 실행
    ↓
결과 리포트
```

### 첫 번째 워크플로우 만들기

**초보자를 위한 상세 단계:**

1. **GitHub 저장소로 이동**
2. **Actions 탭 클릭**
3. **"set up a workflow yourself" 클릭**
4. **파일 생성:**
   - 파일명: `.github/workflows/build.yml`
   - 위치: 프로젝트 루트

```yaml
# .github/workflows/build.yml
name: Android Build

# 언제 실행할까?
on:
  push:
    branches: [ main ]  # main 브랜치에 push 시
  pull_request:
    branches: [ main ]  # PR 생성 시

# 실행할 작업들
jobs:
  build:
    # 어떤 환경에서 실행?
    runs-on: ubuntu-latest
    
    steps:
      # 1단계: 코드 체크아웃
      - name: 코드 가져오기
        uses: actions/checkout@v4
      
      # 2단계: JDK 설정
      - name: JDK 17 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      # 3단계: Gradle 캐시 (빌드 속도 향상)
      - name: Gradle 캐시
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      
      # 4단계: 빌드
      - name: 앱 빌드
        run: ./gradlew assembleDebug
      
      # 5단계: APK 업로드 (다운로드 가능)
      - name: APK 업로드
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

**각 단계 설명:**

```yaml
# 왜 checkout이 필요한가?
- uses: actions/checkout@v4
# → GitHub Actions는 빈 가상 머신에서 시작
#   코드를 가져와야 빌드 가능!

# 왜 JDK 설정이 필요한가?
- uses: actions/setup-java@v4
# → Android 빌드에는 Java 필요
#   가상 머신에 JDK 설치

# 왜 캐시를 사용하는가?
- uses: actions/cache@v3
# → Gradle 의존성을 매번 다운로드하면 느림
#   캐시하면 빌드 시간 5분 → 1분!

# 왜 gradlew를 사용하는가?
- run: ./gradlew assembleDebug
# → Gradle Wrapper 사용
#   버전 일관성 보장
```

---

## 자동 빌드 설정

### Debug vs Release 빌드

```yaml
name: Build All Variants

on:
  push:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    strategy:
      matrix:
        variant: [Debug, Release]
    
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: ${{ matrix.variant }} 빌드
        run: ./gradlew assemble${{ matrix.variant }}
      
      - name: APK 업로드
        uses: actions/upload-artifact@v3
        with:
          name: app-${{ matrix.variant }}
          path: app/build/outputs/apk/**/*.apk
```

**matrix 전략이란?**
```
matrix:
  variant: [Debug, Release]

→ 2개의 병렬 작업 생성:
  1. Debug 빌드
  2. Release 빌드

동시에 실행되어 시간 절약!
```

### 서명된 APK 생성

**왜 서명이 필요한가?**
```
서명 없는 APK:
- Play Store 업로드 불가
- 보안 검증 없음

서명된 APK:
- Play Store 업로드 가능
- 앱 출처 검증
- 업데이트 가능
```

**GitHub Secrets 설정:**

1. GitHub 저장소 → Settings → Secrets and variables → Actions
2. New repository secret 클릭
3. 다음 시크릿 추가:
   - `KEYSTORE_FILE`: keystore 파일 (base64 인코딩)
   - `KEYSTORE_PASSWORD`: keystore 비밀번호
   - `KEY_ALIAS`: 키 별칭
   - `KEY_PASSWORD`: 키 비밀번호

**keystore를 base64로 인코딩:**
```bash
# macOS/Linux
base64 -i your-keystore.jks | pbcopy

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("your-keystore.jks")) | Set-Clipboard
```

**워크플로우:**
```yaml
name: Release Build

on:
  push:
    tags:
      - 'v*'  # v1.0.0 같은 태그 push 시

jobs:
  release:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      # Keystore 파일 복원
      - name: Keystore 디코딩
        run: |
          echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks
      
      # 서명 정보를 gradle.properties에 추가
      - name: 서명 정보 설정
        run: |
          echo "KEYSTORE_FILE=../keystore.jks" >> gradle.properties
          echo "KEYSTORE_PASSWORD=${{ secrets.KEYSTORE_PASSWORD }}" >> gradle.properties
          echo "KEY_ALIAS=${{ secrets.KEY_ALIAS }}" >> gradle.properties
          echo "KEY_PASSWORD=${{ secrets.KEY_PASSWORD }}" >> gradle.properties
      
      # Release 빌드
      - name: Release APK 빌드
        run: ./gradlew assembleRelease
      
      # APK 업로드
      - name: APK 업로드
        uses: actions/upload-artifact@v3
        with:
          name: app-release
          path: app/build/outputs/apk/release/app-release.apk
```

---

## 자동 테스트

### Unit 테스트 실행

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      # Unit 테스트 실행
      - name: Unit 테스트
        run: ./gradlew test
      
      # 테스트 결과 리포트 생성
      - name: 테스트 리포트 업로드
        if: always()  # 테스트 실패해도 리포트 업로드
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/
```

### UI 테스트 (Instrumented Tests)

```yaml
name: UI Tests

on: [push, pull_request]

jobs:
  ui-test:
    runs-on: macos-latest  # Android Emulator는 macOS에서만 가능
    
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      # Android Emulator에서 테스트
      - name: UI 테스트 실행
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          target: default
          arch: x86_64
          profile: Nexus 6
          script: ./gradlew connectedCheck
      
      - name: 테스트 결과 업로드
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: instrumentation-test-results
          path: app/build/reports/androidTests/
```

**왜 macOS를 사용하는가?**
```
Linux: Android Emulator 느림 (하드웨어 가속 없음)
macOS: 하드웨어 가속 지원 → 빠름!
Windows: 지원 제한적

하지만 macOS는 비용이 더 높음
→ UI 테스트는 중요한 PR에만 실행
```

---

## 코드 품질 검사

### Lint 검사

```yaml
name: Code Quality

on: [push, pull_request]

jobs:
  lint:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      # Android Lint 실행
      - name: Lint 검사
        run: ./gradlew lint
      
      # Lint 결과 업로드
      - name: Lint 리포트 업로드
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: lint-results
          path: app/build/reports/lint-results-*.html
```

### ktlint (Kotlin 코드 스타일)

```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}
```

```yaml
- name: ktlint 검사
  run: ./gradlew ktlintCheck
```

### Detekt (정적 분석)

```kotlin
// build.gradle.kts
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

detekt {
    config = files("config/detekt/detekt.yml")
}
```

```yaml
- name: Detekt 검사
  run: ./gradlew detekt
```

---

## 자동 배포

### Google Play Console 배포

**준비 사항:**

1. **Google Play Console API 활성화**
   - Google Cloud Console → APIs & Services
   - Google Play Android Developer API 활성화

2. **서비스 계정 생성**
   - IAM & Admin → Service Accounts
   - 서비스 계정 생성
   - JSON 키 다운로드

3. **GitHub Secret에 추가**
   - `PLAY_STORE_JSON_KEY`: 서비스 계정 JSON (전체 내용)

**워크플로우:**
```yaml
name: Deploy to Play Store

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      # Keystore 설정 (위와 동일)
      - name: Keystore 디코딩
        run: |
          echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks
      
      # AAB 빌드 (Play Store는 AAB 권장)
      - name: AAB 빌드
        run: ./gradlew bundleRelease
      
      # Play Store에 업로드
      - name: Play Store 배포
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_STORE_JSON_KEY }}
          packageName: com.example.myapp
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal  # internal, alpha, beta, production
          status: completed
          whatsNewDirectory: distribution/whatsnew
```

**배포 트랙:**
```
internal → alpha → beta → production
(내부)   (알파)  (베타)  (프로덕션)

각 단계에서 테스트 후 다음 단계로 승격
```

---

## 실전 워크플로우

### 완전한 CI/CD 파이프라인

```yaml
name: Complete CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  # 1단계: 코드 품질 검사
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Gradle 캐시
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      
      - name: Lint 검사
        run: ./gradlew lint
      
      - name: ktlint 검사
        run: ./gradlew ktlintCheck
      
      - name: Detekt 검사
        run: ./gradlew detekt
  
  # 2단계: 테스트
  test:
    needs: quality  # quality 작업 완료 후 실행
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Gradle 캐시
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      
      - name: Unit 테스트
        run: ./gradlew test
      
      - name: 테스트 커버리지
        run: ./gradlew jacocoTestReport
      
      - name: 커버리지 업로드
        uses: codecov/codecov-action@v3
        with:
          files: app/build/reports/jacoco/test/jacocoTestReport.xml
  
  # 3단계: 빌드
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: JDK 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Gradle 캐시
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      
      - name: Debug APK 빌드
        run: ./gradlew assembleDebug
      
      - name: APK 업로드
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
  
  # 4단계: 알림 (선택사항)
  notify:
    needs: [quality, test, build]
    if: always()
    runs-on: ubuntu-latest
    steps:
      - name: Slack 알림
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          text: 'CI/CD 파이프라인 완료!'
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

**파이프라인 흐름:**
```
코드 push
    ↓
┌───────────────┐
│ 1. 코드 품질  │ (Lint, ktlint, Detekt)
└───────┬───────┘
        ↓
┌───────────────┐
│ 2. 테스트     │ (Unit Tests, Coverage)
└───────┬───────┘
        ↓
┌───────────────┐
│ 3. 빌드       │ (APK 생성)
└───────┬───────┘
        ↓
┌───────────────┐
│ 4. 알림       │ (Slack, Email 등)
└───────────────┘
```

---

## 💡 CI/CD 베스트 프랙티스

### 1. 빌드 시간 최적화

```yaml
# Gradle 캐시 사용
- uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}

# 병렬 빌드
- run: ./gradlew assemble --parallel

# 불필요한 작업 스킵
- run: ./gradlew assemble -x lint
```

### 2. 비용 절감

```yaml
# PR에서만 UI 테스트 실행
on:
  pull_request:
    types: [opened, synchronize]

# 특정 파일 변경 시만 실행
on:
  push:
    paths:
      - 'app/**'
      - '**.gradle'
```

### 3. 보안

```yaml
# 절대 하지 말 것
- run: echo "API_KEY=sk_live_123..." >> gradle.properties

# 올바른 방법
- run: echo "API_KEY=${{ secrets.API_KEY }}" >> gradle.properties
```

---

## 🎯 체크리스트

### 기본 CI 설정
- [ ] 코드 push 시 자동 빌드
- [ ] PR 생성 시 자동 테스트
- [ ] Lint 검사 자동화
- [ ] 테스트 커버리지 측정

### 고급 CD 설정
- [ ] 태그 push 시 Release 빌드
- [ ] 서명된 APK/AAB 생성
- [ ] Play Store 자동 배포
- [ ] 배포 알림 설정

---

**마지막 업데이트**: 2025-11-30  
**작성자**: Antigravity AI Assistant

Automate Everything! 🤖
