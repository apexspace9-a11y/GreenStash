/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starry.greenstash.other.WelcomeDataStore
import com.starry.greenstash.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val welcomeDataStore: WelcomeDataStore,
    private val preferenceUtil: PreferenceUtil
) : ViewModel() {

    fun saveOnBoardingState(completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            welcomeDataStore.saveOnBoardingState(completed = completed)
        }
    }

    fun setDefaultCurrency(newValue: String) {
        preferenceUtil.putString(PreferenceUtil.DEFAULT_CURRENCY_STR, newValue)
    }

    fun getDefaultCurrencyValue() = preferenceUtil.getString(
        PreferenceUtil.DEFAULT_CURRENCY_STR, "VND"
    )
}
