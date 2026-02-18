package com.openkala.app.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import com.openkala.app.domain.model.Banner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TopBannersSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topBannersSection_hiddenWhenNoItems() {
        composeRule.setContent {
            TopBannersSection(
                banners = emptyList(),
                styleSpec = PixelPerfectHomeStyle
            )
        }

        assertTrue(
            composeRule.onAllNodesWithTag("home_top_banners_section").fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun topBannersSection_rendersAtMostFourCards() {
        composeRule.setContent {
            TopBannersSection(
                banners = listOf(
                    banner(1),
                    banner(2),
                    banner(3),
                    banner(4),
                    banner(5)
                ),
                styleSpec = PixelPerfectHomeStyle
            )
        }

        assertEquals(
            1,
            composeRule.onAllNodesWithTag("home_top_banners_section").fetchSemanticsNodes().size
        )
        assertEquals(
            4,
            composeRule.onAllNodesWithTag("home_top_banner_card").fetchSemanticsNodes().size
        )
    }

    private fun banner(id: Long): Banner {
        return Banner(
            id = id,
            title = "banner-$id",
            imageUrl = "https://example.com/$id.webp",
            deeplink = "/banner/$id"
        )
    }
}
