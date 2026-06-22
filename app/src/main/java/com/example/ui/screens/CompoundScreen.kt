package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.DayResult
import com.example.ui.DayCalculation
import com.example.ui.FormatHelper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import java.util.Locale
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IceBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CompoundScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Inputs & Results observed from VM
    val initBalance by viewModel.initialBalance.collectAsState()
    val targetPipsVal by viewModel.targetPips.collectAsState()
    val stepSizeVal by viewModel.stepSize.collectAsState()
    val maxLotVal by viewModel.maxLot.collectAsState()
    val totalDaysVal by viewModel.totalDays.collectAsState()
    val initialLotVal by viewModel.initialLot.collectAsState()
    val riskRewardVal by viewModel.riskRewardRatio.collectAsState()
    val currentKursIdr by viewModel.kursIdr.collectAsState()
    
    val currentLoadedName by viewModel.loadedPlanName.collectAsState()
    val projections by viewModel.projectionResult.collectAsState()

    var isHeaderCardExpanded by remember { mutableStateOf(true) }
    var isInputsExpanded by remember { mutableStateOf(false) } // collapsed by default to allow full view timeline immediately!
    var showSaveDialog by remember { mutableStateOf(false) }
    var savePlanName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. PINNED STATIC HEADER ROW & FINAL PROJECTION CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SMC CALCULATOR",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "COMPOUND PROJECTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                }
                if (currentLoadedName != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2618))
                            .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE: $currentLoadedName",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = IceBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "FINAL PROJECTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF072960),
                            letterSpacing = 2.sp
                        )
                        IconButton(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isHeaderCardExpanded = !isHeaderCardExpanded 
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isHeaderCardExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle projection view",
                                tint = Color(0xFF072960)
                            )
                        }
                    }

                    // Giant final balance
                    Text(
                        text = FormatHelper.formatUsd(projections.finalBalance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF072960)
                    )
                    
                    Text(
                        text = FormatHelper.formatIdr(projections.finalBalance, currentKursIdr),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF072960).copy(alpha = 0.82f)
                    )

                    AnimatedVisibility(
                        visible = isHeaderCardExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Profit card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE2F0EA))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL PROFIT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E8056)
                                        )
                                        Text(
                                            text = "+" + FormatHelper.formatUsd(projections.totalProfit),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E8056)
                                        )
                                        Text(
                                            text = FormatHelper.formatIdr(projections.totalProfit, currentKursIdr),
                                            fontSize = 9.sp,
                                            color = Color(0xFF1E8056).copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // Return card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE0EAFD))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL RETURN (ROI)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF072960)
                                        )
                                        Text(
                                            text = "+" + String.format(Locale.US, "%,.2f", projections.totalReturnPercentage) + "%",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF072960)
                                        )
                                        Text(
                                            text = "From initial deposit",
                                            fontSize = 9.sp,
                                            color = Color(0xFF072960).copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    savePlanName = currentLoadedName ?: ""
                                    showSaveDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF072960),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_save_strategy")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "+ SAVE STRATEGY PLAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. SCROLLABLE TIMELINE + COLLAPSIBLE INPUTS
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // Collapsible Inputs Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isInputsExpanded = !isInputsExpanded
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PLAN PARAMETER INPUTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isInputsExpanded) "HIDE" else "SHOW ALL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Icon(
                                    imageVector = if (isInputsExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Toggle parameter inputs",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isInputsExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Row 1: INITIAL ($) & KURS IDR (2-Column Proportional Mobile Setup!)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SMCInputCard(
                                        label = "INITIAL ($)",
                                        value = initBalance,
                                        onValueChange = { viewModel.initialBalance.value = it },
                                        subText = FormatHelper.formatIdr(initBalance, currentKursIdr),
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    SMCInputCard(
                                        label = "KURS IDR",
                                        value = currentKursIdr,
                                        onValueChange = { viewModel.kursIdr.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 2: PIPS TARGET & STEP SIZE ($)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SMCInputCard(
                                        label = "PIPS TARGET",
                                        value = targetPipsVal,
                                        onValueChange = { viewModel.targetPips.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SMCInputCard(
                                        label = "STEP SIZE ($)",
                                        value = stepSizeVal,
                                        onValueChange = { viewModel.stepSize.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 3: INIT LOT & MAX LOT
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SMCInputCard(
                                        label = "INIT LOT",
                                        value = initialLotVal,
                                        onValueChange = { viewModel.initialLot.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SMCInputCard(
                                        label = "MAX LOT",
                                        value = maxLotVal,
                                        onValueChange = { viewModel.maxLot.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Row 4: TOTAL DAYS & AVG RISK:REWARD (RR)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SMCInputCard(
                                        label = "TOTAL DAYS",
                                        value = totalDaysVal.toDouble(),
                                        onValueChange = { viewModel.totalDays.value = maxOf(1, it.toInt()) },
                                        isInteger = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SMCInputCard(
                                        label = "AVG RISK:REWARD (RR)",
                                        value = riskRewardVal,
                                        onValueChange = { viewModel.riskRewardRatio.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. TIMELINE HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "COMPOUND TIMELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.resetProjection()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_reset_timeline")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = CrimsonRed
                        )
                    }
                }
            }

            // 5. TIMELINE RENDERER (Day cards in active compile loop)
            items(projections.daysList) { dayItem ->
                TimelineDayRow(
                    dayCalculation = dayItem,
                    rate = currentKursIdr,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.cycleDayOverride(dayItem.day)
                    }
                )
            }
            
            // Padding gap under list for beautiful float bar layout
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // SAVE DIALOG
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                containerColor = CardBackground,
                title = { 
                    Text(
                        text = "Save Compound Strategy", 
                        color = TextPrimary, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp 
                    ) 
                },
                text = {
                    Column {
                        Text(
                            text = "Simpan parameter rencana kalkulasi compounding saat ini ke daftar strategi.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = savePlanName,
                            onValueChange = { savePlanName = it },
                            label = { Text("Strategy Plan Name") },
                            placeholder = { Text("e.g. SMC Gold Conservative") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_plan_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (savePlanName.isNotBlank()) {
                                viewModel.saveStrategyPlan(savePlanName.trim())
                                showSaveDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_plan_save")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSaveDialog = false }
                    ) {
                        Text("Cancel", color = CrimsonRed, fontSize = 12.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun TimelineDayRow(
    dayCalculation: DayCalculation,
    rate: Double,
    onClick: () -> Unit
) {
    val statusBg = when (dayCalculation.resultType) {
        DayResult.PROJECTED -> Color(0xFF1E1E24)
        DayResult.WIN -> Color(0x0D00FFA3) // ~5% alpha
        DayResult.LOSS -> Color(0x0DFF3366) // ~5% alpha
    }

    val statusBorder = when (dayCalculation.resultType) {
        DayResult.PROJECTED -> Color(0xFF333333)
        DayResult.WIN -> Color(0xFF00FFA3).copy(alpha = 0.3f)
        DayResult.LOSS -> Color(0xFFFF3366).copy(alpha = 0.3f)
    }

    val profitColor = when (dayCalculation.resultType) {
        DayResult.PROJECTED -> Color(0xFF00FFA3)
        DayResult.WIN -> Color(0xFF00FFA3)
        DayResult.LOSS -> Color(0xFFFF3366)
    }

    val badgeBg = when (dayCalculation.resultType) {
        DayResult.PROJECTED -> Color(0xFF333333)
        DayResult.WIN -> Color(0xFF00FFA3).copy(alpha = 0.2f)
        DayResult.LOSS -> Color(0xFFFF3366).copy(alpha = 0.2f)
    }

    val badgeTextColor = when (dayCalculation.resultType) {
        DayResult.PROJECTED -> Color.White
        DayResult.WIN -> Color(0xFF00FFA3)
        DayResult.LOSS -> Color(0xFFFF3366)
    }

    val isLoss = dayCalculation.resultType == DayResult.LOSS

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(statusBg)
            .border(1.dp, statusBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left details with day circle badge
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Badge D1, D2 etc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D${dayCalculation.day}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }

            // Current day balance and info
            Column {
                Text(
                    text = FormatHelper.formatUsd(dayCalculation.endBalance),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "%.2f Lot", dayCalculation.lotSize) + " • " + FormatHelper.formatIdrCompact(dayCalculation.endBalance, rate),
                    fontSize = 10.sp,
                    color = Color(0xFFA0A0A0),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Right details with status tag and amount change
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = dayCalculation.resultType.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isLoss) {
                    "-" + FormatHelper.formatUsd(kotlin.math.abs(dayCalculation.profitLoss))
                } else {
                    "+" + FormatHelper.formatUsd(dayCalculation.profitLoss)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = profitColor,
                textDecoration = if (isLoss) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}
