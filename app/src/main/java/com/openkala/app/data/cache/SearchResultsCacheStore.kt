package com.openkala.app.data.cache

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.searchResultsDataStore by preferencesDataStore(name = "search_results_cache")

@Singleton
class SearchResultsCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun read(signature: String): SearchResultsCacheSnapshot? {
        val prefs = context.searchResultsDataStore.data.first()
        val key = signatureHash(signature)
        val json = prefs[stringPreferencesKey("search_results_json_$key")]
        if (json.isNullOrBlank()) return null

        return SearchResultsCacheSnapshot(
            json = json,
            updatedAt = prefs[longPreferencesKey("search_results_updated_at_$key")] ?: 0L
        )
    }

    suspend fun write(signature: String, json: String) {
        val key = signatureHash(signature)
        context.searchResultsDataStore.edit { prefs ->
            prefs[stringPreferencesKey("search_results_json_$key")] = json
            prefs[longPreferencesKey("search_results_updated_at_$key")] = System.currentTimeMillis()
        }
    }

    private fun signatureHash(signature: String): String {
        val digest = MessageDigest.getInstance("MD5")
            .digest(signature.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}

data class SearchResultsCacheSnapshot(
    val json: String,
    val updatedAt: Long
)
