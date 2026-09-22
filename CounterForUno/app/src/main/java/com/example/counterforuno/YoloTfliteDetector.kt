package com.example.counterforuno

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min


class YoloTfliteDetector(private val context: Context) {
    companion object {
        private const val MODEL_FILENAME = "model.tflite"
        private const val INPUT_SIZE = 640
        private const val CONF_THRESHOLD = 0.12f
        private const val NMS_THRESHOLD = 0.45f
    }

    data class Detection(
        var x: Float,
        var y: Float,
        var w: Float,
        var h: Float,
        var conf: Float,
        var cls: Int,
        var clsName: String = "",
        var score: Int = 0
    )

    private var interpreter: Interpreter? = null
    private var originalWidth = 0
    private var originalHeight = 0

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelFile = File(context.cacheDir, MODEL_FILENAME)
            if (!modelFile.exists()) {
                context.assets.open(MODEL_FILENAME).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            interpreter = Interpreter(modelFile)
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<Detection> {
        originalWidth = bitmap.width
        originalHeight = bitmap.height
        if (interpreter == null) {
            return emptyList()
        }
        return try {
            val size = min(bitmap.width, bitmap.height)
            val startX = (bitmap.width - size) / 2
            val startY = (bitmap.height - size) / 2
            val centeredBitmap = Bitmap.createBitmap(bitmap, startX, startY, size, size)
            val inputBitmap = Bitmap.createScaledBitmap(centeredBitmap, INPUT_SIZE, INPUT_SIZE, true)
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
            val detections = parseYoloOutput(outputBuffer)

            detections.map {
                Detection(
                    x = (it.x * size + startX).coerceIn(0f, originalWidth.toFloat()),
                    y = (it.y * size + startY).coerceIn(0f, originalHeight.toFloat()),
                    w = it.w * size,
                    h = it.h * size,
                    conf = it.conf,
                    cls = it.cls,
                    clsName = "",
                    score = 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseYoloOutput(outputArray: Array<Array<FloatArray>>): List<Detection> {
        val detections = mutableListOf<Detection>()
        try {
            val rawOutput = outputArray[0]
            val numPredictions = rawOutput[0].size
            var globalMax = 0f
            for (predIdx in 0 until numPredictions) {
                for (c in 4 until rawOutput.size) {
                    val value = rawOutput[c][predIdx]
                    if (value > globalMax) globalMax = value
                }
            }
            for (predIdx in 0 until numPredictions) {
                val cx = rawOutput[0][predIdx]
                val cy = rawOutput[1][predIdx]
                val w = rawOutput[2][predIdx]
                val h = rawOutput[3][predIdx]

                if (w < 0.05f || w > 0.9f || h < 0.05f || h > 0.9f) {
                    continue
                }

                var maxConf = 0f
                var bestClass = -1

                for (c in 4 until rawOutput.size) {
                    var conf = rawOutput[c][predIdx]
                    if (globalMax > 0) {
                        conf = conf / globalMax
                    }
                    if (conf > maxConf) {
                        maxConf = conf
                        bestClass = c - 4
                    }
                }

                if (maxConf > CONF_THRESHOLD && bestClass >= 0) {
                    val x = (cx - w / 2f).coerceIn(0f, 1f)
                    val y = (cy - h / 2f).coerceIn(0f, 1f)
                    detections.add(
                        Detection(
                            x = x,
                            y = y,
                            w = w,
                            h = h,
                            conf = maxConf,
                            cls = bestClass,
                            clsName = "",
                            score = 0
                        )
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return applyNMS(detections)
    }

    private fun applyNMS(detections: List<Detection>): List<Detection> {
        if (detections.isEmpty()) {
            return emptyList()
        }

        val sorted = detections.sortedByDescending { it.conf }
        val result = mutableListOf<Detection>()
        val suppressed = BooleanArray(sorted.size)

        for (i in sorted.indices) {
            if (suppressed[i]) continue
            val current = sorted[i]
            result.add(current)
            for (j in i + 1 until sorted.size) {
                if (suppressed[j]) continue
                val iou = calculateIOU(current, sorted[j])
                if (iou > NMS_THRESHOLD) suppressed[j] = true
            }
        }

        return result
    }

    private fun calculateIOU(box1: Detection, box2: Detection): Float {
        val x1 = max(box1.x, box2.x)
        val y1 = max(box1.y, box2.y)
        val x2 = min(box1.x + box1.w, box2.x + box2.w)
        val y2 = min(box1.y + box1.h, box2.y + box2.h)
        val intersection = max(0f, x2 - x1) * max(0f, y2 - y1)
        val area1 = box1.w * box1.h
        val area2 = box2.w * box2.h
        val union = area1 + area2 - intersection
        return if (union > 0) intersection / union else 0f
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }



}