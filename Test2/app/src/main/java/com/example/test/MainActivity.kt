package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
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
    private lateinit var lastCardTextView: TextView
    private lateinit var scanButton: Button
    private lateinit var resetButton: Button
    private lateinit var finishButton: Button
    private lateinit var removeLastButton: Button
    private lateinit var detector: YoloTfliteDetector
    private lateinit var centerFrameView: View

    private var currentBitmap: Bitmap? = null
    private var isScanning = false
    private val scannedCards = mutableListOf<YoloTfliteDetector.Detection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        detector = YoloTfliteDetector(this)
        if (checkPermission()) startCamera() else requestPermission()
    }

    private fun setupUI() {
        previewView = PreviewView(this)

        val titleTextView = TextView(this).apply {
            text = "UNO СЧЁТЧИК ОЧКОВ"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x80000000.toInt())
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        val cameraContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.5f
            )
            addView(previewView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }

        centerFrameView = object : View(this) {
            private val paint = android.graphics.Paint().apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 8f
                color = android.graphics.Color.GREEN
            }

            override fun onDraw(canvas: android.graphics.Canvas) {
                super.onDraw(canvas)
                val size = width.coerceAtMost(height) * 0.5f
                val left = (width - size) / 2
                val top = (height - size) / 2
                canvas.drawRect(left, top, left + size, top + size, paint)
            }
        }
        cameraContainer.addView(centerFrameView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        lastCardTextView = TextView(this).apply {
            text = "🎯 ПОМЕСТИТЕ КАРТУ В РАМКУ\n👇 НАЖМИТЕ СКАНИРОВАТЬ"
            textSize = 14f
            setTextColor(0xFFFFFF00.toInt())
            setBackgroundColor(0x88000000.toInt())
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        scanButton = Button(this).apply {
            text = "🔍 СКАНИРОВАТЬ"
            textSize = 20f
            setOnClickListener { performDetection() }
        }

        removeLastButton = Button(this).apply {
            text = "🗑️ УДАЛИТЬ ПОСЛЕДНЮЮ"
            textSize = 18f
            setOnClickListener { removeLastCard() }
        }

        resetButton = Button(this).apply {
            text = "🔄 СБРОСИТЬ ВСЁ"
            textSize = 18f
            setOnClickListener { resetAll() }
        }

        finishButton = Button(this).apply {
            text = "✅ ЗАВЕРШИТЬ"
            textSize = 20f
            setOnClickListener { finishScanning() }
        }

        textView = TextView(this).apply {
            text = "Нет карт"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x88000000.toInt())
            setPadding(16, 16, 16, 16)
            textSize = 14f
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.8f
            )
            addView(textView)
        }

        val buttonRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(scanButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(finishButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val buttonRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(removeLastButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(resetButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val buttonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(buttonRow1)
            addView(buttonRow2)
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleTextView)
            addView(cameraContainer)
            addView(lastCardTextView)
            addView(buttonsContainer)
            addView(scrollView)
        }

        setContentView(mainLayout)
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Камера не разрешена", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                currentBitmap = imageProxyToBitmap(imageProxy)
                imageProxy.close()
            }
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            showBigToast("✅ Камера готова")
        }, ContextCompat.getMainExecutor(this))
    }

    private fun showBigToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    private fun performDetection() {
        if (isScanning) return

        val bitmap = currentBitmap ?: run {
            showBigToast("❌ Нет изображения")
            return
        }

        isScanning = true
        scanButton.isEnabled = false
        lastCardTextView.text = "🔍 СКАНИРОВАНИЕ..."

        Thread {
            try {
                val results = detector.detect(bitmap)
                val bestCard = results.maxByOrNull { it.conf }

                Handler(Looper.getMainLooper()).post {
                    if (bestCard != null && bestCard.conf > 0.08f) {
                        Log.d(TAG, "=== РАСПОЗНАНА КАРТА ===")
                        Log.d(TAG, "ID: ${bestCard.cls}")
                        Log.d(TAG, "Название: ${bestCard.clsName}")
                        Log.d(TAG, "Уверенность: ${(bestCard.conf * 100).toInt()}%")

                        // Получаем очки по ID (быстро)
                        val score = CardMapping.getScoreById(bestCard.cls)

                        scannedCards.add(bestCard)
                        updateUI()
                        showBigToast("✅ ${bestCard.clsName} +$score очков")
                        lastCardTextView.text = "📷 ПОСЛЕДНЯЯ: ${bestCard.clsName}"
                    } else {
                        showBigToast("❌ КАРТА НЕ РАСПОЗНАНА\nПоместите карту в рамку")
                        lastCardTextView.text = "🎯 Поместите карту в рамку"
                    }
                    isScanning = false
                    scanButton.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    showBigToast("❌ Ошибка сканирования")
                    isScanning = false
                    scanButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun removeLastCard() {
        if (scannedCards.isNotEmpty()) {
            val removed = scannedCards.removeAt(scannedCards.size - 1)
            updateUI()
            showBigToast("🗑️ Удалена: ${removed.clsName}")
            if (scannedCards.isNotEmpty()) {
                lastCardTextView.text = "📷 ПОСЛЕДНЯЯ: ${scannedCards.last().clsName}"
            } else {
                lastCardTextView.text = "🎯 ПОМЕСТИТЕ КАРТУ В РАМКУ\n👇 НАЖМИТЕ СКАНИРОВАТЬ"
            }
        } else {
            showBigToast("⚠️ Нет карт для удаления")
        }
    }

    private fun updateUI() {
        val sb = StringBuilder()

        if (scannedCards.isEmpty()) {
            sb.append("═══════════════════════════\n")
            sb.append("📋 СПИСОК КАРТ\n")
            sb.append("═══════════════════════════\n\n")
            sb.append("Пока нет карт\n\n")
            sb.append("1. Поместите карту в рамку\n")
            sb.append("2. Нажмите СКАНИРОВАТЬ\n")
            sb.append("3. Повторите для следующей")
        } else {
            sb.append("═══════════════════════════\n")
            sb.append("📋 СПИСОК КАРТ (${scannedCards.size})\n")
            sb.append("═══════════════════════════\n\n")

            scannedCards.forEachIndexed { index, card ->
                // Получаем очки по ID (быстро)
                val score = CardMapping.getScoreById(card.cls)
                sb.append("${index + 1}. ${card.clsName}\n")
                sb.append("   💰 $score очков\n")
                if (index < scannedCards.size - 1) sb.append("   ───────────\n")
            }

            // Подсчет суммы через ID
            val totalScore = scannedCards.sumOf { CardMapping.getScoreById(it.cls) }
            sb.append("\n═══════════════════════════\n")
            sb.append("💰 ВСЕГО ОЧКОВ: $totalScore\n")
            sb.append("═══════════════════════════\n")
            sb.append("🎨 Цвет и тип карты показаны выше")
        }

        textView.text = sb.toString()
        (textView.parent as? ScrollView)?.post {
            (textView.parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun finishScanning() {
        if (scannedCards.isEmpty()) {
            showBigToast("⚠️ Нет отсканированных карт")
            return
        }

        val totalScore = scannedCards.sumOf { CardMapping.getScoreById(it.cls) }
        val cardList = scannedCards.joinToString(", ") { it.clsName }

        showBigToast("🏆 ИТОГО: $totalScore очков\n📋 ${scannedCards.size} карт")

        Log.d(TAG, "=== ФИНАЛЬНЫЙ РЕЗУЛЬТАТ ===")
        Log.d(TAG, "Карты: $cardList")
        Log.d(TAG, "Очки: $totalScore")
    }

    private fun resetAll() {
        scannedCards.clear()
        updateUI()
        lastCardTextView.text = "🎯 ПОМЕСТИТЕ КАРТУ В РАМКУ\n👇 НАЖМИТЕ СКАНИРОВАТЬ"
        showBigToast("🔄 Все карты удалены")
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
            Log.e(TAG, "Ошибка: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }
}