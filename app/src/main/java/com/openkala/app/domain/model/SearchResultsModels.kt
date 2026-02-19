package com.openkala.app.domain.model

data class SearchFilterChip(
    val id: String,
    val title: String,
    val selected: Boolean = false
)

data class SearchProductItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val rating: Double?,
    val price: Long?,
    val rrpPrice: Long?,
    val discountPercent: Int?,
    val shippingText: String?,
    val colorCount: Int
)

data class SearchRecommendationItem(
    val keyword: String,
    val categoryCode: String,
    val categoryTitle: String
)

data class SearchSortItem(
    val id: Int,
    val title: String,
    val selected: Boolean
)

data class SearchResultsHeader(
    val query: String,
    val categoryCode: String?,
    val totalItems: Int,
    val chips: List<SearchFilterChip>,
    val sortOptions: List<SearchSortItem>
)

data class SearchResultsPage(
    val products: List<SearchProductItem>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val sortOptions: List<SearchSortItem>
)

data class SearchResultsData(
    val header: SearchResultsHeader,
    val recommendations: List<SearchRecommendationItem>,
    val page: SearchResultsPage
)

data class SearchResultsPayload(
    val data: SearchResultsData,
    val source: DataSource,
    val isStale: Boolean = false,
    val message: String? = null
)
