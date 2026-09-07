package com.example.ui.console

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.ConsoleLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen(
    viewModel: ConsoleViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serverInfo by viewModel.serverInfo.collectAsState()
    val engineStatus by viewModel.engineStatus.collectAsState()
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.console_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status Grid Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ConsoleStatusItem(label = stringResource(R.string.console_gateway), status = "ONLINE", color = MaterialTheme.colorScheme.tertiary)
                        ConsoleStatusItem(label = stringResource(R.string.console_engine), status = engineStatus.status, color = if (engineStatus.status == "RUNNING") MaterialTheme.colorScheme.tertiary else Color(0xFFFFCC66))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ConsoleStatusItem(label = stringResource(R.string.console_model), status = "LOADED", color = MaterialTheme.colorScheme.tertiary)
                        ConsoleStatusItem(label = stringResource(R.string.console_connection), status = "OK", color = MaterialTheme.colorScheme.tertiary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "GPU: ${engineStatus.gpu ?: "Allocated"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "RAM: ${engineStatus.ram ?: "Active"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Model: ${engineStatus.model ?: "Default"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Buttons: [Refresh] [Restart Engine]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.console_refresh), color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { viewModel.restartEngine() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283654)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.console_restart_engine), fontSize = 12.sp)
                }
            }

            Text(
                text = "SYSTEM LOGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            // Real-time Logs Console Box
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs) { log ->
                        ConsoleLogRow(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsoleStatusItem(label: String, status: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ConsoleLogRow(log: ConsoleLog) {
    val levelColor = when (log.level) {
        "ERROR" -> MaterialTheme.colorScheme.error
        "WARN" -> Color(0xFFFFCC66)
        else -> MaterialTheme.colorScheme.primary
    }

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "[${log.timestamp}]",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "[${log.level}]",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = levelColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = log.message,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE6EDF3)
        )
    }
}
