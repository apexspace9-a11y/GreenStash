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

    fun navigateTo(item: DrawerScreens) {
        coroutineScope.launch {
            drawerState.close()
            if (item != DrawerScreens.Home) {
                navController.navigate(item)
            }
        }
    }

    fun createGoal() {
        coroutineScope.launch {
            drawerState.close()
            navController.navigate(OtherScreens.InputScreen())
        }
    }

    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier
            .width(286.dp)
            .fillMaxHeight()
            .liquidGlass(radius = 28.dp, blurAmount = 22.dp),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = Color.Transparent,
        drawerTonalElevation = 0.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            item(key = "brand") {
                CompactBrandHeader()
            }

            item(key = "new-goal") {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.drawer_quick_new_goal),
                            fontFamily = greenstashFont,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selected = false,
                    onClick = {
                        view.weakHapticFeedback()
                        createGoal()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
            }

            item(key = "main-divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
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
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.nameResId),
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
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
            }

            item(key = "utility-divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            item(key = "share") {
                CompactUtilityItem(
                    icon = Icons.Filled.Share,
                    title = stringResource(R.string.drawer_share),
                    onClick = {
                        view.weakHapticFeedback()
                        coroutineScope.launch {
                            drawerState.close()
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
                    }
                )
            }

            item(key = "privacy") {
                CompactUtilityItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = stringResource(R.string.drawer_privacy),
                    onClick = {
                        view.weakHapticFeedback()
                        coroutineScope.launch {
                            drawerState.close()
                            Utils.openWebLink(
                                context,
                                "https://github.com/apexspace9-a11y/GreenStash/blob/main/legal/PRIVACY-POLICY.md"
                            )
                        }
                    }
                )
            }

            item(key = "footer") {
                Text(
                    text = stringResource(R.string.drawer_version, BuildConfig.VERSION_NAME),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 14.dp),
                    fontFamily = greenstashFont,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CompactBrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = stringResource(R.string.brand_name),
                fontFamily = greenstashFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.drawer_brand_tagline),
                fontFamily = greenstashFont,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactUtilityItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(21.dp)
            )
        },
        label = {
            Text(
                text = title,
                fontFamily = greenstashFont,
                maxLines = 1
            )
        },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(18.dp)
    )
}
