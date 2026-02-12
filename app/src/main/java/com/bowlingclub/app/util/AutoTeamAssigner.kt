package com.bowlingclub.app.util

data class TeamAssignment(
    val teamName: String,
    val memberIds: List<Int>
)

object AutoTeamAssigner {

    /**
     * Assigns players to teams using snake draft algorithm.
     * Players are sorted by average (descending) and distributed in snake order.
     *
     * @param memberAverages Map of memberId to average score
     * @param teamCount Number of teams to create
     * @param teamNames Optional custom team names (defaults to "팀 1", "팀 2", etc.)
     * @return List of TeamAssignment (teamName to memberIds)
     */
    fun assignBySnakeDraft(
        memberAverages: Map<Int, Double>,
        teamCount: Int,
        teamNames: List<String>? = null
    ): List<TeamAssignment> {
        require(teamCount > 0) { "팀 수는 1 이상이어야 합니다" }
        require(memberAverages.isNotEmpty()) { "참가자가 없습니다" }

        val names = teamNames ?: (1..teamCount).map { "팀 $it" }
        require(names.size >= teamCount) { "팀 이름 수가 부족합니다" }

        // Sort by average descending
        val sortedMembers = memberAverages.entries
            .sortedByDescending { it.value }
            .map { it.key }

        val teams = Array(teamCount) { mutableListOf<Int>() }

        if (teamCount > 0) {
            sortedMembers.forEachIndexed { index, memberId ->
                val round = index / teamCount
                val position = index % teamCount
                val teamIndex = if (round % 2 == 0) position else (teamCount - 1 - position)
                teams[teamIndex].add(memberId)
            }
        }

        return teams.mapIndexed { index, memberIds ->
            TeamAssignment(
                teamName = names[index],
                memberIds = memberIds
            )
        }
    }

    /**
     * Assigns players randomly to teams.
     */
    fun assignRandomly(
        memberIds: List<Int>,
        teamCount: Int,
        teamNames: List<String>? = null
    ): List<TeamAssignment> {
        require(teamCount > 0) { "팀 수는 1 이상이어야 합니다" }
        require(memberIds.isNotEmpty()) { "참가자가 없습니다" }

        val names = teamNames ?: (1..teamCount).map { "팀 $it" }
        val shuffled = memberIds.shuffled()
        val teams = Array(teamCount) { mutableListOf<Int>() }

        if (teamCount > 0) {
            shuffled.forEachIndexed { index, memberId ->
                teams[index % teamCount].add(memberId)
            }
        }

        return teams.mapIndexed { index, members ->
            TeamAssignment(teamName = names[index], memberIds = members)
        }
    }
}
