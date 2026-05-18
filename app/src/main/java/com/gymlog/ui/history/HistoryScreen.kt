package com.gymlog.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymlog.data.local.SessionExerciseWithDetails
import com.gymlog.data.local.WorkoutSessionEntity
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.domain.WorkoutCalculator
import com.gymlog.domain.WorkoutSetInput
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.common.AppScaffold
import com.gymlog.ui.common.formatSetValue
import com.gymlog.ui.common.sessionDurationSeconds
import com.gymlog.ui.formatDuration
import com.gymlog.ui.formatKoreanDate

@Composable
internal fun HistoryScreen(viewModel: GymLogViewModel) {
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
internal fun HistoryDetailScreen(viewModel: GymLogViewModel, sessionId: Long) {
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
