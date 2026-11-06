package com.example.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDownload.setOnClickListener {
            viewModel.startDownload()
        }

        binding.btnCancel.setOnClickListener {
            viewModel.cancelDownload()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.progressState.collect { progress ->
                        binding.progressBar.progress = progress
                    }
                }
                launch {
                    viewModel.statusText.collect { status ->
                        binding.tvStatus.text = status
                    }
                }
                launch {
                    viewModel.isDownloading.collect { isDownloading ->
                        binding.btnDownload.isEnabled = !isDownloading
                    }
                }
            }
        }
    }
}