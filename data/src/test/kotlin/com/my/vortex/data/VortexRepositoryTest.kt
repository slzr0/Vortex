/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyRespositoryTest.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.data

import com.my.vortex.data.remote.ApiService
import com.my.vortex.data.remote.Imgur
import com.my.vortex.data.remote.ImgurApiService
import com.my.vortex.data.repository.VortexRepositoryImpl
import com.my.vortex.data.rpc.Constants
import com.my.vortex.domain.repository.VortexRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.File

class VortexRepositoryTest {
    private lateinit var apiService: ApiService
    private lateinit var imgurService: ImgurApiService
    private lateinit var vortexRepository: VortexRepository
    @Before
    fun setup() {
        val client = setupClient()
        apiService = ApiService(
            client = client,
            baseUrl = BuildConfig.BASE_URL,
            githubBaseUrl = BuildConfig.GITHUB_API_BASE_URL
        )
        imgurService = ImgurApiService(
            client = client,
            discordBaseUrl = BuildConfig.DISCORD_API_BASE_URL,
            imgurBaseUrl = BuildConfig.IMGUR_API_BASE_URL,
        )
        vortexRepository = VortexRepositoryImpl(apiService,imgurService)
    }

    private fun setupClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            logger = object: Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }

    @Test
    fun `Upload an Image Through Api`() = runBlocking {
        val file = File("C:\\Users\\Administrator\\Downloads", "images.jpg")
        val response = vortexRepository.uploadImage(file)
        assert(!response.isNullOrEmpty())
    }

    @Test
    fun `Get an Image Through Api`() = runBlocking {
        val response = vortexRepository.getImage(Constants.NINTENDO_LINK)
        assert(!response.isNullOrEmpty())
    }

    @Test
    fun `Get Games Through Api`() = runBlocking {
        val games = vortexRepository.getGames()
        assert(games.isNotEmpty())
    }

    @Test
    fun `Get a User Through Api`() = runBlocking {
        val user = vortexRepository.getUser("888890990956511263")
        assert(user.username == "yzzik")
        assert(user.verified)
    }

    @Test
    fun `Get Contributors Through Api`() = runBlocking {
        val response = vortexRepository.getContributors()
        println(response)
    }

    @Test
    fun `Check for Update`() = runBlocking {
        val response = vortexRepository.checkForUpdate()
        println(response)
    }
}