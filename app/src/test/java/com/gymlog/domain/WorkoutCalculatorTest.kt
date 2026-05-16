package com.gymlog.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutCalculatorTest {
    @Test
    fun totalVolumeSumsWeightTimesRepsForAllSets() {
        val total = WorkoutCalculator.totalVolume(
            listOf(
                WorkoutSetInput(weightKg = 100.0, reps = 5),
                WorkoutSetInput(weightKg = 90.0, reps = 8),
                WorkoutSetInput(weightKg = 0.0, reps = 12),
            )
        )

        assertEquals(1220.0, total, 0.001)
    }

    @Test
    fun summarizeSessionCalculatesVolumeDurationAndCounts() {
        val summary = WorkoutCalculator.summarizeSession(
            sets = listOf(
                WorkoutSetInput(weightKg = 100.0, reps = 5),
                WorkoutSetInput(weightKg = 90.0, reps = 8),
            ),
            exerciseCount = 2,
            startedAtMillis = 1_000L,
            endedAtMillis = 181_000L,
        )

        assertEquals(1220.0, summary.totalVolumeKg, 0.001)
        assertEquals(180L, summary.durationSeconds)
        assertEquals(2, summary.exerciseCount)
        assertEquals(2, summary.setCount)
    }

    @Test
    fun nextSetDefaultsUseLastSetValues() {
        val next = WorkoutCalculator.nextSetDefaults(
            WorkoutSetInput(weightKg = 72.5, reps = 10)
        )

        assertEquals(72.5, next.weightKg, 0.001)
        assertEquals(10, next.reps)
    }

    @Test
    fun nextSetDefaultsAreEmptyWhenNoPreviousSetExists() {
        val next = WorkoutCalculator.nextSetDefaults(null)

        assertEquals(0.0, next.weightKg, 0.001)
        assertEquals(0, next.reps)
    }

    @Test
    fun canRemoveLastSetOnlyWhenMoreThanOneSetExists() {
        assertEquals(false, WorkoutCalculator.canRemoveLastSet(1))
        assertEquals(true, WorkoutCalculator.canRemoveLastSet(2))
    }
}
