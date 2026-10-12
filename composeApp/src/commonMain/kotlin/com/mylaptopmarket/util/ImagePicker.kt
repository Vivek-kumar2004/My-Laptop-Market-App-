package com.mylaptopmarket.util

import androidx.compose.runtime.Composable

@Composable
expect fun ImagePicker(
    selectionLimit: Int = 0,
    onImagesSelected: (List<ByteArray>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
)

expect fun hideKeyboard()
