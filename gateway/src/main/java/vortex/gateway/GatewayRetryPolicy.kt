package vortex.gateway

internal object GatewayRetryPolicy {
    private val fatalCodes = setOf(4004, 4010, 4011, 4012, 4013, 4014)

    fun canRetry(closeCode: Int?): Boolean = closeCode !in fatalCodes

    fun shouldClearSession(closeCode: Int?): Boolean = closeCode == 4007 || closeCode == 4009

    fun delayMillis(attempt: Int): Long {
        val exponent = attempt.coerceIn(0, 6)
        return (1_000L shl exponent).coerceAtMost(60_000L)
    }
}
