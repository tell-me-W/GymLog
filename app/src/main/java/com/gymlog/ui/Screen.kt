package com.gymlog.ui

sealed interface Screen {
    data object Dashboard : Screen
    data object Start : Screen
    data object CopyFromDate : Screen
    data object Settings : Screen
    data object History : Screen
    data class HistoryDetail(val sessionId: Long) : Screen
    data class Logger(val sessionId: Long) : Screen
    data class Summary(val summary: SummaryUiState) : Screen
}

fun Screen.backDestination(): Screen {
    return when (this) {
        Screen.Dashboard -> Screen.Dashboard
        is Screen.HistoryDetail -> Screen.History
        else -> Screen.Dashboard
    }
}
