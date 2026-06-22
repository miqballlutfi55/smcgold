package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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
