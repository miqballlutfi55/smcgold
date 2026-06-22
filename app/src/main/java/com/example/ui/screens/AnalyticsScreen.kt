package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.DayResult
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
fun AnalyticsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val projections by viewModel.projectionResult.collectAsState()
    val initBalance by viewModel.initialBalance.collectAsState()
    val rate by viewModel.kursIdr.collectAsState()

    val daysList = projections.daysList
    
    // 1. Calculate stats
    val totalDays = daysList.size
    val winDays = daysList.count { it.resultType == DayResult.WIN || it.resultType == DayResult.PROJECTED }
    val lossDays = daysList.count { it.resultType == DayResult.LOSS }
    val winRate = if (totalDays > 0) (winDays.toDouble() / totalDays.toDouble()) * 100.0 else 0.0

    // Peak Drawdown calculations
    var peakVal = initBalance
    var maxDrawdownPct = 0.0
    for (day in daysList) {
        val currentEnd = day.endBalance
        if (currentEnd > peakVal) {
            peakVal = currentEnd
        } else {
            val ddValue = if (peakVal > 0) ((peakVal - currentEnd) / peakVal) * 100.0 else 0.0
            if (ddValue > maxDrawdownPct) {
                maxDrawdownPct = ddValue
            }
        }
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
            // Title
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "ANALYTICS DESK",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "RISK COMPLIANCE & PERFORMANCE STATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // High priority line chart card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                        .testTag("growth_curve_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "COMPOUND GROWTH CURVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cumulative balance curve progression",
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom canvas chart
                        if (daysList.isNotEmpty()) {
                            SMCGrowthCurveChart(
                                initialBalance = initBalance,
                                balances = daysList.map { it.endBalance },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No compounding timeline data available.", color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Side by Side Stats: Win Rate Circular Ring Gauge & Drawdown Analysis
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Win rate circular gauge card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier
                            .weight(1.1f)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                            .testTag("win_rate_gauge_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TRADING WIN RATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(90.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 10.dp.toPx()
                                    
                                    // Raw background ring
                                    drawCircle(
                                        color = CrimsonRed.copy(alpha = 0.15f),
                                        style = Stroke(width = strokeWidth)
                                    )
                                    
                                    // Active sweep percentage for Win Rate
                                    val winSweepAngle = (winRate.toFloat() / 100f) * 360f
                                    drawArc(
                                        color = NeonGreen,
                                        startAngle = -90f,
                                        sweepAngle = winSweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%%", winRate),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                    Text(
                                        text = "$winDays W - $lossDays L",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Based on active timeline status",
                                fontSize = 8.sp,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Max Drawdown card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                            .testTag("drawdown_metric_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "PEAK DRAWDOWN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = if (maxDrawdownPct > 0.0) String.format(Locale.US, "%.2f%%", maxDrawdownPct) else "0.00%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (maxDrawdownPct > 5.0) CrimsonRed else ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Worst decline from previous equity peaks",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (maxDrawdownPct > 10.0) CrimsonRed.copy(alpha = 0.1f) else Color(0xFF161C2C))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (maxDrawdownPct > 10.0) CrimsonRed else ElectricBlue,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (maxDrawdownPct > 10.0) "HIGH DRAWDOWN" else "SAFE DRAWDOWN",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (maxDrawdownPct > 10.0) CrimsonRed else ElectricBlue
                                )
                            }
                        }
                    }
                }
            }

            // Summary metrics grid
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "METRIC BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("INITIAL PORTFOLIO", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(FormatHelper.formatUsd(initBalance), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(FormatHelper.formatIdr(initBalance, rate), color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("FINAL BALANCE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(FormatHelper.formatUsd(projections.finalBalance), color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(FormatHelper.formatIdr(projections.finalBalance, rate), color = TextSecondary, fontSize = 9.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TOTAL PROJECTIONS RUN", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$totalDays Days / Trades", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("NET CAPITAL CHANGE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("+" + String.format(Locale.US, "%,.2f%%", projections.totalReturnPercentage), color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Padding gap
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SMCGrowthCurveChart(
    initialBalance: Double,
    balances: List<Double>,
    modifier: Modifier = Modifier
) {
    val fullDataPoints = remember(initialBalance, balances) {
        listOf(initialBalance) + balances
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Define padding for axes and labels
        val paddingLeft = 48.dp.toPx()
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val paddingRight = 12.dp.toPx()

        val drawWidth = width - paddingLeft - paddingRight
        val drawHeight = height - paddingTop - paddingBottom

        val minVal = fullDataPoints.minOrNull() ?: 0.0
        val maxVal = fullDataPoints.maxOrNull() ?: 1.0
        val delta = if (maxVal - minVal > 0) maxVal - minVal else 1.0

        val pointsCount = fullDataPoints.size

        // Map data index to screen coordinates within drawing area
        fun getCoordinates(index: Int, value: Double): Offset {
            if (pointsCount <= 1) return Offset(paddingLeft, paddingTop + drawHeight)
            val x = paddingLeft + index * (drawWidth / (pointsCount - 1))
            val normY = (value - minVal) / delta
            val y = paddingTop + drawHeight - (normY * drawHeight).toFloat()
            return Offset(x, y)
        }

        // Draw Axes lines (Y and X axis)
        // Y Axis line
        drawLine(
            color = Color(0xFF444444),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, paddingTop + drawHeight),
            strokeWidth = 1.dp.toPx()
        )
        // X Axis line
        drawLine(
            color = Color(0xFF444444),
            start = Offset(paddingLeft, paddingTop + drawHeight),
            end = Offset(paddingLeft + drawWidth, paddingTop + drawHeight),
            strokeWidth = 1.dp.toPx()
        )

        // Draw light grid lines for min, mid, max values on Y axis
        val midVal = minVal + delta / 2.0
        val gridValues = listOf(minVal, midVal, maxVal)
        gridValues.forEach { gVal ->
            val coords = getCoordinates(0, gVal)
            drawLine(
                color = Color(0xFF222225),
                start = Offset(paddingLeft, coords.y),
                end = Offset(paddingLeft + drawWidth, coords.y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (pointsCount > 0) {
            val path = Path()
            val fillPath = Path()

            val startLoc = getCoordinates(0, fullDataPoints[0])
            path.moveTo(startLoc.x, startLoc.y)
            
            fillPath.moveTo(paddingLeft, paddingTop + drawHeight)
            fillPath.lineTo(startLoc.x, startLoc.y)

            for (i in 1 until pointsCount) {
                val point = getCoordinates(i, fullDataPoints[i])
                path.lineTo(point.x, point.y)
                fillPath.lineTo(point.x, point.y)
            }

            val endLoc = getCoordinates(pointsCount - 1, fullDataPoints.last())
            fillPath.lineTo(endLoc.x, paddingTop + drawHeight)
            fillPath.close()

            // Draw translucent gradient underneath the line
            val colorBrush = Brush.verticalGradient(
                colors = listOf(
                    ElectricBlue.copy(alpha = 0.35f),
                    ElectricBlue.copy(alpha = 0.001f)
                ),
                startY = paddingTop,
                endY = paddingTop + drawHeight
            )
            drawPath(path = fillPath, brush = colorBrush)

            // Draw core graph line
            drawPath(
                path = path,
                color = ElectricBlue,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw little point marker on the final day projection
            drawCircle(
                color = ElectricBlue,
                radius = 5.dp.toPx(),
                center = endLoc
            )
            drawCircle(
                color = NeonGreen,
                radius = 2.5.dp.toPx(),
                center = endLoc
            )

            // Draw Texts and Labels (using drawIntoCanvas with Android Paint)
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#A0A0A0")
                    textSize = 8.5.dp.toPx()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }

                // Draw Y axis labels: Max, Mid, Min Balance (representing Y)
                canvas.nativeCanvas.drawText(
                    FormatHelper.formatUsdDecimalCompact(maxVal),
                    paddingLeft - 6.dp.toPx(),
                    getCoordinates(0, maxVal).y + 3.dp.toPx(),
                    paint
                )

                canvas.nativeCanvas.drawText(
                    FormatHelper.formatUsdDecimalCompact(midVal),
                    paddingLeft - 6.dp.toPx(),
                    getCoordinates(0, midVal).y + 3.dp.toPx(),
                    paint
                )

                canvas.nativeCanvas.drawText(
                    FormatHelper.formatUsdDecimalCompact(minVal),
                    paddingLeft - 6.dp.toPx(),
                    getCoordinates(0, minVal).y + 3.dp.toPx(),
                    paint
                )

                // Draw X axis labels: Days progression (representing X)
                paint.apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                val lastDay = pointsCount - 1
                // Day 0
                canvas.nativeCanvas.drawText(
                    "D0",
                    paddingLeft,
                    paddingTop + drawHeight + 16.dp.toPx(),
                    paint
                )

                // Day Halfway
                if (lastDay > 4) {
                    val halfDay = lastDay / 2
                    val halfCoords = getCoordinates(halfDay, fullDataPoints[halfDay])
                    canvas.nativeCanvas.drawText(
                        "D$halfDay",
                        halfCoords.x,
                        paddingTop + drawHeight + 16.dp.toPx(),
                        paint
                    )
                }

                // Final Day
                canvas.nativeCanvas.drawText(
                    "D$lastDay",
                    paddingLeft + drawWidth,
                    paddingTop + drawHeight + 16.dp.toPx(),
                    paint
                )

                // Draw Axis Titles
                paint.apply {
                    color = android.graphics.Color.parseColor("#FFFFFF")
                    textSize = 7.5.dp.toPx()
                }
                
                // Y-Axis title: Balance ($)
                paint.textAlign = android.graphics.Paint.Align.LEFT
                canvas.nativeCanvas.drawText(
                    "Balance Growth (Y)",
                    paddingLeft + 4.dp.toPx(),
                    paddingTop + 10.dp.toPx(),
                    paint
                )

                // X-Axis title: Time (Days)
                paint.textAlign = android.graphics.Paint.Align.RIGHT
                canvas.nativeCanvas.drawText(
                    "Timeline Days (X)",
                    paddingLeft + drawWidth - 4.dp.toPx(),
                    paddingTop + drawHeight - 4.dp.toPx(),
                    paint
                )
            }
        }
    }
}
