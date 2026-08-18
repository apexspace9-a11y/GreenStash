package com.starry.greenstash.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.database.goal.Goal
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.reminder.ReminderManager
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val reminderManager: ReminderManager,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    val archivedGoals = goalDao.getAllArchivedGoals()

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal.goalId)
            if (reminderManager.isReminderSet(goal.goalId)) {
                reminderManager.stopReminder(goal.goalId)
            }
        }
    }

    fun restoreGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            val restoredGoal = goal.copy(archived = false)
            restoredGoal.goalId = goal.goalId
            goalDao.updateGoal(restoredGoal)
            if (goal.reminder) {
                reminderManager.scheduleReminder(goal.goalId)
            }
        }
    }

    fun getDefaultCurrency(): String {
        return preferenceUtil.getString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND")
            .orEmpty()
            .ifBlank { "VND" }
    }
}
