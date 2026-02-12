package com.bowlingclub.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParticipantCheckUiState(
    val allMembers: List<Member> = emptyList(),
    val selectedMemberIds: Set<Int> = emptySet(),
    val handicapMap: Map<Int, Int> = emptyMap(),
    val tournamentHandicapEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class ParticipantCheckViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tournamentId: Int = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(ParticipantCheckUiState())
    val uiState: StateFlow<ParticipantCheckUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null
    private var participantCollectJob: Job? = null

    init {
        loadTournament()
        loadData()
    }

    private fun loadTournament(): Unit {
        viewModelScope.launch {
            tournamentRepository.getTournamentById(tournamentId)
                .catch { }
                .collect { tournament ->
                    _uiState.update { it.copy(tournamentHandicapEnabled = tournament?.handicapEnabled ?: false) }
                }
        }
    }

    fun loadData(): Unit {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                memberRepository.getActiveMembers()
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "회원 조회 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { members ->
                        _uiState.update { it.copy(allMembers = members) }
                        loadExistingParticipants()
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

    private fun loadExistingParticipants(): Unit {
        participantCollectJob?.cancel()
        participantCollectJob = viewModelScope.launch {
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
                        val selectedIds = participants.map { it.memberId }.toSet()
                        val handicaps = participants.associate { it.memberId to (it.handicap ?: 0) }
                        _uiState.update {
                            it.copy(
                                selectedMemberIds = selectedIds,
                                handicapMap = handicaps,
                                isLoading = false
                            )
                        }
                    }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "참가자 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun toggleMember(memberId: Int): Unit {
        val currentIds = _uiState.value.selectedMemberIds
        val newIds = if (currentIds.contains(memberId)) {
            currentIds - memberId
        } else {
            currentIds + memberId
        }
        _uiState.update { it.copy(selectedMemberIds = newIds) }
    }

    fun selectAll(): Unit {
        val allIds = _uiState.value.allMembers.map { it.id }.toSet()
        _uiState.update { it.copy(selectedMemberIds = allIds) }
    }

    fun deselectAll(): Unit {
        _uiState.update { it.copy(selectedMemberIds = emptySet()) }
    }

    fun updateHandicap(memberId: Int, handicap: Int): Unit {
        val currentHandicaps = _uiState.value.handicapMap.toMutableMap()
        currentHandicaps[memberId] = handicap
        _uiState.update { it.copy(handicapMap = currentHandicaps) }
    }

    fun saveParticipants(): Unit {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Flow 수집 중지 - DB 변경 후 재수집이 isSaved 상태를 간섭하지 않도록
                collectJob?.cancel()
                participantCollectJob?.cancel()

                tournamentRepository.removeAllParticipants(tournamentId)

                val selectedIds = _uiState.value.selectedMemberIds.toList()
                val handicapMap = _uiState.value.handicapMap
                if (selectedIds.isNotEmpty()) {
                    tournamentRepository.addParticipants(tournamentId, selectedIds, handicapMap)
                }

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

    fun resetSaved() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
