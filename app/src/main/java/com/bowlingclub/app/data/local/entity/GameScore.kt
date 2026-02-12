package com.bowlingclub.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "game_scores",
    foreignKeys = [
        ForeignKey(
            entity = Tournament::class,
            parentColumns = ["id"],
            childColumns = ["tournamentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tournamentId", "memberId", "gameNumber"]),
        Index(value = ["tournamentId"]),
        Index(value = ["memberId"]),
        Index(value = ["createdAt"])
    ]
)
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val memberId: Int,
    val gameNumber: Int,                // 1, 2, 3, 4
    val score: Int,                     // 0-300
    val handicapScore: Int = 0,         // 핸디캡 추가 점수
    val finalScore: Int,                // score + handicapScore
    val recordedBy: String = "MANUAL",  // MANUAL or OCR
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
