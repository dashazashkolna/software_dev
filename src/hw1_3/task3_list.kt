fun main() {
    val numbers = Array(100) {
        (-100..100).random()
    }

    println("Початкова колекція:")
    println(numbers.contentToString())

    // Залишаємо лише додатні числа
    val positiveNumbers = numbers.filter { it > 0 }

    // Залишаємо числа, які діляться на 5 без остачі
    val divisibleByFive = positiveNumbers.filter { it % 5 == 0 }

    // Підносимо кожне число до квадрату
    val squaredNumbers = divisibleByFive.map { it * it }

    // Сортуємо за спаданням
    val sortedNumbers = squaredNumbers.sortedDescending()

    println("Відсортована колекція:")
    println(sortedNumbers)

    // Виводимо найбільше та найменше число
    if (sortedNumbers.isNotEmpty()) {
        println("Найбільше число: ${sortedNumbers.first()}")
        println("Найменше число: ${sortedNumbers.last()}")
    } else {
        println("Колекція порожня")
    }

    // Перетворюємо колекцію в колекцію рядків
    val stringNumbers = sortedNumbers.map { it.toString() }

    // З'єднуємо в один рядок через пробіл
    val result = stringNumbers.joinToString(" ")

    println(result)
}