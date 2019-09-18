package com.seanghay.studioexample.sticker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.plus
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.seanghay.studio.utils.clamp
import com.seanghay.studioexample.sticker.gesture.MoveGestureDetector
import com.seanghay.studioexample.sticker.gesture.RotateGestureDetector
import kotlin.math.roundToInt

@SuppressLint("NewApi")
class StickerView : FrameLayout, QuoteDesigner {


    private var text: CharSequence = "Hello, World!"

    @Px
    private var textSize: Float = 24f.dip()

    @ColorInt
    private var textColor: Int = Color.BLACK

    private var textWidth: Int = width

    private var typeface: Typeface = Typeface.DEFAULT_BOLD

    private var position: PointF = PointF(0f, 0f)

    private var scaleFactor: Float = 1f

    private var textRotationAngle: Float = 0f

    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).also {
        it.color = textColor
        it.textSize = textSize
        it.typeface = typeface
        it.setShadowLayer(
            2f.dip(),
            1f.dip(),
            1f.dip(), Color.parseColor("#80000000")
        )
    }

    private var staticLayout: StaticLayout = createStaticLayout()

    private fun createStaticLayout(): StaticLayout {
        return StaticLayout.Builder.obtain(
            text, 0,
            text.length,
            textPaint,
            textWidth
        ).setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setMaxLines(3)
            .build()
    }


    private val moveGestureDetector: MoveGestureDetector =
        MoveGestureDetector(context, MoveGestureListener())

    private val scaleGestureDetector: ScaleGestureDetector =
        ScaleGestureDetector(context, ScaleGestureListner())

    private val rotateGestureDetector: RotateGestureDetector =
        RotateGestureDetector(context, RotationGestureListener())


    private val touchListener: OnTouchListener = object : OnTouchListener {
        override fun onTouch(view: View?, e: MotionEvent?): Boolean {
            return moveGestureDetector.onTouchEvent(e)
                    && scaleGestureDetector.onTouchEvent(e)
                    && rotateGestureDetector.onTouchEvent(e)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    init {
        setWillNotDraw(false)
        setOnTouchListener(touchListener)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        textWidth = measuredWidth
        updateQuote()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        position.x = position.x.clamp(measuredWidth.toFloat() / -2f, measuredWidth.toFloat() / 2f)
        position.y = position.y.clamp(0f, measuredHeight.toFloat() - staticLayout.height)

        val scale = scaleFactor.clamp(0.5f, 5f)
        val pivotX = (measuredWidth.toFloat() - position.x) / 2f
        val pivotY = (measuredHeight.toFloat() - position.y) / 2f

        canvas.withTranslation(position.x, position.y) {
            withRotation(textRotationAngle, pivotX, pivotY) {
                withScale(scale, scale, pivotX, pivotY) {
                    staticLayout.draw(canvas)
                }
            }
        }
    }


    @Px
    private fun Int.dip(): Int {
        return dipF().roundToInt()
    }

    @Px
    private fun Int.dipF(): Float {
        return toFloat().dip()
    }

    @Px
    private fun Float.dip(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
    }

    override fun getText(): CharSequence {
        return text
    }

    @Px
    override fun getTextSize(): Float {
        return textSize
    }

    override fun getTypeface(): Typeface {
        return typeface
    }

    @ColorInt
    override fun getTextColor(): Int {
        return textColor
    }

    override fun getScale(): Float {
        return scaleFactor
    }

    override fun getPosition(): PointF {
        return position
    }

    override fun setText(text: CharSequence) {
        this.text = text
        updateQuote()
    }

    override fun setTextSize(size: Float) {
        this.textSize = size
        updateQuote()
    }

    override fun setTextColor(@ColorInt textColor: Int) {
        this.textColor = textColor
        updateQuote()
    }

    override fun setTypeface(typeface: Typeface) {
        this.typeface = typeface
        updateQuote()
    }


    override fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawText(canvas)
        return bitmap
    }

    override fun getTextRotation(): Float {
        return textRotationAngle
    }

    override fun setTextRotation(angle: Float) {
        this.textRotationAngle = angle
        updateQuote()
    }

    override fun updateQuote() {
        textPaint.also {
            it.textSize = textSize
            it.color = textColor
            it.typeface = typeface
        }

        staticLayout = createStaticLayout()
        invalidate()
    }

    override fun setPosition(position: PointF) {
        this.position = position
        updateQuote()
    }

    override fun setScale(scaleFactor: Float) {
        this.scaleFactor = scaleFactor
        updateQuote()
    }

    private inner class RotationGestureListener :
        RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector?): Boolean {
            if (detector == null) return super.onRotate(detector)
            textRotationAngle -= detector.rotationDegreesDelta
            invalidate()
            return true
        }
    }

    private inner class ScaleGestureListner : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            if (detector == null) return super.onScale(detector)
            scaleFactor += detector.scaleFactor - 1f
            invalidate()
            return true
        }
    }

    private inner class MoveGestureListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector?): Boolean {
            if (detector == null) return super.onMove(detector)
            position += detector.focusDelta
            invalidate()
            return true
        }
    }
}