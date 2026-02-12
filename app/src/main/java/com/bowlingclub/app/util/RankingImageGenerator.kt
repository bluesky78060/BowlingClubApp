package com.bowlingclub.app.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.bowlingclub.app.data.model.RankingResult
import java.io.File
import java.io.FileOutputStream

object RankingImageGenerator {

    private const val IMAGE_WIDTH = 1080
    private const val PADDING = 40f
    private const val HEADER_HEIGHT = 160f
    private const val ROW_HEIGHT = 60f
    private const val TABLE_HEADER_HEIGHT = 50f

    // Colors
    private val BACKGROUND_COLOR = Color.WHITE
    private val HEADER_BG_COLOR = Color.parseColor("#1976D2")
    private val TABLE_HEADER_BG = Color.parseColor("#E3F2FD")
    private val GOLD_COLOR = Color.parseColor("#FFD700")
    private val SILVER_COLOR = Color.parseColor("#C0C0C0")
    private val BRONZE_COLOR = Color.parseColor("#CD7F32")
    private val TEXT_COLOR = Color.parseColor("#212121")
    private val BORDER_COLOR = Color.parseColor("#BDBDBD")
    private val ALTERNATE_ROW_COLOR = Color.parseColor("#FAFAFA")

    /**
     * 개인전 순위표 Bitmap을 생성합니다 (미리보기용).
     */
    fun generateRankingBitmap(
        tournamentName: String,
        tournamentDate: String,
        rankings: List<RankingResult>,
        handicapEnabled: Boolean,
        gameCount: Int
    ): Bitmap {
        val imageHeight = (HEADER_HEIGHT + TABLE_HEADER_HEIGHT + ROW_HEIGHT * rankings.size + PADDING * 3 + 60f).toInt()
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BACKGROUND_COLOR)
        drawHeader(canvas, tournamentName, tournamentDate)
        val tableTop = HEADER_HEIGHT + PADDING
        drawRankingTable(canvas, rankings, handicapEnabled, gameCount, tableTop)
        drawFooter(canvas, imageHeight.toFloat())
        return bitmap
    }

    /**
     * 개인전 순위표 이미지를 생성하고 공유 가능한 Uri를 반환합니다.
     */
    fun generateRankingImage(
        context: Context,
        tournamentName: String,
        tournamentDate: String,
        rankings: List<RankingResult>,
        handicapEnabled: Boolean,
        gameCount: Int
    ): Uri? {
        val imageHeight = (HEADER_HEIGHT + TABLE_HEADER_HEIGHT + ROW_HEIGHT * rankings.size + PADDING * 3 + 60f).toInt()
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(BACKGROUND_COLOR)

        // Draw header
        drawHeader(canvas, tournamentName, tournamentDate)

        // Draw table
        val tableTop = HEADER_HEIGHT + PADDING
        drawRankingTable(canvas, rankings, handicapEnabled, gameCount, tableTop)

        // Draw footer
        drawFooter(canvas, imageHeight.toFloat())

        // Save and return Uri
        return saveBitmapAndGetUri(context, bitmap, "ranking_${System.currentTimeMillis()}")
    }

    private fun drawHeader(canvas: Canvas, title: String, date: String) {
        // Header background
        val headerPaint = Paint().apply {
            color = HEADER_BG_COLOR
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), HEADER_HEIGHT, headerPaint)

        // Title text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, PADDING, 70f, titlePaint)

        // Date text
        val datePaint = Paint().apply {
            color = Color.parseColor("#BBDEFB")
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText(date, PADDING, 120f, datePaint)

        // "개인전 순위표" subtitle
        val subtitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("개인전 순위표", IMAGE_WIDTH - PADDING - subtitlePaint.measureText("개인전 순위표"), 120f, subtitlePaint)
    }

    private fun drawRankingTable(
        canvas: Canvas,
        rankings: List<RankingResult>,
        handicapEnabled: Boolean,
        gameCount: Int,
        startY: Float
    ) {
        val columns = mutableListOf("순위", "이름")
        for (i in 1..gameCount) columns.add("G$i")
        columns.add("합계")
        if (handicapEnabled) {
            columns.add("H/C")
            columns.add("최종")
        }
        columns.add("AVG")

        // Calculate column widths
        val totalWidth = IMAGE_WIDTH - PADDING * 2
        val rankWidth = totalWidth * 0.08f
        val nameWidth = totalWidth * 0.14f
        val gameWidth = totalWidth * (if (handicapEnabled) 0.10f else 0.12f)
        val totalScoreWidth = totalWidth * 0.10f
        val hcWidth = totalWidth * 0.08f
        val finalWidth = totalWidth * 0.10f
        val avgWidth = totalWidth * 0.10f

        val colWidths = mutableListOf(rankWidth, nameWidth)
        for (i in 1..gameCount) colWidths.add(gameWidth)
        colWidths.add(totalScoreWidth)
        if (handicapEnabled) {
            colWidths.add(hcWidth)
            colWidths.add(finalWidth)
        }
        colWidths.add(avgWidth)

        // Draw table header
        val headerBgPaint = Paint().apply { color = TABLE_HEADER_BG; style = Paint.Style.FILL }
        canvas.drawRect(PADDING, startY, IMAGE_WIDTH - PADDING, startY + TABLE_HEADER_HEIGHT, headerBgPaint)

        val headerTextPaint = Paint().apply {
            color = Color.parseColor("#1565C0")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        var x = PADDING
        columns.forEachIndexed { index, col ->
            val centerX = x + colWidths[index] / 2
            canvas.drawText(col, centerX, startY + TABLE_HEADER_HEIGHT / 2 + 7f, headerTextPaint)
            x += colWidths[index]
        }

        // Draw rows
        val rowTextPaint = Paint().apply {
            color = TEXT_COLOR
            textSize = 20f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val altRowPaint = Paint().apply { color = ALTERNATE_ROW_COLOR; style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = BORDER_COLOR; style = Paint.Style.STROKE; strokeWidth = 1f }

        rankings.forEachIndexed { index, ranking ->
            val rowY = startY + TABLE_HEADER_HEIGHT + ROW_HEIGHT * index

            // Alternate row background
            if (index % 2 == 1) {
                canvas.drawRect(PADDING, rowY, IMAGE_WIDTH - PADDING, rowY + ROW_HEIGHT, altRowPaint)
            }

            // Rank medal color
            val rankPaint = Paint(rowTextPaint)
            when (ranking.rank) {
                1 -> {
                    val medalPaint = Paint().apply { color = GOLD_COLOR; style = Paint.Style.FILL }
                    canvas.drawCircle(PADDING + rankWidth / 2, rowY + ROW_HEIGHT / 2, 14f, medalPaint)
                    rankPaint.color = Color.WHITE
                    rankPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                2 -> {
                    val medalPaint = Paint().apply { color = SILVER_COLOR; style = Paint.Style.FILL }
                    canvas.drawCircle(PADDING + rankWidth / 2, rowY + ROW_HEIGHT / 2, 14f, medalPaint)
                    rankPaint.color = Color.WHITE
                    rankPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                3 -> {
                    val medalPaint = Paint().apply { color = BRONZE_COLOR; style = Paint.Style.FILL }
                    canvas.drawCircle(PADDING + rankWidth / 2, rowY + ROW_HEIGHT / 2, 14f, medalPaint)
                    rankPaint.color = Color.WHITE
                    rankPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                else -> {}
            }

            // Row data
            x = PADDING
            val values = mutableListOf(ranking.rank.toString(), ranking.memberName)
            for (score in ranking.gameScores) values.add(score.toString())
            values.add(ranking.totalScore.toString())
            if (handicapEnabled) {
                values.add(ranking.handicapTotal.toString())
                values.add(ranking.finalTotal.toString())
            }
            values.add(String.format("%.1f", ranking.average))

            values.forEachIndexed { colIdx, value ->
                val paint = if (colIdx == 0) rankPaint else rowTextPaint
                val centerX = x + colWidths[colIdx] / 2
                canvas.drawText(value, centerX, rowY + ROW_HEIGHT / 2 + 7f, paint)
                x += colWidths[colIdx]
            }

            // Row border bottom
            canvas.drawLine(PADDING, rowY + ROW_HEIGHT, IMAGE_WIDTH - PADDING, rowY + ROW_HEIGHT, borderPaint)
        }

        // Table border
        val tableBottom = startY + TABLE_HEADER_HEIGHT + ROW_HEIGHT * rankings.size
        canvas.drawRect(PADDING, startY, IMAGE_WIDTH - PADDING, tableBottom, borderPaint)

        // Vertical column lines
        x = PADDING
        colWidths.forEach { width ->
            canvas.drawLine(x, startY, x, tableBottom, borderPaint)
            x += width
        }
        canvas.drawLine(x, startY, x, tableBottom, borderPaint)
    }

    private fun drawFooter(canvas: Canvas, imageHeight: Float) {
        val footerPaint = Paint().apply {
            color = Color.parseColor("#9E9E9E")
            textSize = 18f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("볼링클럽 앱", IMAGE_WIDTH / 2f, imageHeight - 20f, footerPaint)
    }

    internal fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap, filename: String): Uri? {
        return try {
            val imagesDir = File(context.cacheDir, "shared_images")
            imagesDir.mkdirs()
            val imageFile = File(imagesDir, "$filename.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
