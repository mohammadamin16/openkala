package com.openkala.app.data.repository

import com.openkala.app.domain.model.Banner
import com.openkala.app.domain.model.HomeCategoryItem
import com.openkala.app.domain.model.HomeScreenData
import com.openkala.app.domain.model.IncredibleOfferItem
import com.openkala.app.domain.model.IncredibleSection
import com.openkala.app.domain.model.ShortcutItem
import com.openkala.app.domain.model.SuperAppTab
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRepositoryPolicyTest {

    @Test
    fun suppressStaleIncredibleSection_hidesRedItemsWhenStale() {
        val now = 30_000_000L
        val updatedAt = now - (6 * 60 * 60 * 1000L) - 1L
        val input = sampleHomeData(incredibleItems = listOf(sampleOffer(1L), sampleOffer(2L)))

        val result = suppressStaleIncredibleSection(
            data = input,
            updatedAtMillis = updatedAt,
            nowMillis = now
        )

        assertTrue(result.incredibleOffers.items.isEmpty())
        assertEquals(input.freshIncredibleOffers.items.size, result.freshIncredibleOffers.items.size)
        assertEquals(input.topBanners.size, result.topBanners.size)
    }

    @Test
    fun suppressStaleIncredibleSection_keepsItemsWhenFresh() {
        val now = 30_000_000L
        val updatedAt = now - (2 * 60 * 60 * 1000L)
        val input = sampleHomeData(incredibleItems = listOf(sampleOffer(1L)))

        val result = suppressStaleIncredibleSection(
            data = input,
            updatedAtMillis = updatedAt,
            nowMillis = now
        )

        assertEquals(1, result.incredibleOffers.items.size)
    }

    @Test
    fun retryWithAttempts_retriesUntilSuccess() = runBlocking {
        var attempts = 0
        val value = retryWithAttempts(
            maxAttempts = 3,
            retryDelaysMs = emptyList()
        ) {
            attempts += 1
            if (attempts < 2) error("temporary")
            "ok"
        }

        assertEquals("ok", value)
        assertEquals(2, attempts)
    }

    @Test
    fun retryWithAttempts_throwsAfterMaxAttempts() {
        runBlocking {
            var attempts = 0
            var thrown = false
            try {
                retryWithAttempts(
                    maxAttempts = 3,
                    retryDelaysMs = emptyList()
                ) {
                    attempts += 1
                    throw IllegalStateException("fail")
                }
            } catch (_: IllegalStateException) {
                thrown = true
            }
            assertTrue(thrown)
            assertEquals(3, attempts)
        }
    }

    private fun sampleHomeData(incredibleItems: List<IncredibleOfferItem>): HomeScreenData {
        return HomeScreenData(
            topStripBanner = Banner(id = 10, title = "top", imageUrl = "u", deeplink = "/top"),
            superAppTabs = listOf(
                SuperAppTab(
                    name = "digikala",
                    title = "دیجی‌کالا",
                    iconUrl = "icon",
                    isWebView = false,
                    webUrl = "",
                    backgroundColorHex = "#fff",
                    textColorHex = "#111",
                    focusedTextColorHex = "#fff",
                    defaultTab = true
                )
            ),
            selectedTabName = "digikala",
            heroBanners = listOf(Banner(id = 20, title = "hero", imageUrl = "u", deeplink = "/hero")),
            shortcuts = listOf(ShortcutItem(id = 30, title = "shortcut", iconUrl = "u", deeplink = "/s")),
            incredibleOffers = IncredibleSection(title = "شگفت‌انگیز", items = incredibleItems),
            topBanners = listOf(Banner(id = 40, title = "top-banner", imageUrl = "u", deeplink = "/b")),
            freshIncredibleOffers = IncredibleSection(title = "fresh", items = listOf(sampleOffer(3L))),
            middlePromoBanners = listOf(Banner(id = 50, title = "middle", imageUrl = "u", deeplink = "/m")),
            homeCategoriesTitle = "دسته‌بندی‌ها",
            homeCategoriesRows = 2,
            homeCategories = listOf(HomeCategoryItem(id = 60, title = "cat", imageUrl = "u", deeplink = "/c"))
        )
    }

    private fun sampleOffer(id: Long): IncredibleOfferItem {
        return IncredibleOfferItem(
            id = id,
            title = "item-$id",
            imageUrl = "https://example.com/$id.webp",
            price = 1000,
            originalPrice = 1200,
            discountPercent = 10,
            timerSeconds = 300
        )
    }
}
