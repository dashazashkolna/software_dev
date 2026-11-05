package com.example.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    private val timerModel = TimerModel()

    private val _timerLiveData = MutableLiveData<Long>(30)
    val timerLiveData: LiveData<Long> get() = _timerLiveData

    private val _isTimerRunning = MutableLiveData<Boolean>(false)
    val isTimerRunning: LiveData<Boolean> get() = _isTimerRunning

    fun startTimer(durationInSeconds: Long = 30) {
        _isTimerRunning.value = true
        timerModel.start(
            durationInSeconds = durationInSeconds,
            onTick = { secondsLeft ->
                _timerLiveData.value = secondsLeft
            },
            onFinish = {
                _timerLiveData.value = 0
                _isTimerRunning.value = false
            }
        )
    }

    fun resetTimer(defaultSeconds: Long = 30) {
        timerModel.stop()
        _timerLiveData.value = defaultSeconds
        _isTimerRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        timerModel.stop()
    }
}