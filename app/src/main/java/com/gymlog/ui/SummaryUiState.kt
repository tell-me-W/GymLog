package com.gymlog.ui

import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.domain.WorkoutCalculator
import com.gymlog.domain.WorkoutSetInput

data class SummaryUiState(
    val totalVolumeKg: Double,
    val durationSeconds: Long,
    val exerciseCount: Int,
    val setCount: Int,
    val completedAtMillis: Long = System.currentTimeMillis(),
)

internal fun WorkoutSessionWithExercises.toSummaryUiState(nowMillis: Long = System.currentTimeMillis()): SummaryUiState {
    val sets = exercises.flatMap { it.sets }
    val summary = WorkoutCalculator.summarizeSession(
        sets = sets.map { WorkoutSetInput(weightKg = it.weightKg, reps = it.reps) },
        exerciseCount = exercises.size,
        startedAtMillis = session.startedAtMillis,
        endedAtMillis = session.endedAtMillis ?: nowMillis,
    )
    return SummaryUiState(
        totalVolumeKg = summary.totalVolumeKg,
        durationSeconds = summary.durationSeconds,
        exerciseCount = summary.exerciseCount,
        setCount = summary.setCount,
        completedAtMillis = session.endedAtMillis ?: session.startedAtMillis,
    )
}
