package com.chessassistant.data.model

// Note: PlayerColor is defined in GameState.kt
// This file can be used for additional color-related utilities

/**
 * Extension to get the opposite color.
 */
fun PieceColor.opposite(): PieceColor = when (this) {
    PieceColor.WHITE -> PieceColor.BLACK
    PieceColor.BLACK -> PieceColor.WHITE
}

/**
 * Extension to convert to player color.
 */
fun PieceColor.toPlayerColor(): PlayerColor = when (this) {
    PieceColor.WHITE -> PlayerColor.WHITE
    PieceColor.BLACK -> PlayerColor.BLACK
}

/**
 * Extension to get display name.
 */
fun PieceColor.displayName(): String = when (this) {
    PieceColor.WHITE -> "White"
    PieceColor.BLACK -> "Black"
}

/**
 * Extension to get display name.
 */
fun PlayerColor.displayName(): String = when (this) {
    PlayerColor.WHITE -> "White"
    PlayerColor.BLACK -> "Black"
}
