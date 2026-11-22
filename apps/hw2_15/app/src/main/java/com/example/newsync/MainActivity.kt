package com.example.newsync

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val _taskStatus = MutableStateFlow("Очікує...")
    private val taskStatus: StateFlow<String> = _taskStatus.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NewSyncScreen(
                        statusFlow = taskStatus,
                        onSyncNow = { context -> startOneTimeSync(context) },
                        onEnableNightSync = { context -> startPeriodicNightSync(context) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun startOneTimeSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString("user_token", "abc123")
            .build()

        val request = OneTimeWorkRequestBuilder<NewSyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request)

        workManager.getWorkInfoByIdLiveData(request.id).observe(this) { workInfo ->
            if (workInfo != null) {
                _taskStatus.value = when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> "Очікує..."
                    WorkInfo.State.RUNNING -> "Виконується..."
                    WorkInfo.State.SUCCEEDED -> {
                        val message = workInfo.outputData.getString("result") ?: "Синхронізовано"
                        "Успіх: $message"
                    }
                    WorkInfo.State.FAILED -> "Помилка синхронізації"
                    WorkInfo.State.CANCELLED -> "Скасовано"
                    else -> workInfo.state.name
                }
            }
        }
    }

    private fun startPeriodicNightSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val inputData = Data.Builder()
            .putString("user_token", "night_token")
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<NewSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "night_news_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}

@Composable
fun NewSyncScreen(
    statusFlow: StateFlow<String>,
    onSyncNow: (Context) -> Unit,
    onEnableNightSync: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by statusFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Синхронізація новин",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = status,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSyncNow(context) },
            modifier = Modifier.width(240.dp)
        ) {
            Text(text = "Синхронізувати зараз")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEnableNightSync(context) },
            modifier = Modifier.width(240.dp)
        ) {
            Text(text = "Увімкнути нічну синхронізацію")
        }
    }
}