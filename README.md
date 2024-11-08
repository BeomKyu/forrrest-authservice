# Forrrest 프로젝트 개요
## 1. 아키텍처
### 마이크로서비스 아키텍처 기반
- 공통 모듈(common)과 인증 서비스(auth-service) 구조

## 2. Common Module
### 2.1 보안 컴포넌트

#### *Security Features*
참조:
- Token-based Authentication
- JWT(JSON Web Token) 기반의 인증 시스템을 구현하며, 다음과 같은 토큰 타입을 지원합니다:

```
  public enum TokenType {
    USER_ACCESS,    // 일반 사용자 접근 토큰
    USER_REFRESH,   // 일반 사용자 갱신 토큰
    PROFILE_ACCESS, // 프로필 접근 토큰
    PROFILE_REFRESH,// 프로필 갱신 토큰
    NONCE          // 일회성 토큰

}
```
### 2.2 핵심 컴포넌트
- TokenProvider: 토큰 생성 및 검증 인터페이스
- TokenAuthentication: Spring Security Authentication 구현체
- UserTokenAuthentication
- ProfileTokenAuthentication
- NonceTokenAuthentication
- AbstractTokenFilter: 토큰 타입별 필터 구현

## Auth Service
### 3.1 주요 엔티티
1. User: 사용자 정보 관리
  - email (unique)
  - password
  - username
2. profiles (OneToMany)
  - Profile: 사용자 프로필 관리
  - name
  - isDefault
  - user (ManyToOne)
3. RefreshToken: 토큰 갱신 관리
  - email (PK)
  - refreshToken
  - expiryDate
### 3.2 인증 흐름
1. 로그인/회원가입
2. 사용자 인증 후 User Access/Refresh 토큰 발급
3. 기본 프로필에 대한 Profile Access/Refresh 토큰 발급
4. 프로필 전환
5. 선택한 프로필에 대한 새로운 Profile 토큰 발급
6. 기존 User 토큰 유지
7. 토큰 갱신
8. Refresh 토큰을 통한 Access 토큰 재발급

## 4. 테스트 전략
### 4.1 단위 테스트
- Service Layer
- AuthService: 로그인, 토큰 갱신 검증
- ProfileService: 프로필 선택, 관리 검증
### 4.2 보안 테스트
- TokenFilter
- 유효한/유효하지 않은 토큰 검증
- 토큰 타입별 접근 제어 검증
## 5. 예외 처리
- CustomException을 통한 비즈니스 예외 처리
- ErrorCode를 통한 일관된 에러 응답 관리
- TokenExceptionHandler를 통한 보안 예외 처리
## 6. API 보안
- Bearer 토큰 기반 인증
- 경로별 토큰 타입 검증
- JWT를 통한 안전한 사용자/프로필 정보 전달
- 이 구조를 통해 멀티 프로필을 지원하는 안전하고 확장 가능한 인증 시스템을 구현했습니다.
