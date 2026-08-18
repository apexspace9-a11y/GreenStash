/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.starry.greenstash.BuildConfig
import com.starry.greenstash.R
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.weakHapticFeedback

private object MocQuyLinks {
    const val Repository = "https://github.com/apexspace9-a11y/GreenStash"
    const val Privacy = "https://github.com/apexspace9-a11y/GreenStash/blob/main/legal/PRIVACY-POLICY.md"
    const val Issues = "https://github.com/apexspace9-a11y/GreenStash/issues"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_screen_header),
                        fontFamily = greenstashFont,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.weakHapticFeedback()
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
                .liquidGlass(radius = 26.dp, blurAmount = 16.dp)
                .padding(vertical = 4.dp)
        ) {
            SettingsItem(
                title = stringResource(R.string.mocquy_repository),
                description = stringResource(R.string.about_readme_desc),
                icon = Icons.AutoMirrored.Filled.Notes,
                onClick = { Utils.openWebLink(context, MocQuyLinks.Repository) }
            )
            SettingsItem(
                title = stringResource(R.string.about_privacy_title),
                description = stringResource(R.string.about_privacy_desc),
                icon = Icons.Filled.PrivacyTip,
                onClick = { Utils.openWebLink(context, MocQuyLinks.Privacy) }
            )
            SettingsItem(
                title = stringResource(R.string.about_gh_issue_title),
                description = stringResource(R.string.about_gh_issue_desc),
                icon = ImageVector.vectorResource(R.drawable.ic_about_gh_issue),
                onClick = { Utils.openWebLink(context, MocQuyLinks.Issues) }
            )
            VersionRow()
        }
    }
}

@Composable
private fun VersionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = stringResource(R.string.about_version_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = greenstashFont
            )
            Text(
                text = stringResource(R.string.about_version_desc).format(BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = greenstashFont
            )
        }
    }
}
