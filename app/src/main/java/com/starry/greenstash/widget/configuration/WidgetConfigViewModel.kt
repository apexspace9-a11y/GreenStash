package com.starry.greenstash.widget.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.database.widget.WidgetDao
import com.starry.greenstash.database.widget.WidgetData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val widgetDao: WidgetDao
) : ViewModel() {
    val allGoals: LiveData<List<GoalWithTransactions>> = goalDao.getAllGoalsAsLiveData()

    fun setWidgetData(
        widgetId: Int,
        goalId: Long,
        onComplete: (goalItem: GoalWithTransactions) -> Unit
    ) {
        if (widgetId <= 0 || goalId <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            val goalItem = goalDao.getGoalWithTransactionById(goalId) ?: return@launch
            widgetDao.insertWidgetData(WidgetData(appWidgetId = widgetId, goalId = goalId))
            withContext(Dispatchers.Main) { onComplete(goalItem) }
        }
    }
}
