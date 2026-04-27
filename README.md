# 땀땀

- 프로젝트 소개
    
    PT트레이너 혹은 개인 운동자가 사용가능한, 운동 기록(일지) 및 폴더 공유 기능을 갖춘 헬스케어 앱으로, Spring Boot 백엔드 + Android 클라이언트 구조입니다.
    
- 기술 스택
    - be
        
        
        | 분류 | 기술 |
        | --- | --- |
        | 언어 | Java 21 |
        | 프레임워크 | Spring Boot 4.0.5 |
        | 보안 | Spring Security + JWT (jjwt 0.12.6) |
        | ORM | Spring Data JPA (Hibernate) |
        | DB | MySQL  |
        | OAuth 관련 | Google API Client (Google OAuth ID Token 검증) |
    - app
        
        
        | 분류 | 기술 |
        | --- | --- |
        | 언어 / 플랫폼 | Kotlin / Android (minSdk 24, targetSdk 35) |
        | UI | Jetpack Compose + XML View |
        | 네트워크 / 인증 | Retrofit2 |
        | 인증 연동 | Firebase Auth + Google Sign-In |
- 핵심 기능 3가지
    - 구글 로그인 및 인증
        - POST /api/auth/google (idToken + email + nickname)
        - Spring Security가 Google API로 토큰 검증
        - JWT 발급 → Android 저장 → 이후 요청마다 Bearer 헤더
    - 운동폴더 공유 (폴더 하위에 운동일지 개념 존재)
        - 사용자가 운동폴더 생성
        - 폴더에 대한 초대링크 발급 가능
        - 딥링크 통해 전달, 다른 사용자가 폴더 초대 수락 가능 (폴더 멤버로 추가)
    - 운동일지
        - 운동 후 신체부위 통증, 강도, 기분 등의 기록 일지 작성 가능
- 테이블 (*개발하면서 수정될 수 있습니다)
    - **`users`**
        
        서비스 사용자 정보를 저장합니다.
        
        ```
        id          BIGINT        사용자 PK
        email       VARCHAR       사용자 이메일, unique, not null
        id_token    VARCHAR(2000) Google OAuth ID Token
        nickname    VARCHAR       사용자 닉네임
        ```
        
    - **`diary_folder`**
        
        운동 일지를 함께 관리하는 폴더 정보를 저장합니다.
        
        ```
        id          BIGINT        다이어리 폴더 PK
        name        VARCHAR(18)   폴더 이름, not null
        status      ENUM          폴더 상태, ACTIVE / CLOSED, not null
        created_at  DATETIME      생성 시각, not null
        updated_at  DATETIME      수정 시각, not null
        ```
        
    - **`diary_folder_member`**
        
        사용자와 다이어리 폴더의 관계를 관리하는 중간 테이블입니다.
        
        ```
        id          BIGINT        폴더 멤버 PK
        folder_id   BIGINT        diary_folder.id 참조, not null
        user_id     BIGINT        users.id 참조, not null
        joined_at   DATETIME      폴더 참여 시각, not null
        left_at     DATETIME      폴더 탈퇴 시각, null이면 현재 활성 멤버
        ```
        
    - **`invite`**
        
        다이어리 폴더 초대 정보를 저장합니다.
        
        ```
        id          BIGINT        초대 PK
        folder_id   BIGINT        diary_folder.id 참조, not null
        token_hash  VARCHAR(64)   초대 토큰 해시값, unique, not null
        active      BOOLEAN       초대 활성 여부, not null
        created_at  DATETIME      생성 시각, not null
        ```
        
    - **`workout_journal`**
        
        운동 전/후 컨디션과 일지 내용을 저장하는 핵심 테이블입니다.
        
        ```
        id                       BIGINT         운동 일지 PK
        folder_id                BIGINT         다이어리 폴더 ID, FK 선언 없음, 원시 참조
        author_id                BIGINT         작성자 ID, FK 선언 없음, 원시 참조
        
        pre_joint_muscle_pain    INT            운동 전 관절/근육 통증
        pre_sleep_hours          INT            운동 전 수면 시간
        pre_sleep_quality        INT            운동 전 수면 질
        pre_previous_fatigue     INT            운동 전 이전 피로도
        pre_overall_condition    INT            운동 전 전반적인 컨디션
        
        post_joint_muscle_pain   INT            운동 후 관절/근육 통증, nullable
        post_intensity_fit       INT            운동 후 강도 적합도, nullable
        post_goal_achieved       INT            운동 후 목표 달성도, nullable
        post_dizziness           INT            운동 후 어지러움 정도, nullable
        post_mood                INT            운동 후 기분 상태, nullable
        post_recorded_at         DATETIME       운동 후 기록 시각, nullable
        
        content                  VARCHAR(5000)  운동 일지 내용, nullable
        created_at               DATETIME       생성 시각
        updated_at               DATETIME       수정 시각
        deleted_at               DATETIME       삭제 시각, soft delete 용도
        ```
        
    - **`journal_pain_record`**
        
        운동 일지에 연결된 통증 기록을 저장합니다. 운동 전/후 시점별로 신체 부위, 방향, 통증 정도를 기록합니다.
        
        ```
        id          BIGINT        통증 기록 PK
        journal_id  BIGINT        workout_journal.id 참조, not null
        timing      ENUM          통증 기록 시점, PRE / POST
        body_part   ENUM          통증 부위
        side        ENUM          통증 방향, LEFT / RIGHT / BOTH
        pain_level  INT           통증 정도, not null
        
        UNIQUE      (journal_id, timing, body_part, side)
        ```
        
- 주요 관계
    - users → diary_folder_member
        - 한 사용자는 여러 폴더에 참여할 수 있습니다.
        - users 1 : N diary_folder_member
    - diary_folder → diary_folder_member
        - 하나의 폴더는 여러 멤버를 가질 수 있습니다.
        - diary_folder 1 : N diary_folder_member
    - workout_journal → journal_pain_record
        - 하나의 운동 일지는 여러 통증 기록을 가질 수 있습니다.
        - workout_journal 1 : N journal_pain_record
