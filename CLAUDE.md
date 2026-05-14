# 프로젝트 규칙

## 커밋 시 제외할 파일

다음 파일들은 절대 커밋하지 않는다:

- `HealthCareApp/app/src/main/java/com/example/healthcareapp/MainActivity.kt` — Google 로그인 클라이언트 ID 등 민감한 정보 포함
- `HealthCareApp/.idea/` — IDE 설정 파일
- `HealthCareApp/gradle.properties` — 로컬 JVM 경로 등 로컬 설정 포함
- `HealthCareApp/gradlew` — 불필요한 변경
