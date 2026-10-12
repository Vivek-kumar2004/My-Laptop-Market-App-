package com.mylaptopmarket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.regex.Pattern

@Composable
fun AndroidYoutubePlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val videoId = remember(videoUrl) {
        extractYoutubeVideoId(videoUrl)
    }

    if (videoId == null) {
        Text("Invalid YouTube video URL")
        return
    }

    var loadFailed by remember { mutableStateOf(false) }

    if (loadFailed) {
        YoutubeFallback(videoUrl = videoUrl, videoId = videoId, modifier = modifier)
        return
    }

    AndroidView(
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                enableAutomaticInitialization = false
                initialize(
                    object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            try {
                                youTubePlayer.cueVideo(videoId, 0f)
                            } catch (e: Exception) {
                                loadFailed = true
                            }
                        }
                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
                        ) {
                            loadFailed = true
                        }
                    },
                    IFramePlayerOptions.Builder()
                        .controls(1)
                        .fullscreen(1)
                        .origin("https://www.youtube.com")
                        .build()
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

@Composable
fun YoutubeFallback(
    videoUrl: String,
    videoId: String,
    modifier: Modifier = Modifier
) {
    val thumbnail = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable {
                // In a real app, use the shared makeCall-like platform action for URLs
            }
    ) {
        AsyncImage(
            model = thumbnail,
            contentDescription = null,
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
                tint = Color.White
            )
        }
    }
}

private fun extractYoutubeVideoId(url: String): String? {
    if (url.isBlank()) return null
    val regex = "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*"
    val compiledPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
    val matcher = compiledPattern.matcher(url.trim())
    return if (matcher.find()) {
        val id = matcher.group(1)
        if (id?.length == 11) id else null
    } else {
        if (url.trim().length == 11) url.trim() else null
    }
}
