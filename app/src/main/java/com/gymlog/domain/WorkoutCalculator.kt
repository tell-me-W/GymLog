package com.gymlog.domain

data class WorkoutSetInput(
    val weightKg: Double,
    val reps: Int,
)

data class WorkoutSummary(
    val totalVolumeKg: Double,
    val durationSeconds: Long,
    val exerciseCount: Int,
    val setCount: Int,
)

object WorkoutCalculator {
    fun totalVolume(sets: List<WorkoutSetInput>): Double {
        return sets.sumOf { it.weightKg * it.reps }
    }

    fun summarizeSession(
        sets: List<WorkoutSetInput>,
        exerciseCount: Int,
        startedAtMillis: Long,
        endedAtMillis: Long,
    ): WorkoutSummary {
        return WorkoutSummary(
            totalVolumeKg = totalVolume(sets),
            durationSeconds = ((endedAtMillis - startedAtMillis) / 1000).coerceAtLeast(0),
            exerciseCount = exerciseCount,
            setCount = sets.size,
        )
    }
}
