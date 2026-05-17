package com.gymlog.data.backup

import com.gymlog.data.importer.ImportedExercise
import com.gymlog.data.importer.ImportedSet
import com.gymlog.data.importer.ImportedWorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutBackupCodecTest {
    @Test
    fun encodesAndDecodesCompletedWorkoutSessions() {
        val sessions = listOf(
            ImportedWorkoutSession(
                startedAtMillis = 1_000L,
                endedAtMillis = 2_000L,
                exercises = listOf(
                    ImportedExercise(
                        name = "와이드 풀다운",
                        targetArea = "등",
                        defaultRestSeconds = 90,
                        sets = listOf(
                            ImportedSet(weightKg = 30.0, reps = 12),
                            ImportedSet(weightKg = 30.0, reps = 10),
                        ),
                    )
                ),
            )
        )

        val json = WorkoutBackupCodec.encode(sessions)
        val decoded = WorkoutBackupCodec.decode(json).getOrThrow()

        assertTrue(json.contains("\"schemaVersion\""))
        assertEquals(sessions, decoded)
    }
}
