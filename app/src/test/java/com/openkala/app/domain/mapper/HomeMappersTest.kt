package com.openkala.app.domain.mapper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapHomeScreenData_parsesTopBanners() {
        val home = homeJson(
            """
            "top_banners": [
              {
                "id": 10,
                "title": "banner-a",
                "webp_image": "https://cdn.example/a.webp",
                "image": "https://cdn.example/a.jpg",
                "url": { "uri": "/a" }
              },
              {
                "id": 11,
                "title": "banner-b",
                "webp_image": "https://cdn.example/b.webp",
                "image": "https://cdn.example/b.jpg",
                "url": { "uri": "/b" }
              }
            ]
            """.trimIndent()
        )

        val result = mapHomeScreenData(home, pillarsJson())

        assertEquals(2, result.topBanners.size)
        assertEquals(10L, result.topBanners[0].id)
        assertEquals("https://cdn.example/a.webp", result.topBanners[0].imageUrl)
        assertEquals("/a", result.topBanners[0].deeplink)
    }

    @Test
    fun mapHomeScreenData_topBannerFallsBackToImageWhenWebpMissing() {
        val home = homeJson(
            """
            "top_banners": [
              {
                "id": 20,
                "title": "fallback",
                "webp_image": "",
                "image": "https://cdn.example/fallback.jpg",
                "url": { "uri": "/fallback" }
              }
            ]
            """.trimIndent()
        )

        val result = mapHomeScreenData(home, pillarsJson())

        assertEquals(1, result.topBanners.size)
        assertEquals("https://cdn.example/fallback.jpg", result.topBanners.first().imageUrl)
    }

    @Test
    fun mapHomeScreenData_returnsEmptyTopBannersWhenFieldMissing() {
        val home = homeJson(extraFields = "")

        val result = mapHomeScreenData(home, pillarsJson())

        assertTrue(result.topBanners.isEmpty())
        assertTrue(result.middlePromoBanners.isEmpty())
    }

    @Test
    fun mapHomeScreenData_parsesTabWebViewFields() {
        val result = mapHomeScreenData(
            home = homeJson(extraFields = ""),
            pillars = pillarsJson(
                """
                "active_pillars": [
                  {
                    "name": "jet",
                    "title": "۴۵ دقیقه‌ای",
                    "is_webview": true,
                    "web_url": "https://example.com/jet",
                    "image": { "url": ["https://example.com/icon.png"] },
                    "background_color": "#000000",
                    "text_color": "#111111",
                    "focused_text_color": "#ffffff",
                    "default_tab": false
                  }
                ]
                """.trimIndent()
            )
        )

        assertEquals(1, result.superAppTabs.size)
        assertEquals("jet", result.superAppTabs.first().name)
        assertEquals(true, result.superAppTabs.first().isWebView)
        assertEquals("https://example.com/jet", result.superAppTabs.first().webUrl)
    }

    @Test
    fun mapHomeScreenData_parsesFreshIncredibleProducts() {
        val home = homeJson(
            """
            "fresh_incredible_products": {
              "title": "شگفت‌انگیز سوپرمارکتی",
              "products": [
                {
                  "id": 301,
                  "title_fa": "item-301",
                  "images": {
                    "main": {
                      "webp_url": ["https://cdn.example/301.webp"],
                      "url": ["https://cdn.example/301.jpg"]
                    }
                  },
                  "default_variant": {
                    "price": {
                      "selling_price": 199000,
                      "rrp_price": 259000,
                      "discount_percent": 23,
                      "timer": 4500
                    }
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val result = mapHomeScreenData(home, pillarsJson())

        assertEquals("شگفت‌انگیز سوپرمارکتی", result.freshIncredibleOffers.title)
        assertEquals(1, result.freshIncredibleOffers.items.size)
        assertEquals(301L, result.freshIncredibleOffers.items.first().id)
        assertEquals(199000L, result.freshIncredibleOffers.items.first().price)
        assertEquals(259000L, result.freshIncredibleOffers.items.first().originalPrice)
        assertEquals(23, result.freshIncredibleOffers.items.first().discountPercent)
        assertEquals(4500L, result.freshIncredibleOffers.items.first().timerSeconds)
    }

    @Test
    fun mapHomeScreenData_mergesMiddleBannersAndThirdAndTakesFour() {
        val home = homeJson(
            """
            "middle_banners": [
              {
                "id": 1,
                "title": "m1",
                "webp_image": "https://cdn.example/m1.webp",
                "image": "https://cdn.example/m1.jpg",
                "url": { "uri": "/m1" }
              },
              {
                "id": 2,
                "title": "m2",
                "webp_image": "",
                "image": "https://cdn.example/m2.jpg",
                "url": { "uri": "/m2" }
              },
              {
                "id": 3,
                "title": "m3",
                "webp_image": "https://cdn.example/m3.webp",
                "image": "https://cdn.example/m3.jpg",
                "url": { "uri": "/m3" }
              }
            ],
            "middle_banners_third": [
              {
                "id": 4,
                "title": "t1",
                "webp_image": "https://cdn.example/t1.webp",
                "image": "https://cdn.example/t1.jpg",
                "url": { "uri": "/t1" }
              },
              {
                "id": 5,
                "title": "t2",
                "webp_image": "https://cdn.example/t2.webp",
                "image": "https://cdn.example/t2.jpg",
                "url": { "uri": "/t2" }
              }
            ]
            """.trimIndent()
        )

        val result = mapHomeScreenData(home, pillarsJson())

        assertEquals(4, result.middlePromoBanners.size)
        assertEquals(listOf(1L, 2L, 3L, 4L), result.middlePromoBanners.map { it.id })
        assertEquals("https://cdn.example/m2.jpg", result.middlePromoBanners[1].imageUrl)
    }

    @Test
    fun mapHomeScreenData_filtersMiddleBannersWithoutImages() {
        val home = homeJson(
            """
            "middle_banners": [
              {
                "id": 11,
                "title": "invalid",
                "webp_image": "",
                "image": "",
                "url": { "uri": "/invalid" }
              }
            ],
            "middle_banners_third": [
              {
                "id": 12,
                "title": "valid",
                "webp_image": "https://cdn.example/valid.webp",
                "image": "",
                "url": { "uri": "/valid" }
              }
            ]
            """.trimIndent()
        )

        val result = mapHomeScreenData(home, pillarsJson())

        assertEquals(1, result.middlePromoBanners.size)
        assertEquals(12L, result.middlePromoBanners.first().id)
    }

    private fun homeJson(extraFields: String): JsonObject {
        return json.parseToJsonElement(
            """
            {
              "data": {
                "header_banners": [],
                "deep_links": [],
                "incredible_products": {
                  "title": "",
                  "products": []
                },
                "fresh_incredible_products": {
                  "title": "",
                  "products": []
                }
                ${if (extraFields.isNotBlank()) ",$extraFields" else ""}
              }
            }
            """.trimIndent()
        ).jsonObject
    }

    private fun pillarsJson(extraFields: String = "\"active_pillars\": []"): JsonObject {
        return json.parseToJsonElement(
            """
            {
              "data": {
                $extraFields
              }
            }
            """.trimIndent()
        ).jsonObject
    }
}
