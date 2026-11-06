package com.example.dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.random.Random

class DiceViewModel : ViewModel() {

    private val diceModel = DiceModel()

    private val _diceValues = MutableLiveData<List<Int>>(listOf(6, 6, 6, 6, 6))
    val diceValues: LiveData<List<Int>> get() = _diceValues

    private val _isRolling = MutableLiveData<Boolean>(false)
    val isRolling: LiveData<Boolean> get() = _isRolling

    private val _buttonText = MutableLiveData<String>("ROLL")
    val buttonText: LiveData<String> get() = _buttonText

    private var rollingJob: Job? = null
    private val currentValues = mutableListOf(6, 6, 6, 6, 6)

    fun onRollButtonClicked() {
        if (_isRolling.value == true) {
            rollingJob?.cancel()
        }

        rollingJob = viewModelScope.launch {
            _isRolling.value = true
            _buttonText.value = "RESTART"

            val deferredList = (0 until 5).map { index ->
                async {
                    val duration = Random.nextLong(1500, 3001)
                    diceModel.rollSingleDice(duration) { newValue ->
                        synchronized(currentValues) {
                            currentValues[index] = newValue
                            _diceValues.postValue(currentValues.toList())
                        }
                    }
                }
            }

            deferredList.awaitAll()

            _isRolling.value = false
            _buttonText.value = "ROLL"
        }
    }
}