package com.chessassistant.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chessassistant.data.model.AnalysisResult
import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.GameResult
import com.chessassistant.data.model.GameState
import com.chessassistant.data.model.MoveRecord
import com.chessassistant.data.model.PieceColor
import com.chessassistant.data.model.PieceType
import com.chessassistant.data.model.PlayerColor
import com.chessassistant.data.model.Square
import com.chessassistant.data.repository.ChessRepository
import com.chessassistant.data.repository.GameStorageRepository
import com.chessassistant.data.repository.SettingsRepository
import com.chessassistant.engine.ChessLogic
import com.chessassistant.engine.FenParser
import com.chessassistant.engine.MoveFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the chess game screen.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val chessRepository: ChessRepository,
    private val settingsRepository: SettingsRepository,
    private val gameStorageRepository: GameStorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var analysisDepth = 18

    init {
        viewModelScope.launch {
            analysisDepth = settingsRepository.getAnalysisDepth()
            chessRepository.initializeEngine()
        }
    }

    fun selectColor(color: PlayerColor) {
        val startPosition = chessRepository.getStartingPosition()
        _uiState.update {
            it.copy(
                gameState = GameState(
                    position = startPosition,
                    playerColor = color,
                    moveHistory = emptyList()
                ),
                showColorDialog = false,
                boardFlipped = color == PlayerColor.BLACK
            )
        }
        triggerAnalysis()
    }

    fun onSquareClick(square: Square) {
        val state = _uiState.value.gameState ?: return
        val position = state.position

        if (state.gameResult != GameResult.IN_PROGRESS) return

        val currentSelectedSquare = state.selectedSquare
        val piece = position.getPieceAt(square)
        val currentTurnColor = if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK

        if (currentSelectedSquare == null) {
            // No piece selected - try to select
            if (piece != null && piece.color == currentTurnColor) {
                _uiState.update {
                    it.copy(
                        gameState = state.copy(selectedSquare = square),
                        statusMessage = "Selected ${square.toAlgebraic()} - tap destination"
                    )
                }
            }
        } else {
            // Piece already selected - try to move or reselect
            val legalMoves = ChessLogic.getLegalMoves(position)
                .filter { it.from == currentSelectedSquare }

            // Check if clicking a legal destination
            val move = legalMoves.find { it.to == square }

            if (move != null) {
                // Handle pawn promotion
                if (position.getPieceAt(currentSelectedSquare)?.type == PieceType.PAWN &&
                    (square.rank == 0 || square.rank == 7)) {
                    // Show promotion dialog
                    _uiState.update {
                        it.copy(
                            pendingPromotionMove = move,
                            showPromotionDialog = true
                        )
                    }
                } else {
                    makeMove(move)
                }
            } else if (piece != null && piece.color == currentTurnColor) {
                // Reselect different piece
                _uiState.update {
                    it.copy(
                        gameState = state.copy(selectedSquare = square),
                        statusMessage = "Selected ${square.toAlgebraic()} - tap destination"
                    )
                }
            } else {
                // Invalid move - deselect
                _uiState.update {
                    it.copy(
                        gameState = state.copy(selectedSquare = null),
                        statusMessage = "Invalid move"
                    )
                }
            }
        }
    }

    fun selectPromotion(pieceType: PieceType) {
        val pendingMove = _uiState.value.pendingPromotionMove ?: return
        val promotionMove = pendingMove.copy(promotion = pieceType)

        _uiState.update {
            it.copy(
                showPromotionDialog = false,
                pendingPromotionMove = null
            )
        }

        makeMove(promotionMove)
    }

    fun dismissPromotionDialog() {
        _uiState.update {
            it.copy(
                showPromotionDialog = false,
                pendingPromotionMove = null
            )
        }
    }

    private fun makeMove(move: ChessMove) {
        val state = _uiState.value.gameState ?: return
        val position = state.position

        // Format move before making it
        val san = MoveFormatter.formatSan(move, position)
        val description = MoveFormatter.formatDescriptive(move, position)
        val fenBefore = FenParser.toFen(position)

        // Make the move
        val newPosition = ChessLogic.makeMove(position, move)

        // Create move record
        val moveRecord = MoveRecord(
            move = move,
            san = san,
            description = description,
            positionBefore = fenBefore,
            moveNumber = (state.moveHistory.size / 2) + 1,
            isWhiteMove = position.whiteToMove
        )

        // Check game result
        val gameResult = when {
            ChessLogic.isCheckmate(newPosition) -> {
                if (newPosition.whiteToMove) GameResult.BLACK_WINS_CHECKMATE
                else GameResult.WHITE_WINS_CHECKMATE
            }
            ChessLogic.isStalemate(newPosition) -> GameResult.DRAW_STALEMATE
            ChessLogic.isInsufficientMaterial(newPosition) -> GameResult.DRAW_INSUFFICIENT_MATERIAL
            else -> GameResult.IN_PROGRESS
        }

        val statusMessage = when {
            gameResult == GameResult.WHITE_WINS_CHECKMATE ||
            gameResult == GameResult.BLACK_WINS_CHECKMATE -> "Checkmate!"
            gameResult == GameResult.DRAW_STALEMATE -> "Stalemate - Draw!"
            ChessLogic.isInCheck(newPosition, if (newPosition.whiteToMove) PieceColor.WHITE else PieceColor.BLACK) -> "Check!"
            else -> "Played: $description"
        }

        _uiState.update {
            it.copy(
                gameState = state.copy(
                    position = newPosition,
                    selectedSquare = null,
                    lastMove = move,
                    moveHistory = state.moveHistory + moveRecord,
                    gameResult = gameResult
                ),
                statusMessage = statusMessage
            )
        }

        if (gameResult == GameResult.IN_PROGRESS) {
            triggerAnalysis()
        }
    }

    fun playBestMove() {
        val state = _uiState.value.gameState ?: return
        val bestMove = state.bestMove

        if (bestMove != null && state.isPlayerTurn) {
            makeMove(bestMove)
        }
    }

    fun undoMove() {
        val state = _uiState.value.gameState ?: return
        if (state.moveHistory.isEmpty()) return

        // Rebuild position from move history minus last move
        var position = chessRepository.getStartingPosition()
        val newHistory = state.moveHistory.dropLast(1)

        for (record in newHistory) {
            position = ChessLogic.makeMove(position, record.move)
        }

        val lastMove = newHistory.lastOrNull()?.move

        _uiState.update {
            it.copy(
                gameState = state.copy(
                    position = position,
                    selectedSquare = null,
                    lastMove = lastMove,
                    moveHistory = newHistory,
                    gameResult = GameResult.IN_PROGRESS
                ),
                statusMessage = "Move undone"
            )
        }

        triggerAnalysis()
    }

    fun newGame() {
        _uiState.update {
            it.copy(
                gameState = null,
                showColorDialog = true,
                statusMessage = "Choose your color"
            )
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            val state = _uiState.value.gameState ?: return@launch
            try {
                val file = gameStorageRepository.saveGame(state)
                _uiState.update { it.copy(statusMessage = "Game saved: ${file.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Failed to save game") }
            }
        }
    }

    private fun triggerAnalysis() {
        val state = _uiState.value.gameState ?: return

        _uiState.update {
            it.copy(gameState = state.copy(isAnalyzing = true))
        }

        viewModelScope.launch {
            val result = chessRepository.analyzePosition(state.position, analysisDepth)

            _uiState.update { currentState ->
                val currentGameState = currentState.gameState ?: return@update currentState
                currentState.copy(
                    gameState = currentGameState.copy(
                        isAnalyzing = false,
                        currentAnalysis = result,
                        bestMove = result?.bestMove
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chessRepository.shutdown()
    }
}

/**
 * UI state for the game screen.
 */
data class GameUiState(
    val gameState: GameState? = null,
    val showColorDialog: Boolean = true,
    val showPromotionDialog: Boolean = false,
    val pendingPromotionMove: ChessMove? = null,
    val boardFlipped: Boolean = false,
    val statusMessage: String = "Choose your color"
)
