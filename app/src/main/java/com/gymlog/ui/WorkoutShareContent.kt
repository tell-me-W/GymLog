package com.gymlog.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object WorkoutShareContent {
    fun buildText(summary: SummaryUiState): String {
        val date = Instant.ofEpochMilli(summary.completedAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        return buildString {
            appendLine("GymLog 운동 완료")
            appendLine(date)
            appendLine("총 볼륨 ${summary.totalVolumeKg.toInt()} kg")
            appendLine("운동 시간 ${formatDuration(summary.durationSeconds)}")
            appendLine("종목 ${summary.exerciseCount}개 · 세트 ${summary.setCount}개")
        }.trim()
    }
}
