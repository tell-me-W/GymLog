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

    fun nextSetDefaults(previousSet: WorkoutSetInput?): WorkoutSetInput {
        return previousSet ?: WorkoutSetInput(weightKg = 0.0, reps = 0)
    }

    fun canRemoveLastSet(setCount: Int): Boolean {
        return setCount > 1
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
