package com.bowlingclub.app.data.repository

import com.bowlingclub.app.data.local.dao.GameScoreDao
import com.bowlingclub.app.data.local.entity.GameScore
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepository @Inject constructor(
    private val gameScoreDao: GameScoreDao
) {
    fun getScoresByTournament(tournamentId: Int): Flow<List<GameScore>> {
        return gameScoreDao.getScoresByTournament(tournamentId)
    }

    fun getScoresByMember(memberId: Int): Flow<List<GameScore>> {
        return gameScoreDao.getScoresByMember(memberId)
    }

    fun getScoresByTournamentAndMember(
        tournamentId: Int,
        memberId: Int
    ): Flow<List<GameScore>> {
        return gameScoreDao.getScoresByTournamentAndMember(tournamentId, memberId)
    }

    suspend fun saveScores(scores: List<GameScore>) {
        val now = LocalDateTime.now()
        val updatedScores = scores.map { score ->
            score.copy(
                createdAt = if (score.id == 0) now else score.createdAt,
                updatedAt = now
            )
        }
        gameScoreDao.insertAll(updatedScores)
    }

    suspend fun deleteScoresByTournament(tournamentId: Int) {
        gameScoreDao.deleteByTournament(tournamentId)
    }

    fun getAverageScore(memberId: Int): Flow<Double?> {
        return gameScoreDao.getAverageScore(memberId)
    }

    fun getHighScore(memberId: Int): Flow<Int?> {
        return gameScoreDao.getHighScore(memberId)
    }

    fun getTotalGamesPlayed(memberId: Int): Flow<Int> {
        return gameScoreDao.getTotalGamesPlayed(memberId)
    }
}
