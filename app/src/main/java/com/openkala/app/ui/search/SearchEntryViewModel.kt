package com.openkala.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openkala.app.data.repository.SearchRepository
import com.openkala.app.domain.model.SearchSuggestionItem
import com.openkala.app.domain.model.SearchTrendItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchEntryUiState(
    val query: String = "",
    val trends: List<SearchTrendItem> = emptyList(),
    val suggestions: List<SearchSuggestionItem> = emptyList(),
    val isLoadingTrends: Boolean = true,
    val isLoadingSuggestions: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchEntryViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchEntryUiState())
    val uiState: StateFlow<SearchEntryUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var trendsJob: Job? = null

    init {
        loadTrends()
        observeQuery()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, errorMessage = null) }
        queryFlow.value = query
    }

    fun onTrendClick(keyword: String) {
        onQueryChange(keyword)
    }

    fun retryTrends() {
        loadTrends()
    }

    private fun loadTrends() {
        trendsJob?.cancel()
        trendsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingTrends = true,
                    errorMessage = null
                )
            }

            runCatching {
                repository.fetchHotTrends()
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        trends = data.trends,
                        isLoadingTrends = false,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingTrends = false,
                        errorMessage = error.message ?: "خطا در دریافت جستجوهای پرطرفدار"
                    )
                }
            }
        }
    }

    private fun observeQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(
                                suggestions = emptyList(),
                                isLoadingSuggestions = false,
                                errorMessage = null
                            )
                        }
                        return@collectLatest
                    }

                    _uiState.update {
                        it.copy(
                            isLoadingSuggestions = true,
                            errorMessage = null
                        )
                    }

                    runCatching {
                        repository.fetchSuggestions(query)
                    }.onSuccess { suggestions ->
                        _uiState.update {
                            it.copy(
                                suggestions = suggestions,
                                isLoadingSuggestions = false
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingSuggestions = false,
                                errorMessage = error.message ?: "خطا در دریافت پیشنهادها"
                            )
                        }
                    }
                }
        }
    }
}
