package com.openkala.app.data.network

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET

interface DigikalaApiService {
    @GET("v1/")
    suspend fun getHome(): JsonObject

    @GET("v1/super-app/pillars/")
    suspend fun getSuperAppPillars(): JsonObject
}

