# DiaryFolder 기능 요구사항 및 구현 계획

## 1. 서비스 개요

**앱명**: HealthCareApp (ttamtam)  
**담당 범위**: DiaryFolder 기능 — 백엔드 API + Android 연동  
**대상 사용자**: PT 트레이너, 개인 운동 기록 사용자

---

## 2. 비즈니스 규칙

| 항목 | 규칙 |
|------|------|
| 폴더 이름 | 공백/이모지 허용, **trim 후 최대 18자**. 줄바꿈/탭/제어문자는 허용하지 않음 |
| 폴더 인원 | **최대 2명** |
| 초대 방식 | Airbridge 딥링크를 통한 초대 링크 공유 (폴더 멤버 누구나 발급 가능) |
| 역할 구분 | 없음. 생성자 포함 모두 동등한 멤버 |
| 폴더 나가기 | soft delete (`left_at` 기록). 모든 멤버가 나가면 폴더 `CLOSED` 처리 |
| 폴더 삭제 | **제공하지 않음**. 마지막 활성 멤버가 나가면 자동으로 `CLOSED` 전환 |
| 접근 권한 | **현재 활성 멤버만** 폴더와 폴더 내 일지 조회/접근 가능 |
| 과거 멤버 | `left_at IS NOT NULL`이면 현재는 조회/접근 불가. 단, 재초대 후 재입장은 가능 |
| CLOSED 상태 | 영구 비공개. 관리상 데이터는 남기되 일반 조회/접근 불가 |
| 재가입 | 가능. 유효한 초대 링크로 다시 들어오면 기존 멤버십을 재활성화 |
| 공유 상태 | 활성 멤버가 2명이면 "공유됨", 1명이면 "개인" |

---

## 3. 엔티티 모델링

### 3.1 기존 엔티티

```
User
├── id (PK, BIGINT AUTO_INCREMENT)
├── email (VARCHAR, UNIQUE, NOT NULL)
├── id_token (VARCHAR 2000)
└── nickname (VARCHAR)
```

### 3.2 신규 엔티티

```
DiaryFolder
├── id (PK, BIGINT AUTO_INCREMENT)
├── name (VARCHAR 18, NOT NULL)
├── status (ENUM: ACTIVE | CLOSED, DEFAULT ACTIVE)  -- 모든 멤버 나가면 CLOSED
├── created_at (DATETIME, NOT NULL)
└── updated_at (DATETIME, NOT NULL)                 -- 최근 수정일 (정렬 기준)

DiaryFolderMember (다대다 중간 테이블)
├── id (PK, BIGINT AUTO_INCREMENT)
├── folder_id (FK → DiaryFolder.id)
├── user_id (FK → User.id)
├── joined_at (DATETIME, NOT NULL)
└── left_at (DATETIME, NULL)                        -- NULL=활성, NOT NULL=나간 상태 (soft delete)

Invite (MVP 최소 초대 리소스)
├── id (PK, BIGINT AUTO_INCREMENT)
├── folder_id (FK → DiaryFolder.id)
├── token_hash (VARCHAR, NOT NULL, UNIQUE)
├── active (BOOLEAN, NOT NULL DEFAULT TRUE)
└── created_at (DATETIME, NOT NULL)
```

### 3.3 관계

```
User 1 ─── N DiaryFolderMember N ─── 1 DiaryFolder
```

- 한 유저는 여러 DiaryFolder에 속할 수 있음
- 한 DiaryFolder의 **활성 멤버** (`left_at IS NULL`)는 최대 2명
- 모든 멤버가 나가면 (`left_at IS NOT NULL`) DiaryFolder.status = `CLOSED`
- 과거 멤버는 기본적으로 접근할 수 없지만, 유효한 초대를 통해 다시 활성 멤버가 될 수 있음
- 재입장 시에는 기존 `DiaryFolderMember` row를 재활성화(`left_at = NULL`)하는 방식을 우선 사용
- `CLOSED` 폴더는 영구 비공개 상태로 관리상만 보존
- 폴더 삭제 API는 제공하지 않음

### 3.4 인덱스 고려사항

```sql
-- 폴더 목록 조회 (사용자별, 정렬)
INDEX idx_folder_member_user_active (user_id, left_at, folder_id)

-- 폴더 상태 조회
INDEX idx_diary_folder_status (status, updated_at)
```

---

## 4. 주요 시나리오 및 예외 처리

### 4.1 폴더 생성

| 시나리오 | 처리 |
|---------|------|
| 정상 생성 | DiaryFolder 생성 + DiaryFolderMember 1건 추가 (`left_at = NULL`) |
| 이름 형식 위반 | `400 INVALID_FOLDER_NAME` |

### 4.2 초대 링크 생성

| 시나리오 | 처리 |
|---------|------|
| 정상 | Airbridge 딥링크 생성 후 반환 |
| 요청자가 해당 폴더 멤버가 아님 | `403 FORBIDDEN` |
| 이미 활성 멤버 2명 | `409 FOLDER_FULL` |
| 폴더가 CLOSED 상태 | `400 FOLDER_CLOSED` |

### 4.3 초대 수락

| 시나리오 | 처리 |
|---------|------|
| 정상 수락 | 신규 멤버면 `DiaryFolderMember` 생성, 과거 멤버면 기존 row 재활성화 (`left_at = NULL`) |
| 유효하지 않은 토큰 | `400 INVALID_TOKEN` |
| 이미 활성 멤버인 경우 | `400 ALREADY_MEMBER` |
| 과거 멤버인 경우 | 유효한 초대면 재입장 허용 |
| 폴더 정원 초과 (활성 2명) | `409 FOLDER_FULL` |
| 폴더가 CLOSED 상태 | `400 FOLDER_CLOSED` |
| 동시 요청 발생 | 트랜잭션 내에서 활성 멤버 수 재검증 후 2명 초과 시 실패 |

### 4.4 폴더 나가기

| 시나리오 | 처리 |
|---------|------|
| 정상 나가기 | `left_at = now()` 세팅 |
| 나간 후 활성 멤버 수 = 0 | DiaryFolder.status = `CLOSED` |
| 이미 나간 멤버 | `400 ALREADY_LEFT` |
| 해당 폴더 멤버가 아님 | `403 FORBIDDEN` |

### 4.5 폴더 이름 수정

| 시나리오 | 처리 |
|---------|------|
| 정상 수정 | DiaryFolder.name 수정, updated_at 갱신 |
| 활성 멤버가 아닌 사람이 요청 | `403 FORBIDDEN` |
| CLOSED 폴더에 수정 요청 | `400 FOLDER_CLOSED` |
| 이름 형식 위반 | `400 INVALID_FOLDER_NAME` |

### 4.6 폴더 목록 조회

| 시나리오 | 처리 |
|---------|------|
| 정상 조회 | 활성 멤버인 폴더만 반환 (`left_at IS NULL`) |
| 과거 멤버였더라도 현재 활성 폴더가 없으면 | 빈 목록 반환 가능 |
| CLOSED 폴더 | 목록에서 제외 |
| 커서가 유효하지 않음 | `400 INVALID_CURSOR` |

### 4.7 폴더 단일 조회

| 시나리오 | 처리 |
|---------|------|
| 정상 조회 | 현재 활성 멤버인 경우 폴더 상세 + 포함된 멤버 정보 반환 |
| 해당 폴더의 현재 활성 멤버가 아님 | `403 FORBIDDEN` |
| 폴더가 존재하지 않음 | `404 FOLDER_NOT_FOUND` |
| CLOSED 폴더 조회 | `404 FOLDER_NOT_FOUND` 반환 (목록/상세 모두 비노출 정책) |

---

## 5. API 명세

### 5.1 폴더 목록 조회

```
GET /api/folders
```

**인증 필요**: 현재 로그인한 사용자 기준으로 본인의 활성 멤버 폴더만 조회 가능

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `cursor` | String | - | base64 인코딩된 커서 값 |
| `size` | Int | 20 | 페이지 크기 |
| `sort` | String | `UPDATED_AT` | `UPDATED_AT` \| `CREATED_AT` |
| `shared` | Boolean? | - | `true`=공유만, `false`=개인만, 없으면 전체 |

**Response** `200 OK`
```json
{
  "data": [
    {
      "folderId": 1,
      "name": "내 운동 일지",
      "isShared": false,
      "memberCount": 1,
      "members": [
        {
          "userId": 1,
          "nickname": "제로"
        }
      ],
      "createdAt": "2026-04-23T10:00:00",
      "updatedAt": "2026-04-23T10:00:00"
    }
  ],
  "nextCursor": "eyJpZCI6MX0=",
  "hasNext": true
}
```

> 목록 조회에서는 각 폴더에 포함된 멤버 정보를 함께 반환한다. MVP 기준 최대 2명이라 payload 부담이 크지 않다.
> `members` 배열에는 현재 활성 멤버만 포함되며, `memberCount`와 `isShared`도 동일한 활성 멤버 기준으로 계산한다.
> 멤버 정보는 MVP 기준 `userId`, `nickname`만 포함한다.

---

### 5.2 폴더 단일 조회

```
GET /api/folders/{folderId}
```

**인증 필요**: 해당 폴더의 현재 활성 멤버만 가능

**Response** `200 OK`
```json
{
  "folderId": 1,
  "name": "내 운동 일지",
  "isShared": true,
  "memberCount": 2,
  "members": [
    {
      "userId": 1,
      "nickname": "제로"
    },
    {
      "userId": 2,
      "nickname": "트레이너"
    }
  ],
  "createdAt": "2026-04-23T10:00:00",
  "updatedAt": "2026-04-23T11:00:00"
}
```

---

### 5.3 폴더 생성

```
POST /api/folders
```

**인증 필요**

**Request Body**
```json
{ "name": "내 운동 일지" }
```

**Validation**: `name` 필수, trim 후 1~18자
> 일반 문자(한글/영어/숫자/공백/이모지 포함)는 허용  
> 불허: 제어문자, 줄바꿈, 탭

**Response** `201 Created`
```json
{ "folderId": 1, "name": "내 운동 일지", "isShared": false, "createdAt": "..." }
```

---

### 5.4 폴더 이름 수정

```
PATCH /api/folders/{folderId}
```

**인증 필요**: 해당 폴더의 현재 활성 멤버만 가능

**Request Body**
```json
{ "name": "새 이름" }
```

**Response** `200 OK`
```json
{
  "folderId": 1,
  "name": "새 이름",
  "isShared": false,
  "createdAt": "2026-04-23T10:00:00",
  "updatedAt": "2026-04-23T12:00:00"
}
```

---

### 5.5 폴더 나가기

```
DELETE /api/folders/{folderId}/members/me
```

**인증 필요**: 해당 폴더의 현재 활성 멤버만 가능

**Response** `204 No Content`

---

### 5.6 초대 링크 생성

```
POST /api/folders/{folderId}/invite-link
```

**인증 필요**: 해당 폴더의 현재 활성 멤버만 가능

**Response** `200 OK`
```json
{ "inviteLink": "https://ttdev.airbridge.io/invite?folderId=1&token=xxxx" }
```

---

### 5.7 초대 수락

```
POST /api/folders/invite/accept
```

**인증 필요**: 로그인 완료 후 수락 처리

**Request Body**
```json
{ "folderId": 1, "token": "xxxx" }
```

**Response** `200 OK`
```json
{
  "folderId": 1,
  "name": "내 운동 일지",
  "isShared": true,
  "memberCount": 2,
  "members": [
    {
      "userId": 1,
      "nickname": "제로"
    },
    {
      "userId": 2,
      "nickname": "트레이너"
    }
  ],
  "createdAt": "2026-04-23T10:00:00",
  "updatedAt": "2026-04-23T11:00:00"
}
```

---

### 5.8 인증 및 응답 규약

#### 인증
- `/api/auth/google` 성공 후 백엔드가 **자체 access token**을 발급한다.
- MVP에서는 **access token만** 사용하며, refresh token은 도입하지 않는다.
- access token 만료 시간은 **30일**이다.
- 폴더 관련 API는 모두 `Authorization: Bearer <access-token>` 헤더를 사용한다.

#### 성공 응답
- 성공 응답은 공통 wrapper 없이 **API별 DTO를 직접 반환**한다.
- 목록/상세 응답에는 폴더에 포함된 멤버 정보를 함께 담을 수 있다.
- 멤버 정보는 MVP 기준 `userId`, `nickname`만 반환하며, role / email / profileImage 는 포함하지 않는다.
- 예: 폴더 생성 성공 시 `{ folderId, name, isShared, createdAt }`

#### 실패 응답
- 실패 응답은 공통 포맷을 사용한다.

```json
{
  "code": "FOLDER_FULL",
  "message": "폴더 정원이 가득 찼습니다.",
  "status": 409,
  "timestamp": "2026-04-23T12:00:00"
}
```

- 공통 에러 응답은 `GlobalExceptionHandler`에서 변환한다.

### 5.9 공통 백엔드 지원 구조 설계

`loopers-spring-java-template`의 공통 구조를 참고하되, `ttamtam`에는 필요한 범위만 가져온다.

#### 예외 / 에러 응답
- `ErrorCode` : HTTP status + code + message 정의
- `CoreException` : 서비스/도메인 계층에서 사용하는 공통 예외 베이스
- `ErrorResponse` : 실패 응답 DTO (`code`, `message`, `status`, `timestamp`)
- `GlobalExceptionHandler` : validation, JSON parse, custom exception, fallback 500 처리

#### 인증 / 보안
- `SecurityConfig` : Spring SecurityFilterChain 기본 설정, API 서버 기준 CSRF 비활성화
- `JwtTokenProvider` 또는 `AccessTokenProvider` : access token 생성/검증 담당
- `JwtAuthenticationFilter` 또는 동등한 Bearer 인증 필터 : 요청 헤더에서 access token 추출
- `AuthenticationEntryPoint` / `AccessDeniedHandler` : `401`, `403` 응답 일관화

#### 설정 / 프로퍼티
- `JwtProperties` : secret, 만료 시간(30일) 등 토큰 설정을 `@ConfigurationProperties`로 관리
- 필요 시 `GoogleAuthProperties`, `AirbridgeProperties` 등도 동일 패턴으로 확장 가능
- `application.yml` + 분리 설정 파일 구조를 참고하되, 현재 프로젝트 규모에서는 과도한 모듈 분리는 하지 않는다

#### 패키지 방향
- 현재 `controller/service/repository/Entity/dto` 구조는 유지한다
- 공통 코드는 `exception/`, `auth/` 또는 `security/`, `config/`, `support/` 성격의 패키지로 분리한다
- 폴더 도메인 코드는 기존 패키지 스타일을 유지하되, 공통 인프라와 섞이지 않도록 한다

> 참고 프로젝트: `/Users/sylee/Projects/loopers-spring-java-template`
> 특히 `ApiControllerAdvice`, `ErrorType`, `CoreException`, `SecurityConfig`, `application.yml`, `jpa.yml` 구조를 참고한다.

---

## 6. 에러 코드 정의

| 코드 | HTTP | 설명 |
|------|------|------|
| `FOLDER_NOT_FOUND` | 404 | 폴더를 찾을 수 없음 |
| `FOLDER_FULL` | 409 | 활성 멤버 2명으로 정원 초과 |
| `FOLDER_CLOSED` | 400 | 모든 멤버가 나간 닫힌 폴더 |
| `ALREADY_MEMBER` | 400 | 이미 해당 폴더의 활성 멤버 |
| `ALREADY_LEFT` | 400 | 이미 나간 상태 |
| `INVALID_FOLDER_NAME` | 400 | 폴더명 형식/길이 위반 |
| `INVALID_TOKEN` | 400 | 초대 토큰 유효하지 않음 |
| `INVALID_CURSOR` | 400 | 커서 값 형식 오류 |
| `FORBIDDEN` | 403 | 해당 폴더에 대한 접근 권한 없음 |

---

## 7. Airbridge 딥링크 설계

- **플랫폼**: Airbridge (앱명: `ttdev`)
- **딥링크 포맷**: `ttdev://invite?folderId={id}&token={token}`
- **Android**: AndroidManifest에 intent-filter 추가 필요

### 초대 설계

- Airbridge는 **초대 링크 전달 수단**으로 사용한다.
- 실제 초대 유효성 판단과 폴더 멤버 추가는 **백엔드에서 처리**한다.
- MVP에서는 **최소 Invite 테이블/리소스 방식**을 사용한다.
- 현재 기준 Invite는 아래 필드만 우선 관리한다.
    - `folder_id`
    - `token_hash`
    - `active`
    - `created_at`
- 현재 MVP 범위에서는 **만료 시간(expiry)**, **revoke**, **누가 생성했는지/누가 수락했는지 추적**은 포함하지 않는다.
- 초대 수락 시 서버는 아래를 검증한다.
    1. invite token 유효성
    2. 현재 로그인 사용자 여부
    3. 폴더 상태 (`CLOSED` 여부)
    4. 이미 활성 멤버인지 여부
    5. 활성 멤버 수 2명 초과 여부
- 과거 멤버는 유효한 초대를 통해 재입장할 수 있으며, 이 경우 기존 `DiaryFolderMember` row를 재활성화한다.
- 동시 수락 요청에서도 활성 멤버가 2명을 넘지 않도록 트랜잭션으로 제어한다.

---

## 8. 구현 계획 및 Task 순서

### Phase 1 — 백엔드 (Spring Boot)

| # | Task | 파일/위치 |
|---|------|-----------|
| 1 | `ErrorCode` / `CoreException` / `ErrorResponse` 정의 | `exception/ErrorCode.java` `exception/CoreException.java` `exception/ErrorResponse.java` |
| 2 | 글로벌 예외 처리 구현 | `exception/GlobalExceptionHandler.java` |
| 3 | `JwtProperties` 및 토큰 설정 정의 | `config/` 또는 `auth/` 패키지 |
| 4 | access token provider 구현 | `auth/JwtTokenProvider.java` 또는 동등 클래스 |
| 5 | `SecurityConfig` + Bearer 인증 필터 반영 | `security/SecurityConfig.java` `auth/JwtAuthenticationFilter.java` |
| 6 | `DiaryFolder` 엔티티 생성 | `Entity/DiaryFolder.java` |
| 7 | `DiaryFolderMember` 엔티티 생성 | `Entity/DiaryFolderMember.java` |
| 8 | `Invite` 엔티티/리소스 생성 | `Entity/Invite.java` |
| 9 | Repository 생성 | `repository/DiaryFolderRepository.java` `repository/DiaryFolderMemberRepository.java` `repository/InviteRepository.java` |
| 10 | DTO 생성 | `dto/folder/` |
| 11 | `DiaryFolderService` 구현 | `service/DiaryFolderService.java` |
| 12 | `InviteService` 구현 | `service/InviteService.java` |
| 13 | 커서 페이지네이션 유틸 | `util/CursorUtils.java` |
| 14 | `DiaryFolderController` 구현 (목록/단일조회/생성/수정/나가기/초대 API 포함) | `controller/DiaryFolderController.java` |
| 15 | `/api/auth/google` 응답을 access token 구조에 맞게 정리 | `controller/AuthController.java` `dto/auth/` |

### Phase 2 — Android 연동

| # | Task | 파일/위치 |
|---|------|-----------|
| 16 | 폴더 API 인터페이스 정의 | `network/DiaryFolderService.kt` |
| 17 | 폴더/멤버 DTO 모델 | `data/DiaryFolderDto.kt` `data/FolderMemberDto.kt` |
| 18 | auth 응답 DTO + access token 저장 모델 정리 | `data/UserResponse.kt` 또는 `data/auth/` |
| 19 | access token 저장/전달 방식 반영 | interceptor 또는 요청 헤더 처리 |
| 20 | `FolderActivity` 목록 조회 연동 | `FolderActivity.kt` |
| 21 | 폴더 생성/수정/나가기 UI 연동 | `FolderActivity.kt` |
| 22 | 딥링크 처리 (초대 수락) | `AndroidManifest.xml` + Activity |

### Phase 3 — 딥링크 설정

| # | Task | 설명 |
|---|------|------|
| 23 | AndroidManifest에 intent-filter 추가 | `ttdev://invite` scheme 처리 |
| 24 | 초대 수락 화면 구현 | 딥링크 진입 → 로그인 확인 → API 호출 → 폴더 진입 |

---

## 9. 현재 상태 (2026-04-23 기준)

- [x] Airbridge Android SDK 설치 완료 (v4.9.4)
- [x] Airbridge 앱 플랫폼 스토어 URL 등록 (기본값)
- [x] `App.kt` Application 클래스 생성 및 SDK 초기화
- [x] `local.properties`에 `AIRBRIDGE_APP_TOKEN` 설정 완료
- [ ] Phase 1~3 구현 예정
- [x] 현재 코드 기준 공통 API 응답 포맷 없음 (`UserResponse`만 존재)
- [x] 현재 코드 기준 폴더 API용 인증/인가 인프라 없음 (Spring Security / JWT / interceptor 미구현)

---

## 10. 미결정 사항

| 항목 | 내용 |
|------|------|
| 인증 방식 | **확정**. Google 로그인 후 백엔드가 자체 access token 발급, 폴더 API는 Bearer 인증 사용 |
| 초대 저장 방식 | **확정**. MVP에서는 최소 Invite 테이블/리소스 방식 사용 |
| 초대 토큰 만료 시간 | MVP에서는 미적용. 추후 필요 시 도입 검토 |
| 폴더명 정책 | **확정**. trim 후 1~18자, 공백/이모지 허용, 줄바꿈/탭/제어문자 금지 |
| 공통 응답 포맷 | **확정**. 성공은 DTO 직접 반환, 실패는 공통 에러 포맷 사용 |
