package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.viewmodel.AuthViewModel
import mylaptopmarket.composeapp.generated.resources.Res
import mylaptopmarket.composeapp.generated.resources.mylaptoplogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetDone: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val resetDone by viewModel.passwordResetDone.collectAsState()

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank() && !resetDone) otpSent = true
    }
    LaunchedEffect(resetDone) {
        if (resetDone) onResetDone()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(Res.drawable.mylaptoplogo),
                contentDescription = "Logo",
                modifier = Modifier.height(72.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Forgot Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "OTP will be sent to the email used at registration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.sendPasswordOtp(email) },
                enabled = !isLoading && email.isValidEmail(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (otpSent) "Resend OTP" else "Send OTP")
            }

            if (otpSent) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("OTP") },
                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New password (min 6)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.resetPassword(email, otp, password) },
                    enabled = !isLoading && email.isValidEmail() && otp.length == 6 && password.length >= 6,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    } else {
                        Text("Update password", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!infoMessage.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(infoMessage!!, color = PrimaryBlue)
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBackClick) { Text("Back to login", color = PrimaryBlue) }
        }
    }
}
