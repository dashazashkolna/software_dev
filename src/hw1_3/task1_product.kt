fun main() {
    val a = readln().toInt()
    val b = readln().toInt()

    var result = 0

    repeat(b) {
        result += a
    }

    println("Добуток: $result")
}