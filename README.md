# JWT/KAKAO 로그인 템플릿 (초기세팅)

## 주요 기능

*   **ERD 기반 도메인 모델:** `user`, `category`, `asset_detail`, `asset_info`, `custom_recommend`, `faq`, `branch`, `booking` 등 ERD에 정의된 데이터 모델 구현.
*   **RESTful API:** 각 도메인에 대한 CRUD(Create, Read, Update, Delete) 작업을 위한 기본적인 RESTful API 엔드포인트 제공.
*   **카카오 로그인 통합:** 외부 인증 시스템으로 카카오 간편 로그인 지원.
*   **JWT 기반 인증:** 로그인 성공 시 JWT(JSON Web Token)를 발급하여 API 요청에 대한 인증을 처리.
*   **Spring Security:** 보안 설정은 Spring Security를 통해 관리되며, 모든 API는 현재 `permitAll()`로 설정되어 있어 인증 없이 접근 가능. (추후 개발 시 재설정 필요)


  **`application.properties` 설정:**

    ```properties
    kakao.client.id=YOUR_KAKAO_REST_API_KEY
    kakao.redirect.uri=http://localhost:8080/kakao/callback
    ```
  **KAKAO API 개발자 페이지에서 "닉네임" "이메일" 동의설정 필요.**

## 프로젝트 빌드 및 실행



### 빌드

```bash
./gradlew build
```


## API 엔드포인트 (예시)

*   **카카오 로그인:** `POST /auth/kakao` (Request Body: `{"code": "YOUR_KAKAO_AUTH_CODE"}`)


## 기술 스택

*   Java 16+
*   Spring Framework (Core, Web MVC, Security)
*   MyBatis
*   Gradle
*   Lombok
*   Swagger (API 문서화)
*   MySQL (데이터베이스)
*   Kakao API
