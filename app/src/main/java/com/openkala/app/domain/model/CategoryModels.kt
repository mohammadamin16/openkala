package com.openkala.app.domain.model

data class CategoryTabItem(
    val id: Long,
    val title: String,
    val code: String,
    val iconToken: String,
    val urlUri: String?
)

data class CategoryLeafItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val urlUri: String?
)

data class CategorySection(
    val id: Long,
    val title: String,
    val rowNumber: Int,
    val columnNumber: Int,
    val items: List<CategoryLeafItem>
)

data class CategoriesScreenData(
    val tabs: List<CategoryTabItem>,
    val selectedTabId: Long,
    val sectionsByTabId: Map<Long, List<CategorySection>>
)

data class CategoriesPayload(
    val data: CategoriesScreenData,
    val source: DataSource,
    val isStale: Boolean = false,
    val message: String? = null
)
