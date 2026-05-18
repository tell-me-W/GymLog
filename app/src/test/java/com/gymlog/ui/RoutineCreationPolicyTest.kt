package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineCreationPolicyTest {
    @Test
    fun routineCanBeCreatedOnlyWithNameAndSelectedExercises() {
        assertTrue(RoutineCreationPolicy.canCreate("상체 루틴", selectedExerciseCount = 2))

        assertFalse(RoutineCreationPolicy.canCreate("", selectedExerciseCount = 2))
        assertFalse(RoutineCreationPolicy.canCreate("   ", selectedExerciseCount = 2))
        assertFalse(RoutineCreationPolicy.canCreate("상체 루틴", selectedExerciseCount = 0))
    }

    @Test
    fun selectedExerciseNamesKeepSelectionOrder() {
        val exercises = listOf(
            ExerciseEntity(id = 1L, name = "스쿼트", targetArea = "하체"),
            ExerciseEntity(id = 2L, name = "벤치 프레스", targetArea = "가슴"),
        )

        assertEquals(
            listOf("스쿼트", "벤치 프레스"),
            RoutineCreationPolicy.selectedExerciseNames(exercises),
        )
    }
}
