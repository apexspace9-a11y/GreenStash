package com.starry.greenstash.ui.screens.home.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    LaunchedEffect(searchBarState) {
        consumeBackPress.value = searchBarState == SearchBarState.OPENED
    }

    Crossfade(
        targetState = searchBarState,
        animationSpec = tween(180),
        label = "mocquy-compact-header"
    ) { state ->
        when (state) {
            SearchBarState.CLOSED -> CompactHomeHeader(
                onMenuClicked = onMenuClicked,
                onFilterClicked = onFilterClicked,
                onSearchClicked = onSearchClicked
            )

            SearchBarState.OPENED -> CompactSearchHeader(
                text = searchTextState,
                onTextChange = onSearchTextChange,
                onCloseClicked = onSearchCloseClicked,
                onSearchClicked = onSearchImeAction
            )
        }
    }
}

@Composable
private fun CompactHomeHeader(
    onMenuClicked: () -> Unit,
    onFilterClicked: () -> Unit,
    onSearchClicked: () -> Unit,
) {
    val view = LocalView.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .liquidGlass(radius = 24.dp, blurAmount = 20.dp)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactHeaderButton(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(R.string.menu_button_desc),
            onClick = {
                view.weakHapticFeedback()
                onMenuClicked()
            }
        )

        Text(
            text = stringResource(R.string.brand_name),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            fontFamily = greenstashFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        CompactHeaderButton(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = stringResource(R.string.filter_button_desc),
            onClick = {
                view.weakHapticFeedback()
                onFilterClicked()
            }
        )
        Spacer(Modifier.size(2.dp))
        CompactHeaderButton(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.search_button_desc),
            onClick = {
                view.weakHapticFeedback()
                onSearchClicked()
            }
        )
    }
}

@Composable
private fun CompactHeaderButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                CircleShape
            )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(21.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompactSearchHeader(
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
) {
    val focusRequester = FocusRequester()
    val view = LocalView.current

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .liquidGlass(radius = 24.dp, blurAmount = 20.dp)
            .focusRequester(focusRequester),
        value = text,
        onValueChange = onTextChange,
        placeholder = {
            Text(
                text = stringResource(R.string.home_search_label),
                fontFamily = greenstashFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
