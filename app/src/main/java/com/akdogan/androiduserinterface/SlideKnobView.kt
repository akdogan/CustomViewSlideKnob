package com.akdogan.androiduserinterface

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class SlideKnobView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {



    @ColorInt
    var sliderColor = 0
        set(value) {
            field = value
            sliderPaint.color = value
            invalidate()
        }
    @ColorInt
    var knobColor = 0
        set(value) {
            field = value
            knobPaint.color = value
            invalidate()
        }
    @ColorInt
    var knobFocusColor = 0
        set(value) {
            field = value
            knobFocusPaint.color = value
            invalidate()
        }
    @ColorInt
    var textColor = 0
        set(value) {
            field = value
            textPaint.color = value
            invalidate()
        }

    private lateinit var settingPoints : List<Int>
    private var currentFocus = 0

    private fun moveToNext(){
        currentFocus++
        Log.i("TOUCH", "currentFocus = $currentFocus")
        if (currentFocus >= settingPoints.size) currentFocus = 0
    }


    private var sliderRect = RectF(0f, 0f, 0f, 0f)
    private var leftStart = 0f
    private var rightStart = 0f
    private var topStart = 0f
    private var bottomStart = 0f

    private var sliderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = sliderColor
    }

    private var sliderStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = knobColor
    }

    private var knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = knobWidth
        color = knobColor
    }

    private var knobFocusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = knobFocusWidth
        color = knobFocusColor
    }

    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = internalTextSize
        typeface = Typeface.DEFAULT_BOLD
        color = textColor
    }

    private var customContentDescription = contentDescription ?: ""

    init {
        this.isClickable = true
        context.withStyledAttributes(attrs, R.styleable.SlideKnobView) {
            sliderColor = getColor(R.styleable.SlideKnobView_sliderColor, sliderColorDef)
            knobColor = getColor(R.styleable.SlideKnobView_knobColor, knobColorDef)
            knobFocusColor = getColor(R.styleable.SlideKnobView_knobFocusColor, getThemeColor(
                context,
                R.attr.colorSecondary,
                knobFocusColorDef
            ))
            textColor = getColor(R.styleable.SlideKnobView_textColor, getThemeColor(
                context,
                R.attr.colorOnBackground,
                textColorDef
            ))
            settingPoints = createList(getInt(R.styleable.SlideKnobView_settingCount, settingCountDef))
        }
        // Accessibility
        ViewCompat.setAccessibilityDelegate(this, object: AccessibilityDelegateCompat(){
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    getNextInfoString()
                )
                info.addAction(customClick)
            }
        })


        updateContentDescription()
    }

    private fun getNextInfoString(): String{
        val value = if (currentFocus >= settingPoints.size)  0 else currentFocus + 1
        return resources.getString(R.string.slide_knob_view_next_setting, value)
    }

    private fun createList(elements: Int): List<Int>{
        return (0..elements).toList()
    }



    companion object {
        private const val internalMarginTop = 75
        private const val internalMarginBottom = 20
        private const val internalMarginHorizontal = 20
        private const val internalTextSize = 50f
        private const val sideTrim = 30
        private const val knobWidth = 10f
        private const val knobFocusWidth = 25f
        private const val knobFocusVerticalAdd = (knobFocusWidth - sideTrim) * 2

        // Default Values
        private const val settingCountDef = 5
        private const val sliderColorDef = Color.LTGRAY
        private const val knobColorDef = Color.DKGRAY
        private const val knobFocusColorDef = Color.CYAN
        private const val textColorDef = Color.BLACK

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

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        moveToNext()
        //contentDescription = resources.getString(fanSpeed.label)
        updateContentDescription()
        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas?) {
        fun getX(index: Int): Float {
            val left = leftStart + sideTrim
            val right = rightStart - sideTrim
            return left + ( (right - left) / (settingPoints.size - 1) * index)
        }
        super.onDraw(canvas)
        canvas?.let{
            it.drawRoundRect(sliderRect, 5f, 5f, sliderPaint)
            it.drawRoundRect(sliderRect, 5f, 5f, sliderStrokePaint)
            for (setting in settingPoints){
                val x = getX(setting)
                if (setting == currentFocus){
                    it.drawLine(
                        x,
                        topStart + knobFocusVerticalAdd,
                        x,
                        bottomStart - knobFocusVerticalAdd,
                        knobFocusPaint
                    )
                    it.drawText(
                        setting.toString(),
                        x,
                        topStart + knobFocusVerticalAdd * 2.5f,
                        textPaint
                    )
                }
                it.drawLine(
                    x,
                    topStart,
                    x,
                    bottomStart,
                    knobPaint
                )
            }
        }


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.i("SIZING", "Width: $w, Height: $h")
        leftStart = (0 + paddingLeft + internalMarginHorizontal).toFloat()
        rightStart = (w - paddingRight - internalMarginHorizontal).toFloat()
        topStart = (0 + paddingTop + internalMarginTop).toFloat()
        bottomStart = (h - paddingBottom - internalMarginBottom).toFloat()
        sliderRect = RectF(
            leftStart,
            topStart,
            rightStart,
            bottomStart
        )
    }

    private fun updateContentDescription(){
        contentDescription = "$customContentDescription ${resources.getString(
            R.string.slide_knob_view_content_description,
            currentFocus
        )}"
    }
}