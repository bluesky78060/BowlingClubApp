package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.Tournament
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY date DESC")
    fun getAllTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    fun getTournamentById(id: Int): Flow<Tournament?>

    @Query("SELECT * FROM tournaments WHERE status = 'SCHEDULED' ORDER BY date ASC")
    fun getUpcomingTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE status = 'COMPLETED' ORDER BY date DESC")
    fun getCompletedTournaments(): Flow<List<Tournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tournament: Tournament): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tournaments: List<Tournament>)

    @Update
    suspend fun update(tournament: Tournament)

    @Delete
    suspend fun delete(tournament: Tournament)

    @Query("UPDATE tournaments SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, updatedAt: LocalDateTime)

    @Query("DELETE FROM tournaments")
    suspend fun deleteAll()
}
