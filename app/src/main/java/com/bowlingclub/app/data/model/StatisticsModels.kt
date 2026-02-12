package com.bowlingclub.app.data.model

import java.time.LocalDateTime

data class PersonalStats(
    val averageScore: Double,
    val highScore: Int,
    val lowScore: Int,
    val totalGames: Int,
    val tournamentCount: Int
)

data class ScoreTrendItem(
    val date: LocalDateTime,
    val score: Int,
    val gameNumber: Int,
    val tournamentId: Int
)

data class ScoreDistributionItem(
    val range: String,      // "0-99", "100-129", etc.
    val count: Int
)

data class ClubStats(
    val totalGames: Int,
    val overallAverage: Double,
    val highestScore: Int,
    val activeMemberCount: Int,
    val totalTournaments: Int
)

data class MemberRankingItem(
    val memberId: Int,
    val memberName: String,
    val averageScore: Double,
    val highScore: Int,
    val gamesPlayed: Int
)
