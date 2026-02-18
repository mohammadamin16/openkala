package com.openkala.app.ui.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openkala.app.domain.model.ProductColorOption
import com.openkala.app.domain.model.ProductDetailData
import com.openkala.app.domain.model.ProductVariant
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun ProductDetailScreenRoute(
    onClose: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProductDetailScreen(
        state = state,
        onClose = onClose,
        onRetry = viewModel::refresh,
        onVariantClick = viewModel::selectVariant
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductDetailScreen(
    state: ProductDetailScreenState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onVariantClick: (Long) -> Unit
) {
    val data = state.data
    val selectedVariant = remember(data, state.selectedVariantId) {
        data?.variants?.firstOrNull { it.id == state.selectedVariantId }
            ?: data?.variants?.firstOrNull()
    }

    val galleryImages = remember(data, selectedVariant, state.preview) {
        buildList {
            selectedVariant?.imageUrl?.takeIf { it.isNotBlank() }?.let(::add)
            data?.imageUrls?.forEach { url -> if (url.isNotBlank()) add(url) }
            state.preview?.imageUrl?.takeIf { it.isNotBlank() }?.let(::add)
        }.distinct()
    }

    if (state.isLoading && state.preview == null && data == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OpenKalaColorTokens.BrandPrimary)
        }
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            StickyBuyBar(
                selectedVariant = selectedVariant,
                previewPrice = state.preview?.price,
                previewDiscount = state.preview?.discountPercent
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OpenKalaColorTokens.AppBackground)
                .statusBarsPadding()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                TopActionBar(onClose = onClose)
            }

            item {
                BreadcrumbRow(data = data)
            }

            item {
                MediaSection(galleryImages = galleryImages)
            }

            item {
                OfferCard(
                    data = data,
                    selectedVariant = selectedVariant
                )
            }

            item {
                ColorSection(
                    data = data,
                    selectedVariantId = selectedVariant?.id,
                    onVariantClick = onVariantClick
                )
            }

            item {
                SpecificationSection(data = data)
            }

            if (state.errorMessage != null && data == null) {
                item {
                    ErrorCard(message = state.errorMessage, onRetry = onRetry)
                }
            }
        }
    }
}

@Composable
private fun TopActionBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close",
                tint = OpenKalaColorTokens.TextHigh
            )
        }
        Row {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = OpenKalaColorTokens.TextHigh
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart",
                    tint = OpenKalaColorTokens.TextHigh
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Menu",
                    tint = OpenKalaColorTokens.TextHigh
                )
            }
        }
    }
}

@Composable
private fun BreadcrumbRow(data: ProductDetailData?) {
    val breadcrumbs = data?.breadcrumb?.dropLast(1).orEmpty().takeLast(3)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        breadcrumbs.forEachIndexed { index, title ->
            Text(
                text = title,
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.TextLow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (index < breadcrumbs.lastIndex) {
                Text(
                    text = "›",
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.TextLow
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaSection(galleryImages: List<String>) {
    val pagerState = rememberPagerState(pageCount = { galleryImages.size.coerceAtLeast(1) })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.SurfaceMuted)
            .padding(top = 6.dp, bottom = 14.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) { page ->
            val image = galleryImages.getOrNull(page)
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(galleryImages.size.coerceAtLeast(1)) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(width = if (pagerState.currentPage == index) 18.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) OpenKalaColorTokens.TextLow
                            else OpenKalaColorTokens.Border
                        )
                )
            }
        }
    }
}

@Composable
private fun OfferCard(
    data: ProductDetailData?,
    selectedVariant: ProductVariant?
) {
    val title = data?.title.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(OpenKalaRadiusTokens.Large)
            .background(OpenKalaColorTokens.Surface)
            .border(1.dp, OpenKalaColorTokens.Border, OpenKalaRadiusTokens.Large)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedVariant?.badgeTitle?.ifBlank { "پیشنهاد ویژه" } ?: "پیشنهاد ویژه",
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.BrandPrimary
            )
            selectedVariant?.timerSeconds?.let {
                CountdownTimerText(totalSeconds = it)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = OpenKalaColorTokens.TextHigh
                )
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = OpenKalaColorTokens.TextHigh
                )
            }
            Text(
                text = data?.breadcrumb?.dropLast(1)?.lastOrNull().orEmpty(),
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.TextLow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Text(
            text = title,
            style = OpenKalaTypographyTokens.H5.copy(fontWeight = FontWeight.W900),
            color = OpenKalaColorTokens.TextPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip(
                text = "${(data?.questionsCount ?: 0).toPersianDigits()} پرسش و پاسخ"
            )
            MetricChip(
                text = "${(data?.commentsCount ?: 0).toPersianDigits()} دیدگاه"
            )
            val ratingText = data?.rating?.let { rating ->
                val count = data.ratingCount ?: 0
                "${rating.toString().toPersianDigits()} (${count.toPersianDigits()}) ★"
            } ?: "-"
            MetricChip(text = ratingText)
        }

        val shipping = selectedVariant?.shippingText ?: data?.shippingText
        if (!shipping.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(OpenKalaRadiusTokens.Medium)
                    .background(Color(0xFFE9EBF8))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = shipping,
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CountdownTimerText(totalSeconds: Long) {
    val initial = totalSeconds.coerceAtLeast(0L)
    var remaining by remember(initial) { mutableStateOf(initial) }

    LaunchedEffect(initial) {
        remaining = initial
        while (remaining > 0L) {
            delay(1000)
            remaining -= 1L
        }
    }

    Text(
        text = remaining.toTimerText(),
        style = OpenKalaTypographyTokens.SubtitleStrong,
        color = OpenKalaColorTokens.BrandPrimary
    )
}

@Composable
private fun MetricChip(text: String) {
    Text(
        text = text,
        style = OpenKalaTypographyTokens.SubtitleStrong,
        color = OpenKalaColorTokens.TextHigh,
        modifier = Modifier
            .clip(OpenKalaRadiusTokens.Medium)
            .background(OpenKalaColorTokens.SurfaceMuted)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun ColorSection(
    data: ProductDetailData?,
    selectedVariantId: Long?,
    onVariantClick: (Long) -> Unit
) {
    val variants = data?.variants.orEmpty()
    val selectedVariant = variants.firstOrNull { it.id == selectedVariantId } ?: variants.firstOrNull()

    if (variants.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = "رنگ: ${selectedVariant?.colorTitle.orEmpty()}",
            style = OpenKalaTypographyTokens.H5.copy(fontWeight = FontWeight.W800),
            color = OpenKalaColorTokens.TextPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            variants.forEach { variant ->
                ColorChip(
                    option = ProductColorOption(
                        id = variant.colorId ?: variant.id,
                        title = variant.colorTitle,
                        hexCode = variant.colorHex,
                        variantId = variant.id
                    ),
                    selected = variant.id == selectedVariant?.id,
                    onClick = { onVariantClick(variant.id) }
                )
            }
        }
    }
}

@Composable
private fun ColorChip(
    option: ProductColorOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(OpenKalaRadiusTokens.Medium)
            .background(OpenKalaColorTokens.Surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) OpenKalaColorTokens.TextHigh else OpenKalaColorTokens.Border,
                shape = OpenKalaRadiusTokens.Medium
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = option.title,
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = OpenKalaColorTokens.TextHigh
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colorFromHex(option.hexCode))
                .border(1.dp, OpenKalaColorTokens.Border, CircleShape)
        )
    }
}

@Composable
private fun SpecificationSection(data: ProductDetailData?) {
    val specs = data?.specifications.orEmpty()
    if (specs.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(OpenKalaColorTokens.Surface)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مشخصات کالا",
                style = OpenKalaTypographyTokens.H5.copy(fontWeight = FontWeight.W900),
                color = OpenKalaColorTokens.TextPrimary
            )
            Text(
                text = "مشاهده همه",
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.TextHigh
            )
        }

        specs.forEach { spec ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(OpenKalaRadiusTokens.Medium)
                    .background(OpenKalaColorTokens.SurfaceMuted)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = spec.title,
                    style = OpenKalaTypographyTokens.Body1,
                    color = OpenKalaColorTokens.TextLow
                )
                Text(
                    text = spec.value,
                    style = OpenKalaTypographyTokens.Body1Strong,
                    color = OpenKalaColorTokens.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StickyBuyBar(
    selectedVariant: ProductVariant?,
    previewPrice: Long?,
    previewDiscount: Int?
) {
    val price = selectedVariant?.price ?: previewPrice
    val discount = selectedVariant?.discountPercent ?: previewDiscount
    val rrpPrice = selectedVariant?.rrpPrice

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .border(1.dp, OpenKalaColorTokens.Border)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = OpenKalaRadiusTokens.Large,
            colors = ButtonDefaults.buttonColors(
                containerColor = OpenKalaColorTokens.Primary500,
                contentColor = OpenKalaColorTokens.White
            )
        ) {
            Text(
                text = "افزودن به سبد خرید",
                style = OpenKalaTypographyTokens.SubtitleStrong.copy(fontWeight = FontWeight.W900)
            )
        }

        Column(
            modifier = Modifier.widthIn(min = 110.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (rrpPrice != null && discount != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${discount.toPersianDigits()}%",
                        style = OpenKalaTypographyTokens.CaptionStrong,
                        color = OpenKalaColorTokens.White,
                        modifier = Modifier
                            .clip(OpenKalaRadiusTokens.Medium)
                            .background(OpenKalaColorTokens.BrandPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = rrpPrice.toToman().toPersianDigits(),
                        style = OpenKalaTypographyTokens.Body1,
                        color = OpenKalaColorTokens.TextLow,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }

            Text(
                text = (price?.toToman()?.toPersianDigits() ?: "-") ,
                style = OpenKalaTypographyTokens.H5.copy(fontWeight = FontWeight.W900),
                color = OpenKalaColorTokens.TextPrimary
            )
            Text(
                text = "تومان",
                style = OpenKalaTypographyTokens.Subtitle,
                color = OpenKalaColorTokens.TextHigh
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(OpenKalaRadiusTokens.Large)
            .background(OpenKalaColorTokens.Surface)
            .padding(16.dp)
    ) {
        Text(
            text = message,
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = OpenKalaColorTokens.TextPrimary
        )
        Text(
            text = "تلاش مجدد",
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = OpenKalaColorTokens.BrandPrimary,
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable(onClick = onRetry)
        )
    }
}

private fun colorFromHex(raw: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(raw)) }
        .getOrDefault(OpenKalaColorTokens.Border)
}

private fun Long.toToman(): String {
    val tomanValue = this / 10
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
    return formatter.format(tomanValue)
}

private fun Long.toTimerText(): String {
    val safe = coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return "${hours.pad2()}:${minutes.pad2()}:${seconds.pad2()}".toPersianDigits()
}

private fun Number.toPersianDigits(): String = toString().toPersianDigits()

private fun String.toPersianDigits(): String {
    val map = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder(length)
    forEach { ch ->
        if (ch in '0'..'9') {
            builder.append(map[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

private fun Long.pad2(): String = toString().padStart(2, '0')
