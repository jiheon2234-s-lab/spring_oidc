이 리포지토리는 스프링 OAuth 2.0 라이브러리를 사용하지 않고 직접 구현해본 예시입니다.

demo

Spring Boot만을 사용하여 OAuth 2.0 로그인을 구현한 예시입니다. 로그인 시, 각각 받았던 정보들을 그대로 브라우저에 출력합니다.
구글 로그인은 OIDC를 사용하고, 네이버 로그인은 OAuth 2.0만을 사용합니다.

- [application.yml](demo/src/main/resources/application.yml)에 네이버와 Google OAuth 2.0의 client_id, client_secret을 넣으면 됩니다.
- 구글 클라우드 콘솔에서 `http://localhost:8080/custom/oauth2/code/google`을 리다이렉트 URI로 설정합니다.
- 네이버 클라우드 콘솔에서 `http://localhost:8080/custom/oauth2/code/naver`를 콜백 URL로 설정합니다.

frontend 없이 demo만으로 확인이 가능합니다.

---

frontend

프론트엔드에서 구글의 id_token을 받아 백엔드에 넘기고, 백엔드에서 구글의 공개 키로 id_token을 검증하는 예시입니다.
원래대로라면 절대 client_secret이 프론트엔드에 노출되면 안 되지만, 테스트용으로 이렇게 구현했습니다.

- Node.js가 설치되어 있어야 합니다.
- [nuxt.config.ts](frontend/nuxt.config.ts)에 구글의 client_id와 client_secret을 추가합니다. demo 프로젝트의 설정을 그대로 사용하면 됩니다.
- 구글 클라우드 콘솔에서 `http://localhost:3000/google-redirect`를 리다이렉트 URI로 추가합니다.
- 위의 demo 프로젝트가 구동 중이어야 정상적으로 실행됩니다.


