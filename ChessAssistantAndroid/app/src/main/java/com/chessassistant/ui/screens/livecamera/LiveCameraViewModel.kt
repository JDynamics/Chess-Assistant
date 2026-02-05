package com.chessassistant.ui.screens.livecamera

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
 * UI state for Live Camera screen.
 */
data class LiveCameraUiState(
    val isAnalyzing: Boolean = false,
    val playingAs: PlayerColor = PlayerColor.WHITE,
    val lastResult: ImageAnalysisResult? = null,
    val errorMessage: String? = null,
    val autoAnalyzeEnabled: Boolean = false,
    val flashEnabled: Boolean = false,
    val useFrontCamera: Boolean = false,
    val hasApiKey: Boolean = false
)

/**
 * ViewModel for Live Camera screen.
 */
@HiltViewModel
class LiveCameraViewModel @Inject constructor(
    private val chessRepository: ChessRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveCameraUiState())
    val uiState: StateFlow<LiveCameraUiState> = _uiState.asStateFlow()

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

    fun setPlayingAs(color: PlayerColor) {
        _uiState.update { it.copy(playingAs = color) }
    }

    fun toggleAutoAnalyze() {
        _uiState.update { it.copy(autoAnalyzeEnabled = !it.autoAnalyzeEnabled) }
    }

    fun toggleFlash() {
        _uiState.update { it.copy(flashEnabled = !it.flashEnabled) }
    }

    fun switchCamera() {
        _uiState.update { it.copy(useFrontCamera = !it.useFrontCamera) }
    }

    /**
     * Analyze a frame from the camera (used by auto-analyze).
     */
    fun analyzeFrame(bitmap: Bitmap) {
        if (_uiState.value.isAnalyzing) return

        viewModelScope.launch {
            performAnalysis(bitmap)
        }
    }

    /**
     * Capture current frame and analyze (manual button).
     */
    fun captureAndAnalyze(bitmap: Bitmap) {
        viewModelScope.launch {
            performAnalysis(bitmap)
        }
    }

    private suspend fun performAnalysis(bitmap: Bitmap) {
        if (apiKey.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "API key not configured. Go to Settings to add your Claude API key.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isAnalyzing = true,
                errorMessage = null
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
                lastResult = if (result.success) result else null,
                errorMessage = if (!result.success) result.errorMessage else null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        chessRepository.shutdown()
    }
}
