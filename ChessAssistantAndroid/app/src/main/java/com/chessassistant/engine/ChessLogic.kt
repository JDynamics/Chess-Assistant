package com.chessassistant.engine

import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.Piece
import com.chessassistant.data.model.PieceColor
import com.chessassistant.data.model.PieceType
import com.chessassistant.data.model.Square

/**
 * Chess game logic: legal move generation, check detection, game state.
 */
object ChessLogic {

    /**
     * Get all legal moves for the current position.
     */
    fun getLegalMoves(position: ChessPosition): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()
        val color = if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK

        for (rank in 0 until 8) {
            for (file in 0 until 8) {
                val piece = position.board[rank][file]
                if (piece != null && piece.color == color) {
                    val square = Square(file, 7 - rank) // Convert board index to chess rank
                    moves.addAll(getPieceMoves(position, square, piece))
                }
            }
        }

        // Filter out moves that leave the king in check
        return moves.filter { move ->
            val newPosition = makeMove(position, move)
            !isInCheck(newPosition, color)
        }
    }

    /**
     * Get pseudo-legal moves for a piece (doesn't check for leaving king in check).
     */
    private fun getPieceMoves(position: ChessPosition, from: Square, piece: Piece): List<ChessMove> {
        return when (piece.type) {
            PieceType.PAWN -> getPawnMoves(position, from, piece.color)
            PieceType.KNIGHT -> getKnightMoves(position, from, piece.color)
            PieceType.BISHOP -> getBishopMoves(position, from, piece.color)
            PieceType.ROOK -> getRookMoves(position, from, piece.color)
            PieceType.QUEEN -> getQueenMoves(position, from, piece.color)
            PieceType.KING -> getKingMoves(position, from, piece.color)
        }
    }

    private fun getPawnMoves(position: ChessPosition, from: Square, color: PieceColor): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()
        val direction = if (color == PieceColor.WHITE) 1 else -1
        val startRank = if (color == PieceColor.WHITE) 1 else 6
        val promotionRank = if (color == PieceColor.WHITE) 7 else 0

        // Single push
        val oneForward = Square(from.file, from.rank + direction)
        if (oneForward.isValid() && position.getPieceAt(oneForward) == null) {
            if (oneForward.rank == promotionRank) {
                // Promotion
                for (promoType in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                    moves.add(ChessMove(from, oneForward, promotion = promoType))
                }
            } else {
                moves.add(ChessMove(from, oneForward))
            }

            // Double push from starting position
            if (from.rank == startRank) {
                val twoForward = Square(from.file, from.rank + 2 * direction)
                if (position.getPieceAt(twoForward) == null) {
                    moves.add(ChessMove(from, twoForward))
                }
            }
        }

        // Captures
        for (fileDelta in listOf(-1, 1)) {
            val captureSquare = Square(from.file + fileDelta, from.rank + direction)
            if (captureSquare.isValid()) {
                val target = position.getPieceAt(captureSquare)
                if (target != null && target.color != color) {
                    if (captureSquare.rank == promotionRank) {
                        for (promoType in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                            moves.add(ChessMove(from, captureSquare, promotion = promoType))
                        }
                    } else {
                        moves.add(ChessMove(from, captureSquare))
                    }
                }

                // En passant
                if (position.enPassantSquare == captureSquare) {
                    moves.add(ChessMove(from, captureSquare, isEnPassant = true))
                }
            }
        }

        return moves
    }

    private fun getKnightMoves(position: ChessPosition, from: Square, color: PieceColor): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()
        val deltas = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )

        for ((df, dr) in deltas) {
            val to = Square(from.file + df, from.rank + dr)
            if (to.isValid()) {
                val target = position.getPieceAt(to)
                if (target == null || target.color != color) {
                    moves.add(ChessMove(from, to))
                }
            }
        }

        return moves
    }

    private fun getSlidingMoves(
        position: ChessPosition,
        from: Square,
        color: PieceColor,
        directions: List<Pair<Int, Int>>
    ): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()

        for ((df, dr) in directions) {
            var to = Square(from.file + df, from.rank + dr)
            while (to.isValid()) {
                val target = position.getPieceAt(to)
                if (target == null) {
                    moves.add(ChessMove(from, to))
                } else {
                    if (target.color != color) {
                        moves.add(ChessMove(from, to))
                    }
                    break
                }
                to = Square(to.file + df, to.rank + dr)
            }
        }

        return moves
    }

    private fun getBishopMoves(position: ChessPosition, from: Square, color: PieceColor): List<ChessMove> {
        val diagonals = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        return getSlidingMoves(position, from, color, diagonals)
    }

    private fun getRookMoves(position: ChessPosition, from: Square, color: PieceColor): List<ChessMove> {
        val orthogonals = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
        return getSlidingMoves(position, from, color, orthogonals)
    }

    private fun getQueenMoves(position: ChessPosition, from: Square, color: PieceColor): List<ChessMove> {
        return getBishopMoves(position, from, color) + getRookMoves(position, from, color)
    }

    private fun getKingMoves(position: ChessPosition, from: Square, color: PieceColor): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()

        // Regular king moves
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val to = Square(from.file + df, from.rank + dr)
                if (to.isValid()) {
                    val target = position.getPieceAt(to)
                    if (target == null || target.color != color) {
                        moves.add(ChessMove(from, to))
                    }
                }
            }
        }

        // Castling
        if (!isInCheck(position, color)) {
            if (color == PieceColor.WHITE && from == Square(4, 0)) {
                // Kingside
                if (position.whiteKingsideCastle &&
                    position.getPieceAt(Square(5, 0)) == null &&
                    position.getPieceAt(Square(6, 0)) == null &&
                    !isSquareAttacked(position, Square(5, 0), PieceColor.BLACK) &&
                    !isSquareAttacked(position, Square(6, 0), PieceColor.BLACK)) {
                    moves.add(ChessMove(from, Square(6, 0), isCastleKingside = true))
                }
                // Queenside
                if (position.whiteQueensideCastle &&
                    position.getPieceAt(Square(3, 0)) == null &&
                    position.getPieceAt(Square(2, 0)) == null &&
                    position.getPieceAt(Square(1, 0)) == null &&
                    !isSquareAttacked(position, Square(3, 0), PieceColor.BLACK) &&
                    !isSquareAttacked(position, Square(2, 0), PieceColor.BLACK)) {
                    moves.add(ChessMove(from, Square(2, 0), isCastleQueenside = true))
                }
            } else if (color == PieceColor.BLACK && from == Square(4, 7)) {
                // Kingside
                if (position.blackKingsideCastle &&
                    position.getPieceAt(Square(5, 7)) == null &&
                    position.getPieceAt(Square(6, 7)) == null &&
                    !isSquareAttacked(position, Square(5, 7), PieceColor.WHITE) &&
                    !isSquareAttacked(position, Square(6, 7), PieceColor.WHITE)) {
                    moves.add(ChessMove(from, Square(6, 7), isCastleKingside = true))
                }
                // Queenside
                if (position.blackQueensideCastle &&
                    position.getPieceAt(Square(3, 7)) == null &&
                    position.getPieceAt(Square(2, 7)) == null &&
                    position.getPieceAt(Square(1, 7)) == null &&
                    !isSquareAttacked(position, Square(3, 7), PieceColor.WHITE) &&
                    !isSquareAttacked(position, Square(2, 7), PieceColor.WHITE)) {
                    moves.add(ChessMove(from, Square(2, 7), isCastleQueenside = true))
                }
            }
        }

        return moves
    }

    /**
     * Check if a square is attacked by the given color.
     */
    fun isSquareAttacked(position: ChessPosition, square: Square, byColor: PieceColor): Boolean {
        // Check pawn attacks
        val pawnDirection = if (byColor == PieceColor.WHITE) -1 else 1
        for (fileDelta in listOf(-1, 1)) {
            val pawnSquare = Square(square.file + fileDelta, square.rank + pawnDirection)
            if (pawnSquare.isValid()) {
                val piece = position.getPieceAt(pawnSquare)
                if (piece?.type == PieceType.PAWN && piece.color == byColor) {
                    return true
                }
            }
        }

        // Check knight attacks
        val knightDeltas = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for ((df, dr) in knightDeltas) {
            val knightSquare = Square(square.file + df, square.rank + dr)
            if (knightSquare.isValid()) {
                val piece = position.getPieceAt(knightSquare)
                if (piece?.type == PieceType.KNIGHT && piece.color == byColor) {
                    return true
                }
            }
        }

        // Check sliding piece attacks (bishop, rook, queen)
        val diagonals = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
        val orthogonals = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        for ((df, dr) in diagonals) {
            var checkSquare = Square(square.file + df, square.rank + dr)
            while (checkSquare.isValid()) {
                val piece = position.getPieceAt(checkSquare)
                if (piece != null) {
                    if (piece.color == byColor &&
                        (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                checkSquare = Square(checkSquare.file + df, checkSquare.rank + dr)
            }
        }

        for ((df, dr) in orthogonals) {
            var checkSquare = Square(square.file + df, square.rank + dr)
            while (checkSquare.isValid()) {
                val piece = position.getPieceAt(checkSquare)
                if (piece != null) {
                    if (piece.color == byColor &&
                        (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                checkSquare = Square(checkSquare.file + df, checkSquare.rank + dr)
            }
        }

        // Check king attacks
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val kingSquare = Square(square.file + df, square.rank + dr)
                if (kingSquare.isValid()) {
                    val piece = position.getPieceAt(kingSquare)
                    if (piece?.type == PieceType.KING && piece.color == byColor) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * Check if the given color's king is in check.
     */
    fun isInCheck(position: ChessPosition, color: PieceColor): Boolean {
        val kingSquare = findKing(position, color) ?: return false
        val attackingColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        return isSquareAttacked(position, kingSquare, attackingColor)
    }

    /**
     * Find the king's position for the given color.
     */
    fun findKing(position: ChessPosition, color: PieceColor): Square? {
        for (rank in 0 until 8) {
            for (file in 0 until 8) {
                val piece = position.board[rank][file]
                if (piece?.type == PieceType.KING && piece.color == color) {
                    return Square(file, 7 - rank)
                }
            }
        }
        return null
    }

    /**
     * Make a move and return the new position.
     */
    fun makeMove(position: ChessPosition, move: ChessMove): ChessPosition {
        val newBoard = position.board.map { it.copyOf() }.toTypedArray()
        val piece = position.getPieceAt(move.from) ?: return position

        // Remove piece from source
        val fromBoardRank = 7 - move.from.rank
        val toBoardRank = 7 - move.to.rank
        newBoard[fromBoardRank][move.from.file] = null

        // Handle special moves
        when {
            move.isCastleKingside -> {
                // Move king
                newBoard[toBoardRank][move.to.file] = piece
                // Move rook
                val rookRank = if (piece.color == PieceColor.WHITE) 7 else 0
                newBoard[rookRank][5] = newBoard[rookRank][7]
                newBoard[rookRank][7] = null
            }
            move.isCastleQueenside -> {
                // Move king
                newBoard[toBoardRank][move.to.file] = piece
                // Move rook
                val rookRank = if (piece.color == PieceColor.WHITE) 7 else 0
                newBoard[rookRank][3] = newBoard[rookRank][0]
                newBoard[rookRank][0] = null
            }
            move.isEnPassant -> {
                // Move pawn
                newBoard[toBoardRank][move.to.file] = piece
                // Remove captured pawn
                val capturedRank = 7 - move.from.rank
                newBoard[capturedRank][move.to.file] = null
            }
            move.promotion != null -> {
                // Place promoted piece
                newBoard[toBoardRank][move.to.file] = Piece(move.promotion, piece.color)
            }
            else -> {
                // Regular move
                newBoard[toBoardRank][move.to.file] = piece
            }
        }

        // Update castling rights
        var newWhiteKingside = position.whiteKingsideCastle
        var newWhiteQueenside = position.whiteQueensideCastle
        var newBlackKingside = position.blackKingsideCastle
        var newBlackQueenside = position.blackQueensideCastle

        if (piece.type == PieceType.KING) {
            if (piece.color == PieceColor.WHITE) {
                newWhiteKingside = false
                newWhiteQueenside = false
            } else {
                newBlackKingside = false
                newBlackQueenside = false
            }
        }
        if (piece.type == PieceType.ROOK) {
            when {
                move.from == Square(0, 0) -> newWhiteQueenside = false
                move.from == Square(7, 0) -> newWhiteKingside = false
                move.from == Square(0, 7) -> newBlackQueenside = false
                move.from == Square(7, 7) -> newBlackKingside = false
            }
        }

        // Update en passant square
        val newEnPassant = if (piece.type == PieceType.PAWN &&
            kotlin.math.abs(move.to.rank - move.from.rank) == 2) {
            Square(move.from.file, (move.from.rank + move.to.rank) / 2)
        } else null

        // Update clocks
        val newHalfmove = if (piece.type == PieceType.PAWN ||
            position.getPieceAt(move.to) != null) 0 else position.halfmoveClock + 1
        val newFullmove = if (!position.whiteToMove) position.fullmoveNumber + 1
        else position.fullmoveNumber

        return ChessPosition(
            board = newBoard,
            whiteToMove = !position.whiteToMove,
            whiteKingsideCastle = newWhiteKingside,
            whiteQueensideCastle = newWhiteQueenside,
            blackKingsideCastle = newBlackKingside,
            blackQueensideCastle = newBlackQueenside,
            enPassantSquare = newEnPassant,
            halfmoveClock = newHalfmove,
            fullmoveNumber = newFullmove
        )
    }

    /**
     * Check if the game is over (checkmate or stalemate).
     */
    fun isGameOver(position: ChessPosition): Boolean {
        return getLegalMoves(position).isEmpty()
    }

    /**
     * Check if it's checkmate.
     */
    fun isCheckmate(position: ChessPosition): Boolean {
        val color = if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK
        return isInCheck(position, color) && getLegalMoves(position).isEmpty()
    }

    /**
     * Check if it's stalemate.
     */
    fun isStalemate(position: ChessPosition): Boolean {
        val color = if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK
        return !isInCheck(position, color) && getLegalMoves(position).isEmpty()
    }

    /**
     * Check for insufficient material.
     */
    fun isInsufficientMaterial(position: ChessPosition): Boolean {
        val pieces = mutableListOf<Piece>()
        for (rank in 0 until 8) {
            for (file in 0 until 8) {
                position.board[rank][file]?.let { pieces.add(it) }
            }
        }

        // King vs King
        if (pieces.size == 2) return true

        // King + minor piece vs King
        if (pieces.size == 3) {
            val nonKings = pieces.filter { it.type != PieceType.KING }
            if (nonKings.size == 1 &&
                (nonKings[0].type == PieceType.BISHOP || nonKings[0].type == PieceType.KNIGHT)) {
                return true
            }
        }

        return false
    }
}
