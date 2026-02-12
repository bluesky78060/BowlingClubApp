# 볼링클럽 관리 앱 - 기술 아키텍처 문서

**버전**: 1.0
**최종 수정**: 2026-02-09
**플랫폼**: Android (Kotlin, Jetpack Compose)

---

## 목차

1. [시스템 아키텍처](#1-시스템-아키텍처)
2. [데이터베이스 설계](#2-데이터베이스-설계)
3. [OCR 처리 파이프라인](#3-ocr-처리-파이프라인)
4. [순위 산출 알고리즘](#4-순위-산출-알고리즘)
5. [이미지 생성 시스템](#5-이미지-생성-시스템)
6. [백업/복원 시스템](#6-백업복원-시스템)
7. [네비게이션 구조](#7-네비게이션-구조)
8. [의존성 그래프](#8-의존성-그래프)

---

## 1. 시스템 아키텍처

### 1.1 MVVM 패턴 개요

볼링클럽 관리 앱은 **MVVM (Model-View-ViewModel)** 패턴을 기반으로 설계되었습니다. 이 패턴은 관심사의 분리(Separation of Concerns)를 통해 코드의 유지보수성과 테스트 가능성을 극대화합니다.

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Presentation)               │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Composable Screens & Components                │   │
│  │  (HomeScreen, TournamentScreen, etc.)           │   │
│  └──────────────────────────────────────────────────┘   │
│                          ▲                               │
│                          │ (Observe State)               │
│                          │ (Emit Events)                 │
└──────────────────────────┼───────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────┐
│              ViewModel Layer (Presentation Logic)         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  TournamentViewModel                             │   │
│  │  - 토너먼트 상태 관리                            │   │
│  │  - UI 이벤트 처리                                │   │
│  │  - CoroutineScope 관리                          │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  MemberViewModel                                 │   │
│  │  - 멤버 CRUD 관리                                │   │
│  │  - OCR 처리 조율                                 │   │
│  │  - 프로필 이미지 관리                            │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  GameScoreViewModel                              │   │
│  │  - 점수 입력 및 검증                             │   │
│  │  - 순위 계산 조율                                │   │
│  └──────────────────────────────────────────────────┘   │
│                          ▲                               │
│                          │ (Call Use Cases)              │
│                          │ (Collect Flows)               │
└──────────────────────────┼───────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────┐
│            Repository Layer (Business Logic)             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  TournamentRepository                            │   │
│  │  - 토너먼트 CRUD                                 │   │
│  │  - 데이터 변환 (Entity ↔ Domain Model)          │   │
│  │  - 비즈니스 로직 구현                            │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  MemberRepository                                │   │
│  │  - 멤버 CRUD                                     │   │
│  │  - 멤버 검색 및 필터링                           │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  GameScoreRepository                             │   │
│  │  - 점수 입출력                                   │   │
│  │  - OCR 결과 저장                                 │   │
│  │  - 순위 계산                                     │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  SettingsRepository                              │   │
│  │  - 앱 설정 조회/저장                             │   │
│  └──────────────────────────────────────────────────┘   │
│                          ▲                               │
│                          │ (CRUD Operations)             │
│                          │ (Data Queries)                │
└──────────────────────────┼───────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────┐
│           Data Access Layer (DAO & Entities)             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Room DAOs                                       │   │
│  │  ├── MemberDao                                   │   │
│  │  ├── TournamentDao                               │   │
│  │  ├── TournamentParticipantDao                    │   │
│  │  ├── GameScoreDao                                │   │
│  │  ├── TeamDao                                     │   │
│  │  └── SettingsDao                                 │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  Database Entities (Room)                        │   │
│  │  └── MemberEntity, TournamentEntity, etc.        │   │
│  └──────────────────────────────────────────────────┘   │
│                          ▲                               │
│                          │ (Query Execution)             │
│                          │ (Transaction Control)         │
└──────────────────────────┼───────────────────────────────┘
                           │
┌──────────────────────────┼───────────────────────────────┐
│        Database Layer & External Services                │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Room Database (SQLite)                          │   │
│  │  └── members, tournaments, scores, etc.          │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  Naver CLOVA OCR API (OCR)                       │   │
│  │  └── Score sheet recognition                     │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  File System (Images, Backups)                   │   │
│  │  └── Context.filesDir, External Storage         │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

### 1.2 의존성 흐름 (Unidirectional)

모든 의존성은 **한 방향으로만** 흐릅니다:

```
UI Layer
   ↓ (의존)
ViewModel Layer
   ↓ (의존)
Repository Layer
   ↓ (의존)
DAO + Entity Layer
   ↓ (의존)
Database & External Services
```

**핵심 원칙**:
- UI는 ViewModel에만 의존
- ViewModel은 Repository에만 의존
- Repository는 DAO와 외부 서비스에만 의존
- 역방향 의존성은 **금지** (이벤트 또는 콜백 사용)

### 1.3 레이어 책임 정의

#### UI Layer (Presentation)
- Jetpack Compose를 사용한 선언형 UI 구성
- ViewModel에서 발출하는 State 구독
- 사용자 입력을 ViewModel에 이벤트로 전달
- 부작용(Side Effects) 처리: 스낵바, 네비게이션 등

**핵심 파일**:
```
app/src/main/java/com/bowlingclub/ui/
├── screens/
│   ├── HomeScreen.kt
│   ├── TournamentScreen.kt
│   ├── MemberScreen.kt
│   ├── StatisticsScreen.kt
│   └── SettingsScreen.kt
├── components/
│   ├── MemberCard.kt
│   ├── ScoreInput.kt
│   ├── RankingTable.kt
│   └── ...
└── theme/
    └── Theme.kt
```

#### ViewModel Layer (Presentation Logic)
- UI State를 `StateFlow`로 관리
- 사용자 이벤트 처리 (`onEvent(event: UiEvent)`)
- Repository 호출 및 비동기 작업 조율
- 네비게이션 이벤트 발출

**핵심 구조**:
```kotlin
abstract class BaseViewModel : ViewModel() {
    protected fun launchIO(
        onError: (String) -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                onError(e.message ?: "알 수 없는 오류")
            }
        }
    }
}

class TournamentViewModel(
    private val repository: TournamentRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow<TournamentUiState>(
        TournamentUiState.Loading
    )
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: TournamentUiEvent) {
        when (event) {
            is TournamentUiEvent.CreateTournament -> createTournament(event)
            is TournamentUiEvent.DeleteTournament -> deleteTournament(event)
            // ...
        }
    }
}
```

#### Repository Layer (Business Logic)
- 데이터 소스 추상화 (DB, API, 파일시스템)
- 도메인 로직 구현 (순위 계산, 핸디캡 적용 등)
- 엔티티 ↔ 도메인 모델 변환
- 트랜잭션 관리

**핵심 책임**:
```kotlin
interface GameScoreRepository {
    suspend fun saveGameScore(
        tournamentId: Long,
        memberId: Long,
        gameNumber: Int,
        rawScore: Int,
        handicap: Int,
        inputMethod: String // "ocr" or "manual"
    )

    suspend fun calculateRankings(
        tournamentId: Long,
        handicapPerGame: Int,
        useTeamMatch: Boolean
    ): List<RankingResult>

    suspend fun getTournamentScores(
        tournamentId: Long
    ): List<GameScore>
}
```

#### DAO Layer (Data Access)
- Room DAOs를 통한 데이터베이스 접근
- SQL 쿼리 정의
- 트랜잭션 처리

**핵심 특징**:
- `@Dao` 인터페이스로 정의
- `suspend` 함수를 통한 비동기 지원
- `Flow<T>`를 통한 실시간 관찰(Observation)

---

## 2. 데이터베이스 설계

### 2.1 Room 데이터베이스 구성

```kotlin
@Database(
    entities = [
        MemberEntity::class,
        TournamentEntity::class,
        TournamentParticipantEntity::class,
        GameScoreEntity::class,
        TeamEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BowlingClubDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun tournamentParticipantDao(): TournamentParticipantDao
    abstract fun gameScoreDao(): GameScoreDao
    abstract fun teamDao(): TeamDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        private var instance: BowlingClubDatabase? = null

        fun getInstance(context: Context): BowlingClubDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    BowlingClubDatabase::class.java,
                    "bowling_club_db"
                )
                    .addMigrations(/* migrations */)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
```

### 2.2 테이블 스키마

#### 1) members 테이블
**설명**: 클럽 멤버 정보 저장

```sql
CREATE TABLE members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    nickname TEXT NOT NULL UNIQUE,
    gender TEXT NOT NULL CHECK (gender IN ('M', 'F')),
    phone TEXT,
    profile_image_path TEXT,
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive')),
    join_date TEXT NOT NULL,
    memo TEXT,
    created_at TEXT NOT NULL
);

-- 인덱스
CREATE INDEX idx_members_status ON members(status);
CREATE INDEX idx_members_gender ON members(gender);
CREATE UNIQUE INDEX idx_members_nickname ON members(nickname);
```

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "members",
    indices = [
        Index("status"),
        Index("gender"),
        Index("nickname", unique = true)
    ]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "nickname")
    val nickname: String,

    @ColumnInfo(name = "gender")
    val gender: String, // "M" 또는 "F"

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "profile_image_path")
    val profileImagePath: String? = null,

    @ColumnInfo(name = "status")
    val status: String = "active", // "active" 또는 "inactive"

    @ColumnInfo(name = "join_date")
    val joinDate: String, // ISO 8601 format

    @ColumnInfo(name = "memo")
    val memo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String // ISO 8601 format
)
```

#### 2) tournaments 테이블
**설명**: 토너먼트 정보 저장

```sql
CREATE TABLE tournaments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    date TEXT NOT NULL,
    location TEXT,
    game_count INTEGER NOT NULL CHECK (game_count IN (3, 4)),
    has_team_match INTEGER NOT NULL DEFAULT 0,
    handicap_enabled INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled', 'completed')),
    created_at TEXT NOT NULL
);

-- 인덱스
CREATE INDEX idx_tournaments_status ON tournaments(status);
CREATE INDEX idx_tournaments_date ON tournaments(date);
```

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "tournaments",
    indices = [
        Index("status"),
        Index("date")
    ]
)
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "date")
    val date: String, // ISO 8601 format

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "game_count")
    val gameCount: Int, // 3 또는 4

    @ColumnInfo(name = "has_team_match")
    val hasTeamMatch: Boolean = false,

    @ColumnInfo(name = "handicap_enabled")
    val handicapEnabled: Boolean = false,

    @ColumnInfo(name = "status")
    val status: String = "scheduled", // "scheduled" 또는 "completed"

    @ColumnInfo(name = "created_at")
    val createdAt: String // ISO 8601 format
)
```

#### 3) tournament_participants 테이블
**설명**: 토너먼트 참가자 매핑 (N:M 관계)

```sql
CREATE TABLE tournament_participants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tournament_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    team_id INTEGER,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE RESTRICT,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    UNIQUE (tournament_id, member_id)
);

-- 인덱스
CREATE INDEX idx_tp_tournament ON tournament_participants(tournament_id);
CREATE INDEX idx_tp_member ON tournament_participants(member_id);
CREATE INDEX idx_tp_team ON tournament_participants(team_id);
```

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "tournament_participants",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournament_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["team_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("tournament_id"),
        Index("member_id"),
        Index("team_id"),
        Index("tournament_id", "member_id", unique = true)
    ]
)
data class TournamentParticipantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tournament_id")
    val tournamentId: Long,

    @ColumnInfo(name = "member_id")
    val memberId: Long,

    @ColumnInfo(name = "team_id")
    val teamId: Long? = null
)
```

#### 4) game_scores 테이블
**설명**: 각 게임별 점수 저장

```sql
CREATE TABLE game_scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tournament_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    game_number INTEGER NOT NULL CHECK (game_number BETWEEN 1 AND 4),
    raw_score INTEGER NOT NULL CHECK (raw_score BETWEEN 0 AND 300),
    handicap INTEGER NOT NULL DEFAULT 0,
    final_score INTEGER NOT NULL,
    input_method TEXT NOT NULL CHECK (input_method IN ('ocr', 'manual')),
    ocr_image_path TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    UNIQUE (tournament_id, member_id, game_number)
);

-- 인덱스
CREATE INDEX idx_gs_tournament ON game_scores(tournament_id);
CREATE INDEX idx_gs_member ON game_scores(member_id);
CREATE INDEX idx_gs_game_number ON game_scores(game_number);
```

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "game_scores",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournament_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("tournament_id"),
        Index("member_id"),
        Index("game_number"),
        Index("tournament_id", "member_id", "game_number", unique = true)
    ]
)
data class GameScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tournament_id")
    val tournamentId: Long,

    @ColumnInfo(name = "member_id")
    val memberId: Long,

    @ColumnInfo(name = "game_number")
    val gameNumber: Int, // 1-4

    @ColumnInfo(name = "raw_score")
    val rawScore: Int, // 0-300

    @ColumnInfo(name = "handicap")
    val handicap: Int = 0,

    @ColumnInfo(name = "final_score")
    val finalScore: Int, // rawScore + handicap

    @ColumnInfo(name = "input_method")
    val inputMethod: String, // "ocr" 또는 "manual"

    @ColumnInfo(name = "ocr_image_path")
    val ocrImagePath: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String // ISO 8601 format
)
```

#### 5) teams 테이블
**설명**: 팀 정보 저장 (팀 경기용)

```sql
CREATE TABLE teams (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tournament_id INTEGER NOT NULL,
    team_name TEXT NOT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    UNIQUE (tournament_id, team_name)
);

-- 인덱스
CREATE INDEX idx_teams_tournament ON teams(tournament_id);
```

**Kotlin Entity**:
```kotlin
@Entity(
    tableName = "teams",
    foreignKeys = [
        ForeignKey(
            entity = TournamentEntity::class,
            parentColumns = ["id"],
            childColumns = ["tournament_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("tournament_id"),
        Index("tournament_id", "team_name", unique = true)
    ]
)
data class TeamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tournament_id")
    val tournamentId: Long,

    @ColumnInfo(name = "team_name")
    val teamName: String
)
```

#### 6) settings 테이블
**설명**: 앱 설정 저장 (Key-Value 쌍)

```sql
CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);
```

**Kotlin Entity**:
```kotlin
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "value")
    val value: String
)
```

**저장되는 설정값들**:
```
설정키                         설명                      예시값
─────────────────────────────────────────────────────────────
club_name                    클럽 이름                 "서울 볼링클럽"
handicap_per_game            게임당 여성 핸디캡        "10"
handicap_per_game_male       게임당 남성 핸디캡 (선택) "0"
admin_pin                    관리자 PIN                "1234"
last_backup_date             마지막 백업 날짜          "2026-02-09T10:30:00Z"
backup_frequency             백업 빈도                 "auto"
image_output_quality         이미지 출력 품질          "high"
```

### 2.3 ER 다이어그램 (Entity-Relationship)

```
┌─────────────────────────┐
│        members          │
├─────────────────────────┤
│ id (PK)                 │
│ name                    │
│ nickname (UNIQUE)       │
│ gender (M/F)            │
│ phone                   │
│ profile_image_path      │
│ status (active/inactive)│
│ join_date               │
│ memo                    │
│ created_at              │
└──────────────┬──────────┘
               │ (1:N)
               │
    ┌──────────┴──────────┐
    │                     │
┌───┴──────────────┐  ┌──┴─────────────────────────────┐
│ tournament_      │  │     game_scores                │
│ participants     │  ├─────────────────────────────────┤
├──────────────────┤  │ id (PK)                         │
│ id (PK)          │  │ tournament_id (FK)              │
│ tournament_id(FK)│  │ member_id (FK)                  │
│ member_id (FK)   │  │ game_number (1-4)               │
│ team_id (FK)     │  │ raw_score (0-300)               │
└──────────────────┘  │ handicap                        │
       │              │ final_score                     │
       │              │ input_method (ocr/manual)       │
       │              │ ocr_image_path                  │
       └──────┬───────┤ created_at                      │
              │       └─────────────────────────────────┘
              │             │ (N:1)
              │             │
              │  (N:1)      │
              │  (from team)│
              │             │
┌─────────────┴─────────────┴──────────┐
│        tournaments                   │
├──────────────────────────────────────┤
│ id (PK)                              │
│ title                                │
│ date                                 │
│ location                             │
│ game_count (3/4)                     │
│ has_team_match (0/1)                 │
│ handicap_enabled (0/1)               │
│ status (scheduled/completed)         │
│ created_at                           │
└──────┬───────────────────┬───────────┘
       │                   │
       │ (1:N)             │ (1:N)
       │                   │
       │          ┌────────┴──────────┐
       │          │                   │
       │     ┌────┴─────────┐    ┌────┴────────────┐
       │     │   teams      │    │    settings     │
       │     ├──────────────┤    ├─────────────────┤
       │     │ id (PK)      │    │ key (PK)        │
       │     │ tournament_id│    │ value           │
       │     │ team_name    │    └─────────────────┘
       │     └──────────────┘
       │
       └──────────────────────────────────────

관계 설명:
─────────────────────────────────────────────
1. members (1) ─── (N) tournament_participants
   한 멤버는 여러 토너먼트에 참가 가능

2. members (1) ─── (N) game_scores
   한 멤버는 여러 게임 점수 보유

3. tournaments (1) ─── (N) tournament_participants
   한 토너먼트는 여러 참가자 보유

4. tournaments (1) ─── (N) game_scores
   한 토너먼트는 여러 점수 기록

5. tournaments (1) ─── (N) teams
   한 토너먼트는 여러 팀 보유 (팀 경기용)

6. teams (1) ─── (N) tournament_participants
   한 팀은 여러 참가자 포함

Foreign Key 무결성:
─────────────────────────────────────────────
- tournament_participants의 tournament_id 삭제 시 CASCADE
- game_scores의 tournament_id 삭제 시 CASCADE
- game_scores의 member_id 삭제 시 CASCADE
- tournament_participants의 member_id 삭제 시 RESTRICT
- tournament_participants의 team_id 삭제 시 SET NULL
- teams의 tournament_id 삭제 시 CASCADE
```

### 2.4 DAO 인터페이스

#### MemberDao
```kotlin
@Dao
interface MemberDao {
    @Insert
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Delete
    suspend fun delete(member: MemberEntity)

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Long): MemberEntity?

    @Query("SELECT * FROM members WHERE status = 'active' ORDER BY name ASC")
    fun getAllActiveMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE nickname LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    fun searchMembers(query: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE gender = :gender")
    fun getMembersByGender(gender: String): Flow<List<MemberEntity>>

    @Query("SELECT COUNT(*) FROM members WHERE status = 'active'")
    fun getActiveMemberCount(): Flow<Int>
}
```

#### TournamentDao
```kotlin
@Dao
interface TournamentDao {
    @Insert
    suspend fun insert(tournament: TournamentEntity): Long

    @Update
    suspend fun update(tournament: TournamentEntity)

    @Delete
    suspend fun delete(tournament: TournamentEntity)

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getTournamentById(id: Long): TournamentEntity?

    @Query("SELECT * FROM tournaments ORDER BY date DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE status = 'scheduled' ORDER BY date ASC")
    fun getScheduledTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE status = 'completed' ORDER BY date DESC")
    fun getCompletedTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE date >= :startDate AND date <= :endDate")
    fun getTournamentsByDateRange(startDate: String, endDate: String): Flow<List<TournamentEntity>>
}
```

#### GameScoreDao
```kotlin
@Dao
interface GameScoreDao {
    @Insert
    suspend fun insert(score: GameScoreEntity): Long

    @Update
    suspend fun update(score: GameScoreEntity)

    @Delete
    suspend fun delete(score: GameScoreEntity)

    @Query("SELECT * FROM game_scores WHERE id = :id")
    suspend fun getScoreById(id: Long): GameScoreEntity?

    @Query("SELECT * FROM game_scores WHERE tournament_id = :tournamentId ORDER BY member_id, game_number")
    suspend fun getTournamentScores(tournamentId: Long): List<GameScoreEntity>

    @Query("SELECT * FROM game_scores WHERE tournament_id = :tournamentId AND member_id = :memberId ORDER BY game_number")
    suspend fun getMemberTournamentScores(tournamentId: Long, memberId: Long): List<GameScoreEntity>

    @Query("""
        SELECT SUM(final_score) as total_score, member_id
        FROM game_scores
        WHERE tournament_id = :tournamentId
        GROUP BY member_id
    """)
    suspend fun getTotalScoresByTournament(tournamentId: Long): List<TotalScoreResult>

    @Query("DELETE FROM game_scores WHERE tournament_id = :tournamentId")
    suspend fun deleteTournamentScores(tournamentId: Long)
}
```

#### TournamentParticipantDao
```kotlin
@Dao
interface TournamentParticipantDao {
    @Insert
    suspend fun insert(participant: TournamentParticipantEntity): Long

    @Update
    suspend fun update(participant: TournamentParticipantEntity)

    @Delete
    suspend fun delete(participant: TournamentParticipantEntity)

    @Query("SELECT * FROM tournament_participants WHERE tournament_id = :tournamentId")
    suspend fun getTournamentParticipants(tournamentId: Long): List<TournamentParticipantEntity>

    @Query("SELECT member_id FROM tournament_participants WHERE tournament_id = :tournamentId")
    suspend fun getParticipantMemberIds(tournamentId: Long): List<Long>

    @Query("DELETE FROM tournament_participants WHERE tournament_id = :tournamentId")
    suspend fun deleteTournamentParticipants(tournamentId: Long)
}
```

#### TeamDao
```kotlin
@Dao
interface TeamDao {
    @Insert
    suspend fun insert(team: TeamEntity): Long

    @Update
    suspend fun update(team: TeamEntity)

    @Delete
    suspend fun delete(team: TeamEntity)

    @Query("SELECT * FROM teams WHERE tournament_id = :tournamentId")
    suspend fun getTournamentTeams(tournamentId: Long): List<TeamEntity>

    @Query("DELETE FROM teams WHERE tournament_id = :tournamentId")
    suspend fun deleteTournamentTeams(tournamentId: Long)
}
```

#### SettingsDao
```kotlin
@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SettingsEntity)

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingsEntity?

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsEntity>

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
}
```

---

## 3. OCR 처리 파이프라인

### 3.1 처리 흐름

```
┌─────────────────────────────────────────────────────────────┐
│ 1. 카메라로 점수표 촬영                                      │
│    (CameraX 사용)                                            │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────┐
│ 2. 이미지 전처리                                             │
│    - 자동 회전 감지 및 교정                                  │
│    - 밝기/대비 조정                                          │
│    - 노이즈 제거                                             │
│    - 압축 (maxSize: 1920px)                                 │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────┐
│ 3. Naver CLOVA OCR API 호출                                 │
│    - Base64 인코딩된 이미지 전송                             │
│    - 구조화된 JSON 응답 요청                                │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────┐
│ 4. JSON 파싱 및 검증                                        │
│    - 응답 필드 검증                                         │
│    - 데이터 타입 변환                                       │
│    - 오류 처리                                              │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────┐
│ 5. 멤버 자동 매칭                                           │
│    - 닉네임 또는 이름으로 멤버 찾기                          │
│    - 오타 감지 (Levenshtein distance)                       │
│    - 매칭 확률 표시                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────┐
│ 6. 미리보기 & 사용자 확인                                   │
│    - 인식된 점수 표시                                       │
│    - 멤버 확인/수정 가능                                    │
│    - 의도치 않은 데이터 수정 옵션 제공                       │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────┐
│ 7. 데이터베이스 저장                                        │
│    - game_scores 테이블에 저장                              │
│    - 원본 이미지 파일 저장                                  │
│    - 메타데이터 기록                                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                  완료 ✓
```

### 3.2 Naver CLOVA OCR API 요청/응답 형식

#### 요청 구조

```kotlin
data class NaverOcrRequest(
    val version: String = "V2",
    val requestId: String, // UUID
    val timestamp: Long,
    val images: List<NaverOcrImage>
)

data class NaverOcrImage(
    val format: String, // "jpg", "png", etc.
    val name: String,
    val data: String // Base64 encoded image
)
```

#### Retrofit API 정의

```kotlin
interface NaverOcrApiService {
    @POST("custom/v1/YOUR_DOMAIN/YOUR_INVOKE_URL")
    suspend fun recognizeScores(
        @Header("X-OCR-SECRET") secretKey: String,
        @Body request: NaverOcrRequest
    ): NaverOcrResponse
}

data class NaverOcrResponse(
    val version: String,
    val requestId: String,
    val timestamp: Long,
    val images: List<NaverOcrImageResult>
)

data class NaverOcrImageResult(
    val uid: String,
    val name: String,
    val inferResult: String,
    val message: String,
    val validationResult: ValidationResult?,
    val fields: List<NaverOcrField>
)

data class NaverOcrField(
    val valueType: String,
    val boundingPoly: BoundingPoly,
    val inferText: String,
    val inferConfidence: Double
)

data class BoundingPoly(
    val vertices: List<Vertex>
)

data class Vertex(
    val x: Double,
    val y: Double
)

data class ValidationResult(
    val result: String
)
```

#### API 사용 참고사항

Naver CLOVA OCR API는 Custom API로 구성되어 있으며, 템플릿 기반으로 볼링 점수표 인식을 위해 사전 학습된 모델을 사용합니다.

- API 엔드포인트: `https://apigw.ntruss.com/custom/v1/{domain}/{invoke-url}`
- 요청 헤더: `X-OCR-SECRET` (Naver Cloud Platform에서 발급받은 Secret Key)
- 요청 본문: JSON 형식으로 이미지 정보 전송
- 응답: 인식된 텍스트 필드와 신뢰도 정보 포함

#### 실제 사용 예시

```kotlin
class OcrRepository(private val apiService: NaverOcrApiService) {
    suspend fun recognizeScores(
        imageFile: File,
        secretKey: String
    ): Result<ScoreRecognitionResult> = withContext(Dispatchers.IO) {
        try {
            // 1. 이미지 전처리
            val processedImage = preprocessImage(imageFile)

            // 2. Base64 인코딩
            val base64Image = processedImage.readBytes().toBase64()

            // 3. API 요청 구성
            val request = NaverOcrRequest(
                version = "V2",
                requestId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                images = listOf(
                    NaverOcrImage(
                        format = "jpg",
                        name = "bowling_score",
                        data = base64Image
                    )
                )
            )

            // 4. API 호출
            val response = apiService.recognizeScores(
                secretKey = secretKey,
                request = request
            )

            // 5. 응답 파싱
            val recognizedFields = response.images.firstOrNull()?.fields
                ?: throw Exception("No OCR fields found")

            val scoreResult = parseOcrFields(recognizedFields)

            // 6. 결과 반환
            Result.success(scoreResult)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseOcrFields(fields: List<NaverOcrField>): ScoreRecognitionResult {
        // OCR 필드에서 점수 정보 추출
        val scores = mutableListOf<Int>()
        val memberNames = mutableListOf<String>()

        fields.forEach { field ->
            // 숫자 패턴 검출 (0-300 범위의 볼링 점수)
            val scorePattern = """(\d{1,3})""".toRegex()
            val matchResult = scorePattern.find(field.inferText)

            matchResult?.let {
                val score = it.value.toIntOrNull()
                if (score != null && score in 0..300) {
                    scores.add(score)
                }
            }

            // 이름 패턴 검출 (한글 또는 영문)
            if (field.inferText.matches("""[가-힣a-zA-Z]+""".toRegex())) {
                memberNames.add(field.inferText)
            }
        }

        return ScoreRecognitionResult(
            recognized = scores.isNotEmpty(),
            confidence = fields.firstOrNull()?.inferConfidence ?: 0.0,
            members = listOf(
                RecognizedMember(
                    name = memberNames.firstOrNull(),
                    nickname = null,
                    scores = scores,
                    total = scores.sum()
                )
            ),
            errors = if (scores.isEmpty()) listOf("점수를 인식하지 못했습니다.") else emptyList()
        )
    }
}
```

### 3.3 스코어 인식 결과 데이터 클래스

```kotlin
data class ScoreRecognitionResult(
    val recognized: Boolean,
    val confidence: Double, // 0.0 ~ 1.0
    val members: List<RecognizedMember>,
    val errors: List<String>
)

data class RecognizedMember(
    val name: String?,
    val nickname: String?,
    val scores: List<Int>, // 각 게임 점수
    val total: Int
)
```

### 3.4 오류 처리 전략

| 오류 타입 | 원인 | 처리 방법 |
|---------|------|---------|
| **인식 실패** | 이미지 품질 낮음 | 재촬영 안내, 수동 입력 옵션 제공 |
| **API 오류** | 네트워크 문제 | 재시도 로직 (최대 3회), 오프라인 모드 |
| **JSON 파싱 오류** | 응답 형식 오류 | 원본 응답 로깅, 개발자 알림 |
| **멤버 매칭 실패** | 오타/미등록 멤버 | 사용자에게 멤버 선택 요청 |
| **점수 범위 오류** | 인식된 점수 > 300 | 오류 표시, 수동 수정 요청 |

```kotlin
enum class OcrError {
    IMAGE_QUALITY_TOO_LOW("이미지 품질이 낮습니다. 다시 촬영해주세요."),
    NO_SCORES_DETECTED("점수를 인식하지 못했습니다."),
    INVALID_SCORE_RANGE("인식된 점수가 유효하지 않습니다 (0-300)."),
    MEMBER_NOT_FOUND("멤버를 찾을 수 없습니다."),
    NETWORK_ERROR("네트워크 연결을 확인해주세요."),
    API_ERROR("API 오류가 발생했습니다."),
    JSON_PARSE_ERROR("응답 파싱에 실패했습니다.");

    val message: String
}
```

---

## 4. 순위 산출 알고리즘

### 4.1 개인 순위 산출 (Individual Ranking)

```kotlin
data class RankingResult(
    val rank: Int,
    val memberId: Long,
    val memberName: String,
    val totalScore: Int,
    val scores: List<Int>, // 각 게임별 최종 점수
    val handicap: Int,
    val lastGameScore: Int,
    val highestSingleGame: Int,
    val tiebreaker: Int
)

suspend fun calculateIndividualRankings(
    tournamentId: Long,
    handicapPerGame: Int
): List<RankingResult> {
    // 1단계: 토너먼트 참가자 조회
    val participants = tournamentParticipantDao
        .getTournamentParticipants(tournamentId)

    // 2단계: 각 멤버의 게임 점수 조회
    val memberScores = mutableMapOf<Long, MutableList<GameScoreEntity>>()
    participants.forEach { participant ->
        val scores = gameScoreDao.getMemberTournamentScores(
            tournamentId,
            participant.memberId
        )
        memberScores[participant.memberId] = scores.toMutableList()
    }

    // 3단계: 각 멤버별 최종 점수 계산
    val rankings = memberScores.map { (memberId, scores) ->
        val member = memberDao.getMemberById(memberId)!!

        // 여성(F) 멤버에게 게임당 handicap_per_game 포인트 추가
        val handicap = if (member.gender == "F") {
            handicapPerGame * scores.size
        } else {
            0
        }

        val finalScores = scores.map { it.finalScore }
        val totalScore = finalScores.sum() + handicap

        RankingResult(
            rank = 0, // 임시, 정렬 후 재설정
            memberId = memberId,
            memberName = member.name,
            totalScore = totalScore,
            scores = finalScores,
            handicap = handicap,
            lastGameScore = finalScores.lastOrNull() ?: 0,
            highestSingleGame = finalScores.maxOrNull() ?: 0,
            tiebreaker = 0 // 임시
        )
    }

    // 4단계: 정렬 규칙 적용
    val sorted = rankings
        .sortedWith(compareBy(
            // 1순위: 총점 (내림차순)
            { -it.totalScore },
            // 2순위: 마지막 게임 점수 (내림차순)
            { -it.lastGameScore },
            // 3순위: 최고 단일 게임 점수 (내림차순)
            { -it.highestSingleGame },
            // 4순위: 멤버 이름 (오름차순, 동점 처리)
            { it.memberName }
        ))

    // 5단계: 순위 재설정 (동점 처리)
    var currentRank = 1
    var previousScore = -1
    val finalRankings = mutableListOf<RankingResult>()

    sorted.forEachIndexed { index, result ->
        if (result.totalScore != previousScore) {
            currentRank = index + 1
            previousScore = result.totalScore
        }
        finalRankings.add(result.copy(rank = currentRank))
    }

    return finalRankings
}
```

#### 정렬 규칙 상세

```
1. 총점 (Total Score) - 내림차순
   ├─ 계산식: Σ(각 게임 점수) + 핸디캡
   ├─ 예시: [게임1: 150, 게임2: 160, 게임3: 140] → 총점 450

2. 마지막 게임 점수 (Last Game Score) - 내림차순
   ├─ 가장 최근 게임의 점수로 판별
   ├─ 3게임 토너먼트: 게임3 점수
   ├─ 4게임 토너먼트: 게임4 점수

3. 최고 단일 게임 점수 (Highest Single Game) - 내림차순
   ├─ 모든 게임 중 가장 높은 점수
   ├─ 예시: [150, 160, 140] → 최고 160

4. 멤버 이름 (Name) - 오름차순
   └─ 모든 동점 처리 후 마지막 보조 기준
```

### 4.2 팀 순위 산출 (Team Ranking)

```kotlin
data class TeamRankingResult(
    val rank: Int,
    val teamId: Long,
    val teamName: String,
    val totalScore: Int,
    val memberScores: List<MemberTeamScore>,
    val memberCount: Int
)

data class MemberTeamScore(
    val memberId: Long,
    val memberName: String,
    val individualTotal: Int
)

suspend fun calculateTeamRankings(
    tournamentId: Long,
    handicapPerGame: Int
): List<TeamRankingResult> {
    // 1단계: 토너먼트의 모든 팀 조회
    val teams = teamDao.getTournamentTeams(tournamentId)

    // 2단계: 각 팀별 점수 계산
    val teamRankings = teams.map { team ->
        // 팀의 멤버들 조회
        val teamMembers = tournamentParticipantDao
            .getTournamentParticipants(tournamentId)
            .filter { it.teamId == team.id }

        // 팀 멤버들의 개인 순위 계산
        val individualRankings = calculateIndividualRankings(
            tournamentId,
            handicapPerGame
        )

        // 팀 멤버들의 점수만 필터링
        val memberScores = teamMembers.mapNotNull { participant ->
            individualRankings.find { it.memberId == participant.memberId }?.let {
                MemberTeamScore(
                    memberId = it.memberId,
                    memberName = it.memberName,
                    individualTotal = it.totalScore
                )
            }
        }

        // 팀 총점 계산
        val teamTotalScore = memberScores.sumOf { it.individualTotal }

        TeamRankingResult(
            rank = 0, // 임시
            teamId = team.id,
            teamName = team.teamName,
            totalScore = teamTotalScore,
            memberScores = memberScores,
            memberCount = memberScores.size
        )
    }

    // 3단계: 팀 총점 기준 정렬
    val sorted = teamRankings
        .sortedByDescending { it.totalScore }
        .mapIndexed { index, result ->
            result.copy(rank = index + 1)
        }

    return sorted
}
```

### 4.3 핸디캡 계산

```kotlin
object HandicapCalculator {
    /**
     * 여성 멤버의 핸디캡 계산
     *
     * 규칙:
     * - 여성 멤버: 게임당 handicapPerGame 포인트 추가
     * - 남성 멤버: 0 포인트 (또는 별도 설정값)
     *
     * @param member 멤버 정보
     * @param gameCount 토너먼트 게임 수
     * @param handicapPerGame 게임당 핸디캡 포인트
     * @return 총 핸디캡 포인트
     */
    fun calculateHandicap(
        member: MemberEntity,
        gameCount: Int,
        handicapPerGame: Int
    ): Int {
        return if (member.gender == "F") {
            handicapPerGame * gameCount
        } else {
            0
        }
    }

    /**
     * 최종 점수 계산 (원점 + 핸디캡)
     */
    fun calculateFinalScore(
        rawScore: Int,
        handicap: Int
    ): Int = rawScore + handicap

    /**
     * 토너먼트 총점 계산 (모든 게임 포함)
     */
    fun calculateTotalScore(
        gameScores: List<Int>,
        totalHandicap: Int
    ): Int = gameScores.sum() + totalHandicap
}
```

### 4.4 동점 처리 (Tiebreaker)

```
시나리오 1: 총점이 같은 경우
───────────────────────────────
멤버 A: [150, 160, 140] = 450 (마지막 게임: 140)
멤버 B: [145, 155, 150] = 450 (마지막 게임: 150)

→ 결과: 멤버 B가 1위 (마지막 게임 150 > 140)

시나리오 2: 총점과 마지막 게임이 같은 경우
───────────────────────────────────────────
멤버 A: [160, 140, 150] = 450 (최고: 160)
멤버 B: [150, 150, 150] = 450 (최고: 150)

→ 결과: 멤버 A가 1위 (최고 점수 160 > 150)

시나리오 3: 모든 점수가 같은 경우
───────────────────────────────
멤버 A: [150, 150, 150] = 450
멤버 B: [150, 150, 150] = 450

→ 결과: 1위 동점 (동일 순위 부여, 이름 순서)
```

---

## 5. 이미지 생성 시스템

### 5.1 순위표 이미지 생성

순위표를 비트맵 이미지로 변환하여 공유 기능을 지원합니다.

```kotlin
class RankingImageGenerator(
    private val context: Context,
    private val clubName: String,
    private val tournamentDate: String
) {
    companion object {
        const val IMAGE_WIDTH = 1080
        const val IMAGE_HEIGHT = 1920
        const val COLOR_HEADER = 0xFF2196F3.toInt()
        const val COLOR_BACKGROUND = 0xFFFFFFFF.toInt()
        const val COLOR_TEXT = 0xFF212121.toInt()
        const val COLOR_SUBTITLE = 0xFF757575.toInt()
        const val COLOR_RANK_ROW = 0xFFF5F5F5.toInt()
    }

    fun generateRankingImage(
        rankings: List<RankingResult>,
        tournamentTitle: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        // 배경색 설정
        canvas.drawColor(COLOR_BACKGROUND)

        // 헤더 드로우
        drawHeader(canvas, tournamentTitle)

        // 순위표 드로우
        drawRankingTable(canvas, rankings)

        // 푸터 드로우
        drawFooter(canvas)

        return bitmap
    }

    private fun drawHeader(
        canvas: Canvas,
        tournamentTitle: String
    ) {
        val paint = Paint().apply {
            color = COLOR_HEADER
            style = Paint.Style.FILL
        }

        // 헤더 배경
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), 200f, paint)

        // 클럽명
        val clubPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            clubName,
            IMAGE_WIDTH / 2f,
            50f,
            clubPaint
        )

        // 토너먼트명
        val titlePaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            tournamentTitle,
            IMAGE_WIDTH / 2f,
            100f,
            titlePaint
        )

        // 날짜
        val datePaint = Paint().apply {
            color = 0xFFBBBBBB.toInt()
            textSize = 16f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "대회일: $tournamentDate",
            IMAGE_WIDTH / 2f,
            140f,
            datePaint
        )
    }

    private fun drawRankingTable(
        canvas: Canvas,
        rankings: List<RankingResult>
    ) {
        var yPosition = 250f
        val rowHeight = 80f
        val padding = 20f

        // 테이블 헤더
        drawTableHeader(canvas, yPosition)
        yPosition += rowHeight

        // 순위 행
        rankings.take(20).forEachIndexed { index, ranking ->
            val rowColor = if (index % 2 == 0) {
                COLOR_BACKGROUND
            } else {
                COLOR_RANK_ROW
            }

            val rowPaint = Paint().apply {
                color = rowColor
                style = Paint.Style.FILL
            }

            canvas.drawRect(
                0f,
                yPosition,
                IMAGE_WIDTH.toFloat(),
                yPosition + rowHeight,
                rowPaint
            )

            drawRankingRow(
                canvas,
                yPosition,
                ranking,
                padding
            )

            yPosition += rowHeight
        }
    }

    private fun drawTableHeader(
        canvas: Canvas,
        yPosition: Float
    ) {
        val headerPaint = Paint().apply {
            color = COLOR_HEADER
            style = Paint.Style.FILL
        }

        canvas.drawRect(
            0f,
            yPosition,
            IMAGE_WIDTH.toFloat(),
            yPosition + 60f,
            headerPaint
        )

        val textPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        // 순위
        canvas.drawText("순위", 80f, yPosition + 35f, textPaint)
        // 이름
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("이름", 150f, yPosition + 35f, textPaint)
        // 점수
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("점수", 1000f, yPosition + 35f, textPaint)
    }

    private fun drawRankingRow(
        canvas: Canvas,
        yPosition: Float,
        ranking: RankingResult,
        padding: Float
    ) {
        val textPaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        // 순위 표시 (메달 아이콘 또는 숫자)
        val rankDisplay = when (ranking.rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "${ranking.rank}"
        }

        canvas.drawText(
            rankDisplay,
            80f,
            yPosition + 45f,
            textPaint
        )

        // 멤버명
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(
            ranking.memberName,
            150f,
            yPosition + 45f,
            textPaint
        )

        // 최종 점수
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            "${ranking.totalScore}점",
            1000f,
            yPosition + 45f,
            textPaint
        )
    }

    private fun drawFooter(canvas: Canvas) {
        val footerPaint = Paint().apply {
            color = COLOR_SUBTITLE
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(
            "생성: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
            IMAGE_WIDTH / 2f,
            (IMAGE_HEIGHT - 20).toFloat(),
            footerPaint
        )
    }
}
```

### 5.2 이미지 사양

```
이미지 사양 (Ranking Card)
─────────────────────────────────────
해상도:           1080 x 1920 px (9:16 비율, 모바일 최적)
포맷:             PNG (투명도 지원) 또는 JPEG
색상 모드:        RGB (ARGB_8888 in Android)
DPI:              96 DPI (표준)
파일 크기:        약 200-400 KB

레이아웃 구조
─────────────────────────────────────
[헤더: 200px]
  - 배경색: 파란색 (#2196F3)
  - 클럽명: 28sp, 굵게
  - 토너먼트명: 32sp, 굵게
  - 날짜: 16sp

[순위표: 1620px]
  - 테이블 헤더: 60px
  - 각 행: 80px (최대 20명)
  - 줄무늬 배경: 흰색 & 밝은 회색

[푸터: 100px]
  - 생성 시간: 14sp
```

### 5.3 공유 기능 (Android Share Intent)

```kotlin
class RankingShareManager(private val context: Context) {

    fun shareRankingImage(
        bitmap: Bitmap,
        tournamentTitle: String
    ) {
        // 임시 파일 생성
        val imageFile = saveBitmapToFile(bitmap, tournamentTitle)

        // URI 생성 (FileProvider 사용)
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        // Share Intent 생성
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "볼링 토너먼트 순위: $tournamentTitle"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "클럽 토너먼트 순위표입니다."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(
                shareIntent,
                "순위표 공유"
            )
        )
    }

    private fun saveBitmapToFile(
        bitmap: Bitmap,
        tournamentTitle: String
    ): File {
        val timestamp = System.currentTimeMillis()
        val fileName = "ranking_${timestamp}.png"
        val file = File(context.filesDir, fileName)

        file.outputStream().use { outputStream ->
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                outputStream
            )
        }

        return file
    }
}
```

#### AndroidManifest.xml 설정

```xml
<application>
    <!-- FileProvider 설정 -->
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.fileprovider"
        android:exported="false"
        tools:replace="android:authorities">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>
</application>
```

#### res/xml/file_paths.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 앱 내부 파일 시스템 공유 -->
    <files-path
        name="images"
        path="." />

    <!-- 캐시 디렉토리 공유 -->
    <cache-path
        name="cache"
        path="." />
</paths>
```

---

## 6. 백업/복원 시스템

### 6.1 Full DB Export (JSON)

```kotlin
data class BackupData(
    val version: String = "1.0",
    val exportedAt: String,
    val clubName: String,
    val data: BackupDataContent
)

data class BackupDataContent(
    val members: List<MemberEntity>,
    val tournaments: List<TournamentEntity>,
    val teams: List<TeamEntity>,
    val participants: List<TournamentParticipantEntity>,
    val scores: List<GameScoreEntity>,
    val settings: List<SettingsEntity>
)

class BackupRepository(
    private val database: BowlingClubDatabase,
    private val gson: Gson
) {
    /**
     * 전체 데이터베이스를 JSON으로 내보내기
     */
    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val members = database.memberDao().getAllMembers().first()
        val tournaments = database.tournamentDao().getAllTournaments().first()
        val teams = database.teamDao().getTournamentTeams(0) // 전체
        val participants = database.tournamentParticipantDao()
            .getTournamentParticipants(0) // 전체
        val scores = database.gameScoreDao().getTournamentScores(0) // 전체
        val settings = database.settingsDao().getAllSettings()

        val backupData = BackupData(
            exportedAt = LocalDateTime.now().toIso8601String(),
            clubName = database.settingsDao()
                .getSettingValue("club_name") ?: "미정",
            data = BackupDataContent(
                members = members,
                tournaments = tournaments,
                teams = teams,
                participants = participants,
                scores = scores,
                settings = settings
            )
        )

        gson.toJson(backupData)
    }

    /**
     * 자동 백업 (토너먼트 완료 시)
     * 최근 5개 백업만 유지
     */
    suspend fun autoBackupOnTournamentCompletion() {
        val jsonData = exportDatabaseToJson()
        val backupFile = createBackupFile(jsonData)

        // 이전 백업 정리 (최근 5개만 유지)
        cleanupOldBackups(maxBackups = 5)
    }

    private fun createBackupFile(jsonData: String): File {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "backup_$timestamp.json"
        val backupDir = File(
            context.getExternalFilesDir(null),
            "backups"
        )

        backupDir.mkdirs()

        val file = File(backupDir, fileName)
        file.writeText(jsonData)

        return file
    }

    private suspend fun cleanupOldBackups(maxBackups: Int) {
        val backupDir = File(
            context.getExternalFilesDir(null),
            "backups"
        )

        val backupFiles = backupDir.listFiles()
            ?.filter { it.name.startsWith("backup_") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        // 오래된 백업 삭제
        backupFiles.drop(maxBackups).forEach { file ->
            file.delete()
        }
    }

    /**
     * JSON 백업에서 데이터 복원
     */
    suspend fun restoreFromJsonBackup(jsonData: String) = withContext(Dispatchers.IO) {
        try {
            val backupData = gson.fromJson(jsonData, BackupData::class.java)

            database.withTransaction {
                // 데이터 삭제 (외래키 제약 때문에 순서 중요)
                database.gameScoreDao().deleteTournamentScores(0) // 전체
                database.tournamentParticipantDao()
                    .deleteTournamentParticipants(0) // 전체
                database.teamDao().deleteTournamentTeams(0) // 전체
                database.tournamentDao().delete() // 전체 (직접 삭제 불가, 각각 삭제)
                database.memberDao().deleteAll() // 전체

                // 새 데이터 삽입
                backupData.data.members.forEach {
                    database.memberDao().insert(it)
                }
                backupData.data.tournaments.forEach {
                    database.tournamentDao().insert(it)
                }
                backupData.data.teams.forEach {
                    database.teamDao().insert(it)
                }
                backupData.data.participants.forEach {
                    database.tournamentParticipantDao().insert(it)
                }
                backupData.data.scores.forEach {
                    database.gameScoreDao().insert(it)
                }
                backupData.data.settings.forEach {
                    database.settingsDao().saveSetting(it)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 6.2 CSV Export (Excel 호환)

```kotlin
class CsvExportRepository(
    private val database: BowlingClubDatabase,
    private val context: Context
) {
    /**
     * 토너먼트 순위 데이터를 CSV로 내보내기
     */
    suspend fun exportTournamentToCSV(
        tournamentId: Long,
        rankings: List<RankingResult>
    ): File = withContext(Dispatchers.IO) {
        val tournament = database.tournamentDao()
            .getTournamentById(tournamentId) ?: throw Exception("Tournament not found")

        val csvContent = buildString {
            // 헤더 정보
            appendLine("볼링클럽 순위표")
            appendLine("토너먼트,${tournament.title}")
            appendLine("날짜,${tournament.date}")
            appendLine("장소,${tournament.location ?: "미정"}")
            appendLine("게임 수,${tournament.gameCount}")
            appendLine()

            // 순위 테이블 헤더
            appendLine("순위,이름,점수,게임1,게임2,게임3${if (tournament.gameCount == 4) ",게임4" else ""}")

            // 데이터 행
            rankings.forEach { ranking ->
                val gameScoresStr = ranking.scores.joinToString(",")
                appendLine("${ranking.rank},${ranking.memberName},${ranking.totalScore},$gameScoresStr")
            }
        }

        // 파일 저장
        val timestamp = System.currentTimeMillis()
        val fileName = "ranking_${tournament.id}_$timestamp.csv"
        val csvFile = File(context.getExternalFilesDir(null), fileName)

        csvFile.writeText(csvContent, Charsets.UTF_8)

        csvFile
    }
}
```

### 6.3 백업 일정 및 정책

```kotlin
object BackupPolicy {
    // 백업 설정
    const val BACKUP_ON_TOURNAMENT_COMPLETION = true
    const val AUTO_BACKUP_INTERVAL_DAYS = 7
    const val MAX_BACKUPS_TO_KEEP = 5
    const val MAX_BACKUP_SIZE_MB = 50

    // 백업 위치
    val BACKUP_DIRECTORY = "backups"
    val BACKUP_EXTENSION = ".json"

    // 파일명 형식
    fun generateBackupFileName(): String {
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "backup_$timestamp$BACKUP_EXTENSION"
    }
}
```

---

## 7. 네비게이션 구조

### 7.1 앱 네비게이션 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                          │
│                 (Navigation Host)                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │        NavHost (Jetpack Navigation)               │  │
│  │                                                    │  │
│  │  ┌────────────────────────────────────────────┐   │  │
│  │  │           Bottom Navigation                │   │  │
│  │  │  ┌─────┬────────┬────────┬────────┬──────┐ │   │  │
│  │  │  │Home │ Tourn. │ Member │ Statistic│Menu│ │   │  │
│  │  │  └─────┴────────┴────────┴────────┴──────┘ │   │  │
│  │  │                                            │   │  │
│  │  │        Screen Graph:                       │   │  │
│  │  │        ┌────────────────────────────┐     │   │  │
│  │  │        │   Active Fragment/Screen   │     │   │  │
│  │  │        │ (동적 교체, Jetpack Nav)  │     │   │  │
│  │  │        └────────────────────────────┘     │   │  │
│  │  │                                            │   │  │
│  │  └────────────────────────────────────────────┘   │  │
│  │                                                    │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 7.2 화면 구조 및 흐름

```
             START (첫 실행)
                  │
                  ▼
        ┌─────────────────┐
        │  스플래시 화면  │
        │  (로딩, 초기화)  │
        └────────┬────────┘
                 │
            (완료)│
                 ▼
        ┌─────────────────────────────────┐
        │    메인 탭 네비게이션            │
        ├─────────────────────────────────┤
        │                                  │
    ┌───┴───┬────────┬──────┬──────┬──┐   │
    │       │        │      │      │  │   │
    ▼       ▼        ▼      ▼      ▼  ▼   │
  ┌──┐  ┌──────┐ ┌──────┐┌──────┐┌─────┐ │
  │홈│  │토너  │ │멤버  ││통계  ││설정  │ │
  │화│  │먼트  │ │관리  ││      ││      │ │
  │면│  │화면  │ │화면  ││화면  ││      │ │
  └──┘  └──────┘ └──────┘└──────┘└─────┘ │
    │       │        │      │      │      │
    │ ┌─────┼────────┼──────┼──────┤      │
    │ │     │        │      │      │      │
    └─┤─────┼────────┼──────┴──────┴─────┤
      │     │        │                    │
      │  ┌──┴───┐    │                    │
      │  │상세  │    │                    │
      │  │화면들│    │                    │
      │  └──────┘    │                    │
      │              │                    │
      └──────────────┴────────────────────┘
```

### 7.3 상세 화면 흐름

#### Home Tab (홈 탭)

```
            HomeScreen
                │
       ┌────────┼────────┐
       │        │        │
       ▼        ▼        ▼
    [최근]  [빠른]    [설정]
    [토너]  [접근]    [버튼]
    [먼트]  [버튼]    (⚙️)
       │        │        │
       │    ┌───┴────┐   │
       │    │        │   │
       ▼    ▼        ▼   ▼
   ┌─────┬────────┬────────┬───────┐
   │상세 │멤버    │토너먼트│설정   │
   │보기 │추가    │시작    │화면   │
   │     │        │        │       │
```

#### Tournament Tab (토너먼트 탭)

```
    TournamentListScreen
            │
     ┌──────┼──────┐
     │      │      │
  [전체] [진행] [완료]
     │      │      │
     └──────┼──────┘
            │
        [선택]
            │
     ┌──────┘
     │
     ▼
TournamentDetailScreen
     │
 ┌───┼───┬───┬───┐
 │   │   │   │   │
 ▼   ▼   ▼   ▼   ▼
점수 순위 팀  편집 삭제
입력 보기 관리
```

#### Member Tab (멤버 탭)

```
MemberListScreen
     │
 ┌───┴───┬───┐
 │       │   │
[검색] [필터] [+]
 │       │   │
 └───┬───┴───┘
     │
  [선택]
     │
     ▼
MemberDetailScreen
     │
 ┌───┼───┐
 │   │   │
 ▼   ▼   ▼
편집 삭제 상세
```

### 7.4 NavGraph 정의 (Jetpack Navigation)

```kotlin
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home Tab
        composable(Screen.Home.route) {
            HomeScreen(
                onTournamentClick = { tournamentId ->
                    navController.navigate(
                        "${Screen.TournamentDetail.route}/$tournamentId"
                    )
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onMemberClick = { memberId ->
                    navController.navigate(
                        "${Screen.MemberDetail.route}/$memberId"
                    )
                }
            )
        }

        // Tournament Tab
        composable(Screen.Tournament.route) {
            TournamentListScreen(
                onTournamentClick = { tournamentId ->
                    navController.navigate(
                        "${Screen.TournamentDetail.route}/$tournamentId"
                    )
                },
                onCreateClick = {
                    navController.navigate(Screen.CreateTournament.route)
                }
            )
        }

        composable(
            "${Screen.TournamentDetail.route}/{tournamentId}",
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getLong("tournamentId") ?: return@composable
            TournamentDetailScreen(
                tournamentId = tournamentId,
                onScoreInputClick = {
                    navController.navigate(
                        "${Screen.ScoreInput.route}/$tournamentId"
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Member Tab
        composable(Screen.Member.route) {
            MemberListScreen(
                onMemberClick = { memberId ->
                    navController.navigate(
                        "${Screen.MemberDetail.route}/$memberId"
                    )
                },
                onCreateClick = {
                    navController.navigate(Screen.CreateMember.route)
                }
            )
        }

        // Statistics Tab
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Tournament : Screen("tournament")
    object TournamentDetail : Screen("tournament_detail")
    object CreateTournament : Screen("create_tournament")
    object ScoreInput : Screen("score_input")
    object Member : Screen("member")
    object MemberDetail : Screen("member_detail")
    object CreateMember : Screen("create_member")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}
```

### 7.5 Bottom Navigation Bar 구현

```kotlin
@Composable
fun BowlingClubApp() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem("home", "홈", Icons.Filled.Home),
        BottomNavItem("tournament", "토너먼트", Icons.Filled.Event),
        BottomNavItem("member", "멤버", Icons.Filled.People),
        BottomNavItem("statistics", "통계", Icons.Filled.BarChart),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.material.icons.Icons
)
```

---

## 8. 의존성 그래프

### 8.1 기술 스택 및 라이브러리

```
┌─────────────────────────────────────────────────────────┐
│            프로젝트 기술 스택                            │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  IDE & Build Tools:                                     │
│  ├── Android Studio Giraffe (2022.3.1) 이상            │
│  ├── Gradle 8.1+                                        │
│  ├── Kotlin 1.9.x                                       │
│  └── Android API Level 26+ (Target 34+)                │
│                                                          │
│  Core Framework (androidx):                             │
│  ├── androidx.appcompat:appcompat:1.6.1                 │
│  ├── androidx.core:core-ktx:1.12.0                      │
│  ├── androidx.lifecycle:lifecycle-runtime-ktx:2.6.2     │
│  └── androidx.activity:activity-compose:1.8.1          │
│                                                          │
│  UI Framework:                                          │
│  ├── androidx.compose.ui:ui:1.6.0                       │
│  ├── androidx.compose.foundation:foundation:1.6.0       │
│  ├── androidx.compose.material3:material3:1.1.2         │
│  ├── androidx.compose.material:material-icons-extended │
│  └── androidx.compose.runtime:runtime:1.6.0             │
│                                                          │
│  Navigation:                                            │
│  ├── androidx.navigation:navigation-compose:2.7.5       │
│  └── androidx.navigation:navigation-runtime-ktx:2.7.5   │
│                                                          │
│  Database (Room):                                       │
│  ├── androidx.room:room-runtime:2.6.1                   │
│  ├── androidx.room:room-ktx:2.6.1                       │
│  ├── androidx.room:room-compiler:2.6.1 (kapt)           │
│  └── androidx.sqlite:sqlite-ktx:2.4.0                   │
│                                                          │
│  Dependency Injection (Hilt):                           │
│  ├── com.google.dagger:hilt-android:2.48                │
│  ├── com.google.dagger:hilt-compiler:2.48 (kapt)        │
│  ├── androidx.hilt:hilt-navigation-compose:1.1.0        │
│  └── androidx.hilt:hilt-lifecycle-viewmodel:1.0.0       │
│                                                          │
│  Async & Coroutines:                                    │
│  ├── org.jetbrains.kotlinx:kotlinx-coroutines-core      │
│  ├── org.jetbrains.kotlinx:kotlinx-coroutines-android   │
│  └── androidx.lifecycle:lifecycle-viewmodel-compose     │
│                                                          │
│  Networking:                                            │
│  ├── com.squareup.retrofit2:retrofit:2.10.0             │
│  ├── com.squareup.retrofit2:converter-gson:2.10.0       │
│  ├── com.squareup.okhttp3:okhttp:4.11.0                 │
│  ├── com.squareup.okhttp3:logging-interceptor:4.11.0    │
│  └── com.google.code.gson:gson:2.10.1                   │
│                                                          │
│  Image Processing:                                      │
│  ├── androidx.camera:camera-core:1.3.0-rc01             │
│  ├── androidx.camera:camera-camera2:1.3.0-rc01          │
│  ├── androidx.camera:camera-lifecycle:1.3.0-rc01        │
│  ├── androidx.camera:camera-view:1.3.0-rc01             │
│  └── com.github.bumptech.glide:glide:4.16.0             │
│                                                          │
│  Charts & Visualization:                                │
│  └── com.github.PhilJay:MPAndroidChart:v3.1.0           │
│                                                          │
│  File Operations:                                       │
│  ├── androidx.documentfile:documentfile:1.0.1           │
│  └── commons-io:commons-io:2.14.0                       │
│                                                          │
│  DateTime:                                              │
│  └── org.threeten:threetenbp:1.6.8 (또는 java.time)    │
│                                                          │
│  Serialization:                                         │
│  └── com.google.code.gson:gson:2.10.1                   │
│                                                          │
│  Testing:                                               │
│  ├── junit:junit:4.13.2                                 │
│  ├── androidx.test.ext:junit:1.1.5                      │
│  ├── androidx.test.espresso:espresso-core:3.5.1         │
│  ├── org.mockito:mockito-core:5.2.0                     │
│  ├── org.mockito.kotlin:mockito-kotlin:5.1.0            │
│  ├── io.mockk:mockk:1.13.7                              │
│  └── kotlinx-coroutines-test                            │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 8.2 build.gradle.kts 의존성 정의

```kotlin
// Project build.gradle.kts
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}

// App build.gradle.kts
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.bowlingclub.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bowlingclub.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core & AppCompat
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose UI
    implementation(platform("androidx.compose:compose-bom:2023.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Image Processing & Camera
    implementation("androidx.camera:camera-core:1.3.0-rc01")
    implementation("androidx.camera:camera-camera2:1.3.0-rc01")
    implementation("androidx.camera:camera-lifecycle:1.3.0-rc01")
    implementation("androidx.camera:camera-view:1.3.0-rc01")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // File Operations
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("commons-io:commons-io:2.14.0")

    // DateTime
    implementation("org.threeten:threetenbp:1.6.8")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

### 8.3 의존성 트리 (시각화)

```
BowlingClubApp
│
├── UI Layer
│   ├── androidx.compose.* (Jetpack Compose)
│   ├── androidx.navigation.compose (Navigation)
│   └── com.github.PhilJay:MPAndroidChart (Charts)
│
├── ViewModel & Presentation Logic
│   ├── androidx.lifecycle.* (Lifecycle)
│   └── org.jetbrains.kotlinx:kotlinx-coroutines (Async)
│
├── Data Layer (Repository)
│   ├── Room Database
│   │   ├── androidx.room.* (Room ORM)
│   │   └── androidx.sqlite.* (SQLite)
│   │
│   └── Networking
│       ├── com.squareup.retrofit2 (REST API)
│       └── com.squareup.okhttp3 (HTTP Client)
│
├── Dependency Injection (Hilt)
│   ├── com.google.dagger:hilt-android
│   └── androidx.hilt:hilt-*
│
├── Utilities
│   ├── com.google.code.gson (JSON Serialization)
│   ├── androidx.camera.* (Camera API)
│   ├── commons-io (File Operations)
│   └── org.threeten (DateTime)
│
└── Testing
    ├── junit (Unit Testing)
    ├── androidx.test.espresso (UI Testing)
    ├── org.mockito (Mocking)
    └── io.mockk (Kotlin Mocking)
```

### 8.4 라이브러리별 용도 및 버전 정책

| 라이브러리 | 버전 | 목적 | 유지보수 상태 |
|----------|------|------|------------|
| Kotlin | 1.9.x | 주 프로그래밍 언어 | ✅ 활발 |
| Jetpack Compose | 1.6.x | 선언형 UI 프레임워크 | ✅ 활발 |
| Room | 2.6.x | 로컬 데이터베이스 | ✅ 활발 |
| Hilt | 2.48 | 의존성 주입 | ✅ 활발 |
| Retrofit | 2.10.x | REST API 클라이언트 | ✅ 활발 |
| OkHttp | 4.11.x | HTTP 클라이언트 | ✅ 활발 |
| CameraX | 1.3.x | 카메라 API 래퍼 | ✅ 활발 |
| Coroutines | 1.7.x | 비동기 프로그래밍 | ✅ 활발 |
| MPAndroidChart | 3.1.x | 차트 시각화 | ⚠️ 유지보수 중 |
| Gson | 2.10.x | JSON 직렬화 | ✅ 활발 |

### 8.5 프로젝트 구조

```
BowlingClubApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bowlingclub/
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── BowlingClubDatabase.kt
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   └── dao/
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MemberRepository.kt
│   │   │   │   │   │   ├── TournamentRepository.kt
│   │   │   │   │   │   ├── GameScoreRepository.kt
│   │   │   │   │   │   └── ...
│   │   │   │   │   └── network/
│   │   │   │   │       ├── NaverOcrApiService.kt
│   │   │   │   │       └── OcrRepository.kt
│   │   │   │   │
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── screens/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   ├── TournamentScreen.kt
│   │   │   │   │   │   │   └── ...
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   └── theme/
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       ├── HomeViewModel.kt
│   │   │   │   │       ├── TournamentViewModel.kt
│   │   │   │   │       └── ...
│   │   │   │   │
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Member.kt
│   │   │   │   │   │   ├── Tournament.kt
│   │   │   │   │   │   └── ...
│   │   │   │   │   ├── usecase/
│   │   │   │   │   └── repository/
│   │   │   │   │
│   │   │   │   ├── util/
│   │   │   │   │   ├── RankingCalculator.kt
│   │   │   │   │   ├── ImageGenerator.kt
│   │   │   │   │   ├── BackupManager.kt
│   │   │   │   │   └── ...
│   │   │   │   │
│   │   │   │   └── MainActivity.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── xml/
│   │   │   │   │   └── file_paths.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── colors.xml
│   │   │   │   └── ...
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/
│   │   │   └── java/com/bowlingclub/
│   │   │       └── ...
│   │   │
│   │   └── androidTest/
│   │       └── java/com/bowlingclub/
│   │           └── ...
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

---

## 부록: 주요 인터페이스 및 추상화

### 주요 Repository 인터페이스

```kotlin
interface MemberRepository {
    suspend fun saveMember(member: Member): Result<Long>
    suspend fun updateMember(member: Member): Result<Unit>
    suspend fun deleteMember(memberId: Long): Result<Unit>
    suspend fun getMember(memberId: Long): Result<Member?>
    fun getMembers(): Flow<Result<List<Member>>>
    fun getActiveMembers(): Flow<Result<List<Member>>>
    suspend fun searchMembers(query: String): Result<List<Member>>
}

interface TournamentRepository {
    suspend fun saveTournament(tournament: Tournament): Result<Long>
    suspend fun getTournament(id: Long): Result<Tournament?>
    fun getTournaments(): Flow<Result<List<Tournament>>>
    suspend fun startTournament(tournamentId: Long): Result<Unit>
    suspend fun completeTournament(tournamentId: Long): Result<Unit>
    suspend fun addParticipants(tournamentId: Long, memberIds: List<Long>): Result<Unit>
}

interface GameScoreRepository {
    suspend fun saveGameScore(score: GameScore): Result<Long>
    suspend fun getTournamentScores(tournamentId: Long): Result<List<GameScore>>
    suspend fun calculateRankings(
        tournamentId: Long,
        handicapPerGame: Int
    ): Result<List<RankingResult>>
}
```

---

## 용어 정의

| 용어 | 정의 |
|------|------|
| **DAO** | Data Access Object - 데이터베이스 접근 계층 |
| **Entity** | 데이터베이스 테이블과 매핑되는 Kotlin 데이터 클래스 |
| **Repository** | 데이터 소스를 추상화하는 패턴 |
| **ViewModel** | UI 상태 및 비즈니스 로직 관리 |
| **Composable** | Jetpack Compose의 UI 함수 |
| **State** | 어느 시점의 UI 또는 비즈니스 상태 |
| **Flow** | Kotlin Coroutines의 비동기 데이터 스트림 |
| **핸디캡** | 여성 멤버에게 부여되는 추가 점수 |
| **순위** | 토너먼트에서의 최종 등수 |
| **OCR** | Optical Character Recognition - 광학 문자 인식 |

---

**END OF DOCUMENT**
