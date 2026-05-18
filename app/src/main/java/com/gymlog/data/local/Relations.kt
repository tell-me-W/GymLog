package com.gymlog.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutSessionWithExercises(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        entity = SessionExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val exercises: List<SessionExerciseWithDetails>,
)

data class SessionExerciseWithDetails(
    @Embedded val sessionExercise: SessionExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id",
    )
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionExerciseId",
    )
    val sets: List<WorkoutSetEntity>,
)

data class RoutineWithExercises(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId",
    )
    val exercises: List<RoutineExerciseEntity>,
)
