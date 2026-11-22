package com.example.waterreminder

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WaterReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val workManager = WorkManager.getInstance(context)
    private val prefs = context.getSharedPreferences(
        WaterReminderWorker.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _sentCount = MutableStateFlow(
        prefs.getInt(WaterReminderWorker.KEY_SENT_COUNT, 0)
    )
    val sentCount: StateFlow<Int> = _sentCount.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        if (key == WaterReminderWorker.KEY_SENT_COUNT) {
            _sentCount.value = sharedPrefs.getInt(WaterReminderWorker.KEY_SENT_COUNT, 0)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        observeWorkState()
    }

    private fun observeWorkState() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(WaterReminderWorker.WORK_NAME)
                .collect { workInfos ->
                    val active = workInfos.any {
                        it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                    }
                    _isEnabled.value = active
                }
        }
    }

    fun startReminder(intervalMinutes: Long) {
        val safeInterval = if (intervalMinutes < 15L) 15L else intervalMinutes

        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            safeInterval,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WaterReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        _isEnabled.value = true
    }

    fun stopReminder() {
        workManager.cancelUniqueWork(WaterReminderWorker.WORK_NAME)
        _isEnabled.value = false
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        super.onCleared()
    }
}