package com.openkala.app.domain.model

data class SearchTrendItem(
    val keyword: String,
    val uri: String?
)

data class SearchSuggestionItem(
    val keyword: String,
    val uri: String?
)

data class SearchEntryData(
    val trends: List<SearchTrendItem>,
    val suggestions: List<SearchSuggestionItem>
)
