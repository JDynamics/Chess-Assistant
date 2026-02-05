package com.chessassistant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chessassistant.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore for app settings.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    private val context: Context
) {
    companion object {
        private val API_KEY = stringPreferencesKey("anthropic_api_key")
        private val ANALYSIS_DEPTH = intPreferencesKey("analysis_depth")
    }

    /**
     * Get the stored API key.
     */
    val apiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[API_KEY] ?: ""
        }

    /**
     * Get the analysis depth setting.
     */
    val analysisDepth: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[ANALYSIS_DEPTH] ?: Constants.STOCKFISH_DEFAULT_DEPTH
        }

    /**
     * Save the API key.
     */
    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }

    /**
     * Save the analysis depth.
     */
    suspend fun saveAnalysisDepth(depth: Int) {
        val validDepth = depth.coerceIn(Constants.STOCKFISH_MIN_DEPTH, Constants.STOCKFISH_MAX_DEPTH)
        context.dataStore.edit { preferences ->
            preferences[ANALYSIS_DEPTH] = validDepth
        }
    }

    /**
     * Clear all settings.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
