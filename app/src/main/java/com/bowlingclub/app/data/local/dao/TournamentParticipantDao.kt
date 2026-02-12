package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.TournamentParticipant
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentParticipantDao {
    @Query("SELECT * FROM tournament_participants WHERE tournamentId = :tournamentId")
    fun getParticipantsByTournament(tournamentId: Int): Flow<List<TournamentParticipant>>

    @Query("SELECT * FROM tournament_participants")
    fun getAllParticipants(): Flow<List<TournamentParticipant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(participant: TournamentParticipant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(participants: List<TournamentParticipant>)

    @Query("DELETE FROM tournament_participants WHERE tournamentId = :tournamentId")
    suspend fun deleteByTournament(tournamentId: Int)

    @Query("DELETE FROM tournament_participants")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM tournament_participants WHERE tournamentId = :tournamentId")
    fun getParticipantCount(tournamentId: Int): Flow<Int>

    @Query("UPDATE tournament_participants SET handicap = :handicap WHERE tournamentId = :tournamentId AND memberId = :memberId")
    suspend fun updateHandicap(tournamentId: Int, memberId: Int, handicap: Int)
}
