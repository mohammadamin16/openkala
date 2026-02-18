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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.openkala.app.ui.theme.LightGraySurface
import com.openkala.app.ui.theme.PlusPurple
import com.openkala.app.ui.theme.SearchBorder
import com.openkala.app.ui.theme.TextPrimary
import com.openkala.app.ui.theme.TextSecondary

@Composable
fun HomeScreenRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (state) {
        HomeUiState.Loading -> LoadingHomeScreen()
        is HomeUiState.Error -> ErrorHomeScreen(
            message = (state as HomeUiState.Error).message,
            onRetry = viewModel::refresh
        )
        is HomeUiState.Content -> HomeScreen(
            data = (state as HomeUiState.Content).data,
            isRefreshing = (state as HomeUiState.Content).isRefreshing
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
        Text(text = "خطا در دریافت اطلاعات")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("تلاش مجدد")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    data: HomeScreenData,
    isRefreshing: Boolean
) {
    var selectedTab by remember(data.selectedTabName) {
        mutableStateOf(data.selectedTabName)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = { HomeBottomBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGraySurface)
                .padding(padding)
                .testTag("home_list"),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            item {
                data.topStripBanner?.let {
                    AsyncImage(
                        model = it.imageUrl,
                        contentDescription = it.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(Color(0xFFB39E71))
                    )
                }
            }

            item {
                TopTabsRow(
                    tabs = data.superAppTabs,
                    selectedTab = selectedTab,
                    onTabClick = { tab ->
                        if (tab.name == "digikala") {
                            selectedTab = tab.name
                        }
                    }
                )
            }

            item {
                SearchAndLocationSection()
            }

            item {
                PlusMockBanner()
            }

            item {
                val pagerState = rememberPagerState(pageCount = { data.heroBanners.size.coerceAtLeast(1) })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(Color.White)
                        .padding(vertical = 10.dp)
                ) { page ->
                    val banner = data.heroBanners.getOrNull(page)
                    if (banner != null) {
                        AsyncImage(
                            model = banner.imageUrl,
                            contentDescription = banner.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            item {
                ShortcutsRow(data = data)
            }

            item {
                IncredibleSection(data.incredibleOffers.items)
            }

            if (isRefreshing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "در حال به‌روزرسانی...",
                            color = TextSecondary,
                            fontSize = 12.sp
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
    onTabClick: (SuperAppTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDEDED))
            .padding(top = 10.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        items(tabs, key = { it.name }) { tab ->
            val selected = tab.name == selectedTab
            Column(
                modifier = Modifier
                    .size(width = 90.dp, height = 92.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) colorFromHex(tab.backgroundColorHex) else Color(0xFFF6F6F6)
                    )
                    .border(1.dp, Color(0xFFE2E2E2), RoundedCornerShape(14.dp))
                    .clickable { onTabClick(tab) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AsyncImage(
                    model = tab.iconUrl,
                    contentDescription = tab.title,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = tab.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) colorFromHex(tab.focusedTextColorHex) else TextPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SearchAndLocationSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDEDED))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F4F4))
                    .border(1.dp, SearchBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notification",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF4F4F4))
                    .border(1.dp, SearchBorder, RoundedCornerShape(28.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "جستجو در ",
                    color = Color(0xFF9E9E9E),
                    fontSize = 16.sp
                )
                Text(
                    text = "دیجی‌کالا",
                    color = DigikalaRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "تحویل به استان تهران، شهر تهران",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PlusMockBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PlusPurple)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✕",
            color = Color.White,
            fontSize = 20.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "تمدید",
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                color = PlusPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "اشتراک پلاس شما تمام شده",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun ShortcutsRow(data: HomeScreenData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        data.shortcuts.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(90.dp)
            ) {
                AsyncImage(
                    model = item.iconUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF3A3A62),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun IncredibleSection(items: List<IncredibleOfferItem>) {
    val firstTimer = items.firstOrNull()?.timerSeconds ?: 0L
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
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            TimerBadge(firstTimer)
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
                        .width(160.dp)
                        .background(Color.White)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier.size(120.dp)
                    )
                    Text(
                        text = product.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        product.discountPercent?.let {
                            Text(
                                text = "$it%",
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DigikalaRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = product.price?.toString().orEmpty(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerBadge(totalSeconds: Long) {
    val hours = (totalSeconds / 3600).coerceAtLeast(0)
    val minutes = ((totalSeconds % 3600) / 60).coerceAtLeast(0)
    val seconds = (totalSeconds % 60).coerceAtLeast(0)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TimePart(seconds.toString().padStart(2, '0'))
        TimePart(minutes.toString().padStart(2, '0'))
        TimePart(hours.toString().padStart(2, '0'))
    }
}

@Composable
private fun TimePart(value: String) {
    Text(
        text = value,
        color = Color(0xFF2D2D56),
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

@Composable
private fun HomeBottomBar() {
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
            .background(Color.White)
            .padding(top = 6.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = item.second,
                    contentDescription = item.first,
                    tint = if (item.third) TextPrimary else Color(0xFF9EA0A8),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = item.first,
                    color = if (item.third) TextPrimary else Color(0xFF7A7D86),
                    fontSize = 15.sp
                )
            }
        }
    }
}

private fun colorFromHex(value: String): Color {
    return runCatching { Color(parseColor(value)) }.getOrDefault(Color.White)
}
