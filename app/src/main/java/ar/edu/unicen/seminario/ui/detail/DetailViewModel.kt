package ar.edu.unicen.seminario.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.seminario.data.remote.dto.MovieDetailDto
import ar.edu.unicen.seminario.data.repository.MovieRepository
import ar.edu.unicen.seminario.ui.common.UiState
import ar.edu.unicen.seminario.ui.common.toUserMessage
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: MovieRepository,
    private val movieId: Int
) : ViewModel() {

    private val _state = MutableLiveData<UiState<MovieDetailDto>>()
    val state: LiveData<UiState<MovieDetailDto>> = _state

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            repository.getMovieDetail(movieId)
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.toUserMessage()) }
        }
    }
}
