package com.openkala.app.data.repository

import com.openkala.app.data.network.DigikalaApiService
import com.openkala.app.domain.mapper.mapAutocompleteSuggestions
import com.openkala.app.domain.mapper.mapAutocompleteTrends
import com.openkala.app.domain.model.SearchEntryData
import com.openkala.app.domain.model.SearchSuggestionItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: DigikalaApiService,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun fetchHotTrends(): SearchEntryData = withContext(ioDispatcher) {
        mapAutocompleteTrends(apiService.getAutocomplete(query = null))
    }

    suspend fun fetchSuggestions(query: String): List<SearchSuggestionItem> = withContext(ioDispatcher) {
        mapAutocompleteSuggestions(apiService.getAutocomplete(query = query))
    }
}
