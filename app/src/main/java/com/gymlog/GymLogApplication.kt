package com.gymlog

import android.app.Application
import androidx.room.Room
import com.gymlog.data.local.GymLogDatabase
import com.gymlog.data.repository.ExerciseRepository
import com.gymlog.data.repository.WorkoutRepository

class GymLogApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            GymLogDatabase::class.java,
            "gymlog.db",
        ).build()
        container = AppContainer(
            exerciseRepository = ExerciseRepository(database.exerciseDao()),
            workoutRepository = WorkoutRepository(database.workoutDao()),
        )
    }
}

data class AppContainer(
    val exerciseRepository: ExerciseRepository,
    val workoutRepository: WorkoutRepository,
)
