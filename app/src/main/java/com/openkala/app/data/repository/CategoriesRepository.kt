package com.openkala.app.data.repository

import com.openkala.app.data.cache.CategoriesCacheStore
import com.openkala.app.data.network.DigikalaApiService
import com.openkala.app.domain.mapper.mapMegaMenuEnvelope
import com.openkala.app.domain.model.CategoriesPayload
import com.openkala.app.domain.model.CategoriesScreenData
import com.openkala.app.domain.model.DataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Singleton
class CategoriesRepository @Inject constructor(
    private val apiService: DigikalaApiService,
    private val cacheStore: CategoriesCacheStore,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {

    fun streamCategories(): Flow<CategoriesPayload> = flow {
        val cached = cacheStore.read()
        var cachedData: CategoriesScreenData? = null

        if (cached != null) {
            val cachedEnvelope = mapMegaMenuEnvelope(parse(cached.megaMenuJson))
            if (cachedEnvelope != null) {
                cachedData = cachedEnvelope.data
                emit(
                    CategoriesPayload(
                        data = cachedEnvelope.data,
                        source = DataSource.CACHE
                    )
                )
            }
        }

        runCatching {
            val response = apiService.getDictionaries(
                type = "mega_menu",
                hash = cached?.megaMenuHash.orEmpty()
            )

            val envelope = mapMegaMenuEnvelope(response)
            when {
                envelope != null -> {
                    cacheStore.write(
                        megaMenuJson = json.encodeToString(JsonObject.serializer(), response),
                        megaMenuHash = envelope.hash
                    )
                    emit(
                        CategoriesPayload(
                            data = envelope.data,
                            source = DataSource.NETWORK
                        )
                    )
                }

                cachedData != null -> {
                    emit(
                        CategoriesPayload(
                            data = cachedData!!,
                            source = DataSource.NETWORK
                        )
                    )
                }

                else -> {
                    error("No category data available")
                }
            }
        }.onFailure { error ->
            if (cachedData != null) {
                emit(
                    CategoriesPayload(
                        data = cachedData!!,
                        source = DataSource.NETWORK,
                        isStale = true,
                        message = error.message ?: "خطا در به‌روزرسانی دسته‌بندی‌ها"
                    )
                )
            } else {
                throw error
            }
        }
    }.flowOn(ioDispatcher)

    private fun parse(content: String): JsonObject {
        return json.parseToJsonElement(content).jsonObject
    }
}
