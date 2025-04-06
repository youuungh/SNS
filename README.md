# SNS

![Kotlin](https://img.shields.io/badge/kotlin-1.9.24-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-darkcyan)
![Architecture](https://img.shields.io/badge/Architecture-Clean-green)
![Ktor](https://img.shields.io/badge/Backend-Ktor-red)


**SNS는 클린 아키텍처와 최신 Android 기술을 활용한 소셜 네트워킹 앱입니다. 사용자는 게시물을 작성하고, 댓글을 달고, 다른 사용자를 팔로우하며, 실시간 채팅을 할 수
있습니다. Jetpack Compose, Kotlin Coroutines, Hilt 등 최신 안드로이드 개발 기술을 활용하였습니다.**

## 프리뷰

<a href="https://streamable.com/m9ybby">
  <img src="https://github.com/user-attachments/assets/5ca7f006-7db9-4651-8da7-a7a21d4fa034" height="600px"/>
</a>

## 다운로드

## 주요 기능

### 인증 시스템
    - 이메일/비밀번호 로그인 및 회원가입
    - Google, Kakao, Naver 소셜 로그인 통합

### 소셜 피드
    - 사용자 게시물 타임라인 및 탐색 
    - 게시물 보기/생성/수정/삭제
    - 다중 이미지 및 리치 텍스트 지원
    - 좋아요, 댓글, 게시물 저장 기능

### 실시간 기능
    - WebSocket 기반 1:1 채팅
    - 메시지 읽음 확인
    - FCM 알림 시스템(좋아요, 댓글, 팔로우 등의 활동에 대한 푸시 알림)

### 탐색 및 검색
    - 사용자 검색 및 최근 검색 기록
    - 개인 프로필 및 다른 사용자 프로필 조회
    - 팔로우/언팔로우 시스템
    - 추천 사용자

### 사용자 경험
    - 라이트/다크 테마 지원
    - 사용자 맞춤 알림 설정
    - 애니메이션 및 트랜지션 효과
    - 오프라인 지원

## 기술 스택

### 아키텍처
- **클린 아키텍처**: 도메인, 데이터, 프레젠테이션 레이어 분리
- **[MVI (Model-View-Intent)](https://orbit-mvi.org/)**: Orbit MVI를 활용한 단방향 데이터 흐름
- **모듈화**: 기능 및 레이어별 모듈 분리

### 안드로이드 & 코틀린
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)**: 선언적 UI 구현
- **[Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)**: 비동기 프로그래밍
- **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)**: 의존성 주입
- **[Room](https://developer.android.com/training/data-storage/room)**: 로컬 데이터베이스 관리
- **[DataStore](https://developer.android.com/topic/libraries/architecture/datastore)**: 사용자 설정 저장
- **[WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)**: 백그라운드 작업 처리

### 네트워킹
- **[Ktor Client](https://ktor.io/docs/client.html)**: 네트워크 통신
- **[WebSocket](https://ktor.io/docs/websocket.html)**: 실시간 채팅 구현
- **[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)**: JSON 직렬화/역직렬화

### 기타 라이브러리
- **[Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)**: 페이지네이션 처리
- **[Coil](https://github.com/coil-kt/coil)**: 이미지 로딩
- **[RichEditor](https://github.com/wasabeef/richeditor-android)**: 리치 텍스트 에디터
- **[Lottie](https://airbnb.design/lottie/)**: 애니메이션
- **[Timber](https://github.com/JakeWharton/timber)**: 로깅

### 백엔드 & 서비스
- **[Firebase Authentication](https://firebase.google.com/docs/auth)**: 사용자 인증
- **[Firebase Cloud Messaging (FCM)](https://firebase.google.com/docs/cloud-messaging)**: 푸시 알림

## 아키텍처

```
com.ninezero.sns
├── domain
│   ├── model         # 도메인 모델
│   ├── repository    # 인터페이스
│   └── usecase       # 비즈니스 로직
│
├── data
│   ├── di            # 의존성 주입 모듈
│   ├── db            # Room 데이터베이스
│   ├── ktor          # 네트워크 클라이언트
│   ├── model         # DTO 및 매핑
│   ├── repository    # 구현체
│   ├── usecase       # 구현체
│   ├── worker        # Background 작업
│   └── util          # 유틸리티
│
└── presentation
    ├── auth          # 인증 화면
    ├── base          # 기본 컴포넌트
    ├── chat          # 채팅 화면
    ├── component     # 재사용 가능 UI 컴포넌트
    ├── detail        # 상세 화면
    ├── feed          # 피드 화면
    ├── main          # 메인 화면
    ├── model         # UI 모델
    ├── notification  # 알림 화면
    ├── post          # 게시물 작성
    ├── profile       # 프로필 화면
    ├── search        # 검색 화면
    ├── setting       # 설정 화면
    ├── theme         # 테마 및 스타일
    ├── user          # 사용자 화면
    └── util          # UI 유틸리티
```

### Domain
- **엔티티**: 비즈니스 모델 정의
- **유스케이스**: 비즈니스 로직 캡슐화
- **레포지토리 인터페이스**: 데이터 액세스 추상화

### Data
- **레포지토리 구현**: 도메인 레이어에 정의된 인터페이스 구현
- **데이터 소스**: 로컬(Room) 및 원격(Ktor) 데이터 소스
- **모델 매퍼**: DTO와 도메인 모델 간 변환

### Presentation
- **ViewModels**: UI 상태 관리 및 비즈니스 로직 처리
- **Compose UI**: 선언적 UI 구성
- **Navigation**: 화면 간 이동 관리

## 상태 관리
- **State**: UI 상태를 정의하는 불변 객체
- **Intent**: 사용자 액션 또는 이벤트를 나타내는 함수
- **SideEffect**: 일회성 이벤트(토스트, 내비게이션 등)

## 스크린샷
<table align="center">
  <tr>
    <td><img src="https://github.com/user-attachments/assets/42c7a4de-99e5-4696-a41f-63baa6420f21" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/1529a81d-c688-4e5b-8729-de7340706437" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/83644a73-0697-4bd9-8333-84e4bc61ddc3" height="20%" /></td>
    <td><img src="(https://github.com/user-attachments/assets/0ae867b8-747f-4c1b-b9f8-f84b12ddbe2d" height="20%" /></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/8cbb91b4-a40c-499d-978a-fc080c7480ad" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/fdfc2dee-c6af-490f-b387-3e3df4f178f5" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/3f0d313e-e85e-47ec-91ec-712eebcdc7de" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/86e736a7-d41b-4f66-a297-e069ed41f413" height="20%" /></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/21d1629f-1d69-4b54-aee4-685b7592fc59" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/8b0a9690-213e-4217-9c4a-fc8706c147d4" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/3f3dc904-9ac9-479b-bee9-cae4c74a13dc" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/653bdeff-f67f-4bcc-b64e-dd7608081cff" height="20%" /></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/0bc57b6e-7f13-4045-b441-61427e61b920" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/095c9479-7089-484e-b4fa-32135dff1f31" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/6a4191b2-0149-4563-bb2e-19fb328924d2" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/05e83eeb-3d5b-4d17-ab42-145368387a46" height="20%" /></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/44ac7e13-55f6-4054-adf6-3a892051e935" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/5bedefd0-b992-4874-a1c7-8423d061ad31" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/b1b2d19f-562e-4b2c-8df0-b4c8adaebbaa" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/4e33035a-78e0-46d5-9dfa-71e03365c5ab" height="20%" /></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/306b0a57-ca6c-49f3-bfca-8978059c6e2c" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/b61cfd91-4440-450f-89d6-31674dc5abc5" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/cbe2edf1-06fa-400b-b50b-e9868b2c90e5" height="20%" /></td>
    <td><img src="https://github.com/user-attachments/assets/c3ac26b0-736c-4882-985c-bee9d07d8410" height="20%" /></td>
  </tr>
</table>

## 설정 및 빌드

### 백엔드 설정

<a href="https://github.com/youuungh/ktor-sns-api">
<img src="https://img.shields.io/badge/백엔드_서버-서버_저장소로_이동-blue?style=for-the-badge&logo=github" alt="Backend Server" />
</a>

### 필수 요구사항
```
- Android Studio Arctic Fox 이상
- Kotlin 1.8.0 이상
- Gradle 8.0 이상
- JDK 17
```

### 필수 환경 변수

- **Firebase 프로젝트를 설정**
```
google-services.json
```

- **소셜 로그인 API 키를 설정**
```
KAKAO_NATIVE_KEY=your_kakao_key
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
```