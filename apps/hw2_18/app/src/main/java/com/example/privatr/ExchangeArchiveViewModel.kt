package com.example.privatr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface SyncStatusUi {
    data object Idle : SyncStatusUi
    data object EnqueuedOrRunning : SyncStatusUi
    data class Succeeded(val message: String) : SyncStatusUi
    data class Failed(val message: String) : SyncStatusUi
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val dao = ExchangeDatabase.getInstance(appContext).exchangeRateDao()
    private val workManager = WorkManager.getInstance(appContext)

    private val _selectedDate = MutableStateFlow(defaultTodayFormatted())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val rates: StateFlow<List<ExchangeRateEntity>> = _selectedDate
        .flatMapLatest { date ->
            val trimmed = date.trim()
            if (trimmed.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) {
                dao.observeRates(trimmed)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val savedDates: StateFlow<List<String>> = dao.observeSavedDates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val syncStatus: StateFlow<SyncStatusUi> = _selectedDate
        .flatMapLatest { date ->
            val trimmed = date.trim()
            if (!trimmed.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) {
                flowOf(SyncStatusUi.Idle)
            } else {
                workManager.getWorkInfosForUniqueWorkFlow(
                    ExchangeRatesSyncWorker.oneTimeWorkName(trimmed)
                ).mapToSyncStatus()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SyncStatusUi.Idle
        )

    fun onDateChanged(newDate: String) {
        _selectedDate.value = newDate
    }

    fun triggerRefreshForSelectedDate() {
        val date = _selectedDate.value.trim()
        if (!date.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ExchangeRatesSyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(ExchangeRatesSyncWorker.KEY_DATE to date))
            .build()

        workManager.enqueueUniqueWork(
            ExchangeRatesSyncWorker.oneTimeWorkName(date),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private fun kotlinx.coroutines.flow.Flow<List<WorkInfo>>.mapToSyncStatus(): kotlinx.coroutines.flow.Flow<SyncStatusUi> {
        return kotlinx.coroutines.flow.flow {
            collect { infos ->
                val latest = infos.lastOrNull()
                if (latest == null) {
                    emit(SyncStatusUi.Idle)
                    return@collect
                }
                when (latest.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.BLOCKED -> emit(SyncStatusUi.EnqueuedOrRunning)

                    WorkInfo.State.SUCCEEDED -> {
                        val msg = latest.outputData.getString(ExchangeRatesSyncWorker.KEY_MESSAGE)
                            ?: "Оновлено"
                        emit(SyncStatusUi.Succeeded(msg))
                    }

                    WorkInfo.State.FAILED -> {
                        val msg = latest.outputData.getString(ExchangeRatesSyncWorker.KEY_MESSAGE)
                            ?: "Помилка оновлення"
                        emit(SyncStatusUi.Failed(msg))
                    }

                    WorkInfo.State.CANCELLED -> emit(SyncStatusUi.Idle)
                }
            }
        }
    }

    private fun defaultTodayFormatted(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        return formatter.format(Date())
    }
}