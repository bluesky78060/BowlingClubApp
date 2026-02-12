package com.bowlingclub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.repository.MemberRepository
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

data class MemberListUiState(
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showActiveOnly: Boolean = true
)

@HiltViewModel
class MemberListViewModel @Inject constructor(
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberListUiState())
    val uiState: StateFlow<MemberListUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        loadMembers()
    }

    fun loadMembers(): Unit {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val memberFlow = if (_uiState.value.showActiveOnly) {
                memberRepository.getActiveMembers()
            } else {
                memberRepository.getAllMembers()
            }

            memberFlow
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "알 수 없는 오류가 발생했습니다"
                        )
                    }
                }
                .collect { members ->
                    _uiState.update {
                        it.copy(
                            members = members,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun searchMembers(query: String): Unit {
        _uiState.update { it.copy(searchQuery = query, isLoading = true, error = null) }

        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            if (query.isBlank()) {
                loadMembers()
            } else {
                memberRepository.searchMembers(query)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "검색 중 오류가 발생했습니다"
                            )
                        }
                    }
                    .collect { members ->
                        _uiState.update {
                            it.copy(
                                members = members,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            }
        }
    }

    fun toggleFilter(activeOnly: Boolean): Unit {
        _uiState.update { it.copy(showActiveOnly = activeOnly) }
        loadMembers()
    }

    fun toggleMemberStatus(memberId: Int, isActive: Boolean): Unit {
        viewModelScope.launch {
            try {
                memberRepository.toggleActiveStatus(memberId, isActive)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(error = exception.message ?: "상태 변경 중 오류가 발생했습니다")
                }
            }
        }
    }
}
