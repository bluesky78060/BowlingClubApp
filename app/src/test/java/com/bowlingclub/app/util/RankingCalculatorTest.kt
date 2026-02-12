package com.bowlingclub.app.util

import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.model.RankingResult
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class RankingCalculatorTest {

    private lateinit var members: Map<Int, String>

    @Before
    fun setup() {
        members = mapOf(
            1 to "김철수",
            2 to "이영희",
            3 to "박민수",
            4 to "최지원"
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
    fun `calculateRanking - 정상 순위 계산 (3명, 고유 점수)`() {
        // Given: 3명의 플레이어, 각 3게임
        val scores = listOf(
            // 멤버 1: 총 600점 (200+200+200)
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            // 멤버 2: 총 660점 (220+220+220)
            createGameScore(memberId = 2, gameNumber = 1, score = 220),
            createGameScore(memberId = 2, gameNumber = 2, score = 220),
            createGameScore(memberId = 2, gameNumber = 3, score = 220),
            // 멤버 3: 총 540점 (180+180+180)
            createGameScore(memberId = 3, gameNumber = 1, score = 180),
            createGameScore(memberId = 3, gameNumber = 2, score = 180),
            createGameScore(memberId = 3, gameNumber = 3, score = 180)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result).hasSize(3)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].memberId).isEqualTo(2) // 이영희 1위
        assertThat(result[0].finalTotal).isEqualTo(660)
        assertThat(result[1].rank).isEqualTo(2)
        assertThat(result[1].memberId).isEqualTo(1) // 김철수 2위
        assertThat(result[1].finalTotal).isEqualTo(600)
        assertThat(result[2].rank).isEqualTo(3)
        assertThat(result[2].memberId).isEqualTo(3) // 박민수 3위
        assertThat(result[2].finalTotal).isEqualTo(540)
    }

    @Test
    fun `calculateRanking - 동점 처리 (같은 finalTotal, 다른 highGame)`() {
        // Given: 2명이 같은 finalTotal이지만 highGame이 다름
        val scores = listOf(
            // 멤버 1: 총 600점, highGame = 250
            createGameScore(memberId = 1, gameNumber = 1, score = 250),
            createGameScore(memberId = 1, gameNumber = 2, score = 180),
            createGameScore(memberId = 1, gameNumber = 3, score = 170),
            // 멤버 2: 총 600점, highGame = 220
            createGameScore(memberId = 2, gameNumber = 1, score = 220),
            createGameScore(memberId = 2, gameNumber = 2, score = 200),
            createGameScore(memberId = 2, gameNumber = 3, score = 180)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].memberId).isEqualTo(1) // highGame 250
        assertThat(result[0].highGame).isEqualTo(250)
        assertThat(result[1].rank).isEqualTo(2)
        assertThat(result[1].memberId).isEqualTo(2) // highGame 220
        assertThat(result[1].highGame).isEqualTo(220)
    }

    @Test
    fun `calculateRanking - 완전 동점 (같은 finalTotal, 같은 highGame)`() {
        // Given: 2명이 완전히 같은 점수
        val scores = listOf(
            // 멤버 1
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            // 멤버 2
            createGameScore(memberId = 2, gameNumber = 1, score = 200),
            createGameScore(memberId = 2, gameNumber = 2, score = 200),
            createGameScore(memberId = 2, gameNumber = 3, score = 200)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then: 둘 다 1위
        assertThat(result).hasSize(2)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[1].rank).isEqualTo(1)
    }

    @Test
    fun `calculateRanking - 단일 플레이어`() {
        // Given: 1명만 게임
        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 210),
            createGameScore(memberId = 1, gameNumber = 3, score = 190)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].memberId).isEqualTo(1)
        assertThat(result[0].totalScore).isEqualTo(600)
        assertThat(result[0].highGame).isEqualTo(210)
        assertThat(result[0].average).isWithin(0.1).of(200.0)
    }

    @Test
    fun `calculateRanking - 빈 리스트`() {
        // Given
        val scores = emptyList<GameScore>()

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `calculateRanking - 핸디캡 점수 포함`() {
        // Given: 핸디캡 점수가 있는 경우
        val scores = listOf(
            // 멤버 1: score 500, handicap 60 = finalTotal 560
            createGameScore(memberId = 1, gameNumber = 1, score = 150, handicapScore = 20),
            createGameScore(memberId = 1, gameNumber = 2, score = 170, handicapScore = 20),
            createGameScore(memberId = 1, gameNumber = 3, score = 180, handicapScore = 20),
            // 멤버 2: score 600, handicap 0 = finalTotal 600
            createGameScore(memberId = 2, gameNumber = 1, score = 200, handicapScore = 0),
            createGameScore(memberId = 2, gameNumber = 2, score = 200, handicapScore = 0),
            createGameScore(memberId = 2, gameNumber = 3, score = 200, handicapScore = 0)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].memberId).isEqualTo(2) // finalTotal 600
        assertThat(result[0].totalScore).isEqualTo(600)
        assertThat(result[0].handicapTotal).isEqualTo(0)
        assertThat(result[0].finalTotal).isEqualTo(600)
        assertThat(result[1].rank).isEqualTo(2)
        assertThat(result[1].memberId).isEqualTo(1) // finalTotal 560
        assertThat(result[1].totalScore).isEqualTo(500)
        assertThat(result[1].handicapTotal).isEqualTo(60)
        assertThat(result[1].finalTotal).isEqualTo(560)
    }

    @Test
    fun `calculateRanking - highGame 계산 확인`() {
        // Given
        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 1, score = 150),
            createGameScore(memberId = 1, gameNumber = 2, score = 250),
            createGameScore(memberId = 1, gameNumber = 3, score = 180)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result[0].highGame).isEqualTo(250)
    }

    @Test
    fun `calculateRanking - average 계산 확인`() {
        // Given
        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 1, score = 180),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 220)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then: (180 + 200 + 220) / 3 = 200.0
        assertThat(result[0].average).isWithin(0.1).of(200.0)
    }

    @Test
    fun `calculateRanking - 알 수 없는 멤버 처리`() {
        // Given: members 맵에 없는 memberId
        val scores = listOf(
            createGameScore(memberId = 999, gameNumber = 1, score = 200)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].memberName).isEqualTo("Unknown")
    }

    @Test
    fun `calculateHighGame - 가장 높은 점수 반환`() {
        // Given
        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 1, score = 180),
            createGameScore(memberId = 1, gameNumber = 2, score = 250),
            createGameScore(memberId = 2, gameNumber = 1, score = 200),
            createGameScore(memberId = 2, gameNumber = 2, score = 220)
        )

        // When
        val result = RankingCalculator.calculateHighGame(scores)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.score).isEqualTo(250)
        assertThat(result?.memberId).isEqualTo(1)
    }

    @Test
    fun `calculateHighGame - 빈 리스트는 null 반환`() {
        // Given
        val scores = emptyList<GameScore>()

        // When
        val result = RankingCalculator.calculateHighGame(scores)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `calculateHighSeries - 가장 높은 합계 반환`() {
        // Given
        val scores = listOf(
            // 멤버 1: 총 600점
            createGameScore(memberId = 1, gameNumber = 1, score = 200),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 200),
            // 멤버 2: 총 660점
            createGameScore(memberId = 2, gameNumber = 1, score = 220),
            createGameScore(memberId = 2, gameNumber = 2, score = 220),
            createGameScore(memberId = 2, gameNumber = 3, score = 220)
        )

        // When
        val result = RankingCalculator.calculateHighSeries(scores, members)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.memberId).isEqualTo(2)
        assertThat(result?.finalTotal).isEqualTo(660)
        assertThat(result?.rank).isEqualTo(1)
    }

    @Test
    fun `calculateHighSeries - 빈 리스트는 null 반환`() {
        // Given
        val scores = emptyList<GameScore>()

        // When
        val result = RankingCalculator.calculateHighSeries(scores, members)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `calculateRanking - 마지막 게임 점수로 동점 처리`() {
        // Given: finalTotal, highGame이 같고, 마지막 게임 점수만 다름
        val scores = listOf(
            // 멤버 1: 총 600점, highGame 250, 마지막 게임 150
            createGameScore(memberId = 1, gameNumber = 1, score = 250),
            createGameScore(memberId = 1, gameNumber = 2, score = 200),
            createGameScore(memberId = 1, gameNumber = 3, score = 150),
            // 멤버 2: 총 600점, highGame 250, 마지막 게임 180
            createGameScore(memberId = 2, gameNumber = 1, score = 250),
            createGameScore(memberId = 2, gameNumber = 2, score = 170),
            createGameScore(memberId = 2, gameNumber = 3, score = 180)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then: 마지막 게임 점수가 높은 멤버 2가 1위
        assertThat(result).hasSize(2)
        assertThat(result[0].rank).isEqualTo(1)
        assertThat(result[0].memberId).isEqualTo(2)
        assertThat(result[0].gameScores.last()).isEqualTo(180)
        assertThat(result[1].rank).isEqualTo(2)
        assertThat(result[1].memberId).isEqualTo(1)
        assertThat(result[1].gameScores.last()).isEqualTo(150)
    }

    @Test
    fun `calculateRanking - gameScores 정렬 확인`() {
        // Given: gameNumber 순서대로 입력되지 않은 경우
        val scores = listOf(
            createGameScore(memberId = 1, gameNumber = 3, score = 220),
            createGameScore(memberId = 1, gameNumber = 1, score = 180),
            createGameScore(memberId = 1, gameNumber = 2, score = 200)
        )

        // When
        val result = RankingCalculator.calculateRanking(scores, members)

        // Then: gameScores는 gameNumber 순서대로 정렬되어야 함
        assertThat(result[0].gameScores).isEqualTo(listOf(180, 200, 220))
    }
}
