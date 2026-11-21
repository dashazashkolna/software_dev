package com.example.timerapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val _timerFlow = MutableStateFlow(0)
    private val timerFlow: StateFlow<Int> = _timerFlow.asStateFlow()

    private var isBound = false
    private var collectJob: Job? = null
    private var boundService: TimerForegroundService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerForegroundService.LocalBinder
            val myService = binder.getService()
            boundService = myService
            isBound = true

            collectJob?.cancel()
            collectJob = lifecycleScope.launch {
                myService.getTimerFlow().collect { seconds ->
                    _timerFlow.value = seconds
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            collectJob?.cancel()
            boundService = null
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerForegroundService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        collectJob?.cancel()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        boundService = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerScreen(
                        secondsFlow = timerFlow,
                        onStartTimer = {
                            val intent = Intent(this, TimerForegroundService::class.java)
                            ContextCompat.startForegroundService(this, intent)
                        },
                        onStopTimer = {
                            boundService?.stopTimer()
                            val intent = Intent(this, TimerForegroundService::class.java)
                            stopService(intent)
                            _timerFlow.value = 0
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerScreen(
    secondsFlow: StateFlow<Int>,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val seconds by secondsFlow.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Фоновий таймер",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$seconds с",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                onStartTimer()
            },
            modifier = Modifier.width(200.dp)
        ) {
            Text(text = "Старт Таймер")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onStopTimer()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.width(200.dp)
        ) {
            Text(text = "Стоп Таймер")
        }
    }
}