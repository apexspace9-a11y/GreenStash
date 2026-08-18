package com.starry.greenstash

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.starry.greenstash.reminder.ReminderNotificationSender
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GreenStashApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    override fun onCreate() {
        super.onCreate()
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi"))
        }
        migrateLegacyCurrencyDefault()
        createNotificationChannel()
        CaocConfig.Builder.create().restartActivity(MainActivity::class.java).apply()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun migrateLegacyCurrencyDefault() {
        if (!preferenceUtil.getBoolean(PreferenceUtil.MOCQUY_VND_MIGRATION_BOOL, false)) {
            val currentCurrency = preferenceUtil.getString(
                PreferenceUtil.DEFAULT_CURRENCY_STR,
                "VND"
            )
            if (currentCurrency == "USD") {
                preferenceUtil.putString(PreferenceUtil.DEFAULT_CURRENCY_STR, "VND")
            }
            preferenceUtil.putBoolean(PreferenceUtil.MOCQUY_VND_MIGRATION_BOOL, true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderNotificationSender.REMINDER_CHANNEL_ID,
                "Nhắc mục tiêu",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Nhắc bạn duy trì tiến độ cho các mục tiêu tiết kiệm."
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
