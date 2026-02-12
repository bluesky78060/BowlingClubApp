package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.TeamMember
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members WHERE teamId = :teamId")
    fun getMembersByTeam(teamId: Int): Flow<List<TeamMember>>

    @Query("SELECT * FROM team_members WHERE teamId IN (SELECT id FROM teams WHERE tournamentId = :tournamentId)")
    fun getMembersByTournament(tournamentId: Int): Flow<List<TeamMember>>

    @Query("SELECT * FROM team_members")
    fun getAllTeamMembers(): Flow<List<TeamMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teamMember: TeamMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teamMembers: List<TeamMember>)

    @Delete
    suspend fun delete(teamMember: TeamMember)

    @Query("DELETE FROM team_members WHERE teamId = :teamId")
    suspend fun deleteByTeam(teamId: Int)

    @Query("DELETE FROM team_members WHERE teamId IN (SELECT id FROM teams WHERE tournamentId = :tournamentId)")
    suspend fun deleteByTournament(tournamentId: Int)

    @Query("DELETE FROM team_members")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM team_members WHERE teamId = :teamId")
    fun getMemberCount(teamId: Int): Flow<Int>
}
