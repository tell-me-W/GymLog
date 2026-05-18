package com.gymlog

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.ExerciseInputType
import com.gymlog.data.local.RoutineWithExercises
import com.gymlog.data.local.SessionExerciseWithDetails
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSetEntity
import com.gymlog.data.repository.MonthlyWorkoutSummary
import com.gymlog.data.repository.SeedExercises
import com.gymlog.domain.WorkoutCalculator
import com.gymlog.domain.WorkoutSetInput
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.GymLogViewModelFactory
import com.gymlog.ui.ExercisePickerSorter
import com.gymlog.ui.RecentExerciseRecord
import com.gymlog.ui.RoutineCreationPolicy
import com.gymlog.ui.Screen
import com.gymlog.ui.SummaryUiState
import com.gymlog.ui.WorkoutShareContent
import com.gymlog.ui.WorkoutShareImage
import com.gymlog.ui.formatDuration
import com.gymlog.ui.formatKoreanDate
import com.gymlog.ui.formatKoreanYearMonth
import com.gymlog.ui.rest.RestTimerManager
import java.time.LocalDate
import java.time.YearMonth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    title: String,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text(title, color = Color.White) }, 
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            ) 
        },
        containerColor = Color(0xFF121212)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(viewModel: GymLogViewModel) {
    val completedDates by viewModel.completedDates.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val draftSessionId by viewModel.draftSessionId.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GymLog", color = Color.White) },
                actions = {
                    IconButton(onClick = viewModel::openSettings) {
                        Text("⚙", color = Color.White, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatKoreanYearMonth(selectedMonth),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Row {
                    TextButton(onClick = viewModel::showPreviousMonth) {
                        Text("<", color = Color.White, fontSize = 20.sp)
                    }
                    TextButton(onClick = viewModel::showNextMonth) {
                        Text(">", color = Color.White, fontSize = 20.sp)
                    }
                }
            }
            MonthCalendar(
                month = selectedMonth,
                completedDates = completedDates,
            )
            MonthlySummaryRow(monthlySummary)
            Spacer(modifier = Modifier.weight(1f))
            draftSessionId?.let {
                Button(
                    onClick = { viewModel.resumeDraft(it) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("진행 중 운동 이어하기", color = Color.White)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = viewModel::openStartWorkout,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("운동 시작", color = Color.White)
                }
                Button(
                    onClick = viewModel::openHistory,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("기록 보기", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryRow(summary: MonthlyWorkoutSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonthlySummaryMetric(
            label = "운동",
            value = "${summary.sessionCount}회",
            modifier = Modifier.weight(1f),
        )
        MonthlySummaryMetric(
            label = "총 볼륨",
            value = "${summary.totalVolumeKg.toInt()} kg",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MonthlySummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(
            value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    completedDates: Set<LocalDate>,
) {
    val firstDay = month.atDay(1)
    val days = (1..month.lengthOfMonth()).map { firstDay.withDayOfMonth(it) }
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { day ->
                Text(text = day, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val emptyDays = firstDay.dayOfWeek.value - 1
            items(emptyDays) { Spacer(modifier = Modifier.size(40.dp)) }
            
            items(days) { date ->
                val hasWorkout = completedDates.contains(date)
                val isToday = date == LocalDate.now()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (hasWorkout) Color(0xFF3B82F6) else Color.Transparent,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.dayOfMonth.toString(), 
                        color = if (hasWorkout) Color.White else Color.LightGray,
                        fontWeight = if (hasWorkout) FontWeight.Bold else FontWeight.Normal,
                    )
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: GymLogViewModel) {
    val profile by viewModel.profile.collectAsState()
    val message by viewModel.settingsMessage.collectAsState()
    val context = LocalContext.current
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var showTextImport by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val appVersionLabel = remember {
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            "${packageInfo.versionName} ($versionCode)"
        }.getOrDefault("알 수 없음")
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingBackupJson ?: return@rememberLauncherForActivityResult
        pendingBackupJson = null
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) viewModel.importBackupJson(json)
        }
    }

    LaunchedEffect(profile) {
        profile?.let {
            height = if (it.heightCm > 0.0) it.heightCm.toString() else ""
            weight = if (it.weightKg > 0.0) it.weightKg.toString() else ""
            gender = it.gender
            age = if (it.age > 0) it.age.toString() else ""
        }
    }

    AppScaffold("설정") {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("프로필", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("키(cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("몸무게(kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("성별") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("나이") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                height.toDoubleOrNull() ?: 0.0,
                                weight.toDoubleOrNull() ?: 0.0,
                                gender,
                                age.toIntOrNull() ?: 0,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    ) {
                        Text("프로필 저장", color = Color.White)
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("앱 버전", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(appVersionLabel, color = Color.Gray)
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("운동 기록", color = Color.White, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("불러오기")
                    }
                    Button(
                        onClick = { showTextImport = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("텍스트로 추가")
                    }
                    Button(
                        onClick = {
                            viewModel.exportBackupJson { json ->
                                pendingBackupJson = json
                                val name = "gymlog-backup-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}.json"
                                backupLauncher.launch(name)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("운동 기록 백업하기")
                    }
                }
            }
        }
    }

    if (showTextImport) {
        AlertDialog(
            onDismissRequest = { showTextImport = false },
            title = { Text("운동 기록 텍스트 추가") },
            text = {
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    minLines = 10,
                    label = { Text("운동 기록 붙여넣기") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.importWorkoutText(importText)
                        showTextImport = false
                        importText = ""
                    },
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextImport = false }) { Text("취소") }
            },
        )
    }

    message?.let {
        AlertDialog(
            onDismissRequest = viewModel::clearSettingsMessage,
            title = { Text("알림") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = viewModel::clearSettingsMessage) { Text("확인") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartWorkoutBottomSheet(viewModel: GymLogViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { viewModel.handleBack() },
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "운동 시작",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = viewModel::startEmptyWorkout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("빈 운동으로 시작", color = Color.White)
            }
            Button(
                onClick = viewModel::openCopyFromDate,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("이전 기록 불러오기", color = Color.White)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CopyFromDateScreen(viewModel: GymLogViewModel) {
    val sessions by viewModel.completedSessions.collectAsState()
    var selectedSessionId by remember(sessions) { mutableStateOf(sessions.firstOrNull()?.id) }
    val sessionState = selectedSessionId?.let { viewModel.observeSession(it).collectAsState(initial = null) }
    val details = sessionState?.value
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.handleBack() },
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "이전 기록 불러오기",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            if (sessions.isEmpty()) {
                Text("완료한 운동 기록이 없습니다.", color = Color.Gray, modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally))
            } else {
                val dotFormatter = remember { java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault()) }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sessions) { session ->
                        val isSelected = session.id == selectedSessionId
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) Color(0xFF4B5563) else Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedSessionId = session.id }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dotFormatter.format(java.util.Date(session.startedAtMillis)), 
                                    color = if (isSelected) Color.White else Color.Gray, 
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                
                details?.let {
                    val orderedExercises = it.exercises.sortedBy { ex -> ex.sessionExercise.sortOrder }
                    val duration = formatDuration(sessionDurationSeconds(it.session))
                    Text("${orderedExercises.size}종목 • $duration", fontWeight = FontWeight.Bold, color = Color.White)
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false).heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orderedExercises) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.BottomEnd)
                                            .background(Color(0xFFD1D5DB), RoundedCornerShape(topStart = 8.dp, bottomEnd = 12.dp))
                                    )
                                }
                                Column {
                                    Text(item.exercise.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    val setGroups = item.sets.groupBy {
                                        if (item.exercise.inputType == ExerciseInputType.DURATION) {
                                            "시간 ${it.durationSeconds / 60}분"
                                        } else if (it.weightKg > 0.0) {
                                            "${if (it.weightKg % 1.0 == 0.0) it.weightKg.toInt() else it.weightKg}kg·${it.reps}회"
                                        } else {
                                            "${it.reps}회"
                                        }
                                    }
                                    val setString = setGroups.entries.joinToString("\n") { (key, groupedSets) ->
                                        "$key ${groupedSets.size}세트"
                                    }
                                    Text(setString, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.handleBack()
                        viewModel.startEmptyWorkout() 
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("빈운동", color = Color.White)
                }
                Button(
                    onClick = {
                        selectedSessionId?.let { 
                            viewModel.copyWorkout(it) 
                        }
                    },
                    enabled = selectedSessionId != null,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("불러오기", color = Color.White)
                }
            }
        }
    }
}
@Composable
private fun HistoryScreen(viewModel: GymLogViewModel) {
    val sessions by viewModel.completedSessions.collectAsState()
    var deleteTarget by remember { mutableStateOf<WorkoutSessionEntity?>(null) }

    AppScaffold(
        title = "운동 기록",
        actions = {
            TextButton(onClick = viewModel::goDashboard) {
                Text("대시보드로 돌아가기", color = Color(0xFF9CA3AF))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("완료한 운동", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sessions) { session ->
                    HistorySessionRow(
                        session = session,
                        onClick = { viewModel.openHistoryDetail(session.id) },
                        onDelete = { deleteTarget = session },
                    )
                }
                if (sessions.isEmpty()) {
                    item { Text("아직 완료한 운동 기록이 없습니다.", color = Color.Gray) }
                }
            }
        }
    }

    deleteTarget?.let { session ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("운동 기록 삭제") },
            text = { Text("${formatKoreanDate(session.startedAtMillis)} 기록을 삭제할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHistorySession(session.id)
                        deleteTarget = null
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun HistorySessionRow(
    session: WorkoutSessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(formatKoreanDate(session.startedAtMillis), fontWeight = FontWeight.Bold, color = Color.White)
            Text("운동 시간 ${formatDuration(sessionDurationSeconds(session))}", color = Color.Gray)
        }
        TextButton(onClick = onDelete) {
            Text("삭제", color = Color(0xFFEF4444))
        }
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
                        Text(formatKoreanDate(details.session.startedAtMillis), fontWeight = FontWeight.Bold, color = Color.White)
                        Text("총 볼륨 ${summary.totalVolumeKg.toInt()} kg", color = Color.Gray)
                        Text("운동 시간 ${formatDuration(summary.durationSeconds)}", color = Color.Gray)
                        Text("종목 ${summary.exerciseCount}개 · 세트 ${summary.setCount}개", color = Color.Gray)
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
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(item.exercise.name, fontWeight = FontWeight.Bold, color = Color.White)
        item.sets.sortedBy { it.sortOrder }.forEachIndexed { index, set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${index + 1}세트", color = Color.Gray)
                Text(formatSetValue(set, item.exercise.inputType), color = Color.White)
            }
        }
    }
}

private fun formatSetValue(set: WorkoutSetEntity, inputType: ExerciseInputType): String {
    return if (inputType == ExerciseInputType.DURATION) {
        "${set.durationSeconds / 60}분"
    } else {
        "${set.weightKg} kg × ${set.reps} reps"
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddExerciseDialog(
    exercises: List<ExerciseEntity>,
    routines: List<RoutineWithExercises>,
    recentRecords: Map<Long, RecentExerciseRecord>,
    selectedTarget: String,
    onTargetSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onExercisesSelected: (List<ExerciseEntity>) -> Unit,
    onCustomExercise: (String, String, Int) -> Unit,
    onRoutineSelected: (RoutineWithExercises) -> Unit,
    onCreateRoutine: (String, List<Long>) -> Unit,
    onDeleteRoutine: (Long) -> Unit,
) {
    var customName by remember { mutableStateOf("") }
    var customRest by remember { mutableStateOf("90") }
    var showCustomForm by remember { mutableStateOf(false) }
    var routineName by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("종목") }
    var customTarget by remember(selectedTarget) {
        mutableStateOf(
            selectedTarget.takeUnless { it == SeedExercises.ALL_TARGET_AREA }
                ?: SeedExercises.exerciseTargetAreas.first()
        )
    }
    var selectedExercises by remember { mutableStateOf<Map<Long, ExerciseEntity>>(emptyMap()) }
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("종목 추가", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                if (activeTab == "종목") {
                    IconButton(onClick = {
                        if (selectedExercises.isNotEmpty()) {
                            onExercisesSelected(selectedExercises.values.toList())
                        }
                    }) {
                        Text("+", fontSize = 28.sp, color = Color.Black)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("종목", "루틴").forEach { tab ->
                    Button(
                        onClick = { activeTab = tab },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == tab) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
                        ),
                    ) {
                        Text(tab, color = if (activeTab == tab) Color.White else Color.Black)
                    }
                }
            }
            if (activeTab == "루틴") {
                val canCreateRoutine = RoutineCreationPolicy.canCreate(routineName, selectedExercises.size)
                val selectedExerciseNames = RoutineCreationPolicy.selectedExerciseNames(selectedExercises.values)

                Text("선택한 종목으로 루틴 만들기", fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text(
                    text = if (selectedExerciseNames.isEmpty()) {
                        "종목 탭에서 운동을 선택한 뒤 루틴을 만들 수 있습니다."
                    } else {
                        "${selectedExerciseNames.size}개 종목 선택됨"
                    },
                    color = Color.Gray,
                    fontSize = 12.sp,
                )
                if (selectedExerciseNames.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        selectedExerciseNames.forEachIndexed { index, name ->
                            Text(
                                text = "${index + 1}. $name",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("루틴명") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        if (canCreateRoutine) {
                            onCreateRoutine(routineName, selectedExercises.values.map { it.id })
                            routineName = ""
                        }
                    },
                    enabled = canCreateRoutine,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("루틴 만들기")
                }
                Text("저장된 루틴", fontWeight = FontWeight.SemiBold, color = Color.Black)
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (routines.isEmpty()) {
                        item {
                            Text("저장된 루틴이 없습니다.", color = Color.Gray)
                        }
                    }
                    items(routines) { routine ->
                        val routineExerciseNames = routine.exercises
                            .sortedBy { it.sortOrder }
                            .mapNotNull { routineExercise ->
                                exercises.firstOrNull { it.id == routineExercise.exerciseId }?.name
                            }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                .clickable { onRoutineSelected(routine) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(routine.routine.name, color = Color.Black, fontWeight = FontWeight.Bold)
                                Text(
                                    routineExerciseNames.joinToString(" · ").ifBlank { "${routine.exercises.size}개 종목" },
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                )
                            }
                            TextButton(onClick = { onDeleteRoutine(routine.routine.id) }) {
                                Text("삭제")
                            }
                        }
                    }
                }
            } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("종목 이름을 검색하세요 (ex. ㅅㅋㅌ)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SeedExercises.targetAreas) { target ->
                    val isSelected = selectedTarget == target
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Color(0xFF4B5563) else Color(0xFFF3F4F6),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onTargetSelected(target) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = target,
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            val filteredExercises = ExercisePickerSorter.sort(
                exercises = exercises.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                },
                recentRecords = recentRecords,
            )
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false).heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExercises) { exercise ->
                    val isSelected = exercise.id in selectedExercises
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) Color(0xFFEFF6FF) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedExercises = selectedExercises.toggle(exercise) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color(0xFFD1D5DB), RoundedCornerShape(topStart = 8.dp, bottomEnd = 12.dp))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            val recent = recentRecords[exercise.id]
                            Text(
                                text = recent?.let {
                                    "최근 ${SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(it.lastPerformedMillis))} · ${it.setCount}세트"
                                } ?: "최근 기록 없음",
                                color = Color.Gray,
                                fontSize = 12.sp,
                            )
                        }
                        if (isSelected) {
                            Text("✓", color = Color.Blue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { showCustomForm = !showCustomForm },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (showCustomForm) "커스텀 닫기" else "커스텀 종목 추가")
            }
            if (showCustomForm) {
                Text("커스텀 종목", fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SeedExercises.exerciseTargetAreas) { target ->
                        FilterChip(
                            selected = customTarget == target,
                            onClick = { customTarget = target },
                            label = { Text(target) },
                        )
                    }
                }
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
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            onCustomExercise(customName, customTarget, customRest.toIntOrNull() ?: 90)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("추가하기")
                }
            }
            }
        }
    }
}

private fun Map<Long, ExerciseEntity>.toggle(exercise: ExerciseEntity): Map<Long, ExerciseEntity> {
    return if (exercise.id in this) this - exercise.id else this + (exercise.id to exercise)
}

@Composable
private fun SummaryScreen(viewModel: GymLogViewModel, summary: SummaryUiState) {
    val context = LocalContext.current
    AppScaffold("운동 완료") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("총 볼륨", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text("${summary.totalVolumeKg.toInt()} kg", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("운동 시간 ${formatDuration(summary.durationSeconds)}", color = Color.White)
            Text("종목 ${summary.exerciseCount}개 · 세트 ${summary.setCount}개", color = Color.White)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, WorkoutShareContent.buildText(summary))
                    }
                    context.startActivity(Intent.createChooser(intent, "운동 기록 공유"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("텍스트 공유", color = Color.White)
            }
            Button(
                onClick = {
                    val imageUri = WorkoutShareImage.createImageUri(context, summary)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "운동 인증 이미지 공유"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("이미지 공유", color = Color.White)
            }
            Button(
                onClick = viewModel::goDashboard,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("대시보드로 돌아가기", color = Color.White)
            }
        }
    }
}
