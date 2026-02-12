package com.bowlingclub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.dao.SettingDao
import com.bowlingclub.app.data.local.entity.Tournament
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

data class HomeUiState(
    val clubName: String = "볼링클럽",
    val upcomingTournaments: List<Tournament> = emptyList(),
    val recentCompletedTournaments: List<Tournament> = emptyList(),
    val activeMemberCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
    private val memberRepository: MemberRepository,
    private val settingDao: SettingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        loadClubName()
        loadDashboard()
    }

    private fun loadClubName() {
        viewModelScope.launch {
            settingDao.getSetting(SettingsViewModel.KEY_CLUB_NAME)
                .collect { setting ->
                    _uiState.update { it.copy(clubName = setting?.value ?: "볼링클럽") }
                }
        }
    }

    fun loadDashboard(): Unit {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                launch {
                    tournamentRepository.getUpcomingTournaments()
                        .catch { exception ->
                            _uiState.update {
                                it.copy(
                                    error = exception.message ?: "예정 대회 조회 중 오류가 발생했습니다"
                                )
                            }
                        }
                        .collect { tournaments ->
                            _uiState.update {
                                it.copy(upcomingTournaments = tournaments)
                            }
                        }
                }

                launch {
                    tournamentRepository.getCompletedTournaments()
                        .catch { exception ->
                            _uiState.update {
                                it.copy(
                                    error = exception.message ?: "완료 대회 조회 중 오류가 발생했습니다"
                                )
                            }
                        }
                        .collect { tournaments ->
                            _uiState.update {
                                it.copy(
                                    recentCompletedTournaments = tournaments.take(5)
                                )
                            }
                        }
                }

                launch {
                    memberRepository.getActiveMemberCount()
                        .catch { exception ->
                            _uiState.update {
                                it.copy(
                                    error = exception.message ?: "회원 수 조회 중 오류가 발생했습니다"
                                )
                            }
                        }
                        .collect { count ->
                            _uiState.update {
                                it.copy(activeMemberCount = count)
                            }
                        }
                }

                // All flows launched - set loading to false
                // Flows are reactive and will update UI state as data arrives
                _uiState.update { it.copy(isLoading = false) }
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
}
