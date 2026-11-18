package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.util.ImagePicker
import com.mylaptopmarket.util.SHOW_VENDOR_PAYMENT
import com.mylaptopmarket.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerVerificationScreen(
    vendorId: Int?,
    viewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val session by SessionManager.currentSession.collectAsState()
    val status by viewModel.sellerStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()
    val uriHandler = LocalUriHandler.current

    val resolvedId = vendorId ?: session?.id

    LaunchedEffect(resolvedId) {
        if (resolvedId != null) {
            viewModel.loadSellerStatus(resolvedId)
        }
    }

    val seller = status ?: session
    val shopVerified = seller?.shopTrusted == true
    val gstDone = seller?.gstVerified == true
    val photoDone = !seller?.shopPhotoUrl.isNullOrBlank()
    var gstin by remember(seller?.id) { mutableStateOf(seller?.gstin.orEmpty()) }
    var shopPhoto by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(seller?.gstin) {
        gstin = seller?.gstin.orEmpty()
    }
    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) shopPhoto = null
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Seller Verification",
                        modifier = Modifier.tourTarget("page-verify"),
                        fontWeight = FontWeight.Bold,
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (shopVerified) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = if (shopVerified) SuccessGreen else WarningOrange,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (shopVerified) "You're a verified seller" else "Verification pending",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Verified tab hota hai jab active GSTIN aur shop photo dono save ho jayein.",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            if (shopVerified) {
                Spacer(Modifier.height(12.dp))
                ShopVerifiedStamp()
            }

            if (seller != null) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RequirementRow("Active GSTIN", gstDone)
                        RequirementRow("Shop photo", photoDone)
                    }
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(15) },
                    label = { Text("GSTIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                val registry = seller.gstProfile?.takeIf { gstin.equals(seller.gstin, ignoreCase = true) }
                if (registry != null) {
                    Spacer(Modifier.height(12.dp))
                    GstDetailsCard(registry)
                }
                Spacer(Modifier.height(12.dp))
                ImagePicker(selectionLimit = 1, onImagesSelected = { shopPhoto = it.firstOrNull() }) { onClick ->
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (shopPhoto != null || photoDone) "Change shop photo" else "Upload shop photo")
                    }
                }
                val preview = shopPhoto ?: seller.shopPhotoUrl
                if (preview != null) {
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = preview,
                        contentDescription = "Shop photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.saveShopVerification(gstin, shopPhoto) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save GST and shop photo", fontWeight = FontWeight.Bold)
                }
            } else if (resolvedId == null) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Login or register first to check seller verification.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage!!, color = ErrorRed, textAlign = TextAlign.Center)
            } else if (!infoMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(infoMessage!!, color = SuccessGreen, textAlign = TextAlign.Center)
            }

            if (resolvedId != null) {
                if (SHOW_VENDOR_PAYMENT) {
                    Spacer(Modifier.height(28.dp))
                    Text(
                        "Subscription",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (seller?.isVerified == true) {
                            "Subscription active${seller.subscriptionEnd?.let { " till $it" } ?: ""}${if (seller.daysLeft > 0) " · ${seller.daysLeft} days left" else ""}."
                        } else {
                            "Products live karne ke liye ₹1 / 30 din ki subscription alag se chahiye."
                        },
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { uriHandler.openUri(viewModel.subscriptionUrl(resolvedId)) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seller?.isVerified == true) PrimaryBlue else WarningOrange
                        )
                    ) {
                        Text(
                            if (seller?.isVerified == true) "Renew Subscription" else "Pay / Activate Subscription",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Spacer(Modifier.height(28.dp))
                }
                OutlinedButton(
                    onClick = { viewModel.loadSellerStatus(resolvedId) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Refresh Status")
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementRow(label: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(
            if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) SuccessGreen else WarningOrange,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(
            if (done) "Done" else "Required",
            color = if (done) SuccessGreen else WarningOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
