package com.gymlog.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.sp
import com.gymlog.data.local.ExerciseInputType
import com.gymlog.data.local.WorkoutSessionWithExercises
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.common.AppScaffold
import com.gymlog.ui.common.formatSetValue
import com.gymlog.ui.common.sessionDurationSeconds
import com.gymlog.ui.formatDuration
import com.gymlog.ui.formatKoreanDate

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun CopyFromDateScreen(viewModel: GymLogViewModel) {
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
