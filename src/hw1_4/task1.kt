import kotlin.math.sqrt
import kotlin.random.Random

class QuadraticEquation(a: Double, var b: Double, var c: Double) {

    var a: Double = a
        set(value) {
            require(value != 0.0) {
                "Коефіцієнт 'a' не може бути нулем!"
            }
            field = value
        }

    init {
        require(a != 0.0) {
            "Коефіцієнт 'a' не може бути нулем!"
        }
    }

    constructor(a: Int, b: Int, c: Int) :
            this(a.toDouble(), b.toDouble(), c.toDouble())

    constructor(b: Double, c: Double) :
            this(1.0, b, c)

    val discriminant: Double
        get() = b * b - 4 * a * c

    fun solve(): List<Double> {
        return when {
            discriminant > 0 -> {
                val x1 = (-b + sqrt(discriminant)) / (2 * a)
                val x2 = (-b - sqrt(discriminant)) / (2 * a)
                listOf(x1, x2)
            }

            discriminant == 0.0 -> {
                val x = -b / (2 * a)
                listOf(x)
            }

            else -> {
                emptyList()
            }
        }
    }

    override fun toString(): String {
        return "${a}x^2 + (${b})x + ${c} = 0"
    }
}

fun main() {

    val equation = QuadraticEquation(1.0, 2.0, 1.0)

    println("Рівняння: $equation")
    println("Дискримінант: ${equation.discriminant}")
    println("Корені: ${equation.solve()}")

    equation.c = -3.0

    println()
    println("Після зміни c:")
    println("Рівняння: $equation")
    println("Дискримінант: ${equation.discriminant}")
    println("Корені: ${equation.solve()}")

    val equations = List(100) {

        var a = Random.nextInt(-50, 51)

        while (a == 0) {
            a = Random.nextInt(-50, 51)
        }

        val b = Random.nextInt(-50, 51)
        val c = Random.nextInt(-50, 51)

        QuadraticEquation(a, b, c)
    }

    val twoRoots = equations.filter {
        it.discriminant > 0
    }

    println()
    println("Рівняння з двома коренями:")

    twoRoots.forEach {
        val roots = it.solve()

        println(
            "$it -> x1 = ${roots[0]}, x2 = ${roots[1]}"
        )
    }

    println()
    println("Кількість рівнянь з двома коренями: ${twoRoots.size}")
}
