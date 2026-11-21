package com.my.vortex

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscordUrlPolicyTest {
    @Test fun onlyAllowsTrustedHttpsOrigins() {
        assertTrue(isDiscordWebUrl("https://discord.com/login"))
        assertTrue(isDiscordWebUrl("https://www.discord.com:443/channels/@me"))
        assertFalse(isDiscordWebUrl("http://discord.com/login"))
        assertFalse(isDiscordWebUrl("https://discord.com.evil.test/app"))
        assertFalse(isDiscordWebUrl("https://discord.com@evil.test/app"))
        assertFalse(isDiscordWebUrl("https://discord.com:444/app"))
        assertFalse(isDiscordWebUrl("javascript:alert(1)"))
    }

    @Test fun readsSessionOnlyOnAppRoutes() {
        assertTrue(isDiscordAppUrl("https://discord.com/app"))
        assertTrue(isDiscordAppUrl("https://discord.com/channels/@me"))
        assertFalse(isDiscordAppUrl("https://discord.com/login"))
        assertFalse(isDiscordAppUrl("https://other.test/channels/@me"))
        assertFalse(isDiscordAppUrl("https://discord.com/channels-evil"))
    }
}
