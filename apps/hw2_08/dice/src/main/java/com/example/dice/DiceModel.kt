package com.example.dice

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random

class DiceModel {

    suspend fun rollSingleDice(durationMillis: Long, onValueChanged: (Int) -> Unit): Int {
        val startTime = System.currentTimeMillis()
        var finalValue = 6
        while (System.currentTimeMillis() - startTime < durationMillis) {
            delay(100)
            finalValue = Random.nextInt(1, 7)
            onValueChanged(finalValue) // одразу повідомляємо нове значення
        }
        return finalValue
    }

    suspend fun rollAllDice(): List<Int> = coroutineScope {
        val deferredDice = (0 until 5).map { index ->
            val duration = Random.nextLong(1500, 3001)
            async {
                val startTime = System.currentTimeMillis()
                var value = 6
                while (System.currentTimeMillis() - startTime < duration) {
                    delay(100)
                    value = Random.nextInt(1, 7)
                }
                value
            }
        }
        deferredDice.awaitAll()
    }
}