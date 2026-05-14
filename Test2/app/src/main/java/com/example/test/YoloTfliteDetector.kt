package com.example.test

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

class YoloTfliteDetector(private val context: Context) {

    companion object {
        private const val INPUT_SIZE = 640
        private const val CONF_THRESHOLD = 0.25f      // Низкий порог для захвата всех карт
        private const val TAG = "YoloDetector"
        // NMS ОТКЛЮЧЁН!
    }

    data class Detection(
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val conf: Float,
        val cls: Int,
        val clsName: String = ""
    )

    private var interpreter: Interpreter? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelFile = File(context.cacheDir, "model.tflite")
            if (!modelFile.exists()) {
                context.assets.open("model.tflite").use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            interpreter = Interpreter(modelFile)
            Log.d(TAG, "✅ Модель загружена")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка: ${e.message}")
        }
    }

    fun detect(bitmap: Bitmap): List<Detection> {
        if (interpreter == null) return emptyList()

        return try {
            // Центрируем карты в кадре
            val size = min(bitmap.width, bitmap.height)
            val startX = (bitmap.width - size) / 2
            val startY = (bitmap.height - size) / 2
            val croppedBitmap = Bitmap.createBitmap(bitmap, startX, startY, size, size)
            val inputBitmap = Bitmap.createScaledBitmap(croppedBitmap, INPUT_SIZE, INPUT_SIZE, true)

            val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            inputBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

            for (pixel in pixels) {
                byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
                byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
                byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
            }

            val outputBuffer = Array(1) { Array(58) { FloatArray(8400) } }
            interpreter?.run(byteBuffer.rewind(), outputBuffer)

            val rawOutput = outputBuffer[0]
            val allBoxes = mutableListOf<Detection>()

            var maxDetect = 0f
            for (i in 0 until 8400) {
                for (c in 0 until 54) {
                    maxDetect = max(maxDetect, rawOutput[c + 4][i])
                }
            }

            for (i in 0 until 8400) {
                val w = rawOutput[2][i]
                val h = rawOutput[3][i]

                if (w < 0.05f || w > 0.9f || h < 0.05f || h > 0.9f) continue

                var maxConf = 0f
                var bestClass = -1
                for (c in 0 until 54) {
                    var conf = rawOutput[c + 4][i]
                    if (maxDetect > 0) conf /= maxDetect
                    if (conf > maxConf) {
                        maxConf = conf
                        bestClass = c
                    }
                }

                if (maxConf > CONF_THRESHOLD && bestClass >= 0) {
                    val cx = rawOutput[0][i]
                    val cy = rawOutput[1][i]
                    val x = (cx - w/2).coerceIn(0f, 1f)
                    val y = (cy - h/2).coerceIn(0f, 1f)
                    allBoxes.add(Detection(x, y, w, h, maxConf, bestClass))
                }
            }

            Log.d(TAG, "Детекций (всего, без NMS): ${allBoxes.size}")

            // ВОЗВРАЩАЕМ ВСЕ ДЕТЕКЦИИ без NMS
            // Координаты потом пересчитаем в MainActivity
            val scaleX = bitmap.width.toFloat() / INPUT_SIZE
            val scaleY = bitmap.height.toFloat() / INPUT_SIZE
            val offsetX = startX.toFloat()
            val offsetY = startY.toFloat()

            allBoxes.map {
                Detection(
                    x = (it.x * INPUT_SIZE * scaleX) + offsetX,
                    y = (it.y * INPUT_SIZE * scaleY) + offsetY,
                    w = it.w * INPUT_SIZE * scaleX,
                    h = it.h * INPUT_SIZE * scaleY,
                    conf = it.conf,
                    cls = it.cls,
                    clsName = CardMapping.getName(it.cls)
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка: ${e.message}")
            emptyList()
        }
    }

    fun close() {
        interpreter?.close()
    }
}