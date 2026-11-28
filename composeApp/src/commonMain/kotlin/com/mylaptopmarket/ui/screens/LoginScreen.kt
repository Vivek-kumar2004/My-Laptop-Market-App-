package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.util.openUrl
import com.mylaptopmarket.viewmodel.AuthViewModel
import com.mylaptopmarket.viewmodel.SignInNotice
import mylaptopmarket.composeapp.generated.resources.Res
import mylaptopmarket.composeapp.generated.resources.mylaptoplogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onPendingVerification: (Int) -> Unit,
    onNeedsProfile: (vendorId: Int, email: String, signupToken: String) -> Unit,
    onBackClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val signInNotice by SignInNotice.message.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val pending by viewModel.pendingVerification.collectAsState()
    val needsProfile by viewModel.needsProfile.collectAsState()

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) onLoginSuccess()
    }

    LaunchedEffect(pending) {
        val vendorId = pending?.vendorId
        if (vendorId != null && vendorId > 0) {
            onPendingVerification(vendorId)
            viewModel.clearPending()
        }
    }

    LaunchedEffect(needsProfile) {
        val pendingProfile = needsProfile
        if (pendingProfile != null && (pendingProfile.vendorId > 0 || pendingProfile.signupToken.isNotBlank())) {
            onNeedsProfile(pendingProfile.vendorId, pendingProfile.email, pendingProfile.signupToken)
            viewModel.clearNeedsProfile()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.mylaptoplogo),
                contentDescription = "Logo",
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Vendor Login",
                modifier = Modifier.tourTarget("page-login"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username, email or mobile") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            val notice = signInNotice ?: errorMessage
            if (notice != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = notice,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    SignInNotice.clear()
                    openUrl("https://mylaptopmarket-app.firebaseapp.com/google-auth.html")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue with Google", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot password?", color = PrimaryBlue)
            }

            TextButton(onClick = onRegisterClick) {
                Text("Don't have an account? Register here", color = PrimaryBlue)
            }

            TextButton(onClick = onBackClick) {
                Text("Back to Home")
            }
        }
    }
}
