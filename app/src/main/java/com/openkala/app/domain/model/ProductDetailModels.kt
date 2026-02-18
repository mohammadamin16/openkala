package com.openkala.app.domain.model

data class ProductPreview(
    val productId: Long,
    val title: String,
    val imageUrl: String,
    val price: Long?,
    val discountPercent: Int?
)

data class ProductDetailData(
    val id: Long,
    val title: String,
    val breadcrumb: List<String>,
    val imageUrls: List<String>,
    val rating: Double?,
    val ratingCount: Int?,
    val commentsCount: Int?,
    val questionsCount: Int?,
    val variants: List<ProductVariant>,
    val colorOptions: List<ProductColorOption>,
    val selectedVariantId: Long?,
    val shippingText: String?,
    val specifications: List<ProductSpecification>
)

data class ProductVariant(
    val id: Long,
    val colorId: Long?,
    val colorTitle: String,
    val colorHex: String,
    val imageUrl: String,
    val sellerTitle: String,
    val warrantyTitle: String,
    val shippingText: String,
    val price: Long?,
    val rrpPrice: Long?,
    val discountPercent: Int?,
    val timerSeconds: Long?,
    val badgeTitle: String
)

data class ProductColorOption(
    val id: Long,
    val title: String,
    val hexCode: String,
    val variantId: Long?
)

data class ProductSpecification(
    val title: String,
    val value: String
)

data class ProductDetailPayload(
    val data: ProductDetailData,
    val source: DataSource
)
