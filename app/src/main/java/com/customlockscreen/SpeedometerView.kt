package com.customlockscreen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentSpeed = 0f
    private var maxSpeed = 260f
    private var listener: ((Float, Boolean) -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B1220")
        style = Paint.Style.FILL
    }

    private val outerArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F2937")
        style = Paint.Style.STROKE
        strokeWidth = 28f
        strokeCap = Paint.Cap.ROUND
    }

    private val progressArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#34C759")
        style = Paint.Style.STROKE
        strokeWidth = 28f
        strokeCap = Paint.Cap.ROUND
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5E7EB")
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4E50")
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 6f
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F9FAFB")
        textAlign = Paint.Align.CENTER
        textSize = 30f
        isFakeBoldText = true
    }

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F3F4F6")
        style = Paint.Style.FILL
    }

    fun setOnSpeedChangedListener(listener: (Float, Boolean) -> Unit) {
        this.listener = listener
    }

    fun setSpeed(value: Float) {
        currentSpeed = value.coerceIn(0f, maxSpeed)
        invalidate()
        val unlocked = currentSpeed >= maxSpeed * 0.98f
        listener?.invoke(currentSpeed, unlocked)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f + 18f
        val radius = minOf(width, height) * 0.38f

        val arcRect = RectF(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        canvas.drawOval(arcRect, bgPaint)
        canvas.drawArc(arcRect, 225f, 270f, false, outerArcPaint)

        val progressAngle = (currentSpeed / maxSpeed) * 270f
        canvas.drawArc(arcRect, 225f, progressAngle, false, progressArcPaint)

        for (i in 0..10) {
            val angle = 225f + (i * 27f)
            val rad = Math.toRadians(angle.toDouble())
            val innerX = cx + cos(rad) * (radius - 38)
            val innerY = cy + sin(rad) * (radius - 38)
            val outerX = cx + cos(rad) * (radius - 10)
            val outerY = cy + sin(rad) * (radius - 10)

            canvas.drawLine(innerX.toFloat(), innerY.toFloat(), outerX.toFloat(), outerY.toFloat(), tickPaint)

            val labelValue = ((i.toFloat() / 10f) * maxSpeed).roundToInt()
            val labelRad = Math.toRadians((angle + 2f).toDouble())
            val labelX = cx + cos(labelRad) * (radius - 68)
            val labelY = cy + sin(labelRad) * (radius - 68)

            canvas.drawText(labelValue.toString(), labelX.toFloat(), labelY.toFloat(), textPaint)
        }

        val needleAngle = 225f + (currentSpeed / maxSpeed) * 270f
        val needleRad = Math.toRadians(needleAngle.toDouble())
        val needleLength = radius * 0.72f

        val needleEndX = cx + cos(needleRad) * needleLength
        val needleEndY = cy + sin(needleRad) * needleLength

        canvas.drawLine(cx, cy, needleEndX.toFloat(), needleEndY.toFloat(), needlePaint)
        canvas.drawCircle(cx, cy, 26f, centerPaint)
        canvas.drawCircle(cx, cy, 14f, markerPaint)
        canvas.drawText("${currentSpeed.roundToInt()} km/h", cx, cy + 100f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val speed = touchToSpeed(event.x, event.y)
                setSpeed(speed)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (currentSpeed >= maxSpeed * 0.98f) {
                    listener?.invoke(currentSpeed, true)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun touchToSpeed(x: Float, y: Float): Float {
        val cx = width / 2f
        val cy = height / 2f + 18f
        val dx = x - cx
        val dy = y - cy
        val rawDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        val minAngle = -150f
        val maxAngle = 30f
        val clamped = rawDeg.coerceIn(minAngle, maxAngle)
        val normalized = (clamped - minAngle) / (maxAngle - minAngle)
        return (normalized * maxSpeed).coerceIn(0f, maxSpeed)
    }
}
