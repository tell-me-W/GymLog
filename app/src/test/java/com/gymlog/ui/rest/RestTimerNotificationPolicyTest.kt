package com.gymlog.ui.rest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerNotificationPolicyTest {
    @Test
    fun onlyFinishedEventNotifiesUser() {
        assertFalse(RestTimerNotificationPolicy.shouldNotify(RestTimerEvent.Started))
        assertFalse(RestTimerNotificationPolicy.shouldNotify(RestTimerEvent.Tick))
        assertTrue(RestTimerNotificationPolicy.shouldNotify(RestTimerEvent.Finished))
    }

    @Test
    fun onlyFinishedNotificationOpensAppWhenTapped() {
        assertFalse(RestTimerNotificationPolicy.shouldOpenAppWhenTapped(RestTimerEvent.Started))
        assertFalse(RestTimerNotificationPolicy.shouldOpenAppWhenTapped(RestTimerEvent.Tick))
        assertTrue(RestTimerNotificationPolicy.shouldOpenAppWhenTapped(RestTimerEvent.Finished))
    }
}
