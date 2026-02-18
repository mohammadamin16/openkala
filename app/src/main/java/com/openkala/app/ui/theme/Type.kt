package com.openkala.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.openkala.app.R

val IRANYekan = FontFamily(
    Font(R.font.iranyekan_thin, FontWeight.W100),
    Font(R.font.iranyekan_light, FontWeight.W300),
    Font(R.font.iranyekan_regular, FontWeight.W400),
    Font(R.font.iranyekan_medium, FontWeight.W500),
    Font(R.font.iranyekan_bold, FontWeight.W700),
    Font(R.font.iranyekan_extrabold, FontWeight.W800),
    Font(R.font.iranyekan_black, FontWeight.W900),
    Font(R.font.iranyekan_extrablack, FontWeight.W900)
)

object OpenKalaTypographyTokens {
    // Mapped from Digikala text utility classes where 1rem ~= 10px.
    val H5 = TextStyle(fontFamily = IRANYekan, fontSize = 14.sp, lineHeight = 29.sp, fontWeight = FontWeight.W700)
    val Subtitle = TextStyle(fontFamily = IRANYekan, fontSize = 13.sp, lineHeight = 28.sp, fontWeight = FontWeight.W400)
    val SubtitleStrong = TextStyle(fontFamily = IRANYekan, fontSize = 13.sp, lineHeight = 28.sp, fontWeight = FontWeight.W700)
    val Body1 = TextStyle(fontFamily = IRANYekan, fontSize = 12.sp, lineHeight = 26.sp, fontWeight = FontWeight.W400)
    val Body1Strong = TextStyle(fontFamily = IRANYekan, fontSize = 12.sp, lineHeight = 26.sp, fontWeight = FontWeight.W700)
    val Body2 = TextStyle(fontFamily = IRANYekan, fontSize = 11.sp, lineHeight = 24.sp, fontWeight = FontWeight.W400)
    val Body2Strong = TextStyle(fontFamily = IRANYekan, fontSize = 11.sp, lineHeight = 24.sp, fontWeight = FontWeight.W700)
    val Caption = TextStyle(fontFamily = IRANYekan, fontSize = 10.sp, lineHeight = 22.sp, fontWeight = FontWeight.W400)
    val CaptionStrong = TextStyle(fontFamily = IRANYekan, fontSize = 10.sp, lineHeight = 22.sp, fontWeight = FontWeight.W700)
    val Button2 = TextStyle(fontFamily = IRANYekan, fontSize = 12.sp, lineHeight = 26.sp, fontWeight = FontWeight.W700)
}

val Typography = Typography(
    headlineSmall = OpenKalaTypographyTokens.H5,
    titleMedium = OpenKalaTypographyTokens.SubtitleStrong,
    bodyLarge = OpenKalaTypographyTokens.Body1,
    bodyMedium = OpenKalaTypographyTokens.Body2,
    bodySmall = OpenKalaTypographyTokens.Caption,
    labelLarge = OpenKalaTypographyTokens.Button2,
    labelMedium = OpenKalaTypographyTokens.Body2Strong
)
