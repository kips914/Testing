package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

sealed class MarkdownBlock {
    data class Header(val text: String, val level: Int) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
}

@Composable
fun MarkdownViewer(
    markdown: String,
    modifier: Modifier = Modifier,
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val blocks = parseMarkdown(markdown)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Header -> {
                    val fontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.sp
                    }
                    Text(
                        text = block.text,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = formatInlineMarkdown(block.text),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = formatInlineMarkdown(block.text),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(language = block.language, code = block.code)
                }
            }
        }
    }
}

@Composable
fun CodeBlockCard(language: String, code: String) {
    val context = LocalContext.current
    val displayLang = if (language.isBlank()) "Code" else language

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1318)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("code_block_card")
    ) {
        Column {
            // Header bar with language and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayLang,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                        Toast.makeText(context, context.getString(R.string.chat_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.chat_action_copy),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.chat_action_copy),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Monospaced code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFE6EDF3)
                )
            }
        }
    }
}

@Composable
fun MessageActionsBar(
    rawText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("OLI message", rawText))
                Toast.makeText(context, context.getString(R.string.chat_copied_to_clipboard), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("copy_message_button")
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.chat_action_copy),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.chat_action_copy),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, rawText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share with OLI"))
            },
            modifier = Modifier.testTag("share_message_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.chat_action_share),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.chat_action_share),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseMarkdown(text: String): List<MarkdownBlock> {
    val lines = text.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var inCodeBlock = false
    var codeLang = ""
    val codeBuilder = StringBuilder()

    for (line in lines) {
        val trimmed = line.trimEnd()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(MarkdownBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLang = trimmed.removePrefix("```").trim()
            }
            continue
        }

        if (inCodeBlock) {
            codeBuilder.appendLine(line)
            continue
        }

        when {
            trimmed.startsWith("### ") -> blocks.add(MarkdownBlock.Header(trimmed.removePrefix("### "), 3))
            trimmed.startsWith("## ") -> blocks.add(MarkdownBlock.Header(trimmed.removePrefix("## "), 2))
            trimmed.startsWith("# ") -> blocks.add(MarkdownBlock.Header(trimmed.removePrefix("# "), 1))
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> blocks.add(MarkdownBlock.Bullet(trimmed.substring(2)))
            trimmed.isNotBlank() -> blocks.add(MarkdownBlock.Paragraph(trimmed))
        }
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
    }

    return blocks
}

@Composable
private fun formatInlineMarkdown(text: String) = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        if (text.startsWith("**", index)) {
            val end = text.indexOf("**", index + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                    append(text.substring(index + 2, end))
                }
                index = end + 2
                continue
            }
        }

        if (text.startsWith("`", index)) {
            val end = text.indexOf("`", index + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF1E2835),
                        color = Color(0xFF79C0FF)
                    )
                ) {
                    append(" ${text.substring(index + 1, end)} ")
                }
                index = end + 1
                continue
            }
        }

        if (text[index] == '*' && index + 1 < text.length && text[index + 1] != '*') {
            val end = text.indexOf('*', index + 1)
            if (end != -1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onBackground)) {
                    append(text.substring(index + 1, end))
                }
                index = end + 1
                continue
            }
        }

        append(text[index])
        index++
    }
}
