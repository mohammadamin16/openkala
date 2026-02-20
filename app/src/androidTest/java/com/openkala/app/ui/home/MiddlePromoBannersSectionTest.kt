package com.openkala.app.ui.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import com.openkala.app.domain.model.Banner
import com.openkala.app.domain.model.HomeScreenData
import com.openkala.app.domain.model.HomeCategoryItem
import com.openkala.app.domain.model.IncredibleOfferItem
import com.openkala.app.domain.model.IncredibleSection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MiddlePromoBannersSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun middlePromoBannersSection_hiddenWhenNoItems() {
        composeRule.setContent {
            MiddlePromoBannersSection(
                banners = emptyList(),
                styleSpec = PixelPerfectHomeStyle
            )
        }

        composeRule.onAllNodesWithTag("home_middle_banners_section").assertCountEquals(0)
    }

    @Test
    fun middlePromoBannersSection_rendersAtMostFourCards() {
        composeRule.setContent {
            MiddlePromoBannersSection(
                banners = listOf(
                    banner(1), banner(2), banner(3), banner(4), banner(5)
                ),
                styleSpec = PixelPerfectHomeStyle
            )
        }

        composeRule.onAllNodesWithTag("home_middle_banners_section").assertCountEquals(1)
        composeRule.onAllNodesWithTag("home_middle_banner_card").assertCountEquals(4)
    }

    @Test
    @OptIn(ExperimentalSharedTransitionApi::class)
    fun homeScreen_middleBannersRenderedAfterFreshSection() {
        composeRule.setContent {
            HomeScreen(
                data = sampleHomeData(),
                isRefreshing = false,
                styleSpec = PixelPerfectHomeStyle,
                pixelPerfectMode = false,
                onProductClick = {},
                onSearchClick = {},
                onWebModeChanged = {},
                onBannerOpenStateChanged = {},
                sharedTransitionScope = null,
                animatedVisibilityScope = null
            )
        }

        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(hasTestTag("home_middle_banners_section"))

        val freshTop = composeRule.onAllNodesWithTag("home_fresh_incredible_section")
            .fetchSemanticsNodes().first().boundsInRoot.top
        val middleTop = composeRule.onAllNodesWithTag("home_middle_banners_section")
            .fetchSemanticsNodes().first().boundsInRoot.top

        assertTrue(middleTop > freshTop)
    }

    private fun sampleHomeData(): HomeScreenData {
        return HomeScreenData(
            topStripBanner = null,
            superAppTabs = emptyList(),
            selectedTabName = "digikala",
            heroBanners = emptyList(),
            shortcuts = emptyList(),
            incredibleOffers = IncredibleSection(
                title = "red",
                items = emptyList()
            ),
            topBanners = listOf(banner(101), banner(102), banner(103), banner(104)),
            freshIncredibleOffers = IncredibleSection(
                title = "green",
                items = listOf(
                    IncredibleOfferItem(
                        id = 301,
                        title = "item-301",
                        imageUrl = "https://example.com/301.webp",
                        price = 1000,
                        originalPrice = 1500,
                        discountPercent = 33,
                        timerSeconds = 1000
                    )
                )
            ),
            middlePromoBanners = listOf(banner(201), banner(202), banner(203), banner(204)),
            homeCategoriesTitle = "دسته‌بندی‌ها",
            homeCategoriesRows = 2,
            homeCategories = listOf(
                HomeCategoryItem(
                    id = 1L,
                    title = "cat",
                    imageUrl = "https://example.com/cat.jpg",
                    deeplink = "/cat"
                )
            )
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
