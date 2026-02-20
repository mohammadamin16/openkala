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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private const val HOME_REFRESH_MAX_ATTEMPTS = 3
private const val INCREDIBLE_MAX_CACHE_AGE_MS = 6 * 60 * 60 * 1000L
private val HOME_REFRESH_RETRY_DELAYS_MS = listOf(250L, 500L)

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
            val mapped = mapHomeScreenData(home, pillars, widget66)
            emit(
                HomeScreenPayload(
                    data = suppressStaleIncredibleSection(
                        data = mapped,
                        updatedAtMillis = cached.updatedAt
                    ),
                    source = DataSource.CACHE
                )
            )
            emittedCache = true
        }

        runCatching {
            val bundle = retryWithAttempts(
                maxAttempts = HOME_REFRESH_MAX_ATTEMPTS,
                retryDelaysMs = HOME_REFRESH_RETRY_DELAYS_MS
            ) {
                coroutineScope {
                    val homeDeferred = async { apiService.getHome() }
                    val pillarsDeferred = async { apiService.getSuperAppPillars() }
                    val widget66Deferred = async { apiService.getHomeCategoriesWidget() }
                    HomeBundle(
                        home = homeDeferred.await(),
                        pillars = pillarsDeferred.await(),
                        widget66 = widget66Deferred.await()
                    )
                }
            }
            cacheStore.write(
                homeJson = json.encodeToString(JsonObject.serializer(), bundle.home),
                pillarsJson = json.encodeToString(JsonObject.serializer(), bundle.pillars),
                widget66Json = json.encodeToString(JsonObject.serializer(), bundle.widget66)
            )
            emit(
                HomeScreenPayload(
                    data = mapHomeScreenData(bundle.home, bundle.pillars, bundle.widget66),
                    source = DataSource.NETWORK
                )
            )
        }.onFailure { error ->
            if (!emittedCache) throw error
        }
    }.flowOn(ioDispatcher)

    private fun parse(content: String): JsonObject {
        return json.parseToJsonElement(content).jsonObject
    }
}

private data class HomeBundle(
    val home: JsonObject,
    val pillars: JsonObject,
    val widget66: JsonObject
)

internal suspend fun <T> retryWithAttempts(
    maxAttempts: Int,
    retryDelaysMs: List<Long>,
    block: suspend () -> T
): T {
    require(maxAttempts >= 1) { "maxAttempts must be at least 1" }

    var lastError: Throwable? = null
    repeat(maxAttempts) { attempt ->
        try {
            return block()
        } catch (error: Throwable) {
            lastError = error
            val isLastAttempt = attempt == maxAttempts - 1
            if (!isLastAttempt) {
                delay(retryDelaysMs.getOrElse(attempt) { 0L })
            }
        }
    }

    throw lastError ?: IllegalStateException("Retry failed without an exception")
}

internal fun suppressStaleIncredibleSection(
    data: com.openkala.app.domain.model.HomeScreenData,
    updatedAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    maxAgeMillis: Long = INCREDIBLE_MAX_CACHE_AGE_MS
): com.openkala.app.domain.model.HomeScreenData {
    if (updatedAtMillis <= 0L) return data
    val age = nowMillis - updatedAtMillis
    if (age <= maxAgeMillis || age < 0L) return data

    return data.copy(
        incredibleOffers = data.incredibleOffers.copy(items = emptyList())
    )
}
