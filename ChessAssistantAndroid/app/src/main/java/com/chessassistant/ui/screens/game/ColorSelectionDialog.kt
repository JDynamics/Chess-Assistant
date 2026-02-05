package com.chessassistant.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chessassistant.R
import com.chessassistant.data.model.PieceType
import com.chessassistant.ui.theme.BackgroundColor
import com.chessassistant.ui.theme.PanelColor

/**
 * Dialog for selecting which color to play as.
 */
@Composable
fun ColorSelectionDialog(
    onSelectWhite: () -> Unit,
    onSelectBlack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = PanelColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "♔ Choose Your Side ♚",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.color_select_subtitle),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // White button
                    Button(
                        onClick = onSelectWhite,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(
                            text = "♔ White",
                            color = Color(0xFF333333),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Black button
                    Button(
                        onClick = onSelectBlack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(
                            text = "♚ Black",
                            color = Color(0xFFF0F0F0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for selecting pawn promotion piece.
 */
@Composable
fun PromotionDialog(
    onSelect: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PanelColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Promote to:",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PromotionButton("♕", PieceType.QUEEN, onSelect)
                    PromotionButton("♖", PieceType.ROOK, onSelect)
                    PromotionButton("♗", PieceType.BISHOP, onSelect)
                    PromotionButton("♘", PieceType.KNIGHT, onSelect)
                }
            }
        }
    }
}

@Composable
private fun PromotionButton(
    symbol: String,
    pieceType: PieceType,
    onSelect: (PieceType) -> Unit
) {
    Button(
        onClick = { onSelect(pieceType) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(56.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 28.sp,
            color = Color.White
        )
    }
}
