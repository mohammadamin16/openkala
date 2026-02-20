package com.openkala.app.data.repository

import com.openkala.app.data.cache.HomeCacheStore
import com.openkala.app.data.network.DigikalaApiService
import com.openkala.app.domain.mapper.mapHomeScreenData
import com.openkala.app.domain.model.DataSource
import com.openkala.app.domain.model.HomeScreenPayload
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
class HomeRepository @Inject constructor(
    private val apiService: DigikalaApiService,
    private val cacheStore: HomeCacheStore,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun streamHome(): Flow<HomeScreenPayload> = flow {
        val cached = cacheStore.read()
        var emittedCache = false
        if (cached != null) {
            val home = parse(cached.homeJson)
            val pillars = parse(cached.pillarsJson)
            val widget66 = cached.widget66Json?.let(::parse) ?: JsonObject(emptyMap())
            emit(
                HomeScreenPayload(
                    data = mapHomeScreenData(home, pillars, widget66),
                    source = DataSource.CACHE
                )
            )
            emittedCache = true
        }

        runCatching {
            coroutineScope {
                val homeDeferred = async { apiService.getHome() }
                val pillarsDeferred = async { apiService.getSuperAppPillars() }
                val widget66Deferred = async { apiService.getHomeCategoriesWidget() }
                val home = homeDeferred.await()
                val pillars = pillarsDeferred.await()
                val widget66 = widget66Deferred.await()
                cacheStore.write(
                    homeJson = json.encodeToString(JsonObject.serializer(), home),
                    pillarsJson = json.encodeToString(JsonObject.serializer(), pillars),
                    widget66Json = json.encodeToString(JsonObject.serializer(), widget66)
                )
                emit(
                    HomeScreenPayload(
                        data = mapHomeScreenData(home, pillars, widget66),
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
