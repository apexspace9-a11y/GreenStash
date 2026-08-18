package com.starry.greenstash.ui.screens.home.composables

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.starry.greenstash.BuildConfig
import com.starry.greenstash.R
import com.starry.greenstash.ui.navigation.DrawerScreens
import com.starry.greenstash.ui.navigation.OtherScreens
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.launch

@Composable
fun MocQuyHomeDrawer(drawerState: DrawerState, navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val items = DrawerScreens.getAllItems()

    fun navigateTo(item: DrawerScreens) {
        scope.launch {
            drawerState.close()
            if (item != DrawerScreens.Home) navController.navigate(item)
        }
    }

    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier.width(286.dp).fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerTonalElevation = 6.dp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            item("brand") { CompactBrandHeader() }
            item("new-goal") {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Add, null, Modifier.size(22.dp)) },
                    label = {
                        Text(
                            stringResource(R.string.drawer_quick_new_goal),
                            fontFamily = greenstashFont,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selected = false,
                    onClick = {
                        view.weakHapticFeedback()
                        scope.launch {
                            drawerState.close()
                            navController.navigate(OtherScreens.InputScreen())
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item("main-divider") { DrawerDivider() }
            items(items, key = { it::class.qualifiedName ?: it.nameResId.toString() }) { item ->
                val selected = item == DrawerScreens.Home
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(item.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            stringResource(item.nameResId),
                            fontFamily = greenstashFont,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
                    selected = selected,
                    onClick = {
                        view.weakHapticFeedback()
                        navigateTo(item)
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item("utility-divider") { DrawerDivider() }
            item("share") {
                CompactUtilityItem(Icons.Filled.Share, stringResource(R.string.drawer_share)) {
                    view.weakHapticFeedback()
                    scope.launch {
                        drawerState.close()
                        val repoUrl = "https://github.com/apexspace9-a11y/GreenStash"
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.drawer_share_message, repoUrl))
                        }
                        context.startActivity(Intent.createChooser(share, context.getString(R.string.drawer_share)))
                    }
                }
            }
            item("privacy") {
                CompactUtilityItem(Icons.Filled.PrivacyTip, stringResource(R.string.drawer_privacy)) {
                    view.weakHapticFeedback()
                    scope.launch {
                        drawerState.close()
                        Utils.openWebLink(
                            context,
                            "https://github.com/apexspace9-a11y/GreenStash/blob/main/legal/PRIVACY-POLICY.md"
                        )
                    }
                }
            }
            item("footer") {
                Text(
                    text = stringResource(R.string.drawer_version, BuildConfig.VERSION_NAME),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 14.dp),
                    fontFamily = greenstashFont,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DrawerDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun CompactBrandHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(42.dp)
        )
        Spacer(Modifier.width(9.dp))
        Column {
            Text(
                stringResource(R.string.brand_name),
                fontFamily = greenstashFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                stringResource(R.string.drawer_brand_tagline),
                fontFamily = greenstashFont,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactUtilityItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(icon, null, Modifier.size(21.dp)) },
        label = { Text(title, fontFamily = greenstashFont, maxLines = 1) },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
