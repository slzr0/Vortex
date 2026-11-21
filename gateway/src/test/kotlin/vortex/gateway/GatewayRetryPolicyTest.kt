package vortex.gateway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GatewayRetryPolicyTest {
    @Test fun retriesTransientErrorsWithBoundedBackoff() {
        assertTrue(GatewayRetryPolicy.canRetry(null))
        assertTrue(GatewayRetryPolicy.canRetry(4000))
        assertTrue(GatewayRetryPolicy.canRetry(1006))
        assertEquals(1_000L, GatewayRetryPolicy.delayMillis(0))
        assertEquals(4_000L, GatewayRetryPolicy.delayMillis(2))
        assertEquals(60_000L, GatewayRetryPolicy.delayMillis(100))
    }

    @Test fun distinguishesInvalidSessionsAndFatalErrors() {
        assertTrue(GatewayRetryPolicy.shouldClearSession(4007))
        assertTrue(GatewayRetryPolicy.shouldClearSession(4009))
        assertFalse(GatewayRetryPolicy.shouldClearSession(4000))
        listOf(4004, 4010, 4011, 4012, 4013, 4014).forEach {
            assertFalse(GatewayRetryPolicy.canRetry(it))
        }
    }
}
