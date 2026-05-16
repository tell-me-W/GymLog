package com.gymlog.data.repository

import com.gymlog.data.local.ExerciseDao
import com.gymlog.data.local.ExerciseEntity
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
) {
    fun observeExercises(targetArea: String? = null): Flow<List<ExerciseEntity>> {
        return if (targetArea == null) {
            exerciseDao.observeExercises()
        } else {
            exerciseDao.observeExercises(targetArea)
        }
    }

    suspend fun seedDefaultsIfEmpty() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertExercises(SeedExercises.defaults)
        }
    }

    suspend fun addCustomExercise(
        name: String,
        targetArea: String,
        defaultRestSeconds: Int,
    ): Long {
        return exerciseDao.insertExercise(
            ExerciseEntity(
                name = name.trim(),
                targetArea = targetArea,
                isCustom = true,
                defaultRestSeconds = defaultRestSeconds.coerceAtLeast(0),
            )
        )
    }
}
