package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity

data class RecentExerciseRecord(
    val lastPerformedMillis: Long,
    val setCount: Int,
)

object ExercisePickerSorter {
    fun sort(
        exercises: List<ExerciseEntity>,
        recentRecords: Map<Long, RecentExerciseRecord>,
    ): List<ExerciseEntity> {
        return exercises.sortedWith(
            compareByDescending<ExerciseEntity> { recentRecords[it.id]?.lastPerformedMillis ?: Long.MIN_VALUE }
                .thenBy { if (recentRecords.containsKey(it.id)) 0 else 1 }
                .thenBy { it.name }
        )
    }
}
