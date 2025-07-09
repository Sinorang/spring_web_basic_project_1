# 🎧음악 추천 및 공유 플랫폼
<img src="/src/main/resources/static/images/playlistWebImage_GIF.gif" width="450" alt="playlistWebImage GIF"/>

## 🎵 플레이리스트 공유 기능 설명

### 1. 기능의 목적
이 기능은 음악을 사랑하는 사용자들이 자신만의 플레이리스트를 쉽고 편리하게 공유할 수 있도록 하는 데 목적이 있습니다.
특히 YouTube(YouTube Music) 플레이리스트를 활용함으로써,
플랫폼에 구애받지 않고 누구나 자신이 즐겨 듣는 곡 모음을 다른 사람들과 나누고,
다른 사용자의 플레이리스트를 감상하며 새로운 음악을 발견할 수 있습니다.

## 2. 상세 기능

#### 🎼 YouTube 플레이리스트 연동
- 사용자는 자신의 **YouTube(또는 YouTube Music) 플레이리스트 URL**을 입력합니다.
- 입력한 URL에서 곡 정보(제목, 아티스트, 썸네일 등)를 **자동으로 불러옵니다**.

#### 💾 플레이리스트 및 곡 정보 저장
- 불러온 플레이리스트와 곡 정보는 **데이터베이스에 저장**되어,
- 웹사이트 내에서 **언제든지 조회**할 수 있습니다.

#### 📋 플레이리스트 상세 조회
- 저장된 플레이리스트의 곡 목록을 **리스트 형태**로 확인할 수 있습니다.
- 각 곡의 **제목, 아티스트, 썸네일 등 주요 정보**를 한눈에 볼 수 있습니다.

#### ▶️ 개별 곡 재생
- 곡 리스트에서 **특정 곡을 클릭**하면,
- 해당 곡의 **YouTube embed player**가 모달로 나타나 **바로 감상**할 수 있습니다.

#### 🔁 플레이리스트 전체 재생
- **"플레이리스트 전체 재생"** 버튼을 통해,
- 해당 플레이리스트의 모든 곡을 **유튜브 플레이리스트 embed player**로 **연속 재생**할 수 있습니다.

---

## 3. YouTube Data API 활용 방식

#### 1️⃣ 플레이리스트 ID 추출
- 사용자가 입력한 **YouTube 플레이리스트 URL**에서 **playlistId**를 추출합니다.

#### 2️⃣ API 호출
- **YouTube Data API v3**의
  `playlists` 및 `playlistItems` 엔드포인트를 사용하여
  플레이리스트의 **메타데이터(제목, 설명, 썸네일 등)** 와
  곡(영상) 정보(**제목, videoId, 아티스트, 썸네일, 순서 등**)를 가져옵니다.

#### 3️⃣ DB 저장
- 가져온 플레이리스트 정보는 **Playlist 엔티티**에,
- 곡 정보는 **PlaylistSong 엔티티**에 각각 저장합니다.

#### 4️⃣ 재생 기능 구현
- **개별 곡 재생**:
  각 곡의 videoId로 YouTube embed player를 모달로 띄워 재생  
  (예시: 
  ```html
  <iframe src="https://www.youtube.com/embed/{videoId}"></iframe>
  ```
  )
- **전체 재생**:
  playlistId로 YouTube 플레이리스트 embed player를 띄워
  곡들이 순서대로 자동 재생  
  (예시:
  ```html
  <iframe src="https://www.youtube.com/embed?listType=playlist&list={playlistId}"></iframe>
  ```
  )

---

## 📆프로젝트 일정
✅ **프로젝트 기간**
- 2024.04.22 ~ 2024.05.03

✅ **프로젝트 일정**
- 프로젝트 초기 설정(~04.22)
- 프로젝트 기획 및 ERD 작성(~04.23)
- 게시판 기능 MVP, 게시글 기능 MVP 설계 및 구현(~4.26)
- 코드 리팩토링(~4.29)
- 댓글 기능 MVP 구현(~4.30)
- 개발 프로세스 점검(~5.1)
- REST API 구현(~5/2)
- 코드 리팩토링(~5/3)

## ❓ 프로젝트 소개 : 개발하고자 하는 서비스
✅ **목적**
- 음악을 즐기는 사람들이 모여 자신의 플레이리스트를 공유하고 좋아하는 음악을 추천할 수 있는 음악 추천 및 공유 플랫폼을 구현합니다.

✅ **목표**
- 게시판 메인 페이지의 상단 네비게이터 바를 통해 여러 카테고리(플레이리스트 공유 | 국내, 해외 | 발라드, 힙합, ost 등) 별로, 글 주제를 확인할 수 있습니다.
- 회원 가입을 하지 않아도 게시글을 구경할 수 있습니다.
- 로그인 한 회원은 댓글 기능을 이용할 수 있습니다.
- (기능 구현 x : 투표) 자신이 좋아하는 노래나 가수 혹은 선호하는 음원 스트리밍 플랫폼 등 여러 주제를 투표 안건으로 올려 사용자들의 여론을 파악할 수 있습니다.

## 🗄️ 데이터베이스 구조 (ERD)
<img src="/src/main/resources/static/images/database_erd.png" width="500" alt="Database ERD"/>

**주요 엔티티 관계:**
- **User**: 시스템 사용자 (회원가입/로그인)
- **Board**: 게시판 (User가 생성)
- **Post**: 게시글 (User가 작성, Board에 속함)
- **Comment**: 댓글 (User가 작성, Post에 속함)

**관계 구조:**
- User (1) ←→ (N) Board, Post, Comment
- Board (1) ←→ (N) Post
- Post (1) ←→ (N) Comment

## 📑 플레이리스트 공유 기능을 위한 엔티티/DTO 설계

### Playlist (플레이리스트)
| 필드명             | 설명                        | YouTube API 데이터                |
|--------------------|-----------------------------|-----------------------------------|
| id                 | DB 자동 생성 PK             | -                                 |
| title              | 플레이리스트 제목           | playlists.snippet.title           |
| description        | 플레이리스트 설명           | playlists.snippet.description     |
| youtubePlaylistId  | 유튜브 플레이리스트 ID      | playlists.id                      |
| coverImageUrl      | 썸네일 이미지 URL           | playlists.snippet.thumbnails.*    |
| owner              | 플레이리스트 소유자(회원)   | (내 서비스 사용자)                |
| isPublic           | 공개 여부                   | (내 서비스에서 설정)              |
| createdAt          | 생성일                      | playlists.snippet.publishedAt     |
| updatedAt          | 수정일                      | -                                 |
| songs              | 곡 리스트(PlaylistSong)      | playlistItems API로 별도 조회      |

### PlaylistSong (플레이리스트 곡)
| 필드명         | 설명                          | YouTube API 데이터                        |
|----------------|-------------------------------|------------------------------------------|
| id             | DB 자동 생성 PK               | -                                        |
| playlist       | 소속 플레이리스트             | -                                        |
| title          | 곡(영상) 제목                 | playlistItems.snippet.title               |
| artist         | 아티스트/업로더               | playlistItems.snippet.videoOwnerChannelTitle 또는 videos.snippet.channelTitle |
| album          | 앨범명(YouTube Music API 한정)| (YouTube Music API에서만)                |
| youtubeVideoId | 유튜브 영상 ID                | playlistItems.snippet.resourceId.videoId  |
| thumbnailUrl   | 썸네일 이미지 URL             | playlistItems.snippet.thumbnails.*        |
| orderIndex     | 플레이리스트 내 곡 순서        | playlistItems.snippet.position            |

### PlaylistDTO / PlaylistSongDTO
- API/프론트엔드와 데이터 교환을 위한 DTO로, 위 엔티티의 주요 필드를 포함합니다.
- PlaylistCreateRequest: 사용자가 YouTube URL을 입력할 때 사용하는 요청 DTO

---

# Git Convention
## Branch
### 종류
- `main`: 바로 product로 release(배포)할 수 있는 브랜치
- `dev(develop)`: 출시를 위해 개발하는 브랜치
    - `feat/{기능명}`: 새로운 기능 개발하는 브랜치
    - `refactor/{기능명}`: 개발된 기능을 리팩터링하는 브랜치

### 예시
- `dev/feat/login`
- `dev/feat/register`

## Commit
### 커밋 형식
```
<type><break or not>: <subject>
<BLANK LINE>
<body>
<BLANK LINE>
```

### 제목
제목에는 변경 사항에 대한 간결한 설명이 포함되어 있습니다.

#### 허용하는 타입 `<type>`
- feat        : 기능 (새로운 기능)
- fix         : 버그 (버그 수정)
- refactor    : 리팩토링
- design      : CSS 등 사용자 UI 디자인 변경
- comment     : 필요한 주석 추가 및 변경
- style       : 스타일 (코드 형식, 세미콜론 추가: 비즈니스 로직에 변경 없음)
- docs        : 문서 수정 (문서 추가, 수정, 삭제, README)
- test        : 테스트 (테스트 코드 추가, 수정, 삭제: 비즈니스 로직에 변경 없음)
- chore       : 기타 변경사항 (빌드 스크립트 수정, assets, 패키지 매니저 등)
- init        : 초기 생성
- rename      : 파일 혹은 폴더명을 수정하거나 옮기는 작업만 한 경우
- remove      : 파일을 삭제하는 작업만 수행한 경우

#### 내용 `<subject>`
- 명령조, 현재 시제 사용
- 끝에 . 없이 작성
  예시: `feat: 로그인 기능 구현`

### 메세지 본문 `<body>`
- 명령조, 현재 시제 사용
- 본문은 "어떻게" 보다 "무엇을", "왜"를 설명한다.
- 본문에 여러줄의 메시지를 작성할 땐 "-"로 구분

#### 예시
```
feat: 게시글 생성 기능 구현

- ex) 게시글 생성 구현
- ex) ...
```

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Gradle
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **AOP**: Spring AOP 기반 서비스 계층 로깅/트랜잭션 관리
- **Security**: Spring Security, JWT, OAuth2
- **Template Engine**: Thymeleaf
- **Validation**: Spring Boot Validation
- **Mapping**: MapStruct

### Frontend
- **Template Engine**: Thymeleaf
- **CSS Framework**: Bootstrap 5.3.2
- **JavaScript**: Vanilla JavaScript
- **Icons**: Material Symbols Outlined
- **Font**: Elice DX Neolli

### External APIs
- **YouTube Data API v3**: 플레이리스트 정보 및 곡 데이터 가져오기
- **Google OAuth2**: 소셜 로그인

### Development Tools
- **IDE**: IntelliJ IDEA (권장)
- **Database Tool**: MySQL Workbench
- **Version Control**: Git
- **Testing**: JUnit 5, Spring Boot Test

---

## 🚀 고도화된 기능
**고도화 시작일:** 2025.06.30

### 1. 전역 예외 처리 시스템
- **GlobalExceptionHandler**: 모든 예외를 일관되게 처리하는 전역 예외 핸들러
- **PliException**: 애플리케이션 전용 커스텀 예외 클래스
- **ErrorCode**: 체계적으로 분류된 에러 코드 enum (1000번대: 공통, 2000번대: 인증/인가, 3000번대: 사용자, 4000번대: 게시판, 5000번대: 게시글, 6000번대: 댓글, 7000번대: 플레이리스트, 8000번대: OAuth, 9000번대: 파일)
- **ErrorResponse**: 일관된 에러 응답 형식 제공
- **ExceptionUtils**: 비즈니스 로직에서 쉽게 예외를 발생시킬 수 있는 유틸리티 클래스
- **사용자 친화적 에러 메시지**: 개발자와 사용자 모두에게 유용한 에러 정보 제공
- **로깅 시스템 강화**: 모든 예외 발생 시 상세한 로그 기록

#### ErrorCode 체계
**실무 표준을 참고한 비즈니스 도메인별 에러 코드 분류**

| 번대 | 도메인 | 주요 에러 코드 | 설명 |
|------|--------|---------------|------|
| 1000번대 | 공통 | INVALID_INPUT_VALUE, METHOD_NOT_ALLOWED, INTERNAL_SERVER_ERROR | 애플리케이션 전반에서 발생하는 공통 에러 |
| 2000번대 | 인증/인가 | UNAUTHORIZED, FORBIDDEN, INVALID_TOKEN, EXPIRED_TOKEN | 로그인, 권한, 토큰 관련 에러 |
| 3000번대 | 사용자 | USER_NOT_FOUND, USER_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS | 회원가입, 로그인, 프로필 관리 관련 에러 |
| 4000번대 | 게시판 | BOARD_NOT_FOUND, BOARD_ACCESS_DENIED, BOARD_ALREADY_EXISTS | 게시판 CRUD 관련 에러 |
| 5000번대 | 게시글 | POST_NOT_FOUND, POST_ACCESS_DENIED, POST_UPDATE_DENIED | 게시글 CRUD 관련 에러 |
| 6000번대 | 댓글 | COMMENT_NOT_FOUND, COMMENT_ACCESS_DENIED, COMMENT_UPDATE_DENIED | 댓글 CRUD 관련 에러 |
| 7000번대 | 플레이리스트 | PLAYLIST_NOT_FOUND, INVALID_YOUTUBE_URL, YOUTUBE_API_ERROR | 플레이리스트 및 YouTube API 관련 에러 |
| 8000번대 | OAuth | OAUTH_AUTHENTICATION_FAILED, OAUTH_USER_INFO_ERROR | 소셜 로그인 관련 에러 |
| 9000번대 | 파일 | FILE_UPLOAD_ERROR, INVALID_FILE_TYPE, FILE_SIZE_EXCEEDED | 파일 업로드 관련 에러 |

**에러 코드 설계 원칙**
- **도메인별 명확한 구분**: 각 기능별로 에러 코드를 쉽게 찾을 수 있음
- **확장성**: 새로운 도메인 추가 시 번대만 추가하면 됨
- **가독성**: 개발자가 에러 코드만 봐도 어느 도메인인지 알 수 있음
- **디버깅 용이성**: 로그에서 에러 코드로 문제 영역을 빠르게 파악 가능

**실무 표준과의 차이점**
- **HTTP 상태 코드**: 400, 401, 403, 404, 500 등 (표준)
- **비즈니스 에러 코드**: 1000, 2000, 3000 등 (프로젝트 특화)
- **응답 예시**:
  ```json
  {
    "status": 404,           // HTTP 상태 코드
    "errorCode": 3000,       // 비즈니스 에러 코드
    "message": "ID: testuser123인 사용자를 찾을 수 없습니다."
  }
  ```

### 2. JWT 기반 인증
- JWT 토큰을 활용한 인증 및 사용자 정보 추출
- 쿠키 또는 Authorization 헤더에서 토큰을 자동으로 감지
- 기존 세션/쿠키 기반 로그인 로직을 JWT 기반으로 고도화

### 2. 공통 인터셉터를 통한 로그인 사용자 정보 자동 주입
- 각 컨트롤러에서 중복적으로 처리하던 로그인 사용자 정보(`loginId`, `loginNickname`) 주입 로직을 제거
- `LoginInterceptor`와 `WebConfig`를 통해 모든 뷰에서 로그인 정보를 자동으로 사용할 수 있도록 개선
- 코드 일관성 및 유지보수성 향상

### 3. 플레이리스트 공유 기능 (플리)
- **YouTube 플레이리스트 연동**: YouTube Data API v3를 활용한 플레이리스트 정보 자동 파싱
- **플레이리스트 CRUD**: 생성, 조회, 삭제 기능 구현
- **개별 곡 재생**: YouTube Embedded Player API를 활용한 모달 기반 개별 곡 재생
- **전체 재생**: 플레이리스트 전체 연속 재생 기능
- **YouTube 이동**: 원본 YouTube 플레이리스트 페이지로 이동 버튼
- **사용자별 플리 관리**: 전체 플리 목록과 내 플리 분리 관리

### 4. OAuth2 소셜 로그인 및 보안 강화
- **Google OAuth2 연동**: Spring Security OAuth2를 활용한 Google 로그인 구현
- **JWT 토큰 보안 강화**: 토큰 만료시간 단축(30분), HTTPS 자동 감지
- **OAuth 사용자 프로필 제한**: 비밀번호 변경 UI 숨김, 이메일 수정 불가
- **OAuth 스코프 최적화**: openid, profile, email 권한으로 필요한 정보만 수집
- **보안 경로 설정**: OAuth 인증 과정에 필요한 경로 자동 허용

---



## 🚀 향후 고도화 계획

### 1. 전역 예외 처리 시스템 구현 ✅
- GlobalExceptionHandler를 통한 일관된 에러 응답 처리
- 사용자 친화적인 에러 메시지 제공
- 로깅 시스템 강화

### 2. AOP(관점 지향 프로그래밍) 기반 공통 기능 고도화 ✅
- **로깅 AOP**: 서비스 계층(@Service)의 주요 메서드 실행 시 입력 파라미터, 반환값, 실행 시간, 예외 정보를 자동으로 로깅 (LoggingAspect)
    - @Aspect, @Component 기반 구현
    - @Around("execution(* com.elice.boardproject..service..*(..))")로 service 계층 한정 적용
    - 테스트는 src/test/java/com/elice/boardproject/service/TestService로 분리하여 TDD 방식으로 검증
- **트랜잭션 관리**: create, update, delete 등 데이터 변경 메서드에 @Transactional 어노테이션 적용
    - 클래스 단위/메서드 단위로 명확하게 적용
    - 데이터 무결성 및 롤백 보장
- **AOP 적용 범위**: 인프라(필터, 컨트롤러 등)에는 적용하지 않고, 비즈니스 로직(service 계층)에만 한정
- **확장성**: 커스텀 어노테이션 기반 AOP 확장 가능(향후 필요 시 annotation 패키지 활용)

### 3. 관리자 페이지 및 API 개발
- 관리자 권한 시스템 구현
- 사용자 관리, 게시판 관리, 플레이리스트 관리 기능
- 통계 대시보드 및 모니터링 기능

### 4. 플레이리스트 좋아요 기능 추가
- 플레이리스트 좋아요/취소 기능
- 인기 플레이리스트 추천 시스템
- 사용자별 좋아요 목록 관리

### 5. 플레이리스트 댓글 기능 추가
- 플레이리스트별 댓글 시스템
- 댓글 CRUD 기능
- 댓글 알림 시스템

### 6. 프로젝트 전반적 리팩토링
- 코드 구조 개선 및 최적화
- 성능 향상을 위한 쿼리 최적화
- 테스트 코드 보강

### 7. Thymeleaf → React 전환 (서버 사이드 → 클라이언트 사이드)
- **프론트엔드 구성 변경**: UI/UX 개선으로 사용자 친화적이고 심미성 있는 인터페이스 구현
- RESTful API 기반 SPA(Single Page Application) 구축
- 상태 관리 및 컴포넌트 기반 아키텍처 적용

### 8. Docker를 활용한 이미지 배포 추가
- 컨테이너화를 통한 배포 환경 표준화
- CI/CD 파이프라인 구축
- 멀티 스테이지 빌드 최적화

### 9. 백엔드 API 활용한 안드로이드 앱 개발 (Kotlin 사용)
- RESTful API 기반 모바일 앱 개발
- 플레이리스트 공유 및 재생 기능
- 푸시 알림 시스템

---

## 🚩 구현 현황 (2024.07 기준)

### ✅ 완료된 주요 기능
- **관리자 권한 시스템**: 역할/권한/사용자 관리, 권한 기반 AOP, 보안 설정
- **관리자 REST API**: 역할/권한/사용자 관리 API, 통합 테스트 및 자동화
- **게시판 CUD 관리자 전용**: 게시판 생성/수정/삭제는 관리자만 가능, 게시글/댓글은 작성자만 가능
- **테스트 자동화**: SpringBootTest 기반 통합 테스트, 권한/보안 정책 변경 시 테스트 연동

### ⏳ 향후 구현 예정
- **권한별 도메인 조작 API**: 게시판/게시글/댓글/플레이리스트 등 도메인별로 세분화된 권한 검증 및 API 적용
- **권한 검증 강화**: AOP 기반 권한 체크의 예외/실패 케이스 보강, 일관된 에러코드/메시지 반환, 상세 로깅
- **관리자 대시보드/통계 API**: 관리자용 통계, 활동 로그, 시스템 현황 등 대시보드 데이터 제공
- **(선택) 관리자 UI 연동**: 실제 운영 환경에서의 권한/역할/사용자 관리 프론트엔드 연동