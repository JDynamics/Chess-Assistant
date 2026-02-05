package com.chessassistant.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.min

/**
 * Utility functions for image processing.
 */
object ImageUtils {

    /**
     * Load a bitmap from a URI with optional resize.
     */
    fun loadBitmapFromUri(
        context: Context,
        uri: Uri,
        maxSize: Int = 1024
    ): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Get dimensions first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxSize)

            // Load with sample size
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            val newInputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, loadOptions)
            newInputStream.close()

            // Handle rotation
            val rotation = getRotation(context, uri)
            if (rotation != 0 && bitmap != null) {
                rotateBitmap(bitmap, rotation)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calculate sample size for loading large images.
     */
    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > maxSize || height / sampleSize > maxSize) {
            sampleSize *= 2
        }
        return sampleSize
    }

    /**
     * Get rotation from EXIF data.
     */
    private fun getRotation(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Rotate a bitmap.
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Crop a bitmap to a square centered on the image.
     */
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    /**
     * Crop the approximate chess board area from an image.
     * Uses the same heuristic as the Python app.
     */
    fun cropBoardArea(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Chess.com board is typically a square in the center-left of the window
        // Heuristic: board is usually 70-90% of the shorter dimension
        val boardSize = (min(width, height) * 0.75).toInt()

        // Center the crop vertically, slight left offset
        val left = (width * 0.05).toInt()
        val top = (height - boardSize) / 2
        val right = min(left + boardSize, width)
        val bottom = min(top + boardSize, height)

        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    /**
     * Scale a bitmap to a target size.
     */
    fun scaleBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        val scale = targetSize.toFloat() / max(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun max(a: Int, b: Int): Int = if (a > b) a else b
}
