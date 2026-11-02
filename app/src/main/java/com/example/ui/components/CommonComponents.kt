package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskLevel
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeAccentSoft
import com.example.ui.theme.ThemeBorder
import com.example.ui.theme.ThemeDanger
import com.example.ui.theme.ThemeGlassBorder
import com.example.ui.theme.ThemeMuted
import com.example.ui.theme.ThemeSuccess
import com.example.ui.theme.ThemeSurface

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ThemeSurface,
    borderColor: Color = ThemeGlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor)
            .clickable { onClick() }
    } else {
        modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor)
    }

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (riskLevel) {
        RiskLevel.L0 -> Pair(Color(0x225B8C6E), ThemeSuccess)
        RiskLevel.L1 -> Pair(Color(0x22C9A227), ThemeAccent)
        RiskLevel.L2 -> Pair(Color(0x228B8778), Color(0xFFC0BCAD))
        RiskLevel.L3 -> Pair(Color(0x22D6573F), ThemeDanger)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = riskLevel.labelAr,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp
        )
    }
}

@Composable
fun GlowingMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.22f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(110.dp)
    ) {
        // Outer glow halo
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(if (isListening) pulseScale else 1f)
                .clip(CircleShape)
                .background(if (isListening) ThemeAccent.copy(alpha = 0.25f) else ThemeAccentSoft)
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .border(2.dp, ThemeAccent.copy(alpha = if (isListening) 0.9f else 0.4f), CircleShape)
                .background(ThemeSurface)
        )

        // Core interactive button
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(if (isListening) ThemeAccent else ThemeAccentSoft)
                .clickable { onClick() }
                .testTag("voice_listen_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isListening) "جاري الاستماع" else "تحدث الآن",
                color = if (isListening) Color(0xFF0E0F13) else ThemeAccent,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 11.sp
            )
        }
    }
}
