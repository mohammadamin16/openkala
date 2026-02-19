package com.openkala.app.data.repository

import com.openkala.app.data.cache.SearchResultsCacheStore
import com.openkala.app.data.network.DigikalaApiService
import com.openkala.app.domain.mapper.mapSearchRecommendations
import com.openkala.app.domain.mapper.mapSearchResults
import com.openkala.app.domain.model.DataSource
import com.openkala.app.domain.model.SearchResultsData
import com.openkala.app.domain.model.SearchResultsPayload
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

@Singleton
class SearchResultsRepository @Inject constructor(
    private val apiService: DigikalaApiService,
    private val cacheStore: SearchResultsCacheStore,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {

    @Volatile
    private var memoryBySignature: Map<String, SearchResultsData> = emptyMap()

    fun streamFirstPage(
        query: String,
        categoryCode: String?,
        sort: Int?
    ): Flow<SearchResultsPayload> = flow {
        val signature = signature(query, categoryCode, sort)
        var cachedData = memoryBySignature[signature]

        if (cachedData != null) {
            emit(SearchResultsPayload(data = cachedData, source = DataSource.CACHE))
        } else {
            val cached = cacheStore.read(signature)
            if (cached != null) {
                cachedData = runCatching {
                    decodeCached(query = query, categoryCode = categoryCode, content = cached.json)
                }.getOrNull()
                if (cachedData != null) {
                    memoryBySignature = memoryBySignature + (signature to cachedData)
                    emit(SearchResultsPayload(data = cachedData, source = DataSource.CACHE))
                }
            }
        }

        runCatching {
            val fresh = fetchFirstPage(
                query = query,
                categoryCode = categoryCode,
                sort = sort
            )

            cacheStore.write(
                signature = signature,
                json = json.encodeToString(JsonObject.serializer(), fresh.cacheEnvelope)
            )
            memoryBySignature = memoryBySignature + (signature to fresh.data)

            emit(
                SearchResultsPayload(
                    data = fresh.data,
                    source = DataSource.NETWORK
                )
            )
        }.onFailure { error ->
            if (cachedData != null) {
                emit(
                    SearchResultsPayload(
                        data = cachedData,
                        source = DataSource.NETWORK,
                        isStale = true,
                        message = error.message ?: "خطا در به‌روزرسانی نتایج"
                    )
                )
            } else {
                throw error
            }
        }
    }.flowOn(ioDispatcher)

    suspend fun loadNextPage(
        query: String,
        categoryCode: String?,
        page: Int,
        sort: Int?
    ): SearchResultsData {
        val response = if (categoryCode.isNullOrBlank()) {
            apiService.getSearch(query = query, page = page, sort = sort)
        } else {
            apiService.getCategorySearch(
                categoryCode = categoryCode,
                query = query,
                page = page,
                sort = sort
            )
        }

        return mapSearchResults(
            query = query,
            categoryCode = categoryCode,
            response = response,
            recommendations = emptyList()
        )
    }

    private suspend fun fetchFirstPage(
        query: String,
        categoryCode: String?,
        sort: Int?
    ): FreshEnvelope = coroutineScope {
        val searchDeferred = async {
            if (categoryCode.isNullOrBlank()) {
                apiService.getSearch(query = query, page = 1, sort = sort)
            } else {
                apiService.getCategorySearch(
                    categoryCode = categoryCode,
                    query = query,
                    page = 1,
                    sort = sort
                )
            }
        }

        val autocompleteDeferred = async {
            if (categoryCode.isNullOrBlank()) {
                runCatching { apiService.getAutocomplete(query = query) }.getOrNull()
            } else {
                null
            }
        }

        val searchResponse = searchDeferred.await()
        val autocompleteResponse = autocompleteDeferred.await()
        val recommendations = autocompleteResponse?.let { mapSearchRecommendations(it) }.orEmpty()

        FreshEnvelope(
            data = mapSearchResults(
                query = query,
                categoryCode = categoryCode,
                response = searchResponse,
                recommendations = recommendations
            ),
            cacheEnvelope = buildJsonObject {
                put("search", searchResponse)
                if (autocompleteResponse != null) {
                    put("autocomplete", autocompleteResponse)
                }
            }
        )
    }

    private fun decodeCached(
        query: String,
        categoryCode: String?,
        content: String
    ): SearchResultsData {
        val root = json.parseToJsonElement(content).jsonObject
        val searchObject = root["search"]?.jsonObject ?: root
        val recommendations = root["autocomplete"]?.jsonObject?.let { mapSearchRecommendations(it) }.orEmpty()

        return mapSearchResults(
            query = query,
            categoryCode = categoryCode,
            response = searchObject,
            recommendations = recommendations
        )
    }

    private fun signature(query: String, categoryCode: String?, sort: Int?): String {
        return listOf(query.trim().lowercase(), categoryCode.orEmpty(), sort?.toString().orEmpty())
            .joinToString("|")
    }

    private data class FreshEnvelope(
        val data: SearchResultsData,
        val cacheEnvelope: JsonObject
    )
}
