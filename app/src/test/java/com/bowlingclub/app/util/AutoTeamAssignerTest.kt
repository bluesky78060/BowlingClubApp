package com.bowlingclub.app.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AutoTeamAssignerTest {

    @Test
    fun `assignBySnakeDraft - 6명을 3팀으로 균등 배분 (각 2명)`() {
        // Given: 6명의 평균 점수
        val memberAverages = mapOf(
            1 to 200.0, // 1위
            2 to 190.0, // 2위
            3 to 180.0, // 3위
            4 to 170.0, // 4위
            5 to 160.0, // 5위
            6 to 150.0  // 6위
        )
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)

        // Then
        assertThat(result).hasSize(3)

        // Snake draft 순서: 1→2→3→6→5→4
        // 팀 1: 1, 6 (1라운드 1번, 2라운드 역순 3번)
        assertThat(result[0].teamName).isEqualTo("팀 1")
        assertThat(result[0].memberIds).containsExactly(1, 6)

        // 팀 2: 2, 5
        assertThat(result[1].teamName).isEqualTo("팀 2")
        assertThat(result[1].memberIds).containsExactly(2, 5)

        // 팀 3: 3, 4
        assertThat(result[2].teamName).isEqualTo("팀 3")
        assertThat(result[2].memberIds).containsExactly(3, 4)
    }

    @Test
    fun `assignBySnakeDraft - 7명을 3팀으로 불균등 배분`() {
        // Given: 7명의 평균 점수
        val memberAverages = mapOf(
            1 to 200.0,
            2 to 190.0,
            3 to 180.0,
            4 to 170.0,
            5 to 160.0,
            6 to 150.0,
            7 to 140.0
        )
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)

        // Then
        assertThat(result).hasSize(3)

        // Snake draft: 1→2→3→6→5→4→7
        // 팀 1: 1, 6, 7 (3명)
        assertThat(result[0].memberIds).hasSize(3)
        assertThat(result[0].memberIds).containsExactly(1, 6, 7)

        // 팀 2: 2, 5 (2명)
        assertThat(result[1].memberIds).hasSize(2)
        assertThat(result[1].memberIds).containsExactly(2, 5)

        // 팀 3: 3, 4 (2명)
        assertThat(result[2].memberIds).hasSize(2)
        assertThat(result[2].memberIds).containsExactly(3, 4)
    }

    @Test
    fun `assignBySnakeDraft - 평균 점수 내림차순 정렬 확인`() {
        // Given: 순서가 뒤섞인 평균 점수
        val memberAverages = mapOf(
            5 to 160.0,
            1 to 200.0,
            3 to 180.0,
            2 to 190.0,
            4 to 170.0
        )
        val teamCount = 2

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)

        // Then: 정렬 후 Snake draft
        // 정렬: 1(200) → 2(190) → 3(180) → 4(170) → 5(160)
        // Snake draft 순서: 1 → 2 → 3 → 4 → 5
        // Round 0 (정방향): Team0←1, Team1←2
        // Round 1 (역방향): Team1←3, Team0←4
        // Round 2 (정방향): Team0←5
        // 팀 0: 1, 4, 5
        assertThat(result[0].memberIds).containsExactly(1, 4, 5)

        // 팀 1: 2, 3
        assertThat(result[1].memberIds).containsExactly(2, 3)
    }

    @Test
    fun `assignBySnakeDraft - 커스텀 팀 이름 사용`() {
        // Given
        val memberAverages = mapOf(1 to 200.0, 2 to 190.0)
        val teamCount = 2
        val teamNames = listOf("레드팀", "블루팀")

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount, teamNames)

        // Then
        assertThat(result[0].teamName).isEqualTo("레드팀")
        assertThat(result[1].teamName).isEqualTo("블루팀")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `assignBySnakeDraft - 팀 수가 0이면 예외 발생`() {
        // Given
        val memberAverages = mapOf(1 to 200.0)
        val teamCount = 0

        // When/Then
        AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `assignBySnakeDraft - 참가자가 없으면 예외 발생`() {
        // Given
        val memberAverages = emptyMap<Int, Double>()
        val teamCount = 2

        // When/Then
        AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `assignBySnakeDraft - 팀 이름 수가 부족하면 예외 발생`() {
        // Given
        val memberAverages = mapOf(1 to 200.0, 2 to 190.0)
        val teamCount = 3
        val teamNames = listOf("팀 1") // 3개 필요한데 1개만 제공

        // When/Then
        AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount, teamNames)
    }

    @Test
    fun `assignBySnakeDraft - 단일 팀 (모든 멤버가 한 팀)`() {
        // Given
        val memberAverages = mapOf(1 to 200.0, 2 to 190.0, 3 to 180.0)
        val teamCount = 1

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].memberIds).containsExactly(1, 2, 3)
    }

    @Test
    fun `assignRandomly - 모든 팀에 최소 1명 배정`() {
        // Given
        val memberIds = listOf(1, 2, 3, 4, 5, 6)
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then
        assertThat(result).hasSize(3)
        result.forEach { team ->
            assertThat(team.memberIds).isNotEmpty()
        }
    }

    @Test
    fun `assignRandomly - 균등 배분 확인`() {
        // Given
        val memberIds = listOf(1, 2, 3, 4, 5, 6)
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then: 각 팀은 2명씩 배정되어야 함
        assertThat(result[0].memberIds).hasSize(2)
        assertThat(result[1].memberIds).hasSize(2)
        assertThat(result[2].memberIds).hasSize(2)
    }

    @Test
    fun `assignRandomly - 불균등 배분 (7명을 3팀)`() {
        // Given
        val memberIds = listOf(1, 2, 3, 4, 5, 6, 7)
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then
        val totalMembers = result.sumOf { it.memberIds.size }
        assertThat(totalMembers).isEqualTo(7)
    }

    @Test
    fun `assignRandomly - 커스텀 팀 이름 사용`() {
        // Given
        val memberIds = listOf(1, 2, 3, 4)
        val teamCount = 2
        val teamNames = listOf("화이트팀", "블랙팀")

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount, teamNames)

        // Then
        assertThat(result[0].teamName).isEqualTo("화이트팀")
        assertThat(result[1].teamName).isEqualTo("블랙팀")
    }

    @Test
    fun `assignRandomly - 모든 멤버가 정확히 한 팀에 배정`() {
        // Given
        val memberIds = listOf(1, 2, 3, 4, 5)
        val teamCount = 2

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then
        val allAssignedMembers = result.flatMap { it.memberIds }
        assertThat(allAssignedMembers).containsExactlyElementsIn(memberIds)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `assignRandomly - 팀 수가 0이면 예외 발생`() {
        // Given
        val memberIds = listOf(1, 2, 3)
        val teamCount = 0

        // When/Then
        AutoTeamAssigner.assignRandomly(memberIds, teamCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `assignRandomly - 참가자가 없으면 예외 발생`() {
        // Given
        val memberIds = emptyList<Int>()
        val teamCount = 2

        // When/Then
        AutoTeamAssigner.assignRandomly(memberIds, teamCount)
    }

    @Test
    fun `assignRandomly - 단일 팀 (모든 멤버가 한 팀)`() {
        // Given
        val memberIds = listOf(1, 2, 3)
        val teamCount = 1

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].memberIds).containsExactly(1, 2, 3)
    }

    @Test
    fun `assignRandomly - 기본 팀 이름 생성 확인`() {
        // Given
        val memberIds = listOf(1, 2, 3, 4)
        val teamCount = 2

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then
        assertThat(result[0].teamName).isEqualTo("팀 1")
        assertThat(result[1].teamName).isEqualTo("팀 2")
    }

    @Test
    fun `assignBySnakeDraft - 팀 수보다 멤버가 적을 때`() {
        // Given: 2명을 3팀으로
        val memberAverages = mapOf(1 to 200.0, 2 to 190.0)
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)

        // Then: 3팀이 생성되지만 1팀은 비어있음
        assertThat(result).hasSize(3)
        assertThat(result[0].memberIds).hasSize(1)
        assertThat(result[1].memberIds).hasSize(1)
        assertThat(result[2].memberIds).isEmpty()
    }

    @Test
    fun `assignRandomly - 팀 수보다 멤버가 적을 때`() {
        // Given: 2명을 3팀으로
        val memberIds = listOf(1, 2)
        val teamCount = 3

        // When
        val result = AutoTeamAssigner.assignRandomly(memberIds, teamCount)

        // Then: 3팀이 생성되지만 1팀은 비어있음
        assertThat(result).hasSize(3)
        val nonEmptyTeams = result.filter { it.memberIds.isNotEmpty() }
        assertThat(nonEmptyTeams).hasSize(2)
    }

    @Test
    fun `TeamAssignment 데이터 클래스 구조 확인`() {
        // Given/When
        val assignment = TeamAssignment(
            teamName = "테스트팀",
            memberIds = listOf(1, 2, 3)
        )

        // Then
        assertThat(assignment.teamName).isEqualTo("테스트팀")
        assertThat(assignment.memberIds).containsExactly(1, 2, 3)
    }

    @Test
    fun `assignBySnakeDraft - 같은 평균 점수 처리`() {
        // Given: 모든 멤버가 같은 평균
        val memberAverages = mapOf(
            1 to 180.0,
            2 to 180.0,
            3 to 180.0,
            4 to 180.0
        )
        val teamCount = 2

        // When
        val result = AutoTeamAssigner.assignBySnakeDraft(memberAverages, teamCount)

        // Then: 순서대로 배정 (Map 순서 의존)
        assertThat(result).hasSize(2)
        assertThat(result[0].memberIds).hasSize(2)
        assertThat(result[1].memberIds).hasSize(2)
    }
}
