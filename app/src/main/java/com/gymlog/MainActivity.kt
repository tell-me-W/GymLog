package com.gymlog

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.SessionExerciseWithDetails
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSetEntity
import com.gymlog.data.repository.SeedExercises
import com.gymlog.domain.WorkoutCalculator
import com.gymlog.domain.WorkoutSetInput
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.GymLogViewModelFactory
import com.gymlog.ui.Screen
import com.gymlog.ui.SummaryUiState
import com.gymlog.ui.formatDuration
import com.gymlog.ui.formatKoreanDate
import com.gymlog.ui.formatKoreanYearMonth
import com.gymlog.ui.rest.RestTimerManager
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

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
        )
    )
    val screen by viewModel.screen.collectAsState()

    when (val current = screen) {
        Screen.Dashboard -> DashboardScreen(viewModel)
        Screen.Start -> StartWorkoutScreen(viewModel)
        Screen.CopyFromDate -> CopyFromDateScreen(viewModel)
        Screen.History -> HistoryScreen(viewModel)
        is Screen.HistoryDetail -> HistoryDetailScreen(viewModel, current.sessionId)
        is Screen.Logger -> LoggerScreen(viewModel, current.sessionId)
        is Screen.Summary -> SummaryScreen(viewModel, current.summary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(title: String, content: @Composable () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

@Composable
private fun DashboardScreen(viewModel: GymLogViewModel) {
    val completedDates by viewModel.completedDates.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val draftSessionId by viewModel.draftSessionId.collectAsState()
    AppScaffold("GymLog") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "이번 달 운동",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = viewModel::showPreviousMonth) {
                    Text("<")
                }
                Text(
                    text = formatKoreanYearMonth(selectedMonth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedButton(onClick = viewModel::showNextMonth) {
                    Text(">")
                }
            }
            MonthCalendar(
                month = selectedMonth,
                completedDates = completedDates,
            )
            draftSessionId?.let {
                OutlinedButton(
                    onClick = { viewModel.resumeDraft(it) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("진행 중 운동 이어하기")
                }
            }
            Button(
                onClick = viewModel::openStartWorkout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("운동 시작")
            }
            OutlinedButton(
                onClick = viewModel::openHistory,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("운동 기록 보기")
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    completedDates: Set<LocalDate>,
) {
    val firstDay = month.atDay(1)
    val days = (1..month.lengthOfMonth()).map { firstDay.withDayOfMonth(it) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(days) { date ->
            val hasWorkout = completedDates.contains(date)
            Column(
                modifier = Modifier
                    .background(
                        color = if (hasWorkout) Color(0xFFE0F7EA) else Color(0xFFF4F4F5),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(date.dayOfMonth.toString(), fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (hasWorkout) Color(0xFF16A34A) else Color.Transparent,
                            shape = RoundedCornerShape(3.dp),
                        )
                )
            }
        }
    }
}

@Composable
private fun StartWorkoutScreen(viewModel: GymLogViewModel) {
    AppScaffold("운동 시작") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = viewModel::startEmptyWorkout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("빈 운동으로 시작")
            }
            OutlinedButton(
                onClick = viewModel::openCopyFromDate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("이전 날짜 기록 불러오기")
            }
            TextButton(onClick = viewModel::goDashboard) {
                Text("돌아가기")
            }
        }
    }
}

@Composable
private fun CopyFromDateScreen(viewModel: GymLogViewModel) {
    val sessions by viewModel.completedSessions.collectAsState()
    AppScaffold("날짜 선택") {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text("완료한 운동 날짜를 선택하세요.", style = MaterialTheme.typography.titleMedium)
            }
            items(sessions) { session ->
                OutlinedButton(
                    onClick = { viewModel.copyWorkout(session.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(formatKoreanDate(session.startedAtMillis))
                }
            }
            if (sessions.isEmpty()) {
                item { Text("아직 복사할 완료 운동이 없습니다.") }
            }
        }
    }
}

@Composable
private fun HistoryScreen(viewModel: GymLogViewModel) {
    val sessions by viewModel.completedSessions.collectAsState()
    AppScaffold("운동 기록") {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text("완료한 운동", style = MaterialTheme.typography.titleMedium)
            }
            items(sessions) { session ->
                HistorySessionRow(
                    session = session,
                    onClick = { viewModel.openHistoryDetail(session.id) },
                )
            }
            if (sessions.isEmpty()) {
                item { Text("아직 완료한 운동 기록이 없습니다.") }
            }
            item {
                TextButton(onClick = viewModel::goDashboard) {
                    Text("대시보드로 돌아가기")
                }
            }
        }
    }
}

@Composable
private fun HistorySessionRow(
    session: WorkoutSessionEntity,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(formatKoreanDate(session.startedAtMillis), fontWeight = FontWeight.Bold)
        Text("운동 시간 ${formatDuration(sessionDurationSeconds(session))}")
    }
}

@Composable
private fun HistoryDetailScreen(viewModel: GymLogViewModel, sessionId: Long) {
    val session by viewModel.observeSession(sessionId).collectAsState(initial = null)
    AppScaffold("운동 기록 상세") {
        val details = session
        if (details == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("운동 기록을 불러오는 중입니다.")
                TextButton(onClick = viewModel::openHistory) {
                    Text("목록으로")
                }
            }
        } else {
            val orderedExercises = details.exercises.sortedBy { it.sessionExercise.sortOrder }
            val sets = orderedExercises.flatMap { it.sets }
            val summary = WorkoutCalculator.summarizeSession(
                sets = sets.map { WorkoutSetInput(weightKg = it.weightKg, reps = it.reps) },
                exerciseCount = orderedExercises.size,
                startedAtMillis = details.session.startedAtMillis,
                endedAtMillis = details.session.endedAtMillis ?: details.session.startedAtMillis,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(formatKoreanDate(details.session.startedAtMillis), fontWeight = FontWeight.Bold)
                        Text("총 볼륨 ${summary.totalVolumeKg.toInt()} kg")
                        Text("운동 시간 ${formatDuration(summary.durationSeconds)}")
                        Text("종목 ${summary.exerciseCount}개 · 세트 ${summary.setCount}개")
                    }
                }
                items(orderedExercises) { item ->
                    HistoryExerciseBlock(item)
                }
                item {
                    Button(
                        onClick = { viewModel.copyWorkout(sessionId) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("이 기록으로 운동 시작")
                    }
                }
                item {
                    TextButton(onClick = viewModel::openHistory) {
                        Text("목록으로")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryExerciseBlock(item: SessionExerciseWithDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(item.exercise.name, fontWeight = FontWeight.Bold)
        item.sets.sortedBy { it.sortOrder }.forEachIndexed { index, set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${index + 1}세트")
                Text("${set.weightKg} kg × ${set.reps} reps")
            }
        }
    }
}

private fun sessionDurationSeconds(session: WorkoutSessionEntity): Long {
    val endedAtMillis = session.endedAtMillis ?: session.startedAtMillis
    return ((endedAtMillis - session.startedAtMillis) / 1000).coerceAtLeast(0)
}

@Composable
private fun LoggerScreen(viewModel: GymLogViewModel, sessionId: Long) {
    val session by viewModel.observeSession(sessionId).collectAsState(initial = null)
    val exercises by viewModel.exercises.collectAsState()
    val selectedTarget by viewModel.selectedTarget.collectAsState()
    val context = LocalContext.current
    val restTimer = remember { RestTimerManager(context) }
    var showAddExercise by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    var restTimerEndsAt by remember { mutableStateOf<Long?>(null) }
    var restSecondsLeft by remember { mutableStateOf(0L) }
    val startMillis = session?.session?.startedAtMillis
    val exerciseListState = rememberLazyListState()
    val orderedExercises = session?.exercises.orEmpty()
        .sortedBy { it.sessionExercise.sortOrder }
    val totalSetCount = orderedExercises.sumOf { it.sets.size }
    var scrollToBottomAfterSetCount by remember { mutableStateOf<Int?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(startMillis) {
        while (true) {
            startMillis?.let { elapsedSeconds = (System.currentTimeMillis() - it) / 1000 }
            restSecondsLeft = restTimerEndsAt
                ?.let { ((it - System.currentTimeMillis()) / 1000).coerceAtLeast(0) }
                ?: 0
            delay(1000)
        }
    }
    DisposableEffect(Unit) {
        onDispose { restTimer.cancel() }
    }
    LaunchedEffect(totalSetCount, orderedExercises.size, scrollToBottomAfterSetCount) {
        val targetSetCount = scrollToBottomAfterSetCount ?: return@LaunchedEffect
        if (orderedExercises.isNotEmpty() && totalSetCount >= targetSetCount) {
            scrollToBottomAfterSetCount = null
            delay(100)
            exerciseListState.animateScrollToItem(orderedExercises.size)
        }
    }

    AppScaffold("실시간 로깅") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("경과 ${formatDuration(elapsedSeconds)}")
                if (restSecondsLeft > 0) {
                    Text("휴식 ${formatDuration(restSecondsLeft)}")
                }
                Button(onClick = { viewModel.completeWorkout(sessionId) }) {
                    Text("운동 완료")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = exerciseListState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(orderedExercises) { index, item ->
                    SessionExerciseCard(
                        item = item,
                        onAddSet = {
                            if (index == orderedExercises.lastIndex) {
                                scrollToBottomAfterSetCount = totalSetCount + 1
                            }
                            viewModel.addSet(item.sessionExercise.id)
                        },
                        onRemoveLastSet = { set -> viewModel.deleteSet(set.id) },
                        onUpdateSet = { set, weight, reps, completed ->
                            val wasIncomplete = !set.isCompleted && completed
                            viewModel.updateSet(set.id, weight, reps, completed)
                            if (wasIncomplete) {
                                restTimer.start(item.exercise.defaultRestSeconds)
                                restTimerEndsAt = System.currentTimeMillis() +
                                    item.exercise.defaultRestSeconds * 1000L
                            }
                        },
                    )
                }
                if (orderedExercises.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
            Button(
                onClick = { showAddExercise = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("운동 종목 추가")
            }
        }
    }

    if (showAddExercise) {
        AddExerciseDialog(
            exercises = exercises,
            selectedTarget = selectedTarget,
            onTargetSelected = viewModel::selectTarget,
            onDismiss = { showAddExercise = false },
            onExercisesSelected = {
                viewModel.addExercises(sessionId, it.map { exercise -> exercise.id })
                showAddExercise = false
            },
            onCustomExercise = { name, target, restSeconds ->
                viewModel.addCustomExerciseAndAttach(sessionId, name, target, restSeconds)
                showAddExercise = false
            },
        )
    }
}

@Composable
private fun SessionExerciseCard(
    item: SessionExerciseWithDetails,
    onAddSet: () -> Unit,
    onRemoveLastSet: (WorkoutSetEntity) -> Unit,
    onUpdateSet: (WorkoutSetEntity, Double, Int, Boolean) -> Unit,
) {
    val orderedSets = item.sets.sortedBy { it.sortOrder }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(item.exercise.name, fontWeight = FontWeight.Bold)
                Text("${item.exercise.targetArea} · 휴식 ${item.exercise.defaultRestSeconds}초")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    orderedSets.lastOrNull()?.let(onRemoveLastSet)
                },
                enabled = orderedSets.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "-",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "세트",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(
                onClick = onAddSet,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        orderedSets.forEachIndexed { index, set ->
            SetRow(
                index = index + 1,
                set = set,
                onUpdateSet = onUpdateSet,
            )
        }
    }
}

@Composable
private fun SetRow(
    index: Int,
    set: WorkoutSetEntity,
    onUpdateSet: (WorkoutSetEntity, Double, Int, Boolean) -> Unit,
) {
    var weight by remember(set.id, set.weightKg) { mutableStateOf(set.weightKg.toString()) }
    var reps by remember(set.id, set.reps) { mutableStateOf(set.reps.toString()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$index", modifier = Modifier.size(24.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                onUpdateSet(set, it.toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0, set.isCompleted)
            },
            label = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it
                onUpdateSet(set, weight.toDoubleOrNull() ?: 0.0, it.toIntOrNull() ?: 0, set.isCompleted)
            },
            label = { Text("reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = {
                onUpdateSet(set, weight.toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0, it)
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddExerciseDialog(
    exercises: List<ExerciseEntity>,
    selectedTarget: String,
    onTargetSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onExercisesSelected: (List<ExerciseEntity>) -> Unit,
    onCustomExercise: (String, String, Int) -> Unit,
) {
    var customName by remember { mutableStateOf("") }
    var customRest by remember { mutableStateOf("90") }
    var selectedExercises by remember { mutableStateOf<Map<Long, ExerciseEntity>>(emptyMap()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("운동 종목 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SeedExercises.targetAreas) { target ->
                        FilterChip(
                            selected = selectedTarget == target,
                            onClick = { onTargetSelected(target) },
                            label = { Text(target) },
                        )
                    }
                }
                LazyColumn(modifier = Modifier.height(220.dp)) {
                    items(exercises) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExercises = selectedExercises.toggle(exercise)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        )
                        {
                            Checkbox(
                                checked = exercise.id in selectedExercises,
                                onCheckedChange = {
                                    selectedExercises = selectedExercises.toggle(exercise)
                                },
                            )
                            Text(exercise.name)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("커스텀 종목", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("종목명") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = customRest,
                    onValueChange = { customRest = it },
                    label = { Text("기본 휴식 초") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Column {
                TextButton(
                    onClick = {
                        if (selectedExercises.isNotEmpty()) {
                            onExercisesSelected(selectedExercises.values.toList())
                        }
                    }
                ) {
                    Text("선택 추가 (${selectedExercises.size})")
                }
                TextButton(
                    onClick = {
                        if (customName.isNotBlank()) {
                            onCustomExercise(customName, selectedTarget, customRest.toIntOrNull() ?: 90)
                        }
                    }
                ) {
                    Text("커스텀 추가")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
    )
}

private fun Map<Long, ExerciseEntity>.toggle(exercise: ExerciseEntity): Map<Long, ExerciseEntity> {
    return if (exercise.id in this) this - exercise.id else this + (exercise.id to exercise)
}

@Composable
private fun SummaryScreen(viewModel: GymLogViewModel, summary: SummaryUiState) {
    AppScaffold("운동 완료") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("총 볼륨", style = MaterialTheme.typography.titleMedium)
            Text("${summary.totalVolumeKg.toInt()} kg", style = MaterialTheme.typography.headlineMedium)
            Text("운동 시간 ${formatDuration(summary.durationSeconds)}")
            Text("종목 ${summary.exerciseCount}개 · 세트 ${summary.setCount}개")
            Button(
                onClick = viewModel::goDashboard,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("대시보드로 돌아가기")
            }
        }
    }
}
