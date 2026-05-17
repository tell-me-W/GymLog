package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExercisePickerSorterTest {
    @Test
    fun sortsRecentExercisesFirstAndRemainingByName() {
        val oldRecent = ExerciseEntity(id = 1L, name = "벤치프레스", targetArea = "가슴")
        val noRecentA = ExerciseEntity(id = 2L, name = "덤벨 컬", targetArea = "팔")
        val newest = ExerciseEntity(id = 3L, name = "스쿼트", targetArea = "하체")
        val noRecentB = ExerciseEntity(id = 4L, name = "랫풀다운", targetArea = "등")

        val sorted = ExercisePickerSorter.sort(
            exercises = listOf(noRecentB, oldRecent, newest, noRecentA),
            recentRecords = mapOf(
                oldRecent.id to RecentExerciseRecord(lastPerformedMillis = 100L, setCount = 3),
                newest.id to RecentExerciseRecord(lastPerformedMillis = 200L, setCount = 4),
            ),
        )

        assertEquals(listOf(newest, oldRecent, noRecentA, noRecentB), sorted)
    }
}
