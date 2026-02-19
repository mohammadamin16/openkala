package com.openkala.app.ui.search.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openkala.app.data.repository.SearchResultsRepository
import com.openkala.app.domain.model.SearchProductItem
import com.openkala.app.domain.model.SearchRecommendationItem
import com.openkala.app.domain.model.SearchResultsHeader
import com.openkala.app.domain.model.DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchResultsUiState {
    data object LoadingNoCache : SearchResultsUiState

    data class Content(
        val query: String,
        val categoryCode: String?,
        val items: List<SearchProductItem>,
        val header: SearchResultsHeader,
        val recommendations: List<SearchRecommendationItem>,
        val page: Int,
        val totalPages: Int,
        val sort: Int?,
        val isRefreshing: Boolean,
        val isAppending: Boolean,
        val isStale: Boolean,
        val transientMessage: String?
    ) : SearchResultsUiState {
        val hasMore: Boolean get() = page < totalPages
    }

    data class ErrorNoData(val message: String) : SearchResultsUiState
}

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val repository: SearchResultsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchResultsUiState>(SearchResultsUiState.LoadingNoCache)
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    private var initialLoadJob: Job? = null
    private var appendJob: Job? = null
    private var loadedSignature: String? = null

    fun ensureLoaded(query: String, categoryCode: String?) {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            _uiState.value = SearchResultsUiState.ErrorNoData("عبارت جستجو خالی است")
            return
        }

        val signature = "$normalized|${categoryCode.orEmpty()}"
        if (signature == loadedSignature && _uiState.value !is SearchResultsUiState.ErrorNoData) {
            return
        }
        loadedSignature = signature
        loadInitial(query = normalized, categoryCode = categoryCode, sort = null)
    }

    fun reloadWith(query: String, categoryCode: String?) {
        loadedSignature = "$query|${categoryCode.orEmpty()}"
        loadInitial(query = query, categoryCode = categoryCode, sort = null)
    }

    fun loadNextPage() {
        val content = _uiState.value as? SearchResultsUiState.Content ?: return
        if (!content.hasMore || content.isAppending) return

        appendJob?.cancel()
        appendJob = viewModelScope.launch {
            _uiState.update { current ->
                val c = current as? SearchResultsUiState.Content ?: return@update current
                c.copy(isAppending = true, transientMessage = null)
            }

            runCatching {
                repository.loadNextPage(
                    query = content.query,
                    categoryCode = content.categoryCode,
                    page = content.page + 1,
                    sort = content.sort
                )
            }.onSuccess { next ->
                _uiState.update { current ->
                    val c = current as? SearchResultsUiState.Content ?: return@update current
                    val merged = (c.items + next.page.products).distinctBy { it.id }
                    c.copy(
                        items = merged,
                        page = next.page.currentPage,
                        totalPages = next.page.totalPages,
                        isAppending = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    val c = current as? SearchResultsUiState.Content ?: return@update current
                    c.copy(
                        isAppending = false,
                        transientMessage = error.message ?: "خطا در دریافت صفحه بعد"
                    )
                }
            }
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { current ->
            val c = current as? SearchResultsUiState.Content ?: return@update current
            c.copy(transientMessage = null)
        }
    }

    private fun loadInitial(query: String, categoryCode: String?, sort: Int?) {
        initialLoadJob?.cancel()
        appendJob?.cancel()
        _uiState.value = SearchResultsUiState.LoadingNoCache

        initialLoadJob = viewModelScope.launch {
            runCatching {
                repository.streamFirstPage(
                    query = query,
                    categoryCode = categoryCode,
                    sort = sort
                ).collect { payload ->
                    _uiState.value = SearchResultsUiState.Content(
                        query = query,
                        categoryCode = categoryCode,
                        items = payload.data.page.products,
                        header = payload.data.header,
                        recommendations = payload.data.recommendations,
                        page = payload.data.page.currentPage,
                        totalPages = payload.data.page.totalPages,
                        sort = payload.data.page.sortOptions.firstOrNull { it.selected }?.id,
                        isRefreshing = payload.source == DataSource.CACHE,
                        isAppending = false,
                        isStale = payload.isStale,
                        transientMessage = payload.message
                    )
                }
            }.onFailure { error ->
                _uiState.value = SearchResultsUiState.ErrorNoData(
                    message = error.message ?: "خطا در دریافت نتایج"
                )
            }
        }
    }
}
