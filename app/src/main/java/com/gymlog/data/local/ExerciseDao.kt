package com.gymlog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE isArchived = 0 ORDER BY targetArea, name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE targetArea = :targetArea AND isArchived = 0 ORDER BY name")
    fun observeExercises(targetArea: String): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("SELECT * FROM exercises ORDER BY name")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE name = :name ORDER BY isCustom ASC LIMIT 1")
    suspend fun getByName(name: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
}
