package com.seanghay.studioexample

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.Px
import kotlin.math.roundToInt


@Px
fun Int.dip(resources: Resources): Int {
    return dipF(resources).roundToInt()
}

@Px
fun Int.dipF(resources: Resources): Float {
    return toFloat().dip(resources)
}

@Px
fun Float.dip(resources: Resources): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        resources.displayMetrics
    )
}