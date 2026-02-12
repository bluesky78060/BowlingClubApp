package com.bowlingclub.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val nickname: String? = null,
    val gender: String = "M",          // "M" or "F" (핸디캡 시스템용)
    val birthDate: LocalDate? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profileImagePath: String? = null,
    val isActive: Boolean = true,       // true=활동, false=휴면
    val joinDate: LocalDate,
    val memo: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
