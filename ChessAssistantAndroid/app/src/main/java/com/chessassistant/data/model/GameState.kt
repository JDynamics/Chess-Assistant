package com.chessassistant.data.model

/**
 * Represents the full state of a chess game.
 */
data class GameState(
    val position: ChessPosition,
    val moveHistory: List<MoveRecord> = emptyList(),
    val playerColor: PlayerColor = PlayerColor.WHITE,
    val selectedSquare: Square? = null,
    val lastMove: ChessMove? = null,
    val gameResult: GameResult = GameResult.IN_PROGRESS,
    val isAnalyzing: Boolean = false,
    val currentAnalysis: AnalysisResult? = null,
    val bestMove: ChessMove? = null
) {
    val isPlayerTurn: Boolean
        get() = (position.whiteToMove && playerColor == PlayerColor.WHITE) ||
                (!position.whiteToMove && playerColor == PlayerColor.BLACK)

    val currentTurnColor: PieceColor
        get() = if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK
}

/**
 * Player color selection.
 */
enum class PlayerColor {
    WHITE, BLACK;

    fun toPieceColor(): PieceColor = when (this) {
        WHITE -> PieceColor.WHITE
        BLACK -> PieceColor.BLACK
    }
}

/**
 * Possible game results.
 */
enum class GameResult {
    IN_PROGRESS,
    WHITE_WINS_CHECKMATE,
    BLACK_WINS_CHECKMATE,
    DRAW_STALEMATE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_FIFTY_MOVES,
    DRAW_THREEFOLD_REPETITION,
    DRAW_AGREEMENT,
    WHITE_RESIGNS,
    BLACK_RESIGNS
}

/**
 * Record of a move made in the game.
 */
data class MoveRecord(
    val move: ChessMove,
    val san: String,
    val description: String,
    val positionBefore: String,  // FEN before move
    val moveNumber: Int,
    val isWhiteMove: Boolean
)
