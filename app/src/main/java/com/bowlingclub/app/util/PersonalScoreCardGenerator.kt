package com.bowlingclub.app.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.bowlingclub.app.data.model.RankingResult

object PersonalScoreCardGenerator {

    private const val CARD_WIDTH = 800
    private const val CARD_HEIGHT = 500
    private const val PADDING = 30f

    private val GRADIENT_START = Color.parseColor("#667eea")
    private val GRADIENT_END = Color.parseColor("#764ba2")
    private val GOLD_COLOR = Color.parseColor("#FFD700")
    private val SILVER_COLOR = Color.parseColor("#C0C0C0")
    private val BRONZE_COLOR = Color.parseColor("#CD7F32")

    /**
     * 개인 성적 카드 이미지를 생성합니다.
     *
     * @param context Android Context
     * @param playerName 선수 이름
     * @param tournamentName 정기전 이름
     * @param tournamentDate 날짜 문자열
     * @param ranking 해당 선수의 RankingResult
     * @param totalParticipants 전체 참가자 수
     * @param handicapEnabled 핸디캡 적용 여부
     * @return 공유 가능한 Uri
     */
    fun generatePersonalCard(
        context: Context,
        playerName: String,
        tournamentName: String,
        tournamentDate: String,
        ranking: RankingResult,
        totalParticipants: Int,
        handicapEnabled: Boolean
    ): Uri? {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient background
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
                GRADIENT_START, GRADIENT_END, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat()),
            24f, 24f, gradientPaint
        )

        // Semi-transparent overlay for content area
        val overlayPaint = Paint().apply {
            color = Color.argb(40, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(PADDING, PADDING, CARD_WIDTH - PADDING, CARD_HEIGHT - PADDING),
            16f, 16f, overlayPaint
        )

        // Tournament info (top)
        val tournamentPaint = Paint().apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 20f
            isAntiAlias = true
        }
        canvas.drawText(tournamentName, PADDING + 20f, PADDING + 35f, tournamentPaint)
        canvas.drawText(tournamentDate, PADDING + 20f, PADDING + 62f, tournamentPaint)

        // Player name (large)
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(playerName, PADDING + 20f, 170f, namePaint)

        // Rank badge
        val rankBadgeColor = when (ranking.rank) {
            1 -> GOLD_COLOR
            2 -> SILVER_COLOR
            3 -> BRONZE_COLOR
            else -> Color.argb(150, 255, 255, 255)
        }
        val badgePaint = Paint().apply { color = rankBadgeColor; style = Paint.Style.FILL; isAntiAlias = true }
        val badgeCenterX = CARD_WIDTH - PADDING - 70f
        val badgeCenterY = 130f
        canvas.drawCircle(badgeCenterX, badgeCenterY, 45f, badgePaint)

        val rankTextPaint = Paint().apply {
            color = if (ranking.rank <= 3) Color.WHITE else Color.parseColor("#333333")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("${ranking.rank}", badgeCenterX, badgeCenterY + 13f, rankTextPaint)

        val rankLabelPaint = Paint().apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 16f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("/ ${totalParticipants}명", badgeCenterX, badgeCenterY + 60f, rankLabelPaint)

        // Separator
        val sepPaint = Paint().apply { color = Color.argb(80, 255, 255, 255); strokeWidth = 2f }
        canvas.drawLine(PADDING + 20f, 200f, CARD_WIDTH - PADDING - 20f, 200f, sepPaint)

        // Score details section
        val labelPaint = Paint().apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 18f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Game scores row
        val gameY = 250f
        var scoreX = PADDING + 20f
        ranking.gameScores.forEachIndexed { index, score ->
            canvas.drawText("G${index + 1}", scoreX, gameY - 5f, labelPaint)
            canvas.drawText("$score", scoreX, gameY + 28f, valuePaint)
            scoreX += 120f
        }

        // Stats row
        val statsY = 340f
        val statLabelPaint = Paint(labelPaint)
        val statValuePaint = Paint(valuePaint).apply { textSize = 32f }

        // Total
        canvas.drawText("합계", PADDING + 20f, statsY, statLabelPaint)
        canvas.drawText("${ranking.totalScore}", PADDING + 20f, statsY + 38f, statValuePaint)

        if (handicapEnabled) {
            // Handicap
            canvas.drawText("핸디캡", PADDING + 200f, statsY, statLabelPaint)
            canvas.drawText("+${ranking.handicapTotal}", PADDING + 200f, statsY + 38f, statValuePaint)

            // Final
            canvas.drawText("최종점수", PADDING + 380f, statsY, statLabelPaint)
            statValuePaint.color = GOLD_COLOR
            canvas.drawText("${ranking.finalTotal}", PADDING + 380f, statsY + 38f, statValuePaint)
        }

        // Average & High Game
        val avgX = if (handicapEnabled) PADDING + 560f else PADDING + 300f
        canvas.drawText("평균", avgX, statsY, statLabelPaint)
        val avgPaint = Paint(valuePaint).apply { textSize = 28f }
        canvas.drawText(String.format("%.1f", ranking.average), avgX, statsY + 38f, avgPaint)

        val highX = if (handicapEnabled) CARD_WIDTH - PADDING - 80f else PADDING + 500f
        canvas.drawText("하이게임", highX - 30f, statsY, statLabelPaint)
        canvas.drawText("${ranking.highGame}", highX - 30f, statsY + 38f, avgPaint)

        // Footer
        val footerPaint = Paint().apply {
            color = Color.argb(100, 255, 255, 255)
            textSize = 14f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("볼링클럽 앱", CARD_WIDTH / 2f, CARD_HEIGHT - 15f, footerPaint)

        return RankingImageGenerator.saveBitmapAndGetUri(context, bitmap, "personal_${playerName}_${System.currentTimeMillis()}")
    }
}
