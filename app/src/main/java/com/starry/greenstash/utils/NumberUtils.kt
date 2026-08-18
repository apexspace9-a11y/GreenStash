/**
 * MIT License
 *
 * Copyright (c) [2022 - Present] Stɑrry Shivɑm
 */
package com.starry.greenstash.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object NumberUtils {

    fun getValidatedNumber(text: String): String {
        val filteredChars = text.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && index != 0
                    && text.indexOf('.') == index)
                    || (c == '.' && index != 0
                    && text.count { it == '.' } <= 1)
        }
        return if (filteredChars.count { it == '.' } == 1) {
            val beforeDecimal = filteredChars.substringBefore('.')
            val afterDecimal = filteredChars.substringAfter('.')
            "$beforeDecimal.$afterDecimal"
        } else {
            filteredChars
        }
    }

    fun roundDecimal(number: Double): Double {
        val locale = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#.##", locale)
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toDouble()
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val locale = if (currencyCode == "VND") {
            Locale.forLanguageTag("vi-VN")
        } else {
            Locale.getDefault()
        }
        val nf = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = if (currencyCode in setOf(
                    "JPY", "DJF", "GNF", "IDR", "KMF", "KRW", "LAK",
                    "PYG", "RWF", "VND", "VUV", "XAF", "XOF", "XPF"
                )
            ) 0 else 2
        }
        return nf.format(amount)
    }

    fun getCurrencySymbol(currencyCode: String): String {
        return if (currencyCode == "VND") "₫" else Currency.getInstance(currencyCode).symbol
    }

    fun prettyCount(number: Number): String {
        val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        if (numValue <= 0L) return "0"
        val value = floor(log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / 10.0.pow((base * 3).toDouble())
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }
}
