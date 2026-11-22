package com.example.privatr

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExchangeRatesSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val rawDate = inputData.getString(KEY_DATE)
        val targetDate = if (rawDate.isNullOrBlank()) {
            todayFormatted()
        } else {
            val normalized = validateAndNormalizeDate(rawDate)
                ?: return Result.failure(
                    workDataOf(KEY_MESSAGE to "Некоректний формат дати: $rawDate")
                )
            normalized
        }

        val response = try {
            PrivatNetwork.api.getExchangeRates(targetDate)
        } catch (_: IOException) {
            return Result.retry()
        } catch (_: Exception) {
            return Result.retry()
        }

        if (!response.isSuccessful) {
            return if (response.code() in 500..599 || response.code() == 429) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(KEY_MESSAGE to "Помилка відповіді сервера (${response.code()})")
                )
            }
        }

        val body = response.body()
            ?: return Result.failure(
                workDataOf(KEY_MESSAGE to "Порожнє тіло відповіді від сервера")
            )

        val responseDate = body.date?.trim()
        if (responseDate != null && responseDate != targetDate) {
            return Result.failure(
                workDataOf(KEY_MESSAGE to "Дата відповіді ($responseDate) не збігається з запитаною ($targetDate)")
            )
        }

        val entities = body.exchangeRate
            .filter { !it.currency.isNullOrBlank() }
            .map { item ->
                ExchangeRateEntity(
                    date = responseDate ?: targetDate,
                    currency = item.currency!!.trim(),
                    baseCurrency = item.baseCurrency?.trim().orEmpty().ifBlank { "UAH" },
                    purchaseRate = item.purchaseRate,
                    saleRate = item.saleRate
                )
            }

        if (entities.isEmpty()) {
            return Result.success(
                workDataOf(KEY_MESSAGE to "Сервер не повернув курсів")
            )
        }

        val dao = ExchangeDatabase.getInstance(applicationContext).exchangeRateDao()
        dao.upsertAll(entities)

        return Result.success(
            workDataOf(KEY_MESSAGE to "Оновлено (${entities.size} валют)")
        )
    }

    private fun validateAndNormalizeDate(dateStr: String): String? {
        val trimmed = dateStr.trim()
        if (!trimmed.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) return null
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US).apply {
            isLenient = false
        }
        return try {
            val parsed = formatter.parse(trimmed) ?: return null
            formatter.format(parsed)
        } catch (_: ParseException) {
            null
        }
    }

    private fun todayFormatted(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        return formatter.format(Date())
    }

    companion object {
        const val KEY_DATE = "date"
        const val KEY_MESSAGE = "message"
        const val PERIODIC_WORK_NAME = "exchange_rates_periodic"

        fun oneTimeWorkName(date: String): String = "exchange_rates_$date"
    }
}