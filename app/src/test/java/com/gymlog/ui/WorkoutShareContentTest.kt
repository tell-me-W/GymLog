package com.gymlog.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutShareContentTest {
    @Test
    fun shareTextContainsWorkoutSummaryValues() {
        val text = WorkoutShareContent.buildText(
            SummaryUiState(
                totalVolumeKg = 1250.0,
                durationSeconds = 1_800,
                exerciseCount = 4,
                setCount = 16,
            )
        )

        assertTrue(text.contains("GymLog"))
        assertTrue(text.contains("총 볼륨 1250 kg"))
        assertTrue(text.contains("운동 시간 30:00"))
        assertTrue(text.contains("종목 4개"))
        assertTrue(text.contains("세트 16개"))
    }
}
