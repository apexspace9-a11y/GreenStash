/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.starry.greenstash.R
import com.starry.greenstash.ui.theme.greenstashFont
import com.starry.greenstash.ui.theme.liquidGlass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class CurrencyPickerData(
    val currencyNames: Array<String>,
    val currencyValues: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CurrencyPickerData
        return currencyNames.contentEquals(other.currencyNames) &&
            currencyValues.contentEquals(other.currencyValues)
    }

    override fun hashCode(): Int {
        var result = currencyNames.contentHashCode()
        result = 31 * result + currencyValues.contentHashCode()
        return result
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    defaultCurrencyValue: String,
    currencyPickerData: CurrencyPickerData,
    showBottomSheet: MutableState<Boolean>,
    onCurrencySelected: (String) -> Unit
) {
    val currencyNames = currencyPickerData.currencyNames
    val currencyValues = currencyPickerData.currencyValues
    val safeIndex = currencyValues.indexOf(defaultCurrencyValue)
        .takeIf { it in currencyNames.indices }
        ?: currencyValues.indexOf("VND").takeIf { it in currencyNames.indices }
        ?: 0
    val defaultCurrencyEntry = currencyNames.getOrElse(safeIndex) { "Đồng Việt Nam (₫)" }

    val (selectedCurrencyOption, onCurrencyOptionSelected) = rememberSaveable {
        mutableStateOf(defaultCurrencyEntry)
    }
    val (searchText, onSearchTextChanged) = rememberSaveable { mutableStateOf("") }
    val filteredCurrencies = currencyNames.filter {
        it.contains(searchText, ignoreCase = true)
    }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    fun selectedCode(): String {
        val selectedIndex = currencyNames.indexOf(selectedCurrencyOption)
        return currencyValues.getOrElse(selectedIndex) { "VND" }
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            containerColor = Color.Transparent,
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(220)
                    withContext(Dispatchers.Main) {
                        showBottomSheet.value = false
                        onCurrencySelected(selectedCode())
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .liquidGlass(radius = 30.dp, blurAmount = 32.dp)
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    placeholder = { Text(stringResource(R.string.search_currency)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(18.dp)
                )

                Column(
                    modifier = Modifier
                        .height(360.dp)
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredCurrencies.forEach { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = text == selectedCurrencyOption,
                                    onClick = { onCurrencyOptionSelected(text) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = text == selectedCurrencyOption,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = text,
                                modifier = Modifier.padding(start = 10.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = greenstashFont
                            )
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            delay(220)
                            withContext(Dispatchers.Main) {
                                showBottomSheet.value = false
                                onCurrencySelected(selectedCode())
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm), fontFamily = greenstashFont)
                }
            }
        }
    }
}
