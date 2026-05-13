package com.example.test


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var detections: List<YoloTfliteDetector.Detection> = emptyList()
    private val paint = Paint()
    private val textPaint = Paint()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.GREEN

        textPaint.color = Color.GREEN
        textPaint.textSize = 40f
        textPaint.style = Paint.Style.FILL
    }

    fun setDetections(detections: List<YoloTfliteDetector.Detection>) {
        this.detections = detections
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (detection in detections) {
            canvas.drawRect(
                detection.x,
                detection.y,
                detection.x + detection.w,
                detection.y + detection.h,
                paint
            )

            val text = "${detection.clsName} ${(detection.conf * 100).toInt()}%"
            canvas.drawText(text, detection.x, detection.y - 10, textPaint)
        }
    }
}