package com.openkala.app.ui.categories

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.TwoWheeler
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openkala.app.domain.model.CategoryLeafItem
import com.openkala.app.domain.model.CategorySection
import com.openkala.app.domain.model.CategoryTabItem
import com.openkala.app.ui.search.SharedSearchBar
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CategoriesScreenRoute(
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        val content = state as? CategoriesUiState.Content ?: return@LaunchedEffect
        val message = content.transientMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeTransientMessage()
    }

    CategoriesScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onRetry = viewModel::refresh,
        onSearchClick = onSearchClick,
        onBackClick = onBackClick,
        onTabSelected = viewModel::onTabSelected,
        onSectionToggle = viewModel::onSectionToggle,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CategoriesScreen(
    state: CategoriesUiState,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    onTabSelected: (Long) -> Unit,
    onSectionToggle: (Long) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = OpenKalaColorTokens.AppBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when (state) {
            CategoriesUiState.LoadingNoCache -> CategoriesLoadingShimmer(
                modifier = Modifier.padding(padding)
            )

            is CategoriesUiState.ErrorNoData -> CategoriesErrorState(
                message = state.message,
                onRetry = onRetry,
                modifier = Modifier.padding(padding)
            )

            is CategoriesUiState.Content -> CategoriesContent(
                state = state,
                onSearchClick = onSearchClick,
                onBackClick = onBackClick,
                onTabSelected = onTabSelected,
                onSectionToggle = onSectionToggle,
                modifier = Modifier.padding(padding),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CategoriesContent(
    state: CategoriesUiState.Content,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    onTabSelected: (Long) -> Unit,
    onSectionToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val selectedTab = state.data.tabs.firstOrNull { it.id == state.selectedTabId }
    val sections = state.data.sectionsByTabId[state.selectedTabId].orEmpty()
    val expandedSectionId = state.expandedSectionByTab[state.selectedTabId]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenKalaColorTokens.AppBackground)
            .statusBarsPadding()
    ) {
        CategoriesTopSearchBar(
            onSearchClick = onSearchClick,
            onBackClick = onBackClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )

        AnimatedVisibility(visible = state.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = OpenKalaColorTokens.BrandPrimary,
                trackColor = OpenKalaColorTokens.SurfaceMuted
            )
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            CategoryRail(
                tabs = state.data.tabs,
                selectedTabId = state.selectedTabId,
                onTabSelected = onTabSelected,
                modifier = Modifier
                    .width(126.dp)
                    .fillMaxHeight()
            )

            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(),
                color = OpenKalaColorTokens.Border
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = OpenKalaColorTokens.TextMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "همه محصولات ${selectedTab?.title.orEmpty()}",
                        style = OpenKalaTypographyTokens.H5,
                        color = Color(0xFF0F9CD8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Crossfade(
                    targetState = sections,
                    animationSpec = tween(durationMillis = 180),
                    label = "category_sections"
                ) { targetSections ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 18.dp)
                    ) {
                        items(targetSections, key = { it.id }) { section ->
                            CategorySectionRow(
                                section = section,
                                expanded = section.id == expandedSectionId,
                                onToggle = { onSectionToggle(section.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CategoriesTopSearchBar(
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = null,
            tint = OpenKalaColorTokens.TextHigh,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBackClick)
        )

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
    }
}

@Composable
private fun CategoryRail(
    tabs: List<CategoryTabItem>,
    selectedTabId: Long,
    onTabSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.background(OpenKalaColorTokens.SurfaceMuted)
    ) {
        items(tabs, key = { it.id }) { tab ->
            val selected = tab.id == selectedTabId
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (selected) OpenKalaColorTokens.Surface else OpenKalaColorTokens.SurfaceMuted)
                    .clickable { onTabSelected(tab.id) }
                    .padding(horizontal = 6.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = iconForCategory(tab),
                    contentDescription = tab.title,
                    tint = if (selected) OpenKalaColorTokens.BrandPrimary else OpenKalaColorTokens.TextMedium,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tab.title,
                    style = OpenKalaTypographyTokens.Subtitle,
                    color = if (selected) OpenKalaColorTokens.BrandPrimary else OpenKalaColorTokens.TextHigh,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 56.dp)
                )
            }

            HorizontalDivider(color = OpenKalaColorTokens.Border)
        }
    }
}

@Composable
private fun CategorySectionRow(
    section: CategorySection,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.Surface)
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = OpenKalaColorTokens.TextMedium,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = section.title,
                style = OpenKalaTypographyTokens.H5,
                color = OpenKalaColorTokens.TextPrimary
            )
        }

        if (expanded) {
            if (section.items.isEmpty()) {
                Text(
                    text = "آیتمی برای نمایش وجود ندارد",
                    style = OpenKalaTypographyTokens.Body1,
                    color = OpenKalaColorTokens.TextLow,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    section.items.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { item ->
                                CategoryLeafItemCard(item = item)
                            }

                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.size(width = 92.dp, height = 112.dp))
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = OpenKalaColorTokens.Border)
    }
}

@Composable
private fun CategoryLeafItemCard(item: CategoryLeafItem) {
    Column(
        modifier = Modifier
            .width(92.dp)
            .padding(vertical = 10.dp)
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(OpenKalaColorTokens.SurfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = null,
                    tint = OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = item.title,
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = OpenKalaColorTokens.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategoriesLoadingShimmer(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenKalaColorTokens.AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(OpenKalaRadiusTokens.Pill)
                .background(shimmerBrush)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(168.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(9) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(OpenKalaRadiusTokens.Medium)
                            .background(shimmerBrush)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(10) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (it == 2) 150.dp else 56.dp)
                            .clip(OpenKalaRadiusTokens.Medium)
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "خطا در دریافت دسته‌بندی‌ها",
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = OpenKalaColorTokens.TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = OpenKalaTypographyTokens.Body1,
            color = OpenKalaColorTokens.TextLow,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(onClick = onRetry) {
            Text("تلاش مجدد", style = OpenKalaTypographyTokens.Button2)
        }
        Spacer(modifier = Modifier.height(12.dp))
        CircularProgressIndicator(
            color = OpenKalaColorTokens.BrandPrimary,
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val offset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    return Brush.linearGradient(
        colors = listOf(
            OpenKalaColorTokens.SurfaceMuted,
            OpenKalaColorTokens.Surface,
            OpenKalaColorTokens.SurfaceMuted
        ),
        start = Offset(offset - 220f, offset - 220f),
        end = Offset(offset, offset)
    )
}

private fun iconForCategory(tab: CategoryTabItem): ImageVector {
    val key = "${tab.iconToken}|${tab.code}|${tab.title}".lowercase()
    return when {
        "mobile" in key -> Icons.Outlined.PhoneAndroid
        "laptop" in key -> Icons.Outlined.Laptop
        "elect" in key || "digital" in key -> Icons.Outlined.ElectricalServices
        "home" in key || "kitchen" in key || "خانه" in key -> Icons.Outlined.Home
        "beauty" in key || "health" in key || "آرایشی" in key -> Icons.Outlined.FavoriteBorder
        "fashion" in key || "apparel" in key || "پوشاک" in key -> Icons.Outlined.Checkroom
        "gold" in key || "silver" in key || "طلا" in key -> Icons.Outlined.Diamond
        "vehicle" in key || "خودرو" in key || "موتور" in key -> Icons.Outlined.TwoWheeler
        "medical" in key || "پزشکی" in key || "سلامت" in key -> Icons.Outlined.HealthAndSafety
        "tool" in key || "ابزار" in key -> Icons.Outlined.Build
        else -> Icons.Outlined.Category
    }
}
