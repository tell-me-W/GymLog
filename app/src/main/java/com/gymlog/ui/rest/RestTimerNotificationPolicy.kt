package com.gymlog.ui.rest

enum class RestTimerEvent {
    Started,
    Tick,
    Finished,
}

object RestTimerNotificationPolicy {
    fun shouldNotify(event: RestTimerEvent): Boolean {
        return event == RestTimerEvent.Finished
    }
}
