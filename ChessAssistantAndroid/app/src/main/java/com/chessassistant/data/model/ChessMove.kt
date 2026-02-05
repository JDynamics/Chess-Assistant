package com.chessassistant.data.model

/**
 * Represents a chess move.
 */
data class ChessMove(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCastleKingside: Boolean = false,
    val isCastleQueenside: Boolean = false,
    val isEnPassant: Boolean = false
) {
    /**
     * Convert to UCI notation (e.g., "e2e4", "e7e8q").
     */
    fun toUci(): String {
        val promoChar = promotion?.let {
            when (it) {
                PieceType.QUEEN -> "q"
                PieceType.ROOK -> "r"
                PieceType.BISHOP -> "b"
                PieceType.KNIGHT -> "n"
                else -> ""
            }
        } ?: ""
        return "${from.toAlgebraic()}${to.toAlgebraic()}$promoChar"
    }
}

/**
 * Represents a square on the chess board.
 */
data class Square(
    val file: Int,  // 0-7 (a-h)
    val rank: Int   // 0-7 (1-8)
) {
    /**
     * Convert to algebraic notation (e.g., "e4").
     */
    fun toAlgebraic(): String {
        val fileChar = ('a' + file)
        val rankChar = ('1' + rank)
        return "$fileChar$rankChar"
    }

    /**
     * Check if the square is valid.
     */
    fun isValid(): Boolean = file in 0..7 && rank in 0..7

    companion object {
        /**
         * Parse from algebraic notation.
         */
        fun fromAlgebraic(algebraic: String): Square? {
            if (algebraic.length != 2) return null
            val file = algebraic[0].lowercaseChar() - 'a'
            val rank = algebraic[1].digitToInt() - 1
            if (file !in 0..7 || rank !in 0..7) return null
            return Square(file, rank)
        }
    }
}

/**
 * Chess piece types.
 */
enum class PieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
}

/**
 * Chess piece colors.
 */
enum class PieceColor {
    WHITE, BLACK
}

/**
 * Represents a chess piece.
 */
data class Piece(
    val type: PieceType,
    val color: PieceColor
)
