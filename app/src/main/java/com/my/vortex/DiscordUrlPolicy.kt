package com.my.vortex

import java.net.URI

internal fun isDiscordWebUrl(value: String): Boolean = try {
    val uri = URI(value)
    uri.scheme.equals("https", ignoreCase = true) && uri.userInfo == null &&
        (uri.host.equals("discord.com", ignoreCase = true) || uri.host.equals("www.discord.com", ignoreCase = true)) &&
        (uri.port == -1 || uri.port == 443)
} catch (_: Exception) { false }

internal fun isDiscordAppUrl(value: String): Boolean = try {
    val path = URI(value).path.orEmpty()
    isDiscordWebUrl(value) && (path == "/app" || path == "/channels" || path.startsWith("/channels/"))
} catch (_: Exception) { false }
