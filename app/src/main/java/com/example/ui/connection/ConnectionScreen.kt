package com.example.ui.connection

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel,
    onConnected: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentServer by viewModel.currentServer.collectAsState()
    val foundServer by viewModel.foundServer.collectAsState()
    val isPairing by viewModel.isPairing.collectAsState()
    val pairingMessage by viewModel.pairingMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showScanDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualAddressInput by remember { mutableStateOf("") }
    var qrSimulatorInput by remember { mutableStateOf("") }
    var serverPasswordInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.conn_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Current Connected Server Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentServer != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(currentServer!!.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
                                Text(currentServer!!.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "No server connected yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Text(
                text = "Connect a new OLI Server using one of the methods below:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            // Method 1: Scan QR
            Button(
                onClick = { showScanDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("scan_qr_button")
            ) {
                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.conn_scan_qr), fontSize = 15.sp)
            }

            // Method 2: Manual IP / Host
            OutlinedButton(
                onClick = { showManualDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("enter_manual_button")
            ) {
                Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.conn_enter_manual), fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            }

            // Method 3: Discover on Local Network / Tailscale
            OutlinedButton(
                onClick = {
                    viewModel.discoverServers()
                    viewModel.handleManualAddress("http://100.84.12.9:8080")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("discover_servers_button")
            ) {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.conn_discover), fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }

    // QR Scan / Input Dialog
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = { Text(stringResource(R.string.conn_scan_qr), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Scan the QR code displayed in OLI PC Server, or paste an invite link (oli://invite?id=...)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = qrSimulatorInput,
                        onValueChange = { qrSimulatorInput = it },
                        label = { Text("Invite link or Token") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showScanDialog = false
                        viewModel.handleScannedQr(qrSimulatorInput)
                    }
                ) {
                    Text("Process QR", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) {
                    Text(stringResource(R.string.general_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Manual Address Dialog
    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text(stringResource(R.string.conn_enter_manual), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = manualAddressInput,
                        onValueChange = { manualAddressInput = it },
                        placeholder = { Text(stringResource(R.string.conn_manual_address_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showManualDialog = false
                        viewModel.handleManualAddress(manualAddressInput)
                    }
                ) {
                    Text(stringResource(R.string.general_confirm), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text(stringResource(R.string.general_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Server Found Confirmation Dialog (Section 20 requirement: no auto connect without confirmation)
    if (foundServer != null) {
        val server = foundServer!!
        AlertDialog(
            onDismissRequest = { viewModel.cancelFoundServer() },
            title = { Text(stringResource(R.string.conn_server_found), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: ${server.name}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    Text("Description: ${server.description}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text("Address: ${server.address}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text("Access: ${server.accessMode}", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)

                    if (server.requiresPassword) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = serverPasswordInput,
                            onValueChange = { serverPasswordInput = it },
                            label = { Text(stringResource(R.string.conn_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                        )
                    }

                    if (isPairing) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.conn_pairing), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmConnect(
                            password = if (server.requiresPassword) serverPasswordInput else null,
                            onConnected = {
                                onConnected()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isPairing
                ) {
                    Text(stringResource(R.string.servers_connect))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelFoundServer() },
                    enabled = !isPairing
                ) {
                    Text(stringResource(R.string.general_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
