package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseArchivePolicyTest {
    @Test
    fun onlyCustomExercisesCanBeArchivedFromPicker() {
        assertTrue(
            ExerciseArchivePolicy.canArchive(
                ExerciseEntity(id = 1L, name = "내 운동", targetArea = "기타", isCustom = true)
            )
        )

        assertFalse(
            ExerciseArchivePolicy.canArchive(
                ExerciseEntity(id = 2L, name = "벤치 프레스", targetArea = "가슴", isCustom = false)
            )
        )
    }
}
