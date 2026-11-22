package com.example.privatr

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExchangeRatesRoomScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ExchangeRatesRoomScreen(
    modifier: Modifier = Modifier,
    viewModel: ExchangeArchiveViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val rates by viewModel.rates.collectAsStateWithLifecycle()
    val savedDates by viewModel.savedDates.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun openDatePicker() {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        val cal = Calendar.getInstance()
        val parsed = runCatching { formatter.parse(selectedDate) }.getOrNull()
        if (parsed != null) {
            cal.time = parsed
        }

        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.onDateChanged(formatter.format(picked.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        val minCal = Calendar.getInstance().apply { add(Calendar.YEAR, -4) }
        dialog.datePicker.minDate = minCal.timeInMillis
        dialog.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ПриватБанк — валюти (Room)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { viewModel.onDateChanged(it) },
                singleLine = true,
                label = { Text("Дата (dd.MM.yyyy)") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(onClick = { openDatePicker() }) {
                Text("Календар")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.triggerRefreshForSelectedDate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Оновити")
        }

        if (savedDates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(savedDates, key = { it }) { dateItem ->
                    FilterChip(
                        selected = dateItem == selectedDate,
                        onClick = { viewModel.onDateChanged(dateItem) },
                        label = { Text(dateItem) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SyncStatusBanner(syncStatus = syncStatus)

        Spacer(modifier = Modifier.height(10.dp))

        if (rates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Курси за цю дату ще не завантажені",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Text(
                text = "Збережені курси на дату: $selectedDate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = rates,
                    key = { "${it.date}_${it.currency}" }
                ) { item ->
                    RateEntityCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun SyncStatusBanner(syncStatus: SyncStatusUi) {
    when (syncStatus) {
        is SyncStatusUi.Idle -> Unit
        is SyncStatusUi.EnqueuedOrRunning -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Очікує мережі / оновлюється...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        is SyncStatusUi.Succeeded -> {
            Text(
                text = syncStatus.message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        is SyncStatusUi.Failed -> {
            Text(
                text = syncStatus.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RateEntityCard(item: ExchangeRateEntity) {
    val buyText = item.purchaseRate?.let { "%.2f ${item.baseCurrency}".format(Locale.US, it) } ?: "—"
    val sellText = item.saleRate?.let { "%.2f ${item.baseCurrency}".format(Locale.US, it) } ?: "—"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.currency,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Базова: ${item.baseCurrency}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Банк купує: $buyText",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Банк продає: $sellText",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}