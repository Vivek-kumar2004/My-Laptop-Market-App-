package com.mylaptopmarket.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

@Suppress("DEPRECATION")
internal actual fun searchFieldPlatformStyle(): TextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)
