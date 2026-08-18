package com.starry.greenstash.ui.screens.home.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.starry.greenstash.R
import com.starry.greenstash.ui.screens.home.HomeViewModel
import com.starry.greenstash.ui.screens.home.SearchBarState
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.weakHapticFeedback
import java.time.LocalTime

@Composable
fun HomeAppBar(
    searchBarState: SearchBarState,
    searchTextState: String,
    consumeBackPress: MutableState<Boolean>,
    onMenuClicked: () -> Unit,
    onFilterClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onSearchCloseClicked: () -> Unit,
    onSearchImeAction: (String) -> Unit,
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val goals by homeViewModel.goalsList.observeAsState(emptyList())
    val completedGoalCount = remember(goals) {
        goals.count { it.getCurrentlySavedAmount() >= it.goal.targetAmount }
    }

    Crossfade(
        targetState = searchBarState,
        animationSpec = tween(240),
        label = "mocquy-header-mode"
    ) { state ->
        when (state) {
            SearchBarState.CLOSED -> {
                MocQuyDashboardHeader(
                    goalCount = goals.size,
                    completedGoalCount = completedGoalCount,
                    onMenuClicked = onMenuClicked,
                    onFilterClicked = onFilterClicked,
                    onSearchClicked = onSearchClicked
                )
                consumeBackPress.value = false
            }

            SearchBarState.OPENED -> {
                MocQuySearchHeader(
                    text = searchTextState,
                    onTextChange = onSearchTextChange,
                    onCloseClicked = onSearchCloseClicked,
                    onSearchClicked = onSearchImeAction
                )
                consumeBackPress.value = true
            }
        }
    }
}

@Composable
private fun MocQuyDashboardHeader(
    goalCount: Int,
    completedGoalCount: Int,
    onMenuClicked: () -> Unit,
    onFilterClicked: () -> Unit,
    onSearchClicked: () -> Unit,
) {
    val view = LocalView.current
    val hour = remember { LocalTime.now().hour }
    val greeting = when (hour) {
        in 5..10 -> stringResource(R.string.home_header_greeting_morning)
        in 11..17 -> stringResource(R.string.home_header_greeting_afternoon)
        else -> stringResource(R.string.home_header_greeting_evening)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp)
            .liquidGlass(radius = 32.dp, blurAmount = 32.dp)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderActionButton(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.menu_button_desc),
                onClick = {
                    view.weakHapticFeedback()
                    onMenuClicked()
                }
            )

            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = greeting,
                    fontFamily = greenstashFont,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.brand_name),
                    fontFamily = greenstashFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HeaderActionButton(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.filter_button_desc),
                onClick = {
                    view.weakHapticFeedback()
                    onFilterClicked()
                }
            )
            Spacer(Modifier.size(6.dp))
            HeaderActionButton(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.search_button_desc),
                onClick = {
                    view.weakHapticFeedback()
                    onSearchClicked()
                }
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.16f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.home_screen_header),
                fontFamily = greenstashFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.home_header_goal_summary,
                    goalCount,
                    completedGoalCount
                ),
                fontFamily = greenstashFont,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeaderActionButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
                CircleShape
            )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MocQuySearchHeader(
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .liquidGlass(radius = 30.dp, blurAmount = 34.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    stringResource(R.string.home_search_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = greenstashFont
                )
            },
            singleLine = true,
            leadingIcon = {
                IconButton(
                    onClick = {
                        view.weakHapticFeedback()
                        onCloseClicked()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back_desc)
                    )
                }
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_button_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchClicked(text) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp)
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}
