package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.SessionExerciseEntity
import com.gymlog.data.local.SessionExerciseWithDetails
import com.gymlog.data.local.SessionStatus
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.data.local.WorkoutSetEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryUiStateTest {
    @Test
    fun sessionSummaryKeepsVolumeDurationAndCompletedTime() {
        val session = WorkoutSessionWithExercises(
            session = WorkoutSessionEntity(
                id = 10L,
                startedAtMillis = 1_000L,
                endedAtMillis = 61_000L,
                status = SessionStatus.COMPLETED,
            ),
            exercises = listOf(
                SessionExerciseWithDetails(
                    sessionExercise = SessionExerciseEntity(
                        id = 20L,
                        sessionId = 10L,
                        exerciseId = 30L,
                        sortOrder = 0,
                    ),
                    exercise = ExerciseEntity(
                        id = 30L,
                        name = "벤치 프레스",
                        targetArea = "가슴",
                    ),
                    sets = listOf(
                        WorkoutSetEntity(
                            id = 40L,
                            sessionExerciseId = 20L,
                            sortOrder = 0,
                            weightKg = 20.0,
                            reps = 5,
                            isCompleted = true,
                        )
                    ),
                )
            ),
        )

        val summary = session.toSummaryUiState(nowMillis = 99_000L)

        assertEquals(100.0, summary.totalVolumeKg, 0.0)
        assertEquals(60L, summary.durationSeconds)
        assertEquals(1, summary.exerciseCount)
        assertEquals(1, summary.setCount)
        assertEquals(61_000L, summary.completedAtMillis)
    }
}
