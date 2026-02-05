package com.chessassistant.data.model

/**
 * Result from Stockfish analysis.
 */
data class AnalysisResult(
    val bestMove: ChessMove?,
    val bestMoveUci: String?,
    val score: Int?,           // Centipawns (positive = white advantage)
    val isMate: Boolean = false,
    val mateIn: Int? = null,   // Moves until mate (positive = white mates)
    val principalVariation: List<String> = emptyList(),  // PV in UCI format
    val depth: Int = 0
) {
    /**
     * Get a human-readable evaluation string.
     */
    fun getEvaluationText(playingAsWhite: Boolean): String {
        if (isMate && mateIn != null) {
            val playerMates = (mateIn > 0) == playingAsWhite
            return if (playerMates) {
                "Mate in ${kotlin.math.abs(mateIn)}!"
            } else {
                "Opponent mates in ${kotlin.math.abs(mateIn)}"
            }
        }

        val centipawns = score ?: return "N/A"
        val adjustedScore = if (playingAsWhite) centipawns else -centipawns
        val pawns = adjustedScore / 100.0

        return when {
            pawns > 0.5 -> "You're ahead +${String.format("%.1f", pawns)}"
            pawns < -0.5 -> "You're behind ${String.format("%.1f", pawns)}"
            else -> "≈ Equal position"
        }
    }
}

/**
 * Result from getting the best move.
 */
data class BestMoveResult(
    val move: ChessMove,
    val moveUci: String,
    val score: Int?,
    val depth: Int
)

/**
 * Result from vision analysis (Claude API).
 */
data class VisionAnalysisResult(
    val fen: String?,
    val rawResponse: String,
    val success: Boolean,
    val errorMessage: String? = null
)
