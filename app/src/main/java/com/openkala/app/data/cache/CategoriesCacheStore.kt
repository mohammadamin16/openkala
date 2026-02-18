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

private val Context.categoriesDataStore by preferencesDataStore(name = "categories_cache")

@Singleton
class CategoriesCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun read(): CategoriesCacheSnapshot? {
        val prefs = context.categoriesDataStore.data.first()
        val megaMenuJson = prefs[Keys.MEGA_MENU_JSON]
        if (megaMenuJson.isNullOrBlank()) return null

        return CategoriesCacheSnapshot(
            megaMenuJson = megaMenuJson,
            megaMenuHash = prefs[Keys.MEGA_MENU_HASH].orEmpty(),
            updatedAt = prefs[Keys.UPDATED_AT] ?: 0L
        )
    }

    suspend fun write(megaMenuJson: String, megaMenuHash: String) {
        context.categoriesDataStore.edit { prefs ->
            prefs[Keys.MEGA_MENU_JSON] = megaMenuJson
            prefs[Keys.MEGA_MENU_HASH] = megaMenuHash
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    private object Keys {
        val MEGA_MENU_JSON: Preferences.Key<String> = stringPreferencesKey("mega_menu_json")
        val MEGA_MENU_HASH: Preferences.Key<String> = stringPreferencesKey("mega_menu_hash")
        val UPDATED_AT: Preferences.Key<Long> = longPreferencesKey("updated_at")
    }
}

data class CategoriesCacheSnapshot(
    val megaMenuJson: String,
    val megaMenuHash: String,
    val updatedAt: Long
)
