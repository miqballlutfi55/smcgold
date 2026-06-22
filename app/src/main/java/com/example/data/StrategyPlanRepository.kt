package com.example.data

import kotlinx.coroutines.flow.Flow

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
