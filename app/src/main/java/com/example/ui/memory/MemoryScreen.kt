package com.example.ui.memory

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.OliMemoryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel,
    modifier: Modifier = Modifier
) {
    val memory by viewModel.memory.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    var userName by remember(memory) { mutableStateOf(memory.userName) }
    var userAge by remember(memory) { mutableStateOf(memory.userAge) }
    var userLanguage by remember(memory) { mutableStateOf(memory.userLanguage) }
    var interestsText by remember(memory) { mutableStateOf(memory.interests.joinToString(", ")) }
    var projectsText by remember(memory) { mutableStateOf(memory.projects.joinToString(", ")) }
    var preferencesText by remember(memory) { mutableStateOf(memory.preferences.joinToString(", ")) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            snackbarHostState.showSnackbar("Memory synchronized with OLI Server!")
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
                        text = stringResource(R.string.memory_title),
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
            // User Header Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.memory_user_header),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text(stringResource(R.string.memory_user_name)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth().testTag("memory_user_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = userAge,
                            onValueChange = { userAge = it },
                            label = { Text(stringResource(R.string.memory_user_age)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = userLanguage,
                            onValueChange = { userLanguage = it },
                            label = { Text(stringResource(R.string.memory_user_language)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Interests Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.memory_interests),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = interestsText,
                        onValueChange = { interestsText = it },
                        placeholder = { Text("AI, Robotics, Distributed systems...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Projects Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.memory_projects),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = projectsText,
                        onValueChange = { projectsText = it },
                        placeholder = { Text("OLI Ecosystem, Mobile Client...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Preferences Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.memory_preferences),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = preferencesText,
                        onValueChange = { preferencesText = it },
                        placeholder = { Text("Direct explanations, Dark theme, Privacy focus...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val updated = OliMemoryData(
                        userName = userName.trim(),
                        userAge = userAge.trim(),
                        userLanguage = userLanguage.trim(),
                        interests = interestsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        projects = projectsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        preferences = preferencesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    )
                    viewModel.saveMemory(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_memory_button")
            ) {
                Icon(if (isSaved) Icons.Default.Check else Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaved) stringResource(R.string.memory_saved) else stringResource(R.string.memory_save),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
