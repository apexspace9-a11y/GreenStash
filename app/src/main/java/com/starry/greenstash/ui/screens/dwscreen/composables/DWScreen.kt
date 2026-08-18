package com.starry.greenstash.ui.screens.dwscreen.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.date_time.DateTimeDialog
import com.maxkeppeler.sheets.date_time.models.DateTimeConfig
import com.maxkeppeler.sheets.date_time.models.DateTimeSelection
import com.starry.greenstash.R
import com.starry.greenstash.database.transaction.TransactionType
import com.starry.greenstash.ui.common.DateTimeCard
import com.starry.greenstash.ui.navigation.DrawerScreens
import com.starry.greenstash.ui.navigation.OtherScreens
import com.starry.greenstash.ui.screens.dwscreen.DWViewModel
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.validateAmount
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DWScreen(goalId: String, transactionTypeName: String, navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val viewModel: DWViewModel = hiltViewModel()
    val parsedGoalId = remember(goalId) { goalId.toLongOrNull() }
    val transactionType = remember(transactionTypeName) {
        viewModel.convertTransactionType(transactionTypeName)
    }

    val selectedDateTime = remember { mutableStateOf(LocalDateTime.now()) }
    val dateTimeDialogState = rememberUseCaseState(visible = false)
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val showTransactionAddedAnim = remember { mutableStateOf(false) }

    LaunchedEffect(parsedGoalId, transactionType) {
        if (parsedGoalId == null || transactionType == TransactionType.Invalid) {
            navController.navigateUp()
        }
    }

    DateTimeDialog(
        state = dateTimeDialogState,
        selection = DateTimeSelection.DateTime(
            selectedDate = selectedDateTime.value.toLocalDate(),
            selectedTime = selectedDateTime.value.toLocalTime(),
        ) { selectedDateTime.value = it },
        config = DateTimeConfig(locale = Locale.US)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (transactionType == TransactionType.Deposit) {
                            stringResource(R.string.deposit_screen_title)
                        } else {
                            stringResource(R.string.withdraw_screen_title)
                        },
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
    ) { paddingValues ->
        if (showTransactionAddedAnim.value) {
            TransactionAddedAnimation(transactionType)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState(), reverseScrolling = true),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainDWAnimation(transactionType)
                DateTimeCard(
                    selectedDateTime = selectedDateTime.value,
                    dateStyle = { viewModel.getDateStyle() },
                    onClick = { dateTimeDialogState.show() }
                )
                DWInputFields(
                    amountValue = viewModel.state.amount,
                    notesValue = viewModel.state.notes,
                    onAmountChange = { amount ->
                        viewModel.state = viewModel.state.copy(
                            amount = NumberUtils.getValidatedNumber(amount)
                        )
                    },
                    onNotesChange = { notes ->
                        viewModel.state = viewModel.state.copy(notes = notes)
                    }
                )

                Button(
                    enabled = parsedGoalId != null && transactionType != TransactionType.Invalid,
                    onClick = {
                        if (!viewModel.state.amount.validateAmount()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(context.getString(R.string.amount_empty_err))
                            }
                            return@Button
                        }
                        val id = parsedGoalId ?: return@Button
                        val onFailure = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(context.getString(R.string.unknown_error))
                            }
                        }
                        when (transactionType) {
                            TransactionType.Deposit -> viewModel.deposit(
                                goalId = id,
                                dateTime = selectedDateTime.value,
                                onGoalAchieved = {
                                    coroutineScope.launch {
                                        showTransactionAddedAnim.value = true
                                        delay(1100)
                                        navController.navigate(OtherScreens.CongratsScreen)
                                    }
                                },
                                onComplete = {
                                    navigateToHome(navController, coroutineScope, showTransactionAddedAnim)
                                },
                                onFailure = onFailure
                            )
                            TransactionType.Withdraw -> viewModel.withdraw(
                                goalId = id,
                                dateTime = selectedDateTime.value,
                                onWithDrawOverflow = {
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.withdraw_overflow_error)
                                        )
                                    }
                                },
                                onComplete = {
                                    navigateToHome(navController, coroutineScope, showTransactionAddedAnim)
                                },
                                onFailure = onFailure
                            )
                            TransactionType.Invalid -> onFailure()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (transactionType == TransactionType.Deposit) {
                            stringResource(R.string.deposit_button)
                        } else {
                            stringResource(R.string.withdraw_button)
                        },
                        fontFamily = greenstashFont
                    )
                }
            }
        }
    }
}

@Composable
private fun MainDWAnimation(transactionType: TransactionType) {
    val compositionResult: LottieCompositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(
            if (transactionType == TransactionType.Deposit) R.raw.dw_deposit_lottie
            else R.raw.dw_withdraw_lottie
        )
    )
    val progressAnimation by animateLottieCompositionAsState(
        compositionResult.value,
        isPlaying = true,
        iterations = 1,
        speed = 1f
    )
    LottieAnimation(
        composition = compositionResult.value,
        progress = { progressAnimation },
        modifier = Modifier.size(280.dp).padding(top = 28.dp),
        enableMergePaths = true
    )
}

@Composable
private fun DWInputFields(
    amountValue: String,
    notesValue: String,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    OutlinedTextField(
        value = amountValue,
        onValueChange = onAmountChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
        label = { Text(stringResource(R.string.transaction_amount), fontFamily = greenstashFont) },
        leadingIcon = {
            Icon(ImageVector.vectorResource(R.drawable.ic_input_amount), contentDescription = null)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        ),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    OutlinedTextField(
        value = notesValue,
        onValueChange = onNotesChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp),
        label = { Text(stringResource(R.string.input_additional_notes), fontFamily = greenstashFont) },
        leadingIcon = {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_input_additional_notes),
                contentDescription = null
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        ),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
private fun TransactionAddedAnimation(transactionType: TransactionType) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val compositionResult = rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.transaction_added_lottie)
        )
        val progressAnimation by animateLottieCompositionAsState(
            compositionResult.value,
            isPlaying = true,
            iterations = 1,
            speed = 1.4f
        )
        Spacer(Modifier.weight(1f))
        LottieAnimation(
            composition = compositionResult.value,
            progress = { progressAnimation },
            modifier = Modifier.size(320.dp)
        )
        Text(
            text = if (transactionType == TransactionType.Deposit) {
                stringResource(R.string.deposit_successful)
            } else {
                stringResource(R.string.withdraw_successful)
            },
            fontWeight = FontWeight.SemiBold,
            fontFamily = greenstashFont,
            fontSize = 20.sp
        )
        Spacer(Modifier.weight(1.4f))
    }
}

private fun navigateToHome(
    navController: NavController,
    coroutineScope: CoroutineScope,
    showTransactionAddedAnim: MutableState<Boolean>
) {
    coroutineScope.launch {
        showTransactionAddedAnim.value = true
        delay(1100)
        withContext(Dispatchers.Main) {
            if (!navController.popBackStack(DrawerScreens.Home, false)) {
                navController.navigate(DrawerScreens.Home)
            }
        }
    }
}
