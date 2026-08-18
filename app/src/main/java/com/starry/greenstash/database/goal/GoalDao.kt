package com.starry.greenstash.database.goal

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.database.transaction.Transaction as GoalTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insertGoal(goal: Goal): Long

    @Transaction
    suspend fun insertGoalWithTransactions(goalsWithTransactions: List<GoalWithTransactions>) {
        goalsWithTransactions.forEach { item ->
            val restoredGoal = item.goal.copy().apply { goalId = 0L }
            val goalId = insertGoal(restoredGoal)
            val transactions = item.transactions.map {
                it.copy(ownerGoalId = goalId).apply { transactionId = 0L }
            }
            insertTransactions(transactions)
        }
    }

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("DELETE FROM saving_goal WHERE goalId = :goalId")
    suspend fun deleteGoal(goalId: Long)

    @Transaction
    @Query("SELECT * FROM saving_goal WHERE archived = 0")
    suspend fun getAllGoals(): List<GoalWithTransactions>

    @Transaction
    @Query("SELECT * FROM saving_goal")
    suspend fun getAllGoalsIncludingArchived(): List<GoalWithTransactions>

    @Transaction
    @Query("SELECT * FROM saving_goal WHERE archived = 0")
    fun getAllGoalsAsLiveData(): LiveData<List<GoalWithTransactions>>

    @Query("SELECT * FROM saving_goal WHERE goalId = :goalId")
    suspend fun getGoalById(goalId: Long): Goal?

    @Transaction
    @Query("SELECT * FROM saving_goal WHERE goalId = :goalId")
    suspend fun getGoalWithTransactionById(goalId: Long): GoalWithTransactions?

    @Transaction
    @Query("SELECT * FROM saving_goal WHERE goalId = :goalId")
    fun getGoalWithTransactionByIdAsFlow(goalId: Long): Flow<GoalWithTransactions?>

    @Transaction
    @Query(
        "SELECT * FROM saving_goal WHERE archived = 0 ORDER BY " +
            "CASE WHEN :sortOrder = 1 THEN title END ASC, " +
            "CASE WHEN :sortOrder = 2 THEN title END DESC"
    )
    fun getAllGoalsByTitle(sortOrder: Int): Flow<List<GoalWithTransactions>>

    @Transaction
    @Query(
        "SELECT * FROM saving_goal WHERE archived = 0 ORDER BY " +
            "CASE WHEN :sortOrder = 1 THEN targetAmount END ASC, " +
            "CASE WHEN :sortOrder = 2 THEN targetAmount END DESC"
    )
    fun getAllGoalsByAmount(sortOrder: Int): Flow<List<GoalWithTransactions>>

    @Transaction
    @Query(
        "SELECT * FROM saving_goal WHERE archived = 0 ORDER BY " +
            "CASE WHEN :sortOrder = 1 THEN priority END ASC, " +
            "CASE WHEN :sortOrder = 2 THEN priority END DESC"
    )
    fun getAllGoalsByPriority(sortOrder: Int): Flow<List<GoalWithTransactions>>

    @Transaction
    @Query("SELECT * FROM saving_goal WHERE archived = 1")
    fun getAllArchivedGoals(): Flow<List<GoalWithTransactions>>

    @Insert
    suspend fun insertTransactions(transactions: List<GoalTransaction>)
}
