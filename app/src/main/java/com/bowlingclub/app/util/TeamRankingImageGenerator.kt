package com.bowlingclub.app.util

import android.content.Context
import android.graphics.*
import android.net.Uri

object TeamRankingImageGenerator {

    private const val IMAGE_WIDTH = 1080
    private const val PADDING = 40f
    private const val HEADER_HEIGHT = 160f
    private const val TEAM_CARD_HEADER = 70f
    private const val MEMBER_ROW_HEIGHT = 45f
    private const val CARD_GAP = 20f
    private const val CARD_PADDING = 16f

    private val HEADER_BG_COLOR = Color.parseColor("#2E7D32")
    private val CARD_COLORS = listOf(
        Color.parseColor("#FFF8E1"),  // 1st - Gold tint
        Color.parseColor("#F5F5F5"),  // 2nd - Silver tint
        Color.parseColor("#FBE9E7"),  // 3rd - Bronze tint
        Color.parseColor("#FFFFFF")   // Others - White
    )
    private val CARD_BORDER_COLORS = listOf(
        Color.parseColor("#FFD700"),
        Color.parseColor("#C0C0C0"),
        Color.parseColor("#CD7F32"),
        Color.parseColor("#BDBDBD")
    )
    private val TEXT_COLOR = Color.parseColor("#212121")

    /**
     * 팀전 결과 Bitmap을 생성합니다 (미리보기용).
     */
    fun generateTeamRankingBitmap(
        tournamentName: String,
        tournamentDate: String,
        teamRankings: List<TeamRankingResult>,
        handicapEnabled: Boolean,
        gameCount: Int
    ): Bitmap {
        var totalHeight = HEADER_HEIGHT + PADDING * 2 + 60f
        teamRankings.forEach { team ->
            totalHeight += TEAM_CARD_HEADER + MEMBER_ROW_HEIGHT * (team.memberRankings.size + 1) + CARD_GAP + CARD_PADDING * 2
        }
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, totalHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        drawHeader(canvas, tournamentName, tournamentDate)
        var currentY = HEADER_HEIGHT + PADDING
        teamRankings.forEach { teamRanking ->
            currentY = drawTeamCard(canvas, teamRanking, handicapEnabled, gameCount, currentY)
            currentY += CARD_GAP
        }
        drawFooter(canvas, totalHeight)
        return bitmap
    }

    fun generateTeamRankingImage(
        context: Context,
        tournamentName: String,
        tournamentDate: String,
        teamRankings: List<TeamRankingResult>,
        handicapEnabled: Boolean,
        gameCount: Int
    ): Uri? {
        // Calculate total height
        var totalHeight = HEADER_HEIGHT + PADDING * 2 + 60f  // header + top/bottom padding + footer
        teamRankings.forEach { team ->
            totalHeight += TEAM_CARD_HEADER + MEMBER_ROW_HEIGHT * (team.memberRankings.size + 1) + CARD_GAP + CARD_PADDING * 2
        }

        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, totalHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // Draw header
        drawHeader(canvas, tournamentName, tournamentDate)

        // Draw team cards
        var currentY = HEADER_HEIGHT + PADDING
        teamRankings.forEach { teamRanking ->
            currentY = drawTeamCard(canvas, teamRanking, handicapEnabled, gameCount, currentY)
            currentY += CARD_GAP
        }

        // Draw footer
        drawFooter(canvas, totalHeight)

        return RankingImageGenerator.saveBitmapAndGetUri(context, bitmap, "team_ranking_${System.currentTimeMillis()}")
    }

    private fun drawHeader(canvas: Canvas, title: String, date: String) {
        val headerPaint = Paint().apply { color = HEADER_BG_COLOR; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), HEADER_HEIGHT, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE; textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true
        }
        canvas.drawText(title, PADDING, 70f, titlePaint)

        val datePaint = Paint().apply {
            color = Color.parseColor("#C8E6C9"); textSize = 28f; isAntiAlias = true
        }
        canvas.drawText(date, PADDING, 120f, datePaint)

        val subtitlePaint = Paint().apply {
            color = Color.WHITE; textSize = 24f; isAntiAlias = true
        }
        canvas.drawText("팀전 결과", IMAGE_WIDTH - PADDING - subtitlePaint.measureText("팀전 결과"), 120f, subtitlePaint)
    }

    private fun drawTeamCard(
        canvas: Canvas,
        teamRanking: TeamRankingResult,
        handicapEnabled: Boolean,
        gameCount: Int,
        startY: Float
    ): Float {
        val memberCount = teamRanking.memberRankings.size
        val cardHeight = TEAM_CARD_HEADER + MEMBER_ROW_HEIGHT * (memberCount + 1) + CARD_PADDING * 2
        val cardLeft = PADDING
        val cardRight = IMAGE_WIDTH - PADDING

        // Card background
        val colorIndex = (teamRanking.rank - 1).coerceIn(0, CARD_COLORS.size - 1)
        val bgPaint = Paint().apply { color = CARD_COLORS[colorIndex]; style = Paint.Style.FILL }
        val borderPaint = Paint().apply {
            color = CARD_BORDER_COLORS[colorIndex]
            style = Paint.Style.STROKE; strokeWidth = 3f
        }
        val rect = RectF(cardLeft, startY, cardRight, startY + cardHeight)
        canvas.drawRoundRect(rect, 16f, 16f, bgPaint)
        canvas.drawRoundRect(rect, 16f, 16f, borderPaint)

        // Team header
        val rankDisplay = "${teamRanking.rank}위"
        val teamHeaderPaint = Paint().apply {
            color = TEXT_COLOR; textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true
        }
        canvas.drawText("$rankDisplay  ${teamRanking.team.name}", cardLeft + CARD_PADDING, startY + CARD_PADDING + 35f, teamHeaderPaint)

        // Team total score (right aligned)
        val scorePaint = Paint().apply {
            color = Color.parseColor("#1565C0"); textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val scoreText = if (handicapEnabled) "최종: ${teamRanking.teamFinalTotal}" else "합계: ${teamRanking.teamTotalScore}"
        canvas.drawText(scoreText, cardRight - CARD_PADDING, startY + CARD_PADDING + 35f, scorePaint)

        // Separator line
        val sepY = startY + TEAM_CARD_HEADER
        val sepPaint = Paint().apply { color = CARD_BORDER_COLORS[colorIndex]; strokeWidth = 1f }
        canvas.drawLine(cardLeft + CARD_PADDING, sepY, cardRight - CARD_PADDING, sepY, sepPaint)

        // Member table header
        val tableHeaderY = sepY + 5f
        val colHeaderPaint = Paint().apply {
            color = Color.parseColor("#616161"); textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val memberAreaWidth = cardRight - cardLeft - CARD_PADDING * 2
        val nameColW = memberAreaWidth * 0.18f
        val gameColW = memberAreaWidth * (if (handicapEnabled) 0.11f else 0.14f)
        val totalColW = memberAreaWidth * 0.12f

        var colX = cardLeft + CARD_PADDING
        canvas.drawText("이름", colX + nameColW / 2, tableHeaderY + MEMBER_ROW_HEIGHT / 2 + 5f, colHeaderPaint)
        colX += nameColW
        for (g in 1..gameCount) {
            canvas.drawText("G$g", colX + gameColW / 2, tableHeaderY + MEMBER_ROW_HEIGHT / 2 + 5f, colHeaderPaint)
            colX += gameColW
        }
        canvas.drawText("합계", colX + totalColW / 2, tableHeaderY + MEMBER_ROW_HEIGHT / 2 + 5f, colHeaderPaint)
        if (handicapEnabled) {
            colX += totalColW
            canvas.drawText("최종", colX + totalColW / 2, tableHeaderY + MEMBER_ROW_HEIGHT / 2 + 5f, colHeaderPaint)
        }

        // Member rows
        val memberTextPaint = Paint().apply {
            color = TEXT_COLOR; textSize = 18f; isAntiAlias = true; textAlign = Paint.Align.CENTER
        }
        teamRanking.memberRankings.forEachIndexed { idx, member ->
            val rowY = tableHeaderY + MEMBER_ROW_HEIGHT * (idx + 1)
            colX = cardLeft + CARD_PADDING

            // Name (left aligned)
            val namePaint = Paint(memberTextPaint).apply { textAlign = Paint.Align.LEFT }
            canvas.drawText(member.memberName, colX + 4f, rowY + MEMBER_ROW_HEIGHT / 2 + 5f, namePaint)
            colX += nameColW

            // Game scores
            member.gameScores.forEach { score ->
                canvas.drawText(score.toString(), colX + gameColW / 2, rowY + MEMBER_ROW_HEIGHT / 2 + 5f, memberTextPaint)
                colX += gameColW
            }

            // Total
            val totalPaint = Paint(memberTextPaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(member.totalScore.toString(), colX + totalColW / 2, rowY + MEMBER_ROW_HEIGHT / 2 + 5f, totalPaint)

            if (handicapEnabled) {
                colX += totalColW
                canvas.drawText(member.finalTotal.toString(), colX + totalColW / 2, rowY + MEMBER_ROW_HEIGHT / 2 + 5f, totalPaint)
            }
        }

        return startY + cardHeight
    }

    private fun drawFooter(canvas: Canvas, totalHeight: Float) {
        val footerPaint = Paint().apply {
            color = Color.parseColor("#9E9E9E"); textSize = 18f; isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("볼링클럽 앱", IMAGE_WIDTH / 2f, totalHeight - 20f, footerPaint)
    }
}
