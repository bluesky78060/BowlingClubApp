package com.bowlingclub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.model.ClubStats
import com.bowlingclub.app.data.model.MemberRankingItem
import com.bowlingclub.app.data.model.PersonalStats
import com.bowlingclub.app.data.model.ScoreDistributionItem
import com.bowlingclub.app.data.model.ScoreTrendItem
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 간단한 회원 요약 (드롭다운용)
data class MemberSummary(
    val id: Int,
    val name: String
)

// UiState for 개인 통계 탭
data class PersonalStatsUiState(
    val selectedMemberId: Int? = null,
    val memberList: List<MemberSummary> = emptyList(),
    val personalStats: PersonalStats? = null,
    val scoreTrend: List<ScoreTrendItem> = emptyList(),
    val scoreDistribution: List<ScoreDistributionItem> = emptyList(),
    val recentScores: List<GameScore> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// UiState for 클럽 통계 탭
data class ClubStatsUiState(
    val clubStats: ClubStats? = null,
    val memberRankings: List<MemberRankingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _personalState = MutableStateFlow(PersonalStatsUiState())
    val personalState: StateFlow<PersonalStatsUiState> = _personalState.asStateFlow()

    private val _clubState = MutableStateFlow(ClubStatsUiState())
    val clubState: StateFlow<ClubStatsUiState> = _clubState.asStateFlow()

    private var personalJob: Job? = null
    private var clubJob: Job? = null
    private var memberListJob: Job? = null

    init {
        loadMemberList()
        loadClubStats()
    }

    fun loadMemberList(): Unit {
        memberListJob?.cancel()
        memberListJob = viewModelScope.launch {
            try {
                memberRepository.getActiveMembers()
                    .catch { exception ->
                        _personalState.update {
                            it.copy(
                                error = exception.message ?: "회원 목록 조회 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { members ->
                        val memberSummaryList = members.map { member ->
                            MemberSummary(id = member.id, name = member.name)
                        }
                        _personalState.update {
                            it.copy(memberList = memberSummaryList)
                        }
                    }
            } catch (exception: Exception) {
                _personalState.update {
                    it.copy(
                        error = exception.message ?: "회원 목록 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun selectMember(memberId: Int): Unit {
        personalJob?.cancel()
        personalJob = viewModelScope.launch {
            _personalState.update {
                it.copy(
                    selectedMemberId = memberId,
                    isLoading = true,
                    error = null
                )
            }

            try {
                // 개인 통계 데이터를 병렬로 로드
                launch {
                    statisticsRepository.getPersonalStats(memberId)
                        .catch { exception ->
                            _personalState.update {
                                it.copy(
                                    error = exception.message ?: "개인 통계 조회 중 오류가 발생했습니다",
                                    isLoading = false
                                )
                            }
                        }
                        .collect { stats ->
                            _personalState.update {
                                it.copy(personalStats = stats, isLoading = false)
                            }
                        }
                }

                launch {
                    statisticsRepository.getScoreTrend(memberId)
                        .catch { exception ->
                            _personalState.update {
                                it.copy(
                                    error = exception.message ?: "점수 추이 조회 중 오류가 발생했습니다",
                                    isLoading = false
                                )
                            }
                        }
                        .collect { trend ->
                            _personalState.update {
                                it.copy(scoreTrend = trend, isLoading = false)
                            }
                        }
                }

                launch {
                    statisticsRepository.getScoreDistribution(memberId)
                        .catch { exception ->
                            _personalState.update {
                                it.copy(
                                    error = exception.message ?: "점수 분포 조회 중 오류가 발생했습니다",
                                    isLoading = false
                                )
                            }
                        }
                        .collect { distribution ->
                            _personalState.update {
                                it.copy(scoreDistribution = distribution, isLoading = false)
                            }
                        }
                }

                launch {
                    statisticsRepository.getRecentScores(memberId)
                        .catch { exception ->
                            _personalState.update {
                                it.copy(
                                    error = exception.message ?: "최근 점수 조회 중 오류가 발생했습니다"
                                )
                            }
                        }
                        .collect { scores ->
                            _personalState.update {
                                it.copy(
                                    recentScores = scores,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (exception: Exception) {
                _personalState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "개인 통계 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun loadClubStats(): Unit {
        clubJob?.cancel()
        clubJob = viewModelScope.launch {
            _clubState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                launch {
                    statisticsRepository.getClubStats()
                        .catch { exception ->
                            _clubState.update {
                                it.copy(
                                    error = exception.message ?: "클럽 통계 조회 중 오류가 발생했습니다",
                                    isLoading = false
                                )
                            }
                        }
                        .collect { stats ->
                            _clubState.update {
                                it.copy(clubStats = stats, isLoading = false)
                            }
                        }
                }

                launch {
                    statisticsRepository.getMemberRankings()
                        .catch { exception ->
                            _clubState.update {
                                it.copy(
                                    error = exception.message ?: "회원 랭킹 조회 중 오류가 발생했습니다"
                                )
                            }
                        }
                        .collect { rankings ->
                            _clubState.update {
                                it.copy(
                                    memberRankings = rankings,
                                    isLoading = false
                                )
                            }
                        }
                }
            } catch (exception: Exception) {
                _clubState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "클럽 통계 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun refreshStats(): Unit {
        loadMemberList()
        loadClubStats()
        _personalState.value.selectedMemberId?.let { memberId ->
            selectMember(memberId)
        }
    }
}
