package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionPulseIndicator(isOnline: Boolean) {
    val activeColor = MaterialTheme.colorScheme.tertiary
    val inactiveColor = MaterialTheme.colorScheme.error

    if (isOnline) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_scale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_alpha"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            // Pulsing background
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = alpha))
            )
            // Core dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(inactiveColor)
            )
        }
    }
}
