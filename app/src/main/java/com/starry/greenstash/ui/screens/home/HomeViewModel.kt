package com.starry.greenstash.ui.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.database.goal.Goal
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.reminder.ReminderManager
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchBarState { OPENED, CLOSED }
enum class FilterField { Title, Amount, Priority }
enum class FilterSortType(val value: Int) { Ascending(1), Descending(2) }
enum class GoalCardStyle { Classic, Compact }
data class FilterFlowData(val filterField: FilterField, val sortType: FilterSortType)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val reminderManager: ReminderManager,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    private val initialFilter = FilterFlowData(
        filterField = FilterField.entries.getOrElse(
            preferenceUtil.getInt(PreferenceUtil.GOAL_FILTER_FIELD_INT, FilterField.Title.ordinal)
        ) { FilterField.Title },
        sortType = FilterSortType.entries.getOrElse(
            preferenceUtil.getInt(
                PreferenceUtil.GOAL_FILTER_SORT_TYPE_INT,
                FilterSortType.Ascending.ordinal
            )
        ) { FilterSortType.Ascending }
    )

    private val _filterFlowData: MutableState<FilterFlowData> = mutableStateOf(initialFilter)
    val filterFlowData: State<FilterFlowData> = _filterFlowData
    private val filterFlow = MutableStateFlow(initialFilter)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val goalsListFlow = filterFlow.flatMapLatest { data ->
        preferenceUtil.putInt(PreferenceUtil.GOAL_FILTER_FIELD_INT, data.filterField.ordinal)
        preferenceUtil.putInt(PreferenceUtil.GOAL_FILTER_SORT_TYPE_INT, data.sortType.ordinal)
        when (data.filterField) {
            FilterField.Title -> goalDao.getAllGoalsByTitle(data.sortType.value)
            FilterField.Amount -> goalDao.getAllGoalsByAmount(data.sortType.value)
            FilterField.Priority -> goalDao.getAllGoalsByPriority(data.sortType.value)
        }
    }
    val goalsList = goalsListFlow.asLiveData()

    private val _searchBarState = mutableStateOf(SearchBarState.CLOSED)
    val searchBarState: State<SearchBarState> = _searchBarState
    private val _searchTextState = mutableStateOf("")
    val searchTextState: State<String> = _searchTextState

    private val _showOnboardingTapTargets = mutableStateOf(
        preferenceUtil.getBoolean(PreferenceUtil.HOME_SCREEN_ONBOARDING_BOOL, true)
    )
    val showOnboardingTapTargets: State<Boolean> = _showOnboardingTapTargets

    fun updateSearchWidgetState(newValue: SearchBarState) { _searchBarState.value = newValue }
    fun updateSearchTextState(newValue: String) { _searchTextState.value = newValue }

    fun updateFilterField(filterField: FilterField) {
        val updated = filterFlow.value.copy(filterField = filterField)
        filterFlow.value = updated
        _filterFlowData.value = updated
    }

    fun updateFilterSort(filterSortType: FilterSortType) {
        val updated = filterFlow.value.copy(sortType = filterSortType)
        filterFlow.value = updated
        _filterFlowData.value = updated
    }

    fun archiveGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedGoal = goal.copy(archived = true).apply { goalId = goal.goalId }
            goalDao.updateGoal(updatedGoal)
            if (reminderManager.isReminderSet(goal.goalId)) reminderManager.stopReminder(goal.goalId)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal.goalId)
            if (reminderManager.isReminderSet(goal.goalId)) reminderManager.stopReminder(goal.goalId)
        }
    }

    fun getDefaultCurrency(): String =
        preferenceUtil.getString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND").orEmpty()
            .ifBlank { "VND" }

    fun getDateStyle(): DateStyle {
        val index = preferenceUtil.getInt(PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal)
        return DateStyle.entries.getOrElse(index) { DateStyle.DD_MM_YYYY }
    }

    fun onboardingTapTargetsShown() {
        preferenceUtil.putBoolean(PreferenceUtil.HOME_SCREEN_ONBOARDING_BOOL, false)
        _showOnboardingTapTargets.value = false
    }
}
