package com.openkala.app.data.repository

import com.openkala.app.data.cache.ProductDetailCacheStore
import com.openkala.app.data.network.DigikalaApiService
import com.openkala.app.domain.mapper.mapProductDetail
import com.openkala.app.domain.mapper.readPagerTotal
import com.openkala.app.domain.model.DataSource
import com.openkala.app.domain.model.ProductDetailPayload
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Singleton
class ProductDetailRepository @Inject constructor(
    private val apiService: DigikalaApiService,
    private val cacheStore: ProductDetailCacheStore,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun streamProduct(productId: Long): Flow<ProductDetailPayload> = flow {
        val cached = cacheStore.read(productId)
        var emittedCache = false
        if (cached != null) {
            val detail = parse(cached.detailJson)
            emit(
                ProductDetailPayload(
                    data = mapProductDetail(
                        detail = detail,
                        commentsCount = cached.commentsCount,
                        questionsCount = cached.questionsCount
                    ),
                    source = DataSource.CACHE
                )
            )
            emittedCache = true
        }

        runCatching {
            coroutineScope {
                val detailDeferred = async { apiService.getProductDetail(productId) }
                val commentsDeferred = async {
                    runCatching {
                        readPagerTotal(apiService.getProductComments(productId = productId, page = 1))
                    }.getOrNull()
                }
                val questionsDeferred = async {
                    runCatching {
                        readPagerTotal(apiService.getProductQuestions(productId = productId, page = 1))
                    }.getOrNull()
                }

                val detail = detailDeferred.await()
                val commentsCount = commentsDeferred.await()
                val questionsCount = questionsDeferred.await()

                cacheStore.write(
                    productId = productId,
                    detailJson = json.encodeToString(JsonObject.serializer(), detail),
                    commentsCount = commentsCount,
                    questionsCount = questionsCount
                )

                emit(
                    ProductDetailPayload(
                        data = mapProductDetail(
                            detail = detail,
                            commentsCount = commentsCount,
                            questionsCount = questionsCount
                        ),
                        source = DataSource.NETWORK
                    )
                )
            }
        }.onFailure { error ->
            if (!emittedCache) throw error
        }
    }.flowOn(ioDispatcher)

    private fun parse(content: String): JsonObject {
        return json.parseToJsonElement(content).jsonObject
    }
}
