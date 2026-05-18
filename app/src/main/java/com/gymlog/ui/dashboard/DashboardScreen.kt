package com.gymlog.ui.dashboard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.data.repository.MonthlyWorkoutSummary
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.formatKoreanYearMonth
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardScreen(viewModel: GymLogViewModel) {
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
