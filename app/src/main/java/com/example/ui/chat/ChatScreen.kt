package com.example.ui.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.entities.MessageEntity
import com.example.ui.components.ConnectionPulseIndicator
import com.example.ui.components.MarkdownViewer
import com.example.ui.components.MessageActionsBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToOcr: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingText by viewModel.streamingText.collectAsState()
    val isServerOnline by viewModel.isServerOnline.collectAsState()
    val allChats by viewModel.allChats.collectAsState()
    val currentChatId by viewModel.currentChatId.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var chatToRename by remember { mutableStateOf<String?>(null) }
    var renameDialogText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty() || streamingText.isNotBlank()) {
            listState.animateScrollToItem(index = maxOf(0, messages.size))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "OLI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // Online / Offline Indicator
                        ConnectionPulseIndicator(isOnline = isServerOnline)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isServerOnline) stringResource(R.string.chat_online) else stringResource(R.string.chat_offline),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isServerOnline) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("chat_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = stringResource(R.string.chat_history),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { viewModel.startNewChat() },
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.chat_new_dialog),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content area: Messages or Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && streamingText.isBlank()) {
                    // Empty state with quick suggestions
                    ChatEmptyState(
                        onQuickPrompt = { prompt ->
                            if (prompt == "OCR") {
                                onNavigateToOcr()
                            } else {
                                inputText = prompt
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageItemView(
                                message = msg
                            )
                        }

                        // Active streaming response item
                        if (streamingText.isNotBlank()) {
                            item {
                                StreamingMessageItem(
                                    streamingText = streamingText
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Input Bar at bottom
            ChatInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                isGenerating = isGenerating,
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                onStop = { viewModel.stopGeneration() },
                onMenuAction = { action ->
                    when (action) {
                        "ocr" -> onNavigateToOcr()
                        "camera" -> onNavigateToOcr()
                        "gallery" -> onNavigateToOcr()
                        "file" -> {
                            inputText = "Please analyze this file: "
                        }
                    }
                }
            )
        }
    }

    // Chat History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chat_history),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = {
                            viewModel.startNewChat()
                            showHistorySheet = false
                        }
                    ) {
                        Text("+ ${stringResource(R.string.chat_new_dialog)}", color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allChats, key = { it.id }) { chat ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (chat.id == currentChatId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectChat(chat.id)
                                    showHistorySheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chat.title,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            chatToRename = chat.id
                                            renameDialogText = chat.title
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.chat_rename),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteChat(chat.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.chat_delete),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Rename Dialog
    if (chatToRename != null) {
        AlertDialog(
            onDismissRequest = { chatToRename = null },
            title = { Text(text = stringResource(R.string.chat_rename), color = MaterialTheme.colorScheme.onBackground) },
            text = {
                OutlinedTextField(
                    value = renameDialogText,
                    onValueChange = { renameDialogText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = chatToRename
                        if (id != null && renameDialogText.isNotBlank()) {
                            viewModel.renameChat(id, renameDialogText.trim())
                        }
                        chatToRename = null
                    }
                ) {
                    Text(stringResource(R.string.general_save), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToRename = null }) {
                    Text(stringResource(R.string.general_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun MessageItemView(message: MessageEntity) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF283654) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (isUser) {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    MarkdownViewer(markdown = message.content)
                    Spacer(modifier = Modifier.height(6.dp))
                    MessageActionsBar(rawText = message.content)
                }
            }
        }
    }
}

@Composable
fun StreamingMessageItem(streamingText: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                MarkdownViewer(markdown = "$streamingText ▍")
            }
        }
    }
}

@Composable
fun ChatEmptyState(
    onQuickPrompt: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.chat_empty_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.chat_empty_subtitle),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Quick suggestions chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                SuggestionChip(
                    onClick = { onQuickPrompt("Explain how OLI works") },
                    label = { Text(stringResource(R.string.chat_quick_explain), fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                SuggestionChip(
                    onClick = { onQuickPrompt("Write code for ") },
                    label = { Text(stringResource(R.string.chat_quick_code), fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                SuggestionChip(
                    onClick = { onQuickPrompt("Analyze ") },
                    label = { Text(stringResource(R.string.chat_quick_analyze), fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                SuggestionChip(
                    onClick = { onQuickPrompt("OCR") },
                    label = { Text(stringResource(R.string.chat_quick_ocr), fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    isGenerating: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onMenuAction: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [+] action button
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("chat_attachment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_camera), color = MaterialTheme.colorScheme.onBackground) },
                    leadingIcon = { Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        menuExpanded = false
                        onMenuAction("camera")
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_gallery), color = MaterialTheme.colorScheme.onBackground) },
                    leadingIcon = { Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        menuExpanded = false
                        onMenuAction("gallery")
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_ocr), color = MaterialTheme.colorScheme.onBackground) },
                    leadingIcon = { Icon(Icons.Default.DocumentScanner, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        menuExpanded = false
                        onMenuAction("ocr")
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_file), color = MaterialTheme.colorScheme.onBackground) },
                    leadingIcon = { Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        menuExpanded = false
                        onMenuAction("file")
                    }
                )
            }
        }

        // Multi-line Text Field
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.chat_input_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .testTag("chat_input_field")
        )

        // Send / Stop button
        if (isGenerating) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .testTag("chat_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = stringResource(R.string.chat_action_stop),
                    tint = Color.White
                )
            }
        } else {
            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color(0xFF21262D))
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.chat_action_send),
                    tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
