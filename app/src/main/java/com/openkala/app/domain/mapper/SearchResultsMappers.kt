package com.openkala.app.domain.mapper

import com.openkala.app.domain.model.SearchFilterChip
import com.openkala.app.domain.model.SearchProductItem
import com.openkala.app.domain.model.SearchRecommendationItem
import com.openkala.app.domain.model.SearchResultsData
import com.openkala.app.domain.model.SearchResultsHeader
import com.openkala.app.domain.model.SearchResultsPage
import com.openkala.app.domain.model.SearchSortItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

fun mapSearchResults(
    query: String,
    categoryCode: String?,
    response: JsonObject,
    recommendations: List<SearchRecommendationItem>
): SearchResultsData {
    val data = response.objectAt("data")
    val pager = data.objectAt("pager")

    val defaultSort = data.objectAt("sort").intAt("default")
    val sortOptions = data.arrayAt("sort_options").mapNotNull { element ->
        val obj = element.asObjectOrNull() ?: return@mapNotNull null
        val id = obj.intAt("id")
        SearchSortItem(
            id = id,
            title = obj.stringAt("title_fa"),
            selected = id == defaultSort
        )
    }

    val products = data.arrayAt("products").mapNotNull { it.toSearchProductItem() }

    val header = SearchResultsHeader(
        query = query,
        categoryCode = categoryCode,
        totalItems = pager.intAt("total_items"),
        chips = buildFilterChips(sortOptions.firstOrNull { it.selected }?.title.orEmpty()),
        sortOptions = sortOptions
    )

    return SearchResultsData(
        header = header,
        recommendations = recommendations,
        page = SearchResultsPage(
            products = products,
            currentPage = pager.intAt("current_page").coerceAtLeast(1),
            totalPages = pager.intAt("total_pages").coerceAtLeast(1),
            totalItems = pager.intAt("total_items"),
            sortOptions = sortOptions
        )
    )
}

fun mapSearchRecommendations(response: JsonObject): List<SearchRecommendationItem> {
    val data = response.objectAt("data")
    return data.arrayAt("categories").mapNotNull { element ->
        val obj = element.asObjectOrNull() ?: return@mapNotNull null
        val keyword = obj.stringAt("keyword")
        val category = obj.objectAt("category")
        val code = category.stringAt("code")
        if (keyword.isBlank() || code.isBlank()) return@mapNotNull null

        SearchRecommendationItem(
            keyword = keyword,
            categoryCode = code,
            categoryTitle = category.stringAt("title_fa")
        )
    }
}

private fun buildFilterChips(selectedSortTitle: String): List<SearchFilterChip> {
    val sortTitle = selectedSortTitle.ifBlank { "مرتبط‌ترین" }
    return listOf(
        SearchFilterChip(id = "ai", title = "جستجوی توصیفی", selected = true),
        SearchFilterChip(id = "sort", title = sortTitle),
        SearchFilterChip(id = "filter", title = "فیلتر"),
        SearchFilterChip(id = "price", title = "محدوده قیمت")
    )
}

private fun JsonElement.toSearchProductItem(): SearchProductItem? {
    val obj = asObjectOrNull() ?: return null
    val defaultVariant = obj.objectAt("default_variant")
    val price = defaultVariant.objectAt("price")

    val image = obj.objectAt("images")
        .objectAt("main")
        .arrayAt("webp_url")
        .firstString()
        .ifBlank {
            obj.objectAt("images")
                .objectAt("main")
                .arrayAt("url")
                .firstString()
        }

    val rawRate = obj.objectAt("rating").doubleAtOrNull("rate")
    val normalizedRate = rawRate?.let {
        if (it > 5.0) it / 20.0 else it
    }

    val shippingText = defaultVariant
        .objectAt("digiplus")
        .stringAt("fast_shipping_text")
        .ifBlank {
            defaultVariant.objectAt("shipment_methods").stringAt("description")
        }
        .ifBlank { null }

    return SearchProductItem(
        id = obj.longAt("id"),
        title = obj.stringAt("title_fa"),
        imageUrl = image,
        rating = normalizedRate,
        price = price.longAtOrNull("selling_price"),
        rrpPrice = price.longAtOrNull("rrp_price"),
        discountPercent = price.intAtOrNull("discount_percent"),
        shippingText = shippingText,
        colorCount = obj.arrayAt("colors").size
    )
}

private fun JsonObject.objectAt(key: String): JsonObject {
    return this[key]?.asObjectOrNull() ?: JsonObject(emptyMap())
}

private fun JsonObject.arrayAt(key: String): JsonArray {
    return this[key]?.asArrayOrNull() ?: JsonArray(emptyList())
}

private fun JsonObject.stringAt(key: String): String {
    return this[key]?.asString().orEmpty()
}

private fun JsonObject.intAt(key: String): Int {
    return this[key]?.asIntOrNull() ?: 0
}

private fun JsonObject.intAtOrNull(key: String): Int? {
    return this[key]?.asIntOrNull()
}

private fun JsonObject.longAt(key: String): Long {
    return this[key]?.asLongOrNull() ?: 0L
}

private fun JsonObject.longAtOrNull(key: String): Long? {
    return this[key]?.asLongOrNull()
}

private fun JsonObject.doubleAtOrNull(key: String): Double? {
    return this[key]?.asDoubleOrNull()
}

private fun JsonArray.firstString(): String {
    return firstOrNull()?.asString().orEmpty()
}

private fun JsonElement.asObjectOrNull(): JsonObject? = runCatching { jsonObject }.getOrNull()

private fun JsonElement.asArrayOrNull(): JsonArray? = runCatching { jsonArray }.getOrNull()

private fun JsonElement.asString(): String = (this as? JsonPrimitive)?.contentOrNull.orEmpty()

private fun JsonElement.asLongOrNull(): Long? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.longOrNull ?: primitive.doubleOrNull?.toLong()
}

private fun JsonElement.asIntOrNull(): Int? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.intOrNull ?: primitive.longOrNull?.toInt()
}

private fun JsonElement.asDoubleOrNull(): Double? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.doubleOrNull ?: primitive.longOrNull?.toDouble()
}
