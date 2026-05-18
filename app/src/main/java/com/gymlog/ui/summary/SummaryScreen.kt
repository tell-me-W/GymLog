package com.gymlog.ui.summary

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.SummaryUiState
import com.gymlog.ui.WorkoutShareContent
import com.gymlog.ui.WorkoutShareImage
import com.gymlog.ui.common.AppScaffold
import com.gymlog.ui.formatDuration

@Composable
internal fun SummaryScreen(viewModel: GymLogViewModel, summary: SummaryUiState) {
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
