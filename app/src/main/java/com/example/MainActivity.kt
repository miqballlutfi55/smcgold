package com.example

import android.app.Application
import android.content.Context
import android.graphics.Canvas

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup continuous ViewModel
        viewModel = ViewModelProvider(this)[AppViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

sealed class NavigationTab(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Compound : NavigationTab(
        route = "compound",
        title = "Compound",
        icon = { Icon(Icons.Default.Build, contentDescription = "Calculations") }
    )
    object Execution : NavigationTab(
        route = "execution",
        title = "Jurnal",
        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Instant lot calculator") }
    )
    object Analytics : NavigationTab(
        route = "analytics",
        title = "Analytics",
        icon = { Icon(Icons.Default.Info, contentDescription = "Analytics stats") }
    )
    object Plans : NavigationTab(
        route = "plans",
        title = "Plans",
        icon = { Icon(Icons.Default.List, contentDescription = "Local strategy plans") }
    )
}

@Composable
fun MainAppContainer(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationTab.Compound.route
    val haptic = LocalHapticFeedback.current

    val tabsList = listOf(
        NavigationTab.Compound,
        NavigationTab.Execution,
        NavigationTab.Analytics,
        NavigationTab.Plans
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            // Curvaceous Floating Curved Bottom Navigation Bar inside 16.dp margin padding
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
            ) {
                tabsList.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = tab.icon,
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = ElectricBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = NavigationTab.Compound.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavigationTab.Compound.route) {
                    CompoundScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Execution.route) {
                    ExecutionScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Analytics.route) {
                    AnalyticsScreen(viewModel = viewModel)
                }
                composable(NavigationTab.Plans.route) {
                    PlansScreen(
                        viewModel = viewModel,
                        onPlanLoaded = {
                            navController.navigate(NavigationTab.Compound.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
class StrategyPlanRepository(private val dao: StrategyPlanDao) {
    val allPlans: Flow<List<StrategyPlan>> = dao.getAllPlans()

    suspend fun insert(plan: StrategyPlan): Long {
        return dao.insertPlan(plan)
    }

    suspend fun delete(plan: StrategyPlan) {
        dao.deletePlan(plan)
    }

    suspend fun clear() {
        dao.clearAll()
    }
}
@Dao
interface JournalPlanDao {
    @Query("SELECT * FROM journal_plans ORDER BY timestamp DESC")
    fun getAllJournalPlans(): Flow<List<JournalPlan>>

    @Insert
    suspend fun insertJournalPlan(plan: JournalPlan): Long

    @Delete
    suspend fun deleteJournalPlan(plan: JournalPlan)
}
@Database(entities = [StrategyPlan::class, JournalPlan::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun strategyPlanDao(): StrategyPlanDao
    abstract fun journalPlanDao(): JournalPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smc_trade_compounder_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
@Entity(tableName = "strategy_plans")
data class StrategyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val initialBalance: Double,
    val targetPips: Double,
    val stepSize: Double,
    val maxLot: Double,
    val totalDays: Int,
    val initialLot: Double,
    val riskRewardRatio: Double,
    val kursIdr: Double,
    val overridesData: String, // format: "1:WIN,2:LOSS"
    val timestamp: Long = System.currentTimeMillis()
)
@Dao
interface StrategyPlanDao {
    @Query("SELECT * FROM strategy_plans ORDER BY timestamp DESC")
    fun getAllPlans(): Flow<List<StrategyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StrategyPlan): Long

    @Delete
    suspend fun deletePlan(plan: StrategyPlan)

    @Query("DELETE FROM strategy_plans")
    suspend fun clearAll()
}
@Entity(tableName = "journal_plans")
data class JournalPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val initialBalance: Double,
    val riskPercent: Double,
    val stopLoss: Double,
    val pipValue: Double,
    val pipPreset: String,
    val rewardPercent: Double,
    val journalDays: Int,
    val overridesData: String, // format: "1:1,2:2" (day:state, state is 1=Win, 2=Loss)
    val timestamp: Long = System.currentTimeMillis()
)
class JournalPlanRepository(private val dao: JournalPlanDao) {
    val allPlans: Flow<List<JournalPlan>> = dao.getAllJournalPlans()

    suspend fun insertPlan(plan: JournalPlan): Long {
        return dao.insertJournalPlan(plan)
    }

    suspend fun deletePlan(plan: JournalPlan) {
        dao.deleteJournalPlan(plan)
    }
}
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                        androidx.compose.material3.IconButton(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                com.example.JournalExportHelper.exportCompoundToCsv(context, daysList)
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFF0F2618), RoundedCornerShape(8.dp))
                        ) {
                            Text("CSV", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.IconButton(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                com.example.JournalExportHelper.exportCompoundToPdf(context, daysList)
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFF260F0F), RoundedCornerShape(8.dp))
                        ) {
                            Text("PDF", color = CrimsonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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

        val minValRaw = fullDataPoints.minOrNull() ?: 0.0
        val maxValRaw = fullDataPoints.maxOrNull() ?: 1.0
        val deltaRaw = if (maxValRaw - minValRaw > 0) maxValRaw - minValRaw else 1.0
        val minVal = minValRaw - (deltaRaw * 0.10)
        val maxVal = maxValRaw + (deltaRaw * 0.10)
        val delta = maxVal - minVal

        val pointsCount = fullDataPoints.size

        val xPadding = 10.dp.toPx()
        val paddedDrawWidth = drawWidth - (xPadding * 2)

        // Map data index to screen coordinates within drawing area
        fun getCoordinates(index: Int, value: Double): Offset {
            if (pointsCount <= 1) return Offset(paddingLeft + xPadding, paddingTop + drawHeight)
            val x = paddingLeft + xPadding + index * (paddedDrawWidth / (pointsCount - 1))
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
        val midValRaw = minValRaw + deltaRaw / 2.0
        val gridValues = listOf(minValRaw, midValRaw, maxValRaw)
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
            
            fillPath.moveTo(startLoc.x, paddingTop + drawHeight)
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

            // Draw core graph line with segments based on profit/loss
            for (i in 1 until pointsCount) {
                val prevPoint = getCoordinates(i - 1, fullDataPoints[i - 1])
                val point = getCoordinates(i, fullDataPoints[i])
                val isLoss = fullDataPoints[i] < fullDataPoints[i - 1]
                
                drawLine(
                    color = if (isLoss) CrimsonRed else if (fullDataPoints[i] > fullDataPoints[i - 1]) NeonGreen else ElectricBlue,
                    start = prevPoint,
                    end = point,
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

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
                    FormatHelper.formatUsdDecimalCompact(maxValRaw),
                    paddingLeft - 6.dp.toPx(),
                    getCoordinates(0, maxValRaw).y + 3.dp.toPx(),
                    paint
                )

                canvas.nativeCanvas.drawText(
                    FormatHelper.formatUsdDecimalCompact(midValRaw),
                    paddingLeft - 6.dp.toPx(),
                    getCoordinates(0, midValRaw).y + 3.dp.toPx(),
                    paint
                )

                canvas.nativeCanvas.drawText(
                    FormatHelper.formatUsdDecimalCompact(minValRaw),
                    paddingLeft - 6.dp.toPx(),
                    getCoordinates(0, minValRaw).y + 3.dp.toPx(),
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
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = label.uppercase(Locale.ROOT),
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        
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
                cursorColor = ElectricBlue,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (subText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                color = ElectricBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// Parse text input stripped of commas back to a Double
private fun parseTextToDouble(text: String): Double {
    return text.replace(",", "").toDoubleOrNull() ?: 0.0
}

@Composable
fun SMCPresetDropdown(
    label: String,
    selectedValue: String,
    onValueSelected: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        Pair("EURUSD", 10.0),
        Pair("GBPUSD", 10.0),
        Pair("USDJPY", 10.0),
        Pair("AUDUSD", 10.0),
        Pair("USDCAD", 10.0),
        Pair("USDCHF", 10.0),
        Pair("NZDUSD", 10.0),
        Pair("XAUUSD", 10.0),
        Pair("BTCUSD", 1.0),
        Pair("ETHUSD", 1.0)
    )

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(12.dp)
            .height(56.dp)
    ) {
        Text(
            text = label.uppercase(Locale.ROOT),
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedValue,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBackground)
        ) {
            presets.forEach { preset ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { 
                        Text(
                            text = preset.first, 
                            color = TextPrimary,
                            fontWeight = if (selectedValue == preset.first) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    onClick = {
                        onValueSelected(preset.first, preset.second)
                        expanded = false
                    }
                )
            }
        }
    }
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
    var isInputsExpanded by remember { mutableStateOf(true) } // Always show inputs
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
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 10.dp) // padding remains on the text if desired, or we handle it in animated visibility
                            )
                            Icon(
                                imageVector = if (isInputsExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle parameter inputs",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(visible = isInputsExpanded) {
                            Column(
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

                                // Row 5: PIP VALUE & PIP PRESET
                                val pipVal by viewModel.pipValue.collectAsState()
                                val pipPreset by viewModel.pipValuePreset.collectAsState()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SMCInputCard(
                                        label = "PIP VALUE",
                                        value = pipVal,
                                        onValueChange = { viewModel.pipValue.value = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SMCPresetDropdown(
                                        label = "PIP VALUE PRESET",
                                        selectedValue = pipPreset,
                                        onValueSelected = { presetName, newValue -> 
                                            viewModel.pipValuePreset.value = presetName
                                            viewModel.pipValue.value = newValue
                                        },
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        androidx.compose.material3.IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                com.example.JournalExportHelper.exportCompoundToCsv(context, projections.daysList)
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFF0F2618), RoundedCornerShape(8.dp))
                        ) {
                            Text("CSV", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                com.example.JournalExportHelper.exportCompoundToPdf(context, projections.daysList)
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFF260F0F), RoundedCornerShape(8.dp))
                        ) {
                            Text("PDF", color = CrimsonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = FormatHelper.formatUsd(dayCalculation.startBalance),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f Lot", dayCalculation.lotSize),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFECA332)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildAnnotatedString {
                        if (dayCalculation.resultType != com.example.DayResult.PROJECTED) {
                            append("${dayCalculation.resultType.name} • ")
                        }
                        append(FormatHelper.formatIdr(dayCalculation.startBalance, rate))
                    },
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
                text = FormatHelper.formatUsd(dayCalculation.endBalance),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = FormatHelper.formatIdr(dayCalculation.endBalance, rate),
                fontSize = 10.sp,
                color = Color(0xFFA0A0A0),
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    append(if (isLoss) "-" else "+")
                    append(FormatHelper.formatUsd(kotlin.math.abs(dayCalculation.profitLoss)))
                    append(" (")
                    append(FormatHelper.formatIdr(kotlin.math.abs(dayCalculation.profitLoss), rate))
                    append(")")
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = profitColor,
                textDecoration = if (isLoss) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}
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
    val journalDays by viewModel.journalDays.collectAsState()
    val journalTimeline by viewModel.journalProjectionResult.collectAsState()
    val pipPreset by viewModel.execPipValuePreset.collectAsState() 
    val currentLoadedName by viewModel.loadedJournalPlanName.collectAsState()
    
    var isInputsExpanded by remember { mutableStateOf(true) } // Default true so it's visible, user can toggle it
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var savePlanName by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            
            // 1. TITLE ROW
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = "RISK CALCULATOR",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "INSTANT POSITION & JURNAL TRADING",
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
                        .border(1.dp, riskAdviceColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .testTag("execution_output_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
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

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MONEY AT RISK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = FormatHelper.formatUsd(totalRiskUsd),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = FormatHelper.formatIdr(totalRiskUsd, kursRate),
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "RISK EXPOSURE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$riskPercent%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed
                                )
                                Text(
                                    text = "Suggested SL: ${stopLossVal} Pips",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF333333)))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MONEY AT REWARD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = FormatHelper.formatUsd(totalRewardUsd),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = FormatHelper.formatIdr(totalRewardUsd, kursRate),
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "REWARD EXPOSURE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", execRewardPercent),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "Suggested TP: ${String.format(Locale.US, "%.1f", calculatedTpPips)} Pips",
                                    fontSize = 10.sp,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
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
                                text = "RISK & REWARD PARAMETERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            androidx.compose.material3.Icon(
                                imageVector = if (isInputsExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle parameter inputs",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(visible = isInputsExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                    SMCPresetDropdown(
                                        label = "PIP VALUE PRESET",
                                        selectedValue = pipPreset,
                                        onValueSelected = { presetName, newValue -> 
                                            viewModel.execPipValuePreset.value = presetName
                                            viewModel.execPipValue.value = newValue
                                        },
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SMCInputCard(
                                        label = "JOURNAL DAYS",
                                        value = journalDays.toDouble(),
                                        onValueChange = { viewModel.journalDays.value = it.toInt() },
                                        isInteger = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SMCInputCard(
                                        label = "PIP VALUE",
                                        value = pipVal,
                                        onValueChange = { viewModel.execPipValue.value = it },
                                        modifier = Modifier.weight(1f)
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

            // 6. JOURNAL TIMELINE HEADER
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TRADING JOURNAL TIMELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                com.example.JournalExportHelper.exportToCsv(context, journalTimeline)
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFF0F2618), RoundedCornerShape(8.dp))
                        ) {
                            Text("CSV", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                com.example.JournalExportHelper.exportToPdf(context, journalTimeline)
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFF260F0F), RoundedCornerShape(8.dp))
                        ) {
                            Text("PDF", color = CrimsonRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showLoadDialog = true
                            },
                            modifier = Modifier.size(32.dp).background(ElectricBlue, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Load Journal",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                savePlanName = currentLoadedName ?: ""
                                showSaveDialog = true
                            },
                            modifier = Modifier.size(32.dp).background(NeonGreen, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Journal",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 7. JOURNAL TIMELINE ITEMS
            items(journalTimeline) { dayData ->
                val hapticLocal = LocalHapticFeedback.current
                val isProfit = dayData.state == 1
                val isLoss = dayData.state == 2
                val isEmpty = dayData.state == 0
                
                val cardColor = when {
                    isProfit -> NeonGreen.copy(alpha = 0.1f)
                    isLoss -> CrimsonRed.copy(alpha = 0.1f)
                    else -> CardBackground
                }
                
                val borderColor = when {
                    isProfit -> NeonGreen.copy(alpha = 0.5f)
                    isLoss -> CrimsonRed.copy(alpha = 0.5f)
                    else -> Color(0xFF333333)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            hapticLocal.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.cycleJournalDay(dayData.day)
                        }
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Column: Day Label + Stats if active
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DAY ${dayData.day}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isEmpty) TextSecondary else TextPrimary
                                )
                                if (!isEmpty) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isProfit) "PROFIT (${execRewardPercent}%)" else "LOSS (${riskPercent}%)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isProfit) NeonGreen else CrimsonRed
                                    )
                                }
                            }
                            
                            if (isEmpty) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to mark profit, double tap for loss (cumulative)",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        // Right Column: Values if active
                        if (!isEmpty) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatHelper.formatUsd(dayData.endBalance),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = FormatHelper.formatIdr(dayData.endBalance, kursRate),
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = (if (isProfit) "+" else "") + FormatHelper.formatUsd(dayData.pLossAmount) + 
                                           " (" + FormatHelper.formatIdr(kotlin.math.abs(dayData.pLossAmount), kursRate) + ")",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProfit) NeonGreen else CrimsonRed
                                )
                            }
                        }
                    }
                }
            }

            // Padding gap for bottom navigation bars
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // SAVE DIALOG
        if (showSaveDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                containerColor = CardBackground,
                title = { 
                    Text(
                        text = "Save Journal Strategy", 
                        color = TextPrimary, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp 
                    ) 
                },
                text = {
                    Column {
                        Text(
                            text = "Simpan parameter eksekusi jurnal saat ini.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = savePlanName,
                            onValueChange = { savePlanName = it },
                            label = { Text("Journal Plan Name") },
                            placeholder = { Text("e.g. Daily Gold Scalp") },
                            singleLine = true,
                            textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (savePlanName.isNotBlank()) {
                                viewModel.saveCurrentJournalPlan(savePlanName.trim())
                                showSaveDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showSaveDialog = false }
                    ) {
                        Text("Cancel", color = CrimsonRed, fontSize = 12.sp)
                    }
                }
            )
        }

        // LOAD DIALOG
        if (showLoadDialog) {
            val savedJournals by viewModel.savedJournalPlans.collectAsState()
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showLoadDialog = false },
                containerColor = CardBackground,
                title = { 
                    Text("Load Journal Plan", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) 
                },
                text = {
                    if (savedJournals.isEmpty()) {
                        Text("No saved journal plans found.", color = TextSecondary, fontSize = 13.sp)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(savedJournals) { plan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.loadJournalPlan(plan)
                                            showLoadDialog = false 
                                        }
                                        .padding(vertical = 12.dp)
                                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(plan.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Balance: ${FormatHelper.formatUsd(plan.initialBalance)}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteJournalPlan(plan) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showLoadDialog = false }) {
                        Text("Close", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            )
        }
    }
}
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
enum class DayResult {
    PROJECTED,
    WIN,
    LOSS
}
// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
  )
private val DarkColorScheme get() = darkColorScheme(
    primary = ElectricBlue,
    primaryContainer = ElectricBlueDark,
    secondary = NeonGreen,
    tertiary = CrimsonRed,
    background = DarkBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = OutlineColor,
    surfaceVariant = CardBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark theme by default
    dynamicColor: Boolean = false, // Disable dynamic colors to keep original branding scheme!
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
val DarkBackground = Color(0xFF0D0E12)
val CardBackground = Color(0xFF1E1E24)
val NeonGreen = Color(0xFF00FFA3)
val CrimsonRed = Color(0xFFFF3366)
val ElectricBlue = Color(0xFF2979FF)
val ElectricBlueDark = Color(0xFF1E5BBF)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0A0A0)
val OutlineColor = Color(0xFF333333)

// Additional support colors
val GoldGold = Color(0xFFFFD700)
val IceBlue = Color(0xFFD6E2FE)
val DarkBlueText = Color(0xFF072960)
val DarkGreenText = Color(0xFF1E8056)
data class DayCalculation(
    val day: Int,
    val startBalance: Double,
    val lotSize: Double,
    val profitLoss: Double,
    val endBalance: Double,
    val resultType: DayResult
)

data class CompoundProjectionResult(
    val finalBalance: Double,
    val totalProfit: Double,
    val totalReturnPercentage: Double,
    val daysList: List<DayCalculation>
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StrategyPlanRepository
    private val journalRepository: JournalPlanRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StrategyPlanRepository(database.strategyPlanDao())
        journalRepository = JournalPlanRepository(database.journalPlanDao())
    }

    // Saved plans form Room DB
    val savedPlans: StateFlow<List<StrategyPlan>> = repository.allPlans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedJournalPlans: StateFlow<List<JournalPlan>> = journalRepository.allPlans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current inputs for Compounding Screen
    val initialBalance = MutableStateFlow(1000.0)
    val targetPips = MutableStateFlow(100.0)
    val stepSize = MutableStateFlow(100.0)
    val maxLot = MutableStateFlow(50.0)
    val totalDays = MutableStateFlow(30)
    val initialLot = MutableStateFlow(0.01)
    val riskRewardRatio = MutableStateFlow(2.0)
    val kursIdr = MutableStateFlow(16200.0)
    val pipValue = MutableStateFlow(10.0)
    val pipValuePreset = MutableStateFlow("EURUSD")

    // Manual Day overrides: Map of day index (1-based) to DayResult
    private val _dayOverrides = MutableStateFlow<Map<Int, DayResult>>(emptyMap())
    val dayOverrides = _dayOverrides.asStateFlow()

    // Loaded Strategy Plan ID tracking
    private val _loadedPlanId = MutableStateFlow<Int?>(null)
    val loadedPlanId = _loadedPlanId.asStateFlow()
    private val _loadedPlanName = MutableStateFlow<String?>(null)
    val loadedPlanName = _loadedPlanName.asStateFlow()

    // Calculated Compounding Projection Reactively
    val projectionResult: StateFlow<CompoundProjectionResult> = combine(
        listOf(
            initialBalance,
            targetPips,
            stepSize,
            maxLot,
            totalDays,
            initialLot,
            riskRewardRatio,
            pipValue,
            _dayOverrides
        )
    ) { array ->
        val initBal = array[0] as Double
        val targetPipsVal = array[1] as Double
        val step = array[2] as Double
        val maxLotVal = array[3] as Double
        val daysCount = array[4] as Int
        val initLotVal = array[5] as Double
        val rr = array[6] as Double
        val pipVal = array[7] as Double
        @Suppress("UNCHECKED_CAST")
        val overrides = array[8] as Map<Int, DayResult>
        
        calculateProjection(initBal, targetPipsVal, step, maxLotVal, daysCount, initLotVal, rr, pipVal, overrides)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompoundProjectionResult(1000.0, 0.0, 0.0, emptyList())
    )

    // Tab 2: Journal (formerly Execution) Tool inputs
    val execBalance = MutableStateFlow(10000.0)
    val execRiskPercent = MutableStateFlow(2.0)
    val execStopLoss = MutableStateFlow(20.0)
    val execPipValue = MutableStateFlow(10.0) // USD per standard lot per pip
    val execPipValuePreset = MutableStateFlow("EURUSD")
    val execRewardPercent = MutableStateFlow(6.0) // Target reward exposure %

    // Journal Timeline Tracking
    val journalDays = MutableStateFlow(30)
    private val _journalOverrides = MutableStateFlow<Map<Int, Int>>(emptyMap()) // 0 = Empty, 1 = Win, 2 = Loss
    val journalOverrides = _journalOverrides.asStateFlow()

    // Persistent Journal Plan ID tracking
    private val _loadedJournalPlanId = MutableStateFlow<Int?>(null)
    val loadedJournalPlanId = _loadedJournalPlanId.asStateFlow()
    private val _loadedJournalPlanName = MutableStateFlow<String?>(null)
    val loadedJournalPlanName = _loadedJournalPlanName.asStateFlow()

    fun cycleJournalDay(dayIndex: Int) {
        val currentMap = _journalOverrides.value.toMutableMap()
        val currentMode = currentMap[dayIndex] ?: 0
        val nextMode = when (currentMode) {
            0 -> 1 // Win
            1 -> 2 // Loss
            else -> 0 // Empty
        }
        currentMap[dayIndex] = nextMode
        _journalOverrides.value = currentMap
        _loadedJournalPlanId.value = null
        _loadedJournalPlanName.value = null
    }

    data class JournalDayCalc(
        val day: Int,
        val state: Int,
        val startBalance: Double,
        val pLossAmount: Double,
        val endBalance: Double
    )

    val journalProjectionResult: StateFlow<List<JournalDayCalc>> = combine(
        listOf(execBalance, execRiskPercent, execRewardPercent, journalDays, _journalOverrides)
    ) { array ->
        val startBal = array[0] as Double
        val riskPct = array[1] as Double
        val rewardPct = array[2] as Double
        val days = array[3] as Int
        @Suppress("UNCHECKED_CAST")
        val overrides = array[4] as Map<Int, Int>

        val list = ArrayList<JournalDayCalc>()
        var currentBalance = startBal

        for (day in 1..days) {
            val state = overrides[day] ?: 0
            val pLoss = when (state) {
                1 -> currentBalance * (rewardPct / 100.0)
                2 -> -(currentBalance * (riskPct / 100.0))
                else -> 0.0
            }
            
            val endBal = currentBalance + pLoss
            list.add(
                JournalDayCalc(
                    day = day,
                    state = state,
                    startBalance = currentBalance,
                    pLossAmount = pLoss,
                    endBalance = endBal
                )
            )
            currentBalance = endBal
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun calculateProjection(
        initialBal: Double,
        pips: Double,
        step: Double,
        maxLotVal: Double,
        days: Int,
        initLotVal: Double,
        rr: Double,
        pipVal: Double,
        overrides: Map<Int, DayResult>
    ): CompoundProjectionResult {
        val list = ArrayList<DayCalculation>()
        var currentBalance = initialBal
        val resolvedInitLot = if (initLotVal <= 0.0) 0.01 else initLotVal
        val resolvedRr = if (rr <= 0.0) 1.0 else rr

        for (day in 1..days) {
            val steps = if (step > 0.0) maxOf(1.0, floor(currentBalance / step)) else 1.0
            var currentLot = steps * resolvedInitLot
            if (currentLot < resolvedInitLot) currentLot = resolvedInitLot
            if (currentLot > maxLotVal) currentLot = maxLotVal

            val winAmount = currentLot * pips * pipVal
            val lossAmount = winAmount / resolvedRr

            val overrideResult = overrides[day] ?: DayResult.PROJECTED

            val pLoss = when (overrideResult) {
                DayResult.PROJECTED, DayResult.WIN -> winAmount
                DayResult.LOSS -> -lossAmount
            }

            val startBal = currentBalance
            val endBal = currentBalance + pLoss

            list.add(
                DayCalculation(
                    day = day,
                    startBalance = startBal,
                    lotSize = currentLot,
                    profitLoss = pLoss,
                    endBalance = endBal,
                    resultType = overrideResult
                )
            )

            currentBalance = endBal
        }

        val totalProfit = currentBalance - initialBal
        val totalReturn = if (initialBal > 0.0) (totalProfit / initialBal) * 100.0 else 0.0

        return CompoundProjectionResult(
            finalBalance = currentBalance,
            totalProfit = totalProfit,
            totalReturnPercentage = totalReturn,
            daysList = list
        )
    }

    // Toggle specific day override result type (PROJECTED -> WIN -> LOSS -> PROJECTED)
    fun cycleDayOverride(dayIndex: Int) {
        val currentMap = _dayOverrides.value.toMutableMap()
        val currentMode = currentMap[dayIndex] ?: DayResult.PROJECTED
        val nextMode = when (currentMode) {
            DayResult.PROJECTED -> DayResult.WIN
            DayResult.WIN -> DayResult.LOSS
            DayResult.LOSS -> DayResult.PROJECTED
        }
        currentMap[dayIndex] = nextMode
        _dayOverrides.value = currentMap
        _loadedPlanId.value = null // marked modified
        _loadedPlanName.value = null
    }

    // Reset all overrides and inputs
    fun resetProjection() {
        _dayOverrides.value = emptyMap()
        initialBalance.value = 1000.0
        targetPips.value = 100.0
        stepSize.value = 100.0
        maxLot.value = 50.0
        totalDays.value = 30
        initialLot.value = 0.01
        riskRewardRatio.value = 2.0
        _loadedPlanId.value = null
        _loadedPlanName.value = null
    }

    // Save current configuration and performance timeline overrides as a named strategy
    fun saveStrategyPlan(planName: String) {
        viewModelScope.launch {
            val serializedOverrides = _dayOverrides.value.entries.joinToString(",") { "${it.key}:${it.value.name}" }
            val plan = StrategyPlan(
                name = planName,
                initialBalance = initialBalance.value,
                targetPips = targetPips.value,
                stepSize = stepSize.value,
                maxLot = maxLot.value,
                totalDays = totalDays.value,
                initialLot = initialLot.value,
                riskRewardRatio = riskRewardRatio.value,
                kursIdr = kursIdr.value,
                overridesData = serializedOverrides
            )
            val newId = repository.insert(plan)
            _loadedPlanId.value = newId.toInt()
            _loadedPlanName.value = planName
        }
    }

    // Load strategy plan into current variables
    fun loadStrategyPlan(plan: StrategyPlan) {
        initialBalance.value = plan.initialBalance
        targetPips.value = plan.targetPips
        stepSize.value = plan.stepSize
        maxLot.value = plan.maxLot
        totalDays.value = plan.totalDays
        initialLot.value = plan.initialLot
        riskRewardRatio.value = plan.riskRewardRatio
        kursIdr.value = plan.kursIdr

        // parse overridesData
        val overridesMap = mutableMapOf<Int, DayResult>()
        if (plan.overridesData.isNotEmpty()) {
            plan.overridesData.split(",").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val dayIdx = parts[0].toIntOrNull()
                    val resultType = runCatching { DayResult.valueOf(parts[1]) }.getOrNull()
                    if (dayIdx != null && resultType != null) {
                        overridesMap[dayIdx] = resultType
                    }
                }
            }
        }
        _dayOverrides.value = overridesMap
        _loadedPlanId.value = plan.id
        _loadedPlanName.value = plan.name
    }

    // Delete stored strategy plan
    fun deletePlan(plan: StrategyPlan) {
        viewModelScope.launch {
            repository.delete(plan)
            if (_loadedPlanId.value == plan.id) {
                _loadedPlanId.value = null
                _loadedPlanName.value = null
            }
        }
    }

    // Save Journal configuration
    fun saveCurrentJournalPlan(planName: String) {
        viewModelScope.launch {
            val overridesStr = _journalOverrides.value.entries.joinToString(",") { "${it.key}:${it.value}" }
            
            val plan = JournalPlan(
                id = _loadedJournalPlanId.value ?: 0,
                name = planName,
                initialBalance = execBalance.value,
                riskPercent = execRiskPercent.value,
                stopLoss = execStopLoss.value,
                pipValue = execPipValue.value,
                pipPreset = execPipValuePreset.value,
                rewardPercent = execRewardPercent.value,
                journalDays = journalDays.value,
                overridesData = overridesStr
            )
            
            val newId = journalRepository.insertPlan(plan)
            _loadedJournalPlanId.value = newId.toInt()
            _loadedJournalPlanName.value = planName
        }
    }

    // Load Journal configuration
    fun loadJournalPlan(plan: JournalPlan) {
        execBalance.value = plan.initialBalance
        execRiskPercent.value = plan.riskPercent
        execStopLoss.value = plan.stopLoss
        execPipValue.value = plan.pipValue
        execPipValuePreset.value = plan.pipPreset
        execRewardPercent.value = plan.rewardPercent
        journalDays.value = plan.journalDays

        val overridesMap = mutableMapOf<Int, Int>()
        if (plan.overridesData.isNotEmpty()) {
            plan.overridesData.split(",").forEach { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val dayIdx = parts[0].toIntOrNull()
                    val stateCode = parts[1].toIntOrNull()
                    if (dayIdx != null && stateCode != null) {
                        overridesMap[dayIdx] = stateCode
                    }
                }
            }
        }
        _journalOverrides.value = overridesMap
        _loadedJournalPlanId.value = plan.id
        _loadedJournalPlanName.value = plan.name
    }

    // Delete stored journal plan
    fun deleteJournalPlan(plan: JournalPlan) {
        viewModelScope.launch {
            journalRepository.deletePlan(plan)
            if (_loadedJournalPlanId.value == plan.id) {
                _loadedJournalPlanId.value = null
                _loadedJournalPlanName.value = null
            }
        }
    }
}
object JournalExportHelper {

    fun exportToCsv(context: Context, history: List<AppViewModel.JournalDayCalc>) {
        try {
            val sb = java.lang.StringBuilder()
            sb.append("Day,Status,StartBalance,PnL,EndBalance\n")
            for (day in history) {
                if (day.state == 0) continue // Skip empty days
                val statusStr = if (day.state == 1) "WIN" else "LOSS"
                sb.append("${day.day},$statusStr,${day.startBalance},${day.pLossAmount},${day.endBalance}\n")
            }
            val fileName = "JournalExport_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            outputStream.write(sb.toString().toByteArray())
            outputStream.close()
            Toast.makeText(context, "Exported CSV to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting CSV", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportToPdf(context: Context, history: List<AppViewModel.JournalDayCalc>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size 72 PPI
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas: Canvas = page.canvas
            val paint = Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            
            canvas.drawText("SMC Trading Journal Report", 50f, 50f, paint)
            
            var yPos = 80f
            canvas.drawText("Day | Status | Start Balance | PnL | End Balance", 50f, yPos, paint)
            yPos += 20f
            
            for (day in history) {
                if (day.state == 0) continue
                
                val statusStr = if (day.state == 1) "WIN" else "LOSS"
                val startStr = String.format(Locale.US, "$%.2f", day.startBalance)
                val pnlStr = String.format(Locale.US, "$%.2f", day.pLossAmount)
                val endStr = String.format(Locale.US, "$%.2f", day.endBalance)
                
                canvas.drawText("${day.day} | $statusStr | $startStr | $pnlStr | $endStr", 50f, yPos, paint)
                yPos += 20f
                
                if (yPos > 800f) {
                     // Normally you'd create a new page, but this is a simplified demo
                     canvas.drawText("... more entries truncated", 50f, yPos, paint)
                     break
                }
            }
            
            pdfDocument.finishPage(page)
            
            val fileName = "JournalExport_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            Toast.makeText(context, "Exported PDF to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportCompoundToCsv(context: Context, history: List<DayCalculation>) {
        try {
            val sb = java.lang.StringBuilder()
            sb.append("Day,StartBalance,LotSize,PnL,EndBalance,Result\n")
            for (day in history) {
                sb.append("${day.day},${day.startBalance},${day.lotSize},${day.profitLoss},${day.endBalance},${day.resultType.name}\n")
            }
            val fileName = "CompoundExport_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            outputStream.write(sb.toString().toByteArray())
            outputStream.close()
            Toast.makeText(context, "Exported CSV to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting CSV", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportCompoundToPdf(context: Context, history: List<DayCalculation>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas: Canvas = page.canvas
            val paint = Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 10f
            
            canvas.drawText("SMC Compounding Plan Report", 50f, 50f, paint)
            
            var yPos = 80f
            canvas.drawText("Day | Start Bal | Lot | PnL | End Bal | Status", 50f, yPos, paint)
            yPos += 20f
            
            for (day in history) {
                val startStr = String.format(Locale.US, "$%.2f", day.startBalance)
                val lotStr = String.format(Locale.US, "%.2f", day.lotSize)
                val pnlSign = if (day.profitLoss >= 0) "+" else ""
                val pnlStr = String.format(Locale.US, "$pnlSign$%.2f", day.profitLoss)
                val endStr = String.format(Locale.US, "$%.2f", day.endBalance)
                
                canvas.drawText("${day.day} | $startStr | $lotStr | $pnlStr | $endStr | ${day.resultType.name}", 50f, yPos, paint)
                yPos += 15f
                
                if (yPos > 800f) {
                     canvas.drawText("... more entries truncated", 50f, yPos, paint)
                     break
                }
            }
            
            pdfDocument.finishPage(page)
            
            val fileName = "CompoundExport_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            Toast.makeText(context, "Exported PDF to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
