package com.openkala.app.domain.mapper

import com.openkala.app.domain.model.ProductColorOption
import com.openkala.app.domain.model.ProductDetailData
import com.openkala.app.domain.model.ProductSpecification
import com.openkala.app.domain.model.ProductVariant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

fun mapProductDetail(
    detail: JsonObject,
    commentsCount: Int?,
    questionsCount: Int?
): ProductDetailData {
    val product = detail.objectAt("data").objectAt("product")
    val imageUrls = product.extractImageUrls()

    val variants = product.arrayAt("variants").mapIndexedNotNull { index, element ->
        val obj = element.asObjectOrNull() ?: return@mapIndexedNotNull null
        val color = obj.objectAt("color")
        val price = obj.objectAt("price")
        ProductVariant(
            id = obj.longAt("id"),
            colorId = color.longAtOrNull("id"),
            colorTitle = color.stringAt("title"),
            colorHex = color.stringAt("hex_code").ifBlank { "#D0D0D0" },
            imageUrl = product.variantImageFor(index = index, colorId = color.longAtOrNull("id"), fallback = imageUrls),
            sellerTitle = obj.objectAt("seller").stringAt("title").ifBlank { "فروشنده نامشخص" },
            warrantyTitle = obj.objectAt("warranty").stringAt("title_fa").ifBlank { "گارانتی نامشخص" },
            shippingText = resolveShippingText(obj.objectAt("shipment_methods")),
            price = price.longAtOrNull("selling_price"),
            rrpPrice = price.longAtOrNull("rrp_price"),
            discountPercent = price.intAtOrNull("discount_percent"),
            timerSeconds = price.longAtOrNull("timer"),
            badgeTitle = price.objectAt("badge").stringAt("title")
        )
    }

    val variantByColor = variants
        .filter { it.colorId != null }
        .associateBy({ it.colorId!! }, { it.id })

    val colorOptions = product.arrayAt("colors").mapNotNull { element ->
        val color = element.asObjectOrNull() ?: return@mapNotNull null
        val id = color.longAt("id")
        ProductColorOption(
            id = id,
            title = color.stringAt("title"),
            hexCode = color.stringAt("hex_code").ifBlank { "#D0D0D0" },
            variantId = variantByColor[id]
        )
    }

    val defaultVariantId = product.objectAt("default_variant").longAtOrNull("id")
    val defaultVariant = variants.firstOrNull { it.id == defaultVariantId } ?: variants.firstOrNull()

    return ProductDetailData(
        id = product.longAt("id"),
        title = product.stringAt("title_fa"),
        breadcrumb = product.arrayAt("breadcrumb")
            .mapNotNull { crumb -> crumb.asObjectOrNull()?.stringAt("title") }
            .filter { it.isNotBlank() },
        imageUrls = imageUrls,
        rating = product.objectAt("rating").intAtOrNull("rate")?.div(20.0),
        ratingCount = product.objectAt("rating").intAtOrNull("count"),
        commentsCount = commentsCount ?: product.intAtOrNull("comments_count"),
        questionsCount = questionsCount ?: product.intAtOrNull("questions_count"),
        variants = variants,
        colorOptions = colorOptions,
        selectedVariantId = defaultVariant?.id,
        shippingText = defaultVariant?.shippingText,
        specifications = product.extractSpecifications()
    )
}

fun readPagerTotal(response: JsonObject): Int? {
    return response.objectAt("data").objectAt("pager").intAtOrNull("total_items")
}

private fun JsonObject.extractImageUrls(): List<String> {
    val product = this
    val variantImages = product.objectAt("variants_images")

    val main = variantImages.objectAt("main").firstImageUrl()

    val list = variantImages.arrayAt("list").mapNotNull { image ->
        image.asObjectOrNull()?.firstImageUrl()?.takeIf { it.isNotBlank() }
    }

    val fallbackMain = product.objectAt("images").objectAt("main").firstImageUrl()
    val fallbackList = product.objectAt("images").arrayAt("list").mapNotNull { image ->
        image.asObjectOrNull()?.firstImageUrl()?.takeIf { it.isNotBlank() }
    }

    return buildList {
        if (main.isNotBlank()) add(main)
        addAll(list)
        if (fallbackMain.isNotBlank()) add(fallbackMain)
        addAll(fallbackList)
    }.distinct().ifEmpty {
        listOfNotNull(fallbackMain.takeIf { it.isNotBlank() })
    }
}

private fun JsonObject.variantImageFor(
    index: Int,
    colorId: Long?,
    fallback: List<String>
): String {
    val colorImage = arrayAt("colors").firstNotNullOfOrNull { color ->
        val colorObj = color.asObjectOrNull() ?: return@firstNotNullOfOrNull null
        if (colorObj.longAtOrNull("id") != colorId) return@firstNotNullOfOrNull null
        colorObj.arrayAt("images")
            .mapNotNull { it.asObjectOrNull()?.firstImageUrl() }
            .firstOrNull { it.isNotBlank() }
    }
    if (!colorImage.isNullOrBlank()) return colorImage
    if (fallback.isEmpty()) return ""
    return fallback[index % fallback.size]
}

private fun JsonObject.extractSpecifications(): List<ProductSpecification> {
    val firstSection = arrayAt("specifications").firstOrNull()?.asObjectOrNull() ?: return emptyList()
    return firstSection.arrayAt("attributes")
        .mapNotNull { attribute ->
            val obj = attribute.asObjectOrNull() ?: return@mapNotNull null
            val title = obj.stringAt("title")
            val value = obj.arrayAt("values")
                .mapNotNull { it.asString().trim().takeIf { item -> item.isNotBlank() } }
                .joinToString("، ")
                .ifBlank { "-" }
            if (title.isBlank()) return@mapNotNull null
            ProductSpecification(title = title, value = value)
        }
        .take(8)
}

private fun resolveShippingText(shipmentMethods: JsonObject): String {
    val providers = shipmentMethods.arrayAt("providers")
    val preferred = providers
        .mapNotNull { it.asObjectOrNull() }
        .firstOrNull { it.stringAt("type") == "jet" }
        ?: providers.firstOrNull()?.asObjectOrNull()

    if (preferred == null) {
        return shipmentMethods.stringAt("description")
    }

    val dayPrefix = when (preferred.stringAt("delivery_day")) {
        "today" -> "تحویل امروز"
        "tomorrow" -> "تحویل فردا"
        else -> "تحویل"
    }

    val providerTitle = preferred.objectAt("label").stringAt("title")
        .ifBlank { preferred.stringAt("title") }

    return if (providerTitle.isBlank()) {
        shipmentMethods.stringAt("description")
    } else {
        "$dayPrefix با $providerTitle"
    }
}

private fun JsonObject.firstImageUrl(): String {
    return arrayAt("webp_url").firstString().ifBlank { arrayAt("url").firstString() }
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
    return longAtOrNull(key) ?: 0L
}

private fun JsonObject.longAtOrNull(key: String): Long? {
    return this[key]?.asLongOrNull()
}

private fun JsonObject.intAtOrNull(key: String): Int? {
    return this[key]?.asIntOrNull()
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
