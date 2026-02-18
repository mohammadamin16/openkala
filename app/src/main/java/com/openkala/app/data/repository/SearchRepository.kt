package com.openkala.app.data.repository

import com.openkala.app.data.cache.SearchCacheStore
import com.openkala.app.data.network.DigikalaApiService
import com.openkala.app.domain.mapper.mapAutocompleteSuggestions
import com.openkala.app.domain.mapper.mapAutocompleteTrends
import com.openkala.app.domain.model.SearchEntryData
import com.openkala.app.domain.model.SearchSuggestionItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: DigikalaApiService,
    private val cacheStore: SearchCacheStore,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun readCachedHotTrends(): SearchEntryData? = withContext(ioDispatcher) {
        val cached = cacheStore.readTrends() ?: return@withContext null
        runCatching {
            mapAutocompleteTrends(parse(cached.trendsJson))
        }.getOrNull()
    }

    suspend fun fetchHotTrends(): SearchEntryData = withContext(ioDispatcher) {
        val response = apiService.getAutocomplete(query = null)
        cacheStore.writeTrends(
            trendsJson = json.encodeToString(JsonObject.serializer(), response)
        )
        mapAutocompleteTrends(response)
    }

    suspend fun fetchSuggestions(query: String): List<SearchSuggestionItem> = withContext(ioDispatcher) {
        mapAutocompleteSuggestions(apiService.getAutocomplete(query = query))
    }

    private fun parse(content: String): JsonObject {
        return json.parseToJsonElement(content).jsonObject
    }
}
