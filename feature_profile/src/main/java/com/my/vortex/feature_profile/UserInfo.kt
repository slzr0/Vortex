/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UserInfo.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_profile

import com.my.vortex.domain.model.user.User
import com.my.vortex.preference.Prefs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import vortex.gateway.DiscordWebSocketImpl
import vortex.gateway.entities.Payload
import vortex.gateway.entities.ReadyUser

suspend fun getUserInfo(token: String): Boolean {
    val ready = CompletableDeferred<ReadyUser>()
    val socket = object : DiscordWebSocketImpl(token) {
        override fun Payload.handleDispatch(jsonString: String) {
            if (t == "READY") decodeReady(jsonString)?.user?.let { ready.complete(it) }
        }
    }
    return try {
        socket.connect()
        val user = withTimeoutOrNull(30_000) { ready.await() } ?: return false
        val id = user.id?.takeIf { it.isNotBlank() } ?: return false
        Prefs[Prefs.USER_ID] = id
        Prefs[Prefs.USER_BIO] = user.bio.orEmpty()
        Prefs[Prefs.USER_NITRO] = user.premiumType in 1..3
        Prefs[Prefs.USER_DATA] = Json.encodeToString(User(
            id = id, bio = user.bio.orEmpty(), nitro = user.premiumType in 1..3,
            username = user.username, globalName = user.globalName,
            avatar = user.avatar, discriminator = user.discriminator
        ))
        true
    } finally {
        socket.close()
    }
}
