package ar.edu.unicen.seminario.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.seminario.data.remote.dto.MovieDto
import ar.edu.unicen.seminario.data.remote.dto.MovieListResponse
import ar.edu.unicen.seminario.data.repository.MovieRepository
import ar.edu.unicen.seminario.ui.common.UiState
import ar.edu.unicen.seminario.ui.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _state = MutableLiveData<UiState<List<MovieDto>>>()
    val state: LiveData<UiState<List<MovieDto>>> = _state

    var query: String = ""
        private set

    private var job: Job? = null

    init {
        loadPopularMovies()
    }

    fun loadPopularMovies() {
        job?.cancel()
        query = ""
        _state.value = UiState.Loading
        job = viewModelScope.launch { load { repository.getPopularMovies() } }
    }

    fun onQueryChanged(text: String) {
        val newQuery = text.trim()
        if (newQuery == query) return
        query = newQuery
        job?.cancel()
        if (newQuery.isEmpty()) {
            loadPopularMovies()
            return
        }
        _state.value = UiState.Loading
        job = viewModelScope.launch {
            delay(SEARCH_DELAY_MS)
            load { repository.searchMovies(newQuery) }
        }
    }

    fun retry() {
        job?.cancel()
        _state.value = UiState.Loading
        job = viewModelScope.launch {
            if (query.isEmpty()) load { repository.getPopularMovies() }
            else load { repository.searchMovies(query) }
        }
    }

    private suspend fun load(call: suspend () -> Result<MovieListResponse>) {
        call()
            .onSuccess { response ->
                _state.value = if (response.results.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(response.results)
                }
            }
            .onFailure { _state.value = UiState.Error(it.toUserMessage()) }
    }

    private companion object {
        const val SEARCH_DELAY_MS = 400L
    }
}
