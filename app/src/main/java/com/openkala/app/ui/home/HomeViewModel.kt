package com.openkala.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openkala.app.data.repository.HomeRepository
import com.openkala.app.domain.model.DataSource
import com.openkala.app.domain.model.HomeScreenData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(
        val data: HomeScreenData,
        val isRefreshing: Boolean
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            runCatching {
                repository.streamHome().collect { payload ->
                    _uiState.value = HomeUiState.Content(
                        data = payload.data,
                        isRefreshing = payload.source == DataSource.CACHE
                    )
                }
            }.onFailure { error ->
                _uiState.value = HomeUiState.Error(
                    message = error.message ?: "Unknown error"
                )
            }
        }
    }
}
