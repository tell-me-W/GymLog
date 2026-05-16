package com.gymlog.data.repository

import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.SessionExerciseEntity
import com.gymlog.data.local.SessionExerciseWithDetails
import com.gymlog.data.local.SessionStatus
import com.gymlog.data.local.WorkoutDao
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.data.local.WorkoutSetEntity
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutRepositoryTest {
    @Test
    fun createEmptyDraftSessionDoesNotDeleteExistingDraft() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepository(
            workoutDao = dao,
            zoneId = ZoneId.of("Asia/Seoul"),
            nowMillis = dao::nextNow,
        )

        val firstDraftId = repository.createEmptyDraftSession()
        val secondDraftId = repository.createEmptyDraftSession()

        assertNotEquals(firstDraftId, secondDraftId)
        assertEquals(SessionStatus.DRAFT, repository.sessionStatus(firstDraftId))
        assertEquals(SessionStatus.DRAFT, repository.sessionStatus(secondDraftId))
    }

    @Test
    fun copyCompletedSessionToDraftDoesNotDeleteExistingDraft() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepository(
            workoutDao = dao,
            zoneId = ZoneId.of("Asia/Seoul"),
            nowMillis = dao::nextNow,
        )
        val completedSessionId = repository.createEmptyDraftSession()
        val completedExerciseId = repository.addExerciseToSession(completedSessionId, exerciseId = 1L)
        repository.addSet(completedExerciseId, weightKg = 100.0, reps = 5, isCompleted = true)
        repository.completeSession(completedSessionId)
        val existingDraftId = repository.createEmptyDraftSession()

        val copiedDraftId = repository.copyCompletedSessionToDraft(completedSessionId)

        assertNotEquals(existingDraftId, copiedDraftId)
        assertEquals(SessionStatus.DRAFT, repository.sessionStatus(existingDraftId))
        assertEquals(SessionStatus.DRAFT, repository.sessionStatus(copiedDraftId))
    }

    @Test
    fun completeSessionKeepsOnlyCompletedSetsInSavedRecord() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepository(
            workoutDao = dao,
            zoneId = ZoneId.of("Asia/Seoul"),
            nowMillis = dao::nextNow,
        )
        val sessionId = repository.createEmptyDraftSession()
        val benchId = repository.addExerciseToSession(sessionId, exerciseId = 1L)
        repository.addSet(benchId, weightKg = 100.0, reps = 5, isCompleted = true)
        repository.addSet(benchId, weightKg = 90.0, reps = 8, isCompleted = false)
        val squatId = repository.addExerciseToSession(sessionId, exerciseId = 2L)
        repository.addSet(squatId, weightKg = 120.0, reps = 5, isCompleted = false)

        repository.completeSession(sessionId)

        val saved = repository.sessionSnapshot(sessionId)!!
        assertEquals(SessionStatus.COMPLETED, saved.session.status)
        assertEquals(1, saved.exercises.size)
        assertEquals(1, saved.exercises.first().sets.size)
        assertEquals(100.0, saved.exercises.first().sets.first().weightKg, 0.001)
        assertTrue(saved.exercises.first().sets.all { it.isCompleted })
    }

    @Test
    fun completeSessionDeletesDraftWhenNoSetsAreCompleted() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepository(
            workoutDao = dao,
            zoneId = ZoneId.of("Asia/Seoul"),
            nowMillis = dao::nextNow,
        )
        val sessionId = repository.createEmptyDraftSession()
        val benchId = repository.addExerciseToSession(sessionId, exerciseId = 1L)
        repository.addSet(benchId, weightKg = 100.0, reps = 5, isCompleted = false)

        val saved = repository.completeSession(sessionId)

        assertEquals(false, saved)
        assertNull(repository.sessionSnapshot(sessionId))
        assertTrue(repository.completedDatesInMonth(2023, 11).isEmpty())
    }

    @Test
    fun updateSetCopiesWeightAndRepsToOtherIncompleteSetsInSameExercise() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepository(
            workoutDao = dao,
            zoneId = ZoneId.of("Asia/Seoul"),
            nowMillis = dao::nextNow,
        )
        val sessionId = repository.createEmptyDraftSession()
        val benchId = repository.addExerciseToSession(sessionId, exerciseId = 1L)
        val editedSetId = repository.addSet(benchId, weightKg = 100.0, reps = 5, isCompleted = false)
        repository.addSet(benchId, weightKg = 100.0, reps = 5, isCompleted = false)
        repository.addSet(benchId, weightKg = 80.0, reps = 3, isCompleted = true)
        val squatId = repository.addExerciseToSession(sessionId, exerciseId = 2L)
        repository.addSet(squatId, weightKg = 120.0, reps = 5, isCompleted = false)

        repository.updateSet(editedSetId, weightKg = 110.0, reps = 6, isCompleted = false)

        val exercises = repository.sessionSnapshot(sessionId)!!.exercises
        val benchSets = exercises.first { it.sessionExercise.id == benchId }.sets.sortedBy { it.sortOrder }
        val squatSets = exercises.first { it.sessionExercise.id == squatId }.sets
        assertEquals(listOf(110.0, 110.0, 80.0), benchSets.map { it.weightKg })
        assertEquals(listOf(6, 6, 3), benchSets.map { it.reps })
        assertTrue(benchSets.last().isCompleted)
        assertEquals(120.0, squatSets.first().weightKg, 0.001)
        assertEquals(5, squatSets.first().reps)
    }

    @Test
    fun deleteSessionRemovesCompletedRecordAndCalendarDate() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepository(
            workoutDao = dao,
            zoneId = ZoneId.of("Asia/Seoul"),
            nowMillis = dao::nextNow,
        )
        val sessionId = repository.createEmptyDraftSession()
        val benchId = repository.addExerciseToSession(sessionId, exerciseId = 1L)
        repository.addSet(benchId, weightKg = 100.0, reps = 5, isCompleted = true)
        repository.completeSession(sessionId)

        repository.deleteSession(sessionId)

        assertNull(repository.sessionSnapshot(sessionId))
        assertTrue(repository.completedDatesInMonth(2023, 11).isEmpty())
    }
}

private class FakeWorkoutDao : WorkoutDao() {
    private val sessions = linkedMapOf<Long, WorkoutSessionEntity>()
    private val sessionExercises = linkedMapOf<Long, SessionExerciseEntity>()
    private val sets = linkedMapOf<Long, WorkoutSetEntity>()
    private var sessionId = 1L
    private var sessionExerciseId = 1L
    private var setId = 1L
    private var clock = 1_700_000_000_000L

    fun nextNow(): Long = clock++

    override suspend fun insertSession(session: WorkoutSessionEntity): Long {
        val id = sessionId++
        sessions[id] = session.copy(id = id)
        return id
    }

    override suspend fun insertSessionExercise(sessionExercise: SessionExerciseEntity): Long {
        val id = sessionExerciseId++
        sessionExercises[id] = sessionExercise.copy(id = id)
        return id
    }

    override suspend fun insertSet(set: WorkoutSetEntity): Long {
        val id = setId++
        sets[id] = set.copy(id = id)
        return id
    }

    override suspend fun updateSet(set: WorkoutSetEntity) {
        sets[set.id] = set
    }

    override suspend fun deleteSet(set: WorkoutSetEntity) {
        sets.remove(set.id)
    }

    override suspend fun deleteSessionExercise(sessionExerciseId: Long) {
        sessionExercises.remove(sessionExerciseId)
        sets.values.removeAll { it.sessionExerciseId == sessionExerciseId }
    }

    override suspend fun deleteSession(sessionId: Long) {
        sessions.remove(sessionId)
        val deletedSessionExerciseIds = sessionExercises.values
            .filter { it.sessionId == sessionId }
            .map { it.id }
            .toSet()
        sessionExercises.keys.removeAll(deletedSessionExerciseIds)
        sets.values.removeAll { it.sessionExerciseId in deletedSessionExerciseIds }
    }

    override suspend fun getSession(sessionId: Long): WorkoutSessionEntity? {
        return sessions[sessionId]
    }

    override suspend fun latestDraftSession(): WorkoutSessionEntity? {
        return sessions.values
            .filter { it.status == SessionStatus.DRAFT }
            .maxByOrNull { it.startedAtMillis }
    }

    override suspend fun sessionStatus(sessionId: Long): SessionStatus? {
        return sessions[sessionId]?.status
    }

    override suspend fun getSet(setId: Long): WorkoutSetEntity? {
        return sets[setId]
    }

    override suspend fun getSetsForSessionExercise(sessionExerciseId: Long): List<WorkoutSetEntity> {
        return sets.values.filter { it.sessionExerciseId == sessionExerciseId }
    }

    override suspend fun maxExerciseOrder(sessionId: Long): Int {
        return sessionExercises.values
            .filter { it.sessionId == sessionId }
            .maxOfOrNull { it.sortOrder } ?: -1
    }

    override suspend fun maxSetOrder(sessionExerciseId: Long): Int {
        return sets.values
            .filter { it.sessionExerciseId == sessionExerciseId }
            .maxOfOrNull { it.sortOrder } ?: -1
    }

    override fun observeSession(sessionId: Long): Flow<WorkoutSessionWithExercises?> {
        return flowOf(getSessionWithExercisesSnapshot(sessionId))
    }

    override suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises? {
        return getSessionWithExercisesSnapshot(sessionId)
    }

    override suspend fun completedSessionStartTimes(startMillis: Long, endMillis: Long): List<Long> {
        return sessions.values
            .filter {
                it.status == SessionStatus.COMPLETED &&
                    it.startedAtMillis >= startMillis &&
                    it.startedAtMillis < endMillis
            }
            .map { it.startedAtMillis }
    }

    override fun observeCompletedSessions(): Flow<List<WorkoutSessionEntity>> {
        return flowOf(sessions.values.filter { it.status == SessionStatus.COMPLETED })
    }

    override suspend fun markCompleted(sessionId: Long, endedAtMillis: Long) {
        sessions[sessionId] = sessions.getValue(sessionId).copy(
            status = SessionStatus.COMPLETED,
            endedAtMillis = endedAtMillis,
        )
    }

    override suspend fun deleteDraftSessions() {
        val draftIds = sessions.values
            .filter { it.status == SessionStatus.DRAFT }
            .map { it.id }
            .toSet()
        sessions.keys.removeAll(draftIds)
        val deletedSessionExerciseIds = sessionExercises.values
            .filter { it.sessionId in draftIds }
            .map { it.id }
            .toSet()
        sessionExercises.keys.removeAll(deletedSessionExerciseIds)
        sets.values.removeAll { it.sessionExerciseId in deletedSessionExerciseIds }
    }

    private fun getSessionWithExercisesSnapshot(sessionId: Long): WorkoutSessionWithExercises? {
        val session = sessions[sessionId] ?: return null
        val exerciseDetails = sessionExercises.values
            .filter { it.sessionId == sessionId }
            .map { sessionExercise ->
                SessionExerciseWithDetails(
                    sessionExercise = sessionExercise,
                    exercise = ExerciseEntity(
                        id = sessionExercise.exerciseId,
                        name = "테스트 운동",
                        targetArea = "테스트",
                    ),
                    sets = sets.values.filter { it.sessionExerciseId == sessionExercise.id },
                )
            }
        return WorkoutSessionWithExercises(session, exerciseDetails)
    }
}
