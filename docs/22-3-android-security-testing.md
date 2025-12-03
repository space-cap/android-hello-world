# Android 보안 테스팅

> 📖 **시리즈 구성**
> - **22-1**: [Android 보안 기초](./22-1-android-security-basics.md)
> - **22-2**: [Android 보안 고급](./22-2-android-security-advanced.md)
> - **22-3**: Android 보안 테스팅 (현재 문서)

---

## 보안 테스팅 도구

### MobSF (Mobile Security Framework)

```bash
# Docker로 실행
docker pull opensecurity/mobile-security-framework-mobsf
docker run -it -p 8000:8000 opensecurity/mobile-security-framework-mobsf

# APK 업로드 및 분석
http://localhost:8000
```

### APK 디컴파일

```bash
# apktool 사용
apktool d app.apk

# API 키 검색
grep -r "api_key" app/
grep -r "sk_live" app/
```

---

## 실전 보안 테스팅 시나리오

### 1. APK 디컴파일 및 분석

```bash
# 1. APK 디컴파일
apktool d app-release.apk

# 2. strings.xml 확인
cat app/res/values/strings.xml

# 3. BuildConfig 확인
cat app/smali/com/example/app/BuildConfig.smali
```

### 2. 네트워크 트래픽 분석

```bash
# Burp Suite 또는 Charles Proxy 사용
# 1. 프록시 설정
# 2. HTTPS 인증서 설치
# 3. 앱 실행 및 트래픽 캡처
# 4. API 키, 토큰 확인
```

### 3. 로컬 데이터 접근 테스트

```bash
# 루팅된 기기에서
adb shell
su
cd /data/data/com.example.app
cat shared_prefs/my_prefs.xml
```

---

## 보안 체크리스트

### 출시 전 필수 확인

- [ ] HTTPS만 사용
- [ ] API 키 하드코딩 없음
- [ ] 민감한 데이터 암호화
- [ ] ProGuard/R8 활성화
- [ ] 로그에 민감 정보 없음
- [ ] 백업 정책 설정

---

**마지막 업데이트**: 2024-12-03
