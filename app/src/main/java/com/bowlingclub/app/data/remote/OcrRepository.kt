package com.bowlingclub.app.data.remote

import android.util.Base64
import com.bowlingclub.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepository @Inject constructor(
    private val ocrApiService: OcrApiService
) {
    suspend fun recognizeScoreBoard(
        imageBytes: ByteArray,
        format: String = "jpg"
    ): Result<VisionAnnotateResponse> {
        return try {
            val apiKey = BuildConfig.GOOGLE_VISION_API_KEY
            if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
                return Result.failure(
                    Exception("Google Vision API 키가 설정되지 않았습니다.")
                )
            }

            val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val request = VisionRequest(
                requests = listOf(
                    VisionAnnotateRequest(
                        image = VisionImage(content = base64Data),
                        features = listOf(VisionFeature(type = "DOCUMENT_TEXT_DETECTION")),
                        imageContext = VisionImageContext(
                            languageHints = listOf("ko"),
                            textDetectionParams = VisionTextDetectionParams(
                                enableTextDetectionConfidenceScore = true
                            )
                        )
                    )
                )
            )

            val response = ocrApiService.annotateImage(
                apiKey = apiKey,
                body = request
            )

            if (response.isSuccessful && response.body() != null) {
                val visionResponse = response.body()!!
                val firstResult = visionResponse.responses?.firstOrNull()

                if (firstResult?.error != null) {
                    Result.failure(
                        Exception("Vision API 오류: ${firstResult.error.message}")
                    )
                } else if (firstResult != null) {
                    Result.success(firstResult)
                } else {
                    Result.failure(Exception("Vision API 응답이 비어있습니다"))
                }
            } else {
                Result.failure(
                    Exception("Vision API 호출 실패: ${response.code()} ${response.message()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Vision API 연결 실패: ${e.message}"))
        }
    }
}
