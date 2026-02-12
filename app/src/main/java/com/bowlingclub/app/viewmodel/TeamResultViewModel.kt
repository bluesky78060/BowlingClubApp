package com.bowlingclub.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.local.entity.Team
import com.bowlingclub.app.data.local.entity.TeamMember
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.ScoreRepository
import com.bowlingclub.app.data.repository.TeamRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import com.bowlingclub.app.util.TeamRankingCalculator
import com.bowlingclub.app.util.TeamRankingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

data class TeamResultUiState(
    val tournamentName: String = "",
    val teamRankings: List<TeamRankingResult> = emptyList(),
    val handicapEnabled: Boolean = false,
    val gameCount: Int = 3,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeamResultViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val teamRepository: TeamRepository,
    private val scoreRepository: ScoreRepository,
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tournamentId: Int = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(TeamResultUiState())
    val uiState: StateFlow<TeamResultUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTeamResults()
    }

    fun loadTeamResults(): Unit {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load tournament info
                val tournament: Tournament? = tournamentRepository.getTournamentById(tournamentId)
                    .firstOrNull()

                if (tournament == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "대회 정보를 찾을 수 없습니다"
                        )
                    }
                    return@launch
                }

                // Load teams
                val teams: List<Team> = teamRepository.getTeamsByTournament(tournamentId)
                    .firstOrNull() ?: emptyList()

                // Load team members
                val teamMembers: List<TeamMember> = teamRepository.getAllTeamMembersByTournament(tournamentId)
                    .firstOrNull() ?: emptyList()

                // Load all scores
                val scores: List<GameScore> = scoreRepository.getScoresByTournament(tournamentId)
                    .firstOrNull() ?: emptyList()

                // Build member names map
                val memberIds = teamMembers.map { it.memberId }.distinct()
                val memberNames = mutableMapOf<Int, String>()
                memberIds.forEach { memberId ->
                    val member = memberRepository.getMemberById(memberId).firstOrNull()
                    member?.let {
                        memberNames[memberId] = it.name
                    }
                }

                // Calculate team rankings
                val teamRankings = TeamRankingCalculator.calculateTeamRanking(
                    teams = teams,
                    teamMembers = teamMembers,
                    scores = scores,
                    memberNames = memberNames
                )

                // Determine game count from scores
                val maxGameNumber = scores.maxOfOrNull { it.gameNumber } ?: 3

                _uiState.update {
                    it.copy(
                        tournamentName = tournament.name,
                        teamRankings = teamRankings,
                        handicapEnabled = tournament.handicapEnabled,
                        gameCount = maxGameNumber,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "팀 결과 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }
}
