package com.starry.greenstash.ui.screens.home.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.starry.greenstash.R
import com.starry.greenstash.ui.screens.home.SearchBarState
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.weakHapticFeedback

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
    Crossfade(targetState = searchBarState, animationSpec = tween(280), label = "searchbar cross-fade") {
        when (it) {
            SearchBarState.CLOSED -> {
                DefaultAppBar(onMenuClicked, onFilterClicked, onSearchClicked)
                consumeBackPress.value = false
            }
            SearchBarState.OPENED -> {
                SearchAppBar(searchTextState, onSearchTextChange, onSearchCloseClicked, onSearchImeAction)
                consumeBackPress.value = true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultAppBar(
    onMenuClicked: () -> Unit,
    onFilterClicked: () -> Unit,
    onSearchClicked: () -> Unit,
) {
    val view = LocalView.current
    TopAppBar(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).liquidGlass(28.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        title = {
            Text(
                stringResource(R.string.home_screen_header),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = greenstashFont
            )
        },
        navigationIcon = {
            IconButton(onClick = { view.weakHapticFeedback(); onMenuClicked() }) {
                Icon(Icons.Filled.Menu, stringResource(R.string.menu_button_desc))
            }
        },
        actions = {
            IconButton(onClick = { view.weakHapticFeedback(); onFilterClicked() }) {
                Icon(Icons.AutoMirrored.Filled.Sort, stringResource(R.string.filter_button_desc))
            }
            IconButton(onClick = { view.weakHapticFeedback(); onSearchClicked() }) {
                Icon(Icons.Filled.Search, stringResource(R.string.search_button_desc))
            }
        }
    )
}

@Composable
private fun SearchAppBar(
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .focusRequester(focusRequester)
            .liquidGlass(28.dp),
        color = Color.Transparent
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(stringResource(R.string.home_search_label), color = MaterialTheme.colorScheme.onSurface, fontFamily = greenstashFont)
            },
            singleLine = true,
            leadingIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
            },
            trailingIcon = {
                IconButton(onClick = { if (text.isNotEmpty()) onTextChange("") else onCloseClicked() }) {
                    Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
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