package com.seanghay.studio.gles.kenburns

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import com.seanghay.studio.utils.smoothStep

class SimpleKenburns(
    open var scaleFrom: Float = 1f,
    open var scaleTo: Float = 1f,
    open var interpolator: Interpolator = LinearInterpolator()
): Kenburns {

    override fun flip() {
        val tmp = scaleFrom
        scaleFrom = scaleTo
        scaleTo = tmp
    }

    override fun getValue(@FloatRange(from = 0.0, to = 1.0) progress: Float): Float {
        val interpolation = interpolator.getInterpolation(progress).smoothStep(0f, 1f)
        // Scale down to a value
        return if (scaleFrom >= scaleTo) scaleTo + (scaleFrom - scaleTo) * (1f - interpolation)
        // Scale up to a value
        else scaleFrom + (scaleTo - scaleFrom) * interpolation
    }
}
