package com.openkala.app.data.network

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DigikalaApiService {
    @GET("v1/")
    suspend fun getHome(): JsonObject

    @GET("v1/super-app/pillars/")
    suspend fun getSuperAppPillars(): JsonObject

    @GET("v2/product/{productId}/")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): JsonObject

    @GET("v1/product/{productId}/comments/")
    suspend fun getProductComments(
        @Path("productId") productId: Long,
        @Query("page") page: Int = 1
    ): JsonObject

    @GET("v1/product/{productId}/questions/")
    suspend fun getProductQuestions(
        @Path("productId") productId: Long,
        @Query("page") page: Int = 1
    ): JsonObject
}
