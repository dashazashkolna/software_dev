fun generateRandomArray2(size: Int, maxValue: Int): IntArray? {
    if (size <= 0 || maxValue <= 0) {
        return null
    }

    return IntArray(size) {
        kotlin.random.Random.nextInt(0, maxValue + 1)
    }
}

fun main() {
    generateRandomArray2(size = 10, maxValue = 50)
        ?.let {
            it
        }
        ?.apply {
            indices.forEach { index ->
                if (this[index] % 2 != 0) {
                    this[index] *= 2
                } else {
                    this[index] /= 2
                }
            }
        }
        ?.also {
            println("Модифікований масив: ${it.contentToString()}")
        }
        ?.run {
            maxOrNull()
        }
        ?.let {
            println("Максимальне значення масиву: $it")
        }
}