package com.openkala.app.ui.home

import android.graphics.Color.parseColor
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openkala.app.domain.model.Banner
import com.openkala.app.domain.model.HomeScreenData
import com.openkala.app.domain.model.IncredibleOfferItem
import com.openkala.app.domain.model.SuperAppTab
import com.openkala.app.ui.search.SharedSearchBar
import com.openkala.app.ui.theme.DigikalaRed
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens
import com.openkala.app.ui.theme.TextPrimary
import com.openkala.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenRoute(
    onProductClick: (IncredibleOfferItem) -> Unit = {},
    onSearchClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (state) {
        HomeUiState.Loading -> LoadingHomeScreen()
        is HomeUiState.Error -> ErrorHomeScreen(
            message = (state as HomeUiState.Error).message,
            onRetry = viewModel::refresh
        )
        is HomeUiState.Content -> HomeScreen(
            data = (state as HomeUiState.Content).data,
            isRefreshing = (state as HomeUiState.Content).isRefreshing,
            styleSpec = PixelPerfectHomeStyle,
            pixelPerfectMode = PixelPerfectMode.Enabled,
            onProductClick = onProductClick,
            onSearchClick = onSearchClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun LoadingHomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = DigikalaRed)
    }
}

@Composable
private fun ErrorHomeScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "خطا در دریافت اطلاعات",
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = OpenKalaTypographyTokens.Body1,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("تلاش مجدد", style = OpenKalaTypographyTokens.Button2)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeScreen(
    data: HomeScreenData,
    isRefreshing: Boolean,
    styleSpec: HomeStyleSpec,
    pixelPerfectMode: Boolean,
    onProductClick: (IncredibleOfferItem) -> Unit,
    onSearchClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
) {
    var selectedTab by remember(data.selectedTabName) { mutableStateOf(data.selectedTabName) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {}
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(OpenKalaColorTokens.AppBackground)
                .padding(padding)
                .testTag("home_list"),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            item {
                TopTabsRow(
                    tabs = data.superAppTabs,
                    selectedTab = selectedTab,
                    styleSpec = styleSpec,
                    onTabClick = { tab -> selectedTab = tab.name }
                )
            }

            item {
                SearchAndLocationSection(
                    styleSpec = styleSpec,
                    onSearchClick = onSearchClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }

            item {
                val banners = data.heroBanners
                val pagerState = rememberPagerState(pageCount = { banners.size.coerceAtLeast(1) })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(styleSpec.heroSectionHeight)
                        .background(OpenKalaColorTokens.Surface)
                        .padding(vertical = 10.dp)
                ) { page ->
                    val banner = banners.getOrNull(page)
                    if (banner != null) {
                        AsyncImage(
                            model = banner.imageUrl,
                            contentDescription = banner.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(styleSpec.heroSectionHeight - 20.dp)
                                .padding(horizontal = styleSpec.heroHorizontalPadding)
                                .clip(OpenKalaRadiusTokens.Large),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item { ShortcutsRow(data, styleSpec) }

            item {
                IncredibleSection(
                    items = data.incredibleOffers.items,
                    styleSpec = styleSpec,
                    pixelPerfectMode = pixelPerfectMode,
                    onProductClick = onProductClick
                )
            }
            item {
                TopBannersSection(
                    banners = data.topBanners,
                    styleSpec = styleSpec
                )
            }

            if (isRefreshing && !pixelPerfectMode) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "در حال به‌روزرسانی...",
                            style = OpenKalaTypographyTokens.Caption,
                            color = OpenKalaColorTokens.TextLow
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TopBannersSection(
    banners: List<Banner>,
    styleSpec: HomeStyleSpec,
    onBannerClick: (Banner) -> Unit = {}
) {
    val visibleBanners = banners.take(4)
    if (visibleBanners.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.AppBackground)
            .padding(
                horizontal = styleSpec.topBannersSectionHorizontalPadding,
                vertical = styleSpec.topBannersSectionVerticalPadding
            )
            .testTag("home_top_banners_section"),
        verticalArrangement = Arrangement.spacedBy(styleSpec.topBannersGridGap)
    ) {
        if (visibleBanners.size == 1) {
            val banner = visibleBanners.first()
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(styleSpec.topBannersCardAspectRatio)
                    .clip(RoundedCornerShape(styleSpec.topBannersCardRadius))
                    .clickable { onBannerClick(banner) }
                    .testTag("home_top_banner_card")
            )
            return
        }

        visibleBanners.chunked(2).forEach { rowBanners ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(styleSpec.topBannersGridGap)
            ) {
                rowBanners.forEach { banner ->
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(styleSpec.topBannersCardAspectRatio)
                            .clip(RoundedCornerShape(styleSpec.topBannersCardRadius))
                            .clickable { onBannerClick(banner) }
                            .testTag("home_top_banner_card")
                    )
                }
                if (rowBanners.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TopTabsRow(
    tabs: List<SuperAppTab>,
    selectedTab: String,
    styleSpec: HomeStyleSpec,
    onTabClick: (SuperAppTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.AppBackground)
            .padding(top = styleSpec.tabRowTopPadding, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = styleSpec.tabRowHorizontalPadding)
    ) {
        items(tabs, key = { it.name }) { tab ->
            val selected = tab.name == selectedTab
            Column(
                modifier = Modifier
                    .size(width = styleSpec.tabCardWidth, height = styleSpec.tabCardHeight)
                    .clip(OpenKalaRadiusTokens.Large)
                    .background(if (selected) DigikalaRed else OpenKalaColorTokens.SurfaceMuted)
                    .border(1.dp, OpenKalaColorTokens.Border, OpenKalaRadiusTokens.Large)
                    .clickable { onTabClick(tab) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = tab.iconUrl,
                    contentDescription = tab.title,
                    modifier = Modifier.size(styleSpec.tabIconSize)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.title,
                    style = OpenKalaTypographyTokens.Body1Strong.copy(
                        lineHeight = 20.sp
                    ),
                    color = if (selected) OpenKalaColorTokens.White else OpenKalaColorTokens.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SearchAndLocationSection(
    styleSpec: HomeStyleSpec,
    onSearchClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.AppBackground)
            .padding(
                horizontal = styleSpec.searchSectionHorizontalPadding,
                vertical = styleSpec.searchSectionVerticalPadding
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SharedSearchBar(
                query = "",
                placeholder = "جستجو در همه کالاها",
                onQueryChange = {},
                readOnly = true,
                onClick = onSearchClick,
                modifier = Modifier.weight(1f),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )

            Box(
                modifier = Modifier
                    .size(styleSpec.notificationBubbleSize)
                    .clip(CircleShape)
                    .background(OpenKalaColorTokens.SurfaceMuted)
                    .border(1.dp, OpenKalaColorTokens.Border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notification",
                    tint = OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ShortcutsRow(
    data: HomeScreenData,
    styleSpec: HomeStyleSpec
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = styleSpec.shortcutRowHorizontalPadding,
                vertical = styleSpec.shortcutRowVerticalPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.shortcuts.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(styleSpec.shortcutLabelWidth)
            ) {
                AsyncImage(
                    model = item.iconUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(styleSpec.shortcutIconSize)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.TextHigh
                )
            }
        }
    }
}

@Composable
private fun IncredibleSection(
    items: List<IncredibleOfferItem>,
    styleSpec: HomeStyleSpec,
    pixelPerfectMode: Boolean,
    onProductClick: (IncredibleOfferItem) -> Unit
) {
    val firstTimer = if (pixelPerfectMode) 31736L else (items.firstOrNull()?.timerSeconds ?: 0L)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE71E52))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "شگفت‌انگیز",
                style = OpenKalaTypographyTokens.H5,
                color = OpenKalaColorTokens.White,
                fontSize = styleSpec.incredibleHeaderTextSize,
                fontWeight = FontWeight.W900
            )
            TimerBadge(firstTimer, styleSpec)
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items, key = { it.id }) { product ->
                Column(
                    modifier = Modifier
                        .width(styleSpec.productCardWidth)
                        .clip(OpenKalaRadiusTokens.Medium)
                        .background(OpenKalaColorTokens.Surface)
                        .clickable { onProductClick(product) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier.size(styleSpec.productImageSize)
                    )
                    Text(
                        text = product.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = OpenKalaTypographyTokens.Body2,
                        color = OpenKalaColorTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        product.discountPercent?.let {
                            Text(
                                text = "${it.toString().toPersianDigits()}%",
                                color = OpenKalaColorTokens.White,
                                style = OpenKalaTypographyTokens.CaptionStrong,
                                modifier = Modifier
                                    .clip(OpenKalaRadiusTokens.Medium)
                                    .background(OpenKalaColorTokens.BrandPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = product.price?.toString()?.toPersianDigits().orEmpty(),
                            style = OpenKalaTypographyTokens.Body2Strong
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerBadge(totalSeconds: Long, styleSpec: HomeStyleSpec) {
    val initial = totalSeconds.coerceAtLeast(0)
    var remaining by remember(initial) { mutableStateOf(initial) }

    LaunchedEffect(initial) {
        remaining = initial
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
    }

    val hours = (remaining / 3600).coerceAtLeast(0)
    val minutes = ((remaining % 3600) / 60).coerceAtLeast(0)
    val seconds = (remaining % 60).coerceAtLeast(0)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TimePart(seconds.toString().padStart(2, '0').toPersianDigits(), styleSpec)
        TimePart(minutes.toString().padStart(2, '0').toPersianDigits(), styleSpec)
        TimePart(hours.toString().padStart(2, '0').toPersianDigits(), styleSpec)
    }
}

@Composable
private fun TimePart(value: String, styleSpec: HomeStyleSpec) {
    Box(
        modifier = Modifier
            .height(styleSpec.timerBadgeHeight)
            .widthIn(min = styleSpec.timerBadgeMinWidth)
            .clip(OpenKalaRadiusTokens.Medium)
            .background(OpenKalaColorTokens.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = OpenKalaColorTokens.TextHigh,
            style = OpenKalaTypographyTokens.H5,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private fun colorFromHex(value: String): Color {
    return runCatching { Color(parseColor(value)) }.getOrDefault(Color.White)
}

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
