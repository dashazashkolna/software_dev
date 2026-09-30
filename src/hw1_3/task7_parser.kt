fun main() {
    val rawLogs = listOf(
        "2026-09-01 INFO: User logged in",
        "2026-09-01 ERROR: 500 Internal Server Error",
        "2026-09-01 WARN: High memory usage",
        "2026-09-01 ERROR: 404 Not Found",
        "2026-09-01 INFO: Payment processed"
    )

    var infoCount = 0
    var warnCount = 0
    var errorCount = 0

    val errorMessages = mutableListOf<String>()

    rawLogs.forEach { log ->
        when {
            log.contains("INFO") -> {
                infoCount++
            }

            log.contains("WARN") -> {
                warnCount++
            }

            log.contains("ERROR") -> {
                errorCount++
                errorMessages.add(log)
            }
        }
    }

    val report = """
INFO messages:  $infoCount
WARN messages:  $warnCount
ERROR messages: $errorCount

Error messages:
${errorMessages.joinToString("\n")}
    """.trimIndent()

    println(report)
}