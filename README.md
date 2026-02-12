# 볼링클럽 관리 앱

Android 네이티브 애플리케이션으로 볼링클럽 운영진을 위한 통합 관리 시스템입니다.

## 주요 기능

### 회원 관리
- **등록/수정/삭제**: 회원 정보 입력 및 관리
- **상태 관리**: 활동/휴면 상태 전환
- **회원 검색**: 이름으로 빠른 회원 검색

### 정기전 운영
- **정기전 생성**: 새로운 정기전 등록 및 설정
- **참가자 체크**: QR 코드 또는 수동으로 참석 인원 확인
- **점수 입력**: 점수 수동 입력 또는 OCR 자동 인식
- **순위 산출**: 자동 순위 계산 및 순위 이력 추적

### OCR 점수 인식
- **Google Cloud Vision** 통합: 점수표 사진 자동 인식
- **한국어 최적화**: Google AI 기반 한국어 손글씨 인식
- **빠른 입력**: 대량 점수 입력 시간 단축

### 핸디캡 시스템
- **여성 회원 추가 점수**: 게임당 고정 점수 자동 추가
- **공정한 경쟁**: 실력 차이 보정으로 모든 회원 참여 유도

### 팀전 편성
- **수동 편성**: 운영진의 세밀한 팀 구성
- **에버리지 기반 자동 편성**: 평균 점수 기반 균형잡힌 팀 자동 생성
- **팀 밸런싱**: 실력 차이 최소화로 경쟁력 있는 매경

### 카카오톡 결과 공유
- **순위표 이미지 생성**: 시각화된 순위 결과
- **Share Intent 연동**: 카카오톡 및 기타 메시징 앱 공유
- **실시간 공유**: 대회 종료 후 즉시 결과 배포

### 통계 및 분석
- **에버리지**: 회원별 평균 점수 추적
- **최고점**: 개인 최고 기록 관리
- **참석률**: 정기전 참석 현황
- **순위 이력**: 시간별 순위 변동 추적
- **클럽 랭킹**: 통합 클럽 랭킹 시스템

### 백업 및 복원
- **JSON 백업**: 전체 데이터 백업
- **CSV 내보내기**: 엑셀 호환 형식 지원
- **자동 백업**: 정기적 자동 백업 스케줄
- **데이터 복원**: 백업 파일에서 데이터 복원

## 기술 스택

| 분류 | 기술 |
|------|------|
| **언어** | Kotlin |
| **UI 프레임워크** | Jetpack Compose |
| **로컬 데이터베이스** | Room (SQLite) |
| **의존성 주입** | Hilt |
| **비동기 처리** | Coroutines, Flow |
| **HTTP 클라이언트** | Retrofit + OkHttp |
| **이미지 처리** | CameraX, Canvas API |
| **차트/그래프** | MPAndroidChart |
| **AI/비전** | Google Cloud Vision API |
| **JSON 직렬화** | Gson |
| **아키텍처 패턴** | MVVM (ViewModel + Repository) |

## 프로젝트 구조

```
BowlingClubApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/bowlingclub/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screen/              # Compose 화면 구성
│   │   │   │   │   ├── component/           # 재사용 가능한 컴포넌트
│   │   │   │   │   └── theme/              # 디자인 시스템
│   │   │   │   ├── viewmodel/              # ViewModel 계층
│   │   │   │   ├── repository/             # Repository 패턴
│   │   │   │   ├── model/                  # 데이터 모델 클래스
│   │   │   │   ├── database/               # Room DB 엔티티 및 DAO
│   │   │   │   ├── di/                     # Hilt 의존성 주입
│   │   │   │   ├── api/                    # Retrofit API 서비스
│   │   │   │   ├── util/                   # 유틸리티 함수
│   │   │   │   └── MainActivity.kt         # 메인 액티비티
│   │   │   └── res/
│   │   │       ├── drawable/               # 이미지 자산
│   │   │       ├── values/                 # 리소스 값
│   │   │       └── AndroidManifest.xml     # 앱 메니페스트
│   │   └── test/
│   ├── build.gradle.kts                    # 앱 레벨 빌드 설정
│   └── ...
├── build.gradle.kts                        # 프로젝트 레벨 빌드 설정
├── settings.gradle.kts                     # Gradle 설정
├── local.properties                        # 로컬 설정 (미포함)
└── README.md                               # 이 파일
```

## 설치 및 실행

### 필수 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17 이상
- Android SDK 26 (Android 8.0) 이상
- Gradle 8.0 이상

### 설치 단계

1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd BowlingClubApp
   ```

2. **Android Studio에서 프로젝트 열기**
   - Android Studio 실행
   - File > Open 선택
   - BowlingClubApp 폴더 선택

3. **Gradle 동기화**
   - Android Studio가 자동으로 Gradle 동기화 수행
   - 또는 File > Sync Now 클릭

4. **빌드 및 실행**
   - 에뮬레이터 시작 또는 실제 기기 연결
   - Run > Run 'app' 클릭 (또는 Shift + F10)

### 최소 요구사항
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **최소 메모리**: 2GB RAM

## 빌드

### 디버그 빌드
```bash
./gradlew assembleDebug
```
결과 위치: `app/build/outputs/apk/debug/app-debug.apk`

### 릴리즈 빌드
```bash
./gradlew assembleRelease
```
결과 위치: `app/build/outputs/bundle/release/app-release.aab`

### 전체 클린 빌드
```bash
./gradlew clean build
```

### 테스트 실행
```bash
./gradlew test              # 유닛 테스트
./gradlew connectedTest     # 통합 테스트
```

## 아키텍처

### MVVM 패턴
앱은 Model-View-ViewModel 아키텍처를 따릅니다.

```
UI Layer (Jetpack Compose)
    ↓
ViewModel (상태 관리 및 비즈니스 로직)
    ↓
Repository (데이터 접근 추상화)
    ↓
Data Layer (Room DB + API)
```

### 계층 설명

| 계층 | 역할 |
|------|------|
| **UI Layer** | Jetpack Compose를 사용한 선언형 UI |
| **ViewModel** | UI 상태 관리 및 사용자 이벤트 처리 |
| **Repository** | DAO, API, 외부 데이터소스 통합 |
| **Data Layer** | Room 로컬 DB, Google Cloud Vision API 등 |

### 핵심 디자인 원칙
- **단일 책임 원칙**: 각 클래스는 하나의 책임만 수행
- **의존성 역전**: Hilt를 통한 의존성 주입
- **테스트 가능성**: 인터페이스 기반 설계
- **상태 불변성**: Compose의 immutable 상태 관리

## 개발 단계

앱 개발은 총 8단계로 진행됩니다.

| Phase | 단계 | 설명 |
|-------|------|------|
| 1 | 기본 UI 구축 | 메인 화면, 회원 목록, 기본 네비게이션 |
| 2 | 회원 관리 | 등록, 수정, 삭제, 상태 관리 |
| 3 | 정기전 관리 | 정기전 생성, 기본 점수 입력 |
| 4 | OCR 및 Vision | Google Cloud Vision API 통합 |
| 5 | 팀 편성 시스템 | 수동/자동 팀 편성 알고리즘 |
| 6 | 통계 및 차트 | 데이터 분석 및 시각화 |
| 7 | 공유 기능 | 이미지 생성, 카카오톡 공유 |
| 8 | 백업/복원 | 데이터 백업, 복원, 마이그레이션 |

## 환경 설정

### API 키 관리

1. **Google Cloud Vision API 키**
   - `local.properties` 파일에 추가:
   ```properties
   google_vision_api_key=your-google-vision-api-key-here
   ```

2. **ProGuard 규칙** (릴리즈 빌드)
   - `app/proguard-rules.pro`에 설정

### 기본 설정 파일

- `build.gradle.kts`: 의존성 버전 및 설정
- `AndroidManifest.xml`: 권한 및 컴포넌트 선언
- `strings.xml`: 문자열 리소스

필요 권한:
- `android.permission.CAMERA`: 카메라 접근
- `android.permission.READ_EXTERNAL_STORAGE`: 파일 읽기
- `android.permission.WRITE_EXTERNAL_STORAGE`: 파일 쓰기
- `android.permission.INTERNET`: 네트워크 통신

## 주요 의존성

```kotlin
// Jetpack
androidx.compose.ui:ui
androidx.lifecycle:lifecycle-viewmodel-compose
androidx.room:room-runtime
androidx.hilt:hilt-navigation-compose

// Hilt
com.google.dagger:hilt-android

// Network
com.squareup.retrofit2:retrofit
com.squareup.okhttp3:okhttp

// Image & Camera
androidx.camera:camera-core
androidx.camera:camera-camera2

// Charts
com.github.PhilJay:MPAndroidChart

// Serialization
com.google.code.gson:gson

// Testing
junit:junit
androidx.test.espresso:espresso-core
```

## 문제 해결

### 빌드 오류
- Gradle 캐시 초기화: `./gradlew clean`
- IDE 캐시 초기화: File > Invalidate Caches > Invalidate and Restart

### 런타임 오류
- 로그 확인: Logcat 필터에서 패키지 이름으로 검색
- 디버거 사용: Run > Debug 'app'

### API 연결 오류
- 네트워크 연결 확인
- API 키 설정 확인
- 서버 상태 확인

## 라이선스

Private Project - 모든 권리 보유

---

**최종 수정**: 2026년 2월 9일
**개발자**: Bowling Club Management Team
