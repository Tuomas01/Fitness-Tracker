package com.example.fitnesstracker.supabase

import android.util.Log
import com.example.fitnesstracker.ui.screens.training.TrainingPlan
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TrainingRepository {
    suspend fun getAllTrainingPlans(): List<TrainingPlan>?
    suspend fun getTrainingPlan(id: Int): TrainingPlan
    suspend fun addPlanToUser(planId: Int, userId: String)
}

class TrainingRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : TrainingRepository {

    override suspend fun getAllTrainingPlans(): List<TrainingPlan>? {
        try {
            return withContext(Dispatchers.IO) {
                val result = postgrest.from("training_plans")
                    .select(columns = Columns.list("id, name, type, target_area")).decodeList<TrainingPlan>()
                Log.d("TrainingRepo", "getAllTrainingPlans() test: $result")
                result
            }
        } catch (e: Exception) {
            Log.d("TrainingRepo", "getAllTrainingPlans() error: $e")
        }
        return null
    }

    override suspend fun getTrainingPlan(id: Int): TrainingPlan {
        try {
            return withContext(Dispatchers.IO) {
                val result = postgrest.from("training_plans")
                    .select(columns = Columns.list("id, name, type, target_area")) {
                        filter {
                            eq("id" , id)
                        }
                }
                Log.d("TrainingRepo", "getTrainingPlan() test: $result \n decoding: ${result.decodeSingle<TrainingPlan>()}")
                result.decodeSingle()
            }
        } catch (e: Exception) {
            Log.d("TrainingRepo", "getTrainingPlan() error: $e")
        }
        return TrainingPlan()
    }

    override suspend fun addPlanToUser(planId: Int, userId: String) {

    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TrainingModule {

    @Binds
    abstract fun bindTrainingRepository(
        trainingRepositoryImpl: TrainingRepositoryImpl
    ): TrainingRepository
}
