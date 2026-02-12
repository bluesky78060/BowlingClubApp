package com.bowlingclub.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: LocalDate,
    val location: String? = null,       // 볼링장 이름
    val gameCount: Int = 3,             // 게임 수 (3 or 4)
    val isTeamGame: Boolean = false,
    val handicapEnabled: Boolean = false,
    val status: String = "SCHEDULED",   // SCHEDULED, IN_PROGRESS, COMPLETED
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
