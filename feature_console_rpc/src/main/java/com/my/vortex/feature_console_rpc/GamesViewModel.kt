/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GamesViewModel.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_console_rpc

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.vortex.domain.model.Game
import com.my.vortex.domain.model.Resource
import com.my.vortex.domain.use_case.get_games.GetGamesUseCase
import com.my.vortex.resources.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val getGamesUseCase: GetGamesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state: MutableState<GamesState> = mutableStateOf(GamesState.Loading)
    val state: State<GamesState> = _state
    private var games: List<Game> = emptyList()
    val isSearchBarVisible = mutableStateOf(false)

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var query: String = ""
    private var catalogLoaded = false

    init {
        getGames()
    }

    private fun getGames() {
        loadJob?.cancel()
        searchJob?.cancel()
        catalogLoaded = false
        loadJob = getGamesUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    games = result.data.orEmpty().toList()
                    catalogLoaded = true
                    searchForGame(query, 0)
                }

                is Resource.Error -> {
                    _state.value = GamesState.Error(
                        error = result.message ?: context.getString(R.string.unknown_error)
                    )
                }

                is Resource.Loading -> {
                    _state.value = GamesState.Loading
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            UiEvent.CloseSearchBar -> isSearchBarVisible.value = false
            UiEvent.OpenSearchBar -> isSearchBarVisible.value = true
            is UiEvent.Search -> onSearch(uiEvent.query)
            UiEvent.TryAgain -> getGames()
        }
    }

    private fun onSearch(query: String) {
        this.query = query
        searchForGame(query, 300)
    }

    private fun searchForGame(query: String, debounceMillis: Long) {
        searchJob?.cancel()
        if (!catalogLoaded) return
        val snapshot = games
        searchJob = viewModelScope.launch {
            if (debounceMillis > 0) delay(debounceMillis)
            val filtered = withContext(Dispatchers.Default) { filterConsoleGames(snapshot, query) }
            _state.value = GamesState.Success(filtered)
        }
    }
}
