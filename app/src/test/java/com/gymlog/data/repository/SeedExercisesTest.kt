package com.gymlog.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
