package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycle.addObserver(AudioPlayer())
        lifecycle.addObserver(AnalyticsTracker())

        UserTracker.register(this)
        Timber.d("Зареєстровано слухачів у UserTracker: ${UserTracker.getListenersCount()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        UserTracker.unregister(this)
    }
}