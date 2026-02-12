package com.bowlingclub.app.util

object HandicapCalculator {
    /**
     * 수동 핸디캡을 게임 점수에 적용합니다.
     *
     * @param score 원본 게임 점수
     * @param handicap 핸디캡 점수 (게임당)
     * @return Pair(핸디캡 점수, 최종 점수)
     */
    fun applyHandicap(score: Int, handicap: Int): Pair<Int, Int> {
        return Pair(handicap, score + handicap)
    }
}
