package com.mylaptopmarket.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun ImagePicker(
    selectionLimit: Int,
    onImagesSelected: (List<ByteArray>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        scope.launch {
            val byteArrays = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
            }
            onImagesSelected(byteArrays.take(if (selectionLimit > 0) selectionLimit else byteArrays.size))
        }
    }

    content {
        launcher.launch("image/*")
    }
}

actual fun hideKeyboard() = Unit