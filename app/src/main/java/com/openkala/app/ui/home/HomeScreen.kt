package com.openkala.app.ui.home

import android.content.Intent
import android.graphics.Color.parseColor
import android.os.SystemClock
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openkala.app.domain.model.Banner
import com.openkala.app.domain.model.HomeCategoryItem
import com.openkala.app.domain.model.HomeScreenData
import com.openkala.app.domain.model.IncredibleOfferItem
import com.openkala.app.domain.model.ShortcutItem
import com.openkala.app.domain.model.SuperAppTab
import com.openkala.app.ui.search.SharedSearchBar
import com.openkala.app.ui.theme.DigikalaRed
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens
import com.openkala.app.ui.theme.TextPrimary
import com.openkala.app.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenRoute(
    onProductClick: (IncredibleOfferItem) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onWebModeChanged: (Boolean) -> Unit = {},
    onBannerOpenStateChanged: (Boolean) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (state) {
        HomeUiState.Loading -> {
            LaunchedEffect(Unit) { onWebModeChanged(false) }
            LaunchedEffect(Unit) { onBannerOpenStateChanged(false) }
            LoadingHomeScreen()
        }
        is HomeUiState.Error -> {
            LaunchedEffect(Unit) { onWebModeChanged(false) }
            LaunchedEffect(Unit) { onBannerOpenStateChanged(false) }
            ErrorHomeScreen(
                message = (state as HomeUiState.Error).message,
                onRetry = viewModel::refresh
            )
        }
        is HomeUiState.Content -> HomeScreen(
            data = (state as HomeUiState.Content).data,
            isRefreshing = (state as HomeUiState.Content).isRefreshing,
            styleSpec = PixelPerfectHomeStyle,
            pixelPerfectMode = PixelPerfectMode.Enabled,
            onProductClick = onProductClick,
            onSearchClick = onSearchClick,
            onWebModeChanged = onWebModeChanged,
            onBannerOpenStateChanged = onBannerOpenStateChanged,
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
internal fun HomeScreen(
    data: HomeScreenData,
    isRefreshing: Boolean,
    styleSpec: HomeStyleSpec,
    pixelPerfectMode: Boolean,
    onProductClick: (IncredibleOfferItem) -> Unit,
    onSearchClick: () -> Unit,
    onWebModeChanged: (Boolean) -> Unit,
    onBannerOpenStateChanged: (Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
) {
    val context = LocalContext.current
    var selectedTab by remember(data.selectedTabName) { mutableStateOf(data.selectedTabName) }
    var currentWebView by remember { mutableStateOf<WebView?>(null) }
    val webLoadingByTab = remember { mutableStateMapOf<String, Boolean>() }
    var isBannerOverlayVisible by remember { mutableStateOf(false) }
    var bannerOverlayInitialUrl by remember { mutableStateOf("") }
    var bannerOverlayCurrentUrl by remember { mutableStateOf("") }
    var bannerOverlayTitle by remember { mutableStateOf("فروشگاه اینترنتی دیجی‌کالا") }
    var bannerOverlayWebView by remember { mutableStateOf<WebView?>(null) }
    val selectedTabData = remember(data.superAppTabs, selectedTab) {
        data.superAppTabs.firstOrNull { it.name == selectedTab }
    }
    val inWebMode = selectedTab != "digikala"
    val webLoading = webLoadingByTab[selectedTab] ?: false
    val topTabsWebViewPool = remember(context) { TopTabsWebViewPool(context.applicationContext) }
    val listState = rememberLazyListState()
    val topTabsExpandedHeight = styleSpec.tabCardHeight + styleSpec.tabRowTopPadding + 6.dp
    val collapseRangePx = with(LocalDensity.current) { topTabsExpandedHeight.toPx().coerceAtLeast(1f) }
    val targetCollapseProgress by remember(inWebMode, listState, collapseRangePx) {
        derivedStateOf {
            if (inWebMode) {
                0f
            } else {
                val scrolledPx = if (listState.firstVisibleItemIndex > 0) {
                    collapseRangePx
                } else {
                    listState.firstVisibleItemScrollOffset.toFloat()
                }
                (scrolledPx / collapseRangePx).coerceIn(0f, 1f)
            }
        }
    }
    val collapseProgress by animateFloatAsState(
        targetValue = targetCollapseProgress,
        label = "home_top_tabs_collapse"
    )

    LaunchedEffect(inWebMode) {
        onWebModeChanged(inWebMode)
    }
    LaunchedEffect(isBannerOverlayVisible) {
        onBannerOpenStateChanged(isBannerOverlayVisible)
    }
    LaunchedEffect(data.superAppTabs, selectedTab) {
        if (selectedTab != "digikala") return@LaunchedEffect
        delay(1200)
        val preloadCandidate = data.superAppTabs.firstOrNull { tab ->
            tab.name != "digikala" && normalizeWebUrl(tab.webUrl).isNotBlank()
        } ?: return@LaunchedEffect
        val preloadUrl = normalizeWebUrl(preloadCandidate.webUrl)
        topTabsWebViewPool.preload(
            tabKey = preloadCandidate.name,
            normalizedUrl = preloadUrl,
            onLoadingChanged = { loading ->
                webLoadingByTab[preloadCandidate.name] = loading
            }
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            topTabsWebViewPool.destroyAll()
            onWebModeChanged(false)
            onBannerOpenStateChanged(false)
        }
    }

    val closeBannerOverlay = {
        isBannerOverlayVisible = false
        bannerOverlayInitialUrl = ""
        bannerOverlayCurrentUrl = ""
        bannerOverlayTitle = "فروشگاه اینترنتی دیجی‌کالا"
        bannerOverlayWebView = null
    }
    val openWebOverlay: (String, String) -> Unit = { title, deeplink ->
        val normalized = normalizeWebUrl(deeplink)
        if (normalized.isNotBlank()) {
            bannerOverlayTitle = title.ifBlank { "فروشگاه اینترنتی دیجی‌کالا" }
            bannerOverlayInitialUrl = normalized
            bannerOverlayCurrentUrl = normalized
            isBannerOverlayVisible = true
        }
    }
    val openBannerOverlay: (Banner) -> Unit = { banner ->
        openWebOverlay("فروشگاه اینترنتی دیجی‌کالا", banner.deeplink)
    }

    BackHandler(enabled = isBannerOverlayVisible) {
        val webView = bannerOverlayWebView
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            closeBannerOverlay()
        }
    }

    BackHandler(enabled = inWebMode && !isBannerOverlayVisible) {
        val webView = currentWebView
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            selectedTab = "digikala"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(OpenKalaColorTokens.AppBackground)
            .testTag("home_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topTabsExpandedHeight * (1f - collapseProgress))
            ) {
                if (collapseProgress < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(1f - collapseProgress)
                    ) {
                        TopTabsRow(
                            tabs = data.superAppTabs,
                            selectedTab = selectedTab,
                            styleSpec = styleSpec,
                            onTabClick = { tab -> selectedTab = tab.name }
                        )
                    }
                }
            }

            if (!inWebMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("home_native_content")
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_list"),
                        state = listState,
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
                    stickyHeader {
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
                    LaunchedEffect(banners.size) {
                        if (banners.size <= 1) return@LaunchedEffect
                        while (true) {
                            delay(7_000)
                            val nextPage = (pagerState.currentPage + 1) % banners.size
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OpenKalaColorTokens.Surface)
                            .padding(vertical = 10.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = styleSpec.heroCarouselHorizontalPadding),
                            pageSpacing = styleSpec.heroCarouselPageSpacing
                        ) { page ->
                            val banner = banners.getOrNull(page)
                            if (banner != null) {
                                AsyncImage(
                                    model = banner.imageUrl,
                                    contentDescription = banner.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(styleSpec.heroCardAspectRatio)
                                        .clip(RoundedCornerShape(styleSpec.heroCardRadius))
                                        .clickable { openBannerOverlay(banner) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        if (banners.size > 1) {
                            val indicatorSlotWidth = 14.dp
                            val indicatorGap = 4.dp
                            val indicatorStep = indicatorSlotWidth + indicatorGap
                            val activeOffsetX by animateDpAsState(
                                targetValue = indicatorStep * pagerState.currentPage,
                                animationSpec = spring(
                                    dampingRatio = 0.72f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "hero_indicator_offset"
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x8A111827))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(indicatorGap),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(banners.size) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = indicatorSlotWidth, height = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x809CA3AF))
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .offset(x = activeOffsetX)
                                        .size(width = indicatorSlotWidth, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(OpenKalaColorTokens.White)
                                )
                            }
                        }
                    }
                }

                    item {
                        ShortcutsRow(
                            data = data,
                            styleSpec = styleSpec,
                            onShortcutClick = { shortcut ->
                                openWebOverlay(shortcut.title, shortcut.deeplink)
                            }
                        )
                    }

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
                        styleSpec = styleSpec,
                        onBannerClick = openBannerOverlay
                    )
                }

                item {
                    FreshIncredibleSection(
                        title = data.freshIncredibleOffers.title,
                        items = data.freshIncredibleOffers.items,
                        styleSpec = styleSpec,
                        pixelPerfectMode = pixelPerfectMode,
                        onProductClick = onProductClick
                    )
                }
                item {
                    MiddlePromoBannersSection(
                        banners = data.middlePromoBanners,
                        styleSpec = styleSpec,
                        onBannerClick = openBannerOverlay
                    )
                }
                item {
                    HomeCategoriesSection(
                        title = data.homeCategoriesTitle,
                        rows = data.homeCategoriesRows,
                        categories = data.homeCategories,
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
            } else {
                TopTabWebViewContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("home_top_tab_webview_container"),
                    tabKey = selectedTabData?.name.orEmpty(),
                    url = selectedTabData?.webUrl.orEmpty(),
                    pool = topTabsWebViewPool,
                    onWebViewReady = { currentWebView = it },
                    onLoadingChanged = { loading ->
                        webLoadingByTab[selectedTab] = loading
                    }
                )

                if (webLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = DigikalaRed,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        if (isBannerOverlayVisible) {
            BannerWebViewOverlay(
                title = bannerOverlayTitle,
                url = bannerOverlayInitialUrl,
                onClose = closeBannerOverlay,
                onShare = {
                    val shareUrl = bannerOverlayCurrentUrl.ifBlank { bannerOverlayInitialUrl }
                    if (shareUrl.isBlank()) return@BannerWebViewOverlay
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareUrl)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                onWebViewReady = { bannerOverlayWebView = it },
                onCurrentUrlChanged = { bannerOverlayCurrentUrl = it }
            )
        }
    }
}

@Composable
private fun TopTabWebViewContainer(
    modifier: Modifier = Modifier,
    tabKey: String,
    url: String,
    pool: TopTabsWebViewPool,
    onWebViewReady: (WebView) -> Unit,
    onLoadingChanged: (Boolean) -> Unit
) {
    val normalizedUrl = remember(url) { normalizeWebUrl(url) }
    if (normalizedUrl.isBlank()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لینک این بخش در دسترس نیست",
                style = OpenKalaTypographyTokens.Subtitle,
                color = OpenKalaColorTokens.TextMedium
            )
        }
        return
    }

    key(tabKey) {
        AndroidView(
            modifier = modifier.testTag("home_top_tab_webview"),
            factory = {
                pool.acquire(
                    tabKey = tabKey,
                    normalizedUrl = normalizedUrl,
                    onLoadingChanged = onLoadingChanged
                ).also(onWebViewReady)
            },
            update = { webView ->
                onWebViewReady(webView)
                pool.bind(
                    tabKey = tabKey,
                    normalizedUrl = normalizedUrl,
                    onLoadingChanged = onLoadingChanged
                )
            }
        )
    }
}

@Composable
private fun BannerWebViewOverlay(
    title: String,
    url: String,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onWebViewReady: (WebView) -> Unit,
    onCurrentUrlChanged: (String) -> Unit
) {
    val normalizedUrl = remember(url) { normalizeWebUrl(url) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OpenKalaColorTokens.Surface)
            .testTag("banner_webview_overlay")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = OpenKalaColorTokens.TextHigh
                )
            }
            Text(
                text = title,
                style = OpenKalaTypographyTokens.SubtitleStrong,
                color = OpenKalaColorTokens.TextPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = OpenKalaColorTokens.TextHigh
                )
            }
        }

        if (normalizedUrl.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لینک این بخش در دسترس نیست",
                    style = OpenKalaTypographyTokens.Subtitle,
                    color = OpenKalaColorTokens.TextMedium
                )
            }
            return
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("banner_webview"),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false

                        override fun onPageFinished(view: WebView?, url: String?) {
                            onCurrentUrlChanged(url.orEmpty())
                        }
                    }
                    loadUrl(normalizedUrl)
                }
            },
            update = { webView ->
                onWebViewReady(webView)
                onCurrentUrlChanged(webView.url.orEmpty())
                if (webView.url != normalizedUrl) {
                    webView.loadUrl(normalizedUrl)
                }
            }
        )
    }
}

@Composable
internal fun MiddlePromoBannersSection(
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
                horizontal = styleSpec.middleBannersSectionHorizontalPadding,
                vertical = styleSpec.middleBannersSectionVerticalPadding
            )
            .testTag("home_middle_banners_section"),
        verticalArrangement = Arrangement.spacedBy(styleSpec.middleBannersGridGap)
    ) {
        visibleBanners.chunked(2).forEach { rowBanners ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(styleSpec.middleBannersGridGap)
            ) {
                rowBanners.forEach { banner ->
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(styleSpec.middleBannersCardAspectRatio)
                            .clip(RoundedCornerShape(styleSpec.middleBannersCardRadius))
                            .clickable { onBannerClick(banner) }
                            .testTag("home_middle_banner_card")
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
private fun HomeCategoriesSection(
    title: String,
    rows: Int,
    categories: List<HomeCategoryItem>,
    styleSpec: HomeStyleSpec
) {
    if (categories.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OpenKalaColorTokens.AppBackground)
            .padding(
                start = styleSpec.categorySectionHorizontalPadding,
                end = styleSpec.categorySectionHorizontalPadding,
                top = styleSpec.categorySectionTopPadding,
                bottom = styleSpec.categorySectionBottomPadding
            )
            .testTag("home_categories_section"),
        verticalArrangement = Arrangement.spacedBy(styleSpec.categoryGridRowGap)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val rowCount = rows.coerceAtLeast(1)
            val itemsPerRow = 5
            val pageSize = rowCount * itemsPerRow
            val pages = categories.chunked(pageSize)
            Text(
                text = title,
                style = OpenKalaTypographyTokens.H5,
                color = OpenKalaColorTokens.TextPrimary,
                fontWeight = FontWeight.W900,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        top = styleSpec.categoryTitleTopPadding,
                        bottom = styleSpec.categoryTitleBottomPadding
                    )
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_categories_carousel"),
                horizontalArrangement = Arrangement.spacedBy(styleSpec.categoryGridColumnGap)
            ) {
                items(pages) { pageItems ->
                    val categoryRows = pageItems.chunked(itemsPerRow)
                    Column(modifier = Modifier.fillParentMaxWidth()) {
                        categoryRows.forEachIndexed { rowIndex, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(styleSpec.categoryGridColumnGap)
                            ) {
                                rowItems.forEach { item ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(styleSpec.categoryImageSize)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                        ) {
                                            AsyncImage(
                                                model = item.imageUrl,
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .graphicsLayer(
                                                        scaleX = 1.28f,
                                                        scaleY = 1.28f
                                                    )
                                            )
                                        }
                                        Text(
                                            text = item.title,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            style = OpenKalaTypographyTokens.SubtitleStrong,
                                            color = OpenKalaColorTokens.TextPrimary,
                                            modifier = Modifier.padding(top = styleSpec.categoryLabelTopPadding)
                                        )
                                    }
                                }
                                repeat((itemsPerRow - rowItems.size).coerceAtLeast(0)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            if (rowIndex < categoryRows.lastIndex) {
                                Spacer(modifier = Modifier.height(styleSpec.categoryGridRowGap))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PooledTopTabWebView(
    val webView: WebView,
    var lastLoadedUrl: String,
    var lastUsedAt: Long,
    var isPageReady: Boolean,
    var onLoadingChanged: (Boolean) -> Unit
)

private class TopTabsWebViewPool(
    private val context: android.content.Context
) {
    private val entries = linkedMapOf<String, PooledTopTabWebView>()
    private val maxSize = 2

    fun preload(
        tabKey: String,
        normalizedUrl: String,
        onLoadingChanged: (Boolean) -> Unit
    ) {
        if (tabKey.isBlank() || normalizedUrl.isBlank()) return
        val entry = entries[tabKey] ?: createEntry(tabKey, onLoadingChanged).also {
            entries[tabKey] = it
        }
        entry.onLoadingChanged = onLoadingChanged
        entry.lastUsedAt = SystemClock.elapsedRealtime()
        if (entry.lastLoadedUrl != normalizedUrl || entry.webView.url.isNullOrBlank()) {
            entry.isPageReady = false
            entry.lastLoadedUrl = normalizedUrl
            entry.onLoadingChanged(true)
            entry.webView.loadUrl(normalizedUrl)
        }
        trimToSize(exceptKey = tabKey)
    }

    fun acquire(
        tabKey: String,
        normalizedUrl: String,
        onLoadingChanged: (Boolean) -> Unit
    ): WebView {
        val entry = entries[tabKey] ?: createEntry(tabKey, onLoadingChanged).also {
            entries[tabKey] = it
        }
        entry.onLoadingChanged = onLoadingChanged
        entry.lastUsedAt = SystemClock.elapsedRealtime()
        if (entry.lastLoadedUrl != normalizedUrl || entry.webView.url.isNullOrBlank()) {
            entry.isPageReady = false
            entry.lastLoadedUrl = normalizedUrl
            entry.onLoadingChanged(true)
            entry.webView.loadUrl(normalizedUrl)
        } else {
            entry.onLoadingChanged(!entry.isPageReady)
        }
        detachFromParent(entry.webView)
        trimToSize(exceptKey = tabKey)
        return entry.webView
    }

    fun bind(
        tabKey: String,
        normalizedUrl: String,
        onLoadingChanged: (Boolean) -> Unit
    ) {
        val entry = entries[tabKey] ?: return
        entry.onLoadingChanged = onLoadingChanged
        entry.lastUsedAt = SystemClock.elapsedRealtime()
        if (normalizedUrl.isNotBlank() && entry.lastLoadedUrl != normalizedUrl) {
            entry.isPageReady = false
            entry.lastLoadedUrl = normalizedUrl
            entry.onLoadingChanged(true)
            entry.webView.loadUrl(normalizedUrl)
        } else {
            entry.onLoadingChanged(!entry.isPageReady)
        }
    }

    fun destroyAll() {
        entries.values.forEach { pooled ->
            detachFromParent(pooled.webView)
            pooled.webView.destroy()
        }
        entries.clear()
    }

    private fun createEntry(
        tabKey: String,
        onLoadingChanged: (Boolean) -> Unit
    ): PooledTopTabWebView {
        val webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        val pooled = PooledTopTabWebView(
            webView = webView,
            lastLoadedUrl = "",
            lastUsedAt = SystemClock.elapsedRealtime(),
            isPageReady = false,
            onLoadingChanged = onLoadingChanged
        )
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                val entry = entries[tabKey] ?: pooled
                entry.isPageReady = false
                entry.onLoadingChanged(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val entry = entries[tabKey] ?: pooled
                entry.isPageReady = true
                entry.onLoadingChanged(false)
            }
        }
        return pooled
    }

    private fun trimToSize(exceptKey: String) {
        if (entries.size <= maxSize) return
        val candidate = entries
            .filterKeys { it != exceptKey }
            .minByOrNull { it.value.lastUsedAt }
            ?: return
        entries.remove(candidate.key)
        detachFromParent(candidate.value.webView)
        candidate.value.webView.destroy()
    }

    private fun detachFromParent(webView: WebView) {
        val parent = webView.parent as? ViewGroup ?: return
        parent.removeView(webView)
    }
}

private fun normalizeWebUrl(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return ""
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("/") -> "https://www.digikala.com$trimmed"
        else -> "https://$trimmed"
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
        SharedSearchBar(
            query = "",
            placeholder = "جستجو در همه کالاها",
            onQueryChange = {},
            readOnly = true,
            onClick = onSearchClick,
            modifier = Modifier.fillMaxWidth(),
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun ShortcutsRow(
    data: HomeScreenData,
    styleSpec: HomeStyleSpec,
    onShortcutClick: (ShortcutItem) -> Unit
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
                modifier = Modifier
                    .width(styleSpec.shortcutLabelWidth)
                    .clickable { onShortcutClick(item) }
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
    if (items.isEmpty()) return
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
            TimerBadge(
                totalSeconds = firstTimer,
                badgeHeight = styleSpec.timerBadgeHeight,
                badgeMinWidth = styleSpec.timerBadgeMinWidth
            )
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
                            text = product.price?.toToman()?.toPersianDigits().orEmpty(),
                            style = OpenKalaTypographyTokens.Body2Strong
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FreshIncredibleSection(
    title: String,
    items: List<IncredibleOfferItem>,
    styleSpec: HomeStyleSpec,
    pixelPerfectMode: Boolean,
    onProductClick: (IncredibleOfferItem) -> Unit
) {
    if (items.isEmpty()) return

    val firstTimer = if (pixelPerfectMode) 31736L else (items.firstOrNull()?.timerSeconds ?: 0L)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF6FBE44))
            .testTag("home_fresh_incredible_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(styleSpec.freshHeaderHeight)
                .padding(horizontal = styleSpec.freshHeaderHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable { /* UI-only in this iteration */ }
                    .testTag("home_fresh_see_all"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "همه",
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "‹",
                    style = OpenKalaTypographyTokens.H5,
                    color = OpenKalaColorTokens.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TimerBadge(
                totalSeconds = firstTimer,
                badgeHeight = styleSpec.freshTimerBadgeHeight,
                badgeMinWidth = styleSpec.freshTimerBadgeMinWidth,
                modifier = Modifier.testTag("home_fresh_timer")
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title.ifBlank { "شگفت‌انگیز سوپرمارکتی" },
                style = OpenKalaTypographyTokens.H5,
                color = OpenKalaColorTokens.White,
                fontWeight = FontWeight.W900,
                fontSize = styleSpec.freshHeaderTitleSizeSp.sp
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = styleSpec.freshSectionBottomPadding)
                .testTag("home_fresh_products_row"),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items, key = { it.id }) { product ->
                Column(
                    modifier = Modifier
                        .width(styleSpec.freshProductCardWidth)
                        .clip(OpenKalaRadiusTokens.Medium)
                        .background(OpenKalaColorTokens.Surface)
                        .clickable { onProductClick(product) }
                        .padding(8.dp)
                        .testTag("home_fresh_product_card"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier.size(styleSpec.freshProductImageSize),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = product.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = OpenKalaTypographyTokens.Body2,
                        color = OpenKalaColorTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    product.originalPrice?.let { original ->
                        Text(
                            text = original.toToman().toPersianDigits(),
                            style = OpenKalaTypographyTokens.Caption,
                            color = OpenKalaColorTokens.TextLow,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
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
                            text = product.price?.toToman()?.toPersianDigits().orEmpty(),
                            style = OpenKalaTypographyTokens.Body2Strong
                        )
                    }
                    Text(
                        text = "تومان",
                        style = OpenKalaTypographyTokens.Caption,
                        color = OpenKalaColorTokens.TextMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerBadge(
    totalSeconds: Long,
    badgeHeight: androidx.compose.ui.unit.Dp,
    badgeMinWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimePart(seconds.toString().padStart(2, '0').toPersianDigits(), badgeHeight, badgeMinWidth)
        TimePart(minutes.toString().padStart(2, '0').toPersianDigits(), badgeHeight, badgeMinWidth)
        TimePart(hours.toString().padStart(2, '0').toPersianDigits(), badgeHeight, badgeMinWidth)
    }
}

@Composable
private fun TimePart(
    value: String,
    badgeHeight: androidx.compose.ui.unit.Dp,
    badgeMinWidth: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .height(badgeHeight)
            .widthIn(min = badgeMinWidth)
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

private fun Long.toToman(): String {
    val tomanValue = this / 10
    val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
    return formatter.format(tomanValue)
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
