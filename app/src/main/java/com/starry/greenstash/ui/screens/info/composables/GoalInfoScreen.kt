package com.starry.greenstash.ui.screens.info.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.starry.greenstash.R
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.database.goal.GoalPriority.High
import com.starry.greenstash.database.goal.GoalPriority.Low
import com.starry.greenstash.database.goal.GoalPriority.Normal
import com.starry.greenstash.ui.common.ExpandableTextCard
import com.starry.greenstash.ui.common.TipCard
import com.starry.greenstash.ui.screens.info.InfoViewModel
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.greenstashNumberFont
import com.starry.greenstash.utils.GoalTextUtils
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.displayName
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalInfoScreen(goalId: String, navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val viewModel: InfoViewModel = hiltViewModel()
    val parsedGoalId = remember(goalId) { goalId.toLongOrNull() }
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(parsedGoalId) {
        if (parsedGoalId == null) navController.navigateUp()
        else viewModel.loadGoalData(parsedGoalId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.info_screen_header),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val goalData = viewModel.state.goalData?.collectAsState(initial = null)?.value
            Crossfade(targetState = goalData, label = "GoalDataLoading") { item ->
                if (item == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val currency = viewModel.getDefaultCurrencyValue()
                    val saved = item.getCurrentlySavedAmount()
                    val target = item.goal.targetAmount
                    val progress = if (target.isFinite() && target > 0.0 && saved.isFinite()) {
                        (saved / target).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        GoalInfoCard(
                            currencySymbol = currency,
                            targetAmount = target,
                            savedAmount = saved,
                            daysLeftText = GoalTextUtils.getRemainingDaysText(
                                context = context,
                                goalItem = item,
                                dateStyle = viewModel.getDateStyle()
                            ),
                            progress = progress
                        )
                        GoalPriorityCard(item.goal.priority, item.goal.reminder)
                        if (item.goal.additionalNotes.isNotBlank()) {
                            GoalNotesCard(item.goal.additionalNotes)
                            Spacer(Modifier.height(6.dp))
                        }
                        if (item.transactions.isNotEmpty()) {
                            val showTip = remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                if (viewModel.shouldShowTransactionTip()) {
                                    delay(800)
                                    showTip.value = true
                                }
                            }
                            TipCard(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                description = stringResource(R.string.info_transaction_swipe_tip),
                                showTipCard = showTip.value,
                                onDismissRequest = {
                                    showTip.value = false
                                    viewModel.transactionTipDismissed()
                                }
                            )
                            TransactionItems(item.getOrderedTransactions(), currency, viewModel)
                        } else {
                            NoTransactionAnim()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalInfoCard(
    currencySymbol: String,
    targetAmount: Double,
    savedAmount: Double,
    daysLeftText: String,
    progress: Float
) {
    val safeProgress = progress.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
    val formattedTarget = NumberUtils.formatCurrency(NumberUtils.roundDecimal(targetAmount), currencySymbol)
    val formattedSaved = NumberUtils.formatCurrency(NumberUtils.roundDecimal(savedAmount), currencySymbol)
    val animatedProgress by animateFloatAsState(targetValue = safeProgress, label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
            .padding(top = 12.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = stringResource(R.string.info_card_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = greenstashFont,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formattedSaved,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                fontFamily = greenstashNumberFont,
                maxLines = 3,
                lineHeight = 1.3f.em,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.info_card_remaining_amount, formattedTarget),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = greenstashFont,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(12.dp)
                    .padding(horizontal = 8.dp).clip(RoundedCornerShape(40.dp)),
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(safeProgress * 100).toInt()}% | $daysLeftText",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = greenstashFont,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GoalPriorityCard(goalPriority: GoalPriority, reminders: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp, top = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val indicatorColor = when (goalPriority) {
                High -> Color(0xFFE09A24)
                Normal -> MaterialTheme.colorScheme.primary
                Low -> MaterialTheme.colorScheme.tertiary
            }
            val reminderIcon = if (reminders) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff
            val reminderText = stringResource(
                if (reminders) R.string.info_reminder_status_on else R.string.info_reminder_status_off
            )
            Box(Modifier.padding(start = 8.dp)) {
                PriorityIndicator(Modifier.size(13.dp), indicatorColor)
            }
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(R.string.info_goal_priority).format(goalPriority.displayName()),
                fontWeight = FontWeight.Medium,
                fontFamily = greenstashFont
            )
            Spacer(Modifier.weight(1f))
            Icon(reminderIcon, contentDescription = reminderText)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun GoalNotesCard(notesText: String) {
    ExpandableTextCard(
        title = stringResource(R.string.info_notes_card_title),
        description = notesText,
        showCopyButton = true,
        urlToOpen = Utils.extractFirstUrl(notesText)
    )
}

@Composable
private fun NoTransactionAnim() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val compositionResult: LottieCompositionResult = rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.no_transaction_found_lottie)
        )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = 1,
            speed = 1f
        )
        Spacer(Modifier.weight(1f))
        LottieAnimation(
            composition = compositionResult.value,
            progress = { progressAnimation },
            modifier = Modifier.size(320.dp),
            enableMergePaths = true
        )
        Text(
            text = stringResource(R.string.info_goal_no_transactions),
            fontFamily = greenstashFont,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp).offset(y = (-16).dp)
        )
        Spacer(Modifier.weight(2f))
    }
}
