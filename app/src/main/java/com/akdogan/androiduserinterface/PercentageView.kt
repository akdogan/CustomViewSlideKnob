package com.akdogan.androiduserinterface

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import kotlin.math.roundToInt

class PercentageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var goal: Int = 1
        set(value) {
            field = value
            calculatePercentage()
            invalidate()
        }
    var progress: Int = 0
        set(value) {
            field = value
            calculatePercentage()
            invalidate()
        }
    var percentage = 0

    var goalColor = getThemeColor(context, R.attr.colorPrimary, Color.BLACK)
        set(value) {
            field = value
            goalPaint.color = value
            invalidate()
        }
    var progressColor = getThemeColor(context, R.attr.colorSecondary, Color.LTGRAY)
        set(value) {
            field = value
            progressPaint.color = value
            invalidate()
        }
    var textColor = goalColor
        set(value) {
            field = value
            textPaint.color = value
            invalidate()
        }


    private var goalRect = RectF(0f, 0f, 0f, 0f)

    private var goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = goalColor
    }

    private var progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = progressColor
    }

    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        textSize = 100f
        typeface = Typeface.DEFAULT_BOLD
        color = textColor
    }

    init {
        calculatePercentage()
    }

    companion object {
        val marginStart = 150
        val marginEnd = 50
        val textSize = 50
    }

    private fun calculatePercentage() {
        percentage = try {
            ((progress.toFloat() / goal.toFloat()) * 100).roundToInt()
        } catch (e: ArithmeticException) {
            0
        }
    }

    @ColorInt
    private fun getThemeColor(
        context: Context,
        @AttrRes attr: Int,
        @ColorInt default: Int
    ): Int {
        val typedVal = TypedValue()
        return if (context.theme.resolveAttribute(attr, typedVal, true)) {
            typedVal.data
        } else {
            default
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRoundRect(goalRect, 5f, 5f, goalPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.i("SIZING", "Width: $w, Height: $h")
        goalRect = RectF(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingRight).toFloat()
        )
    }
}