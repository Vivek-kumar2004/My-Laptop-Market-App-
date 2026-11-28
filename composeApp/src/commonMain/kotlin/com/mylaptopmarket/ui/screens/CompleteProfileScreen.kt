package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.ui.components.StateField
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.util.SHOW_VENDOR_OTP
import com.mylaptopmarket.viewmodel.AuthViewModel
import mylaptopmarket.composeapp.generated.resources.Res
import mylaptopmarket.composeapp.generated.resources.mylaptoplogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun CompleteProfileScreen(
    vendorId: Int,
    email: String,
    signupToken: String = "",
    viewModel: AuthViewModel,
    onCompleted: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var usernameTouched by remember { mutableStateOf(false) }
    var mobile by remember { mutableStateOf("") }
    var mobileOtp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val otpMobile by viewModel.registerOtpMobile.collectAsState()
    val verifiedMobile by viewModel.verifiedMobile.collectAsState()
    val pending by viewModel.pendingVerification.collectAsState()
    val suggestedUsername by viewModel.suggestedUsername.collectAsState()
    val usernameStatus by viewModel.usernameStatus.collectAsState()
    val gstStatus by viewModel.gstStatus.collectAsState()
    val mobileMatches = otpMobile != null && otpMobile == mobile
    val mobileVerified = verifiedMobile != null && verifiedMobile == mobile
    val gstOk = gstin.isBlank() || (gstStatus?.ok == true && gstStatus?.verified == true)

    LaunchedEffect(shopName) {
        viewModel.suggestUsernameFromShop(shopName)
    }
    LaunchedEffect(suggestedUsername, usernameTouched) {
        val suggestion = suggestedUsername
        if (!usernameTouched && !suggestion.isNullOrBlank()) {
            username = suggestion
        }
    }
    LaunchedEffect(username) {
        if (username.isNotBlank()) viewModel.checkUsername(username)
    }
    LaunchedEffect(gstin) {
        viewModel.checkGst(gstin)
    }
    LaunchedEffect(pending) {
        val id = pending?.vendorId
        if (id != null && id > 0) {
            onCompleted(id)
            viewModel.clearPending()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.mylaptoplogo),
                contentDescription = "Logo",
                modifier = Modifier.height(72.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Finish your dealer profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (email.isNotBlank()) {
                    "Signed in as $email. Shop name, username, city, state and mobile are required."
                } else {
                    "Shop name, username, city, state and mobile are required."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                value = username,
                onValueChange = {
                    usernameTouched = true
                    username = it.trim()
                },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    val status = usernameStatus
                    if (status != null && username.isNotBlank()) {
                        Text(
                            status.message,
                            color = if (status.available) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            if (usernameStatus?.available == false && !usernameStatus?.suggestion.isNullOrBlank() &&
                usernameStatus?.suggestion != username
            ) {
                TextButton(onClick = {
                    usernameTouched = true
                    username = usernameStatus?.suggestion.orEmpty()
                }) {
                    Text("Use ${usernameStatus?.suggestion}", color = PrimaryBlue)
                }
            }

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
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (optional, min 6)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(15) },
                label = { Text("GSTIN (optional)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    val status = gstStatus
                    if (status != null && !status.empty && status.message.isNotBlank()) {
                        Text(
                            status.message,
                            color = if (status.ok && status.verified) PrimaryBlue else MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            if (gstStatus?.verified == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryLight, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = PrimaryBlue)
                    Text(
                        "GST verified — builds dealer trust.",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!infoMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(infoMessage!!, color = PrimaryBlue, style = MaterialTheme.typography.bodyMedium)
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
            val canSubmit = (vendorId > 0 || signupToken.isNotBlank()) &&
                shopName.isNotBlank() &&
                username.length >= 4 &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                mobile.length == 10 &&
                (!SHOW_VENDOR_OTP || mobileVerified) &&
                (password.isEmpty() || password.length >= 6) &&
                gstOk &&
                !isLoading

            Button(
                onClick = {
                    viewModel.completeProfile(
                        vendorId = vendorId,
                        signupToken = signupToken,
                        shopName = shopName,
                        username = username,
                        city = city,
                        state = state,
                        mobile = mobile,
                        mobileOtp = mobileOtp,
                        password = password,
                        gstin = gstin
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = canSubmit,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save and continue", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onBackClick) {
                Text("Back")
            }
        }
    }
}
