fun main() {
    val text = readln()

    val characterCount = mutableMapOf<Char, Int>()

    text.forEach { char ->
        characterCount[char] = characterCount.getOrDefault(char, 0) + 1
    }

    println("Кількість входжень кожного символу:")

    characterCount.forEach { (char, count) ->
        println("'$char' : $count")
    }
}