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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
                    ExchangeRatesScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ExchangeRatesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExchangeArchiveViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isLoading = uiState is RatesUiState.Loading

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
            text = "ПриватБанк — валюти",
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
                enabled = !isLoading,
                singleLine = true,
                label = { Text("Дата (dd.MM.yyyy)") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { openDatePicker() },
                enabled = !isLoading
            ) {
                Text("Календар")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.loadRates() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Показати курси")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is RatesUiState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Оберіть дату та натисніть «Показати курси».",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is RatesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RatesUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "За обрану дату курси валют відсутні.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is RatesUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadRates() }) {
                        Text("Повторити")
                    }
                }
            }

            is RatesUiState.Success -> {
                Text(
                    text = "Курси на дату: ${state.responseDate}",
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
                        items = state.rates,
                        key = { item -> "${item.currency}_${item.baseCurrency}" }
                    ) { item ->
                        RateCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun RateCard(item: ExchangeRateItem) {
    val base = item.baseCurrency ?: "UAH"
    val buyText = item.purchaseRate?.let { "%.2f $base".format(Locale.US, it) } ?: "—"
    val sellText = item.saleRate?.let { "%.2f $base".format(Locale.US, it) } ?: "—"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.currency ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Базова: $base",
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