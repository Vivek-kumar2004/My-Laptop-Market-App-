package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.mylaptopmarket.network.GstProfile
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.network.VendorSession
import com.mylaptopmarket.repository.ApiAuthRepository
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.util.ImagePicker
import kotlinx.coroutines.launch

private val Gold = Color(0xFFE8B931)
private val Ink = Color(0xFF0B1220)

@Composable
fun ShopVerifiedStamp(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFFFFF7D6),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Verified", color = Color(0xFF92400E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ShopTrustPopup(
    session: VendorSession,
    repository: ApiAuthRepository,
    onDismiss: () -> Unit
) {
    var gstin by remember(session.id) { mutableStateOf(session.gstin) }
    var photo by remember(session.id) { mutableStateOf<ByteArray?>(null) }
    var current by remember(session.id) { mutableStateOf(session) }
    var error by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceWhite
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF123A73), Ink))
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Gold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Ink, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shop trust", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                Text(
                                    if (current.shopTrusted) "Your shop carries the verified stamp."
                                    else "Optional. An active GSTIN and a shop photo unlock the stamp.",
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                        if (current.shopTrusted) {
                            Spacer(Modifier.height(14.dp))
                            ShopVerifiedStamp()
                        }
                    }
                }
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(15) },
                        label = { Text("GSTIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    val registry = current.gstProfile?.takeIf { gstin.equals(current.gstin, ignoreCase = true) }
                    if (registry != null) {
                        GstDetailsCard(registry)
                    }
                    ImagePicker(selectionLimit = 1, onImagesSelected = { photo = it.firstOrNull() }) { onClick ->
                        Button(
                            onClick = onClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (photo != null || !current.shopPhotoUrl.isNullOrBlank()) "Change shop photo" else "Upload shop photo")
                        }
                    }
                    val preview = photo ?: current.shopPhotoUrl
                    if (preview != null) {
                        AsyncImage(
                            model = preview,
                            contentDescription = "Shop photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (!error.isNullOrBlank()) {
                        Text(error!!, color = Color(0xFFB91C1C), fontSize = 13.sp)
                    } else if (!note.isNullOrBlank()) {
                        Text(note!!, color = Color(0xFF166534), fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                saving = true
                                error = null
                                note = null
                                repository.saveShopTrust(current.id, gstin, photo)
                                    .onSuccess { saved ->
                                        val merged = saved.session.copy(daysLeft = current.daysLeft)
                                        current = merged
                                        gstin = merged.gstin
                                        note = saved.message
                                        SessionManager.login(merged)
                                        photo = null
                                    }
                                    .onFailure { error = it.message ?: "Could not save" }
                                saving = false
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = SurfaceWhite, strokeWidth = 2.dp)
                        } else {
                            Text(if (current.shopTrusted) "Update" else "Save", fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Not now", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun GstDetailsCard(profile: GstProfile, modifier: Modifier = Modifier) {
    val active = profile.status.equals("Active", ignoreCase = true)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (active) Color(0xFFF0FDF4) else Color(0xFFFFF7ED),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) Color(0xFF86EFAC) else Color(0xFFFDBA74)
        )
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "GST registry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                if (profile.status.isNotBlank()) {
                    Text(
                        profile.status,
                        color = if (active) Color(0xFF166534) else Color(0xFF9A3412),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            GstDetailLine("Trade name", profile.tradeName)
            GstDetailLine("Legal name", profile.legalName)
            GstDetailLine("Constitution", profile.constitution)
            GstDetailLine("Taxpayer type", profile.taxpayerType)
            GstDetailLine("Registered on", profile.registrationDate)
            GstDetailLine("State jurisdiction", profile.stateJurisdiction)
            GstDetailLine("Centre jurisdiction", profile.centralJurisdiction)
            GstDetailLine("Address", profile.address)
        }
    }
}

@Composable
private fun GstDetailLine(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
    }
}
