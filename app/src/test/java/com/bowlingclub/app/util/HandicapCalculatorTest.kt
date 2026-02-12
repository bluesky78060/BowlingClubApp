package com.bowlingclub.app.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HandicapCalculatorTest {

    @Test
    fun `calculateHandicap - 평균이 기준점수보다 낮을 때 핸디캡 계산`() {
        // Given: average = 150, base = 200, percentage = 80
        val average = 150.0
        val baseScore = 200
        val percentage = 80

        // When
        val handicap = HandicapCalculator.calculateHandicap(average, baseScore, percentage)

        // Then: (200 - 150) * 0.8 = 40
        assertThat(handicap).isEqualTo(40)
    }

    @Test
    fun `calculateHandicap - 평균이 기준점수와 같을 때 핸디캡 0`() {
        // Given: average = 200, base = 200
        val average = 200.0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average)

        // Then
        assertThat(handicap).isEqualTo(0)
    }

    @Test
    fun `calculateHandicap - 평균이 기준점수보다 높을 때 핸디캡 0`() {
        // Given: average = 250, base = 200
        val average = 250.0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average)

        // Then: 핸디캡은 0 (음수가 되지 않음)
        assertThat(handicap).isEqualTo(0)
    }

    @Test
    fun `calculateHandicap - 커스텀 기준점수와 비율`() {
        // Given: average = 100, base = 220, percentage = 90
        val average = 100.0
        val baseScore = 220
        val percentage = 90

        // When
        val handicap = HandicapCalculator.calculateHandicap(average, baseScore, percentage)

        // Then: (220 - 100) * 0.9 = 108
        assertThat(handicap).isEqualTo(108)
    }

    @Test
    fun `calculateHandicap - 반올림 동작 확인`() {
        // Given: average = 150.5, base = 200, percentage = 80
        val average = 150.5
        val baseScore = 200
        val percentage = 80

        // When
        val handicap = HandicapCalculator.calculateHandicap(average, baseScore, percentage)

        // Then: (200 - 150.5) * 0.8 = 39.6 → 40 (반올림)
        assertThat(handicap).isEqualTo(40)
    }

    @Test
    fun `calculateHandicap - 소수점 반올림 내림 케이스`() {
        // Given: average = 170, base = 200, percentage = 80
        val average = 170.0
        val baseScore = 200
        val percentage = 80

        // When
        val handicap = HandicapCalculator.calculateHandicap(average, baseScore, percentage)

        // Then: (200 - 170) * 0.8 = 24.0
        assertThat(handicap).isEqualTo(24)
    }

    @Test
    fun `calculateHandicap - 기본값 사용 (base=200, percentage=80)`() {
        // Given: average = 150
        val average = 150.0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average)

        // Then: (200 - 150) * 0.8 = 40
        assertThat(handicap).isEqualTo(40)
    }

    @Test
    fun `calculateHandicap - 평균이 0일 때`() {
        // Given: average = 0
        val average = 0.0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average)

        // Then: (200 - 0) * 0.8 = 160
        assertThat(handicap).isEqualTo(160)
    }

    @Test
    fun `calculateTotalHandicap - 여러 게임의 총 핸디캡 계산`() {
        // Given: average = 150, gameCount = 3
        val average = 150.0
        val gameCount = 3

        // When
        val totalHandicap = HandicapCalculator.calculateTotalHandicap(average, gameCount)

        // Then: 40 * 3 = 120
        assertThat(totalHandicap).isEqualTo(120)
    }

    @Test
    fun `calculateTotalHandicap - 게임 수가 1일 때`() {
        // Given: average = 150, gameCount = 1
        val average = 150.0
        val gameCount = 1

        // When
        val totalHandicap = HandicapCalculator.calculateTotalHandicap(average, gameCount)

        // Then
        assertThat(totalHandicap).isEqualTo(40)
    }

    @Test
    fun `calculateTotalHandicap - 게임 수가 0일 때`() {
        // Given: average = 150, gameCount = 0
        val average = 150.0
        val gameCount = 0

        // When
        val totalHandicap = HandicapCalculator.calculateTotalHandicap(average, gameCount)

        // Then
        assertThat(totalHandicap).isEqualTo(0)
    }

    @Test
    fun `calculateTotalHandicap - 커스텀 파라미터 사용`() {
        // Given: average = 100, gameCount = 4, base = 220, percentage = 90
        val average = 100.0
        val gameCount = 4
        val baseScore = 220
        val percentage = 90

        // When
        val totalHandicap = HandicapCalculator.calculateTotalHandicap(
            average, gameCount, baseScore, percentage
        )

        // Then: 108 * 4 = 432
        assertThat(totalHandicap).isEqualTo(432)
    }

    @Test
    fun `calculateGameWithHandicap - 게임 점수와 핸디캡 점수 반환`() {
        // Given: score = 180, average = 150
        val score = 180
        val average = 150.0

        // When
        val (handicap, finalScore) = HandicapCalculator.calculateGameWithHandicap(score, average)

        // Then
        assertThat(handicap).isEqualTo(40)
        assertThat(finalScore).isEqualTo(220) // 180 + 40
    }

    @Test
    fun `calculateGameWithHandicap - 핸디캡이 0일 때`() {
        // Given: score = 220, average = 200
        val score = 220
        val average = 200.0

        // When
        val (handicap, finalScore) = HandicapCalculator.calculateGameWithHandicap(score, average)

        // Then
        assertThat(handicap).isEqualTo(0)
        assertThat(finalScore).isEqualTo(220)
    }

    @Test
    fun `calculateGameWithHandicap - 커스텀 파라미터 사용`() {
        // Given: score = 150, average = 100, base = 220, percentage = 90
        val score = 150
        val average = 100.0
        val baseScore = 220
        val percentage = 90

        // When
        val (handicap, finalScore) = HandicapCalculator.calculateGameWithHandicap(
            score, average, baseScore, percentage
        )

        // Then
        assertThat(handicap).isEqualTo(108)
        assertThat(finalScore).isEqualTo(258) // 150 + 108
    }

    @Test
    fun `calculateHandicap - percentage가 0일 때`() {
        // Given: average = 150, percentage = 0
        val average = 150.0
        val percentage = 0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average, percentage = percentage)

        // Then: (200 - 150) * 0.0 = 0
        assertThat(handicap).isEqualTo(0)
    }

    @Test
    fun `calculateHandicap - percentage가 100일 때`() {
        // Given: average = 150, percentage = 100
        val average = 150.0
        val percentage = 100

        // When
        val handicap = HandicapCalculator.calculateHandicap(average, percentage = percentage)

        // Then: (200 - 150) * 1.0 = 50
        assertThat(handicap).isEqualTo(50)
    }

    @Test
    fun `DEFAULT_BASE_SCORE 상수 확인`() {
        assertThat(HandicapCalculator.DEFAULT_BASE_SCORE).isEqualTo(200)
    }

    @Test
    fun `DEFAULT_PERCENTAGE 상수 확인`() {
        assertThat(HandicapCalculator.DEFAULT_PERCENTAGE).isEqualTo(80)
    }

    @Test
    fun `calculateHandicap - 실제 USBC 예제 테스트`() {
        // Given: USBC 표준 핸디캡 (200 베이스, 80%)
        // 평균 175점인 볼러
        val average = 175.0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average)

        // Then: (200 - 175) * 0.8 = 20
        assertThat(handicap).isEqualTo(20)
    }

    @Test
    fun `calculateHandicap - 매우 낮은 평균에서 핸디캡 계산`() {
        // Given: 초보자 평균 50점
        val average = 50.0

        // When
        val handicap = HandicapCalculator.calculateHandicap(average)

        // Then: (200 - 50) * 0.8 = 120
        assertThat(handicap).isEqualTo(120)
    }

    @Test
    fun `calculateTotalHandicap - 음수 게임 수 처리`() {
        // Given: 음수 게임 수 (비정상적이지만 방어적 코드 테스트)
        val average = 150.0
        val gameCount = -1

        // When
        val totalHandicap = HandicapCalculator.calculateTotalHandicap(average, gameCount)

        // Then: 음수가 되지만 비즈니스 로직에서 validation 필요
        assertThat(totalHandicap).isEqualTo(-40)
    }
}
