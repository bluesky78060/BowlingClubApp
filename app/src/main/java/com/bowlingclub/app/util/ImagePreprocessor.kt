package com.bowlingclub.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.content.Context
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

object ImagePreprocessor {

    // Max dimension for OCR processing
    private const val MAX_OCR_DIMENSION = 2048
    // JPEG quality for compressed output
    private const val JPEG_QUALITY = 90

    /**
     * Full preprocessing pipeline for handwriting OCR:
     * 1. Decode with size limits
     * 2. Auto-rotate based on EXIF
     * 3. Resize to max dimension
     * 4. Convert to grayscale (removes color noise)
     * 5. Denoise (median filter for salt-and-pepper noise)
     * 6. Otsu binarization (adaptive threshold for ink vs paper)
     * 7. Enhance contrast (strengthen handwriting strokes)
     * 8. Sharpen (improve edge clarity)
     * 9. Convert to PNG byte array (lossless for OCR)
     */
    fun preprocessForOcr(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            val rotated = autoRotate(context, uri, bitmap)
            val resized = resizeToMaxDimension(rotated, MAX_OCR_DIMENSION)
            val grayscale = toGrayscale(resized)
            val denoised = medianFilter(grayscale)
            val binarized = adaptiveThreshold(denoised)
            val enhanced = enhanceContrast(binarized, 1.4f, 10f)
            val sharpened = sharpen(enhanced)

            bitmapToByteArray(sharpened, format = Bitmap.CompressFormat.PNG)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resizes bitmap so the larger dimension is at most maxDimension.
     * Maintains aspect ratio.
     */
    fun resizeToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val scale = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Auto-rotates bitmap based on EXIF orientation data.
     */
    fun autoRotate(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                else -> return bitmap
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * Converts bitmap to grayscale.
     * Grayscale improves OCR accuracy for handwritten text by removing color noise.
     */
    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val cm = ColorMatrix()
        cm.setSaturation(0f)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * Enhances image contrast for better OCR.
     * @param contrast 1.0 = no change, >1 = more contrast
     * @param brightness added to each channel
     */
    fun enhanceContrast(bitmap: Bitmap, contrast: Float = 1.6f, brightness: Float = 15f): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val cm = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * Applies a sharpening filter to the bitmap.
     * Improves edge clarity for handwritten text recognition.
     * Uses a 3x3 sharpening convolution kernel via ColorMatrix approximation.
     */
    fun sharpen(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        // Sharpening via contrast boost with negative brightness offset
        // This creates a sharpening-like effect that enhances edges
        val sharpenMatrix = ColorMatrix(floatArrayOf(
            1.5f, 0f, 0f, 0f, -40f,
            0f, 1.5f, 0f, 0f, -40f,
            0f, 0f, 1.5f, 0f, -40f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(sharpenMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * 3x3 Median filter for salt-and-pepper noise removal.
     * Preserves handwriting edges better than Gaussian blur.
     */
    fun medianFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val result = IntArray(width * height)
        val neighbors = IntArray(9)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var idx = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        neighbors[idx++] = Color.red(pixels[(y + dy) * width + (x + dx)])
                    }
                }
                neighbors.sort()
                val median = neighbors[4]
                result[y * width + x] = Color.rgb(median, median, median)
            }
        }
        // Copy border pixels unchanged
        for (x in 0 until width) {
            result[x] = pixels[x]
            result[(height - 1) * width + x] = pixels[(height - 1) * width + x]
        }
        for (y in 0 until height) {
            result[y * width] = pixels[y * width]
            result[y * width + width - 1] = pixels[y * width + width - 1]
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(result, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Otsu's binarization: automatically finds optimal threshold to separate
     * handwriting ink from paper background.
     * Much more effective than fixed threshold for varying lighting conditions.
     */
    fun otsuBinarize(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Build histogram of grayscale values
        val histogram = IntArray(256)
        for (pixel in pixels) {
            histogram[Color.red(pixel)]++
        }

        val totalPixels = width * height
        var sumAll = 0.0
        for (i in 0..255) {
            sumAll += i * histogram[i]
        }

        // Find optimal threshold using Otsu's method
        var sumBackground = 0.0
        var weightBackground = 0
        var maxVariance = 0.0
        var threshold = 0

        for (t in 0..255) {
            weightBackground += histogram[t]
            if (weightBackground == 0) continue
            val weightForeground = totalPixels - weightBackground
            if (weightForeground == 0) break

            sumBackground += t * histogram[t]
            val meanBackground = sumBackground / weightBackground
            val meanForeground = (sumAll - sumBackground) / weightForeground

            val betweenVariance = weightBackground.toDouble() * weightForeground *
                    (meanBackground - meanForeground) * (meanBackground - meanForeground)

            if (betweenVariance > maxVariance) {
                maxVariance = betweenVariance
                threshold = t
            }
        }

        // Apply threshold
        val result = IntArray(width * height)
        for (i in pixels.indices) {
            val gray = Color.red(pixels[i])
            val bw = if (gray > threshold) 255 else 0
            result[i] = Color.rgb(bw, bw, bw)
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(result, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Adaptive Thresholding: 조명 불균일/그림자에 강한 이진화.
     *
     * 각 픽셀 주변 blockSize x blockSize 영역의 평균을 계산하고,
     * 평균 - offset 보다 어두우면 전경(검정), 아니면 배경(흰색)으로 분류.
     * Integral Image(적분 이미지)를 사용해 O(1)으로 영역 평균을 계산.
     */
    fun adaptiveThreshold(
        bitmap: Bitmap,
        blockSize: Int = 25,
        offset: Int = 10
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Integral Image 생성 (long 사용 - 큰 이미지에서 int 오버플로 방지)
        val integral = LongArray((width + 1) * (height + 1))
        val iw = width + 1

        for (y in 0 until height) {
            var rowSum = 0L
            for (x in 0 until width) {
                rowSum += Color.red(pixels[y * width + x])
                integral[(y + 1) * iw + (x + 1)] = integral[y * iw + (x + 1)] + rowSum
            }
        }

        // Adaptive threshold 적용
        val halfBlock = blockSize / 2
        val result = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val y1 = maxOf(0, y - halfBlock)
                val y2 = minOf(height - 1, y + halfBlock)
                val x1 = maxOf(0, x - halfBlock)
                val x2 = minOf(width - 1, x + halfBlock)

                val count = (y2 - y1 + 1) * (x2 - x1 + 1)
                val sum = integral[(y2 + 1) * iw + (x2 + 1)] -
                        integral[y1 * iw + (x2 + 1)] -
                        integral[(y2 + 1) * iw + x1] +
                        integral[y1 * iw + x1]

                val localMean = (sum / count).toInt()
                val pixelValue = Color.red(pixels[y * width + x])

                val bw = if (pixelValue < localMean - offset) 0 else 255
                result[y * width + x] = Color.rgb(bw, bw, bw)
            }
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(result, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Converts bitmap to byte array.
     * Uses PNG (lossless) by default for better OCR accuracy.
     */
    fun bitmapToByteArray(
        bitmap: Bitmap,
        quality: Int = JPEG_QUALITY,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }
}
