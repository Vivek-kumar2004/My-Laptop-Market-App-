package com.mylaptopmarket.util

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun ExcelPicker(
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val picked = withContext(Dispatchers.IO) {
                val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                } ?: "stock.xlsx"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) null else name to bytes
            }
            if (picked != null) onFileSelected(picked.first, picked.second)
        }
    }

    content {
        launcher.launch(
            arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
            )
        )
    }
}
