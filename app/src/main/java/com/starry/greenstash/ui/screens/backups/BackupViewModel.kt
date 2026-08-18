package com.starry.greenstash.ui.screens.backups

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.starry.greenstash.backup.AutoBackupWorker
import com.starry.greenstash.backup.BackupManager
import com.starry.greenstash.backup.BackupType
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val preferenceUtil: PreferenceUtil,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    companion object {
        const val AUTO_BACKUP_WORK_NAME = "auto_backup_work"
        private const val MIN_INTERVAL_DAYS = 1
        private const val MAX_INTERVAL_DAYS = 365
        private const val MIN_BACKUPS = 1
        private const val MAX_BACKUPS = 100
    }

    private val _autoBackup = MutableLiveData(false)
    private val _autoBackupDirectory = MutableLiveData("")
    private val _autoBackupInterval = MutableLiveData(MIN_INTERVAL_DAYS)
    private val _autoBackupMaxKeep = MutableLiveData(5)
    private val _lastBackupTime = MutableLiveData(0L)

    val autoBackup: LiveData<Boolean> = _autoBackup
    val autoBackupDirectory: LiveData<String> = _autoBackupDirectory
    val autoBackupInterval: LiveData<Int> = _autoBackupInterval
    val autoBackupMaxKeep: LiveData<Int> = _autoBackupMaxKeep
    val lastBackupTime: LiveData<Long> = _lastBackupTime

    init {
        _autoBackup.value = preferenceUtil.getBoolean(PreferenceUtil.AUTO_BACKUP_BOOL, false)
        _autoBackupDirectory.value = preferenceUtil.getString(
            PreferenceUtil.AUTO_BACKUP_DIRECTORY_URI_STR, ""
        ).orEmpty()
        val interval = preferenceUtil.getInt(
            PreferenceUtil.AUTO_BACKUP_INTERVAL_DAYS_INT, MIN_INTERVAL_DAYS
        ).coerceIn(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS)
        val keep = preferenceUtil.getInt(
            PreferenceUtil.AUTO_BACKUP_MAX_KEEP_INT, 5
        ).coerceIn(MIN_BACKUPS, MAX_BACKUPS)
        _autoBackupInterval.value = interval
        _autoBackupMaxKeep.value = keep
        _lastBackupTime.value = preferenceUtil.getLong(PreferenceUtil.AUTO_BACKUP_LAST_TIME_MS_LONG, 0L)
        preferenceUtil.putInt(PreferenceUtil.AUTO_BACKUP_INTERVAL_DAYS_INT, interval)
        preferenceUtil.putInt(PreferenceUtil.AUTO_BACKUP_MAX_KEEP_INT, keep)
    }

    fun takeBackup(backupType: BackupType, onComplete: (Intent) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { backupManager.createDatabaseBackup(backupType) }
                .getOrNull()
                ?.let { intent -> withContext(Dispatchers.Main) { onComplete(intent) } }
        }
    }

    fun restoreBackup(
        backupType: BackupType,
        backupString: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            backupManager.restoreDatabaseBackup(
                backupType = backupType,
                backupString = backupString,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }

    fun setAutoBackup(enabled: Boolean, onDirectoryMissing: () -> Unit) {
        if (enabled && _autoBackupDirectory.value.isNullOrBlank()) {
            _autoBackup.value = false
            onDirectoryMissing()
            return
        }
        _autoBackup.value = enabled
        preferenceUtil.putBoolean(PreferenceUtil.AUTO_BACKUP_BOOL, enabled)
        if (enabled) scheduleAutoBackup() else cancelAutoBackup()
    }

    fun setAutoBackupDirectory(uri: String) {
        _autoBackupDirectory.value = uri
        preferenceUtil.putString(PreferenceUtil.AUTO_BACKUP_DIRECTORY_URI_STR, uri)
        if (_autoBackup.value == true) scheduleAutoBackup()
    }

    fun setAutoBackupInterval(days: Int) {
        val safeDays = days.coerceIn(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS)
        _autoBackupInterval.value = safeDays
        preferenceUtil.putInt(PreferenceUtil.AUTO_BACKUP_INTERVAL_DAYS_INT, safeDays)
        if (_autoBackup.value == true) scheduleAutoBackup()
    }

    fun setAutoBackupMaxKeep(maxKeep: Int) {
        val safeMax = maxKeep.coerceIn(MIN_BACKUPS, MAX_BACKUPS)
        _autoBackupMaxKeep.value = safeMax
        preferenceUtil.putInt(PreferenceUtil.AUTO_BACKUP_MAX_KEEP_INT, safeMax)
    }

    private fun scheduleAutoBackup() {
        val days = (_autoBackupInterval.value ?: MIN_INTERVAL_DAYS)
            .coerceIn(MIN_INTERVAL_DAYS, MAX_INTERVAL_DAYS)
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(days.toLong(), TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AUTO_BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelAutoBackup() {
        WorkManager.getInstance(context).cancelUniqueWork(AUTO_BACKUP_WORK_NAME)
    }
}
