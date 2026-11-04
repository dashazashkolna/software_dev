package com.example.simplecalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simplecalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var firstNumber: Double? = null
    private var operation: String? = null
    private var isNewInput = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btn0.setOnClickListener { appendToDisplay("0") }
        binding.btn1.setOnClickListener { appendToDisplay("1") }
        binding.btn2.setOnClickListener { appendToDisplay("2") }
        binding.btn3.setOnClickListener { appendToDisplay("3") }
        binding.btn4.setOnClickListener { appendToDisplay("4") }
        binding.btn5.setOnClickListener { appendToDisplay("5") }
        binding.btn6.setOnClickListener { appendToDisplay("6") }
        binding.btn7.setOnClickListener { appendToDisplay("7") }
        binding.btn8.setOnClickListener { appendToDisplay("8") }
        binding.btn9.setOnClickListener { appendToDisplay("9") }

        binding.btnDot.setOnClickListener {
            val current = binding.tvDisplay.text.toString()
            if (!current.contains(".")) appendToDisplay(".")
        }

        binding.btnPlus.setOnClickListener { setOperation("+") }
        binding.btnMinus.setOnClickListener { setOperation("-") }
        binding.btnMultiply.setOnClickListener { setOperation("*") }
        binding.btnDivide.setOnClickListener { setOperation("/") }

        binding.btnEquals.setOnClickListener {
            val second = binding.tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val first = firstNumber ?: return@setOnClickListener
            val op = operation ?: return@setOnClickListener

            val result = calculate(first, second, op)
            binding.tvDisplay.text = if (result == null) "Помилка" else format(result)

            firstNumber = null
            operation = null
            isNewInput = true
        }

        binding.btnClear.setOnClickListener { resetAll() }

        binding.btnBackspace.setOnClickListener {
            val current = binding.tvDisplay.text.toString()
            if (current.length <= 1) {
                binding.tvDisplay.text = "0"
                isNewInput = true
            } else {
                binding.tvDisplay.text = current.dropLast(1)
            }
        }
    }

    private fun appendToDisplay(symbol: String) {
        if (isNewInput) {
            binding.tvDisplay.text = symbol
            isNewInput = false
        } else {
            binding.tvDisplay.text = binding.tvDisplay.text.toString() + symbol
        }
    }

    private fun setOperation(op: String) {
        val current = binding.tvDisplay.text.toString().toDoubleOrNull() ?: return

        if (firstNumber != null && operation != null && !isNewInput) {
            val result = calculate(firstNumber!!, current, operation!!)
            if (result == null) {
                binding.tvDisplay.text = "Помилка"
                firstNumber = null
                operation = null
                isNewInput = true
                return
            }
            binding.tvDisplay.text = format(result)
            firstNumber = result
        } else {
            firstNumber = current
        }

        operation = op
        isNewInput = true
    }

    private fun calculate(a: Double, b: Double, op: String): Double? {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b == 0.0) null else a / b
            else -> null
        }
    }

    private fun resetAll() {
        binding.tvDisplay.text = "0"
        firstNumber = null
        operation = null
        isNewInput = true
    }

    private fun format(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString()
        else value.toString()
    }
}