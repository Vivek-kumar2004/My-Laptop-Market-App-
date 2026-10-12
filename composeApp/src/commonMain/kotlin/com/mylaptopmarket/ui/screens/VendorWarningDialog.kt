package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ApiAuthRepository
import com.mylaptopmarket.ui.theme.ErrorRed
import kotlinx.coroutines.launch

@Composable
fun VendorWarningDialog(
    vendorId: Int,
    message: String,
    repository: ApiAuthRepository
) {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                enabled = !sending,
                onClick = {
                    sending = true
                    error = null
                    scope.launch {
                        repository.acknowledgeWarning(vendorId)
                            .onSuccess {
                                SessionManager.update { current -> current.copy(adminWarning = "") }
                            }
                            .onFailure {
                                error = it.message ?: "Could not acknowledge the warning"
                                sending = false
                            }
                    }
                }
            ) { Text("I understand") }
        },
        title = { Text("Warning from My Laptop Market", fontWeight = FontWeight.Bold, color = ErrorRed) },
        text = {
            Column {
                Text(message)
                if (!error.isNullOrBlank()) {
                    Text(error!!, color = ErrorRed)
                }
            }
        }
    )
}
