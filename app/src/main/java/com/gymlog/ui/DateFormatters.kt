package com.gymlog.ui

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatKoreanDate(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
}

fun formatKoreanYearMonth(month: YearMonth): String {
    return "${month.year}년 ${month.monthValue}월"
}
