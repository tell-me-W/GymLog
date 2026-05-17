package com.gymlog.ui.rest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import com.gymlog.R

class RestTimerManager(
    private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var timer: CountDownTimer? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "휴식 타이머",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }
    }

    fun start(seconds: Int) {
        cancel()
        if (seconds <= 0) return
        notifyIfNeeded(RestTimerEvent.Started, seconds)
        timer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                notifyIfNeeded(RestTimerEvent.Tick, (millisUntilFinished / 1000L).toInt())
            }

            override fun onFinish() {
                notifyIfNeeded(RestTimerEvent.Finished, 0)
            }
        }.start()
    }

    fun cancel() {
        timer?.cancel()
        timer = null
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun notifyIfNeeded(event: RestTimerEvent, secondsLeft: Int) {
        if (!RestTimerNotificationPolicy.shouldNotify(event)) return
        when (event) {
            RestTimerEvent.Started,
            RestTimerEvent.Tick -> showRunning(secondsLeft)
            RestTimerEvent.Finished -> showFinished()
        }
    }

    private fun showRunning(secondsLeft: Int) {
        notificationManager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("휴식 중")
                .setContentText("${secondsLeft}초 남았습니다")
                .setOngoing(true)
                .build()
        )
    }

    private fun showFinished() {
        notificationManager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("휴식 완료")
                .setContentText("다음 세트를 시작할 시간입니다")
                .setOngoing(false)
                .build()
        )
    }

    private companion object {
        const val CHANNEL_ID = "rest_timer"
        const val NOTIFICATION_ID = 1001
    }
}
