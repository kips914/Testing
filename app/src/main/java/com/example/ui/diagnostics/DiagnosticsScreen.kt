package com.example.ui.diagnostics

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diag by viewModel.diagnostics.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.diag_title),
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
                    IconButton(onClick = { viewModel.runDiagnostics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Run", tint = MaterialTheme.colorScheme.primary)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "System Diagnostics Checklist",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DiagCheckItem(label = stringResource(R.string.diag_server_reachable), passed = diag.serverReachable)
                    DiagCheckItem(label = stringResource(R.string.diag_api), passed = diag.apiHealthy)
                    DiagCheckItem(label = stringResource(R.string.diag_auth), passed = diag.authenticationValid)
                    DiagCheckItem(label = stringResource(R.string.diag_tunnel), passed = diag.tunnelConnected)
                    DiagCheckItem(label = stringResource(R.string.diag_engine), passed = diag.engineRunning)
                    DiagCheckItem(label = stringResource(R.string.diag_model), passed = diag.modelLoaded)
                }
            }

            // Summary Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (diag.serverReachable) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF2E1A1A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (diag.serverReachable) "System Status: Healthy" else stringResource(R.string.err_unable_to_connect),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (diag.serverReachable) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (diag.serverReachable) diag.details else stringResource(R.string.err_possible_causes),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.runDiagnostics() },
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onBackground)
                    } else {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.general_retry))
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToConnection,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Connection Settings", color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DiagCheckItem(label: String, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (passed) "OK" else "FAILED",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
        }
    }
}
