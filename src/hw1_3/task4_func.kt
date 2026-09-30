fun countStrings(
    strings: Collection<String>,
    predicate: (String) -> Boolean
): Int {
    return strings.count { predicate(it) }
}

fun main() {
    val strings = listOf(
        "Hello",
        "blablabla1",
        "kotlin",
        "12345",
        "World"
    )

    val result = countStrings(strings) {
        it.any { char -> char.isDigit() }
    }

    println("Кількість рядків з цифрами: $result")
}