package com.gymlog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.RoutineWithExerciseDetails
import com.gymlog.data.local.UserProfileEntity
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.data.importer.WorkoutTextParser
import com.gymlog.data.repository.ExerciseRepository
import com.gymlog.data.repository.ProfileRepository
import com.gymlog.data.repository.RoutineRepository
import com.gymlog.data.repository.SeedExercises
import com.gymlog.data.repository.MonthlyWorkoutSummary
import com.gymlog.data.repository.WorkoutImportRepository
import com.gymlog.data.repository.WorkoutRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GymLogViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
    private val workoutImportRepository: WorkoutImportRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    private val zoneId = ZoneId.systemDefault()
    private val _screen = MutableStateFlow<Screen>(Screen.Dashboard)
    val screen: StateFlow<Screen> = _screen

    private val _selectedTarget = MutableStateFlow(SeedExercises.ALL_TARGET_AREA)
    val selectedTarget: StateFlow<String> = _selectedTarget

    val exercises: StateFlow<List<ExerciseEntity>> = _selectedTarget
        .flatMapLatest { exerciseRepository.observeExercises(SeedExercises.queryTargetOrNull(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedSessions: StateFlow<List<WorkoutSessionEntity>> = workoutRepository
        .observeCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _completedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val completedDates: StateFlow<Set<LocalDate>> = _completedDates

    private val _monthlySummary = MutableStateFlow(MonthlyWorkoutSummary())
    val monthlySummary: StateFlow<MonthlyWorkoutSummary> = _monthlySummary

    private val _selectedMonth = MutableStateFlow(YearMonth.now(zoneId))
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    private val _draftSessionId = MutableStateFlow<Long?>(null)
    val draftSessionId: StateFlow<Long?> = _draftSessionId

    private val _recentExerciseRecords = MutableStateFlow<Map<Long, RecentExerciseRecord>>(emptyMap())
    val recentExerciseRecords: StateFlow<Map<Long, RecentExerciseRecord>> = _recentExerciseRecords

    private val _settingsMessage = MutableStateFlow<String?>(null)
    val settingsMessage: StateFlow<String?> = _settingsMessage

    val profile: StateFlow<UserProfileEntity?> = profileRepository
        .observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val routines: StateFlow<List<RoutineWithExerciseDetails>> = routineRepository
        .observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            exerciseRepository.seedDefaultsIfEmpty()
            refreshCalendar()
            refreshDraft()
            refreshRecentExerciseRecords()
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

    fun handleBack() {
        _screen.value = _screen.value.backDestination()
    }

    fun openStartWorkout() {
        _screen.value = Screen.Start
    }

    fun openCopyFromDate() {
        _screen.value = Screen.CopyFromDate
    }

    fun openSettings() {
        _screen.value = Screen.Settings
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

    fun saveProfile(heightCm: Double, weightKg: Double, gender: String, age: Int) {
        viewModelScope.launch {
            profileRepository.saveProfile(
                UserProfileEntity(
                    heightCm = heightCm.coerceAtLeast(0.0),
                    weightKg = weightKg.coerceAtLeast(0.0),
                    gender = gender.trim(),
                    age = age.coerceAtLeast(0),
                )
            )
        }
    }

    fun refreshRecentExerciseRecords() {
        viewModelScope.launch {
            _recentExerciseRecords.value = workoutImportRepository.recentExerciseRecordsWithinMonths()
        }
    }

    fun exportBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { workoutImportRepository.exportCompletedJson() }
                .onSuccess(onReady)
                .onFailure { _settingsMessage.value = SettingsMessages.backupExportFailure(it) }
        }
    }

    fun importBackupJson(json: String) {
        viewModelScope.launch {
            runCatching { workoutImportRepository.importBackupJson(json) }
                .onSuccess {
                    refreshCalendar()
                    refreshRecentExerciseRecords()
                    _settingsMessage.value = SettingsMessages.backupImportSuccess(it)
                }
                .onFailure { _settingsMessage.value = SettingsMessages.backupImportFailure(it) }
        }
    }

    fun importWorkoutText(text: String) {
        viewModelScope.launch {
            runCatching {
                val sessions = WorkoutTextParser(zoneId).parseMany(text).getOrThrow()
                workoutImportRepository.importSessions(sessions)
            }
                .onSuccess {
                    refreshCalendar()
                    refreshRecentExerciseRecords()
                    _settingsMessage.value = SettingsMessages.textImportSuccess(it)
                }
                .onFailure { _settingsMessage.value = SettingsMessages.textImportFailure(it) }
        }
    }

    fun clearSettingsMessage() {
        _settingsMessage.value = null
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

    fun addRoutine(sessionId: Long, routine: RoutineWithExerciseDetails) {
        addExercises(
            sessionId = sessionId,
            exerciseIds = routine.exercises.sortedBy { it.routineExercise.sortOrder }.map { it.routineExercise.exerciseId },
        )
    }

    fun createRoutine(name: String, exerciseIds: List<Long>) {
        viewModelScope.launch {
            runCatching { routineRepository.createRoutine(name, exerciseIds) }
                .onFailure { _settingsMessage.value = it.message ?: "루틴 생성에 실패했습니다." }
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
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
        durationSeconds: Int = 0,
    ) {
        viewModelScope.launch {
            workoutRepository.addSet(
                sessionExerciseId = sessionExerciseId,
                weightKg = weightKg,
                reps = reps,
                durationSeconds = durationSeconds,
            )
        }
    }

    fun updateSet(setId: Long, weightKg: Double, reps: Int, isCompleted: Boolean, durationSeconds: Int? = null) {
        viewModelScope.launch {
            workoutRepository.updateSet(setId, weightKg, reps, isCompleted, durationSeconds)
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
            _screen.value = Screen.Summary(completed.toSummaryUiState())
        }
    }

    private suspend fun refreshCalendar() {
        val month = _selectedMonth.value
        _completedDates.value = workoutRepository.completedDatesInMonth(month.year, month.monthValue)
        _monthlySummary.value = workoutRepository.completedSummaryInMonth(month)
    }

    private suspend fun refreshDraft() {
        _draftSessionId.value = workoutRepository.latestDraftSession()?.id
    }

}

class GymLogViewModelFactory(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
    private val workoutImportRepository: WorkoutImportRepository,
    private val routineRepository: RoutineRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GymLogViewModel(
            exerciseRepository,
            workoutRepository,
            profileRepository,
            workoutImportRepository,
            routineRepository,
        ) as T
    }
}
