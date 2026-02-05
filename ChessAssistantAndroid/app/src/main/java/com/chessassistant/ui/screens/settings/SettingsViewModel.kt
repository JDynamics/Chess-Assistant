package com.chessassistant.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessassistant.data.repository.SettingsRepository
import com.chessassistant.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for Settings screen.
 */
data class SettingsUiState(
    val apiKey: String = "",
    val analysisDepth: Int = Constants.STOCKFISH_DEFAULT_DEPTH,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel for Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val apiKey = settingsRepository.getApiKey()
            val depth = settingsRepository.getAnalysisDepth()

            _uiState.update {
                it.copy(
                    apiKey = apiKey,
                    analysisDepth = depth,
                    isLoading = false
                )
            }
        }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key) }
    }

    fun updateAnalysisDepth(depth: Int) {
        _uiState.update { it.copy(analysisDepth = depth) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.saveApiKey(_uiState.value.apiKey)
            settingsRepository.saveAnalysisDepth(_uiState.value.analysisDepth)

            _uiState.update { it.copy(saveSuccess = true) }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
