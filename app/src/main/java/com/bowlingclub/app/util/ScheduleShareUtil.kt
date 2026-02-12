package com.bowlingclub.app.util

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ScheduleShareUtil {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)")

    /**
     * 다음 정기전 일정 공유 메시지를 생성합니다.
     */
    fun formatScheduleMessage(
        tournamentName: String,
        date: LocalDate,
        location: String?,
        gameCount: Int,
        isTeamGame: Boolean,
        participantNames: List<String> = emptyList()
    ): String {
        val sb = StringBuilder()
        sb.appendLine("🎳 볼링 정기전 안내 🎳")
        sb.appendLine()
        sb.appendLine("📋 대회명: $tournamentName")
        sb.appendLine("📅 일  시: ${date.format(dateFormatter)}")
        if (!location.isNullOrBlank()) {
            sb.appendLine("📍 장  소: $location")
        }
        sb.appendLine("🎮 게  임: ${gameCount}게임")
        if (isTeamGame) {
            sb.appendLine("👥 형  식: 팀전")
        }
        sb.appendLine()

        if (participantNames.isNotEmpty()) {
            sb.appendLine("참가자 (${participantNames.size}명):")
            participantNames.forEachIndexed { index, name ->
                sb.appendLine("  ${index + 1}. $name")
            }
            sb.appendLine()
        }

        sb.appendLine("━━━━━━━━━━━━━━━")
        sb.appendLine("볼링클럽 앱에서 공유됨")
        return sb.toString()
    }

    /**
     * 일정 공유 메시지를 생성하고 공유합니다.
     */
    fun shareSchedule(
        context: Context,
        tournamentName: String,
        date: LocalDate,
        location: String?,
        gameCount: Int,
        isTeamGame: Boolean,
        participantNames: List<String> = emptyList()
    ) {
        val message = formatScheduleMessage(
            tournamentName, date, location, gameCount, isTeamGame, participantNames
        )
        ShareUtil.shareText(context, message, "정기전 일정 안내")
    }
}
