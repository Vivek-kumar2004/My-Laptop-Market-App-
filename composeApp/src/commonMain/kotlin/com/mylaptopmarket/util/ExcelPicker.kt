package com.mylaptopmarket.util

import androidx.compose.runtime.Composable

@Composable
expect fun ExcelPicker(
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
)
