package com.starry.greenstash.backup

import android.graphics.Bitmap
import androidx.annotation.Keep
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.database.core.parseOldDeadlineToMillis
import com.starry.greenstash.database.goal.Goal
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.database.transaction.Transaction
import com.starry.greenstash.database.transaction.TransactionType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GoalToJSONConverter {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    companion object { const val BACKUP_SCHEMA_VERSION = 2 }

    @Keep
    @Serializable
    data class BackupJsonModel(
        val version: Int = BACKUP_SCHEMA_VERSION,
        val timestamp: Long,
        val data: List<GoalWithTransactions>
    )

    @Keep
    @Serializable
    data class BackupJsonModelV1(
        val version: Int = 1,
        val timestamp: Long,
        val data: List<GoalWithTransactionsV1>
    )

    @Keep
    @Serializable
    data class GoalWithTransactionsV1(
        val goal: GoalV1,
        val transactions: List<Transaction>
    )

    @Keep
    @Serializable
    data class GoalV1(
        val title: String,
        val targetAmount: Double,
        val deadline: String,
        @Serializable(with = BitmapSerializer::class)
        val goalImage: Bitmap? = null,
        val additionalNotes: String = "",
        val priority: GoalPriority = GoalPriority.Normal,
        val reminder: Boolean = false,
        val goalIconId: String? = "Image",
        val archived: Boolean = false,
        val goalId: Long = 0L
    )

    fun convertToJson(data: List<GoalWithTransactions>): String = json.encodeToString(
        BackupJsonModel.serializer(),
        BackupJsonModel(timestamp = System.currentTimeMillis(), data = data)
    )

    fun convertFromJson(jsonString: String): BackupJsonModel {
        val element = json.parseToJsonElement(jsonString)
        val version = element.jsonObject["version"]?.jsonPrimitive?.intOrNull
            ?: error("Missing backup schema version")
        val model = when (version) {
            1 -> json.decodeFromString(BackupJsonModelV1.serializer(), jsonString).toCurrentModel()
            BACKUP_SCHEMA_VERSION -> json.decodeFromString(BackupJsonModel.serializer(), jsonString)
            else -> error("Unsupported backup schema version: $version")
        }
        validate(model)
        return model
    }

    private fun BackupJsonModelV1.toCurrentModel(): BackupJsonModel = BackupJsonModel(
        timestamp = timestamp,
        data = data.map { old ->
            val g = old.goal
            GoalWithTransactions(
                goal = Goal(
                    title = g.title,
                    targetAmount = g.targetAmount,
                    deadline = parseOldDeadlineToMillis(g.deadline),
                    goalImage = g.goalImage,
                    additionalNotes = g.additionalNotes,
                    priority = g.priority,
                    reminder = g.reminder,
                    goalIconId = g.goalIconId,
                    archived = g.archived
                ).apply { goalId = g.goalId },
                transactions = old.transactions
            )
        }
    )

    private fun validate(model: BackupJsonModel) {
        require(model.timestamp >= 0L) { "Invalid backup timestamp" }
        model.data.forEach { item ->
            require(item.goal.targetAmount.isFinite() && item.goal.targetAmount > 0.0) {
                "Invalid target amount"
            }
            require(item.goal.deadline >= 0L) { "Invalid deadline" }
            item.transactions.forEach { transaction ->
                require(transaction.amount.isFinite() && transaction.amount >= 0.0) {
                    "Invalid transaction amount"
                }
                require(transaction.type != TransactionType.Invalid) { "Invalid transaction type" }
                require(transaction.timeStamp >= 0L) { "Invalid transaction timestamp" }
            }
        }
    }
}
