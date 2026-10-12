package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.viewmodel.AdminVendorFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVendorFormScreen(
    vendorId: Int?,
    viewModel: AdminVendorFormViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val vendor by viewModel.vendor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saved by viewModel.saved.collectAsState()

    var shopName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var stateCode by remember { mutableStateOf("") }
    var billingAddress by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var filled by remember { mutableStateOf(false) }

    LaunchedEffect(vendorId) {
        if (vendorId != null) viewModel.load(vendorId)
    }

    LaunchedEffect(vendor) {
        val current = vendor ?: return@LaunchedEffect
        if (filled) return@LaunchedEffect
        shopName = current.shopName.orEmpty()
        username = current.username
        mobile = current.mobile.orEmpty()
        location = current.location.orEmpty()
        gstin = current.gstin.orEmpty()
        state = current.state.orEmpty()
        stateCode = current.stateCode.orEmpty()
        billingAddress = current.billingAddress.orEmpty()
        contactPerson = current.contactPerson.orEmpty()
        filled = true
    }

    LaunchedEffect(saved) {
        if (saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vendorId == null) "Add Vendor" else "Edit Vendor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AdminField("Shop name", shopName) { shopName = it }
            AdminField("Username", username) { username = it }
            AdminField("Mobile", mobile) { mobile = it }
            AdminField(
                if (vendorId == null) "Password" else "Password (leave blank to keep)",
                password
            ) { password = it }
            AdminField("Location", location) { location = it }
            AdminField("GSTIN", gstin) { gstin = it }
            AdminField("Contact person", contactPerson) { contactPerson = it }
            AdminField("State", state) { state = it }
            AdminField("State code", stateCode) { stateCode = it }
            AdminField("Billing address", billingAddress, singleLine = false) { billingAddress = it }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.save(
                        id = vendorId,
                        shopName = shopName,
                        username = username,
                        mobile = mobile,
                        password = password,
                        location = location,
                        gstin = gstin,
                        state = state,
                        stateCode = stateCode,
                        billingAddress = billingAddress,
                        contactPerson = contactPerson
                    )
                },
                enabled = !isLoading && shopName.isNotBlank() && username.isNotBlank() && mobile.isNotBlank() &&
                    (vendorId != null || password.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator()
                else Text(if (vendorId == null) "Save Vendor" else "Update Vendor", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(12.dp)
    )
}
