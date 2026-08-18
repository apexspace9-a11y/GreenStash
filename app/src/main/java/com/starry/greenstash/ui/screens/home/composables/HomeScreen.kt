/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.home.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.psoffritti.taptargetcompose.TapTargetCoordinator
import com.psoffritti.taptargetcompose.TapTargetStyle
import com.psoffritti.taptargetcompose.TextDefinition
import com.starry.greenstash.R
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.ui.navigation.OtherScreens
import com.starry.greenstash.ui.screens.home.FilterField
import com.starry.greenstash.ui.screens.home.FilterSortType
import com.starry.greenstash.ui.screens.home.HomeViewModel
import com.starry.greenstash.ui.screens.home.SearchBarState
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.displayName
import com.starry.greenstash.utils.isScrollingUp
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val allGoalState = viewModel.goalsList.observeAsState(emptyList())
    val showFilterSheet = remember { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val searchBarState by viewModel.searchBarState
    val searchTextState by viewModel.searchTextState
    val lazyListState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val consumeBackPress = remember { mutableStateOf(false) }

    BackHandler(enabled = consumeBackPress.value) {
        if (viewModel.searchBarState.value == SearchBarState.OPENED) {
            if (searchTextState.isNotBlank()) {
                viewModel.updateSearchTextState("")
            } else {
                viewModel.updateSearchWidgetState(SearchBarState.CLOSED)
            }
        }
    }

    if (showFilterSheet.value) {
        ModalBottomSheet(
            containerColor = Color.Transparent,
            onDismissRequest = {
                coroutineScope.launch {
                    filterSheetState.hide()
                    delay(220)
                    showFilterSheet.value = false
                }
            },
            sheetState = filterSheetState
        ) {
            FilterSheetContent(viewModel)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = { MocQuyHomeDrawer(drawerState, navController) }
    ) {
        val showTapTargets = remember { mutableStateOf(false) }
        LaunchedEffect(viewModel.showOnboardingTapTargets.value) {
            delay(700)
            showTapTargets.value = viewModel.showOnboardingTapTargets.value
        }

        TapTargetCoordinator(
            showTapTargets = showTapTargets.value,
            onComplete = { viewModel.onboardingTapTargetsShown() }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackBarHostState) },
                topBar = {
                    HomeAppBar(
                        searchBarState = searchBarState,
                        searchTextState = searchTextState,
                        consumeBackPress = consumeBackPress,
                        onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                        onFilterClicked = { showFilterSheet.value = true },
                        onSearchClicked = {
                            viewModel.updateSearchWidgetState(SearchBarState.OPENED)
                        },
                        onSearchTextChange = { viewModel.updateSearchTextState(it) },
                        onSearchCloseClicked = {
                            viewModel.updateSearchWidgetState(SearchBarState.CLOSED)
                        },
                        onSearchImeAction = { }
                    )
                },
                floatingActionButton = {
                    HomeExtendedFAB(
                        modifier = Modifier.tapTarget(
                            precedence = 0,
                            title = TextDefinition(
                                text = stringResource(R.string.new_goal_onboarding_title),
                                textStyle = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = greenstashFont,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            description = TextDefinition(
                                text = stringResource(R.string.new_goal_onboarding_desc),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontFamily = greenstashFont
                            ),
                            tapTargetStyle = TapTargetStyle(
                                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                tapTargetHighlightColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                backgroundAlpha = 1f
                            )
                        ),
                        lazyListState = lazyListState,
                        navController = navController
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (allGoalState.value.isEmpty()) {
                        MocQuyEmptyState()
                    } else if (searchTextState.isNotBlank()) {
                        GoalSearchResults(
                            allGoalState,
                            searchTextState,
                            viewModel,
                            navController,
                            snackBarHostState,
                            coroutineScope
                        )
                    } else {
                        AllGoalsList(
                            lazyListState,
                            allGoalState,
                            viewModel,
                            navController,
                            snackBarHostState,
                            coroutineScope
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSearchResults(
    allGoalState: State<List<GoalWithTransactions>>,
    searchTextState: String,
    viewModel: HomeViewModel,
    navController: NavController,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val locale = LocalConfiguration.current.locales[0]
    val allGoals = allGoalState.value
    val filteredList = allGoals.filter { goalItem ->
        goalItem.goal.title.lowercase(locale).contains(searchTextState.lowercase(locale))
    }

    if (filteredList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val compositionResult: LottieCompositionResult = rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.no_goal_found_lottie)
            )
            val progressAnimation by animateLottieCompositionAsState(
                compositionResult.value,
                isPlaying = true,
                iterations = LottieConstants.IterateForever
            )
            Spacer(Modifier.weight(1f))
            LottieAnimation(
                composition = compositionResult.value,
                progress = { progressAnimation },
                modifier = Modifier.size(280.dp),
                enableMergePaths = true
            )
            Text(
                text = stringResource(R.string.search_goal_not_found),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                fontFamily = greenstashFont
            )
            Spacer(Modifier.weight(2f))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                count = filteredList.size,
                key = { filteredList[it].goal.goalId },
                contentType = { 0 }
            ) { idx ->
                Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                    GoalLazyColumnItem(
                        viewModel,
                        filteredList[idx],
                        snackBarHostState,
                        coroutineScope,
                        navController,
                        idx
                    )
                }
            }
        }
    }
}

@Composable
private fun AllGoalsList(
    lazyListState: LazyListState,
    allGoalState: State<List<GoalWithTransactions>>,
    viewModel: HomeViewModel,
    navController: NavController,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val allGoals = allGoalState.value
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        item(key = "mocquy-overview", contentType = "overview") {
            SavingsOverviewCard(allGoals)
        }
        items(
            count = allGoals.size,
            key = { allGoals[it].goal.goalId },
            contentType = { 0 }
        ) { idx ->
            Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                GoalLazyColumnItem(
                    viewModel,
                    allGoals[idx],
                    snackBarHostState,
                    coroutineScope,
                    navController,
                    idx
                )
            }
        }
        item { Spacer(Modifier.height(84.dp)) }
    }
}

@Composable
private fun HomeExtendedFAB(
    modifier: Modifier,
    lazyListState: LazyListState,
    navController: NavController
) {
    val isFabVisible = lazyListState.isScrollingUp()
    val density = LocalDensity.current
    val view = LocalView.current

    AnimatedVisibility(
        visible = isFabVisible,
        enter = slideInVertically { with(density) { 40.dp.roundToPx() } } + fadeIn(),
        exit = fadeOut(animationSpec = keyframes { durationMillis = 120 })
    ) {
        ExtendedFloatingActionButton(
            modifier = modifier
                .padding(end = 10.dp, bottom = 12.dp)
                .liquidGlass(radius = 24.dp, blurAmount = 28.dp),
            onClick = {
                view.weakHapticFeedback()
                navController.navigate(OtherScreens.InputScreen())
            },
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.new_goal_fab),
                fontFamily = greenstashFont
            )
        }
    }
}

@Composable
private fun FilterSheetContent(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .liquidGlass(radius = 30.dp, blurAmount = 32.dp)
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                FilterField.entries.forEach {
                    FilterButton(
                        text = it.displayName(),
                        isSelected = it == viewModel.filterFlowData.value.filterField,
                        onClick = { viewModel.updateFilterField(it) }
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                FilterSortType.entries.forEach {
                    FilterButton(
                        text = it.displayName(),
                        isSelected = viewModel.filterFlowData.value.sortType.name == it.name,
                        onClick = { viewModel.updateFilterSort(it) }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val view = LocalView.current
    Card(
        modifier = Modifier
            .height(60.dp)
            .padding(6.dp)
            .liquidGlass(radius = 18.dp, blurAmount = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
            } else Color.Transparent
        ),
        shape = RoundedCornerShape(18.dp),
        onClick = {
            view.weakHapticFeedback()
            onClick()
        }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontFamily = greenstashFont,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MocQuyEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .liquidGlass(radius = 38.dp, blurAmount = 32.dp)
                .padding(horizontal = 34.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(170.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_goal_set),
                fontFamily = greenstashFont,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
