package com.openkala.app.data.cache

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.productDetailDataStore by preferencesDataStore(name = "product_detail_cache")

@Singleton
class ProductDetailCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun read(productId: Long): ProductDetailCacheSnapshot? {
        val prefs = context.productDetailDataStore.data.first()
        val detailJson = prefs[Keys.detailJson(productId)]
        if (detailJson.isNullOrBlank()) return null

        return ProductDetailCacheSnapshot(
            detailJson = detailJson,
            commentsCount = prefs[Keys.commentsCount(productId)],
            questionsCount = prefs[Keys.questionsCount(productId)],
            updatedAt = prefs[Keys.updatedAt(productId)] ?: 0L
        )
    }

    suspend fun write(
        productId: Long,
        detailJson: String,
        commentsCount: Int?,
        questionsCount: Int?
    ) {
        context.productDetailDataStore.edit { prefs ->
            prefs[Keys.detailJson(productId)] = detailJson
            commentsCount?.let { prefs[Keys.commentsCount(productId)] = it }
            questionsCount?.let { prefs[Keys.questionsCount(productId)] = it }
            prefs[Keys.updatedAt(productId)] = System.currentTimeMillis()
        }
    }

    private object Keys {
        fun detailJson(productId: Long): Preferences.Key<String> =
            stringPreferencesKey("product_${productId}_detail_json")

        fun commentsCount(productId: Long): Preferences.Key<Int> =
            intPreferencesKey("product_${productId}_comments_count")

        fun questionsCount(productId: Long): Preferences.Key<Int> =
            intPreferencesKey("product_${productId}_questions_count")

        fun updatedAt(productId: Long): Preferences.Key<Long> =
            longPreferencesKey("product_${productId}_updated_at")
    }
}

data class ProductDetailCacheSnapshot(
    val detailJson: String,
    val commentsCount: Int?,
    val questionsCount: Int?,
    val updatedAt: Long
)
