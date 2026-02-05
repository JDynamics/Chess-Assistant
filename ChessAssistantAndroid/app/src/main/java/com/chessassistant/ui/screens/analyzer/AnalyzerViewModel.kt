package com.chessassistant.ui.screens.analyzer

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessassistant.data.model.PlayerColor
import com.chessassistant.data.repository.ChessRepository
import com.chessassistant.data.repository.ImageAnalysisResult
import com.chessassistant.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for Analyzer screen.
 */
data class AnalyzerUiState(
    val selectedImage: Bitmap? = null,
    val playingAs: PlayerColor = PlayerColor.WHITE,
    val isAnalyzing: Boolean = false,
    val analysisResult: ImageAnalysisResult? = null,
    val errorMessage: String? = null,
    val hasApiKey: Boolean = false
)

/**
 * ViewModel for Screenshot Analyzer screen.
 */
@HiltViewModel
class AnalyzerViewModel @Inject constructor(
    private val chessRepository: ChessRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyzerUiState())
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    private var apiKey = ""
    private var analysisDepth = 18

    init {
        viewModelScope.launch {
            apiKey = settingsRepository.getApiKey()
            analysisDepth = settingsRepository.getAnalysisDepth()
            chessRepository.initializeEngine()

            _uiState.update { it.copy(hasApiKey = apiKey.isNotBlank()) }
        }
    }

    fun setImage(bitmap: Bitmap?) {
        _uiState.update {
            it.copy(
                selectedImage = bitmap,
                analysisResult = null,
                errorMessage = null
            )
        }
    }

    fun setPlayingAs(color: PlayerColor) {
        _uiState.update { it.copy(playingAs = color) }
    }

    fun analyzeImage() {
        val bitmap = _uiState.value.selectedImage
        if (bitmap == null) {
            _uiState.update { it.copy(errorMessage = "Please capture or select an image first") }
            return
        }

        if (apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "API key not configured. Please set it in Settings.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    errorMessage = null,
                    analysisResult = null
                )
            }

            val playingAsWhite = _uiState.value.playingAs == PlayerColor.WHITE

            val result = chessRepository.analyzeImage(
                bitmap = bitmap,
                apiKey = apiKey,
                playingAsWhite = playingAsWhite,
                depth = analysisDepth
            )

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analysisResult = result,
                    errorMessage = if (!result.success) result.errorMessage else null
                )
            }
        }
    }

    fun clearResult() {
        _uiState.update {
            it.copy(
                analysisResult = null,
                errorMessage = null
            )
        }
    }

    fun clearImage() {
        _uiState.update {
            it.copy(
                selectedImage = null,
                analysisResult = null,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        chessRepository.shutdown()
    }
}
