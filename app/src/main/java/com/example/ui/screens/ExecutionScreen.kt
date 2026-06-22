package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.FormatHelper
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ExecutionScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Observe execution inputs
    val execBal by viewModel.execBalance.collectAsState()
    val riskPercent by viewModel.execRiskPercent.collectAsState()
    val stopLossVal by viewModel.execStopLoss.collectAsState()
    val pipVal by viewModel.execPipValue.collectAsState()
    val execRewardPercent by viewModel.execRewardPercent.collectAsState()
    val kursRate by viewModel.kursIdr.collectAsState()

    // Calculations
    val totalRiskUsd = execBal * (riskPercent / 100.0)
    val totalRiskIdr = totalRiskUsd * kursRate

    val totalRewardUsd = execBal * (execRewardPercent / 100.0)
    val totalRewardIdr = totalRewardUsd * kursRate

    // Formula: Suggested Lot = Risk $ / (Stop Loss pips * Pip Value per standard lot)
    val divider = stopLossVal * pipVal
    val computedLot = if (divider > 0) totalRiskUsd / divider else 0.0

    // Target TP in Pips = Reward USD / (Suggested Lot * Pip Value)
    val lotValueFactor = computedLot * pipVal
    val calculatedTpPips = if (lotValueFactor > 0.0) totalRewardUsd / lotValueFactor else 0.0

    // Design risk advice content dynamically
    val (riskLabel, riskAdviceColor, riskAdviceText) = when {
        riskPercent <= 2.0 -> Triple("CONSERVATIVE", NeonGreen, "Low risk profile aligned with standard SMC trading. Excellent for account funding and prop firm milestones.")
        riskPercent <= 5.0 -> Triple("MODERATE", ElectricBlue, "Moderate risk exposure. Ensure Stop Loss matches structural order blocks or liquidity sweeps.")
        else -> Triple("AGGRESSIVE WARNING", CrimsonRed, "High risk exposure! Standard SMC methods recommend max 1-2% risk per execution. Be prepared for severe drawdowns.")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. TITLE ROW
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "RISK CALCULATOR",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "INSTANT POSITION & LOT EXECUTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // 2. PRIMARY OUTPUT CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, riskAdviceColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .testTag("execution_output_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "SUGGESTED LOT SIZE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = String.format(Locale.US, "%,.2f Lots", computedLot),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = riskAdviceColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MONEY AT RISK",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = FormatHelper.formatUsd(totalRiskUsd),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = FormatHelper.formatIdr(totalRiskUsd, kursRate),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "RISK EXPOSURE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$riskPercent%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskAdviceColor
                                )
                                Text(
                                    text = "of total account",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF333333)))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MONEY AT REWARD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = FormatHelper.formatUsd(totalRewardUsd),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = FormatHelper.formatIdr(totalRewardUsd, kursRate),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "REWARD EXPOSURE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", execRewardPercent),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "Suggested TP: ${String.format(Locale.US, "%.1f", calculatedTpPips)} Pips",
                                    fontSize = 11.sp,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. INPUT CONFIGS
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "RISK & REWARD PARAMETERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp
                    )
 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SMCInputCard(
                            label = "BALANCE ($)",
                            value = execBal,
                            onValueChange = { viewModel.execBalance.value = it },
                            subText = FormatHelper.formatIdr(execBal, kursRate),
                            modifier = Modifier.weight(1f)
                        )
                        SMCInputCard(
                            label = "RISK (%)",
                            value = riskPercent,
                            onValueChange = { viewModel.execRiskPercent.value = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SMCInputCard(
                            label = "STOP LOSS (PIPS)",
                            value = stopLossVal,
                            onValueChange = { viewModel.execStopLoss.value = it },
                            modifier = Modifier.weight(1f)
                        )
                        SMCInputCard(
                            label = "PIP VALUE ($/LOT)",
                            value = pipVal,
                            onValueChange = { viewModel.execPipValue.value = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SMCInputCard(
                            label = "REWARD (%)",
                            value = execRewardPercent,
                            onValueChange = { viewModel.execRewardPercent.value = it },
                            modifier = Modifier.weight(1f)
                        )
                        SMCInputCard(
                            label = "TAKE PROFIT (PIPS)",
                            value = calculatedTpPips,
                            onValueChange = { targetPipsInput ->
                                val currentFactor = computedLot * pipVal
                                if (execBal > 0.0 && currentFactor > 0.0) {
                                    val derivedRewardUsd = targetPipsInput * currentFactor
                                    viewModel.execRewardPercent.value = (derivedRewardUsd / execBal) * 100.0
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. PRESETS FOR FAST INPUTS
            item {
                Column {
                    Text(
                        text = "PIP VALUE PRESETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            Triple("EUR/USD ($10)", 10.0, "Standard Forex pips"),
                            Triple("XAU/USD ($10)", 10.0, "Gold Spot lot rate"),
                            Triple("CRYPTO ($1)", 1.0, "Micro Crypto Lot size")
                        )
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBackground)
                                    .border(1.dp, if (pipVal == preset.second) ElectricBlue else Color(0xFF333333), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.execPipValue.value = preset.second
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = preset.first,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pipVal == preset.second) ElectricBlue else TextPrimary
                                    )
                                    Text(
                                        text = preset.third,
                                        fontSize = 8.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. SMC RISK ADVICE ADVISORY
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(riskAdviceColor.copy(alpha = 0.06f))
                        .border(1.dp, riskAdviceColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (riskPercent > 5.0) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = "Risk advice level indicator status",
                            tint = riskAdviceColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RISK PROFILE: $riskLabel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskAdviceColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = riskAdviceText,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Padding gap for bottom navigation bars
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
