package com.ahoura.notekeeper.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.ui.theme.NoteKeeperTheme

/**
 * Centered empty-state with a hand-drawn "note" illustration (pure [Canvas], no stock asset)
 * plus a caption. Used by every list screen when there is nothing to show.
 */
@Composable
fun EmptyStateView(
    message: String,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val faint = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = w * 0.035f, cap = StrokeCap.Round)

            // Note sheet outline.
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.18f, h * 0.12f),
                size = Size(w * 0.64f, h * 0.76f),
                cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                style = stroke
            )
            // Text lines on the sheet.
            val lineColor = faint
            listOf(0.30f, 0.45f, 0.60f).forEachIndexed { index, fy ->
                val endX = if (index == 2) 0.55f else 0.70f
                drawLine(
                    color = lineColor,
                    start = Offset(w * 0.30f, h * fy),
                    end = Offset(w * endX, h * fy),
                    strokeWidth = w * 0.03f,
                    cap = StrokeCap.Round
                )
            }
            // A little "plus" accent in the corner.
            val cx = w * 0.74f
            val cy = h * 0.74f
            val r = w * 0.07f
            drawLine(accent, Offset(cx - r, cy), Offset(cx + r, cy), w * 0.03f, StrokeCap.Round)
            drawLine(accent, Offset(cx, cy - r), Offset(cx, cy + r), w * 0.03f, StrokeCap.Round)
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Preview(name = "Empty - Light", showBackground = true)
@Preview(name = "Empty - Dark", showBackground = true, backgroundColor = 0xFF202124)
@Composable
private fun EmptyStateViewPreview() {
    NoteKeeperTheme {
        EmptyStateView(message = "Your notes appear here")
    }
}
