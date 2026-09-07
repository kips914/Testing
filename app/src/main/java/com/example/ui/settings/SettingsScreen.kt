package com.example.ui.settings

import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToConnection: () -> Unit,
    onNavigateToConsole: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Connection
            Text(
                text = stringResource(R.string.settings_section_conn),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionItem(
                        title = stringResource(R.string.settings_server),
                        subtitle = "Configuration & Endpoints",
                        icon = Icons.Default.Computer,
                        onClick = onNavigateToConnection
                    )
                    SettingsActionItem(
                        title = stringResource(R.string.tailscale_open),
                        subtitle = "Launch Tailscale secure mesh VPN",
                        icon = Icons.Default.OpenInNew,
                        onClick = {
                            val opened = viewModel.openTailscale(context)
                            if (!opened) {
                                Toast.makeText(context, "Opening Tailscale in store...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Section 2: AI Settings
            Text(
                text = stringResource(R.string.settings_section_ai),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Quick mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_quick), color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Fast response with low token latency", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.quickMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(quickMode = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Model name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.settings_model), color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        Text(settings.model, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Temperature slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.settings_temperature), color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                            Text(String.format("%.2f", settings.temperature), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                        Slider(
                            value = settings.temperature,
                            onValueChange = { viewModel.updateSettings(settings.copy(temperature = it)) },
                            valueRange = 0.1f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Context Window
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.settings_context), color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        Text("${settings.contextWindow} tokens", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }

            // Section 3: Appearance
            Text(
                text = stringResource(R.string.settings_section_appearance),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val themes = listOf("Dark", "Light", "AMOLED")
                            val nextTheme = themes[(themes.indexOf(settings.theme) + 1) % themes.size]
                            viewModel.updateSettings(settings.copy(theme = nextTheme))
                        }.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.settings_theme), color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        Text(settings.theme, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val langs = listOf("System", "EN", "RU")
                            val nextLang = langs[(langs.indexOf(settings.language) + 1) % langs.size]
                            viewModel.updateSettings(settings.copy(language = nextLang))
                        }.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.settings_language), color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        Text(settings.language, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Section 4: Advanced Tools
            Text(
                text = stringResource(R.string.settings_section_advanced),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionItem(
                        title = stringResource(R.string.nav_console),
                        subtitle = "Real-time engine & gateway logs",
                        icon = Icons.Default.Terminal,
                        onClick = onNavigateToConsole
                    )
                    SettingsActionItem(
                        title = stringResource(R.string.nav_diagnostics),
                        subtitle = "Automated connectivity checklist",
                        icon = Icons.Default.Build,
                        onClick = onNavigateToDiagnostics
                    )
                    SettingsActionItem(
                        title = stringResource(R.string.nav_about),
                        subtitle = "Version, architecture & licenses",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAbout
                    )
                    SettingsActionItem(
                        title = stringResource(R.string.settings_clear_cache),
                        subtitle = "Free up memory & local temp files",
                        icon = Icons.Default.Delete,
                        onClick = {
                            viewModel.clearCache()
                            Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                    SettingsActionItem(
                        title = stringResource(R.string.settings_reset_connection),
                        subtitle = "Disconnect current server session",
                        icon = Icons.Default.LinkOff,
                        onClick = {
                            viewModel.resetConnection()
                            Toast.makeText(context, "Session disconnected", Toast.LENGTH_SHORT).show()
                        },
                        titleColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}
