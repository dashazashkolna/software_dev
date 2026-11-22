package com.example.privatr

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "exchange_rates",
    primaryKeys = ["date", "currency"]
)
data class ExchangeRateEntity(
    val date: String,
    val currency: String,
    val baseCurrency: String,
    val purchaseRate: Double?,
    val saleRate: Double?
)

@Dao
interface ExchangeRateDao {
    @Upsert
    suspend fun upsertAll(rates: List<ExchangeRateEntity>)

    @Query("SELECT * FROM exchange_rates WHERE date = :date ORDER BY currency ASC")
    fun observeRates(date: String): Flow<List<ExchangeRateEntity>>

    @Query("SELECT DISTINCT date FROM exchange_rates ORDER BY date DESC")
    fun observeSavedDates(): Flow<List<String>>
}

@Database(
    entities = [ExchangeRateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExchangeDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        @Volatile
        private var INSTANCE: ExchangeDatabase? = null

        fun getInstance(context: Context): ExchangeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExchangeDatabase::class.java,
                    "exchange_rates.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}