package com.ahoura.notekeeper.presentation.common

/** Generic three-state wrapper for asynchronously-loaded screen content. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
