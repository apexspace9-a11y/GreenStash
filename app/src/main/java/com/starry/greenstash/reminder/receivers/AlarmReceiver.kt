package com.starry.greenstash.reminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.reminder.ReminderManager
import com.starry.greenstash.reminder.ReminderNotificationSender
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var goalDao: GoalDao
    @Inject lateinit var reminderManager: ReminderManager
    @Inject lateinit var reminderNotificationSender: ReminderNotificationSender

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Received alarm at ${LocalDateTime.now()}")
        val pendingResult = goAsync()
        val goalId = intent.getLongExtra(ReminderManager.INTENT_EXTRA_GOAL_ID, 0L)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (goalId <= 0L) return@launch
                val goalItem = goalDao.getGoalWithTransactionById(goalId) ?: return@launch
                reminderManager.scheduleReminder(goalItem.goal.goalId)
                val remaining = goalItem.goal.targetAmount - goalItem.getCurrentlySavedAmount()
                if (!remaining.isFinite() || remaining <= 0.0) return@launch
                val day = LocalDate.now().dayOfWeek
                val shouldNotify = when (goalItem.goal.priority) {
                    GoalPriority.High -> true
                    GoalPriority.Normal -> day == DayOfWeek.MONDAY || day == DayOfWeek.FRIDAY
                    GoalPriority.Low -> day == DayOfWeek.SUNDAY
                }
                if (shouldNotify) reminderNotificationSender.sendNotification(goalItem)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
