/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studio.gles.kenburns

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import com.seanghay.studio.utils.smoothStep

class SimpleKenburns(
  open var scaleFrom: Float = 1f,
  open var scaleTo: Float = 1f,
  open var interpolator: Interpolator = LinearInterpolator()
) : Kenburns {

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
