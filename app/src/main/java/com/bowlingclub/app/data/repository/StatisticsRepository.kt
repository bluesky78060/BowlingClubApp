package com.bowlingclub.app.data.repository

import com.bowlingclub.app.data.local.dao.GameScoreDao
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.model.ClubStats
import com.bowlingclub.app.data.model.MemberRankingItem
import com.bowlingclub.app.data.model.PersonalStats
import com.bowlingclub.app.data.model.ScoreDistributionItem
import com.bowlingclub.app.data.model.ScoreTrendItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val gameScoreDao: GameScoreDao,
    private val memberRepository: MemberRepository,
    private val tournamentRepository: TournamentRepository
) {
    /**
     * Get personal statistics for a member
     */
    fun getPersonalStats(memberId: Int): Flow<PersonalStats> {
        return combine(
            gameScoreDao.getAverageScore(memberId),
            gameScoreDao.getHighScore(memberId),
            gameScoreDao.getMinScore(memberId),
            gameScoreDao.getTotalGamesPlayed(memberId),
            gameScoreDao.getTournamentCount(memberId)
        ) { avg, high, low, total, tournaments ->
            PersonalStats(
                averageScore = avg ?: 0.0,
                highScore = high ?: 0,
                lowScore = low ?: 0,
                totalGames = total,
                tournamentCount = tournaments
            )
        }
    }

    /**
     * Get score trend for chart visualization
     */
    fun getScoreTrend(memberId: Int): Flow<List<ScoreTrendItem>> {
        return gameScoreDao.getScoresOrderByDate(memberId).map { scores ->
            scores.map { score ->
                ScoreTrendItem(
                    date = score.createdAt,
                    score = score.finalScore,
                    gameNumber = score.gameNumber,
                    tournamentId = score.tournamentId
                )
            }
        }
    }

    /**
     * Get recent game scores
     */
    fun getRecentScores(memberId: Int, limit: Int = 10): Flow<List<GameScore>> {
        return gameScoreDao.getRecentScores(memberId, limit)
    }

    /**
     * Get score distribution by ranges
     */
    fun getScoreDistribution(memberId: Int): Flow<List<ScoreDistributionItem>> {
        val ranges = listOf(
            "0-99" to (0 to 99),
            "100-129" to (100 to 129),
            "130-159" to (130 to 159),
            "160-189" to (160 to 189),
            "190-219" to (190 to 219),
            "220-249" to (220 to 249),
            "250-300" to (250 to 300)
        )
        val flows = ranges.map { (_, range) ->
            gameScoreDao.getScoreCountByRange(memberId, range.first, range.second)
        }
        return combine(flows) { values ->
            ranges.mapIndexed { index, (label, _) ->
                ScoreDistributionItem(label, values[index])
            }
        }
    }

    /**
     * Get club-wide statistics
     */
    fun getClubStats(): Flow<ClubStats> {
        return combine(
            gameScoreDao.getTotalGameCount(),
            gameScoreDao.getOverallAverageScore(),
            gameScoreDao.getOverallHighScore(),
            memberRepository.getActiveMembers(),
            tournamentRepository.getAllTournaments()
        ) { totalGames, overallAverage, highestScore, activeMembers, tournaments ->
            ClubStats(
                totalGames = totalGames,
                overallAverage = overallAverage ?: 0.0,
                highestScore = highestScore ?: 0,
                activeMemberCount = activeMembers.size,
                totalTournaments = tournaments.size
            )
        }
    }

    /**
     * Get member rankings ordered by average score
     */
    fun getMemberRankings(): Flow<List<MemberRankingItem>> {
        return combine(
            gameScoreDao.getAllScores(),
            memberRepository.getAllMembers()
        ) { allScores, allMembers ->
            val memberMap = allMembers.associateBy { it.id }

            allScores
                .groupBy { it.memberId }
                .map { (memberId, scores) ->
                    val member = memberMap[memberId]
                    MemberRankingItem(
                        memberId = memberId,
                        memberName = member?.name ?: "Unknown",
                        averageScore = scores.map { it.finalScore }.average(),
                        highScore = scores.maxOfOrNull { it.finalScore } ?: 0,
                        gamesPlayed = scores.size
                    )
                }
                .sortedByDescending { it.averageScore }
        }
    }
}
