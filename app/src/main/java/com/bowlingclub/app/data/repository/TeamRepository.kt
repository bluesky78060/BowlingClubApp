package com.bowlingclub.app.data.repository

import androidx.room.withTransaction
import com.bowlingclub.app.data.local.BowlingClubDatabase
import com.bowlingclub.app.data.local.dao.TeamDao
import com.bowlingclub.app.data.local.dao.TeamMemberDao
import com.bowlingclub.app.data.local.entity.Team
import com.bowlingclub.app.data.local.entity.TeamMember
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val teamDao: TeamDao,
    private val teamMemberDao: TeamMemberDao,
    private val database: BowlingClubDatabase
) {
    fun getTeamsByTournament(tournamentId: Int): Flow<List<Team>> {
        return teamDao.getTeamsByTournament(tournamentId)
    }

    fun getTeamMembers(teamId: Int): Flow<List<TeamMember>> {
        return teamMemberDao.getMembersByTeam(teamId)
    }

    fun getAllTeamMembersByTournament(tournamentId: Int): Flow<List<TeamMember>> {
        return teamMemberDao.getMembersByTournament(tournamentId)
    }

    suspend fun createTeam(tournamentId: Int, name: String): Long {
        val now = LocalDateTime.now()
        return teamDao.insert(Team(tournamentId = tournamentId, name = name, createdAt = now, updatedAt = now))
    }

    suspend fun updateTeam(team: Team) {
        teamDao.update(team.copy(updatedAt = LocalDateTime.now()))
    }

    suspend fun deleteTeam(team: Team) {
        teamDao.delete(team)
    }

    suspend fun assignMembers(teamId: Int, memberIds: List<Int>) {
        teamMemberDao.deleteByTeam(teamId)
        val teamMembers = memberIds.map { TeamMember(teamId = teamId, memberId = it) }
        teamMemberDao.insertAll(teamMembers)
    }

    suspend fun saveTeamsWithMembers(tournamentId: Int, teams: Map<String, List<Int>>) {
        database.withTransaction {
            // Delete existing teams for this tournament
            teamDao.deleteByTournament(tournamentId)

            // Insert new teams with members
            val now = LocalDateTime.now()
            teams.forEach { (teamName, memberIds) ->
                val teamId = teamDao.insert(Team(tournamentId = tournamentId, name = teamName, createdAt = now, updatedAt = now))
                val teamMembers = memberIds.map { TeamMember(teamId = teamId.toInt(), memberId = it) }
                teamMemberDao.insertAll(teamMembers)
            }
        }
    }

    suspend fun clearTeams(tournamentId: Int) {
        teamDao.deleteByTournament(tournamentId)
    }
}
