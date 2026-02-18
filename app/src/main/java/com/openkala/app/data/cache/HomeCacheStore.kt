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

private val Context.homeDataStore by preferencesDataStore(name = "home_cache")

@Singleton
class HomeCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun read(): CacheSnapshot? {
        val prefs = context.homeDataStore.data.first()
        val home = prefs[Keys.HOME_JSON]
        val pillars = prefs[Keys.PILLARS_JSON]
        if (home.isNullOrBlank() || pillars.isNullOrBlank()) return null
        return CacheSnapshot(
            homeJson = home,
            pillarsJson = pillars,
            updatedAt = prefs[Keys.UPDATED_AT] ?: 0L
        )
    }

    suspend fun write(homeJson: String, pillarsJson: String) {
        context.homeDataStore.edit { prefs ->
            prefs[Keys.HOME_JSON] = homeJson
            prefs[Keys.PILLARS_JSON] = pillarsJson
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    private object Keys {
        val HOME_JSON: Preferences.Key<String> = stringPreferencesKey("home_json")
        val PILLARS_JSON: Preferences.Key<String> = stringPreferencesKey("pillars_json")
        val UPDATED_AT: Preferences.Key<Long> = longPreferencesKey("updated_at")
    }
}

data class CacheSnapshot(
    val homeJson: String,
    val pillarsJson: String,
    val updatedAt: Long
)

