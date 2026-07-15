package com.example.test

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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

    private lateinit var viewModel: MainViewModel
    private var currentDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observeViewModel()

        detector = YoloTfliteDetector(this)
        if (checkPermission()) startCamera() else requestPermission()
    }

    private fun observeViewModel() {
        viewModel.scannedCards.observe(this) { cards ->
            updateUI()
        }

        viewModel.isScanning.observe(this) { isScanning ->
            scanButton.isEnabled = !isScanning && !viewModel.isWaitingForChoice.value!!
        }

        viewModel.lastCardText.observe(this) { text ->
            lastCardTextView.text = text
        }
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            showBigToast("✅ Камера готова")
            Log.d(TAG, "✅ Камера запущена")
        }, ContextCompat.getMainExecutor(this))
    }

    private fun showBigToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    private fun performDetection() {
        if (viewModel.isScanning.value == true || viewModel.isWaitingForChoice.value == true) return

        currentDialog?.dismiss()
        currentDialog = null

        val bitmap = previewView.bitmap
        if (bitmap == null) {
            showBigToast("❌ Нет изображения")
            return
        }

        viewModel.setScanning(true)
        scanButton.isEnabled = false
        viewModel.setLastCardText("🔍 СКАНИРОВАНИЕ...")

        Thread {
            try {
                val results = detector.detect(bitmap)

                // ЗАПОЛНЯЕМ ИМЯ КАРТЫ С УЧЕТОМ ЯЗЫКА
                val bestCard = results.maxByOrNull { it.conf }
                bestCard?.let { card ->
                    card.clsName = CardMapping.getReadableName(card.cls, this)
                }

                Handler(Looper.getMainLooper()).post {
                    if (bestCard != null && bestCard.conf > 0.08f) {
                        handleDetection(bestCard)
                    } else {
                        showBigToast("❌ КАРТА НЕ РАСПОЗНАНА")
                        viewModel.setLastCardText("🎯 Поместите карту в рамку")
                        viewModel.setScanning(false)
                        scanButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    showBigToast("❌ Ошибка сканирования")
                    viewModel.setScanning(false)
                    scanButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun handleDetection(detection: YoloTfliteDetector.Detection) {
        val id = detection.cls
        Log.d(TAG, "📊 ID: $id")

        if (!CardMapping.isKnown(id)) {
            showBigToast("❌ Неизвестный ID: $id")
            viewModel.setScanning(false)
            scanButton.isEnabled = true
            viewModel.setLastCardText("❌ Неизвестная карта (ID: $id)")
            return
        }

        if (CardMapping.isAmbiguous(id)) {
            Log.d(TAG, "🤔 Неоднозначный ID: ${CardMapping.getReadableNames(id, this)}")
            showCardSelectionDialog(id, detection)
            return
        }

        val card = CardMapping.getCard(id)
        if (card != null) {
            // ИСПОЛЬЗУЕМ УЖЕ ЗАПОЛНЕННОЕ ИМЯ
            val cardName = detection.clsName.ifEmpty { CardMapping.formatCardForDisplay(card, this) }

            val detectionWithScore = YoloTfliteDetector.Detection(
                x = detection.x,
                y = detection.y,
                w = detection.w,
                h = detection.h,
                conf = detection.conf,
                cls = id,
                clsName = cardName,
                score = card.type.value
            )

            Log.d(TAG, "✅ Одна карта: $cardName (${card.type.value} очков)")
            addCardToScanned(detectionWithScore, cardName, card.type.value)
        } else {
            showBigToast("❌ Карта не найдена")
            viewModel.setScanning(false)
            scanButton.isEnabled = true
        }
    }

    private fun showCardSelectionDialog(id: Int, detection: YoloTfliteDetector.Detection) {
        viewModel.setWaitingForChoice(true)
        viewModel.setScanning(false)
        scanButton.isEnabled = true

        val cards = CardMapping.getCards(id)

        Log.d(TAG, "========================================")
        Log.d(TAG, "🤔 ПОКАЗ ДИАЛОГА ВЫБОРА")
        Log.d(TAG, "ID: $id")
        Log.d(TAG, "Количество вариантов: ${cards.size}")

        if (cards.isEmpty()) {
            showBigToast("❌ Нет вариантов для выбора")
            viewModel.setWaitingForChoice(false)
            scanButton.isEnabled = true
            viewModel.setLastCardText("🎯 ПОМЕСТИТЕ КАРТУ В РАМКУ\n👇 НАЖМИТЕ СКАНИРОВАТЬ")
            return
        }

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val infoText = TextView(this).apply {
            text = "Выберите правильный вариант:"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(infoText)

        cards.forEachIndexed { index, card ->

            val cardName = CardMapping.formatCardForDisplay(card, this)
            val score = card.type.value
            val buttonText = "$cardName ($score очков)"

            val button = Button(this).apply {
                text = buttonText
                textSize = 18f
                setPadding(32, 20, 32, 20)
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.transparent))
                setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                tag = index

                setOnClickListener {
                    val selectedIndex = tag as Int
                    val selectedCard = cards[selectedIndex]
                    val selectedCardName = CardMapping.formatCardForDisplay(selectedCard, this@MainActivity)
                    val selectedScore = selectedCard.type.value

                    val selectedDetection = YoloTfliteDetector.Detection(
                        x = detection.x,
                        y = detection.y,
                        w = detection.w,
                        h = detection.h,
                        conf = detection.conf,
                        cls = id,
                        clsName = selectedCardName,
                        score = selectedScore
                    )

                    currentDialog?.dismiss()
                    currentDialog = null

                    addCardToScanned(selectedDetection, selectedCardName, selectedScore)
                    viewModel.setWaitingForChoice(false)
                    scanButton.isEnabled = true
                }
            }

            if (index > 0) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
                }
                dialogView.addView(divider)
            }

            dialogView.addView(button)
        }

        val cancelButton = Button(this).apply {
            text = "❌ ОТМЕНА"
            textSize = 16f
            setPadding(32, 20, 32, 20)
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.transparent))
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
            gravity = Gravity.CENTER
            setOnClickListener {
                Log.d(TAG, "❌ Выбор отменен")
                currentDialog?.dismiss()
                currentDialog = null

                showBigToast("❌ Сканирование отменено")
                viewModel.setWaitingForChoice(false)
                scanButton.isEnabled = true
                viewModel.setLastCardText("🎯 ПОМЕСТИТЕ КАРТУ В РАМКУ\n👇 НАЖМИТЕ СКАНИРОВАТЬ")
            }
        }

        val divider2 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
        }
        dialogView.addView(divider2)
        dialogView.addView(cancelButton)

        currentDialog = AlertDialog.Builder(this)
            .setTitle("🤔 Выберите карту")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        currentDialog?.show()
        Log.d(TAG, "✅ Кастомный диалог с ${cards.size} вариантами показан")
    }

    private fun addCardToScanned(detection: YoloTfliteDetector.Detection, cardName: String, score: Int) {
        viewModel.addCard(detection)
        updateUI()
        showBigToast("✅ $cardName +$score очков")

        viewModel.setScanning(false)
        scanButton.isEnabled = true
    }

    private fun removeLastCard() {
        val removed = viewModel.removeLastCard()
        if (removed != null) {
            updateUI()
            showBigToast("🗑️ Удалена: ${removed.clsName}")
        } else {
            showBigToast("⚠️ Нет карт для удаления")
        }
    }

    private fun updateUI() {
        val sb = StringBuilder()

        if (!viewModel.hasCards()) {
            sb.append("═══════════════════════════\n")
            sb.append("📋 СПИСОК КАРТ\n")
            sb.append("═══════════════════════════\n\n")
            sb.append("Пока нет карт\n\n")
            sb.append("1. Поместите карту в рамку\n")
            sb.append("2. Нажмите СКАНИРОВАТЬ\n")
            sb.append("3. Повторите для следующей")
        } else {
            sb.append("═══════════════════════════\n")
            sb.append("📋 СПИСОК КАРТ (${viewModel.getCardCount()})\n")
            sb.append("═══════════════════════════\n\n")
            sb.append(viewModel.getCardListString())

            val totalScore = viewModel.getTotalScore()
            sb.append("\n═══════════════════════════\n")
            sb.append("💰 ВСЕГО ОЧКОВ: $totalScore\n")
            sb.append("═══════════════════════════\n")
        }

        textView.text = sb.toString()
        (textView.parent as? ScrollView)?.post {
            (textView.parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun finishScanning() {
        if (!viewModel.hasCards()) {
            showBigToast("⚠️ Нет отсканированных карт")
            return
        }

        val totalScore = viewModel.getTotalScore()
        val cards = viewModel.scannedCards.value ?: emptyList()
        val cardList = cards.joinToString(", ") { it.clsName }

        showBigToast("🏆 ИТОГО: $totalScore очков\n📋 ${cards.size} карт")

        Log.d(TAG, "=== ФИНАЛЬНЫЙ РЕЗУЛЬТАТ ===")
        Log.d(TAG, "Карты: $cardList")
        Log.d(TAG, "Очки: $totalScore")
    }

    private fun resetAll() {
        viewModel.clearAll()
        currentDialog?.dismiss()
        currentDialog = null
        updateUI()
        showBigToast("🔄 Все карты удалены")
    }

    override fun onDestroy() {
        super.onDestroy()
        currentDialog?.dismiss()
        currentDialog = null
        detector.close()
    }
}