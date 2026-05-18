package com.gymlog.data.importer

import com.gymlog.data.local.ExerciseInputType

data class ImportedWorkoutSession(
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val exercises: List<ImportedExercise>,
)

data class ImportedExercise(
    val name: String,
    val targetArea: String,
    val defaultRestSeconds: Int,
    val inputType: ExerciseInputType = ExerciseInputType.REPS,
    val sets: List<ImportedSet>,
)

data class ImportedSet(
    val weightKg: Double,
    val reps: Int,
    val durationSeconds: Int = 0,
    val isCompleted: Boolean = true,
)
