package com.gymlog.data.repository

import com.gymlog.data.local.RoutineDao
import com.gymlog.data.local.RoutineEntity
import com.gymlog.data.local.RoutineExerciseEntity
import com.gymlog.data.local.RoutineWithExerciseDetails
import kotlinx.coroutines.flow.Flow

class RoutineRepository(
    private val routineDao: RoutineDao,
) {
    fun observeRoutines(): Flow<List<RoutineWithExerciseDetails>> {
        return routineDao.observeRoutines()
    }

    suspend fun createRoutine(name: String, exerciseIds: List<Long>): Long {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "루틴 이름을 입력하세요." }
        require(exerciseIds.isNotEmpty()) { "루틴에 운동을 1개 이상 선택하세요." }
        val routineId = routineDao.insertRoutine(RoutineEntity(name = cleanName))
        routineDao.insertRoutineExercises(
            exerciseIds.mapIndexed { index, exerciseId ->
                RoutineExerciseEntity(
                    routineId = routineId,
                    exerciseId = exerciseId,
                    sortOrder = index,
                )
            }
        )
        return routineId
    }

    suspend fun deleteRoutine(routineId: Long) {
        routineDao.deleteRoutine(routineId)
    }
}
