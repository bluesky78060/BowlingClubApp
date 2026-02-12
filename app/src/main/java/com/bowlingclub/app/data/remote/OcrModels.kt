package com.bowlingclub.app.data.remote

// Google Cloud Vision API Request Models
data class VisionRequest(
    val requests: List<VisionAnnotateRequest>
)

data class VisionAnnotateRequest(
    val image: VisionImage,
    val features: List<VisionFeature>,
    val imageContext: VisionImageContext? = null
)

data class VisionImageContext(
    val languageHints: List<String>? = null,
    val textDetectionParams: VisionTextDetectionParams? = null
)

data class VisionTextDetectionParams(
    val enableTextDetectionConfidenceScore: Boolean = true,
    val advancedOcrOptions: List<String>? = null
)

data class VisionImage(
    val content: String  // base64 encoded image
)

data class VisionFeature(
    val type: String = "DOCUMENT_TEXT_DETECTION",
    val maxResults: Int = 50
)

// Google Cloud Vision API Response Models
data class VisionResponse(
    val responses: List<VisionAnnotateResponse>?
)

data class VisionAnnotateResponse(
    val textAnnotations: List<VisionTextAnnotation>?,
    val fullTextAnnotation: VisionFullTextAnnotation?,
    val error: VisionError?
)

data class VisionTextAnnotation(
    val locale: String?,
    val description: String?,
    val boundingPoly: VisionBoundingPoly?
)

data class VisionFullTextAnnotation(
    val text: String?,
    val pages: List<VisionPage>? = null
)

data class VisionPage(
    val width: Int?,
    val height: Int?,
    val blocks: List<VisionBlock>?,
    val confidence: Float?
)

data class VisionBlock(
    val paragraphs: List<VisionParagraph>?,
    val boundingBox: VisionBoundingPoly?,
    val confidence: Float?
)

data class VisionParagraph(
    val words: List<VisionWord>?,
    val boundingBox: VisionBoundingPoly?,
    val confidence: Float?
)

data class VisionWord(
    val symbols: List<VisionSymbol>?,
    val boundingBox: VisionBoundingPoly?,
    val confidence: Float?
)

data class VisionSymbol(
    val text: String?,
    val boundingBox: VisionBoundingPoly?,
    val confidence: Float?
)

data class VisionBoundingPoly(
    val vertices: List<VisionVertex>?
)

data class VisionVertex(
    val x: Int?,
    val y: Int?
)

data class VisionError(
    val code: Int?,
    val message: String?
)
