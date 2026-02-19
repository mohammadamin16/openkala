package com.openkala.app.ui.search.results

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.SettingsInputComponent
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openkala.app.domain.model.SearchFilterChip
import com.openkala.app.domain.model.SearchProductItem
import com.openkala.app.domain.model.SearchRecommendationItem
import com.openkala.app.ui.search.SharedSearchBar
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchResultsScreenRoute(
    query: String,
    categoryCode: String?,
    onBack: () -> Unit,
    onSearchBarClick: (String) -> Unit,
    onProductClick: (SearchProductItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: SearchResultsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(query, categoryCode) {
        viewModel.ensureLoaded(query = query, categoryCode = categoryCode)
    }

    LaunchedEffect(listState, state) {
        snapshotFlow { shouldLoadMore(listState) }
            .map { should -> should && (state as? SearchResultsUiState.Content)?.hasMore == true }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                viewModel.loadNextPage()
            }
    }

    LaunchedEffect(state) {
        val content = state as? SearchResultsUiState.Content ?: return@LaunchedEffect
        val message = content.transientMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeTransientMessage()
    }

    SearchResultsScreen(
        state = state,
        listState = listState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = { viewModel.reloadWith(query, categoryCode) },
        onSearchBarClick = onSearchBarClick,
        onRecommendationClick = { recommendation ->
            viewModel.reloadWith(
                query = recommendation.keyword,
                categoryCode = recommendation.categoryCode
            )
        },
        onProductClick = onProductClick,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SearchResultsScreen(
    state: SearchResultsUiState,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSearchBarClick: (String) -> Unit,
    onRecommendationClick: (SearchRecommendationItem) -> Unit,
    onProductClick: (SearchProductItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = OpenKalaColorTokens.AppBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (state) {
            SearchResultsUiState.LoadingNoCache -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OpenKalaColorTokens.BrandPrimary)
                }
            }

            is SearchResultsUiState.ErrorNoData -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "خطا در دریافت نتایج",
                        style = OpenKalaTypographyTokens.SubtitleStrong,
                        color = OpenKalaColorTokens.TextPrimary
                    )
                    Text(
                        text = state.message,
                        style = OpenKalaTypographyTokens.Body1,
                        color = OpenKalaColorTokens.TextLow,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                        Text("تلاش مجدد", style = OpenKalaTypographyTokens.Button2)
                    }
                }
            }

            is SearchResultsUiState.Content -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(OpenKalaColorTokens.AppBackground),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    stickyHeader {
                        ResultsStickyHeader(
                            query = state.query,
                            chips = state.header.chips,
                            onBack = onBack,
                            onSearchBarClick = onSearchBarClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }

                    if (state.isRefreshing) {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = OpenKalaColorTokens.BrandPrimary,
                                trackColor = OpenKalaColorTokens.SurfaceMuted
                            )
                        }
                    }

                    if (state.recommendations.isNotEmpty() && state.categoryCode.isNullOrBlank()) {
                        item {
                            RecommendationSection(
                                recommendations = state.recommendations,
                                onRecommendationClick = onRecommendationClick,
                                totalItems = state.header.totalItems
                            )
                        }
                    }

                    items(state.items, key = { it.id }) { product ->
                        SearchProductRow(
                            item = product,
                            onClick = { onProductClick(product) }
                        )
                    }

                    if (state.isAppending) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = OpenKalaColorTokens.BrandPrimary,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ResultsStickyHeader(
    query: String,
    chips: List<SearchFilterChip>,
    onBack: () -> Unit,
    onSearchBarClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(OpenKalaColorTokens.AppBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = OpenKalaColorTokens.TextHigh,
                modifier = Modifier
                    .size(30.dp)
                    .clickable(onClick = onBack)
            )

            SharedSearchBar(
                query = query,
                placeholder = "جستجو در همه کالاها",
                onQueryChange = {},
                readOnly = true,
                onClick = { onSearchBarClick(query) },
                modifier = Modifier.weight(1f),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp)
        ) {
            items(chips, key = { it.id }) { chip ->
                FilterChip(chip)
            }
        }

        HorizontalDivider(color = OpenKalaColorTokens.Border)
    }
}

@Composable
private fun FilterChip(chip: SearchFilterChip) {
    val borderColor = if (chip.selected) colorBlue else OpenKalaColorTokens.Border
    val textColor = if (chip.selected) colorBlue else OpenKalaColorTokens.TextHigh

    Row(
        modifier = Modifier
            .height(42.dp)
            .clip(OpenKalaRadiusTokens.Pill)
            .border(1.dp, borderColor, OpenKalaRadiusTokens.Pill)
            .background(OpenKalaColorTokens.Surface)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (chip.id) {
            "sort" -> Icon(Icons.Outlined.Sort, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            "filter" -> Icon(Icons.Outlined.SettingsInputComponent, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            "price" -> Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            else -> Unit
        }

        Text(
            text = chip.title,
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RecommendationSection(
    recommendations: List<SearchRecommendationItem>,
    onRecommendationClick: (SearchRecommendationItem) -> Unit,
    totalItems: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "همه کالاها",
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.TextMedium
            )
            Text(
                text = "${totalItems.toString().toPersianDigits()} کالا",
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.TextLow
            )
        }

        Text(
            text = "برای نتایج دقیق‌تر یک دسته‌بندی انتخاب کنید",
            style = OpenKalaTypographyTokens.H5,
            color = OpenKalaColorTokens.TextHigh,
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
        )

        recommendations.take(5).forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecommendationClick(item) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "${item.keyword} در دسته ${item.categoryTitle}",
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.TextHigh,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            if (index != recommendations.take(5).lastIndex) {
                HorizontalDivider(color = OpenKalaColorTokens.Border)
            }
        }
    }
}

@Composable
private fun SearchProductRow(
    item: SearchProductItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(132.dp)
                    .clip(OpenKalaRadiusTokens.Large),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(132.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = OpenKalaTypographyTokens.H5,
                    color = OpenKalaColorTokens.TextHigh,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.rating?.let {
                        Text(
                            text = it.formatOneDecimal().toPersianDigits(),
                            style = OpenKalaTypographyTokens.SubtitleStrong,
                            color = OpenKalaColorTokens.TextHigh
                        )
                    }
                    Text(
                        text = "★",
                        style = OpenKalaTypographyTokens.SubtitleStrong,
                        color = colorGold
                    )
                    item.shippingText?.let {
                        Text(
                            text = it,
                            style = OpenKalaTypographyTokens.Subtitle,
                            color = OpenKalaColorTokens.TextLow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.discountPercent?.takeIf { it > 0 }?.let {
                                Text(
                                    text = "%${it.toString().toPersianDigits()}",
                                    style = OpenKalaTypographyTokens.CaptionStrong,
                                    color = OpenKalaColorTokens.White,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(OpenKalaColorTokens.BrandPrimary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = item.price?.toSeparatedPersian().orEmpty(),
                                style = OpenKalaTypographyTokens.H5,
                                color = OpenKalaColorTokens.TextHigh,
                                fontWeight = FontWeight.W700
                            )
                            Text(
                                text = "تومان",
                                style = OpenKalaTypographyTokens.Subtitle,
                                color = OpenKalaColorTokens.TextMedium
                            )
                        }

                        item.rrpPrice?.takeIf { item.discountPercent != null && item.discountPercent > 0 }?.let {
                            Text(
                                text = it.toSeparatedPersian(),
                                style = OpenKalaTypographyTokens.Subtitle,
                                color = OpenKalaColorTokens.TextLow
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(item.colorCount.coerceIn(0, 3)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(OpenKalaColorTokens.SurfaceMuted)
                                    .border(1.dp, OpenKalaColorTokens.Border, CircleShape)
                            )
                        }
                        if (item.colorCount > 3) {
                            Text(
                                text = "+",
                                style = OpenKalaTypographyTokens.Subtitle,
                                color = OpenKalaColorTokens.TextLow
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = OpenKalaColorTokens.Border,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

private fun shouldLoadMore(state: LazyListState): Boolean {
    val total = state.layoutInfo.totalItemsCount
    if (total == 0) return false
    val lastVisible = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return false
    return lastVisible >= total - 4
}

private fun Double.formatOneDecimal(): String = String.format(java.util.Locale.US, "%.1f", this)

private fun Long.toSeparatedPersian(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
        .toPersianDigits()
}

private fun String.toPersianDigits(): String {
    val map = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder(length)
    forEach { ch ->
        if (ch in '0'..'9') builder.append(map[ch - '0']) else builder.append(ch)
    }
    return builder.toString()
}

private val colorBlue = androidx.compose.ui.graphics.Color(0xFF5A5EF5)
private val colorGold = androidx.compose.ui.graphics.Color(0xFFFFC107)
