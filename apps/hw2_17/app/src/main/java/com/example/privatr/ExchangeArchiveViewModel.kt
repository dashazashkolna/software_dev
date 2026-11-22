package com.example.privatr

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface RatesUiState {
    data object Idle : RatesUiState
    data object Loading : RatesUiState
    data object Empty : RatesUiState
    data class Success(
        val responseDate: String,
        val rates: List<ExchangeRateItem>
    ) : RatesUiState
    data class Error(val message: String) : RatesUiState
}

class ExchangeArchiveViewModel : ViewModel() {

    private val _selectedDate = MutableStateFlow(defaultTodayFormatted())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow<RatesUiState>(RatesUiState.Idle)
    val uiState: StateFlow<RatesUiState> = _uiState.asStateFlow()

    private var activeCall: Call<ExchangeArchiveResponse>? = null

    fun onDateChanged(newDate: String) {
        _selectedDate.value = newDate
    }

    fun loadRates() {
        val dateStr = _selectedDate.value.trim()
        val validationError = validateDate(dateStr)
        if (validationError != null) {
            _uiState.value = RatesUiState.Error(validationError)
            return
        }

        activeCall?.cancel()
        _uiState.value = RatesUiState.Loading
        Log.d("PrivatRates", "Запит курсів на дату: $dateStr")

        val call = PrivatNetwork.api.getExchangeRates(dateStr)
        activeCall = call

        call.enqueue(object : Callback<ExchangeArchiveResponse> {
            override fun onResponse(
                call: Call<ExchangeArchiveResponse>,
                response: Response<ExchangeArchiveResponse>
            ) {
                if (call.isCanceled) return
                activeCall = null

                if (!response.isSuccessful) {
                    _uiState.value = RatesUiState.Error(
                        "Помилка сервера (${response.code()}). Спробуйте пізніше."
                    )
                    return
                }

                val body = response.body()
                if (body == null) {
                    _uiState.value = RatesUiState.Error("Порожня відповідь від сервера.")
                    return
                }

                val validRates = body.exchangeRate.filter { item ->
                    !item.currency.isNullOrBlank()
                }

                if (validRates.isEmpty()) {
                    _uiState.value = RatesUiState.Empty
                } else {
                    _uiState.value = RatesUiState.Success(
                        responseDate = body.date ?: dateStr,
                        rates = validRates
                    )
                }
            }

            override fun onFailure(call: Call<ExchangeArchiveResponse>, t: Throwable) {
                if (call.isCanceled) return
                activeCall = null
                _uiState.value = RatesUiState.Error(
                    "Не вдалося завантажити курси. Перевірте з'єднання та спробуйте знову."
                )
            }
        })
    }

    override fun onCleared() {
        activeCall?.cancel()
        activeCall = null
        super.onCleared()
    }

    private fun validateDate(dateStr: String): String? {
        if (!dateStr.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) {
            return "Введіть дату у форматі dd.MM.yyyy (наприклад, 01.11.2026)."
        }

        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US).apply {
            isLenient = false
        }

        val parsedDate = try {
            formatter.parse(dateStr)
        } catch (_: ParseException) {
            null
        } ?: return "Такої дати не існує. Перевірте день, місяць і рік."

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        if (parsedDate.after(todayCal.time)) {
            return "Не можна запитувати майбутню дату."
        }

        val minCal = Calendar.getInstance().apply {
            add(Calendar.YEAR, -4)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (parsedDate.before(minCal.time)) {
            return "Архів доступний лише за останні 4 роки."
        }

        return null
    }

    private fun defaultTodayFormatted(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        return formatter.format(Calendar.getInstance().time)
    }
}