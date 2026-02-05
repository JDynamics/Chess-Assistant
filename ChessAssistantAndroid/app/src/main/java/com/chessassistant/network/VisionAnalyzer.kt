package com.chessassistant.network

import android.graphics.Bitmap
import android.util.Base64
import com.chessassistant.data.model.VisionAnalysisResult
import com.chessassistant.network.dto.MessageRequestBuilder
import com.chessassistant.network.dto.getTextContent
import com.chessassistant.util.Constants
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzes chess board images using Claude Vision API.
 */
@Singleton
class VisionAnalyzer @Inject constructor(
    private val apiService: AnthropicApiService,
    private val json: Json
) {

    /**
     * Analyze a chess board image and extract FEN notation.
     *
     * @param bitmap The chess board image
     * @param apiKey The Anthropic API key
     * @param playingAsWhite True if viewing from white's perspective
     * @return VisionAnalysisResult containing the FEN or error
     */
    suspend fun analyzeBoard(
        bitmap: Bitmap,
        apiKey: String,
        playingAsWhite: Boolean = true
    ): VisionAnalysisResult {
        if (apiKey.isBlank()) {
            return VisionAnalysisResult(
                fen = null,
                rawResponse = "",
                success = false,
                errorMessage = "API key not set"
            )
        }

        return try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = buildPrompt(playingAsWhite)

            val request = MessageRequestBuilder.withImage(
                model = Constants.CLAUDE_MODEL,
                maxTokens = 2000,
                imageBase64 = base64Image,
                mediaType = "image/png",
                prompt = prompt
            )

            val response = apiService.sendMessage(
                apiKey = apiKey,
                version = Constants.ANTHROPIC_API_VERSION,
                request = request
            )

            val rawText = response.getTextContent() ?: ""
            val fen = parseFenFromResponse(rawText, playingAsWhite)

            VisionAnalysisResult(
                fen = fen,
                rawResponse = rawText,
                success = fen != null,
                errorMessage = if (fen == null) "Could not parse board position" else null
            )
        } catch (e: Exception) {
            VisionAnalysisResult(
                fen = null,
                rawResponse = "",
                success = false,
                errorMessage = e.message ?: "Vision analysis failed"
            )
        }
    }

    /**
     * Get a brief explanation for a chess move.
     */
    suspend fun explainMove(
        apiKey: String,
        fen: String,
        moveSan: String
    ): String? {
        if (apiKey.isBlank()) return null

        return try {
            val prompt = "Chess position FEN: $fen\nBest move: $moveSan\n" +
                    "In 1-2 sentences, why is this the best move? Be very brief."

            val request = MessageRequestBuilder.textOnly(
                model = Constants.CLAUDE_SONNET_MODEL,
                maxTokens = 150,
                prompt = prompt
            )

            val response = apiService.sendMessage(
                apiKey = apiKey,
                version = Constants.ANTHROPIC_API_VERSION,
                request = request
            )

            response.getTextContent()?.trim()
        } catch (e: Exception) {
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun buildPrompt(playingAsWhite: Boolean): String {
        val coordNote = if (playingAsWhite) {
            """You are viewing from WHITE's perspective.
Visual top = rank 8, visual bottom = rank 1
Visual left = file a, visual right = file h"""
        } else {
            """You are viewing from BLACK's perspective.
Visual top = rank 1, visual bottom = rank 8
Visual left = file h, visual right = file a

When reading coordinates, use the LABELS shown on the board (numbers 1-8 on left, letters h-a on bottom)."""
        }

        return """$coordNote

List EXACTLY what piece is on each square, using the board coordinates shown.
Use: K=white king, Q=white queen, R=white rook, B=white bishop, N=white knight, P=white pawn
     k=black king, q=black queen, r=black rook, b=black bishop, n=black knight, p=black pawn
     . = empty

WHITE pieces are LIGHT colored. BLACK pieces are DARK colored.
Ignore any dots/circles (those are move hints, not pieces).

Output in this EXACT format (8 characters per line, no spaces):
8:????????
7:????????
6:????????
5:????????
4:????????
3:????????
2:????????
1:????????

Replace ? with the piece letter or . for empty.
Example: 8:rnbqkbnr means black's back rank with all pieces."""
    }

    private fun parseFenFromResponse(rawResponse: String, playingAsWhite: Boolean): String? {
        // Parse the structured format (8:rnbqkbnr etc)
        val ranks = mutableMapOf<String, String>()
        val regex = Regex("""^([1-8]):([rnbqkpRNBQKP.]{8})$""", RegexOption.MULTILINE)

        for (match in regex.findAll(rawResponse)) {
            val rankNum = match.groupValues[1]
            val pieces = match.groupValues[2]
            ranks[rankNum] = pieces
        }

        if (ranks.size == 8) {
            val fenParts = mutableListOf<String>()
            for (rank in "87654321") {
                val rankStr = ranks[rank.toString()] ?: "........"
                val fenRank = StringBuilder()
                var emptyCount = 0

                for (char in rankStr) {
                    if (char == '.') {
                        emptyCount++
                    } else {
                        if (emptyCount > 0) {
                            fenRank.append(emptyCount)
                            emptyCount = 0
                        }
                        fenRank.append(char)
                    }
                }
                if (emptyCount > 0) {
                    fenRank.append(emptyCount)
                }
                fenParts.add(fenRank.toString())
            }

            val boardFen = fenParts.joinToString("/")
            val turn = if (playingAsWhite) "w" else "b"
            return "$boardFen $turn KQkq - 0 1"
        }

        // Fallback: try to extract FEN directly
        val fenPattern = Regex("""([rnbqkpRNBQKP1-8]+/){7}[rnbqkpRNBQKP1-8]+""")
        val match = fenPattern.find(rawResponse)
        if (match != null) {
            val turn = if (playingAsWhite) "w" else "b"
            return "${match.value} $turn KQkq - 0 1"
        }

        return null
    }
}
