package com.gymlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.GymLogViewModelFactory
import com.gymlog.ui.Screen
import com.gymlog.ui.dashboard.DashboardScreen
import com.gymlog.ui.history.HistoryDetailScreen
import com.gymlog.ui.history.HistoryScreen
import com.gymlog.ui.logger.LoggerScreen
import com.gymlog.ui.settings.SettingsScreen
import com.gymlog.ui.start.CopyFromDateScreen
import com.gymlog.ui.start.StartWorkoutBottomSheet
import com.gymlog.ui.summary.SummaryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as GymLogApplication).container
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GymLogApp(container)
                }
            }
        }
    }
}

@Composable
private fun GymLogApp(container: AppContainer) {
    val viewModel: GymLogViewModel = viewModel(
        factory = GymLogViewModelFactory(
            exerciseRepository = container.exerciseRepository,
            workoutRepository = container.workoutRepository,
            profileRepository = container.profileRepository,
            workoutImportRepository = container.workoutImportRepository,
            routineRepository = container.routineRepository,
        )
    )
    val screen by viewModel.screen.collectAsState()

    BackHandler(enabled = screen != Screen.Dashboard) {
        viewModel.handleBack()
    }

    when (val current = screen) {
        Screen.Dashboard, Screen.Start, Screen.CopyFromDate -> {
            DashboardScreen(viewModel)
            if (current == Screen.Start) {
                StartWorkoutBottomSheet(viewModel)
            } else if (current == Screen.CopyFromDate) {
                CopyFromDateScreen(viewModel)
            }
        }
        Screen.Settings -> SettingsScreen(viewModel)
        Screen.History -> HistoryScreen(viewModel)
        is Screen.HistoryDetail -> HistoryDetailScreen(viewModel, current.sessionId)
        is Screen.Logger -> LoggerScreen(viewModel, current.sessionId)
        is Screen.Summary -> SummaryScreen(viewModel, current.summary)
    }
}