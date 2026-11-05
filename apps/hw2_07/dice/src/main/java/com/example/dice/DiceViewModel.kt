package com.example.dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.concurrent.thread
import kotlin.random.Random

class DiceViewModel : ViewModel() {

    private val _diceValues = MutableLiveData<List<Int>>(listOf(6, 6, 6, 6, 6))
    val diceValues: LiveData<List<Int>> get() = _diceValues

    private val _isRolling = MutableLiveData<Boolean>(false)
    val isRolling: LiveData<Boolean> get() = _isRolling

    private val currentValues = mutableListOf(6, 6, 6, 6, 6)
    private var activeThreadsCount = 0

    fun rollDice() {
        if (_isRolling.value == true) return
        _isRolling.value = true
        activeThreadsCount = 5

        for (i in 0 until 5) {
            thread {
                val iterations = 15 + i * 5 // різна тривалість для кожної кістки
                for (j in 0 until iterations) {
                    Thread.sleep(100)
                    val randomValue = Random.nextInt(1, 7)
                    synchronized(currentValues) {
                        currentValues[i] = randomValue
                        _diceValues.postValue(currentValues.toList())
                    }
                }

                synchronized(this) {
                    activeThreadsCount--
                    if (activeThreadsCount == 0) {
                        _isRolling.postValue(false)
                    }
                }
            }
        }
    }
}