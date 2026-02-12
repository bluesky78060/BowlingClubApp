# 볼링클럽 관리 앱 - AI 개발 가이드

프로젝트별 AI 어시스턴트 개발 지침. 이 가이드를 따라 일관되고 안전한 코드베이스를 유지합니다.

---

## 1. 프로젝트 개요

### 프로젝트 정보
- **앱명**: 볼링클럽 관리 앱 (Bowling Club Management App)
- **패키지명**: `com.bowlingclub.app`
- **플랫폼**: Android (Native)
- **아키텍처**: MVVM (Model-View-ViewModel)
- **데이터베이스**: Room/SQLite (로컬 전용, 서버 없음)
- **사용자**: 클럽 관리자 1-2명 전용

### 핵심 특징
- 로컬 데이터베이스만 사용 (서버 없음)
- Naver CLOVA OCR을 통한 스코어 인식 (무료 티어)
- Android Share Intent로 카카오톡 공유
- Hilt를 통한 의존성 주입
- Kotlin Coroutines 기반 비동기 처리
- MPAndroidChart를 통한 통계 시각화

### 최소 요구사항
- Min SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 15.0)
- Gradle 8.0+
- Kotlin 2.0+

---

## 2. 기술 스택

### 빌드 및 기본
| 도구 | 버전 | 용도 |
|------|------|------|
| Gradle | 8.0+ | 빌드 시스템 |
| Kotlin | 2.0+ | 주언어 |
| Java | 17 | JDK |

### Jetpack & 안드로이드
| 라이브러리 | 버전 | 용도 |
|----------|------|------|
| Android Gradle Plugin | 8.0+ | AGP |
| Jetpack Compose | 1.6+ | UI 프레임워크 |
| Jetpack Lifecycle | 2.6+ | 라이프사이클 관리 |
| Jetpack ViewModel | 2.6+ | 상태 관리 |
| Room | 2.5+ | 로컬 DB ORM |
| DataStore | 1.0+ | 설정 저장소 |
| Security Crypto | 1.1+ | 암호화된 저장소 |
| Navigation Compose | 2.7+ | 네비게이션 |

### DI & 비동기
| 라이브러리 | 버전 | 용도 |
|----------|------|------|
| Hilt | 2.46+ | 의존성 주입 |
| Kotlin Coroutines | 1.7+ | 비동기 처리 |

### API & 네트워크
| 라이브러리 | 버전 | 용도 |
|----------|------|------|
| Retrofit | 2.9+ | HTTP 클라이언트 (OCR만) |
| Okhttp | 4.11+ | HTTP 레이어 |
| Moshi | 1.15+ | JSON 직렬화 |

### UI 및 시각화
| 라이브러리 | 버전 | 용도 |
|----------|------|------|
| MPAndroidChart | 3.1+ | 차트/그래프 |
| Coil | 2.4+ | 이미지 로딩 |
| Material3 | 1.1+ | Material Design 3 |

### 테스트
| 라이브러리 | 버전 | 범위 |
|----------|------|------|
| JUnit4 | 4.13+ | Unit Test |
| Mockito | 5.0+ | Mocking |
| Truth | 1.1+ | Assertion |
| Robolectric | 4.10+ | Android Unit Test |
| Compose UI Test | 1.6+ | UI Test |

---

## 3. 아키텍처 규칙

### 패키지 구조
```
com.bowlingclub.app/
├── data/
│   ├── db/
│   │   ├── entity/           # Room Entity 정의
│   │   ├── dao/              # Data Access Objects
│   │   └── database/         # Database 클래스
│   ├── repository/           # Repository (데이터 접근 추상화)
│   └── datasource/           # 외부 API (OCR 등)
├── ui/
│   ├── screen/              # 화면별 Composable
│   ├── component/           # 재사용 가능한 UI 컴포넌트
│   ├── navigation/          # Navigation Graph
│   └── theme/               # Theme, Color, Typography
├── viewmodel/               # ViewModel 클래스
├── util/                    # 유틸리티 함수
├── di/                      # Hilt 모듈 (DI 설정)
├── constant/                # 상수 정의
└── App.kt                   # Application 클래스
```

### MVVM 레이어 책임

#### View (UI Layer)
- Jetpack Compose Composable 함수들
- 상태 표시 및 사용자 입력 처리
- ViewModel에서 상태 수집
- 부수 효과 관리 (`LaunchedEffect`, `rememberUpdatedState`)

#### ViewModel
- `ViewModel`을 상속하는 클래스
- UI 상태를 `StateFlow` 또는 `LiveData`로 노출
- 비즈니스 로직을 통한 상태 업데이트
- Repository 계층과 상호작용
- 예: `class MemberViewModel(private val memberRepository: MemberRepository) : ViewModel()`

#### Repository
- 데이터 소스(Room, API) 추상화
- 데이터 일관성 관리
- 캐싱 전략 구현
- 인터페이스로 정의하여 테스트 용이하게 함

#### Data Source
- Room Database (로컬)
- Naver CLOVA OCR API
- EncryptedSharedPreferences (API 키)

### 의존성 주입 (Hilt)

**필수 규칙:**
- 모든 ViewModel, Repository는 Hilt로 제공
- `@HiltViewModel` 데코레이터 사용
- `@Singleton` 또는 적절한 스코프 지정
- Hilt 모듈은 `di/` 패키지에 위치

**예시:**
```kotlin
@HiltViewModel
class MemberViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {
    // 구현
}
```

### 데이터 흐름
```
User Action (UI) → ViewModel → Repository → Room DB / API
                                    ↑
                        (외부 API: Claude Vision)
```

---

## 4. 코딩 컨벤션

### Kotlin 스타일 가이드

#### 명명 규칙
| 요소 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `MemberViewModel`, `MemberRepository` |
| 함수 | camelCase | `getMemberList()`, `updateScore()` |
| 상수 | UPPER_SNAKE_CASE | `MAX_SCORE = 300` |
| 변수 | camelCase | `memberList`, `currentScore` |
| Package | lowercase | `com.bowlingclub.app.data` |
| 파일명 | 클래스명 동일 | `MemberViewModel.kt` |

#### 포매팅
- **들여쓰기**: 4개 공백 (탭 금지)
- **라인 길이**: 최대 120자
- **임포트**: `import` 순서 자동 정렬 (IDE 설정)
- **중괄호**: Allman 스타일 금지, K&R 스타일 사용

```kotlin
// 좋음
if (condition) {
    doSomething()
}

// 나쁨
if (condition)
{
    doSomething()
}
```

#### Null 안전성
- **Nullable 타입**: `String?` 명시적 표현
- **Elvis 연산자**: `value ?: defaultValue`
- **Safe call**: `obj?.property`
- **Not-null assertion**: `!!` 최소화 (피할 수 없을 때만)

```kotlin
// 좋음
val result = member?.name ?: "Unknown"

// 나쁨
val result = member!!.name
```

#### 함수
- **함수 길이**: 최대 30줄 (테스트 제외)
- **파라미터**: 최대 5개 (초과 시 data class 사용)
- **반환 타입**: 명시적 지정 (타입 추론 금지)

```kotlin
// 좋음
fun calculateScore(member: Member, games: List<Game>): Int {
    return games.sumOf { it.score }
}

// 나쁨
fun calculateScore(member, games) = games.sumOf { it.score }
```

#### Collections
- **Immutable**: `List<T>`, `Set<T>`, `Map<K, V>` 선호
- **Mutable**: `mutableListOf()`, `mutableMapOf()` 필요할 때만
- **시퀀스**: 대규모 데이터는 `asSequence()` 사용

```kotlin
// 좋음
val members: List<Member> = repository.getMembers()

// 나쁨
val members: ArrayList<Member> = repository.getMembers()
```

### UI 개발 스킬 규칙

**자동 활성 스킬**: 모든 UI 화면 구현 시 다음 스킬이 자동 적용됩니다.

- **`frontend-ui-ux`**: 디자인 목업 없이도 프로덕션급 UI 생성. Material3 디자인 시스템 준수.
- **`bkit:mobile-app`**: 모바일 네이티브 개발 가이드. Android UX 패턴 참조.

**UI 품질 기준**:

- Material3 디자인 가이드라인 준수
- 일관된 색상 팔레트 (볼링 테마)
- 반응형 레이아웃 (다양한 화면 크기)
- 접근성 (contentDescription, 충분한 터치 영역)
- 다크/라이트 테마 지원
- 애니메이션은 최소한으로 (실용적 앱이므로)

### Jetpack Compose 컨벤션

#### Composable 함수
- **함수명**: 소문자 시작 (관례)
- **@Composable**: 모든 composable 함수에 필수
- **Modifier 파라미터**: 첫 번째 파라미터로 위치
- **Trailing lambda**: 마지막 파라미터가 함수면 괄호 밖

```kotlin
@Composable
fun MemberCard(
    member: Member,
    modifier: Modifier = Modifier,
    onEdit: (Member) -> Unit = {}
) {
    Card(modifier = modifier) {
        // 구현
    }
}

// 사용
MemberCard(member = member) {
    // onEdit lambda
}
```

#### 상태 관리
- **StateFlow**: ViewModel에서 상태 노출
- **collectAsState()**: UI에서 수집
- **remember**: 로컬 UI 상태 (임시)
- **rememberUpdatedState**: 자주 변경되는 콜백

```kotlin
@Composable
fun MemberScreen(viewModel: MemberViewModel) {
    val members by viewModel.members.collectAsState()

    LazyColumn {
        items(members) { member ->
            MemberCard(member)
        }
    }
}
```

#### Preview
- **모든 화면**: @Preview 작성
- **다양한 상태**: 정상, 로딩, 에러 상태
- **기기 크기**: 다양한 해상도 미리보기

```kotlin
@Preview(showBackground = true)
@Composable
fun MemberScreenPreview() {
    MemberScreen(viewModel = MemberViewModel())
}
```

#### 성능
- **Recomposition 최소화**: 상태 범위 최소화
- **key() 함수**: 리스트 아이템에 고유 키 제공
- **derivedStateOf**: 복잡한 계산 캐싱

```kotlin
LazyColumn {
    items(members, key = { it.id }) { member ->
        MemberCard(member)
    }
}
```

---

## 5. DB 스키마 규칙

### Room Entity 설계

#### Members (회원)
```kotlin
@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,                    // 회원 이름
    val joinDate: LocalDate,             // 가입일
    val phoneNumber: String,             // 연락처
    val isActive: Boolean = true,        // 활성 상태
    val createdAt: LocalDateTime,        // 생성일시
    val updatedAt: LocalDateTime         // 수정일시
)
```

#### Tournaments (대회)
```kotlin
@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,                    // 대회명
    val startDate: LocalDate,            // 시작일
    val endDate: LocalDate,              // 종료일
    val description: String? = null,     // 설명
    val type: String,                    // 대회 유형 (e.g., "SINGLE", "TEAM")
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

#### TournamentParticipants (대회 참가자)
```kotlin
@Entity(
    tableName = "tournament_participants",
    indices = [Index(value = ["tournament_id", "member_id"], unique = true)]
)
data class TournamentParticipant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "tournament_id")
    val tournamentId: Int,
    @ColumnInfo(name = "member_id")
    val memberId: Int,
    val joinedDate: LocalDateTime,
    val status: String = "ACTIVE",       // "ACTIVE", "WITHDRAWN"
    @ForeignKey(
        entity = Tournament::class,
        parentColumns = ["id"],
        childColumns = ["tournament_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyTournament: Unit? = null,
    @ForeignKey(
        entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["member_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyMember: Unit? = null
)
```

#### GameScores (게임 스코어)
```kotlin
@Entity(
    tableName = "game_scores",
    indices = [Index(value = ["tournament_id", "member_id", "game_number"])]
)
data class GameScore(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "tournament_id")
    val tournamentId: Int,
    @ColumnInfo(name = "member_id")
    val memberId: Int,
    val gameNumber: Int,                 // 게임 번호 (1, 2, 3...)
    val score: Int,                      // 점수 (0-300)
    val isHandicapped: Boolean = false,  // 핸디캡 적용 여부
    val handicapScore: Int? = null,      // 핸디캡 점수
    val recordedBy: String,              // 기록자 (OCR 또는 수동)
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    @ForeignKey(
        entity = Tournament::class,
        parentColumns = ["id"],
        childColumns = ["tournament_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyTournament: Unit? = null,
    @ForeignKey(
        entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["member_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyMember: Unit? = null
)
```

#### Teams (팀)
```kotlin
@Entity(
    tableName = "teams",
    indices = [Index(value = ["tournament_id"])]
)
data class Team(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "tournament_id")
    val tournamentId: Int,
    val name: String,                    // 팀명
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    @ForeignKey(
        entity = Tournament::class,
        parentColumns = ["id"],
        childColumns = ["tournament_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyTournament: Unit? = null
)
```

#### TeamMembers (팀 멤버)
```kotlin
@Entity(
    tableName = "team_members",
    indices = [Index(value = ["team_id", "member_id"], unique = true)]
)
data class TeamMember(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "team_id")
    val teamId: Int,
    @ColumnInfo(name = "member_id")
    val memberId: Int,
    @ForeignKey(
        entity = Team::class,
        parentColumns = ["id"],
        childColumns = ["team_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyTeam: Unit? = null,
    @ForeignKey(
        entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["member_id"],
        onDelete = ForeignKey.CASCADE
    )
    val foreignKeyMember: Unit? = null
)
```

#### Settings (앱 설정)
```kotlin
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey
    val key: String,                     // 설정 키
    val value: String                    // 설정 값
)
```

### DAO 규칙

- **인터페이스**: `@Dao` 어노테이션 필수
- **메서드명**: 동사 + 명사 패턴 (`getMember()`, `insertTournament()`)
- **반환 타입**: 단건은 `T`, 복수는 `List<T>`, 비동기는 `Flow<T>`
- **Flow 사용**: 관찰 가능한 쿼리는 항상 `Flow<T>` 반환

```kotlin
@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE id = :id")
    fun getMember(id: Int): Flow<Member>

    @Query("SELECT * FROM members WHERE isActive = 1 ORDER BY name")
    fun getAllMembers(): Flow<List<Member>>

    @Insert
    suspend fun insert(member: Member): Long

    @Update
    suspend fun update(member: Member)

    @Delete
    suspend fun delete(member: Member)
}
```

### 데이터베이스 초기화
- `RoomDatabase.Builder`에 `createFromAsset()` 사용 (선택사항)
- 마이그레이션 버전 관리
- `@Database(version = X)` 항상 명시

---

## 6. 테스트 규칙

### 단위 테스트 (Unit Test)

**위치**: `src/test/java/`
**프레임워크**: JUnit4 + Mockito + Truth

#### ViewModel 테스트
```kotlin
class MemberViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val memberRepository = mock<MemberRepository>()
    private lateinit var viewModel: MemberViewModel

    @Before
    fun setup() {
        viewModel = MemberViewModel(memberRepository)
    }

    @Test
    fun loadMembers_success() = runTest {
        // Given
        val expectedMembers = listOf(Member(id = 1, name = "Kim"))
        whenever(memberRepository.getMembers()).thenReturn(
            flowOf(expectedMembers)
        )

        // When
        val result = viewModel.members.first()

        // Then
        assertThat(result).isEqualTo(expectedMembers)
    }
}
```

#### Repository 테스트
```kotlin
class MemberRepositoryTest {
    private val memberDao = mock<MemberDao>()
    private lateinit var repository: MemberRepository

    @Before
    fun setup() {
        repository = MemberRepository(memberDao)
    }

    @Test
    fun insertMember_shouldCallDao() = runTest {
        // Given
        val member = Member(name = "Kim")

        // When
        repository.insertMember(member)

        // Then
        verify(memberDao).insert(member)
    }
}
```

### 통합 테스트 (Integration Test)

**위치**: `src/androidTest/java/`
**프레임워크**: Robolectric + Compose UI Test

#### Room Database 테스트
```kotlin
@RunWith(RobolectricTestRunner::class)
class MemberDaoTest {
    private lateinit var database: BowlingClubDatabase
    private lateinit var memberDao: MemberDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            context = ApplicationProvider.getApplicationContext(),
            klass = BowlingClubDatabase::class.java
        ).build()
        memberDao = database.memberDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieve() = runTest {
        // Given
        val member = Member(name = "Kim")

        // When
        memberDao.insert(member)
        val result = memberDao.getMember(1).first()

        // Then
        assertThat(result.name).isEqualTo("Kim")
    }
}
```

#### Compose UI 테스트
```kotlin
@RunWith(ComposeContentTestRule::class)
class MemberScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysMemberList() {
        composeTestRule.setContent {
            MemberScreen(
                members = listOf(Member(id = 1, name = "Kim")),
                onMemberClick = {}
            )
        }

        composeTestRule.onNodeWithText("Kim").assertIsDisplayed()
    }
}
```

### 테스트 작성 규칙

- **명칭**: `test` + 클래스명 + 메서드명 + 상황
  - 예: `testMemberViewModelLoadMembersSuccess()`
- **구조**: Given-When-Then
- **커버리지**: 최소 70% (비즈니스 로직)
- **Mocking**: Repository 및 외부 의존성만
- **Assertion**: Truth 라이브러리 사용 (`assertThat()`)

---

## 7. 빌드 및 실행 방법

### 빌드 명령어

#### 디버그 빌드
```bash
./gradlew assembleDebug
```

#### 릴리스 빌드
```bash
./gradlew assembleRelease
```

#### 테스트 실행
```bash
# 모든 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests MemberViewModelTest

# Android 통합 테스트
./gradlew connectedAndroidTest
```

#### 린트 검사
```bash
./gradlew lint
```

#### 클린 빌드
```bash
./gradlew clean assembleDebug
```

### 실행 방법

#### 에뮬레이터 실행 (Android Studio)
1. AVD Manager 열기 (`Tools` → `Device Manager`)
2. API Level 26+ 에뮬레이터 선택
3. `Run 'app'` 클릭 (또는 Shift+F10)

#### 실제 기기 연결
```bash
# 연결된 기기 확인
adb devices

# APK 설치
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 앱 실행
adb shell am start -n com.bowlingclub.app/.ui.MainActivity
```

### 빌드 캐시 문제 해결

```bash
# Gradle 캐시 클리어
./gradlew clean

# Build 캐시만 클리어
rm -rf .gradle/caches

# 재빌드
./gradlew assembleDebug
```

---

## 8. 주의사항 및 보안

### API 키 관리

#### Naver CLOVA OCR API 키
**저장 위치**: `EncryptedSharedPreferences`

```kotlin
// 키 저장 (설정 화면에서)
val encryptedSharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedSharedPreferences.edit().putString("naver_ocr_secret_key", apiKey).apply()

// 키 읽기 (OCR 요청 시)
val apiKey = encryptedSharedPreferences.getString("naver_ocr_secret_key", null)
    ?: throw IllegalStateException("API 키가 설정되지 않았습니다")
```

**규칙**:
- API 키는 절대 코드에 하드코딩 금지
- BuildConfig에 저장 금지
- 설정 화면에서만 입력 가능
- 권한: 관리자만 접근 가능

### 네트워크 정책

#### 허용
- **Naver CLOVA OCR API**: OCR 스코어 인식
- **Endpoint**: Naver Cloud Platform CLOVA OCR API Gateway

#### 금지
- 서버 동기화
- 클라우드 백업
- 원격 사용자 인증
- 데이터 전송 (API 키 제외)

#### 네트워크 보안 설정
```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">apigw.ntruss.com</domain>
    </domain-config>
    <!-- 다른 모든 도메인은 HTTPS 필수 -->
    <default-config cleartextTrafficPermitted="false" />
</network-security-config>
```

### 데이터 보안

- **로컬 DB**: SQLite 암호화 (Room 기본값)
- **공유 저장소**: 앱 전용 디렉토리만 사용
- **임시 파일**: 캐시 디렉토리 사용, 자동 정리
- **민감한 데이터**: 메모리에서 즉시 초기화

### 권한 관리

#### 필수 권한 (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

#### 런타임 권한 (Android 6.0+)
```kotlin
// 카메라 권한 요청 (OCR 이미지 캡처)
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // 카메라 열기
    }
}

Button(onClick = { launcher.launch(Manifest.permission.CAMERA) })
```

### 로깅

- **Log 레벨**:
  - `DEBUG`: 개발 중에만 (BuildConfig.DEBUG 확인)
  - `INFO`: 중요한 이벤트
  - `ERROR`: 오류 상황
  - `VERBOSE`: 금지 (프로덕션)
- **민감한 정보**: API 키, 개인 정보 로깅 금지

```kotlin
// 좋음
if (BuildConfig.DEBUG) {
    Log.d("MemberViewModel", "Members loaded: ${members.size}")
}

// 나쁨
Log.d("MemberViewModel", "API key: $apiKey")
```

---

## 9. 커밋 메시지 규칙

### 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type (필수)

| Type | 의미 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 | `feat(member): add member search` |
| `fix` | 버그 수정 | `fix(tournament): handle null scores` |
| `refactor` | 코드 리팩토링 | `refactor(ui): simplify member card` |
| `test` | 테스트 추가/수정 | `test(viewmodel): add member loading tests` |
| `docs` | 문서 수정 | `docs: update API documentation` |
| `style` | 포매팅, 세미콜론 등 | `style: format kotlin files` |
| `chore` | 빌드, 의존성 등 | `chore: upgrade gradle to 8.1` |
| `perf` | 성능 개선 | `perf(score): optimize database query` |

### Scope (선택)

패키지 또는 기능 영역:
- `member`, `tournament`, `score`, `ui`, `db`, `api`, `theme`, `navigation`

### Subject (필수)

- 명령형 사용 (과거형 금지): "add" (X "added")
- 50자 이내
- 대문자 사용 금지
- 마침표 금지

### Body (선택)

- 자세한 설명 (더 복잡한 변경)
- 72자 이내 줄바꿈
- "왜" 변경했는지 설명

```
feat(score): implement OCR score recognition

Added Naver CLOVA OCR API integration for automatic score recognition from photos.
Uses EncryptedSharedPreferences to securely store API key.

- Takes photo of bowling scorecard
- Sends to Naver CLOVA OCR API
- Parses response and populates score table
- Validates score range (0-300)
```

### Footer (선택)

**Issue 연결**:
```
Closes #123
Fixes #456
Related to #789
```

### 예시

```
feat(tournament): add team tournament support

Implement team tournament feature allowing multiple members to compete as teams.

- Create Team and TeamMember entities
- Add team creation and management UI
- Calculate team scores (sum of member games)
- Display team rankings

Closes #42
```

---

## 10. 그 외 규칙

### 마이그레이션 관리

새로운 Entity 추가 또는 스키마 변경 시:

1. `@Database(version = X)`에서 버전 증가
2. `Migration` 클래스 작성
3. 테스트 추가 (`MigrationTest.kt`)

```kotlin
val migration1To2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS new_table (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL
            )
        """)
    }
}

Room.databaseBuilder(context, BowlingClubDatabase::class.java, "db.sqlite")
    .addMigrations(migration1To2)
    .build()
```

### 성능 최적화

- **쿼리**: Index 추가 (`@Index`)
- **DB 접근**: 메인 스레드 금지 (Coroutine 사용)
- **리스트 렌더링**: LazyColumn/LazyRow 사용
- **이미지**: Coil로 로딩 (메모리 캐싱)

### 에러 처리

```kotlin
// Repository
suspend fun loadMembers(): Result<List<Member>> {
    return try {
        val members = memberDao.getAllMembers().first()
        Result.success(members)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ViewModel
viewModelScope.launch {
    repository.loadMembers()
        .onSuccess { members -> _state.value = members }
        .onFailure { error -> _error.value = error.message }
}
```

### 상태 관리 패턴

```kotlin
// ViewModel
data class MemberUiState(
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MemberViewModel @Inject constructor(
    private val repository: MemberRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemberUiState())
    val uiState: StateFlow<MemberUiState> = _uiState.asStateFlow()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getMembers()
                .onSuccess { members ->
                    _uiState.update { it.copy(members = members, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
}
```

---

## 11. AI 개발자 체크리스트

AI 어시스턴트가 작업 완료 전에 확인하는 항목:

- [ ] 모든 새로운 클래스가 패키지 구조를 따르는가?
- [ ] ViewModel은 `@HiltViewModel`과 `@Inject` 사용?
- [ ] Repository는 인터페이스로 정의?
- [ ] DB 쿼리는 `Flow<T>` 반환?
- [ ] Composable은 `@Preview` 작성?
- [ ] 네트워크 호출은 OCR API만?
- [ ] API 키는 `EncryptedSharedPreferences`에 저장?
- [ ] 모든 테스트가 통과?
- [ ] 린트 오류 없음?
- [ ] 커밋 메시지가 규칙을 따르는가?
- [ ] 외부 서버와 통신하는 코드 없음?

---

**최종 수정**: 2026년 2월 9일
**버전**: 1.0
