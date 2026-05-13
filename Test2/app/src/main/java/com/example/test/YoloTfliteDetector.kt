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
        private const val CONF_THRESHOLD = 0.45f
        private const val NMS_THRESHOLD = 0.5f
        private const val TAG = "YoloDetector"
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
            Log.d(TAG, "Загрузка модели...")
            val modelFile = File(context.cacheDir, "model.tflite")
            if (!modelFile.exists()) {
                Log.d(TAG, "Копирование модели из assets...")
                context.assets.open("model.tflite").use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Модель скопирована: ${modelFile.length() / 1024} KB")
            }
            interpreter = Interpreter(modelFile)
            Log.d(TAG, "✅ Модель загружена успешно")

            // Выводим информацию о модели
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            Log.d(TAG, "Вход модели: ${inputShape?.joinToString("x")}")
            Log.d(TAG, "Выход модели: ${outputShape?.joinToString("x")}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка загрузки модели: ${e.message}")
        }
    }

    fun detect(bitmap: Bitmap): List<Detection> {
        if (interpreter == null) {
            Log.e(TAG, "Интерпретатор не инициализирован")
            return emptyList()
        }

        val startTime = System.currentTimeMillis()

        return try {
            Log.d(TAG, "=== ДЕТЕКЦИЯ ===")
            Log.d(TAG, "Входной bitmap: ${bitmap.width}x${bitmap.height}")

            // Центрируем и ресайзим
            val size = min(bitmap.width, bitmap.height)
            val startX = (bitmap.width - size) / 2
            val startY = (bitmap.height - size) / 2
            Log.d(TAG, "Центрирование: size=$size, startX=$startX, startY=$startY")

            val squaredBitmap = Bitmap.createBitmap(bitmap, startX, startY, size, size)
            val inputBitmap = Bitmap.createScaledBitmap(squaredBitmap, INPUT_SIZE, INPUT_SIZE, true)

            // Подготовка буфера
            val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            inputBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

            for (pixel in pixels) {
                byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
                byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
                byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
            }

            // Инференс
            val inferenceStart = System.currentTimeMillis()
            val outputBuffer = Array(1) { Array(58) { FloatArray(8400) } }
            interpreter?.run(byteBuffer.rewind(), outputBuffer)
            val inferenceTime = System.currentTimeMillis() - inferenceStart
            Log.d(TAG, "⏱ Время инференса: ${inferenceTime}ms")

            // Парсинг результатов
            val rawOutput = outputBuffer[0]
            val allBoxes = mutableListOf<Detection>()

            var maxDetect = 0f
            for (i in 0 until 8400) {
                for (c in 0 until 54) {
                    maxDetect = max(maxDetect, rawOutput[c + 4][i])
                }
            }
            Log.d(TAG, "Глобальный максимум: $maxDetect")

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

            Log.d(TAG, "Детекций до NMS: ${allBoxes.size}")

            // NMS (Non-Maximum Suppression)
            val sorted = allBoxes.sortedByDescending { it.conf }
            val result = mutableListOf<Detection>()
            val suppressed = BooleanArray(sorted.size)

            for (i in sorted.indices) {
                if (suppressed[i]) continue
                val current = sorted[i]
                result.add(current)
                for (j in i + 1 until sorted.size) {
                    if (suppressed[j]) continue
                    val iou = calculateIOU(current, sorted[j])
                    if (iou > NMS_THRESHOLD) {
                        suppressed[j] = true
                    }
                }
            }

            Log.d(TAG, "Детекций после NMS: ${result.size}")

            // Масштабируем координаты
            val scaleX = bitmap.width.toFloat() / INPUT_SIZE
            val scaleY = bitmap.height.toFloat() / INPUT_SIZE
            val offsetX = startX.toFloat()
            val offsetY = startY.toFloat()

            val scaled = result.map {
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

            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "⏱ Общее время детекции: ${totalTime}ms")
            Log.d(TAG, "✅ Найдено карт: ${scaled.size}")

            if (scaled.isNotEmpty()) {
                scaled.forEach {
                    Log.d(TAG, "   🃏 ${it.clsName} | Уверенность: ${(it.conf * 100).toInt()}% | Позиция: (${it.x.toInt()}, ${it.y.toInt()})")
                }
            }

            scaled

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка детекции: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun calculateIOU(a: Detection, b: Detection): Float {
        val x1 = max(a.x, b.x)
        val y1 = max(a.y, b.y)
        val x2 = min(a.x + a.w, b.x + b.w)
        val y2 = min(a.y + a.h, b.y + b.h)
        val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
        val union = a.w * a.h + b.w * b.h - inter
        return if (union > 0) inter / union else 0f
    }

    fun close() {
        interpreter?.close()
        Log.d(TAG, "Детектор закрыт")
    }
}