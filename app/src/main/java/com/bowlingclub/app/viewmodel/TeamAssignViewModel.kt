package com.bowlingclub.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.ScoreRepository
import com.bowlingclub.app.data.repository.TeamRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import com.bowlingclub.app.util.AutoTeamAssigner
import com.bowlingclub.app.util.TeamAssignment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

data class TeamAssignUiState(
    val tournamentId: Int = 0,
    val availableMembers: Map<Int, String> = emptyMap(),  // memberId -> name
    val memberAverages: Map<Int, Double> = emptyMap(),
    val teamCount: Int = 2,
    val teams: List<TeamAssignment> = emptyList(),  // current team assignments
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val assignMethod: AssignMethod = AssignMethod.SNAKE_DRAFT
)

enum class AssignMethod {
    SNAKE_DRAFT,
    RANDOM,
    MANUAL
}

@HiltViewModel
class TeamAssignViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val memberRepository: MemberRepository,
    private val scoreRepository: ScoreRepository,
    private val teamRepository: TeamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tournamentId: Int = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(TeamAssignUiState(tournamentId = tournamentId))
    val uiState: StateFlow<TeamAssignUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadParticipants()
    }

    fun loadParticipants() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Get tournament participants
                val participants = tournamentRepository.getParticipantsByTournament(tournamentId)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "참가자 조회 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .firstOrNull() ?: emptyList()

                // Get member names and averages
                val memberMap = mutableMapOf<Int, String>()
                val averageMap = mutableMapOf<Int, Double>()

                for (participant in participants) {
                    val member = memberRepository.getMemberById(participant.memberId).firstOrNull()
                    member?.let {
                        memberMap[it.id] = it.name
                        val average = scoreRepository.getAverageScore(it.id).firstOrNull()
                        averageMap[it.id] = average ?: 0.0
                    }
                }

                // Load existing teams if any
                val existingTeams = teamRepository.getTeamsByTournament(tournamentId).firstOrNull() ?: emptyList()
                val teamAssignments = if (existingTeams.isNotEmpty()) {
                    existingTeams.map { team ->
                        val teamMembers = teamRepository.getTeamMembers(team.id).firstOrNull() ?: emptyList()
                        TeamAssignment(
                            teamName = team.name,
                            memberIds = teamMembers.map { it.memberId }
                        )
                    }
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        availableMembers = memberMap,
                        memberAverages = averageMap,
                        teams = teamAssignments,
                        isLoading = false
                    )
                }

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "데이터 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun updateTeamCount(count: Int) {
        if (count in 2..8) {
            _uiState.update { it.copy(teamCount = count) }
        }
    }

    fun updateAssignMethod(method: AssignMethod) {
        _uiState.update { it.copy(assignMethod = method) }
    }

    fun autoAssign() {
        val state = _uiState.value
        if (state.availableMembers.isEmpty()) {
            _uiState.update { it.copy(error = "참가자가 없습니다") }
            return
        }

        try {
            val teamAssignments = when (state.assignMethod) {
                AssignMethod.SNAKE_DRAFT -> {
                    AutoTeamAssigner.assignBySnakeDraft(
                        memberAverages = state.memberAverages,
                        teamCount = state.teamCount,
                        teamNames = null
                    )
                }
                AssignMethod.RANDOM -> {
                    AutoTeamAssigner.assignRandomly(
                        memberIds = state.availableMembers.keys.toList(),
                        teamCount = state.teamCount,
                        teamNames = null
                    )
                }
                AssignMethod.MANUAL -> {
                    // For manual mode, initialize empty teams
                    (1..state.teamCount).map { index ->
                        TeamAssignment(
                            teamName = "팀 $index",
                            memberIds = emptyList()
                        )
                    }
                }
            }

            _uiState.update { it.copy(teams = teamAssignments) }

        } catch (exception: Exception) {
            _uiState.update {
                it.copy(error = exception.message ?: "팀 편성 중 오류가 발생했습니다")
            }
        }
    }

    fun movePlayer(memberId: Int, toTeamIndex: Int) {
        val state = _uiState.value
        if (toTeamIndex !in state.teams.indices) return

        // Remove player from current team
        val updatedTeams = state.teams.map { team ->
            team.copy(memberIds = team.memberIds.filter { it != memberId })
        }.toMutableList()

        // Add player to new team
        val targetTeam = updatedTeams[toTeamIndex]
        updatedTeams[toTeamIndex] = targetTeam.copy(
            memberIds = targetTeam.memberIds + memberId
        )

        _uiState.update { it.copy(teams = updatedTeams) }
    }

    fun removePlayerFromTeam(memberId: Int) {
        val state = _uiState.value
        val updatedTeams = state.teams.map { team ->
            team.copy(memberIds = team.memberIds.filter { it != memberId })
        }
        _uiState.update { it.copy(teams = updatedTeams) }
    }

    fun getUnassignedMembers(): List<Int> {
        val state = _uiState.value
        val assignedMemberIds = state.teams.flatMap { it.memberIds }.toSet()
        return state.availableMembers.keys.filter { it !in assignedMemberIds }
    }

    fun saveTeams() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val state = _uiState.value

                // Convert TeamAssignment list to Map<String, List<Int>>
                val teamsMap = state.teams.associate { team ->
                    team.teamName to team.memberIds
                }

                // Save to repository
                teamRepository.saveTeamsWithMembers(tournamentId, teamsMap)

                _uiState.update {
                    it.copy(isLoading = false, isSaved = true)
                }

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "저장 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
