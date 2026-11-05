package com.example.myapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import androidx.lifecycle.ProcessLifecycleOwner

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(BackgroundDetector())

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                val state = (activity as? LifecycleOwner)?.lifecycle?.currentState
                Timber.d("onActivityCreated: ${activity.javaClass.simpleName}, стан: $state")
            }

            override fun onActivityStarted(activity: Activity) {
                val state = (activity as? LifecycleOwner)?.lifecycle?.currentState
                Timber.d("onActivityStarted: ${activity.javaClass.simpleName}, стан: $state")
            }

            override fun onActivityResumed(activity: Activity) {
                val state = (activity as? LifecycleOwner)?.lifecycle?.currentState
                Timber.d("onActivityResumed: ${activity.javaClass.simpleName}, стан: $state")
            }

            override fun onActivityPaused(activity: Activity) {
                val state = (activity as? LifecycleOwner)?.lifecycle?.currentState
                Timber.d("onActivityPaused: ${activity.javaClass.simpleName}, стан: $state")
            }

            override fun onActivityStopped(activity: Activity) {
                val state = (activity as? LifecycleOwner)?.lifecycle?.currentState
                Timber.d("onActivityStopped: ${activity.javaClass.simpleName}, стан: $state")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                val state = (activity as? LifecycleOwner)?.lifecycle?.currentState
                Timber.d("onActivityDestroyed: ${activity.javaClass.simpleName}, стан: $state")
            }
        })
    }
}