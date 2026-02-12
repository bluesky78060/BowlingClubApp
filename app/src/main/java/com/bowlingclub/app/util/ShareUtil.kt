package com.bowlingclub.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.bowlingclub.app.data.model.RankingResult

object ShareUtil {

    /**
     * 이미지를 공유합니다 (카카오톡, 메시지 등).
     */
    fun shareImage(context: Context, imageUri: Uri, title: String = "볼링 결과 공유") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    /**
     * 텍스트를 공유합니다.
     */
    fun shareText(context: Context, text: String, title: String = "볼링 결과 공유") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    /**
     * 이미지 + 텍스트를 함께 공유합니다.
     */
    fun shareImageWithText(
        context: Context,
        imageUri: Uri,
        text: String,
        title: String = "볼링 결과 공유"
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    /**
     * 개인전 순위 결과를 텍스트로 포맷합니다.
     */
    fun formatRankingText(
        tournamentName: String,
        tournamentDate: String,
        rankings: List<RankingResult>,
        handicapEnabled: Boolean
    ): String {
        val sb = StringBuilder()
        sb.appendLine("🎳 $tournamentName")
        sb.appendLine("📅 $tournamentDate")
        sb.appendLine("━━━━━━━━━━━━━━━")
        sb.appendLine()

        rankings.forEach { r ->
            val medal = when (r.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "${r.rank}위"
            }
            sb.append("$medal ${r.memberName}")
            sb.append("  |  게임: ${r.gameScores.joinToString(" / ")}")
            sb.append("  |  합계: ${r.totalScore}")
            if (handicapEnabled) {
                sb.append("  |  H/C: +${r.handicapTotal}")
                sb.append("  |  최종: ${r.finalTotal}")
            }
            sb.appendLine()
        }

        sb.appendLine()
        sb.appendLine("━━━━━━━━━━━━━━━")
        sb.appendLine("볼링클럽 앱에서 공유됨")
        return sb.toString()
    }

    /**
     * 팀전 결과를 텍스트로 포맷합니다.
     */
    fun formatTeamRankingText(
        tournamentName: String,
        tournamentDate: String,
        teamRankings: List<TeamRankingResult>,
        handicapEnabled: Boolean
    ): String {
        val sb = StringBuilder()
        sb.appendLine("🎳 $tournamentName [팀전]")
        sb.appendLine("📅 $tournamentDate")
        sb.appendLine("━━━━━━━━━━━━━━━")
        sb.appendLine()

        teamRankings.forEach { team ->
            val medal = when (team.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "${team.rank}위"
            }
            val scoreText = if (handicapEnabled) "최종: ${team.teamFinalTotal}" else "합계: ${team.teamTotalScore}"
            sb.appendLine("$medal ${team.team.name}  ($scoreText)")

            team.memberRankings.forEach { member ->
                sb.appendLine("   - ${member.memberName}: ${member.gameScores.joinToString("/")} = ${member.totalScore}")
            }
            sb.appendLine()
        }

        sb.appendLine("━━━━━━━━━━━━━━━")
        sb.appendLine("볼링클럽 앱에서 공유됨")
        return sb.toString()
    }
}
