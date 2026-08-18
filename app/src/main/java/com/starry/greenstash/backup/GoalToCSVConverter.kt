package com.starry.greenstash.backup

import androidx.annotation.Keep
import com.starry.greenstash.database.core.GoalWithTransactions
import com.starry.greenstash.database.core.parseOldDeadlineToMillis
import com.starry.greenstash.database.goal.Goal
import com.starry.greenstash.database.goal.GoalPriority
import com.starry.greenstash.database.transaction.Transaction
import com.starry.greenstash.database.transaction.TransactionType

class GoalToCSVConverter {
    companion object {
        const val BACKUP_SCHEMA_VERSION = 2
        const val CSV_DELIMITER = ","
        private const val COLUMN_COUNT = 14
    }

    @Keep
    data class BackupCSVModel(
        val version: Int = BACKUP_SCHEMA_VERSION,
        val timestamp: Long,
        val data: List<GoalWithTransactions>
    )

    fun convertToCSV(goalWithTransactions: List<GoalWithTransactions>): String = buildString {
        appendRecord(listOf("Schema Version", BACKUP_SCHEMA_VERSION))
        appendRecord(listOf("Timestamp", System.currentTimeMillis()))
        appendRecord(
            listOf(
                "Goal ID", "Title", "Target Amount", "Deadline", "Priority", "Reminder",
                "Goal Icon ID", "Archived", "Additional Notes", "Transaction ID", "Type",
                "Timestamp", "Amount", "Notes"
            )
        )

        goalWithTransactions.forEach { item ->
            val goal = item.goal
            val transactions = item.transactions.ifEmpty { listOf(null) }
            transactions.forEach { transaction ->
                appendRecord(
                    listOf(
                        goal.goalId,
                        goal.title,
                        goal.targetAmount,
                        goal.deadline,
                        goal.priority.name,
                        goal.reminder,
                        goal.goalIconId.orEmpty(),
                        goal.archived,
                        goal.additionalNotes,
                        transaction?.transactionId ?: "",
                        transaction?.type?.name ?: "",
                        transaction?.timeStamp ?: "",
                        transaction?.amount ?: "",
                        transaction?.notes ?: ""
                    )
                )
            }
        }
    }

    fun convertFromCSV(csv: String): BackupCSVModel {
        val records = parseRecords(csv)
        require(records.size >= 3) { "Backup CSV is incomplete" }
        require(records[0].getOrNull(0) == "Schema Version") { "Missing schema version" }
        require(records[1].getOrNull(0) == "Timestamp") { "Missing timestamp" }

        val version = records[0].getOrNull(1)?.toIntOrNull()
            ?: error("Invalid schema version")
        require(version == 1 || version == BACKUP_SCHEMA_VERSION) {
            "Unsupported backup schema version: $version"
        }
        val timestamp = records[1].getOrNull(1)?.toLongOrNull()
            ?: error("Invalid backup timestamp")

        val byGoalId = linkedMapOf<Long, Pair<Goal, MutableList<Transaction>>>()

        records.drop(3).forEach { columns ->
            if (columns.all { it.isBlank() }) return@forEach
            require(columns.size >= COLUMN_COUNT) { "Malformed CSV row" }

            val sourceGoalId = columns[0].toLongOrNull() ?: error("Invalid goal id")
            val targetAmount = columns[2].toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
                ?: error("Invalid target amount")
            val deadline = if (version == 1) {
                parseOldDeadlineToMillis(columns[3])
            } else {
                columns[3].toLongOrNull() ?: error("Invalid deadline")
            }
            val priority = GoalPriority.entries.firstOrNull { it.name == columns[4] }
                ?: GoalPriority.Normal

            val goal = Goal(
                title = columns[1],
                targetAmount = targetAmount,
                deadline = deadline,
                priority = priority,
                reminder = columns[5].toBooleanStrictOrNull() ?: false,
                goalIconId = columns[6].ifBlank { null },
                archived = columns[7].toBooleanStrictOrNull() ?: false,
                additionalNotes = columns[8],
                goalImage = null
            ).apply { goalId = sourceGoalId }

            val bucket = byGoalId.getOrPut(sourceGoalId) { goal to mutableListOf() }
            if (columns[9].isNotBlank()) {
                val transactionAmount = columns[12].toDoubleOrNull()
                    ?.takeIf { it.isFinite() && it >= 0.0 }
                    ?: error("Invalid transaction amount")
                val transactionType = TransactionType.entries.firstOrNull {
                    it.name == columns[10]
                }?.takeIf { it != TransactionType.Invalid }
                    ?: error("Invalid transaction type")
                bucket.second += Transaction(
                    ownerGoalId = sourceGoalId,
                    type = transactionType,
                    timeStamp = columns[11].toLongOrNull() ?: error("Invalid transaction timestamp"),
                    amount = transactionAmount,
                    notes = columns[13]
                ).apply {
                    transactionId = columns[9].toLongOrNull() ?: error("Invalid transaction id")
                }
            }
        }

        return BackupCSVModel(
            version = version,
            timestamp = timestamp,
            data = byGoalId.values.map { (goal, transactions) ->
                GoalWithTransactions(goal, transactions)
            }
        )
    }

    private fun StringBuilder.appendRecord(fields: List<Any?>) {
        append(fields.joinToString(CSV_DELIMITER) { escape(it?.toString().orEmpty()) })
        append('\n')
    }

    private fun escape(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun parseRecords(csv: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0

        fun finishField() {
            fields += field.toString()
            field.setLength(0)
        }
        fun finishRecord() {
            finishField()
            records += fields
            fields = mutableListOf()
        }

        while (index < csv.length) {
            val char = csv[index]
            when {
                char == '"' && inQuotes && index + 1 < csv.length && csv[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> finishField()
                (char == '\n' || char == '\r') && !inQuotes -> {
                    if (char == '\r' && index + 1 < csv.length && csv[index + 1] == '\n') index++
                    finishRecord()
                }
                else -> field.append(char)
            }
            index++
        }
        require(!inQuotes) { "Unclosed quoted CSV field" }
        if (field.isNotEmpty() || fields.isNotEmpty()) finishRecord()
        return records
    }
}
