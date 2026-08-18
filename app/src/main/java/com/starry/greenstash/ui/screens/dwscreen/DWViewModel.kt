package com.starry.greenstash.ui.screens.dwscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.database.transaction.Transaction
import com.starry.greenstash.database.transaction.TransactionDao
import com.starry.greenstash.database.transaction.TransactionType
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.utils.NumberUtils
import com.starry.greenstash.utils.PreferenceUtil
import com.starry.greenstash.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

data class DWScreenState(val amount: String = "", val notes: String = "")

@HiltViewModel
class DWViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val transactionDao: TransactionDao,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    var state by mutableStateOf(DWScreenState())

    fun getDateStyle(): DateStyle {
        val index = preferenceUtil.getInt(PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal)
        return DateStyle.entries.getOrElse(index) { DateStyle.DD_MM_YYYY }
    }

    fun convertTransactionType(type: String): TransactionType = when (type) {
        TransactionType.Deposit.name -> TransactionType.Deposit
        TransactionType.Withdraw.name -> TransactionType.Withdraw
        else -> TransactionType.Invalid
    }

    fun deposit(
        goalId: Long,
        dateTime: LocalDateTime,
        onGoalAchieved: () -> Any?,
        onComplete: () -> Any?,
        onFailure: () -> Any?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = goalDao.getGoalById(goalId)
            val amount = amountToDouble(state.amount)
            if (goal == null || amount == null) {
                withContext(Dispatchers.Main) { onFailure() }
                return@launch
            }
            addTransaction(goal.goalId, amount, state.notes, dateTime, TransactionType.Deposit)
            val goalItem = goalDao.getGoalWithTransactionById(goal.goalId)
            if (goalItem == null) {
                withContext(Dispatchers.Main) { onFailure() }
                return@launch
            }
            val remaining = goal.targetAmount - goalItem.getCurrentlySavedAmount()
            withContext(Dispatchers.Main) {
                if (remaining <= 0.0) onGoalAchieved() else onComplete()
            }
        }
    }

    fun withdraw(
        goalId: Long,
        dateTime: LocalDateTime,
        onWithDrawOverflow: () -> Any?,
        onComplete: () -> Any?,
        onFailure: () -> Any?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = goalDao.getGoalById(goalId)
            val amount = amountToDouble(state.amount)
            if (goal == null || amount == null) {
                withContext(Dispatchers.Main) { onFailure() }
                return@launch
            }
            val goalItem = goalDao.getGoalWithTransactionById(goal.goalId)
            if (goalItem == null) {
                withContext(Dispatchers.Main) { onFailure() }
                return@launch
            }
            if (amount > goalItem.getCurrentlySavedAmount()) {
                withContext(Dispatchers.Main) { onWithDrawOverflow() }
                return@launch
            }
            addTransaction(goal.goalId, amount, state.notes, dateTime, TransactionType.Withdraw)
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    private fun amountToDouble(amount: String): Double? = amount.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0.0 }
        ?.let(NumberUtils::roundDecimal)
        ?.takeIf { it > 0.0 }

    private suspend fun addTransaction(
        goalId: Long,
        amount: Double,
        notes: String,
        dateTime: LocalDateTime,
        transactionType: TransactionType
    ) {
        transactionDao.insertTransaction(
            Transaction(
                ownerGoalId = goalId,
                type = transactionType,
                timeStamp = Utils.getEpochTime(dateTime),
                amount = amount,
                notes = notes
            )
        )
    }
}
