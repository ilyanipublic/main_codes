package com.airprint.smart.printer.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.airprint.smart.printer.R

class RippleBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val DEFAULT_RIPPLE_COUNT = 6
    private val DEFAULT_DURATION_TIME = 3000
    private val DEFAULT_SCALE = 6f
    private val DEFAULT_FILL_TYPE = 0

    private val rippleColor: Int
    private var rippleStrokeWidth: Float
    private val rippleRadius: Float
    private val rippleDurationTime: Int
    private val rippleAmount: Int
    private val rippleDelay: Int
    private val rippleScale: Float
    private val rippleType: Int
    private val paint: Paint
    private var animationRunning = false
    private val animatorSet: AnimatorSet
    private val animatorList: ArrayList<Animator>
    private val rippleParams: LayoutParams
    private val rippleViewList = ArrayList<RippleView>()

    init {
        if (isInEditMode)

            if (attrs == null) {
                throw IllegalArgumentException("Attributes should be provided to this view,")
            }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground)
        rippleColor = typedArray.getColor(
            R.styleable.RippleBackground_rb_color,
            context.resources.getColor(R.color.animation_color)
        )
        rippleStrokeWidth = typedArray.getDimension(
            R.styleable.RippleBackground_rb_strokeWidth,
            resources.getDimension(com.intuit.sdp.R.dimen._1sdp)
        )
        rippleRadius = typedArray.getDimension(
            R.styleable.RippleBackground_rb_radius,
            resources.getDimension(com.intuit.sdp.R.dimen._1sdp)
        )
        rippleDurationTime = typedArray.getInt(
            R.styleable.RippleBackground_rb_duration,
            DEFAULT_DURATION_TIME
        )
        rippleAmount = typedArray.getInt(
            R.styleable.RippleBackground_rb_rippleAmount,
            DEFAULT_RIPPLE_COUNT
        )
        rippleScale = typedArray.getFloat(
            R.styleable.RippleBackground_rb_scale,
            DEFAULT_SCALE
        )
        rippleType = typedArray.getInt(
            R.styleable.RippleBackground_rb_type,
            DEFAULT_FILL_TYPE
        )
        typedArray.recycle()

        rippleDelay = rippleDurationTime / rippleAmount

        paint = Paint().apply {
            isAntiAlias = true
            style = if (rippleType == DEFAULT_FILL_TYPE) {
                rippleStrokeWidth = 0f
                Paint.Style.FILL
            } else {
                Paint.Style.STROKE
            }
            color = rippleColor
        }

        rippleParams = LayoutParams(
            (2 * (rippleRadius + rippleStrokeWidth)).toInt(),
            (2 * (rippleRadius + rippleStrokeWidth)).toInt()
        ).apply {
            addRule(CENTER_IN_PARENT, TRUE)
        }

        animatorSet = AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
        }
        animatorList = ArrayList()

        for (i in 0 until rippleAmount) {
            val rippleView = RippleView(context)
            addView(rippleView, rippleParams)
            rippleViewList.add(rippleView)

            val scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "scaleX", 1.0f, rippleScale).apply {
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
                startDelay = (i * rippleDelay).toLong()
                duration = rippleDurationTime.toLong()
            }
            animatorList.add(scaleXAnimator)

            val scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "scaleY", 1.0f, rippleScale).apply {
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
                startDelay = (i * rippleDelay).toLong()
                duration = rippleDurationTime.toLong()
            }
            animatorList.add(scaleYAnimator)

            val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "alpha", 1.0f, 0f).apply {
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
                startDelay = (i * rippleDelay).toLong()
                duration = rippleDurationTime.toLong()
            }
            animatorList.add(alphaAnimator)
        }

        animatorSet.playTogether(animatorList)
    }

    private inner class RippleView(context: Context) : View(context) {
        init {
            visibility = INVISIBLE
        }

        override fun onDraw(canvas: Canvas) {
            val radius = (Math.min(width, height)) / 2
            canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius - rippleStrokeWidth, paint)
        }
    }

    fun startRippleAnimation() {
        if (!isRippleAnimationRunning) {
            rippleViewList.forEach { it.visibility = VISIBLE }
            animatorSet.start()
            animationRunning = true
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning) {
            animatorSet.end()
            animationRunning = false
        }
    }

    val isRippleAnimationRunning: Boolean
        get() = animationRunning
}
