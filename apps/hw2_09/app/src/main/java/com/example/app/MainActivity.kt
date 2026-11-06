package com.example.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var calculationJob: Job? = null
    private val TAG = "COROUTINES_TEST"

    private val ceh = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Глобальне перехоплення крашу: ${throwable.message}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Блок 1
        binding.btnStart.setOnClickListener {
            calculationJob = lifecycleScope.launch(Dispatchers.Default) {
                try {
                    for (i in 1..100) {
                        Thread.sleep(50)
                        ensureActive()
                        Log.d(TAG, "Обробка: $i%")
                    }
                    Log.d(TAG, "Роботу успішно завершено!")
                } catch (e: CancellationException) {
                    Log.d(TAG, "Обробку скасовано користувачем!")
                    throw e
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            calculationJob?.cancel()
        }

        // Блок 3
        binding.btnTestCrash.setOnClickListener {
            lifecycleScope.launch {
                supervisorScope {
                    launch {
                        try {
                            delay(1000)
                            throw RuntimeException("Банер не завантажився!")
                        } catch (e: Exception) {
                            Log.e(TAG, "Помилка банера: ${e.message}")
                        }
                    }

                    launch {
                        delay(2000)
                        Log.d(TAG, "Стрічка новин завантажена")
                    }
                }
            }
        }

        // Блок 4
        binding.btnGlobalCrash.setOnClickListener {
            val safeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + ceh)
            safeScope.launch {
                delay(500)
                throw IllegalArgumentException("Ой, сталася помилка!")
            }
        }
    }
}