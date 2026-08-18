package com.starry.greenstash.reminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.reminder.ReminderManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DateTimeChangeReceiver : BroadcastReceiver() {
    @Inject lateinit var goalDao: GoalDao
    @Inject lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent?) {
        val supported = intent?.action == Intent.ACTION_TIME_CHANGED ||
            intent?.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent?.action == Intent.ACTION_DATE_CHANGED
        if (!supported) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderManager.checkAndScheduleReminders(goalDao.getAllGoals())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
