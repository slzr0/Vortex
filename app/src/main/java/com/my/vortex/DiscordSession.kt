package com.my.vortex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal suspend fun verifyDiscordToken(token: String): Boolean = withContext(Dispatchers.IO) {
    if (token.isBlank() || token.length > 4096 || token.any { it <= ' ' || it.code >= 127 }) return@withContext false
    val connection = URL("https://discord.com/api/v10/users/@me").openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Authorization", token)
        connection.setRequestProperty("Accept", "application/json")
        if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext false
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        JSONObject(response).optString("id").isNotBlank()
    } finally {
        connection.disconnect()
    }
}
