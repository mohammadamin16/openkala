package com.openkala.app.domain.mapper

import com.openkala.app.domain.model.SearchEntryData
import com.openkala.app.domain.model.SearchSuggestionItem
import com.openkala.app.domain.model.SearchTrendItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

fun mapAutocompleteTrends(response: JsonObject): SearchEntryData {
    val data = response.objectAt("data")
    val trends = data.arrayAt("trends").mapNotNull { item ->
        val obj = item.asObjectOrNull() ?: return@mapNotNull null
        val keyword = obj.stringAt("keyword")
        if (keyword.isBlank()) return@mapNotNull null
        SearchTrendItem(
            keyword = keyword,
            uri = obj.objectAt("url").stringAt("uri").takeIf { it.isNotBlank() }
        )
    }

    return SearchEntryData(
        trends = trends,
        suggestions = emptyList()
    )
}

fun mapAutocompleteSuggestions(response: JsonObject): List<SearchSuggestionItem> {
    val data = response.objectAt("data")
    return data.arrayAt("auto_complete").mapNotNull { item ->
        val obj = item.asObjectOrNull() ?: return@mapNotNull null
        val keyword = obj.stringAt("keyword")
        if (keyword.isBlank()) return@mapNotNull null
        SearchSuggestionItem(
            keyword = keyword,
            uri = obj.objectAt("url").stringAt("uri").takeIf { it.isNotBlank() }
        )
    }
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

private fun JsonElement.asObjectOrNull(): JsonObject? = runCatching { jsonObject }.getOrNull()

private fun JsonElement.asArrayOrNull(): JsonArray? = runCatching { jsonArray }.getOrNull()

private fun JsonElement.asString(): String = (this as? JsonPrimitive)?.contentOrNull.orEmpty()
