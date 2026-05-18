package com.gymlog.data.repository

import com.gymlog.data.local.ExerciseInputType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedExercisesTest {
    @Test
    fun targetAreasStartWithAllFilter() {
        assertEquals(SeedExercises.ALL_TARGET_AREA, SeedExercises.targetAreas.first())
    }

    @Test
    fun allTargetAreaMapsToUnfilteredQuery() {
        assertNull(SeedExercises.queryTargetOrNull(SeedExercises.ALL_TARGET_AREA))
    }

    @Test
    fun exerciseTargetAreaMapsToFilteredQuery() {
        assertEquals("가슴", SeedExercises.queryTargetOrNull("가슴"))
    }

    @Test
    fun durationBasedExercisesMoveToCardioAndUseDurationInput() {
        val cardioNames = listOf("로잉 머신", "사이클", "러닝 (트레드밀)")

        cardioNames.forEach { name ->
            val exercise = SeedExercises.defaultByName(name)!!
            assertEquals("유산소", exercise.targetArea)
            assertEquals(ExerciseInputType.DURATION, exercise.inputType)
        }
        assertTrue(SeedExercises.targetAreas.contains("유산소"))
    }

    @Test
    fun repetitionBasedOtherExercisesStayInOtherAndUseRepsInput() {
        val exercise = SeedExercises.defaultByName("버피 테스트")!!

        assertEquals("기타", exercise.targetArea)
        assertEquals(ExerciseInputType.REPS, exercise.inputType)
    }
}
