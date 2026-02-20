package com.openkala.app.domain.mapper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductDetailMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapProductDetail_filtersBlankColorTitles() {
        val detail = detailJson(
            """
            "colors": [
              {"id": 1, "title": " ", "hex_code": "#ffffff"},
              {"id": 2, "title": "سفید", "hex_code": "#fefefe"}
            ],
            "variants": [
              {
                "id": 101,
                "color": {"id": 1, "title": "", "hex_code": "#ffffff"},
                "price": {},
                "seller": {},
                "warranty": {},
                "shipment_methods": {}
              },
              {
                "id": 102,
                "color": {"id": 2, "title": "سفید", "hex_code": "#fefefe"},
                "price": {},
                "seller": {},
                "warranty": {},
                "shipment_methods": {}
              }
            ]
            """.trimIndent()
        )

        val result = mapProductDetail(detail, commentsCount = null, questionsCount = null)

        assertEquals(1, result.colorOptions.size)
        assertEquals(2L, result.colorOptions.first().id)
        assertEquals("سفید", result.colorOptions.first().title)
        assertEquals(102L, result.colorOptions.first().variantId)
    }

    @Test
    fun mapProductDetail_keepsColorWithoutVariantAsUnselectable() {
        val detail = detailJson(
            """
            "colors": [
              {"id": 9, "title": "خاکستری", "hex_code": "#cccccc"}
            ],
            "variants": [
              {
                "id": 501,
                "color": {"id": 3, "title": "آبی", "hex_code": "#0000ff"},
                "price": {},
                "seller": {},
                "warranty": {},
                "shipment_methods": {}
              }
            ]
            """.trimIndent()
        )

        val result = mapProductDetail(detail, commentsCount = null, questionsCount = null)

        assertEquals(1, result.colorOptions.size)
        assertEquals("خاکستری", result.colorOptions.first().title)
        assertNull(result.colorOptions.first().variantId)
    }

    private fun detailJson(extraProductFields: String): JsonObject {
        return json.parseToJsonElement(
            """
            {
              "data": {
                "product": {
                  "id": 123,
                  "title_fa": "test",
                  "breadcrumb": [],
                  "rating": {"rate": 0, "count": 0},
                  "comments_count": 0,
                  "questions_count": 0,
                  "default_variant": {},
                  "variants_images": {"main": {"webp_url": [], "url": []}, "list": []},
                  "images": {"main": {"webp_url": [], "url": []}, "list": []},
                  "specifications": []
                  ${if (extraProductFields.isNotBlank()) ",$extraProductFields" else ""}
                }
              }
            }
            """.trimIndent()
        ).jsonObject
    }
}
