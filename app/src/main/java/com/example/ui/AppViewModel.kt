package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StrategyPlan
import com.example.data.StrategyPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.floor

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

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StrategyPlanRepository(database.strategyPlanDao())
    }

    // Saved plans form Room DB
    val savedPlans: StateFlow<List<StrategyPlan>> = repository.allPlans
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
        @Suppress("UNCHECKED_CAST")
        val overrides = array[7] as Map<Int, DayResult>
        
        calculateProjection(initBal, targetPipsVal, step, maxLotVal, daysCount, initLotVal, rr, overrides)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompoundProjectionResult(1000.0, 0.0, 0.0, emptyList())
    )

    // Tab 2: Execution Tool inputs
    val execBalance = MutableStateFlow(10000.0)
    val execRiskPercent = MutableStateFlow(2.0)
    val execStopLoss = MutableStateFlow(20.0)
    val execPipValue = MutableStateFlow(10.0) // USD per standard lot per pip
    val execRewardPercent = MutableStateFlow(6.0) // Target reward exposure %

    private fun calculateProjection(
        initialBal: Double,
        pips: Double,
        step: Double,
        maxLotVal: Double,
        days: Int,
        initLotVal: Double,
        rr: Double,
        overrides: Map<Int, DayResult>
    ): CompoundProjectionResult {
        val list = ArrayList<DayCalculation>()
        var currentBalance = initialBal
        val resolvedInitLot = if (initLotVal <= 0.0) 0.01 else initLotVal

        for (day in 1..days) {
            val steps = if (step > 0.0) maxOf(1.0, floor(currentBalance / step)) else 1.0
            var currentLot = steps * resolvedInitLot
            if (currentLot < resolvedInitLot) currentLot = resolvedInitLot
            if (currentLot > maxLotVal) currentLot = maxLotVal

            val winAmount = currentLot * pips * 10.0
            val lossAmount = winAmount / rr

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
}
