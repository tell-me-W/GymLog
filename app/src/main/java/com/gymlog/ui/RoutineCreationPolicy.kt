package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity

object RoutineCreationPolicy {
    fun canCreate(name: String, selectedExerciseCount: Int): Boolean {
        return name.isNotBlank() && selectedExerciseCount > 0
    }

    fun selectedExerciseNames(exercises: Collection<ExerciseEntity>): List<String> {
        return exercises.map { it.name }
    }
}
