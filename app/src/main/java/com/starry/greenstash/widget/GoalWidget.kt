package com.starry.greenstash.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.starry.greenstash.R
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.utils.GoalTextUtils
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.EntryPoints

private const val WIDGET_MANUAL_REFRESH = "widget_manual_refresh"
private const val FULL_WIDGET_MIN_HEIGHT = 110

class GoalWidget : AppWidgetProvider() {
    private lateinit var viewModel: WidgetViewModel

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        initialiseVm(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        initialiseVm(context)
        appWidgetIds.forEach { id ->
            val options = appWidgetManager.getAppWidgetOptions(id)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            viewModel.getGoalFromWidgetId(id) { updateWidgetContents(context, id, it, minHeight) }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == Intent.ACTION_SCREEN_ON) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, GoalWidget::class.java))
            if (ids.isNotEmpty()) onUpdate(context, manager, ids)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val minHeight = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0) ?: 0
        initialiseVm(context)
        viewModel.getGoalFromWidgetId(appWidgetId) {
            updateWidgetContents(context, appWidgetId, it, minHeight)
        }
    }

    fun updateWidgetContents(
        context: Context,
        appWidgetId: Int,
        goalItem: GoalWithTransactions,
        minHeight: Int? = null
    ) {
        val preferences = PreferenceUtil(context)
        val manager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.goal_widget)
        views.setCharSequence(R.id.widgetTitle, "setText", goalItem.goal.title)

        val currency = preferences.getString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND")
            .orEmpty().ifBlank { "VND" }
        val dateIndex = preferences.getInt(
            PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal
        )
        val dateStyle = DateStyle.entries.getOrElse(dateIndex) { DateStyle.DD_MM_YYYY }

        val saved = "${NumberUtils.getCurrencySymbol(currency)}${NumberUtils.prettyCount(goalItem.getCurrentlySavedAmount())}"
        val target = "${NumberUtils.getCurrencySymbol(currency)}${NumberUtils.prettyCount(goalItem.goal.targetAmount)}"
        views.setCharSequence(
            R.id.widgetDesc,
            "setText",
            context.getString(R.string.goal_widget_desc).format("$saved / $target")
        )

        handleSavingsPerDuration(context, views, goalItem, currency, dateStyle, minHeight)
        handleGoalAchieved(views, goalItem, minHeight)
        handleProgress(views, goalItem)

        val intent = Intent(context, GoalWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            type = WIDGET_MANUAL_REFRESH
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                manager.getAppWidgetIds(ComponentName(context, GoalWidget::class.java))
            )
        }
        views.setOnClickPendingIntent(
            R.id.widgetLayout,
            PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun handleSavingsPerDuration(
        context: Context,
        views: RemoteViews,
        goalItem: GoalWithTransactions,
        currency: String,
        dateStyle: DateStyle,
        minHeight: Int?
    ) {
        views.setViewVisibility(R.id.widgetAmountDay, View.GONE)
        views.setViewVisibility(R.id.widgetAmountWeek, View.GONE)
        views.setViewVisibility(R.id.amountDurationGroup, View.GONE)

        val remaining = goalItem.goal.targetAmount - goalItem.getCurrentlySavedAmount()
        if (!remaining.isFinite() || remaining <= 0.0 || goalItem.goal.deadline == 0L) return
        val days = GoalTextUtils.calcRemainingDays(goalItem.goal.deadline, dateStyle).remainingDays
        if (days <= 2L) return

        val localeEnglish = context.resources.configuration.locales[0].language == "en"
        val dayAmount = NumberUtils.roundDecimal(remaining / days.toDouble())
        val daySuffix = context.getString(R.string.goal_approx_saving_day).let {
            if (localeEnglish && it.isNotEmpty()) it.dropLast(1) else it
        }
        views.setCharSequence(
            R.id.widgetAmountDay,
            "setText",
            "${NumberUtils.getCurrencySymbol(currency)}${NumberUtils.prettyCount(dayAmount)}/$daySuffix"
        )
        views.setViewVisibility(R.id.widgetAmountDay, View.VISIBLE)

        if (days > 7L) {
            val weeks = (days / 7L).coerceAtLeast(1L)
            val weekAmount = NumberUtils.roundDecimal(remaining / weeks.toDouble())
            val weekSuffix = context.getString(R.string.goal_approx_saving_week).let {
                if (localeEnglish && it.isNotEmpty()) it.dropLast(1) else it
            }
            views.setCharSequence(
                R.id.widgetAmountWeek,
                "setText",
                "${NumberUtils.getCurrencySymbol(currency)}${NumberUtils.prettyCount(weekAmount)}/$weekSuffix"
            )
            views.setViewVisibility(R.id.widgetAmountWeek, View.VISIBLE)
        }

        if (minHeight == null || minHeight >= FULL_WIDGET_MIN_HEIGHT) {
            views.setViewVisibility(R.id.amountDurationGroup, View.VISIBLE)
        }
        views.setViewVisibility(R.id.widgetGoalAchieved, View.GONE)
    }

    private fun handleGoalAchieved(
        views: RemoteViews,
        goalItem: GoalWithTransactions,
        minHeight: Int?
    ) {
        val achieved = goalItem.goal.targetAmount > 0.0 &&
            goalItem.getCurrentlySavedAmount() >= goalItem.goal.targetAmount
        if (achieved) {
            views.setViewVisibility(R.id.amountDurationGroup, View.GONE)
            views.setViewVisibility(
                R.id.widgetGoalAchieved,
                if (minHeight != null && minHeight < FULL_WIDGET_MIN_HEIGHT) View.GONE else View.VISIBLE
            )
        } else {
            views.setViewVisibility(R.id.widgetGoalAchieved, View.GONE)
        }
    }

    private fun handleProgress(views: RemoteViews, goalItem: GoalWithTransactions) {
        val target = goalItem.goal.targetAmount
        val saved = goalItem.getCurrentlySavedAmount()
        val progress = if (target.isFinite() && target > 0.0 && saved.isFinite()) {
            ((saved / target) * 100.0).toInt().coerceIn(0, 100)
        } else 0
        views.setProgressBar(R.id.widgetGoalProgress, 100, progress, false)
    }

    private fun initialiseVm(context: Context) {
        if (!this::viewModel.isInitialized) {
            viewModel = EntryPoints.get(context.applicationContext, WidgetEntryPoint::class.java)
                .getViewModel()
        }
    }
}
