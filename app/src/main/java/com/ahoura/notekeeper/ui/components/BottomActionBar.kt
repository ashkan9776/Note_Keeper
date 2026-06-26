package com.ahoura.notekeeper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.R

/**
 * Bottom action bar shown during multi-select. Slides up from the bottom and exposes the bulk
 * actions: pin, archive, change color, and delete.
 */
@Composable
fun BottomActionBar(
    visible: Boolean,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onChangeColor: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionIcon(Icons.Filled.PushPin, R.string.action_pin, onPin)
                ActionIcon(Icons.Filled.Archive, R.string.action_archive, onArchive)
                ActionIcon(Icons.Filled.Palette, R.string.action_change_color, onChangeColor)
                ActionIcon(Icons.Filled.Delete, R.string.action_delete, onDelete)
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescriptionRes: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescriptionRes),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
