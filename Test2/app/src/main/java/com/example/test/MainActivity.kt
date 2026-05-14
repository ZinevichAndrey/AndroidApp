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
import android.view.ViewGroup
import android.widget.Button
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

    private var currentBitmap: Bitmap? = null
    private var isScanning = false
    private val scannedCards = mutableListOf<YoloTfliteDetector.Detection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI
        previewView = PreviewView(this)

        // Заголовок
        val titleTextView = TextView(this).apply {
            text = "UNO СЧЁТЧИК ОЧКОВ"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x80000000.toInt())
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // Экран камеры с фиксированной высотой
        val cameraContainer = android.widget.FrameLayout(this).apply {
            addView(previewView, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            ))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.5f  // Камера занимает больше места
            )
        }

        // Статус и последняя карта
        lastCardTextView = TextView(this).apply {
            text = "📷 Наведите камеру на карту"
            textSize = 18f
            setTextColor(0xFFFFFF00.toInt())
            setBackgroundColor(0x88000000.toInt())
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // Кнопки
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

        // Список отсканированных карт (ScrollView)
        textView = TextView(this).apply {
            text = "Нет карт"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x88000000.toInt())
            setPadding(16, 16, 16, 16)
            textSize = 14f
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                0.8f  // Список карт занимает фиксированную долю места
            )
            addView(textView)
        }

        // Кнопки в два ряда
        val buttonRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(scanButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(finishButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val buttonRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(removeLastButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(resetButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val buttonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(buttonRow1)
            addView(buttonRow2)
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(titleTextView)
            addView(cameraContainer)
            addView(lastCardTextView)
            addView(buttonsContainer)
            addView(scrollView)
        }

        setContentView(mainLayout)

        detector = YoloTfliteDetector(this)

        if (checkPermission()) {
            startCamera()
        } else {
            requestPermission()
        }
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
            Log.d(TAG, "✅ Камера запущена")

        }, ContextCompat.getMainExecutor(this))
    }

    private fun showBigToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun performDetection() {
        if (isScanning) return

        val bitmap = currentBitmap
        if (bitmap == null) {
            showBigToast("❌ Нет изображения")
            return
        }

        isScanning = true
        scanButton.isEnabled = false
        lastCardTextView.text = "🔍 СКАНИРОВАНИЕ..."

        Thread {
            try {
                val startTime = System.currentTimeMillis()
                val results = detector.detect(bitmap)
                val elapsed = System.currentTimeMillis() - startTime

                val bestCard = results.maxByOrNull { it.conf }

                Handler(Looper.getMainLooper()).post {
                    if (bestCard != null && bestCard.conf > 0.5f) {
                        scannedCards.add(bestCard)
                        updateUI()

                        val score = CardMapping.getScore(bestCard.clsName)
                        showBigToast("✅ ${bestCard.clsName} +$score очков")

                        lastCardTextView.text = "📷 ПОСЛЕДНЯЯ: ${bestCard.clsName} (${(bestCard.conf * 100).toInt()}%)"
                        Log.d(TAG, "✅ ${bestCard.clsName} | ${(bestCard.conf * 100).toInt()}% | ${elapsed}ms")
                    } else {
                        showBigToast("❌ КАРТА НЕ РАСПОЗНАНА")
                        lastCardTextView.text = "📷 Карта не распознана"
                        Log.d(TAG, "❌ Не распознано")
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
                lastCardTextView.text = "📷 Нет карт"
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
            sb.append("1. Поднесите карту к камере\n")
            sb.append("2. Нажмите СКАНИРОВАТЬ\n")
            sb.append("3. Повторите для следующей")
        } else {
            sb.append("═══════════════════════════\n")
            sb.append("📋 СПИСОК КАРТ (${scannedCards.size})\n")
            sb.append("═══════════════════════════\n\n")

            scannedCards.forEachIndexed { index, card ->
                val score = CardMapping.getScore(card.clsName)
                sb.append("${index + 1}. ${card.clsName}\n")
                sb.append("   💰 $score очков\n")
                if (index < scannedCards.size - 1) sb.append("   ───────────\n")
            }

            val totalScore = scannedCards.sumOf { CardMapping.getScore(it.clsName) }
            sb.append("\n═══════════════════════════\n")
            sb.append("💰 ВСЕГО ОЧКОВ: $totalScore\n")
        }

        textView.text = sb.toString()
        // Прокручиваем ScrollView вниз, чтобы показать последнюю карту
        (textView.parent as? ScrollView)?.post {
            (textView.parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun finishScanning() {
        if (scannedCards.isEmpty()) {
            showBigToast("⚠️ Нет отсканированных карт")
            return
        }

        val totalScore = scannedCards.sumOf { CardMapping.getScore(it.clsName) }
        val cardList = scannedCards.joinToString(", ") { it.clsName }

        showBigToast("🏆 ИТОГО: $totalScore очков (${scannedCards.size} карт)")

        Log.d(TAG, "=== ФИНАЛЬНЫЙ РЕЗУЛЬТАТ ===")
        Log.d(TAG, "Карты: $cardList")
        Log.d(TAG, "Очки: $totalScore")
    }

    private fun resetAll() {
        scannedCards.clear()
        updateUI()
        lastCardTextView.text = "📷 Наведите камеру на карту"
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