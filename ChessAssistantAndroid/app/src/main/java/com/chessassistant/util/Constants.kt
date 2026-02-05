package com.chessassistant.util

/**
 * Application-wide constants.
 */
object Constants {
    // Anthropic API
    const val ANTHROPIC_BASE_URL = "https://api.anthropic.com/"
    const val ANTHROPIC_API_VERSION = "2024-01-01"
    const val CLAUDE_MODEL = "claude-opus-4-20250514"
    const val CLAUDE_SONNET_MODEL = "claude-sonnet-4-20250514"

    // Stockfish engine
    const val STOCKFISH_DEFAULT_DEPTH = 18
    const val STOCKFISH_MIN_DEPTH = 1
    const val STOCKFISH_MAX_DEPTH = 25
    const val STOCKFISH_ANALYSIS_TIME_MS = 500L

    // Chess
    const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    const val BOARD_SIZE = 8

    // UI
    const val SQUARE_SIZE_DP = 44
    const val PIECE_FONT_SIZE_SP = 36

    // File extensions
    const val PGN_EXTENSION = ".pgn"

    // Piece names for human-readable output
    val PIECE_NAMES = mapOf(
        'P' to "Pawn",
        'N' to "Knight",
        'B' to "Bishop",
        'R' to "Rook",
        'Q' to "Queen",
        'K' to "King",
        'p' to "Pawn",
        'n' to "Knight",
        'b' to "Bishop",
        'r' to "Rook",
        'q' to "Queen",
        'k' to "King"
    )

    // Piece Unicode characters
    val PIECE_UNICODE = mapOf(
        'P' to "♙",  // White Pawn
        'N' to "♘",  // White Knight
        'B' to "♗",  // White Bishop
        'R' to "♖",  // White Rook
        'Q' to "♕",  // White Queen
        'K' to "♔",  // White King
        'p' to "♟",  // Black Pawn
        'n' to "♞",  // Black Knight
        'b' to "♝",  // Black Bishop
        'r' to "♜",  // Black Rook
        'q' to "♛",  // Black Queen
        'k' to "♚"   // Black King
    )
}
