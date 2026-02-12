package com.bowlingclub.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface OcrApiService {
    @POST("v1/images:annotate")
    suspend fun annotateImage(
        @Query("key") apiKey: String,
        @Body body: VisionRequest
    ): Response<VisionResponse>
}
