# Android 보안 테스팅

> 📖 **시리즈 구성**
> - **22-1**: [Android 보안 기초](./22-1-android-security-basics.md)
> - **22-2**: [Android 보안 고급](./22-2-android-security-advanced.md)
> - **22-3**: Android 보안 테스팅 (현재 문서)

---

## 📚 목차

1. [보안 테스트 자동화](#보안-테스트-자동화)
2. [취약점 스캔](#취약점-스캔)
3. [침투 테스트](#침투-테스트)
4. [보안 모니터링](#보안-모니터링)
5. [실전 시나리오](#실전-시나리오)

---

## 보안 테스트 자동화

### OWASP Mobile Security Testing Guide

**OWASP MSTG 체크리스트**

```kotlin
/**
 * OWASP Mobile Security Testing Guide 기반 테스트
 * 
 * https://github.com/OWASP/owasp-mstg
 */
class OWASPSecurityTests {
    
    /**
     * M1: 부적절한 플랫폼 사용
     */
    @Test
    fun testPlatformUsage() {
        // 1. 권한 최소화 확인
        val manifest = getManifestPermissions()
        val unnecessaryPermissions = manifest.filter { permission ->
            !isPermissionNecessary(permission)
        }
        assertTrue("불필요한 권한 발견: $unnecessaryPermissions", 
            unnecessaryPermissions.isEmpty())
        
        // 2. Exported 컴포넌트 확인
        val exportedComponents = getExportedComponents()
        exportedComponents.forEach { component ->
            assertTrue("보호되지 않은 컴포넌트: ${component.name}",
                component.hasPermissionProtection())
        }
    }
    
    /**
     * M2: 안전하지 않은 데이터 저장
     */
    @Test
    fun testDataStorage() {
        // 1. SharedPreferences 암호화 확인
        val sharedPrefs = context.getSharedPreferences("test", Context.MODE_PRIVATE)
        assertFalse("평문 SharedPreferences 사용",
            sharedPrefs is SharedPreferences)
        
        // 2. 로그에 민감한 정보 없는지 확인
        val logs = getApplicationLogs()
        val sensitivePatterns = listOf(
            "password=",
            "token=",
            "api_key=",
            Regex("\\d{4}-\\d{4}-\\d{4}-\\d{4}")  // 카드 번호
        )
        
        logs.forEach { log ->
            sensitivePatterns.forEach { pattern ->
                assertFalse("로그에 민감한 정보 발견: $log",
                    log.contains(pattern))
            }
        }
    }
    
    /**
     * M3: 안전하지 않은 통신
     */
    @Test
    fun testNetworkSecurity() {
        // 1. HTTPS 강제 확인
        val networkConfig = getNetworkSecurityConfig()
        assertFalse("HTTP 통신 허용됨",
            networkConfig.cleartextTrafficPermitted)
        
        // 2. Certificate Pinning 확인
        assertTrue("Certificate Pinning 미설정",
            networkConfig.hasCertificatePinning())
    }
    
    /**
     * M4: 안전하지 않은 인증
     */
    @Test
    fun testAuthentication() {
        // 1. 토큰 저장 방식 확인
        val tokenStorage = getTokenStorage()
        assertTrue("토큰이 암호화되지 않음",
            tokenStorage.isEncrypted())
        
        // 2. 세션 타임아웃 확인
        val sessionTimeout = getSessionTimeout()
        assertTrue("세션 타임아웃이 너무 김: $sessionTimeout",
            sessionTimeout <= 15 * 60 * 1000)  // 15분
    }
    
    /**
     * M5: 불충분한 암호화
     */
    @Test
    fun testCryptography() {
        // 1. 약한 암호화 알고리즘 확인
        val weakAlgorithms = listOf("DES", "MD5", "SHA1")
        val usedAlgorithms = getUsedCryptoAlgorithms()
        
        usedAlgorithms.forEach { algorithm ->
            assertFalse("약한 암호화 알고리즘 사용: $algorithm",
                weakAlgorithms.contains(algorithm))
        }
        
        // 2. 하드코딩된 키 확인
        val hardcodedKeys = findHardcodedKeys()
        assertTrue("하드코딩된 암호화 키 발견",
            hardcodedKeys.isEmpty())
    }
    
    // 헬퍼 메서드들
    private fun getManifestPermissions(): List<String> = emptyList()
    private fun isPermissionNecessary(permission: String): Boolean = true
    private fun getExportedComponents(): List<Component> = emptyList()
    private fun getApplicationLogs(): List<String> = emptyList()
    private fun getNetworkSecurityConfig(): NetworkConfig = NetworkConfig()
    private fun getTokenStorage(): TokenStorage = TokenStorage()
    private fun getSessionTimeout(): Long = 0L
    private fun getUsedCryptoAlgorithms(): List<String> = emptyList()
    private fun findHardcodedKeys(): List<String> = emptyList()
}

data class Component(val name: String) {
    fun hasPermissionProtection(): Boolean = true
}

class NetworkConfig {
    val cleartextTrafficPermitted: Boolean = false
    fun hasCertificatePinning(): Boolean = true
}

class TokenStorage {
    fun isEncrypted(): Boolean = true
}
```

### 정적 분석 도구

**Android Lint**

```kotlin
// build.gradle.kts
android {
    lint {
        // ✅ 보안 검사 활성화
        checkReleaseBuilds = true
        abortOnError = true
        
        // 보안 관련 이슈를 에러로 처리
        error += listOf(
            "HardcodedText",
            "SetJavaScriptEnabled",
            "ExportedContentProvider",
            "ExportedReceiver",
            "ExportedService"
        )
        
        // 경고 레벨 설정
        warning += listOf(
            "AllowBackup",
            "GoogleAppIndexingWarning",
            "UnusedResources"
        )
    }
}
```

**SpotBugs (FindBugs 후속)**

```kotlin
// build.gradle.kts
plugins {
    id("com.github.spotbugs") version "5.0.13"
}

spotbugs {
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
    
    // 보안 버그 탐지
    includeFilter.set(file("spotbugs-security.xml"))
}

// spotbugs-security.xml
/*
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <!-- SQL Injection -->
    <Match>
        <Bug pattern="SQL_INJECTION" />
    </Match>
    
    <!-- XSS -->
    <Match>
        <Bug pattern="XSS_REQUEST_PARAMETER_TO_SEND_ERROR" />
    </Match>
    
    <!-- 약한 암호화 -->
    <Match>
        <Bug pattern="WEAK_MESSAGE_DIGEST" />
    </Match>
</FindBugsFilter>
*/
```

### 동적 분석 도구

**MobSF (Mobile Security Framework)**

```bash
# MobSF 설치
docker pull opensecurity/mobile-security-framework-mobsf

# MobSF 실행
docker run -it -p 8000:8000 opensecurity/mobile-security-framework-mobsf:latest

# APK 업로드 및 분석
# http://localhost:8000
```

**MobSF 분석 항목:**
- 권한 분석
- 코드 분석 (난독화, 취약점)
- 네트워크 보안
- 데이터 저장
- 암호화
- 바이너리 분석

---

## 취약점 스캔

### 의존성 취약점 검사

**OWASP Dependency-Check**

```kotlin
// build.gradle.kts
plugins {
    id("org.owasp.dependencycheck") version "8.4.0"
}

dependencyCheck {
    // ✅ 취약점 데이터베이스 자동 업데이트
    autoUpdate = true
    
    // ✅ 심각도 설정
    failBuildOnCVSS = 7.0f  // CVSS 7.0 이상이면 빌드 실패
    
    // ✅ 리포트 형식
    formats = listOf("HTML", "JSON", "XML")
    
    // ✅ 제외 항목
    suppressionFile = "dependency-check-suppressions.xml"
}

// CI/CD에서 실행
// ./gradlew dependencyCheckAnalyze
```

**Snyk**

```bash
# Snyk 설치
npm install -g snyk

# 프로젝트 테스트
snyk test --all-projects

# 취약점 모니터링
snyk monitor
```

### 코드 취약점 분석

**SonarQube**

```kotlin
// build.gradle.kts
plugins {
    id("org.sonarqube") version "4.0.0.2929"
}

sonarqube {
    properties {
        property("sonar.projectKey", "my-android-app")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.login", "your-token")
        
        // ✅ 보안 규칙 활성화
        property("sonar.java.binaries", "build/intermediates/javac")
        property("sonar.android.lint.report", "build/reports/lint-results.xml")
    }
}

// 분석 실행
// ./gradlew sonarqube
```

**SonarQube 보안 규칙:**
- SQL Injection
- XSS
- 하드코딩된 비밀번호
- 약한 암호화
- 안전하지 않은 난수 생성

### 네트워크 취약점 테스트

**Burp Suite**

```kotlin
/**
 * Burp Suite를 통한 네트워크 테스트
 * 
 * 1. 프록시 설정
 * 2. SSL Pinning 우회 (테스트용)
 * 3. 요청/응답 분석
 */
class BurpSuiteTest {
    
    /**
     * SSL Pinning 우회 (테스트 환경만)
     */
    fun bypassSslPinning() {
        // ⚠️ 테스트 환경에서만 사용!
        
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        
        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }
}
```

**테스트 시나리오:**
1. **중간자 공격 (MITM)**: SSL Pinning 확인
2. **요청 변조**: 파라미터 조작, 헤더 변조
3. **응답 변조**: 클라이언트 검증 확인
4. **세션 하이재킹**: 토큰 탈취 시도

---

## 침투 테스트

### Man-in-the-Middle 테스트

```kotlin
/**
 * MITM 공격 시뮬레이션
 */
@Test
fun testMitmResistance() {
    // 1. 프록시 서버 설정
    val proxyHost = "127.0.0.1"
    val proxyPort = 8080
    
    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort))
    
    // 2. 프록시를 통한 요청
    val client = OkHttpClient.Builder()
        .proxy(proxy)
        .build()
    
    // 3. API 호출
    val request = Request.Builder()
        .url("https://api.example.com/users")
        .build()
    
    try {
        val response = client.newCall(request).execute()
        
        // ✅ Certificate Pinning이 있으면 실패해야 함
        fail("MITM 공격에 취약함")
        
    } catch (e: SSLPeerUnverifiedException) {
        // ✅ 예상된 동작: Certificate Pinning이 MITM 차단
        assertTrue(true)
    }
}
```

### SQL Injection 테스트

```kotlin
/**
 * SQL Injection 취약점 테스트
 */
@Test
fun testSqlInjection() {
    val database = getDatabase()
    
    // ❌ 취약한 쿼리
    val maliciousInput = "'; DROP TABLE users; --"
    
    try {
        // 직접 문자열 연결 (취약)
        val query = "SELECT * FROM users WHERE name = '$maliciousInput'"
        database.rawQuery(query, null)
        
        fail("SQL Injection에 취약함")
        
    } catch (e: SQLException) {
        // 예외 발생 (취약)
        fail("SQL Injection에 취약함")
    }
    
    // ✅ 안전한 쿼리 (Prepared Statement)
    val safeQuery = "SELECT * FROM users WHERE name = ?"
    val cursor = database.rawQuery(safeQuery, arrayOf(maliciousInput))
    
    // 정상 동작 확인
    assertNotNull(cursor)
    cursor.close()
}
```

### XSS 테스트

```kotlin
/**
 * XSS (Cross-Site Scripting) 테스트
 */
@Test
fun testXssProtection() {
    // WebView XSS 테스트
    val webView = WebView(context)
    
    // ❌ 취약한 코드
    val userInput = "<script>alert('XSS')</script>"
    
    // JavaScript 활성화 확인
    assertFalse("JavaScript가 활성화됨",
        webView.settings.javaScriptEnabled)
    
    // ✅ 안전한 처리
    val sanitizedInput = sanitizeHtml(userInput)
    assertFalse("XSS 스크립트가 제거되지 않음",
        sanitizedInput.contains("<script>"))
}

/**
 * HTML 새니타이제이션
 */
fun sanitizeHtml(input: String): String {
    return input
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
        .replace("/", "&#x2F;")
}
```

### 인증 우회 테스트

```kotlin
/**
 * 인증 우회 시도 테스트
 */
@Test
fun testAuthenticationBypass() {
    val apiService = getApiService()
    
    // 1. 토큰 없이 요청
    try {
        runBlocking {
            apiService.getProtectedResource(token = null)
        }
        fail("인증 없이 접근 가능")
    } catch (e: HttpException) {
        assertEquals("401 Unauthorized 반환되어야 함", 401, e.code())
    }
    
    // 2. 만료된 토큰으로 요청
    val expiredToken = "expired_token_12345"
    try {
        runBlocking {
            apiService.getProtectedResource(token = expiredToken)
        }
        fail("만료된 토큰으로 접근 가능")
    } catch (e: HttpException) {
        assertEquals("401 Unauthorized 반환되어야 함", 401, e.code())
    }
    
    // 3. 변조된 토큰으로 요청
    val tamperedToken = "tampered_token_67890"
    try {
        runBlocking {
            apiService.getProtectedResource(token = tamperedToken)
        }
        fail("변조된 토큰으로 접근 가능")
    } catch (e: HttpException) {
        assertEquals("401 Unauthorized 반환되어야 함", 401, e.code())
    }
}
```

---

## 보안 모니터링

### 런타임 보안 모니터링

```kotlin
/**
 * 런타임 보안 이벤트 모니터링
 */
class SecurityMonitor(private val context: Context) {
    
    private val securityEvents = mutableListOf<SecurityEvent>()
    
    /**
     * 보안 이벤트 기록
     */
    fun logSecurityEvent(event: SecurityEvent) {
        securityEvents.add(event)
        
        // 심각한 이벤트는 즉시 보고
        if (event.severity == Severity.CRITICAL) {
            reportCriticalEvent(event)
        }
        
        // 로컬 저장
        saveEventToStorage(event)
        
        // 서버로 전송 (배칭)
        if (securityEvents.size >= 10) {
            sendEventsToServer(securityEvents.toList())
            securityEvents.clear()
        }
    }
    
    /**
     * 이상 탐지
     */
    fun detectAnomalies() {
        // 1. 비정상적인 API 호출 패턴
        val apiCallRate = getApiCallRate()
        if (apiCallRate > 100) {  // 분당 100회 초과
            logSecurityEvent(SecurityEvent(
                type = EventType.ABNORMAL_API_USAGE,
                severity = Severity.HIGH,
                message = "비정상적인 API 호출: $apiCallRate/min"
            ))
        }
        
        // 2. 비정상적인 위치 접근
        val locationAccessCount = getLocationAccessCount()
        if (locationAccessCount > 50) {  // 시간당 50회 초과
            logSecurityEvent(SecurityEvent(
                type = EventType.ABNORMAL_LOCATION_ACCESS,
                severity = Severity.MEDIUM,
                message = "비정상적인 위치 접근: $locationAccessCount/hour"
            ))
        }
        
        // 3. 반복된 로그인 실패
        val failedLoginCount = getFailedLoginCount()
        if (failedLoginCount >= 5) {
            logSecurityEvent(SecurityEvent(
                type = EventType.BRUTE_FORCE_ATTEMPT,
                severity = Severity.CRITICAL,
                message = "무차별 대입 공격 의심: $failedLoginCount 실패"
            ))
            
            // 계정 잠금
            lockAccount()
        }
    }
    
    /**
     * 보안 메트릭 수집
     */
    fun collectSecurityMetrics(): SecurityMetrics {
        return SecurityMetrics(
            totalEvents = securityEvents.size,
            criticalEvents = securityEvents.count { it.severity == Severity.CRITICAL },
            highEvents = securityEvents.count { it.severity == Severity.HIGH },
            mediumEvents = securityEvents.count { it.severity == Severity.MEDIUM },
            lowEvents = securityEvents.count { it.severity == Severity.LOW }
        )
    }
    
    private fun reportCriticalEvent(event: SecurityEvent) {
        // 즉시 서버로 전송
        // 알림 표시
    }
    
    private fun saveEventToStorage(event: SecurityEvent) {
        // 로컬 DB에 저장
    }
    
    private fun sendEventsToServer(events: List<SecurityEvent>) {
        // 서버로 배치 전송
    }
    
    private fun getApiCallRate(): Int = 0
    private fun getLocationAccessCount(): Int = 0
    private fun getFailedLoginCount(): Int = 0
    private fun lockAccount() {}
}

data class SecurityEvent(
    val type: EventType,
    val severity: Severity,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class EventType {
    ABNORMAL_API_USAGE,
    ABNORMAL_LOCATION_ACCESS,
    BRUTE_FORCE_ATTEMPT,
    ROOTED_DEVICE_DETECTED,
    DEBUGGER_DETECTED,
    TAMPERED_APK_DETECTED
}

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class SecurityMetrics(
    val totalEvents: Int,
    val criticalEvents: Int,
    val highEvents: Int,
    val mediumEvents: Int,
    val lowEvents: Int
)
```

### 보안 로깅

```kotlin
/**
 * 보안 감사 로그
 */
class AuditLogger(private val context: Context) {
    
    /**
     * 사용자 활동 로깅
     */
    fun logUserActivity(activity: UserActivity) {
        val logEntry = AuditLog(
            timestamp = System.currentTimeMillis(),
            userId = activity.userId,
            action = activity.action,
            resource = activity.resource,
            result = activity.result,
            ipAddress = getIpAddress(),
            deviceId = getDeviceId()
        )
        
        // 암호화하여 저장
        saveEncryptedLog(logEntry)
        
        // 서버로 전송
        sendLogToServer(logEntry)
    }
    
    /**
     * 데이터 접근 로깅
     */
    fun logDataAccess(
        userId: String,
        dataType: String,
        action: String
    ) {
        logUserActivity(UserActivity(
            userId = userId,
            action = action,
            resource = dataType,
            result = "SUCCESS"
        ))
    }
    
    /**
     * 보안 이벤트 로깅
     */
    fun logSecurityEvent(
        eventType: String,
        severity: String,
        details: String
    ) {
        val logEntry = SecurityAuditLog(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            severity = severity,
            details = details,
            deviceInfo = getDeviceInfo()
        )
        
        saveEncryptedLog(logEntry)
        sendLogToServer(logEntry)
    }
    
    private fun saveEncryptedLog(log: Any) {
        // 암호화하여 로컬 저장
    }
    
    private fun sendLogToServer(log: Any) {
        // 서버로 전송
    }
    
    private fun getIpAddress(): String = "192.168.1.1"
    private fun getDeviceId(): String = "device123"
    private fun getDeviceInfo(): String = "Android 13, Samsung Galaxy S21"
}

data class UserActivity(
    val userId: String,
    val action: String,
    val resource: String,
    val result: String
)

data class AuditLog(
    val timestamp: Long,
    val userId: String,
    val action: String,
    val resource: String,
    val result: String,
    val ipAddress: String,
    val deviceId: String
)

data class SecurityAuditLog(
    val timestamp: Long,
    val eventType: String,
    val severity: String,
    val details: String,
    val deviceInfo: String
)
```

### 인시던트 대응

```kotlin
/**
 * 보안 인시던트 대응
 */
class IncidentResponse(
    private val context: Context,
    private val securityMonitor: SecurityMonitor
) {
    
    /**
     * 인시던트 감지 및 대응
     */
    fun handleIncident(incident: SecurityIncident) {
        // 1. 인시던트 기록
        logIncident(incident)
        
        // 2. 심각도에 따른 대응
        when (incident.severity) {
            Severity.CRITICAL -> handleCriticalIncident(incident)
            Severity.HIGH -> handleHighIncident(incident)
            Severity.MEDIUM -> handleMediumIncident(incident)
            Severity.LOW -> handleLowIncident(incident)
        }
        
        // 3. 알림
        notifySecurityTeam(incident)
    }
    
    /**
     * 치명적 인시던트 처리
     */
    private fun handleCriticalIncident(incident: SecurityIncident) {
        // 1. 앱 즉시 종료
        if (incident.type == IncidentType.APK_TAMPERED) {
            exitProcess(0)
        }
        
        // 2. 세션 무효화
        invalidateAllSessions()
        
        // 3. 로컬 데이터 삭제
        if (incident.type == IncidentType.DEVICE_COMPROMISED) {
            wipeLocalData()
        }
        
        // 4. 서버에 긴급 보고
        reportToServer(incident, urgent = true)
    }
    
    /**
     * 높은 수준 인시던트 처리
     */
    private fun handleHighIncident(incident: SecurityIncident) {
        // 1. 민감한 기능 차단
        disableSensitiveFeatures()
        
        // 2. 추가 인증 요구
        requireAdditionalAuth()
        
        // 3. 경고 표시
        showSecurityWarning(incident.message)
    }
    
    /**
     * 중간 수준 인시던트 처리
     */
    private fun handleMediumIncident(incident: SecurityIncident) {
        // 1. 사용자에게 알림
        showNotification(incident.message)
        
        // 2. 로그 기록
        logIncident(incident)
    }
    
    /**
     * 낮은 수준 인시던트 처리
     */
    private fun handleLowIncident(incident: SecurityIncident) {
        // 로그만 기록
        logIncident(incident)
    }
    
    private fun logIncident(incident: SecurityIncident) {}
    private fun notifySecurityTeam(incident: SecurityIncident) {}
    private fun invalidateAllSessions() {}
    private fun wipeLocalData() {}
    private fun reportToServer(incident: SecurityIncident, urgent: Boolean) {}
    private fun disableSensitiveFeatures() {}
    private fun requireAdditionalAuth() {}
    private fun showSecurityWarning(message: String) {}
    private fun showNotification(message: String) {}
}

data class SecurityIncident(
    val type: IncidentType,
    val severity: Severity,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class IncidentType {
    APK_TAMPERED,
    DEVICE_COMPROMISED,
    BRUTE_FORCE_ATTACK,
    ABNORMAL_BEHAVIOR,
    DATA_BREACH_ATTEMPT
}
```

---

## 실전 시나리오

### 시나리오 1: 보안 감사 준비

```kotlin
/**
 * 보안 감사 체크리스트
 */
class SecurityAuditChecklist {
    
    /**
     * 감사 실행
     */
    fun performAudit(): AuditReport {
        val results = mutableListOf<AuditResult>()
        
        // 1. 코드 보안
        results.add(checkCodeSecurity())
        
        // 2. 데이터 보안
        results.add(checkDataSecurity())
        
        // 3. 네트워크 보안
        results.add(checkNetworkSecurity())
        
        // 4. 인증 및 권한
        results.add(checkAuthSecurity())
        
        // 5. 앱 무결성
        results.add(checkAppIntegrity())
        
        return AuditReport(
            results = results,
            overallScore = calculateScore(results),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 코드 보안 검사
     */
    private fun checkCodeSecurity(): AuditResult {
        val checks = mutableListOf<Check>()
        
        // ProGuard/R8 활성화
        checks.add(Check(
            name = "ProGuard/R8 활성화",
            passed = isMinifyEnabled(),
            severity = Severity.HIGH
        ))
        
        // 하드코딩된 비밀 확인
        checks.add(Check(
            name = "하드코딩된 비밀 없음",
            passed = !hasHardcodedSecrets(),
            severity = Severity.CRITICAL
        ))
        
        // 디버그 빌드 확인
        checks.add(Check(
            name = "Release 빌드",
            passed = !BuildConfig.DEBUG,
            severity = Severity.HIGH
        ))
        
        return AuditResult(
            category = "코드 보안",
            checks = checks
        )
    }
    
    /**
     * 데이터 보안 검사
     */
    private fun checkDataSecurity(): AuditResult {
        val checks = mutableListOf<Check>()
        
        // 암호화된 저장소 사용
        checks.add(Check(
            name = "암호화된 저장소 사용",
            passed = usesEncryptedStorage(),
            severity = Severity.CRITICAL
        ))
        
        // 로그에 민감한 정보 없음
        checks.add(Check(
            name = "로그 보안",
            passed = !logsContainSensitiveData(),
            severity = Severity.HIGH
        ))
        
        return AuditResult(
            category = "데이터 보안",
            checks = checks
        )
    }
    
    /**
     * 네트워크 보안 검사
     */
    private fun checkNetworkSecurity(): AuditResult {
        val checks = mutableListOf<Check>()
        
        // HTTPS 강제
        checks.add(Check(
            name = "HTTPS 강제",
            passed = enforcesHttps(),
            severity = Severity.CRITICAL
        ))
        
        // Certificate Pinning
        checks.add(Check(
            name = "Certificate Pinning",
            passed = hasCertificatePinning(),
            severity = Severity.HIGH
        ))
        
        return AuditResult(
            category = "네트워크 보안",
            checks = checks
        )
    }
    
    /**
     * 인증 보안 검사
     */
    private fun checkAuthSecurity(): AuditResult {
        val checks = mutableListOf<Check>()
        
        // 토큰 암호화
        checks.add(Check(
            name = "토큰 암호화",
            passed = encryptsTokens(),
            severity = Severity.CRITICAL
        ))
        
        // 세션 타임아웃
        checks.add(Check(
            name = "세션 타임아웃",
            passed = hasSessionTimeout(),
            severity = Severity.MEDIUM
        ))
        
        return AuditResult(
            category = "인증 보안",
            checks = checks
        )
    }
    
    /**
     * 앱 무결성 검사
     */
    private fun checkAppIntegrity(): AuditResult {
        val checks = mutableListOf<Check>()
        
        // 루팅 탐지
        checks.add(Check(
            name = "루팅 탐지",
            passed = hasRootDetection(),
            severity = Severity.HIGH
        ))
        
        // 디버깅 방지
        checks.add(Check(
            name = "디버깅 방지",
            passed = hasDebugDetection(),
            severity = Severity.MEDIUM
        ))
        
        return AuditResult(
            category = "앱 무결성",
            checks = checks
        )
    }
    
    /**
     * 점수 계산
     */
    private fun calculateScore(results: List<AuditResult>): Int {
        val totalChecks = results.sumOf { it.checks.size }
        val passedChecks = results.sumOf { result ->
            result.checks.count { it.passed }
        }
        
        return (passedChecks * 100) / totalChecks
    }
    
    // 헬퍼 메서드들
    private fun isMinifyEnabled(): Boolean = true
    private fun hasHardcodedSecrets(): Boolean = false
    private fun usesEncryptedStorage(): Boolean = true
    private fun logsContainSensitiveData(): Boolean = false
    private fun enforcesHttps(): Boolean = true
    private fun hasCertificatePinning(): Boolean = true
    private fun encryptsTokens(): Boolean = true
    private fun hasSessionTimeout(): Boolean = true
    private fun hasRootDetection(): Boolean = true
    private fun hasDebugDetection(): Boolean = true
}

data class AuditReport(
    val results: List<AuditResult>,
    val overallScore: Int,
    val timestamp: Long
)

data class AuditResult(
    val category: String,
    val checks: List<Check>
)

data class Check(
    val name: String,
    val passed: Boolean,
    val severity: Severity
)
```

### 시나리오 2: 취약점 수정 프로세스

```kotlin
/**
 * 취약점 수정 워크플로우
 */
class VulnerabilityFixWorkflow {
    
    /**
     * 1단계: 취약점 발견
     */
    fun discoverVulnerabilities(): List<Vulnerability> {
        val vulnerabilities = mutableListOf<Vulnerability>()
        
        // 정적 분석
        vulnerabilities.addAll(runStaticAnalysis())
        
        // 동적 분석
        vulnerabilities.addAll(runDynamicAnalysis())
        
        // 의존성 검사
        vulnerabilities.addAll(checkDependencies())
        
        return vulnerabilities
    }
    
    /**
     * 2단계: 우선순위 지정
     */
    fun prioritizeVulnerabilities(vulnerabilities: List<Vulnerability>): List<Vulnerability> {
        return vulnerabilities.sortedWith(
            compareByDescending<Vulnerability> { it.severity }
                .thenByDescending { it.exploitability }
                .thenByDescending { it.impact }
        )
    }
    
    /**
     * 3단계: 수정
     */
    fun fixVulnerability(vulnerability: Vulnerability): FixResult {
        return when (vulnerability.type) {
            VulnerabilityType.SQL_INJECTION -> fixSqlInjection(vulnerability)
            VulnerabilityType.XSS -> fixXss(vulnerability)
            VulnerabilityType.WEAK_CRYPTO -> fixWeakCrypto(vulnerability)
            VulnerabilityType.INSECURE_STORAGE -> fixInsecureStorage(vulnerability)
            else -> FixResult.NotImplemented
        }
    }
    
    /**
     * 4단계: 검증
     */
    fun verifyFix(vulnerability: Vulnerability): Boolean {
        // 수정 후 재테스트
        val stillVulnerable = testVulnerability(vulnerability)
        return !stillVulnerable
    }
    
    /**
     * 5단계: 문서화
     */
    fun documentFix(vulnerability: Vulnerability, fix: FixResult) {
        val documentation = FixDocumentation(
            vulnerability = vulnerability,
            fix = fix,
            timestamp = System.currentTimeMillis(),
            tester = "Security Team"
        )
        
        saveDocumentation(documentation)
    }
    
    private fun runStaticAnalysis(): List<Vulnerability> = emptyList()
    private fun runDynamicAnalysis(): List<Vulnerability> = emptyList()
    private fun checkDependencies(): List<Vulnerability> = emptyList()
    private fun fixSqlInjection(v: Vulnerability): FixResult = FixResult.Fixed
    private fun fixXss(v: Vulnerability): FixResult = FixResult.Fixed
    private fun fixWeakCrypto(v: Vulnerability): FixResult = FixResult.Fixed
    private fun fixInsecureStorage(v: Vulnerability): FixResult = FixResult.Fixed
    private fun testVulnerability(v: Vulnerability): Boolean = false
    private fun saveDocumentation(doc: FixDocumentation) {}
}

data class Vulnerability(
    val type: VulnerabilityType,
    val severity: Severity,
    val exploitability: Int,  // 1-10
    val impact: Int,  // 1-10
    val description: String,
    val location: String
)

enum class VulnerabilityType {
    SQL_INJECTION,
    XSS,
    WEAK_CRYPTO,
    INSECURE_STORAGE,
    HARDCODED_SECRET
}

sealed class FixResult {
    object Fixed : FixResult()
    object PartiallyFixed : FixResult()
    object NotImplemented : FixResult()
}

data class FixDocumentation(
    val vulnerability: Vulnerability,
    val fix: FixResult,
    val timestamp: Long,
    val tester: String
)
```

### 시나리오 3: 보안 인증 획득

```kotlin
/**
 * 보안 인증 준비 (예: PCI DSS, HIPAA)
 */
class SecurityCertificationPrep {
    
    /**
     * PCI DSS 준수 확인
     */
    fun checkPciDssCompliance(): ComplianceReport {
        val requirements = listOf(
            // Requirement 1: 방화벽 설정
            checkRequirement("방화벽 설정") {
                hasNetworkSecurityConfig() && enforcesHttps()
            },
            
            // Requirement 2: 기본 설정 변경
            checkRequirement("기본 설정 변경") {
                !usesDefaultPasswords() && !usesDefaultKeys()
            },
            
            // Requirement 3: 저장된 카드 데이터 보호
            checkRequirement("카드 데이터 암호화") {
                encryptsCardData() && !storesFullCardNumber()
            },
            
            // Requirement 4: 전송 중 데이터 암호화
            checkRequirement("전송 암호화") {
                usesTls() && hasCertificatePinning()
            },
            
            // Requirement 6: 보안 시스템 유지
            checkRequirement("보안 업데이트") {
                hasSecurityPatches() && noKnownVulnerabilities()
            },
            
            // Requirement 8: 사용자 식별 및 인증
            checkRequirement("강력한 인증") {
                hasStrongAuth() && hasSessionTimeout()
            },
            
            // Requirement 10: 네트워크 리소스 접근 추적
            checkRequirement("접근 로깅") {
                logsAllAccess() && hasAuditTrail()
            }
        )
        
        return ComplianceReport(
            standard = "PCI DSS",
            requirements = requirements,
            compliant = requirements.all { it.passed }
        )
    }
    
    private fun checkRequirement(name: String, check: () -> Boolean): ComplianceRequirement {
        return ComplianceRequirement(
            name = name,
            passed = check()
        )
    }
    
    // 헬퍼 메서드들
    private fun hasNetworkSecurityConfig(): Boolean = true
    private fun enforcesHttps(): Boolean = true
    private fun usesDefaultPasswords(): Boolean = false
    private fun usesDefaultKeys(): Boolean = false
    private fun encryptsCardData(): Boolean = true
    private fun storesFullCardNumber(): Boolean = false
    private fun usesTls(): Boolean = true
    private fun hasCertificatePinning(): Boolean = true
    private fun hasSecurityPatches(): Boolean = true
    private fun noKnownVulnerabilities(): Boolean = true
    private fun hasStrongAuth(): Boolean = true
    private fun hasSessionTimeout(): Boolean = true
    private fun logsAllAccess(): Boolean = true
    private fun hasAuditTrail(): Boolean = true
}

data class ComplianceReport(
    val standard: String,
    val requirements: List<ComplianceRequirement>,
    val compliant: Boolean
)

data class ComplianceRequirement(
    val name: String,
    val passed: Boolean
)
```

---

## 💡 베스트 프랙티스 요약

### 보안 테스트 자동화
- ✅ OWASP MSTG 체크리스트 활용
- ✅ Android Lint 설정
- ✅ SpotBugs 통합
- ✅ MobSF로 정기 분석

### 취약점 스캔
- ✅ OWASP Dependency-Check
- ✅ Snyk 모니터링
- ✅ SonarQube 통합
- ✅ Burp Suite 테스트

### 침투 테스트
- ✅ MITM 공격 시뮬레이션
- ✅ SQL Injection 테스트
- ✅ XSS 테스트
- ✅ 인증 우회 테스트

### 보안 모니터링
- ✅ 런타임 모니터링
- ✅ 이상 탐지
- ✅ 보안 로깅
- ✅ 인시던트 대응

### CI/CD 통합
- ✅ 자동화된 보안 테스트
- ✅ 빌드 시 취약점 검사
- ✅ 배포 전 보안 검증

---

## 🎯 다음 단계

보안 테스팅을 마스터했습니다! 이제:

1. **정기적인 보안 테스트**: 주간/월간 보안 스캔
2. **CI/CD 통합**: 자동화된 보안 검사
3. **보안 교육**: 팀 전체 보안 인식 향상
4. **인증 획득**: PCI DSS, HIPAA 등

---

**마지막 업데이트**: 2024-12-04  
**작성자**: Antigravity AI Assistant

Happy Testing! 🔒
