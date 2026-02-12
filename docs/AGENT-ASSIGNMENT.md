# 서브에이전트 배정표

> 볼링클럽 관리 앱 - 모듈별 에이전트 배정 및 실행 계획

---

## 0. 활성 스킬

모든 작업에 걸쳐 다음 스킬이 자동으로 활성화됩니다.

| 스킬 | 트리거 | 역할 |
|------|--------|------|
| **`frontend-ui-ux`** | UI/컴포넌트/스타일링 작업 시 자동 | 디자이너 관점의 UI/UX 설계. 모든 화면 작업에 자동 적용 |
| **`bkit:mobile-app`** | 모바일 앱 개발 전반 | React Native/Flutter/Expo 가이드 대신 Android 네이티브 가이드로 활용. 크로스플랫폼 패턴 참조 |
| **`git-master`** | git/commit 작업 시 자동 | 원자적 커밋, 히스토리 관리 |

### UI 작업 규칙
- **모든 UI 화면 작업 시** `frontend-ui-ux` 스킬이 자동 활성화됨
- designer/designer-low 에이전트는 `frontend-ui-ux` 스킬의 디자인 원칙을 따름
- 디자인 목업 없이도 고품질 UI를 생성하는 것이 목표

---

## 1. 단계별 에이전트 배정

### 1단계: 프로젝트 셋업 + 회원 관리 (Week 1-2)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| Room Entity 6개 테이블 | `executor` | sonnet | Yes (독립) | 6개 Entity 클래스 생성 |
| DAO 인터페이스 | `executor` | sonnet | Yes (Entity 후) | CRUD + 복합 쿼리 |
| AppDatabase + Migration | `executor` | sonnet | No (Entity+DAO 후) | DB 초기화, 마이그레이션 |
| Hilt DI 모듈 | `executor-low` | haiku | No (DB 후) | Module, Application 설정 |
| 하단 탭 네비게이션 | `designer-low` | haiku | Yes (독립) | BottomNavigation + NavHost |
| 테마/색상 설정 | `designer-low` | haiku | Yes (독립) | Material3 테마, 볼링 컬러 |
| 회원 목록 화면 | `designer` | sonnet | No (Entity+Nav 후) | LazyColumn, 검색, 필터 |
| 회원 등록/수정 화면 | `designer` | sonnet | Yes (목록과 병렬) | 폼 입력, 유효성 검증 |
| 회원 상세 화면 | `designer` | sonnet | Yes (목록과 병렬) | 개인 정보 + 자동계산 통계 |
| MemberRepository | `executor` | sonnet | No (DAO 후) | 비즈니스 로직 |
| MemberViewModel | `executor` | sonnet | No (Repository 후) | 상태 관리, UI 이벤트 |

### 2단계: 정기전 + 수동 점수 입력 (Week 3-4)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| TournamentRepository | `executor` | sonnet | Yes (독립) | 정기전 CRUD + 참가자 |
| 정기전 목록 화면 | `designer` | sonnet | Yes | 리스트, 상태 뱃지 |
| 정기전 생성 화면 | `designer` | sonnet | Yes | 폼, DatePicker, 옵션 |
| 참가자 체크 화면 | `designer` | sonnet | No (생성 후) | 체크박스 목록 |
| ScoreRepository | `executor` | sonnet | Yes (독립) | 점수 CRUD |
| 수동 점수 입력 화면 | `designer` | sonnet | No (Score Repo 후) | 숫자 키패드, 범위 검증 |
| RankingCalculator | `executor` | sonnet | Yes (독립) | 순위 알고리즘 (4단계 타이브레이커) |
| 정기전 상세 화면 | `designer` | sonnet | No (순위 후) | 점수표, 순위, 버튼 |
| 홈 대시보드 | `designer` | sonnet | Yes (독립) | 카드 UI, 요약 정보 |

### 3단계: OCR + 핸디캡 (Week 5-6)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| Naver CLOVA OCR API 클라이언트 | `executor-high` | opus | Yes (독립) | Retrofit, API 키 암호화 |
| OCR 프롬프트 설계 | `architect` | opus | Yes (독립) | 볼링 점수표 인식 최적화 |
| CameraX 촬영 | `executor` | sonnet | Yes (독립) | 카메라 + 이미지 저장 |
| 이미지 전처리 | `executor` | sonnet | Yes (독립) | 밝기/대비 보정 |
| OCR 결과 파서 | `executor-high` | opus | No (API 후) | JSON 파싱 + 회원 매칭 |
| OCR 미리보기 화면 | `designer` | sonnet | No (파서 후) | 수정 가능한 결과 표시 |
| HandicapCalculator | `executor-low` | haiku | Yes (독립) | 간단한 계산 로직 |
| 핸디캡 설정 UI | `designer-low` | haiku | Yes (독립) | 설정 화면 일부 |

### 4단계: 팀전 (Week 7)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| TeamRepository | `executor` | sonnet | Yes | 팀 CRUD + 배정 |
| 수동 팀 편성 UI | `designer` | sonnet | No (Repo 후) | 드래그&드롭/선택 |
| AutoTeamAssigner | `executor` | sonnet | Yes (독립) | 스네이크 드래프트 알고리즘 |
| TeamRankingCalculator | `executor-low` | haiku | Yes (독립) | 팀 합산 순위 |
| 팀전 결과 UI | `designer` | sonnet | No (순위 후) | 팀별 점수표 |

### 5단계: 카카오톡 공유 (Week 8)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| RankingImageGenerator | `executor` | sonnet | Yes | Canvas API 순위표 |
| PersonalScoreCardGenerator | `executor` | sonnet | Yes (병렬) | 개인 성적 카드 |
| ShareUtil | `executor-low` | haiku | Yes (독립) | Share Intent 래퍼 |
| 미리보기 다이얼로그 | `designer` | sonnet | No (이미지 후) | 공유 전 미리보기 |
| 일정 알림 공유 | `executor-low` | haiku | Yes (독립) | 텍스트 메시지 공유 |

### 6단계: 통계 (Week 9)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| StatisticsRepository | `executor` | sonnet | Yes | 복합 SQL 쿼리 |
| 개인 통계 화면 | `designer` | sonnet | No (Repo 후) | 드롭다운 + 통계 카드 |
| MPAndroidChart 차트 | `designer` | sonnet | No (Repo 후) | 점수 추이 라인 차트 |
| 클럽 통계 화면 | `designer` | sonnet | Yes (개인과 병렬) | 랭킹, MVP, 히스토리 |
| 통계 탭 구성 | `designer-low` | haiku | No (위 화면들 후) | 탭 전환 |

### 7단계: 백업/설정 (Week 10)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| BackupManager | `executor` | sonnet | Yes | JSON 내보내기 |
| RestoreManager | `executor` | sonnet | Yes (병렬) | JSON 복원 |
| AutoBackupWorker | `executor` | sonnet | Yes (병렬) | WorkManager 자동 백업 |
| CsvExporter | `executor-low` | haiku | Yes (독립) | CSV 내보내기 |
| 설정 화면 | `designer` | sonnet | No (백업 후) | 전체 설정 통합 |
| PIN 잠금 화면 | `designer-low` | haiku | Yes (독립) | 4자리 PIN |
| 앱 아이콘 + 스플래시 | `designer` | sonnet | Yes (독립) | 볼링 테마 |

### 8단계: 테스트 + 배포 (Week 11)

| 작업 | 에이전트 | 모델 | 병렬 가능 | 설명 |
|------|---------|------|---------|------|
| Unit 테스트 작성 | `tdd-guide` | sonnet | Yes | Repository, ViewModel |
| UI 테스트 작성 | `qa-tester` | sonnet | Yes (병렬) | Compose E2E |
| 통합 시나리오 테스트 | `qa-tester-high` | opus | No (위 테스트 후) | 전체 흐름 |
| 보안 리뷰 | `security-reviewer` | opus | Yes (독립) | API 키, 데이터 보호 |
| 코드 리뷰 | `code-reviewer` | opus | No (테스트 후) | 전체 품질 검토 |
| 성능 최적화 | `architect` | opus | No (리뷰 후) | DB 쿼리, 메모리 |
| ProGuard/R8 | `executor-low` | haiku | Yes (독립) | 난독화 설정 |
| Play Store 준비 | `writer` | haiku | Yes (독립) | 스크린샷, 설명문 |

---

## 2. 에이전트별 작업량 요약

| 에이전트 | 모델 | 총 작업 수 | 주요 담당 | 활성 스킬 |
|---------|------|----------|----------|----------|
| `executor` | sonnet | 22개 | Repository, 비즈니스 로직, API 연동 | `bkit:mobile-app` |
| `designer` | sonnet | 20개 | UI 화면, Compose 레이아웃 | **`frontend-ui-ux`** + `bkit:mobile-app` |
| `executor-low` | haiku | 9개 | 간단한 로직, 설정, 유틸리티 | - |
| `designer-low` | haiku | 6개 | 간단한 UI, 테마, 네비게이션 | **`frontend-ui-ux`** |
| `executor-high` | opus | 3개 | OCR 핵심 (API + 파서) | - |
| `architect` | opus | 3개 | 설계 검증, OCR 프롬프트, 성능 | - |
| `tdd-guide` | sonnet | 1개 | Unit 테스트 | - |
| `qa-tester` | sonnet | 1개 | UI 테스트 | - |
| `qa-tester-high` | opus | 1개 | 통합 테스트 | - |
| `security-reviewer` | opus | 1개 | 보안 리뷰 | - |
| `code-reviewer` | opus | 1개 | 코드 리뷰 | - |
| `writer` | haiku | 1개 | Store 문서 | - |

### 스킬 자동 활성화 규칙

| 작업 유형 | 자동 활성 스킬 | 설명 |
|----------|--------------|------|
| UI 화면 구현 | `frontend-ui-ux` | 디자인 목업 없이도 프로덕션급 UI 생성 |
| 모바일 앱 패턴 | `bkit:mobile-app` | 모바일 네이티브 개발 가이드 참조 |
| Git 커밋 | `git-master` | 원자적 커밋, 스타일 감지 |

---

## 3. 병렬 실행 계획

### 최대 병렬도 포인트 (한 번에 3-5개 에이전트 동시 실행)

**1단계 Week 1 - 최대 3개 병렬:**
```
[executor] Room Entity 생성
[designer-low] 네비게이션 구조
[designer-low] 테마/색상 설정
```

**2단계 Week 3 - 최대 4개 병렬:**
```
[executor] TournamentRepository
[designer] 정기전 목록 화면
[designer] 정기전 생성 화면
[executor] RankingCalculator
```

**3단계 Week 5 - 최대 4개 병렬:**
```
[executor-high] Naver CLOVA OCR API 클라이언트
[architect] OCR 프롬프트 설계
[executor] CameraX 촬영
[executor] 이미지 전처리
```

**5단계 Week 8 - 최대 3개 병렬:**
```
[executor] RankingImageGenerator
[executor] PersonalScoreCardGenerator
[executor-low] ShareUtil
```

---

## 4. 비용 최적화 전략

| 전략 | 설명 |
|------|------|
| **haiku 우선** | 단순 작업(설정, 유틸, 네비게이션)은 haiku로 비용 절감 |
| **opus 최소화** | opus는 OCR 핵심, 아키텍처 검증, 보안/코드 리뷰에만 사용 |
| **sonnet 기본** | 대부분의 구현 작업은 sonnet으로 비용/성능 균형 |
| **병렬 실행** | 독립적인 작업은 동시 실행으로 전체 시간 단축 |

**예상 에이전트 호출 비용 비율:**
- haiku (16개 작업): ~15%
- sonnet (44개 작업): ~55%
- opus (9개 작업): ~30%

---

## 5. 검증 체크포인트

각 단계 완료 시 반드시 실행:

| 체크포인트 | 검증 에이전트 | 검증 내용 |
|-----------|------------|----------|
| 1단계 완료 | `architect` | DB 스키마 정합성, MVVM 구조 준수 |
| 2단계 완료 | `architect` | 순위 알고리즘 정확성, 화면 흐름 |
| 3단계 완료 (MVP) | `architect` + `security-reviewer-low` | OCR 파이프라인, API 키 보안 |
| 5단계 완료 | `code-reviewer-low` | 이미지 생성 품질, 공유 기능 |
| 7단계 완료 | `security-reviewer` | 백업 데이터 무결성, PIN 보안 |
| 8단계 완료 | `code-reviewer` + `architect` | 최종 품질, 배포 준비 |
