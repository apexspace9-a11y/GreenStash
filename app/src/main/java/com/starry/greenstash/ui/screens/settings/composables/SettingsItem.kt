/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.starry.greenstash.R
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.utils.weakHapticFeedback

@Composable
fun SettingsItem(
    title: String,
    description: String?,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                view.weakHapticFeedback()
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        SettingsText(
            title = title,
            description = description,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String?,
    icon: ImageVector,
    switchState: State<Boolean>,
    onCheckChange: (Boolean) -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                role = Role.Switch
                contentDescription = listOfNotNull(title, description).joinToString(", ")
                stateDescription = if (switchState.value) {
                    context.getString(R.string.switch_state_open)
                } else {
                    context.getString(R.string.switch_state_close)
                }
                onClick(
                    label = if (switchState.value) {
                        context.getString(R.string.switch_action_close)
                    } else {
                        context.getString(R.string.switch_action_open)
                    }
                ) {
                    view.weakHapticFeedback()
                    onCheckChange(!switchState.value)
                    true
                }
            }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        SettingsText(
            title = title,
            description = description,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 10.dp)
        )
        Switch(
            checked = switchState.value,
            onCheckedChange = {
                view.weakHapticFeedback()
                onCheckChange(it)
            },
            thumbContent = if (switchState.value) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsText(
    title: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = greenstashFont
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = greenstashFont
            )
        }
    }
}
