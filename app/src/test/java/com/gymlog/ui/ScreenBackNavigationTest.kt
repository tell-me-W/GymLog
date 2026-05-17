package com.gymlog.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenBackNavigationTest {
    @Test
    fun historyDetailBackReturnsToHistoryList() {
        assertEquals(Screen.History, Screen.HistoryDetail(12L).backDestination())
    }

    @Test
    fun nestedScreensBackReturnToDashboard() {
        assertEquals(Screen.Dashboard, Screen.Start.backDestination())
        assertEquals(Screen.Dashboard, Screen.CopyFromDate.backDestination())
        assertEquals(Screen.Dashboard, Screen.History.backDestination())
        assertEquals(Screen.Dashboard, Screen.Settings.backDestination())
        assertEquals(Screen.Dashboard, Screen.Logger(12L).backDestination())
        assertEquals(Screen.Dashboard, Screen.Summary(SummaryUiState(0.0, 0L, 0, 0)).backDestination())
    }
}
