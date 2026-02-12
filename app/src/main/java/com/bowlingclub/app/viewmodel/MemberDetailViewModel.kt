package com.bowlingclub.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.repository.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberDetailUiState(
    val member: Member? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memberId: Int = checkNotNull(savedStateHandle.get<Int>("memberId")) {
        "memberId는 필수 파라미터입니다"
    }

    private val _uiState = MutableStateFlow(MemberDetailUiState())
    val uiState: StateFlow<MemberDetailUiState> = _uiState.asStateFlow()

    init {
        loadMember()
    }

    fun loadMember(): Unit {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            memberRepository.getMemberById(memberId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "회원 정보를 불러오는데 실패했습니다"
                        )
                    }
                }
                .collect { member ->
                    if (member != null) {
                        _uiState.update {
                            it.copy(
                                member = member,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "회원을 찾을 수 없습니다"
                            )
                        }
                    }
                }
        }
    }

    fun deleteMember(): Unit {
        viewModelScope.launch {
            val currentMember = _uiState.value.member

            if (currentMember == null) {
                _uiState.update {
                    it.copy(error = "삭제할 회원 정보가 없습니다")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                memberRepository.deleteMember(currentMember)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        isDeleted = true
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "삭제 중 오류가 발생했습니다",
                        isDeleted = false
                    )
                }
            }
        }
    }

    fun toggleActiveStatus(): Unit {
        viewModelScope.launch {
            val currentMember = _uiState.value.member

            if (currentMember == null) {
                _uiState.update {
                    it.copy(error = "회원 정보가 없습니다")
                }
                return@launch
            }

            try {
                val newStatus = !currentMember.isActive
                memberRepository.toggleActiveStatus(currentMember.id, newStatus)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(error = exception.message ?: "상태 변경 중 오류가 발생했습니다")
                }
            }
        }
    }
}
