package com.example.mvvm

import android.os.CountDownTimer

class TimerModel {
    private var countDownTimer: CountDownTimer? = null

    fun start(durationInSeconds: Long, onTick: (Long) -> Unit, onFinish: () -> Unit) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(durationInSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTick(millisUntilFinished / 1000)
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    fun stop() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
}