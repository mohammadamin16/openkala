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

    @GET("v1/widget-factory/widget/66/")
    suspend fun getHomeCategoriesWidget(
        @Query("endpoint") endpoint: String = "/v1/widget-factory/widget/66/",
        @Query("sa_user") saUser: Boolean = true
    ): JsonObject

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

    @GET("v1/autocomplete/")
    suspend fun getAutocomplete(
        @Query("q") query: String? = null
    ): JsonObject

    @GET("v1/dictionaries/")
    suspend fun getDictionaries(
        @Query("types[0]") type: String,
        @Query("hashes[0]") hash: String = ""
    ): JsonObject

    @GET("v1/search/")
    suspend fun getSearch(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("sort") sort: Int? = null
    ): JsonObject

    @GET("v1/categories/{categoryCode}/search/")
    suspend fun getCategorySearch(
        @Path("categoryCode") categoryCode: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("sort") sort: Int? = null
    ): JsonObject
}
