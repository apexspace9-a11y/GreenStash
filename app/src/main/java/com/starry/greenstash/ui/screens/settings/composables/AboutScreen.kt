/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.settings.composables

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.starry.greenstash.BuildConfig
import com.starry.greenstash.R
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .liquidGlass(radius = 30.dp),
                title = {
                    Text(
                        stringResource(R.string.about_screen_header),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = greenstashFont
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.weakHapticFeedback()
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                SettingsItem(
                    title = stringResource(R.string.mocquy_repository),
                    description = stringResource(R.string.about_readme_desc),
                    icon = Icons.AutoMirrored.Filled.Notes,
                    onClick = { Utils.openWebLink(context, MocQuyLinks.Repository) }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.about_privacy_title),
                    description = stringResource(R.string.about_privacy_desc),
                    icon = Icons.Filled.PrivacyTip,
                    onClick = { Utils.openWebLink(context, MocQuyLinks.Privacy) }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.about_gh_issue_title),
                    description = stringResource(R.string.about_gh_issue_desc),
                    icon = ImageVector.vectorResource(R.drawable.ic_about_gh_issue),
                    onClick = { Utils.openWebLink(context, MocQuyLinks.Issues) }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.about_version_title),
                    description = stringResource(R.string.about_version_desc).format(
                        BuildConfig.VERSION_NAME
                    ),
                    icon = Icons.Filled.Info,
                    onClick = {
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("", getVersionReport())
                            clipboard.setClipEntry(ClipEntry(clipData))
                        }
                    }
                )
            }
        }
    }
}

fun getVersionReport(): String {
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE
    val release = if (Build.VERSION.SDK_INT >= 30) {
        Build.VERSION.RELEASE_OR_CODENAME
    } else {
        Build.VERSION.RELEASE
    }
    return buildString {
        append("Mộc Quỹ: $versionName ($versionCode)\n")
        append("Android: $release (API ${Build.VERSION.SDK_INT})\n")
        append("Thiết bị: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})\n")
        append("ABI hỗ trợ: ${Build.SUPPORTED_ABIS.contentToString()}\n")
    }
}
