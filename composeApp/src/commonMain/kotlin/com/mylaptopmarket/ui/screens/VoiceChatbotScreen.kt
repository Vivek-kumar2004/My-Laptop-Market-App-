package com.mylaptopmarket.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.viewmodel.AiStatus
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.viewmodel.VoiceChatbotViewModel

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = com.mylaptopmarket.util.currentTimeMillis(),
    val language: String? = null,
    val fromAi: Boolean = false
)

private val VoiceBg = Color(0xFF070B14)
private val VoiceCard = Color(0xFF121826)
private val VoiceGlass = Color(0x1AFFFFFF)
private val VoiceText = Color(0xFFF8FAFC)
private val VoiceMuted = Color(0xFF94A3B8)
private val VoiceAccent = Color(0xFF7C5CFF)
private val VoiceAccent2 = Color(0xFF22D3EE)
private val VoiceOnline = Color(0xFF34D399)
private val VoiceOffline = Color(0xFFF87171)

@Composable
fun VoiceChatbotScreen(
    viewModel: VoiceChatbotViewModel,
    onBackClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isConversationMode by viewModel.isConversationMode.collectAsState()
    val currentTranscript by viewModel.currentTranscript.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()
    val voiceReadoutEnabled by viewModel.voiceReadoutEnabled.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.checkAiStatus()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1020), VoiceBg, Color(0xFF05070D))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(listOf(VoiceAccent.copy(alpha = 0.22f), Color.Transparent))
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(listOf(VoiceAccent2.copy(alpha = 0.16f), Color.Transparent))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            VoiceTopBar(
                aiStatus = aiStatus,
                voiceReadoutEnabled = voiceReadoutEnabled,
                onBackClick = onBackClick,
                onToggleVoiceReadout = { viewModel.toggleVoiceReadout() }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    EmptyVoiceState(aiStatus = aiStatus)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageItem(message = message)
                        }
                    }
                }
            }

            VoiceDock(
                isConversationMode = isConversationMode,
                isListening = isListening,
                isProcessing = isProcessing,
                isSpeaking = isSpeaking,
                voiceReadoutEnabled = voiceReadoutEnabled,
                transcript = currentTranscript,
                errorMessage = errorMessage,
                onToggleTalk = { viewModel.toggleConversationMode() },
                onSendText = { viewModel.sendTypedMessage(it) }
            )
        }
    }
}

@Composable
private fun VoiceTopBar(
    aiStatus: AiStatus,
    voiceReadoutEnabled: Boolean,
    onBackClick: () -> Unit,
    onToggleVoiceReadout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = VoiceText
            )
        }
        Column(modifier = Modifier.weight(1f).tourTarget("page-ask")) {
            Text(
                "Floor Desk",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = VoiceText,
                letterSpacing = (-0.3).sp
            )
            Text(
                if (voiceReadoutEnabled) "Voice replies on" else "Text only — not read out",
                fontSize = 12.sp,
                color = VoiceMuted
            )
        }
        SpeakToggle(
            enabled = voiceReadoutEnabled,
            onToggle = onToggleVoiceReadout
        )
        Spacer(Modifier.width(8.dp))
        StatusPill(aiStatus)
    }
}

@Composable
private fun SpeakToggle(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (enabled) VoiceAccent.copy(alpha = 0.18f) else VoiceGlass)
            .border(
                1.dp,
                if (enabled) VoiceAccent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(50)
            )
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (enabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            contentDescription = if (enabled) "Voice on" else "Voice off",
            tint = if (enabled) VoiceAccent2 else VoiceMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = VoiceText,
                checkedTrackColor = VoiceAccent,
                uncheckedThumbColor = VoiceMuted,
                uncheckedTrackColor = Color(0xFF2A3348),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun StatusPill(aiStatus: AiStatus) {
    val (label, color) = when (aiStatus) {
        AiStatus.Checking -> "Checking" to Color(0xFFFBBF24)
        AiStatus.Online -> "Online" to VoiceOnline
        AiStatus.Offline -> "Offline" to VoiceOffline
    }
    val pulse = rememberInfiniteTransition(label = "status")
    val alpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "statusAlpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (aiStatus == AiStatus.Checking) alpha else 1f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyVoiceState(
    aiStatus: AiStatus
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(VoiceAccent.copy(alpha = 0.35f), VoiceAccent2.copy(alpha = 0.2f)))
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.GraphicEq,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = VoiceText
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "The floor is yours",
            color = VoiceText,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (aiStatus == AiStatus.Online) {
                "You're live. Tap Talk and start dealing — stock, prices, buyers."
            } else if (aiStatus == AiStatus.Checking) {
                "Warming up the floor..."
            } else {
                "Floor's quiet right now. I can still help from what's already listed."
            },
            color = VoiceMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun VoiceDock(
    isConversationMode: Boolean,
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    voiceReadoutEnabled: Boolean,
    transcript: String,
    errorMessage: String?,
    onToggleTalk: () -> Unit,
    onSendText: (String) -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (transcript.isNotBlank()) {
            Text(
                text = transcript,
                color = VoiceText.copy(alpha = 0.9f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFF87171),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        TalkOrb(
            active = isConversationMode,
            listening = isListening,
            processing = isProcessing,
            speaking = isSpeaking,
            onClick = onToggleTalk
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = when {
                isProcessing -> "Thinking..."
                isSpeaking && voiceReadoutEnabled -> "Speaking..."
                isListening -> "Listening — just talk"
                isConversationMode && !voiceReadoutEnabled -> "Listening · replies shown as text"
                isConversationMode -> "Conversation on · tap to end"
                else -> "Tap Talk to speak, or type below"
            },
            color = VoiceMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = typedText,
            onValueChange = { typedText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type your question...", color = VoiceMuted) },
            enabled = !isProcessing,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendText(typedText)
                    typedText = ""
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        onSendText(typedText)
                        typedText = ""
                    },
                    enabled = typedText.isNotBlank() && !isProcessing
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (typedText.isNotBlank()) VoiceAccent2 else VoiceMuted
                    )
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = VoiceText,
                unfocusedTextColor = VoiceText,
                focusedBorderColor = VoiceAccent.copy(alpha = 0.6f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                cursorColor = VoiceAccent2,
                focusedContainerColor = VoiceCard,
                unfocusedContainerColor = VoiceCard,
                disabledContainerColor = VoiceCard
            )
        )
    }
}

@Composable
private fun TalkOrb(
    active: Boolean,
    listening: Boolean,
    processing: Boolean,
    speaking: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    Box(
        modifier = Modifier.size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(
                        when {
                            processing -> Color(0xFFFBBF24).copy(alpha = 0.18f)
                            speaking -> VoiceAccent.copy(alpha = 0.22f)
                            listening -> VoiceAccent2.copy(alpha = 0.18f)
                            else -> VoiceAccent.copy(alpha = 0.16f)
                        }
                    )
            )
        }

        androidx.compose.material3.Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.size(92.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            if (active) {
                                listOf(VoiceAccent, VoiceAccent2)
                            } else {
                                listOf(Color(0xFF2A3348), Color(0xFF1A2233))
                            }
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = if (active) "End talk" else "Start talk",
                    modifier = Modifier.size(36.dp),
                    tint = VoiceText
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage
) {
    val bubbleBrush = if (message.isUser) {
        Brush.linearGradient(listOf(Color(0xFF4F46E5), VoiceAccent))
    } else {
        Brush.linearGradient(listOf(VoiceCard, Color(0xFF171F2E)))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(VoiceGlass)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = VoiceAccent2,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(bubbleBrush)
                .border(
                    1.dp,
                    Color.White.copy(alpha = if (message.isUser) 0.08f else 0.06f),
                    RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                if (!message.isUser) {
                    Text(
                        text = if (message.fromAi) "On the floor" else "Quick note",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (message.fromAi) VoiceAccent2 else VoiceMuted,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = markdownToAnnotatedString(
                        message.text,
                        if (message.isUser) VoiceText else Color(0xFFE2E8F0)
                    ),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

fun markdownToAnnotatedString(text: String, color: Color): AnnotatedString {
    val pattern = Regex("(\\*\\*(.+?)\\*\\*)|(\\*(.+?)\\*)|(`(.+?)`)")
    return buildAnnotatedString {
        var lastIndex = 0
        pattern.findAll(text).forEach { match ->
            append(text.substring(lastIndex, match.range.first))
            val bold = match.groupValues[2]
            val italic = match.groupValues[4]
            val code = match.groupValues[6]
            when {
                bold.isNotEmpty() -> withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                    append(bold)
                }
                italic.isNotEmpty() -> withStyle(SpanStyle(color = color, fontStyle = FontStyle.Italic)) {
                    append(italic)
                }
                code.isNotEmpty() -> withStyle(SpanStyle(color = color, fontFamily = FontFamily.Monospace)) {
                    append(code)
                }
            }
            lastIndex = match.range.last + 1
        }
        withStyle(SpanStyle(color = color)) {
            append(text.substring(lastIndex))
        }
    }
}
