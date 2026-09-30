data class OrderItem(
    val name: String,
    val price: Double,
    val quantity: Int
)

class Order(val id: String) {
    var customerName: String = ""
    var deliveryAddress: String = ""
    var discountPercent: Int = 0
    val items: MutableList<OrderItem> = mutableListOf()

    fun addItem(item: OrderItem) {
        items.add(item)
    }
}

fun main() {
    val order = Order("ORD-2026-001")
        .apply {
            customerName = "Олексій Коваленко"
            deliveryAddress = "вул. Хрещатик, 22, Київ"
            discountPercent = 10

            addItem(OrderItem("Клавіатура", 2500.0, 1))
            addItem(OrderItem("Мишка", 1200.0, 1))
            addItem(OrderItem("Килимок", 800.0, 2))
        }
        .also {
            println(
                "[LOG] Замовлення ${it.id} успішно налаштовано. " +
                        "Кількість позицій: ${it.items.size}"
            )
        }

    val finalPrice = order.run {
        val total = items.sumOf {
            it.price * it.quantity
        }

        total - total * discountPercent / 100
    }

    println("Розрахована сума: $finalPrice грн")

    val paymentPayload = finalPrice.let {
        val amount = (it * 100).toLong()
        "TXN-${order.id}_AMOUNT_${amount}_UAH"
    }

    println("Транзакційний payload: $paymentPayload")

    with(order) {
        println("========================================")
        println("ЧЕК ЗАМОВЛЕННЯ: $id")
        println("Отримувач: $customerName")
        println("Адреса доставки: $deliveryAddress")
        println("----------------------------------------")
        println("Товари:")

        items.forEach {
            println(" • ${it.name} (${it.quantity} шт.) - ${it.price} грн")
        }

        println("----------------------------------------")
        println("Знижка клієнта: $discountPercent%")
        println("Разом до сплати: $finalPrice грн")
        println("========================================")
    }
}