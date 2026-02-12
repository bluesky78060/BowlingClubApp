package com.bowlingclub.app.util

import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.local.entity.Team
import com.bowlingclub.app.data.local.entity.TeamMember
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TeamRankingCalculatorTest {

    private lateinit var memberNames: Map<Int, String>

    @Before
    fun setup() {
        memberNames = mapOf(
            1 to "김철수",
            2 to "이영희",
            3 to "박민수",
            4 to "최지원",
            5 to "정수연",
            6 to "한동훈"
        )
    }

    // 테스트 헬퍼: Team 생성
    private fun createTeam(id: Int, name: String, tournamentId: Int = 1): Team {
        return Team(
            id = id,
            tournamentId = tournamentId,
            name = name,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    // 테스트 헬퍼: TeamMember 생성
    private fun createTeamMember(id: Int, teamId: Int, memberId: Int): TeamMember {
        return TeamMember(
            id = id,
            teamId = teamId,
            memberId = memberId
        )
    }

    // 테스트 헬퍼: GameScore 생성
    private fun createGameScore(
        id: Int = 0,
        tournamentId: Int = 1,
        memberId: Int,
        gameNumber: Int,
        score: Int,
        handicapScore: Int = 0
    ): GameScore {
        return GameScore(
            id = id,
            tournamentId = tournamentId,
            memberId = memberId,
            gameNumber = gameNumber,
            score = score,
            handicapScore = handicapScore,
            finalScore = score + handicapScore,
            recordedBy = "MANUAL",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `calculateTeamRanking - 정상 순위 계산 (3팀, 다른 총점)`() {
        // Given: 3개 팀, 각 2명씩
        val teams = listOf(
            createTeam(1, "레드팀"),
            createTeam(2, "블루팀"),
            createTeam(3, "그린팀")
        )

        val teamMembers = listOf(
            // 레드팀: 멤버 1, 2
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 1, memberId = 2),
            // 블루팀: 멤버 3, 4
            createTeamMember(3, teamId = 2, memberId = 3),
            createTeamMember(4, teamId = 2, memberId = 4),
            // 그린팀: 멤버 5, 6
            createTeamMember(5, teamId = 3, memberId = 5),
            createTeamMember(6, teamId = 3, memberId = 6)
        )

        val scores = listOf(
            // 레드팀: 멤버 1 (200*3=600), 멤버 2 (220*3=660) → 총 1260
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            createGameScore(memberId = 2, gameNumber = 1, score = 220),
            createGameScore(memberId = 2, gameNumber = 2, score = 220),
            createGameScore(memberId = 2, gameNumber = 3, score = 220),
            // 블루팀: 멤버 3 (180*3=540), 멤버 4 (190*3=570) → 총 1110
            createGameScore(memberId = 3, gameNumber = 1, score = 180),
            createGameScore(memberId = 3, gameNumber = 2, score = 180),
            createGameScore(memberId = 3, gameNumber = 3, score = 180),
            createGameScore(memberId = 4, gameNumber = 1, score = 190),
            createGameScore(memberId = 4, gameNumber = 2, score = 190),
            createGameScore(memberId = 4, gameNumber = 3, score = 190),
            // 그린팀: 멤버 5 (210*3=630), 멤버 6 (210*3=630) → 총 1260
            createGameScore(memberId = 5, gameNumber = 1, score = 210),
            createGameScore(memberId = 5, gameNumber = 2, score = 210),
            createGameScore(memberId = 5, gameNumber = 3, score = 210),
            createGameScore(memberId = 6, gameNumber = 1, score = 210),
            createGameScore(memberId = 6, gameNumber = 2, score = 210),
            createGameScore(memberId = 6, gameNumber = 3, score = 210)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then: 레드팀과 그린팀 동점 1260 (highGame으로 결정)
        assertThat(result).hasSize(3)

        // 레드팀: highGame = 220
        // 그린팀: highGame = 210
        // 레드팀이 1위
        assertThat(result[0].team.name).isEqualTo("레드팀")
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].teamFinalTotal).isEqualTo(1260)
        assertThat(result[0].teamHighGame).isEqualTo(220)

        assertThat(result[1].team.name).isEqualTo("그린팀")
        assertThat(result[1].rank).isEqualTo(2)
        assertThat(result[1].teamFinalTotal).isEqualTo(1260)
        assertThat(result[1].teamHighGame).isEqualTo(210)

        assertThat(result[2].team.name).isEqualTo("블루팀")
        assertThat(result[2].rank).isEqualTo(3)
        assertThat(result[2].teamFinalTotal).isEqualTo(1110)
    }

    @Test
    fun `calculateTeamRanking - 동점 처리 (같은 finalTotal과 highGame)`() {
        // Given: 2팀이 완전 동점
        val teams = listOf(
            createTeam(1, "팀 A"),
            createTeam(2, "팀 B")
        )

        val teamMembers = listOf(
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 2, memberId = 2)
        )

        val scores = listOf(
            // 팀 A: 600점, highGame 200
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            // 팀 B: 600점, highGame 200
            createGameScore(memberId = 2, gameNumber = 1, score = 200),
            createGameScore(memberId = 2, gameNumber = 2, score = 200),
            createGameScore(memberId = 2, gameNumber = 3, score = 200)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then: 둘 다 1위
        assertThat(result).hasSize(2)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[1].rank).isEqualTo(1)
    }

    @Test
    fun `calculateTeamRanking - 단일 팀`() {
        // Given: 1개 팀만
        val teams = listOf(createTeam(1, "단독팀"))
        val teamMembers = listOf(createTeamMember(1, teamId = 1, memberId = 1))
        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 210),
            createGameScore(memberId = 1, gameNumber = 3, score = 190)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].teamFinalTotal).isEqualTo(600)
        assertThat(result[0].teamHighGame).isEqualTo(210)
    }

    @Test
    fun `calculateTeamRanking - 다른 멤버 수를 가진 팀`() {
        // Given: 팀 1은 3명, 팀 2는 2명
        val teams = listOf(
            createTeam(1, "대팀"),
            createTeam(2, "소팀")
        )

        val teamMembers = listOf(
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 1, memberId = 2),
            createTeamMember(3, teamId = 1, memberId = 3),
            createTeamMember(4, teamId = 2, memberId = 4),
            createTeamMember(5, teamId = 2, memberId = 5)
        )

        val scores = listOf(
            // 대팀: 멤버 1,2,3 각 200점씩 → 총 1800
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            createGameScore(memberId = 2, gameNumber = 1, score = 200),
            createGameScore(memberId = 2, gameNumber = 2, score = 200),
            createGameScore(memberId = 2, gameNumber = 3, score = 200),
            createGameScore(memberId = 3, gameNumber = 1, score = 200),
            createGameScore(memberId = 3, gameNumber = 2, score = 200),
            createGameScore(memberId = 3, gameNumber = 3, score = 200),
            // 소팀: 멤버 4,5 각 200점씩 → 총 1200
            createGameScore(memberId = 4, gameNumber = 1, score = 200),
            createGameScore(memberId = 4, gameNumber = 2, score = 200),
            createGameScore(memberId = 4, gameNumber = 3, score = 200),
            createGameScore(memberId = 5, gameNumber = 1, score = 200),
            createGameScore(memberId = 5, gameNumber = 2, score = 200),
            createGameScore(memberId = 5, gameNumber = 3, score = 200)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then
        assertThat(result[0].team.name).isEqualTo("대팀")
        assertThat(result[0].teamFinalTotal).isEqualTo(1800)
        assertThat(result[0].memberRankings).hasSize(3)

        assertThat(result[1].team.name).isEqualTo("소팀")
        assertThat(result[1].teamFinalTotal).isEqualTo(1200)
        assertThat(result[1].memberRankings).hasSize(2)
    }

    @Test
    fun `calculateTeamRanking - 팀 점수 합산 정확도 검증`() {
        // Given
        val teams = listOf(createTeam(1, "테스트팀"))
        val teamMembers = listOf(
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 1, memberId = 2)
        )

        val scores = listOf(
            // 멤버 1: 150, 170, 180 = 500, handicap 20*3 = 60
            createGameScore(memberId = 1, gameNumber = 1, score = 150, handicapScore = 20),
            createGameScore(memberId = 1, gameNumber = 2, score = 170, handicapScore = 20),
            createGameScore(memberId = 1, gameNumber = 3, score = 180, handicapScore = 20),
            // 멤버 2: 200*3 = 600, handicap 0
            createGameScore(memberId = 2, gameNumber = 1, score = 200, handicapScore = 0),
            createGameScore(memberId = 2, gameNumber = 2, score = 200, handicapScore = 0),
            createGameScore(memberId = 2, gameNumber = 3, score = 200, handicapScore = 0)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then
        assertThat(result[0].teamTotalScore).isEqualTo(1100) // 500 + 600
        assertThat(result[0].teamHandicapTotal).isEqualTo(60) // 60 + 0
        assertThat(result[0].teamFinalTotal).isEqualTo(1160) // 1100 + 60
    }

    @Test
    fun `calculateTeamRanking - teamHighGame 계산 확인`() {
        // Given
        val teams = listOf(createTeam(1, "테스트팀"))
        val teamMembers = listOf(
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 1, memberId = 2)
        )

        val scores = listOf(
            // 멤버 1: highGame = 180
            createGameScore(memberId = 1, gameNumber = 1, score = 150),
            createGameScore(memberId = 1, gameNumber = 2, score = 180),
            createGameScore(memberId = 1, gameNumber = 3, score = 170),
            // 멤버 2: highGame = 250
            createGameScore(memberId = 2, gameNumber = 1, score = 200),
            createGameScore(memberId = 2, gameNumber = 2, score = 250),
            createGameScore(memberId = 2, gameNumber = 3, score = 220)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then: 팀의 highGame은 모든 멤버 중 최고점 (250)
        assertThat(result[0].teamHighGame).isEqualTo(250)
    }

    @Test
    fun `calculateTeamRanking - teamAverage 계산 확인`() {
        // Given
        val teams = listOf(createTeam(1, "테스트팀"))
        val teamMembers = listOf(
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 1, memberId = 2)
        )

        val scores = listOf(
            // 멤버 1: finalTotal = 600
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            // 멤버 2: finalTotal = 660
            createGameScore(memberId = 2, gameNumber = 1, score = 220),
            createGameScore(memberId = 2, gameNumber = 2, score = 220),
            createGameScore(memberId = 2, gameNumber = 3, score = 220)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then: teamAverage = teamFinalTotal / memberCount = 1260 / 2 = 630.0
        assertThat(result[0].teamAverage).isWithin(0.1).of(630.0)
    }

    @Test
    fun `calculateTeamRanking - 멤버가 없는 팀 처리`() {
        // Given: 팀은 있지만 멤버가 없음
        val teams = listOf(createTeam(1, "빈팀"))
        val teamMembers = emptyList<TeamMember>()
        val scores = emptyList<GameScore>()

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].teamFinalTotal).isEqualTo(0)
        assertThat(result[0].teamHighGame).isEqualTo(0)
        assertThat(result[0].teamAverage).isWithin(0.1).of(0.0)
        assertThat(result[0].memberRankings).isEmpty()
    }

    @Test
    fun `calculateTeamRanking - memberRankings 포함 확인`() {
        // Given
        val teams = listOf(createTeam(1, "테스트팀"))
        val teamMembers = listOf(
            createTeamMember(1, teamId = 1, memberId = 1),
            createTeamMember(2, teamId = 1, memberId = 2)
        )

        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            createGameScore(memberId = 2, gameNumber = 1, score = 180),
            createGameScore(memberId = 2, gameNumber = 2, score = 180),
            createGameScore(memberId = 2, gameNumber = 3, score = 180)
        )

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then
        assertThat(result[0].memberRankings).hasSize(2)
        assertThat(result[0].memberRankings[0].memberId).isEqualTo(1) // 1위 (600점)
        assertThat(result[0].memberRankings[1].memberId).isEqualTo(2) // 2위 (540점)
    }

    @Test
    fun `TeamRankingResult 데이터 클래스 구조 확인`() {
        // Given/When
        val team = createTeam(1, "테스트팀")
        val rankingResult = TeamRankingResult(
            rank = 1,
            team = team,
            memberRankings = emptyList(),
            teamTotalScore = 1200,
            teamHandicapTotal = 60,
            teamFinalTotal = 1260,
            teamHighGame = 250,
            teamAverage = 630.0
        )

        // Then
        assertThat(rankingResult.rank).isEqualTo(1)
        assertThat(rankingResult.team.name).isEqualTo("테스트팀")
        assertThat(rankingResult.teamTotalScore).isEqualTo(1200)
        assertThat(rankingResult.teamHandicapTotal).isEqualTo(60)
        assertThat(rankingResult.teamFinalTotal).isEqualTo(1260)
        assertThat(rankingResult.teamHighGame).isEqualTo(250)
        assertThat(rankingResult.teamAverage).isWithin(0.1).of(630.0)
    }

    @Test
    fun `calculateTeamRanking - 빈 팀 리스트`() {
        // Given
        val teams = emptyList<Team>()
        val teamMembers = emptyList<TeamMember>()
        val scores = emptyList<GameScore>()

        // When
        val result = TeamRankingCalculator.calculateTeamRanking(
            teams, teamMembers, scores, memberNames
        )

        // Then
        assertThat(result).isEmpty()
    }
}
