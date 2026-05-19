package com.gymlog.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutToastMessagesTest {
    @Test
    fun copyWorkoutStartedMessageMatchesProductText() {
        assertEquals("기록으로 새 운동을 시작했습니다.", WorkoutToastMessages.copyWorkoutStarted)
    }

    @Test
    fun emptyWorkoutNotSavedMessageMatchesProductText() {
        assertEquals("완료한 세트가 없어서 기록되지 않았습니다.", WorkoutToastMessages.emptyWorkoutNotSaved)
    }
}
