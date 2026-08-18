package com.starry.greenstash.ui.screens.input.composables

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.starry.greenstash.R
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.ui.navigation.DrawerScreens
import com.starry.greenstash.ui.screens.input.InputViewModel
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.utils.ImageUtils
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.displayName
import com.starry.greenstash.utils.hasNotificationPermission
import com.starry.greenstash.utils.validateAmount
import com.starry.greenstash.utils.weakHapticFeedback
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(editGoalId: String?, navController: NavController) {
    val context = LocalContext.current
    val view = LocalView.current
    val viewModel: InputViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val parsedEditId = remember(editGoalId) { editGoalId?.toLongOrNull() }
    var image: Any? by remember { mutableStateOf(R.drawable.default_goal_image) }
    var icon by remember { mutableStateOf(Icons.Filled.Image) }
    val showIconPicker = remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val calendarState = rememberUseCaseState(visible = false)

    LaunchedEffect(editGoalId, parsedEditId) {
        if (editGoalId != null && parsedEditId == null) {
            navController.navigateUp()
            return@LaunchedEffect
        }
        parsedEditId?.let { id ->
            viewModel.setEditGoalData(id) { bitmap, iconId ->
                if (bitmap != null) image = bitmap
                if (!iconId.isNullOrBlank()) icon = ImageUtils.createIconVector(iconId) ?: Icons.Filled.Image
            }
            selectedDate = viewModel.state.deadline.takeIf { it > 0L }
                ?.let(Utils::convertEpochToLocalDate) ?: LocalDate.now()
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            image = uri
            viewModel.state = viewModel.state.copy(goalImageUri = uri)
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (!granted) viewModel.updateReminder(false) }

    val startDate = selectedDate?.takeIf { it.isBefore(LocalDate.now()) } ?: LocalDate.now()
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
            boundary = startDate..LocalDate.now().plusYears(100)
        ),
        selection = CalendarSelection.Date(selectedDate = selectedDate) { date ->
            selectedDate = date
            viewModel.state = viewModel.state.copy(deadline = Utils.convertLocalDateToEpoch(date))
        }
    )

    IconPickerDialog(
        viewModel = viewModel,
        showDialog = showIconPicker,
        onIconSelected = { item ->
            item?.let {
                icon = it.image ?: Icons.Filled.Image
                viewModel.updateSelectedIcon(it)
            }
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (parsedEditId == null) R.string.input_screen_header else R.string.input_edit_goal_header),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = greenstashFont,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.weakHapticFeedback()
                        navController.navigateUp()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(Modifier.fillMaxWidth().height(190.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(image).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp))
                    )
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(ImageVector.vectorResource(R.drawable.ic_input_image), null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.input_pick_image_fab), fontFamily = greenstashFont)
                    }
                }
            }

            Card(
                onClick = { showIconPicker.value = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.input_pick_icon), fontFamily = greenstashFont, fontWeight = FontWeight.Medium)
                }
            }

            PrioritySelector(viewModel.state.priority, viewModel::updatePriority)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.input_goal_reminder), Modifier.weight(1f), fontFamily = greenstashFont)
                    Switch(
                        checked = viewModel.state.reminder,
                        onCheckedChange = { enabled ->
                            viewModel.updateReminder(enabled)
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasNotificationPermission()) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        thumbContent = if (viewModel.state.reminder) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(SwitchDefaults.IconSize)) }
                        } else null
                    )
                }
            }

            AppField(
                viewModel.state.goalTitleText,
                viewModel::updateTitle,
                stringResource(R.string.input_text_title),
                ImageVector.vectorResource(R.drawable.ic_input_title),
                KeyboardType.Text
            )
            AppField(
                viewModel.state.targetAmount,
                { viewModel.updateTargetAmount(NumberUtils.getValidatedNumber(it)) },
                stringResource(R.string.input_text_amount),
                ImageVector.vectorResource(R.drawable.ic_input_amount),
                KeyboardType.Number
            )

            val deadline = viewModel.state.deadline.takeIf { it > 0L }
                ?.let(Utils::convertEpochToLocalDate)
                ?.format(DateTimeFormatter.ofPattern(viewModel.getDateStyleFormat(), Locale.ENGLISH)).orEmpty()
            OutlinedTextField(
                value = deadline,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.input_deadline), fontFamily = greenstashFont) },
                leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.ic_input_deadline), null) },
                trailingIcon = {
                    if (deadline.isNotEmpty()) {
                        IconButton(onClick = { viewModel.removeDeadLine() }) {
                            Icon(Icons.Filled.RemoveCircleOutline, null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )
            OutlinedButton(
                onClick = { calendarState.show() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text(stringResource(R.string.input_deadline), fontFamily = greenstashFont) }

            AppField(
                viewModel.state.additionalNotes,
                viewModel::updateAdditionalNotes,
                stringResource(R.string.input_additional_notes),
                ImageVector.vectorResource(R.drawable.ic_input_additional_notes),
                KeyboardType.Text,
                false
            )

            Button(
                onClick = {
                    when {
                        viewModel.state.goalTitleText.isBlank() -> scope.launch {
                            snackbar.showSnackbar(context.getString(R.string.title_empty_err))
                        }
                        !viewModel.state.targetAmount.validateAmount() -> scope.launch {
                            snackbar.showSnackbar(context.getString(R.string.amount_empty_err))
                        }
                        else -> {
                            val onComplete = {
                                if (!navController.popBackStack(DrawerScreens.Home, false)) {
                                    navController.navigate(DrawerScreens.Home)
                                }
                            }
                            val onFailure = {
                                scope.launch { snackbar.showSnackbar(context.getString(R.string.unknown_error)) }
                            }
                            if (parsedEditId != null) {
                                viewModel.editSavingGoal(parsedEditId, context, onComplete, onFailure)
                            } else {
                                viewModel.addSavingGoal(context, onComplete, onFailure)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Check, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (parsedEditId == null) R.string.input_add_goal_button else R.string.input_edit_goal_button),
                    fontFamily = greenstashFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PrioritySelector(selected: String, onSelected: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.input_goal_priority), fontFamily = greenstashFont, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GoalPriority.entries.forEach { priority ->
                    OutlinedButton(
                        onClick = { onSelected(priority.name) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (selected == priority.name) {
                            Icon(Icons.Filled.Check, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                        }
                        Text(priority.displayName(), fontFamily = greenstashFont, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontFamily = greenstashFont) },
        leadingIcon = { Icon(icon, null) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors()
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
)
