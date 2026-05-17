package com.gymlog.data.importer

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTextParserTest {
    private val parser = WorkoutTextParser(ZoneId.of("Asia/Seoul"))

    @Test
    fun parsesFreeWorkoutTextIntoCompletedSession() {
        val text = """
            [자유 운동] 2026년 5월 15일
            28분

            어시스트 풀업 머신
            45kg x 15회
            45kg x 15회
            45kg x 12회
            45kg x 12회

            와이드 풀다운
            30kg x 14회
            30kg x 12회
            30kg x 12회
            30kg x 12회

            체스트 프레스 머신
            23kg x 12회
            23kg x 12회
            23kg x 12회
            23kg x 12회
            23kg x 12회
        """.trimIndent()

        val result = parser.parse(text).getOrThrow()

        val expectedStart = LocalDate.of(2026, 5, 15)
            .atTime(12, 0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toEpochMilli()
        assertEquals(expectedStart, result.startedAtMillis)
        assertEquals(expectedStart + 28 * 60_000L, result.endedAtMillis)
        assertEquals(3, result.exercises.size)
        assertEquals("어시스트 풀업 머신", result.exercises[0].name)
        assertEquals(listOf(15, 15, 12, 12), result.exercises[0].sets.map { it.reps })
        assertTrue(result.exercises.flatMap { it.sets }.all { it.isCompleted })
    }

    @Test
    fun rejectsInvalidSetLine() {
        val result = parser.parse(
            """
            [자유 운동] 2026년 5월 15일
            28분

            와이드 풀다운
            30킬로 12회
            """.trimIndent()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun parsesMultipleSessionsSeparatedByWorkoutHeader() {
        val result = parser.parseMany(
            """
            [자유 운동] 2026년 5월 15일
            28분

            와이드 풀다운
            30kg x 14회

            [자유 운동] 2026년 5월 17일
            35분

            벤치프레스
            60kg x 10회
            60kg x 8회
            """.trimIndent()
        ).getOrThrow()

        assertEquals(2, result.size)
        assertEquals("와이드 풀다운", result[0].exercises.single().name)
        assertEquals("벤치프레스", result[1].exercises.single().name)
        assertEquals(2, result[1].exercises.single().sets.size)
    }

    @Test
    fun skipsGymworkSummaryLinesAndParsesConnectedHeadersAndRepOnlySets() {
        val result = parser.parseMany(
            """
            [자유 운동] 2026년 4월 4일
            3분

            밴드 풀어파트
            15회
            15회

            펙 덱 플라이
            25kg x 12회
            30kg x 12회

            총 볼륨: 5,625kg
            칼로리: 230kcal

            #짐워크[자유 운동] 2026년 4월 15일
            47분

            덤벨 해머 컬
            6kg x 15회
            6kg x 14회

            총 볼륨: 5,145kg
            칼로리: 190kcal

            #짐워크
            """.trimIndent()
        ).getOrThrow()

        assertEquals(2, result.size)
        assertEquals("밴드 풀어파트", result[0].exercises[0].name)
        assertEquals(listOf(0.0, 0.0), result[0].exercises[0].sets.map { it.weightKg })
        assertEquals(listOf(15, 15), result[0].exercises[0].sets.map { it.reps })
        assertEquals("덤벨 해머 컬", result[1].exercises.single().name)
        assertEquals(2, result[1].exercises.single().sets.size)
    }
}
