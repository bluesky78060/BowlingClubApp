package com.bowlingclub.app.util

import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.model.RankingResult

object RankingCalculator {

    /**
     * 게임 스코어 리스트에서 순위를 계산합니다.
     *
     * 정렬 기준:
     * 1. 최종 합계 (finalTotal) 내림차순
     * 2. 하이 게임 내림차순
     * 3. 마지막 게임 점수 내림차순
     *
     * @param scores 모든 게임 스코어 리스트
     * @param members 멤버 ID 맵핑 (memberId -> memberName)
     * @return 순위별 RankingResult 리스트
     */
    fun calculateRanking(
        scores: List<GameScore>,
        members: Map<Int, String>
    ): List<RankingResult> {
        // memberId별로 점수 그룹핑
        val groupedByMember = scores.groupBy { it.memberId }

        // 각 멤버별 RankingResult 생성
        val rankingResults = groupedByMember.map { (memberId, memberScores) ->
            val memberName = members[memberId] ?: "Unknown"
            val gameScores = memberScores
                .sortedBy { it.gameNumber }
                .map { it.score }
            val totalScore = gameScores.sum()
            val handicapTotal = memberScores
                .sortedBy { it.gameNumber }
                .sumOf { it.handicapScore }
            val finalTotal = totalScore + handicapTotal
            val highGame = gameScores.maxOrNull() ?: 0
            val average = if (gameScores.isNotEmpty() && gameScores.size > 0) {
                totalScore.toDouble() / gameScores.size
            } else {
                0.0
            }

            RankingResult(
                rank = 0, // 임시값, 나중에 설정
                memberId = memberId,
                memberName = memberName,
                gameScores = gameScores,
                totalScore = totalScore,
                handicapTotal = handicapTotal,
                finalTotal = finalTotal,
                highGame = highGame,
                average = average
            )
        }

        // finalTotal > highGame > 마지막 게임 순서로 정렬
        val sortedResults = rankingResults.sortedWith(compareBy(
            { -it.finalTotal }, // 최종 합계 내림차순
            { -it.highGame },   // 하이 게임 내림차순
            { -(it.gameScores.lastOrNull() ?: 0) } // 마지막 게임 내림차순
        ))

        // 순위 부여 (동점자는 같은 순위)
        var currentRank = 1
        return sortedResults.mapIndexed { index, result ->
            // 동점 처리: 이전 점수와 동일하면 같은 순위
            if (index > 0) {
                val prevResult = sortedResults[index - 1]
                if (result.finalTotal == prevResult.finalTotal &&
                    result.highGame == prevResult.highGame &&
                    result.gameScores.lastOrNull() == prevResult.gameScores.lastOrNull()
                ) {
                    // 동점자이므로 rank 유지
                } else {
                    currentRank = index + 1
                }
            }
            result.copy(rank = currentRank)
        }
    }

    /**
     * 주어진 게임 스코어 리스트에서 하이 게임을 찾습니다.
     *
     * @param scores 게임 스코어 리스트
     * @return 가장 높은 점수의 GameScore, 없으면 null
     */
    fun calculateHighGame(scores: List<GameScore>): GameScore? {
        return scores.maxByOrNull { it.score }
    }

    /**
     * 주어진 게임 스코어 리스트에서 하이 시리즈(최고 합계)를 찾습니다.
     *
     * @param scores 게임 스코어 리스트
     * @param members 멤버 ID 맵핑
     * @return 하이 시리즈의 RankingResult, 없으면 null
     */
    fun calculateHighSeries(
        scores: List<GameScore>,
        members: Map<Int, String>
    ): RankingResult? {
        return calculateRanking(scores, members).firstOrNull()
    }
}
