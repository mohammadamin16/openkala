package com.openkala.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openkala.app.data.repository.CategoriesRepository
import com.openkala.app.domain.model.CategoriesScreenData
import com.openkala.app.domain.model.DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CategoriesUiState {
    data object LoadingNoCache : CategoriesUiState

    data class Content(
        val data: CategoriesScreenData,
        val selectedTabId: Long,
        val expandedSectionByTab: Map<Long, Long?>,
        val isRefreshing: Boolean,
        val isStale: Boolean,
        val transientMessage: String?
    ) : CategoriesUiState

    data class ErrorNoData(val message: String) : CategoriesUiState
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.LoadingNoCache)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            runCatching {
                repository.streamCategories().collect { payload ->
                    val previous = _uiState.value as? CategoriesUiState.Content
                    val selectedTabId = previous?.selectedTabId
                        ?.takeIf { id -> payload.data.tabs.any { it.id == id } }
                        ?: payload.data.selectedTabId

                    val expandedByTab = previous?.expandedSectionByTab.orEmpty().toMutableMap()
                    payload.data.tabs.forEach { tab ->
                        expandedByTab.putIfAbsent(tab.id, null)
                    }

                    _uiState.value = CategoriesUiState.Content(
                        data = payload.data,
                        selectedTabId = selectedTabId,
                        expandedSectionByTab = expandedByTab,
                        isRefreshing = payload.source == DataSource.CACHE,
                        isStale = payload.isStale,
                        transientMessage = payload.message
                    )
                }
            }.onFailure { error ->
                _uiState.value = CategoriesUiState.ErrorNoData(
                    message = error.message ?: "خطا در دریافت دسته‌بندی‌ها"
                )
            }
        }
    }

    fun onTabSelected(tabId: Long) {
        _uiState.update { current ->
            val content = current as? CategoriesUiState.Content ?: return@update current
            if (content.selectedTabId == tabId) return@update current

            content.copy(
                selectedTabId = tabId,
                transientMessage = null
            )
        }
    }

    fun onSectionToggle(sectionId: Long) {
        _uiState.update { current ->
            val content = current as? CategoriesUiState.Content ?: return@update current
            val currentTabId = content.selectedTabId
            val expandedSectionId = content.expandedSectionByTab[currentTabId]
            val next = if (expandedSectionId == sectionId) null else sectionId

            content.copy(
                expandedSectionByTab = content.expandedSectionByTab + (currentTabId to next)
            )
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { current ->
            val content = current as? CategoriesUiState.Content ?: return@update current
            if (content.transientMessage == null) return@update current
            content.copy(transientMessage = null)
        }
    }
}
