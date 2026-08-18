package com.starry.greenstash.ui.screens.input

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.R
import com.starry.greenstash.database.goal.Goal
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.reminder.ReminderManager
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.ui.screens.settings.dateStyleToDisplayFormat
import com.starry.greenstash.utils.ImageUtils
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class IconItem(var id: String = "", var name: String = "", var image: ImageVector? = null)
data class IconsState(
    val searchText: String = "",
    val icons: List<List<IconItem>> = emptyList(),
    val currentIcon: IconItem? = null,
    val selectedIcon: IconItem? = null,
    val isLoading: Boolean = true
)
data class InputScreenState(
    val goalImageUri: Uri? = null,
    val goalTitleText: String = "",
    val targetAmount: String = "",
    val deadline: Long = 0L,
    val additionalNotes: String = "",
    val priority: String = GoalPriority.Normal.name,
    val reminder: Boolean = false
)

@HiltViewModel
class InputViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val reminderManager: ReminderManager,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    var state by mutableStateOf(InputScreenState())
    private val _iconState = mutableStateOf(IconsState())
    val iconState: State<IconsState> = _iconState
    private var iconSearchJob: Job? = null
    private val _showOnboardingTapTargets: MutableState<Boolean> = mutableStateOf(
        preferenceUtil.getBoolean(PreferenceUtil.INPUT_SCREEN_ONBOARDING_BOOL, true)
    )
    val showOnboardingTapTargets: State<Boolean> = _showOnboardingTapTargets

    fun addSavingGoal(context: Context, onComplete: () -> Any?, onFailure: () -> Any?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val amount = parseAmount(state.targetAmount) ?: error("Invalid amount")
                val image = state.goalImageUri?.let { ImageUtils.uriToBitmap(it, context, 1024) }
                val priority = GoalPriority.entries.firstOrNull { it.name == state.priority }
                    ?: GoalPriority.Normal
                val goal = Goal(
                    title = state.goalTitleText.trim(),
                    targetAmount = amount,
                    deadline = state.deadline.coerceAtLeast(0L),
                    goalImage = image,
                    additionalNotes = state.additionalNotes,
                    priority = priority,
                    reminder = state.reminder,
                    goalIconId = iconState.value.selectedIcon?.id
                )
                val goalId = goalDao.insertGoal(goal)
                if (goal.reminder) reminderManager.scheduleReminder(goalId)
            }.fold(
                onSuccess = { withContext(Dispatchers.Main) { onComplete() } },
                onFailure = { withContext(Dispatchers.Main) { onFailure() } }
            )
        }
    }

    fun setEditGoalData(
        goalId: Long,
        onEditDataSet: (goalImage: Bitmap?, goalIconId: String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = goalDao.getGoalById(goalId)
            withContext(Dispatchers.Main) {
                if (goal == null) {
                    onEditDataSet(null, null)
                    return@withContext
                }
                state = state.copy(
                    goalTitleText = goal.title,
                    targetAmount = goal.targetAmount.toString(),
                    deadline = goal.deadline,
                    additionalNotes = goal.additionalNotes,
                    priority = goal.priority.name,
                    reminder = goal.reminder
                )
                onEditDataSet(goal.goalImage, goal.goalIconId)
            }
        }
    }

    fun editSavingGoal(
        goalId: Long,
        context: Context,
        onComplete: () -> Any?,
        onFailure: () -> Any?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val oldGoal = goalDao.getGoalById(goalId) ?: error("Goal not found")
                val amount = parseAmount(state.targetAmount) ?: error("Invalid amount")
                val image = state.goalImageUri?.let { ImageUtils.uriToBitmap(it, context, 1024) }
                    ?: oldGoal.goalImage
                val priority = GoalPriority.entries.firstOrNull { it.name == state.priority }
                    ?: GoalPriority.Normal
                val updated = Goal(
                    title = state.goalTitleText.trim(),
                    targetAmount = amount,
                    deadline = state.deadline.coerceAtLeast(0L),
                    goalImage = image,
                    additionalNotes = state.additionalNotes,
                    priority = priority,
                    reminder = state.reminder,
                    goalIconId = iconState.value.selectedIcon?.id ?: oldGoal.goalIconId
                ).apply { this.goalId = oldGoal.goalId }
                goalDao.updateGoal(updated)
                if (updated.reminder) {
                    if (!reminderManager.isReminderSet(goalId)) reminderManager.scheduleReminder(goalId)
                } else {
                    reminderManager.stopReminder(goalId)
                }
            }.fold(
                onSuccess = { withContext(Dispatchers.Main) { onComplete() } },
                onFailure = { withContext(Dispatchers.Main) { onFailure() } }
            )
        }
    }

    fun updatePriority(priority: String) {
        state = state.copy(
            priority = GoalPriority.entries.firstOrNull { it.name == priority }?.name
                ?: GoalPriority.Normal.name
        )
    }
    fun updateReminder(reminder: Boolean) { state = state.copy(reminder = reminder) }
    fun updateTitle(title: String) { state = state.copy(goalTitleText = title) }
    fun updateTargetAmount(amount: String) { state = state.copy(targetAmount = amount) }
    fun removeDeadLine() { state = state.copy(deadline = 0L) }
    fun updateAdditionalNotes(notes: String) { state = state.copy(additionalNotes = notes) }

    fun getDateStyleFormat(): String {
        val index = preferenceUtil.getInt(PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal)
        return dateStyleToDisplayFormat(DateStyle.entries.getOrElse(index) { DateStyle.DD_MM_YYYY })
    }

    fun updateIconSearch(context: Context, search: String) {
        _iconState.value = _iconState.value.copy(searchText = search)
        iconSearchJob?.cancel()
        iconSearchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(400)
            withContext(Dispatchers.Main) {
                _iconState.value = _iconState.value.copy(isLoading = true)
            }
            val icons = getNamesIcons(context)
                .filter { it.contains(search, ignoreCase = true) }
                .take(50)
                .mapNotNull(::parseIconItem)
                .chunked(3)
            withContext(Dispatchers.Main) {
                _iconState.value = _iconState.value.copy(icons = icons, isLoading = false)
            }
        }
    }

    fun updateCurrentIcon(icon: IconItem) {
        _iconState.value = _iconState.value.copy(currentIcon = icon)
    }
    fun updateSelectedIcon(icon: IconItem) {
        _iconState.value = _iconState.value.copy(selectedIcon = icon)
    }

    private fun parseIconItem(line: String): IconItem? {
        val split = line.split(",", limit = 2)
        if (split.size != 2 || split[0].isBlank()) return null
        return IconItem(split[0], split[1], ImageUtils.createIconVector(split[0]))
    }
    private fun getNamesIcons(context: Context): List<String> =
        context.resources.openRawResource(R.raw.icons_names).bufferedReader().use { it.readLines() }
    private fun parseAmount(value: String): Double? = value.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0.0 }
        ?.let(NumberUtils::roundDecimal)
        ?.takeIf { it > 0.0 }

    fun onboardingTapTargetsShown() {
        preferenceUtil.putBoolean(PreferenceUtil.INPUT_SCREEN_ONBOARDING_BOOL, false)
        _showOnboardingTapTargets.value = false
    }
    fun shouldShowRemoveDeadlineTip(): Boolean = state.deadline != 0L && preferenceUtil.getBoolean(
        PreferenceUtil.INPUT_REMOVE_DEADLINE_TIP_BOOL, true
    )
    fun removeDeadlineTipShown() {
        preferenceUtil.putBoolean(PreferenceUtil.INPUT_REMOVE_DEADLINE_TIP_BOOL, false)
    }
}
