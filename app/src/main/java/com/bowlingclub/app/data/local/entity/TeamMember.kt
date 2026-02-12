package com.bowlingclub.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "team_members",
    foreignKeys = [
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
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
        Index(value = ["teamId", "memberId"], unique = true),
        Index(value = ["teamId"]),
        Index(value = ["memberId"])
    ]
)
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Int,
    val memberId: Int
)
