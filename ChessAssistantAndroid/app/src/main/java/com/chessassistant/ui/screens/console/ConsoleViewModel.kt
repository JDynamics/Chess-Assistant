package com.chessassistant.ui.screens.console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessassistant.data.model.AnalysisResult
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.repository.ChessRepository
import com.chessassistant.data.repository.SettingsRepository
import com.chessassistant.engine.FenParser
import com.chessassistant.engine.MoveFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Record of a console query.
 */
data class ConsoleQueryRecord(
    val fen: String,
    val result: String,
    val explanation: String?,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * UI state for Console screen.
 */
data class ConsoleUiState(
    val fenInput: String = "",
    val isAnalyzing: Boolean = false,
    val currentResult: String? = null,
    val currentExplanation: String? = null,
    val errorMessage: String? = null,
    val history: List<ConsoleQueryRecord> = emptyList(),
    val hasApiKey: Boolean = false
)

/**
 * ViewModel for FEN Console screen.
 */
@HiltViewModel
class ConsoleViewModel @Inject constructor(
    private val chessRepository: ChessRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsoleUiState())
    val uiState: StateFlow<ConsoleUiState> = _uiState.asStateFlow()

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

    fun updateFenInput(fen: String) {
        _uiState.update { it.copy(fenInput = fen, errorMessage = null) }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(
                fenInput = "",
                currentResult = null,
                currentExplanation = null,
                errorMessage = null
            )
        }
    }

    fun analyzeFen() {
        val fen = _uiState.value.fenInput.trim()

        if (fen.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a FEN position") }
            return
        }

        // Validate FEN format
        if (!fen.contains('/') || fen.count { it == '/' } < 7) {
            _uiState.update { it.copy(errorMessage = "Invalid FEN format") }
            return
        }

        // Try to parse
        val position = FenParser.parse(fen)
        if (position == null) {
            _uiState.update { it.copy(errorMessage = "Could not parse FEN position") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    errorMessage = null,
                    currentResult = null,
                    currentExplanation = null
                )
            }

            // Get best move
            val result = chessRepository.analyzeFen(fen, analysisDepth)

            if (result == null || result.bestMove == null) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Could not calculate best move"
                    )
                }
                return@launch
            }

            // Format the move
            val formattedMove = MoveFormatter.formatMove(result.bestMove, position)
            val san = MoveFormatter.formatSan(result.bestMove, position)

            // Get explanation if API key is available
            var explanation: String? = null
            if (apiKey.isNotBlank()) {
                explanation = chessRepository.explainMove(apiKey, fen, san)
            }

            // Add to history
            val record = ConsoleQueryRecord(
                fen = fen,
                result = formattedMove,
                explanation = explanation
            )

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    currentResult = formattedMove,
                    currentExplanation = explanation,
                    history = listOf(record) + it.history.take(9) // Keep last 10
                )
            }
        }
    }

    fun loadFromHistory(record: ConsoleQueryRecord) {
        _uiState.update {
            it.copy(
                fenInput = record.fen,
                currentResult = record.result,
                currentExplanation = record.explanation,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        chessRepository.shutdown()
    }
}
