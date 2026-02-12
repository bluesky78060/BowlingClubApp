package com.bowlingclub.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "tournament_participants",
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
        Index(value = ["tournamentId", "memberId"], unique = true),
        Index(value = ["tournamentId"]),
        Index(value = ["memberId"])
    ]
)
data class TournamentParticipant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val memberId: Int,
    val handicap: Int = 0,              // 수동 핸디캡 점수 (게임당)
    val status: String = "ACTIVE",      // ACTIVE, WITHDRAWN
    val joinedAt: LocalDateTime = LocalDateTime.now()
)
