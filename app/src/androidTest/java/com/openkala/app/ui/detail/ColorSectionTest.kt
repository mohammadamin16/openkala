package com.openkala.app.ui.detail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.openkala.app.domain.model.ProductColorOption
import com.openkala.app.domain.model.ProductDetailData
import com.openkala.app.domain.model.ProductSpecification
import com.openkala.app.domain.model.ProductVariant
import org.junit.Rule
import org.junit.Test

class ColorSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun colorSection_doesNotRenderBlankTitleOptions() {
        composeRule.setContent {
            ColorSection(
                data = sampleData(
                    colorOptions = listOf(
                        ProductColorOption(id = 1, title = "", hexCode = "#fff", variantId = 11),
                        ProductColorOption(id = 2, title = "سفید", hexCode = "#fefefe", variantId = 12)
                    )
                ),
                selectedVariantId = 12,
                onVariantClick = {},
                isLoading = false
            )
        }

        composeRule.onAllNodesWithTag("product_color_section").assertCountEquals(1)
        composeRule.onAllNodesWithTag("product_color_chip").assertCountEquals(1)
        composeRule.onNodeWithTag("product_color_header").assertTextContains("سفید")
    }

    @Test
    fun colorSection_hidesWhenOnlyBlankTitleOptionsExist() {
        composeRule.setContent {
            ColorSection(
                data = sampleData(
                    colorOptions = listOf(
                        ProductColorOption(id = 1, title = "", hexCode = "#fff", variantId = 11)
                    )
                ),
                selectedVariantId = 11,
                onVariantClick = {},
                isLoading = false
            )
        }

        composeRule.onAllNodesWithTag("product_color_section").assertCountEquals(0)
        composeRule.onAllNodesWithTag("product_color_chip").assertCountEquals(0)
    }

    @Test
    fun colorSection_usesFallbackLabelWhenVariantColorTitleBlank() {
        composeRule.setContent {
            ColorSection(
                data = sampleData(
                    variants = listOf(
                        ProductVariant(
                            id = 44,
                            colorId = 7,
                            colorTitle = "",
                            colorHex = "#111111",
                            imageUrl = "",
                            sellerTitle = "",
                            warrantyTitle = "",
                            shippingText = "",
                            price = null,
                            rrpPrice = null,
                            discountPercent = null,
                            timerSeconds = null,
                            badgeTitle = ""
                        )
                    ),
                    colorOptions = listOf(
                        ProductColorOption(id = 7, title = "خاکستری", hexCode = "#999999", variantId = 44)
                    )
                ),
                selectedVariantId = 44,
                onVariantClick = {},
                isLoading = false
            )
        }

        composeRule.onNodeWithTag("product_color_header").assertTextContains("خاکستری")
    }

    private fun sampleData(
        variants: List<ProductVariant> = listOf(
            ProductVariant(
                id = 11,
                colorId = 1,
                colorTitle = "قرمز",
                colorHex = "#ff0000",
                imageUrl = "",
                sellerTitle = "",
                warrantyTitle = "",
                shippingText = "",
                price = null,
                rrpPrice = null,
                discountPercent = null,
                timerSeconds = null,
                badgeTitle = ""
            ),
            ProductVariant(
                id = 12,
                colorId = 2,
                colorTitle = "سفید",
                colorHex = "#ffffff",
                imageUrl = "",
                sellerTitle = "",
                warrantyTitle = "",
                shippingText = "",
                price = null,
                rrpPrice = null,
                discountPercent = null,
                timerSeconds = null,
                badgeTitle = ""
            )
        ),
        colorOptions: List<ProductColorOption>
    ): ProductDetailData {
        return ProductDetailData(
            id = 1,
            title = "test",
            breadcrumb = emptyList(),
            imageUrls = emptyList(),
            rating = null,
            ratingCount = null,
            commentsCount = null,
            questionsCount = null,
            variants = variants,
            colorOptions = colorOptions,
            selectedVariantId = variants.firstOrNull()?.id,
            shippingText = null,
            specifications = emptyList<ProductSpecification>()
        )
    }
}
