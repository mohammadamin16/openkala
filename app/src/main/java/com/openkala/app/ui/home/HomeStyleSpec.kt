package com.openkala.app.ui.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PixelPerfectMode {
    const val Enabled: Boolean = false
}

data class HomeStyleSpec(
    val topStripHeight: Dp = 68.dp,
    val tabCardWidth: Dp = 90.dp,
    val tabCardHeight: Dp = 84.dp,
    val tabIconSize: Dp = 38.dp,
    val tabRowHorizontalPadding: Dp = 10.dp,
    val tabRowTopPadding: Dp = 8.dp,

    val searchSectionHorizontalPadding: Dp = 10.dp,
    val searchSectionVerticalPadding: Dp = 8.dp,
    val notificationBubbleSize: Dp = 52.dp,
    val searchBarHeight: Dp = 52.dp,
    val searchIconSize: Dp = 28.dp,
    val searchFontSizeSp: Int = 16,

    val locationTextSizeSp: Int = 17,
    val plusBarVerticalPadding: Dp = 10.dp,
    val plusBarHorizontalPadding: Dp = 14.dp,
    val plusButtonHeight: Dp = 34.dp,
    val plusTitleSizeSp: Int = 18,

    val heroSectionHeight: Dp = 140.dp,
    val heroCardRadius: Dp = 16.dp,
    val heroHorizontalPadding: Dp = 14.dp,
    val heroCardAspectRatio: Float = 2f,
    val heroCarouselHorizontalPadding: Dp = 18.dp,
    val heroCarouselPageSpacing: Dp = 10.dp,

    val shortcutIconSize: Dp = 54.dp,
    val shortcutLabelWidth: Dp = 80.dp,
    val shortcutRowHorizontalPadding: Dp = 18.dp,
    val shortcutRowVerticalPadding: Dp = 8.dp,

    val incredibleHeaderTextSizeSp: Int = 34,
    val timerBadgeHeight: Dp = 42.dp,
    val timerBadgeMinWidth: Dp = 36.dp,
    val productCardWidth: Dp = 160.dp,
    val productImageSize: Dp = 120.dp,
    val freshHeaderHeight: Dp = 74.dp,
    val freshHeaderHorizontalPadding: Dp = 14.dp,
    val freshSectionBottomPadding: Dp = 10.dp,
    val freshHeaderTitleSizeSp: Int = 28,
    val freshTimerBadgeHeight: Dp = 38.dp,
    val freshTimerBadgeMinWidth: Dp = 34.dp,
    val freshProductCardWidth: Dp = 152.dp,
    val freshProductImageSize: Dp = 108.dp,
    val topBannersSectionHorizontalPadding: Dp = 12.dp,
    val topBannersSectionVerticalPadding: Dp = 12.dp,
    val topBannersCardAspectRatio: Float = 4f / 3f,
    val topBannersCardRadius: Dp = 12.dp,
    val topBannersGridGap: Dp = 8.dp,
    val middleBannersSectionHorizontalPadding: Dp = 12.dp,
    val middleBannersSectionVerticalPadding: Dp = 8.dp,
    val middleBannersCardAspectRatio: Float = 4f / 3f,
    val middleBannersCardRadius: Dp = 12.dp,
    val middleBannersGridGap: Dp = 8.dp,

    val bottomNavTopPadding: Dp = 6.dp,
    val bottomNavBottomPadding: Dp = 14.dp,
    val bottomNavIconSize: Dp = 30.dp,
    val bottomNavTextSizeSp: Int = 15
)

val PixelPerfectHomeStyle = HomeStyleSpec()

val HomeStyleSpec.searchFontSize get() = searchFontSizeSp.sp
val HomeStyleSpec.locationTextSize get() = locationTextSizeSp.sp
val HomeStyleSpec.plusTitleSize get() = plusTitleSizeSp.sp
val HomeStyleSpec.incredibleHeaderTextSize get() = incredibleHeaderTextSizeSp.sp
val HomeStyleSpec.bottomNavTextSize get() = bottomNavTextSizeSp.sp
