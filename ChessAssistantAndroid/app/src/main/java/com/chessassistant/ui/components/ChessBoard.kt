package com.chessassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.PieceColor
import com.chessassistant.data.model.Square
import com.chessassistant.engine.ChessLogic
import com.chessassistant.ui.theme.DarkSquare
import com.chessassistant.ui.theme.TextColor

/**
 * Responsive chess board composable that scales to any screen size.
 * Works on phones, tablets, and foldables.
 *
 * The board automatically adjusts:
 * - Square sizes based on available width
 * - Label font sizes proportional to squares
 * - Border widths for visual consistency
 */
@Composable
fun ChessBoard(
    position: ChessPosition,
    selectedSquare: Square?,
    lastMove: ChessMove?,
    legalMoves: List<ChessMove>,
    flipped: Boolean,
    onSquareClick: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val inCheck = ChessLogic.isInCheck(
        position,
        if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK
    )
    val kingSquare = if (inCheck) {
        ChessLogic.findKing(
            position,
            if (position.whiteToMove) PieceColor.WHITE else PieceColor.BLACK
        )
    } else null

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Calculate sizes based on available width
        val availableWidth = maxWidth
        val labelWidth = availableWidth * 0.06f // 6% for labels
        val boardWidth = availableWidth * 0.88f // 88% for board
        val squareSize = boardWidth / 8
        val fontSize = with(LocalDensity.current) { (squareSize * 0.35f).toSp() }
        val borderWidth = (squareSize * 0.08f).coerceIn(2.dp, 4.dp)

        Column {
            // Top file labels
            FileLabels(
                flipped = flipped,
                labelWidth = labelWidth,
                squareSize = squareSize,
                fontSize = fontSize
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Left rank labels
                RankLabels(
                    flipped = flipped,
                    labelWidth = labelWidth,
                    squareSize = squareSize,
                    fontSize = fontSize
                )

                // Board
                Box(
                    modifier = Modifier
                        .width(boardWidth)
                        .aspectRatio(1f)
                        .border(borderWidth, Color(0xFF8B4513))
                        .background(DarkSquare)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (row in 0 until 8) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (col in 0 until 8) {
                                    val file = if (flipped) 7 - col else col
                                    val rank = if (flipped) row else 7 - row
                                    val square = Square(file, rank)

                                    ChessSquare(
                                        position = position,
                                        square = square,
                                        isSelected = square == selectedSquare,
                                        isLastMoveSquare = lastMove?.let {
                                            square == it.from || square == it.to
                                        } == true,
                                        isLegalDestination = legalMoves.any { it.to == square },
                                        isKingInCheck = square == kingSquare,
                                        onClick = { onSquareClick(square) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right rank labels
                RankLabels(
                    flipped = flipped,
                    labelWidth = labelWidth,
                    squareSize = squareSize,
                    fontSize = fontSize
                )
            }

            // Bottom file labels
            FileLabels(
                flipped = flipped,
                labelWidth = labelWidth,
                squareSize = squareSize,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun FileLabels(
    flipped: Boolean,
    labelWidth: Dp,
    squareSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Spacer for left label column
        Box(modifier = Modifier.width(labelWidth))

        for (col in 0 until 8) {
            val file = if (flipped) 7 - col else col
            Box(
                modifier = Modifier
                    .width(squareSize)
                    .height(squareSize * 0.4f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('a' + file).toString(),
                    color = TextColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Spacer for right label column
        Box(modifier = Modifier.width(labelWidth))
    }
}

@Composable
private fun RankLabels(
    flipped: Boolean,
    labelWidth: Dp,
    squareSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Column(
        modifier = Modifier.width(labelWidth),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (row in 0 until 8) {
            val rank = if (flipped) row + 1 else 8 - row
            Box(
                modifier = Modifier
                    .width(labelWidth)
                    .height(squareSize),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = TextColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
