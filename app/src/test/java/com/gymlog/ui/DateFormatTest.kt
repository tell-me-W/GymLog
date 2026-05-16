package com.gymlog.ui

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatTest {
    @Test
    fun formatKoreanYearMonthShowsYearAndMonth() {
        assertEquals("2026년 5월", formatKoreanYearMonth(YearMonth.of(2026, 5)))
    }
}
