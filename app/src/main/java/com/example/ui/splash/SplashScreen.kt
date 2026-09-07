package com.example.ui.splash

import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(350)
        step = 1 // Connecting
        delay(400)
        step = 2 // Loading settings
        delay(400)
        step = 3 // Checking server
        delay(350)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // OLI Logo Title
            Text(
                text = stringResource(R.string.splash_title),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.splash_initializing),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Steps status
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.Start
            ) {
                SplashStepRow(
                    label = stringResource(R.string.splash_connecting),
                    isDone = step >= 1,
                    isActive = step == 0
                )
                SplashStepRow(
                    label = stringResource(R.string.splash_loading_settings),
                    isDone = step >= 2,
                    isActive = step == 1
                )
                SplashStepRow(
                    label = stringResource(R.string.splash_checking_server),
                    isDone = step >= 3,
                    isActive = step == 2
                )
            }
        }
    }
}

@Composable
private fun SplashStepRow(
    label: String,
    isDone: Boolean,
    isActive: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (isDone || isActive) 1f else 0.4f,
        animationSpec = tween(300),
        label = "step_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.alpha(alpha)
    ) {
        if (isDone) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        } else if (isActive) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF30363D))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isDone) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
