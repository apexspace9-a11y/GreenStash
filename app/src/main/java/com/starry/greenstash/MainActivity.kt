package com.starry.greenstash

import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.starry.greenstash.ui.screens.main.MainScreen
import com.starry.greenstash.ui.screens.settings.SettingsViewModel
import com.starry.greenstash.ui.theme.GreenStashTheme
import com.starry.greenstash.utils.Utils
import com.starry.greenstash.utils.toToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var mainViewModel: MainViewModel

    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null
    private val shortcutIntentState = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        installSplashScreen().setKeepOnScreenCondition { mainViewModel.isLoading.value }
        enableEdgeToEdge()
        mainViewModel.refreshReminders()
        shortcutIntentState.value = intent.takeIf(::isLauncherShortcutIntent)

        val showAppContents = mutableStateOf(false)
        if (settingsViewModel.getAppLockValue() && !mainViewModel.isAppUnlocked()) {
            setupBiometric(showAppContents)
        } else {
            showAppContents.value = true
        }
        setAppContents(showAppContents)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isLauncherShortcutIntent(intent)) shortcutIntentState.value = intent
    }

    override fun onResume() {
        super.onResume()
        updateShortcuts()
    }

    private fun setupBiometric(showAppContents: androidx.compose.runtime.MutableState<Boolean>) {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    showAppContents.value = true
                    mainViewModel.setAppUnlocked(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val manager = BiometricManager.from(this@MainActivity)
                    if (manager.canAuthenticate(Utils.getAuthenticators()) != BiometricManager.BIOMETRIC_SUCCESS) {
                        showAppContents.value = true
                        mainViewModel.setAppUnlocked(true)
                        settingsViewModel.setAppLock(false)
                        getString(R.string.app_lock_unable_to_authenticate).toToast(this@MainActivity)
                    } else {
                        showAppContents.value = false
                    }
                }
            }
        )
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bio_lock_title))
            .setSubtitle(getString(R.string.bio_lock_subtitle))
            .setAllowedAuthenticators(Utils.getAuthenticators())
            .build()
    }

    private fun setAppContents(showAppContents: State<Boolean>) {
        setContent {
            GreenStashTheme(settingsViewModel = settingsViewModel) {
                MainScreen(
                    activity = this,
                    showAppContents = showAppContents.value,
                    startDestination = mainViewModel.startDestination.value,
                    currentThemeMode = settingsViewModel.getCurrentTheme(),
                    shortcutIntent = shortcutIntentState.value,
                    onShortcutConsumed = { shortcutIntentState.value = null },
                    onAuthRequest = {
                        val prompt = biometricPrompt
                        val info = promptInfo
                        if (prompt != null && info != null) prompt.authenticate(info)
                    }
                )
            }
        }
    }

    private fun isLauncherShortcutIntent(intent: Intent): Boolean =
        intent.data?.scheme == MainViewModel.LAUNCHER_SHORTCUT_SCHEME

    private fun updateShortcuts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        val manager = getSystemService(ShortcutManager::class.java)
        mainViewModel.buildDynamicShortcuts(
            context = this,
            limit = manager.maxShortcutCountPerActivity,
            onComplete = { shortcuts ->
                runCatching { manager.dynamicShortcuts = shortcuts }
                    .onFailure { Log.e("MainActivity", "Failed to update shortcuts", it) }
            }
        )
    }
}
