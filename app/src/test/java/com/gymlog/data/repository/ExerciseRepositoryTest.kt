package com.gymlog.data.repository

import com.gymlog.data.local.ExerciseDao
import com.gymlog.data.local.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseRepositoryTest {
    @Test
    fun seedDefaultsUpdatesExistingDefaultExerciseThatWasImportedAsUncategorized() = runTest {
        val dao = FakeExerciseDao(
            ExerciseEntity(
                id = 1L,
                name = "어시스트 풀업 머신",
                targetArea = SeedExercises.UNCATEGORIZED_TARGET_AREA,
                isCustom = true,
                defaultRestSeconds = 30,
            )
        )
        val repository = ExerciseRepository(dao)

        repository.seedDefaultsIfEmpty()

        val exercise = dao.getByName("어시스트 풀업 머신")!!
        assertEquals("등", exercise.targetArea)
        assertEquals(90, exercise.defaultRestSeconds)
        assertFalse(exercise.isCustom)
    }

    @Test
    fun seedDefaultsInsertsMissingDefaultExercisesEvenWhenDatabaseIsNotEmpty() = runTest {
        val dao = FakeExerciseDao(
            ExerciseEntity(id = 1L, name = "내 커스텀 운동", targetArea = "기타", isCustom = true)
        )
        val repository = ExerciseRepository(dao)

        repository.seedDefaultsIfEmpty()

        assertTrue(dao.exercises.any { it.name == "어시스트 풀업 머신" && it.targetArea == "등" })
    }
}

private class FakeExerciseDao(
    vararg initialExercises: ExerciseEntity,
) : ExerciseDao {
    val exercises = initialExercises.toMutableList()
    private var nextId = (exercises.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeExercises(): Flow<List<ExerciseEntity>> = flowOf(exercises)

    override fun observeExercises(targetArea: String): Flow<List<ExerciseEntity>> {
        return flowOf(exercises.filter { it.targetArea == targetArea })
    }

    override suspend fun count(): Int = exercises.size

    override suspend fun getAllExercises(): List<ExerciseEntity> = exercises.toList()

    override suspend fun getByName(name: String): ExerciseEntity? {
        return exercises.sortedBy { it.isCustom }.firstOrNull { it.name == name }
    }

    override suspend fun insertExercise(exercise: ExerciseEntity): Long {
        val id = nextId++
        exercises += exercise.copy(id = id)
        return id
    }

    override suspend fun insertExercises(exercises: List<ExerciseEntity>) {
        exercises.forEach { insertExercise(it) }
    }

    override suspend fun updateExercise(exercise: ExerciseEntity) {
        val index = exercises.indexOfFirst { it.id == exercise.id }
        if (index >= 0) {
            exercises[index] = exercise
        }
    }
}
