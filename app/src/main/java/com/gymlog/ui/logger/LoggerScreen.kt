package com.gymlog.ui.logger

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.R
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.ExerciseInputType
import com.gymlog.data.local.SessionExerciseWithDetails
import com.gymlog.data.local.WorkoutSetEntity
import com.gymlog.domain.WorkoutCalculator
import com.gymlog.domain.WorkoutSetInput
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.common.AppScaffold
import com.gymlog.ui.common.formatSetValue
import com.gymlog.ui.common.sessionDurationSeconds
import com.gymlog.ui.exercisepicker.AddExerciseDialog
import com.gymlog.ui.formatDuration
import com.gymlog.ui.rest.RestTimerManager
import kotlinx.coroutines.delay

@Composable
internal fun LoggerScreen(viewModel: GymLogViewModel, sessionId: Long) {
    val session by viewModel.observeSession(sessionId).collectAsState(initial = null)
    val exercises by viewModel.exercises.collectAsState()
    val selectedTarget by viewModel.selectedTarget.collectAsState()
    val recentExerciseRecords by viewModel.recentExerciseRecords.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val context = LocalContext.current
    val restTimer = remember { RestTimerManager(context) }
    var showAddExercise by remember { mutableStateOf(false) }
    var deleteExerciseTarget by remember { mutableStateOf<SessionExerciseWithDetails?>(null) }
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
                Text("경과 ${formatDuration(elapsedSeconds)}", color = Color.White)
                if (restSecondsLeft > 0) {
                    Text("휴식 ${formatDuration(restSecondsLeft)}", color = Color(0xFF3B82F6))
                }
                Button(
                    onClick = { viewModel.completeWorkout(sessionId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("운동 완료", color = Color.White)
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
                        onAddSet = { defaults ->
                            if (index == orderedExercises.lastIndex) {
                                scrollToBottomAfterSetCount = totalSetCount + 1
                            }
                            viewModel.addSet(
                                sessionExerciseId = item.sessionExercise.id,
                                weightKg = defaults.weightKg,
                                reps = defaults.reps,
                                durationSeconds = defaults.durationSeconds,
                            )
                        },
                        onRemoveLastSet = { set -> viewModel.deleteSet(set.id) },
                        onDeleteExercise = {
                            deleteExerciseTarget = item
                        },
                        onUpdateSet = { set, weight, reps, durationSeconds, completed ->
                            val wasIncomplete = !set.isCompleted && completed
                            viewModel.updateSet(set.id, weight, reps, completed, durationSeconds)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("운동 종목 추가", color = Color.White)
            }
        }
    }

    if (showAddExercise) {
        LaunchedEffect(Unit) {
            viewModel.refreshRecentExerciseRecords()
        }
        AddExerciseDialog(
            exercises = exercises,
            routines = routines,
            recentRecords = recentExerciseRecords,
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
            onArchiveCustomExercise = { exercise ->
                viewModel.archiveCustomExercise(exercise.id)
            },
            onRoutineSelected = { routine ->
                viewModel.addRoutine(sessionId, routine)
                showAddExercise = false
            },
            onCreateRoutine = { name, exerciseIds ->
                viewModel.createRoutine(name, exerciseIds)
            },
            onDeleteRoutine = viewModel::deleteRoutine,
        )
    }

    deleteExerciseTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteExerciseTarget = null },
            title = { Text("운동 삭제") },
            text = { Text("${target.exercise.name} 종목을 삭제할까요? 입력한 세트도 함께 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExercise(target.sessionExercise.id)
                        deleteExerciseTarget = null
                    }
                ) {
                    Text("삭제", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteExerciseTarget = null }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun SessionExerciseCard(
    item: SessionExerciseWithDetails,
    onAddSet: (WorkoutSetInput) -> Unit,
    onRemoveLastSet: (WorkoutSetEntity) -> Unit,
    onDeleteExercise: () -> Unit,
    onUpdateSet: (WorkoutSetEntity, Double, Int, Int, Boolean) -> Unit,
) {
    val orderedSets = item.sets.sortedBy { it.sortOrder }
    val canRemoveLastSet = WorkoutCalculator.canRemoveLastSet(orderedSets.size)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(item.exercise.name, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${item.exercise.targetArea} · 휴식 ${item.exercise.defaultRestSeconds}초", color = Color.Gray)
            }
            IconButton(onClick = onDeleteExercise) {
                Image(
                    painter = painterResource(id = R.drawable.ic_trash_64px),
                    contentDescription = "운동 삭제",
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                    .clickable(enabled = canRemoveLastSet) {
                        orderedSets.lastOrNull()?.let(onRemoveLastSet)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canRemoveLastSet) Color.White else Color.Gray
                )
            }
            Text(
                text = "세트",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                    .clickable {
                        val lastSet = orderedSets.lastOrNull()?.let {
                            WorkoutSetInput(
                                weightKg = it.weightKg,
                                reps = it.reps,
                                durationSeconds = it.durationSeconds,
                            )
                        }
                        onAddSet(WorkoutCalculator.nextSetDefaults(lastSet))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(32.dp))
        }
        orderedSets.forEachIndexed { index, set ->
            SetRow(
                index = index + 1,
                set = set,
                inputType = item.exercise.inputType,
                onUpdateSet = onUpdateSet,
            )
        }
    }
}

@Composable
private fun SetRow(
    index: Int,
    set: WorkoutSetEntity,
    inputType: ExerciseInputType,
    onUpdateSet: (WorkoutSetEntity, Double, Int, Int, Boolean) -> Unit,
) {
    var weight by remember(set.id, set.weightKg) { mutableStateOf(if (set.weightKg > 0) set.weightKg.toString() else "") }
    var reps by remember(set.id, set.reps) { mutableStateOf(if (set.reps > 0) set.reps.toString() else "") }
    var minutes by remember(set.id, set.durationSeconds) {
        mutableStateOf(if (set.durationSeconds > 0) (set.durationSeconds / 60).toString() else "")
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF333333), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("$index", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        if (inputType == ExerciseInputType.DURATION) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(2f)
            ) {
                BasicTextField(
                    value = minutes,
                    onValueChange = {
                        minutes = it
                        onUpdateSet(set, 0.0, 0, (it.toIntOrNull() ?: 0) * 60, set.isCompleted)
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.width(70.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("분", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        onUpdateSet(set, it.toDoubleOrNull() ?: 0.0, reps.toIntOrNull() ?: 0, set.durationSeconds, set.isCompleted)
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("kg", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = reps,
                    onValueChange = {
                        reps = it
                        onUpdateSet(set, weight.toDoubleOrNull() ?: 0.0, it.toIntOrNull() ?: 0, set.durationSeconds, set.isCompleted)
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.width(50.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("회", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (set.isCompleted) Color(0xFF3B82F6) else Color(0xFF333333),
                    shape = CircleShape
                )
                .clickable {
                    onUpdateSet(
                        set,
                        if (inputType == ExerciseInputType.DURATION) 0.0 else weight.toDoubleOrNull() ?: 0.0,
                        if (inputType == ExerciseInputType.DURATION) 0 else reps.toIntOrNull() ?: 0,
                        if (inputType == ExerciseInputType.DURATION) (minutes.toIntOrNull() ?: 0) * 60 else set.durationSeconds,
                        !set.isCompleted,
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
