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
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var goalDao: GoalDao
    @Inject lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
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
