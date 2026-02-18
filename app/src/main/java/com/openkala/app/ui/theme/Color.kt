package com.openkala.app.ui.theme

import androidx.compose.ui.graphics.Color

object OpenKalaColorTokens {
    // Core tokens from Digikala CSS root variables.
    val AppBackground = Color(0xFFF2F2F2)       // --color-app-background
    val Surface = Color(0xFFFFFFFF)             // --color-neutral-000
    val SurfaceMuted = Color(0xFFF0F0F1)        // --color-neutral-100
    val Border = Color(0xFFE0E0E2)              // --color-neutral-200
    val TextLow = Color(0xFF81858B)             // --color-neutral-500
    val TextMedium = Color(0xFF62666D)          // --color-neutral-600
    val TextHigh = Color(0xFF3F4064)            // --color-neutral-700
    val TextPrimary = Color(0xFF212121)         // --color-neutral-850
    val BrandPrimary = Color(0xFFE6123D)        // --color-brand-primary
    val Primary500 = Color(0xFFEF4056)          // --color-primary-500
    val Plus500 = Color(0xFFB12BA4)             // --color-plus-500
    val White = Color(0xFFFFFFFF)
}

val DigikalaRed = OpenKalaColorTokens.BrandPrimary
val LightGraySurface = OpenKalaColorTokens.AppBackground
val SearchBorder = OpenKalaColorTokens.Border
val PlusPurple = OpenKalaColorTokens.Plus500
val TextPrimary = OpenKalaColorTokens.TextPrimary
val TextSecondary = OpenKalaColorTokens.TextLow
