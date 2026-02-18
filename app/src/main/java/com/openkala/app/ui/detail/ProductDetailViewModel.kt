package com.openkala.app.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openkala.app.data.repository.ProductDetailRepository
import com.openkala.app.domain.model.DataSource
import com.openkala.app.domain.model.ProductDetailData
import com.openkala.app.domain.model.ProductPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailScreenState(
    val preview: ProductPreview?,
    val data: ProductDetailData? = null,
    val selectedVariantId: Long? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    private val preview = ProductPreview(
        productId = productId,
        title = Uri.decode(savedStateHandle.get<String>("title").orEmpty()),
        imageUrl = Uri.decode(savedStateHandle.get<String>("imageUrl").orEmpty()),
        price = (savedStateHandle.get<Long>("price") ?: -1L).takeIf { it >= 0L },
        discountPercent = (savedStateHandle.get<Int>("discount") ?: -1).takeIf { it >= 0 }
    ).takeIf { it.productId > 0L }

    private val _uiState = MutableStateFlow(ProductDetailScreenState(preview = preview))
    val uiState: StateFlow<ProductDetailScreenState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        if (productId <= 0L) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = "شناسه کالا معتبر نیست"
            )
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            runCatching {
                repository.streamProduct(productId).collect { payload ->
                    val previousSelected = _uiState.value.selectedVariantId
                    val selectedVariantId = payload.data.variants
                        .firstOrNull { it.id == previousSelected }
                        ?.id
                        ?: payload.data.selectedVariantId
                        ?: payload.data.variants.firstOrNull()?.id

                    _uiState.value = _uiState.value.copy(
                        data = payload.data,
                        selectedVariantId = selectedVariantId,
                        isLoading = false,
                        isRefreshing = payload.source == DataSource.CACHE,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = error.message ?: "خطا در دریافت اطلاعات کالا"
                )
            }
        }
    }

    fun selectVariant(variantId: Long) {
        val state = _uiState.value
        val data = state.data ?: return
        val exists = data.variants.any { it.id == variantId }
        if (!exists) return
        _uiState.value = state.copy(selectedVariantId = variantId)
    }
}
