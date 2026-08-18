package com.starry.greenstash.ui.screens.settings.composables

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.starry.greenstash.MainActivity
import com.starry.greenstash.R
import com.starry.greenstash.ui.common.CurrencyPicker
import com.starry.greenstash.ui.common.CurrencyPickerData
import com.starry.greenstash.ui.navigation.OtherScreens
import com.starry.greenstash.ui.screens.home.GoalCardStyle
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.ui.screens.settings.SettingsViewModel
import com.starry.greenstash.ui.screens.settings.ThemeMode
import com.starry.greenstash.ui.screens.settings.dateStyleToDisplayFormat
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.getActivity
import com.starry.greenstash.utils.toToast
import com.starry.greenstash.utils.weakHapticFeedback
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val view = LocalView.current
    val context = LocalContext.current
    val viewModel = (context.getActivity() as MainActivity).settingsViewModel

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_screen_header),
                        fontFamily = greenstashFont,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.weakHapticFeedback()
                            navController.navigateUp()
                        },
                        modifier = Modifier.semantics {
                            onClick(label = context.getString(R.string.navigate_back_desc)) { true }
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DisplaySettings(viewModel, navController) }
            item { LocaleSettings(viewModel) }
            item { SecuritySettings(viewModel) }
            item { MiscSettings(navController) }
        }
    }
}

@Composable
private fun DisplaySettings(
    viewModel: SettingsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val showThemeDialog = rememberSaveable { mutableStateOf(false) }
    val theme = viewModel.theme.observeAsState(ThemeMode.Auto).value
    val goalStyle = viewModel.goalCardStyle.observeAsState(GoalCardStyle.Classic).value
    val materialYou = viewModel.materialYou.observeAsState(false)
    val amoledTheme = viewModel.amoledTheme.observeAsState(false)

    val themeValue = when (theme) {
        ThemeMode.Light -> stringResource(R.string.theme_dialog_option1)
        ThemeMode.Dark -> stringResource(R.string.theme_dialog_option2)
        ThemeMode.Auto -> stringResource(R.string.theme_dialog_option3)
    }
    val goalStyleValue = when (goalStyle) {
        GoalCardStyle.Classic -> stringResource(R.string.goal_card_option1)
        GoalCardStyle.Compact -> stringResource(R.string.goal_card_option2)
    }

    SettingsSection(title = stringResource(R.string.display_settings_title)) {
        SettingsItem(
            title = stringResource(R.string.theme_setting),
            description = themeValue,
            icon = Icons.Filled.BrightnessMedium,
            onClick = { showThemeDialog.value = true }
        )
        SettingsItem(
            title = stringResource(R.string.amoled_theme_setting),
            description = stringResource(R.string.amoled_theme_desc),
            icon = Icons.Filled.Contrast,
            switchState = amoledTheme,
            onCheckChange = viewModel::setAmoledTheme
        )
        SettingsItem(
            title = stringResource(R.string.material_you_setting),
            description = stringResource(R.string.material_you_setting_desc),
            icon = Icons.Filled.Palette,
            switchState = materialYou,
            onCheckChange = { enabled ->
                if (enabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    viewModel.setMaterialYou(false)
                    context.getString(R.string.material_you_error).toToast(context)
                } else {
                    viewModel.setMaterialYou(enabled)
                }
            }
        )
        SettingsItem(
            title = stringResource(R.string.goal_card_setting),
            description = goalStyleValue,
            icon = Icons.Filled.Style,
            onClick = { navController.navigate(OtherScreens.GoalCardStyleScreen) }
        )
    }

    if (showThemeDialog.value) {
        ThemePickerDialog(
            selectedTheme = theme,
            showDialog = showThemeDialog,
            onThemeChange = viewModel::setTheme
        )
    }
}

@Composable
private fun ThemePickerDialog(
    selectedTheme: ThemeMode,
    showDialog: MutableState<Boolean>,
    onThemeChange: (ThemeMode) -> Unit
) {
    val options = listOf(
        ThemeMode.Light to stringResource(R.string.theme_dialog_option1),
        ThemeMode.Dark to stringResource(R.string.theme_dialog_option2),
        ThemeMode.Auto to stringResource(R.string.theme_dialog_option3)
    )
    val selected = remember(selectedTheme) { mutableStateOf(selectedTheme) }

    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = {
            Text(
                text = stringResource(R.string.theme_dialog_title),
                fontFamily = greenstashFont
            )
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { (theme, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = selected.value == theme,
                                onClick = { selected.value = theme },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected.value == theme,
                            onClick = null
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 12.dp),
                            fontFamily = greenstashFont
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onThemeChange(selected.value)
                    showDialog.value = false
                }
            ) {
                Text(
                    text = stringResource(R.string.theme_dialog_apply_button),
                    fontFamily = greenstashFont
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = greenstashFont
                )
            }
        }
    )
}

@SuppressLint("InlinedApi")
@Composable
private fun LocaleSettings(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val dateStyle = viewModel.dateStyle.observeAsState(DateStyle.DD_MM_YYYY).value
    val dateValue = dateStyleToDisplayFormat(dateStyle)
    val dateDialog = rememberSaveable { mutableStateOf(false) }
    val selectedDate = rememberSaveable(dateStyle) { mutableStateOf(dateStyle) }
    val currencyDialog = rememberSaveable { mutableStateOf(false) }

    val currencyNames = context.resources.getStringArray(R.array.currency_names)
    val currencyValues = context.resources.getStringArray(R.array.currency_values)
    val defaultCode = viewModel.getDefaultCurrencyValue() ?: "VND"
    val defaultIndex = currencyValues.indexOf(defaultCode)
        .takeIf { it >= 0 }
        ?: currencyValues.indexOf("VND").takeIf { it >= 0 }
        ?: 0
    val selectedCurrencyName = rememberSaveable(defaultCode) {
        mutableStateOf(currencyNames.getOrElse(defaultIndex) { defaultCode })
    }

    SettingsSection(title = stringResource(R.string.locales_setting_title)) {
        val showAppLocale = remember {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Utils.isMiui()
        }
        if (showAppLocale) {
            SettingsItem(
                title = stringResource(R.string.app_locale_setting),
                description = stringResource(R.string.app_locale_setting_desc),
                icon = Icons.Filled.Language,
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                }
            )
        }
        SettingsItem(
            title = stringResource(R.string.date_format_setting),
            description = dateValue,
            icon = ImageVector.vectorResource(R.drawable.ic_settings_calender),
            onClick = { dateDialog.value = true }
        )
        SettingsItem(
            title = stringResource(R.string.preferred_currency_setting),
            description = selectedCurrencyName.value,
            icon = ImageVector.vectorResource(R.drawable.ic_settings_currency),
            onClick = { currencyDialog.value = true }
        )
    }

    if (dateDialog.value) {
        AlertDialog(
            onDismissRequest = { dateDialog.value = false },
            title = {
                Text(
                    text = stringResource(R.string.date_format_dialog_title),
                    fontFamily = greenstashFont
                )
            },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    DateStyle.entries.forEach { style ->
                        val label = dateStyleToDisplayFormat(style)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = selectedDate.value == style,
                                    onClick = { selectedDate.value = style },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDate.value == style,
                                onClick = null
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 12.dp),
                                fontFamily = greenstashFont
                            )
                        }
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        viewModel.setDateStyle(selectedDate.value)
                        dateDialog.value = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        fontFamily = greenstashFont
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dateDialog.value = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontFamily = greenstashFont
                    )
                }
            }
        )
    }

    CurrencyPicker(
        defaultCurrencyValue = currencyValues.getOrElse(defaultIndex) { "VND" },
        currencyPickerData = CurrencyPickerData(
            currencyNames = currencyNames,
            currencyValues = currencyValues
        ),
        showBottomSheet = currencyDialog,
        onCurrencySelected = { code ->
            viewModel.setDefaultCurrency(code)
            val index = currencyValues.indexOf(code)
            selectedCurrencyName.value = if (index >= 0) {
                currencyNames.getOrElse(index) { code }
            } else {
                code
            }
        }
    )
}

@Composable
private fun SecuritySettings(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val appLockSwitch = rememberSaveable { mutableStateOf(viewModel.getAppLockValue()) }

    SettingsSection(title = stringResource(R.string.security_settings_title)) {
        SettingsItem(
            title = stringResource(R.string.app_lock_setting),
            description = stringResource(R.string.app_lock_setting_desc),
            icon = Icons.Filled.Lock,
            switchState = appLockSwitch,
            onCheckChange = { enabled ->
                if (!enabled) {
                    appLockSwitch.value = false
                    viewModel.setAppLock(false)
                    return@SettingsItem
                }

                val activity = context.getActivity() as MainActivity
                val executor: Executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            appLockSwitch.value = false
                            context.getString(R.string.auth_error)
                                .format(errString)
                                .toToast(context)
                        }

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            appLockSwitch.value = true
                            activity.mainViewModel.setAppUnlocked(true)
                            viewModel.setAppLock(true)
                            context.getString(R.string.auth_successful).toToast(context)
                        }

                        override fun onAuthenticationFailed() {
                            appLockSwitch.value = false
                            context.getString(R.string.auth_failed).toToast(context)
                        }
                    }
                )
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(context.getString(R.string.bio_lock_title))
                    .setSubtitle(context.getString(R.string.bio_lock_subtitle))
                    .setAllowedAuthenticators(Utils.getAuthenticators())
                    .build()
                prompt.authenticate(promptInfo)
            }
        )
    }
}

@Composable
private fun MiscSettings(navController: NavController) {
    SettingsSection(title = stringResource(R.string.misc_setting_title)) {
        SettingsItem(
            title = stringResource(R.string.license_setting),
            description = stringResource(R.string.license_setting_desc),
            icon = Icons.Filled.LocalPolice,
            onClick = { navController.navigate(OtherScreens.OSLScreen) }
        )
        SettingsItem(
            title = stringResource(R.string.app_info_setting),
            description = stringResource(R.string.app_info_setting_desc),
            icon = Icons.Filled.Info,
            onClick = { navController.navigate(OtherScreens.AboutScreen) }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontFamily = greenstashFont,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp, bottom = 7.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(radius = 26.dp, blurAmount = 16.dp)
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}
