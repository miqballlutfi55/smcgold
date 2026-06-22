package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun SMCInputCard(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    subText: String? = null,
    modifier: Modifier = Modifier,
    isInteger: Boolean = false
) {
    // Format the double value with commas for presentation, but keep raw input trackable
    var textInput by remember { mutableStateOf("") }
    
    // Convert current double to formatted string
    val formatter = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))
    
    // Sync external value updates (e.g. database load or reset)
    LaunchedEffect(value) {
        val formatted = formatter.format(value)
        if (parseTextToDouble(textInput) != value) {
            textInput = formatted
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(Locale.ROOT),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
                // Strip commas for text representation and sanitize characters
                val stripped = input.replace(",", "")
                if (stripped.isEmpty()) {
                    textInput = ""
                    onValueChange(0.0)
                } else {
                    // Match integer or decimal input format
                    val regex = if (isInteger) "^\\d*$".toRegex() else "^\\d*\\.?\\d*$".toRegex()
                    if (regex.matches(stripped)) {
                        textInput = formatRawInputWithCommas(stripped)
                        onValueChange(stripped.toDoubleOrNull() ?: 0.0)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Color(0xFF333333),
                cursorColor = ElectricBlue
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (subText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                color = ElectricBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// Parse text input stripped of commas back to a Double
private fun parseTextToDouble(text: String): Double {
    return text.replace(",", "").toDoubleOrNull() ?: 0.0
}

// Interactively format any typed sequence to comma-separated blocks
private fun formatRawInputWithCommas(raw: String): String {
    if (raw.isEmpty()) return ""
    return try {
        val parts = raw.split(".")
        val intVal = parts[0].toLongOrNull() ?: 0L
        val decVal = if (parts.size > 1) parts[1] else null
        
        val formatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
        val formattedInt = formatter.format(intVal)
        
        if (decVal != null) {
            if (raw.endsWith(".")) {
                "$formattedInt."
            } else {
                "$formattedInt.$decVal"
            }
        } else {
            formattedInt
        }
    } catch (e: Exception) {
        raw
    }
}
