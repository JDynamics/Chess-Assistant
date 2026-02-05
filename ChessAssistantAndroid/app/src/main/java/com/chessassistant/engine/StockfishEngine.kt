package com.chessassistant.engine

import android.content.Context
import android.os.Build
import com.chessassistant.data.model.AnalysisResult
import com.chessassistant.data.model.BestMoveResult
import com.chessassistant.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper for the Stockfish chess engine using UCI protocol.
 */
@Singleton
class StockfishEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var process: Process? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private var isInitialized = false

    /**
     * Initialize the Stockfish engine.
     * Extracts the binary from assets and starts the UCI process.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext true

        try {
            val engineFile = extractEngine() ?: return@withContext false

            process = ProcessBuilder(engineFile.absolutePath)
                .redirectErrorStream(true)
                .start()

            reader = process!!.inputStream.bufferedReader()
            writer = process!!.outputStream.bufferedWriter()

            // Initialize UCI
            sendCommand("uci")
            val uciResponse = waitForResponse("uciok", timeoutMs = 5000)
            if (!uciResponse) {
                shutdown()
                return@withContext false
            }

            // Wait for engine to be ready
            sendCommand("isready")
            val readyResponse = waitForResponse("readyok", timeoutMs = 5000)
            if (!readyResponse) {
                shutdown()
                return@withContext false
            }

            isInitialized = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            shutdown()
            false
        }
    }

    /**
     * Extract Stockfish binary from assets to app files directory.
     */
    private fun extractEngine(): File? {
        val supportedAbis = Build.SUPPORTED_ABIS
        val abi = supportedAbis.firstOrNull { it in listOf("arm64-v8a", "armeabi-v7a") }
            ?: return null

        val engineFile = File(context.filesDir, "stockfish")

        // Check if already extracted
        if (engineFile.exists() && engineFile.canExecute()) {
            return engineFile
        }

        return try {
            // Try to copy from assets
            val assetPath = "stockfish/$abi/stockfish"
            context.assets.open(assetPath).use { input ->
                engineFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            engineFile.setExecutable(true)
            engineFile
        } catch (e: Exception) {
            // Assets not found - engine needs to be added manually
            e.printStackTrace()
            null
        }
    }

    /**
     * Send a command to the engine.
     */
    private fun sendCommand(command: String) {
        writer?.apply {
            write(command)
            newLine()
            flush()
        }
    }

    /**
     * Wait for a specific response from the engine.
     */
    private fun waitForResponse(expected: String, timeoutMs: Long = 10000): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val line = reader?.readLine() ?: return false
            if (line.contains(expected)) {
                return true
            }
        }
        return false
    }

    /**
     * Read all available output from the engine.
     */
    private fun readOutput(untilContains: String, timeoutMs: Long = 30000): List<String> {
        val output = mutableListOf<String>()
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val line = reader?.readLine() ?: break
            output.add(line)
            if (line.contains(untilContains)) {
                break
            }
        }

        return output
    }

    /**
     * Get the best move for a position.
     */
    suspend fun getBestMove(fen: String, depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH): BestMoveResult? {
        return withContext(Dispatchers.IO) {
            if (!isInitialized && !initialize()) {
                return@withContext null
            }

            try {
                // Set position
                sendCommand("position fen $fen")

                // Start analysis
                sendCommand("go depth $depth")

                // Wait for bestmove
                val output = readOutput("bestmove")

                // Parse best move
                val bestMoveLine = output.lastOrNull { it.startsWith("bestmove") }
                    ?: return@withContext null

                val parts = bestMoveLine.split(" ")
                if (parts.size < 2) return@withContext null

                val moveUci = parts[1]
                val move = MoveFormatter.parseUci(moveUci) ?: return@withContext null

                // Get evaluation from info lines
                val lastInfoLine = output.lastOrNull { it.startsWith("info") && it.contains("score") }
                val score = parseScore(lastInfoLine)

                BestMoveResult(
                    move = move,
                    moveUci = moveUci,
                    score = score,
                    depth = depth
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Analyze a position and get detailed information.
     */
    suspend fun analyze(fen: String, depth: Int = Constants.STOCKFISH_DEFAULT_DEPTH): AnalysisResult? {
        return withContext(Dispatchers.IO) {
            if (!isInitialized && !initialize()) {
                return@withContext null
            }

            try {
                sendCommand("position fen $fen")
                sendCommand("go depth $depth")

                val output = readOutput("bestmove")

                // Parse best move
                val bestMoveLine = output.lastOrNull { it.startsWith("bestmove") }
                    ?: return@withContext null

                val parts = bestMoveLine.split(" ")
                if (parts.size < 2) return@withContext null

                val bestMoveUci = parts[1]
                val bestMove = MoveFormatter.parseUci(bestMoveUci)

                // Parse principal variation (PV)
                val pvLine = output.lastOrNull { it.contains(" pv ") }
                val pv = parsePv(pvLine)

                // Parse score
                val infoLine = output.lastOrNull { it.startsWith("info") && it.contains("score") }
                val score = parseScore(infoLine)
                val isMate = infoLine?.contains("score mate") == true
                val mateIn = if (isMate) parseMateIn(infoLine) else null

                // Parse depth reached
                val actualDepth = parseDepth(infoLine) ?: depth

                AnalysisResult(
                    bestMove = bestMove,
                    bestMoveUci = bestMoveUci,
                    score = score,
                    isMate = isMate,
                    mateIn = mateIn,
                    principalVariation = pv,
                    depth = actualDepth
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseScore(infoLine: String?): Int? {
        if (infoLine == null) return null

        val scoreIndex = infoLine.indexOf("score cp ")
        if (scoreIndex != -1) {
            val start = scoreIndex + "score cp ".length
            val end = infoLine.indexOf(" ", start).takeIf { it != -1 } ?: infoLine.length
            return infoLine.substring(start, end).toIntOrNull()
        }

        val mateIndex = infoLine.indexOf("score mate ")
        if (mateIndex != -1) {
            val start = mateIndex + "score mate ".length
            val end = infoLine.indexOf(" ", start).takeIf { it != -1 } ?: infoLine.length
            val mateIn = infoLine.substring(start, end).toIntOrNull() ?: return null
            // Return a large value for mate scores
            return if (mateIn > 0) 10000 - mateIn else -10000 - mateIn
        }

        return null
    }

    private fun parseMateIn(infoLine: String?): Int? {
        if (infoLine == null) return null

        val mateIndex = infoLine.indexOf("score mate ")
        if (mateIndex != -1) {
            val start = mateIndex + "score mate ".length
            val end = infoLine.indexOf(" ", start).takeIf { it != -1 } ?: infoLine.length
            return infoLine.substring(start, end).toIntOrNull()
        }

        return null
    }

    private fun parsePv(pvLine: String?): List<String> {
        if (pvLine == null) return emptyList()

        val pvIndex = pvLine.indexOf(" pv ")
        if (pvIndex == -1) return emptyList()

        val pvPart = pvLine.substring(pvIndex + " pv ".length)
        return pvPart.split(" ").filter { it.isNotEmpty() && it.matches(Regex("[a-h][1-8][a-h][1-8][qrbn]?")) }
    }

    private fun parseDepth(infoLine: String?): Int? {
        if (infoLine == null) return null

        val depthIndex = infoLine.indexOf("depth ")
        if (depthIndex != -1) {
            val start = depthIndex + "depth ".length
            val end = infoLine.indexOf(" ", start).takeIf { it != -1 } ?: infoLine.length
            return infoLine.substring(start, end).toIntOrNull()
        }

        return null
    }

    /**
     * Stop the current analysis.
     */
    fun stop() {
        try {
            sendCommand("stop")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Shutdown the engine and release resources.
     */
    fun shutdown() {
        try {
            sendCommand("quit")
        } catch (e: Exception) {
            // Ignore
        }

        try {
            writer?.close()
            reader?.close()
            process?.destroy()
        } catch (e: Exception) {
            // Ignore
        }

        process = null
        reader = null
        writer = null
        isInitialized = false
    }

    /**
     * Check if the engine is ready.
     */
    fun isReady(): Boolean = isInitialized
}
