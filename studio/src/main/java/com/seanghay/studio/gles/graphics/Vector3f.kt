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
package com.seanghay.studio.gles.graphics

internal fun vec3(x: Float, y: Float, z: Float) = Vector3f(x, y, z)
internal fun vec3(x: Float) = Vector3f(x, x, x)
internal fun vec3(vec2: Vector2f, z: Float) = Vector3f(vec2.x, vec2.y, z)
internal fun vec3() = Vector3f()

data class Vector3f(
  var x: Float = 0f,
  var y: Float = 0f,
  var z: Float = 0f
) {

    val values get() = floatArrayOf(x, y, z)

    // TODO: Add transformations Add, Sub, Multiply, Divide

    companion object {
        fun valueOf(vararg values: Float): Vector3f {
            return Vector3f(values[0], values[1], values[2])
        }

        fun fromArray(values: FloatArray): Vector3f {
            return Vector3f(values[0], values[1], values[2])
        }
    }
}
