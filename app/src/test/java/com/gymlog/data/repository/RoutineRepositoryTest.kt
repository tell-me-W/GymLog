package com.gymlog.data.repository

import com.gymlog.data.local.RoutineDao
import com.gymlog.data.local.RoutineEntity
import com.gymlog.data.local.RoutineExerciseEntity
import com.gymlog.data.local.RoutineWithExercises
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
        assertEquals(listOf(3L, 7L, 2L), routine.exercises.map { it.exerciseId })
        assertEquals(listOf(0, 1, 2), routine.exercises.map { it.sortOrder })
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
    val routines = MutableStateFlow<List<RoutineWithExercises>>(emptyList())
    private var nextRoutineId = 1L
    private var nextRoutineExerciseId = 1L

    override fun observeRoutines(): Flow<List<RoutineWithExercises>> = routines

    override suspend fun insertRoutine(routine: RoutineEntity): Long {
        val id = nextRoutineId++
        routines.value = routines.value + RoutineWithExercises(routine.copy(id = id), emptyList())
        return id
    }

    override suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>) {
        val grouped = exercises.map { exercise ->
            exercise.copy(id = nextRoutineExerciseId++)
        }.groupBy { it.routineId }
        routines.value = routines.value.map { routine ->
            val additions = grouped[routine.routine.id].orEmpty()
            if (additions.isEmpty()) routine else routine.copy(exercises = routine.exercises + additions)
        }
    }

    override suspend fun deleteRoutine(routineId: Long) {
        routines.value = routines.value.filterNot { it.routine.id == routineId }
    }
}
