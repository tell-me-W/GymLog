package com.gymlog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertSessionExercise(sessionExercise: SessionExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    abstract suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    abstract suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("DELETE FROM session_exercises WHERE id = :sessionExerciseId")
    abstract suspend fun deleteSessionExercise(sessionExerciseId: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    abstract suspend fun deleteSession(sessionId: Long)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    abstract suspend fun getSession(sessionId: Long): WorkoutSessionEntity?

    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE status = 'DRAFT'
        ORDER BY startedAtMillis DESC
        LIMIT 1
        """
    )
    abstract suspend fun latestDraftSession(): WorkoutSessionEntity?

    @Query("SELECT status FROM workout_sessions WHERE id = :sessionId")
    abstract suspend fun sessionStatus(sessionId: Long): SessionStatus?

    @Query("SELECT * FROM workout_sets WHERE id = :setId")
    abstract suspend fun getSet(setId: Long): WorkoutSetEntity?

    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY sortOrder")
    abstract suspend fun getSetsForSessionExercise(sessionExerciseId: Long): List<WorkoutSetEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM session_exercises WHERE sessionId = :sessionId")
    abstract suspend fun maxExerciseOrder(sessionId: Long): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId")
    abstract suspend fun maxSetOrder(sessionExerciseId: Long): Int

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    abstract fun observeSession(sessionId: Long): Flow<WorkoutSessionWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    abstract suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?

    @Transaction
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE status = 'COMPLETED'
        ORDER BY startedAtMillis DESC
        """
    )
    abstract suspend fun getCompletedSessionsWithExercises(): List<WorkoutSessionWithExercises>

    @Transaction
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE status = 'COMPLETED'
        AND startedAtMillis >= :startMillis
        ORDER BY startedAtMillis DESC
        """
    )
    abstract suspend fun getCompletedSessionsWithExercisesSince(startMillis: Long): List<WorkoutSessionWithExercises>

    @Query(
        """
        SELECT startedAtMillis FROM workout_sessions
        WHERE status = 'COMPLETED'
        AND startedAtMillis >= :startMillis
        AND startedAtMillis < :endMillis
        ORDER BY startedAtMillis
        """
    )
    abstract suspend fun completedSessionStartTimes(startMillis: Long, endMillis: Long): List<Long>

    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE status = 'COMPLETED'
        ORDER BY startedAtMillis DESC
        """
    )
    abstract fun observeCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Query(
        """
        UPDATE workout_sessions
        SET status = 'COMPLETED', endedAtMillis = :endedAtMillis
        WHERE id = :sessionId
        """
    )
    abstract suspend fun markCompleted(sessionId: Long, endedAtMillis: Long)

    @Query("DELETE FROM workout_sessions WHERE status = 'DRAFT'")
    abstract suspend fun deleteDraftSessions()

    @Transaction
    open suspend fun copyCompletedSessionToDraft(sourceSessionId: Long, nowMillis: Long): Long {
        val source = getSessionWithExercises(sourceSessionId)
            ?: error("Source session does not exist")
        require(source.session.status == SessionStatus.COMPLETED) {
            "Only completed sessions can be copied"
        }

        val newSessionId = insertSession(
            WorkoutSessionEntity(
                startedAtMillis = nowMillis,
                status = SessionStatus.DRAFT,
            )
        )
        source.exercises
            .sortedBy { it.sessionExercise.sortOrder }
            .forEach { sourceExercise ->
                val newSessionExerciseId = insertSessionExercise(
                    SessionExerciseEntity(
                        sessionId = newSessionId,
                        exerciseId = sourceExercise.exercise.id,
                        sortOrder = sourceExercise.sessionExercise.sortOrder,
                    )
                )
                sourceExercise.sets
                    .sortedBy { it.sortOrder }
                    .forEach { sourceSet ->
                        insertSet(
                            WorkoutSetEntity(
                                sessionExerciseId = newSessionExerciseId,
                                sortOrder = sourceSet.sortOrder,
                                weightKg = sourceSet.weightKg,
                                reps = sourceSet.reps,
                                isCompleted = false,
                            )
                        )
                    }
            }
        return newSessionId
    }
}
