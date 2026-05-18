package com.gymlog.data.repository

import com.gymlog.data.local.RoutineDao
import com.gymlog.data.local.RoutineEntity
import com.gymlog.data.local.RoutineExerciseEntity
import com.gymlog.data.local.RoutineExerciseWithDetails
import com.gymlog.data.local.RoutineWithExerciseDetails
import com.gymlog.data.local.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineRepositoryTest {
    @Test
    fun createRoutineStoresExerciseIdsInOrder() = runTest {
        val dao = FakeRoutineDao()
        val repository = RoutineRepository(dao)

        repository.createRoutine("상체", listOf(3L, 7L, 2L))

        val routine = dao.routines.value.single()
        assertEquals("상체", routine.routine.name)
        assertEquals(listOf(3L, 7L, 2L), routine.exercises.map { it.routineExercise.exerciseId })
        assertEquals(listOf(0, 1, 2), routine.exercises.map { it.routineExercise.sortOrder })
        assertEquals(listOf("운동 3", "운동 7", "운동 2"), routine.exercises.map { it.exercise.name })
    }

    @Test
    fun deleteRoutineRemovesItFromList() = runTest {
        val dao = FakeRoutineDao()
        val repository = RoutineRepository(dao)
        val routineId = repository.createRoutine("하체", listOf(1L))

        repository.deleteRoutine(routineId)

        assertTrue(dao.routines.value.isEmpty())
    }
}

private class FakeRoutineDao : RoutineDao {
    val routines = MutableStateFlow<List<RoutineWithExerciseDetails>>(emptyList())
    private var nextRoutineId = 1L
    private var nextRoutineExerciseId = 1L

    override fun observeRoutines(): Flow<List<RoutineWithExerciseDetails>> = routines

    override suspend fun insertRoutine(routine: RoutineEntity): Long {
        val id = nextRoutineId++
        routines.value = routines.value + RoutineWithExerciseDetails(routine.copy(id = id), emptyList())
        return id
    }

    override suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>) {
        val grouped = exercises.map { exercise ->
            exercise.copy(id = nextRoutineExerciseId++)
        }.groupBy { it.routineId }
        routines.value = routines.value.map { routine ->
            val additions = grouped[routine.routine.id].orEmpty().map { routineExercise ->
                RoutineExerciseWithDetails(
                    routineExercise = routineExercise,
                    exercise = ExerciseEntity(
                        id = routineExercise.exerciseId,
                        name = "운동 ${routineExercise.exerciseId}",
                        targetArea = "기타",
                    ),
                )
            }
            if (additions.isEmpty()) routine else routine.copy(exercises = routine.exercises + additions)
        }
    }

    override suspend fun deleteRoutine(routineId: Long) {
        routines.value = routines.value.filterNot { it.routine.id == routineId }
    }
}
