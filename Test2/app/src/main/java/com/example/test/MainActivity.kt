package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var previewView: PreviewView
    private lateinit var textView: TextView
    private lateinit var detector: YoloTfliteDetector
    private var frameCount = 0
    private var detectionBuffer: List<List<YoloTfliteDetector.Detection>> = emptyList()  // ← ИСПРАВЛЕНО
    private val bufferSize = 5
    private var confirmedCards = mutableListOf<YoloTfliteDetector.Detection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "=== ПРИЛОЖЕНИЕ ЗАПУЩЕНО ===")

        // Создаём UI
        previewView = PreviewView(this)
        textView = TextView(this).apply {
            text = "Ожидание..."
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x80000000.toInt())
            setPadding(50, 50, 50, 50)
            textSize = 16f
        }

        val layout = android.widget.FrameLayout(this)
        layout.addView(previewView, android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ))
        layout.addView(textView, android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
            topMargin = 100
        })

        setContentView(layout)

        detector = YoloTfliteDetector(this)
        Log.d(TAG, "Детектор инициализирован")

        if (checkPermission()) {
            startCamera()
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Разрешение на камеру: $hasPermission")
        return hasPermission
    }

    private fun requestPermission() {
        Log.d(TAG, "Запрос разрешения на камеру")
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "✅ Разрешение получено")
            startCamera()
        } else {
            Log.e(TAG, "❌ Разрешение не получено")
            Toast.makeText(this, "Камера не разрешена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        Log.d(TAG, "Запуск камеры...")
        val cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            Log.d(TAG, "✅ CameraProvider получен")

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                frameCount++
                val bitmap = imageProxyToBitmap(imageProxy)
                if (bitmap != null) {
                    val results = detector.detect(bitmap)

                    // Логируем каждый кадр
                    if (results.isNotEmpty()) {
                        Log.d(TAG, "📸 Кадр #$frameCount | Найдено карт: ${results.size}")
                        results.forEach { detection ->
                            Log.d(TAG, "   Карта: ${detection.clsName} | Уверенность: ${(detection.conf * 100).toInt()}% | Позиция: (${detection.x.toInt()}, ${detection.y.toInt()})")
                        }
                    }

                    // Стабилизация детекций (ИСПРАВЛЕНО)
                    detectionBuffer = (detectionBuffer + listOf(results)).takeLast(bufferSize)

                    if (detectionBuffer.size == bufferSize) {
                        val stableCards = getStableCards()
                        if (stableCards.isNotEmpty()) {
                            confirmedCards = stableCards.toMutableList()
                            val totalScore = confirmedCards.sumOf { CardMapping.getScore(it.clsName) }
                            Log.d(TAG, "🎯 ПОДТВЕРЖДЁННЫЕ КАРТЫ: ${confirmedCards.size}")
                            confirmedCards.forEach {
                                Log.d(TAG, "   ${it.clsName} | Уверенность: ${(it.conf * 100).toInt()}% | Очки: ${CardMapping.getScore(it.clsName)}")
                            }
                            Log.d(TAG, "💰 ОБЩАЯ СУММА ОЧКОВ: $totalScore")
                        }
                    }

                    runOnUiThread {
                        updateUI(results, confirmedCards)
                    }
                }
                imageProxy.close()
            }

            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)

            Toast.makeText(this, "Камера работает!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "✅ Камера запущена, ожидание кадров...")

        }, ContextCompat.getMainExecutor(this))
    }

    private fun getStableCards(): List<YoloTfliteDetector.Detection> {
        val allCards = detectionBuffer.flatten()
        val grouped = allCards.groupBy { it.cls }

        val stable = mutableListOf<YoloTfliteDetector.Detection>()
        for ((_, cards) in grouped) {
            if (cards.size >= bufferSize * 0.7) {
                val bestCard = cards.maxByOrNull { it.conf }
                if (bestCard != null) {
                    stable.add(bestCard)
                }
            }
        }
        return stable
    }

    private fun updateUI(temporary: List<YoloTfliteDetector.Detection>, confirmed: List<YoloTfliteDetector.Detection>) {
        val sb = StringBuilder()
        sb.append("═══ UNO СКАНЕР ═══\n")
        sb.append("📊 Кадров: $frameCount\n")
        sb.append("────────────────\n")
        sb.append("🃏 ВРЕМЕННЫЕ КАРТЫ:\n")
        if (temporary.isEmpty()) {
            sb.append("   Нет\n")
        } else {
            temporary.forEach {
                sb.append("   ${it.clsName} (${(it.conf * 100).toInt()}%)\n")
            }
        }
        sb.append("────────────────\n")
        sb.append("✅ ПОДТВЕРЖДЁННЫЕ:\n")
        if (confirmed.isEmpty()) {
            sb.append("   Нет\n")
        } else {
            confirmed.forEach {
                val score = CardMapping.getScore(it.clsName)
                sb.append("   ${it.clsName} | ${score} очков\n")
            }
            val totalScore = confirmed.sumOf { CardMapping.getScore(it.clsName) }
            sb.append("────────────────\n")
            sb.append("💰 ОЧКИ: $totalScore\n")
        }
        textView.text = sb.toString()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val image = imageProxy.image ?: return null
            val planes = image.planes
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
            val jpegData = out.toByteArray()

            val bitmap = android.graphics.BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)

            val matrix = Matrix()
            matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка конвертации: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
        Log.d(TAG, "=== ПРИЛОЖЕНИЕ ОСТАНОВЛЕНО ===")
    }
}