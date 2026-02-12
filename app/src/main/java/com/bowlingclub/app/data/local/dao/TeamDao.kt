package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.Team
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId ORDER BY name ASC")
    fun getTeamsByTournament(tournamentId: Int): Flow<List<Team>>

    @Query("SELECT * FROM teams")
    fun getAllTeams(): Flow<List<Team>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: Team): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teams: List<Team>)

    @Update
    suspend fun update(team: Team)

    @Delete
    suspend fun delete(team: Team)

    @Transaction
    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun getTeamWithMembers(teamId: Int): Flow<Team?>

    @Query("DELETE FROM teams WHERE tournamentId = :tournamentId")
    suspend fun deleteByTournament(tournamentId: Int)

    @Query("DELETE FROM teams")
    suspend fun deleteAll()
}
