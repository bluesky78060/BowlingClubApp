package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.GameScore
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores WHERE tournamentId = :tournamentId ORDER BY gameNumber ASC, memberId ASC")
    fun getScoresByTournament(tournamentId: Int): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getScoresByMember(memberId: Int): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores WHERE tournamentId = :tournamentId AND memberId = :memberId ORDER BY gameNumber ASC")
    fun getScoresByTournamentAndMember(tournamentId: Int, memberId: Int): Flow<List<GameScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: GameScore)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scores: List<GameScore>)

    @Query("DELETE FROM game_scores WHERE tournamentId = :tournamentId")
    suspend fun deleteByTournament(tournamentId: Int)

    @Query("SELECT AVG(finalScore) FROM game_scores WHERE memberId = :memberId")
    fun getAverageScore(memberId: Int): Flow<Double?>

    @Query("SELECT MAX(finalScore) FROM game_scores WHERE memberId = :memberId")
    fun getHighScore(memberId: Int): Flow<Int?>

    @Query("SELECT COUNT(*) FROM game_scores WHERE memberId = :memberId")
    fun getTotalGamesPlayed(memberId: Int): Flow<Int>

    @Query("SELECT MIN(finalScore) FROM game_scores WHERE memberId = :memberId")
    fun getMinScore(memberId: Int): Flow<Int?>

    @Query("SELECT * FROM game_scores WHERE memberId = :memberId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentScores(memberId: Int, limit: Int): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores WHERE memberId = :memberId ORDER BY createdAt ASC")
    fun getScoresOrderByDate(memberId: Int): Flow<List<GameScore>>

    @Query("SELECT * FROM game_scores")
    fun getAllScores(): Flow<List<GameScore>>

    @Query("SELECT COUNT(*) FROM game_scores")
    fun getTotalGameCount(): Flow<Int>

    @Query("SELECT AVG(finalScore) FROM game_scores")
    fun getOverallAverageScore(): Flow<Double?>

    @Query("SELECT MAX(finalScore) FROM game_scores")
    fun getOverallHighScore(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM game_scores WHERE memberId = :memberId AND finalScore BETWEEN :minScore AND :maxScore")
    fun getScoreCountByRange(memberId: Int, minScore: Int, maxScore: Int): Flow<Int>

    @Query("SELECT COUNT(DISTINCT tournamentId) FROM game_scores WHERE memberId = :memberId")
    fun getTournamentCount(memberId: Int): Flow<Int>

    @Query("DELETE FROM game_scores")
    suspend fun deleteAll()
}
