package com.starry.greenstash.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.starry.greenstash.MainActivity
import com.starry.greenstash.R
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.reminder.receivers.ReminderDepositReceiver
import com.starry.greenstash.reminder.receivers.ReminderDismissReceiver
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.utils.GoalTextUtils
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.PreferenceUtil

class ReminderNotificationSender(
    private val context: Context,
    private val preferenceUtil: PreferenceUtil
) {
    companion object {
        const val REMINDER_CHANNEL_ID = "reminder_notification_channel"
        const val REMINDER_CHANNEL_NAME = "Mộc Quỹ"
        private const val INTENT_UNIQUE_CODE = 7546
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun sendNotification(goalItem: GoalWithTransactions) {
        val goal = goalItem.goal
        val frequency = context.getString(
            when (goal.priority) {
                GoalPriority.High -> R.string.reminder_frequency_daily
                GoalPriority.Normal -> R.string.reminder_frequency_semiweekly
                GoalPriority.Low -> R.string.reminder_frequency_weekly
            }
        )
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(context.getString(R.string.reminder_notification_title, frequency, goal.title))
            .setContentText(context.getString(R.string.reminder_notification_desc))
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(createActivityIntent())
            .setAutoCancel(true)

        val remainingAmount = goal.targetAmount - goalItem.getCurrentlySavedAmount()
        val currency = preferenceUtil.getString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND")
            .orEmpty().ifBlank { "VND" }
        val dateIndex = preferenceUtil.getInt(
            PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal
        )
        val dateStyle = DateStyle.entries.getOrElse(dateIndex) { DateStyle.DD_MM_YYYY }

        if (goal.deadline != 0L && remainingAmount.isFinite() && remainingAmount > 0.0) {
            val days = GoalTextUtils.calcRemainingDays(goal.deadline, dateStyle)
                .remainingDays.coerceAtLeast(1L)
            val periods = when (goal.priority) {
                GoalPriority.High -> days
                GoalPriority.Normal -> (days / 4L).coerceAtLeast(1L)
                GoalPriority.Low -> (days / 7L).coerceAtLeast(1L)
            }
            val amount = remainingAmount / periods.toDouble()
            if (amount.isFinite() && amount > 0.0) {
                val rounded = NumberUtils.roundDecimal(amount)
                notification.addAction(
                    R.drawable.ic_notification_deposit,
                    "${context.getString(R.string.deposit_button)} ${NumberUtils.formatCurrency(rounded, currency)}",
                    createDepositIntent(goal.goalId, rounded)
                )
            }
        }

        notification.addAction(
            R.drawable.ic_notification_dismiss,
            context.getString(R.string.dismiss_notification_button),
            createDismissIntent(goal.goalId)
        )
        notificationManager.notify(goal.goalId.toInt(), notification.build())
    }

    fun updateWithDepositNotification(goalId: Long, amount: Double) {
        val currency = preferenceUtil.getString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND")
            .orEmpty().ifBlank { "VND" }
        val safeAmount = NumberUtils.roundDecimal(amount)
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(context.getString(R.string.notification_deposited_title))
            .setContentText(
                context.getString(R.string.notification_deposited_desc)
                    .format(NumberUtils.formatCurrency(safeAmount, currency))
            )
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(createActivityIntent())
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification_dismiss,
                context.getString(R.string.dismiss_notification_button),
                createDismissIntent(goalId)
            )
        notificationManager.notify(goalId.toInt(), notification.build())
    }

    fun dismissNotification(goalId: Long) = notificationManager.cancel(goalId.toInt())

    private fun createDepositIntent(goalId: Long, amount: Double): PendingIntent {
        val intent = Intent(context, ReminderDepositReceiver::class.java).apply {
            putExtra(ReminderDepositReceiver.REMINDER_GOAL_ID, goalId)
            putExtra(ReminderDepositReceiver.REMINDER_DEPOSIT_AMOUNT, amount)
        }
        return PendingIntent.getBroadcast(
            context,
            goalId.toInt() + INTENT_UNIQUE_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDismissIntent(goalId: Long): PendingIntent {
        val intent = Intent(context, ReminderDismissReceiver::class.java).apply {
            putExtra(ReminderDismissReceiver.REMINDER_GOAL_ID, goalId)
        }
        return PendingIntent.getBroadcast(
            context,
            goalId.toInt() + INTENT_UNIQUE_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActivityIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
