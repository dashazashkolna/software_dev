package com.example.mvvm

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.timerLiveData.observe(this) { seconds ->
            binding.tvTimer.text = seconds.toString()
        }

        viewModel.isTimerRunning.observe(this) { isRunning ->
            binding.btnStart.isEnabled = !isRunning
        }

        binding.btnStart.setOnClickListener {
            val input = binding.etSeconds.text.toString().toLongOrNull() ?: 30
            viewModel.startTimer(input)
        }

        binding.btnReset.setOnClickListener {
            val input = binding.etSeconds.text.toString().toLongOrNull() ?: 30
            viewModel.resetTimer(input)
        }
    }
}