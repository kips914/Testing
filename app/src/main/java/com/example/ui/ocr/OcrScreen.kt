package com.example.ui.ocr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    viewModel: OcrViewModel,
    onNavigateBack: () -> Unit,
    onSendToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val rotationAngle by viewModel.rotationAngle.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()
    val resultText by viewModel.resultText.collectAsState()

    // Photo picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bmp = OcrEngine.loadBitmapFromUri(context, uri)
                viewModel.setImageBitmap(bmp)
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            viewModel.setImageBitmap(bmp)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ocr_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Preview Area
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedBitmap != null) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotationAngle.toFloat())
                                .padding(8.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.ocr_no_image),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Image input pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                    modifier = Modifier.weight(1f).testTag("ocr_gallery_button")
                ) {
                    Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ocr_choose_gallery), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                }

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                    modifier = Modifier.weight(1f).testTag("ocr_camera_button")
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ocr_take_photo), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Rotation & Recognition Controls
            Card(
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.cycleRotation() },
                            modifier = Modifier.testTag("ocr_rotate_button")
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stringResource(R.string.ocr_rotation)}: $rotationAngle°",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = { viewModel.recognize() },
                        enabled = selectedBitmap != null && !isRecognizing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("ocr_recognize_button")
                    ) {
                        if (isRecognizing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White)
                        } else {
                            Text(stringResource(R.string.ocr_recognize))
                        }
                    }
                }
            }

            // Recognition Result Box
            if (resultText.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.ocr_result),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resultText,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE6EDF3),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("OCR text", resultText))
                                    Toast.makeText(context, context.getString(R.string.chat_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("ocr_copy_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.chat_action_copy), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onSendToChat(resultText)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f).testTag("ocr_send_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.ocr_send_to_oli), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
