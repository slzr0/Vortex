package com.my.vortex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscordSessionTest {
    @Test fun rejectsMalformedTokensBeforeSending() {
        assertTrue(isPlausibleSessionToken("a".repeat(30)))
        assertFalse(isPlausibleSessionToken("null"))
        assertFalse(isPlausibleSessionToken("a".repeat(20) + "\nX-Injection: value"))
        assertFalse(isPlausibleSessionToken("a".repeat(4097)))
    }

    @Test fun distinguishesFailuresForRetry() {
        assertEquals(SessionVerification.INVALID, verificationForHttpStatus(401))
        assertEquals(SessionVerification.INVALID, verificationForHttpStatus(403))
        assertEquals(SessionVerification.RATE_LIMITED, verificationForHttpStatus(429))
        assertEquals(SessionVerification.UNAVAILABLE, verificationForHttpStatus(500))
    }
}
