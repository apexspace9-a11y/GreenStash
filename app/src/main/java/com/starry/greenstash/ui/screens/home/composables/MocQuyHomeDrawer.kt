package com.starry.greenstash.ui.screens.home.composables

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.starry.greenstash.BuildConfig
import com.starry.greenstash.R
import com.starry.greenstash.ui.navigation.DrawerScreens
import com.starry.greenstash.ui.navigation.OtherScreens
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.launch

@Composable
fun MocQuyHomeDrawer(
    drawerState: DrawerState,
    navController: NavController
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val items = DrawerScreens.getAllItems()

    fun closeAndNavigate(destination: Any) {
        coroutineScope.launch {
            drawerState.close()
            when (destination) {
                is DrawerScreens -> navController.navigate(destination)
                is OtherScreens.InputScreen -> navController.navigate(destination)
            }
        }
    }

    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier
            .width(326.dp)
            .fillMaxHeight()
            .liquidGlass(radius = 36.dp, blurAmount = 38.dp),
        drawerShape = RoundedCornerShape(topEnd = 36.dp, bottomEnd = 36.dp),
        drawerContainerColor = Color.Transparent,
        drawerTonalElevation = 0.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item(key = "brand-card") {
                DrawerBrandCard()
            }

            item(key = "quick-title") {
                DrawerSectionTitle(stringResource(R.string.drawer_section_quick))
            }

            item(key = "quick-actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DrawerQuickAction(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Add,
                        title = stringResource(R.string.drawer_quick_new_goal),
                        description = stringResource(R.string.drawer_quick_new_goal_desc),
                        onClick = {
                            view.weakHapticFeedback()
                            closeAndNavigate(OtherScreens.InputScreen())
                        }
                    )
                    DrawerQuickAction(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Backup,
                        title = stringResource(R.string.drawer_quick_backup),
                        description = stringResource(R.string.drawer_quick_backup_desc),
                        onClick = {
                            view.weakHapticFeedback()
                            closeAndNavigate(DrawerScreens.Backups)
                        }
                    )
                }
            }

            item(key = "navigation-title") {
                DrawerSectionTitle(stringResource(R.string.drawer_section_navigation))
            }

            items(
                items = items,
                key = { it::class.qualifiedName ?: it.nameResId.toString() }
            ) { item ->
                val selected = item == DrawerScreens.Home
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(item.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Column {
                            Text(
                                text = stringResource(item.nameResId),
                                fontFamily = greenstashFont,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1
                            )
                            Text(
                                text = stringResource(drawerDescriptionRes(item)),
                                fontFamily = greenstashFont,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    badge = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selected = selected,
                    onClick = {
                        view.weakHapticFeedback()
                        if (item == DrawerScreens.Home) {
                            coroutineScope.launch { drawerState.close() }
                        } else {
                            closeAndNavigate(item)
                        }
                    },
                    modifier = Modifier.padding(vertical = 1.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
            }

            item(key = "utility-divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            item(key = "utility-title") {
                DrawerSectionTitle(stringResource(R.string.drawer_section_utilities))
            }

            item(key = "share") {
                DrawerUtilityItem(
                    icon = Icons.Filled.Share,
                    title = stringResource(R.string.drawer_share),
                    description = stringResource(R.string.drawer_share_desc),
                    onClick = {
                        view.weakHapticFeedback()
                        coroutineScope.launch { drawerState.close() }
                        val repoUrl = "https://github.com/apexspace9-a11y/GreenStash"
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                context.getString(R.string.drawer_share_message, repoUrl)
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(share, context.getString(R.string.drawer_share))
                        )
                    }
                )
            }

            item(key = "privacy") {
                DrawerUtilityItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = stringResource(R.string.drawer_privacy),
                    description = stringResource(R.string.drawer_privacy_desc),
                    onClick = {
                        view.weakHapticFeedback()
                        coroutineScope.launch { drawerState.close() }
                        Utils.openWebLink(
                            context,
                            "https://github.com/apexspace9-a11y/GreenStash/blob/main/legal/PRIVACY-POLICY.md"
                        )
                    }
                )
            }

            item(key = "footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.drawer_version, BuildConfig.VERSION_NAME),
                        fontFamily = greenstashFont,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.drawer_footer_text),
                        fontFamily = greenstashFont,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerBrandCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 6.dp)
            .liquidGlass(radius = 28.dp, blurAmount = 30.dp)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(58.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.brand_name),
                fontFamily = greenstashFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.drawer_brand_tagline),
                fontFamily = greenstashFont,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp),
        fontFamily = greenstashFont,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun DrawerQuickAction(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(112.dp)
            .liquidGlass(radius = 24.dp, blurAmount = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        CircleShape
                    )
                    .padding(7.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontFamily = greenstashFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = description,
                fontFamily = greenstashFont,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DrawerUtilityItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = greenstashFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    fontFamily = greenstashFont,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun drawerDescriptionRes(item: DrawerScreens): Int = when (item) {
    DrawerScreens.Home -> R.string.drawer_home_desc
    DrawerScreens.Archive -> R.string.drawer_archive_desc
    DrawerScreens.Backups -> R.string.drawer_backup_desc
    DrawerScreens.Settings -> R.string.drawer_settings_desc
}
