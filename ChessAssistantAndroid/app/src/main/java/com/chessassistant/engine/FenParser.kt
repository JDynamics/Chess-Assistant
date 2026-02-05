package com.chessassistant.engine

import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.Piece
import com.chessassistant.data.model.PieceColor
import com.chessassistant.data.model.PieceType
import com.chessassistant.data.model.Square
import com.chessassistant.util.Constants

/**
 * Parses and generates FEN (Forsyth-Edwards Notation) strings.
 */
object FenParser {

    /**
     * Parse a FEN string into a ChessPosition.
     */
    fun parse(fen: String): ChessPosition? {
        return try {
            val parts = fen.trim().split(" ")
            if (parts.size < 4) return null

            val boardPart = parts[0]
            val turnPart = parts[1]
            val castlingPart = parts[2]
            val enPassantPart = parts[3]
            val halfmoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val fullmoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1

            // Parse board
            val board = parseBoardPart(boardPart) ?: return null

            // Parse turn
            val whiteToMove = turnPart.lowercase() == "w"

            // Parse castling rights
            val whiteKingside = castlingPart.contains('K')
            val whiteQueenside = castlingPart.contains('Q')
            val blackKingside = castlingPart.contains('k')
            val blackQueenside = castlingPart.contains('q')

            // Parse en passant square
            val enPassantSquare = if (enPassantPart != "-") {
                parseSquare(enPassantPart)
            } else null

            ChessPosition(
                board = board,
                whiteToMove = whiteToMove,
                whiteKingsideCastle = whiteKingside,
                whiteQueensideCastle = whiteQueenside,
                blackKingsideCastle = blackKingside,
                blackQueensideCastle = blackQueenside,
                enPassantSquare = enPassantSquare,
                halfmoveClock = halfmoveClock,
                fullmoveNumber = fullmoveNumber
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate a FEN string from a ChessPosition.
     */
    fun toFen(position: ChessPosition): String {
        val boardPart = toBoardPart(position.board)
        val turnPart = if (position.whiteToMove) "w" else "b"

        val castlingPart = buildString {
            if (position.whiteKingsideCastle) append('K')
            if (position.whiteQueensideCastle) append('Q')
            if (position.blackKingsideCastle) append('k')
            if (position.blackQueensideCastle) append('q')
            if (isEmpty()) append('-')
        }

        val enPassantPart = position.enPassantSquare?.toAlgebraic() ?: "-"

        return "$boardPart $turnPart $castlingPart $enPassantPart ${position.halfmoveClock} ${position.fullmoveNumber}"
    }

    private fun parseBoardPart(boardPart: String): Array<Array<Piece?>>? {
        val board = Array(8) { arrayOfNulls<Piece?>(8) }
        val ranks = boardPart.split("/")

        if (ranks.size != 8) return null

        for ((rankIndex, rankStr) in ranks.withIndex()) {
            var fileIndex = 0
            for (char in rankStr) {
                when {
                    char.isDigit() -> {
                        val emptySquares = char.digitToInt()
                        fileIndex += emptySquares
                    }
                    char.isLetter() -> {
                        val piece = charToPiece(char)
                        if (piece != null && fileIndex < 8) {
                            // FEN ranks are from 8 to 1 (top to bottom)
                            board[rankIndex][fileIndex] = piece
                        }
                        fileIndex++
                    }
                }
            }
            if (fileIndex != 8) return null
        }

        return board
    }

    private fun toBoardPart(board: Array<Array<Piece?>>): String {
        return (0 until 8).joinToString("/") { rank ->
            var rankStr = ""
            var emptyCount = 0

            for (file in 0 until 8) {
                val piece = board[rank][file]
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        rankStr += emptyCount.toString()
                        emptyCount = 0
                    }
                    rankStr += pieceToChar(piece)
                }
            }

            if (emptyCount > 0) {
                rankStr += emptyCount.toString()
            }

            rankStr
        }
    }

    private fun charToPiece(char: Char): Piece? {
        val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
        val type = when (char.lowercaseChar()) {
            'p' -> PieceType.PAWN
            'n' -> PieceType.KNIGHT
            'b' -> PieceType.BISHOP
            'r' -> PieceType.ROOK
            'q' -> PieceType.QUEEN
            'k' -> PieceType.KING
            else -> return null
        }
        return Piece(type, color)
    }

    private fun pieceToChar(piece: Piece): Char {
        val char = when (piece.type) {
            PieceType.PAWN -> 'p'
            PieceType.KNIGHT -> 'n'
            PieceType.BISHOP -> 'b'
            PieceType.ROOK -> 'r'
            PieceType.QUEEN -> 'q'
            PieceType.KING -> 'k'
        }
        return if (piece.color == PieceColor.WHITE) char.uppercaseChar() else char
    }

    private fun parseSquare(algebraic: String): Square? {
        if (algebraic.length != 2) return null
        val file = algebraic[0].lowercaseChar() - 'a'
        val rank = algebraic[1].digitToInt() - 1
        if (file !in 0..7 || rank !in 0..7) return null
        return Square(file, rank)
    }

    /**
     * Validate a FEN string.
     */
    fun isValidFen(fen: String): Boolean {
        return parse(fen) != null
    }

    /**
     * Get the starting position FEN.
     */
    fun startingPosition(): ChessPosition {
        return parse(Constants.STARTING_FEN)!!
    }
}
