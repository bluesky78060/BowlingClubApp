package com.bowlingclub.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bowlingclub.app.data.local.BowlingClubDatabase
import com.bowlingclub.app.data.local.dao.*
import com.bowlingclub.app.data.local.entity.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Database Integration Test (Task 8-3)
 * Tests full database integration with in-memory Room database.
 * Covers CRUD operations, E2E flows, and data integrity.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    private lateinit var database: BowlingClubDatabase
    private lateinit var memberDao: MemberDao
    private lateinit var tournamentDao: TournamentDao
    private lateinit var scoreDao: GameScoreDao
    private lateinit var participantDao: TournamentParticipantDao
    private lateinit var teamDao: TeamDao
    private lateinit var teamMemberDao: TeamMemberDao
    private lateinit var settingDao: SettingDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BowlingClubDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memberDao = database.memberDao()
        tournamentDao = database.tournamentDao()
        scoreDao = database.gameScoreDao()
        participantDao = database.tournamentParticipantDao()
        teamDao = database.teamDao()
        teamMemberDao = database.teamMemberDao()
        settingDao = database.settingDao()
    }

    @After
    @Throws(IOException::class)
    fun cleanup() {
        database.close()
    }

    // ========== Member CRUD Tests ==========

    @Test
    fun memberCrud_insertQueryUpdateDelete_worksCorrectly() = runTest {
        // Insert
        val member = Member(
            name = "김철수",
            nickname = "철수",
            gender = "M",
            phoneNumber = "010-1234-5678",
            joinDate = LocalDate.now()
        )
        val memberId = memberDao.insert(member).toInt()

        // Query
        val retrievedMember = memberDao.getMemberById(memberId).first()
        assertThat(retrievedMember).isNotNull()
        assertThat(retrievedMember?.name).isEqualTo("김철수")
        assertThat(retrievedMember?.nickname).isEqualTo("철수")

        // Update
        val updatedMember = retrievedMember!!.copy(
            nickname = "수",
            phoneNumber = "010-9999-8888"
        )
        memberDao.update(updatedMember)
        val afterUpdate = memberDao.getMemberById(memberId).first()
        assertThat(afterUpdate?.nickname).isEqualTo("수")
        assertThat(afterUpdate?.phoneNumber).isEqualTo("010-9999-8888")

        // Delete
        memberDao.delete(afterUpdate!!)
        val afterDelete = memberDao.getMemberById(memberId).first()
        assertThat(afterDelete).isNull()
    }

    @Test
    fun memberDao_getAllMembers_returnsOrderedByName() = runTest {
        // Insert members in random order
        memberDao.insert(Member(name = "최민수", joinDate = LocalDate.now()))
        memberDao.insert(Member(name = "김영희", joinDate = LocalDate.now()))
        memberDao.insert(Member(name = "박지성", joinDate = LocalDate.now()))

        // Retrieve all members
        val members = memberDao.getAllMembers().first()

        // Verify ordered by name
        assertThat(members).hasSize(3)
        assertThat(members[0].name).isEqualTo("김영희")
        assertThat(members[1].name).isEqualTo("박지성")
        assertThat(members[2].name).isEqualTo("최민수")
    }

    @Test
    fun memberDao_getActiveMembers_filtersInactiveMembers() = runTest {
        // Insert active and inactive members
        memberDao.insert(Member(name = "활동회원1", joinDate = LocalDate.now(), isActive = true))
        memberDao.insert(Member(name = "휴면회원", joinDate = LocalDate.now(), isActive = false))
        memberDao.insert(Member(name = "활동회원2", joinDate = LocalDate.now(), isActive = true))

        // Retrieve only active members
        val activeMembers = memberDao.getActiveMembers().first()

        assertThat(activeMembers).hasSize(2)
        assertThat(activeMembers.all { it.isActive }).isTrue()
    }

    // ========== Tournament Lifecycle Tests ==========

    @Test
    fun tournamentLifecycle_createAddParticipantsAddScores_worksCorrectly() = runTest {
        // Create tournament
        val tournament = Tournament(
            name = "2024년 1월 정기전",
            date = LocalDate.now(),
            location = "서울볼링장",
            gameCount = 3
        )
        val tournamentId = tournamentDao.insert(tournament).toInt()

        // Add participants (members)
        val member1Id = memberDao.insert(Member(name = "회원1", joinDate = LocalDate.now())).toInt()
        val member2Id = memberDao.insert(Member(name = "회원2", joinDate = LocalDate.now())).toInt()

        participantDao.insert(TournamentParticipant(tournamentId = tournamentId, memberId = member1Id))
        participantDao.insert(TournamentParticipant(tournamentId = tournamentId, memberId = member2Id))

        // Add scores for each member
        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = member1Id, gameNumber = 1, score = 180, finalScore = 180))
        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = member1Id, gameNumber = 2, score = 200, finalScore = 200))
        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = member1Id, gameNumber = 3, score = 190, finalScore = 190))

        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = member2Id, gameNumber = 1, score = 150, finalScore = 150))
        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = member2Id, gameNumber = 2, score = 170, finalScore = 170))
        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = member2Id, gameNumber = 3, score = 160, finalScore = 160))

        // Verify participants
        val participants = participantDao.getParticipantsByTournament(tournamentId).first()
        assertThat(participants).hasSize(2)

        // Verify scores
        val member1Scores = scoreDao.getScoresByTournamentAndMember(tournamentId, member1Id).first()
        assertThat(member1Scores).hasSize(3)
        assertThat(member1Scores.map { it.score }).containsExactly(180, 200, 190)

        // Calculate average
        val member1Average = scoreDao.getAverageScore(member1Id).first()
        assertThat(member1Average).isWithin(0.1).of((180 + 200 + 190) / 3.0)
    }

    @Test
    fun tournamentDao_getUpcomingTournaments_filtersScheduledOnly() = runTest {
        // Insert tournaments with different statuses
        tournamentDao.insert(Tournament(name = "예정된 대회", date = LocalDate.now().plusDays(7), status = "SCHEDULED"))
        tournamentDao.insert(Tournament(name = "진행 중 대회", date = LocalDate.now(), status = "IN_PROGRESS"))
        tournamentDao.insert(Tournament(name = "완료된 대회", date = LocalDate.now().minusDays(7), status = "COMPLETED"))

        val upcomingTournaments = tournamentDao.getUpcomingTournaments().first()

        assertThat(upcomingTournaments).hasSize(1)
        assertThat(upcomingTournaments[0].name).isEqualTo("예정된 대회")
        assertThat(upcomingTournaments[0].status).isEqualTo("SCHEDULED")
    }

    // ========== Score Operations Tests ==========

    @Test
    fun scoreDao_insertAndQueryScores_worksCorrectly() = runTest {
        // Create member and tournament
        val memberId = memberDao.insert(Member(name = "테스트회원", joinDate = LocalDate.now())).toInt()
        val tournamentId = tournamentDao.insert(Tournament(name = "테스트대회", date = LocalDate.now())).toInt()

        // Insert scores
        val scores = listOf(
            GameScore(tournamentId = tournamentId, memberId = memberId, gameNumber = 1, score = 220, finalScore = 220),
            GameScore(tournamentId = tournamentId, memberId = memberId, gameNumber = 2, score = 240, finalScore = 240),
            GameScore(tournamentId = tournamentId, memberId = memberId, gameNumber = 3, score = 210, finalScore = 210)
        )
        scoreDao.insertAll(scores)

        // Query by member
        val memberScores = scoreDao.getScoresByMember(memberId).first()
        assertThat(memberScores).hasSize(3)

        // Query by tournament
        val tournamentScores = scoreDao.getScoresByTournament(tournamentId).first()
        assertThat(tournamentScores).hasSize(3)

        // Verify statistics
        val average = scoreDao.getAverageScore(memberId).first()
        val highScore = scoreDao.getHighScore(memberId).first()
        val minScore = scoreDao.getMinScore(memberId).first()

        assertThat(average).isWithin(0.1).of((220 + 240 + 210) / 3.0)
        assertThat(highScore).isEqualTo(240)
        assertThat(minScore).isEqualTo(210)
    }

    @Test
    fun scoreDao_getRecentScores_returnsLimitedResults() = runTest {
        val memberId = memberDao.insert(Member(name = "회원", joinDate = LocalDate.now())).toInt()
        val tournamentId = tournamentDao.insert(Tournament(name = "대회", date = LocalDate.now())).toInt()

        // Insert 10 scores
        repeat(10) { i ->
            scoreDao.insert(GameScore(
                tournamentId = tournamentId,
                memberId = memberId,
                gameNumber = i + 1,
                score = 150 + i * 10,
                finalScore = 150 + i * 10
            ))
        }

        // Get only 5 most recent scores
        val recentScores = scoreDao.getRecentScores(memberId, 5).first()

        assertThat(recentScores).hasSize(5)
    }

    // ========== Team Operations Tests ==========

    @Test
    fun teamOperations_createTeamAddMembers_worksCorrectly() = runTest {
        // Create tournament
        val tournamentId = tournamentDao.insert(Tournament(name = "팀전", date = LocalDate.now(), isTeamGame = true)).toInt()

        // Create team
        val team = Team(tournamentId = tournamentId, name = "A팀")
        val teamId = teamDao.insert(team).toInt()

        // Create members and add to team
        val member1Id = memberDao.insert(Member(name = "팀원1", joinDate = LocalDate.now())).toInt()
        val member2Id = memberDao.insert(Member(name = "팀원2", joinDate = LocalDate.now())).toInt()
        val member3Id = memberDao.insert(Member(name = "팀원3", joinDate = LocalDate.now())).toInt()

        teamMemberDao.insertAll(listOf(
            TeamMember(teamId = teamId, memberId = member1Id),
            TeamMember(teamId = teamId, memberId = member2Id),
            TeamMember(teamId = teamId, memberId = member3Id)
        ))

        // Query team members
        val teamMembers = teamMemberDao.getMembersByTeam(teamId).first()
        assertThat(teamMembers).hasSize(3)

        // Verify member count
        val memberCount = teamMemberDao.getMemberCount(teamId).first()
        assertThat(memberCount).isEqualTo(3)
    }

    @Test
    fun teamDao_deleteByTournament_cascadesCorrectly() = runTest {
        val tournamentId = tournamentDao.insert(Tournament(name = "팀전", date = LocalDate.now())).toInt()
        val teamId = teamDao.insert(Team(tournamentId = tournamentId, name = "팀")).toInt()
        val memberId = memberDao.insert(Member(name = "회원", joinDate = LocalDate.now())).toInt()
        teamMemberDao.insert(TeamMember(teamId = teamId, memberId = memberId))

        // Delete teams by tournament
        teamDao.deleteByTournament(tournamentId)

        // Verify teams are deleted
        val teams = teamDao.getTeamsByTournament(tournamentId).first()
        assertThat(teams).isEmpty()
    }

    // ========== Settings Tests ==========

    @Test
    fun settingDao_insertUpdateRetrieve_worksCorrectly() = runTest {
        // Insert setting
        val setting = Setting(key = "app_theme", value = "dark")
        settingDao.insertOrUpdate(setting)

        // Read setting
        val retrieved = settingDao.getSetting("app_theme").first()
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.value).isEqualTo("dark")

        // Update setting
        val updatedSetting = Setting(key = "app_theme", value = "light")
        settingDao.insertOrUpdate(updatedSetting)

        val afterUpdate = settingDao.getSettingValue("app_theme")
        assertThat(afterUpdate).isEqualTo("light")
    }

    @Test
    fun settingDao_getAllSettings_returnsAllSettings() = runTest {
        settingDao.insertOrUpdate(Setting(key = "theme", value = "dark"))
        settingDao.insertOrUpdate(Setting(key = "language", value = "ko"))
        settingDao.insertOrUpdate(Setting(key = "notifications", value = "enabled"))

        val allSettings = settingDao.getAllSettings().first()

        assertThat(allSettings).hasSize(3)
        assertThat(allSettings.map { it.key }).containsExactly("theme", "language", "notifications")
    }

    // ========== Foreign Key Cascade Tests ==========

    @Test
    fun foreignKeyCascade_deleteTournament_deletesRelatedData() = runTest {
        // Create tournament with participants and scores
        val tournamentId = tournamentDao.insert(Tournament(name = "대회", date = LocalDate.now())).toInt()
        val memberId = memberDao.insert(Member(name = "회원", joinDate = LocalDate.now())).toInt()

        participantDao.insert(TournamentParticipant(tournamentId = tournamentId, memberId = memberId))
        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = memberId, gameNumber = 1, score = 200, finalScore = 200))

        // Delete tournament
        val tournament = tournamentDao.getTournamentById(tournamentId).first()
        tournamentDao.delete(tournament!!)

        // Verify cascade delete
        val participants = participantDao.getParticipantsByTournament(tournamentId).first()
        val scores = scoreDao.getScoresByTournament(tournamentId).first()

        assertThat(participants).isEmpty()
        assertThat(scores).isEmpty()
    }

    @Test
    fun foreignKeyCascade_deleteMember_deletesRelatedScores() = runTest {
        val memberId = memberDao.insert(Member(name = "회원", joinDate = LocalDate.now())).toInt()
        val tournamentId = tournamentDao.insert(Tournament(name = "대회", date = LocalDate.now())).toInt()

        scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = memberId, gameNumber = 1, score = 180, finalScore = 180))

        // Delete member
        val member = memberDao.getMemberById(memberId).first()
        memberDao.delete(member!!)

        // Verify scores are deleted
        val scores = scoreDao.getScoresByMember(memberId).first()
        assertThat(scores).isEmpty()
    }

    // ========== Backup/Restore Tests ==========

    @Test
    fun backupRestore_insertDataReadClearRestore_worksCorrectly() = runTest {
        // Insert data
        memberDao.insert(Member(name = "회원1", joinDate = LocalDate.now()))
        memberDao.insert(Member(name = "회원2", joinDate = LocalDate.now()))
        tournamentDao.insert(Tournament(name = "대회1", date = LocalDate.now()))

        // Read all (backup)
        val members = memberDao.getAllMembers().first()
        val tournaments = tournamentDao.getAllTournaments().first()

        assertThat(members).hasSize(2)
        assertThat(tournaments).hasSize(1)

        // Clear database
        memberDao.deleteAll()
        tournamentDao.deleteAll()

        assertThat(memberDao.getAllMembers().first()).isEmpty()
        assertThat(tournamentDao.getAllTournaments().first()).isEmpty()

        // Restore
        members.forEach { memberDao.insert(it) }
        tournaments.forEach { tournamentDao.insert(it) }

        // Verify restored
        assertThat(memberDao.getAllMembers().first()).hasSize(2)
        assertThat(tournamentDao.getAllTournaments().first()).hasSize(1)
    }

    // ========== Complex Query Tests ==========

    @Test
    fun complexQuery_getTournamentCount_returnsCorrectCount() = runTest {
        val memberId = memberDao.insert(Member(name = "회원", joinDate = LocalDate.now())).toInt()

        // Participate in 3 different tournaments
        repeat(3) { i ->
            val tournamentId = tournamentDao.insert(Tournament(name = "대회$i", date = LocalDate.now())).toInt()
            participantDao.insert(TournamentParticipant(tournamentId = tournamentId, memberId = memberId))
            scoreDao.insert(GameScore(tournamentId = tournamentId, memberId = memberId, gameNumber = 1, score = 200, finalScore = 200))
        }

        val tournamentCount = scoreDao.getTournamentCount(memberId).first()
        assertThat(tournamentCount).isEqualTo(3)
    }

    @Test
    fun complexQuery_getScoreCountByRange_calculatesCorrectly() = runTest {
        val memberId = memberDao.insert(Member(name = "회원", joinDate = LocalDate.now())).toInt()
        val tournamentId = tournamentDao.insert(Tournament(name = "대회", date = LocalDate.now())).toInt()

        // Insert scores in different ranges
        val scoreValues = listOf(150, 180, 200, 220, 250, 280)
        scoreValues.forEachIndexed { index, score ->
            scoreDao.insert(GameScore(
                tournamentId = tournamentId,
                memberId = memberId,
                gameNumber = index + 1,
                score = score,
                finalScore = score
            ))
        }

        // Count scores in range 180-220
        val countInRange = scoreDao.getScoreCountByRange(memberId, 180, 220).first()
        assertThat(countInRange).isEqualTo(3) // 180, 200, 220
    }
}
