package com.gymlog.ui

import com.gymlog.data.local.ExerciseEntity

object ExerciseArchivePolicy {
    fun canArchive(exercise: ExerciseEntity): Boolean {
        return exercise.isCustom
    }
}
