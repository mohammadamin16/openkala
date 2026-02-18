package com.openkala.app.data.cache

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.searchDataStore by preferencesDataStore(name = "search_cache")

@Singleton
class SearchCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun readTrends(): SearchCacheSnapshot? {
        val prefs = context.searchDataStore.data.first()
        val trendsJson = prefs[Keys.TRENDS_JSON]
        if (trendsJson.isNullOrBlank()) return null

        return SearchCacheSnapshot(
            trendsJson = trendsJson,
            updatedAt = prefs[Keys.UPDATED_AT] ?: 0L
        )
    }

    suspend fun writeTrends(trendsJson: String) {
        context.searchDataStore.edit { prefs ->
            prefs[Keys.TRENDS_JSON] = trendsJson
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    private object Keys {
        val TRENDS_JSON: Preferences.Key<String> = stringPreferencesKey("search_trends_json")
        val UPDATED_AT: Preferences.Key<Long> = longPreferencesKey("search_trends_updated_at")
    }
}

data class SearchCacheSnapshot(
    val trendsJson: String,
    val updatedAt: Long
)
