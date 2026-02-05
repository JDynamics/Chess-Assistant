package com.chessassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chessassistant.data.model.AnalysisResult
import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.PlayerColor
import com.chessassistant.engine.MoveFormatter
import com.chessassistant.ui.theme.AccentColor
import com.chessassistant.ui.theme.PanelColor

/**
 * Panel displaying Stockfish analysis.
 */
@Composable
fun AnalysisPanel(
    analysis: AnalysisResult?,
    position: ChessPosition?,
    isPlayerTurn: Boolean,
    playerColor: PlayerColor,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "♟ Stockfish Analysis",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isAnalyzing) {
            Text(
                text = "Analyzing...",
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        } else if (analysis == null) {
            Text(
                text = "No analysis available",
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        } else {
            val isWhite = playerColor == PlayerColor.WHITE

            // Evaluation
            val evalText = analysis.getEvaluationText(isWhite)
            Text(
                text = "📊 $evalText",
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )

            // Depth
            Text(
                text = "🔍 Depth: ${analysis.depth}",
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Divider
            Text(
                text = "─".repeat(25),
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Best move or waiting for opponent
            if (isPlayerTurn && analysis.bestMove != null && position != null) {
                Text(
                    text = "💡 BEST MOVE:",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                val moveText = MoveFormatter.formatMove(analysis.bestMove, position)
                Text(
                    text = "   $moveText",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Principal variation
                if (analysis.principalVariation.isNotEmpty()) {
                    Text(
                        text = "\n📈 Expected line:",
                        color = Color(0xFF00FF00),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    analysis.principalVariation.take(5).forEachIndexed { index, moveUci ->
                        val who = if (index % 2 == 0) "You" else "Opp"
                        Text(
                            text = "   $who: $moveUci",
                            color = Color(0xFF00FF00),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (!isPlayerTurn) {
                Text(
                    text = "⏳ OPPONENT'S TURN",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\nEnter their move on the board\nto see your next suggestion.",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
