package com.starry.greenstash.ui.screens.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.database.core.GoalWithTransactions
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class InfoScreenState(val goalData: Flow<GoalWithTransactions?>? = null)
data class EditTransactionState(val amount: String = "", val notes: String = "")

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val transactionDao: TransactionDao,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {
    var state by mutableStateOf(InfoScreenState())
    var editTransactionState by mutableStateOf(EditTransactionState())

    fun loadGoalData(goalId: Long) {
        state = state.copy(goalData = goalDao.getGoalWithTransactionByIdAsFlow(goalId))
    }

    fun setEditTransactionState(transaction: Transaction) {
        editTransactionState = EditTransactionState(
            amount = transaction.amount.toString(),
            notes = transaction.notes
        )
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) { transactionDao.deleteTransaction(transaction) }
    }

    fun updateTransaction(
        transaction: Transaction,
        transactionTime: LocalDateTime,
        transactionType: TransactionType
    ) {
        val amount = parsedEditAmount() ?: return
        if (transactionType == TransactionType.Invalid) return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = transaction.copy(
                type = transactionType,
                timeStamp = Utils.getEpochTime(transactionTime),
                amount = amount,
                notes = editTransactionState.notes
            ).apply { transactionId = transaction.transactionId }
            transactionDao.updateTransaction(updated)
        }
    }

    fun duplicateTransaction(transaction: Transaction, transactionType: TransactionType) {
        val amount = parsedEditAmount() ?: return
        if (transactionType == TransactionType.Invalid) return
        viewModelScope.launch(Dispatchers.IO) {
            val duplicate = transaction.copy(
                type = transactionType,
                timeStamp = Utils.getEpochTime(LocalDateTime.now()),
                amount = amount,
                notes = editTransactionState.notes
            ).apply { transactionId = 0L }
            transactionDao.insertTransaction(duplicate)
        }
    }

    fun getDefaultCurrencyValue(): String =
        preferenceUtil.getString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND").orEmpty()
            .ifBlank { "VND" }

    fun getDateStyle(): DateStyle {
        val index = preferenceUtil.getInt(PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal)
        return DateStyle.entries.getOrElse(index) { DateStyle.DD_MM_YYYY }
    }

    fun shouldShowTransactionTip() = preferenceUtil.getBoolean(
        PreferenceUtil.INFO_TRANSACTION_SWIPE_TIP_BOOL, true
    )

    fun transactionTipDismissed() = preferenceUtil.putBoolean(
        PreferenceUtil.INFO_TRANSACTION_SWIPE_TIP_BOOL, false
    )

    private fun parsedEditAmount(): Double? = editTransactionState.amount.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0.0 }
        ?.let(NumberUtils::roundDecimal)
        ?.takeIf { it > 0.0 }
}
