package com.starry.greenstash.reminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.database.transaction.Transaction
import com.starry.greenstash.database.transaction.TransactionDao
import com.starry.greenstash.database.transaction.TransactionType
import com.starry.greenstash.reminder.ReminderNotificationSender
import com.starry.greenstash.utils.NumberUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderDepositReceiver : BroadcastReceiver() {
    companion object {
        const val REMINDER_GOAL_ID = "reminder_deposit_goal_id"
        const val REMINDER_DEPOSIT_AMOUNT = "reminder_deposit_amount"
    }

    @Inject lateinit var goalDao: GoalDao
    @Inject lateinit var transactionDao: TransactionDao
    @Inject lateinit var reminderNotificationSender: ReminderNotificationSender

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderDepositReceiver", "Received deposit action")
        val pendingResult = goAsync()
        val goalId = intent.getLongExtra(REMINDER_GOAL_ID, 0L)
        val requestedAmount = intent.getDoubleExtra(REMINDER_DEPOSIT_AMOUNT, 0.0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (goalId <= 0L || !requestedAmount.isFinite() || requestedAmount <= 0.0) return@launch
                val item = goalDao.getGoalWithTransactionById(goalId) ?: return@launch
                val remaining = (item.goal.targetAmount - item.getCurrentlySavedAmount())
                    .takeIf { it.isFinite() }?.coerceAtLeast(0.0) ?: return@launch
                val amount = NumberUtils.roundDecimal(minOf(requestedAmount, remaining))
                if (amount <= 0.0) return@launch

                transactionDao.insertTransaction(
                    Transaction(
                        ownerGoalId = item.goal.goalId,
                        type = TransactionType.Deposit,
                        timeStamp = System.currentTimeMillis(),
                        amount = amount,
                        notes = ""
                    )
                )
                reminderNotificationSender.updateWithDepositNotification(item.goal.goalId, amount)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
