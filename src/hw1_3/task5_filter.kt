fun stringFilter(
    strings: Collection<String>,
    predicate: (String) -> Boolean
): Collection<String> {
    return strings.filter { predicate(it) }
}

fun main() {
    val strings = listOf(
        "Hello",
        "blablabla1",
        "kotlin",
        "12345",
        "125",
        "World"
    )

    val result = stringFilter(strings) {
        it.any { char -> char.isDigit() }
    }

    println("Рядки, що містять цифри:")
    println(result)
}