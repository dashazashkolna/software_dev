package com.example.simplecalculator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etFirst = findViewById<EditText>(R.id.etFirst)
        val etSecond = findViewById<EditText>(R.id.etSecond)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnPlus = findViewById<Button>(R.id.btnPlus)
        val btnMinus = findViewById<Button>(R.id.btnMinus)
        val btnMultiply = findViewById<Button>(R.id.btnMultiply)
        val btnDivide = findViewById<Button>(R.id.btnDivide)
        val btnClear = findViewById<Button>(R.id.btnClear)

        val tag = "Calculator"

        // повертає null, якщо щось не так
        fun readNumbers(): Pair<Double, Double>? {
            val a = etFirst.text.toString()
            val b = etSecond.text.toString()

            if (a.isEmpty() || b.isEmpty()) {
                Log.w(tag, "Порожні поля")
                tvResult.text = "Будь ласка, введіть обидва числа"
                return null
            }
            return Pair(a.toDouble(), b.toDouble())
        }

        fun format(value: Double): String {
            return if (value % 1.0 == 0.0) value.toLong().toString()
            else value.toString()
        }

        btnPlus.setOnClickListener {
            val nums = readNumbers() ?: return@setOnClickListener
            Log.d(tag, "Додавання: ${nums.first} + ${nums.second}")
            tvResult.text = "Результат: ${format(nums.first + nums.second)}"
        }

        btnMinus.setOnClickListener {
            val nums = readNumbers() ?: return@setOnClickListener
            Log.d(tag, "Віднімання: ${nums.first} - ${nums.second}")
            tvResult.text = "Результат: ${format(nums.first - nums.second)}"
        }

        btnMultiply.setOnClickListener {
            val nums = readNumbers() ?: return@setOnClickListener
            Log.d(tag, "Множення: ${nums.first} * ${nums.second}")
            tvResult.text = "Результат: ${format(nums.first * nums.second)}"
        }

        btnDivide.setOnClickListener {
            val nums = readNumbers() ?: return@setOnClickListener
            if (nums.second == 0.0) {
                Log.w(tag, "Ділення на нуль: ${nums.first} / ${nums.second}")
                tvResult.text = "Помилка: ділення на нуль неможливе!"
                return@setOnClickListener
            }
            Log.d(tag, "Ділення: ${nums.first} / ${nums.second}")
            tvResult.text = "Результат: ${format(nums.first / nums.second)}"
        }

        btnClear.setOnClickListener {
            Log.d(tag, "Очищення")
            etFirst.text.clear()
            etSecond.text.clear()
            tvResult.text = "Результат: "
        }
    }
}