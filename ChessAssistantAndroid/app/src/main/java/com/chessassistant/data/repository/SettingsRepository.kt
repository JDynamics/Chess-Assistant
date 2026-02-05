package com.chessassistant.data.repository

import com.chessassistant.data.local.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing app settings.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    /**
     * Flow of the current API key.
     */
    val apiKeyFlow: Flow<String> = settingsDataStore.apiKey

    /**
     * Flow of the current analysis depth.
     */
    val analysisDepthFlow: Flow<Int> = settingsDataStore.analysisDepth

    /**
     * Get the current API key (suspending).
     */
    suspend fun getApiKey(): String = settingsDataStore.apiKey.first()

    /**
     * Get the current analysis depth (suspending).
     */
    suspend fun getAnalysisDepth(): Int = settingsDataStore.analysisDepth.first()

    /**
     * Save the API key.
     */
    suspend fun saveApiKey(key: String) {
        settingsDataStore.saveApiKey(key)
    }

    /**
     * Save the analysis depth.
     */
    suspend fun saveAnalysisDepth(depth: Int) {
        settingsDataStore.saveAnalysisDepth(depth)
    }

    /**
     * Check if the API key is configured.
     */
    suspend fun hasApiKey(): Boolean = getApiKey().isNotBlank()
}
