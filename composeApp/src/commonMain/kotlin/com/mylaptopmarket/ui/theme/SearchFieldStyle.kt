package com.mylaptopmarket.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

internal fun searchFieldTextStyle(color: Color): TextStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 20.sp,
    color = color,
    fontWeight = FontWeight.Medium,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
).merge(searchFieldPlatformStyle())

internal expect fun searchFieldPlatformStyle(): TextStyle
