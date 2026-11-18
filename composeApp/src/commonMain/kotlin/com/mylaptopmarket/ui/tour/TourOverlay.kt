package com.mylaptopmarket.ui.tour

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TourOverlay() {
    val running by AppTour.running.collectAsState()
    val index by AppTour.index.collectAsState()
    if (!running) return
    val cue = AppTour.cues.getOrNull(index) ?: return
    val session by SessionManager.currentSession.collectAsState()
    val targetId = cue.resolvedTarget(session)
    val raw = AppTour.bounds[targetId]

    var origin by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(index) {
        AppTour.navigator?.invoke(cue.place)
        if (cue.place == TourPlace.Home) {
            delay(280)
            AppTour.scrollHome?.invoke(targetId)
        } else {
            delay(280)
        }
        AppTour.waitFor(targetId)
    }

    val local = raw?.let { rect ->
        Rect(
            rect.left - origin.x,
            rect.top - origin.y,
            rect.right - origin.x,
            rect.bottom - origin.y
        )
    }
    var sticky by remember { mutableStateOf(Rect.Zero) }
    val live = local?.takeIf { it.width > 8f && it.height > 8f }
    if (live != null && sticky != live) sticky = live
    val animated by animateRectAsState(
        targetValue = if (sticky.width > 1f) sticky else Rect(0f, 0f, 1f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "spotlight"
    )
    val pulse = rememberInfiniteTransition(label = "pulse")
    val ring by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ring"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { origin = it.positionInRoot() }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(Color.Black.copy(alpha = 0.58f))
            if (animated.width > 8f) {
                val hole = if (cue.wide) {
                    val height = 250.dp.toPx()
                    Rect(
                        left = 12.dp.toPx(),
                        top = (animated.top - 8.dp.toPx()).coerceAtLeast(8.dp.toPx()),
                        right = size.width - 12.dp.toPx(),
                        bottom = (animated.top + height).coerceAtMost(size.height - 24.dp.toPx())
                    )
                } else {
                    animated.inflate(8.dp.toPx())
                }
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = hole.topLeft,
                    size = hole.size,
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
                val grow = 10.dp.toPx() + ring * 16.dp.toPx()
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f - ring * 0.7f),
                    topLeft = Offset(hole.left - grow, hole.top - grow),
                    size = Size(hole.width + grow * 2, hole.height + grow * 2),
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false).consume()
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                }
        )
        val cardWidth = 300.dp
        val density = LocalDensity.current
        val y = with(density) {
            if (animated.width <= 8f) {
                520.dp.toPx()
            } else {
                val holeBottom = if (cue.wide) animated.top + 250.dp.toPx() else animated.bottom
                val below = holeBottom + 16.dp.toPx()
                if (below > 620.dp.toPx()) (animated.top - 168.dp.toPx()).coerceAtLeast(72.dp.toPx()) else below
            }
        }
        Surface(
                modifier = Modifier
                    .offset { IntOffset(20.dp.toPx().roundToInt(), y.roundToInt()) }
                    .width(cardWidth),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceWhite,
                shadowElevation = 8.dp
            ) {
                AnimatedContent(
                    targetState = cue,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                    label = "cue"
                ) { item ->
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${index + 1} / ${AppTour.cues.size}",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(item.title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(item.body, color = TextSecondary, fontSize = 14.sp, lineHeight = 19.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (item == AppTour.cues.last()) AppTour.finish() else AppTour.next()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text(
                                    if (item == AppTour.cues.last()) "Done" else "Next",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        TextButton(
            onClick = { AppTour.finish() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(SurfaceWhite)
        ) {
            Text("Skip", color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Rect.inflate(delta: Float) = Rect(left - delta, top - delta, right + delta, bottom + delta)
