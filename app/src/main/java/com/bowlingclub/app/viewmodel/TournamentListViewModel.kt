package com.bowlingclub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TournamentListUiState(
    val tournaments: List<Tournament> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterStatus: String = "ALL"
)

@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentListUiState())
    val uiState: StateFlow<TournamentListUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        loadTournaments()
    }

    fun loadTournaments(): Unit {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val tournamentFlow = when (_uiState.value.filterStatus) {
                "SCHEDULED" -> tournamentRepository.getUpcomingTournaments()
                "COMPLETED" -> tournamentRepository.getCompletedTournaments()
                else -> tournamentRepository.getAllTournaments()
            }

            tournamentFlow
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "알 수 없는 오류가 발생했습니다"
                        )
                    }
                }
                .collect { tournaments ->
                    _uiState.update {
                        it.copy(
                            tournaments = tournaments,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun filterByStatus(status: String): Unit {
        _uiState.update { it.copy(filterStatus = status) }
        loadTournaments()
    }
}
