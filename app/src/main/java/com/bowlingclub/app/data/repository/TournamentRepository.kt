package com.bowlingclub.app.data.repository

import com.bowlingclub.app.data.local.dao.TournamentDao
import com.bowlingclub.app.data.local.dao.TournamentParticipantDao
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.local.entity.TournamentParticipant
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TournamentRepository @Inject constructor(
    private val tournamentDao: TournamentDao,
    private val participantDao: TournamentParticipantDao
) {
    fun getAllTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getAllTournaments()
    }

    fun getTournamentById(id: Int): Flow<Tournament?> {
        return tournamentDao.getTournamentById(id)
    }

    fun getUpcomingTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getUpcomingTournaments()
    }

    fun getCompletedTournaments(): Flow<List<Tournament>> {
        return tournamentDao.getCompletedTournaments()
    }

    suspend fun insertTournament(tournament: Tournament): Long {
        val now = LocalDateTime.now()
        val tournamentWithTimestamps = tournament.copy(
            createdAt = now,
            updatedAt = now
        )
        return tournamentDao.insert(tournamentWithTimestamps)
    }

    suspend fun updateTournament(tournament: Tournament) {
        val updatedTournament = tournament.copy(
            updatedAt = LocalDateTime.now()
        )
        tournamentDao.update(updatedTournament)
    }

    suspend fun deleteTournament(tournament: Tournament) {
        tournamentDao.delete(tournament)
    }

    suspend fun updateStatus(tournamentId: Int, status: String) {
        validateStatusTransition(status)
        tournamentDao.updateStatus(
            id = tournamentId,
            status = status,
            updatedAt = LocalDateTime.now()
        )
    }

    fun getParticipantsByTournament(tournamentId: Int): Flow<List<TournamentParticipant>> {
        return participantDao.getParticipantsByTournament(tournamentId)
    }

    suspend fun addParticipants(tournamentId: Int, memberIds: List<Int>, handicapMap: Map<Int, Int> = emptyMap()) {
        val now = LocalDateTime.now()
        val participants = memberIds.map { memberId ->
            TournamentParticipant(
                tournamentId = tournamentId,
                memberId = memberId,
                handicap = handicapMap[memberId] ?: 0,
                joinedAt = now
            )
        }
        participantDao.insertAll(participants)
    }

    suspend fun removeAllParticipants(tournamentId: Int) {
        participantDao.deleteByTournament(tournamentId)
    }

    fun getParticipantCount(tournamentId: Int): Flow<Int> {
        return participantDao.getParticipantCount(tournamentId)
    }

    private fun validateStatusTransition(status: String) {
        val validStatuses = listOf("SCHEDULED", "IN_PROGRESS", "COMPLETED")
        require(status in validStatuses) {
            "Invalid status: $status. Must be one of $validStatuses"
        }
    }
}
