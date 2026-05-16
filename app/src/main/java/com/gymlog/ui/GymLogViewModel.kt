package com.gymlog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.data.repository.ExerciseRepository
import com.gymlog.data.repository.SeedExercises
import com.gymlog.data.repository.WorkoutRepository
import com.gymlog.domain.WorkoutCalculator
import com.gymlog.domain.WorkoutSetInput
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Dashboard : Screen
    data object Start : Screen
    data object CopyFromDate : Screen
    data object History : Screen
    data class HistoryDetail(val sessionId: Long) : Screen
    data class Logger(val sessionId: Long) : Screen
    data class Summary(val summary: SummaryUiState) : Screen
}

data class SummaryUiState(
    val totalVolumeKg: Double,
    val durationSeconds: Long,
    val exerciseCount: Int,
    val setCount: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class GymLogViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val zoneId = ZoneId.systemDefault()
    private val _screen = MutableStateFlow<Screen>(Screen.Dashboard)
    val screen: StateFlow<Screen> = _screen

    private val _selectedTarget = MutableStateFlow(SeedExercises.targetAreas.first())
    val selectedTarget: StateFlow<String> = _selectedTarget

    val exercises: StateFlow<List<ExerciseEntity>> = _selectedTarget
        .flatMapLatest { exerciseRepository.observeExercises(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedSessions: StateFlow<List<WorkoutSessionEntity>> = workoutRepository
        .observeCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _completedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val completedDates: StateFlow<Set<LocalDate>> = _completedDates

    private val _selectedMonth = MutableStateFlow(YearMonth.now(zoneId))
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    private val _draftSessionId = MutableStateFlow<Long?>(null)
    val draftSessionId: StateFlow<Long?> = _draftSessionId

    init {
        viewModelScope.launch {
            exerciseRepository.seedDefaultsIfEmpty()
            refreshCalendar()
            refreshDraft()
        }
    }

    fun observeSession(sessionId: Long): Flow<WorkoutSessionWithExercises?> {
        return workoutRepository.observeSession(sessionId)
    }

    fun goDashboard() {
        _screen.value = Screen.Dashboard
        viewModelScope.launch {
            refreshCalendar()
            refreshDraft()
        }
    }

    fun openStartWorkout() {
        _screen.value = Screen.Start
    }

    fun openCopyFromDate() {
        _screen.value = Screen.CopyFromDate
    }

    fun openHistory() {
        _screen.value = Screen.History
    }

    fun openHistoryDetail(sessionId: Long) {
        _screen.value = Screen.HistoryDetail(sessionId)
    }

    fun showPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
        viewModelScope.launch { refreshCalendar() }
    }

    fun showNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
        viewModelScope.launch { refreshCalendar() }
    }

    fun selectTarget(targetArea: String) {
        _selectedTarget.value = targetArea
    }

    fun startEmptyWorkout() {
        viewModelScope.launch {
            val sessionId = workoutRepository.createEmptyDraftSession()
            _screen.value = Screen.Logger(sessionId)
        }
    }

    fun copyWorkout(sourceSessionId: Long) {
        viewModelScope.launch {
            val sessionId = workoutRepository.copyCompletedSessionToDraft(sourceSessionId)
            _screen.value = Screen.Logger(sessionId)
        }
    }

    fun addExercise(sessionId: Long, exerciseId: Long) {
        viewModelScope.launch {
            val sessionExerciseId = workoutRepository.addExerciseToSession(sessionId, exerciseId)
            workoutRepository.addSet(sessionExerciseId)
        }
    }

    fun addExercises(sessionId: Long, exerciseIds: List<Long>) {
        viewModelScope.launch {
            exerciseIds.forEach { exerciseId ->
                val sessionExerciseId = workoutRepository.addExerciseToSession(sessionId, exerciseId)
                workoutRepository.addSet(sessionExerciseId)
            }
        }
    }

    fun resumeDraft(sessionId: Long) {
        _screen.value = Screen.Logger(sessionId)
    }

    fun addCustomExerciseAndAttach(
        sessionId: Long,
        name: String,
        targetArea: String,
        restSeconds: Int,
    ) {
        viewModelScope.launch {
            val exerciseId = exerciseRepository.addCustomExercise(name, targetArea, restSeconds)
            val sessionExerciseId = workoutRepository.addExerciseToSession(sessionId, exerciseId)
            workoutRepository.addSet(sessionExerciseId)
        }
    }

    fun addSet(
        sessionExerciseId: Long,
        weightKg: Double = 0.0,
        reps: Int = 0,
    ) {
        viewModelScope.launch {
            workoutRepository.addSet(
                sessionExerciseId = sessionExerciseId,
                weightKg = weightKg,
                reps = reps,
            )
        }
    }

    fun updateSet(setId: Long, weightKg: Double, reps: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            workoutRepository.updateSet(setId, weightKg, reps, isCompleted)
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { workoutRepository.deleteSet(setId) }
    }

    fun deleteHistorySession(sessionId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSession(sessionId)
            refreshCalendar()
            refreshDraft()
        }
    }

    fun deleteExercise(sessionExerciseId: Long) {
        viewModelScope.launch { workoutRepository.deleteSessionExercise(sessionExerciseId) }
    }

    fun completeWorkout(sessionId: Long) {
        viewModelScope.launch {
            val saved = workoutRepository.completeSession(sessionId)
            if (!saved) {
                refreshCalendar()
                refreshDraft()
                _screen.value = Screen.Dashboard
                return@launch
            }
            val completed = workoutRepository.sessionSnapshot(sessionId) ?: return@launch
            refreshCalendar()
            refreshDraft()
            _screen.value = Screen.Summary(completed.toSummary())
        }
    }

    private suspend fun refreshCalendar() {
        val month = _selectedMonth.value
        _completedDates.value = workoutRepository.completedDatesInMonth(month.year, month.monthValue)
    }

    private suspend fun refreshDraft() {
        _draftSessionId.value = workoutRepository.latestDraftSession()?.id
    }

    private fun WorkoutSessionWithExercises.toSummary(): SummaryUiState {
        val sets = exercises.flatMap { it.sets }
        val summary = WorkoutCalculator.summarizeSession(
            sets = sets.map { WorkoutSetInput(weightKg = it.weightKg, reps = it.reps) },
            exerciseCount = exercises.size,
            startedAtMillis = session.startedAtMillis,
            endedAtMillis = session.endedAtMillis ?: System.currentTimeMillis(),
        )
        return SummaryUiState(
            totalVolumeKg = summary.totalVolumeKg,
            durationSeconds = summary.durationSeconds,
            exerciseCount = summary.exerciseCount,
            setCount = summary.setCount,
        )
    }
}

class GymLogViewModelFactory(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GymLogViewModel(exerciseRepository, workoutRepository) as T
    }
}

fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatKoreanDate(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
}

fun formatKoreanYearMonth(month: YearMonth): String {
    return "${month.year}년 ${month.monthValue}월"
}
