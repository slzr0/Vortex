/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyRepository.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */
package com.my.vortex.domain.repository

import com.my.vortex.domain.model.Contributor
import com.my.vortex.domain.model.Game
import com.my.vortex.domain.model.release.Release
import com.my.vortex.domain.model.user.User
import java.io.File

interface VortexRepository {
    suspend fun getImage(url: String): String?
    suspend fun uploadImage(file: File): String?
    suspend fun getGames(): List<Game>
    suspend fun getUser(userid: String): User
    suspend fun getContributors(): List<Contributor>
    suspend fun checkForUpdate(): Release
}
