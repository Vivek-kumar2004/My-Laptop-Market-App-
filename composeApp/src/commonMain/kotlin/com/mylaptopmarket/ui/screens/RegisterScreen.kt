package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.ui.components.StateField
import com.mylaptopmarket.util.SHOW_VENDOR_OTP
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.viewmodel.AuthViewModel
import mylaptopmarket.composeapp.generated.resources.Res
import mylaptopmarket.composeapp.generated.resources.mylaptoplogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistered: (Int) -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var mobileOtp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var emailOtp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val otpEmail by viewModel.registerOtpEmail.collectAsState()
    val verifiedEmail by viewModel.verifiedEmail.collectAsState()
    val verifiedOtp by viewModel.verifiedOtp.collectAsState()
    val otpMobile by viewModel.registerOtpMobile.collectAsState()
    val verifiedMobile by viewModel.verifiedMobile.collectAsState()
    val pending by viewModel.pendingVerification.collectAsState()
    val otpMatchesEmail = otpEmail != null && otpEmail.equals(email.trim(), ignoreCase = true)
    val emailVerified = verifiedEmail != null &&
        verifiedEmail.equals(email.trim(), ignoreCase = true) &&
        verifiedOtp == emailOtp
    val mobileMatches = otpMobile != null && otpMobile == mobile
    val mobileVerified = verifiedMobile != null && verifiedMobile == mobile

    LaunchedEffect(pending) {
        val vendorId = pending?.vendorId
        if (vendorId != null && vendorId > 0) {
            onRegistered(vendorId)
            viewModel.clearPending()
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
                modifier = Modifier.height(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Vendor Register",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (SHOW_VENDOR_OTP) {
                Text(
                    "We send a 6-digit OTP to your email and mobile so both have to be real.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop Name") },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it.filter { ch -> ch.isDigit() }.take(10) },
                label = { Text("Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            if (SHOW_VENDOR_OTP) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = mobileOtp,
                    onValueChange = { mobileOtp = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("Mobile OTP") },
                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.sendRegisterMobileOtp(mobile) },
                        enabled = !isLoading && mobile.length == 10,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (mobileMatches) "Resend SMS" else "Send SMS")
                    }
                    Button(
                        onClick = { viewModel.verifyRegisterMobileOtp(mobile, mobileOtp) },
                        enabled = !isLoading && mobileMatches && mobileOtp.length == 6 && !mobileVerified,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (mobileVerified) "Verified" else "Verify")
                    }
                }
                if (otpMobile != null && !mobileMatches) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Mobile number changed. Send a new OTP.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            StateField(
                selected = state,
                onSelect = { state = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
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
            if (SHOW_VENDOR_OTP) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = emailOtp,
                    onValueChange = { emailOtp = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("Email OTP") },
                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.sendRegisterOtp(email) },
                        enabled = !isLoading && email.isValidEmail(),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (otpMatchesEmail) "Resend OTP" else "Send OTP")
                    }
                    Button(
                        onClick = { viewModel.verifyRegisterOtp(email, emailOtp) },
                        enabled = !isLoading && email.isValidEmail() && emailOtp.length == 6 && !emailVerified,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (emailVerified) "Verified" else "Verify")
                    }
                }
                if (otpEmail != null && !otpMatchesEmail) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Email changed. Send a new OTP to this address.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (!infoMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = infoMessage!!,
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val canSubmit = shopName.isNotBlank() &&
                username.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                mobile.length == 10 &&
                email.isValidEmail() &&
                (!SHOW_VENDOR_OTP || (emailVerified && mobileVerified)) &&
                password.length >= 6 &&
                !isLoading

            Button(
                onClick = {
                    viewModel.register(shopName, username, city, state, mobile, email, password, emailOtp, mobileOtp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onLoginClick) {
                Text("Already have account? Login", color = PrimaryBlue)
            }
            TextButton(onClick = onBackClick) {
                Text("Back to Home")
            }
        }
    }
}

internal fun String.isValidEmail(): Boolean {
    val parts = lowercase().split("@")
    return parts.size == 2 &&
        parts[0].isNotBlank() &&
        parts[1].contains(".") &&
        !contains(" ")
}
