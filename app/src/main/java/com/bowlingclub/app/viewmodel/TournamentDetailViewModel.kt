package com.bowlingclub.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.local.entity.TournamentParticipant
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.ScoreRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TournamentDetailUiState(
    val tournament: Tournament? = null,
    val participants: List<TournamentParticipant> = emptyList(),
    val participantMembers: Map<Int, Member> = emptyMap(),
    val scores: List<GameScore> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val handicapEnabled: Boolean = false
)

@HiltViewModel
class TournamentDetailViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val scoreRepository: ScoreRepository,
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tournamentId: Int = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(TournamentDetailUiState())
    val uiState: StateFlow<TournamentDetailUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        loadTournament()
    }

    fun loadTournament(): Unit {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                tournamentRepository.getTournamentById(tournamentId)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "대회 조회 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { tournament ->
                        _uiState.update {
                            it.copy(
                                tournament = tournament,
                                handicapEnabled = tournament?.handicapEnabled ?: false
                            )
                        }
                        loadParticipantsWithMembers()
                        loadScores()
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

    fun loadParticipantsWithMembers(): Unit {
        viewModelScope.launch {
            try {
                tournamentRepository.getParticipantsByTournament(tournamentId)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                error = exception.message ?: "참가자 조회 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { participants ->
                        _uiState.update { it.copy(participants = participants) }

                        val memberMap = mutableMapOf<Int, Member>()
                        participants.forEach { participant ->
                            val member = memberRepository.getMemberById(participant.memberId)
                                .firstOrNull()
                            member?.let { memberMap[it.id] = it }
                        }
                        _uiState.update {
                            it.copy(
                                participantMembers = memberMap,
                                isLoading = false
                            )
                        }
                    }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "참가자 정보 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    private fun loadScores(): Unit {
        viewModelScope.launch {
            try {
                scoreRepository.getScoresByTournament(tournamentId)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                error = exception.message ?: "점수 조회 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { scores ->
                        _uiState.update { it.copy(scores = scores) }
                    }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        error = exception.message ?: "점수 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun deleteTournament(): Unit {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val tournament = _uiState.value.tournament
                if (tournament != null) {
                    tournamentRepository.deleteTournament(tournament)
                    _uiState.update {
                        it.copy(isLoading = false, isDeleted = true)
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "삭제 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun updateStatus(newStatus: String): Unit {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                tournamentRepository.updateStatus(tournamentId, newStatus)

                _uiState.update { it.copy(isLoading = false) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "상태 변경 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }
}
