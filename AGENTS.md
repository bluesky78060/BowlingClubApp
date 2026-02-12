# 볼링클럽 관리 앱 (Bowling Club Management App) - 에이전트 역할 문서

**프로젝트**: 안드로이드 볼링클럽 회원 관리 및 대회 운영 시스템
**언어**: Kotlin + Jetpack Compose
**아키텍처**: Clean Architecture + MVVM + Repository Pattern
**주요 라이브러리**: Room, Hilt, Jetpack Compose, Coroutines

---

## 목차
1. [개요](#개요)
2. [모듈별 에이전트 할당](#모듈별-에이전트-할당)
3. [데이터 레이어](#데이터-레이어)
4. [UI 레이어](#ui-레이어)
5. [기능별 레이어](#기능별-레이어)
6. [의존성 그래프](#의존성-그래프)

---

## 개요

### 핵심 책임 분담

| 도메인 | 담당 에이전트 | 모델 | 복잡도 |
|--------|------------|------|--------|
| 데이터베이스 스키마 | executor | sonnet | MEDIUM |
| 비즈니스 로직 | executor | sonnet | MEDIUM |
| DI 설정 | executor-low | haiku | LOW |
| 대시보드 UI | designer | sonnet | MEDIUM |
| 회원 관리 UI | designer | sonnet | MEDIUM |
| 대회 운영 UI | designer | sonnet | MEDIUM |
| 스코어 입력 (OCR) | executor-high | opus | HIGH |
| 통계 & 차트 | designer | sonnet | MEDIUM |
| 설정 화면 | executor-low | haiku | LOW |
| KakaoTalk 공유 | executor | sonnet | MEDIUM |
| 공유 컴포넌트 | designer-low | haiku | LOW |
| 네비게이션 | executor-low | haiku | LOW |
| 테마 설정 | designer-low | haiku | LOW |
| PIN 보안 | executor-low | haiku | LOW |
| Naver CLOVA OCR | executor-high | opus | HIGH |
| 백업/복구 | executor | sonnet | MEDIUM |
| 유틸리티 함수 | executor-low | haiku | LOW |

---

### 활성 스킬 (자동 트리거)

모든 UI 작업에는 다음 스킬이 자동으로 적용됩니다:

| 스킬 | 트리거 조건 | 역할 |
| --- | --- | --- |
| **`frontend-ui-ux`** | UI/컴포넌트/스타일링 작업 감지 시 | 디자이너 관점의 UI/UX. 목업 없이도 프로덕션급 인터페이스 생성 |
| **`bkit:mobile-app`** | 모바일 앱 개발 전반 | Android 네이티브 개발 패턴, 모바일 UX 가이드라인 |
| **`git-master`** | git/commit 컨텍스트 감지 시 | 원자적 커밋, 브랜치 관리, 히스토리 관리 |

**규칙**:

- `designer`, `designer-low`, `designer-high` 에이전트 사용 시 `frontend-ui-ux` 스킬이 항상 병행 활성화
- UI 관련 executor 작업에서도 `frontend-ui-ux` 디자인 원칙 준수
- 모바일 앱 패턴은 `bkit:mobile-app` 스킬에서 참조

---

## 모듈별 에이전트 할당

### 프로젝트 구조
```
com.bowlingclub.app/
├── data/                    # 데이터 레이어
│   ├── local/               # 로컬 데이터베이스
│   │   ├── entity/          # Room Entity (6개 테이블)
│   │   ├── dao/             # DAO 인터페이스
│   │   └── converter/       # 타입 변환기
│   ├── repository/          # Repository 구현
│   ├── model/               # 도메인 모델
│   └── source/              # 데이터 소스 (Room, API, etc.)
├── di/                      # Hilt 의존성 주입
├── ui/                      # UI 레이어
│   ├── home/                # 대시보드
│   ├── member/              # 회원 관리
│   ├── tournament/          # 대회 운영
│   ├── score/               # 스코어 입력
│   ├── statistics/          # 통계 & 차트
│   ├── settings/            # 앱 설정
│   ├── share/               # KakaoTalk 공유
│   ├── components/          # 공유 UI 컴포넌트
│   ├── navigation/          # 네비게이션
│   ├── theme/               # 테마/색상 설정
│   └── pin/                 # PIN 보안 화면
├── ocr/                     # Naver CLOVA OCR API 통합
├── backup/                  # 백업/복구 로직
├── util/                    # 유틸리티 함수
└── MainActivity.kt          # 진입점
```

---

## 데이터 레이어

### `data/local/entity/` - 데이터베이스 엔티티

**담당 에이전트**: `executor` (Sonnet)
**복잡도**: MEDIUM

#### 설명
Room 데이터베이스 스키마를 정의하는 Entity 클래스들입니다. 6개의 핵심 테이블을 포함합니다.

#### 핵심 파일
- `MemberEntity.kt` - 회원 정보 (ID, 이름, 연락처, 가입일 등)
- `TournamentEntity.kt` - 대회 정보 (이름, 개최일, 장소 등)
- `RoundEntity.kt` - 라운드 정보 (대회별 라운드, 순서 등)
- `ScoreEntity.kt` - 스코어 기록 (회원, 라운드, 점수 등)
- `MemberTournamentEntity.kt` - 회원-대회 참가 기록
- `NotificationEntity.kt` - 푸시 알림 로그

#### 의존성
- Android Room framework
- Kotlin data classes
- kotlinx.serialization (선택사항)

#### 예상 구현 내용
```
- @Entity 어노테이션으로 테이블 정의
- Primary key 및 외래키 정의
- Index 최적화
- Timestamp 필드 (createdAt, updatedAt)
- Type converter 통합 (@TypeConverters)
```

---

### `data/local/dao/` - Data Access Objects

**담당 에이전트**: `executor` (Sonnet)
**복잡도**: MEDIUM

#### 설명
Room DAO 인터페이스로, 데이터베이스 조회/수정/삭제를 담당합니다.

#### 핵심 파일
- `MemberDao.kt` - 회원 CRUD 및 쿼리
- `TournamentDao.kt` - 대회 CRUD 및 쿼리
- `RoundDao.kt` - 라운드 CRUD 및 쿼리
- `ScoreDao.kt` - 스코어 CRUD 및 쿼리 (집계 포함)
- `MemberTournamentDao.kt` - 참가 기록 관리
- `NotificationDao.kt` - 알림 로그 조회

#### 의존성
- Room DAO framework
- Kotlin Flow/LiveData
- entity 정의

#### 예상 구현 내용
```
- @Insert, @Update, @Delete, @Query
- Flow<List<Entity>> 반환 (반응형)
- 복잡한 JOIN 쿼리 (회원+대회+스코어)
- 페이징 지원 (@RewriteQueriesToDropUnusedColumns)
- 트랜잭션 관리 (@Transaction)
```

---

### `data/local/converter/` - 타입 변환기

**담당 에이전트**: `executor-low` (Haiku)
**복잡도**: LOW

#### 설명
Room이 기본 지원하지 않는 타입(List, Enum, Date 등)을 데이터베이스 타입으로 변환합니다.

#### 핵심 파일
- `DateConverter.kt` - Long ↔ LocalDateTime
- `EnumConverter.kt` - Enum ↔ String
- `ListConverter.kt` - List ↔ JSON String

#### 의존성
- kotlinx.serialization
- java.time API

---

### `data/repository/` - Repository 구현

**담당 에이전트**: `executor` (Sonnet)
**복잡도**: MEDIUM-HIGH

#### 설명
비즈니스 로직과 데이터 소스를 분리하는 중간 계층입니다. DAO를 통해 데이터에 접근하고, 필요시 변환/필터링을 수행합니다.

#### 핵심 파일
- `MemberRepository.kt` - 회원 관리 로직
- `TournamentRepository.kt` - 대회 관리 로직
- `ScoreRepository.kt` - 스코어 및 통계 계산
- `BackupRepository.kt` - 백업/복구 로직
- `PreferencesRepository.kt` - 사용자 설정 관리

#### 의존성
- DAO 레이어
- Domain models
- Coroutines
- Hilt (의존성 주입)

#### 예상 구현 내용
```
- 트랜잭션 처리 (여러 DAO 호출)
- 데이터 검증 및 변환
- 오류 처리 (try-catch, Result wrapper)
- 캐싱 전략
- Coroutines를 통한 비동기 처리
```

---

### `data/model/` - 도메인 모델

**담당 에이전트**: `executor` (Sonnet)
**복잡도**: LOW-MEDIUM

#### 설명
Entity와 분리된 UI/비즈니스 로직용 도메인 모델입니다.

#### 핵심 파일
- `Member.kt` - 회원 도메인 모델
- `Tournament.kt` - 대회 도메인 모델
- `Score.kt` - 스코어 도메인 모델
- `Statistics.kt` - 통계 데이터 모델

#### 의존성
- kotlinx.serialization

---

## DI 레이어

### `di/` - Hilt 의존성 주입

**담당 에이전트**: `executor-low` (Haiku)
**복잡도**: LOW

#### 설명
Hilt를 통한 의존성 주입 설정 및 모듈 정의입니다.

#### 핵심 파일
- `DatabaseModule.kt` - Room 데이터베이스 싱글톤 제공
- `RepositoryModule.kt` - Repository 바인딩
- `DataStoreModule.kt` - DataStore 설정

#### 예상 구현 내용
```
- @Module, @Provides, @Singleton
- Room 데이터베이스 초기화
- Repository 생명주기 관리
```

---

## UI 레이어

### `ui/home/` - 대시보드

**담당 에이전트**: `designer` (Sonnet)
**자동 활성 스킬**: `frontend-ui-ux`
**복잡도**: MEDIUM

#### 설명
앱의 메인 화면으로, 핵심 통계와 빠른 액션을 제공합니다.

#### 핵심 파일
- `HomeScreen.kt` - 메인 대시보드 UI
- `HomeViewModel.kt` - 화면 로직
- `HomeState.kt` - UI 상태 관리
- `DashboardCard.kt` - 대시보드 카드 컴포넌트
- `QuickActionBar.kt` - 빠른 액션 바

#### 의존성
- MemberRepository
- TournamentRepository
- ScoreRepository
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 총 회원 수, 진행 중인 대회 수 등 KPI
- 최근 스코어 기록
- 다가오는 대회 일정
- 네비게이션 (회원, 대회, 스코어 입력으로)
```

---

### `ui/member/` - 회원 관리

**담당 에이전트**: `designer` (Sonnet)
**자동 활성 스킬**: `frontend-ui-ux`
**복잡도**: MEDIUM

#### 설명
회원 정보 관리 화면입니다. CRUD 기능과 검색/필터링을 포함합니다.

#### 핵심 파일
- `MemberListScreen.kt` - 회원 목록 화면
- `MemberDetailScreen.kt` - 회원 상세 정보
- `MemberEditScreen.kt` - 회원 수정/추가
- `MemberViewModel.kt` - 화면 로직
- `MemberListItem.kt` - 리스트 아이템 컴포넌트

#### 의존성
- MemberRepository
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 회원 목록 (스크롤, 검색, 필터링)
- 회원 추가/수정/삭제
- 회원별 통계 조회
- 데이터 유효성 검증
```

---

### `ui/tournament/` - 대회 운영

**담당 에이전트**: `designer` (Sonnet)
**자동 활성 스킬**: `frontend-ui-ux`
**복잡도**: MEDIUM-HIGH

#### 설명
대회 관리 화면입니다. 대회 생성, 라운드 관리, 참가자 지정 등을 수행합니다.

#### 핵심 파일
- `TournamentListScreen.kt` - 대회 목록 화면
- `TournamentDetailScreen.kt` - 대회 상세 정보
- `TournamentEditScreen.kt` - 대회 생성/수정
- `RoundManagementScreen.kt` - 라운드 관리
- `ParticipantSelectionScreen.kt` - 참가자 선택
- `TournamentViewModel.kt` - 화면 로직

#### 의존성
- TournamentRepository
- MemberRepository
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 대회 생성 (이름, 날짜, 장소 등)
- 라운드 추가/수정 (순서, 형식 등)
- 참가자 선택 및 관리
- 대회 상태 관리 (계획 중, 진행 중, 종료)
```

---

### `ui/score/` - 스코어 입력 (OCR + 수동)

**담당 에이전트**: `executor-high` (Opus)
**복잡도**: HIGH

#### 설명
스코어 입력 화면으로, Naver CLOVA OCR을 통한 자동 입력과 수동 입력을 모두 지원합니다.

#### 핵심 파일
- `ScoreInputScreen.kt` - 스코어 입력 메인 화면
- `OcrCameraScreen.kt` - OCR 카메라 화면
- `ManualScoreEntryScreen.kt` - 수동 입력 화면
- `ScoreInputViewModel.kt` - 화면 로직
- `ScoreConfirmationDialog.kt` - 확인 다이얼로그

#### 의존성
- ScoreRepository
- OcrService (Naver CLOVA OCR API)
- Camera 권한
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 카메라를 통한 스코어 시트 촬영
- Naver CLOVA OCR API를 통한 OCR 처리
- OCR 결과 검증 및 수정 UI
- 수동 입력 폼 (회원별 점수 입력)
- 스코어 저장 및 검증
- 오류 처리 (OCR 실패, 유효하지 않은 데이터 등)
```

---

### `ui/statistics/` - 통계 & 차트

**담당 에이전트**: `designer` (Sonnet)
**자동 활성 스킬**: `frontend-ui-ux`
**복잡도**: MEDIUM

#### 설명
통계 데이터 시각화 화면입니다. 차트, 그래프, 순위 등을 표시합니다.

#### 핵심 파일
- `StatisticsScreen.kt` - 통계 메인 화면
- `StatisticsViewModel.kt` - 통계 계산 로직
- `LineChart.kt` - 선 그래프 (개인 추이)
- `BarChart.kt` - 막대 그래프 (회원별 평균)
- `RankingTable.kt` - 순위 테이블
- `TimeRangeSelector.kt` - 기간 선택 컴포넌트

#### 의존성
- ScoreRepository
- 차트 라이브러리 (예: Vico)
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 개인별 스코어 추이 그래프
- 전체 평균, 최고점, 최저점
- 회원별 순위
- 기간별 필터링 (주, 월, 연도)
- 통계 데이터 내보내기 (CSV 등)
```

---

### `ui/settings/` - 앱 설정

**담당 에이전트**: `executor-low` (Haiku)
**복잡도**: LOW

#### 설명
앱 설정 화면으로, 언어, 테마, 알림 등을 관리합니다.

#### 핵심 파일
- `SettingsScreen.kt` - 설정 메인 화면
- `SettingsViewModel.kt` - 설정 로직
- `ThemeSettingCard.kt` - 테마 설정 카드
- `NotificationSettingCard.kt` - 알림 설정 카드
- `BackupSettingCard.kt` - 백업 설정 카드

#### 의존성
- PreferencesRepository
- DataStore
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 테마 선택 (라이트/다크/시스템)
- 언어 선택
- 알림 활성화/비활성화
- 백업 설정
- 앱 정보/버전
```

---

### `ui/share/` - KakaoTalk 공유

**담당 에이전트**: `executor` (Sonnet)
**복잡도**: MEDIUM

#### 설명
스코어 및 통계를 이미지로 변환하여 KakaoTalk으로 공유하는 기능입니다.

#### 핵심 파일
- `ShareBottomSheet.kt` - 공유 옵션 바텀 시트
- `ShareViewModel.kt` - 공유 로직
- `ImageGenerator.kt` - 이미지 생성 (Compose 렌더링)
- `KakaoShareHelper.kt` - KakaoTalk 공유 통합

#### 의존성
- ScoreRepository
- KakaoTalk SDK
- Compose rendering (Canvas)
- File storage

#### 예상 구현 내용
```
- 스코어 기록을 이미지로 변환
- 통계 차트를 이미지로 렌더링
- 이미지 저장 (내부 저장소)
- KakaoTalk 공유 (텍스트, 이미지)
- 공유 성공/실패 처리
```

---

### `ui/components/` - 공유 UI 컴포넌트

**담당 에이전트**: `designer-low` (Haiku)
**복잡도**: LOW

#### 설명
여러 화면에서 재사용되는 Compose 컴포넌트입니다.

#### 핵심 파일
- `CommonButton.kt` - 버튼 (기본, 프라이머리, 세컨더리)
- `CommonTextField.kt` - 텍스트 필드
- `CommonDialog.kt` - 다이얼로그 (확인, 선택 등)
- `CommonTopAppBar.kt` - 상단 앱 바
- `CommonLoader.kt` - 로딩 인디케이터
- `EmptyStateView.kt` - 빈 상태 화면

#### 의존성
- Jetpack Compose
- Material 3

#### 예상 구현 내용
```
- 디자인 시스템 준수
- 높은 재사용성
- 파라미터화된 커스터마이징
- 접근성 지원
```

---

### `ui/navigation/` - 네비게이션

**담당 에이전트**: `executor-low` (Haiku)
**복잡도**: LOW

#### 설명
Jetpack Navigation을 통한 앱 네비게이션 설정입니다.

#### 핵심 파일
- `NavGraph.kt` - 네비게이션 그래프 정의
- `Route.kt` - 라우트 상수
- `Navigator.kt` - 네비게이션 헬퍼

#### 의존성
- Jetpack Navigation Compose
- ViewModel (shared)

#### 예상 구현 내용
```
- 화면 간 네비게이션 그래프
- Deep linking 지원
- Back stack 관리
- 파라미터 전달 (Safe Args)
```

---

### `ui/theme/` - 테마 설정

**담당 에이전트**: `designer-low` (Haiku)
**복잡도**: LOW

#### 설명
앱 전역 테마 정의 (색상, 폰트, 모양 등)입니다.

#### 핵심 파일
- `Theme.kt` - 테마 설정
- `Color.kt` - 색상 팔레트
- `Type.kt` - 타이포그래피
- `Shape.kt` - 모양 정의

#### 의존성
- Jetpack Compose Material 3

#### 예상 구현 내용
```
- 라이트/다크 테마
- Material 3 색상 시스템
- 커스텀 폰트
- 컴포넌트 모양
```

---

### `ui/pin/` - PIN 보안 화면

**담당 에이전트**: `executor-low` (Haiku)
**복잡도**: LOW

#### 설명
앱 실행 시 PIN 입력 화면입니다. 생체인식(지문) 지원 가능합니다.

#### 핵심 파일
- `PinLockScreen.kt` - PIN 입력 화면
- `PinLockViewModel.kt` - PIN 로직
- `BiometricHelper.kt` - 생체인식 통합 (선택사항)

#### 의존성
- DataStore (PIN 저장)
- BiometricPrompt (선택사항)
- Jetpack Compose
- ViewModel, StateFlow

#### 예상 구현 내용
```
- 4-6자리 PIN 입력
- PIN 저장 및 검증
- 실패 시 재시도 제한
- 생체인식 옵션
- 초기 PIN 설정
```

---

## 기능별 레이어

### `ocr/` - Naver CLOVA OCR API 통합

**담당 에이전트**: `executor-high` (Opus)
**복잡도**: HIGH

#### 설명
Naver CLOVA OCR API를 통한 OCR 처리로, 스코어 시트를 자동 인식합니다.

#### 핵심 파일
- `OcrService.kt` - Naver CLOVA OCR API 클라이언트
- `OcrResponse.kt` - API 응답 모델
- `OcrProcessor.kt` - OCR 결과 처리 로직
- `ScoreExtractor.kt` - 스코어 추출 로직

#### 의존성
- Naver CLOVA OCR API
- Retrofit/OkHttp (HTTP 클라이언트)
- Hilt (의존성 주입)

#### 예상 구현 내용
```
- 이미지를 Base64로 인코딩
- Naver CLOVA OCR API에 요청
- JSON 응답 파싱
- 스코어 데이터 추출
- 오류 처리 (API 오류, 타임아웃 등)
- 재시도 로직
- 결과 캐싱 (선택사항)
```

---

### `backup/` - 백업/복구 로직

**담당 에이전트**: `executor` (Sonnet)
**복잡도**: MEDIUM

#### 설명
앱 데이터를 JSON으로 내보내고 복구하는 기능입니다.

#### 핵심 파일
- `BackupManager.kt` - 백업 관리자
- `BackupExporter.kt` - JSON 내보내기
- `BackupImporter.kt` - JSON 복구
- `BackupModel.kt` - 백업 구조 정의

#### 의존성
- kotlinx.serialization
- File I/O (내부 저장소, 외부 저장소)
- BackupRepository

#### 예상 구현 내용
```
- 전체 데이터 내보내기 (JSON)
- 선택적 내보내기 (특정 대회만 등)
- JSON 파일 읽기 및 복구
- 데이터 검증
- 충돌 처리 (기존 데이터와의 병합)
- 타임스탬프 기반 버전 관리
```

---

### `util/` - 유틸리티 함수

**담당 에이전트**: `executor-low` (Haiku)
**복잡도**: LOW

#### 설명
앱 전체에서 사용되는 공통 유틸리티 함수들입니다.

#### 핵심 파일
- `DateUtils.kt` - 날짜/시간 포맷팅
- `NumberUtils.kt` - 숫자 포맷팅 (소수점, 천 단위 쉼표 등)
- `StringUtils.kt` - 문자열 처리
- `ValidationUtils.kt` - 데이터 검증
- `FileUtils.kt` - 파일 I/O
- `PermissionUtils.kt` - 권한 요청

#### 의존성
- Kotlin stdlib
- Android framework

#### 예상 구현 내용
```
- 날짜 포맷팅 (yyyy-MM-dd 등)
- 숫자 포맷팅 (1,000.5)
- 이메일/전화번호 유효성 검증
- 파일 크기 포맷팅
- 권한 확인/요청
```

---

## 의존성 그래프

### 계층 구조

```
┌─────────────────────────────────────────────┐
│ UI Layer (Jetpack Compose)                  │
├─────────────────────────────────────────────┤
│ Home │ Member │ Tournament │ Score │ Stats   │
│ Settings │ Share │ PIN │ Components         │
├─────────────────────────────────────────────┤
│ ViewModel + StateFlow (UI State Management)  │
├─────────────────────────────────────────────┤
│ Domain Layer (Business Logic)                │
├─────────────────────────────────────────────┤
│ Repository (MemberRepo, TournamentRepo...)   │
├─────────────────────────────────────────────┤
│ Data Layer                                   │
├─────────────────────────────────────────────┤
│ DAO │ Entity │ TypeConverter │ DataStore    │
├─────────────────────────────────────────────┤
│ Room Database + Preferences DataStore        │
└─────────────────────────────────────────────┘
```

### 모듈 간 의존성

```
home/
  └─ MemberRepository
  └─ TournamentRepository
  └─ ScoreRepository

member/
  └─ MemberRepository
  └─ util/ValidationUtils

tournament/
  └─ TournamentRepository
  └─ MemberRepository
  └─ util/ValidationUtils

score/
  ├─ ScoreRepository
  ├─ OcrService
  ├─ share/ImageGenerator (결과 공유)
  └─ util/ValidationUtils

statistics/
  └─ ScoreRepository
  └─ util/NumberUtils

share/
  └─ ScoreRepository
  └─ components/ImageGenerator

ocr/
  └─ HttpClient (Retrofit)
  └─ util/ValidationUtils

backup/
  └─ BackupRepository
  └─ data/model/* (직렬화)
  └─ util/FileUtils

di/
  └─ 모든 Repository
  └─ Room Database
  └─ DataStore
```

---

## 구현 순서 (권장)

### Phase 1: Foundation
1. **data/local/entity/** - 데이터베이스 스키마 정의
2. **data/local/dao/** - DAO 인터페이스 구현
3. **data/model/** - 도메인 모델 정의
4. **di/** - 의존성 주입 설정

### Phase 2: Repository & Utils
5. **data/repository/** - Repository 구현
6. **util/** - 유틸리티 함수
7. **backup/** - 백업/복구 기능

### Phase 3: UI Foundation
8. **ui/theme/** - 테마 정의
9. **ui/components/** - 공유 컴포넌트
10. **ui/navigation/** - 네비게이션 그래프

### Phase 4: Core Features
11. **ui/member/** - 회원 관리 화면
12. **ui/tournament/** - 대회 운영 화면
13. **ui/home/** - 대시보드

### Phase 5: Advanced Features
14. **ocr/** - Naver CLOVA OCR API 통합
15. **ui/score/** - 스코어 입력 (OCR + 수동)
16. **ui/statistics/** - 통계 & 차트
17. **ui/share/** - KakaoTalk 공유

### Phase 6: Secondary Features
18. **ui/settings/** - 앱 설정
19. **ui/pin/** - PIN 보안

---

## 에이전트 선택 가이드

### 언제 어떤 에이전트를 사용할까?

| 작업 | 에이전트 | 이유 |
|------|---------|------|
| 데이터베이스 스키마 설계 | executor (sonnet) | 복잡한 비즈니스 로직, 성능 고려 필요 |
| Entity/DAO 구현 | executor (sonnet) | 복잡한 SQL 쿼리, 트랜잭션 관리 |
| Hilt 모듈 설정 | executor-low (haiku) | 표준 DI 패턴, 복잡도 낮음 |
| UI 화면 구현 | designer (sonnet) | UI/UX 설계, 컴포넌트 배치, 스타일링 |
| 대시보드 디자인 | designer (sonnet) | 복잡한 레이아웃, 여러 컴포넌트 |
| 공유 컴포넌트 | designer-low (haiku) | 간단한 Compose 컴포넌트 |
| OCR 기능 (Naver CLOVA OCR) | executor-high (opus) | 복잡한 API 통합, 결과 처리 로직 |
| 스코어 입력 UI | executor-high (opus) | 복잡한 상태 관리, OCR 처리 |
| 통계 계산 | designer (sonnet) | 데이터 시각화, 차트 생성 |
| 백업/복구 | executor (sonnet) | 파일 I/O, 직렬화, 오류 처리 |
| 유틸리티 함수 | executor-low (haiku) | 간단한 함수, 표준 라이브러리 사용 |
| PIN 보안 | executor-low (haiku) | 간단한 암호화, 생체인식 통합 |
| KakaoTalk 공유 | executor (sonnet) | API 통합, 이미지 생성, 공유 로직 |

---

## 커뮤니케이션 규칙

### 모듈 간 통신

1. **Repository 기반 통신**
   - UI Layer는 Repository를 통해서만 데이터 접근
   - Repository는 DAO와 도메인 모델을 중개

2. **Dependency Injection**
   - 모든 의존성은 Hilt를 통해 주입
   - 직접 인스턴스 생성 금지

3. **Coroutines & Flow**
   - 비동기 작업은 Coroutines 사용
   - Repository에서 Flow<> 반환하여 반응형 처리

4. **Error Handling**
   - Result 타입 또는 try-catch로 오류 처리
   - UI에서 오류 메시지 사용자에게 표시

---

## 체크리스트

### 새 기능 추가 시
- [ ] 필요한 Entity/DAO 정의 (있다면)
- [ ] Repository 메서드 추가 (있다면)
- [ ] ViewModel 작성
- [ ] UI 구현
- [ ] 유닛 테스트 작성
- [ ] 통합 테스트 (필요시)

### 모듈 간 의존성 추가 시
- [ ] 의존성이 순환 참조를 만들지 않는지 확인
- [ ] Hilt 모듈에서 바인딩 추가
- [ ] 테스트 업데이트

---

## 참고 자료

- [Jetpack Compose 공식 문서](https://developer.android.com/jetpack/compose)
- [Room 데이터베이스](https://developer.android.com/training/data-storage/room)
- [Hilt 의존성 주입](https://developer.android.com/training/dependency-injection/hilt-android)
- [Android 아키텍처 권장사항](https://developer.android.com/topic/architecture)
- [Naver CLOVA OCR API](https://www.ncloud.com/product/ai/clova-ocr)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

**최종 수정**: 2026-02-09
**버전**: 1.0
