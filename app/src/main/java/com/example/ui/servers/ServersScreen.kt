package com.example.ui.servers

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.entities.ServerEntity
import com.example.data.models.DiscoveryServerItem
import com.example.ui.components.QrCodeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    viewModel: ServersViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myServers by viewModel.myServers.collectAsState()
    val otherServers by viewModel.discoveredServers.collectAsState()
    val serverInfo by viewModel.serverInfo.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val invitations by viewModel.invitations.collectAsState()

    var showControlSheet by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showInvitationsDialog by remember { mutableStateOf(false) }
    var showDevicesDialog by remember { mutableStateOf(false) }

    var selectedDiscoveredServer by remember { mutableStateOf<DiscoveryServerItem?>(null) }
    var showDiscoveredServerSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.servers_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToConnection() },
                        modifier = Modifier.testTag("add_server_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.conn_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.servers_my_servers),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // My Servers list
            if (myServers.isEmpty()) {
                item {
                    Text(
                        text = "No saved servers. Use the + button to add one.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                items(myServers, key = { it.id }) { server ->
                    MyServerCard(
                        server = server,
                        onOpenChat = onNavigateToChat,
                        onOpenControl = { showControlSheet = true },
                        onDelete = { viewModel.deleteServer(server.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.servers_other_servers),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Discovered / Other servers list
            if (otherServers.isEmpty()) {
                item {
                    Text(
                        text = "No additional servers found on the local network.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                items(otherServers, key = { it.id }) { disc ->
                    OtherServerCard(
                        item = disc,
                        onConnect = {
                            val newServer = ServerEntity(
                                id = disc.id,
                                name = disc.name,
                                description = disc.description,
                                address = disc.address,
                                accessMode = disc.accessMode,
                                isConnected = true
                            )
                            viewModel.connectServer(newServer)
                            onNavigateToChat()
                        },
                        onClick = {
                            selectedDiscoveredServer = disc
                            showDiscoveredServerSheet = true
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDiscoveredServerSheet && selectedDiscoveredServer != null) {
        val server = selectedDiscoveredServer!!
        ModalBottomSheet(
            onDismissRequest = {
                showDiscoveredServerSheet = false
                selectedDiscoveredServer = null
            },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Server Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoRow("Name", server.name)
                        InfoRow("Address", server.address)
                        InfoRow("Status", server.status)
                        InfoRow("Access Mode", server.accessMode)

                        if (server.internalIp != null) {
                            InfoRow("Internal IP", server.internalIp)
                        }
                        if (server.uptime != null) {
                            InfoRow("Uptime", server.uptime)
                        }
                        if (server.currentLoad != null) {
                            InfoRow("Current Load", server.currentLoad)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // OLI PC Server Control Bottom Sheet
    if (showControlSheet) {
        ModalBottomSheet(
            onDismissRequest = { showControlSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OLI Server Control",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Status badge
                    val isRunning = serverInfo.status == "RUNNING"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = serverInfo.status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(label = stringResource(R.string.server_name), value = serverInfo.name)
                        InfoRow(label = stringResource(R.string.server_access), value = serverInfo.accessMode)
                        InfoRow(label = stringResource(R.string.server_tunnel), value = serverInfo.tunnelStatus)
                        InfoRow(label = stringResource(R.string.server_engine), value = serverInfo.engineStatus)
                        InfoRow(label = stringResource(R.string.server_model), value = serverInfo.modelName)
                        InfoRow(label = stringResource(R.string.server_devices_count), value = "${devices.size}")
                    }
                }

                // Action buttons: Start / Stop / Restart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (serverInfo.status == "RUNNING") {
                        Button(
                            onClick = { viewModel.stopServer() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.server_stop))
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startServer() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.server_start))
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.restartEngine() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restart", color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Secondary action buttons: Settings, QR, Invitations, Devices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.server_settings), fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    OutlinedButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text(stringResource(R.string.server_qr), fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    OutlinedButton(
                        onClick = { showInvitationsDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.server_invitations), fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    OutlinedButton(
                        onClick = { showDevicesDialog = true },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text(stringResource(R.string.server_devices), fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // QR Code Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Server Pairing QR", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QrCodeView(
                        content = "oli://invite?id=oli_inv_9f82d04a&server=My_OLI_Server",
                        size = 180.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "oli://invite?id=oli_inv_9f82d04a",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text(stringResource(R.string.general_confirm), color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Server Settings Dialog
    if (showSettingsDialog) {
        var sName by remember { mutableStateOf(serverInfo.name) }
        var sDesc by remember { mutableStateOf(serverInfo.description) }
        var sPort by remember { mutableStateOf(serverInfo.port.toString()) }
        var sAccess by remember { mutableStateOf(serverInfo.accessMode) }
        var sProtected by remember { mutableStateOf(serverInfo.passwordProtected) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.server_settings), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { sName = it },
                        label = { Text(stringResource(R.string.server_name)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                    )
                    OutlinedTextField(
                        value = sDesc,
                        onValueChange = { sDesc = it },
                        label = { Text(stringResource(R.string.server_description)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                    )
                    OutlinedTextField(
                        value = sPort,
                        onValueChange = { sPort = it },
                        label = { Text("Port") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password Protected", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        Switch(
                            checked = sProtected,
                            onCheckedChange = { sProtected = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateServerConfig(
                            name = sName,
                            description = sDesc,
                            port = sPort.toIntOrNull() ?: 8080,
                            accessMode = sAccess,
                            passwordProtected = sProtected
                        )
                        showSettingsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.general_save), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.general_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Invitations Dialog
    if (showInvitationsDialog) {
        AlertDialog(
            onDismissRequest = { showInvitationsDialog = false },
            title = { Text(stringResource(R.string.server_invitations), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.createInvitation(1, 24) {} },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Create Invitation (24h, 1 use)")
                    }

                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(invitations, key = { it.id }) { inv ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242E)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(inv.token, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(inv.expiresAt, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    }
                                    if (inv.status == "Active") {
                                        TextButton(onClick = { viewModel.revokeInvitation(inv.id) }) {
                                            Text("Revoke", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                        }
                                    } else {
                                        Text("Revoked", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInvitationsDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    // Devices Dialog
    if (showDevicesDialog) {
        AlertDialog(
            onDismissRequest = { showDevicesDialog = false },
            title = { Text(stringResource(R.string.devices_connected), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                LazyColumn(modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(devices, key = { it.id }) { dev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(dev.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${dev.platform} • Last seen: ${dev.lastSeen}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                                TextButton(onClick = { viewModel.revokeDevice(dev.id) }) {
                                    Text(stringResource(R.string.devices_revoke), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevicesDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun MyServerCard(
    server: ServerEntity,
    onOpenChat: () -> Unit,
    onOpenControl: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_server_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = server.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = server.address,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Connected badge and delete icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E3A28))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (server.isConnected) "CONNECTED" else "SAVED",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!server.isConnected) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = server.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenChat,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.servers_open_chat), fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onOpenControl,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.server_settings), fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherServerCard(
    item: DiscoveryServerItem,
    onConnect: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283654))
            ) {
                Text(stringResource(R.string.servers_connect), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}
