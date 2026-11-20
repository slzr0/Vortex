/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyRPC.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.data.rpc

import com.my.vortex.domain.interfaces.Logger
import com.my.vortex.domain.repository.VortexRepository
import com.my.vortex.preference.Prefs
import com.my.vortex.preference.Prefs.CUSTOM_ACTIVITY_TYPE
import vortex.gateway.DiscordWebSocket
import vortex.gateway.entities.presence.Activity
import vortex.gateway.entities.presence.Assets
import vortex.gateway.entities.presence.Metadata
import vortex.gateway.entities.presence.Party
import vortex.gateway.entities.presence.Presence
import vortex.gateway.entities.presence.Timestamps
import kotlinx.coroutines.isActive

class VortexRPC(
    private val token: String,
    private val vortexRepository: VortexRepository,
    private val discordWebSocket: DiscordWebSocket,
    private val logger: Logger
) {
    private lateinit var presence: Presence
    private var activityName: String? = null
    private var applicationIdNumber = Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID, Constants.APPLICATION_ID]
    private var details: String? = null
    private var state: String? = null
    private var party: Party? = null
    private var largeImage: RpcImage? = null
    private var smallImage: RpcImage? = null
    private var largeText: String? = null
    private var smallText: String? = null
    private var status: String? = null
    private var startTimestamps: Long? = null
    private var stopTimestamps: Long? = null
    private var type: Int = 0
    private var platform: String? = null
    private var buttons = ArrayList<String>()
    private var buttonUrl = ArrayList<String>()
    private var url: String? = null

    fun closeRPC() {
        discordWebSocket.close()
    }

    fun isRpcRunning(): Boolean {
        return discordWebSocket.isWebSocketConnected()
    }

    private fun isUserTokenValid(): Boolean {
        return token.isNotBlank()
    }

    fun setName(activity_name: String?): VortexRPC {
        this.activityName = activity_name
        return this
    }

    fun setDetails(details: String?): VortexRPC {
        this.details = details
        return this
    }

    fun setState(state: String?): VortexRPC {
        this.state = state
        return this
    }

    fun setPartySize(current: Int?, max: Int?): VortexRPC {
        if (current != null && max != null) {
            this.party = Party(
                id = "vortex",
                size = arrayOf(current, max)
            )
        }
        return this
    }

    fun setLargeImage(large_image: RpcImage?, large_text: String? = null): VortexRPC {
        this.largeText = large_text.takeIf { !it.isNullOrEmpty() }
        this.largeImage = large_image
        return this
    }

    fun setSmallImage(small_image: RpcImage?, small_text: String? = null): VortexRPC {
        this.smallText = small_text.takeIf { !it.isNullOrEmpty() }
        this.smallImage = small_image
        return this
    }

    fun setStartTimestamps(start_timestamps: Long?): VortexRPC {
        this.startTimestamps = start_timestamps
        return this
    }

    fun setStopTimestamps(stop_timestamps: Long?): VortexRPC {
        this.stopTimestamps = stop_timestamps
        return this
    }


    fun setType(type: Int): VortexRPC {
        if (type in 0..5)
            this.type = type
        else this.type = 0
        return this
    }

    fun setStatus(status: String?): VortexRPC {
        this.status = status
        return this
    }

    fun setPlatform(platform: String?): VortexRPC {
        this.platform = platform
        return this
    }

    fun setButton1(button1_Text: String?): VortexRPC {
        button1_Text?.let { buttons.add(it) }
        return this
    }

    fun setButton2(button2_text: String?): VortexRPC {
        button2_text?.let { buttons.add(it) }
        return this
    }

    fun setButton1URL(url: String?): VortexRPC {
        url?.let { buttonUrl.add(it) }
        return this
    }

    fun setButton2URL(url: String?): VortexRPC {
        url?.let { buttonUrl.add(it) }
        return this
    }
    fun setStreamUrl(url: String?): VortexRPC {
        this.url = url
        return this
    }

    private fun String.sanitize(): String {
        return if (this.length > 128) {
            this.substring(0, 128)
        } else {
            this
        }
    }

    suspend fun build() {
        presence = Presence(
            activities = listOf(
                Activity(
                    name = activityName,
                    state = state?.sanitize(),
                    details = details?.sanitize(),
                    party = party.takeIf { party != null },
                    type = type,
                    platform = platform?.sanitize(),
                    timestamps = Timestamps(
                        start = startTimestamps,
                        end = stopTimestamps
                    ).takeIf { startTimestamps != null || stopTimestamps != null },
                    assets = Assets(
                        largeImage = largeImage?.resolveImage(vortexRepository),
                        smallImage = smallImage?.resolveImage(vortexRepository),
                        largeText = largeText?.sanitize(),
                        smallText = smallText?.sanitize()
                    ).takeIf { largeImage != null || smallImage != null },
                    buttons = buttons.takeIf { buttons.size > 0 },
                    metadata = Metadata(buttonUrls = buttonUrl).takeIf { buttonUrl.size > 0 },
                    applicationId = applicationIdNumber.takeIf { it.isNotEmpty() } ?: Constants.APPLICATION_ID,
                    url = url
                )
            ),
            afk = true,
            since = startTimestamps.takeIf { startTimestamps != null }?: System.currentTimeMillis(),
            status = status
        )
        connectToWebSocket()
    }
    private suspend fun connectToWebSocket() {
        if (!isUserTokenValid())
            logger.e(
                tag = "VortexRPC",
                event = "Token Seems to be invalid, Please Login if you haven't"
            )
        discordWebSocket.connect()
        discordWebSocket.sendActivity(presence)
    }

    suspend fun updateRPC(commonRpc: CommonRpc, enableTimestamps: Boolean? = true) {
        if (!discordWebSocket.isActive) return
        var time = Timestamps(start = startTimestamps)
        if (commonRpc.time != null)
            Timestamps(end = commonRpc.time.end, start = commonRpc.time.start).also { time = it }
        if (commonRpc.partyCurrentSize != null && commonRpc.partyMaxSize != null)
            Party(id = "vortex", size = arrayOf(commonRpc.partyCurrentSize, commonRpc.partyMaxSize)).also { party = it }
        discordWebSocket.sendActivity(
            Presence(
                activities = listOf(
                    Activity(
                        name = commonRpc.name,
                        details = commonRpc.details?.takeIf { it.isNotEmpty() }?.sanitize(),
                        state = commonRpc.state?.takeIf { it.isNotEmpty() }?.sanitize(),
                        type = commonRpc.type ?: Prefs[CUSTOM_ACTIVITY_TYPE, 0],
                        platform = commonRpc.platform?.sanitize(),
                        timestamps = time.takeIf { enableTimestamps == true },
                        assets = Assets(
                                largeImage = commonRpc.largeImage?.resolveImage(vortexRepository),
                                smallImage = commonRpc.smallImage?.resolveImage(vortexRepository),
                                largeText = commonRpc.largeText?.sanitize(),
                                smallText = commonRpc.smallText?.sanitize()
                            ).takeIf { commonRpc.largeImage != null || commonRpc.smallImage != null },
                        party = party.takeIf { party != null },
                        buttons = buttons.takeIf { buttons.size > 0 },
                        metadata = Metadata(buttonUrls = buttonUrl).takeIf { buttonUrl.size > 0 },
                        applicationId = applicationIdNumber.takeIf { it.isNotEmpty() } ?: Constants.APPLICATION_ID

                    )
                ),
                afk = true,
                since = startTimestamps,
                status = this.status
            )
        )
    }
}
