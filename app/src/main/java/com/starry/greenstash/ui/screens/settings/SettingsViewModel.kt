/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.starry.greenstash.ui.screens.home.GoalCardStyle
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {

    private val _theme = MutableLiveData(ThemeMode.Auto)
    private val _amoledTheme = MutableLiveData(false)
    private val _materialYou = MutableLiveData(false)
    private val _goalCardStyle = MutableLiveData(GoalCardStyle.Classic)
    private val _dateStyle = MutableLiveData(DateStyle.DD_MM_YYYY)

    val theme: LiveData<ThemeMode> = _theme
    val amoledTheme: LiveData<Boolean> = _amoledTheme
    val materialYou: LiveData<Boolean> = _materialYou
    val goalCardStyle: LiveData<GoalCardStyle> = _goalCardStyle
    val dateStyle: LiveData<DateStyle> = _dateStyle

    init {
        _theme.value = ThemeMode.entries.getOrElse(getThemeValue()) { ThemeMode.Auto }
        _amoledTheme.value = getAmoledThemeValue()
        _materialYou.value = getMaterialYouValue()
        _goalCardStyle.value = GoalCardStyle.entries.getOrElse(getGoalCardStyleValue()) {
            GoalCardStyle.Classic
        }
        _dateStyle.value = DateStyle.entries.getOrElse(getDateStyleValue()) {
            DateStyle.DD_MM_YYYY
        }
    }

    fun setTheme(newTheme: ThemeMode) {
        _theme.postValue(newTheme)
        preferenceUtil.putInt(PreferenceUtil.APP_THEME_INT, newTheme.ordinal)
    }

    fun setAmoledTheme(newValue: Boolean) {
        _amoledTheme.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.AMOLED_THEME_BOOL, newValue)
    }

    fun setMaterialYou(newValue: Boolean) {
        _materialYou.postValue(newValue)
        preferenceUtil.putBoolean(PreferenceUtil.MATERIAL_YOU_BOOL, newValue)
    }

    fun setGoalCardStyle(newValue: GoalCardStyle) {
        _goalCardStyle.postValue(newValue)
        preferenceUtil.putInt(PreferenceUtil.GOAL_CARD_STYLE_INT, newValue.ordinal)
    }

    fun setDateStyle(newValue: DateStyle) {
        _dateStyle.postValue(newValue)
        preferenceUtil.putInt(PreferenceUtil.DATE_STYLE_INT, newValue.ordinal)
    }

    fun setDefaultCurrency(newValue: String) {
        preferenceUtil.putString(PreferenceUtil.DEFAULT_CURRENCY_STR, newValue)
    }

    fun setAppLock(newValue: Boolean) {
        preferenceUtil.putBoolean(PreferenceUtil.APP_LOCK_BOOL, newValue)
    }

    fun getDefaultCurrencyValue() = preferenceUtil.getString(
        PreferenceUtil.DEFAULT_CURRENCY_STR, "VND"
    )

    fun getAppLockValue() = preferenceUtil.getBoolean(
        PreferenceUtil.APP_LOCK_BOOL, false
    )

    @Composable
    fun getCurrentTheme(): ThemeMode {
        return when (theme.value ?: ThemeMode.Auto) {
            ThemeMode.Auto -> if (isSystemInDarkTheme()) ThemeMode.Dark else ThemeMode.Light
            ThemeMode.Dark -> ThemeMode.Dark
            ThemeMode.Light -> ThemeMode.Light
        }
    }

    private fun getThemeValue() = preferenceUtil.getInt(
        PreferenceUtil.APP_THEME_INT, ThemeMode.Auto.ordinal
    )

    private fun getAmoledThemeValue() = preferenceUtil.getBoolean(
        PreferenceUtil.AMOLED_THEME_BOOL, false
    )

    private fun getMaterialYouValue() = preferenceUtil.getBoolean(
        PreferenceUtil.MATERIAL_YOU_BOOL, false
    )

    private fun getGoalCardStyleValue() = preferenceUtil.getInt(
        PreferenceUtil.GOAL_CARD_STYLE_INT, GoalCardStyle.Classic.ordinal
    )

    private fun getDateStyleValue() = preferenceUtil.getInt(
        PreferenceUtil.DATE_STYLE_INT, DateStyle.DD_MM_YYYY.ordinal
    )
}
