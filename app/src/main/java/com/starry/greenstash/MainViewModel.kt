package com.starry.greenstash

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.other.WelcomeDataStore
import com.starry.greenstash.reminder.ReminderManager
import com.starry.greenstash.ui.navigation.BaseScreen
import com.starry.greenstash.ui.navigation.DrawerScreens
import com.starry.greenstash.ui.navigation.OtherScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val welcomeDataStore: WelcomeDataStore,
    private val goalDao: GoalDao,
    private val reminderManager: ReminderManager
) : ViewModel() {
    private var _appUnlocked = false
    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading
    private val _startDestination: MutableState<BaseScreen> = mutableStateOf(OtherScreens.WelcomeScreen)
    val startDestination: State<BaseScreen> = _startDestination

    companion object {
        const val LAUNCHER_SHORTCUT_SCHEME = "greenstash_lc_shortcut"
        const val LC_SHORTCUT_GOAL_ID = "lc_shortcut_goal_id"
        const val LC_SHORTCUT_NEW_GOAL = "lc_shortcut_new_goal"
    }

    init {
        viewModelScope.launch {
            welcomeDataStore.readOnBoardingState().collect { completed ->
                _startDestination.value = if (completed) DrawerScreens.Home else OtherScreens.WelcomeScreen
                delay(120)
                _isLoading.value = false
            }
        }
    }

    fun isAppUnlocked(): Boolean = _appUnlocked
    fun setAppUnlocked(value: Boolean) { _appUnlocked = value }

    fun refreshReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { reminderManager.checkAndScheduleReminders(goalDao.getAllGoals()) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun buildDynamicShortcuts(
        context: Context,
        limit: Int,
        onComplete: (List<ShortcutInfo>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val topGoals = goalDao.getAllGoals()
                .sortedByDescending { it.goal.priority.value }
                .take((limit - 1).coerceAtLeast(0))
                .map { it.goal }

            val newGoal = ShortcutInfo.Builder(context, "new_goal")
                .setShortLabel(context.getString(R.string.new_goal_fab))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_new_goal))
                .setIntent(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("$LAUNCHER_SHORTCUT_SCHEME://newGoal")
                    putExtra(LC_SHORTCUT_NEW_GOAL, true)
                })
                .build()

            val shortcuts = listOf(newGoal) + topGoals.map { goal ->
                ShortcutInfo.Builder(context, goal.goalId.toString())
                    .setShortLabel(goal.title)
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_widget_config_item))
                    .setIntent(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("$LAUNCHER_SHORTCUT_SCHEME://goalId")
                        putExtra(LC_SHORTCUT_GOAL_ID, goal.goalId)
                    })
                    .build()
            }
            withContext(Dispatchers.Main) { onComplete(shortcuts) }
        }
    }
}
