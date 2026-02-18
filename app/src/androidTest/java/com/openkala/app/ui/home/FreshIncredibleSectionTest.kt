package com.openkala.app.ui.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import com.openkala.app.domain.model.IncredibleOfferItem
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FreshIncredibleSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun freshIncredibleSection_hiddenWhenNoItems() {
        composeRule.setContent {
            FreshIncredibleSection(
                title = "شگفت‌انگیز سوپرمارکتی",
                items = emptyList(),
                styleSpec = PixelPerfectHomeStyle,
                pixelPerfectMode = false,
                onProductClick = {}
            )
        }

        composeRule.onAllNodesWithTag("home_fresh_incredible_section").assertCountEquals(0)
    }

    @Test
    fun freshIncredibleSection_rendersHeaderTimerAndCards() {
        composeRule.setContent {
            FreshIncredibleSection(
                title = "شگفت‌انگیز سوپرمارکتی",
                items = listOf(item(1), item(2), item(3)),
                styleSpec = PixelPerfectHomeStyle,
                pixelPerfectMode = false,
                onProductClick = {}
            )
        }

        composeRule.onAllNodesWithTag("home_fresh_incredible_section").assertCountEquals(1)
        composeRule.onAllNodesWithTag("home_fresh_timer").assertCountEquals(1)
        composeRule.onAllNodesWithTag("home_fresh_see_all").assertCountEquals(1)
        composeRule.onAllNodesWithTag("home_fresh_product_card").assertCountEquals(3)
    }

    @Test
    fun freshIncredibleSection_productClickInvokesCallback() {
        var clickedId = -1L
        composeRule.setContent {
            FreshIncredibleSection(
                title = "شگفت‌انگیز سوپرمارکتی",
                items = listOf(item(10)),
                styleSpec = PixelPerfectHomeStyle,
                pixelPerfectMode = false,
                onProductClick = { clickedId = it.id }
            )
        }

        composeRule.onAllNodesWithTag("home_fresh_product_card")[0].performClick()
        assertEquals(10L, clickedId)
    }

    private fun item(id: Long): IncredibleOfferItem {
        return IncredibleOfferItem(
            id = id,
            title = "product-$id",
            imageUrl = "https://example.com/$id.webp",
            price = 199000,
            originalPrice = 299000,
            discountPercent = 20,
            timerSeconds = 3600
        )
    }
}
