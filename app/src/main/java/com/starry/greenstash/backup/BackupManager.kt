package com.starry.greenstash.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.starry.greenstash.BuildConfig
import com.starry.greenstash.database.goal.GoalDao
import com.starry.greenstash.utils.updateText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class BackupManager(
    private val context: Context,
    private val goalDao: GoalDao
) {
    companion object {
        private const val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"
        private const val BACKUP_FOLDER_NAME = "backups"
        private const val AUTO_PREFIX_NEW = "MocQuy_AutoBackup_"
        private const val AUTO_PREFIX_OLD = "GreenStash_AutoBackup_"
    }

    private val goalToJsonConverter = GoalToJSONConverter()
    private val goalToCsvConverter = GoalToCSVConverter()

    private fun log(message: String) = Log.d("BackupManager", message)

    suspend fun createDatabaseBackup(backupType: BackupType): Intent = withContext(Dispatchers.IO) {
        val goals = goalDao.getAllGoalsIncludingArchived()
        val payload = when (backupType) {
            BackupType.JSON -> goalToJsonConverter.convertToJson(goals)
            BackupType.CSV -> goalToCsvConverter.convertToCSV(goals)
        }

        val folder = File(context.cacheDir, BACKUP_FOLDER_NAME)
        check(folder.exists() || folder.mkdirs()) { "Unable to create backup directory" }
        val fileName = "MocQuy-(${UUID.randomUUID()}).${backupType.name.lowercase(Locale.US)}"
        val file = File(folder, fileName)
        file.updateText(payload)
        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        val mimeType = if (backupType == BackupType.JSON) "application/json" else "text/csv"

        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Mộc Quỹ Backup")
            putExtra(Intent.EXTRA_TEXT, "Created at ${LocalDateTime.now()}")
        }.let { Intent.createChooser(it, fileName) }
    }

    suspend fun performAutomaticBackup(directoryUri: Uri, maxKeep: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val goals = goalDao.getAllGoalsIncludingArchived()
                val payload = goalToJsonConverter.convertToJson(goals)
                val timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                val fileName = "$AUTO_PREFIX_NEW$timestamp.json"
                val keepCount = maxKeep.coerceIn(1, 100)

                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri)
                if (pickedDir == null || !pickedDir.canWrite()) return@withContext false

                val newFile = pickedDir.createFile("application/json", fileName)
                    ?: return@withContext false
                val stream = context.contentResolver.openOutputStream(newFile.uri)
                if (stream == null) {
                    newFile.delete()
                    return@withContext false
                }

                try {
                    stream.use {
                        it.write(payload.toByteArray(StandardCharsets.UTF_8))
                        it.flush()
                    }
                } catch (error: Exception) {
                    newFile.delete()
                    throw error
                }

                val backups = pickedDir.listFiles()
                    .filter { file ->
                        val name = file.name.orEmpty()
                        file.isFile && name.endsWith(".json") &&
                            (name.startsWith(AUTO_PREFIX_NEW) || name.startsWith(AUTO_PREFIX_OLD))
                    }
                    .sortedByDescending { it.lastModified() }

                backups.drop(keepCount).forEach { oldFile ->
                    runCatching { oldFile.delete() }
                }

                log("Automatic backup successful: $fileName")
                true
            } catch (error: Exception) {
                Log.e("BackupManager", "Automatic backup failed", error)
                false
            }
        }

    suspend fun restoreDatabaseBackup(
        backupString: String,
        backupType: BackupType = BackupType.JSON,
        onFailure: () -> Unit,
        onSuccess: () -> Unit,
    ) = withContext(Dispatchers.IO) {
        val restored = runCatching {
            val data = when (backupType) {
                BackupType.JSON -> goalToJsonConverter.convertFromJson(backupString).data
                BackupType.CSV -> goalToCsvConverter.convertFromCSV(backupString).data
            } ?: error("Backup data is empty")
            goalDao.insertGoalWithTransactions(data)
        }.isSuccess

        withContext(Dispatchers.Main) {
            if (restored) onSuccess() else onFailure()
        }
    }
}

enum class BackupType { JSON, CSV }
