package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
