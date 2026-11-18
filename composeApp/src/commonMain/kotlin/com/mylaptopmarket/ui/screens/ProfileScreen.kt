package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.ui.components.StateField
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.util.ImagePicker
import com.mylaptopmarket.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onAccountDeleted: () -> Unit = {},
    onMainTabSelected: ((com.mylaptopmarket.ui.components.MainTab) -> Unit)? = null
) {
    val session by SessionManager.currentSession.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val accountDeleted by viewModel.accountDeleted.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val vendor = profile ?: session

    var shopName by remember { mutableStateOf(session?.shopName.orEmpty()) }
    var mobile by remember { mutableStateOf(session?.mobile.orEmpty()) }
    var location by remember { mutableStateOf(session?.location.orEmpty()) }
    var stateName by remember { mutableStateOf(session?.state.orEmpty()) }
    var email by remember { mutableStateOf(session?.email.orEmpty()) }
    var gstin by remember { mutableStateOf(session?.gstin.orEmpty()) }
    var shopPhoto by remember { mutableStateOf<ByteArray?>(null) }
    var selectedLogo by remember { mutableStateOf<ByteArray?>(null) }
    var removeLogo by remember { mutableStateOf(false) }
    var dirty by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deletePasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(accountDeleted) {
        if (accountDeleted) {
            showDeleteDialog = false
            onAccountDeleted()
        }
    }

    LaunchedEffect(profile) {
        val current = profile ?: return@LaunchedEffect
        if (!dirty) {
            shopName = current.shopName
            mobile = current.mobile
            location = current.location
            stateName = current.state
            email = current.email
            gstin = current.gstin
        }
    }

    LaunchedEffect(error, successMessage) {
        val message = successMessage ?: error
        if (successMessage != null) {
            selectedLogo = null
            removeLogo = false
            dirty = false
        }
        if (message != null) {
            scope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.consumeMessages()
        }
    }

    val currentLogoUrl = vendor?.logoUrl
    val showExistingLogo = !removeLogo && selectedLogo == null && isCustomShopLogo(currentLogoUrl)

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = 1.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "My Profile",
                            modifier = Modifier.tourTarget("page-profile"),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            }
        },
        bottomBar = {
            if (onMainTabSelected != null) {
                com.mylaptopmarket.ui.components.MainBottomBar(
                    selected = com.mylaptopmarket.ui.components.MainTab.Profile,
                    onTabSelected = onMainTabSelected
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedLogo != null -> {
                                AsyncImage(
                                    model = selectedLogo,
                                    contentDescription = "New shop logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            showExistingLogo -> {
                                AsyncImage(
                                    model = currentLogoUrl,
                                    contentDescription = "Shop logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Text(
                                    shopName.take(1).ifBlank { "S" }.uppercase(),
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        vendor?.shopName ?: "Your shop",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "@${vendor?.username ?: ""}",
                        fontSize = 13.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                    if (vendor?.shopTrusted == true) {
                        Spacer(Modifier.height(10.dp))
                        ShopVerifiedStamp()
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImagePicker(
                            selectionLimit = 1,
                            onImagesSelected = { bytes ->
                                selectedLogo = bytes.firstOrNull()
                                if (selectedLogo != null) {
                                    removeLogo = false
                                    dirty = true
                                }
                            }
                        ) { onClick ->
                            OutlinedButton(
                                onClick = onClick,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Change Logo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        if (showExistingLogo || selectedLogo != null) {
                            TextButton(onClick = {
                                selectedLogo = null
                                removeLogo = true
                                dirty = true
                            }) {
                                Text("Remove", color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        "This logo shows on your listings and in the menu.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            OutlinedTextField(
                value = shopName,
                onValueChange = {
                    shopName = it
                    dirty = true
                },
                label = { Text("Shop Name *") },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = vendor?.username.orEmpty(),
                onValueChange = {},
                enabled = false,
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it.filter { ch -> ch.isDigit() }.take(10)
                    dirty = true
                },
                label = { Text("Mobile") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    dirty = true
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    dirty = true
                },
                label = { Text("City") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            StateField(
                selected = stateName,
                onSelect = {
                    stateName = it
                    dirty = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = gstin,
                onValueChange = {
                    gstin = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(15)
                    dirty = true
                },
                label = { Text("GSTIN (optional)") },
                leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "We check this number on the GST registry. An active GSTIN plus a shop photo adds the verified stamp.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            val shownVendor = vendor
            if (shownVendor != null) {
                val registry = shownVendor.gstProfile
                if (registry != null && gstin.equals(shownVendor.gstin, ignoreCase = true)) {
                    GstDetailsCard(registry)
                }
            }
            ImagePicker(
                selectionLimit = 1,
                onImagesSelected = { bytes ->
                    shopPhoto = bytes.firstOrNull()
                    if (shopPhoto != null) dirty = true
                }
            ) { onClick ->
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (shopPhoto != null || !vendor?.shopPhotoUrl.isNullOrBlank()) "Change shop photo" else "Upload shop photo")
                }
            }
            val photoModel = shopPhoto ?: vendor?.shopPhotoUrl
            if (photoModel != null) {
                AsyncImage(
                    model = photoModel,
                    contentDescription = "Shop photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = {
                    viewModel.save(
                        shopName = shopName,
                        mobile = mobile,
                        location = location,
                        state = stateName,
                        email = email,
                        logo = selectedLogo,
                        removeLogo = removeLogo,
                        gstin = gstin,
                        shopPhoto = shopPhoto
                    )
                },
                enabled = !isLoading && shopName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SurfaceWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)

            Text(
                "Danger zone",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Text(
                "Permanently delete your account and associated listings, messages, and profile data. This cannot be undone.",
                color = TextMuted,
                fontSize = 12.sp
            )

            OutlinedButton(
                onClick = {
                    deletePassword = ""
                    showDeleteDialog = true
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFDC2626)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete Account", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    showDeleteDialog = false
                    deletePassword = ""
                }
            },
            title = { Text("Delete account permanently?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Your shop profile, products, requirements, and messages will be permanently deleted. This cannot be undone.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Confirm with password") },
                        singleLine = true,
                        visualTransformation = if (deletePasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { deletePasswordVisible = !deletePasswordVisible }) {
                                Icon(
                                    if (deletePasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(deletePassword)
                    },
                    enabled = !isLoading && deletePassword.isNotBlank(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Text(if (isLoading) "Deleting…" else "Delete permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deletePassword = ""
                    },
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun isCustomShopLogo(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    return !url.contains("mylaptoplogo", ignoreCase = true)
}
