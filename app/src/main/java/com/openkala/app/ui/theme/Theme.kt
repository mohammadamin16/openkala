package com.openkala.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = OpenKalaColorTokens.Primary500,
    onPrimary = OpenKalaColorTokens.White,
    background = OpenKalaColorTokens.AppBackground,
    onBackground = OpenKalaColorTokens.TextPrimary,
    surface = OpenKalaColorTokens.Surface,
    onSurface = OpenKalaColorTokens.TextPrimary,
    outline = OpenKalaColorTokens.Border
)

@Composable
fun OpenKalaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
