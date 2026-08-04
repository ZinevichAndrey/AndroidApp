package com.example.counterforuno

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class CameraActivity : AppCompatActivity() {
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
        hideSystemUI()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        observeViewModel()
        detector = YoloTfliteDetector(this)

        if (checkPermission()) startCamera() else requestPermission()
    }

    private fun observeViewModel() {
        viewModel.scannedCards.observe(this) {
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
            text = getString(R.string.score_counter)
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
                color = Color.GREEN
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
            text = getString(R.string.scan_camera)
            textSize = 14f
            setTextColor(0xFFFFFF00.toInt())
            setBackgroundColor(0x88000000.toInt())
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        scanButton = Button(this).apply {
            text = getString(R.string.scan)
            textSize = 20f
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { performDetection() }
        }

        removeLastButton = Button(this).apply {
            text = getString(R.string.remove_last)
            textSize = 18f
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { removeLastCard() }
        }

        resetButton = Button(this).apply {
            text = getString(R.string.reset_all)
            textSize = 18f
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { resetAll() }
        }

        finishButton = Button(this).apply {
            text = getString(R.string.finish)
            textSize = 20f
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { finishScanning() }
        }

        textView = TextView(this).apply {
            text = getString(R.string.no_cards)
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(16, 16, 16, 16)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
            )
            setBackgroundColor(Color.DKGRAY)
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
            buttonRow1.setPadding(16, 8, 16, 4)
            buttonRow2.setPadding(16, 4, 16, 8)
            addView(buttonRow1)
            addView(buttonRow2)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
            Toast.makeText(this, getString(R.string.camera_not_permission), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            showBigToast(getString(R.string.camera_ready))
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
            showBigToast(getString(R.string.no_image))
            return
        }

        viewModel.setScanning(true)
        scanButton.isEnabled = false
        viewModel.setLastCardText(getString(R.string.scanning))

        Thread {
            try {
                val results = detector.detect(bitmap)
                val bestCard = results.maxByOrNull { it.conf }
                bestCard?.let { card ->
                    // Исправлено: используем CardMapping как объект
                    card.clsName = CardMapping.getReadableName(card.cls, this)
                }

                Handler(Looper.getMainLooper()).post {
                    if (bestCard != null && bestCard.conf > 0.08f) {
                        handleDetection(bestCard)
                    } else {
                        showBigToast(getString(R.string.card_not_recognized))
                        viewModel.setLastCardText(getString(R.string.place_map))
                        viewModel.setScanning(false)
                        scanButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    showBigToast(getString(R.string.exception_scanning))
                    viewModel.setScanning(false)
                    scanButton.isEnabled = true
                }
            }
        }.start()
    }

    private fun handleDetection(detection: YoloTfliteDetector.Detection) {
        val id = detection.cls

        // Исправлено: CardMapping.isKnown - это функция объекта
        if (!CardMapping.isKnown(id)) {
            showBigToast(getString(R.string.unknown_id) + " $id")
            viewModel.setScanning(false)
            scanButton.isEnabled = true
            viewModel.setLastCardText(getString(R.string.unknown_card) + "(ID: $id)")
            return
        }

        // Исправлено: CardMapping.isAmbiguous - это функция объекта
        if (CardMapping.isAmbiguous(id)) {
            showCardSelectionDialog(id, detection)
            return
        }

        // Исправлено: CardMapping.getCard - это функция объекта
        val card = CardMapping.getCard(id)
        if (card != null) {
            // Исправлено: используем CardMapping.formatCardForDisplay
            val cardName = detection.clsName.ifEmpty {
                CardMapping.formatCardForDisplay(card, this)
            }

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

            addCardToScanned(detectionWithScore, cardName, card.type.value)
        } else {
            showBigToast(getString(R.string.card_not_found))
            viewModel.setScanning(false)
            scanButton.isEnabled = true
        }
    }

    private fun showCardSelectionDialog(id: Int, detection: YoloTfliteDetector.Detection) {
        viewModel.setWaitingForChoice(true)
        viewModel.setScanning(false)
        scanButton.isEnabled = true

        // Исправлено: CardMapping.getCards - это функция объекта
        val cards = CardMapping.getCards(id)

        if (cards.isEmpty()) {
            showBigToast(getString(R.string.no_option_choose))
            viewModel.setWaitingForChoice(false)
            scanButton.isEnabled = true
            viewModel.setLastCardText(getString(R.string.scan_camera))
            return
        }

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val infoText = TextView(this).apply {
            text = getString(R.string.correct_option)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@CameraActivity, android.R.color.black))
            setPadding(0, 0, 0, 20)
        }
        dialogView.addView(infoText)

        cards.forEachIndexed { index, card ->
            // Исправлено: используем CardMapping.formatCardForDisplay
            val cardName = CardMapping.formatCardForDisplay(card, this)
            val score = card.type.value
            val buttonText = "$cardName ($score)" + getString(R.string.score)

            val button = Button(this).apply {
                text = buttonText
                textSize = 18f
                setPadding(32, 20, 32, 20)
                setBackgroundColor(ContextCompat.getColor(this@CameraActivity, android.R.color.transparent))
                setTextColor(ContextCompat.getColor(this@CameraActivity, android.R.color.black))
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                tag = index

                setOnClickListener {
                    val selectedIndex = tag as Int
                    val selectedCard = cards[selectedIndex]
                    // Исправлено: используем CardMapping.formatCardForDisplay
                    val selectedCardName = CardMapping.formatCardForDisplay(selectedCard, this@CameraActivity)
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
                    setBackgroundColor(ContextCompat.getColor(this@CameraActivity, android.R.color.darker_gray))
                }
                dialogView.addView(divider)
            }

            dialogView.addView(button)
        }

        val cancelButton = Button(this).apply {
            text = getString(R.string.cancel)
            textSize = 16f
            setPadding(32, 20, 32, 20)
            setBackgroundColor(ContextCompat.getColor(this@CameraActivity, android.R.color.transparent))
            setTextColor(ContextCompat.getColor(this@CameraActivity, android.R.color.holo_red_dark))
            gravity = Gravity.CENTER
            setOnClickListener {
                currentDialog?.dismiss()
                currentDialog = null
                showBigToast(getString(R.string.scanning_cancel))
                viewModel.setWaitingForChoice(false)
                scanButton.isEnabled = true
                viewModel.setLastCardText(getString(R.string.scan_camera))
            }
        }

        val divider2 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            setBackgroundColor(ContextCompat.getColor(this@CameraActivity, android.R.color.darker_gray))
        }
        dialogView.addView(divider2)
        dialogView.addView(cancelButton)

        currentDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_card))
            .setView(dialogView)
            .setCancelable(false)
            .create()

        currentDialog?.show()
    }

    private fun addCardToScanned(detection: YoloTfliteDetector.Detection, cardName: String, score: Int) {
        viewModel.addCard(detection)
        updateUI()
        showBigToast("✅ $cardName +$score" + getString(R.string.score))

        viewModel.setScanning(false)
        scanButton.isEnabled = true
    }

    private fun removeLastCard() {
        val removed = viewModel.removeLastCard()
        if (removed != null) {
            updateUI()
            showBigToast(getString(R.string.removed) + " ${removed.clsName}")
        } else {
            showBigToast(getString(R.string.no_cards_delete))
        }
    }

    private fun updateUI() {
        val sb = StringBuilder()

        if (!viewModel.hasCards()) {
            sb.append("═══════════════════════════\n")
            sb.append(getString(R.string.list_of_cards))
            sb.append("═══════════════════════════\n\n")
            sb.append(getString(R.string.no_cards_yet))
            sb.append("1. " + getString(R.string.place_map) + "\n")
            sb.append("2. " + getString(R.string.click_scan))
            sb.append("3. " + getString(R.string.repeat_next_card))
        } else {
            sb.append("═══════════════════════════\n")
            sb.append(getString(R.string.list_of_cards) + " (${viewModel.getCardCount()})\n")
            sb.append("═══════════════════════════\n\n")
            sb.append(viewModel.getCardListString())

            val totalScore = viewModel.getTotalScore()
            sb.append("\n═══════════════════════════\n")
            sb.append(getString(R.string.total_points) + " $totalScore\n")
            sb.append("═══════════════════════════\n")
        }

        textView.text = sb.toString()
        (textView.parent as? ScrollView)?.post {
            (textView.parent as? ScrollView)?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun finishScanning() {
        if (!viewModel.hasCards()) {
            showBigToast(getString(R.string.no_scanned_cards))
            return
        }

        val totalScore = viewModel.getTotalScore()
        val cards = viewModel.scannedCards.value ?: emptyList()

        // Получаем ID игрока из Intent (Long)
        val playerId = intent.getLongExtra("PLAYER_ID", -1L)

        // Создаем Intent для возврата результата
        val resultIntent = Intent().apply {
            putExtra("SCORED_POINTS", totalScore)
            putExtra("CARDS_COUNT", cards.size)
            putExtra("PLAYER_ID", playerId) // Возвращаем Long
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun resetAll() {
        viewModel.clearAll()
        currentDialog?.dismiss()
        currentDialog = null
        updateUI()
        showBigToast(getString(R.string.all_cards_removed))
    }

    override fun onDestroy() {
        super.onDestroy()
        currentDialog?.dismiss()
        currentDialog = null
        detector.close()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}