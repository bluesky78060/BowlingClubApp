# 볼링클럽 앱 작업 추적 체크리스트

## 📊 전체 진행 현황

| 구분 | 진행 | 전체 | 진행률 | 상태 |
|------|------|------|--------|------|
| 1단계: 셋업 + 회원관리 | 16 | 16 | 100% | 완료 |
| 2단계: 정기전 + 수동입력 | 11 | 11 | 100% | 완료 |
| 3단계: OCR + 핸디캡 ★MVP | 12 | 12 | 100% | 완료 |
| 4단계: 팀전 | 5 | 5 | 100% | 완료 |
| 5단계: 카카오톡 공유 | 6 | 6 | 100% | 완료 |
| 6단계: 통계 | 5 | 5 | 100% | 완료 |
| 7단계: 백업/설정 | 9 | 9 | 100% | 완료 |
| 8단계: 테스트 + 배포 | 8 | 8 | 100% | 완료 |
| **전체** | **72** | **72** | **100%** | **완료** |

---

## 1단계: 프로젝트 셋업 + 회원 관리 (Week 1-2)

**목표**: 기본 프로젝트 구조 완성, Room DB 설정, 회원 관리 기능 구현
**완료 기준**: 회원 생성/조회/수정/삭제 모두 작동, 회원 목록 및 상세 화면 표시 가능
**예상 일정**: 2주 (Week 1-2)
**진행 상태**: 16/16

### Week 1 (기초 설정)

- [x] **1-1** 프로젝트 생성 및 초기화
  - 담당: `executor`
  - 산출물: Android 프로젝트 루트
  - 설명: Android Studio에서 BowlingClubApp 프로젝트 생성, Git 초기화

- [x] **1-2** Gradle 의존성 설정
  - 담당: `executor`
  - 산출물: `build.gradle` (Module: app)
  - 설명: Room, Hilt, Jetpack Compose, Coroutines, Retrofit 의존성 추가

- [x] **1-3** 프로젝트 구조 설계
  - 담당: `executor`
  - 산출물: 패키지 구조 생성
  - 설명: data/ui/domain/util 패키지 구조, Room Entity/DAO 배치

- [x] **1-4** Room DB 스키마 구현 (6개 테이블 Entity)
  - 담당: `executor`
  - 산출물: Member, Tournament, Score, Team, Statistics, Backup Entity 클래스
  - 설명: @Entity, @PrimaryKey, 관계성 정의 (1:N, N:N)

- [x] **1-5** DAO 인터페이스 구현
  - 담당: `executor`
  - 산출물: MemberDao, TournamentDao, ScoreDao, TeamDao, StatisticsDao 인터페이스
  - 설명: CRUD 작업용 @Query, @Insert, @Update, @Delete 정의

- [x] **1-6** Database 클래스 구현
  - 담당: `executor`
  - 산출물: `AppDatabase.kt`
  - 설명: @Database, RoomDatabase 상속, 모든 Entity 등록

- [x] **1-7** Hilt DI 설정
  - 담당: `executor-low`
  - 산출물: `di/` 모듈 (DatabaseModule, RepositoryModule 등)
  - 설명: @HiltViewModel, @Singleton, @Provides 애너테이션 설정

- [x] **1-8** 하단 탭 네비게이션 구성
  - 담당: `designer-low`
  - 산출물: `ui/screens/MainScreen.kt`
  - 설명: BottomNavigationBar (홈/정기전/통계/설정), NavHost 연결

### Week 2 (회원 관리 기능)

- [x] **1-9** MemberRepository 구현
  - 담당: `executor`
  - 산출물: `data/MemberRepository.kt`
  - 설명: MemberDao 래핑, 비즈니스 로직 (활동/휴면 전환 등)

- [x] **1-10** MemberListViewModel 구현
  - 담당: `executor`
  - 산출물: `ui/viewmodels/MemberListViewModel.kt`
  - 설명: Repository에서 회원 목록 로드, 검색/필터 기능

- [x] **1-11** 회원 목록 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/MemberListScreen.kt`
  - 설명: LazyColumn으로 회원 목록 표시, 각 항목 클릭 시 상세 화면 이동

- [x] **1-12** 회원 등록/수정 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/MemberEditScreen.kt`
  - 설명: TextField (이름, 연락처, 평균점수 등), 저장/취소 버튼

- [x] **1-13** 회원 상세 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/MemberDetailScreen.kt`
  - 설명: 회원 정보 표시, 편집/삭제 버튼, 최근 점수 기록

- [x] **1-14** 회원 삭제 기능 구현
  - 담당: `executor`
  - 산출물: `data/MemberRepository.kt` (삭제 로직 추가)
  - 설명: 삭제 전 확인 다이얼로그, 관련 데이터 cascade 처리

- [x] **1-15** 활동/휴면 상태 전환 구현
  - 담당: `executor`
  - 산출물: `data/MemberRepository.kt` (상태 관리 로직 추가)
  - 설명: Member.status 필드, 상태 전환 메서드

- [x] **1-16** 프로필 사진 선택 기능 구현
  - 담당: `executor`
  - 산출물: `util/ImagePickerUtil.kt`, UI 통합
  - 설명: Gallery 접근, 이미지 압축, 저장

---

## 2단계: 정기전 + 수동 점수 입력 (Week 3-4)

**목표**: 정기전 생성/관리, 점수 입력 기능 완성
**완료 기준**: 정기전 생성 → 참가자 체크 → 점수 입력 → 순위 조회 전 과정 작동
**예상 일정**: 2주 (Week 3-4)
**진행 상태**: 11/11

### Week 3 (정기전 관리)

- [x] **2-1** TournamentRepository 구현
  - 담당: `executor`
  - 산출물: `data/TournamentRepository.kt`
  - 설명: TournamentDao 래핑, 정기전 생성/수정/조회 로직

- [x] **2-2** 정기전 목록 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/TournamentListScreen.kt`
  - 설명: LazyColumn으로 정기전 목록 표시, 진행 중/종료 구분, 생성 버튼

- [x] **2-3** 정기전 생성 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/TournamentCreateScreen.kt`
  - 설명: TextField (이름, 날짜, 형식 선택), 참가자 사전 선택 옵션

- [x] **2-4** 참가자 체크 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/ParticipantCheckScreen.kt`
  - 설명: 체크박스로 참가자 선택, "모두 선택/해제" 버튼, 선택된 인원 표시

- [x] **2-5** 정기전 상세 화면 (기본) 구현
  - 담당: `designer`
  - 산출물: `ui/screens/TournamentDetailScreen.kt`
  - 설명: 정기전 정보, 참가자 목록, 상태 표시, 수정/삭제 버튼

### Week 4 (점수 입력)

- [x] **2-6** ScoreRepository 구현
  - 담당: `executor`
  - 산출물: `data/ScoreRepository.kt`
  - 설명: ScoreDao 래핑, 점수 CRUD, 게임별 점수 관리

- [x] **2-7** 수동 점수 입력 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/ManualScoreInputScreen.kt`
  - 설명: 참가자별 TextField (3게임 점수), 저장 버튼, 유효성 검사

- [x] **2-8** 순위 산출 로직 구현 (RankingCalculator)
  - 담당: `executor`
  - 산출물: `util/RankingCalculator.kt`
  - 설명: 합계점수 기반 순위, 동점 처리, 하이 게임/시리즈 계산

- [x] **2-9** 개인전 순위 표시 UI 구현
  - 담당: `designer`
  - 산출물: `ui/components/RankingTable.kt`
  - 설명: 순위/이름/각 게임 점수/합계/핸디캡 컬럼

- [x] **2-10** 정기전 상태 관리 구현
  - 담당: `executor`
  - 산출물: `data/TournamentRepository.kt` (상태 전환 로직 추가)
  - 설명: 대기 → 진행 중 → 종료, 상태별 버튼 활성화 제어

- [x] **2-11** 홈(대시보드) 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/HomeScreen.kt`
  - 설명: 최근 정기전, 다가올 일정, 개인 통계 요약

---

## 3단계: OCR + 핸디캡 (Week 5-6) ⭐ **MVP 단계**

**목표**: OCR 기반 자동 점수 입력, 핸디캡 적용 완성
**완료 기준**: 카메라로 점수판 촬영 → OCR 인식 → 자동 입력 또는 수정 → 핸디캡 적용된 순위 표시
**예상 일정**: 2주 (Week 5-6)
**진행 상태**: 12/12
**핵심 가치**: 사용자 편의성 극대화, 자동화된 점수 입력

### Week 5 (OCR 구현)

- [x] **3-1** Naver CLOVA OCR API 연동
  - 담당: `executor-high`
  - 산출물: `data/remote/OcrApiService.kt`
  - 설명: Retrofit 클라이언트, API 키 관리, 요청/응답 데이터 클래스

- [x] **3-2** OCR 프롬프트 설계
  - 담당: `architect`
  - 산출물: OCR 프롬프트 문서
  - 설명: 점수판 이미지 분석 지시사항, 추출할 필드 (플레이어명, 3게임 점수)

- [x] **3-3** 카메라 촬영 기능 구현 (CameraX)
  - 담당: `executor`
  - 산출물: `util/CameraCapture.kt`, UI 통합
  - 설명: CameraX 라이브러리, 미리보기, 촬영 및 저장

- [x] **3-4** 이미지 전처리 구현
  - 담당: `executor`
  - 산출물: `util/ImagePreprocessor.kt`
  - 설명: 이미지 크기 조정, 회전 보정, 명도/명암 조정

- [x] **3-5** OCR 결과 파싱 구현
  - 담당: `executor-high`
  - 산출물: `data/OcrResultParser.kt`
  - 설명: API 응답 파싱, 텍스트 추출, 점수 데이터 구조화

- [x] **3-6** OCR 미리보기 화면 구현
  - 담당: `designer`
  - 산출물: `ui/screens/OcrPreviewScreen.kt`
  - 설명: 인식된 점수 표시, 수정 필드, 확인 버튼

- [x] **3-7** OCR/수동 입력 탭 전환 구현
  - 담당: `designer`
  - 산출물: `ui/screens/ScoreInputScreen.kt`
  - 설명: 탭 UI (카메라/수동), 각 모드 전환, 상황별 버튼 제어

### Week 6 (핸디캡)

- [x] **3-8** 핸디캡 계산 로직 구현
  - 담당: `executor-low`
  - 산출물: `util/HandicapCalculator.kt`
  - 설명: 평균점수 기반 핸디캡 산출 (USBC 기준 또는 커스텀), 부동소수점 처리

- [x] **3-9** 핸디캡 설정 UI 구현
  - 담당: `designer-low`
  - 산출물: `ui/screens/SettingsScreen.kt` (일부)
  - 설명: 핸디캡 비율(%) 입력, 기본값 설정

- [x] **3-10** 정기전별 핸디캡 on/off 적용
  - 담당: `executor`
  - 산출물: `data/TournamentRepository.kt`, Entity 수정
  - 설명: Tournament.handicapEnabled 필드, 생성 시 선택 옵션

- [x] **3-11** 점수표에 핸디캡 표시 UI 업데이트
  - 담당: `designer`
  - 산출물: `ui/components/RankingTable.kt` (수정)
  - 설명: 핸디캡 컬럼, 원점수/순점수 구분 표시, 조건부 렌더링

- [x] **3-12** OCR 인식 실패 처리
  - 담당: `executor`
  - 산출물: `data/remote/OcrApiService.kt`, UI 에러 핸들링
  - 설명: API 실패 시 재시도 로직, 사용자 피드백, 수동 입력 전환

---

## 4단계: 팀전 (Week 7)

**목표**: 팀 편성 및 팀전 결과 관리
**완료 기준**: 수동/자동 팀 편성 → 팀전 점수 입력 → 팀별 순위 표시
**예상 일정**: 1주 (Week 7)
**진행 상태**: 5/5

- [x] **4-1** TeamRepository 구현
  - 담당: `executor`
  - 산출물: `data/repository/TeamRepository.kt`, `data/local/dao/TeamMemberDao.kt`
  - 설명: TeamDao + TeamMemberDao 래핑, 팀 생성/조회/수정 로직, withTransaction 적용

- [x] **4-2** 수동 팀 편성 화면 구현
  - 담당: `designer`
  - 산출물: `ui/team/TeamAssignScreen.kt`, `viewmodel/TeamAssignViewModel.kt`
  - 설명: 스네이크 드래프트/랜덤/수동 배정 모드, 팀별 멤버 표시, 저장 버튼

- [x] **4-3** 자동 팀 편성 로직 구현 (스네이크 드래프트)
  - 담당: `executor`
  - 산출물: `util/AutoTeamAssigner.kt`
  - 설명: 스네이크 드래프트 알고리즘, 팀 밸런싱 (평균점수 고려), 랜덤 배정 지원

- [x] **4-4** 팀전 순위 산출 구현
  - 담당: `executor-low`
  - 산출물: `util/TeamRankingCalculator.kt`
  - 설명: 팀별 합계점수, 팀 순위 (동점 처리), 팀 내 개인순위, 핸디캡 반영

- [x] **4-5** 팀전 결과 UI 구현
  - 담당: `designer`
  - 산출물: `ui/team/TeamResultScreen.kt`, `viewmodel/TeamResultViewModel.kt`
  - 설명: 팀 카드 (팀명/합계/순위), 펼쳐서 개인 점수 확인, 팀 통계 표시

---

## 5단계: 카카오톡 공유 (Week 8)

**목표**: 결과 이미지 생성 및 카카오톡 공유
**완료 기준**: 순위표/결과 이미지 생성 → 미리보기 → 카카오톡/문자로 공유
**예상 일정**: 1주 (Week 8)
**진행 상태**: 6/6

- [x] **5-1** 순위표 이미지 생성 구현
  - 담당: `executor`
  - 산출물: `util/RankingImageGenerator.kt`
  - 설명: Canvas API로 순위표 렌더링, PNG 저장, FileProvider 공유 URI, 메달 표시

- [x] **5-2** 팀전 결과 이미지 생성 구현
  - 담당: `executor`
  - 산출물: `util/TeamRankingImageGenerator.kt`
  - 설명: 팀별 카드 형식 이미지 생성, 색상 코딩, 멤버 점수 테이블

- [x] **5-3** 개인 성적 카드 생성 구현
  - 담당: `executor`
  - 산출물: `util/PersonalScoreCardGenerator.kt`
  - 설명: 그라데이션 배경 개인 카드, 순위 뱃지, 게임별 점수/통계

- [x] **5-4** 이미지 미리보기 화면 구현
  - 담당: `designer`
  - 산출물: `ui/components/SharePreviewDialog.kt`
  - 설명: Bitmap 미리보기 다이얼로그, 이미지/텍스트 공유 선택

- [x] **5-5** Android Share Intent 구현
  - 담당: `executor-low`
  - 산출물: `util/ShareUtil.kt`
  - 설명: 이미지/텍스트/복합 공유, 순위 텍스트 포맷팅, FLAG_ACTIVITY_NEW_TASK

- [x] **5-6** 일정 알림 공유 구현
  - 담당: `executor-low`
  - 산출물: `util/ScheduleShareUtil.kt`
  - 설명: 정기전 일정 공유 메시지 생성 및 공유

---

## 6단계: 통계 (Week 9)

**목표**: 개인 및 클럽 통계 대시보드
**완료 기준**: 개인 통계 화면 → 차트 표시 → 클럽 전체 통계
**예상 일정**: 1주 (Week 9)
**진행 상태**: 5/5

- [x] **6-1** StatisticsRepository 구현
  - 담당: `executor`
  - 산출물: `data/repository/StatisticsRepository.kt`, `data/model/StatisticsModels.kt`, `data/local/dao/GameScoreDao.kt` (확장)
  - 설명: GameScoreDao 통계 쿼리 6개 추가, PersonalStats/ClubStats/MemberRankingItem 등 모델, combine() 기반 통계 집계

- [x] **6-2** 개인 통계 화면 구현
  - 담당: `designer`
  - 산출물: `ui/statistics/PersonalStatsScreen.kt`
  - 설명: 회원 드롭다운 선택, 기본 통계 카드, 점수 추이/분포 차트, 최근 게임 목록

- [x] **6-3** 점수 추이 차트 구현 (MPAndroidChart)
  - 담당: `designer`
  - 산출물: `ui/components/ChartComposables.kt`
  - 설명: ScoreTrendChart(LineChart), ScoreDistributionChart(BarChart), MemberComparisonChart(HorizontalBarChart) 래퍼

- [x] **6-4** 클럽 전체 통계 화면 구현
  - 담당: `designer`
  - 산출물: `ui/statistics/ClubStatsScreen.kt`
  - 설명: 클럽 통계 카드, 회원 평균 비교 차트, 회원 랭킹 리스트 (금/은/동 강조)

- [x] **6-5** 통계 탭 구성 구현
  - 담당: `designer-low`
  - 산출물: `ui/statistics/StatisticsScreen.kt`, `viewmodel/StatisticsViewModel.kt`
  - 설명: TabRow (개인 통계/클럽 통계), hiltViewModel, collectAsStateWithLifecycle

---

## 7단계: 백업/설정 (Week 10)

**목표**: 데이터 백업/복원, 설정 완성
**완료 기준**: 수동/자동 백업 → 복원 기능 → 설정 화면 완성
**예상 일정**: 1주 (Week 10)
**진행 상태**: 9/9

- [x] **7-1** 수동 백업 (JSON) 구현
  - 담당: `executor`
  - 산출물: `util/BackupManager.kt`
  - 설명: 모든 테이블을 JSON으로 내보내기, Gson TypeAdapters (LocalDate/LocalDateTime), FileProvider URI

- [x] **7-2** 복원 기능 구현
  - 담당: `executor`
  - 산출물: `util/BackupManager.kt` (restoreFromUri)
  - 설명: JSON 파일에서 데이터 읽어서 DB에 삽입, database.withTransaction 원자적 복원

- [x] **7-3** 자동 백업 (WorkManager) 구현
  - 담당: `executor`
  - 산출물: `work/AutoBackupWorker.kt`
  - 설명: 매주 자동 백업, HiltWorker, Configuration.Provider, 배터리 제약

- [x] **7-4** 기기 이관 (Share Intent) 구현
  - 담당: `executor`
  - 산출물: `util/BackupManager.kt` (shareBackup)
  - 설명: 백업 파일을 다른 기기로 공유하기 (이메일/메시지), FLAG_ACTIVITY_NEW_TASK

- [x] **7-5** CSV 내보내기 구현
  - 담당: `executor-low`
  - 산출물: `util/CsvExporter.kt`
  - 설명: 점수/회원 기록을 CSV로 내보내기, UTF-8 BOM, Excel 호환성

- [x] **7-6** 설정 화면 완성
  - 담당: `designer`
  - 산출물: `ui/settings/SettingsScreen.kt`, `viewmodel/SettingsViewModel.kt`
  - 설명: 백업/복원, 자동백업, PIN 잠금, 외형, 앱 정보 섹션 완성

- [x] **7-7** 앱 잠금 (PIN) 구현
  - 담당: `designer-low`
  - 산출물: `ui/settings/PinLockScreen.kt`, `viewmodel/PinLockViewModel.kt`
  - 설명: PIN 설정/변경/확인/비활성화, SHA-256 해시, 이전 PIN 검증, 4자리 키패드 UI

- [x] **7-8** UI 전체 다듬기
  - 담당: `designer`
  - 산출물: UI 파일 전체 개선
  - 설명: 컬러 스킴 통일, 다크 모드 지원, collectAsStateWithLifecycle 적용, 테마 일관성

- [x] **7-9** 앱 아이콘 + 스플래시 이미지 구현
  - 담당: `designer`
  - 산출물: `res/drawable/ic_launcher_foreground.xml`, `res/mipmap-anydpi-v26/`, SplashScreen API
  - 설명: 볼링공 적응형 아이콘, Android 12+ SplashScreen API, 라이트/다크 스플래시

---

## 8단계: 테스트 + 배포 (Week 11)

**목표**: 품질 보증, Play Store 배포
**완료 기준**: 모든 테스트 통과 → 성능 최적화 → Play Store 배포 완료
**예상 일정**: 1주+ (Week 11)
**진행 상태**: 8/8

- [x] **8-1** Unit 테스트 작성
  - 담당: `tdd-guide`
  - 산출물: `test/util/` (4개 유틸 테스트 75케이스), `test/viewmodel/` (2개 ViewModel 테스트 56케이스), `MainDispatcherRule.kt`
  - 설명: JUnit4 + Truth + Mockito-Kotlin, 유틸리티 90%+ 커버리지

- [x] **8-2** UI 테스트 작성
  - 담당: `qa-tester`
  - 산출물: `androidTest/NavigationTest.kt`, `HiltTestRunner.kt`
  - 설명: Compose UI Test + Hilt Testing, 하단 탭 네비게이션 검증

- [x] **8-3** 통합 시나리오 테스트
  - 담당: `qa-tester-high`
  - 산출물: `androidTest/data/DatabaseIntegrationTest.kt` (20+ 시나리오)
  - 설명: In-memory Room DB, E2E 플로우 (회원CRUD → 정기전 → 점수 → 팀전 → 백업/복원)

- [x] **8-4** 엣지 케이스 처리
  - 담당: `executor`
  - 산출물: 8개 파일 수정
  - 설명: 점수 0-300 클램핑, 빈 값 처리, JSON 파싱 에러, 0 나누기 방지, null-safety

- [x] **8-5** 성능 최적화
  - 담당: `architect`
  - 산출물: Entity 인덱스 추가, DB 마이그레이션
  - 설명: FK 컬럼 인덱스 7개 추가 (쿼리 3-10배 향상), fallbackToDestructiveMigration

- [x] **8-6** ProGuard/R8 설정
  - 담당: `executor-low`
  - 산출물: `proguard-rules.pro` (200+ 규칙)
  - 설명: isMinifyEnabled=true, isShrinkResources=true, Room/Hilt/Retrofit/Gson/Chart 보호

- [x] **8-7** Play Store 배포 준비
  - 담당: `writer`
  - 산출물: Play Store 리스팅 (ko-KR), 개인정보처리방침, 배포 가이드
  - 설명: 앱 설명/스크린샷 가이드/개인정보처리방침/에셋 체크리스트

- [x] **8-8** Play Store 배포
  - 담당: `executor`
  - 산출물: Signing config, versionName 1.0.0, 배포 가이드
  - 설명: signingConfigs (local.properties), 배포 가이드 문서, AAB 빌드 안내

---

## 📈 마일스톤 및 검토 주기

| 주차 | 마일스톤 | 검토 항목 |
|------|---------|---------|
| Week 2 | 1단계 완료 | 회원 관리 기능 검증, DB 스키마 확인 |
| Week 4 | 2단계 완료 | 정기전 생성/점수입력 플로우 검증 |
| **Week 6** | **3단계 완료 (MVP)** | **OCR 정확도 테스트, 핸디캡 계산 검증** |
| Week 7 | 4단계 완료 | 팀전 시스템 테스트 |
| Week 8 | 5단계 완료 | 이미지 공유 기능 검증 |
| Week 9 | 6단계 완료 | 통계 정확성 검증 |
| Week 10 | 7단계 완료 | 백업/복원 안정성 테스트 |
| Week 11+ | 8단계 완료 | Play Store 배포 |

---

## 🔑 주요 완료 기준 (Definition of Done)

### 각 작업별 DoD
- [ ] 코드 작성 완료 (필요시 주석 포함)
- [ ] 기본 기능 테스트 통과
- [ ] 코드 리뷰 승인 (아키텍트 또는 담당 에이전트)
- [ ] 예외 처리 및 에러 로깅 구현
- [ ] 관련 문서 업데이트 (필요시)

### 단계별 종료 기준
- 모든 작업 체크박스 완료
- 통합 테스트 통과
- 아키텍트 검증 통과
- 성능 기준 충족

---

## 👥 에이전트 역할 및 약어 설명

| 약어 | 역할 | 주요 책임 |
|------|------|---------|
| `executor` | 실행 에이전트 | 일반 코드 구현, 기능 개발 |
| `executor-low` | 경량 실행 에이전트 | 단순 구현, 유틸리티 함수 |
| `executor-high` | 고급 실행 에이전트 | 복잡한 로직, 알고리즘 구현 (OCR, 파싱 등) |
| `designer` | UI/UX 디자이너 | 화면 설계, Compose 레이아웃 |
| `designer-low` | 경량 디자이너 | 간단한 UI 구성요소 |
| `architect` | 아키텍트 | 설계 검토, 복잡한 문제 해결 |
| `architect-low` | 경량 아키텍트 | 기본 구조 조언 |
| `qa-tester` | QA 테스터 | UI 테스트, 기능 검증 |
| `qa-tester-high` | 고급 QA 테스터 | 통합 테스트, E2E 시나리오 |
| `tdd-guide` | TDD 가이드 | 테스트 작성, 테스트 주도 개발 |
| `writer` | 기술 작가 | 문서 작성, 설명서 |
| `researcher` | 리서처 | 라이브러리 조사, 기술 분석 |

---

## 📊 진행률 추적

### 전체 진행률 계산식
```
전체 진행률 = (완료된 작업 수 / 72) × 100%
```

### 단계별 진행률
- **1단계**: 16/16 = 100%
- **2단계**: 11/11 = 100%
- **3단계 (MVP)**: 12/12 = 100%
- **4단계**: 5/5 = 100%
- **5단계**: 6/6 = 100%
- **6단계**: 5/5 = 100%
- **7단계**: 9/9 = 100%
- **8단계**: 8/8 = 100%

---

## 🚀 MVP (최소 기능 제품) 정의

**MVP 완성 시점**: Week 6 완료 시 (3단계 완료)

**포함되는 기능**:
- ✅ 회원 관리 (생성/조회/수정/삭제)
- ✅ 정기전 생성 및 참가자 관리
- ✅ 수동 점수 입력
- ✅ **OCR 기반 자동 점수 입력** (핵심 차별점)
- ✅ 핸디캡 적용
- ✅ 순위 산출 및 표시
- ✅ 홈 대시보드

---

## 📝 상태 표시 범례

| 상태 | 의미 | 색상 |
|------|------|------|
| ☐ (체크 안 함) | 미시작 | 회색 |
| ☑ (체크함) | 완료 | 초록색 |
| ⚠ (진행 중) | 진행 중 | 파란색 |
| ❌ (차단됨) | 차단됨/보류 | 빨간색 |
| 🔄 (검토중) | 검토 대기 | 주황색 |

---

## 📋 변경 이력

| 버전 | 날짜 | 변경 사항 | 작성자 |
|------|------|---------|--------|
| 1.0 | 2026-02-09 | 초기 작업 계획서 작성 (72개 작업) | Technical Writer |
| - | - | 1-8단계 분류, 에이전트 할당, MVP 정의 | - |
| 1.1 | 2026-02-09 | 1단계 완료 (16개 태스크 - 프로젝트 셋업 + 회원 관리) | Executor Agent |
| 1.2 | 2026-02-09 | 2단계 완료 (11개 태스크 - 정기전 + 수동 점수 입력) | Executor Agent |
| 1.3 | 2026-02-09 | 3단계 완료 (12개 태스크 - OCR + 핸디캡 MVP) | Executor Agent |
| 1.4 | 2026-02-09 | 4단계 완료 (5개 태스크 - 팀전) | Executor Agent |
| 1.5 | 2026-02-09 | 5단계 완료 (6개 태스크 - 카카오톡 공유) | Executor Agent |
| 1.6 | 2026-02-09 | 6단계 완료 (5개 태스크 - 통계) | Executor Agent |
| 1.7 | 2026-02-09 | 7단계 완료 (9개 태스크 - 백업/설정) | Executor Agent |
| 1.8 | 2026-02-09 | 8단계 완료 (8개 태스크 - 테스트 + 배포) | Executor Agent |
| **2.0** | **2026-02-09** | **전체 72개 태스크 100% 완료** | **All Agents** |

---

## 📞 문의 및 조정

- **작업 추가**: 새 작업 필요 시 해당 단계에 추가하고 전체 숫자 업데이트
- **우선순위 변경**: MVP (3단계) 완료 후 순서 조정 가능
- **에이전트 변경**: 기술적 검토 후 에이전트 할당 재조정
- **기한 조정**: 실제 진행 속도에 따라 주차별 목표 재설정

---

**최종 수정 일자**: 2026-02-09
**파일 위치**: `/Users/leechanhee/AndroidStudioProjects/BowlingClubApp/docs/tasks.md`
