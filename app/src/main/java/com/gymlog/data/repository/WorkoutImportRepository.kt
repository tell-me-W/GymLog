package com.gymlog.data.repository

import com.gymlog.data.backup.WorkoutBackupCodec
import com.gymlog.data.importer.ImportedExercise
import com.gymlog.data.importer.ImportedSet
import com.gymlog.data.importer.ImportedWorkoutSession
import com.gymlog.data.local.ExerciseDao
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.SessionExerciseEntity
import com.gymlog.data.local.SessionStatus
import com.gymlog.data.local.WorkoutDao
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.data.local.WorkoutSetEntity
import com.gymlog.ui.RecentExerciseRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ImportResult(
    val inserted: Int,
    val skipped: Int,
)

class WorkoutImportRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    suspend fun exportCompletedJson(): String {
        return WorkoutBackupCodec.encode(
            workoutDao.getCompletedSessionsWithExercises().map { it.toImportedSession() }
        )
    }

    suspend fun importBackupJson(json: String): ImportResult {
        return importSessions(WorkoutBackupCodec.decode(json).getOrThrow())
    }

    suspend fun importSessions(sessions: List<ImportedWorkoutSession>): ImportResult {
        val existing = workoutDao.getCompletedSessionsWithExercises()
            .map { it.toImportedSession().signature() }
            .toMutableSet()
        var inserted = 0
        var skipped = 0

        sessions.forEach { session ->
            val signature = session.signature()
            if (signature in existing) {
                skipped += 1
            } else {
                insertCompletedSession(session)
                existing += signature
                inserted += 1
            }
        }
        return ImportResult(inserted = inserted, skipped = skipped)
    }

    suspend fun recentExerciseRecordsWithinMonths(months: Long = 2): Map<Long, RecentExerciseRecord> {
        val start = LocalDate.now(zoneId)
            .minusMonths(months)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val records = linkedMapOf<Long, RecentExerciseRecord>()
        workoutDao.getCompletedSessionsWithExercisesSince(start)
            .sortedByDescending { it.session.startedAtMillis }
            .forEach { session ->
                session.exercises.forEach { exercise ->
                    records.putIfAbsent(
                        exercise.exercise.id,
                        RecentExerciseRecord(
                            lastPerformedMillis = session.session.startedAtMillis,
                            setCount = exercise.sets.size,
                        )
                    )
                }
            }
        return records
    }

    private suspend fun insertCompletedSession(session: ImportedWorkoutSession) {
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(
                startedAtMillis = session.startedAtMillis,
                endedAtMillis = session.endedAtMillis,
                status = SessionStatus.COMPLETED,
            )
        )
        session.exercises.forEachIndexed { exerciseIndex, importedExercise ->
            val exerciseId = findOrCreateExercise(importedExercise)
            val sessionExerciseId = workoutDao.insertSessionExercise(
                SessionExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    sortOrder = exerciseIndex,
                )
            )
            importedExercise.sets.forEachIndexed { setIndex, importedSet ->
                workoutDao.insertSet(
                    WorkoutSetEntity(
                        sessionExerciseId = sessionExerciseId,
                        sortOrder = setIndex,
                        weightKg = importedSet.weightKg.coerceAtLeast(0.0),
                        reps = importedSet.reps.coerceAtLeast(0),
                        isCompleted = true,
                    )
                )
            }
        }
    }

    private suspend fun findOrCreateExercise(exercise: ImportedExercise): Long {
        val name = exercise.name.trim()
        exerciseDao.getByName(name)?.let { return it.id }
        SeedExercises.defaultByName(name)?.let { defaultExercise ->
            return exerciseDao.insertExercise(defaultExercise)
        }
        return exerciseDao.insertExercise(
            ExerciseEntity(
                name = name,
                targetArea = exercise.targetArea.ifBlank { SeedExercises.UNCATEGORIZED_TARGET_AREA },
                isCustom = true,
                defaultRestSeconds = exercise.defaultRestSeconds.coerceAtLeast(0),
            )
        )
    }
}

private fun WorkoutSessionWithExercises.toImportedSession(): ImportedWorkoutSession {
    return ImportedWorkoutSession(
        startedAtMillis = session.startedAtMillis,
        endedAtMillis = session.endedAtMillis ?: session.startedAtMillis,
        exercises = exercises.sortedBy { it.sessionExercise.sortOrder }.map { exercise ->
            ImportedExercise(
                name = exercise.exercise.name,
                targetArea = exercise.exercise.targetArea,
                defaultRestSeconds = exercise.exercise.defaultRestSeconds,
                sets = exercise.sets.sortedBy { it.sortOrder }.map { set ->
                    ImportedSet(weightKg = set.weightKg, reps = set.reps, isCompleted = true)
                },
            )
        },
    )
}

private fun ImportedWorkoutSession.signature(): String {
    return buildString {
        append(startedAtMillis)
        append('|')
        append(endedAtMillis)
        exercises.forEach { exercise ->
            append("|e:")
            append(exercise.name.trim())
            exercise.sets.forEach { set ->
                append("|s:")
                append(set.weightKg)
                append('x')
                append(set.reps)
            }
        }
    }
}
