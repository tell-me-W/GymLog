package com.gymlog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines ORDER BY name")
    fun observeRoutines(): Flow<List<RoutineWithExerciseDetails>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)
}
