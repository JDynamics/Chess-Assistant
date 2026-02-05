package com.chessassistant.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chessassistant.data.model.Piece
import com.chessassistant.data.model.PieceColor
import com.chessassistant.data.model.PieceType

/**
 * Chess piece rendered as Unicode character.
 */
@Composable
fun ChessPiece(
    piece: Piece,
    modifier: Modifier = Modifier
) {
    val unicode = getPieceUnicode(piece)
    val textColor = if (piece.color == PieceColor.WHITE) {
        Color.White
    } else {
        Color(0xFF1A1A1A)
    }

    Text(
        text = unicode,
        color = textColor,
        fontSize = 36.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(2.dp)
    )
}

/**
 * Get the Unicode character for a chess piece.
 */
fun getPieceUnicode(piece: Piece): String {
    return when (piece.type) {
        PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
    }
}

/**
 * Get piece name for display.
 */
fun getPieceName(pieceType: PieceType): String {
    return when (pieceType) {
        PieceType.KING -> "King"
        PieceType.QUEEN -> "Queen"
        PieceType.ROOK -> "Rook"
        PieceType.BISHOP -> "Bishop"
        PieceType.KNIGHT -> "Knight"
        PieceType.PAWN -> "Pawn"
    }
}
