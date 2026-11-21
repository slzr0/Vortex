package com.my.vortex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal enum class SessionVerification { VALID, INVALID, RATE_LIMITED, UNAVAILABLE }

internal fun isPlausibleSessionToken(token: String): Boolean =
    token.length in 20..4096 && token.none { it <= ' ' || it.code >= 127 }

internal fun verificationForHttpStatus(status: Int): SessionVerification = when (status) {
    HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN -> SessionVerification.INVALID
    429 -> SessionVerification.RATE_LIMITED
    else -> SessionVerification.UNAVAILABLE
}

internal suspend fun verifyDiscordToken(token: String): SessionVerification = withContext(Dispatchers.IO) {
    if (!isPlausibleSessionToken(token)) return@withContext SessionVerification.INVALID
    var connection: HttpURLConnection? = null
    try {
        connection = URL("https://discord.com/api/v10/users/@me").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Authorization", token)
        connection.setRequestProperty("Accept", "application/json")
        val status = connection.responseCode
        if (status != HttpURLConnection.HTTP_OK) return@withContext verificationForHttpStatus(status)
        val response = connection.inputStream.bufferedReader().use { reader ->
            val result = StringBuilder()
            val buffer = CharArray(4096)
            while (result.length < 65_536) {
                val count = reader.read(buffer, 0, minOf(buffer.size, 65_536 - result.length))
                if (count < 0) break
                result.append(buffer, 0, count)
            }
            result.toString()
        }
        val id = JSONObject(response).optString("id", "")
        if (id.isNotEmpty() && id.all(Char::isDigit)) SessionVerification.VALID else SessionVerification.UNAVAILABLE
    } catch (_: IOException) {
        SessionVerification.UNAVAILABLE
    } catch (_: org.json.JSONException) {
        SessionVerification.UNAVAILABLE
    } finally {
        connection?.disconnect()
    }
}
