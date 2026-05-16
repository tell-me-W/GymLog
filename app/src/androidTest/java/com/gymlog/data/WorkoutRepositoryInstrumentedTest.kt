package com.gymlog.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.gymlog.data.local.GymLogDatabase
import com.gymlog.data.local.SessionStatus
import com.gymlog.data.repository.ExerciseRepository
import com.gymlog.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutRepositoryInstrumentedTest {
    private lateinit var database: GymLogDatabase
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var workoutRepository: WorkoutRepository
    private val zoneId = ZoneId.of("Asia/Seoul")
    private val fixedNowMillis = LocalDateTime.of(2026, 5, 16, 10, 0)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GymLogDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseRepository = ExerciseRepository(database.exerciseDao())
        workoutRepository = WorkoutRepository(
            workoutDao = database.workoutDao(),
            zoneId = zoneId,
            nowMillis = { fixedNowMillis },
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedExercisesAddsKoreanDefaultLibrary() = runTest {
        exerciseRepository.seedDefaultsIfEmpty()

        val exercises = exerciseRepository.observeExercises().first()

        assertTrue(exercises.size >= 30)
        assertTrue(exercises.any { it.name == "벤치프레스" && it.targetArea == "가슴" })
        assertTrue(exercises.any { it.name == "스쿼트" && it.targetArea == "하체" })
    }

    @Test
    fun draftSessionsAreExcludedFromCalendarUntilCompleted() = runTest {
        val draftId = workoutRepository.createEmptyDraftSession()

        assertTrue(workoutRepository.completedDatesInMonth(2026, 5).isEmpty())
        assertEquals(draftId, workoutRepository.latestDraftSession()?.id)

        workoutRepository.completeSession(draftId)

        assertEquals(SessionStatus.COMPLETED, workoutRepository.sessionStatus(draftId))
        assertEquals(1, workoutRepository.completedDatesInMonth(2026, 5).size)
        assertEquals(null, workoutRepository.latestDraftSession())
    }

    @Test
    fun copiedSessionPreservesStructureAndResetsSetCompletion() = runTest {
        exerciseRepository.seedDefaultsIfEmpty()
        val bench = exerciseRepository.observeExercises().first().first { it.name == "벤치프레스" }
        val completedId = workoutRepository.createEmptyDraftSession()
        val sessionExerciseId = workoutRepository.addExerciseToSession(completedId, bench.id)
        workoutRepository.addSet(sessionExerciseId, weightKg = 100.0, reps = 5, isCompleted = true)
        workoutRepository.addSet(sessionExerciseId, weightKg = 90.0, reps = 8, isCompleted = true)
        workoutRepository.completeSession(completedId)

        val copiedId = workoutRepository.copyCompletedSessionToDraft(completedId)
        val copied = workoutRepository.observeSession(copiedId).first()!!

        assertEquals(SessionStatus.DRAFT, copied.session.status)
        assertEquals(1, copied.exercises.size)
        assertEquals("벤치프레스", copied.exercises.first().exercise.name)
        assertEquals(listOf(100.0, 90.0), copied.exercises.first().sets.map { it.weightKg })
        assertTrue(copied.exercises.first().sets.none { it.isCompleted })
    }

    @Test
    fun customExerciseCanBeAdded() = runTest {
        val id = exerciseRepository.addCustomExercise("케이블 크런치", "복근", 60)

        val created = exerciseRepository.observeExercises("복근").first().first { it.id == id }

        assertEquals("케이블 크런치", created.name)
        assertTrue(created.isCustom)
    }
}
