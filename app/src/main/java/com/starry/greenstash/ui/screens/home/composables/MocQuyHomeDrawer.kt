package com.starry.greenstash.ui.screens.home.composables

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.starry.greenstash.R
import com.starry.greenstash.ui.navigation.DrawerScreens
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MocQuyHomeDrawer(
    drawerState: DrawerState,
    navController: NavController
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val items = DrawerScreens.getAllItems()
    val selectedItem = remember { mutableStateOf(items.first()) }

    ModalDrawerSheet(
        modifier = Modifier
            .width(304.dp)
            .fillMaxHeight()
            .liquidGlass(radius = 0.dp, blurAmount = 32.dp),
        drawerShape = RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp),
        containerColor = Color.Transparent,
        drawerTonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 28.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(66.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.brand_name),
                        fontFamily = greenstashFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp
                    )
                    Text(
                        text = "Tích lũy cho điều quan trọng",
                        fontFamily = greenstashFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            )

            items.forEach { item ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(item.iconResId),
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.nameResId),
                            fontFamily = greenstashFont
                        )
                    },
                    selected = item == selectedItem.value,
                    onClick = {
                        view.weakHapticFeedback()
                        coroutineScope.launch {
                            drawerState.close()
                            if (item != selectedItem.value) {
                                withContext(Dispatchers.Main) { navController.navigate(item) }
                                selectedItem.value = item
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(Modifier.height(2.dp))
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Share, contentDescription = null) },
                label = { Text(stringResource(R.string.drawer_share), fontFamily = greenstashFont) },
                selected = false,
                onClick = {
                    view.weakHapticFeedback()
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
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
                label = { Text(stringResource(R.string.drawer_privacy), fontFamily = greenstashFont) },
                selected = false,
                onClick = {
                    view.weakHapticFeedback()
                    Utils.openWebLink(
                        context,
                        "https://github.com/apexspace9-a11y/GreenStash/blob/main/legal/PRIVACY-POLICY.md"
                    )
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(Modifier.weight(1f, fill = true))

            Text(
                text = stringResource(R.string.drawer_footer_text),
                modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 24.dp),
                fontFamily = greenstashFont,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
