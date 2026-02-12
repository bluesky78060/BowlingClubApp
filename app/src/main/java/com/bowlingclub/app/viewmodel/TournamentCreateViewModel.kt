package com.bowlingclub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TournamentCreateUiState(
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val location: String = "",
    val gameCount: Int = 3,
    val isTeamGame: Boolean = false,
    val handicapEnabled: Boolean = false,
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val savedTournamentId: Long? = null
)

@HiltViewModel
class TournamentCreateViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TournamentCreateUiState())
    val uiState: StateFlow<TournamentCreateUiState> = _uiState.asStateFlow()

    fun updateName(name: String): Unit {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDate(date: LocalDate): Unit {
        _uiState.update { it.copy(date = date) }
    }

    fun updateLocation(location: String): Unit {
        _uiState.update { it.copy(location = location) }
    }

    fun updateGameCount(count: Int): Unit {
        _uiState.update { it.copy(gameCount = count) }
    }

    fun updateIsTeamGame(isTeamGame: Boolean): Unit {
        _uiState.update { it.copy(isTeamGame = isTeamGame) }
    }

    fun updateHandicapEnabled(enabled: Boolean): Unit {
        _uiState.update { it.copy(handicapEnabled = enabled) }
    }

    fun updateDescription(description: String): Unit {
        _uiState.update { it.copy(description = description) }
    }

    fun resetSaved() {
        _uiState.update { it.copy(isSaved = false, savedTournamentId = null) }
    }

    fun saveTournament(): Unit {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val currentState = _uiState.value

                if (currentState.name.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "대회명을 입력해주세요"
                        )
                    }
                    return@launch
                }

                if (currentState.gameCount <= 0) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "게임 수는 1 이상이어야 합니다"
                        )
                    }
                    return@launch
                }

                val tournament = Tournament(
                    name = currentState.name.trim(),
                    date = currentState.date,
                    location = currentState.location.trim().ifBlank { null },
                    gameCount = currentState.gameCount,
                    isTeamGame = currentState.isTeamGame,
                    handicapEnabled = currentState.handicapEnabled,
                    description = currentState.description.trim().ifBlank { null }
                )

                val tournamentId = tournamentRepository.insertTournament(tournament)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSaved = true,
                        savedTournamentId = tournamentId
                    )
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
}
