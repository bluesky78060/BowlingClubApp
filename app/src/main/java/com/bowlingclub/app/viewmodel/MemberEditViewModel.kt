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
import java.time.LocalDate
import javax.inject.Inject

data class MemberEditUiState(
    val name: String = "",
    val nickname: String = "",
    val gender: String = "M",
    val birthDate: LocalDate? = null,
    val phoneNumber: String = "",
    val address: String = "",
    val profileImagePath: String? = null,
    val memo: String = "",
    val joinDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class MemberEditViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memberId: Int? = savedStateHandle.get<Int>("memberId")?.takeIf { it != -1 }

    private val _uiState = MutableStateFlow(MemberEditUiState())
    val uiState: StateFlow<MemberEditUiState> = _uiState.asStateFlow()

    init {
        if (memberId != null) {
            loadMember()
        }
    }

    fun loadMember(): Unit {
        if (memberId == null) return

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
                                name = member.name,
                                nickname = member.nickname ?: "",
                                gender = member.gender,
                                birthDate = member.birthDate,
                                phoneNumber = member.phoneNumber ?: "",
                                address = member.address ?: "",
                                profileImagePath = member.profileImagePath,
                                memo = member.memo ?: "",
                                joinDate = member.joinDate,
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

    fun updateName(name: String): Unit {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun updateNickname(nickname: String): Unit {
        _uiState.update { it.copy(nickname = nickname) }
    }

    fun updateGender(gender: String): Unit {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateBirthDate(birthDate: LocalDate?): Unit {
        _uiState.update { it.copy(birthDate = birthDate) }
    }

    fun updatePhoneNumber(phoneNumber: String): Unit {
        _uiState.update { it.copy(phoneNumber = phoneNumber.trim()) }
    }

    fun updateAddress(address: String): Unit {
        _uiState.update { it.copy(address = address) }
    }

    fun updateMemo(memo: String): Unit {
        _uiState.update { it.copy(memo = memo) }
    }

    fun updateJoinDate(joinDate: LocalDate): Unit {
        _uiState.update { it.copy(joinDate = joinDate) }
    }

    fun updateProfileImage(path: String): Unit {
        _uiState.update { it.copy(profileImagePath = path) }
    }

    fun saveMember(): Unit {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSaved = false) }

            val currentState = _uiState.value

            if (currentState.name.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "이름을 입력해주세요"
                    )
                }
                return@launch
            }

            val trimmedPhone = currentState.phoneNumber.trim()
            val trimmedNickname = currentState.nickname.trim()
            val trimmedMemo = currentState.memo.trim()
            val trimmedAddress = currentState.address.trim()

            try {
                val member = Member(
                    id = memberId ?: 0,
                    name = currentState.name.trim(),
                    nickname = trimmedNickname.ifBlank { null },
                    gender = currentState.gender,
                    birthDate = currentState.birthDate,
                    phoneNumber = trimmedPhone.ifBlank { null },
                    address = trimmedAddress.ifBlank { null },
                    profileImagePath = currentState.profileImagePath,
                    joinDate = currentState.joinDate,
                    memo = trimmedMemo.ifBlank { null }
                )

                if (memberId == null) {
                    memberRepository.insertMember(member)
                } else {
                    memberRepository.updateMember(member)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        isSaved = true
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "저장 중 오류가 발생했습니다",
                        isSaved = false
                    )
                }
            }
        }
    }
}
