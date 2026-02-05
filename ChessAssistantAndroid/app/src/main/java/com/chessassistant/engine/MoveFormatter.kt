package com.chessassistant.engine

import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.PieceType
import com.chessassistant.util.Constants

/**
 * Formats chess moves into human-readable strings.
 * Output format matches Python app: Piece(StartPos) to (EndPos)
 */
object MoveFormatter {

    private val PIECE_NAMES = mapOf(
        PieceType.PAWN to "Pawn",
        PieceType.KNIGHT to "Knight",
        PieceType.BISHOP to "Bishop",
        PieceType.ROOK to "Rook",
        PieceType.QUEEN to "Queen",
        PieceType.KING to "King"
    )

    /**
     * Format a move in human-readable format: Piece(StartPos) to (EndPos)
     * Example: Knight(g1) to (f3)
     */
    fun formatMove(move: ChessMove, position: ChessPosition): String {
        val piece = position.getPieceAt(move.from)
        val pieceName = piece?.let { PIECE_NAMES[it.type] } ?: "Piece"
        val fromSquare = move.from.toAlgebraic()
        val toSquare = move.to.toAlgebraic()

        // Handle castling
        if (move.isCastleKingside) {
            return "King($fromSquare) to ($toSquare) [Kingside Castle]"
        }
        if (move.isCastleQueenside) {
            return "King($fromSquare) to ($toSquare) [Queenside Castle]"
        }

        // Handle promotion
        val promotionText = move.promotion?.let { promoType ->
            " (promotes to ${PIECE_NAMES[promoType]})"
        } ?: ""

        return "$pieceName($fromSquare) to ($toSquare)$promotionText"
    }

    /**
     * Format move with additional info (captures, check).
     */
    fun formatMoveVerbose(move: ChessMove, position: ChessPosition): String {
        val baseFormat = formatMove(move, position)
        val extras = mutableListOf<String>()

        // Check for capture
        val captured = position.getPieceAt(move.to)
        if (captured != null) {
            extras.add("captures ${PIECE_NAMES[captured.type]}")
        }

        // En passant capture
        if (move.isEnPassant) {
            extras.add("captures en passant")
        }

        // Check if move results in check or checkmate
        // (This would require making the move and checking the resulting position)

        return if (extras.isNotEmpty()) {
            "$baseFormat (${extras.joinToString(", ")})"
        } else {
            baseFormat
        }
    }

    /**
     * Format move in Standard Algebraic Notation (SAN).
     * Example: Nf3, e4, O-O
     */
    fun formatSan(move: ChessMove, position: ChessPosition): String {
        // Castling
        if (move.isCastleKingside) return "O-O"
        if (move.isCastleQueenside) return "O-O-O"

        val piece = position.getPieceAt(move.from) ?: return move.toUci()

        val pieceChar = when (piece.type) {
            PieceType.PAWN -> ""
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }

        val isCapture = position.getPieceAt(move.to) != null || move.isEnPassant
        val captureChar = if (isCapture) "x" else ""

        // For pawn captures, include the file
        val fromFile = if (piece.type == PieceType.PAWN && isCapture) {
            move.from.toAlgebraic()[0].toString()
        } else ""

        val toSquare = move.to.toAlgebraic()

        // Promotion
        val promoChar = move.promotion?.let {
            "=" + when (it) {
                PieceType.QUEEN -> "Q"
                PieceType.ROOK -> "R"
                PieceType.BISHOP -> "B"
                PieceType.KNIGHT -> "N"
                else -> ""
            }
        } ?: ""

        return "$pieceChar$fromFile$captureChar$toSquare$promoChar"
    }

    /**
     * Format move for descriptive notation (used in move history).
     * Example: "Pawn e2 → e4"
     */
    fun formatDescriptive(move: ChessMove, position: ChessPosition): String {
        val piece = position.getPieceAt(move.from)
        val pieceName = piece?.let { PIECE_NAMES[it.type] } ?: "Piece"
        val fromSquare = move.from.toAlgebraic()
        val toSquare = move.to.toAlgebraic()

        // Castling
        if (move.isCastleKingside) return "Castle Kingside (O-O)"
        if (move.isCastleQueenside) return "Castle Queenside (O-O-O)"

        val captured = position.getPieceAt(move.to)
        val captureText = if (captured != null) {
            " captures ${PIECE_NAMES[captured.type]}"
        } else if (move.isEnPassant) {
            " captures en passant"
        } else ""

        val promoText = move.promotion?.let { " promotes to ${PIECE_NAMES[it]}" } ?: ""

        return "$pieceName $fromSquare → $toSquare$captureText$promoText"
    }

    /**
     * Parse a UCI move string (e.g., "e2e4", "e7e8q").
     */
    fun parseUci(uci: String): ChessMove? {
        if (uci.length < 4 || uci.length > 5) return null

        val fromFile = uci[0] - 'a'
        val fromRank = uci[1].digitToInt() - 1
        val toFile = uci[2] - 'a'
        val toRank = uci[3].digitToInt() - 1

        if (fromFile !in 0..7 || fromRank !in 0..7 ||
            toFile !in 0..7 || toRank !in 0..7) {
            return null
        }

        val promotion = if (uci.length == 5) {
            when (uci[4].lowercaseChar()) {
                'q' -> PieceType.QUEEN
                'r' -> PieceType.ROOK
                'b' -> PieceType.BISHOP
                'n' -> PieceType.KNIGHT
                else -> null
            }
        } else null

        return ChessMove(
            from = com.chessassistant.data.model.Square(fromFile, fromRank),
            to = com.chessassistant.data.model.Square(toFile, toRank),
            promotion = promotion
        )
    }
}
