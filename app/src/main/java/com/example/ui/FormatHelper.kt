package com.example.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FormatHelper {
    private val usdFormatter = DecimalFormat("$#,##0.00", DecimalFormatSymbols(Locale.US))
    private val usdCompactFormatter = DecimalFormat("$#,##0", DecimalFormatSymbols(Locale.US))
    
    fun formatUsd(value: Double): String {
        return usdFormatter.format(value)
    }

    fun formatUsdCompact(value: Double): String {
        return usdCompactFormatter.format(value)
    }

    fun formatUsdDecimalCompact(value: Double): String {
        return if (value >= 1_000_000.0) {
            String.format(Locale.US, "$%.1fM", value / 1_000_000.0)
        } else if (value >= 1_000.0) {
            String.format(Locale.US, "$%.1fK", value / 1_000.0)
        } else {
            String.format(Locale.US, "$%.0f", value)
        }
    }

    fun formatIdr(usdValue: Double, rate: Double): String {
        val idrVal = usdValue * rate
        val symbols = DecimalFormatSymbols(Locale("in", "ID"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        val idrFormatter = DecimalFormat("Rp #,##0", symbols)
        return idrFormatter.format(idrVal)
    }

    fun formatIdrCompact(usdValue: Double, rate: Double): String {
        val idrVal = usdValue * rate
        return if (idrVal >= 1_000_000.0) {
            String.format(Locale.US, "Rp %.1fM", idrVal / 1_000_000.0)
        } else if (idrVal >= 1_000.0) {
            String.format(Locale.US, "Rp %.1fK", idrVal / 1_000.0)
        } else {
            String.format(Locale.US, "Rp %.0f", idrVal)
        }
    }

    // A helper for showing raw double numbers with up to 2 decimal places cleanly
    fun formatDecimal(value: Double): String {
        val formatter = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))
        return formatter.format(value)
    }
}
