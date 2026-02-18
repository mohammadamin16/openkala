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
                }
                ${if (extraFields.isNotBlank()) ",$extraFields" else ""}
              }
            }
            """.trimIndent()
        ).jsonObject
    }

    private fun pillarsJson(): JsonObject {
        return json.parseToJsonElement(
            """
            {
              "data": {
                "active_pillars": []
              }
            }
            """.trimIndent()
        ).jsonObject
    }
}
