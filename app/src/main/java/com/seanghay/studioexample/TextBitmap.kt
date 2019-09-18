package com.seanghay.studioexample

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.*
import androidx.annotation.RequiresApi

object TextBitmap {

    fun quoteBitmap(context: Context, text: String): Bitmap {

        val typeface = Typeface.createFromAsset(context.assets, "fonts/Bayon-Regular.ttf")

        val margin = 20

        val w = 720
        val h = 405

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        textPaint.textSize = 30f
        textPaint.color = Color.WHITE
        textPaint.typeface = typeface


        canvas.drawMultilineText(
            text = text,
            textPaint = textPaint,
            width = 600,
            x = 60f,
            y = 250f
        )

        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Canvas.drawMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        x: Float,
        y: Float,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f,
        hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE,
        justificationMode: Int = Layout.JUSTIFICATION_MODE_NONE) {

        val staticLayout = StaticLayout.Builder
            .obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setMaxLines(2)
            .setLineSpacing(spacingAdd, spacingMult)
            .setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED)
            .setJustificationMode(justificationMode)
            .build()


        save()
        translate(x, y)
        staticLayout.draw(this)
        restore()
    }
}