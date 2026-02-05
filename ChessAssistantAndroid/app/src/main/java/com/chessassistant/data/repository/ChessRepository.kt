package com.chessassistant.data.repository

import android.graphics.Bitmap
import com.chessassistant.data.model.AnalysisResult
import com.chessassistant.data.model.BestMoveResult
import com.chessassistant.data.model.ChessMove
import com.chessassistant.data.model.ChessPosition
import com.chessassistant.data.model.VisionAnalysisResult
import com.chessassistant.engine.ChessLogic
import com.chessassistant.engine.FenParser
import com.chessassistant.engine.MoveFormatter
import com.chessassistant.engine.StockfishEngine
import com.chessassistant.network.VisionAnalyzer
import com.chessassistant.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for chess-related operations.
 * Coordinates between the Stockfish engine and Claude Vision API.
 */
@Singleton
class ChessRepository @Inject constructor(
    private val stockfishEngine: StockfishEngine,
    private val visionAnalyzer: VisionAnalyzer
) {
    /**
     * Initialize the chess engine.
     */
    suspend fun initializeEngine(): Boolean {
        return stockfishEngine.initialize()
    }

    /**
     * Get the best move for a position.
     */
    suspend fun getBestMove(
        position: ChessPosition,
        depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH
    ): BestMoveResult? {
        val fen = FenParser.toFen(position)
        return stockfishEngine.getBestMove(fen, depth)
    }

    /**
     * Get the best move from a FEN string.
     */
    suspend fun getBestMoveFromFen(
        fen: String,
        depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH
    ): BestMoveResult? {
        return stockfishEngine.getBestMove(fen, depth)
    }

    /**
     * Analyze a position.
     */
    suspend fun analyzePosition(
        position: ChessPosition,
        depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH
    ): AnalysisResult? {
        val fen = FenParser.toFen(position)
        return stockfishEngine.analyze(fen, depth)
    }

    /**
     * Analyze a FEN string.
     */
    suspend fun analyzeFen(
        fen: String,
        depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH
    ): AnalysisResult? {
        return stockfishEngine.analyze(fen, depth)
    }

    /**
     * Analyze a chess board image and get the best move.
     */
    suspend fun analyzeImage(
        bitmap: Bitmap,
        apiKey: String,
        playingAsWhite: Boolean = true,
        depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH
    ): ImageAnalysisResult {
        // Step 1: Vision analysis to get FEN
        val visionResult = visionAnalyzer.analyzeBoard(bitmap, apiKey, playingAsWhite)

        if (!visionResult.success || visionResult.fen == null) {
            return ImageAnalysisResult(
                success = false,
                errorMessage = visionResult.errorMessage ?: "Failed to analyze board image"
            )
        }

        // Step 2: Validate FEN
        val position = FenParser.parse(visionResult.fen)
        if (position == null) {
            return ImageAnalysisResult(
                success = false,
                errorMessage = "Invalid position detected"
            )
        }

        // Step 3: Get best move from Stockfish
        val analysisResult = stockfishEngine.analyze(visionResult.fen, depth)
        if (analysisResult == null || analysisResult.bestMove == null) {
            return ImageAnalysisResult(
                success = false,
                errorMessage = "Failed to calculate best move",
                fen = visionResult.fen,
                position = position
            )
        }

        // Step 4: Format the move
        val formattedMove = MoveFormatter.formatMove(analysisResult.bestMove, position)

        return ImageAnalysisResult(
            success = true,
            fen = visionResult.fen,
            position = position,
            bestMove = analysisResult.bestMove,
            formattedMove = formattedMove,
            analysis = analysisResult
        )
    }

    /**
     * Get an explanation for a move.
     */
    suspend fun explainMove(
        apiKey: String,
        fen: String,
        moveSan: String
    ): String? {
        return visionAnalyzer.explainMove(apiKey, fen, moveSan)
    }

    /**
     * Get legal moves for a position.
     */
    fun getLegalMoves(position: ChessPosition): List<ChessMove> {
        return ChessLogic.getLegalMoves(position)
    }

    /**
     * Make a move and return the new position.
     */
    fun makeMove(position: ChessPosition, move: ChessMove): ChessPosition {
        return ChessLogic.makeMove(position, move)
    }

    /**
     * Check if a move is legal.
     */
    fun isLegalMove(position: ChessPosition, move: ChessMove): Boolean {
        return ChessLogic.getLegalMoves(position).contains(move)
    }

    /**
     * Parse a FEN string.
     */
    fun parseFen(fen: String): ChessPosition? {
        return FenParser.parse(fen)
    }

    /**
     * Get the starting position.
     */
    fun getStartingPosition(): ChessPosition {
        return FenParser.startingPosition()
    }

    /**
     * Format a move in human-readable format.
     */
    fun formatMove(move: ChessMove, position: ChessPosition): String {
        return MoveFormatter.formatMove(move, position)
    }

    /**
     * Shutdown the engine.
     */
    fun shutdown() {
        stockfishEngine.shutdown()
    }
}

/**
 * Result from analyzing an image.
 */
data class ImageAnalysisResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val fen: String? = null,
    val position: ChessPosition? = null,
    val bestMove: ChessMove? = null,
    val formattedMove: String? = null,
    val analysis: AnalysisResult? = null
)
