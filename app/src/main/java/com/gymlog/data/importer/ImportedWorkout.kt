package com.gymlog.data.importer

data class ImportedWorkoutSession(
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val exercises: List<ImportedExercise>,
)

data class ImportedExercise(
    val name: String,
    val targetArea: String,
    val defaultRestSeconds: Int,
    val sets: List<ImportedSet>,
)

data class ImportedSet(
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = true,
)
