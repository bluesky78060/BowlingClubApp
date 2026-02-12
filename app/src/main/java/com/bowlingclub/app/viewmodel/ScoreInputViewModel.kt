package com.bowlingclub.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.local.entity.Tournament
import com.bowlingclub.app.data.local.entity.TournamentParticipant
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.ScoreRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import com.bowlingclub.app.util.HandicapCalculator
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

data class ScoreInputUiState(
    val tournament: Tournament? = null,
    val participants: List<TournamentParticipant> = emptyList(),
    val memberNames: Map<Int, String> = emptyMap(),
    val scores: Map<Int, List<Int>> = emptyMap(),
    val gameCount: Int = 3,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class ScoreInputViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val tournamentRepository: TournamentRepository,
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tournamentId: Int = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(ScoreInputUiState())
    val uiState: StateFlow<ScoreInputUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        loadData()
    }

    fun loadData(): Unit {
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
                        if (tournament != null) {
                            _uiState.update {
                                it.copy(
                                    tournament = tournament,
                                    gameCount = tournament.gameCount
                                )
                            }
                            loadParticipants()
                        }
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

    private fun loadParticipants(): Unit {
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
                        loadMemberNames(participants)
                        loadExistingScores(participants)
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

    private fun loadMemberNames(
        participants: List<TournamentParticipant>
    ): Unit {
        viewModelScope.launch {
            try {
                val memberNameMap = mutableMapOf<Int, String>()
                participants.forEach { participant ->
                    val member = memberRepository.getMemberById(participant.memberId)
                        .firstOrNull()
                    member?.let { memberNameMap[it.id] = it.name }
                }
                _uiState.update { it.copy(memberNames = memberNameMap) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        error = exception.message ?: "회원 정보 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    private fun loadExistingScores(
        participants: List<TournamentParticipant>
    ): Unit {
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
                    .collect { existingScores ->
                        val scoreMap = mutableMapOf<Int, List<Int>>()
                        val gameCount = _uiState.value.gameCount

                        participants.forEach { participant ->
                            val memberScores = existingScores
                                .filter { it.memberId == participant.memberId }
                                .sortedBy { it.gameNumber }
                                .map { it.score }

                            scoreMap[participant.memberId] = if (memberScores.size == gameCount) {
                                memberScores
                            } else {
                                List(gameCount) { index ->
                                    memberScores.getOrElse(index) { 0 }
                                }
                            }
                        }

                        _uiState.update {
                            it.copy(scores = scoreMap, isLoading = false)
                        }
                    }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "점수 로드 중 오류가 발생했습니다"
                    )
                }
            }
        }
    }

    fun updateScore(memberId: Int, gameNumber: Int, score: Int): Unit {
        val clampedScore = score.coerceIn(0, 300)

        if (score != clampedScore) {
            _uiState.update {
                it.copy(error = "점수는 0에서 300 사이여야 합니다 (자동 보정됨)")
            }
        }

        val currentScores = _uiState.value.scores.toMutableMap()
        val memberScores = currentScores[memberId]?.toMutableList()
            ?: MutableList(_uiState.value.gameCount) { 0 }

        if (gameNumber in 1.._uiState.value.gameCount) {
            memberScores[gameNumber - 1] = clampedScore
            currentScores[memberId] = memberScores
            _uiState.update { it.copy(scores = currentScores, error = if (score == clampedScore) null else it.error) }
        }
    }

    fun saveAllScores(): Unit {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                scoreRepository.deleteScoresByTournament(tournamentId)

                val tournament = _uiState.value.tournament
                val allScores = mutableListOf<GameScore>()

                _uiState.value.scores.forEach { (memberId, scores) ->
                    // Get participant's manual handicap
                    val participant = _uiState.value.participants.find { it.memberId == memberId }
                    val manualHandicap = if (tournament?.handicapEnabled == true) {
                        participant?.handicap ?: 0
                    } else {
                        0
                    }

                    scores.forEachIndexed { index, score ->
                        val (handicapScore, finalScore) = if (manualHandicap > 0) {
                            HandicapCalculator.applyHandicap(score, manualHandicap)
                        } else {
                            Pair(0, score)
                        }

                        allScores.add(
                            GameScore(
                                tournamentId = tournamentId,
                                memberId = memberId,
                                gameNumber = index + 1,
                                score = score,
                                handicapScore = handicapScore,
                                finalScore = finalScore,
                                recordedBy = "MANUAL"
                            )
                        )
                    }
                }

                scoreRepository.saveScores(allScores)

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
}
