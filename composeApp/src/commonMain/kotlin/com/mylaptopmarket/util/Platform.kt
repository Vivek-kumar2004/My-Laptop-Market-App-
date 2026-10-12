package com.mylaptopmarket.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

expect fun makeCall(phoneNumber: String)

expect fun openUrl(url: String)

expect fun currentTimeMillis(): Long

expect fun currentHourOfDay(): Int

@Composable
fun YoutubeVideoPlayer(videoUrl: String, modifier: Modifier) {
    val videoId = extractYoutubeVideoId(videoUrl)
    if (videoId == null) return

    val thumbnail = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

    Box(
        modifier = modifier
            .clickable {
                openUrl(videoUrl)
            }
    ) {
        AsyncImage(
            model = thumbnail,
            contentDescription = "Watch on YouTube",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

fun extractYoutubeVideoId(url: String): String? {
    if (url.isBlank()) return null
    return when {
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        url.contains("v=") -> url.substringAfter("v=").substringBefore("&").substringBefore("?")
        url.contains("embed/") -> url.substringAfter("embed/").substringBefore("?").substringBefore("&")
        url.contains("shorts/") -> url.substringAfter("shorts/").substringBefore("?").substringBefore("&")
        url.length == 11 -> url
        else -> null
    }?.take(11)
}
