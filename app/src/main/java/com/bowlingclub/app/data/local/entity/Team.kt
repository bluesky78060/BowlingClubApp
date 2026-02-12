package com.bowlingclub.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "teams",
    foreignKeys = [
        ForeignKey(
            entity = Tournament::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tournamentId"])]
)
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
