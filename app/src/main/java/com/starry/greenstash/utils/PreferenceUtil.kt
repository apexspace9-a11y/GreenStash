/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.utils

import android.content.Context
import androidx.core.content.edit
import com.starry.greenstash.ui.screens.settings.DateStyle
import com.starry.greenstash.ui.screens.settings.ThemeMode

class PreferenceUtil(context: Context) {

    companion object {
        private const val PREFS_NAME = "greenstash_settings"

        const val APP_THEME_INT = "theme_settings"
        const val AMOLED_THEME_BOOL = "amoled_theme"
        const val MATERIAL_YOU_BOOL = "material_you"
        const val DEFAULT_CURRENCY_STR = "default_currency_code"
        const val DATE_STYLE_INT = "date_style"
        const val APP_LOCK_BOOL = "app_lock"
        const val GOAL_CARD_STYLE_INT = "goal_card_style"
        const val MOCQUY_VND_MIGRATION_BOOL = "mocquy_vnd_default_migrated"
        const val MOCQUY_601_LIGHT_MIGRATION_BOOL = "mocquy_601_light_theme_migrated"

        const val GOAL_FILTER_FIELD_INT = "goal_filter_field"
        const val GOAL_FILTER_SORT_TYPE_INT = "goal_filter_sort_type"

        const val HOME_SCREEN_ONBOARDING_BOOL = "show_home_screen_onboarding"
        const val INPUT_SCREEN_ONBOARDING_BOOL = "show_input_onboarding"
        const val INPUT_REMOVE_DEADLINE_TIP_BOOL = "input_remove_deadline_tip"
        const val INFO_TRANSACTION_SWIPE_TIP_BOOL = "info_transaction_swipe_tip"

        const val AUTO_BACKUP_BOOL = "auto_backup"
        const val AUTO_BACKUP_DIRECTORY_URI_STR = "auto_backup_directory_uri"
        const val AUTO_BACKUP_INTERVAL_DAYS_INT = "auto_backup_interval_days"
        const val AUTO_BACKUP_LAST_TIME_MS_LONG = "auto_backup_last_time_ms"
        const val AUTO_BACKUP_MAX_KEEP_INT = "auto_backup_max_keep"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        if (!keyExists(DEFAULT_CURRENCY_STR)) {
            putString(DEFAULT_CURRENCY_STR, "VND")
        }
        if (!keyExists(DATE_STYLE_INT)) {
            putInt(DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal)
        }
        if (!getBoolean(MOCQUY_601_LIGHT_MIGRATION_BOOL, false)) {
            putInt(APP_THEME_INT, ThemeMode.Light.ordinal)
            putBoolean(AMOLED_THEME_BOOL, false)
            putBoolean(MOCQUY_601_LIGHT_MIGRATION_BOOL, true)
        }
    }

    private fun keyExists(key: String): Boolean = prefs.contains(key)

    fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun putLong(key: String, value: Long) {
        prefs.edit { putLong(key, value) }
    }

    fun getString(key: String, defValue: String): String? = prefs.getString(key, defValue)

    fun getInt(key: String, defValue: Int): Int = prefs.getInt(key, defValue)

    fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)

    fun getLong(key: String, defValue: Long): Long = prefs.getLong(key, defValue)
}
