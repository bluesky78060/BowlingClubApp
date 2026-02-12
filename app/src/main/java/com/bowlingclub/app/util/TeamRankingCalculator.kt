package com.bowlingclub.app.util

import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.local.entity.Team
import com.bowlingclub.app.data.local.entity.TeamMember
import com.bowlingclub.app.data.model.RankingResult

data class TeamRankingResult(
    val rank: Int,
    val team: Team,
    val memberRankings: List<RankingResult>,
    val teamTotalScore: Int,
    val teamHandicapTotal: Int,
    val teamFinalTotal: Int,
    val teamHighGame: Int,
    val teamAverage: Double
)

object TeamRankingCalculator {

    /**
     * Calculate team rankings based on team members' scores.
     */
    fun calculateTeamRanking(
        teams: List<Team>,
        teamMembers: List<TeamMember>,
        scores: List<GameScore>,
        memberNames: Map<Int, String>
    ): List<TeamRankingResult> {
        val teamResults = teams.map { team ->
            val teamMemberIds = teamMembers
                .filter { it.teamId == team.id }
                .map { it.memberId }
                .toSet()

            val teamScores = scores.filter { it.memberId in teamMemberIds }
            val memberRankings = RankingCalculator.calculateRanking(teamScores, memberNames)

            val teamTotalScore = memberRankings.sumOf { it.totalScore }
            val teamHandicapTotal = memberRankings.sumOf { it.handicapTotal }
            val teamFinalTotal = memberRankings.sumOf { it.finalTotal }
            val teamHighGame = memberRankings.maxOfOrNull { it.highGame } ?: 0
            val memberCount = memberRankings.size
            val teamAverage = if (memberRankings.isNotEmpty() && memberCount > 0) {
                teamFinalTotal.toDouble() / memberCount
            } else {
                0.0
            }

            TeamRankingResult(
                rank = 0,
                team = team,
                memberRankings = memberRankings,
                teamTotalScore = teamTotalScore,
                teamHandicapTotal = teamHandicapTotal,
                teamFinalTotal = teamFinalTotal,
                teamHighGame = teamHighGame,
                teamAverage = teamAverage
            )
        }

        // Sort by teamFinalTotal descending, then by teamHighGame
        val sorted = teamResults.sortedWith(compareBy(
            { -it.teamFinalTotal },
            { -it.teamHighGame }
        ))

        // Assign ranks (handle ties)
        var currentRank = 1
        return sorted.mapIndexed { index, result ->
            if (index > 0) {
                val prev = sorted[index - 1]
                if (result.teamFinalTotal != prev.teamFinalTotal || result.teamHighGame != prev.teamHighGame) {
                    currentRank = index + 1
                }
            }
            result.copy(rank = currentRank)
        }
    }
}
