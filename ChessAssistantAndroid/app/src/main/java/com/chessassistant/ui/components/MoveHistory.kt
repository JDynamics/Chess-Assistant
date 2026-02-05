package com.chessassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chessassistant.data.model.MoveRecord
import com.chessassistant.data.model.PlayerColor
import com.chessassistant.ui.theme.PanelColor

/**
 * Panel displaying move history.
 */
@Composable
fun MoveHistory(
    moves: List<MoveRecord>,
    playerColor: PlayerColor,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 150.dp)
            .background(PanelColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "📜 Move History",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (moves.isEmpty()) {
            Text(
                text = "No moves yet",
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Group moves by pairs (white + black)
                moves.chunked(2).forEach { movePair ->
                    val moveNumber = movePair.first().moveNumber
                    val whiteMove = movePair.firstOrNull()
                    val blackMove = movePair.getOrNull(1)

                    val moveText = buildString {
                        append("$moveNumber. ")

                        whiteMove?.let {
                            val who = if (playerColor == PlayerColor.WHITE) "[You]" else "[Opp]"
                            append("$who${it.san} ")
                        }

                        blackMove?.let {
                            val who = if (playerColor == PlayerColor.BLACK) "[You]" else "[Opp]"
                            append("$who${it.san}")
                        }
                    }

                    Text(
                        text = moveText,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
