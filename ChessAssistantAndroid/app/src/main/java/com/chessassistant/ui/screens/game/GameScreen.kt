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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chessassistant.R
import com.chessassistant.data.model.GameResult
import com.chessassistant.engine.ChessLogic
import com.chessassistant.ui.components.AnalysisPanel
import com.chessassistant.ui.components.ChessBoard
import com.chessassistant.ui.components.MoveHistory
import com.chessassistant.ui.theme.AccentColor
import com.chessassistant.ui.theme.BackgroundColor
import com.chessassistant.ui.theme.PanelColor

/**
 * Full chess game screen with board, analysis, and controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.game_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.newGame() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.game_new_game),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { viewModel.undoMove() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(R.string.game_undo),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { viewModel.saveGame() }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(R.string.game_save),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
        )

        // Show color selection dialog
        if (uiState.showColorDialog) {
            ColorSelectionDialog(
                onSelectWhite = { viewModel.selectColor(com.chessassistant.data.model.PlayerColor.WHITE) },
                onSelectBlack = { viewModel.selectColor(com.chessassistant.data.model.PlayerColor.BLACK) }
            )
        } else {
            // Game content
            val gameState = uiState.gameState
            if (gameState != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Turn indicator
                        TurnIndicator(
                            isPlayerTurn = gameState.isPlayerTurn,
                            gameResult = gameState.gameResult
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chess board
                        val legalMoves = if (gameState.selectedSquare != null) {
                            ChessLogic.getLegalMoves(gameState.position)
                                .filter { it.from == gameState.selectedSquare }
                        } else emptyList()

                        ChessBoard(
                            position = gameState.position,
                            selectedSquare = gameState.selectedSquare,
                            lastMove = gameState.lastMove,
                            legalMoves = legalMoves,
                            flipped = uiState.boardFlipped,
                            onSquareClick = { viewModel.onSquareClick(it) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Analysis panel
                        AnalysisPanel(
                            analysis = gameState.currentAnalysis,
                            position = gameState.position,
                            isPlayerTurn = gameState.isPlayerTurn,
                            playerColor = gameState.playerColor,
                            isAnalyzing = gameState.isAnalyzing
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Move history
                        MoveHistory(
                            moves = gameState.moveHistory,
                            playerColor = gameState.playerColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status message
                        Text(
                            text = uiState.statusMessage,
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }

                    // Play best move FAB
                    if (gameState.isPlayerTurn &&
                        gameState.bestMove != null &&
                        gameState.gameResult == GameResult.IN_PROGRESS) {
                        FloatingActionButton(
                            onClick = { viewModel.playBestMove() },
                            containerColor = AccentColor,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.game_play_best_move),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Promotion dialog
        if (uiState.showPromotionDialog) {
            PromotionDialog(
                onSelect = { viewModel.selectPromotion(it) },
                onDismiss = { viewModel.dismissPromotionDialog() }
            )
        }
    }
}

@Composable
private fun TurnIndicator(
    isPlayerTurn: Boolean,
    gameResult: GameResult
) {
    val (text, bgColor) = when (gameResult) {
        GameResult.WHITE_WINS_CHECKMATE,
        GameResult.BLACK_WINS_CHECKMATE -> "Checkmate!" to Color(0xFFFF6B6B)
        GameResult.DRAW_STALEMATE,
        GameResult.DRAW_INSUFFICIENT_MATERIAL -> "Draw!" to Color(0xFF888888)
        else -> if (isPlayerTurn) {
            "Your turn!" to AccentColor
        } else {
            "Opponent's turn" to Color(0xFF666666)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
