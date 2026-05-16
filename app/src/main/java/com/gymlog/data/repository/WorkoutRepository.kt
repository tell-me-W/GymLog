package com.gymlog.data.repository

import com.gymlog.data.local.SessionStatus
import com.gymlog.data.local.WorkoutDao
import com.gymlog.data.local.SessionExerciseEntity
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.data.local.WorkoutSetEntity
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun createEmptyDraftSession(): Long {
        return workoutDao.insertSession(
            WorkoutSessionEntity(
                startedAtMillis = nowMillis(),
                status = SessionStatus.DRAFT,
            )
        )
    }

    fun observeSession(sessionId: Long): Flow<WorkoutSessionWithExercises?> {
        return workoutDao.observeSession(sessionId)
    }

    fun observeCompletedSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.observeCompletedSessions()
    }

    suspend fun sessionSnapshot(sessionId: Long): WorkoutSessionWithExercises? {
        return workoutDao.getSessionWithExercises(sessionId)
    }

    suspend fun latestDraftSession(): WorkoutSessionEntity? {
        return workoutDao.latestDraftSession()
    }

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long): Long {
        val nextOrder = workoutDao.maxExerciseOrder(sessionId) + 1
        return workoutDao.insertSessionExercise(
            SessionExerciseEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                sortOrder = nextOrder,
            )
        )
    }

    suspend fun addSet(
        sessionExerciseId: Long,
        weightKg: Double = 0.0,
        reps: Int = 0,
        isCompleted: Boolean = false,
    ): Long {
        val nextOrder = workoutDao.maxSetOrder(sessionExerciseId) + 1
        return workoutDao.insertSet(
            WorkoutSetEntity(
                sessionExerciseId = sessionExerciseId,
                sortOrder = nextOrder,
                weightKg = weightKg.coerceAtLeast(0.0),
                reps = reps.coerceAtLeast(0),
                isCompleted = isCompleted,
            )
        )
    }

    suspend fun updateSet(setId: Long, weightKg: Double, reps: Int, isCompleted: Boolean) {
        val current = workoutDao.getSet(setId) ?: return
        val cleanWeightKg = weightKg.coerceAtLeast(0.0)
        val cleanReps = reps.coerceAtLeast(0)
        val valuesChanged = current.weightKg != cleanWeightKg || current.reps != cleanReps

        workoutDao.updateSet(
            current.copy(
                weightKg = cleanWeightKg,
                reps = cleanReps,
                isCompleted = isCompleted,
            )
        )

        if (valuesChanged) {
            workoutDao.getSetsForSessionExercise(current.sessionExerciseId)
                .filter { it.id != current.id && !it.isCompleted }
                .forEach { set ->
                    workoutDao.updateSet(
                        set.copy(
                            weightKg = cleanWeightKg,
                            reps = cleanReps,
                        )
                    )
                }
        }
    }

    suspend fun deleteSet(setId: Long) {
        workoutDao.getSet(setId)?.let { workoutDao.deleteSet(it) }
    }

    suspend fun deleteSessionExercise(sessionExerciseId: Long) {
        workoutDao.deleteSessionExercise(sessionExerciseId)
    }

    suspend fun completeSession(sessionId: Long): Boolean {
        pruneIncompleteWork(sessionId)
        val prunedSession = workoutDao.getSessionWithExercises(sessionId)
        val hasCompletedWork = prunedSession?.exercises
            ?.any { sessionExercise -> sessionExercise.sets.isNotEmpty() }
            ?: false
        if (!hasCompletedWork) {
            workoutDao.deleteSession(sessionId)
            return false
        }
        workoutDao.markCompleted(sessionId, nowMillis())
        return true
    }

    suspend fun copyCompletedSessionToDraft(sourceSessionId: Long): Long {
        return workoutDao.copyCompletedSessionToDraft(sourceSessionId, nowMillis())
    }

    suspend fun sessionStatus(sessionId: Long): SessionStatus? {
        return workoutDao.sessionStatus(sessionId)
    }

    suspend fun completedDatesInMonth(year: Int, month: Int): Set<LocalDate> {
        val yearMonth = YearMonth.of(year, month)
        val start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return workoutDao.completedSessionStartTimes(start, end)
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
            .toSet()
    }

    private suspend fun pruneIncompleteWork(sessionId: Long) {
        val session = workoutDao.getSessionWithExercises(sessionId) ?: return
        session.exercises.forEach { sessionExercise ->
            sessionExercise.sets
                .filterNot { it.isCompleted }
                .forEach { workoutDao.deleteSet(it) }

            if (sessionExercise.sets.none { it.isCompleted }) {
                workoutDao.deleteSessionExercise(sessionExercise.sessionExercise.id)
            }
        }
    }
}
