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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StrategyPlan
import com.example.ui.AppViewModel
import com.example.ui.FormatHelper
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlansScreen(
    viewModel: AppViewModel,
    onPlanLoaded: () -> Unit, // Callback to switch tab back to Compound screen
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val savedPlans by viewModel.savedPlans.collectAsState()
    val loadedPlanId by viewModel.loadedPlanId.collectAsState()

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
            
            // TITLE BLOCK
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "STRATEGY PLANS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "SAVED COMPOUNDING SETUPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // LIST RENDERER
            if (savedPlans.isEmpty()) {
                item {
                    EmptyPlansState()
                }
            } else {
                items(savedPlans, key = { it.id }) { plan ->
                    SavedPlanCard(
                        plan = plan,
                        isSelected = loadedPlanId == plan.id,
                        onLoad = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.loadStrategyPlan(plan)
                            onPlanLoaded() // Execute tab switch
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deletePlan(plan)
                        }
                    )
                }
            }

            // Bottom gap under list
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SavedPlanCard(
    plan: StrategyPlan,
    isSelected: Boolean,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(plan.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(plan.timestamp))
    }

    val activeColor = if (isSelected) NeonGreen else OutlineColor

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, activeColor, RoundedCornerShape(20.dp))
            .testTag("strategy_card_${plan.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Name, Active Indicator & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Saved $dateStr",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F2618))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = NeonGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete stored strategy config",
                            tint = CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spec Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("START CAPITAL", fontSize = 9.sp, color = TextSecondary)
                    Text(FormatHelper.formatUsdCompact(plan.initialBalance), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("PIPS TARGET", fontSize = 9.sp, color = TextSecondary)
                    Text("${plan.targetPips.toInt()} pips", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("TIMELINE DAYS", fontSize = 9.sp, color = TextSecondary)
                    Text("${plan.totalDays} Days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("RISK REWARD", fontSize = 9.sp, color = TextSecondary)
                    Text("1 : ${FormatHelper.formatDecimal(plan.riskRewardRatio)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(1.dp).background(OutlineColor))
            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("KURS CONVERSION", fontSize = 8.sp, color = TextSecondary)
                    Text(FormatHelper.formatIdr(1.0, plan.kursIdr) + "/USD", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF1E2F26) else ElectricBlue,
                        contentColor = if (isSelected) NeonGreen else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(34.dp).testTag("load_plan_button_${plan.id}")
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.ElectricBolt else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelected) "RELOAD PARAMS" else "ACTIVATE PLAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPlansState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = "List icon placeholder for empty database state",
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "No Saved Compound Strategy Plans",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create new compounding plans on the 'Compound' tab, then click '+ SAVE STRATEGY PLAN' to store them locally.",
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 16.sp
        )
    }
}
