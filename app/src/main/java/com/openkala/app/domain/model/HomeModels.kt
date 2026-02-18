package com.openkala.app.domain.model

data class HomeScreenData(
    val topStripBanner: Banner?,
    val superAppTabs: List<SuperAppTab>,
    val selectedTabName: String,
    val heroBanners: List<Banner>,
    val shortcuts: List<ShortcutItem>,
    val incredibleOffers: IncredibleSection,
    val topBanners: List<Banner>
)

data class Banner(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val deeplink: String
)

data class SuperAppTab(
    val name: String,
    val title: String,
    val iconUrl: String,
    val isWebView: Boolean,
    val webUrl: String,
    val backgroundColorHex: String,
    val textColorHex: String,
    val focusedTextColorHex: String,
    val defaultTab: Boolean
)

data class ShortcutItem(
    val id: Long,
    val title: String,
    val iconUrl: String,
    val deeplink: String
)

data class IncredibleSection(
    val title: String,
    val items: List<IncredibleOfferItem>
)

data class IncredibleOfferItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val price: Long?,
    val discountPercent: Int?,
    val timerSeconds: Long?
)

enum class DataSource {
    CACHE,
    NETWORK
}

data class HomeScreenPayload(
    val data: HomeScreenData,
    val source: DataSource
)
