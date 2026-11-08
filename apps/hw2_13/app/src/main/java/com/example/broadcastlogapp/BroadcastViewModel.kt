package com.example.broadcastlogapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BroadcastViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BroadcastRepository(application)

    private val _events = MutableStateFlow<List<BroadcastEvent>>(emptyList())
    val events: StateFlow<List<BroadcastEvent>> = _events.asStateFlow()

    init {
        startListening()
    }

    private fun startListening() {
        viewModelScope.launch {
            repository.getSystemEvents().collect { event ->
                val currentList = _events.value.toMutableList()
                currentList.add(0, event)
                _events.value = currentList
            }
        }
    }

    fun deleteEvent(event: BroadcastEvent) {
        _events.value = _events.value.filter { it.id != event.id }
    }

    fun clearAll() {
        _events.value = emptyList()
    }
}