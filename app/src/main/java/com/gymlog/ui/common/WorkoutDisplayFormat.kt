package com.gymlog.ui.common

import com.gymlog.data.local.ExerciseInputType
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSetEntity

internal fun formatSetValue(set: WorkoutSetEntity, inputType: ExerciseInputType): String {
    return if (inputType == ExerciseInputType.DURATION) {
        "${set.durationSeconds / 60}분"
    } else {
        "${set.weightKg} kg × ${set.reps} reps"
    }
}

internal fun sessionDurationSeconds(session: WorkoutSessionEntity): Long {
    val endedAtMillis = session.endedAtMillis ?: session.startedAtMillis
    return ((endedAtMillis - session.startedAtMillis) / 1000).coerceAtLeast(0)
}
