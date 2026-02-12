package com.bowlingclub.app.data.model

data class RankingResult(
    val rank: Int,
    val memberId: Int,
    val memberName: String,
    val gameScores: List<Int>,     // 각 게임 점수
    val totalScore: Int,           // 합계
    val handicapTotal: Int,        // 핸디캡 합계
    val finalTotal: Int,           // 최종 합계 (score + handicap)
    val highGame: Int,             // 하이 게임
    val average: Double            // 평균
)
