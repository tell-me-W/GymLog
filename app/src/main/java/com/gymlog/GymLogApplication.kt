package com.gymlog

import android.app.Application
import androidx.room.Room
import com.gymlog.data.local.GymLogDatabase
import com.gymlog.data.repository.ExerciseRepository
import com.gymlog.data.repository.ProfileRepository
import com.gymlog.data.repository.WorkoutImportRepository
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
        ).addMigrations(GymLogDatabase.MIGRATION_1_2).build()
        container = AppContainer(
            exerciseRepository = ExerciseRepository(database.exerciseDao()),
            workoutRepository = WorkoutRepository(database.workoutDao()),
            profileRepository = ProfileRepository(database.userProfileDao()),
            workoutImportRepository = WorkoutImportRepository(database.exerciseDao(), database.workoutDao()),
        )
    }
}

data class AppContainer(
    val exerciseRepository: ExerciseRepository,
    val workoutRepository: WorkoutRepository,
    val profileRepository: ProfileRepository,
    val workoutImportRepository: WorkoutImportRepository,
)
