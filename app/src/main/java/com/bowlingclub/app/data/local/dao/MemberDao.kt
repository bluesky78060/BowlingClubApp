package com.bowlingclub.app.data.local.dao

import androidx.room.*
import com.bowlingclub.app.data.local.entity.Member
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Int): Flow<Member?>

    @Query("SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR nickname LIKE '%' || :query || '%'")
    fun searchMembers(query: String): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: Member): Long

    @Update
    suspend fun update(member: Member)

    @Delete
    suspend fun delete(member: Member)

    @Query("UPDATE members SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActiveStatus(id: Int, isActive: Boolean, updatedAt: LocalDateTime)

    @Query("SELECT COUNT(*) FROM members WHERE isActive = 1")
    fun getActiveMemberCount(): Flow<Int>

    @Query("SELECT * FROM members WHERE id IN (:ids)")
    suspend fun getMembersByIds(ids: List<Int>): List<Member>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<Member>)

    @Query("DELETE FROM members")
    suspend fun deleteAll()
}
