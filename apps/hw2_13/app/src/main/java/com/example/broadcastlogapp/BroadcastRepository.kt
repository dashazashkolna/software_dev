package com.example.broadcastlogapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BroadcastRepository(private val context: Context) {

    fun getSystemEvents(): Flow<BroadcastEvent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                val action = intent.action ?: "UNKNOWN"

                val humanReadableName = when (action) {
                    Intent.ACTION_POWER_CONNECTED -> "Зарядний пристрій підключено"
                    Intent.ACTION_POWER_DISCONNECTED -> "Зарядний пристрій відключено"
                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> "Зміна режиму 'У літаку'"
                    Intent.ACTION_BATTERY_LOW -> "Критично низький заряд батареї"
                    else -> "Системна подія: $action"
                }

                val extras = intent.extras?.let { bundle ->
                    bundle.keySet().joinToString(", ") { key ->
                        "$key = ${bundle.get(key)}"
                    }
                } ?: "Без додаткових параметрів"

                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                val event = BroadcastEvent(
                    id = UUID.randomUUID().toString(),
                    type = humanReadableName,
                    timestamp = currentTime,
                    details = "Дія: $action\nПараметри: $extras"
                )
                trySend(event)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}