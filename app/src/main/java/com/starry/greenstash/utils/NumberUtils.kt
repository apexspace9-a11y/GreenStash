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
    private val zeroFractionCurrencies = setOf(
        "JPY", "DJF", "GNF", "IDR", "KMF", "KRW", "LAK",
        "PYG", "RWF", "VND", "VUV", "XAF", "XOF", "XPF"
    )

    fun getValidatedNumber(text: String): String {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val dot = filtered.indexOf('.')
        return if (dot >= 0) {
            val before = filtered.substring(0, dot).filter(Char::isDigit)
            val after = filtered.substring(dot + 1).filter(Char::isDigit)
            if (before.isEmpty()) after else "$before.$after"
        } else {
            filtered
        }
    }

    fun roundDecimal(number: Double): Double {
        if (!number.isFinite()) return 0.0
        val symbols = DecimalFormatSymbols(Locale.US)
        return DecimalFormat("#.##", symbols).apply {
            roundingMode = RoundingMode.CEILING
        }.format(number).toDoubleOrNull() ?: 0.0
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val currency = safeCurrency(currencyCode)
        val code = currency.currencyCode
        val locale = if (code == "VND") Locale.forLanguageTag("vi-VN") else Locale.getDefault()
        return runCatching {
            NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = currency
                maximumFractionDigits = if (code in zeroFractionCurrencies) 0 else 2
            }.format(if (amount.isFinite()) amount else 0.0)
        }.getOrElse { "${getCurrencySymbol(code)}0" }
    }

    fun getCurrencySymbol(currencyCode: String): String {
        val currency = safeCurrency(currencyCode)
        return if (currency.currencyCode == "VND") "₫" else currency.getSymbol(Locale.getDefault())
    }

    fun prettyCount(number: Number): String {
        val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        if (numValue <= 0L) return "0"
        val value = floor(log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(numValue / 10.0.pow((base * 3).toDouble())) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }

    private fun safeCurrency(code: String): Currency =
        runCatching { Currency.getInstance(code.ifBlank { "VND" }) }
            .getOrElse { Currency.getInstance("VND") }
}
