package com.openkala.app.domain.mapper

import com.openkala.app.domain.model.Banner
import com.openkala.app.domain.model.HomeScreenData
import com.openkala.app.domain.model.IncredibleOfferItem
import com.openkala.app.domain.model.IncredibleSection
import com.openkala.app.domain.model.ShortcutItem
import com.openkala.app.domain.model.SuperAppTab
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

fun mapHomeScreenData(home: JsonObject, pillars: JsonObject): HomeScreenData {
    val homeData = home.objectAt("data")
    val pillarsData = pillars.objectAt("data")

    val allHeaderBanners = homeData.arrayAt("header_banners").mapNotNull { it.toBanner() }
    val topStrip = allHeaderBanners.firstOrNull()
    val heroBanners = allHeaderBanners.drop(1).ifEmpty { allHeaderBanners }

    val tabs = pillarsData.arrayAt("active_pillars").mapNotNull { it.toSuperAppTab() }
    val selectedTab = tabs.firstOrNull { it.defaultTab }?.name ?: "digikala"

    val shortcuts = homeData.arrayAt("deep_links")
        .mapNotNull { it.toShortcutItem() }
        .take(5)

    val incredibleSection = homeData.objectAt("incredible_products").toIncredibleSection()
    val topBanners = homeData.arrayAt("top_banners").mapNotNull { it.toBanner() }

    return HomeScreenData(
        topStripBanner = topStrip,
        superAppTabs = tabs,
        selectedTabName = selectedTab,
        heroBanners = heroBanners,
        shortcuts = shortcuts,
        incredibleOffers = incredibleSection,
        topBanners = topBanners
    )
}

private fun JsonElement.toBanner(): Banner? {
    val obj = asObjectOrNull() ?: return null
    return Banner(
        id = obj.longAt("id"),
        title = obj.stringAt("title"),
        imageUrl = obj.stringAt("webp_image").ifBlank { obj.stringAt("image") },
        deeplink = obj.objectAt("url").stringAt("uri")
    )
}

private fun JsonElement.toSuperAppTab(): SuperAppTab? {
    val obj = asObjectOrNull() ?: return null
    return SuperAppTab(
        name = obj.stringAt("name"),
        title = obj.stringAt("title"),
        iconUrl = obj.objectAt("image").arrayAt("url").firstString(),
        isWebView = obj.booleanAt("is_webview"),
        webUrl = obj.stringAt("web_url"),
        backgroundColorHex = obj.stringAt("background_color").ifBlank { "#FFFFFF" },
        textColorHex = obj.stringAt("text_color").ifBlank { "#212121" },
        focusedTextColorHex = obj.stringAt("focused_text_color").ifBlank { "#FFFFFF" },
        defaultTab = obj.booleanAt("default_tab")
    )
}

private fun JsonElement.toShortcutItem(): ShortcutItem? {
    val obj = asObjectOrNull() ?: return null
    return ShortcutItem(
        id = obj.longAt("id"),
        title = obj.stringAt("title"),
        iconUrl = obj.objectAt("icon").arrayAt("url").firstString(),
        deeplink = obj.objectAt("url").stringAt("uri")
    )
}

private fun JsonObject.toIncredibleSection(): IncredibleSection {
    val items = arrayAt("products").mapNotNull { element ->
        val obj = element.asObjectOrNull() ?: return@mapNotNull null
        val priceObj = obj.objectAt("default_variant").objectAt("price")
        IncredibleOfferItem(
            id = obj.longAt("id"),
            title = obj.stringAt("title_fa"),
            imageUrl = obj.objectAt("images").objectAt("main").arrayAt("webp_url").firstString()
                .ifBlank {
                    obj.objectAt("images").objectAt("main").arrayAt("url").firstString()
                },
            price = priceObj.longAtOrNull("selling_price"),
            discountPercent = priceObj.intAtOrNull("discount_percent"),
            timerSeconds = priceObj.longAtOrNull("timer")
        )
    }

    return IncredibleSection(
        title = stringAt("title").ifBlank { "شگفت‌انگیز" },
        items = items
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
    return longAtOrNull(key) ?: 0L
}

private fun JsonObject.longAtOrNull(key: String): Long? {
    return this[key]?.asLongOrNull()
}

private fun JsonObject.intAtOrNull(key: String): Int? {
    return this[key]?.asIntOrNull()
}

private fun JsonObject.booleanAt(key: String): Boolean {
    return this[key]?.asBoolean() ?: false
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

private fun JsonElement.asBoolean(): Boolean? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.contentOrNull?.toBooleanStrictOrNull()
}
