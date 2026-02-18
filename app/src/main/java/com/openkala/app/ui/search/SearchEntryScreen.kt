package com.openkala.app.ui.search

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openkala.app.domain.model.SearchSuggestionItem
import com.openkala.app.domain.model.SearchTrendItem
import com.openkala.app.ui.theme.OpenKalaColorTokens
import com.openkala.app.ui.theme.OpenKalaRadiusTokens
import com.openkala.app.ui.theme.OpenKalaTypographyTokens
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchEntryScreenRoute(
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: SearchEntryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchEntryScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onTrendClick = viewModel::onTrendClick,
        onRetryTrends = viewModel::retryTrends,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SearchEntryScreen(
    state: SearchEntryUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTrendClick: (String) -> Unit,
    onRetryTrends: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        // Let shared-element transition settle, then focus and open keyboard.
        delay(260)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        containerColor = OpenKalaColorTokens.AppBackground,
        contentColor = OpenKalaColorTokens.TextPrimary,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(OpenKalaColorTokens.AppBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = OpenKalaColorTokens.TextHigh,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable(onClick = onBack)
                    )

                    SharedSearchBar(
                        query = state.query,
                        placeholder = "جستجو در همه کالاها",
                        onQueryChange = onQueryChange,
                        readOnly = false,
                        modifier = Modifier.weight(1f),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        focusRequester = focusRequester
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = null,
                        tint = OpenKalaColorTokens.TextLow,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "جستجوهای پرطرفدار",
                        style = OpenKalaTypographyTokens.H5,
                        color = OpenKalaColorTokens.TextHigh
                    )
                }
            }

            item {
                if (state.isLoadingTrends) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = OpenKalaColorTokens.BrandPrimary,
                            modifier = Modifier.size(26.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    HotTrendsRow(
                        trends = state.trends,
                        onTrendClick = onTrendClick
                    )
                }
            }

            if (state.errorMessage != null && state.trends.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "تلاش مجدد",
                            style = OpenKalaTypographyTokens.SubtitleStrong,
                            color = OpenKalaColorTokens.BrandPrimary,
                            modifier = Modifier.clickable(onClick = onRetryTrends)
                        )
                    }
                }
            }

            if (state.query.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.isLoadingSuggestions) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = OpenKalaColorTokens.BrandPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                } else {
                    items(state.suggestions, key = { it.keyword }) { suggestion ->
                        SuggestionRow(
                            suggestion = suggestion,
                            onClick = { onQueryChange(suggestion.keyword) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HotTrendsRow(
    trends: List<SearchTrendItem>,
    onTrendClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        items(trends, key = { it.keyword }) { trend ->
            Row(
                modifier = Modifier
                    .clip(OpenKalaRadiusTokens.Pill)
                    .border(1.dp, OpenKalaColorTokens.Border, OpenKalaRadiusTokens.Pill)
                    .background(OpenKalaColorTokens.Surface)
                    .clickable { onTrendClick(trend.keyword) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = null,
                    tint = OpenKalaColorTokens.TextLow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = trend.keyword,
                    style = OpenKalaTypographyTokens.SubtitleStrong,
                    color = OpenKalaColorTokens.TextHigh,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: SearchSuggestionItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.ChevronLeft,
            contentDescription = null,
            tint = OpenKalaColorTokens.TextLow,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = suggestion.keyword,
            style = OpenKalaTypographyTokens.SubtitleStrong,
            color = OpenKalaColorTokens.TextHigh,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
