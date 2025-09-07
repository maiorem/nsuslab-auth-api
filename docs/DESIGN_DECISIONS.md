# 설계 의도 및 기술 선택 이유

## 프로젝트 개요

본 프로젝트는 **확장 가능하고 보안성이 강화된 JWT 기반 인증 시스템**을 구현한 것입니다. 최대한 실제 프로덕션 환경에서 운영 가능한 수준의 인증 시스템을 목표로 설계했습니다.

## 1. 아키텍처 설계 철학

### 1.1 DDD 기반 레이어드 아키텍처

```
┌─────────────────┐
│   Interface     │ ← HTTP, 입력 검증, DTO 변환
├─────────────────┤
│   Application   │ ← 비즈니스 플로우, 트랜잭션 경계
├─────────────────┤
│   Domain        │ ← 핵심 비즈니스 로직, 도메인 규칙
├─────────────────┤
│ Infrastructure  │ ← DB, 외부 API, 캐시 구현체
└─────────────────┘
```

**선택 이유:**
- **관심사 분리**: 각 계층은 명확한 단일 책임을 가져 변경에 강건
- **테스트 용이성**: Domain 계층의 순수한 비즈니스 로직은 독립적 테스트 가능
- **확장성**: 새로운 기능 추가 시 계층별 영향도 최소화

### 1.2 모듈화 전략

```
modules/
├── auth/     ← JWT 토큰 관련 핵심 로직
├── jpa/      ← 데이터 접근 계층 설정
└── redis/    ← 캐싱 계층 설정

supports/
├── error/    ← 통합 예외 처리
└── logging/  ← 로깅 전략
```

**선택 이유:**
- **재사용성**: 다른 프로젝트에서 모듈 단위로 재사용 가능
- **의존성 관리**: 각 모듈의 의존성을 명확히 분리하여 순환 참조 방지
- **팀 협업**: 병렬 개발 가능

## 2. 주요 기술 선택과 근거

### 2.1 Java 21 + Spring Boot 3.x

**선택 이유:**
- **Virtual Threads**: 높은 동시성 처리 능력 (특히 I/O 집약적인 인증 작업에 유리)
- **Pattern Matching**: 더 안전하고 가독성 높은 코드 작성 가능
- **Records**: DTO 클래스의 보일러플레이트 코드 제거
- **GraalVM 호환성**: 필요시 네이티브 이미지 컴파일로 시작 시간 단축 가능

### 2.2 JWT vs 세션 기반 인증

**JWT 선택 이유:**
- **Stateless**: 서버 확장 시 세션 공유 문제 없음
- **Microservices 친화적**: 서비스 간 토큰 전달로 분산 인증 가능
- **Mobile/SPA 최적화**: 쿠키 의존성 없이 다양한 클라이언트 지원

**단점 보완:**
- Refresh Token을 통한 보안 강화
- Redis 캐싱으로 토큰 무효화 기능 구현

### 2.3 Redis-First, Database-Fallback 전략

```java
public Optional<User> findUserByRefreshToken(String refreshToken) {
    // 1. Redis에서 먼저 조회 (성능 우선)
    Optional<Long> userIdFromRedis = refreshTokenCache.getUserIdByToken(refreshToken);
    
    // 2. Redis 실패 시 DB 조회 (안정성 확보)
    if (userIdFromRedis.isEmpty()) {
        return userRepository.findByRefreshToken(refreshToken);
    }
    return userRepository.findById(userIdFromRedis.get());
}
```

**선택 이유:**
- **성능**: 90% 이상의 요청을 Redis(sub-ms)에서 처리
- **가용성**: Redis 장애 시에도 DB를 통해 서비스 지속
- **데이터 일관성**: DB를 Source of Truth로 유지하면서 캐시 무효화 전략 적용

### 2.4 양방향 토큰 매핑

```java
// Token → UserId 매핑 (토큰 검증용)
redisTemplate.opsForValue().set("refresh_token:" + token, userId.toString());

// UserId → Token 매핑 (중복 로그인 방지용)
redisTemplate.opsForValue().set("user_token:" + userId, token);
```

**선택 이유:**
- **보안**: 사용자당 하나의 활성 토큰만 허용하여 계정 탈취 위험 감소
- **성능**: O(1) 시간 복잡도로 빠른 토큰 검증 및 무효화

## 3. 보안 설계 고려사항

### 3.1 다층 보안 전략

1. **네트워크 계층**: HTTPS 강제, CORS 설정
2. **애플리케이션 계층**: Rate Limiting, Input Validation
3. **인증 계층**: JWT + Refresh Token, BCrypt 해싱
4. **데이터 계층**: SQL Injection 방지, 민감 정보 암호화

### 3.2 Rate Limiting 구현

```java
@RateLimit(key = "login", time = 300, count = 5, limitType = RateLimitType.IP)
```

**AOP 기반 선택 이유:**
- **관심사 분리**: 비즈니스 로직과 Rate Limiting 로직 분리
- **재사용성**: 어노테이션으로 쉬운 적용
- **유연성**: IP, USER, API 단위로 다양한 제한 전략 적용 가능

### 3.3 실패 처리 전략

- **계정 잠금**: 5회 실패 시 계정 일시 잠금으로 무차별 대입 공격 방지
- **점진적 지연**: 실패할 때마다 응답 시간 증가로 공격 난이도 증가
- **로그 수집**: 의심스러운 활동 모니터링을 위한 상세 로깅

## 4. 성능 및 확장성 전략

### 4.1 캐싱 전략

```java
// Hot Data는 Redis에, Cold Data는 DB에
Duration expiration = Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMs());
refreshTokenCache.saveToken(refreshToken, user.getId(), expiration);
```

**선택 이유:**
- **메모리 효율성**: TTL을 통한 자동 정리로 메모리 사용량 최적화
- **Cache Warming**: 애플리케이션 재시작 시 DB에서 캐시 복구 가능

### 4.2 데이터베이스 최적화

- **Connection Pool**: HikariCP로 커넥션 최적화
- **N+1 문제 방지**: Batch Fetch Size 설정
- **인덱싱**: 이메일, 리프레시 토큰에 대한 인덱스 설정


## 5. 운영 관점의 고려사항

### 5.1 모니터링 및 로깅

```java
@Slf4j
public class TokenService {
    public void saveRefreshToken(User user, String refreshToken) {
        // ... 비즈니스 로직
        log.debug("Refresh token saved for user: {}", user.getId());
    }
}
```

**구조화된 로깅:**
- 각 요청별 추적 가능한 로그 구조
- 성능 메트릭 수집을 위한 실행 시간 로깅
- 보안 이벤트 로깅 (로그인 실패, 의심스러운 접근 등)

### 5.2 Health Check 

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### 5.3 예외 처리 표준화

```java
public enum ErrorType {
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "잘못된 인증 정보입니다.");
}
```

**일관된 에러 처리:**
- 클라이언트가 파싱하기 쉬운 구조적 에러 응답
- HTTP 상태 코드와 비즈니스 에러 코드 매핑

### 5.4 테스트 전략

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    // 도메인 로직의 독립적 테스트
}
```
- **Unit Test**: 각 계층별 독립적 테스트
- **Integration Test**: 테스트 컨테이너 기반 격리된 통합 테스트 환경 제공
