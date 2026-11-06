package com.example.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class DownloadViewModel : ViewModel() {

    private val fileRepository = FileRepository()
    private var downloadJob: Job? = null

    private val _progressState = MutableStateFlow(0)
    val progressState: StateFlow<Int> = _progressState.asStateFlow()

    private val _statusText = MutableStateFlow("Очікування завантаження")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun startDownload() {
        if (_isDownloading.value) return

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _isDownloading.value = true
            _statusText.value = "Завантаження: 0%"

            fileRepository.downloadFile()
                .onCompletion { cause ->
                    if (cause == null) {
                        _statusText.value = "Файл успішно скачано!"
                    } else {
                        _statusText.value = "Завантаження скасовано"
                    }
                    _isDownloading.value = false
                }
                .collect { progress ->
                    _progressState.value = progress
                    _statusText.value = "Завантаження: $progress%"
                }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
    }
}