package com.bowlingclub.app.data.repository

import com.bowlingclub.app.data.local.dao.MemberDao
import com.bowlingclub.app.data.local.entity.Member
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepository @Inject constructor(
    private val memberDao: MemberDao
) {
    fun getAllMembers(): Flow<List<Member>> {
        return memberDao.getAllMembers()
    }

    fun getActiveMembers(): Flow<List<Member>> {
        return memberDao.getActiveMembers()
    }

    fun getMemberById(id: Int): Flow<Member?> {
        return memberDao.getMemberById(id)
    }

    fun searchMembers(query: String): Flow<List<Member>> {
        return memberDao.searchMembers(query)
    }

    suspend fun insertMember(member: Member): Long {
        val memberWithTimestamp = member.copy(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return memberDao.insert(memberWithTimestamp)
    }

    suspend fun updateMember(member: Member) {
        val memberWithTimestamp = member.copy(
            updatedAt = LocalDateTime.now()
        )
        memberDao.update(memberWithTimestamp)
    }

    suspend fun deleteMember(member: Member) {
        memberDao.delete(member)
    }

    suspend fun toggleActiveStatus(memberId: Int, isActive: Boolean) {
        memberDao.updateActiveStatus(
            id = memberId,
            isActive = isActive,
            updatedAt = LocalDateTime.now()
        )
    }

    fun getActiveMemberCount(): Flow<Int> {
        return memberDao.getActiveMemberCount()
    }
}
