package com.starry.greenstash.ui.screens.main

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.starry.greenstash.MainActivity
import com.starry.greenstash.MainViewModel
import com.starry.greenstash.ui.navigation.BaseScreen
import com.starry.greenstash.ui.navigation.NavGraph
import com.starry.greenstash.ui.navigation.OtherScreens
import com.starry.greenstash.ui.screens.other.AppLockedScreen
import com.starry.greenstash.ui.screens.settings.ThemeMode
import com.starry.greenstash.ui.theme.AdjustEdgeToEdge
import com.starry.greenstash.ui.theme.MocQuyBackground

@Composable
fun MainScreen(
    activity: MainActivity,
    showAppContents: Boolean,
    startDestination: BaseScreen,
    currentThemeMode: ThemeMode,
    shortcutIntent: Intent?,
    onShortcutConsumed: () -> Unit,
    onAuthRequest: () -> Unit,
) {
    AdjustEdgeToEdge(activity = activity, themeState = currentThemeMode)
    MocQuyBackground {
        val navController = rememberNavController()
        Crossfade(
            targetState = showAppContents,
            label = "AppLockCrossFade",
            animationSpec = tween(260)
        ) { visible ->
            if (visible) {
                NavGraph(navController = navController, startDestination = startDestination)
                LaunchedEffect(shortcutIntent) {
                    val intent = shortcutIntent ?: return@LaunchedEffect
                    if (intent.data?.scheme == MainViewModel.LAUNCHER_SHORTCUT_SCHEME) {
                        val goalId = intent.getLongExtra(MainViewModel.LC_SHORTCUT_GOAL_ID, -1L)
                        when {
                            goalId > 0L -> navController.navigate(
                                OtherScreens.GoalInfoScreen(goalId.toString())
                            )
                            intent.getBooleanExtra(MainViewModel.LC_SHORTCUT_NEW_GOAL, false) ->
                                navController.navigate(OtherScreens.InputScreen())
                        }
                    }
                    onShortcutConsumed()
                }
            } else {
                AppLockedScreen(onAuthRequest = onAuthRequest)
            }
        }
    }
}
