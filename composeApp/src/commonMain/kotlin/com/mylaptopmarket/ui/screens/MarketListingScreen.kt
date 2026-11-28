package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.model.MarketListing
import com.mylaptopmarket.util.OfflineSync
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.util.makeCall
import com.mylaptopmarket.util.openUrl

private val Panel = Color(0xFF101A2E)
private val Mist = Color(0xFF94A3B8)
private val Ice = Color(0xFFF8FAFC)
private val LogoBlue = Color(0xFF0767D8)

private fun marketPrice(price: String): String {
    val amount = price.toDoubleOrNull()
    return if (amount == null || amount <= 0.0) "Ask for price" else "₹$price"
}

@Composable
internal fun OpenMarketHeader(
    listings: List<MarketListing>,
    chip: String,
    shownCount: Int,
    onChip: (String) -> Unit
) {
    val categories = listOf("All") + listings.map { it.category }.filter { it.isNotBlank() }.distinct()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .tourTarget("open-market")
            .padding(top = 18.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(LogoBlue)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "OPEN MARKET",
                color = Ice,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Fresh dealer stock",
            color = Ice,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.6).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Shown under products. These listings are not linked to a vendor account.",
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { name ->
                val on = name == chip
                Text(
                    name,
                    color = if (on) LogoBlue else Ice,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (on) Ice else Color.White.copy(alpha = 0.16f))
                        .clickable { onChip(name) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("$shownCount independent listings", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}

@Composable
internal fun OpenMarketCard(
    listing: MarketListing,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .clickable { onOpen(listing.id.toString()) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Panel)
        ) {
            val photo = listing.images.firstOrNull()
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = listing.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    tint = Mist,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }
            Text(
                listing.category.ifBlank { "Stock" },
                color = Ice,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xCC0F172A))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                listing.name,
                color = Ice,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (listing.specs.isNotEmpty()) {
                Text(
                    listing.specs.take(2).joinToString(" · "),
                    color = Ice,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(marketPrice(listing.price), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            val who = listOf(listing.contactName, listing.city).filter { it.isNotBlank() }.joinToString(" · ")
            if (who.isNotBlank()) {
                Text(who, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketListingScreen(
    listingId: String,
    repository: ProductRepository,
    onBackClick: () -> Unit
) {
    var listing by remember { mutableStateOf<MarketListing?>(null) }
    var failed by remember { mutableStateOf(false) }
    var photoIndex by remember { mutableIntStateOf(0) }
    val catalogRevision by OfflineSync.revision.collectAsState()

    LaunchedEffect(listingId, catalogRevision) {
        val found = repository.getMarketListing(listingId)
        listing = found
        failed = found == null
    }

    Scaffold(
        containerColor = LogoBlue,
        topBar = {
            TopAppBar(
                title = { Text("Open Market", color = Ice, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ice)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LogoBlue)
            )
        }
    ) { padding ->
        when {
            listing == null && !failed -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Ice)
            }
            failed || listing == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("This listing is not available.", color = Ice)
            }
            else -> {
                val item = listing ?: return@Scaffold
                val photos = item.images
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Panel)
                    ) {
                        val photo = photos.getOrNull(photoIndex)
                        if (photo != null) {
                            AsyncImage(model = photo, contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                    }
                    if (photos.size > 1) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            photos.forEachIndexed { index, src ->
                                AsyncImage(
                                    model = src,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp, 48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(if (index == photoIndex) 2.dp else 0.dp, Ice, RoundedCornerShape(10.dp))
                                        .clickable { photoIndex = index }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(item.category.ifBlank { "Stock" }.uppercase(), color = Ice, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(item.name, color = Ice, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(marketPrice(item.price), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    val meta = listOf(
                        if (item.stock > 0) "${item.stock} in stock" else "",
                        item.city
                    ).filter { it.isNotBlank() }.joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(meta, color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
                    }
                    if (item.specs.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            item.specs.forEach { spec ->
                                Text(
                                    spec,
                                    color = Ice,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (item.contactName.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Text(item.contactName, color = Ice, fontWeight = FontWeight.Bold)
                    }
                    if (item.contactPhone.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { makeCall(item.contactPhone) },
                                colors = ButtonDefaults.buttonColors(containerColor = Ice, contentColor = LogoBlue)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { openUrl("https://wa.me/91${item.contactPhone}") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color(0xFF052E16))
                            ) {
                                Text("WhatsApp", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Independent listing. This seller is not a registered vendor.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                    if (item.description.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            item.description,
                            color = Ice,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .padding(14.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
