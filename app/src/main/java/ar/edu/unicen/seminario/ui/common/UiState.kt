package ar.edu.unicen.seminario.ui.common

import androidx.annotation.StringRes

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(@param:StringRes val messageRes: Int) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}
