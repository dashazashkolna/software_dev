package com.example.newsync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class NewSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString("user_token")
        Log.d("WorkManager", "Синхронізація почалася, токен: $token")

        delay(3000L)

        val outputData = Data.Builder()
            .putString("result", "Завантажено 15 новин")
            .build()

        return Result.success(outputData)
    }
}