class Employee(
    val firstName: String,
    val lastName: String,
    val position: String
)

data class DataEmployee(
    val firstName: String,
    val lastName: String,
    val position: String
) {
    var bonus: Int = 0
}

class EmployeeManual(
    val firstName: String,
    val lastName: String,
    val position: String
) {
    override fun toString(): String {
        return "Employee($firstName, $lastName, $position)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmployeeManual) return false

        return firstName == other.firstName &&
                lastName == other.lastName &&
                position == other.position
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + position.hashCode()
        return result
    }

    operator fun component1() = firstName
    operator fun component2() = lastName
    operator fun component3() = position

    fun copy(
        firstName: String = this.firstName,
        lastName: String = this.lastName,
        position: String = this.position
    ): EmployeeManual {
        return EmployeeManual(firstName, lastName, position)
    }
}

fun main() {
    // Частина 1
    val emp1 = Employee("Max", "Blablabla", "Developer")
    val emp2 = Employee("Max", "Blablabla", "Developer")
    val emp3 = emp1

    println(emp1)
    println(emp1 === emp2)
    println(emp1 == emp2)
    println(emp1 === emp3)
    println(emp1 == emp3)

    // Частина 2
    val dataEmp1 = DataEmployee("Max", "Blablabla", "Developer")
    val dataEmp2 = DataEmployee("Max", "Blablabla", "Developer")

    println(dataEmp1)
    println(dataEmp1 == dataEmp2)

    val (name, surname, position) = dataEmp1
    println(name)
    println(surname)
    println(position)

    val dataEmp3 = dataEmp1.copy(position = "Senior Developer")
    println(dataEmp3)

    dataEmp1.bonus = 1000
    dataEmp2.bonus = 5000
    println(dataEmp1 == dataEmp2)

    // Частина 3
    val manualEmp1 = EmployeeManual("Max", "Blablabla", "Developer")
    val manualEmp2 = EmployeeManual("Max", "Blablabla", "Developer")

    println(manualEmp1)
    println(manualEmp1 == manualEmp2)

    val (name2, surname2, position2) = manualEmp1
    println(name2)
    println(surname2)
    println(position2)

    val manualEmp3 = manualEmp1.copy(position = "Senior Developer")
    println(manualEmp3)
}