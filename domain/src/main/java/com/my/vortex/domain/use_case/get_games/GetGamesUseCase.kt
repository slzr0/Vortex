/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GetGamesUseCase.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.domain.use_case.get_games

import com.my.vortex.domain.model.Resource
import com.my.vortex.domain.model.Game
import com.my.vortex.domain.repository.VortexRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject

class GetGamesUseCase @Inject constructor(
    private val vortexRepository: VortexRepository
) {
    operator fun invoke(): Flow<Resource<List<Game>>> = flow {
        try {
            emit(Resource.Loading())
            val games = vortexRepository.getGames()
            emit(Resource.Success(games))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }.flowOn(Dispatchers.IO)
}