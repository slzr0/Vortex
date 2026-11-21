/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyRepositoryImpl.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.data.repository

import com.my.vortex.data.remote.ApiService
import com.my.vortex.data.remote.GamesResponse
import com.my.vortex.data.remote.ImgurApiService
import com.my.vortex.data.remote.toGame
import com.my.vortex.data.rpc.Constants
import com.my.vortex.data.utils.toAttachmentAsset
import com.my.vortex.data.utils.toExternalAsset
import com.my.vortex.data.utils.toImageURL
import com.my.vortex.domain.model.Contributor
import com.my.vortex.domain.model.Game
import com.my.vortex.domain.model.release.Release
import com.my.vortex.domain.model.user.User
import com.my.vortex.domain.repository.VortexRepository
import com.my.vortex.preference.Prefs
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.IOException
import javax.inject.Inject

internal class VortexRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val imgurApi: ImgurApiService,
    private val bundledGameCatalog: BundledGameCatalog? = null,
) : VortexRepository {

    override suspend fun getImage(url: String): String? {
        suspend fun fromDiscord(): String? = withTimeoutOrNull(5_000) {
            imgurApi.getImage(url, Prefs[Prefs.TOKEN]).getOrNull()?.toExternalAsset()
        }
        suspend fun fromServer(): String? = withTimeoutOrNull(5_000) {
            api.getImage(url).getOrNull()?.toAttachmentAsset()
        }
        return if (Prefs[Prefs.USE_IMGUR, false]) {
            fromDiscord() ?: fromServer()
        } else {
            fromServer() ?: fromDiscord()
        }
    }

    override suspend fun uploadImage(file: File): String? {
        return if (Prefs[Prefs.USE_IMGUR, false]) {
            imgurApi.uploadImage(file, Prefs[Prefs.IMGUR_CLIENT_ID, Constants.IMGUR_CLIENT_ID])
                .getOrNull()?.toImageURL()?.let { this.getImage(it) }
        } else {
            api.uploadImage(file).getOrNull()?.toAttachmentAsset()
        }
    }

    override suspend fun getGames(): List<Game> {
        val bundled = bundledGameCatalog?.load().orEmpty()
        val remote = withTimeoutOrNull(4_000) {
            try {
                val response = api.getGames().getOrThrow()
                if (response.status.value !in 200..299) emptyList()
                else response.body<List<GamesResponse>>().map { it.toGame() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        }.orEmpty()
        return (remote + bundled).distinctBy { it.platform.lowercase() to it.game_title.trim().lowercase() }
    }

    override suspend fun getUser(userid: String): User {
        return api.getUser(userid).getOrNull()?.body() ?: User()
    }

    override suspend fun getContributors(): List<Contributor> {
        return api.getContributors().getOrNull()?.body() ?: emptyList()
    }

    override suspend fun checkForUpdate(): Release {
        val response = api.checkForUpdate().getOrThrow()
        if (response.status.value != 200) {
            throw IOException("No Vortex release available (HTTP ${response.status.value})")
        }
        return response.releaseBody()
    }
}

suspend fun HttpResponse.releaseBody(): Release {
    return if (this.status.value == 200) {
        Prefs.saveLatestRelease(this.body())
        this.body()
    } else {
        Prefs.getSavedLatestRelease() ?: Release()
    }
}