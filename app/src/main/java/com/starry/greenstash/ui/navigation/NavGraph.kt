package com.starry.greenstash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.starry.greenstash.ui.screens.archive.composables.ArchiveScreen
import com.starry.greenstash.ui.screens.backups.composables.BackupScreen
import com.starry.greenstash.ui.screens.dwscreen.composables.DWScreen
import com.starry.greenstash.ui.screens.home.composables.HomeScreen
import com.starry.greenstash.ui.screens.info.composables.GoalInfoScreen
import com.starry.greenstash.ui.screens.input.composables.InputScreen
import com.starry.greenstash.ui.screens.other.CongratsScreen
import com.starry.greenstash.ui.screens.settings.composables.AboutScreen
import com.starry.greenstash.ui.screens.settings.composables.GoalCardStyle
import com.starry.greenstash.ui.screens.settings.composables.OSLScreen
import com.starry.greenstash.ui.screens.settings.composables.SettingsScreen
import com.starry.greenstash.ui.screens.welcome.composables.WelcomeScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: BaseScreen) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable<OtherScreens.WelcomeScreen>(exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }) { WelcomeScreen(navController) }
        composable<DrawerScreens.Home>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { HomeScreen(navController) }
        composable<OtherScreens.DWScreen>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { entry ->
            val args = entry.toRoute<OtherScreens.DWScreen>()
            DWScreen(args.goalId, args.transactionType, navController)
        }
        composable<OtherScreens.GoalInfoScreen>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { entry ->
            val args = entry.toRoute<OtherScreens.GoalInfoScreen>()
            GoalInfoScreen(args.goalId, navController)
        }
        composable<OtherScreens.InputScreen>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { entry ->
            val args = entry.toRoute<OtherScreens.InputScreen>()
            InputScreen(args.goalId, navController)
        }
        composable<OtherScreens.CongratsScreen>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { CongratsScreen(navController) }
        composable<DrawerScreens.Archive>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { ArchiveScreen(navController) }
        composable<DrawerScreens.Backups>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { BackupScreen(navController) }
        composable<DrawerScreens.Settings>(enterTransition = { enterTransition() }, exitTransition = { exitTransition() }, popEnterTransition = { popEnterTransition() }, popExitTransition = { popExitTransition() }) { SettingsScreen(navController) }
        composable<OtherScreens.GoalCardStyleScreen>(enterTransition = { enterTransition() }, popExitTransition = { popExitTransition() }) { GoalCardStyle(navController) }
        composable<OtherScreens.OSLScreen>(enterTransition = { enterTransition() }, popExitTransition = { popExitTransition() }) { OSLScreen(navController) }
        composable<OtherScreens.AboutScreen>(enterTransition = { enterTransition() }, popExitTransition = { popExitTransition() }) { AboutScreen(navController) }
    }
}
