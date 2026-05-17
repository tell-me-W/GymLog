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
        val existingExercises = exerciseDao.getAllExercises()
        SeedExercises.defaults.forEach { defaultExercise ->
            val existingWithSameName = existingExercises.filter { it.name == defaultExercise.name }
            if (existingWithSameName.isEmpty()) {
                exerciseDao.insertExercise(defaultExercise)
            } else {
                existingWithSameName
                    .filter { existing ->
                        existing.targetArea != defaultExercise.targetArea ||
                            existing.defaultRestSeconds != defaultExercise.defaultRestSeconds ||
                            existing.isCustom
                    }
                    .forEach { existing ->
                        exerciseDao.updateExercise(
                            existing.copy(
                                targetArea = defaultExercise.targetArea,
                                defaultRestSeconds = defaultExercise.defaultRestSeconds,
                                isCustom = false,
                            )
                        )
                    }
            }
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
