package com.chessassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.Square
import com.chessassistant.ui.theme.CheckColor
import com.chessassistant.ui.theme.DarkSquare
import com.chessassistant.ui.theme.HighlightColor
import com.chessassistant.ui.theme.LastMoveDarkSquare
import com.chessassistant.ui.theme.LastMoveLightSquare
import com.chessassistant.ui.theme.LightSquare
import com.chessassistant.ui.theme.SelectedColor

/**
 * Individual chess square with piece and highlighting.
 */
@Composable
fun ChessSquare(
    position: ChessPosition,
    square: Square,
    isSelected: Boolean,
    isLastMoveSquare: Boolean,
    isLegalDestination: Boolean,
    isKingInCheck: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightSquare = (square.file + square.rank) % 2 == 1

    // Determine background color
    val backgroundColor = when {
        isSelected -> SelectedColor
        isKingInCheck -> CheckColor
        isLastMoveSquare -> if (isLightSquare) LastMoveLightSquare else LastMoveDarkSquare
        isLightSquare -> LightSquare
        else -> DarkSquare
    }

    val piece = position.getPieceAt(square)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Show legal move indicator
        if (isLegalDestination && !isSelected) {
            if (piece != null) {
                // Capture indicator (ring around the square)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .background(
                            color = HighlightColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
            } else {
                // Move indicator (small dot)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(HighlightColor.copy(alpha = 0.6f))
                )
            }
        }

        // Draw piece
        if (piece != null) {
            ChessPiece(piece = piece)
        }
    }
}
