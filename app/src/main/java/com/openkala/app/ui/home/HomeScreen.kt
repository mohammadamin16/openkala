package com.openkala.app.ui.home

import android.graphics.Color.parseColor
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.openkala.app.domain.model.HomeScreenData
import com.openkala.app.domain.model.IncredibleOfferItem
import com.openkala.app.domain.model.SuperAppTab
import com.openkala.app.ui.theme.DigikalaRed
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens
import com.openkala.app.ui.theme.TextPrimary
import com.openkala.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun HomeScreenRoute(
    onProductClick: (IncredibleOfferItem) -> Unit = {},
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
            onProductClick = onProductClick
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    data: HomeScreenData,
    isRefreshing: Boolean,
    styleSpec: HomeStyleSpec,
    pixelPerfectMode: Boolean,
    onProductClick: (IncredibleOfferItem) -> Unit
) {
    var selectedTab by remember(data.selectedTabName) { mutableStateOf(data.selectedTabName) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = { HomeBottomBar(styleSpec) }
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
                    onTabClick = { tab ->
                        if (tab.name == "digikala") selectedTab = tab.name
                    }
                )
            }

            item {
                SearchAndLocationSection(styleSpec)
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
                    .background(if (selected) colorFromHex(tab.backgroundColorHex) else OpenKalaColorTokens.SurfaceMuted)
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
                    color = if (selected) colorFromHex(tab.focusedTextColorHex) else OpenKalaColorTokens.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SearchAndLocationSection(styleSpec: HomeStyleSpec) {
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
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(styleSpec.searchBarHeight)
                    .clip(OpenKalaRadiusTokens.Pill)
                    .background(OpenKalaColorTokens.SurfaceMuted)
                    .border(1.dp, OpenKalaColorTokens.Border, OpenKalaRadiusTokens.Pill)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(styleSpec.searchIconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    tint = Color(0xFF7A4BE3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "جستجو در ",
                    style = OpenKalaTypographyTokens.Subtitle,
                    color = OpenKalaColorTokens.TextLow,
                    fontSize = styleSpec.searchFontSize
                )
                Text(
                    text = "دیجی‌کالا",
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.BrandPrimary,
                    fontSize = styleSpec.searchFontSize
                )
            }

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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(6.dp))
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
    Text(
        text = value,
        color = OpenKalaColorTokens.TextHigh,
        style = OpenKalaTypographyTokens.H5,
        modifier = Modifier
            .height(styleSpec.timerBadgeHeight)
            .widthIn(min = styleSpec.timerBadgeMinWidth)
            .clip(OpenKalaRadiusTokens.Medium)
            .background(OpenKalaColorTokens.White)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun HomeBottomBar(styleSpec: HomeStyleSpec) {
    val items = listOf(
        Triple("دیجی‌کالای من", Icons.Outlined.PersonOutline, false),
        Triple("مگنت", Icons.Outlined.PlayArrow, false),
        Triple("سبد خرید", Icons.Outlined.ShoppingCart, false),
        Triple("دسته‌بندی", Icons.Outlined.Category, false),
        Triple("خانه", Icons.Outlined.Home, true)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .padding(top = styleSpec.bottomNavTopPadding, bottom = styleSpec.bottomNavBottomPadding),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = item.second,
                    contentDescription = item.first,
                    tint = if (item.third) OpenKalaColorTokens.TextPrimary else OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(styleSpec.bottomNavIconSize)
                )
                Text(
                    text = item.first,
                    style = OpenKalaTypographyTokens.Subtitle,
                    color = if (item.third) OpenKalaColorTokens.TextPrimary else OpenKalaColorTokens.TextMedium,
                    fontSize = styleSpec.bottomNavTextSize
                )
            }
        }
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
