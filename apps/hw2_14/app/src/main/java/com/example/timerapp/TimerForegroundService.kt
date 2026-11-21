package com.example.timerapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "timer_channel_id"
        const val CHANNEL_NAME = "Таймер-секундомір"
        const val NOTIFICATION_ID = 1
        const val TAG = "Timer"
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    private val binder = LocalBinder()

    private val _timerSeconds = MutableStateFlow(0)

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    fun getTimerFlow(): StateFlow<Int> = _timerSeconds.asStateFlow()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (timerJob?.isActive != true) {
            startTimerLoop()
        }

        return START_STICKY
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            _timerSeconds.value = 0
            while (isActive) {
                Log.d(TAG, "Минуло ${_timerSeconds.value} секунд")
                delay(1000L)
                _timerSeconds.value += 1
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Сповіщення активного фонового таймера"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Таймер працює")
            .setContentText("Відлік часу у фоновому режимі")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerSeconds.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Таймер зупинено")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        _timerSeconds.value = 0
        Log.d(TAG, "Службу зупинено, таймер скасовано")
        super.onDestroy()
    }
}