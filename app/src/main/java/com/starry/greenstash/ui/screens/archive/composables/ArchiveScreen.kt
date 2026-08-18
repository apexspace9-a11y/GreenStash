/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.archive.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.starry.greenstash.R
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.ui.screens.archive.ArchiveViewModel
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.utils.Constants
import com.starry.greenstash.utils.ImageUtils
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val viewModel: ArchiveViewModel = hiltViewModel()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val archivedGoals by viewModel.archivedGoals.collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.archive_screen_header),
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (archivedGoals.isEmpty()) {
                var showEmptyState by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(160)
                    showEmptyState = true
                }
                AnimatedVisibility(
                    visible = showEmptyState,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NoArchivedGoals()
                }
            } else {
                var showGoals by remember { mutableStateOf(false) }
                LaunchedEffect(archivedGoals.size) {
                    delay(180)
                    showGoals = true
                }
                if (!showGoals) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                AnimatedVisibility(
                    visible = showGoals,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            count = archivedGoals.size,
                            key = { index -> archivedGoals[index].goal.goalId }
                        ) { index ->
                            val goalItem = archivedGoals[index]
                            ArchivedLazyItem(
                                modifier = Modifier.animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null
                                ),
                                goalItem = goalItem,
                                defaultCurrency = viewModel.getDefaultCurrency(),
                                onRestoreConfirmed = {
                                    viewModel.restoreGoal(goalItem.goal)
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = context.getString(R.string.goal_restore_success),
                                            actionLabel = context.getString(R.string.ok),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                onDeleteConfirmed = {
                                    viewModel.deleteGoal(goalItem.goal)
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = context.getString(R.string.goal_delete_success),
                                            actionLabel = context.getString(R.string.ok),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedLazyItem(
    modifier: Modifier,
    goalItem: GoalWithTransactions,
    defaultCurrency: String,
    onRestoreConfirmed: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    Box(modifier = modifier) {
        val goalIcon = remember(goalItem.goal.goalIconId) {
            ImageUtils.createIconVector(
                goalItem.goal.goalIconId ?: Constants.DEFAULT_GOAL_ICON_ID
            ) ?: Icons.Filled.Star
        }
        val showRestoreDialog = remember { mutableStateOf(false) }
        val showDeleteDialog = remember { mutableStateOf(false) }

        ArchivedGoalItem(
            title = goalItem.goal.title,
            icon = goalIcon,
            savedAmount = NumberUtils.formatCurrency(
                goalItem.getCurrentlySavedAmount(),
                defaultCurrency
            ),
            onRestoreClicked = { showRestoreDialog.value = true },
            onDeleteClicked = { showDeleteDialog.value = true }
        )

        ArchiveDialogs(
            showRestoreDialog = showRestoreDialog,
            showDeleteDialog = showDeleteDialog,
            onRestoreConfirmed = onRestoreConfirmed,
            onDeleteConfirmed = onDeleteConfirmed
        )
    }
}
