package com.bowlingclub.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.remote.OcrParseResult
import com.bowlingclub.app.data.remote.OcrRepository
import com.bowlingclub.app.data.remote.OcrResultParser
import com.bowlingclub.app.data.repository.MemberRepository
import com.bowlingclub.app.data.repository.ScoreRepository
import com.bowlingclub.app.data.repository.TournamentRepository
import com.bowlingclub.app.util.HandicapCalculator
import com.bowlingclub.app.util.ImagePreprocessor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrUiState(
    val isProcessing: Boolean = false,
    val capturedImageUri: Uri? = null,
    val parseResult: OcrParseResult? = null,
    val editableScores: Map<String, List<Int>> = emptyMap(),  // playerName -> scores
    val memberMapping: Map<String, Int> = emptyMap(),  // playerName -> memberId
    val availableMembers: Map<Int, String> = emptyMap(),  // memberId -> memberName
    val tournamentId: Int = 0,
    val gameCount: Int = 3,
    val error: String? = null,
    val retryCount: Int = 0,
    val isSaved: Boolean = false,
    val ocrConfidence: Float = 0f
)

@HiltViewModel
class OcrViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val ocrRepository: OcrRepository,
    private val scoreRepository: ScoreRepository,
    private val tournamentRepository: TournamentRepository,
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tournamentId: Int = checkNotNull(savedStateHandle["tournamentId"])

    private val _uiState = MutableStateFlow(OcrUiState(tournamentId = tournamentId))
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    companion object {
        const val MAX_RETRY_COUNT = 3
    }

    init {
        loadTournamentInfo()
    }

    private fun loadTournamentInfo() {
        viewModelScope.launch {
            try {
                val tournament = tournamentRepository.getTournamentById(tournamentId).firstOrNull()
                if (tournament != null) {
                    _uiState.update { it.copy(gameCount = tournament.gameCount) }
                }

                // Load available members (participants)
                val participants = tournamentRepository.getParticipantsByTournament(tournamentId).firstOrNull() ?: emptyList()
                val memberMap = mutableMapOf<Int, String>()
                participants.forEach { participant ->
                    val member = memberRepository.getMemberById(participant.memberId).firstOrNull()
                    member?.let { memberMap[it.id] = it.name }
                }
                _uiState.update { it.copy(availableMembers = memberMap) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "대회 정보 로드 실패: ${e.message}") }
            }
        }
    }

    /**
     * Process captured image through OCR pipeline.
     */
    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    error = null,
                    capturedImageUri = uri,
                    parseResult = null
                )
            }

            for (attempt in 0..MAX_RETRY_COUNT) {
                try {
                    // 1. Preprocess image
                    val imageBytes = ImagePreprocessor.preprocessForOcr(appContext, uri)
                    if (imageBytes == null) {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                error = "이미지 처리에 실패했습니다. 다시 촬영해주세요."
                            )
                        }
                        return@launch
                    }

                    // 2. Call OCR API
                    val ocrResult = ocrRepository.recognizeScoreBoard(imageBytes)

                    ocrResult.fold(
                        onSuccess = { response ->
                            // 3. Parse OCR results
                            val parseResult = OcrResultParser.parse(response)

                            if (parseResult.isSuccess && parseResult.rows.isNotEmpty()) {
                                val editableScores = parseResult.rows.associate { row ->
                                    row.playerName to padScores(row.scores, _uiState.value.gameCount)
                                }

                                // Auto-match OCR names to member IDs
                                val memberMapping = autoMatchMembers(
                                    parseResult.rows.map { it.playerName },
                                    _uiState.value.availableMembers
                                )

                                val avgConfidence = if (parseResult.rows.isNotEmpty()) {
                                    parseResult.rows
                                        .map { it.confidence }
                                        .average()
                                        .toFloat()
                                } else {
                                    0f
                                }

                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        parseResult = parseResult,
                                        editableScores = editableScores,
                                        memberMapping = memberMapping,
                                        ocrConfidence = avgConfidence,
                                        retryCount = 0
                                    )
                                }
                                return@launch  // Success, exit loop
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        parseResult = parseResult,
                                        error = parseResult.errorMessage ?: "점수를 인식하지 못했습니다. 다시 촬영하거나 수동 입력을 이용해주세요."
                                    )
                                }
                                return@launch
                            }
                        },
                        onFailure = { exception ->
                            if (attempt < MAX_RETRY_COUNT) {
                                _uiState.update {
                                    it.copy(
                                        retryCount = attempt + 1,
                                        error = "OCR 인식 실패 (${attempt + 1}/${MAX_RETRY_COUNT}). 재시도 중..."
                                    )
                                }
                                return@fold  // Retry
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isProcessing = false,
                                        error = "OCR 인식에 실패했습니다. 수동 입력으로 전환해주세요.",
                                        retryCount = 0
                                    )
                                }
                                return@launch
                            }
                        }
                    )
                } catch (e: Exception) {
                    if (attempt < MAX_RETRY_COUNT) {
                        _uiState.update {
                            it.copy(
                                retryCount = attempt + 1,
                                error = "OCR 인식 실패 (${attempt + 1}/${MAX_RETRY_COUNT}). 재시도 중..."
                            )
                        }
                        // Retry
                    } else {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                error = "OCR 인식에 실패했습니다. 수동 입력으로 전환해주세요.",
                                retryCount = 0
                            )
                        }
                        return@launch
                    }
                }
            }
        }
    }

    /**
     * Update an editable score for a player.
     */
    fun updateScore(playerName: String, gameIndex: Int, score: Int) {
        val clampedScore = score.coerceIn(0, 300)

        val current = _uiState.value.editableScores.toMutableMap()
        val playerScores = current[playerName]?.toMutableList() ?: return
        if (gameIndex in playerScores.indices) {
            playerScores[gameIndex] = clampedScore
            current[playerName] = playerScores
            _uiState.update { it.copy(editableScores = current, error = null) }
        }
    }

    /**
     * Update member mapping for a player name.
     */
    fun updateMemberMapping(playerName: String, memberId: Int) {
        val current = _uiState.value.memberMapping.toMutableMap()
        current[playerName] = memberId
        _uiState.update { it.copy(memberMapping = current) }
    }

    /**
     * Save OCR-recognized scores to database.
     */
    fun saveScores() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessing = true, error = null) }

                val state = _uiState.value
                val tournament = tournamentRepository.getTournamentById(tournamentId).firstOrNull()
                val allScores = mutableListOf<GameScore>()

                // Load participants for handicap values
                val participants = tournamentRepository.getParticipantsByTournament(tournamentId).firstOrNull() ?: emptyList()

                state.editableScores.forEach { (playerName, scores) ->
                    val memberId = state.memberMapping[playerName]
                    if (memberId != null) {
                        val participant = participants.find { it.memberId == memberId }
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
                                    recordedBy = "OCR"
                                )
                            )
                        }
                    }
                }

                if (allScores.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "저장할 점수가 없습니다. 회원 매핑을 확인해주세요."
                        )
                    }
                    return@launch
                }

                // Delete existing scores and save new ones
                scoreRepository.deleteScoresByTournament(tournamentId)
                scoreRepository.saveScores(allScores)

                _uiState.update { it.copy(isProcessing = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "저장 실패: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Reset OCR state for re-capture.
     */
    fun resetOcr() {
        _uiState.update {
            it.copy(
                capturedImageUri = null,
                parseResult = null,
                editableScores = emptyMap(),
                memberMapping = emptyMap(),
                error = null,
                retryCount = 0,
                ocrConfidence = 0f
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Pad scores list to match expected game count.
     */
    private fun padScores(scores: List<Int>, gameCount: Int): List<Int> {
        return List(gameCount) { index ->
            scores.getOrElse(index) { 0 }
        }
    }

    /**
     * Auto-match OCR player names to registered members using fuzzy matching.
     */
    private fun autoMatchMembers(
        ocrNames: List<String>,
        availableMembers: Map<Int, String>
    ): Map<String, Int> {
        val mapping = mutableMapOf<String, Int>()
        val usedMemberIds = mutableSetOf<Int>()

        ocrNames.forEach { ocrName ->
            val cleanName = ocrName.replace("\\s+".toRegex(), "").trim()

            // Exact match first
            val exactMatch = availableMembers.entries.firstOrNull { (id, name) ->
                name.replace("\\s+".toRegex(), "").trim() == cleanName &&
                id !in usedMemberIds
            }

            if (exactMatch != null) {
                mapping[ocrName] = exactMatch.key
                usedMemberIds.add(exactMatch.key)
            } else {
                // Contains match
                val containsMatch = availableMembers.entries.firstOrNull { (id, name) ->
                    (cleanName.contains(name) || name.contains(cleanName)) &&
                    id !in usedMemberIds
                }
                if (containsMatch != null) {
                    mapping[ocrName] = containsMatch.key
                    usedMemberIds.add(containsMatch.key)
                }
            }
        }

        return mapping
    }
}
