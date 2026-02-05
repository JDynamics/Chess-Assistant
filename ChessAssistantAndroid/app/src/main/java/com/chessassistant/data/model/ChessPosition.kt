package com.chessassistant.data.model

/**
 * Represents a complete chess position.
 */
data class ChessPosition(
    val board: Array<Array<Piece?>>,
    val whiteToMove: Boolean = true,
    val whiteKingsideCastle: Boolean = true,
    val whiteQueensideCastle: Boolean = true,
    val blackKingsideCastle: Boolean = true,
    val blackQueensideCastle: Boolean = true,
    val enPassantSquare: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1
) {
    /**
     * Get the piece at a square.
     * Note: Square uses chess coordinates (file 0-7 = a-h, rank 0-7 = 1-8)
     * Board array uses display coordinates (rank 0 = rank 8, rank 7 = rank 1)
     */
    fun getPieceAt(square: Square): Piece? {
        if (!square.isValid()) return null
        val boardRank = 7 - square.rank
        return board[boardRank][square.file]
    }

    /**
     * Create a copy of the board array.
     */
    fun copyBoard(): Array<Array<Piece?>> {
        return board.map { it.copyOf() }.toTypedArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChessPosition

        if (!board.contentDeepEquals(other.board)) return false
        if (whiteToMove != other.whiteToMove) return false
        if (whiteKingsideCastle != other.whiteKingsideCastle) return false
        if (whiteQueensideCastle != other.whiteQueensideCastle) return false
        if (blackKingsideCastle != other.blackKingsideCastle) return false
        if (blackQueensideCastle != other.blackQueensideCastle) return false
        if (enPassantSquare != other.enPassantSquare) return false
        if (halfmoveClock != other.halfmoveClock) return false
        if (fullmoveNumber != other.fullmoveNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + whiteToMove.hashCode()
        result = 31 * result + whiteKingsideCastle.hashCode()
        result = 31 * result + whiteQueensideCastle.hashCode()
        result = 31 * result + blackKingsideCastle.hashCode()
        result = 31 * result + blackQueensideCastle.hashCode()
        result = 31 * result + (enPassantSquare?.hashCode() ?: 0)
        result = 31 * result + halfmoveClock
        result = 31 * result + fullmoveNumber
        return result
    }

    companion object {
        /**
         * Create an empty board.
         */
        fun emptyBoard(): Array<Array<Piece?>> {
            return Array(8) { arrayOfNulls<Piece?>(8) }
        }
    }
}
