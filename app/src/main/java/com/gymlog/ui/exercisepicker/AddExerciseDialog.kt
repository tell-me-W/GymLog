package com.gymlog.ui.exercisepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.data.local.ExerciseEntity
import com.gymlog.data.local.RoutineWithExerciseDetails
import com.gymlog.data.repository.SeedExercises
import com.gymlog.ui.ExerciseArchivePolicy
import com.gymlog.ui.ExercisePickerSorter
import com.gymlog.ui.RecentExerciseRecord
import com.gymlog.ui.RoutineCreationPolicy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun AddExerciseDialog(
    exercises: List<ExerciseEntity>,
    routines: List<RoutineWithExerciseDetails>,
    recentRecords: Map<Long, RecentExerciseRecord>,
    selectedTarget: String,
    onTargetSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onExercisesSelected: (List<ExerciseEntity>) -> Unit,
    onCustomExercise: (String, String, Int) -> Unit,
    onArchiveCustomExercise: (ExerciseEntity) -> Unit,
    onRoutineSelected: (RoutineWithExerciseDetails) -> Unit,
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
    var archiveTarget by remember { mutableStateOf<ExerciseEntity?>(null) }
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
                            .sortedBy { it.routineExercise.sortOrder }
                            .map { it.exercise.name }
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
                        if (ExerciseArchivePolicy.canArchive(exercise)) {
                            TextButton(onClick = { archiveTarget = exercise }) {
                                Text("삭제")
                            }
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

    archiveTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { archiveTarget = null },
            title = { Text("커스텀 종목 삭제") },
            text = { Text("이 커스텀 종목을 목록에서 숨길까요? 기존 운동 기록은 유지됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedExercises = selectedExercises - target.id
                        onArchiveCustomExercise(target)
                        archiveTarget = null
                    },
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { archiveTarget = null }) {
                    Text("취소")
                }
            },
        )
    }
}

private fun Map<Long, ExerciseEntity>.toggle(exercise: ExerciseEntity): Map<Long, ExerciseEntity> {
    return if (exercise.id in this) this - exercise.id else this + (exercise.id to exercise)
}
