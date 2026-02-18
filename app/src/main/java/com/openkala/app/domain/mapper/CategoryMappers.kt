package com.openkala.app.domain.mapper

import com.openkala.app.domain.model.CategoriesScreenData
import com.openkala.app.domain.model.CategoryLeafItem
import com.openkala.app.domain.model.CategorySection
import com.openkala.app.domain.model.CategoryTabItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

data class MegaMenuEnvelope(
    val hash: String,
    val data: CategoriesScreenData
)

fun mapMegaMenuEnvelope(response: JsonObject): MegaMenuEnvelope? {
    val dictionaries = response.arrayAt("data")
    val root = dictionaries.firstOrNull()?.asObjectOrNull() ?: return null
    val payload = root["data"] ?: return null

    val payloadObject = payload.asObjectOrNull() ?: return null
    val hash = payloadObject.stringAt("hash")
    val rootNodes = payloadObject.arrayAt("data")
        .mapNotNull { it.toMegaMenuNode() }
        .sortedWith(compareBy<MegaMenuNode>({ it.rowNumber }, { it.columnNumber }, { it.id }))

    if (rootNodes.isEmpty()) return null

    val tabs = rootNodes.map { node ->
        CategoryTabItem(
            id = node.id,
            title = node.title,
            code = node.urlUri
                ?.substringAfter("/search/category-")
                ?.substringBefore('/')
                .orEmpty(),
            iconToken = node.iconToken,
            urlUri = node.urlUri
        )
    }

    val sectionsByTabId = rootNodes.associate { tabNode ->
        tabNode.id to tabNode.children
            .sortedWith(compareBy<MegaMenuNode>({ it.rowNumber }, { it.columnNumber }, { it.id }))
            .map { sectionNode ->
                CategorySection(
                    id = sectionNode.id,
                    title = sectionNode.title,
                    rowNumber = sectionNode.rowNumber,
                    columnNumber = sectionNode.columnNumber,
                    items = sectionNode.children
                        .sortedWith(compareBy<MegaMenuNode>({ it.rowNumber }, { it.columnNumber }, { it.id }))
                        .map { itemNode ->
                            CategoryLeafItem(
                                id = itemNode.id,
                                title = itemNode.title,
                                imageUrl = itemNode.imageUrl,
                                urlUri = itemNode.urlUri
                            )
                        }
                )
            }
    }

    return MegaMenuEnvelope(
        hash = hash,
        data = CategoriesScreenData(
            tabs = tabs,
            selectedTabId = tabs.firstOrNull()?.id ?: 0L,
            sectionsByTabId = sectionsByTabId
        )
    )
}

private data class MegaMenuNode(
    val id: Long,
    val title: String,
    val rowNumber: Int,
    val columnNumber: Int,
    val iconToken: String,
    val imageUrl: String,
    val urlUri: String?,
    val children: List<MegaMenuNode>
)

private fun JsonElement.toMegaMenuNode(): MegaMenuNode? {
    val obj = asObjectOrNull() ?: return null
    val title = obj.stringAt("title")
    if (title.isBlank()) return null

    return MegaMenuNode(
        id = obj.longAt("id"),
        title = title,
        rowNumber = obj.intAt("row_number"),
        columnNumber = obj.intAt("column_number"),
        iconToken = obj.stringAt("icon"),
        imageUrl = obj.stringAt("image"),
        urlUri = obj.objectAt("url").stringAt("uri").takeIf { it.isNotBlank() },
        children = obj.arrayAt("children").mapNotNull { child -> child.toMegaMenuNode() }
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

private fun JsonObject.longAt(key: String): Long {
    return this[key]?.asLongOrNull() ?: 0L
}

private fun JsonObject.intAt(key: String): Int {
    return this[key]?.asIntOrNull() ?: 0
}

private fun JsonElement.asObjectOrNull(): JsonObject? = runCatching { jsonObject }.getOrNull()

private fun JsonElement.asArrayOrNull(): JsonArray? = runCatching { jsonArray }.getOrNull()

private fun JsonElement.asString(): String = (this as? JsonPrimitive)?.contentOrNull.orEmpty()

private fun JsonElement.asLongOrNull(): Long? = (this as? JsonPrimitive)?.longOrNull

private fun JsonElement.asIntOrNull(): Int? = (this as? JsonPrimitive)?.intOrNull
