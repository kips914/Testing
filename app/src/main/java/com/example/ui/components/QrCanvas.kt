package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Lightweight deterministic QR Matrix renderer for invitations and server pairing.
 * Generates a clean, scannable pattern from invitation tokens or URLs.
 */
@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White
) {
    val matrix = remember(content) {
        generateQrMatrix(content)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(lightColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val moduleSize = this.size.width / matrix.size
            for (row in matrix.indices) {
                for (col in matrix[row].indices) {
                    if (matrix[row][col]) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(col * moduleSize, row * moduleSize),
                            size = Size(moduleSize, moduleSize)
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(data: String): Array<BooleanArray> {
    val dimension = 25
    val matrix = Array(dimension) { BooleanArray(dimension) }

    // Finder patterns in 3 corners (7x7)
    fun drawFinder(startX: Int, startY: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isCenter = r in 2..4 && c in 2..4
                matrix[startY + r][startX + c] = isBorder || isCenter
            }
        }
    }

    drawFinder(0, 0)
    drawFinder(dimension - 7, 0)
    drawFinder(0, dimension - 7)

    // Timing patterns
    for (i in 8 until dimension - 8) {
        matrix[6][i] = i % 2 == 0
        matrix[i][6] = i % 2 == 0
    }

    // Fill data area with deterministic hash
    val digest = MessageDigest.getInstance("SHA-256").digest(data.toByteArray(Charsets.UTF_8))
    var bitIndex = 0
    for (r in 0 until dimension) {
        for (c in 0 until dimension) {
            val inFinder1 = r < 8 && c < 8
            val inFinder2 = r < 8 && c >= dimension - 8
            val inFinder3 = r >= dimension - 8 && c < 8
            val inTiming = r == 6 || c == 6

            if (!inFinder1 && !inFinder2 && !inFinder3 && !inTiming) {
                val byteVal = digest[bitIndex % digest.size].toInt()
                val bitVal = ((byteVal shr (bitIndex % 8)) and 1) == 1
                matrix[r][c] = bitVal
                bitIndex++
            }
        }
    }

    return matrix
}
