package com.chessassistant.data.repository

import android.content.Context
import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.GameState
import com.chessassistant.data.model.MoveRecord
import com.chessassistant.data.model.PlayerColor
import com.chessassistant.engine.FenParser
import com.chessassistant.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for saving and loading chess games.
 */
@Singleton
class GameStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gamesDir: File
        get() = File(context.filesDir, "games").also { it.mkdirs() }

    /**
     * Save a game to PGN format.
     */
    suspend fun saveGame(
        gameState: GameState,
        filename: String? = null
    ): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val finalFilename = filename ?: "chess_game_$timestamp${Constants.PGN_EXTENSION}"
        val file = File(gamesDir, finalFilename)

        val pgn = buildPgn(gameState)
        file.writeText(pgn)

        file
    }

    /**
     * Load a game from PGN file.
     */
    suspend fun loadGame(file: File): GameState? = withContext(Dispatchers.IO) {
        try {
            val pgnContent = file.readText()
            parsePgn(pgnContent)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get list of saved games.
     */
    suspend fun getSavedGames(): List<File> = withContext(Dispatchers.IO) {
        gamesDir.listFiles { file ->
            file.name.endsWith(Constants.PGN_EXTENSION)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Delete a saved game.
     */
    suspend fun deleteGame(file: File): Boolean = withContext(Dispatchers.IO) {
        file.delete()
    }

    private fun buildPgn(gameState: GameState): String {
        val sb = StringBuilder()

        // Headers
        sb.appendLine("[Event \"Chess Assistant Game\"]")
        sb.appendLine("[Site \"Android App\"]")
        sb.appendLine("[Date \"${SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date())}\"]")
        sb.appendLine("[Round \"?\"]")
        sb.appendLine("[White \"${if (gameState.playerColor == PlayerColor.WHITE) "Player" else "Opponent"}\"]")
        sb.appendLine("[Black \"${if (gameState.playerColor == PlayerColor.BLACK) "Player" else "Opponent"}\"]")
        sb.appendLine("[Result \"*\"]")
        sb.appendLine()

        // Moves
        val moves = gameState.moveHistory
        var moveText = ""
        for ((index, record) in moves.withIndex()) {
            if (index % 2 == 0) {
                moveText += "${record.moveNumber}. "
            }
            moveText += "${record.san} "
        }

        // Wrap at 80 characters
        val wrappedMoves = moveText.chunked(80).joinToString("\n")
        sb.appendLine(wrappedMoves)

        return sb.toString()
    }

    private fun parsePgn(pgnContent: String): GameState? {
        // This is a simplified PGN parser
        // For full PGN support, consider using a library

        val lines = pgnContent.lines()
        val moveSection = lines.dropWhile { it.startsWith("[") || it.isBlank() }
            .joinToString(" ")

        // Start from initial position and replay moves
        val startPosition = FenParser.startingPosition()

        // Parse move text (simplified - doesn't handle variations or comments)
        val movePattern = Regex("""(\d+\.)\s*([a-zA-Z0-9+#=\-]+)\s*([a-zA-Z0-9+#=\-]+)?""")
        val moveHistory = mutableListOf<MoveRecord>()

        // This is a placeholder - full SAN parsing would be needed
        // For now, return the starting position
        return GameState(
            position = startPosition,
            moveHistory = moveHistory,
            playerColor = PlayerColor.WHITE
        )
    }
}
