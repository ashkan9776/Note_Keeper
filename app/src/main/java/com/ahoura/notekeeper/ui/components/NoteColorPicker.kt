package com.ahoura.notekeeper.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.domain.model.NoteColor
import com.ahoura.notekeeper.ui.theme.contentColorFor
import com.ahoura.notekeeper.ui.theme.toComposeColor

/**
 * Modal bottom sheet for choosing a note's background color.
 *
 * The full 11-color palette is laid out in a wrapping grid (no scrolling, everything visible at
 * once). The currently selected color is announced by name above the grid and crossfades as the
 * selection changes; each swatch springs up with a soft glow ring and an animated checkmark.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteColorPicker(
    selectedColor: NoteColor,
    onColorSelected: (NoteColor) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    val darkTheme = isSystemInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.color_picker_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedContent(
                        targetState = selectedColor,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut()
                        },
                        label = "colorName"
                    ) { color ->
                        Text(
                            text = stringResource(color.displayNameRes()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val selectedName = stringResource(selectedColor.displayNameRes())
                NoteColor.entries.forEach { color ->
                    val name = stringResource(color.displayNameRes())
                    ColorSwatch(
                        color = color.toComposeColor(darkTheme),
                        selected = color == selectedColor,
                        contentDescription = if (color == selectedColor) {
                            stringResource(R.string.color_selected_cd, selectedName)
                        } else {
                            name
                        },
                        onClick = { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swatchScale"
    )
    val ring = MaterialTheme.colorScheme.primary
    val ringWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        label = "ringWidth"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            // Soft outer glow ring that fades in when this swatch is selected.
            .drawBehind {
                if (ringWidth.toPx() > 0f) {
                    drawCircle(
                        color = ring.copy(alpha = 0.25f),
                        radius = size.minDimension / 2f
                    )
                }
            }
            .padding(4.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) maxOf(ringWidth, 2.dp) else 1.dp,
                color = if (selected) ring else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = selected, enter = scaleIn(), exit = scaleOut()) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = contentDescription,
                tint = color.contentColorFor(),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun NoteColor.displayNameRes(): Int = when (this) {
    NoteColor.DEFAULT -> R.string.color_name_default
    NoteColor.RED -> R.string.color_name_red
    NoteColor.PINK -> R.string.color_name_pink
    NoteColor.ORANGE -> R.string.color_name_orange
    NoteColor.YELLOW -> R.string.color_name_yellow
    NoteColor.GREEN -> R.string.color_name_green
    NoteColor.TEAL -> R.string.color_name_teal
    NoteColor.BLUE -> R.string.color_name_blue
    NoteColor.PURPLE -> R.string.color_name_purple
    NoteColor.GRAY -> R.string.color_name_gray
    NoteColor.CHARCOAL -> R.string.color_name_charcoal
}
