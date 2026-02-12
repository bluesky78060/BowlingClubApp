package com.bowlingclub.app.data.remote

import android.util.Log
import kotlin.math.abs

/**
 * OCR 응답에서 파싱된 한 행의 스코어 데이터.
 *
 * @param playerName 선수 이름
 * @param scores 인식된 게임 점수 리스트 (0-300)
 * @param confidence 평균 OCR 인식 신뢰도 (0.0 - 1.0)
 */
data class ParsedScoreRow(
    val playerName: String,
    val scores: List<Int>,
    val confidence: Float
)

/**
 * OCR 파싱 결과 전체를 담는 데이터 클래스.
 *
 * @param rows 파싱된 스코어 행 리스트
 * @param rawText 디버깅용 원본 텍스트
 * @param isSuccess 파싱 성공 여부
 * @param errorMessage 실패 시 에러 메시지
 */
data class OcrParseResult(
    val rows: List<ParsedScoreRow>,
    val rawText: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

/**
 * Google Cloud Vision OCR 응답을 구조화된 볼링 스코어 데이터로 파싱하는 유틸리티.
 *
 * 파싱 전략 (우선순위):
 * 1차: fullTextAnnotation 단어 수준 파싱 (단어 경계가 가장 정확 → 성명 전체 인식)
 * 2차: textAnnotations 좌표 기반 행 그룹핑 + 이름 사전 활용
 * 3차: fullTextAnnotation 줄바꿈 기반 파싱 (폴백)
 */
object OcrResultParser {

    private const val TAG = "OcrResultParser"
    private const val ROW_Y_THRESHOLD = 30f
    private const val DEFAULT_CONFIDENCE = 1.0f
    private const val MIN_SCORE = 0
    private const val MAX_SCORE = 300
    private const val MAX_TOTAL = 999
    private const val MERGE_GAP_FACTOR = 1.5f

    private val PURE_DIGITS_REGEX = Regex("^\\d+$")
    private val KOREAN_NAME_REGEX = Regex("^[가-힣]{2,4}$")
    private val SINGLE_KOREAN_CHAR_REGEX = Regex("^[가-힣]$")
    private val DIGITS_EXTRACT_REGEX = Regex("\\d+")
    private val KNOWN_NAME_EXTRACT_REGEX = Regex("[가-힣]{2,4}")

    private val HEADER_TEXTS: Set<String> = setOf(
        "이름", "성명", "합계", "총점", "핸디캡", "핸디", "순위",
        "게임", "1G", "2G", "3G", "4G", "5G", "6G",
        "점수", "평균", "AVG", "TOTAL", "NAME", "GAME",
        "RANK", "HDC", "SCORE", "SUM", "HIGH", "번호", "NO",
        "레인", "비고", "LANE", "NOTE", "클럽", "정기전",
        "1GAME", "2GAME", "3GAME", "4GAME", "IGAME", "소계"
    ).map { it.uppercase() }.toSet()

    fun parse(response: VisionAnnotateResponse): OcrParseResult {
        val fullAnnotation = response.fullTextAnnotation
        val rawText = fullAnnotation?.text ?: response.textAnnotations?.firstOrNull()?.description ?: ""

        Log.d(TAG, "=== OCR 파싱 시작 ===")
        Log.d(TAG, "rawText: ${rawText.take(500)}")

        // rawText에서 한글 이름 후보 사전 추출
        val knownNames = extractKnownNames(rawText)
        Log.d(TAG, "fullText 이름 후보: $knownNames")

        // 1차: fullTextAnnotation 단어 수준 파싱 (가장 정확한 단어 경계)
        val wordLevelResult = parseFromWordLevel(fullAnnotation, knownNames)
        if (wordLevelResult != null && wordLevelResult.isNotEmpty()) {
            Log.d(TAG, "=== 단어 수준 파싱 결과: ${wordLevelResult.size}행 ===")
            wordLevelResult.forEach { row ->
                Log.d(TAG, "  ${row.playerName}: ${row.scores}")
            }
            return OcrParseResult(
                rows = wordLevelResult,
                rawText = rawText,
                isSuccess = true
            )
        }

        // 2차: textAnnotations 좌표 기반 파싱 (이름 사전 활용)
        val annotations = response.textAnnotations
        if (!annotations.isNullOrEmpty()) {
            val wordAnnotations = annotations.drop(1)
            if (wordAnnotations.isNotEmpty()) {
                val groupedRows = groupFieldsByRow(wordAnnotations)
                val mergedRows = groupedRows.map { row -> mergeAdjacentAnnotations(row) }
                val parsedRows = parseAllRows(mergedRows, knownNames)

                Log.d(TAG, "=== 좌표 기반 파싱 결과: ${parsedRows.size}행 ===")
                parsedRows.forEach { row ->
                    Log.d(TAG, "  ${row.playerName}: ${row.scores}")
                }
                if (parsedRows.isNotEmpty()) {
                    return OcrParseResult(
                        rows = parsedRows,
                        rawText = rawText,
                        isSuccess = true
                    )
                }
            }
        }

        // 3차: fullText 줄바꿈 기반 파싱
        if (rawText.isNotBlank()) {
            val result = parseFromFullText(rawText)
            Log.d(TAG, "텍스트 기반 파싱 결과: ${result.rows.size}행")
            if (result.isSuccess) return result
        }

        return OcrParseResult(
            rows = emptyList(),
            rawText = rawText,
            isSuccess = false,
            errorMessage = "스코어 데이터를 찾을 수 없습니다"
        )
    }

    /**
     * rawText에서 한글 이름 후보(2-4글자)를 사전으로 추출합니다.
     * 헤더 텍스트는 제외됩니다.
     */
    private fun extractKnownNames(rawText: String): List<String> {
        return KNOWN_NAME_EXTRACT_REGEX.findAll(rawText)
            .map { it.value }
            .filter { !isHeaderText(it) }
            .distinct()
            .toList()
    }

    // ── 1차: fullTextAnnotation 단어 수준 파싱 ──

    /**
     * fullTextAnnotation의 pages → blocks → paragraphs → words 계층에서
     * 단어 단위로 추출하여 행 그룹핑 후 파싱합니다.
     *
     * Google Vision이 이미 문자를 단어로 그룹핑한 결과를 사용하므로,
     * "안성호", "안호준" 같은 성명이 개별 단어로 올바르게 분리됩니다.
     */
    private fun parseFromWordLevel(
        fullAnnotation: VisionFullTextAnnotation?,
        knownNames: List<String>
    ): List<ParsedScoreRow>? {
        val pages = fullAnnotation?.pages ?: return null
        val wordAnnotations = extractWordsFromPages(pages)
        if (wordAnnotations.isEmpty()) return null

        // 작은 글씨 필터링: 계산 메모(550+15, 362+184 등) 제거
        val filtered = filterSmallAnnotations(wordAnnotations)
        Log.d(TAG, "--- 단어 수준: ${wordAnnotations.size}개 → 크기 필터 후 ${filtered.size}개 ---")
        filtered.forEach { a ->
            Log.d(TAG, "  word: '${a.description}' Y=${calculateCenterY(a)} H=${getAnnotationHeight(a)}")
        }

        val groupedRows = groupFieldsByRow(filtered)
        Log.d(TAG, "단어 수준 행 그룹: ${groupedRows.size}행")
        groupedRows.forEachIndexed { i, row ->
            Log.d(TAG, "  Row $i: ${row.mapNotNull { it.description }.joinToString(", ")}")
        }

        return parseAllRows(groupedRows, knownNames)
    }

    /**
     * bounding box 높이가 중간값의 40% 미만인 어노테이션을 제거합니다.
     * 표 안에 작게 적힌 계산 과정(550+15, 362+184 등)을 걸러냅니다.
     * 한글 이름은 크기와 무관하게 보존합니다.
     */
    private fun filterSmallAnnotations(
        annotations: List<VisionTextAnnotation>
    ): List<VisionTextAnnotation> {
        val heights = annotations.mapNotNull { getAnnotationHeight(it) }.sorted()
        if (heights.size < 3) return annotations

        val medianHeight = heights[heights.size / 2]
        val minHeight = medianHeight * 0.4f

        Log.d(TAG, "filterSmall: median height=$medianHeight, minHeight=$minHeight")

        return annotations.filter { annotation ->
            val text = annotation.description?.trim() ?: ""
            // 한글이 포함된 텍스트는 무조건 보존 (이름)
            if (text.any { it in '가'..'힣' }) return@filter true

            val height = getAnnotationHeight(annotation)
            if (height == null || height >= minHeight) {
                true
            } else {
                Log.d(TAG, "filterSmall: 제거 '${text}' (height=$height < $minHeight)")
                false
            }
        }
    }

    /**
     * fullTextAnnotation의 word 계층을 flat한 VisionTextAnnotation 리스트로 변환합니다.
     * 각 VisionWord의 symbols를 합쳐서 단어 텍스트를 만들고, boundingBox를 보존합니다.
     */
    private fun extractWordsFromPages(pages: List<VisionPage>): List<VisionTextAnnotation> {
        val result = mutableListOf<VisionTextAnnotation>()
        for (page in pages) {
            val blocks = page.blocks ?: continue
            for (block in blocks) {
                val paragraphs = block.paragraphs ?: continue
                for (paragraph in paragraphs) {
                    val words = paragraph.words ?: continue
                    for (word in words) {
                        val text = word.symbols?.mapNotNull { it.text }?.joinToString("") ?: ""
                        if (text.isNotBlank()) {
                            result.add(
                                VisionTextAnnotation(
                                    locale = null,
                                    description = text,
                                    boundingPoly = word.boundingBox
                                )
                            )
                        }
                    }
                }
            }
        }
        return result
    }

    // ── 공통 행 파싱 + 고아 행 병합 ──

    /**
     * 행 그룹 리스트를 파싱하고, 고아 행(점수만 있는 행)을 인접한 이름 행에 병합합니다.
     */
    private fun parseAllRows(
        rows: List<List<VisionTextAnnotation>>,
        knownNames: List<String>
    ): List<ParsedScoreRow> {
        val parsedRows = mutableListOf<ParsedScoreRow>()
        val orphanScoreRows = mutableListOf<Pair<Float, List<Int>>>()
        val incompleteNameRows = mutableListOf<Pair<Float, ParsedScoreRow>>()

        rows.forEachIndexed { index, row ->
            val rowY = getRowCenterY(row)
            val result = parseRow(row, knownNames)
            if (result != null) {
                if (result.scores.size >= 3 && result.scores.any { it >= 100 }) {
                    parsedRows.add(result)
                } else {
                    incompleteNameRows.add(Pair(rowY, result))
                }
            } else {
                val orphanScores = extractOrphanScores(row)
                if (orphanScores.isNotEmpty()) {
                    orphanScoreRows.add(Pair(rowY, orphanScores))
                }
            }
        }

        // 불완전한 행에 고아 점수 병합
        for ((nameY, namedRow) in incompleteNameRows) {
            val nearbyScores = mutableListOf<Int>()
            nearbyScores.addAll(namedRow.scores)

            val iterator = orphanScoreRows.iterator()
            while (iterator.hasNext()) {
                val (orphanY, scores) = iterator.next()
                if (abs(orphanY - nameY) <= ROW_Y_THRESHOLD * 2) {
                    nearbyScores.addAll(scores)
                    iterator.remove()
                }
            }

            val hasHighScores = nearbyScores.any { it >= 100 }
            val cleanedScores = if (hasHighScores) {
                nearbyScores.filter { it >= 10 }
            } else {
                nearbyScores
            }
            val gameScores = filterTotalScore(cleanedScores)

            if (gameScores.isNotEmpty()) {
                Log.d(TAG, "merge orphan: ${namedRow.playerName} + orphans → $gameScores")
                parsedRows.add(
                    ParsedScoreRow(
                        playerName = namedRow.playerName,
                        scores = gameScores,
                        confidence = namedRow.confidence
                    )
                )
            }
        }

        return parsedRows
    }

    // ── 행 단위 파싱 ──

    /**
     * 한 행의 어노테이션에서 선수 이름과 점수를 추출합니다.
     *
     * 이름 인식 전략:
     * 1. 2-4글자 한글 → 직접 이름으로 채택
     * 2. 단일 한글 문자 → knownNames 사전과 대조하여 풀네임 복원
     * 3. 풀네임 매칭 실패 시 → 유효 점수(100+)가 2개 이상이면 단일 문자를 이름으로 승격
     */
    private fun parseRow(
        annotations: List<VisionTextAnnotation>,
        knownNames: List<String> = emptyList()
    ): ParsedScoreRow? {
        if (annotations.isEmpty()) return null

        val nameTokens = mutableListOf<String>()
        val singleKoreanChars = mutableListOf<String>()
        val scores = mutableListOf<Int>()

        annotations.forEach { annotation ->
            val text = annotation.description?.trim() ?: return@forEach
            if (text.isBlank()) return@forEach

            // 헤더 텍스트 무시
            if (isHeaderText(text)) return@forEach

            // 한국어 이름 (2-4글자)
            if (KOREAN_NAME_REGEX.matches(text)) {
                nameTokens.add(text)
                return@forEach
            }

            // 단일 한글 문자: 나중에 이름 사전 매칭 또는 승격
            if (SINGLE_KOREAN_CHAR_REGEX.matches(text)) {
                singleKoreanChars.add(text)
                return@forEach
            }

            // 순수 숫자 문자열
            if (PURE_DIGITS_REGEX.matches(text)) {
                val extracted = extractScores(text)
                scores.addAll(extracted)
                return@forEach
            }

            // 숫자+특수문자 혼합 (예: "189-203", "581+45")
            val digits = DIGITS_EXTRACT_REGEX.findAll(text).map { it.value }.toList()
            if (digits.isNotEmpty()) {
                digits.forEach { d ->
                    val extracted = extractScores(d)
                    scores.addAll(extracted)
                }
                return@forEach
            }

            // 한글+숫자 혼합 텍스트 (예: "안성호183")
            val koreanPart = text.takeWhile { it in '가'..'힣' }
            val digitPart = text.dropWhile { it in '가'..'힣' }
            if (koreanPart.length >= 2) {
                nameTokens.add(koreanPart)
                if (digitPart.isNotBlank()) {
                    val digs = DIGITS_EXTRACT_REGEX.findAll(digitPart).map { it.value }.toList()
                    digs.forEach { d -> scores.addAll(extractScores(d)) }
                }
                return@forEach
            }

            // 그 외 텍스트 (영문 이름 등)
            if (text.any { it.isLetter() } && !text.all { it.isDigit() }) {
                nameTokens.add(text)
            }
        }

        // 단일 한글 문자 처리: 유효 점수가 충분하면 그대로 이름으로 승격
        // (findMatchingName 사용하지 않음 - 동명이의 이성 문제 방지, autoMatchMembers에서 처리)
        if (nameTokens.isEmpty() && singleKoreanChars.isNotEmpty()) {
            val combined = singleKoreanChars.joinToString("")
            val highScoreCount = scores.count { it in 100..MAX_SCORE }
            if (highScoreCount >= 2) {
                nameTokens.add(combined)
                Log.d(TAG, "parseRow: 단일 한글 '$combined' → 이름으로 승격 (유효점수 ${highScoreCount}개)")
            }
        }

        val playerName = nameTokens.joinToString("").trim()
        if (playerName.isBlank() || scores.isEmpty()) return null

        Log.d(TAG, "parseRow: name=$playerName, rawScores=$scores")

        // 1단계: 행/레인 번호 필터링
        val hasHighScores = scores.any { it >= 100 }
        val cleanedScores = if (hasHighScores) {
            val filtered = scores.filter { it >= 10 }
            Log.d(TAG, "parseRow: row/lane filter: $scores → $filtered")
            filtered
        } else {
            scores
        }
        if (cleanedScores.isEmpty()) return null

        // 2단계: 합계 제거
        val gameScores = filterTotalScore(cleanedScores)

        Log.d(TAG, "parseRow: name=$playerName, cleaned=$cleanedScores, final=$gameScores")

        return ParsedScoreRow(
            playerName = playerName,
            scores = gameScores,
            confidence = DEFAULT_CONFIDENCE
        )
    }

    /**
     * 단일 한글 문자(들)를 knownNames 사전에서 매칭하여 풀네임을 찾습니다.
     *
     * 매칭 규칙:
     * 1. 정확 매치: combined가 knownNames에 있으면 그대로 반환
     * 2. 접두사 매치: combined로 시작하는 이름이 1개면 해당 이름 반환
     * 3. 포함 매치: combined를 포함하는 이름이 1개면 해당 이름 반환
     */
    private fun findMatchingName(combined: String, knownNames: List<String>): String? {
        if (combined.isBlank() || knownNames.isEmpty()) return null

        // 정확 매치
        val exact = knownNames.firstOrNull { it == combined }
        if (exact != null) return exact

        // 접두사 매치 (예: "안" → "안성호" 또는 "안호준")
        val prefixMatches = knownNames.filter { it.startsWith(combined) }
        if (prefixMatches.size == 1) return prefixMatches.first()

        // 포함 매치
        val containsMatches = knownNames.filter { it.contains(combined) }
        if (containsMatches.size == 1) return containsMatches.first()

        // 여러 후보가 있으면 모호하므로 null 반환 (단일 문자 그대로 사용)
        if (prefixMatches.isNotEmpty()) {
            Log.d(TAG, "findMatchingName: '$combined' → 다중 후보: $prefixMatches (모호, 매칭 안 함)")
        }
        return null
    }

    // ── 행 그룹핑 ──

    private fun groupFieldsByRow(annotations: List<VisionTextAnnotation>): List<List<VisionTextAnnotation>> {
        if (annotations.isEmpty()) return emptyList()

        val annotationsWithY = annotations.mapNotNull { annotation ->
            val centerY = calculateCenterY(annotation)
            if (centerY != null) Pair(annotation, centerY) else null
        }.sortedBy { it.second }

        if (annotationsWithY.isEmpty()) return emptyList()

        val rows = mutableListOf<MutableList<VisionTextAnnotation>>()
        var currentRow = mutableListOf(annotationsWithY.first().first)
        var currentRowY = annotationsWithY.first().second

        for (i in 1 until annotationsWithY.size) {
            val (annotation, y) = annotationsWithY[i]
            if (abs(y - currentRowY) <= ROW_Y_THRESHOLD) {
                currentRow.add(annotation)
            } else {
                rows.add(currentRow)
                currentRow = mutableListOf(annotation)
                currentRowY = y
            }
        }
        rows.add(currentRow)

        return rows.map { row ->
            row.sortedBy { annotation ->
                annotation.boundingPoly?.vertices?.firstOrNull()?.x?.toFloat() ?: 0f
            }
        }
    }

    /** 행의 평균 Y좌표를 계산합니다. */
    private fun getRowCenterY(row: List<VisionTextAnnotation>): Float {
        val ys = row.mapNotNull { calculateCenterY(it) }
        return if (ys.isNotEmpty()) ys.average().toFloat() else 0f
    }

    // ── 인접 어노테이션 병합 (textAnnotations 전용) ──

    /**
     * X축으로 인접한 단일 문자 어노테이션만 같은 타입끼리 병합합니다.
     * "2","1","4" → "214", "안","성","호" → "안성호"
     */
    private fun mergeAdjacentAnnotations(
        row: List<VisionTextAnnotation>
    ): List<VisionTextAnnotation> {
        if (row.size <= 1) return row

        val result = mutableListOf<VisionTextAnnotation>()
        var currentGroup = mutableListOf(row.first())

        for (i in 1 until row.size) {
            val prev = currentGroup.last()
            val curr = row[i]

            if (shouldMergeAnnotations(prev, curr)) {
                currentGroup.add(curr)
            } else {
                result.add(mergeGroup(currentGroup))
                currentGroup = mutableListOf(curr)
            }
        }
        result.add(mergeGroup(currentGroup))

        Log.d(TAG, "mergeAdjacent: ${row.size} annotations → ${result.size} merged tokens: " +
                result.mapNotNull { it.description }.joinToString(", "))

        return result
    }

    private fun shouldMergeAnnotations(
        prev: VisionTextAnnotation,
        curr: VisionTextAnnotation
    ): Boolean {
        val prevText = prev.description?.trim() ?: return false
        val currText = curr.description?.trim() ?: return false

        if (prevText.length != 1 || currText.length != 1) return false

        val prevIsDigit = prevText[0].isDigit()
        val currIsDigit = currText[0].isDigit()
        val prevIsKorean = prevText[0] in '가'..'힣'
        val currIsKorean = currText[0] in '가'..'힣'

        val sameType = (prevIsDigit && currIsDigit) || (prevIsKorean && currIsKorean)
        if (!sameType) return false

        val prevRight = getRightX(prev) ?: return false
        val currLeft = getLeftX(curr) ?: return false
        val prevWidth = getAnnotationWidth(prev) ?: return false
        val currWidth = getAnnotationWidth(curr) ?: return false

        val gap = currLeft - prevRight
        val avgCharWidth = (prevWidth + currWidth) / 2f
        val threshold = avgCharWidth * MERGE_GAP_FACTOR

        return gap < threshold && gap >= -avgCharWidth * 0.5f
    }

    private fun mergeGroup(group: List<VisionTextAnnotation>): VisionTextAnnotation {
        if (group.size == 1) return group.first()

        val mergedText = group.mapNotNull { it.description }.joinToString("")

        val allVertices = group.flatMap { it.boundingPoly?.vertices ?: emptyList() }
        val minX = allVertices.mapNotNull { it.x }.minOrNull() ?: 0
        val maxX = allVertices.mapNotNull { it.x }.maxOrNull() ?: 0
        val minY = allVertices.mapNotNull { it.y }.minOrNull() ?: 0
        val maxY = allVertices.mapNotNull { it.y }.maxOrNull() ?: 0

        val mergedPoly = VisionBoundingPoly(
            vertices = listOf(
                VisionVertex(minX, minY),
                VisionVertex(maxX, minY),
                VisionVertex(maxX, maxY),
                VisionVertex(minX, maxY)
            )
        )

        return VisionTextAnnotation(
            locale = group.first().locale,
            description = mergedText,
            boundingPoly = mergedPoly
        )
    }

    // ── 고아 점수 추출 ──

    private fun extractOrphanScores(annotations: List<VisionTextAnnotation>): List<Int> {
        val scores = mutableListOf<Int>()
        annotations.forEach { annotation ->
            val text = annotation.description?.trim() ?: return@forEach
            if (text.isBlank() || isHeaderText(text)) return@forEach
            if (KOREAN_NAME_REGEX.matches(text)) return@forEach

            if (PURE_DIGITS_REGEX.matches(text)) {
                scores.addAll(extractScores(text))
            } else {
                val digits = DIGITS_EXTRACT_REGEX.findAll(text).map { it.value }.toList()
                digits.forEach { d -> scores.addAll(extractScores(d)) }
            }
        }
        return scores
    }

    // ── 좌표 유틸리티 ──

    private fun getRightX(annotation: VisionTextAnnotation): Float? {
        val vertices = annotation.boundingPoly?.vertices ?: return null
        return if (vertices.size >= 2) {
            vertices[1].x?.toFloat()
        } else {
            vertices.mapNotNull { it.x }.maxOrNull()?.toFloat()
        }
    }

    private fun getLeftX(annotation: VisionTextAnnotation): Float? {
        val vertices = annotation.boundingPoly?.vertices ?: return null
        return vertices[0].x?.toFloat()
    }

    private fun getAnnotationWidth(annotation: VisionTextAnnotation): Float? {
        val left = getLeftX(annotation) ?: return null
        val right = getRightX(annotation) ?: return null
        val width = right - left
        return if (width > 0f) width else null
    }

    private fun calculateCenterY(annotation: VisionTextAnnotation): Float? {
        val vertices = annotation.boundingPoly?.vertices
        if (vertices.isNullOrEmpty()) return null

        return if (vertices.size >= 4) {
            val topY = vertices[0].y?.toFloat() ?: return null
            val bottomY = vertices[2].y?.toFloat() ?: vertices[3].y?.toFloat() ?: return null
            (topY + bottomY) / 2f
        } else if (vertices.size >= 2) {
            val y0 = vertices[0].y?.toFloat() ?: return null
            val y1 = vertices[1].y?.toFloat() ?: return null
            (y0 + y1) / 2f
        } else {
            vertices[0].y?.toFloat()
        }
    }

    /** 어노테이션의 높이를 반환합니다. */
    private fun getAnnotationHeight(annotation: VisionTextAnnotation): Float? {
        val vertices = annotation.boundingPoly?.vertices
        if (vertices.isNullOrEmpty() || vertices.size < 4) return null
        val topY = vertices[0].y?.toFloat() ?: return null
        val bottomY = vertices[2].y?.toFloat() ?: vertices[3].y?.toFloat() ?: return null
        val height = bottomY - topY
        return if (height > 0f) height else null
    }

    // ── 점수 추출/분리 ──

    private fun extractScores(text: String): List<Int> {
        val value = text.toIntOrNull()

        if (value != null && value in MIN_SCORE..MAX_SCORE) {
            return listOf(value)
        }

        if (value != null && text.length == 3 && value in (MAX_SCORE + 1)..MAX_TOTAL) {
            return listOf(value)
        }

        if (text.length >= 4 && text.all { it.isDigit() }) {
            return splitConcatenatedScores(text)
        }

        return emptyList()
    }

    private fun splitConcatenatedScores(text: String): List<Int> {
        val results = mutableListOf<List<Int>>()
        findScoreSplits(text, 0, mutableListOf(), results)

        if (results.isEmpty()) return emptyList()

        return results.maxByOrNull { split ->
            var total = 0
            for (score in split) {
                total += when {
                    score in 100..MAX_SCORE -> 1000
                    score in (MAX_SCORE + 1)..MAX_TOTAL -> 200
                    score in 50..99 -> 500
                    score in 10..49 -> 100
                    score in 1..9 -> -500
                    else -> -200
                }
            }
            total
        } ?: emptyList()
    }

    private fun findScoreSplits(
        text: String,
        startIndex: Int,
        current: MutableList<Int>,
        results: MutableList<List<Int>>
    ) {
        if (startIndex >= text.length) {
            if (current.isNotEmpty()) {
                results.add(current.toList())
            }
            return
        }

        for (len in 3 downTo 1) {
            val endIndex = startIndex + len
            if (endIndex > text.length) continue

            val substr = text.substring(startIndex, endIndex)
            val num = substr.toIntOrNull() ?: continue

            if (num in MIN_SCORE..MAX_TOTAL) {
                if (substr.length > 1 && substr.startsWith("0")) continue

                current.add(num)
                findScoreSplits(text, endIndex, current, results)
                current.removeAt(current.size - 1)
            }
        }
    }

    // ── 합계 필터링 ──

    private fun filterTotalScore(scores: List<Int>): List<Int> {
        val gameOnly = scores.filter { it <= MAX_SCORE }
        val removed = scores.filter { it > MAX_SCORE }
        if (removed.isNotEmpty()) {
            Log.d(TAG, "filterTotal: 합계 제거(>300): $removed")
        }

        if (gameOnly.size <= 3) return gameOnly

        val sorted = gameOnly.sortedDescending()
        val largest = sorted.first()
        val rest = sorted.drop(1)
        val sumOfRest = rest.sum()

        val isLikelyTotal = sumOfRest > 0 &&
                largest >= (sumOfRest * 0.85).toInt() &&
                largest <= (sumOfRest * 1.15).toInt()

        return if (isLikelyTotal) {
            Log.d(TAG, "filterTotal: $largest ≈ sum($rest)=$sumOfRest → 합계로 제거")
            rest.take(3).sorted()
        } else {
            Log.d(TAG, "filterTotal: $largest ≠ sum($rest)=$sumOfRest → 상위 3개 선택")
            sorted.take(3).sorted()
        }
    }

    // ── 유틸리티 ──

    private fun isHeaderText(text: String): Boolean {
        val upper = text.uppercase()
        if (upper in HEADER_TEXTS) return true
        if (upper.contains("GAME") || upper.contains("GARNE") || upper.contains("GANE")) return true
        return false
    }

    // ── 3차: fullText 줄바꿈 기반 파싱 (폴백) ──

    private fun parseFromFullText(fullText: String): OcrParseResult {
        val lines = fullText.split("\n").filter { it.isNotBlank() }
        val rows = mutableListOf<ParsedScoreRow>()

        for (line in lines) {
            val tokens = line.trim().split("\\s+".toRegex())
            val nameTokens = mutableListOf<String>()
            val scores = mutableListOf<Int>()

            for (token in tokens) {
                val cleaned = token.trim()
                if (cleaned.isBlank() || isHeaderText(cleaned)) continue

                if (KOREAN_NAME_REGEX.matches(cleaned)) {
                    nameTokens.add(cleaned)
                    continue
                }

                val digits = DIGITS_EXTRACT_REGEX.findAll(cleaned).map { it.value }.toList()
                for (d in digits) {
                    scores.addAll(extractScores(d))
                }

                if (digits.isEmpty() && cleaned.any { it.isLetter() }) {
                    if (!SINGLE_KOREAN_CHAR_REGEX.matches(cleaned)) {
                        nameTokens.add(cleaned)
                    }
                }
            }

            val playerName = nameTokens.joinToString(" ").trim()
            if (playerName.isNotBlank() && scores.isNotEmpty()) {
                val hasHighScores = scores.any { it >= 100 }
                val cleanedScores = if (hasHighScores) {
                    scores.filter { it >= 10 }
                } else {
                    scores
                }
                if (cleanedScores.isEmpty()) continue

                val gameScores = filterTotalScore(cleanedScores)
                rows.add(
                    ParsedScoreRow(
                        playerName = playerName,
                        scores = gameScores,
                        confidence = DEFAULT_CONFIDENCE
                    )
                )
            }
        }

        return OcrParseResult(
            rows = rows,
            rawText = fullText,
            isSuccess = rows.isNotEmpty(),
            errorMessage = if (rows.isEmpty()) "스코어 데이터를 찾을 수 없습니다" else null
        )
    }
}
